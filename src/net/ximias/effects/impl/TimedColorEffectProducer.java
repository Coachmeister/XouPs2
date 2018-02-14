package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;
import net.ximias.fileParser.JsonSerializable;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

/**
 * A timed color effect. Should probably be renamed. See EventColorEffect doc.
 */
public class TimedColorEffectProducer extends JsonSerializable implements EffectProducer {
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
	
	@Override
	public JsonSerializable fromJson(JSONObject data) {
		return new TimedColorEffectProducer(data.getLong("duration"),getColor(data,"color"));
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
