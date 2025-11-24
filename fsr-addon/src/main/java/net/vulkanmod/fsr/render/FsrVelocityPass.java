package net.vulkanmod.fsr.render;

import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRDynamicRendering;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRenderingAttachmentInfo;
import org.lwjgl.vulkan.VkRenderingInfo;

/**
 * Placeholder velocity pass that clears the velocity buffer. In a full implementation this
 * would render a velocity shader writing per-pixel motion vectors using previous/current matrices.
 */
public final class FsrVelocityPass {
    private final Renderer renderer;

    public FsrVelocityPass(Renderer renderer) {
        this.renderer = renderer;
    }

    public void clear(VkCommandBuffer commandBuffer, MemoryStack stack, FsrRenderTargets targets) {
        if (targets.velocityImage() == null) {
            return;
        }

        VkRenderingAttachmentInfo.Buffer colorAttachment = VkRenderingAttachmentInfo.calloc(1, stack)
                .sType(KHRDynamicRendering.VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO_KHR)
                .imageView(targets.velocityImage().getImageView())
                .imageLayout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
                .loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
                .clearValue(cv -> cv.color().float32(stack.floats(0f, 0f, 0f, 0f)));

        VkRenderingInfo renderingInfo = VkRenderingInfo.calloc(stack)
                .sType(KHRDynamicRendering.VK_STRUCTURE_TYPE_RENDERING_INFO_KHR)
                .renderArea(ra -> {
                    ra.offset().set(0, 0);
                    ra.extent().set(targets.resolution().renderWidth(), targets.resolution().renderHeight());
                })
                .layerCount(1)
                .pColorAttachments(colorAttachment);

        KHRDynamicRendering.vkCmdBeginRenderingKHR(commandBuffer, renderingInfo);
        KHRDynamicRendering.vkCmdEndRenderingKHR(commandBuffer);
    }
}
