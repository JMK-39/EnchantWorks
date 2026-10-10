package dev.xyat.enchantworks.anvil.config;

import java.util.List;
import java.util.Arrays;
import java.util.regex.Pattern;

/** Editable values, independent of Minecraft; legacy config remains compatible. */
record VisualRuleDraft(List<String> targets, List<String> enchantments, boolean all, int multiplier) {
    VisualRuleDraft {
        targets = List.copyOf(targets);
        enchantments = List.copyOf(enchantments);
    }
    static VisualRuleDraft enchantment(String source) {
        String[] parts = source.split(";", -1);
        String effects = parts.length < 2 ? "" : parts[1].trim();
        boolean all = effects.isEmpty() || effects.equals("*") || effects.equalsIgnoreCase("ALL");
        return new VisualRuleDraft(split(parts[0]), all ? List.of() : split(effects), all, 1);
    }
    static VisualRuleDraft smelter(String source) {
        var match = Pattern.compile("^(\\d+)x\\s*(.+)$").matcher(source.trim());
        return match.matches()
                ? new VisualRuleDraft(List.of(match.group(2).trim()), List.of(), false, Integer.parseInt(match.group(1)))
                : new VisualRuleDraft(List.of(source.trim()), List.of(), false, 2);
    }
    private static List<String> split(String text) {
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }
    String enchantmentRule() { return String.join(", ", targets) + "; " + (all ? "*" : String.join(", ", enchantments)); }
    String smelterRule() { return multiplier + "x " + targets.get(0); }
}
