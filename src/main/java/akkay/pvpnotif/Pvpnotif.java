package akkay.pvpnotif;

import blue.endless.jankson.*;
import blue.endless.jankson.api.SyntaxError;
import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.*;

class Config {
	int threshold = 400; // 20 seconds
	boolean soundNotification = true; // TODO
}

class EffectTriggered {
	private boolean effectTriggered = false;
	private int duration;

	public boolean isEffectTriggered() {
		return effectTriggered;
	}
	public void setEffectTriggered(boolean flag) {
		this.effectTriggered = flag;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public EffectTriggered(int duration) {
		this.duration = duration;
	}
}

public class Pvpnotif implements ModInitializer {
	// public static final String MOD_ID = "pvpnotif";
	// public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Set of the effects in minecraft form that are going to be checked on the player
	private final Set<String> pvpEffects = new HashSet<>();

    // Map of the effects that were detected on a player and are going to be checked each tick
    private final HashMap<String, EffectTriggered> trackedEffects = new HashMap<>();

    // List that has all the effects that will be removed from checking, because invalid
	private final List<String> effectsToDelete = new LinkedList<>();

    // File Object
    private final File configFile = new File("config/pvpnotif.json");

	// Config
	private Config config;

    // Jankson Object
    // TODO continue
	Jankson jankson = Jankson.builder().build();

	@Override
	public void onInitialize() {
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
				if ((!effectTriggered.isEffectTriggered()) && timeLeft <= config.threshold) {
					effectTriggered.setEffectTriggered(true);
					System.out.println(effect + " will end in " + timeLeft/20 + " seconds.");
				}
				// Check if the effect was renewed
				else if (effectTriggered.isEffectTriggered() && timeLeft > config.threshold) {
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
		try (FileReader fr = new FileReader(configFile); Scanner scanner = new Scanner(fr)) {
			StringBuilder sb = new StringBuilder();
			while(scanner.hasNextLine()) sb.append(scanner.nextLine()).append("\n");
			JsonObject toRead = jankson.load(sb.toString());
			Config readConfig = jankson.fromJson(toRead, Config.class);
            return readConfig;
		} catch (IOException | SyntaxError e) {
			System.out.println("Config corrupted, creating new one: " + e.getMessage());
			createConfigFile();
			return new Config();
		}
	}
}