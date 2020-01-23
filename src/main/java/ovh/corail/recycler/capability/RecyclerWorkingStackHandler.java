package ovh.corail.recycler.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import ovh.corail.recycler.registry.ModItems;

import java.util.function.Predicate;

public class RecyclerWorkingStackHandler extends ItemStackHandler {
    public enum EnumSlot {
        ITEM(p -> p.getItem() != ModItems.diamond_disk),
        DISK(p -> p.getItem() == ModItems.diamond_disk);
        public final Predicate<ItemStack> predic;

        EnumSlot(Predicate<ItemStack> predic) {
            this.predic = predic;
        }

        public static boolean isItemValid(int slot, ItemStack stack) {
            return (slot == 0 || slot == 1) && EnumSlot.values()[slot].predic.test(stack);
        }
    }

    public RecyclerWorkingStackHandler() {
        super(EnumSlot.values().length);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return EnumSlot.isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
    }
}
