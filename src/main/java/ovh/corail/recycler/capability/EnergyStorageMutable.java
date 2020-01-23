package ovh.corail.recycler.capability;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.energy.EnergyStorage;

public class EnergyStorageMutable extends EnergyStorage {
    public EnergyStorageMutable() {
        super(10000, 20, 10);
    }

    public void setEnergyStored(int energy) {
        this.energy = MathHelper.clamp(energy, 0, getMaxEnergyStored());
    }
}
