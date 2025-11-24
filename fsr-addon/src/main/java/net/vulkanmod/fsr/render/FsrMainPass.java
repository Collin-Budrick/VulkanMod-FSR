package net.vulkanmod.fsr.render;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.vulkanmod.fsr.state.FsrConfig;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.pass.DefaultMainPass;
import net.vulkanmod.vulkan.pass.MainPass;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;

/**
 * Addon-owned MainPass that will host the FSR render path. For now it delegates
 * to the default pass until FSR resources are fully wired.
 */
public class FsrMainPass implements MainPass {
    private final Renderer renderer;
    private final FsrConfig config;
    private final DefaultMainPass fallback;
    private final FsrContext context;

    public FsrMainPass(Renderer renderer, FsrConfig config) {
        this.renderer = renderer;
        this.config = config;
        this.fallback = DefaultMainPass.create();
        this.context = new FsrContext(renderer, config);
    }

    @Override
    public void begin(VkCommandBuffer commandBuffer, MemoryStack stack) {
        context.initializeIfNeeded();

        if (!context.isReady()) {
            fallback.begin(commandBuffer, stack);
            return;
        }

        // Bind offscreen render targets for main scene rendering.
        context.beginFrame(commandBuffer, stack);
    }

    @Override
    public void end(VkCommandBuffer commandBuffer) {
        if (!context.isReady()) {
            fallback.end(commandBuffer);
            return;
        }

        // Close the offscreen render pass if it was started.
        if (context.isFrameActive()) {
            renderer.endRenderPass(commandBuffer);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            context.dispatch(commandBuffer, stack);
        }
    }

    @Override
    public void cleanUp() {
        context.cleanup();
        fallback.cleanUp();
    }

    @Override
    public void onResize() {
        context.resize();
        fallback.onResize();
    }

    @Override
    public void rebindMainTarget() {
        fallback.rebindMainTarget();
    }

    @Override
    public void bindAsTexture() {
        fallback.bindAsTexture();
    }

    @Override
    public GpuTexture getColorAttachment() {
        return fallback.getColorAttachment();
    }

    @Override
    public GpuTextureView getColorAttachmentView() {
        return fallback.getColorAttachmentView();
    }
}
