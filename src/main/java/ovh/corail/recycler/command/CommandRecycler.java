package ovh.corail.recycler.command;

import com.mojang.brigadier.CommandDispatcher;
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
import ovh.corail.recycler.util.Helper;
import ovh.corail.recycler.util.JsonRecyclingRecipe;
import ovh.corail.recycler.util.LangKey;
import ovh.corail.recycler.util.RecyclingManager;

import java.io.File;
import java.util.stream.Collectors;

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

    private int processAddRecipe(CommandSource source, ItemInput inputStack) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity ? (ServerPlayerEntity) source.getEntity() : null;
        ItemStack stackToDiscover = inputStack != null ? inputStack.createStack(1, false) : player != null ? player.getHeldItemMainhand() : ItemStack.EMPTY;
        if (!stackToDiscover.isEmpty() && RecyclingManager.instance.discoverRecipe(source.getWorld(), stackToDiscover)) {
            LangKey.MESSAGE_ADD_RECIPE_SUCCESS.sendMessage(player);
            return 1;
        }
        LangKey.MESSAGE_ADD_RECIPE_FAILED.sendMessage(player);
        return 0;
    }

    private int processRemoveRecipe(CommandSource source, ItemInput stack) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getEntity() instanceof ServerPlayerEntity ? (ServerPlayerEntity) source.getEntity() : null;
        ItemStack stackToRemove = stack != null ? stack.createStack(1, false) : player != null ? player.getHeldItemMainhand() : ItemStack.EMPTY;
        if (!stackToRemove.isEmpty() && RecyclingManager.instance.removeRecipe(stackToRemove)) {
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
        source.sendFeedback(new StringTextComponent("add_recipe : add the recycling recipe of the crafting result of the item hold in main hand"), false);
        source.sendFeedback(new StringTextComponent("remove_recipe : remove the recycling recipe of the item hold in main hand"), false);
        source.sendFeedback(new StringTextComponent("export_crafting_recipes : save the list of all crafting recipes in \"recycling\" format in the config directory"), false);
        return 1;
    }
}
