package ovh.corail.recycler.util;

public class JsonRecyclingRecipe {
    final String inputItem;
    final String[] outputItems;

    JsonRecyclingRecipe(String inputItem, String[] outputItems) {
        this.inputItem = inputItem;
        this.outputItems = outputItems;
    }
}
