package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A timed color effect. Should probably be renamed. See EventColorEffect doc.
 */
public class TimedColorEffect implements Effect {
	protected LocalDateTime endTime;
	protected Color color;
	
	public TimedColorEffect(int duration_miliseconds, Color color_javafx){
		endTime = LocalDateTime.now().plus(duration_miliseconds, ChronoUnit.MILLIS);
		if (endTime.isBefore(LocalDateTime.now())) throw new Error("Time is weird");
		color = color_javafx;
	}
	
	public boolean isDone(){
		return endTime.isBefore(LocalDateTime.now());
	}
	
	public Color getColor() {
		return color;
	}
}
