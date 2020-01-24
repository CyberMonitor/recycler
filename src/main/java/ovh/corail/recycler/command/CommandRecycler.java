package ovh.corail.recycler.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.StringTextComponent;
import ovh.corail.recycler.util.JsonRecyclingRecipe;
import ovh.corail.recycler.util.RecyclingManager;
import ovh.corail.recycler.util.LangKey;

import java.io.File;

public class CommandRecycler {
    private final CommandDispatcher<CommandSource> commandDispatcher;

    public CommandRecycler(CommandDispatcher<CommandSource> commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    public void registerCommand() {
        this.commandDispatcher.register(Commands.literal("recycler").requires(p -> p.hasPermissionLevel(3))
                .executes(c -> showUsage(c.getSource()))
                .then(Commands.literal("add_recipe")
                        .executes(c -> processAddRecipe(c.getSource(), null))
                        .then(Commands.argument("item", ItemArgument.item())
                                .executes(c -> processAddRecipe(c.getSource(), ItemArgument.getItem(c, "item"))))
                ).then(Commands.literal("remove_recipe")
                        .executes(c -> processRemoveRecipe(c.getSource(), null))
                        .then(Commands.argument("item", ItemArgument.item())
                                .executes(c -> processRemoveRecipe(c.getSource(), ItemArgument.getItem(c, "item"))))
                ).then(Commands.literal("export_crafting_recipes")
                        .executes(c -> processExportCraftingRecipes(c.getSource()))
                )
        );
    }

    private int processAddRecipe(CommandSource source, ItemInput stack) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        try {
            RecyclingManager.instance.discoverRecipe(player, stack == null ? player.getHeldItemMainhand() : stack.createStack(1, false));
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int processRemoveRecipe(CommandSource source, ItemInput stack) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        boolean success = RecyclingManager.instance.removeRecipe(stack == null ? player.getHeldItemMainhand() : stack.createStack(1, false));
        (success ? LangKey.MESSAGE_REMOVE_RECIPE_SUCCESS : LangKey.MESSAGE_REMOVE_RECIPE_FAILED).sendMessage(player);
        return success ? 1 : 0;
    }

    private int processExportCraftingRecipes(CommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.asPlayer();
        // TODO check this
        try {
            RecyclingManager rm = RecyclingManager.instance;
            NonNullList<JsonRecyclingRecipe> list = NonNullList.create();
            for (IRecipe crafting_recipe : player.world.getRecipeManager().getRecipes(IRecipeType.CRAFTING).values()) {
                // only recipes not in the recycler
                if (!crafting_recipe.getRecipeOutput().isEmpty() && rm.hasRecipe(crafting_recipe.getRecipeOutput()) == -1) {
                    JsonRecyclingRecipe res = rm.convertRecipeToJson(rm.convertCraftingRecipe(crafting_recipe));
                    if (res != null) {
                        list.add(res);
                    }
                }
            }
            File exportFile = new File(RecyclingManager.instance.CONFIG_DIR, "export_crafting_recipes.json");
            LangKey.MESSAGE_FOUND_RECIPES.sendMessage(player, list.size());
            boolean success = rm.saveAsJson(exportFile, list);
            (success ? LangKey.MESSAGE_EXPORT_SUCCESS : LangKey.MESSAGE_EXPORT_FAILED).sendMessage(player);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int showUsage(CommandSource source) {
        source.sendFeedback(new StringTextComponent("recycler <command>"), false);
        source.sendFeedback(new StringTextComponent("add_recipe : add the recycling recipe of the crafting result of the item hold in main hand"), false);
        source.sendFeedback(new StringTextComponent("remove_recipe : remove the recycling recipe of the item hold in main hand"), false);
        source.sendFeedback(new StringTextComponent("export_crafting_recipes : save the list of all crafting recipes in \"recycling\" format in the config directory"), false);
        return 1;
    }
}
