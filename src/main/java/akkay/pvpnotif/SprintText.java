package akkay.pvpnotif;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SprintText {
    private static final char[] ENABLED_TEXT =
            "Sprinting: Enabled".toCharArray();
    private static final char[] DISABLED_TEXT =
            "Sprinting: Disabled".toCharArray();
    static boolean lastKnownSprintState;

    public static final HudElement INSTANCE = (drawContext, tickCounter) -> {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;
        if (!Pvpnotif.config.showSprintToggled) return;

        boolean sprintToggled = mc.options.toggleSprint().get();

        if (mc.screen == null) lastKnownSprintState = mc.options.keySprint.isDown();
        boolean displayAsEnabled = sprintToggled && lastKnownSprintState;
        char[] text = getText(displayAsEnabled);
        int[] palette = choosePalette(Pvpnotif.config.palette);

        int HUD_X = 4;
        int HUD_Y = 4;

        for (int i = 0; i < text.length; i++) {
            String charStr = String.valueOf(text[i]);
            int colorIndex = (i * palette.length) / text.length;
            int color = palette[colorIndex];

            drawContext.drawString(mc.font, charStr, HUD_X, HUD_Y, color, true);
            HUD_X += mc.font.width(charStr);
        }

        ItemStack item = new ItemStack(Items.FEATHER);
        drawContext.renderItem(item, 32, 32);
    };

    static char[] getText(boolean enabled) {
        return enabled ? ENABLED_TEXT : DISABLED_TEXT;
    }

    static int[] choosePalette(Config.Palette chosenPalette) {
        final int[] REGULAR = {0xFFFFFFFF, 0xFFFDD835};
        final int[] TRANS = {0xFF5BCFFA, 0xFFF5ABB9, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFF5ABB9, 0xFF5BCFFA};
        final int[] LESBIAN = {0xFFD72C00, 0xFFF07527, 0xFFFF9A56, 0xFFFFFFFF, 0xFFD162A4, 0xFFB75592, 0xFFA50162};
        final int[] GAY = {0xFF078D70, 0xFF26CEAA, 0xFF98E8C1, 0xFFFFFFFF, 0xFF7BADE2, 0xFF5049CC, 0xFF3D1A78};

        return switch (chosenPalette) {
            case Trans -> TRANS;
            case Lesbian -> LESBIAN;
            case Gay -> GAY;
            default -> REGULAR;
        };
    }

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("pvpnotif", "sprinttext"), // unique id
                INSTANCE
        );
    }
}
