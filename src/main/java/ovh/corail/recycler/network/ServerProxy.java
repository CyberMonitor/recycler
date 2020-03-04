package ovh.corail.recycler.network;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import ovh.corail.recycler.config.ConfigRecycler;

import static ovh.corail.recycler.ModRecycler.LOGGER;

public class ServerProxy implements IProxy {
    private boolean isConfigDirty = false;

    @Override
    public void updateConfigIfDirty() {
        if (this.isConfigDirty) {
            this.isConfigDirty = false;
            LOGGER.info("Syncing Config on Client");
            PacketHandler.sendToAllPlayers(ConfigRecycler.getUpdatePacket());
        }
    }

    @Override
    public void markConfigDirty() {
        if (!((MinecraftServer) LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER)).getPlayerList().getPlayers().isEmpty()) {
            this.isConfigDirty = true;
        }
    }
}
