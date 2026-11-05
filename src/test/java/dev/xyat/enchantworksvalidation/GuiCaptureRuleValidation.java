package dev.xyat.enchantworksvalidation;

import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfig;
import dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfigGui;
import dev.xyat.enchantworks.anvil.config.VisualRuleEditors;
import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.gui.KineticGui;
import dev.xyat.kineticcore.api.client.gui.page.KineticPage;
import dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors;
import dev.xyat.kineticcore.api.client.gui.ui.KineticUi;
import dev.xyat.kineticcore.api.client.gui.widget.*;
import dev.xyat.kineticcore.api.client.gui.widget.list.KineticRowList;
import dev.xyat.kineticcore.api.client.search.KineticItemSearch;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import dev.xyat.kineticcore.api.registry.KineticRegistries;
import dev.xyat.kineticcore.api.runtime.KineticClientRuntime;
import dev.xyat.kineticcore.internal.client.gui.screen.KineticScreenHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/** Opt-in 1080p UI interaction checks, using local callbacks instead of config writers or permissions changes. */
public final class GuiCaptureRuleValidation {
    private static final List<String> EFFECTS = List.of("minecraft:speed", "removed:old_effect");
    private static final List<String> BLOCKS = List.of("minecraft:tripwire", "removed:old_block");
    private static final List<String> SMELTER = List.of("2x minecraft:iron_ore", "3x minecraft:gold_ore");
    private static final List<String> checks = new ArrayList<>();
    private static List<String> originalEffects, originalBlocks, applied;
    private static Path output;
    private static Screen parent, editor;
    private static int phase;
    private static long due, deadline;
    private static boolean installed, started, finished;

    private GuiCaptureRuleValidation() {}
    public static void install() {
        if (installed) return; installed = true;
        KineticClientEvents.onTick(KineticClientEvents.TickPhase.END, GuiCaptureRuleValidation::tick);
    }
    private static void tick() {
        if (finished) return;
        var mc = Minecraft.getInstance();
        try {
            if (!started) {
                if (mc.player == null || mc.level == null || mc.screen != null) return;
                output = Path.of(System.getProperty("enchantworks.ruleValidation.output", "D:/IDEAWork/EnchantWorks/build/ui-fixes-20261016/rule-validation"));
                require(output.isAbsolute(), "ruleValidation.output must be absolute"); Files.createDirectories(output);
                require(mc.getWindow().getWidth() == 1920 && mc.getWindow().getHeight() == 1080, "Launch original pack at 1920x1080");
                originalEffects = List.copyOf(AnvilEnchantmentConfig.leechEffectList);
                originalBlocks = List.copyOf(AnvilEnchantmentConfig.omniToolForceDropBlocks);
                KineticGui.open(new KineticPage(Component.literal("Rule validation parent")) { @Override protected void build(KineticUi ui) {} });
                parent = mc.screen; started = true; openEffects(); next(1); return;
            }
            if (System.currentTimeMillis() < due) return;
            if (System.currentTimeMillis() > deadline) throw new AssertionError("phase=" + phase + " timed out: screen=" + (mc.screen == null ? "none" : mc.screen.getClass().getSimpleName()) + " itemSearchReady=" + KineticItemSearch.ready());
            switch (phase) {
                case 1 -> {
                    require(KineticGui.currentPage() != null && KineticGui.currentPage().getClass().getSimpleName().equals("EffectPicker"), "Leech list did not directly open EffectPicker");
                    var choices = (List<?>) field(KineticGui.currentPage(), "choices");
                    Set<String> registered = new HashSet<>();
                    for (Object choice : choices) if (record(choice, "effect") != null) registered.add((String) record(choice, "id"));
                    require(registered.equals(KineticRegistries.mobEffects().ids().stream().map(Object::toString).collect(java.util.stream.Collectors.toSet())), "Effect picker omitted registered effects");
                    require(selectedEffects().equals(new LinkedHashSet<>(EFFECTS)), "Initial effect draft changed");
                    capture("effects-all"); search().setTextValue("minecraft:speed"); next(2);
                }
                case 2 -> {
                    var rows = effectRows(); require(rows.items().size() == 1 && record(rows.items().get(0), "id").equals("minecraft:speed"), "Effect ID filter mismatch");
                    click(rows.controlX() + 40, rows.rowTop(0) + 10); require(!selectedEffects().contains("minecraft:speed"), "Effect click did not deselect");
                    click(rows.controlX() + 40, rows.rowTop(0) + 10); require(selectedEffects().containsAll(EFFECTS), "Filter/toggle lost another selected effect");
                    capture("effects-filtered"); search().setTextValue("removed:old_effect"); next(3);
                }
                case 3 -> {
                    var rows = effectRows(); require(rows.items().size() == 1, "Unknown existing effect cannot be searched");
                    click(rows.controlX() + 40, rows.rowTop(0) + 10); require(!selectedEffects().contains("removed:old_effect"), "Unknown effect cannot be individually removed");
                    click(rows.controlX() + 40, rows.rowTop(0) + 10); search().setTextValue("");
                    clickButton("gui.enchantworks.rules.apply"); require(applied != null && new HashSet<>(applied).equals(new HashSet<>(EFFECTS)), "Apply lost filtered/unknown effects");
                    require(mc.screen == parent, "Effect Apply did not return to parent"); checks.add("all registered effect icons/IDs/names; filtering and multi-toggle preserve unknown selections; Apply returns draft");
                    openEffects(); search().setTextValue("minecraft:speed"); next(4);
                }
                case 4 -> {
                    var rows = effectRows(); click(rows.controlX() + 40, rows.rowTop(0) + 10); clickButton("gui.enchantworks.rules.cancel");
                    require(mc.screen == parent && applied == null, "Effect Back changed the caller draft"); checks.add("effect Back cancels unsaved selection");
                    applied = null; configEditor("omni_force_drop").open(Component.translatable("cfg.enchantworks.values.force_drop"), BLOCKS, result -> applied = List.copyOf(result)); next(5);
                }
                case 5 -> {
                    if (mc.screen == parent || !KineticItemSearch.ready()) { due = System.currentTimeMillis() + 100; return; }
                    require(mc.screen.getClass().getSimpleName().equals("ItemListEditorScreen"), "Force-drop list did not directly open core multiselect"); editor = mc.screen;
                    var adapter = (KineticSelectors.ItemListRuleAdapter) field(editor, "adapter");
                    ItemStack string = new ItemStack(Items.STRING);
                    require(adapter.acceptsItem(string) && !adapter.acceptsItem(new ItemStack(Items.DIAMOND)), "Force-drop picker must accept only block items");
                    require("minecraft:tripwire".equals(adapter.itemRule(string)) && adapter.matches("minecraft:tripwire", string) && !adapter.matches("minecraft:string", string), "Item/block ID distinction was lost");
                    require(rules().containsAll(BLOCKS), "Unknown initial block rule was lost"); capture("force-drop-all");
                    search().setTextValue("minecraft:string"); next(6);
                }
                case 6 -> {
                    var items = displayed(); require(items.size() == 1 && items.get(0).stack().is(Items.STRING), "Core search/predicate did not show block item string");
                    int x = (Integer) field(editor, "gridX"), y = (Integer) field(editor, "gridY");
                    click(x + 11, y + 11); require(!rules().contains("minecraft:tripwire"), "Grid click did not deselect block ID");
                    click(x + 11, y + 11); require(rules().contains("minecraft:tripwire") && !rules().contains("minecraft:string"), "Grid stored item ID instead of block ID");
                    activate("#minecraft:mineable/pickaxe"); next(7);
                }
                case 7 -> {
                    var adapter = (KineticSelectors.ItemListRuleAdapter) field(editor, "adapter");
                    require(!displayed().isEmpty() && displayed().stream().allMatch(item -> adapter.matches("#minecraft:mineable/pickaxe", item.stack())), "Block-only tag search used item tags");
                    clickControl((KineticControl) field(editor, "applyFilterBtn")); require(rules().contains("#minecraft:mineable/pickaxe"), "Block tag Apply did not add rule");
                    capture("force-drop-block-tag"); activate("@minecraft"); next(8);
                }
                case 8 -> {
                    var adapter = (KineticSelectors.ItemListRuleAdapter) field(editor, "adapter");
                    require(!displayed().isEmpty() && displayed().stream().allMatch(item -> adapter.matches("@minecraft", item.stack())), "Block namespace search mismatch");
                    clickControl((KineticControl) field(editor, "applyFilterBtn")); require(rules().contains("@minecraft"), "Mod Apply did not add rule");
                    var save = mc.screen.children().stream().filter(child -> child instanceof KineticButton button && button.text().getString().equals(Component.translatable("gui.kineticcore.hud_editor.save").getString())).map(KineticButton.class::cast).findFirst().orElseThrow();
                    clickControl(save);
                    require(applied != null && applied.containsAll(List.of("minecraft:tripwire", "removed:old_block", "#minecraft:mineable/pickaxe", "@minecraft")) && !applied.contains("minecraft:string"), "Save did not return the complete block-rule draft");
                    checks.add("direct core grid multi-select; item->block IDs; block-tag/mod filters; Save retains off-filter and unknown rules");
                    applied = null; VisualRuleEditors.smelter(Component.literal("Delete validation"), SMELTER, result -> applied = List.copyOf(result)); next(9);
                }
                case 9 -> {
                    var page = KineticGui.currentPage();
                    var list = (dev.xyat.kineticcore.api.client.gui.widget.list.KineticSelectionList) field(page, "list");
                    int y = list.controlY();
                    while (y < list.controlY() + list.controlHeight() && list.itemAt(list.controlX() + 24, y) != 1) y++;
                    require(y < list.controlY() + list.controlHeight(), "Second rule row not visible");
                    click(list.controlX() + 24, y + 2);
                    require(((Integer) field(page, "selected")) == 1, "Real rule click did not select second rule");
                    deleteKey();
                    require(field(page, "rules").equals(List.of(SMELTER.get(0))), "Delete removed a different rule");
                    clickButton("gui.enchantworks.rules.apply"); require(applied != null && applied.equals(List.of(SMELTER.get(0))) && mc.screen == parent, "Delete draft Apply/Back mismatch");
                    checks.add("Delete reuses selected rule removal and Apply returns the remaining draft"); finish(null);
                }
                default -> throw new AssertionError("Unexpected phase " + phase);
            }
        } catch (Throwable error) { finish(error); }
    }
    private static dev.xyat.kineticcore.api.config.client.KTConfigPage.ListEditor configEditor(String key) {
        var editor = KTConfigApi.find(AnvilEnchantmentConfigGui.SERVER_PAGE_ID).orElseThrow().listEditor(key);
        require(editor != null, "Missing visual listEditor: " + key); return editor;
    }
    private static void openEffects() {
        applied = null; configEditor("leech_effects").open(Component.translatable("cfg.enchantworks.values.leech_effects"), EFFECTS, result -> applied = List.copyOf(result));
    }
    private static void next(int next) { phase = next; due = System.currentTimeMillis() + 350; deadline = System.currentTimeMillis() + 30_000; }
    private static KineticTextField search() { return Minecraft.getInstance().screen.children().stream().filter(KineticTextField.class::isInstance).map(KineticTextField.class::cast).findFirst().orElseThrow(); }
    private static KineticRowList<?> effectRows() throws Exception { return (KineticRowList<?>) field(KineticGui.currentPage(), "viewport"); }
    @SuppressWarnings("unchecked") private static Set<String> selectedEffects() throws Exception { return (Set<String>) field(KineticGui.currentPage(), "selected"); }
    @SuppressWarnings("unchecked") private static Set<String> rules() throws Exception { return (Set<String>) field(editor, "rules"); }
    @SuppressWarnings("unchecked") private static List<KineticItemSearch.CachedItem> displayed() throws Exception { return (List<KineticItemSearch.CachedItem>) field(editor, "displayList"); }
    private static void activate(String rule) throws Exception {
        search().setTextValue("");
        Method method = editor.getClass().getSuperclass().getDeclaredMethod("applySelectedSuggestion", String.class); method.setAccessible(true); method.invoke(editor, rule);
    }
    private static Object field(Object target, String name) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(target); } catch (NoSuchFieldException ignored) { }
        throw new NoSuchFieldException(name);
    }
    private static Object record(Object row, String name) throws Exception { Method method = row.getClass().getDeclaredMethod(name); method.setAccessible(true); return method.invoke(row); }
    private static void clickButton(String key) {
        var button = Minecraft.getInstance().screen.children().stream()
                .filter(child -> child instanceof KineticButton value && value.text().getString().equals(Component.translatable(key).getString()))
                .map(KineticButton.class::cast).findFirst().orElseThrow();
        clickControl(button);
    }
    private static void clickControl(KineticControl control) { require(control.controlVisible() && control.isEnabled(), "Control is unavailable"); click(control.controlX() + control.controlWidth() / 2.0, control.controlY() + control.controlHeight() / 2.0); }
    private static void deleteKey() {
        //? if >=26.1 {
        /*Minecraft.getInstance().screen.keyPressed(new net.minecraft.client.input.KeyEvent(261, 0, 0));*/
        //?} else {
        Minecraft.getInstance().screen.keyPressed(261, 0, 0);
        //?}
    }
    private static void click(double x, double y) {
        Screen screen = Minecraft.getInstance().screen; var canvas = ((KineticScreenHost) screen).kineticRuntime().canvas();
        double sx = canvas.toScreenX(x), sy = canvas.toScreenY(y);
        //? if >=26.1 {
        /*var input = new net.minecraft.client.input.MouseButtonEvent(sx, sy, new net.minecraft.client.input.MouseButtonInfo(0, 0)); screen.mouseClicked(input, false); screen.mouseReleased(input);
        *///?} else {
        screen.mouseClicked(sx, sy, 0); screen.mouseReleased(sx, sy, 0);
        //?}
    }
    private static void capture(String name) throws Exception {
        var mc = Minecraft.getInstance(); Path path = output.resolve(name + ".png");
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(path);}
    }
    private static void finish(Throwable failure) {
        if (finished) return; finished = true;
        if (originalEffects != null && (!originalEffects.equals(AnvilEnchantmentConfig.leechEffectList) || !originalBlocks.equals(AnvilEnchantmentConfig.omniToolForceDropBlocks))) failure = new AssertionError("Fixture changed the user's configuration", failure);
        try {
            if (output != null) Files.writeString(output.resolve("result.txt"), (failure == null ? "PASS" : "FAIL " + failure) + "\n" + String.join("\n", checks) + "\n");
        } catch (Exception write) { org.slf4j.LoggerFactory.getLogger(GuiCaptureRuleValidation.class).error("Cannot write rule fixture result", write); }
        KineticGui.closeScreen();
        if (failure != null) org.slf4j.LoggerFactory.getLogger(GuiCaptureRuleValidation.class).error("ENCHANTWORKS_RULE_FAIL phase=" + phase, failure);
        if (Boolean.getBoolean("enchantworks.ruleValidation.stopAfter")) KineticClientRuntime.stopClient();
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
}
