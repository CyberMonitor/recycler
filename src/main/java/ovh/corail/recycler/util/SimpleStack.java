package ovh.corail.recycler.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

public class SimpleStack {
    public static final SimpleStack EMPTY = new SimpleStack(ItemStack.EMPTY);
    private final Item item;
    private final int count;
    private String translation = null;

    public SimpleStack(ItemStack stack) {
        this(stack.getItem(), stack.getCount());
    }

    public SimpleStack(Item item, int count) {
        this.item = item;
        this.count = count;
    }

    public SimpleStack(Item item) {
        this(item, 1);
    }

    public SimpleStack(Block block, int count) {
        this(block.asItem(), count);
    }

    public SimpleStack(Block block) {
        this(block, 1);
    }

    public Item getItem() {
        return this.item;
    }

    public int getCount() {
        return this.count;
    }

    public boolean isEmpty() {
        return this.item == Items.AIR;
    }

    public ItemStack asItemStack() {
        return new ItemStack(this.item, this.count);
    }

    public String getTranslation() {
        if (this.translation == null) {
            this.translation = new TranslationTextComponent(this.item.getTranslationKey()).getFormattedText().toLowerCase();
        }
        return this.translation;
    }

    @Override
    public String toString() {
        ResourceLocation rl = this.item.getRegistryName();
        assert rl != null;
        return rl.toString() + ":" + this.count;
    }

    public boolean isItemEqual(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == this.item;
    }

    public static SimpleStack fromJson(String jsonString) {
        String[] parts = jsonString.split(":");
        if (parts.length == 2 || parts.length == 3) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parts[0], parts[1]));
            if (item != null) {
                return new SimpleStack(item, parts.length == 3 ? Integer.valueOf(parts[2]) : 1);
            }
        }
        return new SimpleStack(ItemStack.EMPTY);
    }

    public static boolean areItemEqual(SimpleStack s1, SimpleStack s2) {
        return !s1.isEmpty() && s1.item == s2.item;
    }
}
