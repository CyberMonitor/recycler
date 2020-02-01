package ovh.corail.recycler.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import ovh.corail.recycler.ConfigRecycler;
import ovh.corail.recycler.network.PacketHandler;
import ovh.corail.recycler.network.ServerRecyclingBookMessage;
import ovh.corail.recycler.network.ServerRecyclingBookMessage.RecyclingBookAction;
import ovh.corail.recycler.util.Helper;
import ovh.corail.recycler.util.LangKey;

import java.util.HashMap;
import java.util.Map;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class GuiRecyclingBook extends ContainerScreen<ContainerRecyclingBook> {
    private TextFieldWidget searchBox;
    private String lastText = "";
    private static final ResourceLocation TEXTURE_VANILLA_RECYCLER = new ResourceLocation(MOD_ID + ":textures/gui/vanilla_recycler.png");
    private static final ResourceLocation TEXTURE_RECYCLING_BOOK = new ResourceLocation(MOD_ID + ":textures/gui/book.png");
    private final int textColor = 0xd4af37;
    private Map<Integer, Rectangle2d> recipeFlags = new HashMap<>();

    public GuiRecyclingBook(ContainerRecyclingBook container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.xSize = 250;
        this.ySize = 150;
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);
        addButton(new ButtonRecyclingBook(false, this.guiLeft + 20, this.guiTop + 135, pressable -> {
            if (this.container.getPageNum() > 0) {
                PacketHandler.sendToServer(new ServerRecyclingBookMessage(RecyclingBookAction.CHANGE_PAGE, this.container.getPageNum() - 1));
            }
        }));
        addButton(new ButtonRecyclingBook(true, this.guiLeft + 208, this.guiTop + 135, pressable -> {
            if (this.container.getPageNum() < this.container.getPageMax()) {
                PacketHandler.sendToServer(new ServerRecyclingBookMessage(RecyclingBookAction.CHANGE_PAGE, this.container.getPageNum() + 1));
            }
        }));
        this.recipeFlags.clear();
        container.inventorySlots.stream().filter(slot -> Helper.atInterval(slot.getSlotIndex(), 10, false)).forEach(slot -> {
            int startPosX = guiLeft + slot.xPos;
            int startPosY = guiTop + slot.yPos;
            int slotId = slot.getSlotIndex() / 10;
            this.recipeFlags.put(slotId * 3, new Rectangle2d(startPosX, startPosY + 16, 5, 5));
            this.recipeFlags.put(slotId * 3 + 1, new Rectangle2d(startPosX + 5, startPosY + 16, 5, 5));
            this.recipeFlags.put(slotId * 3 + 2, new Rectangle2d(startPosX + 10, startPosY + 16, 5, 5));
        });
        addButton(this.searchBox = new TextFieldWidget(this.font, (this.width / 2) - 32, this.guiTop + 139, 64, 12, "search"));
        configureSearchBox();
    }

    private void configureSearchBox() {
        this.searchBox.setEnableBackgroundDrawing(true);
        this.searchBox.setFocused2(true);
        this.searchBox.setMaxStringLength(20);
        this.searchBox.setText("");
        this.searchBox.setCanLoseFocus(false);
    }

    private void updateButton(int buttonNum, boolean state) {
        Widget button = this.buttons.get(buttonNum);
        button.active = button.visible = state;
    }

    private void updateButtons() {
        if (this.container.getPageNum() <= 0) {
            this.container.setPageNum(0);
            updateButton(0, false);
            updateButton(1, this.container.getPageMax() > 0);
        } else if (this.container.getPageNum() >= this.container.getPageMax()) {
            this.container.setPageNum(this.container.getPageMax());
            updateButton(0, true);
            updateButton(1, false);
        } else {
            updateButton(0, true);
            updateButton(1, true);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        if (keyCode == 256) {
            return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
        } else if (keyCode == 259) {
            String text = this.searchBox.getText();
            if (text.length() < 2) {
                this.searchBox.setText("");
            } else {
                this.searchBox.setText(text.substring(0, text.length() - 1));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        if (this.searchBox.charTyped(Character.toLowerCase(p_charTyped_1_), p_charTyped_2_)) {
            return true;
        }
        return super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    public void tick() {
        super.tick();
        ClientWorld world = getMinecraft().world;
        if (world != null && Helper.atInterval(world.getGameTime(), 10)) {
            String currentText = this.searchBox.getText();
            if (!this.lastText.equals(currentText)) {
                this.lastText = currentText;
                PacketHandler.sendToServer(new ServerRecyclingBookMessage(RecyclingBookAction.SEARCH_TEXT, this.searchBox.getText().toLowerCase()));
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        updateButtons();
        renderBackground();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        // recycling book background
        getMinecraft().getTextureManager().bindTexture(TEXTURE_RECYCLING_BOOK);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
        // draw slots
        getMinecraft().getTextureManager().bindTexture(TEXTURE_VANILLA_RECYCLER);
        for (Slot slot : this.container.inventorySlots) {
            int startPosX = this.guiLeft + slot.xPos;
            int startPosY = this.guiTop + slot.yPos;
            blit(startPosX, startPosY, 112, 222, 16, 16);
            if (Helper.atInterval(slot.getSlotIndex(), 10, false)) {
                int recipeId = slot.getSlotIndex() / 10;
                if (this.container.isUserDefinedRecipe(recipeId)) {
                    Rectangle2d pos = this.recipeFlags.get(recipeId * 3);
                    fill(pos.getX(), pos.getY(), pos.getX() + pos.getWidth(), pos.getY() + pos.getHeight(), 0xff0000ff);
                }
                if (this.container.isBlacklistRecipe(recipeId)) {
                    Rectangle2d pos = this.recipeFlags.get(recipeId * 3 + 1);
                    fill(pos.getX(), pos.getY(), pos.getX() + pos.getWidth(), pos.getY() + pos.getHeight(), 0xff000000);
                }
                if (this.container.isUnbalancedRecipe(recipeId)) {
                    Rectangle2d pos = this.recipeFlags.get(recipeId * 3 + 2);
                    fill(pos.getX(), pos.getY(), pos.getX() + pos.getWidth(), pos.getY() + pos.getHeight(), 0xff501030);
                }
            }
        }
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        this.container.inventorySlots.stream().filter(slot -> {
            int recipeId;
            return Helper.atInterval(slot.getSlotIndex(), 10, false) && (this.container.isBlacklistRecipe((recipeId = slot.getSlotIndex() / 10)) || !ConfigRecycler.shared_general.unbalanced_recipes.get() && this.container.isUnbalancedRecipe(recipeId));
        }).forEach(slot -> {
            int startPosX = this.guiLeft + slot.xPos;
            int startPosY = this.guiTop + slot.yPos;
            drawCross(startPosX + 21, startPosY - 16, startPosX + 69, startPosY + 32, 0xffff0000);
        });
        this.recipeFlags.entrySet().stream().filter(p -> p.getValue().contains(mouseX, mouseY)).findFirst().ifPresent(entry -> {
            int type = entry.getKey() % 3;
            renderTooltip((type == 0 ? "user defined" : type == 1 ? "blacklist" : "unbalanced"), mouseX, mouseY);
        });
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    private void drawCross(float x1, float y1, float x2, float y2, int color) {
        float[] color4F = Helper.getRGBColor4F(color);
        Matrix4f matrix4f = TransformationMatrix.identity().getMatrix();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        RenderSystem.lineWidth(2.5f);
        bufferbuilder.pos(matrix4f, x1, y1, getBlitOffset()).color(color4F[0], color4F[1], color4F[2], color4F[3]).endVertex();
        bufferbuilder.pos(matrix4f, x2, y2, getBlitOffset()).color(color4F[0], color4F[1], color4F[2], color4F[3]).endVertex();
        bufferbuilder.pos(matrix4f, x2, y1, getBlitOffset()).color(color4F[0], color4F[1], color4F[2], color4F[3]).endVertex();
        bufferbuilder.pos(matrix4f, x1, y2, getBlitOffset()).color(color4F[0], color4F[1], color4F[2], color4F[3]).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.lineWidth(1f);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        RenderSystem.pushMatrix();
        RenderSystem.scaled(0.5d, 0.5d, 0.5d);
        // name of the recipe
        this.container.inventorySlots.stream().filter(p -> Helper.atInterval(p.getSlotIndex(), 10, false) && !p.getStack().isEmpty()).forEach(c -> drawString(this.font, c.getStack().getDisplayName().getUnformattedComponentText(), (c.xPos - 2) * 2, (c.yPos - 22) * 2, this.textColor));
        // page number
        this.font.drawStringWithShadow((this.container.getPageNum() + 1) + "/" + (this.container.getPageMax() + 1), 428, 240, this.textColor);
        RenderSystem.popMatrix();
        // title of the book
        String title = TextFormatting.BOLD + I18n.format(LangKey.MESSAGE_RECYCLING_BOOK.getKey());
        this.font.drawStringWithShadow(title, (this.xSize - this.font.getStringWidth(title)) / 2f, -10, this.textColor);
    }
}
