package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import net.ximias.peripheral.PeripheralEffectProducer;

public interface HueEffect extends PeripheralEffectProducer {
	Effect getEffect();
	void setOpacity(double opacity);
	int getDuration();
}
