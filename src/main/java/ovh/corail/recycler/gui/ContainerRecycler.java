package ovh.corail.recycler.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IIntArray;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.wrapper.InvWrapper;
import ovh.corail.recycler.capability.RecyclerWorkingStackHandler.EnumSlot;
import ovh.corail.recycler.registry.ModContainers;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

public class ContainerRecycler extends Container {
    private final TileEntityRecycler recycler;
    private final IIntArray recyclerData;

    protected ContainerRecycler(ContainerType<? extends ContainerRecycler> containerType, int windowId, PlayerInventory playerInventory, TileEntityRecycler recycler) {
        super(containerType, windowId);
        this.recycler = recycler;
        trackIntArray(this.recyclerData = recycler.new TrackedData());
        addSlots(playerInventory);
        this.recycler.updateRecyclingRecipe();
    }

    public ContainerRecycler(int windowId, PlayerInventory playerInventory, TileEntityRecycler recycler) {
        this(ModContainers.RECYCLER, windowId, playerInventory, recycler);
    }

    public ContainerRecycler(int windowId, PlayerInventory playerInventory, BlockPos pos) {
        this(windowId, playerInventory, (TileEntityRecycler) playerInventory.player.world.getTileEntity(pos));
    }

    public ContainerRecycler(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
        this(windowId, playerInventory, data.readBlockPos());
    }

    public boolean isWorking() {
        return this.recyclerData.get(0) == 1;
    }

    public int getProgress() {
        return this.recyclerData.get(1);
    }

    public int getInputMax() {
        return this.recyclerData.get(2);
    }

    public int getEnergy() {
        return this.recyclerData.get(3);
    }

    public BlockPos getPosition() {
        return this.recycler.getPos();
    }

    public TileEntityRecycler getRecycler() {
        return this.recycler;
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < 38) {
                if (!mergeItemStack(itemstack1, 38, 73, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < 74 && !mergeItemStack(itemstack1, 2, 19, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
        }
        return itemstack;
    }

    private void addSlots(PlayerInventory playerInventory) {
        int x, y;
        // input slot 0
        addSlot(new SlotWorking(this, 0, 81, 69, EnumSlot.ITEM.predic));
        // disk slot 1
        addSlot(new SlotWorking(this, 1, 81, 91, EnumSlot.DISK.predic));
        // input slots 2-19
        for (y = 0; y < 6; y++) {
            for (x = 0; x < 3; x++) {
                addSlot(new SlotRecycler(recycler.getInventoryInput(), x + (y * 3), (x * 18) + 7, (y * 18) + 8));
            }
        }
        // output slots 20-37
        for (y = 0; y < 6; y++) {
            for (x = 0; x < 3; x++) {
                addSlot(new SlotRecycler(recycler.getInventoryOutput(), x + (y * 3), (x * 18) + 173, (y * 18) + 8));
            }
        }
        InvWrapper inventPlayer = new InvWrapper(playerInventory);
        // player slots 38-73
        for (y = 0; y < 4; y++) {
            for (x = 0; x < 9; x++) {
                addSlot(new SlotRecycler(inventPlayer, x + (y * 9), (x * 18) + 7, y == 0 ? 178 : (y * 18) + 102));
            }
        }
        // visual slots 74-82
        for (y = 0; y < 3; y++) {
            for (x = 0; x < 3; x++) {
                addSlot(new SlotRecycler(recycler.getInventoryVisual(), x + (y * 3), (x * 18) + 115, (y * 18) + 8, p -> false, false));
            }
        }
    }
}
