package net.ximias.peripheral.Hue.Hue;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Entertainment;
import com.philips.lighting.hue.sdk.wrapper.entertainment.StartCallback;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import javafx.beans.property.SimpleBooleanProperty;
import net.ximias.peripheral.Hue.Hue.examples.HueExampleEffect;

public class EffectExamples {
	
	private final Entertainment entertainment;
	private SimpleBooleanProperty isPlaying = new SimpleBooleanProperty(false);
	
	public EffectExamples(Entertainment entertainment) {
		this.entertainment = entertainment;
	}
	
	public SimpleBooleanProperty getPlayingProperty() {
		return isPlaying;
	}
	
	public boolean playing() {
		return isPlaying.get();
	}
	
	public void play(HueExampleEffect hueEffect) {
		if (playing()) {
			System.out.println("Can not play effect. Entertainment is already running");
			return;
		}
		startEntertainmentSystem(hueEffect);
	}
	
	private void playEffect(HueExampleEffect hueEffect) {
		Effect effect = hueEffect.getEffect();
		int duration = hueEffect.getDuration();
		
		entertainment.lockMixer();
		entertainment.addEffect(effect);
		effect.enable();
		entertainment.unlockMixer();
		
		turnOffEntertainmentAfterDelayOf(duration);
		effect.disable();
		effect.finish();
	}
	
	private void startEntertainmentSystem(HueExampleEffect effect) {
		entertainment.start(startStatus -> {
			if (startStatus != StartCallback.StartStatus.Success) return;
			isPlaying.set(true);
			playEffect(effect);
		});
	}
	
	private void turnOffEntertainmentAfterDelayOf(int duration) {
		new Thread(() -> {
			try {
				Thread.sleep(duration);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			entertainment.stop(() -> isPlaying.set(false));
		}).run();
	}
}
