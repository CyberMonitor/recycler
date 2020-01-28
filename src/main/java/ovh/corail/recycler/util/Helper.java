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

    @SuppressWarnings("all")
    @Nonnull
    public static <T extends IForgeRegistryEntry> T getDefaultNotNull() {
        return null;
    }
}
