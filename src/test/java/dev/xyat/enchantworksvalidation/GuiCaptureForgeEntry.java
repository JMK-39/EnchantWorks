//? if <1.21 {
package dev.xyat.enchantworksvalidation;

@net.minecraftforge.fml.common.Mod("enchantworks_validation")
public final class GuiCaptureForgeEntry {
    public GuiCaptureForgeEntry() {
        if (Boolean.getBoolean("enchantworks.guiValidation")) {
            dev.xyat.kineticcore.api.runtime.KineticPlatform.runOnClient(() -> GuiCaptureValidation::install);
        }
    }
}
//?}
