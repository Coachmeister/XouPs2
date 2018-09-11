package net.ximias.peripheral.Hue.Hue.hueEffects;

import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;
import javafx.scene.paint.Color;


public class CircularEffect implements LightSourceEffectProducer {
	private int durationMs;
	private int rotationDurationMs;
	private Color startColor;
	private Color endColor;
	private double radius;
	private double opacityMultiplier = 1;
	private static final TweenType TYPE = TweenType.EaseInOutSine;
	
	public CircularEffect(int durationMs, int rotationRateMs, Color startColor, Color endColor, double radius) {
		this.durationMs = durationMs;
		this.rotationDurationMs = rotationRateMs;
		this.startColor = startColor;
		this.endColor = endColor;
		this.radius = radius;
	}
	
	@Override
	public LightSourceEffect getEffect() {
		int quadrantSpeed = rotationDurationMs/4;
		
		LightSourceEffect effect = new LightSourceEffect();
		
		setPositionAnimation(quadrantSpeed, effect);
		HueEffect.setColorAnimationOf(effect,startColor, endColor, durationMs, opacityMultiplier);
		return effect;
	}
	
	private void setPositionAnimation(int quadrantSpeed, LightSourceEffect effect) {
		TweenAnimation hold1 = new TweenAnimation(1, 1, quadrantSpeed, TweenType.Linear);
		TweenAnimation holdMinus1 = new TweenAnimation(-1, -1, quadrantSpeed, TweenType.Linear);
		SequenceAnimation positionX = new SequenceAnimation();
		positionX.append(new TweenAnimation(1,-1,quadrantSpeed, TYPE),"SineOut");
		positionX.append(holdMinus1,"Hold minus 1");
		positionX.append(new TweenAnimation(-1,1,quadrantSpeed, TYPE),"SineIn");
		positionX.append(hold1,"Hold 1");
		
		positionX.setRepeatCount(Math.ceil(durationMs/(quadrantSpeed*4.0)));
		
		SequenceAnimation positionY = new SequenceAnimation();
		positionY.append(hold1,"Hold 1");
		positionY.append(new TweenAnimation(1,-1,quadrantSpeed, TYPE),"SineIn");
		positionY.append(holdMinus1, "Hold -1");
		positionY.append(new TweenAnimation(-1,1,quadrantSpeed, TYPE),"SineOut");
		positionY.setRepeatCount(Math.ceil(durationMs/(quadrantSpeed*4.0)));
		
		effect.setPositionAnimation(positionX, positionY);
		effect.setRadiusAnimation(new ConstantAnimation(radius));
	}
	
	@Override
	public void setOpacity(double opacity) {
		opacityMultiplier = opacity;
	}
	
	@Override
	public int getDuration() {
		return durationMs;
	}
}
