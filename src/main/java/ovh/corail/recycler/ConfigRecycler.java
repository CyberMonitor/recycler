package ovh.corail.recycler;

import net.minecraftforge.common.ForgeConfigSpec;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class ConfigRecycler {

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Boolean> unbalanced_recipes;
        public final ForgeConfigSpec.ConfigValue<Boolean> only_user_recipes;
        public final ForgeConfigSpec.ConfigValue<Boolean> recycle_magic_item;
        public final ForgeConfigSpec.ConfigValue<Boolean> recycle_enchanted_book;
        public final ForgeConfigSpec.ConfigValue<Integer> chance_loss;
        public final ForgeConfigSpec.ConfigValue<Boolean> allow_sound;
        public final ForgeConfigSpec.ConfigValue<Integer> time_to_recycle;
        public final ForgeConfigSpec.ConfigValue<Boolean> recycle_round_down;
        public final ForgeConfigSpec.ConfigValue<Boolean> allow_automation;

        General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            unbalanced_recipes = builder
                    .comment("unbalanced_recipes [false/true|default:false]")
                    .translation(getTranslation("unbalanced_recipes"))
                    .define("unbalanced_recipes", false);
            only_user_recipes = builder
                    .comment("only_user_recipes [false/true|default:false]")
                    .translation(getTranslation("only_user_recipes"))
                    .define("only_user_recipes", false);
            recycle_magic_item = builder
                    .comment("recycle_magic_item [false/true|default:true]")
                    .translation(getTranslation("recycle_magic_item"))
                    .define("recycle_magic_item", true);
            recycle_enchanted_book = builder
                    .comment("recycle_enchanted_book [false/true|default:true]")
                    .translation(getTranslation("recycle_enchanted_book"))
                    .define("recycle_enchanted_book", true);
            chance_loss = builder
                    .comment("chance_loss [0..100|default:0]")
                    .translation(getTranslation("chance_loss"))
                    .defineInRange("chance_loss", 0, 0, 100);
            allow_sound = builder
                    .comment("allow_sound [false/true|default:true]")
                    .translation(getTranslation("allow_sound"))
                    .define("allow_sound", true);
            time_to_recycle = builder
                    .comment("time_to_recycle [5..10000|default:100]")
                    .translation(getTranslation("time_to_recycle"))
                    .defineInRange("time_to_recycle", 100, 5, 10000);
            recycle_round_down = builder
                    .comment("recycle_round_down [false/true|default:false]")
                    .translation(getTranslation("recycle_round_down"))
                    .define("recycle_round_down", false);
            allow_automation = builder
                    .comment("allow_automation [false/true|default:true]")
                    .translation(getTranslation("allow_automation"))
                    .define("allow_automation", true);
            builder.pop();
        }
    }

    private static String getTranslation(String name) {
        return MOD_ID + ".config." + name;
    }

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final General general = new General(BUILDER);
    static final ForgeConfigSpec GENERAL_SPEC = BUILDER.build();
}
