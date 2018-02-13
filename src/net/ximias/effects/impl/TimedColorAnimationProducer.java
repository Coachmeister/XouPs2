package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;

public class TimedColorAnimationProducer implements EffectProducer {
	private Color startColor;
	private Color endColor;
	private long duration;
	
	TimedColorAnimationProducer(Color startColor, Color endColor, long duration_milliseconds) {
		this.startColor = startColor;
		this.endColor = endColor;
		duration = duration_milliseconds;
	}
	
	@Override
	public Effect build() {
		return new TimedColorAnimation(startColor, endColor, duration);
	}
}
class TimedColorAnimation implements Effect{
	private Color startColor;
	private Color endColor;
	private long duration;
	private long startTime;
	
	TimedColorAnimation(Color startColor, Color endColor, long duration_milliseconds) {
		this.startColor = startColor;
		this.endColor = endColor;
		duration = duration_milliseconds;
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public Color getColor() {
		return startColor.interpolate(endColor, Math.min((System.currentTimeMillis()-startTime)/(double)duration,1.0));
	}
	
	@Override
	public boolean isDone() {
		return System.currentTimeMillis()>startTime+duration;
	}
}
