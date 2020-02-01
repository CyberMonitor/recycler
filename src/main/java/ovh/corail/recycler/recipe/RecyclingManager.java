package ovh.corail.recycler.recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.ImmutablePair;
import ovh.corail.recycler.ConfigRecycler;
import ovh.corail.recycler.registry.ModBlocks;
import ovh.corail.recycler.registry.ModItems;
import ovh.corail.recycler.util.Helper;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ovh.corail.recycler.ModRecycler.LOGGER;
import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class RecyclingManager {
    public static final RecyclingManager instance = new RecyclingManager();
    private final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final NonNullList<RecyclingRecipe> recipes = NonNullList.create();
    private final Set<SimpleStack> unbalanced = new HashSet<>();
    private final Set<SimpleStack> blacklist = new HashSet<>();
    private final NonNullList<ImmutablePair<SimpleStack, SimpleStack>> grindList = NonNullList.create();
    public final File CONFIG_DIR = new File(FMLPaths.CONFIGDIR.get().toFile(), MOD_ID);
    private final File unbalancedFile = new File(CONFIG_DIR, "unbalanced_recipes.json");
    private final File blacklistFile = new File(CONFIG_DIR, "blacklist_recipes.json");
    private final File userDefinedFile = new File(CONFIG_DIR, "user_defined_recipes.json");
    private final File grindFile = new File(CONFIG_DIR, "grind_list.json");

    private RecyclingManager() {
        if (!CONFIG_DIR.exists() && !CONFIG_DIR.mkdir()) {
            LOGGER.warn("Impossible to create the config folder");
        }
    }

    public boolean discoverRecipe(ServerWorld world, ItemStack stack) {
        RecyclingRecipe recipe = getRecipe(stack, false);
        // recipe already in recycler
        if (recipe != null) {
            return setAllowedRecipe(recipe, true) || (!ConfigRecycler.shared_general.unbalanced_recipes.get() && setUnbalancedRecipe(recipe, false));
        } else {
            // new recipe added
            recipe = world.getRecipeManager().getRecipes(IRecipeType.CRAFTING).values().stream().filter(craftingRecipe -> Helper.isValidRecipe(craftingRecipe) && Helper.areItemEqual(craftingRecipe.getRecipeOutput(), stack)).map(this::convertCraftingRecipe).findFirst().orElse(null);
            // add recipe and save user defined recipes to json
            if (Helper.isValidRecipe(recipe)) {
                this.recipes.add(recipe);
                return saveUserDefinedRecipes();
            }
            return false;
        }
    }

    public void loadRecipes() {
        clear();
        // load enchanted book recipe
        if (ConfigRecycler.general.recycle_enchanted_book.get()) {
            this.recipes.add(new RecyclingRecipe(new SimpleStack(Items.ENCHANTED_BOOK), new SimpleStack[] {}));
        }
        // load unbalanced recipes
        loadUnbalanced();
        // load blacklist recipes
        loadBlacklist();
        // load default recycling recipes
        RecipeLoaderHelper.loadDefaultRecipes(this.recipes);
        // load json user defined recycling recipes
        loadUserDefinedRecipes();
        // load grind List for damaged items and when losses
        loadGrindList();
    }

    public int getRecipeCount() {
        return this.recipes.size();
    }

    private void clear() {
        // clear when loading another game
        this.recipes.clear();
        this.unbalanced.clear();
        this.blacklist.clear();
        this.grindList.clear();
    }

    private void loadUnbalanced() {
        if (!this.unbalancedFile.exists()) {
            addToCollection(this.unbalanced, Blocks.GRANITE);
            addToCollection(this.unbalanced, Blocks.DIORITE);
            addToCollection(this.unbalanced, Blocks.ANDESITE);
            addToCollection(this.unbalanced, Items.PAPER);
            addToCollection(this.unbalanced, Items.SUGAR);
            addToCollection(this.unbalanced, Items.ENDER_EYE);
            addToCollection(this.unbalanced, Items.BLAZE_POWDER);
            addToCollection(this.unbalanced, Items.MAGMA_CREAM);
            addToCollection(this.unbalanced, Items.FIRE_CHARGE);
            addToCollection(this.unbalanced, Blocks.RED_NETHER_BRICKS);
            addToCollection(this.unbalanced, Blocks.MAGMA_BLOCK);
            addToCollection(this.unbalanced, Blocks.GRANITE);
            saveUnbalanced();
        } else {
            loadAsJson(this.unbalancedFile, String.class).forEach(jsonString -> {
                SimpleStack currentStack = SimpleStack.fromJson(jsonString);
                if (!currentStack.isEmpty()) {
                    this.unbalanced.add(currentStack);
                }
            });
        }
    }

    private void loadBlacklist() {
        if (!this.blacklistFile.exists()) {
            addToCollection(this.blacklist, ModBlocks.recycler);
            saveBlacklist();
        } else {
            loadAsJson(this.blacklistFile, String.class).forEach(jsonString -> {
                SimpleStack currentStack = SimpleStack.fromJson(jsonString);
                if (!currentStack.isEmpty()) {
                    this.blacklist.add(currentStack);
                }
            });
        }
    }

    private void saveBlacklist() {
        saveAsJson(this.blacklistFile, this.blacklist.stream().map(SimpleStack::toString).collect(Collectors.toCollection(NonNullList::create)));
    }

    public boolean isAllowedRecipe(RecyclingRecipe recipe) {
        return this.blacklist.stream().noneMatch(stack -> SimpleStack.areItemEqual(stack, recipe.getItemRecipe()));
    }

    public boolean setAllowedRecipe(RecyclingRecipe recipe, boolean state) {
        boolean allowed = isAllowedRecipe(recipe);
        recipe.setAllowed(state);
        if (state != allowed) {
            if (allowed) {
                this.blacklist.add(recipe.getItemRecipe());
            } else {
                this.blacklist.remove(recipe.getItemRecipe());
            }
            saveBlacklist();
            return true;
        }
        return false;
    }

    private void saveUnbalanced() {
        saveAsJson(this.unbalancedFile, this.unbalanced.stream().map(SimpleStack::toString).collect(Collectors.toCollection(NonNullList::create)));
    }

    public boolean isUnbalancedRecipe(RecyclingRecipe recipe) {
        return this.unbalanced.stream().anyMatch(stack -> SimpleStack.areItemEqual(stack, recipe.getItemRecipe()));
    }

    public boolean setUnbalancedRecipe(RecyclingRecipe recipe, boolean state) {
        boolean allowed = isUnbalancedRecipe(recipe);
        recipe.setUnbalanced(state);
        if (state != allowed) {
            if (allowed) {
                this.unbalanced.remove(recipe.getItemRecipe());
            } else {
                this.unbalanced.add(recipe.getItemRecipe());
            }
            saveUnbalanced();
            return true;
        }
        return false;
    }

    public RecyclingManager addRecipe(RecyclingRecipe recipe) {
        this.recipes.add(recipe);
        return this;
    }

    public boolean removeRecipe(ItemStack stack) {
        RecyclingRecipe recipe = getRecipe(stack, false);
        if (recipe == null) {
            return false;
        }
        if (recipe.isUserDefined()) {
            this.recipes.remove(recipe);
            saveUserDefinedRecipes();
            return true;
        }
        setAllowedRecipe(recipe, false);
        setUnbalancedRecipe(recipe, false);
        return true;
    }

    public NonNullList<RecyclingRecipe> getRecipesForSearch(String searchText) {
        return searchText.isEmpty() ? this.recipes : this.recipes.stream().filter(p -> p.getItemRecipe().getTranslation().contains(searchText)).collect(Collectors.toCollection(NonNullList::create));
    }

    @Nullable
    public RecyclingRecipe getRecipe(ItemStack stack) {
        return getRecipe(stack, true);
    }

    @Nullable
    public RecyclingRecipe getRecipe(ItemStack stack, boolean checked) {
        // enchanted book requires 2 enchants to be recycled
        if (stack.isEmpty() || (checked && stack.getItem() == Items.ENCHANTED_BOOK && EnchantedBookItem.getEnchantments(stack).size() < 2)) {
            return null;
        }
        RecyclingRecipe recipe = this.recipes.stream().filter(recipeIn -> recipeIn.getItemRecipe().isItemEqual(stack)).findFirst().orElse(null);
        if (recipe == null) {
            return null;
        }
        if (checked) {
            // unbalanced recipes
            if (!ConfigRecycler.shared_general.unbalanced_recipes.get() && recipe.isUnbalanced()) {
                return null;
            }
            // only user defined recipes
            if (ConfigRecycler.general.only_user_recipes.get() && !recipe.isUserDefined()) {
                return null;
            }
            // only allowed recipes
            if (!recipe.isAllowed()) {
                return null;
            }
        }
        return recipe;
    }

    private int getRecipeIndex(SimpleStack stack) {
        return stack.isEmpty() ? -1 : IntStream.range(0, this.recipes.size()).filter(slotId -> SimpleStack.areItemEqual(this.recipes.get(slotId).getItemRecipe(), stack)).findFirst().orElse(-1);
    }

    public NonNullList<ItemStack> getResultStack(ItemStack stack, int nb_input) {
        return getResultStack(stack, nb_input, false);
    }

    // TODO clean this
    public NonNullList<ItemStack> getResultStack(ItemStack stack, int nb_input, boolean half) {
        NonNullList<ItemStack> itemsList = NonNullList.create();
        RecyclingRecipe currentRecipe = getRecipe(stack);
        if (currentRecipe == null) {
            return itemsList;
        }

        ItemStack currentStack;
        int currentSize;
        boolean isDamagedStack = stack.getMaxDamage() > 0 && stack.getDamage() > 0;
        // foreach stacks in the recipe
        for (int i = 0; i < currentRecipe.getCount(); i++) {
            // smaller units for damaged items and when there're losses
            ItemStack grind = isDamagedStack || half ? getGrind(currentRecipe.getResult(i)).asItemStack() : ItemStack.EMPTY;
            if (grind.isEmpty()) {
                currentStack = currentRecipe.getResult(i).asItemStack();
                currentSize = currentStack.getCount();
            } else {
                currentStack = grind;
                currentSize = currentRecipe.getResult(i).getCount() * grind.getCount();
            }
            float modifiedSize = currentSize;
            // reduces the stacksize based on damages
            if (isDamagedStack) {
                modifiedSize = modifiedSize * (stack.getMaxDamage() - stack.getDamage()) / (float) stack.getMaxDamage();
            }
            // reduces the stacksize based on config losses
            if (half) {
                modifiedSize /= 2f;
            }
            // size for nb_input
            currentSize = (int) (ConfigRecycler.general.recycle_round_down.get() ? Math.floor(modifiedSize) : Math.round(modifiedSize)) * nb_input;
            // fill with fullstack
            int leftStackCount = currentSize;
            currentStack.setCount(currentStack.getMaxStackSize());
            while (leftStackCount >= currentStack.getMaxStackSize()) {
                itemsList.add(currentStack.copy());
                leftStackCount -= currentStack.getMaxStackSize();
            }
            // stack left
            if (leftStackCount > 0) {
                currentStack.setCount(leftStackCount);
                itemsList.add(currentStack.copy());
            }
        }
        // check enchants, no loss
        if (ConfigRecycler.general.recycle_magic_item.get()) {
            itemsList.addAll(getEnchantedBooks(stack));
        }
        return itemsList;
    }

    private NonNullList<ItemStack> getEnchantedBooks(ItemStack stack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack).entrySet().stream().filter(p -> p.getKey() != null && p.getValue() != null).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        NonNullList<ItemStack> books = NonNullList.create();
        if (!enchants.isEmpty()) {
            if (stack.getItem() == Items.ENCHANTED_BOOK) {
                // skip if the book has only one enchant
                if (enchants.size() < 2) {
                    return books;
                }
                for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    ItemStack currentBook = new ItemStack(Items.ENCHANTED_BOOK);
                    EnchantedBookItem.addEnchantment(currentBook, new EnchantmentData(entry.getKey(), entry.getValue()));
                    books.add(currentBook);
                }
            } else {
                ItemStack currentBook = new ItemStack(Items.ENCHANTED_BOOK);
                for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    EnchantedBookItem.addEnchantment(currentBook, new EnchantmentData(entry.getKey(), entry.getValue()));
                }
                books.add(currentBook);
            }
        }
        return books;
    }

    @SuppressWarnings("unchecked")
    private void loadGrindList() {
        if (!this.grindFile.exists()) {
            this.grindList.add(new ImmutablePair(new SimpleStack(Items.DIAMOND), new SimpleStack(ModItems.diamond_shard, 9)));
            this.grindList.add(new ImmutablePair(new SimpleStack(Items.IRON_INGOT), new SimpleStack(Items.IRON_NUGGET, 9)));
            this.grindList.add(new ImmutablePair(new SimpleStack(Items.GOLD_INGOT), new SimpleStack(Items.GOLD_NUGGET, 9)));
            this.grindList.add(new ImmutablePair(new SimpleStack(Items.LEATHER), new SimpleStack(Items.RABBIT_HIDE, 4)));
            this.grindList.add(new ImmutablePair(new SimpleStack(Blocks.OAK_PLANKS), new SimpleStack(Items.STICK, 4)));
            saveAsJson(this.grindFile, this.grindList.stream().map(p -> new ImmutablePair(p.getLeft().toString(), p.getRight().toString())).collect(Collectors.toCollection(NonNullList::create)));
        } else {
            Type token = new TypeToken<NonNullList<ImmutablePair<String, String>>>() {
            }.getType();
            NonNullList<ImmutablePair<String, String>> jsonStringList = (NonNullList<ImmutablePair<String, String>>) loadAsJson(this.grindFile, token);
            for (ImmutablePair<String, String> pair : jsonStringList) {
                SimpleStack input = SimpleStack.fromJson(pair.getLeft());
                SimpleStack output = SimpleStack.fromJson(pair.getRight());
                if (!input.isEmpty() && !output.isEmpty()) {
                    this.grindList.add(new ImmutablePair(input, output));
                }
            }
        }
    }

    private SimpleStack getGrind(SimpleStack stack) {
        // only call when stack is damaged or there's losses to get smaller units
        return this.grindList.stream().filter(grind -> SimpleStack.areItemEqual(grind.getLeft(), stack)).map(ImmutablePair::getRight).findFirst().orElse(SimpleStack.EMPTY);
    }

    public boolean saveUserDefinedRecipes() {
        return saveAsJson(this.userDefinedFile, this.recipes.stream().filter(recipe -> recipe.isUserDefined() && Helper.isValidRecipe(recipe)).map(JsonRecyclingRecipe::new).collect(Collectors.toCollection(NonNullList::create)));
    }

    public boolean saveAsJson(File file, NonNullList list) {
        if (file.exists()) {
            if (!file.delete()) {
                LOGGER.warn("can't delete file " + file.getName());
            }
        }
        try {
            if (file.createNewFile()) {
                FileWriter fw = new FileWriter(file);
                fw.write(GSON.toJson(list));
                fw.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private NonNullList<?> loadAsJson(File file, Type token) {
        NonNullList<?> list = NonNullList.create();
        try {
            list = new Gson().fromJson(new BufferedReader(new FileReader(file)), token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private <T> NonNullList<T> loadAsJson(File file, Class<T> type) {
        NonNullList<T> list = NonNullList.create();
        try {
            JsonArray arrayDatas = new JsonParser().parse(new BufferedReader(new FileReader(file))).getAsJsonArray();
            for (JsonElement elem : arrayDatas) {
                list.add(GSON.fromJson(elem, type));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void loadUserDefinedRecipes() {
        NonNullList<JsonRecyclingRecipe> jsonRecipesList;
        if (!this.userDefinedFile.exists()) {
            jsonRecipesList = NonNullList.create();
            jsonRecipesList.add(new JsonRecyclingRecipe(MOD_ID + ":recycler:1", new String[] { "minecraft:acacia_planks:4", "minecraft:iron_ingot:4", "minecraft:chest:1" }));
            saveAsJson(this.userDefinedFile, jsonRecipesList);
        } else {
            jsonRecipesList = loadAsJson(this.userDefinedFile, JsonRecyclingRecipe.class);
        }

        for (JsonRecyclingRecipe aJsonRecipesList : jsonRecipesList) {
            RecyclingRecipe recipe = convertJsonRecipe(aJsonRecipesList);
            if (recipe != null && recipe.getCount() > 0) {
                // check for same existing recipe
                int foundRecipe = getRecipeIndex(recipe.getItemRecipe());
                recipe.setUserDefined(true);
                if (foundRecipe == -1) {
                    this.recipes.add(recipe);
                } else {
                    this.recipes.set(foundRecipe, recipe);
                }
            } else {
                LOGGER.warn("Error while reading json recipe : " + aJsonRecipesList.inputItem);
            }
        }
    }

    @Nullable
    private RecyclingRecipe convertJsonRecipe(JsonRecyclingRecipe jRecipe) {
        SimpleStack inputItem = SimpleStack.fromJson(jRecipe.inputItem);
        if (inputItem.isEmpty()) {
            return null;
        }
        RecyclingRecipe recipe = new RecyclingRecipe(inputItem);
        Arrays.stream(jRecipe.outputItems).map(SimpleStack::fromJson).filter(outputItem -> !outputItem.isEmpty()).forEach(recipe::addStack);
        return recipe;
    }

    private void addToCollection(Collection<SimpleStack> list, @Nullable Block block) {
        if (block != null) {
            addToCollection(list, block.asItem());
        }
    }

    private void addToCollection(Collection<SimpleStack> list, @Nullable Item item) {
        if (item != null && item.getRegistryName() != null) {
            list.add(new SimpleStack(item));
        }
    }

    @SuppressWarnings("unchecked")
    public RecyclingRecipe convertCraftingRecipe(IRecipe iRecipe) {
        NonNullList<Ingredient> ingredients = iRecipe.getIngredients();
        NonNullList<ItemStack> stacks = Helper.mergeStackInList(ingredients.stream().filter(p -> p.getMatchingStacks().length > 0 && !p.getMatchingStacks()[0].isEmpty()).map(m -> m.getMatchingStacks()[0].copy()).collect(Collectors.toCollection(NonNullList::create)));
        RecyclingRecipe recipe = new RecyclingRecipe(new SimpleStack(iRecipe.getRecipeOutput()), stacks.stream().map(SimpleStack::new).collect(Collectors.toCollection(NonNullList::create)));
        recipe.setUserDefined(true);
        return recipe;
    }
}
