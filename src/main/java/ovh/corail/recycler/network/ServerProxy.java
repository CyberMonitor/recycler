package ovh.corail.recycler.network;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import ovh.corail.recycler.config.ConfigRecycler;

import static ovh.corail.recycler.ModRecycler.LOGGER;

public class ServerProxy implements IProxy {
    private boolean isConfigDirty = false;

    @Override
    public void preInit() {
        // only register the event on dedicated server
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void markConfigDirty() {
        if (!this.isConfigDirty && !((MinecraftServer) LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER)).getPlayerList().getPlayers().isEmpty()) {
            this.isConfigDirty = true;
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && this.isConfigDirty) {
            this.isConfigDirty = false;
            LOGGER.info("Syncing Config on Client");
            PacketHandler.sendToAllPlayers(ConfigRecycler.getUpdatePacket());
        }
    }
}
