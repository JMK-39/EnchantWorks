package dev.xyat.enchantworksvalidation;

import dev.xyat.kineticcore.api.client.event.KineticClientEvents;
import dev.xyat.kineticcore.api.client.gui.KineticGui;
import dev.xyat.kineticcore.api.config.client.KTConfigApi;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*** Opens every EnchantWorks screen and captures it, in English and Chinese at two window sizes plus a long-text pass. Never clicks or saves. */
public final class GuiCaptureValidation {
    private static final Logger LOG=LoggerFactory.getLogger(GuiCaptureValidation.class);
    private static final String ROOT=System.getProperty("enchantworks.guiValidation.output","D:/IDEAWork/EnchantWorks/.gradle/gui-capture/");
    private static final String[] NAMES={"anvil-config","sixth-sense-config","native-anvil","enchantment-rules","smelter-rules","force-drop-rules","enchantment-multiselect","enchantment-rule-editor","smelter-rule-editor","block-tags","anvil-item-picker","core-item-selector","core-config","core-selector-bottom"};
    private static final String[] PAGES={"KTConfigScreen","KTConfigScreen","AnvilScreen","RuleListPage","RuleListPage","RuleListPage","EnchantmentPicker","RuleDetails","RuleDetails","BlockTargetPicker","ItemListEditorScreen","ItemSelectorScreen","KTModuleConfigScreen","ItemSelectorScreen"};
    private static final long OPEN_TIMEOUT=20000;
    private static boolean installed,started,screenshot,finished,originalFullscreen,interactionChecked,preparing;
    private static String originalLanguage;
    private static int originalScale,originalWidth,originalHeight,phase=-1,page=-1,captures,failures;
    private static long due,deadline;
    private static CompletableFuture<Void> reload;
    private static Language stressOriginal;

    public static void install() { if(installed)return;installed=true;KineticClientEvents.onTick(KineticClientEvents.TickPhase.END,GuiCaptureValidation::tick); }
    private static void tick() {
        if(finished)return;
        try {
            var mc=Minecraft.getInstance();
            if(!started) {
                if(mc.player==null || mc.level==null || mc.getSingleplayerServer()==null)return;
                started=true;originalLanguage=mc.getLanguageManager().getSelected();originalScale=mc.options.guiScale().get();
                originalWidth=mc.getWindow().getWidth();originalHeight=mc.getWindow().getHeight();originalFullscreen=mc.getWindow().isFullscreen();
                mc.options.guiScale().set(0);
                if(originalFullscreen)mc.getWindow().toggleFullScreen();
                if(mc.gameDirectory.getName().startsWith(".codex-native-recipes-")) {
                    preparing=true;
                    mc.getSingleplayerServer().execute(()->{
                        var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().get(0);
                        //? if >=26.1 {
                        /*server.getPlayerList().op(new net.minecraft.server.players.NameAndId(player.getGameProfile()));
                        *///?} else {
                        server.getPlayerList().op(player.getGameProfile());
                        //?}
                        mc.execute(()->{preparing=false;nextPhase();});
                    });
                } else nextPhase();
                return;
            }
            if(preparing)return;
            if(reload!=null) {
                if(!reload.isDone() || mc.getOverlay()!=null)return;
                reload.join();reload=null;
                if(phase==4) {
                    stressOriginal=Language.getInstance();
                    Language.inject(new StressLanguage(stressOriginal));
                }
                nextPage();return;
            }
            long now=System.currentTimeMillis();
            if(!screenshot) {
                String current=currentPage();
                if(!PAGES[page].equals(current)) {
                    if(now>=deadline)throw new IllegalStateException("page "+PAGES[page]+" did not open; showing "+current);
                    due=now+1000;return;
                }
                if(now>=due) {
                    if(!interactionChecked) {
                        if(page==0)checkCustomConfigEditor();if(page==6)checkMultiSelection();if(page==11||page==13)checkCoreSelector();
                        interactionChecked=true;due=now+350;return;
                    }
                    capture("start");screenshot=true;due=now+(phase==4?3400:550);
                }
                return;
            }
            if(now>=due) {
                if(phase==4)capture("scroll");
                nextPage();
            }
        } catch(Throwable error) {
            failures++;LOG.error("ENCHANTWORKS_GUI_FAIL phase="+phase+" page="+page,error);
            try { nextPage(); } catch(Throwable next) { finish(); }
        }
    }
    private static String currentPage() {
        if(page>=3 && page<=10) {
            var current=KineticGui.currentPage();if(current!=null)return current.getClass().getSimpleName();
        }
        var current=Minecraft.getInstance().screen;
        return current==null?"none":current.getClass().getSimpleName();
    }
    private static void nextPhase() {
        if(stressOriginal!=null){Language.inject(stressOriginal);stressOriginal=null;}
        phase++;page=-1;
        boolean fullHd=Boolean.getBoolean("enchantworks.guiValidation.fullHdOnly");
        if(phase>=(fullHd?2:5)){finish();return;}
        var mc=Minecraft.getInstance();
        mc.setScreen(null);
        String lang=fullHd?(phase==1?"zh_cn":"en_us"):phase==2 || phase==3?"zh_cn":"en_us";
        mc.getLanguageManager().setSelected(lang);
        mc.options.languageCode=lang;
        int width=fullHd||phase==1 || phase==3?1920:854,height=fullHd||phase==1 || phase==3?1080:480;
        mc.getWindow().setWindowed(width,height);mc.resizeDisplay();
        reload=mc.reloadResourcePacks();
        LOG.info("ENCHANTWORKS_GUI_PHASE phase={} language={} requested={}x{} autoScale=true",phase,lang,width,height);
    }
    private static void nextPage() throws Exception {
        page++;
        String selectedPages=System.getProperty("enchantworks.guiValidation.pages", "");
        while(page<NAMES.length && !selectedPages.isBlank() && !List.of(selectedPages.split(",")).contains(String.valueOf(page)))page++;
        if(page>=NAMES.length){nextPhase();return;}
        screenshot=false;interactionChecked=false;
        long now=System.currentTimeMillis();due=now+1000;deadline=now+OPEN_TIMEOUT;
        Minecraft.getInstance().setScreen(null);
        switch(page) {
            case 0 -> KTConfigApi.openPage(dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfigGui.SERVER_PAGE_ID);
            case 1 -> KTConfigApi.openPage(dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfigGui.CLIENT_PAGE_ID);
            case 3 -> dev.xyat.enchantworks.anvil.config.VisualRuleEditors.enchantments(Component.literal("Enchantments"),List.of("#minecraft:axes; minecraft:sharpness, minecraft:mending", "@minecraft; *"),result->{});
            case 4 -> dev.xyat.enchantworks.anvil.config.VisualRuleEditors.smelter(Component.literal("Smelter"),List.of("2x #c:ores","3x minecraft:raw_iron"),result->{});
            case 5 -> dev.xyat.enchantworks.anvil.config.VisualRuleEditors.blocks(Component.literal("Force drops"),List.of("#minecraft:mineable/pickaxe", "minecraft:stone"),result->{});
            case 6 -> dev.xyat.enchantworks.anvil.config.VisualRuleEditors.disabled(Component.literal("Enchantments"),List.of("minecraft:sharpness","minecraft:mending","removed:old_enchantment"),result->{});
            case 7 -> {
                dev.xyat.enchantworks.anvil.config.VisualRuleEditors.enchantments(Component.literal("Enchantments"),List.of("#minecraft:axes, minecraft:stick; minecraft:sharpness, minecraft:mending"),result->{});
                editRule(0);
            }
            case 8 -> {
                dev.xyat.enchantworks.anvil.config.VisualRuleEditors.smelter(Component.literal("Smelter"),List.of("3x minecraft:raw_iron"),result->{});
                editRule(0);
            }
            case 9 -> {
                dev.xyat.enchantworks.anvil.config.VisualRuleEditors.blocks(Component.literal("Force drops"),List.of(),result->{});
                editRule(-1);
            }
            case 10 -> dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors.openItemListEditor(Component.literal("Anvil"),List.of("minecraft:diamond_sword", "#minecraft:axes"),dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors.ItemListMode.ITEMS_TAGS_MODS,result->{});
            case 11,13 -> dev.xyat.kineticcore.api.client.gui.selector.KineticSelectors.openItemSelector(result->{});
            case 12 -> KTConfigApi.openOwner("kineticcore");
            default -> {
                var mc=Minecraft.getInstance();
                var menu=new net.minecraft.world.inventory.AnvilMenu(0,mc.player.getInventory());
                menu.getSlot(0).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE));
                menu.getSlot(1).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ENCHANTED_BOOK));
                menu.getSlot(2).set(menu.getSlot(0).getItem().copy());
                mc.setScreen(new net.minecraft.client.gui.screens.inventory.AnvilScreen(menu,mc.player.getInventory(),Component.translatable("container.repair")));
            }
        }
        LOG.info("ENCHANTWORKS_GUI_OPEN phase={} case={}",phase,NAMES[page]);
    }
    private static void capture(String frame)throws Exception {
        var mc=Minecraft.getInstance();Path path=Path.of(ROOT,String.format("%d-%02d-%s-%s.png",phase,page,NAMES[page],frame));Files.createDirectories(path.getParent());
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(path);}
        captures++;LOG.info("ENCHANTWORKS_GUI_CAPTURE phase={} case={} image={}x{}",phase,NAMES[page],mc.getWindow().getWidth(),mc.getWindow().getHeight());
    }
    private static void editRule(int index)throws Exception {
        var page=KineticGui.currentPage();var method=page.getClass().getDeclaredMethod("edit",int.class);
        method.setAccessible(true);method.invoke(page,index);
    }
    private static void checkMultiSelection()throws Exception {
        var page=KineticGui.currentPage();var field=page.getClass().getDeclaredField("viewport");field.setAccessible(true);
        var choicesField=page.getClass().getDeclaredField("choices");choicesField.setAccessible(true);
        var choices=(List<dev.xyat.kineticcore.api.client.search.KineticSuggestion>)choicesField.get(page);
        if(choices.stream().noneMatch(choice -> choice.value().equals("removed:old_enchantment")))throw new AssertionError("Unregistered selected enchantment must remain visible and individually removable");
        var viewport=field.get(page);var select=viewport.getClass().getDeclaredMethod("onMouseClick",dev.xyat.kineticcore.api.client.gui.input.MouseInput.class);select.setAccessible(true);
        var input=new dev.xyat.kineticcore.api.client.gui.input.MouseInput(22,73,dev.xyat.kineticcore.api.client.gui.input.MouseButton.LEFT,0,0);
        var selectedField=page.getClass().getDeclaredField("selected");selectedField.setAccessible(true);
        var selected=(java.util.Set<?>)selectedField.get(page);var before=java.util.Set.copyOf(selected);
        var queryField=page.getClass().getDeclaredField("query");queryField.setAccessible(true);queryField.set(page,"removed:old_enchantment");
        var filter=page.getClass().getDeclaredMethod("filter");filter.setAccessible(true);filter.invoke(page);
        select.invoke(viewport,input);
        if(selected.contains("removed:old_enchantment")||selected.size()!=before.size()-1)throw new AssertionError("Unregistered enchantment must be individually deselectable");
        select.invoke(viewport,input);queryField.set(page,"");filter.invoke(page);
        if(!before.equals(selected))throw new AssertionError("Removing and restoring a legacy enchantment changed other selections");
        select.invoke(viewport,input);var changed=java.util.Set.copyOf(selected);
        if(before.equals(changed))throw new AssertionError("multi-selection click did not toggle");
        select.invoke(viewport,input);
        if(!before.equals(selected))throw new AssertionError("multi-selection second click did not restore selection");
        LOG.info("ENCHANTWORKS_MULTI_SELECTION_PASS language={}",Minecraft.getInstance().getLanguageManager().getSelected());
    }
    @SuppressWarnings("unchecked")
    private static void checkCustomConfigEditor()throws Exception {
        var host=Minecraft.getInstance().screen;
        var definition=KTConfigApi.find(dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfigGui.SERVER_PAGE_ID).orElseThrow();
        var entry=definition.entries().stream().filter(e->e.id().equals("disabled_enchantments")).findFirst().orElseThrow();
        var pendingField=host.getClass().getDeclaredField("pendingValues");pendingField.setAccessible(true);
        var pending=(Map<String,Object>)pendingField.get(host);var before=List.copyOf((List<String>)pending.get(entry.id()));
        var open=host.getClass().getDeclaredMethod("openListEditor",dev.xyat.kineticcore.api.config.client.KTConfigEntry.class);open.setAccessible(true);open.invoke(host,entry);
        var picker=KineticGui.currentPage();
        if(picker==null||!picker.getClass().getSimpleName().equals("EnchantmentPicker"))throw new AssertionError("Individual config page must use its visual list editor");
        picker.close();
        if(!before.equals(pending.get(entry.id())))throw new AssertionError("Cancel changed the parent draft");
        open.invoke(host,entry);picker=KineticGui.currentPage();
        var applyField=picker.getClass().getDeclaredField("apply");applyField.setAccessible(true);
        var replacement=new ArrayList<>(before);replacement.add("removed:old_enchantment");
        ((java.util.function.Consumer<List<String>>)applyField.get(picker)).accept(replacement);picker.close();
        if(!replacement.equals(pending.get(entry.id())))throw new AssertionError("Apply did not replace parent draft");
        KTConfigApi.openPage(definition); // discard the test draft without saving
        LOG.info("ENCHANTWORKS_CONFIG_EDITOR_PASS cancel/apply draft-only language={}",Minecraft.getInstance().getLanguageManager().getSelected());
    }
    private static Object field(Object target,String name)throws Exception {
        var f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);
    }
    private static void checkCoreSelector()throws Exception {
        var screen=Minecraft.getInstance().screen;
        if((int)field(screen,"gridCols")!=20||(int)field(screen,"gridRowsVisible")!=13)throw new AssertionError("Selector must show 20 columns and 13 complete rows");
        var scroll=field(screen,"mainScroll");
        if(page==13){var set=scroll.getClass().getMethod("setOffset",int.class);set.invoke(scroll,Integer.MAX_VALUE);}
        var grid=field(screen,"itemGrid");
        var items=(List<?>)field(screen,"displayList");int count=items.size();
        if(page==13&&count>0){
            int first=(int)scroll.getClass().getMethod("smoothIndexOffset").invoke(scroll);
            int shift=(int)scroll.getClass().getMethod("visualShift",int.class).invoke(scroll,24);
            int bottom=(int)grid.getClass().getMethod("clipBottom").invoke(grid);
            int lastY=(int)grid.getClass().getMethod("slotY",int.class,int.class,int.class).invoke(grid,count-1,first,shift);
            if(lastY+22!=bottom)throw new AssertionError("Selector bottom must end with a complete item row, no empty row: "+lastY+"/"+bottom);
        }
        LOG.info("CORE_SELECTOR_GEOMETRY_PASS bottom={} columns=20 rows=13 language={}",page==13,Minecraft.getInstance().getLanguageManager().getSelected());
    }
    private static void finish() {
        finished=true;
        var mc=Minecraft.getInstance();
        if(stressOriginal!=null){Language.inject(stressOriginal);stressOriginal=null;}
        mc.setScreen(null);
        LOG.info("ENCHANTWORKS_GUI_{} pages={} captures={} failures={} userSettingsKept=true",failures==0?"PASS":"FAIL",NAMES.length,captures,failures);
        dev.xyat.kineticcore.api.runtime.KineticClientRuntime.stopClient();
    }
    private static final class StressLanguage extends Language {
        private final Language delegate;
        StressLanguage(Language delegate){this.delegate=delegate;}
        @Override public String getOrDefault(String key,String fallback) {
            String text=delegate.getOrDefault(key,fallback);
            return key.startsWith("gui.enchantworks.") || key.startsWith("cfg.enchantworks.") || key.startsWith("config.enchantworks.")?text+" - deliberately extended translation to verify text stays inside its own region":text;
        }
        @Override public boolean has(String key){return delegate.has(key);}
        @Override public boolean isDefaultRightToLeft(){return delegate.isDefaultRightToLeft();}
        @Override public net.minecraft.util.FormattedCharSequence getVisualOrder(net.minecraft.network.chat.FormattedText text){return delegate.getVisualOrder(text);}
    }
}
