package ovh.corail.recycler.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ButtonRecyclingBook extends Button {
    private final boolean isForward;
    private static final ResourceLocation TEXTURE_BOOK = new ResourceLocation("textures/gui/book.png");

    public ButtonRecyclingBook(boolean isForward, int x, int y, IPressable pressable) {
        super(x, y, 23, 13, "", pressable);
        this.isForward = isForward;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color4f(1f, 1f, 1f, 1f);
        Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE_BOOK);
        blit(this.x, this.y, isHovered() ? 23 : 0, 192 + (this.isForward ? 0 : 13), 23, 13);
    }
}
