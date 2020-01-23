package ovh.corail.recycler.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class TranslationHelper {
    public static final Style TOOLTIP_DESC = new Style().setColor(TextFormatting.GRAY).setItalic(true).setBold(false);

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

        public ITextComponent getTranslation(Object... params) {
            return new TranslationTextComponent(getKey(), params);
        }
    }

    public static void sendMessage(@Nullable ServerPlayerEntity sender, LangKey langKey, Object... params) {
        if (sender != null && !sender.world.isRemote) {
            sender.sendMessage(langKey.getTranslation(params));
        }
    }

    private static ITextComponent createTranslation(String message, Object... params) {
        return new TranslationTextComponent(message, params);
    }

    public static ITextComponent createTranslationWithStyle(Style style, String message, Object... params) {
        return createTranslation(message, params).setStyle(style);
    }
}
