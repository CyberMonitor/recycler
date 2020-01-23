package ovh.corail.recycler.registry;

import net.minecraft.advancements.CriteriaTriggers;
import ovh.corail.recycler.util.StatelessTrigger;

public class ModTriggers {
    public static final StatelessTrigger BUILD_RECYCLER = register("build_recycler");
    public static final StatelessTrigger BUILD_DISK = register("build_disk");
    public static final StatelessTrigger FIRST_RECYCLE = register("first_recycle");
    public static final StatelessTrigger READ_RECYCLING_BOOK = register("read_recycling_book");

    private static StatelessTrigger register(String name) {
        return CriteriaTriggers.register(new StatelessTrigger(name));
    }
}
