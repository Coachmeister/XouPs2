package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import net.ximias.fileParser.JsonSerializable;
import org.json.JSONObject;

import java.util.HashMap;

public class TimedFadingColorAnimationProducer extends TimedColorAnimationProducer {
	TimedFadingColorAnimationProducer(Color startColor, long duration_milliseconds) {
		super(startColor, Color.TRANSPARENT, duration_milliseconds);
	}
	
	@Override
	public JsonSerializable fromJson(JSONObject data) {
		return new TimedFadingColorAnimationProducer(getColor(data, "color"), data.getLong("duration"));
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>(4);
		h.put("color", startColor.toString());
		h.put("duration", String.valueOf(duration));
		return h;
	}
}
