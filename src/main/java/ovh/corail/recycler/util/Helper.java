package ovh.corail.recycler.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

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

    public static boolean areItemEqual(ItemStack s1, ItemStack s2) {
        return !s1.isEmpty() && s1.getItem() == s2.getItem();
    }

    public static NonNullList<ItemStack> mergeStackInList(NonNullList<ItemStack> itemStackList) {
        NonNullList<ItemStack> outputList = NonNullList.create();
        for (ItemStack stack : itemStackList) {
            ItemStack currentStack = stack.copy();
            // looking for existing same stack
            for (ItemStack lookStack : outputList) {
                if (currentStack.isEmpty() || !currentStack.isStackable()) {
                    break;
                }
                if (!areItemEqual(currentStack, lookStack) || lookStack.isEmpty() || lookStack.getCount() == lookStack.getMaxStackSize()) {
                    continue;
                }
                int add = Math.min(currentStack.getCount(), lookStack.getMaxStackSize() - lookStack.getCount());
                if (add > 0) {
                    currentStack.shrink(add);
                    lookStack.grow(add);
                }
            }
            if (!currentStack.isEmpty()) {
                outputList.add(currentStack);
            }
        }
        return outputList;
    }

    @SuppressWarnings("all")
    @Nonnull
    public static <T extends IForgeRegistryEntry> T getDefaultNotNull() {
        return null;
    }
}
