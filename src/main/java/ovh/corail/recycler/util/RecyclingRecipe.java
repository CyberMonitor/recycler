package ovh.corail.recycler.util;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecyclingRecipe {
    private final ItemStack itemRecipe;
    private boolean isUnbalanced = false;
    private boolean isUserDefined = false;
    private boolean isAllowed = true;
    private final List<ItemStack> itemsList = new ArrayList<>();

    RecyclingRecipe(ItemStack itemStack) {
        this.itemRecipe = itemStack;
    }

    RecyclingRecipe(ItemStack stackIn, List<ItemStack> stacksOut) {
        itemRecipe = stackIn;
        itemsList.addAll(stacksOut);
    }

    RecyclingRecipe(ItemStack stackIn, ItemStack[] stacksOut) {
        itemRecipe = stackIn;
        Collections.addAll(itemsList, stacksOut);
    }

    public ItemStack getItemRecipe() {
        return itemRecipe;
    }

    void setUnbalanced(boolean state) {
        isUnbalanced = state;
    }

    boolean isUnbalanced() {
        return isUnbalanced;
    }

    void setUserDefined(boolean state) {
        isUserDefined = state;
    }

    boolean isUserDefined() {
        return isUserDefined;
    }

    void setAllowed(boolean state) {
        isAllowed = state;
    }

    boolean isAllowed() {
        return isAllowed;
    }

    public Integer getCount() {
        return itemsList.size();
    }

    void addStack(ItemStack stack) {
        itemsList.add(stack);
    }

    public ItemStack getStack(int index) {
        return itemsList.get(index);
    }
}
