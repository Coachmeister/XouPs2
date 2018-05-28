package net.ximias.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import net.ximias.effect.PeripheralEffectProducer;

public interface HueEffect extends PeripheralEffectProducer {
	Effect getEffect();
	void adjustOpacity(double opacity);
	int getDuration();
}
