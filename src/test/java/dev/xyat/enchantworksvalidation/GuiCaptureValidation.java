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
    private static final String[] NAMES={"anvil-config","sixth-sense-config","native-anvil"};
    private static final String[] PAGES={"KTConfigScreen","KTConfigScreen","AnvilScreen"};
    private static final long OPEN_TIMEOUT=20000;
    private static boolean installed,started,screenshot,finished,originalFullscreen;
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
                nextPhase();
                return;
            }
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
                if(now>=due) { capture("start");screenshot=true;due=now+(phase==4?3400:550); }
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
        var current=Minecraft.getInstance().screen;
        return current==null?"none":current.getClass().getSimpleName();
    }
    private static void nextPhase() {
        if(stressOriginal!=null){Language.inject(stressOriginal);stressOriginal=null;}
        phase++;page=-1;
        if(phase>=5){finish();return;}
        var mc=Minecraft.getInstance();
        mc.setScreen(null);
        String lang=phase==2 || phase==3?"zh_cn":"en_us";
        mc.getLanguageManager().setSelected(lang);
        mc.options.languageCode=lang;
        int width=phase==1 || phase==3?1920:854,height=phase==1 || phase==3?1080:480;
        mc.getWindow().setWindowed(width,height);mc.resizeDisplay();
        reload=mc.reloadResourcePacks();
        LOG.info("ENCHANTWORKS_GUI_PHASE phase={} language={} requested={}x{} autoScale=true",phase,lang,width,height);
    }
    private static void nextPage() throws Exception {
        page++;
        String selectedPages=System.getProperty("enchantworks.guiValidation.pages", "");
        while(page<NAMES.length && !selectedPages.isBlank() && !List.of(selectedPages.split(",")).contains(String.valueOf(page)))page++;
        if(page>=NAMES.length){nextPhase();return;}
        screenshot=false;
        long now=System.currentTimeMillis();due=now+1000;deadline=now+OPEN_TIMEOUT;
        Minecraft.getInstance().setScreen(null);
        switch(page) {
            case 0 -> KTConfigApi.openPage(dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfigGui.SERVER_PAGE_ID);
            case 1 -> KTConfigApi.openPage(dev.xyat.enchantworks.anvil.config.AnvilEnchantmentConfigGui.CLIENT_PAGE_ID);
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
