package ovh.corail.recycler.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ovh.corail.recycler.ConfigRecycler;
import ovh.corail.recycler.network.PacketHandler;
import ovh.corail.recycler.network.ServerRecyclerMessage;
import ovh.corail.recycler.network.ServerRecyclerMessage.RecyclerAction;
import ovh.corail.recycler.network.ServerRecyclingBookMessage;
import ovh.corail.recycler.network.ServerRecyclingBookMessage.RecyclingBookAction;
import ovh.corail.recycler.util.LangKey;

import java.util.stream.IntStream;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@OnlyIn(Dist.CLIENT)
public class GuiRecycler extends ContainerScreen<ContainerRecycler> {
    private static final ResourceLocation TEXTURE_RECYCLER = new ResourceLocation(MOD_ID + ":textures/gui/vanilla_recycler.png");
    private final ResourceLocation TEXTURE_BAR = new ResourceLocation("textures/gui/bars.png");
    private boolean isInit = true;

    public GuiRecycler(ContainerRecycler container, PlayerInventory playerInventory, ITextComponent title) {
        super(container, playerInventory, title);
        this.xSize = 232;
        this.ySize = 203;
    }

    @Override
    public void init() {
        super.init();
        getMinecraft().keyboardListener.enableRepeatEvents(true);
        // Recycle
        addButton(new ButtonRecycler(this.guiLeft + 174, this.guiTop + 120, 53, 14, I18n.format(LangKey.BUTTON_RECYLE.getKey()), pressable -> PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.RECYCLE, this.container.getPosition()))));
        // Switch Working
        addButton(new ButtonRecycler(this.guiLeft + 174, this.guiTop + 139, 53, 14, I18n.format(LangKey.BUTTON_AUTO.getKey()), pressable -> PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.SWITCH_AUTO, this.container.getPosition()))));
        // Take All
        addButton(new ButtonRecycler(this.guiLeft + 174, this.guiTop + 157, 53, 14, I18n.format(LangKey.BUTTON_TAKE_ALL.getKey()), pressable -> PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.TAKE_ALL, this.container.getPosition()))));
        // Create Recipe
        addButton(new ButtonRecycler(this.guiLeft + 174, this.guiTop + 175, 53, 14, I18n.format(LangKey.BUTTON_DISCOVER_RECIPE.getKey()), pressable -> PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.DISCOVER_RECIPE, this.container.getPosition()))));
        // Remove Recipe
        addButton(new ButtonRecycler(this.guiLeft + 174, this.guiTop + 175, 53, 14, I18n.format(LangKey.BUTTON_REMOVE_RECIPE.getKey()), pressable -> PacketHandler.sendToServer(new ServerRecyclerMessage(RecyclerAction.REMOVE_RECIPE, this.container.getPosition()))));
        // open recycling_book
        addButton(new ImageButton(this.guiLeft + 148, this.guiTop + 64, 20, 18, 178, 0, 19, INVENTORY_BACKGROUND, pressable -> PacketHandler.sendToServer(new ServerRecyclingBookMessage(RecyclingBookAction.RECYCLING_BOOK))));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        renderBackground();
        RenderSystem.color4f(1f, 1f, 1f, 1f);
        // recycler background
        getMinecraft().getTextureManager().bindTexture(TEXTURE_RECYCLER);
        blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        // draw slots
        for (Slot slot : getContainer().inventorySlots) {
            blit(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 110, 238, 18, 18);
            if (slot instanceof SlotRecycler) {
                SlotRecycler currentSlot = (SlotRecycler) slot;
                if (this.isInit) {
                    currentSlot.timeInUse = 0;
                } else if (currentSlot.timeInUse > 0) {
                    blit(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 110, 203, 18, 18);
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
        //drawEntityOnScreen
        InventoryScreen.func_228187_a_(entityX, entityY, 20, (float) (2 * entityX - mouseX), (float) (entityY - mouseY), getMinecraft().player);
        getMinecraft().textureManager.bindTexture(TEXTURE_RECYCLER);
        // arrow in background
        blit(115, 81, 79, 210, 22, 15);
        // progress bar
        if (this.container.isWorking() && this.container.getInputMax() > 0) {
            blit(115, 81, 79, 225, (this.container.getProgress() * 22 / 100), 15);
        }

        int currentPower = this.container.getEnergy();

        RenderSystem.color4f(1f, 1f, 1f, 1f);
        getMinecraft().getTextureManager().bindTexture(TEXTURE_BAR);
        blit(70, 112, 0f, 20f, 136, 3, 136, 256);
        blit(70, 112, 0f, 25f, (int) (136 * 0.71d * currentPower) / this.container.getRecycler().getMaxEnergy(), 2, 136, 256);
        blit(70, 112, 0f, 81f, 136, 3, 136, 256);

        RenderSystem.pushMatrix();
        RenderSystem.scaled(0.6d, 0.6d, 0.6d);
        // disk useLeft
        Slot diskSlot = this.container.inventorySlots.get(1);
        ItemStack diskStack = diskSlot.getStack();
        int useLeft = diskStack.isEmpty() ? 0 : (diskStack.getMaxDamage() - diskStack.getDamage()) / 10;
        this.font.drawString(useLeft + "", (int) ((diskSlot.xPos + 20) / 0.6d), (int) ((diskSlot.yPos + 4) / 0.6d), useLeft > 0 ? 0x00ff00 : 0xff0000);
        // max recycling actions for the current stack
        Slot recycledSlot = this.container.inventorySlots.get(0);
        this.font.drawString(this.container.getInputMax() + "", (int) ((recycledSlot.xPos + 20) / 0.6d), (int) ((recycledSlot.yPos + 4) / 0.6d), (this.container.getInputMax() > 0 ? 0x00ff00 : 0xff0000));

        this.font.drawString(String.format("%5s", currentPower) + " / " + this.container.getRecycler().getMaxEnergy(), (int) (120 / 0.6d), (int) (104 / 0.6d), currentPower >= 10 ? 0x00ff00 : 0xff0000);
        RenderSystem.popMatrix();
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

    private void updateButtons() {
        boolean isEmptySlot = this.container.getRecycler().getInventoryWorking().getStackInSlot(0).isEmpty();
        boolean hasPermission = getMinecraft().player.canUseCommandBlock();
        boolean hasRecipe = IntStream.range(0, this.container.getRecycler().getInventoryVisual().getSlots()).anyMatch(slot -> !this.container.getRecycler().getInventoryVisual().getStackInSlot(slot).isEmpty());
        // button recycle
        this.buttons.get(0).active = hasRecipe && this.container.getInputMax() > 0 && !this.container.isWorking();
        // button auto
        this.buttons.get(1).active = ConfigRecycler.shared_general.allow_automation.get() && hasRecipe && !this.container.getRecycler().getInventoryWorking().getStackInSlot(1).isEmpty();
        // button take all
        this.buttons.get(2).active = !this.container.getRecycler().isOutputEmpty();
        // button create recipe
        this.buttons.get(3).active = this.buttons.get(3).visible = hasPermission && !hasRecipe && !isEmptySlot;
        // button remove recipe
        this.buttons.get(4).active = this.buttons.get(4).visible = hasPermission && hasRecipe && !isEmptySlot;
    }
}
