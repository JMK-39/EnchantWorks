package dev.xyat.enchantworks.anvil.config;

import java.util.List;

public final class VisualRuleDraftRegression {
    public static void main(String[] args) {
        var rule = VisualRuleDraft.enchantment(" minecraft:stick, #minecraft:axes, @create ; minecraft:sharpness, minecraft:mending ");
        require(rule.targets().equals(List.of("minecraft:stick", "#minecraft:axes", "@create")), "Every legacy target must survive editing");
        require(VisualRuleDraft.enchantment(rule.enchantmentRule()).equals(rule), "Enchantment rule round trip");
        for (String suffix : List.of("", ";", "; *", "; ALL")) {
            require(VisualRuleDraft.enchantment("minecraft:stick" + suffix).all(), "Legacy wildcard: " + suffix);
        }
        var implicit = VisualRuleDraft.smelter("#forge:ores");
        require(implicit.multiplier() == 2, "Omitted multiplier must retain the legacy 2x default");
        var explicit = VisualRuleDraft.smelter("3x minecraft:raw_iron");
        require(explicit.multiplier() == 3 && explicit.targets().equals(List.of("minecraft:raw_iron")), "Explicit multiplier");
        require(VisualRuleDraft.smelter(explicit.smelterRule()).equals(explicit), "Smelter rule round trip");
        System.out.println("VISUAL_RULE_DRAFT_PASS");
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
