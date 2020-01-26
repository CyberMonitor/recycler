package ovh.corail.recycler.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import ovh.corail.recycler.registry.ModTags;

import java.util.function.Predicate;

public class RecyclerWorkingStackHandler extends ItemStackHandler {
    public enum EnumSlot {
        ITEM(p -> !ModTags.Items.disks.contains(p.getItem())),
        DISK(p -> ModTags.Items.disks.contains(p.getItem()));
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
    public boolean isItemValid(int slot, ItemStack stack) {
        return EnumSlot.isItemValid(slot, stack);
    }
}
