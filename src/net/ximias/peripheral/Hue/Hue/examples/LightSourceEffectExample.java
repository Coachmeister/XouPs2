package net.ximias.peripheral.Hue.Hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import javafx.scene.paint.Color;
import net.ximias.peripheral.Hue.Hue.hueEffects.CircularEffect;

public class LightSourceEffectExample implements HueExampleEffect {
	private static final int DURATION = 2300;
	private static final int ROTATION = 400;
	private static final TweenType TYPE = TweenType.EaseInOutSine;
	
	@Override
	public Effect getEffect() {
		CircularEffect hueEffect = new CircularEffect(DURATION, ROTATION, Color.RED, Color.PINK, 1.5);
		return hueEffect.getEffect();
	}
	
	@Override
	public int getDuration() {
		return DURATION;
	}
}