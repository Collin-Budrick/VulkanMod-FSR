package net.vulkanmod.fsr.state;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FsrConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "vulkanmod-fsr.json";
    private static FsrConfig INSTANCE = new FsrConfig();

    public enum Preset {
        NATIVE_AA(1.0f),
        QUALITY(1.5f),
        BALANCED(1.7f),
        PERFORMANCE(2.0f),
        ULTRA_PERFORMANCE(3.0f);

        public final float scale;
        Preset(float scale) { this.scale = scale; }
    }

    private boolean enabled = true;
    private Preset preset = Preset.QUALITY;
    private boolean enableRcas = true;
    private float rcasSharpness = 0.3f;

    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path file = configDir.resolve(FILE_NAME);

        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                INSTANCE = GSON.fromJson(reader, FsrConfig.class);
                return;
            } catch (IOException ignored) {
            }
        }

        save(); // write defaults
    }

    public static void save() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path file = configDir.resolve(FILE_NAME);

        try {
            Files.createDirectories(configDir);
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static FsrConfig get() {
        return INSTANCE;
    }

    public boolean enabled() {
        return enabled;
    }

    public Preset preset() {
        return preset;
    }

    public boolean enableRcas() {
        return enableRcas;
    }

    public float rcasSharpness() {
        return rcasSharpness;
    }
}
