package net.vulkanmod.fsr.render;

import net.vulkanmod.fsr.state.FsrConfig;
import net.vulkanmod.vulkan.Renderer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageBlit;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.KHRSwapchain;

/**
 * Placeholder wrapper for the FidelityFX context. This will own descriptor pools,
 * pipelines, and scratch buffers once the SDK is fully wired.
 */
public final class FsrContext {
    private final Renderer renderer;
    private final FsrConfig config;
    private boolean ready;
    private final FsrRenderTargets renderTargets;
    private boolean frameActive;
    private final FsrVelocityPass velocityPass;
    private final FsrReactivePass reactivePass;
    private final FsrUpscalePass upscalePass;

    public FsrContext(Renderer renderer, FsrConfig config) {
        this.renderer = renderer;
        this.config = config;
        this.renderTargets = new FsrRenderTargets(renderer);
        this.velocityPass = new FsrVelocityPass(renderer);
        this.reactivePass = new FsrReactivePass();
        this.upscalePass = new FsrUpscalePass(renderer);
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isFrameActive() {
        return frameActive;
    }

    public void initializeIfNeeded() {
        if (ready) return;
        // TODO: build FSR pipelines, descriptor sets, and offscreen targets based on config.
        renderTargets.ensure(config);
        ready = true;
    }

    public void beginFrame(VkCommandBuffer commandBuffer, MemoryStack stack) {
        // Ensure targets exist and bind the offscreen framebuffer for rendering.
        renderTargets.ensure(config);
        if (!renderTargets.begin()) {
            return;
        }
        // Placeholder: clear velocity/mask targets. Will be replaced by actual rendering.
        velocityPass.clear(commandBuffer, stack, renderTargets);
        reactivePass.clear(commandBuffer, stack, renderTargets);
        frameActive = true;
    }

    public void dispatch(VkCommandBuffer commandBuffer, MemoryStack stack) {
        if (!frameActive) {
            return;
        }
        // TODO: bind FSR descriptors and dispatch compute using color/depth/velocity/reactive inputs.
        upscalePass.dispatch(commandBuffer, stack, renderTargets);
        frameActive = false;
    }

    public void resize() {
        // TODO: recreate render targets and FSR contexts.
        renderTargets.resize();
    }

    public void cleanup() {
        renderTargets.destroy();
        ready = false;
        frameActive = false;
    }
}
