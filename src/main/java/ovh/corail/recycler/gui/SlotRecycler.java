package ovh.corail.recycler.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.function.Predicate;

public class SlotRecycler extends SlotItemHandler {
    private final Predicate<ItemStack> predic;
    private final boolean canTake;
    int timeInUse = 0;

    SlotRecycler(IItemHandler handler, int slotId, int x, int y) {
        this(handler, slotId, x, y, p -> true);
    }

    SlotRecycler(IItemHandler handler, int slotId, int x, int y, Predicate<ItemStack> predic) {
        this(handler, slotId, x, y, predic, true);
    }

    SlotRecycler(IItemHandler handler, int slotId, int x, int y, Predicate<ItemStack> predic, boolean canTake) {
        super(handler, slotId, x, y);
        this.predic = predic;
        this.canTake = canTake;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return this.predic.test(stack) && super.isItemValid(stack);
    }

    @Override
    public boolean canTakeStack(PlayerEntity player) {
        return this.canTake && super.canTakeStack(player);
    }

    @Override
    public void onSlotChanged() {
        timeInUse = 40;
        super.onSlotChanged();
    }
}
