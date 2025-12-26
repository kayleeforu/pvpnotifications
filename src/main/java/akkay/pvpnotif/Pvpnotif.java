package akkay.pvpnotif;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
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
	private final HashMap<String, EffectTriggered> trackedEffects = new HashMap<>();
	private final Set<String> pvpEffects = new HashSet<>();
	private final List<String> effectsToDelete = new LinkedList<>();

	// Variables
	private final int threshold = 400; // 20 seconds

	@Override
	public void onInitialize() {
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

				// Check if the notification was given and if the duration hit the threshold
				if ((!effectTriggered.isEffectTriggered()) && effectTriggered.getDuration() <= threshold) {
					effectTriggered.setEffectTriggered(true);
					System.out.println(effect + " will end in " + threshold/20 + " seconds.");
				}
				// Check if the effect was renewed
				else if (effectTriggered.isEffectTriggered() && effectTriggered.getDuration() > threshold) {
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
	}
}