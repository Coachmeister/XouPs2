package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;

public interface LightSourceEffectProducer extends HueEffect {
	@Override
	LightSourceEffect getEffect();
}
