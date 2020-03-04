package ovh.corail.recycler.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import ovh.corail.recycler.network.UpdateConfigMessage;

import static ovh.corail.recycler.ModRecycler.MOD_ID;
import static ovh.corail.recycler.ModRecycler.PROXY;

public class ConfigRecycler {

    public static class General {
        public final ForgeConfigSpec.ConfigValue<Boolean> only_user_recipes;
        public final ForgeConfigSpec.ConfigValue<Boolean> recycle_magic_item;
        public final ForgeConfigSpec.ConfigValue<Boolean> recycle_enchanted_book;
        public final ForgeConfigSpec.ConfigValue<Integer> chance_loss;
        public final ForgeConfigSpec.ConfigValue<Boolean> allow_sound;
        public final ForgeConfigSpec.ConfigValue<Integer> time_to_recycle;
        public final ForgeConfigSpec.ConfigValue<Boolean> recycle_round_down;

        General(ForgeConfigSpec.Builder builder) {
            builder.push("General");
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
            builder.pop();
        }
    }

    public static class SharedGeneral {
        public final ForgeConfigSpec.ConfigValue<Boolean> unbalanced_recipes;
        public final ForgeConfigSpec.ConfigValue<Boolean> allow_automation;

        SharedGeneral(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            unbalanced_recipes = builder
                    .comment("unbalanced_recipes [false/true|default:false]")
                    .translation(getTranslation("unbalanced_recipes"))
                    .define("unbalanced_recipes", false);
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

    public static final ForgeConfigSpec GENERAL_SPEC;
    public static final ForgeConfigSpec SHARED_GENERAL_SPEC;
    public static final General general;
    public static final SharedGeneral shared_general;
    static {
        Pair<General, ForgeConfigSpec> confGeneral = new ForgeConfigSpec.Builder().configure(General::new);
        general = confGeneral.getLeft();
        GENERAL_SPEC = confGeneral.getRight();
        Pair<SharedGeneral, ForgeConfigSpec> confSharedGeneral = new ForgeConfigSpec.Builder().configure(SharedGeneral::new);
        shared_general = confSharedGeneral.getLeft();
        SHARED_GENERAL_SPEC = confSharedGeneral.getRight();
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ConfigEvent {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onReloadConfig(ModConfig.Reloading event) {
            if (event.getConfig().getModId().equals(MOD_ID) && event.getConfig().getType() == ModConfig.Type.SERVER) {
                // sync the config on all clients on dedicated server without the need to relog
                PROXY.markConfigDirty();
            }
        }
    }

    public static UpdateConfigMessage getUpdatePacket() {
        return new UpdateConfigMessage(shared_general.unbalanced_recipes.get(), shared_general.allow_automation.get());
    }

    public static void updateConfig(UpdateConfigMessage packet) {
        // directly set the ConfigValues
        shared_general.unbalanced_recipes.set(packet.unbalanced_recipes);
        shared_general.allow_automation.set(packet.allow_automation);
    }
}
