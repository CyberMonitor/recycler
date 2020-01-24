package ovh.corail.recycler.registry;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ovh.corail.recycler.block.BlockRecycler;
import ovh.corail.recycler.block.ItemBlockRecycler;
import ovh.corail.recycler.gui.ContainerRecycler;
import ovh.corail.recycler.gui.ContainerRecyclingBook;
import ovh.corail.recycler.item.ItemDisk;
import ovh.corail.recycler.item.ItemGeneric;
import ovh.corail.recycler.tileentity.TileEntityRecycler;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(withName(new BlockRecycler(), "recycler"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                withName(new ItemGeneric("diamond_shard"), "diamond_shard"),
                withName(new ItemDisk("diamond_disk", 5000), "diamond_disk"),
                withName(new ItemBlockRecycler(ModBlocks.recycler), ModBlocks.recycler.getRegistryName())
        );
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(
                fromSound("recycler"),
                fromSound("recycler_working")
        );
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(withName(TileEntityType.Builder.create(TileEntityRecycler::new, ModBlocks.recycler).build(null), "recycler"));
    }

    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        event.getRegistry().registerAll(
                withName(IForgeContainerType.create(ContainerRecycler::new), "recycler"),
                withName(new ContainerType<>(ContainerRecyclingBook::new), "recycling_book")
        );
    }

    private static SoundEvent fromSound(String name) {
        ResourceLocation rl = new ResourceLocation(MOD_ID, name);
        return new SoundEvent(rl).setRegistryName(rl);
    }

    private static <T extends IForgeRegistryEntry<T>> T withName(T entry, String name) {
        return entry.setRegistryName(new ResourceLocation(MOD_ID, name));
    }

    private static <T extends IForgeRegistryEntry<T>> T withName(T entry, ResourceLocation locName) {
        return entry.setRegistryName(locName);
    }
}
