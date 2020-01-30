package ovh.corail.recycler.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ovh.corail.recycler.network.PacketHandler;
import ovh.corail.recycler.network.ServerRecyclingBookMessage;
import ovh.corail.recycler.network.ServerRecyclingBookMessage.RecyclingBookAction;
import ovh.corail.recycler.util.Helper;
import ovh.corail.recycler.util.LangKey;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class GuiRecyclingBook extends ContainerScreen<ContainerRecyclingBook> {
    private TextFieldWidget searchBox;
    private String lastText = "";
    private static final ResourceLocation TEXTURE_VANILLA_RECYCLER = new ResourceLocation(MOD_ID + ":textures/gui/vanilla_recycler.png");
    private static final ResourceLocation TEXTURE_RECYCLING_BOOK = new ResourceLocation(MOD_ID + ":textures/gui/book.png");
    private final int textColor = 0xd4af37;

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
            blit(this.guiLeft + slot.xPos, this.guiTop + slot.yPos, 112, 222, 16, 16);
        }
        // TODO icons for blacklist recipe / unbalanced / user defined + allow to show blacklist ones with permission level
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        RenderSystem.pushMatrix();
        RenderSystem.scaled(0.5d, 0.5d, 0.5d);
        // name of the recipe
        this.container.inventorySlots.stream().filter(p -> Helper.atInterval(p.getSlotIndex(), 10) && !p.getStack().isEmpty()).forEach(c -> drawString(this.font, c.getStack().getDisplayName().getUnformattedComponentText(), (c.xPos - 2) * 2, (c.yPos - 22) * 2, this.textColor));
        // page number
        this.font.drawStringWithShadow((this.container.getPageNum() + 1) + "/" + (this.container.getPageMax() + 1), 428, 240, this.textColor);
        RenderSystem.popMatrix();
        // title of the book
        String title = TextFormatting.BOLD + I18n.format(LangKey.MESSAGE_RECYCLING_BOOK.getKey());
        this.font.drawStringWithShadow(title, (this.xSize - this.font.getStringWidth(title)) / 2f, -10, this.textColor);
    }
}
