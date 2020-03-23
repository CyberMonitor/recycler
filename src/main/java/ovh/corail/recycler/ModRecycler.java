package ovh.corail.recycler;

import com.google.common.reflect.Reflection;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovh.corail.recycler.command.CommandRecycler;
import ovh.corail.recycler.config.RecyclerModConfig;
import ovh.corail.recycler.gui.GuiRecycler;
import ovh.corail.recycler.gui.GuiRecyclingBook;
import ovh.corail.recycler.item.ItemDisk;
import ovh.corail.recycler.network.ClientProxy;
import ovh.corail.recycler.network.IProxy;
import ovh.corail.recycler.network.PacketHandler;
import ovh.corail.recycler.network.ServerProxy;
import ovh.corail.recycler.registry.ModContainers;
import ovh.corail.recycler.registry.ModItems;
import ovh.corail.recycler.registry.ModTabs;
import ovh.corail.recycler.registry.ModTriggers;
import ovh.corail.recycler.recipe.RecyclingManager;

@Mod("corail_recycler")
public class ModRecycler {
    public static final String MOD_ID = "corail_recycler";
    public static final String MOD_NAME = "Corail Recycler";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final IProxy PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    @SuppressWarnings("UnstableApiUsage")
    public ModRecycler() {
        Reflection.initialize(PacketHandler.class, ModTriggers.class, ModTabs.class);
        final ModLoadingContext context = ModLoadingContext.get();
        registerSharedConfig(context.getActiveContainer());
        context.registerConfig(ModConfig.Type.COMMON, ConfigRecycler.GENERAL_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
        PROXY.preInit();
    }

    private void registerSharedConfig(ModContainer modContainer) {
        modContainer.addConfig(new RecyclerModConfig(ConfigRecycler.SHARED_GENERAL_SPEC, modContainer));
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        new CommandRecycler(event.getCommandDispatcher()).registerCommand();
        RecyclingManager.instance.loadRecipes();
        LOGGER.info(RecyclingManager.instance.getRecipeCount() + " recycling recipes loaded");
    }

    private void clientInit(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(ModContainers.RECYCLER, GuiRecycler::new);
        ScreenManager.registerFactory(ModContainers.RECYCLING_BOOK, GuiRecyclingBook::new);
        event.getMinecraftSupplier().get().getItemColors().register(ItemDisk::getColor, ModItems.diamond_disk, ModItems.steel_disk);
    }
}
