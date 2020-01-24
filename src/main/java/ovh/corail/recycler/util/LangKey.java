package ovh.corail.recycler.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public enum LangKey {
    MESSAGE_ADD_RECIPE_SUCCESS("message.add_recipe_success"),
    MESSAGE_ADD_RECIPE_FAILED("message.add_recipe_failed"),
    MESSAGE_EXPORT_SUCCESS("message.export_success"),
    MESSAGE_EXPORT_FAILED("message.export_failed"),
    MESSAGE_REMOVE_RECIPE_SUCCESS("message.remove_recipe_success"),
    MESSAGE_REMOVE_RECIPE_FAILED("message.remove_recipe_failed"),
    MESSAGE_FOUND_RECIPES("message.found_recipes"),
    MESSAGE_RECYCLING_BOOK("message.recycling_book"),
    MESSAGE_NOT_ENOUGH_OUTPUT_SLOTS("message.not_enough_output_slots"),
    MESSAGE_LOSS("message.loss"),
    MESSAGE_BROKEN_DISK("message.broken_disk"),
    BUTTON_RECYLE("button.recycle"),
    BUTTON_AUTO("button.auto"),
    BUTTON_TAKE_ALL("button.takeAll"),
    BUTTON_DISCOVER_RECIPE("button.discoverRecipe"),
    BUTTON_REMOVE_RECIPE("button.removeRecipe");

    private final String key;

    LangKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return MOD_ID + "." + key;
    }

    public void sendMessage(@Nullable PlayerEntity player, Object... params) {
        if (player != null) {
            player.sendMessage(getTranslation(params));
        }
    }

    public void sendMessageWithStyle(@Nullable PlayerEntity player, Style style, Object... params) {
        if (player != null) {
            player.sendMessage(getTranslationWithStyle(style, params));
        }
    }

    public ITextComponent getTranslation(Object... params) {
        return new TranslationTextComponent(getKey(), params);
    }

    public ITextComponent getTranslationWithStyle(Style style, Object... params) {
        return getTranslation(params).setStyle(style);
    }

    public static ITextComponent makeTranslationWithStyle(Style style, String message, Object... params) {
        return makeTranslation(message, params).setStyle(style);
    }

    public static ITextComponent makeTranslation(String message, Object... params) {
        return new TranslationTextComponent(message, params);
    }
}
