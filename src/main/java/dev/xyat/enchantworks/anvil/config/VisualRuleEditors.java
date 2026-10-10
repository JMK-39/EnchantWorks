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
import dev.xyat.kineticcore.api.client.search.KineticSearch;
import dev.xyat.kineticcore.api.client.search.KineticSuggestion;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.resource.KineticResourceIds;
import dev.xyat.kineticcore.api.text.KineticI18n;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;

import java.util.*;
import java.util.function.Consumer;

/** Selection-only editors. Nothing is written until the parent config page is saved. */
public final class VisualRuleEditors {
    enum Kind { ENCHANTMENTS, SMELTER, BLOCKS }
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
        KineticGui.openChild(new RuleListPage(title, initial, apply, Kind.BLOCKS));
    }
    public static void disabled(Component title, List<String> initial, Consumer<List<String>> apply) {
        KineticGui.openChild(new EnchantmentPicker(title, initial, apply));
    }

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
            if (kind == Kind.BLOCKS) return targetName(raw);
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
        dev.xyat.kineticcore.api.client.gui.widget.list.KineticSelectionList list;
        RuleListPage(Component title, List<String> initial, Consumer<List<String>> apply, Kind kind) {
            super(title); rules = new ArrayList<>(initial); this.apply = apply; this.kind = kind;
        }
        @Override protected void build(KineticUi ui) {
            var rows = rules.stream().map(rule -> new SelectionItem(Component.literal(summary(rule, kind)),
                    null, Component.literal(rule), true, false)).toList();
            list = ui.selectionList(16, 48, 608, 258, rows).textRows().selected(selected).scrollOffset(offset)
                    .onSelect(index -> selected = index).build();
            ui.button(16, 316, 88).text(tr("add")).onClick(() -> edit(-1)).build();
            ui.button(108, 316, 88).text(tr("edit")).onClick(() -> { if (selected >= 0) edit(selected); }).build();
            ui.button(200, 316, 88).text(tr("remove")).onClick(() -> {
                if (selected >= 0 && selected < rules.size()) { offset = list.scrollOffset(); rules.remove(selected); selected = -1; rebuild(); }
            }).build();
            ui.button(436, 316, 88).text(tr("cancel")).onClick(this::close).build();
            ui.button(528, 316, 96).text(tr("apply")).onClick(() -> { apply.accept(List.copyOf(rules)); close(); }).build();
        }
        void edit(int index) {
            offset = list.scrollOffset();
            Consumer<String> accept = value -> {
                if (index < 0) rules.add(value); else rules.set(index, value);
                selected = index < 0 ? rules.size() - 1 : index; rebuild();
            };
            if (kind == Kind.BLOCKS) openChild(new BlockTargetPicker(accept));
            else openChild(new RuleDetails(title(), index < 0 ? null : rules.get(index), kind, accept));
        }
        @Override protected void renderBackground(KineticGraphics g, int x, int y, float pt) {
            KineticTheme.panel(g, 12, 12, 616, 332);
            g.scrollingTextCentered(title(), 320, 24, 592, 0xFFFFAA00, false);
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
        @Override protected void build(KineticUi ui) {
            ui.button(28, 60, 176).text(tr("choose_targets", targets.size())).tooltip(tr("targets_hint"))
                    .onClick(() -> {
                        if (kind == Kind.SMELTER) KineticSelectors.openItemSelector(selection -> {
                            targets = new ArrayList<>(List.of(selectionRule(selection))); rebuild();
                        });
                        else KineticSelectors.openItemListEditor(tr("targets"), targets, KineticSelectors.ItemListMode.ITEMS_TAGS_MODS,
                                result -> { targets = new ArrayList<>(result); rebuild(); });
                    }).build();
            if (kind == Kind.SMELTER) {
                var field=ui.numberField(184, 126, 60, NumberType.INT).label(tr("multiplier")).range(0, Integer.MAX_VALUE)
                        .tooltip(tr("multiplier_hint")).build();
                field.setTextValue(multiplier);field.onTextChange(value -> multiplier=value);
            } else {
                ui.toggle(28, 124, 176).value(all).labels(tr("all"), tr("specified"))
                        .tooltip(tr("all_hint")).onChange(value -> { all = value; rebuild(); }).build();
                ui.button(28, 164, 176).text(tr("choose_enchantments", enchantments.size())).enabled(!all)
                        .tooltip(tr("enchantments_hint")).onClick(() -> openChild(new EnchantmentPicker(tr("enchantments"), enchantments,
                                result -> { enchantments = new ArrayList<>(result); rebuild(); }))).build();
            }
            ui.button(420, 316, 88).text(tr("cancel")).onClick(this::close).build();
            ui.button(512, 316, 100).text(tr("apply")).onClick(() -> {
                if (targets.isEmpty() || (kind == Kind.ENCHANTMENTS && !all && enchantments.isEmpty())) return;
                int number;
                try { number = Integer.parseInt(multiplier); if (number < 0) return; } catch (NumberFormatException invalid) { return; }
                var draft = new VisualRuleDraft(targets, enchantments, all, number);
                apply.accept(kind == Kind.SMELTER ? draft.smelterRule() : draft.enchantmentRule()); close();
            }).enabled(!targets.isEmpty() && (kind != Kind.ENCHANTMENTS || all || !enchantments.isEmpty()))
                    .tooltip(tr("targets_hint")).build();
        }
        @Override protected void renderBackground(KineticGraphics g, int x, int y, float pt) {
            KineticTheme.panel(g, 12, 12, 616, 332);
            g.scrollingTextCentered(title(), 320, 24, 592, 0xFFFFAA00, false);
            g.scrollingText(Component.literal(String.join(", ", targets.stream().map(VisualRuleEditors::targetName).toList())),
                    218, 64, 394, 0xFFFFFFFF, false);
            if (kind == Kind.SMELTER) g.scrollingText(tr("multiplier"), 28, 130, 152, 0xFFFFFFFF, false);
            else g.scrollingText(Component.literal(all ? tr("all").getString()
                    : String.join(", ", enchantments.stream().map(VisualRuleEditors::enchantmentName).toList())),
                    218, 168, 394, 0xFFFFFFFF, false);
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
            viewport = new ChoiceViewport(16, 68, 608, 240, selected, id -> {
                if (!selected.remove(id)) selected.add(id);
            });
            ui.textField(16, 44, 608).value(query).tooltip(tr("search_hint")).onChange(value -> { query = value; filter(); }).build();
            ui.add(viewport); filter();
            ui.button(16, 316, 88).text(tr("clear")).onClick(selected::clear).build();
            ui.button(436, 316, 88).text(tr("cancel")).onClick(this::close).build();
            ui.button(528, 316, 96).text(tr("apply")).onClick(() -> { apply.accept(List.copyOf(selected)); close(); }).build();
        }
        void filter() { viewport.setChoices(choices.stream().filter(s -> KineticSearch.match(s.value() + " " + s.translation().getString(), query)).toList()); }
        @Override protected void renderBackground(KineticGraphics g, int x, int y, float pt) {
            KineticTheme.panel(g, 12, 12, 616, 332);
            g.scrollingTextCentered(title(), 320, 24, 592, 0xFFFFAA00, false);
        }
    }

    private static final class BlockTargetPicker extends KineticPage {
        final Consumer<String> accept;
        int mode;
        String query = "";
        String pendingSelection;
        ChoiceViewport viewport;
        BlockTargetPicker(Consumer<String> accept) { super(tr("blocks")); this.accept = accept; }
        @Override protected void build(KineticUi ui) {
            ui.button(16, 44, 100).text(tr("block_item")).onClick(() -> KineticSelectors.openItemSelectorWithOptions(
                    KineticSelectors.ItemSelectorOptions.itemsOnly(null, List.of(), stack -> stack.getItem() instanceof BlockItem),
                    selection -> {
                        var item = (BlockItem) selection.stack().getItem();
                        pendingSelection = Objects.requireNonNull(KineticRegistries.blocks().id(item.getBlock())).toString();
                    })).build();
            ui.button(120, 44, 100).text(tr("block_tags")).onClick(() -> { mode = 0; filter(); }).build();
            ui.button(224, 44, 100).text(tr("mods")).onClick(() -> { mode = 1; filter(); }).build();
            viewport = new ChoiceViewport(16, 96, 608, 212, Set.of(), value -> { accept.accept(value); close(); });
            ui.textField(16, 70, 608).value(query).tooltip(tr("search_hint")).onChange(value -> { query = value; filter(); }).build();
            ui.add(viewport); filter();
            ui.button(528, 316, 96).text(tr("cancel")).onClick(this::close).build();
        }
        void filter() {
            var ids = mode == 0 ? KineticRegistries.blocks().tagIds().stream()
                    .map(tag -> "#" + tag).distinct().sorted().toList()
                    : KineticRegistries.blocks().entries().keySet().stream().map(id -> "@" + id.getNamespace()).distinct().sorted().toList();
            viewport.setChoices(ids.stream().filter(id -> KineticSearch.match(id, query)).map(KineticSuggestion::of).toList());
        }
        @Override protected void onTick() {
            if (pendingSelection != null && isOpen()) {
                String value = pendingSelection; pendingSelection = null; accept.accept(value); close();
            }
        }
        @Override protected void renderBackground(KineticGraphics g, int x, int y, float pt) {
            KineticTheme.panel(g, 12, 12, 616, 332);
            g.scrollingTextCentered(title(), 320, 24, 592, 0xFFFFAA00, false);
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
