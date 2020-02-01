package ovh.corail.recycler.recipe;

import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.NonNullList;
import ovh.corail.recycler.registry.ModItems;

class RecipeLoaderHelper {
    static void loadDefaultRecipes(NonNullList<RecyclingRecipe> recipes) {
        recipes.add(new RecyclingRecipe(new SimpleStack(Items.DIAMOND), new SimpleStack[] { new SimpleStack(ModItems.diamond_shard, 9) }));
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
