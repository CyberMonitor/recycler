package ovh.corail.recycler.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ovh.corail.recycler.recipe.RecyclingRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Helper {
    public final static Random random = new Random();

    public static int getRandom(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    public static boolean isValidPlayer(@Nullable PlayerEntity player) {
        return player != null && isNotFakePlayer(player);
    }

    public static boolean isValidServerPlayer(@Nullable PlayerEntity player) {
        return player != null && !player.world.isRemote && isNotFakePlayer(player);
    }

    private static boolean isNotFakePlayer(LivingEntity entity) {
        return !(entity instanceof FakePlayer);
    }

    public static boolean atInterval(long ticksExisted, int tick) {
        return ticksExisted > 0 && ticksExisted % tick == 0;
    }

    public static boolean atInterval(World world, int tick) {
        return atInterval(world.getGameTime(), tick);
    }

    public static boolean areItemEqual(ItemStack s1, ItemStack s2) {
        return !s1.isEmpty() && s1.getItem() == s2.getItem();
    }

    public static boolean isValidRecipe(RecyclingRecipe recipe) {
        return !recipe.getResult().isEmpty() && !recipe.getItemRecipe().isEmpty();
    }

    public static boolean isValidRecipe(@Nullable IRecipe recipe) {
        return recipe != null && !recipe.getIngredients().isEmpty() && !recipe.getRecipeOutput().isEmpty();
    }

    public static NonNullList<ItemStack> mergeStackInList(NonNullList<ItemStack> list) {
        for (int i = 0 ; i < list.size() ; i++) {
            ItemStack currentStack = list.get(i);
            if (!currentStack.isEmpty() && currentStack.isStackable() && currentStack.getCount() < currentStack.getMaxStackSize()) {
                for (int j = i + 1; j < list.size(); j++) {
                    ItemStack lookStack = list.get(j);
                    if (!lookStack.isEmpty() && areItemEqual(currentStack, lookStack)) {
                        int add = Math.min(lookStack.getCount(), currentStack.getMaxStackSize() - currentStack.getCount());
                        if (add > 0) {
                            lookStack.shrink(add);
                            currentStack.grow(add);
                            if (currentStack.getCount() == currentStack.getMaxStackSize()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        list.removeIf(ItemStack::isEmpty);
        return list;
    }

    public static boolean canInsertInInventory(IItemHandlerModifiable inventory, NonNullList<ItemStack> items) {
        // list of stacksize of the inventory
        List<Integer> slotSizes = IntStream.range(0, inventory.getSlots()).map(slotId -> {
            ItemStack stack = inventory.getStackInSlot(slotId);
            return stack.isEmpty() ? 0 : stack.getCount();
        }).boxed().collect(Collectors.toList());
        int emptySlots = (int) slotSizes.stream().filter(i -> i == 0).count();
        // simulate : enough empty slots
        if (emptySlots >= items.size()) {
            return true;
        }
        // simulate : try to fill at least minCount stacks depending of empty slots
        int minCount = items.size() - emptySlots;
        // each itemstack to insert in the inventory
        for (ItemStack stackIn : items) {
            if (minCount <= 0) {
                return true;
            }
            // skip empty itemstack
            if (stackIn.isEmpty()) {
                minCount--;
                continue;
            }
            if (!stackIn.isStackable()) {
                continue;
            }
            int left = stackIn.getCount();
            // try to fill same itemstacks in inventory
            for (int slotId = 0; slotId < inventory.getSlots(); slotId++) {
                ItemStack currentStack = inventory.getStackInSlot(slotId);
                // look for similar item not full stacksize
                int stacksize;
                if (Helper.areItemEqual(stackIn, currentStack) && (stacksize = slotSizes.get(slotId)) < currentStack.getMaxStackSize()) {
                    int add = Math.min(left, currentStack.getMaxStackSize() - stacksize);
                    if (add > 0) {
                        slotSizes.set(slotId, stacksize + add);
                        left -= add;
                        // stack completely filled
                        if (left <= 0) {
                            minCount--;
                            break;
                        }
                    }
                }
            }
        }
        return minCount <= 0;
    }

    @SuppressWarnings("all")
    @Nonnull
    public static <T extends IForgeRegistryEntry> T getDefaultNotNull() {
        return null;
    }
}
