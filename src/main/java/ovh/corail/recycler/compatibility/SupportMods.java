package ovh.corail.recycler.compatibility;

import net.minecraftforge.fml.ModList;

public enum SupportMods {
    QUARK("quark"),
    PROJECTE("projecte");
    private final String modid;
    private final boolean isLoaded;
    SupportMods(String modid) {
        this.modid = modid;
        this.isLoaded = ModList.get().isLoaded(modid);
    }

    public String getModid() {
        return this.modid;
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }
}
