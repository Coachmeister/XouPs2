package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;
import javafx.scene.paint.Color;

public class MovementEffect implements LightSourceEffectProducer {
	private final int duration;
	private final int x;
	private final int y;
	private final Color startColor;
	private final Color endColor;
	private double opacityMultiplyer = 1;
	
	private MovementEffect(int duration, int x, int y, Color startColor, Color endColor) {
		this.duration = duration;
		this.x = x;
		this.y = y;
		this.startColor = startColor;
		this.endColor = endColor;
	}
	
	public static MovementEffect fromFrontToBack(int duration, Color startColor, Color endColor) {
		return new MovementEffect(duration, 0, 1, startColor, endColor);
	}
	
	public static MovementEffect fromBackToFront(int duration, Color startColor, Color endColor) {
		return new MovementEffect(duration, 0, -1, startColor, endColor);
	}
	
	public static MovementEffect fromLeftToRight(int duration, Color startColor, Color endColor) {
		return new MovementEffect(duration, -1, 0, startColor, endColor);
	}
	
	public static MovementEffect fromRightToLeft(int duration, Color startColor, Color endColor) {
		return new MovementEffect(duration, 1, 0, startColor, endColor);
	}
	
	public static MovementEffect fromFrontLeftToBackRight(int duration, Color startColor, Color endColor) {
		return new MovementEffect(duration, -1, 1, startColor, endColor);
	}
	
	public static MovementEffect fromBackRightToFrontLeft(int duration, Color startColor, Color endColor) {
		return new MovementEffect(duration, 1, -1, startColor, endColor);
	}
	
	public static MovementEffect fromFrontRightToBackLeft(int duration, Color startColor, Color endColor) {
		return new MovementEffect(duration, 1, 1, startColor, endColor);
	}
	
	public static MovementEffect fromBackLeftToFrontRight(int duration, Color startColor, Color endColor) {
		return new MovementEffect(duration, -1, -1, startColor, endColor);
	}
	
	@Override
	public LightSourceEffect getEffect() {
		LightSourceEffect effect = new LightSourceEffect();
		
		TweenAnimation xAnimation = new TweenAnimation(x, 0 - x, duration, TweenType.Linear);
		TweenAnimation yAnimation = new TweenAnimation(y, 0 - y, duration, TweenType.Linear);
		effect.setPositionAnimation(xAnimation, yAnimation);
		
		HueEffect.setColorAnimationOf(effect, startColor, endColor, duration, opacityMultiplyer);
		effect.setRadiusAnimation(new ConstantAnimation(1.85));
		return effect;
	}
	
	@Override
	public void setOpacity(double opacity) {
		opacityMultiplyer = opacity;
	}
	
	@Override
	public int getDuration() {
		return duration;
	}
}
