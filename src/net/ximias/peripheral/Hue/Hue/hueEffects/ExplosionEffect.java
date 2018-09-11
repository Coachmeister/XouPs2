package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;
import javafx.scene.paint.Color;

public class ExplosionEffect implements LightSourceEffectProducer {
	private final int duration;
	private final Color startColor;
	private final Color endColor;
	private final double locatiolnX;
	private final double locatiolnY;
	private double opacityMultiplier = 1;
	
	public ExplosionEffect(int duration, Color startColor, Color endColor) {
		this.duration = duration;
		this.startColor = startColor;
		this.endColor = endColor;
		locatiolnX = 0;
		locatiolnY = 0;
	}
	
	public ExplosionEffect(int duration, Color startColor, Color endColor, int locationX, int locationY){
		this.duration = duration;
		this.startColor = startColor;
		this.endColor = endColor;
		this.locatiolnX = locationX;
		this.locatiolnY = locationY;
	}
	
	@Override
	public LightSourceEffect getEffect() {
		LightSourceEffect effect = new LightSourceEffect();
		effect.setRadiusAnimation(new TweenAnimation(0.8,3,duration/2, TweenType.Linear));
		effect.setPositionAnimation(new ConstantAnimation(locatiolnX),new ConstantAnimation(locatiolnY));
		setColorAnimation(effect);

		return effect;
	}
	
	private void setColorAnimation(LightSourceEffect effect){
		HueEffect.setColorAnimationOf(effect, startColor, endColor, duration/2,opacityMultiplier);
		SequenceAnimation opacity = new SequenceAnimation();
		opacity.append(effect.getOpacityAnimation(),"Color fade");
		opacity.append(new TweenAnimation(endColor.getOpacity(),0,duration/2,TweenType.EaseInOutQuad),"Fade out");
		effect.setOpacityAnimation(opacity);
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
