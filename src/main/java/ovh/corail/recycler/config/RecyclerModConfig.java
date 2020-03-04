package ovh.corail.recycler.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ConfigFileTypeHandler;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Function;

public class RecyclerModConfig extends ModConfig {
    public RecyclerModConfig(ForgeConfigSpec spec, ModContainer container) {
        super(Type.SERVER, spec, container, String.format("%s-%s.toml", container.getModId(), Type.SERVER.extension()));
    }

    @Override
    public ConfigFileTypeHandler getHandler() {
        return CONFIG_FILE_TYPE_HANDLER;
    }

    private static final ConfigFileTypeHandler CONFIG_FILE_TYPE_HANDLER = new ConfigFileTypeHandler() {
        @Override
        public Function<ModConfig, CommentedFileConfig> reader(Path configBasePath) {
            return super.reader(configBasePath.endsWith("serverconfig") ? FMLPaths.CONFIGDIR.get() : configBasePath);
        }
    };
}
