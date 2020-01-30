package ovh.corail.recycler.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.network.NetworkHooks;
import ovh.corail.recycler.gui.ContainerRecycler;
import ovh.corail.recycler.tileentity.TileEntityRecycler;
import ovh.corail.recycler.util.Helper;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@SuppressWarnings({ "deprecation", "WeakerAccess" })
public class BlockRecycler extends Block {
    private static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");

    public BlockRecycler() {
        this(getBuilder());
    }

    protected BlockRecycler(Properties builder) {
        super(builder);
        setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(ENABLED, false));
    }

    @Override
    public String getTranslationKey() {
        return MOD_ID + ".block.recycler";
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
        if (Helper.isValidPlayer(player) && !player.isSpectator()) {
            if (!world.isRemote) {
                TileEntityRecycler recycler = getTileEntity(world, pos);
                if (recycler != null) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
                        @Override
                        public ITextComponent getDisplayName() {
                            return new TranslationTextComponent(MOD_ID + ".block.recycler");
                        }

                        @Override
                        public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
                            return new ContainerRecycler(windowId, playerInventory, recycler);
                        }
                    }, buf -> buf.writeBlockPos(pos));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onReplaced(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (oldState.getBlock() != newState.getBlock()) {
            TileEntityRecycler recycler = getTileEntity(world, pos);
            if (recycler != null) {
                IntStream.range(0, recycler.getInventoryInput().getSlots()).forEach(slot -> InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), recycler.getInventoryInput().getStackInSlot(slot)));
                IntStream.range(0, recycler.getInventoryWorking().getSlots()).forEach(slot -> InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), recycler.getInventoryWorking().getStackInSlot(slot)));
                IntStream.range(0, recycler.getInventoryOutput().getSlots()).forEach(slot -> InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), recycler.getInventoryOutput().getStackInSlot(slot)));
                world.removeTileEntity(pos);
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        if (entity != null) {
            world.setBlockState(pos, state.with(FACING, entity.getHorizontalFacing().getOpposite()));
        }
    }

    @Override
    public int getLightValue(BlockState state) {
        return state.get(ENABLED) ? 15 : 0;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Nullable
    public static TileEntityRecycler getTileEntity(World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityRecycler ? (TileEntityRecycler) tile : null;
    }

    @Override
    public TileEntityRecycler createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityRecycler();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    public static Properties getBuilder() {
        return Properties.create(Material.ROCK)
                .hardnessAndResistance(5f, 20f)
                .sound(SoundType.STONE)
                .harvestTool(ToolType.PICKAXE)
                .harvestLevel(0);
    }
}
