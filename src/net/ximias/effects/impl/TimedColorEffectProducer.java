package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A timed color effect. Should probably be renamed. See EventColorEffect doc.
 */
public class TimedColorEffectProducer implements EffectProducer {
	private long duration;
	protected Color color;
	
	public TimedColorEffectProducer(long duration_miliseconds, Color color_javafx){
		duration = duration_miliseconds;
		color = color_javafx;
	}
	
	@Override
	public Effect build() {
		return new TimedColorEffect(duration, color);
	}
}
class TimedColorEffect implements Effect{
	private long startTime;
	private long duration;
	protected Color color;
	
	TimedColorEffect(long duration_miliseconds, Color color_javafx){
		startTime = System.currentTimeMillis();
		duration = duration_miliseconds;
		color = color_javafx;
	}
	
	public boolean isDone(){
		return System.currentTimeMillis()-startTime > duration;
	}
	
	public Color getColor() {
		return color;
	}
}
