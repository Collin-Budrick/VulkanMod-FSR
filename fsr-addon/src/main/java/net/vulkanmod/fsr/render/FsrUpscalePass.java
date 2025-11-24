package net.vulkanmod.fsr.render;

import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.framebuffer.SwapChain;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSwapchain;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkImageBlit;

/**
 * Placeholder upscaler: performs a linear blit from render-res color to the swapchain.
 * Intended to be replaced by FidelityFX FSR compute dispatch plus composite.
 */
public final class FsrUpscalePass {
    private final Renderer renderer;

    public FsrUpscalePass(Renderer renderer) {
        this.renderer = renderer;
    }

    public void dispatch(VkCommandBuffer commandBuffer, MemoryStack stack, FsrRenderTargets targets) {
        SwapChain swapChain = renderer.getSwapChain();

        var src = targets.framebuffer().getColorAttachment();
        var dst = swapChain.getColorAttachment();

        src.transitionImageLayout(stack, commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
        dst.transitionImageLayout(stack, commandBuffer, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

        VkImageBlit.Buffer blit = VkImageBlit.calloc(1, stack);
        blit.srcSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
        blit.srcSubresource().layerCount(1);
        blit.srcOffsets(0).set(0, 0, 0);
        blit.srcOffsets(1).set(targets.resolution().renderWidth(), targets.resolution().renderHeight(), 1);

        blit.dstSubresource().aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT);
        blit.dstSubresource().layerCount(1);
        blit.dstOffsets(0).set(0, 0, 0);
        blit.dstOffsets(1).set(swapChain.getWidth(), swapChain.getHeight(), 1);

        VK10.vkCmdBlitImage(
                commandBuffer,
                src.getId(), VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                dst.getId(), VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                blit,
                VK10.VK_FILTER_LINEAR
        );

        dst.transitionImageLayout(stack, commandBuffer, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
    }
}
