package net.ximias;


import javafx.scene.paint.Color;

/**
 * Modifiable color effect.
 * Extends ColorEffect because of poor design...
 * ColorEffect should probably be an interface, and the current ColorEffect should be named TimedColorEffect.
 */
public class EventColorEffect extends ColorEffect {
	private boolean done = false;
	
	public EventColorEffect(int duration_miliseconds, Color color_javafx) {
		super(duration_miliseconds, color_javafx);
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
