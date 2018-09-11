package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.AreaEffect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.ColorAnimationEffect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import javafx.scene.paint.Color;
import net.ximias.peripheral.PeripheralEffectProducer;

public interface HueEffect extends PeripheralEffectProducer {
	Effect getEffect();
	void setOpacity(double opacity);
	int getDuration();
	
	static void setColorAnimationOf(ColorAnimationEffect effect, Color startColor, Color endColor, int durationMs, double opacityMultiplier){
		TweenAnimation r = new TweenAnimation(startColor.getRed(), endColor.getRed(), durationMs, TweenType.Linear);
		TweenAnimation g = new TweenAnimation(startColor.getGreen(), endColor.getGreen(), durationMs, TweenType.Linear);
		TweenAnimation b = new TweenAnimation(startColor.getBlue(), endColor.getBlue(), durationMs, TweenType.Linear);
		TweenAnimation a = new TweenAnimation(startColor.getOpacity() * opacityMultiplier, endColor.getOpacity() * opacityMultiplier, durationMs, TweenType.Linear);
		
		effect.setColorAnimation(r,g,b);
		effect.setOpacityAnimation(a);
	}
}
