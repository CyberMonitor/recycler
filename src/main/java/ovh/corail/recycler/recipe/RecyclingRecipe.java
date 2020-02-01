package ovh.corail.recycler.recipe;

import net.minecraft.util.NonNullList;

import java.util.Collections;

@SuppressWarnings({ "WeakerAccess", "UnusedReturnValue" })
public class RecyclingRecipe {
    protected final SimpleStack itemRecipe;
    protected boolean isUnbalanced = false;
    protected boolean isUserDefined = false;
    protected final NonNullList<SimpleStack> itemsList = NonNullList.create();

    public RecyclingRecipe(SimpleStack stack) {
        this.itemRecipe = stack;
    }

    public RecyclingRecipe(SimpleStack stackIn, NonNullList<SimpleStack> stacksOut) {
        this.itemRecipe = stackIn;
        this.itemsList.addAll(stacksOut);
    }

    public RecyclingRecipe(SimpleStack stackIn, SimpleStack[] stacksOut) {
        this.itemRecipe = stackIn;
        Collections.addAll(itemsList, stacksOut);
    }

    public SimpleStack getItemRecipe() {
        return this.itemRecipe;
    }

    public RecyclingRecipe setUnbalanced(boolean state) {
        this.isUnbalanced = state;
        return this;
    }

    public boolean isUnbalanced() {
        return this.isUnbalanced;
    }

    public RecyclingRecipe setUserDefined(boolean state) {
        this.isUserDefined = state;
        return this;
    }

    public boolean isUserDefined() {
        return this.isUserDefined;
    }

    public boolean setAllowed(boolean state) {
        return RecyclingManager.instance.setAllowedRecipe(this, state);
    }

    public boolean isAllowed() {
        return RecyclingManager.instance.isAllowedRecipe(this);
    }

    public Integer getCount() {
        return this.itemsList.size();
    }

    public void addStack(SimpleStack stack) {
        this.itemsList.add(stack);
    }

    public SimpleStack getResult(int index) {
        return this.itemsList.get(index);
    }

    public NonNullList<SimpleStack> getResult() {
        return this.itemsList;
    }
}
