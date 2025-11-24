package net.vulkanmod.fsr.state;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.vulkan.VK10;

public final class FsrSupport {
    private static boolean checked;
    private static boolean supported;

    private FsrSupport() {}

    public static boolean checkCapabilities() {
        if (checked) return supported;
        checked = true;

        // Basic check: ensure Vulkan library is present and that the platform can load it.
        try {
            VK10.getInstance();
            supported = true;
        } catch (Throwable t) {
            supported = false;
        }

        // Optionally gate on native libraries if missing; skip hard failures in dev environments.
        if (!supported && FabricLoader.getInstance().isDevelopmentEnvironment()) {
            supported = true;
        }

        return supported;
    }
}
