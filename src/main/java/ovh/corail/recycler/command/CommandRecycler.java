package ovh.corail.recycler.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.StringTextComponent;
import ovh.corail.recycler.recipe.JsonRecyclingRecipe;
import ovh.corail.recycler.recipe.RecyclingManager;
import ovh.corail.recycler.recipe.RecyclingRecipe;
import ovh.corail.recycler.recipe.SimpleStack;
import ovh.corail.recycler.util.Helper;
import ovh.corail.recycler.util.LangKey;

import java.io.File;
import java.util.stream.Collectors;

public class CommandRecycler {
    private final CommandDispatcher<CommandSource> commandDispatcher;

    public CommandRecycler(CommandDispatcher<CommandSource> commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    private int processAddRecipe(CommandSource source, SimpleStack ingredient, SimpleStack... results) {
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity ? (ServerPlayerEntity) source.getEntity() : null;
        RecyclingRecipe recipe = new RecyclingRecipe(ingredient, results);
        if (Helper.isValidRecipe(recipe) && RecyclingManager.instance.addRecipe(recipe.setUserDefined(true)).saveUserDefinedRecipes()) {
            LangKey.MESSAGE_ADD_RECIPE_SUCCESS.sendMessage(player);
            return 1;
        }
        LangKey.MESSAGE_ADD_RECIPE_FAILED.sendMessage(player);
        return 0;
    }

    private int processDiscoverRecipe(CommandSource source, ItemStack stack) {
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity ? (ServerPlayerEntity) source.getEntity() : null;
        if (stack.isEmpty() && player != null) {
            stack = player.getHeldItemMainhand();
        }
        if (!stack.isEmpty() && RecyclingManager.instance.discoverRecipe(source.getWorld(), stack)) {
            LangKey.MESSAGE_ADD_RECIPE_SUCCESS.sendMessage(player);
            return 1;
        }
        LangKey.MESSAGE_ADD_RECIPE_FAILED.sendMessage(player);
        return 0;
    }

    private int processRemoveRecipe(CommandSource source, ItemStack stack) {
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity ? (ServerPlayerEntity) source.getEntity() : null;
        if (stack.isEmpty() && player != null) {
            stack = player.getHeldItemMainhand();
        }
        if (!stack.isEmpty() && RecyclingManager.instance.removeRecipe(stack)) {
            LangKey.MESSAGE_REMOVE_RECIPE_SUCCESS.sendMessage(player);
            return 1;
        }
        LangKey.MESSAGE_REMOVE_RECIPE_FAILED.sendMessage(player);
        return 0;
    }

    private int processExportCraftingRecipes(CommandSource source) {
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity ? (ServerPlayerEntity) source.getEntity() : null;
        RecyclingManager rm = RecyclingManager.instance;
        // only recipes not in the recycler
        NonNullList<JsonRecyclingRecipe> list = source.getWorld().getRecipeManager().getRecipes(IRecipeType.CRAFTING).values().stream().filter(recipe -> Helper.isValidRecipe(recipe) && rm.getRecipe(recipe.getRecipeOutput(), false) == null).map(recipe -> new JsonRecyclingRecipe((ICraftingRecipe) recipe)).collect(Collectors.toCollection(NonNullList::create));
        LangKey.MESSAGE_FOUND_RECIPES.sendMessage(player, list.size());
        File exportFile = new File(RecyclingManager.instance.CONFIG_DIR, "export_crafting_recipes.json");
        boolean success = rm.saveAsJson(exportFile, list);
        (success ? LangKey.MESSAGE_EXPORT_SUCCESS : LangKey.MESSAGE_EXPORT_FAILED).sendMessage(player);
        return 1;
    }

    private int showUsage(CommandSource source) {
        source.sendFeedback(new StringTextComponent("recycler <command>"), false);
        source.sendFeedback(new StringTextComponent("discover_recipe : add the recycling recipe of the crafting result of the item hold in main hand"), false);
        source.sendFeedback(new StringTextComponent("remove_recipe : remove the recycling recipe of the item hold in main hand"), false);
        source.sendFeedback(new StringTextComponent("export_crafting_recipes : save the list of all crafting recipes in \"recycling\" format in the config directory"), false);
        source.sendFeedback(new StringTextComponent("add_recipe : add a custom recipe based on the ingredient and results provided in param <item> <count>"), false);
        return 1;
    }

    public void registerCommand() {
        LiteralArgumentBuilder<CommandSource> createRecipeBuilder = Commands.literal("create_recipe").executes(c -> showUsage(c.getSource()));
        createRecipeBuilder.then(createItemArgument("ing").then(createIntegerArgument("ingc", null)
            .then(createItemArgument("r1").then(createIntegerArgument("r1c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1")))
                .then(createItemArgument("r2").then(createIntegerArgument("r2c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1"), createStack(c, "r2")))
                    .then(createItemArgument("r3").then(createIntegerArgument("r3c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1"), createStack(c, "r2"), createStack(c, "r3")))
                        .then(createItemArgument("r4").then(createIntegerArgument("r4c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1"), createStack(c, "r2"), createStack(c, "r3"), createStack(c, "r4")))
                            .then(createItemArgument("r5").then(createIntegerArgument("r5c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1"), createStack(c, "r2"), createStack(c, "r3"), createStack(c, "r4"), createStack(c, "r5")))
                                .then(createItemArgument("r6").then(createIntegerArgument("r6c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1"), createStack(c, "r2"), createStack(c, "r3"), createStack(c, "r4"), createStack(c, "r5"), createStack(c, "r6")))
                                    .then(createItemArgument("r7").then(createIntegerArgument("r7c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1"), createStack(c, "r2"), createStack(c, "r3"), createStack(c, "r4"), createStack(c, "r5"), createStack(c, "r6"), createStack(c, "r7")))
                                        .then(createItemArgument("r8").then(createIntegerArgument("r8c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1"), createStack(c, "r2"), createStack(c, "r3"), createStack(c, "r4"), createStack(c, "r5"), createStack(c, "r6"), createStack(c, "r7"), createStack(c, "r8")))
                                            .then(createItemArgument("r9").then(createIntegerArgument("r9c", c -> processAddRecipe(c.getSource(), createStack(c, "ing"), createStack(c, "r1"), createStack(c, "r2"), createStack(c, "r3"), createStack(c, "r4"), createStack(c, "r5"), createStack(c, "r6"), createStack(c, "r7"), createStack(c, "r8"), createStack(c, "r9")))
        ))))))))))))))))))));
        this.commandDispatcher.register(Commands.literal("recycler").requires(p -> p.hasPermissionLevel(2))
            .executes(c -> showUsage(c.getSource()))
            .then(Commands.literal("discover_recipe").executes(c -> processDiscoverRecipe(c.getSource(), ItemStack.EMPTY))
                .then(Commands.argument("item", ItemArgument.item()).executes(c -> processDiscoverRecipe(c.getSource(), ItemArgument.getItem(c, "item").createStack(1, false))))
            ).then(Commands.literal("remove_recipe").executes(c -> processRemoveRecipe(c.getSource(), ItemStack.EMPTY))
                .then(Commands.argument("item", ItemArgument.item()).executes(c -> processRemoveRecipe(c.getSource(), ItemArgument.getItem(c, "item").createStack(1, false))))
            ).then(Commands.literal("export_crafting_recipes").executes(c -> processExportCraftingRecipes(c.getSource()))
            ).then(createRecipeBuilder));
    }

    private RequiredArgumentBuilder<CommandSource, ItemInput> createItemArgument(String itemName) {
        return Commands.argument(itemName, ItemArgument.item()).executes(c -> showUsage(c.getSource()));
    }

    private RequiredArgumentBuilder<CommandSource, Integer> createIntegerArgument(String intName, Command<CommandSource> action) {
        return Commands.argument(intName, IntegerArgumentType.integer()).executes(action == null ? c -> showUsage(c.getSource()) : action);
    }

    private SimpleStack createStack(CommandContext<CommandSource> context, String name) {
        return new SimpleStack(ItemArgument.getItem(context, name).getItem(), IntegerArgumentType.getInteger(context, name + "c"));
    }
}
