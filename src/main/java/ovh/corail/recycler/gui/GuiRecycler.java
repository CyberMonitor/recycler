package ovh.corail.recycler.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ovh.corail.recycler.network.PacketHandler;
import ovh.corail.recycler.network.ServerRecyclerMessage;
import ovh.corail.recycler.network.ServerRecyclerMessage.RecyclerAction;
import ovh.corail.recycler.network.ServerRecyclingBookMessage;
import ovh.corail.recycler.network.ServerRecyclingBookMessage.RecyclingBookAction;
import ovh.corail.recycler.util.LangKey;

import java.util.List;
import java.util.stream.IntStream;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class GuiRecycler extends ContainerScreen<ContainerRecycler> {
    private static final ResourceLocation TEXTURE_RECYCLER = new ResourceLocation(MOD_ID + ":textures/gui/vanilla_recycler.png");
    private final ResourceLocation TEXTURE_BAR = new ResourceLocation("minecraft", "textures/gui/bars.png");
    private boolean isInit = true;

    public GuiRecycler(ContainerRecycler container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.xSize = 248;
        this.ySize = 203;
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);
        this.buttons.clear();
        // Recycle
        addButton(new ButtonRecycler(guiLeft + 174, guiTop + 120, 53, 14, I18n.format(LangKey.BUTTON_RECYLE.getKey()), pressable -> {
            PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.RECYCLE, this.container.getPosition()));
            //updateButtons();
        }));
        // Switch Working
        addButton(new ButtonRecycler(guiLeft + 174, guiTop + 139, 53, 14, I18n.format(LangKey.BUTTON_AUTO.getKey()), pressable -> {
            PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.SWITCH_AUTO, this.container.getPosition()));
            //updateButtons();
        }));
        // Take All
        addButton(new ButtonRecycler(guiLeft + 174, guiTop + 157, 53, 14, I18n.format(LangKey.BUTTON_TAKE_ALL.getKey()), pressable -> {
            PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.TAKE_ALL, this.container.getPosition()));
        }));
        // Create Recipe
        addButton(new ButtonRecycler(guiLeft + 174, guiTop + 175, 53, 14, I18n.format(LangKey.BUTTON_DISCOVER_RECIPE.getKey()), pressable -> {
            PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.DISCOVER_RECIPE, this.container.getPosition()));
            //updateButtons();
        }));
        // Remove Recipe
        addButton(new ButtonRecycler(guiLeft + 174, guiTop + 175, 53, 14, I18n.format(LangKey.BUTTON_REMOVE_RECIPE.getKey()), pressable -> {
            PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.REMOVE_RECIPE, this.container.getPosition()));
            //updateButtons();
        }));
        // open recycling_book
        addButton(new ImageButton(guiLeft + 148, guiTop + 64, 20, 18, 178, 0, 19, INVENTORY_BACKGROUND, pressable -> {
            PacketHandler.sendToServer(new ServerRecyclingBookMessage(RecyclingBookAction.RECYCLING_BOOK));
        }));
        //updateButtons();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        //if (isInit) {  }
        renderBackground();
        GlStateManager.color4f(1f, 1f, 1f, 1f);
        // recycler background
        getMinecraft().getTextureManager().bindTexture(TEXTURE_RECYCLER);
        blit(guiLeft, guiTop, 0, 0, xSize, ySize);
        // draw slots
        for (Slot slot : getContainer().inventorySlots) {
            blit(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 110, 238, 18, 18);
            if (slot instanceof SlotRecycler) {
                SlotRecycler currentSlot = (SlotRecycler) slot;
                if (isInit) {
                    currentSlot.timeInUse = 0;
                } else if (currentSlot.timeInUse > 0) {
                    blit(guiLeft + slot.xPos - 1, guiTop + slot.yPos - 1, 110, 203, 18, 18);
                    currentSlot.timeInUse--;
                }
            }
        }
        this.isInit = false;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // draw the player on gui
        int entityX = 86, entityY = 50;
        InventoryScreen.drawEntityOnScreen(entityX, entityY, 20, (float) (2 * entityX - mouseX), (float) (entityY - mouseY), getMinecraft().player);
        getMinecraft().textureManager.bindTexture(TEXTURE_RECYCLER);
        // arrow in background
        blit(115, 81, 79, 210, 22, 15);
        // progress bar
        if (this.container.isWorking() && this.container.getInputMax() > 0) {
            blit(115, 81, 79, 225, (this.container.getProgress() * 22 / 100), 15);
        }

        int currentPower = this.container.getRecycler().getEnergy();

        GlStateManager.color4f(1f, 1f, 1f, 1f);
        getMinecraft().getTextureManager().bindTexture(TEXTURE_BAR);
        blit(70, 112, 0f, 20f, 136, 3, 136, 256);
        blit(70, 112, 0f, 25f, (int) (136 * 0.71d * currentPower) / 10000, 2, 136, 256);
        blit(70, 112, 0f, 81f, 136, 3, 136, 256);

        GlStateManager.pushMatrix();
        GlStateManager.scaled(0.6d, 0.6d, 0.6d);
        // disk useLeft
        List<Slot> inventorySlots = getContainer().inventorySlots;
        int useLeft = inventorySlots.get(1).getStack().isEmpty() ? 0 : (inventorySlots.get(1).getStack().getMaxDamage() - inventorySlots.get(1).getStack().getDamage()) / 10;
        this.font.drawString(useLeft + "", (int) ((inventorySlots.get(1).xPos + 20) / 0.6d), (int) ((inventorySlots.get(1).yPos + 4) / 0.6d), useLeft > 0 ? 0x00ff00 : 0xff0000);
        // max recycling actions for the current stack
        this.font.drawString(this.container.getInputMax() + "", (int) ((inventorySlots.get(0).xPos + 20) / 0.6d), (int) ((inventorySlots.get(0).yPos + 4) / 0.6d), (this.container.getInputMax() > 0 ? 0x00ff00 : 0xff0000));

        this.font.drawString(String.format("%5s", currentPower) + " / " + this.container.getRecycler().getMaxEnergy(), (int) (120 / 0.6d), (int) (104 / 0.6d), currentPower >= 10 ? 0x00ff00 : 0xff0000);
        GlStateManager.popMatrix();
    }

    @Override
    public void onClose() {
        super.onClose();
        getMinecraft().keyboardListener.enableRepeatEvents(false);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        updateButtons();
        super.render(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void updateButtons() {
        boolean isEmptySlot = this.container.getRecycler().getInventoryWorking().getStackInSlot(0).isEmpty();
        boolean hasPermission = getMinecraft().player.canUseCommandBlock();
        boolean hasRecipe = IntStream.range(0, this.container.getRecycler().getInventoryVisual().getSlots()).anyMatch(slot -> !this.container.getRecycler().getInventoryVisual().getStackInSlot(slot).isEmpty());
        // button recycle
        this.buttons.get(0).active = hasRecipe && this.container.getInputMax() > 0 && !this.container.isWorking();
        // button auto
        this.buttons.get(1).active = hasRecipe;// && this.container.isWorking() || !isEmptySlot;
        // button take all
        this.buttons.get(2).active = !this.container.getRecycler().isOutputEmpty();
        // button create recipe
        this.buttons.get(3).active = this.buttons.get(3).visible = hasPermission && !hasRecipe && !isEmptySlot;
        // button remove recipe
        this.buttons.get(4).active = this.buttons.get(4).visible = hasPermission && hasRecipe && !isEmptySlot;
        // button recycling book
        //this.buttons.get(5).active = this.buttons.get(5).visible = true;
    }
}
