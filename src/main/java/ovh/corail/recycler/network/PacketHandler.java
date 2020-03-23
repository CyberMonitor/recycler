package ovh.corail.recycler.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import ovh.corail.recycler.ModRecycler;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    private static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(new ResourceLocation(ModRecycler.MOD_ID, "recycler_channel"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    static {
        HANDLER.registerMessage(0, ServerRecyclerMessage.class, ServerRecyclerMessage::toBytes, ServerRecyclerMessage::fromBytes, ServerRecyclerMessage.Handler::handle);
        HANDLER.registerMessage(1, ServerRecyclingBookMessage.class, ServerRecyclingBookMessage::toBytes, ServerRecyclingBookMessage::fromBytes, ServerRecyclingBookMessage.Handler::handle);
        HANDLER.registerMessage(2, UpdateConfigMessage.class, UpdateConfigMessage::toBytes, UpdateConfigMessage::fromBytes, UpdateConfigMessage.Handler::handle);
    }

    public static <T> void sendToServer(T message) {
        HANDLER.sendToServer(message);
    }

    public static <T> void sendToAllPlayers(T message) {
        HANDLER.send(PacketDistributor.ALL.noArg(), message);
    }
}
