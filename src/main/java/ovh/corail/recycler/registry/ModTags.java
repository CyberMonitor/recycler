package ovh.corail.recycler.registry;

import net.minecraft.item.Item;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class ModTags {
    public static class Items {
        public static final Tag<Item> disks = tag("disks");

        private static Tag<Item> tag(String name) {
            return new ItemTags.Wrapper(new ResourceLocation(MOD_ID, name));
        }
    }
}
