package net.ximias.peripheral.Hue.Hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import net.ximias.peripheral.Hue.Hue.HueEffectWrapper;
import net.ximias.peripheral.Hue.Hue.hueEffects.HueEffect;
import net.ximias.effect.producers.BlendingEffectProducer;
import net.ximias.effect.producers.FadingEffectProducer;
import net.ximias.effect.producers.MultiEffectProducer;

public class ExplosionEffectExample implements HueExampleEffect {
	
	private final HueEffect hueEffect;
	
	public ExplosionEffectExample() {
		javafx.scene.paint.Color ex1 = new javafx.scene.paint.Color(1, 1, 0.3, 1);
		javafx.scene.paint.Color ex2 = new javafx.scene.paint.Color(1, 0.6, 0, 1);
		javafx.scene.paint.Color ex3 = new javafx.scene.paint.Color(1, 0.3, 0, 1);
		
		BlendingEffectProducer e0 = new BlendingEffectProducer(javafx.scene.paint.Color.WHITE, ex1, 100);
		BlendingEffectProducer e1 = new BlendingEffectProducer(ex1, ex2, 100);
		BlendingEffectProducer e2 = new BlendingEffectProducer(ex2, ex3, 100);
		FadingEffectProducer e3 = new FadingEffectProducer(ex3, 1000);
		
		MultiEffectProducer explosion = new MultiEffectProducer(e0, e1, e2, e1, e2, e1, e2, e3);
		HueEffectWrapper wrapper = new HueEffectWrapper();
		hueEffect = wrapper.getAsHueEffect(explosion.build());
	}
	
	@Override
	public Effect getEffect() {
		return hueEffect.getEffect();
	}
	
	@Override
	public int getDuration() {
		return hueEffect.getDuration();
	}
}
