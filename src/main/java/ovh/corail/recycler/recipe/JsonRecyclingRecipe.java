package ovh.corail.recycler.recipe;

import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.util.NonNullList;
import ovh.corail.recycler.util.Helper;

import java.util.Collection;
import java.util.stream.Collectors;

public class JsonRecyclingRecipe {
    final String inputItem;
    final String[] outputItems;

    // TODO merge with recyclingRecipe
    JsonRecyclingRecipe(String inputItem, String[] outputItems) {
        this.inputItem = inputItem;
        this.outputItems = outputItems;
    }

    JsonRecyclingRecipe(SimpleStack input, Collection<SimpleStack> outputs) {
        this(input.toString(), outputs.stream().map(SimpleStack::toString).toArray(String[]::new));
    }

    JsonRecyclingRecipe(RecyclingRecipe recipe) {
        this(recipe.getItemRecipe(), recipe.getResult());
    }

    public JsonRecyclingRecipe(ICraftingRecipe recipe) {
        this(new SimpleStack(recipe.getRecipeOutput()).toString(), Helper.mergeStackInList(recipe.getIngredients().stream().filter(p -> p.getMatchingStacks().length > 0 && !p.getMatchingStacks()[0].isEmpty()).map(m -> m.getMatchingStacks()[0].copy()).collect(Collectors.toCollection(NonNullList::create))).stream().map(p -> new SimpleStack(p).toString()).toArray(String[]::new));
    }
}
