package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class TimedColorAnimation implements Effect {
	Color startColor;
	Color endColor;
	long duration;
	long startTime;
	
	public TimedColorAnimation(Color startColor, Color endColor, long duration_milliseconds) {
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
