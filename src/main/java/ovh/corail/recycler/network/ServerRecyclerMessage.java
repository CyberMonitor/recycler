package ovh.corail.recycler.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import ovh.corail.recycler.block.BlockRecycler;
import ovh.corail.recycler.registry.ModTriggers;
import ovh.corail.recycler.tileentity.TileEntityRecycler;
import ovh.corail.recycler.util.LangKey;
import ovh.corail.recycler.util.RecyclingManager;

import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ServerRecyclerMessage {
    public enum RecyclerAction {RECYCLE, SWITCH_AUTO, TAKE_ALL, DISCOVER_RECIPE, REMOVE_RECIPE}

    private final RecyclerAction action;
    private final BlockPos pos;

    public ServerRecyclerMessage(RecyclerAction action, BlockPos pos) {
        this.action = action;
        this.pos = pos;
    }

    static ServerRecyclerMessage fromBytes(PacketBuffer buf) {
        return new ServerRecyclerMessage(RecyclerAction.values()[buf.readShort()], buf.readBlockPos());
    }

    static void toBytes(ServerRecyclerMessage msg, PacketBuffer buf) {
        buf.writeShort(msg.action.ordinal());
        buf.writeBlockPos(msg.pos);
    }

    static class Handler {
        static void handle(final ServerRecyclerMessage message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    TileEntityRecycler recycler = BlockRecycler.getTileEntity(player.world, message.pos);
                    if (recycler != null) {
                        switch (message.action) {
                            case RECYCLE:
                                if (recycler.recycle(player)) {
                                    ModTriggers.FIRST_RECYCLE.trigger(player);
                                }
                                break;
                            case SWITCH_AUTO:
                                recycler.switchWorking();
                                break;
                            case TAKE_ALL:
                                player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(p -> {
                                    ItemStackHandler inventOutput = recycler.getInventoryOutput();
                                    IntStream.range(0, inventOutput.getSlots()).filter(slot -> !inventOutput.getStackInSlot(slot).isEmpty()).forEach(slot -> inventOutput.setStackInSlot(slot, ItemHandlerHelper.insertItemStacked(p, inventOutput.getStackInSlot(slot), false)));
                                });
                                break;
                            case DISCOVER_RECIPE:
                                try {
                                    RecyclingManager.instance.discoverRecipe(player, recycler.getInventoryWorking().getStackInSlot(0));
                                    recycler.updateRecyclingRecipe();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case REMOVE_RECIPE:
                                boolean success = RecyclingManager.instance.removeRecipe(recycler.getInventoryWorking().getStackInSlot(0));
                                if (success) {
                                    recycler.updateRecyclingRecipe();
                                }
                                (success ? LangKey.MESSAGE_REMOVE_RECIPE_SUCCESS : LangKey.MESSAGE_REMOVE_RECIPE_FAILED).sendMessage(player);
                                break;
                        }
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
