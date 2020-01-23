package ovh.corail.recycler.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class SlotWorking extends SlotRecycler {
    private final ContainerRecycler container;

    SlotWorking(ContainerRecycler container, int slotId, int x, int y, Predicate<ItemStack> predic) {
        super(container.getRecycler().getInventoryWorking(), slotId, x, y, predic);
        this.container = container;
    }

    @Override
    public void onSlotChanged() {
        World world = this.container.getRecycler().getWorld();
        if (world != null && !world.isRemote) {
            container.getRecycler().updateRecyclingRecipe();
            container.detectAndSendChanges();
        }
    }
}
