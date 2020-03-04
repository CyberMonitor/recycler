package ovh.corail.recycler.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ovh.corail.recycler.registry.ModTags;
import ovh.corail.recycler.registry.ModTriggers;
import ovh.corail.recycler.util.LangKey;
import ovh.corail.recycler.util.StyleType;

import javax.annotation.Nullable;
import java.util.List;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class ItemDisk extends ItemGeneric {
    private final int color;

    public ItemDisk(String name, int maxDamage, int color) {
        super(name, getBuilder().maxStackSize(1).defaultMaxDamage(maxDamage));
        this.color = color;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(LangKey.makeTranslationWithStyle(StyleType.TOOLTIP_DESC, MOD_ID + ".item.disk.desc"));
    }

    @Override
    public void onCreated(ItemStack stack, World world, PlayerEntity player) {
        ModTriggers.BUILD_DISK.trigger(player);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        return !stack.getItem().isIn(ModTags.Items.disks) ? -1 : ((ItemDisk)stack.getItem()).color;
    }
}
