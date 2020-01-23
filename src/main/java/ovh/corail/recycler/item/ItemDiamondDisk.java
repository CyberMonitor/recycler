package ovh.corail.recycler.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ovh.corail.recycler.registry.ModTriggers;
import ovh.corail.recycler.util.TranslationHelper;

import javax.annotation.Nullable;
import java.util.List;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class ItemDiamondDisk extends ItemGeneric {

    public ItemDiamondDisk() {
        super("diamond_disk", getBuilder().maxStackSize(1).defaultMaxDamage(5000));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(TranslationHelper.createTranslationWithStyle(TranslationHelper.TOOLTIP_DESC, MOD_ID + ".item." + name + ".desc"));
    }

    @Override
    public void onCreated(ItemStack stack, World world, PlayerEntity player) {
        ModTriggers.BUILD_DISK.trigger(player);
    }
}
