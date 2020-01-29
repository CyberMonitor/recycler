package ovh.corail.recycler.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("FieldCanBeLocal")
public class ButtonRecycler extends Button {
    private final int textureX = 0;
    private final int textureY = 214;
    private final int buttonWidth = 74;
    private static final ResourceLocation TEXTURE_RECYCLER = new ResourceLocation(MOD_ID + ":textures/gui/vanilla_recycler.png");

    ButtonRecycler(int x, int y, int width, int height, String title, IPressable pressable) {
        super(x, y, width, height, title, pressable);
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        mc.getTextureManager().bindTexture(TEXTURE_RECYCLER);
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int readButton = (!this.active ? 28 : (getYImage(isHovered()) == 2 ? 14 : 0));
        int halfWidth = this.width / 2;
        blit(this.x, this.y, this.textureX, this.textureY + readButton, halfWidth, this.height);
        blit(this.x + halfWidth, this.y, this.textureX + this.buttonWidth - halfWidth, this.textureY + readButton, halfWidth, this.height);
        renderBg(mc, mouseX, mouseY);
        int j = getFGColor();
        drawCenteredString(mc.fontRenderer, getMessage(), this.x + halfWidth, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255f) << 24);
    }
}
