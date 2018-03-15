package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A timed color effect. Should probably be renamed. See EventColorEffect doc.
 */
public class TimedEffectProducer extends EffectProducer {
	private long duration;
	protected Color color;
	
	public TimedEffectProducer(Color color_javafx, long duration_miliseconds){
		duration = duration_miliseconds;
		color = color_javafx;
	}
	
	public TimedEffectProducer(JSONObject data) {
		this(Color.valueOf(data.getString("color")),data.getLong("duration"));
	}
	
	@Override
	public Effect build() {
		return new TimedColorEffect(duration, color);
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>(4);
		h.put("duration", String.valueOf(duration));
		h.put("color", color.toString());
		return h;
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
