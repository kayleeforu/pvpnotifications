package akkay.pvpnotif;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

public class ModMenu implements ModMenuApi {
    public final Config config;

    public ModMenu() {
        this.config = Pvpnotif.config;
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("PvP Notifications Config"));
            ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

            builder.getOrCreateCategory(Component.literal("Effects"))
                    .addEntry(entryBuilder.startIntSlider(Component.literal("Duration Threshold"),
                                    config.threshold, 1, 50)
                            .setTooltip(Component.literal("The time moment when the notification will pop-up."))
                            .setDefaultValue(config.threshold)
                            .setSaveConsumer(newValue -> {
                                config.threshold = newValue;
                                try {
                                    updateIntField(Config.class.getDeclaredField("threshold"), newValue);
                                } catch (Exception ignored) {
                                }
                            })
                            .build());

            builder.getOrCreateCategory(Component.literal("Miscellaneous"))
                    .addEntry(entryBuilder.startBooleanToggle(Component.literal("Sprinting Status"),
                                    config.showSprintToggled)
                            .setTooltip(Component.literal("If enabled, you will see whether sprint is toggled or not." +
                                    "\nNote: You need to have Sprint set to Toggle in Controls."))
                            .setDefaultValue(config.showSprintToggled)
                            .setSaveConsumer(newValue -> {
                                config.showSprintToggled = newValue;
                                try {
                                    updateBooleanField(Config.class.getDeclaredField("showSprintToggled"), newValue);
                                } catch (Exception ignored) {
                                }
                            })
                            .build());
            Config.Palette palette = config.palette;
            if (config.showSprintToggled) {
                builder.getOrCreateCategory(Component.literal("Miscellaneous"))
                        .addEntry(entryBuilder.startEnumSelector(Component.literal("Text Palette"), Config.Palette.class, palette)
                                .setTooltip(Component.literal("Text Palette."))
                                .setSaveConsumer(newValue -> {
                                    try {
                                        updateEnumField(Config.class.getDeclaredField("palette"), newValue);
                                    } catch (Exception ignored) {
                                    }
                                })
                                .build());
            }

            return builder.build();
        };
    }

    public void updateIntField(Field fieldToChange, int newValue) throws IllegalAccessException {
        // Load Jankson
        Jankson jankson = Jankson.builder().build();
        Config config = Pvpnotif.config;

        // Set the new value via reflection
        fieldToChange.setAccessible(true);
        fieldToChange.setInt(config, newValue);

        // Convert the config object to JSON
        JsonElement jsonElement = jankson.toJson(config);

        // Write it to file
        try (FileWriter fw = new FileWriter(Pvpnotif.configFile)) {
            fw.write(jsonElement.toJson(true, true)); // pretty-print
        } catch (IOException ignored) {
        }
    }

    public void updateBooleanField(Field fieldToChange, boolean newValue) throws IllegalAccessException {
        Jankson jankson = Jankson.builder().build();
        Config config = Pvpnotif.config;

        fieldToChange.setAccessible(true);
        fieldToChange.setBoolean(config, newValue);

        JsonElement jsonElement = jankson.toJson(config);

        try (FileWriter fw = new FileWriter(Pvpnotif.configFile)) {
            fw.write(jsonElement.toJson(true, true));
        } catch (IOException ignored) {
        }
    }

    public void updateEnumField(Field fieldToChange, Enum<?> newValue) throws IllegalAccessException {
        Jankson jankson = Jankson.builder().build();
        Config config = Pvpnotif.config;

        fieldToChange.setAccessible(true);
        fieldToChange.set(config, newValue);

        JsonElement jsonElement = jankson.toJson(config);

        try (FileWriter fw = new FileWriter(Pvpnotif.configFile)) {
            fw.write(jsonElement.toJson(true, true));
        } catch (IOException ignored) {
        }
    }
}
