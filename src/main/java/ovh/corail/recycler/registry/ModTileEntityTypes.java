package ovh.corail.recycler.registry;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;
import ovh.corail.recycler.util.Helper;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@ObjectHolder(MOD_ID)
public class ModTileEntityTypes {
    public static final TileEntityType<?> RECYCLER = Helper.getDefaultNotNull();
}
