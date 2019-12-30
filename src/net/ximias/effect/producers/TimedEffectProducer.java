package net.ximias.effect.producers;

import javafx.scene.paint.Color;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectProducer;
import net.ximias.effect.FixedEffect;
import org.json.JSONObject;

/**
 * A timed color effect. Should probably be renamed. See EventColorEffect doc.
 */
public class TimedEffectProducer extends EffectProducer {
	private final long duration;
	private Color color;
	
	public TimedEffectProducer(String name, Color color_javafx, long duration_milliseconds) {
		this.name = name;
		this.duration = duration_milliseconds;
		color = color_javafx;
	}
	
	public TimedEffectProducer(JSONObject data) {
		this(data.getString("name"), Color.valueOf(data.getString("color")), data.getLong("duration"));
	}
	
	@Override
	public Effect build() {
		return new TimedColorEffect(duration, color, this);
	}
	
	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.put("name", name);
		ret.put("type", "Timed");
		ret.put("duration", String.valueOf(duration));
		ret.put("color", color.toString());
		return ret;
	}
	
	public int getDuration() {
		return (int) duration;
	}
}

class TimedColorEffect implements FixedEffect {
	private final long startTime;
	private final long duration;
	private final Color color;
	private final EffectProducer parent;
	
	TimedColorEffect(long duration_milliseconds, Color color_javafx, EffectProducer parent) {
		this.parent = parent;
		startTime = System.currentTimeMillis();
		duration = duration_milliseconds;
		color = color_javafx;
	}
	
	public boolean isDone() {
		return System.currentTimeMillis() - startTime > duration;
	}
	
	@Override
	public boolean hasIntensity() {
		return true;
	}
	
	@Override
	public EffectProducer getProducer() {
		return parent;
	}
	
	public Color getColor() {
		return color;
	}
	
	@Override
	public long getRemainingTime() {
		return System.currentTimeMillis() - startTime;
	}
}
