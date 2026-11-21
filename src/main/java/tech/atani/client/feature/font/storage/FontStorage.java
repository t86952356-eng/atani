package tech.atani.client.feature.font.storage;

import de.florianmichael.rclasses.storage.Storage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import tech.atani.client.feature.font.FontEntry;
import tech.atani.client.feature.font.renderer.modern.ModernFontRenderer;
import tech.atani.client.feature.font.renderer.old.ClassicFontRenderer;
import tech.atani.client.feature.module.storage.ModuleStorage;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class FontStorage extends Storage<FontEntry> {

    private static FontStorage instance;

    // Lazy font loading: font entries are only *registered* during init() and are
    // actually created the first time they are requested through findFont().
    // Every ClassicFontRenderer allocates four 1024x1024 glyph atlases (~20MB),
    // so eagerly creating all ~80 entries needs ~1.6GB of heap and crashes the
    // client with an OutOfMemoryError while "Initializing game" on default JVM
    // settings. Creating entries on demand produces exactly the same renderers
    // and therefore exactly the same visuals, but only for fonts actually used.
    private final Map<String, Boolean> fontSpecs = new LinkedHashMap<String, Boolean>();

    @Override
    public void init() {
        this.register("Roboto", 14);
        this.register("Roboto", 15);
        this.register("Roboto", 16);
        this.register("Roboto", 17);
        this.register("Roboto", 18);
        this.register("Roboto", 19);
        this.register("Roboto", 20);
        this.register("Roboto", 21);
        this.register("Roboto", 24);
        this.register("Roboto", 31);
        this.register("Roboto Medium", 12);
        this.register("Roboto Medium", 15);
        this.register("Roboto Medium", 16);
        this.register("Roboto Medium", 17);
        this.register("Roboto Medium", 18);
        this.register("Roboto Medium", 19);
        this.register("Roboto Medium", 20);
        this.register("Roboto Medium", 21);
        this.register("Roboto Medium", 24);
        this.register("Tahoma", 16);
        this.register("ESP", 21);
        this.register("ESP", 80);
        this.register("Pangram Bold", 80);
        this.register("Pangram Bold", 20);
        this.register("Pangram Regular", 16);
        this.register("Pangram Regular", 17);
        this.register("Product Sans", 17);
        this.register("Product Sans", 18);
        this.register("Product Sans", 19);
        this.register("Product Sans", 20);
        this.register("Product Sans", 21);
        this.register("SFUI Medium", 16);
        this.register("Rubik", 18);
        this.register("Arial", 17);
        this.register("Arial", 18);
        this.register("Arial", 19);
        this.register("Arial", 20);
        this.register("Arial", 21);
        this.register("Arial", 24);
        this.register("ArialMT", 16);
        this.register("SF Regular", 17);
        this.register("SF Regular", 18);
        this.register("SF Regular", 19);
        this.register("SF Regular", 20);
        this.register("SF Regular", 21);
        this.register("SF Semibold", 17);
        this.register("SF Semibold", 18);
        this.register("SF Semibold", 19);
        this.register("SF Semibold", 20);
        this.register("SF Semibold", 21);
        this.register("Volte Semibold", 17);
        this.register("Volte Semibold", 18);
        this.register("Volte Semibold", 19);
        this.register("Volte Semibold", 20);
        this.register("Volte Semibold", 21);
        this.register("IcoMoon", 12);
        this.register("Consolas", 17);
        this.register("Consolas", 18);
        this.register("Consolas", 19);
        this.register("Tahoma", 17);
        this.register("Tahoma", 18);
        this.register("Tahoma", 19);
        this.register("Raleway Regular", 30);
        this.register("Raleway Regular", 35);
        this.register("Greycliff Medium", 18);
        this.register("Greycliff Medium", 19);
        this.register("Greycliff Medium", 21);
        this.register("Greycliff Bold", 24);
        this.register("Porter Bold", 23);
        this.register("Android 101", 100, true);
    }

    public FontRenderer findFont(String name, float size) {
        FontEntry foundFontEntry = this.getList().stream().filter(fontEntry -> fontEntry.getName().equalsIgnoreCase(name) && fontEntry.getSize() == size).findFirst().orElse(null);

        if (foundFontEntry == null)
        {
            final Boolean otf = this.fontSpecs.get(name.toLowerCase() + "@" + size);

            if (otf != null)
            {
                foundFontEntry = this.createFontEntry(name, size, otf);
                this.add(foundFontEntry);
            }
        }

        return ModuleStorage.getInstance().getByClass(tech.atani.client.feature.module.impl.option.FontRenderer.class).mode.getValue().equalsIgnoreCase("Classic") ? foundFontEntry.getClassicFontRenderer() : foundFontEntry.getModernFontRenderer();
    }

    private void register(String name, float size) {
        this.register(name, size, false);
    }

    private void register(String name, float size, boolean otf) {
        this.fontSpecs.put(name.toLowerCase() + "@" + size, otf);
    }

    private FontEntry createFontEntry(String name, float size) {
        return this.createFontEntry(name, size, false);
    }

    private FontEntry createFontEntry(String name, float size, boolean otf) {
        return new FontEntry(name, size, new ClassicFontRenderer(this.getFontFromFile(new ResourceLocation(String.format("atani/%s.%s", name, otf ? "otf" : "ttf")), size, Font.PLAIN), true), new ModernFontRenderer(this.getFontFromFile(new ResourceLocation(String.format("atani/%s.%s", name, otf ? "otf" : "ttf")), size, Font.PLAIN)));
    }

    private Font getFontFromFile(ResourceLocation loc, float fontSize, int fontType) {
        try {
            Font output = Font.createFont(fontType, Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream());
            output = output.deriveFont(fontSize);
            return output;
        } catch (Exception var5) {
            return null;
        }
    }

    public static FontStorage getInstance() {
        return instance;
    }

    public static void setInstance(FontStorage instance) {
        FontStorage.instance = instance;
    }
}
