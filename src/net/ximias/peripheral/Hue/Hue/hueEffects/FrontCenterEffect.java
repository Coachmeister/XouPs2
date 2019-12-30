package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.Area;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.AreaEffect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import javafx.scene.paint.Color;


public class FrontCenterEffect implements AreaEffectProducer {
	private final Color startColor;
	private final Color endColor;
	private final int duration;
	private double opacityMultiplier = 1;
	
	public FrontCenterEffect(Color startColor, Color endColor, int duration) {
		this.startColor = startColor;
		this.endColor = endColor;
		this.duration = duration;
	}
	
	
	@Override
	public Effect getEffect() {
		AreaEffect effect = new AreaEffect();
		HueEffect.setColorAnimationOf(effect, startColor, endColor, duration, opacityMultiplier);
		
		effect.setArea(Area.Predefine.Front);
		
		return effect;
	}
	
	@Override
	public void setOpacity(double opacity) {
		opacityMultiplier = opacity;
	}
	
	@Override
	public int getDuration() {
		return duration;
	}
}
