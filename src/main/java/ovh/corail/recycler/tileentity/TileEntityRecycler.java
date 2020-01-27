package ovh.corail.recycler.tileentity;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.INameable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import ovh.corail.recycler.ConfigRecycler;
import ovh.corail.recycler.block.BlockRecycler;
import ovh.corail.recycler.capability.RecyclerWorkingStackHandler;
import ovh.corail.recycler.registry.ModSounds;
import ovh.corail.recycler.registry.ModTileEntityTypes;
import ovh.corail.recycler.util.Helper;
import ovh.corail.recycler.util.LangKey;
import ovh.corail.recycler.util.RecyclingManager;
import ovh.corail.recycler.util.RecyclingRecipe;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class TileEntityRecycler extends TileEntity implements ITickableTileEntity, INameable {
    private final ItemStackHandler inventInput = new ItemStackHandler(18);
    private final ItemStackHandler inventWorking = new RecyclerWorkingStackHandler();
    private final ItemStackHandler inventOutput = new ItemStackHandler(18);
    private final ItemStackHandler inventVisual = new ItemStackHandler(9); // not serialized
    private final EnergyStorage energyStorage = new EnergyStorage(10000, 20, 10);

    private String customName;
    private boolean isWorking = false;
    private int countTicks = 0, progress = 0, inputMax = 0, cantRecycleTicks = 0;

    public TileEntityRecycler() {
        super(ModTileEntityTypes.RECYCLER);
    }

    public ItemStackHandler getInventoryInput() {
        return this.inventInput;
    }

    public ItemStackHandler getInventoryWorking() {
        return this.inventWorking;
    }

    public ItemStackHandler getInventoryOutput() {
        return this.inventOutput;
    }

    public ItemStackHandler getInventoryVisual() {
        return this.inventVisual;
    }

    public boolean isOutputEmpty() {
        return IntStream.range(0, this.inventOutput.getSlots()).allMatch(slot -> this.inventOutput.getStackInSlot(slot).isEmpty());
    }

    private void transferSlotWorking() {
        ItemHandlerHelper.insertItemStacked(inventOutput, inventWorking.extractItem(0, inventWorking.getStackInSlot(0).getCount(), false), false);
    }

    private boolean hasSpaceInInventory(NonNullList<ItemStack> itemsList, boolean simulate) {
        // TODO clean this method
        // list of empty slots
        List<Integer> emptySlots = IntStream.range(0, this.inventOutput.getSlots()).filter(slot -> this.inventOutput.getStackInSlot(slot).isEmpty()).boxed().collect(Collectors.toList());
        // simulate : enough empty slots
        if (simulate && emptySlots.size() >= itemsList.size()) {
            return true;
        }
        // simulate : try to fill at least minCount stacks depending of empty slots
        int minCount = simulate ? itemsList.size() - emptySlots.size() : 0;
        int space, maxSize, add, left, emptySlot;
        ItemStack stackCopy;
        // each stack of the input List
        ItemStack stackIn, stackOut;
        for (int i = 0; i < itemsList.size(); i++) {
            stackIn = itemsList.get(i);
            // input stack empty or max stacksize
            if (stackIn.isEmpty()) {
                if (simulate) {
                    minCount--;
                }
                continue;
            }
            if (stackIn.getCount() == stackIn.getMaxStackSize()) {
                continue;
            }
            // try to fill same stacks not full
            left = stackIn.getCount();
            maxSize = stackIn.getMaxStackSize();
            // each stack of the output List
            for (int slot = 0; slot < this.inventOutput.getSlots(); slot++) {
                stackOut = this.inventOutput.getStackInSlot(slot);
                // output stack empty or max stacksize
                if (stackOut.isEmpty() || stackOut.getCount() == stackOut.getMaxStackSize()) {
                    continue;
                }
                // stacks equal and same meta/nbt
                if (Helper.areItemEqual(stackIn, stackOut)) {
                    space = maxSize - stackOut.getCount();
                    add = Math.min(space, left);
                    if (add > 0) {
                        stackOut.grow(add);
                        left -= add;
                        if (left <= 0) {
                            break;
                        }
                    }
                }
            }
            // stack completely filled
            if (left <= 0 && simulate) {
                minCount--;
            }
            // place the stack left in an empty stack
            if (left > 0) {
                if (emptySlots.size() > 0) {
                    emptySlot = emptySlots.get(0);
                    emptySlots.remove(0);
                    stackCopy = stackIn.copy();
                    stackCopy.setCount(left);
                    this.inventOutput.setStackInSlot(emptySlot, stackCopy);
                    if (simulate) {
                        minCount++;
                    }
                    // no empty stack
                } else {
                    return false;
                }
            }
            if (simulate && minCount <= 0) {
                return true;
            }
            itemsList.set(i, ItemStack.EMPTY);
        }
        // add the fullstack left in input
        for (ItemStack stack : itemsList) {
            if (!stack.isEmpty() && emptySlots.size() > 0) {
                emptySlot = emptySlots.get(0);
                emptySlots.remove(0);
                this.inventOutput.setStackInSlot(emptySlot, stack.copy());
            }
        }
        // overwrite the output slots
        if (!simulate) {
            IntStream.range(0, this.inventOutput.getSlots()).forEach(slot -> {
                this.inventOutput.setStackInSlot(slot, this.inventOutput.getStackInSlot(slot));
            });
        }
        return true;
    }

    public boolean recycle(@Nullable ServerPlayerEntity player) {
        assert this.world != null;
        //TODO clean this method
        RecyclingManager recyclingManager = RecyclingManager.instance;
        final ItemStack workingStack = inventWorking.getStackInSlot(0);
        final ItemStack diskStack = inventWorking.getStackInSlot(1);
        if (workingStack.isEmpty() || diskStack.isEmpty()) {
            return false;
        }
        // find the recipe
        RecyclingRecipe recipe = recyclingManager.getRecipe(workingStack);
        // no recipe
        if (recipe == null) {
            transferSlotWorking();
            return false;
        }
        // number of times that the recipe can be used with this stack
        int nb_input = workingStack.getCount() / recipe.getItemRecipe().getCount();
        // not enough stacksize for at least one recipe
        if (nb_input == 0) {
            return false;
        }
        // by unit in auto recycle
        if (isWorking) {
            nb_input = 1;
        }
        // max uses of the disk
        int maxDiskUse = (diskStack.getMaxDamage() - diskStack.getDamage()) / 10;
        if (maxDiskUse < nb_input) {
            nb_input = maxDiskUse;
        }
        // calculation of the result
        NonNullList<ItemStack> itemsList = recyclingManager.getResultStack(workingStack, nb_input);
        // simule the space needed
        if (!hasSpaceInInventory(itemsList, true)) {
            LangKey.MESSAGE_NOT_ENOUGH_OUTPUT_SLOTS.sendMessage(player);
            return false;
        }
        // Loss chance
        int loss = 0;
        if (ConfigRecycler.general.chance_loss.get() > 0) {
            // TODO use probabilities
            for (int i = 0; i < nb_input; i++) {
                if (Helper.getRandom(1, 100) <= ConfigRecycler.general.chance_loss.get()) {
                    loss++;
                }
            }
            if (loss > 0) {
                LangKey.MESSAGE_LOSS.sendMessage(player);
            }
        }
        NonNullList<ItemStack> stackList;
        if (nb_input - loss > 0) {
            stackList = recyclingManager.getResultStack(workingStack, nb_input - loss);
        } else {
            stackList = NonNullList.create();
        }
        if (loss > 0) {
            NonNullList<ItemStack> halfstackList = recyclingManager.getResultStack(workingStack, loss, true);
            stackList.addAll(halfstackList);
        }
        // transfer stacks
        hasSpaceInInventory(stackList, false);
        // consume the working slot
        this.inventWorking.getStackInSlot(0).shrink(nb_input * recipe.getItemRecipe().getCount());
        // damage the disk
        int diskDamage = 10 * nb_input;
        if (diskStack.getDamage() + diskDamage >= diskStack.getMaxDamage()) {
            LangKey.MESSAGE_BROKEN_DISK.sendMessage(player);
            this.inventWorking.setStackInSlot(1, ItemStack.EMPTY);
        } else {
            this.inventWorking.getStackInSlot(1).setDamage(diskStack.getDamage() + diskDamage);
        }
        // play recycler sound
        ModSounds.playSoundAllAround(ModSounds.RECYCLER, SoundCategory.BLOCKS, this.world, this.pos, 0.5f, 0.5f + this.world.rand.nextFloat() * 0.5f);
        return true;
    }

    @Override
    public void tick() {
        if (this.world == null || this.world.isRemote) {
            return;
        }
        // autofill the working slots
        if (this.world.getGameTime() % 10 == 0) {
            ItemStack stackToRecycle = this.inventWorking.getStackInSlot(0);
            boolean requireRecipeUpdate = false;
            if (stackToRecycle.isEmpty()) {
                // transfer the fullstack if the working slot is empty
                int slotId = IntStream.range(0, this.inventInput.getSlots()).filter(slot -> {
                    ItemStack stackInSlot = this.inventInput.getStackInSlot(slot);
                    return !stackInSlot.isEmpty() && this.inventWorking.isItemValid(0, stackInSlot);
                }).findFirst().orElse(-1);
                if (slotId > 0) {
                    this.inventWorking.insertItem(0, this.inventInput.extractItem(slotId, this.inventInput.getStackInSlot(slotId).getCount(), false), false);
                    requireRecipeUpdate = true;
                }
            } else if (stackToRecycle.isStackable() && stackToRecycle.getCount() < stackToRecycle.getMaxStackSize()) {
                // transfer slowly to fill the working slot
                int slotId = IntStream.range(0, this.inventInput.getSlots()).filter(slot -> Helper.areItemEqual(stackToRecycle, this.inventInput.getStackInSlot(slot))).findFirst().orElse(-1);
                if (slotId > 0) {
                    this.inventWorking.insertItem(0, this.inventInput.extractItem(slotId, 1, false), false);
                    requireRecipeUpdate = true;
                }
            }
            if (this.inventWorking.getStackInSlot(1).isEmpty()) {
                // replace disk if needed
                int slotId = IntStream.range(0, this.inventInput.getSlots()).filter(slot -> this.inventWorking.isItemValid(1, this.inventInput.getStackInSlot(slot))).findFirst().orElse(-1);
                if (slotId > 0) {
                    this.inventWorking.insertItem(1, this.inventInput.extractItem(slotId, 1, false), false);
                    requireRecipeUpdate = true;
                }
            }
            if (requireRecipeUpdate) {
                updateRecyclingRecipe();
            }
        }
        // TODO cache last recipe
        RecyclingRecipe baseRecipe = RecyclingManager.instance.getRecipe(this.inventWorking.getStackInSlot(0));
        this.inputMax = baseRecipe != null ? this.inventWorking.getStackInSlot(0).getCount() / baseRecipe.getItemRecipe().getCount() : 0;
        if (this.inputMax > 0) {
            this.inputMax = Math.min(this.inputMax, (this.inventWorking.getStackInSlot(1).getMaxDamage() - this.inventWorking.getStackInSlot(1).getDamage()) / 10);
        }
        if (!this.isWorking) {
            return;
        }
        if (!ConfigRecycler.general.allow_automation.get()) {
            updateWorking(false);
            return;
        }
        if (this.energyStorage.getEnergyStored() >= 10) {
            this.energyStorage.extractEnergy(10, false);
            this.countTicks -= 2;
        } else {
            this.countTicks--;
        }
        int maxTicks = ConfigRecycler.general.time_to_recycle.get();
        // no recipe, or not enough stacksize
        if (this.inputMax < 1) {
            this.cantRecycleTicks++;
            this.countTicks = maxTicks;
        }
        // stop working if it can't recycle since 2 seconds
        if (this.cantRecycleTicks > 40) {
            if (!this.inventWorking.getStackInSlot(0).isEmpty()) {
                transferSlotWorking();
            }
            updateWorking(false);
            this.cantRecycleTicks = 0;
            this.countTicks = maxTicks;
        }
        // try to recycle
        if (this.countTicks <= 0) {
            if (!recycle(null)) {
                this.cantRecycleTicks++;
            }
            this.countTicks = maxTicks;
            // play working sound
        } else if (cantRecycleTicks <= 1 && countTicks % 15 == 0) {
            ModSounds.playSoundAllAround(ModSounds.RECYCLER_WORKING, SoundCategory.BLOCKS, world, pos, 0.5f, 0.5f + world.rand.nextFloat() * 0.5f);
            for (int i = 0; i < 4; i++) {
                world.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + Helper.random.nextDouble(), (double) pos.getY() + Helper.random.nextDouble(), (double) pos.getZ() + Helper.random.nextDouble(), 0d, 0d, 0d);
            }
        }
        this.progress = (maxTicks - this.countTicks) * 100 / maxTicks;
    }

    public void updateRecyclingRecipe() {
        RecyclingRecipe recipe = RecyclingManager.instance.getRecipe(getInventoryWorking().getStackInSlot(0));
        boolean hasRecipe = recipe != null;
        NonNullList<ItemStack> currentRecipe = hasRecipe ? RecyclingManager.instance.getResultStack(getInventoryWorking().getStackInSlot(0), 1) : NonNullList.create();
        // autofill the visual slots if needed
        int slotId = 0;
        if (hasRecipe) {
            while (slotId < Math.min(currentRecipe.size(), this.inventVisual.getSlots())) {
                if (currentRecipe.get(slotId).getItem() != this.inventVisual.getStackInSlot(slotId).getItem()) {
                    this.inventVisual.setStackInSlot(slotId, currentRecipe.get(slotId));
                }
                slotId++;
            }
        }
        while (slotId < this.inventVisual.getSlots()) {
            if (!this.inventVisual.getStackInSlot(slotId).isEmpty()) {
                this.inventVisual.setStackInSlot(slotId, ItemStack.EMPTY);
            }
            slotId++;
        }
        this.progress = 0;
        this.inputMax = hasRecipe ? this.inventWorking.getStackInSlot(0).getCount() / recipe.getItemRecipe().getCount() : 0;
        if (this.inputMax > 0) {
            this.inputMax = Math.min(this.inputMax, (this.inventWorking.getStackInSlot(1).getMaxDamage() - this.inventWorking.getStackInSlot(1).getDamage()) / 10);
        }
    }

    public void switchWorking() {
        updateWorking(!this.isWorking);
    }

    public void updateWorking(boolean isWorking) {
        setProgress(0);
        if (isWorking != this.isWorking) {
            this.isWorking = isWorking;
            this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(BlockRecycler.ENABLED, isWorking));
        }
    }

    public void setProgress(int progress) {
        if (progress == 0) {
            this.countTicks = ConfigRecycler.general.time_to_recycle.get();
        }
        this.progress = progress;
    }

    @Override
    public ITextComponent getName() {
        return new TranslationTextComponent(MOD_ID + "block.recycler.name");
    }

    @Override
    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? new TranslationTextComponent(customName) : getName();
    }

    @Override
    @Nullable
    public ITextComponent getCustomName() {
        return hasCustomName() ? new TranslationTextComponent(this.customName) : null;
    }

    public int getMaxEnergy() {
        return energyStorage.getMaxEnergyStored();
    }

    public int getEnergy() {
        return energyStorage.getEnergyStored();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == Direction.DOWN) {
                return LazyOptional.of(() -> (T) inventOutput);
            } else {
                return LazyOptional.of(() -> (T) inventInput);
            }
        } else if (cap == CapabilityEnergy.ENERGY) {
            return LazyOptional.of(() -> (T) energyStorage);
        }
        return super.getCapability(cap, side);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        writeShareDatas(compound);
        compound.put("invent_input", inventInput.serializeNBT());
        compound.put("invent_working", inventWorking.serializeNBT());
        compound.put("invent_output", inventOutput.serializeNBT());
        return compound;
    }

    private CompoundNBT writeShareDatas(CompoundNBT compound) {
        super.write(compound);
        if (hasCustomName()) {
            compound.putString("custom_name", customName);
        }
        compound.putInt("countTicks", countTicks);
        compound.putBoolean("isWorking", isWorking);
        compound.putInt("progress", progress);
        compound.putInt("cantRecycleTicks", cantRecycleTicks);
        INBT nbt = CapabilityEnergy.ENERGY.writeNBT(energyStorage, null);
        if (nbt != null) {
            compound.put("energy", nbt);
        }
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        inventInput.deserializeNBT(compound.getCompound("invent_input"));
        inventWorking.deserializeNBT(compound.getCompound("invent_working"));
        inventOutput.deserializeNBT(compound.getCompound("invent_output"));
        if (compound.contains("custom_name", Constants.NBT.TAG_STRING)) {
            customName = compound.getString("CustomName");
        }
        countTicks = compound.getInt("countTicks");
        isWorking = compound.getBoolean("isWorking");
        progress = compound.getInt("progress");
        cantRecycleTicks = compound.getInt("cantRecycleTicks");
        CapabilityEnergy.ENERGY.readNBT(energyStorage, null, compound.get("energy"));
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return writeShareDatas(new CompoundNBT());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 1, write(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        read(packet.getNbtCompound());
    }

    public class TrackedData implements IIntArray {
        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return isWorking ? 1 : 0;
                case 1:
                    return progress;
                case 2:
                    return inputMax;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    isWorking = value > 0;
                    break;
                case 1:
                    progress = value;
                    break;
                case 2:
                    inputMax = value;
                    break;
            }
        }

        @Override
        public int size() {
            return 3;
        }
    }
}
