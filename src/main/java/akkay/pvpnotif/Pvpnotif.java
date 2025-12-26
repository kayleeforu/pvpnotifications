package akkay.pvpnotif;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.api.SyntaxError;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class Pvpnotif implements ModInitializer {
    // Set of the effects in minecraft form that are going to be checked on the player
	private final Set<String> pvpEffects = new HashSet<>();

    // Map of the effects that were detected on a player and are going to be checked each tick
    private final HashMap<String, EffectTriggered> trackedEffects = new HashMap<>();

    // List that has all the effects that will be removed from checking, because invalid
	private final List<String> effectsToDelete = new LinkedList<>();

    // File Object
	public static final File configFile = new File("config/pvpnotif.json");

	// Config
	public static Config config;

    // Jankson Object
	Jankson jankson = Jankson.builder().build();

	@Override
	public void onInitialize() {

		// Config file
		try {
			if (!configFile.exists()) {
				createConfigFile();
				config = new Config();
			} else config = readConfig();
			System.out.println();
			System.out.println();
			System.out.println("PvP Notifications has been initialized");
			System.out.println();
			System.out.println();
		} catch (IOException | SyntaxError e) {
			System.out.println(e.getMessage());
		}

		// Sprint HUD
		SprintText.register();

		// Effects
		initializeEffects();
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			Player player = client.player;
			if (player == null) return;
			Set<String> existingEffects = getEffect(player);

			// Check each saved effect
			for (String effect : trackedEffects.keySet()) {
				EffectTriggered effectTriggered = trackedEffects.get(effect);
				// Check if the effect is still on player
				if (!existingEffects.contains(effect)) effectsToDelete.add(effect);
                int timeLeft = effectTriggered.getDuration();

                // Check if the notification was given and if the duration hit the threshold
				if ((!effectTriggered.isEffectTriggered()) && timeLeft <= config.threshold * 20) {
					effectTriggered.setEffectTriggered(true);
					System.out.println(effect + " will end in " + timeLeft/20 + " seconds.");
				}
				// Check if the effect was renewed
				else if (effectTriggered.isEffectTriggered() && timeLeft > config.threshold * 20) {
					effectTriggered.setEffectTriggered(false);
				}
			}
			// Delete each effect that was removed from the player
			for (String effectToDelete : effectsToDelete) {
				trackedEffects.remove(effectToDelete);
			}
			effectsToDelete.clear();
		});
	}

	Set<String> getEffect(Player player) {
		Set <String> existingEffects = new HashSet<>();
		for (MobEffectInstance effect : player.getActiveEffects()) {
			for (String pvpEffect : pvpEffects) {
				String effectID = effect.getDescriptionId();
				if (effectID.equals(pvpEffect)) {
					existingEffects.add(effectID);
					if (!trackedEffects.containsKey(effectID)) {
						EffectTriggered effectTriggered = new EffectTriggered(effect.getDuration());
						trackedEffects.put(effectID, effectTriggered);
					}
					trackedEffects.get(effectID).setDuration(effect.getDuration());
					break;
				}
			}
		}
		return existingEffects;
	}

	void initializeEffects() {
		pvpEffects.add("effect.minecraft.speed");
		pvpEffects.add("effect.minecraft.slowness");
		pvpEffects.add("effect.minecraft.strength");
		pvpEffects.add("effect.minecraft.regeneration");
		pvpEffects.add("effect.minecraft.resistance");
		pvpEffects.add("effect.minecraft.fire_resistance");
		pvpEffects.add("effect.minecraft.invisibility");
		pvpEffects.add("effect.minecraft.slow_falling");
        pvpEffects.add("effect.minecraft.absorption");
	}

    void createConfigFile() throws IOException, SyntaxError {
		try (FileWriter fw = new FileWriter(configFile)) {
			Config config = new Config();
			configFile.createNewFile();
			JsonElement jsonConfig = jankson.toJson(config);
			String toFile = jsonConfig.toJson(true, true);
			fw.write(toFile);
		}
    }

	Config readConfig() throws IOException, SyntaxError {
		try (FileReader fr = new FileReader(configFile);
			 Scanner scanner = new Scanner(fr)) {

			StringBuilder sb = new StringBuilder();
			while (scanner.hasNextLine())
				sb.append(scanner.nextLine()).append("\n");

			JsonObject toRead = jankson.load(sb.toString());
			Config readConfig = jankson.fromJson(toRead, Config.class);

			boolean isDifferent = false;

			for (Field field : readConfig.getClass().getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers())) continue;

				String fieldName = field.getName();

				if (!toRead.containsKey(fieldName)) {
					field.setAccessible(true);
					toRead.put(fieldName, jankson.toJson(field.get(readConfig)));
					isDifferent = true;
				}
			}

			if (isDifferent) {
				try (FileWriter fw = new FileWriter(configFile)) {
					fw.write(toRead.toJson(true, true));
				}
			}

			return readConfig;
		} catch (IOException | SyntaxError | IllegalAccessException e) {
			System.out.println("Config corrupted, creating new one: " + e.getMessage());
			createConfigFile();
			return new Config();
		}
	}
}

