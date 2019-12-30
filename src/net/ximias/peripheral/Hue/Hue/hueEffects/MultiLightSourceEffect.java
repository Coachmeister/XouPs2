package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;

public class MultiLightSourceEffect implements HueEffect {
	private final LightSourceEffectProducer[] lightSourceEffects;
	private SequenceAnimation positionXAnimation;
	private SequenceAnimation positionYAnimation;
	private SequenceAnimation radiusAnimation;
	private SequenceAnimation r;
	private SequenceAnimation g;
	private SequenceAnimation b;
	private SequenceAnimation a;
	private final int duration;
	
	public MultiLightSourceEffect(LightSourceEffectProducer... effectProducers) {
		lightSourceEffects = effectProducers;
		calculateValues();
		int durSum = 0;
		for (LightSourceEffectProducer effectProducer : effectProducers) {
			durSum += effectProducer.getDuration();
		}
		duration = durSum;
	}
	
	private void calculateValues() {
		positionXAnimation = new SequenceAnimation();
		positionYAnimation = new SequenceAnimation();
		radiusAnimation = new SequenceAnimation();
		r = new SequenceAnimation();
		g = new SequenceAnimation();
		b = new SequenceAnimation();
		a = new SequenceAnimation();
		for (int i = 0; i < lightSourceEffects.length; i++) {
			LightSourceEffectProducer lightSourceEffectProducer = lightSourceEffects[i];
			LightSourceEffect effect = lightSourceEffectProducer.getEffect();
			
			positionXAnimation.append(effect.getXAnimation(), "x" + i);
			positionYAnimation.append(effect.getYAnimation(), "y" + i);
			radiusAnimation.append(effect.getRadiusAnimation(), "radius" + i);
			r.append(effect.getRedAnimation(), "r" + i);
			g.append(effect.getGreenAnimation(), "g" + i);
			b.append(effect.getBlueAnimation(), "b" + i);
			a.append(effect.getOpacityAnimation(), "a" + i);
		}
	}
	
	@Override
	public Effect getEffect() {
		LightSourceEffect effect = new LightSourceEffect();
		effect.setPositionAnimation(positionXAnimation, positionYAnimation);
		effect.setColorAnimation(r, g, b);
		effect.setOpacityAnimation(a);
		effect.setRadiusAnimation(radiusAnimation);
		return effect;
	}
	
	@Override
	public void setOpacity(double opacity) {
		for (LightSourceEffectProducer lightSourceEffect : lightSourceEffects) {
			lightSourceEffect.setOpacity(opacity);
		}
		calculateValues();
	}
	
	@Override
	public int getDuration() {
		return duration;
	}
}
