package net.vulkanmod.fsr;

import net.fabricmc.api.ClientModInitializer;
import net.vulkanmod.fsr.lifecycle.FsrLifecycle;

public final class FsrAddonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FsrLifecycle.bootstrap();
    }
}
