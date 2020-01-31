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
        // Recipe already in recycler
        if (recipe != null) {
            // isn't blacklist
            if (recipe.isAllowed()) {
                return false;
            }
            recipe.setAllowed(true);
            saveBlacklist();
            return true;
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
        loadDefaultRecipes();
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
            saveAsJson(this.unbalancedFile, this.unbalanced.stream().map(SimpleStack::toString).collect(Collectors.toCollection(NonNullList::create)));
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
            saveAsJson(this.blacklistFile, this.blacklist.stream().map(SimpleStack::toString).collect(Collectors.toCollection(NonNullList::create)));
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
        saveAsJson(this.blacklistFile, this.recipes.stream().filter(p -> !p.isAllowed()).map(recipe -> recipe.getItemRecipe().toString()).collect(Collectors.toCollection(NonNullList::create)));
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
        recipe.setAllowed(false);
        saveBlacklist();
        return true;
    }

    public NonNullList<RecyclingRecipe> getRecipesForSearch(String searchText) {
        // p.isAllowed() && (ConfigRecycler.general.unbalanced_recipes.get() || !p.isUnbalanced()) &&
        return this.recipes.stream().filter(p -> (searchText.isEmpty() || p.getItemRecipe().getTranslation().contains(searchText))).collect(Collectors.toCollection(NonNullList::create));
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
            if (!ConfigRecycler.general.unbalanced_recipes.get() && recipe.isUnbalanced()) {
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
                recipe.setAllowed(this.blacklist.stream().noneMatch(p -> SimpleStack.areItemEqual(p, recipe.getItemRecipe())));
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
        recipe.setUnbalanced(this.unbalanced.stream().anyMatch(p -> SimpleStack.areItemEqual(p, recipe.getItemRecipe())));
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
        recipe.setUnbalanced(false);
        recipe.setUserDefined(true);
        recipe.setAllowed(true);
        return recipe;
    }

    private void loadDefaultRecipes() {
        // 1.15
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BEEHIVE), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS, 6), new SimpleStack(Items.HONEYCOMB, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.HONEY_BLOCK), new SimpleStack[] { new SimpleStack(Items.HONEY_BOTTLE, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.HONEYCOMB_BLOCK), new SimpleStack[] { new SimpleStack(Items.HONEYCOMB, 4) }));
        // added in 1.14.4
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_RED_SANDSTONE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.SMOOTH_RED_SANDSTONE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_ANDESITE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.POLISHED_ANDESITE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_GRANITE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.POLISHED_GRANITE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.STONE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.END_STONE_BRICK_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.END_STONE_BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRANITE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.GRANITE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MOSSY_STONE_BRICK_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.MOSSY_STONE_BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ANDESITE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.ANDESITE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_NETHER_BRICK_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.RED_NETHER_BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_SANDSTONE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.SMOOTH_SANDSTONE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_QUARTZ_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.SMOOTH_QUARTZ, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DIORITE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.DIORITE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_DIORITE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.POLISHED_DIORITE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MOSSY_COBBLESTONE_STAIRS, 2), new SimpleStack[] { new SimpleStack(Blocks.MOSSY_COBBLESTONE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_NETHER_BRICK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.RED_NETHER_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MOSSY_STONE_BRICK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.MOSSY_STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ANDESITE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.ANDESITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CUT_SANDSTONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.CUT_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_RED_SANDSTONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.SMOOTH_RED_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_GRANITE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.POLISHED_GRANITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_ANDESITE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.POLISHED_ANDESITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_QUARTZ_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.SMOOTH_QUARTZ) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_STONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.SMOOTH_STONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MOSSY_COBBLESTONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.MOSSY_COBBLESTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRANITE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.GRANITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DIORITE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.DIORITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.END_STONE_BRICK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.END_STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_SANDSTONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.SMOOTH_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_DIORITE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.POLISHED_DIORITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CUT_RED_SANDSTONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.CUT_RED_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DIORITE_WALL), new SimpleStack[] { new SimpleStack(Blocks.DIORITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SANDSTONE_WALL), new SimpleStack[] { new SimpleStack(Blocks.SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_SANDSTONE_WALL), new SimpleStack[] { new SimpleStack(Blocks.RED_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.NETHER_BRICK_WALL), new SimpleStack[] { new SimpleStack(Blocks.NETHER_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BRICK_WALL), new SimpleStack[] { new SimpleStack(Blocks.BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRANITE_WALL), new SimpleStack[] { new SimpleStack(Blocks.GRANITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MOSSY_STONE_BRICK_WALL), new SimpleStack[] { new SimpleStack(Blocks.MOSSY_STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PRISMARINE_WALL), new SimpleStack[] { new SimpleStack(Blocks.PRISMARINE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_NETHER_BRICK_WALL), new SimpleStack[] { new SimpleStack(Blocks.RED_NETHER_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ANDESITE_WALL), new SimpleStack[] { new SimpleStack(Blocks.ANDESITE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.END_STONE_BRICK_WALL), new SimpleStack[] { new SimpleStack(Blocks.END_STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE_BRICK_WALL), new SimpleStack[] { new SimpleStack(Blocks.STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONECUTTER), new SimpleStack[] { new SimpleStack(Blocks.STONE, 3), new SimpleStack(Items.IRON_INGOT) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BONE_BLOCK), new SimpleStack[] { new SimpleStack(Items.BONE_MEAL, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.COAL_BLOCK), new SimpleStack[] { new SimpleStack(Items.COAL, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DIAMOND_BLOCK), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GOLD_BLOCK), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.IRON_BLOCK), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.NETHER_WART_BLOCK), new SimpleStack[] { new SimpleStack(Blocks.NETHER_WART, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SLIME_BLOCK), new SimpleStack[] { new SimpleStack(Items.SLIME_BALL, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.EMERALD_BLOCK), new SimpleStack[] { new SimpleStack(Items.EMERALD, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.REDSTONE_BLOCK), new SimpleStack[] { new SimpleStack(Items.REDSTONE, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LAPIS_BLOCK), new SimpleStack[] { new SimpleStack(Items.LAPIS_LAZULI, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLUE_ICE), new SimpleStack[] { new SimpleStack(Blocks.PACKED_ICE, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PACKED_ICE), new SimpleStack[] { new SimpleStack(Blocks.ICE, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.HAY_BLOCK), new SimpleStack[] { new SimpleStack(Items.WHEAT, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SNOW_BLOCK), new SimpleStack[] { new SimpleStack(Items.SNOWBALL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BARREL), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS, 6), new SimpleStack(Blocks.ACACIA_SLAB, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LANTERN), new SimpleStack[] { new SimpleStack(Items.IRON_NUGGET, 8), new SimpleStack(Items.TORCH) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CARTOGRAPHY_TABLE), new SimpleStack[] { new SimpleStack(Items.PAPER, 2), new SimpleStack(Blocks.ACACIA_PLANKS, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.FLETCHING_TABLE), new SimpleStack[] { new SimpleStack(Items.FLINT, 2), new SimpleStack(Blocks.ACACIA_PLANKS, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMITHING_TABLE), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 2), new SimpleStack(Blocks.ACACIA_PLANKS, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.COMPOSTER), new SimpleStack[] { new SimpleStack(Items.DARK_OAK_FENCE, 4), new SimpleStack(Blocks.ACACIA_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLAST_FURNACE), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 5), new SimpleStack(Blocks.SMOOTH_STONE, 3), new SimpleStack(Blocks.FURNACE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRINDSTONE), new SimpleStack[] { new SimpleStack(Items.STICK, 2), new SimpleStack(Blocks.STONE_SLAB), new SimpleStack(Blocks.ACACIA_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LOOM), new SimpleStack[] { new SimpleStack(Items.STRING, 2), new SimpleStack(Blocks.ACACIA_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOKER), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_WOOD, 4), new SimpleStack(Blocks.FURNACE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CAMPFIRE), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_WOOD, 3), new SimpleStack(Items.STICK, 3), new SimpleStack(Items.CHARCOAL) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LECTERN), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_SLAB, 4), new SimpleStack(Blocks.BOOKSHELF) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.WRITABLE_BOOK), new SimpleStack[] { new SimpleStack(Items.BOOK), new SimpleStack(Items.INK_SAC), new SimpleStack(Items.FEATHER) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SCAFFOLDING, 6), new SimpleStack[] { new SimpleStack(Items.BAMBOO, 6), new SimpleStack(Items.STRING) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CUT_RED_SANDSTONE), new SimpleStack[] { new SimpleStack(Blocks.RED_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CUT_SANDSTONE), new SimpleStack[] { new SimpleStack(Blocks.SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LEATHER_HORSE_ARMOR), new SimpleStack[] { new SimpleStack(Items.LEATHER, 7) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.CROSSBOW), new SimpleStack[] { new SimpleStack(Items.STICK, 3), new SimpleStack(Items.IRON_INGOT), new SimpleStack(Items.STRING, 2), new SimpleStack(Items.TRIPWIRE_HOOK) }));
        // sign
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.ACACIA_SIGN, 3), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.OAK_SIGN, 3), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BIRCH_SIGN, 3), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.JUNGLE_SIGN, 3), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.SPRUCE_SIGN, 3), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DARK_OAK_SIGN, 3), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS, 6), new SimpleStack(Items.STICK) }));
        // granite
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRANITE), new SimpleStack[] { new SimpleStack(Blocks.DIORITE), new SimpleStack(Items.QUARTZ) }));
        // diorite
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DIORITE), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE), new SimpleStack(Items.QUARTZ) }));
        // andesite
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ANDESITE, 2), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE), new SimpleStack(Blocks.DIORITE) }));
        // paper
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.PAPER), new SimpleStack[] { new SimpleStack(Blocks.SUGAR_CANE) }));
        // sugar
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.SUGAR), new SimpleStack[] { new SimpleStack(Blocks.SUGAR_CANE) }));
        // ender eye
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.ENDER_EYE), new SimpleStack[] { new SimpleStack(Items.ENDER_PEARL), new SimpleStack(Items.BLAZE_POWDER) }));
        // blaze powder
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BLAZE_POWDER, 2), new SimpleStack[] { new SimpleStack(Items.BLAZE_ROD) }));
        // magma cream
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.MAGMA_CREAM), new SimpleStack[] { new SimpleStack(Items.BLAZE_POWDER), new SimpleStack(Items.SLIME_BALL) }));
        // fire charge
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.FIRE_CHARGE, 3), new SimpleStack[] { new SimpleStack(Items.BLAZE_POWDER), new SimpleStack(Items.GUNPOWDER), new SimpleStack(Items.COAL) }));

        // 1.9 recipes
        // purpur slab
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPUR_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.PURPUR_BLOCK) }));
        // end stone brick
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.END_STONE_BRICKS), new SimpleStack[] { new SimpleStack(Blocks.END_STONE) }));
        // purpur stair
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPUR_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.PURPUR_SLAB, 3) }));
        // purpur block
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPUR_BLOCK), new SimpleStack[] { new SimpleStack(Items.POPPED_CHORUS_FRUIT) }));
        // sculpted purpur
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPUR_PILLAR), new SimpleStack[] { new SimpleStack(Blocks.PURPUR_BLOCK) }));
        // end rod
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.END_ROD, 4), new SimpleStack[] { new SimpleStack(Items.BLAZE_ROD), new SimpleStack(Items.POPPED_CHORUS_FRUIT) }));
        // shield
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.SHIELD), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT), new SimpleStack(Blocks.OAK_PLANKS, 6) }));
        // block
        // stone
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE) }));
        // polished granite
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_GRANITE), new SimpleStack[] { new SimpleStack(Blocks.GRANITE) }));
        // polished diorite
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_DIORITE), new SimpleStack[] { new SimpleStack(Blocks.DIORITE) }));
        // polished andesite
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POLISHED_ANDESITE), new SimpleStack[] { new SimpleStack(Blocks.ANDESITE) }));
        // coarse dirt
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.COARSE_DIRT, 2), new SimpleStack[] { new SimpleStack(Blocks.DIRT), new SimpleStack(Blocks.GRAVEL) }));
        // clay
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CLAY), new SimpleStack[] { new SimpleStack(Items.CLAY_BALL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.CLAY) }));
        // stained terracotta
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLACK_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.INK_SAC)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_GRAY_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.LIGHT_GRAY_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_BLUE_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.LIGHT_BLUE_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BROWN_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.COCOA_BEANS)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CYAN_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.CYAN_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.YELLOW_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.DANDELION_YELLOW)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.WHITE_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.BONE_MEAL)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPLE_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.PURPLE_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MAGENTA_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.MAGENTA_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.ROSE_RED)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ORANGE_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.ORANGE_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GREEN_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.CACTUS_GREEN)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIME_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.LIME_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PINK_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.PINK_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLUE_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.LAPIS_LAZULI)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRAY_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.GRAY_DYE)
        // glazed terracotta
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLACK_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.BLACK_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_GRAY_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_BLUE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BROWN_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.BROWN_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CYAN_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.CYAN_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.YELLOW_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.YELLOW_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.WHITE_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.WHITE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPLE_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.PURPLE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MAGENTA_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.MAGENTA_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.RED_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ORANGE_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.ORANGE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GREEN_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.GREEN_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIME_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.LIME_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PINK_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.PINK_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLUE_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.BLUE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRAY_GLAZED_TERRACOTTA), new SimpleStack[] { new SimpleStack(Blocks.GRAY_TERRACOTTA) }));
        // mossy cobblestone
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MOSSY_COBBLESTONE), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE), new SimpleStack(Blocks.VINE) }));
        // glass
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GLASS), new SimpleStack[] { new SimpleStack(Blocks.SAND) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 3) }));
        // stained glass
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLACK_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.INK_SAC)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_GRAY_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.LIGHT_GRAY_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_BLUE_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.LIGHT_BLUE_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BROWN_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.COCOA_BEANS)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CYAN_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.CYAN_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.YELLOW_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.DANDELION_YELLOW)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.WHITE_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.BONE_MEAL)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPLE_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.PURPLE_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MAGENTA_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.MAGENTA_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.ROSE_RED)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ORANGE_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.ORANGE_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GREEN_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.CACTUS_GREEN)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIME_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.LIME_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PINK_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.PINK_DYE)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLUE_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.LAPIS_LAZULI)
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRAY_STAINED_GLASS, 8), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.GRAY_DYE)
        // glass pane
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLACK_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.BLACK_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_GRAY_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_BLUE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BROWN_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.BROWN_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CYAN_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.CYAN_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.YELLOW_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.YELLOW_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.WHITE_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.WHITE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPLE_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.PURPLE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MAGENTA_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.MAGENTA_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.RED_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ORANGE_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.ORANGE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GREEN_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.GREEN_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIME_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.LIME_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PINK_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.PINK_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLUE_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.BLUE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRAY_STAINED_GLASS_PANE, 8), new SimpleStack[] { new SimpleStack(Blocks.GRAY_STAINED_GLASS, 3) }));
        // sandstone
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SANDSTONE), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_SANDSTONE), new SimpleStack[] { new SimpleStack(Blocks.RED_SAND, 4) }));
        // chiseled smooth sandstone
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CHISELED_SANDSTONE), new SimpleStack[] { new SimpleStack(Blocks.SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_SANDSTONE), new SimpleStack[] { new SimpleStack(Blocks.SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CHISELED_RED_SANDSTONE), new SimpleStack[] { new SimpleStack(Blocks.RED_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SMOOTH_RED_SANDSTONE), new SimpleStack[] { new SimpleStack(Blocks.RED_SANDSTONE) }));
        // stonebrick
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE_BRICKS), new SimpleStack[] { new SimpleStack(Blocks.STONE) }));
        // cracked, mossy, chiseled stonebrick
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MOSSY_STONE_BRICKS), new SimpleStack[] { new SimpleStack(Blocks.STONE_BRICKS), new SimpleStack(Blocks.VINE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CHISELED_STONE_BRICKS), new SimpleStack[] { new SimpleStack(Blocks.STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CRACKED_STONE_BRICKS), new SimpleStack[] { new SimpleStack(Blocks.STONE_BRICKS) }));
        // bricks block
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BRICKS), new SimpleStack[] { new SimpleStack(Items.BRICK, 4) }));
        // glowstone
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GLOWSTONE), new SimpleStack[] { new SimpleStack(Items.GLOWSTONE_DUST, 4) }));
        // prismarine
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PRISMARINE), new SimpleStack[] { new SimpleStack(Items.PRISMARINE_SHARD, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PRISMARINE_BRICKS), new SimpleStack[] { new SimpleStack(Items.PRISMARINE_SHARD, 9) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_PRISMARINE), new SimpleStack[] { new SimpleStack(Items.PRISMARINE_SHARD, 8) })); //, new ItemStack(Items.INK_SAC)
        // sea lantern
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SEA_LANTERN), new SimpleStack[] { new SimpleStack(Items.PRISMARINE_CRYSTALS, 5), new SimpleStack(Items.PRISMARINE_SHARD, 4) }));
        // quartz block
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.QUARTZ_BLOCK), new SimpleStack[] { new SimpleStack(Items.QUARTZ, 4) }));
        // chiseled pillar quartz
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CHISELED_QUARTZ_BLOCK), new SimpleStack[] { new SimpleStack(Blocks.QUARTZ_BLOCK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.QUARTZ_PILLAR), new SimpleStack[] { new SimpleStack(Blocks.QUARTZ_BLOCK) }));
        // planks
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_PLANKS, 4), new SimpleStack[] { new SimpleStack(Blocks.OAK_LOG) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_PLANKS, 4), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_LOG) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_PLANKS, 4), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_LOG) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_PLANKS, 4), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_LOG) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_PLANKS, 4), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_LOG) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_PLANKS, 4), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_LOG) }));
        // brick
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BRICK), new SimpleStack[] { new SimpleStack(Items.CLAY_BALL) }));
        // snow
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SNOW), new SimpleStack[] { new SimpleStack(Items.SNOWBALL, 4) }));
        // machine
        // dispenser
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DISPENSER), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 7), new SimpleStack(Items.REDSTONE), new SimpleStack(Items.BOW) }));
        // noteblock
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.NOTE_BLOCK), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 8), new SimpleStack(Items.REDSTONE) }));
        // chest
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CHEST), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 8) }));
        // ender chest
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ENDER_CHEST), new SimpleStack[] { new SimpleStack(Blocks.OBSIDIAN, 8), new SimpleStack(Items.ENDER_EYE) }));
        // chest
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.TRAPPED_CHEST), new SimpleStack[] { new SimpleStack(Blocks.CHEST), new SimpleStack(Blocks.TRIPWIRE_HOOK) }));
        // crafting table
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CRAFTING_TABLE), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 4) }));
        // furnace
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.FURNACE), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 8) }));
        // jukebox
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUKEBOX), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 8), new SimpleStack(Items.DIAMOND) }));
        // enchantment table
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ENCHANTING_TABLE), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 2), new SimpleStack(Blocks.OBSIDIAN, 4), new SimpleStack(Items.BOOK) }));
        // beacon
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BEACON), new SimpleStack[] { new SimpleStack(Blocks.OBSIDIAN, 3), new SimpleStack(Items.NETHER_STAR), new SimpleStack(Blocks.GLASS, 5) }));
        // anvil
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ANVIL), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 31) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CHIPPED_ANVIL), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 20) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DAMAGED_ANVIL), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 10) }));
        // daylight sensor
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DAYLIGHT_DETECTOR), new SimpleStack[] { new SimpleStack(Blocks.OAK_SLAB, 3), new SimpleStack(Blocks.GLASS, 3), new SimpleStack(Items.QUARTZ, 3) }));
        // hopper
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.HOPPER), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 5), new SimpleStack(Blocks.CHEST) }));
        // dropper
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DROPPER), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 7), new SimpleStack(Items.REDSTONE) }));
        // rail
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RAIL, 16), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DETECTOR_RAIL, 6), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 6), new SimpleStack(Items.REDSTONE), new SimpleStack(Blocks.STONE_PRESSURE_PLATE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.POWERED_RAIL, 6), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 6), new SimpleStack(Items.REDSTONE), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACTIVATOR_RAIL, 6), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 6), new SimpleStack(Items.REDSTONE), new SimpleStack(Items.STICK, 2) }));
        // piston
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STICKY_PISTON, 1), new SimpleStack[] { new SimpleStack(Items.SLIME_BALL), new SimpleStack(Blocks.PISTON) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PISTON, 1), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT), new SimpleStack(Blocks.COBBLESTONE, 4), new SimpleStack(Items.REDSTONE), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        // wool
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.WHITE_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLACK_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRAY_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_GRAY_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.YELLOW_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ORANGE_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MAGENTA_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLUE_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_BLUE_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIME_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BROWN_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CYAN_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GREEN_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPLE_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PINK_WOOL), new SimpleStack[] { new SimpleStack(Items.STRING, 4) }));
        // wood slab
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS) }));
        // stone slab
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.STONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SANDSTONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.COBBLESTONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_SANDSTONE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.RED_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PRISMARINE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.PRISMARINE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PRISMARINE_BRICK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.PRISMARINE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_PRISMARINE_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.DARK_PRISMARINE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.QUARTZ_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.CHISELED_QUARTZ_BLOCK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.NETHER_BRICK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.NETHER_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE_BRICK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.CHISELED_STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BRICK_SLAB, 2), new SimpleStack[] { new SimpleStack(Blocks.BRICKS) }));
        // TNT
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.TNT), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Items.GUNPOWDER, 5) }));
        // bookshelf
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BOOKSHELF), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 6), new SimpleStack(Items.BOOK, 3) }));
        // torch
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.TORCH, 4), new SimpleStack[] { new SimpleStack(Items.COAL), new SimpleStack(Items.STICK) }));
        // wood stair
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.OAK_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_SLAB, 3) }));
        // stone stair
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SANDSTONE_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.SANDSTONE_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_SANDSTONE_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.RED_SANDSTONE_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.QUARTZ_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.CHISELED_QUARTZ_BLOCK, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.NETHER_BRICK_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.NETHER_BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.COBBLESTONE_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PRISMARINE_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.PRISMARINE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PRISMARINE_BRICK_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.PRISMARINE_BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_PRISMARINE_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.DARK_PRISMARINE, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BRICK_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE_BRICK_STAIRS), new SimpleStack[] { new SimpleStack(Blocks.CHISELED_STONE_BRICKS, 3) }));
        // ladder
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LADDER, 3), new SimpleStack[] { new SimpleStack(Items.STICK, 7) }));
        // lever
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LEVER), new SimpleStack[] { new SimpleStack(Items.STICK), new SimpleStack(Blocks.COBBLESTONE) }));
        // wood pressure plate
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS, 2) }));
        // stone pressure plate
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Blocks.STONE, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 2) }));
        // redstone torch
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.REDSTONE_TORCH), new SimpleStack[] { new SimpleStack(Items.REDSTONE), new SimpleStack(Items.STICK) }));
        // button
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_BUTTON), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_BUTTON), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_BUTTON), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_BUTTON), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_BUTTON), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_BUTTON), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.STONE_BUTTON), new SimpleStack[] { new SimpleStack(Blocks.STONE) }));
        // fence
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_FENCE, 3), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 4), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_FENCE, 3), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS, 4), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_FENCE, 3), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS, 4), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_FENCE, 3), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS, 4), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_FENCE, 3), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS, 4), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_FENCE, 3), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS, 4), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.NETHER_BRICK_FENCE), new SimpleStack[] { new SimpleStack(Blocks.NETHER_BRICKS), }));
        // fence gate
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_FENCE_GATE), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 2), new SimpleStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_FENCE_GATE), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS, 2), new SimpleStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_FENCE_GATE), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS, 2), new SimpleStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_FENCE_GATE), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS, 2), new SimpleStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_FENCE_GATE), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS, 2), new SimpleStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_FENCE_GATE), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS, 2), new SimpleStack(Items.STICK, 4) }));
        // lit pumpkin
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JACK_O_LANTERN), new SimpleStack[] { new SimpleStack(Blocks.CARVED_PUMPKIN), new SimpleStack(Blocks.TORCH) }));
        // trap
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_TRAPDOOR, 1), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_TRAPDOOR, 1), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_TRAPDOOR, 1), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_TRAPDOOR, 1), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_TRAPDOOR, 1), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_TRAPDOOR, 1), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.IRON_TRAPDOOR, 1), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 4) }));
        // iron bar
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.IRON_BARS), new SimpleStack[] { new SimpleStack(Items.IRON_NUGGET, 3) }));
        // redstone lamp
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.REDSTONE_LAMP), new SimpleStack[] { new SimpleStack(Items.REDSTONE, 4), new SimpleStack(Blocks.GLOWSTONE) }));
        // tripwire hook
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.TRIPWIRE_HOOK, 2), new SimpleStack[] { new SimpleStack(Items.STICK), new SimpleStack(Blocks.OAK_PLANKS), new SimpleStack(Items.IRON_INGOT) }));
        // cobblestone wall
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.COBBLESTONE_WALL), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MOSSY_COBBLESTONE_WALL), new SimpleStack[] { new SimpleStack(Blocks.MOSSY_COBBLESTONE) }));
        // carpet
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLACK_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.BLACK_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.WHITE_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.WHITE_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLUE_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.BLUE_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_BLUE_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_BLUE_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MAGENTA_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.MAGENTA_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CYAN_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.CYAN_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GREEN_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.GREEN_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.YELLOW_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.YELLOW_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIME_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.LIME_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PINK_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.PINK_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.RED_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRAY_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.GRAY_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_GRAY_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_GRAY_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BROWN_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.BROWN_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ORANGE_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.ORANGE_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPLE_CARPET, 3), new SimpleStack[] { new SimpleStack(Blocks.PURPLE_WOOL, 2) }));
        // flint and steel
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.FLINT_AND_STEEL), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT), new SimpleStack(Items.FLINT) }));
        // stick
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.STICK, 2), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS) }));
        // bowl
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BOWL, 4), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        // door
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OAK_DOOR), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.SPRUCE_DOOR), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BIRCH_DOOR), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.JUNGLE_DOOR), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ACACIA_DOOR), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DARK_OAK_DOOR), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.IRON_DOOR), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 2) }));
        // painting
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.PAINTING), new SimpleStack[] { new SimpleStack(Items.STICK, 8), new SimpleStack(Blocks.WHITE_WOOL) }));
        // empty bucket
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BUCKET), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 3) }));
        // minecart
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.MINECART), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.HOPPER_MINECART), new SimpleStack[] { new SimpleStack(Items.MINECART), new SimpleStack(Blocks.HOPPER) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.TNT_MINECART), new SimpleStack[] { new SimpleStack(Items.MINECART), new SimpleStack(Blocks.TNT, 1) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.FURNACE_MINECART), new SimpleStack[] { new SimpleStack(Items.MINECART), new SimpleStack(Blocks.FURNACE) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.CHEST_MINECART), new SimpleStack[] { new SimpleStack(Items.MINECART), new SimpleStack(Blocks.CHEST) }));
        // boat
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.OAK_BOAT), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.SPRUCE_BOAT), new SimpleStack[] { new SimpleStack(Blocks.SPRUCE_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BIRCH_BOAT), new SimpleStack[] { new SimpleStack(Blocks.BIRCH_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.JUNGLE_BOAT), new SimpleStack[] { new SimpleStack(Blocks.JUNGLE_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.ACACIA_BOAT), new SimpleStack[] { new SimpleStack(Blocks.ACACIA_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DARK_OAK_BOAT), new SimpleStack[] { new SimpleStack(Blocks.DARK_OAK_PLANKS, 5) }));
        // book
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BOOK), new SimpleStack[] { new SimpleStack(Items.PAPER, 3), new SimpleStack(Items.LEATHER) }));
        // compass
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.COMPASS), new SimpleStack[] { new SimpleStack(Items.REDSTONE), new SimpleStack(Items.IRON_INGOT, 4) }));
        // fishing rod
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.FISHING_ROD), new SimpleStack[] { new SimpleStack(Items.STRING, 2), new SimpleStack(Items.STICK, 3) }));
        // clock
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.CLOCK), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 4), new SimpleStack(Items.REDSTONE) }));
        // redstone repeater
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.REPEATER), new SimpleStack[] { new SimpleStack(Blocks.STONE, 3), new SimpleStack(Blocks.REDSTONE_TORCH, 2), new SimpleStack(Items.REDSTONE) }));
        // redstone comparator
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.COMPARATOR), new SimpleStack[] { new SimpleStack(Blocks.STONE, 3), new SimpleStack(Blocks.REDSTONE_TORCH), new SimpleStack(Items.QUARTZ) }));
        // glass bottle
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GLASS_BOTTLE), new SimpleStack[] { new SimpleStack(Blocks.GLASS) }));
        // brewing stand
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BREWING_STAND), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 3), new SimpleStack(Items.BLAZE_ROD) }));
        // cauldron
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CAULDRON), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 7) }));
        // item frame
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.ITEM_FRAME), new SimpleStack[] { new SimpleStack(Items.STICK, 8), new SimpleStack(Items.LEATHER) }));
        // flower pot
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.FLOWER_POT), new SimpleStack[] { new SimpleStack(Items.BRICK, 3) }));
        // carrot on a stick
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.CARROT_ON_A_STICK), new SimpleStack[] { new SimpleStack(Items.CARROT), new SimpleStack(Items.FISHING_ROD) }));
        // armor stand
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.ARMOR_STAND), new SimpleStack[] { new SimpleStack(Items.STICK, 6), new SimpleStack(Blocks.STONE_SLAB) }));
        // lead
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LEAD, 2), new SimpleStack[] { new SimpleStack(Items.STRING, 4), new SimpleStack(Items.SLIME_BALL) }));
        // banner
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BLACK_BANNER), new SimpleStack[] { new SimpleStack(Blocks.BLACK_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.WHITE_BANNER), new SimpleStack[] { new SimpleStack(Blocks.WHITE_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BLUE_BANNER), new SimpleStack[] { new SimpleStack(Blocks.BLUE_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LIGHT_BLUE_BANNER), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_BLUE_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GRAY_BANNER), new SimpleStack[] { new SimpleStack(Blocks.GRAY_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LIGHT_GRAY_BANNER), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_GRAY_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GREEN_BANNER), new SimpleStack[] { new SimpleStack(Blocks.GREEN_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LIME_BANNER), new SimpleStack[] { new SimpleStack(Blocks.LIME_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.YELLOW_BANNER), new SimpleStack[] { new SimpleStack(Blocks.YELLOW_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.MAGENTA_BANNER), new SimpleStack[] { new SimpleStack(Blocks.MAGENTA_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.CYAN_BANNER), new SimpleStack[] { new SimpleStack(Blocks.CYAN_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.PURPLE_BANNER), new SimpleStack[] { new SimpleStack(Blocks.PURPLE_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.PINK_BANNER), new SimpleStack[] { new SimpleStack(Blocks.PINK_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BROWN_BANNER), new SimpleStack[] { new SimpleStack(Blocks.BROWN_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.ORANGE_BANNER), new SimpleStack[] { new SimpleStack(Blocks.ORANGE_WOOL, 6), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.PURPLE_BANNER), new SimpleStack[] { new SimpleStack(Blocks.PURPLE_WOOL, 6), new SimpleStack(Items.STICK) }));
        // end crystal
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.END_CRYSTAL), new SimpleStack[] { new SimpleStack(Blocks.GLASS, 7), new SimpleStack(Items.ENDER_EYE), new SimpleStack(Items.GHAST_TEAR) }));
        // empty map
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.MAP), new SimpleStack[] { new SimpleStack(Items.COMPASS), new SimpleStack(Items.PAPER, 8) }));
        // shears
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.SHEARS), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 2) }));
        // pickaxe
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.WOODEN_PICKAXE), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 3), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.STONE_PICKAXE), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 3), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_PICKAXE), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 3), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_PICKAXE), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 3), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_PICKAXE), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 3), new SimpleStack(Items.STICK, 2) }));
        // axe
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.WOODEN_AXE), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 3), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.STONE_AXE), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 3), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_AXE), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 3), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_AXE), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 3), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_AXE), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 3), new SimpleStack(Items.STICK, 2) }));
        // shovel/spade
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.WOODEN_SHOVEL), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.STONE_SHOVEL), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_SHOVEL), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_SHOVEL), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_SHOVEL), new SimpleStack[] { new SimpleStack(Items.DIAMOND), new SimpleStack(Items.STICK, 2) }));
        // sword
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.WOODEN_SWORD), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 2), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.STONE_SWORD), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 2), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_SWORD), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 2), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_SWORD), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 2), new SimpleStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_SWORD), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 2), new SimpleStack(Items.STICK) }));
        // hoe
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.WOODEN_HOE), new SimpleStack[] { new SimpleStack(Blocks.OAK_PLANKS, 2), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.STONE_HOE), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 2), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_HOE), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 2), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_HOE), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 2), new SimpleStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_HOE), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 2), new SimpleStack(Items.STICK, 2) }));
        // bow
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BOW), new SimpleStack[] { new SimpleStack(Items.STRING, 3), new SimpleStack(Items.STICK, 3) }));
        // arrow
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.ARROW), new SimpleStack[] { new SimpleStack(Items.FEATHER), new SimpleStack(Items.STICK), new SimpleStack(Items.FLINT) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.SPECTRAL_ARROW, 2), new SimpleStack[] { new SimpleStack(Items.GLOWSTONE_DUST, 4), new SimpleStack(Items.ARROW) }));
        // armor
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LEATHER_BOOTS), new SimpleStack[] { new SimpleStack(Items.LEATHER, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LEATHER_HELMET), new SimpleStack[] { new SimpleStack(Items.LEATHER, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LEATHER_CHESTPLATE), new SimpleStack[] { new SimpleStack(Items.LEATHER, 8) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LEATHER_LEGGINGS), new SimpleStack[] { new SimpleStack(Items.LEATHER, 7) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_BOOTS), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_HELMET), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_CHESTPLATE), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 8) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.IRON_LEGGINGS), new SimpleStack[] { new SimpleStack(Items.IRON_INGOT, 7) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_BOOTS), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_HELMET), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_CHESTPLATE), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 8) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GOLDEN_LEGGINGS), new SimpleStack[] { new SimpleStack(Items.GOLD_INGOT, 7) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_BOOTS), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_HELMET), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_CHESTPLATE), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 8) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND_LEGGINGS), new SimpleStack[] { new SimpleStack(Items.DIAMOND, 7) }));
        // 1.12
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.NETHER_BRICKS), new SimpleStack[] { new SimpleStack(Items.NETHER_BRICK, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_NETHER_BRICKS), new SimpleStack[] { new SimpleStack(Items.NETHER_BRICK, 2), new SimpleStack(Items.NETHER_WART, 2) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MAGMA_BLOCK), new SimpleStack[] { new SimpleStack(Items.MAGMA_CREAM, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.OBSERVER), new SimpleStack[] { new SimpleStack(Blocks.COBBLESTONE, 6), new SimpleStack(Items.REDSTONE, 2), new SimpleStack(Items.QUARTZ) }));
        // concrete powder
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLACK_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.WHITE_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GRAY_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_GRAY_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BLUE_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIGHT_BLUE_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.YELLOW_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.LIME_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.GREEN_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CYAN_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.MAGENTA_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.BROWN_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PINK_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.PURPLE_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.ORANGE_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.RED_CONCRETE_POWDER, 8), new SimpleStack[] { new SimpleStack(Blocks.SAND, 4), new SimpleStack(Blocks.GRAVEL, 4) }));
        // bed
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BLACK_BED), new SimpleStack[] { new SimpleStack(Blocks.BLACK_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.WHITE_BED), new SimpleStack[] { new SimpleStack(Blocks.WHITE_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GRAY_BED), new SimpleStack[] { new SimpleStack(Blocks.GRAY_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LIGHT_GRAY_BED), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_GRAY_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BLUE_BED), new SimpleStack[] { new SimpleStack(Blocks.BLUE_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LIGHT_BLUE_BED), new SimpleStack[] { new SimpleStack(Blocks.LIGHT_BLUE_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.YELLOW_BED), new SimpleStack[] { new SimpleStack(Blocks.YELLOW_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.LIME_BED), new SimpleStack[] { new SimpleStack(Blocks.LIME_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.GREEN_BED), new SimpleStack[] { new SimpleStack(Blocks.GREEN_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.CYAN_BED), new SimpleStack[] { new SimpleStack(Blocks.CYAN_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.MAGENTA_BED), new SimpleStack[] { new SimpleStack(Blocks.MAGENTA_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.BROWN_BED), new SimpleStack[] { new SimpleStack(Blocks.BROWN_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.PINK_BED), new SimpleStack[] { new SimpleStack(Blocks.PINK_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.PURPLE_BED), new SimpleStack[] { new SimpleStack(Blocks.PURPLE_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.ORANGE_BED), new SimpleStack[] { new SimpleStack(Blocks.ORANGE_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.RED_BED), new SimpleStack[] { new SimpleStack(Blocks.RED_WOOL, 3), new SimpleStack(Blocks.OAK_PLANKS, 3) }));
        // 1.13
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.TURTLE_HELMET), new SimpleStack[] { new SimpleStack(Items.SCUTE, 5) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.CONDUIT), new SimpleStack[] { new SimpleStack(Items.NAUTILUS_SHELL, 8), new SimpleStack(Items.HEART_OF_THE_SEA) }));
        recipes.add(new RecyclingRecipe(new SimpleStack(Blocks.DRIED_KELP_BLOCK), new SimpleStack[] { new SimpleStack(Items.DRIED_KELP, 9) }));

    }
}
