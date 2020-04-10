package ovh.corail.recycler.compatibility;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ovh.corail.recycler.gui.ContainerRecycler;
import ovh.corail.recycler.gui.GuiRecycler;
import vazkii.quark.api.IQuarkButtonIgnored;

public class CompatibilityQuark {
    @OnlyIn(Dist.CLIENT)
    public static class ButtonIgnoredScreen extends GuiRecycler implements IQuarkButtonIgnored {
        public ButtonIgnoredScreen(ContainerRecycler recyclerContainer, PlayerInventory playerInventory, ITextComponent title) {
            super(recyclerContainer, playerInventory, title);
        }
    }
}
