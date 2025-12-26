package akkay.pvpnotif;

import com.google.gson.Gson;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.*;

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

    // Gson Object
    // TODO continue
    Gson gson = new Gson();

	// Variables
	private final int threshold = 400; // 20 seconds

	@Override
	public void onInitialize() {
		initializeEffects();
        // TODO Change the text output, maybe leave only the init message
        try {
            if (!configFile.exists()) {
                System.out.println("File does not exist, it will be created");
                createConfigFile();
            }
            else System.out.println("File already exists");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("PvP Notifications has been initialized");
        }


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
				if ((!effectTriggered.isEffectTriggered()) && timeLeft <= threshold) {
					effectTriggered.setEffectTriggered(true);
					System.out.println(effect + " will end in " + timeLeft/20 + " seconds.");
				}
				// Check if the effect was renewed
				else if (effectTriggered.isEffectTriggered() && timeLeft > threshold) {
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

    void createConfigFile() throws IOException {
        configFile.createNewFile();
    }
}