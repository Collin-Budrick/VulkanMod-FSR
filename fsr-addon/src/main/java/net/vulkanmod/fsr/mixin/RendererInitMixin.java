package net.vulkanmod.fsr.mixin;

import net.vulkanmod.fsr.lifecycle.FsrLifecycle;
import net.vulkanmod.vulkan.Renderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Renderer.class)
public class RendererInitMixin {
    @Inject(method = "initRenderer", at = @At("TAIL"))
    private static void fsraddon$onInit(CallbackInfo ci) {
        FsrLifecycle.onRendererInit(Renderer.getInstance());
    }
}
