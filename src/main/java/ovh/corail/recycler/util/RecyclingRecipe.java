package ovh.corail.recycler.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Collections;


public class RecyclingRecipe {
    private final ItemStack itemRecipe;
    private boolean isUnbalanced = false;
    private boolean isUserDefined = false;
    private boolean isAllowed = true;
    private final NonNullList<ItemStack> itemsList = NonNullList.create();

    RecyclingRecipe(ItemStack itemStack) {
        this.itemRecipe = itemStack;
    }

    RecyclingRecipe(ItemStack stackIn, NonNullList<ItemStack> stacksOut) {
        this.itemRecipe = stackIn;
        this.itemsList.addAll(stacksOut);
    }

    RecyclingRecipe(ItemStack stackIn, ItemStack[] stacksOut) {
        this.itemRecipe = stackIn;
        Collections.addAll(itemsList, stacksOut);
    }

    public ItemStack getItemRecipe() {
        return this.itemRecipe;
    }

    void setUnbalanced(boolean state) {
        this.isUnbalanced = state;
    }

    boolean isUnbalanced() {
        return this.isUnbalanced;
    }

    void setUserDefined(boolean state) {
        this.isUserDefined = state;
    }

    boolean isUserDefined() {
        return this.isUserDefined;
    }

    void setAllowed(boolean state) {
        this.isAllowed = state;
    }

    boolean isAllowed() {
        return this.isAllowed;
    }

    public Integer getCount() {
        return this.itemsList.size();
    }

    void addStack(ItemStack stack) {
        this.itemsList.add(stack);
    }

    public ItemStack getResult(int index) {
        return this.itemsList.get(index);
    }

    public NonNullList<ItemStack> getResult() {
        return this.itemsList;
    }
}
