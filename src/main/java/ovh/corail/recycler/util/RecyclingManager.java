package ovh.corail.recycler.util;

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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.ImmutablePair;
import ovh.corail.recycler.ConfigRecycler;
import ovh.corail.recycler.registry.ModBlocks;
import ovh.corail.recycler.registry.ModItems;

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
    private final Set<ItemStack> unbalanced = new HashSet<>();
    private final Set<ItemStack> blacklist = new HashSet<>();
    private final NonNullList<ImmutablePair<ItemStack, ItemStack>> grindList = NonNullList.create();
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

    public void discoverRecipe(ServerPlayerEntity player, ItemStack stack) {
        int hasRecipe = getRecipeIndex(stack);
        // Recipe already in recycler
        if (hasRecipe >= 0) {
            // isn't blacklist
            if (getRecipe(hasRecipe).isAllowed()) {
                LangKey.MESSAGE_ADD_RECIPE_FAILED.sendMessage(player);
            } else {
                getRecipe(hasRecipe).setAllowed(true);
                saveBlacklist();
                LangKey.MESSAGE_ADD_RECIPE_SUCCESS.sendMessage(player);
            }
        } else {
            // new recipe added
            boolean valid = false;
            RecyclingRecipe recipe = null;
            for (IRecipe crafting_recipe : player.world.getRecipeManager().getRecipes(IRecipeType.CRAFTING).values()) {
                ItemStack o = crafting_recipe.getRecipeOutput();
                if (Helper.areItemEqual(o, stack)) {
                    recipe = convertCraftingRecipe(crafting_recipe);
                    if (recipe.getCount() > 0 && !recipe.getItemRecipe().isEmpty()) {
                        valid = true;
                        break;
                    }
                }
            }
            // add recipe and save user defined recipes to json
            if (valid) {
                addRecipe(recipe);
                (saveUserDefinedRecipes() ? LangKey.MESSAGE_ADD_RECIPE_SUCCESS : LangKey.MESSAGE_ADD_RECIPE_FAILED).sendMessage(player);
            } else {
                LangKey.MESSAGE_ADD_RECIPE_FAILED.sendMessage(player);
            }
        }
    }

    public void loadRecipes() {
        clear();
        // load enchanted book recipe
        if (ConfigRecycler.general.recycle_enchanted_book.get()) {
            this.recipes.add(new RecyclingRecipe(new ItemStack(Items.ENCHANTED_BOOK), new ItemStack[] {}));
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
            saveAsJson(this.unbalancedFile, this.unbalanced.stream().map(this::itemStackToString).collect(Collectors.toCollection(NonNullList::create)));
        } else {
            loadAsJson(this.unbalancedFile, String.class).forEach(c -> {
                ItemStack currentStack = stringToItemStack(c);
                if (!currentStack.isEmpty()) {
                    this.unbalanced.add(currentStack);
                }
            });
        }
    }

    private void loadBlacklist() {
        if (!this.blacklistFile.exists()) {
            addToCollection(this.blacklist, ModBlocks.recycler);
            saveAsJson(this.blacklistFile, this.blacklist.stream().map(this::itemStackToString).collect(Collectors.toCollection(NonNullList::create)));
        } else {
            loadAsJson(this.blacklistFile, String.class).forEach(c -> {
                ItemStack currentStack = stringToItemStack(c);
                if (!currentStack.isEmpty()) {
                    this.blacklist.add(currentStack);
                }
            });
        }
    }

    private void saveBlacklist() {
        saveAsJson(this.blacklistFile, this.recipes.stream().filter(p -> !p.isAllowed()).map(recipe -> itemStackToString(recipe.getItemRecipe())).collect(Collectors.toCollection(NonNullList::create)));
    }

    private RecyclingRecipe getRecipe(int index) {
        return this.recipes.get(index);
    }

    private void addRecipe(RecyclingRecipe recipe) {
        this.recipes.add(recipe);
    }

    public boolean removeRecipe(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        int index = getRecipeIndex(stack);
        if (index < 0) {
            return false;
        }
        if (this.recipes.get(index).isUserDefined()) {
            this.recipes.remove(index);
            saveUserDefinedRecipes();
        } else {
            this.recipes.get(index).setAllowed(false);
            saveBlacklist();
        }
        return true;
    }

    public NonNullList<RecyclingRecipe> getRecipesForSearch(String searchText) {
        return this.recipes.stream().filter(p -> p.isAllowed() && (ConfigRecycler.general.unbalanced_recipes.get() || !p.isUnbalanced()) && (searchText.isEmpty() || p.getItemRecipe().getDisplayName().getUnformattedComponentText().toLowerCase().contains(searchText))).collect(Collectors.toCollection(NonNullList::create));
    }

    @Nullable
    public RecyclingRecipe getRecipe(ItemStack stack) {
        int recipeId = hasRecipe(stack);
        return recipeId >= 0 ? this.recipes.get(recipeId) : null;
    }

    public int hasRecipe(ItemStack stack) {
        // enchanted book requires 2 enchants to be recycled
        if (stack.isEmpty() || (stack.getItem() == Items.ENCHANTED_BOOK && EnchantedBookItem.getEnchantments(stack).size() < 2)) {
            return -1;
        }
        int recipeId = getRecipeIndex(stack);
        if (recipeId < 0) {
            return -1;
        }
        RecyclingRecipe recipe = this.recipes.get(recipeId);
        // unbalanced recipes
        if (!ConfigRecycler.general.unbalanced_recipes.get() && recipe.isUnbalanced()) {
            return -1;
        }
        // only user defined recipes
        if (ConfigRecycler.general.only_user_recipes.get() && !recipe.isUserDefined()) {
            return -1;
        }
        // only allowed recipes
        if (!recipe.isAllowed()) {
            return -1;
        }
        return recipeId;
    }

    private int getRecipeIndex(ItemStack stack) {
        return stack.isEmpty() ? -1 : IntStream.range(0, this.recipes.size()).filter(slotId -> Helper.areItemEqual(stack, this.recipes.get(slotId).getItemRecipe())).findFirst().orElse(-1);
    }

    public NonNullList<ItemStack> getResultStack(ItemStack stack, int nb_input) {
        return getResultStack(stack, nb_input, false);
    }

    // TODO clean this
    public NonNullList<ItemStack> getResultStack(ItemStack stack, int nb_input, boolean half) {
        NonNullList<ItemStack> itemsList = NonNullList.create();
        int num_recipe = hasRecipe(stack);
        if (num_recipe < 0) {
            return itemsList;
        }

        RecyclingRecipe currentRecipe = recipes.get(num_recipe);
        ItemStack currentStack;
        int currentSize;
        boolean isDamagedStack = stack.getMaxDamage() > 0 && stack.getDamage() > 0;
        // foreach stacks in the recipe
        for (int i = 0; i < currentRecipe.getCount(); i++) {
            // smaller units for damaged items and when there're losses
            ItemStack grind = isDamagedStack || half ? getGrind(currentRecipe.getResult(i)) : ItemStack.EMPTY;
            if (grind.isEmpty()) {
                currentStack = currentRecipe.getResult(i).copy();
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
            this.grindList.add(new ImmutablePair(new ItemStack(Items.DIAMOND), new ItemStack(ModItems.diamond_shard, 9)));
            this.grindList.add(new ImmutablePair(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_NUGGET, 9)));
            this.grindList.add(new ImmutablePair(new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GOLD_NUGGET, 9)));
            this.grindList.add(new ImmutablePair(new ItemStack(Items.LEATHER), new ItemStack(Items.RABBIT_HIDE, 4)));
            this.grindList.add(new ImmutablePair(new ItemStack(Blocks.OAK_PLANKS), new ItemStack(Items.STICK, 4)));
            saveAsJson(this.grindFile, this.grindList.stream().map(p -> new ImmutablePair(itemStackToString(p.getLeft()), itemStackToString(p.getRight()))).collect(Collectors.toCollection(NonNullList::create)));
        } else {
            Type token = new TypeToken<NonNullList<ImmutablePair<String, String>>>() {
            }.getType();
            NonNullList<ImmutablePair<String, String>> jsonStringList = (NonNullList<ImmutablePair<String, String>>) loadAsJson(this.grindFile, token);
            ItemStack input, output;
            for (ImmutablePair<String, String> pair : jsonStringList) {
                input = stringToItemStack(pair.getLeft());
                output = stringToItemStack(pair.getRight());
                if (!input.isEmpty() && !output.isEmpty()) {
                    this.grindList.add(new ImmutablePair(input, output));
                }
            }
        }
    }

    private ItemStack getGrind(ItemStack stack) {
        // only call when stack is damaged or there's losses to get smaller units
        for (ImmutablePair<ItemStack, ItemStack> grind : grindList) {
            if (Helper.areItemEqual(grind.getLeft(), stack)) {
                return grind.getRight().copy();
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean saveUserDefinedRecipes() {
        return saveAsJson(this.userDefinedFile, this.recipes.stream().filter(RecyclingRecipe::isUserDefined).map(this::convertRecipeToJson).collect(Collectors.toCollection(NonNullList::create)));
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
                recipe.setAllowed(this.blacklist.stream().noneMatch(p -> Helper.areItemEqual(p, recipe.getItemRecipe())));
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
        ItemStack inputItem = stringToItemStack(jRecipe.inputItem);
        if (inputItem.isEmpty()) {
            return null;
        }
        RecyclingRecipe recipe = new RecyclingRecipe(inputItem);
        Arrays.stream(jRecipe.outputItems).map(this::stringToItemStack).filter(outputItem -> !outputItem.isEmpty()).forEach(recipe::addStack);
        recipe.setUnbalanced(this.unbalanced.stream().anyMatch(p -> Helper.areItemEqual(p, recipe.getItemRecipe())));
        return recipe;
    }

    @Nullable
    public JsonRecyclingRecipe convertRecipeToJson(RecyclingRecipe recipe) {
        String inputItem = itemStackToString(recipe.getItemRecipe());
        return inputItem.isEmpty() ? null : new JsonRecyclingRecipe(inputItem, IntStream.range(0, recipe.getCount()).mapToObj(i -> itemStackToString(recipe.getResult(i))).toArray(String[]::new));
    }

    private String itemStackToString(ItemStack stack) {
        assert stack.getItem().getRegistryName() != null;
        return stack.getItem().getRegistryName().toString() + ":" + stack.getCount();
    }

    private ItemStack stringToItemStack(String stringStack) {
        String[] parts = stringStack.split(":");
        if (parts.length == 2 || parts.length == 3) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0], parts[1]));
            if (item != null) {
                return new ItemStack(item, parts.length == 3 ? Integer.valueOf(parts[2]) : 1);
            }
        }
        return ItemStack.EMPTY;
    }

    private void addToCollection(Collection<ItemStack> list, @Nullable Block block) {
        if (block != null) {
            addToCollection(list, block.asItem());
        }
    }

    private void addToCollection(Collection<ItemStack> list, @Nullable Item item) {
        if (item != null && item.getRegistryName() != null) {
            list.add(new ItemStack(item));
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends IRecipe> RecyclingRecipe convertCraftingRecipe(T iRecipe) {
        NonNullList<Ingredient> ingredients = iRecipe.getIngredients();
        RecyclingRecipe recipe = new RecyclingRecipe(iRecipe.getRecipeOutput(), Helper.mergeStackInList(ingredients.stream().filter(p -> p.getMatchingStacks().length > 0 && !p.getMatchingStacks()[0].isEmpty()).map(m -> m.getMatchingStacks()[0]).collect(Collectors.toCollection(NonNullList::create))));
        recipe.setUnbalanced(false);
        recipe.setUserDefined(true);
        recipe.setAllowed(true);
        return recipe;
    }

    private void loadDefaultRecipes() {
        // granite
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GRANITE), new ItemStack[] { new ItemStack(Blocks.DIORITE), new ItemStack(Items.QUARTZ) }));
        // diorite
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DIORITE), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE), new ItemStack(Items.QUARTZ) }));
        // andesite
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ANDESITE, 2), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.DIORITE) }));
        // paper
        recipes.add(new RecyclingRecipe(new ItemStack(Items.PAPER), new ItemStack[] { new ItemStack(Blocks.SUGAR_CANE) }));
        // sugar
        recipes.add(new RecyclingRecipe(new ItemStack(Items.SUGAR), new ItemStack[] { new ItemStack(Blocks.SUGAR_CANE) }));
        // ender eye
        recipes.add(new RecyclingRecipe(new ItemStack(Items.ENDER_EYE), new ItemStack[] { new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.BLAZE_POWDER) }));
        // blaze powder
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BLAZE_POWDER, 2), new ItemStack[] { new ItemStack(Items.BLAZE_ROD) }));
        // magma cream
        recipes.add(new RecyclingRecipe(new ItemStack(Items.MAGMA_CREAM), new ItemStack[] { new ItemStack(Items.BLAZE_POWDER), new ItemStack(Items.SLIME_BALL) }));
        // fire charge
        recipes.add(new RecyclingRecipe(new ItemStack(Items.FIRE_CHARGE, 3), new ItemStack[] { new ItemStack(Items.BLAZE_POWDER), new ItemStack(Items.GUNPOWDER), new ItemStack(Items.COAL) }));

        // 1.9 recipes
        // purpur slab
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPUR_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.PURPUR_BLOCK) }));
        // end stone brick
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.END_STONE_BRICKS), new ItemStack[] { new ItemStack(Blocks.END_STONE) }));
        // purpur stair
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPUR_STAIRS), new ItemStack[] { new ItemStack(Blocks.PURPUR_SLAB, 3) }));
        // purpur block
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPUR_BLOCK), new ItemStack[] { new ItemStack(Items.POPPED_CHORUS_FRUIT) }));
        // sculpted purpur
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPUR_PILLAR), new ItemStack[] { new ItemStack(Blocks.PURPUR_BLOCK) }));
        // end rod
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.END_ROD, 4), new ItemStack[] { new ItemStack(Items.BLAZE_ROD), new ItemStack(Items.POPPED_CHORUS_FRUIT) }));
        // shield
        recipes.add(new RecyclingRecipe(new ItemStack(Items.SHIELD), new ItemStack[] { new ItemStack(Items.IRON_INGOT), new ItemStack(Blocks.OAK_PLANKS, 6) }));
        // block
        // stone
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE) }));
        // polished granite
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.POLISHED_GRANITE), new ItemStack[] { new ItemStack(Blocks.GRANITE) }));
        // polished diorite
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.POLISHED_DIORITE), new ItemStack[] { new ItemStack(Blocks.DIORITE) }));
        // polished andesite
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.POLISHED_ANDESITE), new ItemStack[] { new ItemStack(Blocks.ANDESITE) }));
        // coarse dirt
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.COARSE_DIRT, 2), new ItemStack[] { new ItemStack(Blocks.DIRT), new ItemStack(Blocks.GRAVEL) }));
        // clay
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CLAY), new ItemStack[] { new ItemStack(Items.CLAY_BALL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.CLAY) }));
        // stained terracotta
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLACK_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.INK_SAC)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_GRAY_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.LIGHT_GRAY_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_BLUE_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.LIGHT_BLUE_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BROWN_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.COCOA_BEANS)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CYAN_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.CYAN_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.YELLOW_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.DANDELION_YELLOW)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WHITE_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.BONE_MEAL)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPLE_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.PURPLE_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MAGENTA_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.MAGENTA_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.ROSE_RED)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ORANGE_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.ORANGE_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GREEN_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.CACTUS_GREEN)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIME_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.LIME_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PINK_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.PINK_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLUE_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.LAPIS_LAZULI)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GRAY_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.TERRACOTTA, 8) })); //, new ItemStack(Items.GRAY_DYE)
        // glazed terracotta
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLACK_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.BLACK_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.LIGHT_GRAY_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.LIGHT_BLUE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BROWN_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.BROWN_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CYAN_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.CYAN_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.YELLOW_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.YELLOW_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WHITE_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.WHITE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPLE_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.PURPLE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MAGENTA_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.MAGENTA_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.RED_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ORANGE_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.ORANGE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GREEN_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.GREEN_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIME_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.LIME_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PINK_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.PINK_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLUE_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.BLUE_TERRACOTTA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GRAY_GLAZED_TERRACOTTA), new ItemStack[] { new ItemStack(Blocks.GRAY_TERRACOTTA) }));
        // mossy cobblestone
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MOSSY_COBBLESTONE), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.VINE) }));
        // glass
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GLASS), new ItemStack[] { new ItemStack(Blocks.SAND) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 3) }));
        // stained glass
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLACK_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.INK_SAC)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_GRAY_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.LIGHT_GRAY_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_BLUE_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.LIGHT_BLUE_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BROWN_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.COCOA_BEANS)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CYAN_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.CYAN_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.YELLOW_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.DANDELION_YELLOW)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WHITE_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.BONE_MEAL)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPLE_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.PURPLE_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MAGENTA_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.MAGENTA_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.ROSE_RED)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ORANGE_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.ORANGE_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GREEN_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.CACTUS_GREEN)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIME_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.LIME_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PINK_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.PINK_DYE)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLUE_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.LAPIS_LAZULI)
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GRAY_STAINED_GLASS, 8), new ItemStack[] { new ItemStack(Blocks.GLASS, 8) })); //, new ItemStack(Items.GRAY_DYE)
        // glass pane
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLACK_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.BLACK_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.LIGHT_GRAY_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.LIGHT_BLUE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BROWN_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.BROWN_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CYAN_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.CYAN_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.YELLOW_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.YELLOW_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WHITE_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.WHITE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPLE_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.PURPLE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MAGENTA_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.MAGENTA_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.RED_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ORANGE_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.ORANGE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GREEN_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.GREEN_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIME_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.LIME_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PINK_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.PINK_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLUE_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.BLUE_STAINED_GLASS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GRAY_STAINED_GLASS_PANE, 8), new ItemStack[] { new ItemStack(Blocks.GRAY_STAINED_GLASS, 3) }));
        // sandstone
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SANDSTONE), new ItemStack[] { new ItemStack(Blocks.SAND, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_SANDSTONE), new ItemStack[] { new ItemStack(Blocks.RED_SAND, 4) }));
        // chiseled smooth sandstone
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CHISELED_SANDSTONE), new ItemStack[] { new ItemStack(Blocks.SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SMOOTH_SANDSTONE), new ItemStack[] { new ItemStack(Blocks.SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CHISELED_RED_SANDSTONE), new ItemStack[] { new ItemStack(Blocks.RED_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SMOOTH_RED_SANDSTONE), new ItemStack[] { new ItemStack(Blocks.RED_SANDSTONE) }));
        // stonebrick
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_BRICKS), new ItemStack[] { new ItemStack(Blocks.STONE) }));
        // cracked, mossy, chiseled stonebrick
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MOSSY_STONE_BRICKS), new ItemStack[] { new ItemStack(Blocks.STONE_BRICKS), new ItemStack(Blocks.VINE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CHISELED_STONE_BRICKS), new ItemStack[] { new ItemStack(Blocks.STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CRACKED_STONE_BRICKS), new ItemStack[] { new ItemStack(Blocks.STONE_BRICKS) }));
        // bricks block
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BRICKS), new ItemStack[] { new ItemStack(Items.BRICK, 4) }));
        // glowstone
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GLOWSTONE), new ItemStack[] { new ItemStack(Items.GLOWSTONE_DUST, 4) }));
        // prismarine
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE), new ItemStack[] { new ItemStack(Items.PRISMARINE_SHARD, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE_BRICKS), new ItemStack[] { new ItemStack(Items.PRISMARINE_SHARD, 9) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_PRISMARINE), new ItemStack[] { new ItemStack(Items.PRISMARINE_SHARD, 8) })); //, new ItemStack(Items.INK_SAC)
        // sea lantern
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SEA_LANTERN), new ItemStack[] { new ItemStack(Items.PRISMARINE_CRYSTALS, 5), new ItemStack(Items.PRISMARINE_SHARD, 4) }));
        // quartz block
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.QUARTZ_BLOCK), new ItemStack[] { new ItemStack(Items.QUARTZ, 4) }));
        // chiseled pillar quartz
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CHISELED_QUARTZ_BLOCK), new ItemStack[] { new ItemStack(Blocks.QUARTZ_BLOCK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.QUARTZ_PILLAR), new ItemStack[] { new ItemStack(Blocks.QUARTZ_BLOCK) }));
        // planks
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_PLANKS, 4), new ItemStack[] { new ItemStack(Blocks.OAK_LOG) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_PLANKS, 4), new ItemStack[] { new ItemStack(Blocks.SPRUCE_LOG) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_PLANKS, 4), new ItemStack[] { new ItemStack(Blocks.BIRCH_LOG) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_PLANKS, 4), new ItemStack[] { new ItemStack(Blocks.JUNGLE_LOG) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_PLANKS, 4), new ItemStack[] { new ItemStack(Blocks.ACACIA_LOG) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_PLANKS, 4), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_LOG) }));
        // brick
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BRICK), new ItemStack[] { new ItemStack(Items.CLAY_BALL) }));
        // snow
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SNOW), new ItemStack[] { new ItemStack(Items.SNOWBALL, 4) }));
        // machine
        // dispenser
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DISPENSER), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 7), new ItemStack(Items.REDSTONE), new ItemStack(Items.BOW) }));
        // noteblock
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.NOTE_BLOCK), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Items.REDSTONE) }));
        // chest
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CHEST), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 8) }));
        // ender chest
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ENDER_CHEST), new ItemStack[] { new ItemStack(Blocks.OBSIDIAN, 8), new ItemStack(Items.ENDER_EYE) }));
        // chest
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TRAPPED_CHEST), new ItemStack[] { new ItemStack(Blocks.CHEST), new ItemStack(Blocks.TRIPWIRE_HOOK) }));
        // crafting table
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CRAFTING_TABLE), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 4) }));
        // furnace
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.FURNACE), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 8) }));
        // jukebox
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUKEBOX), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 8), new ItemStack(Items.DIAMOND) }));
        // enchantment table
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ENCHANTING_TABLE), new ItemStack[] { new ItemStack(Items.DIAMOND, 2), new ItemStack(Blocks.OBSIDIAN, 4), new ItemStack(Items.BOOK) }));
        // beacon
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BEACON), new ItemStack[] { new ItemStack(Blocks.OBSIDIAN, 3), new ItemStack(Items.NETHER_STAR), new ItemStack(Blocks.GLASS, 5) }));
        // anvil
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ANVIL), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 31) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CHIPPED_ANVIL), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 20) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DAMAGED_ANVIL), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 10) }));
        // daylight sensor
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DAYLIGHT_DETECTOR), new ItemStack[] { new ItemStack(Blocks.OAK_SLAB, 3), new ItemStack(Blocks.GLASS, 3), new ItemStack(Items.QUARTZ, 3) }));
        // hopper
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.HOPPER), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 5), new ItemStack(Blocks.CHEST) }));
        // dropper
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DROPPER), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 7), new ItemStack(Items.REDSTONE) }));
        // rail
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RAIL, 16), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DETECTOR_RAIL, 6), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 6), new ItemStack(Items.REDSTONE), new ItemStack(Blocks.STONE_PRESSURE_PLATE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.POWERED_RAIL, 6), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 6), new ItemStack(Items.REDSTONE), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACTIVATOR_RAIL, 6), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 6), new ItemStack(Items.REDSTONE), new ItemStack(Items.STICK, 2) }));
        // piston
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STICKY_PISTON, 1), new ItemStack[] { new ItemStack(Items.SLIME_BALL), new ItemStack(Blocks.PISTON) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PISTON, 1), new ItemStack[] { new ItemStack(Items.IRON_INGOT), new ItemStack(Blocks.COBBLESTONE, 4), new ItemStack(Items.REDSTONE), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        // wool
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WHITE_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLACK_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GRAY_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_GRAY_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.YELLOW_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ORANGE_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MAGENTA_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLUE_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_BLUE_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIME_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BROWN_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CYAN_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GREEN_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPLE_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PINK_WOOL), new ItemStack[] { new ItemStack(Items.STRING, 4) }));
        // wood slab
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.SPRUCE_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.BIRCH_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.JUNGLE_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.ACACIA_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_PLANKS) }));
        // stone slab
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.STONE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SANDSTONE_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.COBBLESTONE_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_SANDSTONE_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.RED_SANDSTONE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.PRISMARINE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE_BRICK_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.PRISMARINE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_PRISMARINE_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.DARK_PRISMARINE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.QUARTZ_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.CHISELED_QUARTZ_BLOCK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.NETHER_BRICK_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.NETHER_BRICKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_BRICK_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.CHISELED_STONE_BRICKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BRICK_SLAB, 2), new ItemStack[] { new ItemStack(Blocks.BRICKS) }));
        // TNT
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TNT), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Items.GUNPOWDER, 5) }));
        // bookshelf
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BOOKSHELF), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 6), new ItemStack(Items.BOOK, 3) }));
        // torch
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TORCH, 4), new ItemStack[] { new ItemStack(Items.COAL), new ItemStack(Items.STICK) }));
        // wood stair
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_STAIRS), new ItemStack[] { new ItemStack(Blocks.OAK_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_STAIRS), new ItemStack[] { new ItemStack(Blocks.SPRUCE_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_STAIRS), new ItemStack[] { new ItemStack(Blocks.BIRCH_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_STAIRS), new ItemStack[] { new ItemStack(Blocks.JUNGLE_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_STAIRS), new ItemStack[] { new ItemStack(Blocks.ACACIA_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_STAIRS), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_SLAB, 3) }));
        // stone stair
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SANDSTONE_STAIRS), new ItemStack[] { new ItemStack(Blocks.SANDSTONE_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_SANDSTONE_STAIRS), new ItemStack[] { new ItemStack(Blocks.RED_SANDSTONE_SLAB, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.QUARTZ_STAIRS), new ItemStack[] { new ItemStack(Blocks.CHISELED_QUARTZ_BLOCK, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.NETHER_BRICK_STAIRS), new ItemStack[] { new ItemStack(Blocks.NETHER_BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.COBBLESTONE_STAIRS), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE_STAIRS), new ItemStack[] { new ItemStack(Blocks.PRISMARINE, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PRISMARINE_BRICK_STAIRS), new ItemStack[] { new ItemStack(Blocks.PRISMARINE_BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_PRISMARINE_STAIRS), new ItemStack[] { new ItemStack(Blocks.DARK_PRISMARINE, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BRICK_STAIRS), new ItemStack[] { new ItemStack(Blocks.BRICKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_BRICK_STAIRS), new ItemStack[] { new ItemStack(Blocks.CHISELED_STONE_BRICKS, 3) }));
        // ladder
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LADDER, 3), new ItemStack[] { new ItemStack(Items.STICK, 7) }));
        // lever
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LEVER), new ItemStack[] { new ItemStack(Items.STICK), new ItemStack(Blocks.COBBLESTONE) }));
        // wood pressure plate
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Blocks.SPRUCE_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Blocks.BIRCH_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Blocks.JUNGLE_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Blocks.ACACIA_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_PLANKS, 2) }));
        // stone pressure plate
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Blocks.STONE, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 2) }));
        // redstone torch
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.REDSTONE_TORCH), new ItemStack[] { new ItemStack(Items.REDSTONE), new ItemStack(Items.STICK) }));
        // button
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_BUTTON), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_BUTTON), new ItemStack[] { new ItemStack(Blocks.SPRUCE_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_BUTTON), new ItemStack[] { new ItemStack(Blocks.BIRCH_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_BUTTON), new ItemStack[] { new ItemStack(Blocks.JUNGLE_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_BUTTON), new ItemStack[] { new ItemStack(Blocks.ACACIA_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_BUTTON), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_PLANKS) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.STONE_BUTTON), new ItemStack[] { new ItemStack(Blocks.STONE) }));
        // fence
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_FENCE, 3), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 4), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_FENCE, 3), new ItemStack[] { new ItemStack(Blocks.SPRUCE_PLANKS, 4), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_FENCE, 3), new ItemStack[] { new ItemStack(Blocks.BIRCH_PLANKS, 4), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_FENCE, 3), new ItemStack[] { new ItemStack(Blocks.JUNGLE_PLANKS, 4), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_FENCE, 3), new ItemStack[] { new ItemStack(Blocks.ACACIA_PLANKS, 4), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_FENCE, 3), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_PLANKS, 4), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.NETHER_BRICK_FENCE), new ItemStack[] { new ItemStack(Blocks.NETHER_BRICKS), }));
        // fence gate
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_FENCE_GATE), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_FENCE_GATE), new ItemStack[] { new ItemStack(Blocks.SPRUCE_PLANKS, 2), new ItemStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_FENCE_GATE), new ItemStack[] { new ItemStack(Blocks.BIRCH_PLANKS, 2), new ItemStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_FENCE_GATE), new ItemStack[] { new ItemStack(Blocks.JUNGLE_PLANKS, 2), new ItemStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_FENCE_GATE), new ItemStack[] { new ItemStack(Blocks.ACACIA_PLANKS, 2), new ItemStack(Items.STICK, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_FENCE_GATE), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_PLANKS, 2), new ItemStack(Items.STICK, 4) }));
        // lit pumpkin
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JACK_O_LANTERN), new ItemStack[] { new ItemStack(Blocks.CARVED_PUMPKIN), new ItemStack(Blocks.TORCH) }));
        // trap
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_TRAPDOOR, 1), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_TRAPDOOR, 1), new ItemStack[] { new ItemStack(Blocks.SPRUCE_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_TRAPDOOR, 1), new ItemStack[] { new ItemStack(Blocks.BIRCH_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_TRAPDOOR, 1), new ItemStack[] { new ItemStack(Blocks.JUNGLE_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_TRAPDOOR, 1), new ItemStack[] { new ItemStack(Blocks.ACACIA_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_TRAPDOOR, 1), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.IRON_TRAPDOOR, 1), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 4) }));
        // iron bar
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.IRON_BARS), new ItemStack[] { new ItemStack(Items.IRON_NUGGET, 3) }));
        // redstone lamp
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.REDSTONE_LAMP), new ItemStack[] { new ItemStack(Items.REDSTONE, 4), new ItemStack(Blocks.GLOWSTONE) }));
        // tripwire hook
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.TRIPWIRE_HOOK, 2), new ItemStack[] { new ItemStack(Items.STICK), new ItemStack(Blocks.OAK_PLANKS), new ItemStack(Items.IRON_INGOT) }));
        // cobblestone wall
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.COBBLESTONE_WALL), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MOSSY_COBBLESTONE_WALL), new ItemStack[] { new ItemStack(Blocks.MOSSY_COBBLESTONE) }));
        // carpet
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLACK_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.BLACK_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WHITE_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.WHITE_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLUE_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.BLUE_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_BLUE_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.LIGHT_BLUE_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MAGENTA_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.MAGENTA_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CYAN_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.CYAN_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GREEN_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.GREEN_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.YELLOW_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.YELLOW_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIME_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.LIME_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PINK_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.PINK_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.RED_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GRAY_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.GRAY_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_GRAY_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.LIGHT_GRAY_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BROWN_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.BROWN_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ORANGE_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.ORANGE_WOOL, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPLE_CARPET, 3), new ItemStack[] { new ItemStack(Blocks.PURPLE_WOOL, 2) }));
        // flint and steel
        recipes.add(new RecyclingRecipe(new ItemStack(Items.FLINT_AND_STEEL), new ItemStack[] { new ItemStack(Items.IRON_INGOT), new ItemStack(Items.FLINT) }));
        // stick
        recipes.add(new RecyclingRecipe(new ItemStack(Items.STICK, 2), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS) }));
        // bowl
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BOWL, 4), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 3) }));
        // door
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OAK_DOOR), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.SPRUCE_DOOR), new ItemStack[] { new ItemStack(Blocks.SPRUCE_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BIRCH_DOOR), new ItemStack[] { new ItemStack(Blocks.BIRCH_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.JUNGLE_DOOR), new ItemStack[] { new ItemStack(Blocks.JUNGLE_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ACACIA_DOOR), new ItemStack[] { new ItemStack(Blocks.ACACIA_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DARK_OAK_DOOR), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_PLANKS, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.IRON_DOOR), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 2) }));
        // painting
        recipes.add(new RecyclingRecipe(new ItemStack(Items.PAINTING), new ItemStack[] { new ItemStack(Items.STICK, 8), new ItemStack(Blocks.WHITE_WOOL) }));
        // sign
        // TODO sign post
        //recipes.add(new RecyclingRecipe(new ItemStack(Items.SIGN, 3), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 6), new ItemStack(Items.STICK) }));
        // empty bucket
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BUCKET), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 3) }));
        // minecart
        recipes.add(new RecyclingRecipe(new ItemStack(Items.MINECART), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.HOPPER_MINECART), new ItemStack[] { new ItemStack(Items.MINECART), new ItemStack(Blocks.HOPPER) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.TNT_MINECART), new ItemStack[] { new ItemStack(Items.MINECART), new ItemStack(Blocks.TNT, 1) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.FURNACE_MINECART), new ItemStack[] { new ItemStack(Items.MINECART), new ItemStack(Blocks.FURNACE) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.CHEST_MINECART), new ItemStack[] { new ItemStack(Items.MINECART), new ItemStack(Blocks.CHEST) }));
        // boat
        recipes.add(new RecyclingRecipe(new ItemStack(Items.OAK_BOAT), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.SPRUCE_BOAT), new ItemStack[] { new ItemStack(Blocks.SPRUCE_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BIRCH_BOAT), new ItemStack[] { new ItemStack(Blocks.BIRCH_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.JUNGLE_BOAT), new ItemStack[] { new ItemStack(Blocks.JUNGLE_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.ACACIA_BOAT), new ItemStack[] { new ItemStack(Blocks.ACACIA_PLANKS, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DARK_OAK_BOAT), new ItemStack[] { new ItemStack(Blocks.DARK_OAK_PLANKS, 5) }));
        // book
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BOOK), new ItemStack[] { new ItemStack(Items.PAPER, 3), new ItemStack(Items.LEATHER) }));
        // compass
        recipes.add(new RecyclingRecipe(new ItemStack(Items.COMPASS), new ItemStack[] { new ItemStack(Items.REDSTONE), new ItemStack(Items.IRON_INGOT, 4) }));
        // fishing rod
        recipes.add(new RecyclingRecipe(new ItemStack(Items.FISHING_ROD), new ItemStack[] { new ItemStack(Items.STRING, 2), new ItemStack(Items.STICK, 3) }));
        // clock
        recipes.add(new RecyclingRecipe(new ItemStack(Items.CLOCK), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 4), new ItemStack(Items.REDSTONE) }));
        // redstone repeater
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.REPEATER), new ItemStack[] { new ItemStack(Blocks.STONE, 3), new ItemStack(Blocks.REDSTONE_TORCH, 2), new ItemStack(Items.REDSTONE) }));
        // redstone comparator
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.COMPARATOR), new ItemStack[] { new ItemStack(Blocks.STONE, 3), new ItemStack(Blocks.REDSTONE_TORCH), new ItemStack(Items.QUARTZ) }));
        // glass bottle
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GLASS_BOTTLE), new ItemStack[] { new ItemStack(Blocks.GLASS) }));
        // brewing stand
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BREWING_STAND), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 3), new ItemStack(Items.BLAZE_ROD) }));
        // cauldron
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CAULDRON), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 7) }));
        // item frame
        recipes.add(new RecyclingRecipe(new ItemStack(Items.ITEM_FRAME), new ItemStack[] { new ItemStack(Items.STICK, 8), new ItemStack(Items.LEATHER) }));
        // flower pot
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.FLOWER_POT), new ItemStack[] { new ItemStack(Items.BRICK, 3) }));
        // carrot on a stick
        recipes.add(new RecyclingRecipe(new ItemStack(Items.CARROT_ON_A_STICK), new ItemStack[] { new ItemStack(Items.CARROT), new ItemStack(Items.FISHING_ROD) }));
        // armor stand
        recipes.add(new RecyclingRecipe(new ItemStack(Items.ARMOR_STAND), new ItemStack[] { new ItemStack(Items.STICK, 6), new ItemStack(Blocks.STONE_SLAB) }));
        // lead
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LEAD, 2), new ItemStack[] { new ItemStack(Items.STRING, 4), new ItemStack(Items.SLIME_BALL) }));
        // banner
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BLACK_BANNER), new ItemStack[] { new ItemStack(Blocks.BLACK_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.WHITE_BANNER), new ItemStack[] { new ItemStack(Blocks.WHITE_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BLUE_BANNER), new ItemStack[] { new ItemStack(Blocks.BLUE_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LIGHT_BLUE_BANNER), new ItemStack[] { new ItemStack(Blocks.LIGHT_BLUE_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GRAY_BANNER), new ItemStack[] { new ItemStack(Blocks.GRAY_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LIGHT_GRAY_BANNER), new ItemStack[] { new ItemStack(Blocks.LIGHT_GRAY_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GREEN_BANNER), new ItemStack[] { new ItemStack(Blocks.GREEN_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LIME_BANNER), new ItemStack[] { new ItemStack(Blocks.LIME_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.YELLOW_BANNER), new ItemStack[] { new ItemStack(Blocks.YELLOW_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.MAGENTA_BANNER), new ItemStack[] { new ItemStack(Blocks.MAGENTA_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.CYAN_BANNER), new ItemStack[] { new ItemStack(Blocks.CYAN_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.PURPLE_BANNER), new ItemStack[] { new ItemStack(Blocks.PURPLE_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.PINK_BANNER), new ItemStack[] { new ItemStack(Blocks.PINK_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BROWN_BANNER), new ItemStack[] { new ItemStack(Blocks.BROWN_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.ORANGE_BANNER), new ItemStack[] { new ItemStack(Blocks.ORANGE_WOOL, 6), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.PURPLE_BANNER), new ItemStack[] { new ItemStack(Blocks.PURPLE_WOOL, 6), new ItemStack(Items.STICK) }));
        // end crystal
        recipes.add(new RecyclingRecipe(new ItemStack(Items.END_CRYSTAL), new ItemStack[] { new ItemStack(Blocks.GLASS, 7), new ItemStack(Items.ENDER_EYE), new ItemStack(Items.GHAST_TEAR) }));
        // empty map
        recipes.add(new RecyclingRecipe(new ItemStack(Items.MAP), new ItemStack[] { new ItemStack(Items.COMPASS), new ItemStack(Items.PAPER, 8) }));
        // shears
        recipes.add(new RecyclingRecipe(new ItemStack(Items.SHEARS), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 2) }));
        // pickaxe
        recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_PICKAXE), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_PICKAXE), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 3), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_PICKAXE), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_PICKAXE), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_PICKAXE), new ItemStack[] { new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.STICK, 2) }));
        // axe
        recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_AXE), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 3), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_AXE), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 3), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_AXE), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 3), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_AXE), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_AXE), new ItemStack[] { new ItemStack(Items.DIAMOND, 3), new ItemStack(Items.STICK, 2) }));
        // shovel/spade
        recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_SHOVEL), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_SHOVEL), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_SHOVEL), new ItemStack[] { new ItemStack(Items.IRON_INGOT), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_SHOVEL), new ItemStack[] { new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_SHOVEL), new ItemStack[] { new ItemStack(Items.DIAMOND), new ItemStack(Items.STICK, 2) }));
        // sword
        recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_SWORD), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_SWORD), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 2), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_SWORD), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_SWORD), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 2), new ItemStack(Items.STICK) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_SWORD), new ItemStack[] { new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.STICK) }));
        // hoe
        recipes.add(new RecyclingRecipe(new ItemStack(Items.WOODEN_HOE), new ItemStack[] { new ItemStack(Blocks.OAK_PLANKS, 2), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.STONE_HOE), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 2), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_HOE), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_HOE), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 2), new ItemStack(Items.STICK, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_HOE), new ItemStack[] { new ItemStack(Items.DIAMOND, 2), new ItemStack(Items.STICK, 2) }));
        // bow
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BOW), new ItemStack[] { new ItemStack(Items.STRING, 3), new ItemStack(Items.STICK, 3) }));
        // arrow
        recipes.add(new RecyclingRecipe(new ItemStack(Items.ARROW), new ItemStack[] { new ItemStack(Items.FEATHER), new ItemStack(Items.STICK), new ItemStack(Items.FLINT) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.SPECTRAL_ARROW, 2), new ItemStack[] { new ItemStack(Items.GLOWSTONE_DUST, 4), new ItemStack(Items.ARROW) }));
        // armor
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LEATHER_BOOTS), new ItemStack[] { new ItemStack(Items.LEATHER, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LEATHER_HELMET), new ItemStack[] { new ItemStack(Items.LEATHER, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LEATHER_CHESTPLATE), new ItemStack[] { new ItemStack(Items.LEATHER, 8) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LEATHER_LEGGINGS), new ItemStack[] { new ItemStack(Items.LEATHER, 7) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_BOOTS), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_HELMET), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_CHESTPLATE), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 8) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.IRON_LEGGINGS), new ItemStack[] { new ItemStack(Items.IRON_INGOT, 7) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_BOOTS), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_HELMET), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_CHESTPLATE), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 8) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GOLDEN_LEGGINGS), new ItemStack[] { new ItemStack(Items.GOLD_INGOT, 7) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_BOOTS), new ItemStack[] { new ItemStack(Items.DIAMOND, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_HELMET), new ItemStack[] { new ItemStack(Items.DIAMOND, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack[] { new ItemStack(Items.DIAMOND, 8) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack[] { new ItemStack(Items.DIAMOND, 7) }));
        // 1.12
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.NETHER_BRICKS), new ItemStack[] { new ItemStack(Items.NETHER_BRICK, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_NETHER_BRICKS), new ItemStack[] { new ItemStack(Items.NETHER_BRICK, 2), new ItemStack(Items.NETHER_WART, 2) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MAGMA_BLOCK), new ItemStack[] { new ItemStack(Items.MAGMA_CREAM, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.OBSERVER), new ItemStack[] { new ItemStack(Blocks.COBBLESTONE, 6), new ItemStack(Items.REDSTONE, 2), new ItemStack(Items.QUARTZ) }));
        // concrete powder
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLACK_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.WHITE_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GRAY_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_GRAY_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BLUE_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIGHT_BLUE_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.YELLOW_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.LIME_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.GREEN_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CYAN_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.MAGENTA_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.BROWN_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PINK_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.PURPLE_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.ORANGE_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.RED_CONCRETE_POWDER, 8), new ItemStack[] { new ItemStack(Blocks.SAND, 4), new ItemStack(Blocks.GRAVEL, 4) }));
        // bed
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BLACK_BED), new ItemStack[] { new ItemStack(Blocks.BLACK_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.WHITE_BED), new ItemStack[] { new ItemStack(Blocks.WHITE_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GRAY_BED), new ItemStack[] { new ItemStack(Blocks.GRAY_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LIGHT_GRAY_BED), new ItemStack[] { new ItemStack(Blocks.LIGHT_GRAY_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BLUE_BED), new ItemStack[] { new ItemStack(Blocks.BLUE_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LIGHT_BLUE_BED), new ItemStack[] { new ItemStack(Blocks.LIGHT_BLUE_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.YELLOW_BED), new ItemStack[] { new ItemStack(Blocks.YELLOW_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.LIME_BED), new ItemStack[] { new ItemStack(Blocks.LIME_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.GREEN_BED), new ItemStack[] { new ItemStack(Blocks.GREEN_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.CYAN_BED), new ItemStack[] { new ItemStack(Blocks.CYAN_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.MAGENTA_BED), new ItemStack[] { new ItemStack(Blocks.MAGENTA_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.BROWN_BED), new ItemStack[] { new ItemStack(Blocks.BROWN_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.PINK_BED), new ItemStack[] { new ItemStack(Blocks.PINK_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.PURPLE_BED), new ItemStack[] { new ItemStack(Blocks.PURPLE_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.ORANGE_BED), new ItemStack[] { new ItemStack(Blocks.ORANGE_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Items.RED_BED), new ItemStack[] { new ItemStack(Blocks.RED_WOOL, 3), new ItemStack(Blocks.OAK_PLANKS, 3) }));
        // 1.13
        recipes.add(new RecyclingRecipe(new ItemStack(Items.TURTLE_HELMET), new ItemStack[] { new ItemStack(Items.SCUTE, 5) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.CONDUIT), new ItemStack[] { new ItemStack(Items.NAUTILUS_SHELL, 8), new ItemStack(Items.HEART_OF_THE_SEA) }));
        recipes.add(new RecyclingRecipe(new ItemStack(Blocks.DRIED_KELP_BLOCK), new ItemStack[] { new ItemStack(Items.DRIED_KELP, 9) }));
    }
}
