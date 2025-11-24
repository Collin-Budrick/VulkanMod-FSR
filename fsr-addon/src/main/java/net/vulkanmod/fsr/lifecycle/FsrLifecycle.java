package net.vulkanmod.fsr.lifecycle;

import net.minecraft.client.Minecraft;
import net.vulkanmod.fsr.render.FsrMainPass;
import net.vulkanmod.fsr.state.FsrConfig;
import net.vulkanmod.fsr.state.FsrSupport;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.pass.DefaultMainPass;

public final class FsrLifecycle {
    private static boolean initialized;

    private FsrLifecycle() {}

    public static void bootstrap() {
        FsrConfig.load();
    }

    public static void onRendererInit(Renderer renderer) {
        if (initialized) return;
        initialized = true;

        FsrConfig config = FsrConfig.get();
        boolean supported = FsrSupport.checkCapabilities();

        if (!supported || !config.enabled()) {
            renderer.setMainPass(DefaultMainPass.create());
            return;
        }

        renderer.setMainPass(new FsrMainPass(renderer, config));
    }
}
