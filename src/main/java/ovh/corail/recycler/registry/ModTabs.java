package ovh.corail.recycler.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static ovh.corail.recycler.ModRecycler.MOD_ID;
import static ovh.corail.recycler.ModRecycler.MOD_NAME;

public class ModTabs {
    public static final ItemGroup TAB_RECYCLER = new ItemGroup(MOD_ID) {
        @Override
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.recycler);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public String getTranslationKey() {
            return MOD_NAME;
        }
    };
}
