package net.ximias.effects.impl;


import javafx.scene.paint.Color;
import net.ximias.effects.Effect;

/**
 * Modifiable color effect.
 */
public class EventColorEffect implements Effect {
	private boolean done = false;
	private Color color;

	public EventColorEffect(Color color_javafx) {
		color = color_javafx;
	}
	
	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	
	public void setColor(Color color){
		this.color = color;
	}
	
	public void setDone(boolean done) {
		this.done= true;
	}
}
