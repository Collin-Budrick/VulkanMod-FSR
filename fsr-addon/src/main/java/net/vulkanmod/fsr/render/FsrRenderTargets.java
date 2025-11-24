package net.vulkanmod.fsr.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.vulkanmod.fsr.state.FsrConfig;
import net.vulkanmod.fsr.state.FsrResolution;
import net.vulkanmod.render.engine.VkGpuDevice;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.RenderPass;
import net.vulkanmod.vulkan.framebuffer.SwapChain;
import net.vulkanmod.vulkan.texture.VulkanImage;
import net.vulkanmod.fsr.state.FsrConfig;
import net.vulkanmod.fsr.state.FsrResolution;
import org.lwjgl.vulkan.VK10;

/**
 * Holds addon-managed render targets (color, depth, velocity, masks) at render resolution.
 * Actual Vulkan image creation will be wired through VkGpuDevice once the SDK is integrated.
 */
public final class FsrRenderTargets {
    private final Renderer renderer;

    private Framebuffer framebuffer;
    private RenderPass renderPass;
    private FsrResolution resolution;
    private GpuTexture colorTexture;
    private GpuTexture depthTexture;
    private VulkanImage velocityImage;
    private VulkanImage reactiveMaskImage;
    private GpuTexture velocityTexture;
    private GpuTexture reactiveTexture;

    public FsrRenderTargets(Renderer renderer) {
        this.renderer = renderer;
    }

    public void ensure(FsrConfig config) {
        SwapChain swapChain = renderer.getSwapChain();
        FsrResolution desired = FsrResolution.fromPreset(swapChain.getWidth(), swapChain.getHeight(), config.preset());

        boolean needsRecreate = resolution == null
                || desired.renderWidth() != resolution.renderWidth()
                || desired.renderHeight() != resolution.renderHeight();

        if (!needsRecreate && framebuffer != null && renderPass != null) {
            return;
        }

        destroy();
        resolution = desired;

        framebuffer = Framebuffer.builder(resolution.renderWidth(), resolution.renderHeight(), 1, true)
                .setFormat(VK10.VK_FORMAT_R8G8B8A8_UNORM)
                .build();

        RenderPass.Builder builder = RenderPass.builder(framebuffer);
        if (builder.getColorAttachmentInfo() != null) {
            builder.getColorAttachmentInfo()
                    .setOps(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR, VK10.VK_ATTACHMENT_STORE_OP_STORE)
                    .setFinalLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        }
        if (builder.getDepthAttachmentInfo() != null) {
            builder.getDepthAttachmentInfo()
                    .setOps(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR, VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                    .setFinalLayout(VK10.VK_IMAGE_LAYOUT_DEPTH_STENCIL_READ_ONLY_OPTIMAL);
        }

        renderPass = builder.build();

        // Wrap Vulkan images as GpuTextures for sampling in later passes.
        VkGpuDevice device = (VkGpuDevice) RenderSystem.getDevice();
        VulkanImage colorAttachment = framebuffer.getColorAttachment();
        VulkanImage depthAttachment = framebuffer.getDepthAttachment();
        colorTexture = device.gpuTextureFromVulkanImage(colorAttachment);
        depthTexture = device.gpuTextureFromVulkanImage(depthAttachment);

        // Allocate velocity and reactive mask images for motion vectors and masks.
        velocityImage = VulkanImage.builder(resolution.renderWidth(), resolution.renderHeight())
                .setName("fsr_velocity")
                .setFormat(VK10.VK_FORMAT_R16G16_SFLOAT)
                .addUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_STORAGE_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT)
                .setClamp(true)
                .createVulkanImage();

        reactiveMaskImage = VulkanImage.builder(resolution.renderWidth(), resolution.renderHeight())
                .setName("fsr_reactive")
                .setFormat(VK10.VK_FORMAT_R8_UNORM)
                .addUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_STORAGE_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT)
                .setClamp(true)
                .createVulkanImage();

        velocityTexture = device.gpuTextureFromVulkanImage(velocityImage);
        reactiveTexture = device.gpuTextureFromVulkanImage(reactiveMaskImage);
    }

    public void resize() {
        destroy();
        // defer recreation to ensure(...) call with updated config
    }

    public void destroy() {
        if (renderPass != null) {
            renderPass.cleanUp();
            renderPass = null;
        }
        if (framebuffer != null) {
            framebuffer.cleanUp();
            framebuffer = null;
        }
        if (velocityImage != null) {
            velocityImage.free();
            velocityImage = null;
        }
        if (reactiveMaskImage != null) {
            reactiveMaskImage.free();
            reactiveMaskImage = null;
        }
        if (colorTexture != null) {
            colorTexture.close();
            colorTexture = null;
        }
        if (depthTexture != null) {
            depthTexture.close();
            depthTexture = null;
        }
        if (velocityTexture != null) {
            velocityTexture.close();
            velocityTexture = null;
        }
        if (reactiveTexture != null) {
            reactiveTexture.close();
            reactiveTexture = null;
        }
    }

    public Framebuffer framebuffer() {
        return framebuffer;
    }

    public RenderPass renderPass() {
        return renderPass;
    }

    public FsrResolution resolution() {
        return resolution;
    }

    public GpuTexture colorTexture() {
        return colorTexture;
    }

    public GpuTexture depthTexture() {
        return depthTexture;
    }

    public VulkanImage velocityImage() {
        return velocityImage;
    }

    public VulkanImage reactiveMaskImage() {
        return reactiveMaskImage;
    }

    public GpuTexture velocityTexture() {
        return velocityTexture;
    }

    public GpuTexture reactiveTexture() {
        return reactiveTexture;
    }

    public boolean begin() {
        return renderer.beginRendering(renderPass, framebuffer);
    }
}
