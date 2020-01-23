package ovh.corail.recycler.block;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ovh.corail.recycler.registry.ModTabs;
import ovh.corail.recycler.registry.ModTriggers;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class ItemBlockRecycler extends BlockItem {

    public ItemBlockRecycler(Block block) {
        super(block, new Properties().group(ModTabs.TAB_RECYCLER).maxStackSize(64));
    }

    @Override
    public void onCreated(ItemStack stack, World world, PlayerEntity player) {
        ModTriggers.BUILD_RECYCLER.trigger(player);
    }

    @Override
    public String getTranslationKey() {
        return MOD_ID + ".block.recycler";
    }
}
