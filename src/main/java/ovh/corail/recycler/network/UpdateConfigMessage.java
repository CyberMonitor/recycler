package ovh.corail.recycler.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ovh.corail.recycler.config.ConfigRecycler;
import ovh.corail.recycler.util.Helper;

import java.util.function.Supplier;

public class UpdateConfigMessage {
    public final boolean unbalanced_recipes, allow_automation;

    public UpdateConfigMessage(boolean unbalanced_recipes, boolean allow_automation) {
        this.unbalanced_recipes = unbalanced_recipes;
        this.allow_automation = allow_automation;
    }

    static UpdateConfigMessage fromBytes(PacketBuffer buf) {
        return new UpdateConfigMessage(buf.readBoolean(), buf.readBoolean());
    }

    static void toBytes(UpdateConfigMessage msg, PacketBuffer buf) {
        buf.writeBoolean(msg.unbalanced_recipes);
        buf.writeBoolean(msg.allow_automation);
    }

    static class Handler {
        static void handle(UpdateConfigMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
            NetworkEvent.Context ctx = contextSupplier.get();
            if (Helper.isPacketToClient(ctx)) {
                ctx.enqueueWork(() -> ConfigRecycler.updateConfig(message));
            }
            ctx.setPacketHandled(true);
        }
    }
}
