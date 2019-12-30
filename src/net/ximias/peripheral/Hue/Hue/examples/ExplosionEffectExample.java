package net.ximias.peripheral.Hue.Hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import javafx.scene.paint.Color;
import net.ximias.peripheral.Hue.Hue.hueEffects.ExplosionEffect;

public class ExplosionEffectExample implements HueExampleEffect {
	private static final int DURATION = 5000;
	
	@Override
	public Effect getEffect() {
		ExplosionEffect effect = new ExplosionEffect(DURATION, Color.YELLOW, Color.RED, 0, 0);
		return effect.getEffect();
	}
	
	@Override
	public int getDuration() {
		return DURATION;
	}
}
