package ovh.corail.recycler.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ovh.corail.recycler.registry.ModTabs;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class ItemGeneric extends Item {
    protected final String name;

    public ItemGeneric(String name) {
        this(name, getBuilder());
    }

    public ItemGeneric(String name, Properties properties) {
        super(properties);
        this.name = name;
    }

    public String getSimpleName() {
        return this.name;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return MOD_ID + ".item." + name;
    }

    public static Properties getBuilder() {
        return new Properties().group(ModTabs.TAB_RECYCLER).maxStackSize(64);
    }
}
