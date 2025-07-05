package org.embeddedt.embeddium.impl.util;

import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Profiler;

public class ProfilerUtil {
    public static ProfilerFiller get() {
        return Profiler.get();
    }
}
