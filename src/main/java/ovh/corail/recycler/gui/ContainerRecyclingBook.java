package ovh.corail.recycler.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;
import ovh.corail.recycler.registry.ModContainers;
import ovh.corail.recycler.util.RecyclingManager;
import ovh.corail.recycler.util.RecyclingRecipe;

public class ContainerRecyclingBook extends Container {
    private final IntReferenceHolder pageNum = IntReferenceHolder.single(), pageMax = IntReferenceHolder.single();
    private final ItemStackHandler BOOK_INVENTORY = new ItemStackHandler(40);
    private String searchText = ""; // the search is server side so in english only

    protected ContainerRecyclingBook(ContainerType<? extends ContainerRecyclingBook> containerType, int windowId) {
        super(containerType, windowId);
        trackInt(this.pageNum);
        trackInt(this.pageMax);
        initSlots();
        initPage(0);
    }

    public ContainerRecyclingBook(int windowId) {
        this(ModContainers.RECYCLING_BOOK, windowId);
    }

    public ContainerRecyclingBook(int windowId, PlayerInventory playerInventory) {
        this(ModContainers.RECYCLING_BOOK, windowId);
    }

    public void initPage(int pageNum) {
        NonNullList<RecyclingRecipe> recipes = RecyclingManager.instance.getRecipesForSearch(this.searchText);
        this.pageMax.set(recipes.size() / 4);
        this.pageNum.set(Math.min(pageNum, this.pageMax.get()));
        int skipped = this.pageNum.get() * 4;
        int slotId = 0;
        int recipeIdMax = Math.min(skipped + 4, recipes.size());
        for (int recipeId = skipped; recipeId < recipeIdMax; recipeId++) {
            RecyclingRecipe recipe = recipes.get(recipeId);
            BOOK_INVENTORY.setStackInSlot(slotId++, recipe.getItemRecipe().asItemStack());
            for (int i = 0; i < 9; i++) {
                BOOK_INVENTORY.setStackInSlot(slotId++, i < recipe.getCount() ? recipe.getResult(i).asItemStack() : ItemStack.EMPTY);
            }
        }
        while (slotId < 40) {
            BOOK_INVENTORY.setStackInSlot(slotId++, ItemStack.EMPTY);
        }
    }

    public void updateSearchText(String searchText) {
        this.searchText = searchText;
        initPage(0);
    }

    public int getPageNum() {
        return this.pageNum.get();
    }

    public void setPageNum(int num) {
        this.pageNum.set(num);
    }

    public int getPageMax() {
        return this.pageMax.get();
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    private void initSlots() {
        int startX, startY;
        int slotId = 0;
        // each recipes line
        for (int j = 0; j < 2; j++) {
            startX = 40;
            startY = 15 + (j * 16 * 3) + (8 * j);
            // 2 recipes on each line
            for (int i = 0; i < 2; i++) {
                // item to recycle
                addSlot(new SlotRecycler(BOOK_INVENTORY, slotId++, startX, startY + 16, p -> false, false));
                startX += 21;
                // 3X3 grid
                for (int caseY = 0; caseY < 3; caseY++) {
                    for (int caseX = 0; caseX < 3; caseX++) {
                        addSlot(new SlotRecycler(BOOK_INVENTORY, slotId++, startX + (caseX * 16), startY + (caseY * 16), p -> false, false));
                    }
                }
                startX = 140;
            }
        }
    }
}
