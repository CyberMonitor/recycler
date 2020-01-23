package ovh.corail.recycler.registry;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;
import ovh.corail.recycler.util.INotNull;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@ObjectHolder(MOD_ID)
public class ModTileEntityTypes extends INotNull {
    public static final TileEntityType<?> RECYCLER = getDefaultNotNull();
}
