package net.vulkanmod.fsr.state;

public record FsrResolution(int renderWidth, int renderHeight, int displayWidth, int displayHeight) {
    public static FsrResolution fromPreset(int displayWidth, int displayHeight, FsrConfig.Preset preset) {
        float scale = preset.scale;
        int renderW = Math.max(1, Math.round(displayWidth / scale));
        int renderH = Math.max(1, Math.round(displayHeight / scale));
        return new FsrResolution(renderW, renderH, displayWidth, displayHeight);
    }
}
