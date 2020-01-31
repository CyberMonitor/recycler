package ovh.corail.recycler.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.ItemStackHandler;
import ovh.corail.recycler.registry.ModContainers;
import ovh.corail.recycler.recipe.RecyclingManager;
import ovh.corail.recycler.recipe.RecyclingRecipe;

public class ContainerRecyclingBook extends Container {
    private final IIntArray recyclingBookData;
    private int pageNum = 0, pageMax = 0;
    private int[] recipe_flags = new int[4];
    private final ItemStackHandler BOOK_INVENTORY = new ItemStackHandler(40);
    private String searchText = ""; // the search is server side so in english only

    protected ContainerRecyclingBook(ContainerType<? extends ContainerRecyclingBook> containerType, int windowId, PlayerInventory playerInventory) {
        super(containerType, windowId);
        trackIntArray(this.recyclingBookData = new RecyclingBookData());
        initSlots();
        if (!playerInventory.player.world.isRemote) {
            initPage(0);
        }
    }

    public ContainerRecyclingBook(int windowId, PlayerInventory playerInventory) {
        this(ModContainers.RECYCLING_BOOK, windowId, playerInventory);
    }

    public void initPage(int pageNum) {
        NonNullList<RecyclingRecipe> recipes = RecyclingManager.instance.getRecipesForSearch(this.searchText);
        setPageMax(recipes.size() / 4);
        setPageNum(MathHelper.clamp(pageNum, 0, getPageMax()));
        int skipped = getPageNum() * 4;
        int slotId = 0;
        int recipeIdMax = Math.min(skipped + 4, recipes.size());
        for (int recipeId = skipped; recipeId < recipeIdMax; recipeId++) {
            RecyclingRecipe recipe = recipes.get(recipeId);
            this.recyclingBookData.set(recipeId - skipped, (recipe.isUserDefined() ? 1 : 0) + (recipe.isUnbalanced() ? 2 : 0) + (!recipe.isAllowed() ? 4 : 0));
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
        return this.recyclingBookData.get(4);
    }

    public void setPageNum(int num) {
        this.recyclingBookData.set(4, num);
    }

    public int getPageMax() {
        return this.recyclingBookData.get(5);
    }

    public void setPageMax(int num) {
        this.recyclingBookData.set(5, num);
    }

    public boolean isUserDefinedRecipe(int recipeSquareNum) {
        return (this.recyclingBookData.get(recipeSquareNum) & 1) != 0;
    }

    public boolean isUnbalancedRecipe(int recipeSquareNum) {
        return (this.recyclingBookData.get(recipeSquareNum) & 2) != 0;
    }

    public boolean isBlacklistRecipe(int recipeSquareNum) {
        return (this.recyclingBookData.get(recipeSquareNum) & 4) != 0;
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

    public class RecyclingBookData implements IIntArray {

        @Override
        public int get(int index) {
            switch (index) {
                case 0:
                    return recipe_flags[0];
                case 1:
                    return recipe_flags[1];
                case 2:
                    return recipe_flags[2];
                case 3:
                    return recipe_flags[3];
                case 4:
                    return pageNum;
                case 5:
                    return pageMax;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    recipe_flags[0] = value;
                    break;
                case 1:
                    recipe_flags[1] = value;
                    break;
                case 2:
                    recipe_flags[2] = value;
                    break;
                case 3:
                    recipe_flags[3] = value;
                    break;
                case 4:
                    pageNum = value;
                    break;
                case 5:
                    pageMax = value;
                    break;
                default:
                    break;
            }
        }

        @Override
        public int size() {
            return 6;
        }
    }
}
