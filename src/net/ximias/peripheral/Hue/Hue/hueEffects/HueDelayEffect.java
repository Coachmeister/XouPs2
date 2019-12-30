package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;


public class HueDelayEffect implements AreaEffectProducer, LightSourceEffectProducer {
	private final int durationMS;
	
	public HueDelayEffect(int durationMS) {
		this.durationMS = durationMS;
	}
	
	@Override
	public LightSourceEffect getEffect() {
		LightSourceEffect effect = new LightSourceEffect();
		ConstantAnimation emptyAnimation = new ConstantAnimation(0, durationMS);
		effect.setColorAnimation(emptyAnimation, emptyAnimation, emptyAnimation);
		effect.setOpacityAnimation(emptyAnimation);
		effect.setRadiusAnimation(emptyAnimation);
		effect.setPositionAnimation(emptyAnimation, emptyAnimation);
		return effect;
	}
	
	@Override
	public void setOpacity(double opacity) {
	
	}
	
	@Override
	public int getDuration() {
		return durationMS;
	}
}
