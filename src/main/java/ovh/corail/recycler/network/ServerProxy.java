package ovh.corail.recycler.network;

import ovh.corail.recycler.config.ConfigRecycler;

import static ovh.corail.recycler.ModRecycler.LOGGER;

public class ServerProxy implements IProxy {
    @Override
    public void updateConfig() {
        LOGGER.info("Syncing Config on Client");
        PacketHandler.sendToAllPlayers(ConfigRecycler.getUpdatePacket());
    }
}
