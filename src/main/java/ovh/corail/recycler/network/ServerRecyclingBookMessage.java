package ovh.corail.recycler.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import ovh.corail.recycler.gui.ContainerRecyclingBook;
import ovh.corail.recycler.registry.ModTriggers;

import java.util.function.Supplier;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

public class ServerRecyclingBookMessage {
    public enum RecyclingBookAction {RECYCLING_BOOK, CHANGE_PAGE, SEARCH_TEXT}

    private final RecyclingBookAction action;
    private int pageNum;
    private String searchText;

    public ServerRecyclingBookMessage(RecyclingBookAction action, Object... params) {
        this.action = action;
        if (action == RecyclingBookAction.CHANGE_PAGE) {
            this.pageNum = params.length == 1 ? (int) params[0] : 0;
        } else if (action == RecyclingBookAction.SEARCH_TEXT) {
            this.searchText = params.length == 1 ? String.valueOf(params[0]) : "";
        }
    }

    static ServerRecyclingBookMessage fromBytes(PacketBuffer buf) {
        RecyclingBookAction currentAction = RecyclingBookAction.values()[buf.readShort()];
        switch (currentAction) {
            case CHANGE_PAGE:
                return new ServerRecyclingBookMessage(currentAction, buf.readInt());
            case SEARCH_TEXT:
                return new ServerRecyclingBookMessage(currentAction, buf.readString(20));
            case RECYCLING_BOOK:
            default:
                return new ServerRecyclingBookMessage(currentAction);
        }
    }

    static void toBytes(ServerRecyclingBookMessage msg, PacketBuffer buf) {
        buf.writeShort(msg.action.ordinal());
        if (msg.action == RecyclingBookAction.CHANGE_PAGE) {
            buf.writeInt(msg.pageNum);
        } else if (msg.action == RecyclingBookAction.SEARCH_TEXT) {
            buf.writeString(msg.searchText);
        }
    }

    static class Handler {
        static void handle(final ServerRecyclingBookMessage message, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                if (player != null) {
                    switch (message.action) {
                        case RECYCLING_BOOK:
                            ModTriggers.READ_RECYCLING_BOOK.trigger(player);
                            NetworkHooks.openGui(player, new INamedContainerProvider() {
                                @Override
                                public ITextComponent getDisplayName() {
                                    return new TranslationTextComponent(MOD_ID + ".message.recycling_book");
                                }

                                @Override
                                public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
                                    return new ContainerRecyclingBook(windowId);
                                }
                            });
                            break;
                        case CHANGE_PAGE:
                            if (player.openContainer instanceof ContainerRecyclingBook) {
                                ((ContainerRecyclingBook) player.openContainer).initPage(message.pageNum);
                            }
                            break;
                        case SEARCH_TEXT:
                            if (player.openContainer instanceof ContainerRecyclingBook) {
                                ((ContainerRecyclingBook) player.openContainer).updateSearchText(message.searchText);
                            }
                            break;
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
