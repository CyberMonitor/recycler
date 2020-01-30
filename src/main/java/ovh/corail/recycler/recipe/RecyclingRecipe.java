package ovh.corail.recycler.recipe;

import net.minecraft.util.NonNullList;

import java.util.Collections;

public class RecyclingRecipe {
    private final SimpleStack itemRecipe;
    private boolean isUnbalanced = false;
    private boolean isUserDefined = false;
    private boolean isAllowed = true;
    private final NonNullList<SimpleStack> itemsList = NonNullList.create();

    RecyclingRecipe(SimpleStack stack) {
        this.itemRecipe = stack;
    }

    RecyclingRecipe(SimpleStack stack, NonNullList<SimpleStack> stacksOut) {
        this.itemRecipe = stack;
        this.itemsList.addAll(stacksOut);
    }

    public RecyclingRecipe(SimpleStack stackIn, SimpleStack[] stacksOut) {
        this.itemRecipe = stackIn;
        Collections.addAll(itemsList, stacksOut);
    }

    public SimpleStack getItemRecipe() {
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

    void addStack(SimpleStack stack) {
        this.itemsList.add(stack);
    }

    public SimpleStack getResult(int index) {
        return this.itemsList.get(index);
    }

    public NonNullList<SimpleStack> getResult() {
        return this.itemsList;
    }
}
