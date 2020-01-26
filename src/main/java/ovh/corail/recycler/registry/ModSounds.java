package ovh.corail.recycler.registry;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import ovh.corail.recycler.ConfigRecycler;
import ovh.corail.recycler.util.Helper;

import javax.annotation.Nullable;

import static ovh.corail.recycler.ModRecycler.MOD_ID;

@ObjectHolder(MOD_ID)
public class ModSounds {
    public static final SoundEvent RECYCLER = Helper.getDefaultNotNull();
    public static final SoundEvent RECYCLER_WORKING = Helper.getDefaultNotNull();

    public static void playSoundAllAround(@Nullable SoundEvent sound, SoundCategory cat, World world, BlockPos pos, float volume, float pitch) {
        if (!world.isRemote && sound != null && ConfigRecycler.general.allow_sound.get()) {
            world.playSound(null, pos, sound, cat, volume, pitch);
        }
    }
}
