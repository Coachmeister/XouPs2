package net.ximias.peripheral.Hue.Hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;

public interface HueExampleEffect {
	Effect getEffect();
	
	int getDuration();
}
