package net.ximias.effect.producers;

import javafx.scene.paint.Color;
import net.ximias.effect.Effect;
import net.ximias.effect.EffectProducer;
import org.json.JSONObject;

import java.util.HashMap;

public class BlendingEffectProducer extends EffectProducer {
	protected Color startColor;
	private final Color endColor;
	protected final long duration;
	
	public BlendingEffectProducer(Color startColor, Color endColor, long duration_milliseconds) {
		this.startColor = startColor;
		this.endColor = endColor;
		duration = duration_milliseconds;
	}
	
	public BlendingEffectProducer(JSONObject data) {
		this(Color.valueOf(data.getString("startColor")), Color.valueOf(data.getString("endColor")), data.getLong("duration"));
	}
	
	@Override
	public Effect build() {
		return new TimedColorAnimation(startColor, endColor, duration, this);
	}
	
	@Override
	public void setColor(Color color) {
		startColor = color;
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>(4);
		h.put("duration", String.valueOf(duration));
		h.put("startColor", startColor.toString());
		h.put("endColor", endColor.toString());
		return h;
	}
	
	public Color getStartColor() {
		return startColor;
	}
	
	public Color getEndColor() {
		return endColor;
	}
	
	public int getDuration() {
		return (int) duration;
	}
}
class TimedColorAnimation implements Effect{
	private final Color startColor;
	private final Color endColor;
	private final long duration;
	private final long startTime;
	private final EffectProducer parent;
	
	TimedColorAnimation(Color startColor, Color endColor, long duration_milliseconds, EffectProducer parent) {
		this.startColor = startColor;
		this.endColor = endColor;
		this.parent = parent;
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
	
	@Override
	public boolean hasIntensity() {
		return true;
	}
	
	@Override
	public EffectProducer getProducer() {
		return parent;
	}
}
