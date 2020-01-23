package ovh.corail.recycler.util;

import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;

public class INotNull {
    @SuppressWarnings("all")
    @Nonnull
    public static <T extends IForgeRegistryEntry> T getDefaultNotNull() {
        return null;
    }
}
