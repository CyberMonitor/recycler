package ovh.corail.recycler.registry;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.ObjectHolder;
import ovh.corail.recycler.gui.ContainerRecycler;
import ovh.corail.recycler.gui.ContainerRecyclingBook;
import ovh.corail.recycler.util.Helper;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@ObjectHolder(MOD_ID)
public class ModContainers {
    public static final ContainerType<ContainerRecycler> RECYCLER = Helper.getDefaultNotNull();
    public static final ContainerType<ContainerRecyclingBook> RECYCLING_BOOK = Helper.getDefaultNotNull();
}
