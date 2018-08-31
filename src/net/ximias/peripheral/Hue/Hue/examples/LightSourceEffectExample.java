package net.ximias.peripheral.Hue.Hue.examples;

import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.SequenceAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.LightSourceEffect;

import java.util.Timer;
import java.util.TimerTask;

public class LightSourceEffectExample implements HueExampleEffect {
	private static final int QUADRANT_SPEED = 100;
	private static final int DURATION = 10000;
	private static final TweenType TYPE = TweenType.EaseInOutSine;
	
	@Override
	public Effect getEffect() {
		LightSourceEffect effect = new LightSourceEffect();
		
		TweenAnimation hold1 = new TweenAnimation(1, 1, QUADRANT_SPEED, TweenType.Linear);
		TweenAnimation holdMinus1 = new TweenAnimation(-1, -1, QUADRANT_SPEED, TweenType.Linear);
		
		SequenceAnimation positionX = new SequenceAnimation();
		positionX.append(new TweenAnimation(1,-1,QUADRANT_SPEED, TYPE),"SineOut");
		positionX.append(holdMinus1,"Hold minus 1");
		positionX.append(new TweenAnimation(-1,1,QUADRANT_SPEED, TYPE),"SineIn");
		positionX.append(hold1,"Hold 1");
		
		positionX.setRepeatCount(Math.ceil(DURATION/(QUADRANT_SPEED*4.0)));
		
		SequenceAnimation positionY = new SequenceAnimation();
		positionY.append(hold1,"Hold 1");
		positionY.append(new TweenAnimation(1,-1,QUADRANT_SPEED, TYPE),"SineIn");
		positionY.append(holdMinus1, "Hold -1");
		positionY.append(new TweenAnimation(-1,1,QUADRANT_SPEED, TYPE),"SineOut");
		positionY.setRepeatCount(Math.ceil(DURATION/(QUADRANT_SPEED*4.0)));
		
		effect.setPositionAnimation(positionX, positionY);
		effect.setRadiusAnimation(new ConstantAnimation(1.4));
		
		effect.setColorAnimation(new TweenAnimation(1,0,8000,TweenType.Linear),new ConstantAnimation(0),new TweenAnimation(0,1,8000,TweenType.Linear));
		
		new Timer("(Only for debug) Hue effect animation timer").schedule(new TimerTask() {
			@Override
			public void run() {
				effect.setColorAnimation(new ConstantAnimation(1),new ConstantAnimation(0),new TweenAnimation(1,1,10000,TweenType.Linear));
			}
		},5000);
		
		
		return effect;
	}
	
	@Override
	public int getDuration() {
		return DURATION;
	}
}
