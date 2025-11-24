# VulkanMod FSR Addon

Standalone Fabric addon that injects a custom `MainPass` to run FidelityFX Super Resolution for VulkanMod without modifying the base mod.

## Building
- Place or build `VulkanMod_1.21.10.jar` in `../VulkanMod-Refcode/build/libs`.
- Run `./gradlew build` from this directory.

## Notes
- Uses mixins to swap the renderer pass after `Renderer.initRenderer`.
- FSR path is scaffolded; it currently delegates to VulkanMod's `DefaultMainPass` until the FSR pipelines are wired.
