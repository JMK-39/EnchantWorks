package dev.xyat.enchantworks.anvil.config;

import dev.xyat.kineticcore.api.client.gui.KineticGui;
import dev.xyat.kineticcore.api.client.gui.input.*;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.render.KineticGraphics;
import dev.xyat.kineticcore.api.client.gui.scroll.KineticScrollController;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.gui.theme.KineticTheme;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.ui.NumberType;
import dev.xyat.kineticcore.api.client.gui.widget.KineticCustomControl;
import dev.xyat.kineticcore.api.client.gui.widget.list.SelectionItem;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList;
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.search.KineticSuggestion;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.effect.MobEffect;

import java.util.*;
import java.util.function.Consumer;

/** Selection-only editors. Nothing is written until the parent config page is saved. */
public final class VisualRuleEditors {
    enum Kind { ENCHANTMENTS, SMELTER }
    private VisualRuleEditors() { }
    private static Component tr(String key, Object... args) {
        return KineticI18n.translatable("gui.enchantworks.rules." + key, args);
    }
    public static void enchantments(Component title, List<String> initial, Consumer<List<String>> apply) {
        KineticGui.openChild(new RuleListPage(title, initial, apply, Kind.ENCHANTMENTS));
    }
    public static void smelter(Component title, List<String> initial, Consumer<List<String>> apply) {
        KineticGui.openChild(new RuleListPage(title, initial, apply, Kind.SMELTER));
    }
    public static void blocks(Component title, List<String> initial, Consumer<List<String>> apply) {
        KineticSelectors.openItemListEditor(title, initial, KineticSelectors.ItemListMode.ITEMS_TAGS_MODS,
                BLOCK_RULES, apply);
    }
    public static void disabled(Component title, List<String> initial, Consumer<List<String>> apply) {
        KineticGui.openChild(new EnchantmentPicker(title, initial, apply));
    }
    public static void effects(Component title, List<String> initial, Consumer<List<String>> apply) {
        KineticGui.openChild(new EffectPicker(title, initial, apply));
    }

    /** This setting matches blocks, whose IDs, tags and namespaces can differ from those of their items. */
    private static final KineticSelectors.ItemListRuleAdapter BLOCK_RULES = new KineticSelectors.ItemListRuleAdapter() {
        private List<String> tags = List.of(), candidates;
        private Set<String> mods, allowed = Set.of();
        @Override public Collection<String> ruleCandidates() {
            var currentTags = KineticRegistries.blocks().tagIds().stream().map(id -> "#" + id).sorted().toList();
            if (mods == null) {
                var namespaces = new TreeSet<String>(); KineticRegistries.blocks().ids().forEach(id -> namespaces.add("@" + id.getNamespace()));
                mods = Set.copyOf(namespaces);
            }
            if (candidates == null || !tags.equals(currentTags)) {
                tags = currentTags; var values = new TreeSet<String>(tags); values.addAll(mods);
                candidates = List.copyOf(values); allowed = Set.copyOf(values);
            }
            return candidates;
        }
        @Override public boolean acceptsItem(net.minecraft.world.item.ItemStack stack) { return stack.getItem() instanceof BlockItem; }
        @Override public String itemRule(net.minecraft.world.item.ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem item)) return null;
            var id = KineticRegistries.blocks().id(item.getBlock()); return id == null ? null : id.toString();
        }
        @Override public boolean matches(String rule, net.minecraft.world.item.ItemStack stack) {
            if (!(stack.getItem() instanceof BlockItem item)) return false;
            var id = KineticRegistries.blocks().id(item.getBlock()); if (id == null) return false;
            if (rule.startsWith("@")) return id.getNamespace().equals(rule.substring(1));
            if (rule.startsWith("#")) return KineticRegistries.blocks().isInTag(item.getBlock(), KineticResourceIds.tryParse(rule.substring(1)));
            return id.toString().equals(rule);
        }
        @Override public boolean acceptsRule(String rule) {
            if (rule.startsWith("@") || rule.startsWith("#")) { if (candidates == null) ruleCandidates(); return allowed.contains(rule); }
            var id = KineticResourceIds.tryParse(rule); return id != null && KineticRegistries.blocks().contains(id);
        }
    };

    private static String targetName(String target) {
        if (target.startsWith("#")) return tr("tag", target.substring(1)).getString();
        if (target.startsWith("@")) return tr("mod", target.substring(1)).getString();
        var id = KineticResourceIds.tryParse(target);
        var item = id == null ? null : KineticRegistries.items().get(id);
        return item == null ? target : new net.minecraft.world.item.ItemStack(item).getHoverName().getString();
    }
    private static String enchantmentName(String id) {
        return KineticSearch.enchantmentDictionary().stream().filter(s -> s.value().equals(id))
                .map(s -> s.translation().getString().isBlank() ? s.value() : s.translation().getString())
                .findFirst().orElse(id);
    }
    private static String summary(String raw, Kind kind) {
        try {
            var draft = kind == Kind.SMELTER ? VisualRuleDraft.smelter(raw) : VisualRuleDraft.enchantment(raw);
            String targets = String.join(", ", draft.targets().stream().map(VisualRuleEditors::targetName).toList());
            String value = kind == Kind.SMELTER ? tr("multiplier_value", draft.multiplier()).getString()
                    : draft.all() ? tr("all").getString()
                    : String.join(", ", draft.enchantments().stream().map(VisualRuleEditors::enchantmentName).toList());
            return targets + "  →  " + value;
        } catch (RuntimeException invalid) {
            return tr("legacy_invalid").getString();
        }
    }

    private static final class RuleListPage extends KineticPage {
        final ArrayList<String> rules;
        final Consumer<List<String>> apply;
        final Kind kind;
        int selected = -1, offset;
        RuleRows list;
        RuleListPage(Component title, List<String> initial, Consumer<List<String>> apply, Kind kind) {
            super(title); rules = new ArrayList<>(initial); this.apply = apply; this.kind = kind;
        }
        @Override protected void build(KineticUi ui) {
            bindDeleteAction(() -> selected >= 0 && selected < rules.size(), this::removeSelected);
            // Every action sits in the header row; the rules fill the rest of the canvas.
            list = new RuleRows(this);
            list.setItems(rules); list.setSelectedIndex(selected); list.setScrollOffset(offset);
            list.setOnSelect(index -> selected = index);
            ui.add(list);
            ui.button(4, 4, 40).text(tr("cancel")).onClick(this::close).build();
            ui.button(48, 4, 56).text(tr("add")).onClick(() -> edit(-1)).build();
            ui.button(108, 4, 40).text(tr("edit")).onClick(() -> { if (selected >= 0) edit(selected); }).build();
            ui.button(152, 4, 40).text(tr("remove")).onClick(this::removeSelected).build();
            ui.button(596, 4, 40).text(tr("apply")).onClick(() -> { apply.accept(List.copyOf(rules)); close(); }).build();
        }
        void removeSelected() {
            if (selected >= 0 && selected < rules.size()) { offset = list.scrollOffset(); rules.remove(selected); selected = -1; rebuild(); }
        }
        void edit(int index) {
            offset = list.scrollOffset();
            Consumer<String> accept = value -> {
                if (index < 0) rules.add(value); else rules.set(index, value);
                selected = index < 0 ? rules.size() - 1 : index; rebuild();
            };
            openChild(new RuleDetails(title(), index < 0 ? null : rules.get(index), kind, accept));
        }
        void rowMenu(int index, double x, double y) {
            selected = index;
            openContextMenu(x, y, List.of(
                    dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays.MenuItem.action(tr("edit"), () -> edit(index)),
                    dev.xyat.kineticcore.api.client.gui.overlay.KineticOverlays.MenuItem.action(tr("remove"), this::removeSelected)));
        }
        @Override protected void renderBackground(KineticGraphics g, int x, int y, float pt) {
            KineticTheme.panel(g, 0, 0, width(), height());
            KineticTheme.separator(g, 4, 24, width() - 8);
            // Centred on the screen, not in the free space between the buttons.
            g.scrollingTextCentered(title(), 320, 8, 2 * (320 - 196), 0xFFFFAA00, false);
        }
    }

    /** Item stacks shown for a rule target: the item, the items of a tag (cycled) or the first item of a mod. */
    private static final Map<String, List<net.minecraft.world.item.ItemStack>> TARGET_STACKS = new HashMap<>();
    private static List<net.minecraft.world.item.ItemStack> targetStacks(String target) {
        return TARGET_STACKS.computeIfAbsent(target, key -> {
            try {
                if (key.startsWith("#")) {
                    var id = KineticResourceIds.tryParse(key.substring(1));
                    if (id == null) return List.of();
                    return KineticRegistries.items().valuesInTag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, id))
                            .stream().map(net.minecraft.world.item.ItemStack::new).toList();
                }
                if (key.startsWith("@")) {
                    String namespace = key.substring(1);
                    return KineticRegistries.items().ids().stream().filter(id -> id.getNamespace().equals(namespace))
                            .min(Comparator.comparing(Object::toString))
                            .map(id -> List.of(new net.minecraft.world.item.ItemStack(KineticRegistries.items().get(id)))).orElse(List.of());
                }
                var id = KineticResourceIds.tryParse(key);
                var item = id == null ? null : KineticRegistries.items().get(id);
                return item == null ? List.of() : List.of(new net.minecraft.world.item.ItemStack(item));
            } catch (RuntimeException invalid) {
                return List.of();
            }
        });
    }

    /** One rule per row, read left to right: the target icons, an arrow, then what the rule applies. */
    private static final class RuleRows extends KineticRowList<String> {
        static final int SLOT = dev.xyat.kineticcore.api.client.gui.theme.KineticSizes.ITEM_SLOT;
        static final int PITCH = dev.xyat.kineticcore.api.client.gui.theme.KineticSizes.ITEM_PITCH;
        static final int TARGET_SLOTS = 11, ARROW_X = 4 + TARGET_SLOTS * PITCH + 2, VALUE_X = ARROW_X + 24;
        static final net.minecraft.world.item.ItemStack BOOK = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK);
        static final net.minecraft.world.item.ItemStack FURNACE = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FURNACE);
        final RuleListPage page;
        RuleRows(RuleListPage page) { super(4, 28, 632, 328, SLOT + 6); this.page = page; }

        VisualRuleDraft draft(String rule) {
            try { return page.kind == Kind.SMELTER ? VisualRuleDraft.smelter(rule) : VisualRuleDraft.enchantment(rule); }
            catch (RuntimeException invalid) { return null; }
        }
        int targetAt(int x, int width) {
            int local = mouseX() - x - 4;
            if (local < 0 || local >= TARGET_SLOTS * PITCH || local % PITCH >= SLOT) return -1;
            return local / PITCH;
        }
        @Override protected void renderRow(KineticGraphics g, String rule, int index, int x, int y, int width, int height, boolean hovered, boolean selected) {
            var draft = draft(rule);
            int textY = y + (height - 8) / 2;
            if (draft == null) {
                g.scrollingText(tr("legacy_invalid"), x + 6, textY, width - 12, KineticTheme.current().mutedText(), false);
                return;
            }
            var targets = draft.targets();
            int shown = targets.size() > TARGET_SLOTS ? TARGET_SLOTS - 1 : targets.size();
            int hover = hovered && mouseY() >= y + 3 && mouseY() < y + 3 + SLOT ? targetAt(x, width) : -1;
            for (int i = 0; i < shown; i++) {
                int slotX = x + 4 + i * PITCH;
                String target = targets.get(i);
                KineticTheme.itemSlot(g, slotX, y + 3, SLOT, hover == i);
                var stacks = targetStacks(target);
                if (!stacks.isEmpty())
                    KineticTheme.item(g, stacks.get((int) (System.currentTimeMillis() / 1000 % stacks.size())), slotX, y + 3, SLOT, 1.0F, false);
                else g.centeredText(Component.literal("?"), slotX + SLOT / 2, textY, KineticTheme.current().mutedText(), false);
                if (target.startsWith("#") || target.startsWith("@")) marker(g, target.substring(0, 1), slotX, y + 3);
            }
            if (shown < targets.size()) {
                int slotX = x + 4 + shown * PITCH;
                KineticTheme.itemSlot(g, slotX, y + 3, SLOT, hover == shown);
                g.centeredText(Component.literal("+" + (targets.size() - shown)), slotX + SLOT / 2, textY, KineticTheme.current().text(), false);
            }
            // The names of the targets fill the room between their icons and the arrow (tags and mods read here).
            int namesX = x + 4 + Math.min(targets.size(), TARGET_SLOTS) * PITCH + 4;
            g.scrollingText(Component.literal(String.join(", ", targets.stream().map(VisualRuleEditors::targetName).toList())),
                    namesX, textY, x + ARROW_X - 4 - namesX, KineticTheme.current().mutedText(), false);
            g.centeredText(Component.literal("\u2192"), x + ARROW_X + 10, textY, KineticTheme.current().mutedText(), false);
            int valueX = x + VALUE_X;
            KineticTheme.itemSlot(g, valueX, y + 3, SLOT, false);
            KineticTheme.item(g, page.kind == Kind.SMELTER ? FURNACE : BOOK, valueX, y + 3, SLOT, 1.0F, false);
            Component value = page.kind == Kind.SMELTER ? tr("multiplier_value", draft.multiplier())
                    : draft.all() ? tr("all").copy().withStyle(ChatFormatting.GOLD)
                    : Component.literal(String.join(", ", draft.enchantments().stream().map(VisualRuleEditors::enchantmentName).toList()));
            g.scrollingText(value, valueX + SLOT + 6, textY, x + width - (valueX + SLOT + 6) - 6, KineticTheme.current().text(), false);
        }
        static void marker(KineticGraphics g, String symbol, int x, int y) {
            g.push();
            try {
                g.raise(1); g.translate(x + 2, y + 2); g.scale(0.75F, 0.75F);
                g.text(Component.literal(symbol).withStyle(ChatFormatting.GOLD), 0, 0, 0xFFFFFFFF, true);
            } finally { g.pop(); }
        }
        @Override protected boolean onRowClick(String rule, int index, MouseInput input) {
            if (input.isRight()) { select(index); page.rowMenu(index, input.x(), input.y()); return true; }
            if (!input.isLeft()) return false;
            if (index == selectedIndex()) { page.edit(index); return true; }
            select(index); playClickSound(); return true;
        }
        @Override protected Component rowTooltip(String rule, int index) {
            var draft = draft(rule);
            int row = rowTop(index);
            if (draft != null && mouseY() >= row + 3 && mouseY() < row + 3 + SLOT) {
                int target = targetAt(controlX(), rowsWidth());
                if (target >= 0 && target < draft.targets().size()) {
                    String id = draft.targets().get(target);
                    if (target == TARGET_SLOTS - 1 && draft.targets().size() > TARGET_SLOTS)
                        return Component.literal(String.join("\n", draft.targets().subList(target, draft.targets().size()).stream().map(VisualRuleEditors::targetName).toList()));
                    return Component.literal(targetName(id)).append("\n").append(Component.literal(id).withStyle(ChatFormatting.GRAY));
                }
            }
            return Component.literal(summary(rule, page.kind)).append("\n").append(Component.literal(rule).withStyle(ChatFormatting.GRAY))
                    .append("\n").append(tr("row_hint").copy().withStyle(ChatFormatting.GRAY));
        }
    }

    private static final class RuleDetails extends KineticPage {
        final Kind kind;
        final Consumer<String> apply;
        List<String> targets = new ArrayList<>(), enchantments = new ArrayList<>();
        boolean all = true;
        String multiplier = "2";
        RuleDetails(Component title, String source, Kind kind, Consumer<String> apply) {
            super(title); this.kind = kind; this.apply = apply;
            if (source != null) {
                try {
                    var draft = kind == Kind.SMELTER ? VisualRuleDraft.smelter(source) : VisualRuleDraft.enchantment(source);
                    targets = new ArrayList<>(draft.targets()); enchantments = new ArrayList<>(draft.enchantments());
                    all = draft.all(); multiplier = String.valueOf(draft.multiplier());
                } catch (RuntimeException ignored) { /* Invalid legacy rows are kept until explicitly replaced. */ }
            }
        }
        // A form: a label column, then the controls; the targets show as item icons with their names after them.
        private static final int LABEL_X = 4, CONTROL_X = 56, BUTTON_W = 64, ROW1 = 28, ROW2 = 54;
        @Override protected void build(KineticUi ui) {
            ui.button(CONTROL_X, ROW1, BUTTON_W).text(tr("choose_targets", targets.size())).tooltip(tr("targets_hint"))
                    .onClick(() -> {
                        if (kind == Kind.SMELTER) KineticSelectors.openItemSelector(selection -> {
                            targets = new ArrayList<>(List.of(selectionRule(selection))); rebuild();
                        });
                        else KineticSelectors.openItemListEditor(tr("targets"), targets, KineticSelectors.ItemListMode.ITEMS_TAGS_MODS,
                                result -> { targets = new ArrayList<>(result); rebuild(); });
                    }).build();
            if (kind == Kind.SMELTER) {
                var field=ui.numberField(CONTROL_X, ROW2 + 1, 64, NumberType.INT).label(tr("multiplier")).range(0, Integer.MAX_VALUE)
                        .tooltip(tr("multiplier_hint")).build();
                field.setTextValue(multiplier);field.onTextChange(value -> multiplier=value);
            } else {
                ui.toggle(CONTROL_X, ROW2, BUTTON_W).value(all).labels(tr("all"), tr("specified"))
                        .tooltip(tr("all_hint")).onChange(value -> { all = value; rebuild(); }).build();
                ui.button(CONTROL_X + BUTTON_W + 4, ROW2, BUTTON_W).text(tr("choose_enchantments", enchantments.size())).enabled(!all)
                        .tooltip(tr("enchantments_hint")).onClick(() -> openChild(new EnchantmentPicker(tr("enchantments"), enchantments,
                                result -> { enchantments = new ArrayList<>(result); rebuild(); }))).build();
            }
            ui.button(4, 4, 40).text(tr("cancel")).onClick(this::close).build();
            ui.button(596, 4, 40).text(tr("apply")).onClick(() -> {
                if (targets.isEmpty() || (kind == Kind.ENCHANTMENTS && !all && enchantments.isEmpty())) return;
                int number;
                try { number = Integer.parseInt(multiplier); if (number < 0) return; } catch (NumberFormatException invalid) { return; }
                var draft = new VisualRuleDraft(targets, enchantments, all, number);
                apply.accept(kind == Kind.SMELTER ? draft.smelterRule() : draft.enchantmentRule()); close();
            }).enabled(!targets.isEmpty() && (kind != Kind.ENCHANTMENTS || all || !enchantments.isEmpty()))
                    .tooltip(tr("targets_hint")).build();
        }
        private int iconsX() { return CONTROL_X + BUTTON_W + 6; }
        private int shownIcons() { return Math.min(targets.size(), 12); }
        @Override protected void renderBackground(KineticGraphics g, int x, int y, float pt) {
            KineticTheme.panel(g, 0, 0, width(), height());
            KineticTheme.separator(g, 4, 24, width() - 8);
            g.scrollingTextCentered(title(), 320, 8, 536, 0xFFFFAA00, false);
            int text = KineticTheme.current().text();
            g.scrollingText(tr("targets"), LABEL_X, ROW1 + 6, CONTROL_X - LABEL_X - 4, text, false);
            g.scrollingText(tr(kind == Kind.SMELTER ? "multiplier" : "enchantments"), LABEL_X, ROW2 + 6, CONTROL_X - LABEL_X - 4, text, false);
            g.batchItems(() -> {
                for (int i = 0; i < shownIcons(); i++) {
                    int slotX = iconsX() + i * RuleRows.PITCH;
                    String target = targets.get(i);
                    KineticTheme.itemSlot(g, slotX, ROW1 - 1, RuleRows.SLOT, KineticTheme.hovering(x, y, slotX, ROW1 - 1, RuleRows.SLOT, RuleRows.SLOT));
                    var stacks = targetStacks(target);
                    if (!stacks.isEmpty())
                        KineticTheme.item(g, stacks.get((int) (System.currentTimeMillis() / 1000 % stacks.size())), slotX, ROW1 - 1, RuleRows.SLOT, 1.0F, false);
                    if (target.startsWith("#") || target.startsWith("@")) RuleRows.marker(g, target.substring(0, 1), slotX, ROW1 - 1);
                }
            });
            int namesX = iconsX() + shownIcons() * RuleRows.PITCH + 2;
            g.scrollingText(Component.literal(String.join(", ", targets.stream().map(VisualRuleEditors::targetName).toList())),
                    namesX, ROW1 + 6, 636 - namesX, KineticTheme.current().mutedText(), false);
            if (kind != Kind.SMELTER) {
                int valueX = CONTROL_X + 2 * BUTTON_W + 10;
                g.scrollingText(all ? tr("all").copy().withStyle(ChatFormatting.GOLD)
                        : Component.literal(String.join(", ", enchantments.stream().map(VisualRuleEditors::enchantmentName).toList())).withStyle(ChatFormatting.AQUA),
                        valueX, ROW2 + 6, 636 - valueX, text, false);
            }
        }
        @Override protected void renderTooltips(int mx, int my) {
            if (my < ROW1 - 1 || my >= ROW1 - 1 + RuleRows.SLOT || mx < iconsX()) return;
            int local = mx - iconsX();
            if (local % RuleRows.PITCH >= RuleRows.SLOT || local / RuleRows.PITCH >= shownIcons()) return;
            String id = targets.get(local / RuleRows.PITCH);
            showTooltip(Component.literal(targetName(id)).append("\n").append(Component.literal(id).withStyle(ChatFormatting.GRAY)));
        }
    }

    private static String selectionRule(KineticSelectors.ItemSelection selection) {
        if (selection.isTag()) return "#" + selection.value();
        if (selection.isMod()) return "@" + selection.value();
        return Objects.requireNonNull(KineticRegistries.items().id(selection.stack().getItem())).toString();
    }

    private static final class EnchantmentPicker extends KineticPage {
        final List<KineticSuggestion> choices;
        final LinkedHashSet<String> selected;
        final Consumer<List<String>> apply;
        String query = "";
        ChoiceViewport viewport;
        EnchantmentPicker(Component title, List<String> initial, Consumer<List<String>> apply) {
            super(title); selected = new LinkedHashSet<>(initial); this.apply = apply;
            // Disabled or removed enchantments may not be registered, but must remain individually selectable.
            var byId = new TreeMap<String, KineticSuggestion>();
            KineticSearch.enchantmentDictionary().forEach(choice -> byId.put(choice.value(), choice));
            initial.forEach(id -> byId.putIfAbsent(id, KineticSuggestion.of(id)));
            choices = List.copyOf(byId.values());
        }
        @Override protected void build(KineticUi ui) {
            // Same header as the effect picker: Back, search, title, Clear and Apply; the list fills the rest.
            viewport = new ChoiceViewport(4, 28, 632, 328, selected, id -> {
                if (!selected.remove(id)) selected.add(id);
            });
            ui.textField(48, 4, 260).value(query).placeholder(tr("search_hint")).tooltip(tr("search_hint")).onChange(value -> { query = value; filter(); }).build();
            ui.add(viewport); filter();
            ui.button(4, 4, 40).text(tr("cancel")).onClick(this::close).build();
            ui.button(528, 4, 64).text(tr("clear")).onClick(selected::clear).build();
            ui.button(596, 4, 40).text(tr("apply")).onClick(() -> { apply.accept(List.copyOf(selected)); close(); }).build();
        }
        void filter() { viewport.setChoices(choices.stream().filter(s -> KineticSearch.match(s.value() + " " + s.translation().getString(), query)).toList()); }
        @Override protected void renderBackground(KineticGraphics g, int x, int y, float pt) {
            KineticTheme.panel(g, 0, 0, width(), height());
            KineticTheme.separator(g, 4, 24, width() - 8);
            g.scrollingText(title(), 318, 8, 200, 0xFFFFAA00, false);
        }
    }

    private record EffectChoice(String id, MobEffect effect, Component name) { }

    /** Registry effects and legacy selections share one list; filtering never changes the selection draft. */
    private static final class EffectPicker extends KineticPage {
        final List<EffectChoice> choices;
        final LinkedHashSet<String> selected;
        final Consumer<List<String>> apply;
        String query = "";
        EffectRows viewport;
        EffectPicker(Component title, List<String> initial, Consumer<List<String>> apply) {
            super(title); this.apply = apply; selected = new LinkedHashSet<>(initial);
            var byId = new TreeMap<String, EffectChoice>();
            KineticRegistries.mobEffects().entries().forEach((id, effect) -> byId.put(id.toString(),
                    new EffectChoice(id.toString(), effect, KineticI18n.translatable(effect.getDescriptionId()))));
            initial.forEach(id -> byId.putIfAbsent(id, new EffectChoice(id, null, tr("unregistered"))));
            choices = List.copyOf(byId.values());
        }
        @Override protected void build(KineticUi ui) {
            viewport = new EffectRows(selected);
            ui.button(4, 4, 40).text(tr("cancel")).onClick(this::close).build();
            ui.textField(48, 4, 260).value(query).placeholder(tr("search_hint")).tooltip(tr("effects_hint"))
                    .onChange(value -> { query = value; filter(); }).build();
            ui.button(528, 4, 64).text(tr("clear")).onClick(selected::clear).build();
            ui.button(596, 4, 40).text(tr("apply")).onClick(() -> { apply.accept(List.copyOf(selected)); close(); }).build();
            ui.add(viewport); filter();
        }
        void filter() {
            viewport.setItems(choices.stream().filter(choice -> KineticSearch.match(choice.id() + " " + choice.name().getString(), query)).toList());
            viewport.setScrollOffset(0);
        }
        @Override protected void renderBackground(KineticGraphics g, int x, int y, float pt) {
            KineticTheme.panel(g, 0, 0, width(), height());
            KineticTheme.separator(g, 4, 24, width() - 8);
            g.scrollingText(title(), 318, 8, 200, KineticTheme.indicatorColor(KineticTheme.Indicator.WARNING), false);
        }
    }

    private static final class EffectRows extends KineticRowList<EffectChoice> {
        private final Set<String> selected;
        EffectRows(Set<String> selected) { super(4, 28, 632, 328, 20); this.selected = selected; }
        @Override protected void renderRowBackground(KineticGraphics g, int index, int x, int y, int width, int height, boolean hovered, boolean ignored) {
            KineticTheme.stateSurface(g, x, y + 1, width, height - 2,
                    index % 2 == 0 ? KineticTheme.Surface.PANEL_ALT : KineticTheme.Surface.PANEL, false, hovered, false);
            if (selected.contains(items().get(index).id())) KineticTheme.indicatorOutline(g, x, y + 1, width, height - 2, KineticTheme.Indicator.SUCCESS);
        }
        @Override protected void renderRow(KineticGraphics g, EffectChoice choice, int index, int x, int y, int width, int height, boolean hovered, boolean ignored) {
            if (choice.effect() != null) g.effectIcon(choice.effect(), x + 2, y + 1, 18);
            else g.text(Component.literal("?"), x + 8, y + 5, KineticTheme.current().mutedText(), false);
            g.scrollingText(Component.literal(choice.id()), x + 24, y + 5, 278, KineticTheme.current().text(), false);
            g.scrollingText(choice.name(), x + 308, y + 5, Math.max(1, width - 312), KineticTheme.current().translatedText(), false);
        }
        @Override protected boolean onRowClick(EffectChoice choice, int index, MouseInput input) {
            if (!input.isLeft()) return false;
            if (!selected.remove(choice.id())) selected.add(choice.id());
            playClickSound(); return true;
        }
        @Override protected Component rowTooltip(EffectChoice choice, int index) {
            return Component.literal(choice.id()).append("\n").append(choice.name()).append("\n").append(tr("effects_hint"));
        }
    }

    /** Uses the core scroll controller; only row content and multi-selection belong to the add-on. */
    private static final class ChoiceViewport extends KineticCustomControl {
        final KineticScrollController scroll = new KineticScrollController();
        final Set<String> selected;
        final Consumer<String> click;
        List<KineticSuggestion> choices = List.of();
        ChoiceViewport(int x, int y, int w, int h, Set<String> selected, Consumer<String> click) {
            super(x, y, w, h); this.selected = selected; this.click = click;
        }
        void setChoices(List<KineticSuggestion> choices) {
            this.choices = choices; scroll.update(Math.max(0, choices.size() * 16 - 2), controlHeight());
        }
        int index(double mx, double my) {
            if (!KineticTheme.hovering(mx, my, controlX(), controlY(), controlWidth() - 10, controlHeight())) return -1;
            double y = my - controlY() + scroll.smoothOffset(); int index = (int) (y / 16);
            return index < choices.size() && y % 16 < 14 ? index : -1;
        }
        @Override protected void render(KineticGraphics g, int mx, int my, float pt) {
            int hover = index(mx, my); setTooltip(hover < 0 ? null : Component.literal(choices.get(hover).value()));
            g.scissor(controlX(), controlY(), controlX() + controlWidth() - 10, controlY() + controlHeight());
            int first = Math.max(0, (int) scroll.smoothOffset() / 16);
            for (int i = first; i < choices.size(); i++) {
                int y = controlY() + i * 16 - (int) Math.round(scroll.smoothOffset()); if (y >= controlY() + controlHeight()) break;
                var row = choices.get(i); int width = controlWidth() - 10;
                g.fill(controlX(), y, controlX() + width, y + 14, (i & 1) == 0 ? KineticTheme.current().panel() : KineticTheme.current().panelAlt());
                if (selected.contains(row.value()) || hover == i) KineticTheme.stateOutline(g, controlX(), y, width, 14, selected.contains(row.value()), hover == i, false);
                Component label = Component.literal(row.value()).withStyle(ChatFormatting.WHITE);
                if (!row.translation().getString().isBlank()) label = label.copy().append("  ").append(row.translation().copy().withStyle(ChatFormatting.GOLD));
                g.scrollingText(label, controlX() + 3, y + 3, width - 6, 0xFFFFFFFF, false);
            }
            g.endScissor(); scroll.render(g, mx, my, controlX() + controlWidth() - 6, controlY(), 4, controlHeight(), 18);
        }
        @Override protected boolean onMouseClick(MouseInput input) {
            if (input.button() != MouseButton.LEFT) return false;
            if (scroll.beginDrag(input.x(), input.y(), input.button(), controlX() + controlWidth() - 6, controlY(), 4, controlHeight(), 18, 2)) return true;
            int index = index(input.x(), input.y()); if (index < 0) return false;
            playClickSound(); click.accept(choices.get(index).value()); return true;
        }
        @Override protected boolean onMouseScroll(ScrollInput input) { return scroll.scroll(input.deltaY(), 16); }
        @Override protected boolean onMouseDrag(MouseDragInput input) { return scroll.drag(input.y(), controlY(), controlHeight(), 18); }
        @Override protected boolean onMouseRelease(MouseInput input) { return scroll.release(input.button()); }
    }
}
