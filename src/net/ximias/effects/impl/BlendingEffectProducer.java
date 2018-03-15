package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;
import org.json.JSONObject;

import java.util.HashMap;

public class BlendingEffectProducer extends EffectProducer {
	protected Color startColor;
	private Color endColor;
	protected long duration;
	
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
		return new TimedColorAnimation(startColor, endColor, duration);
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>(4);
		h.put("duration", String.valueOf(duration));
		h.put("startColor", startColor.toString());
		h.put("endColor", endColor.toString());
		return h;
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
