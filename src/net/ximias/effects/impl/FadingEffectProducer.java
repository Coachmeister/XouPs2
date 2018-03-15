package net.ximias.effects.impl;

import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.util.HashMap;

public class FadingEffectProducer extends BlendingEffectProducer {
	
	public FadingEffectProducer(Color startColor, long duration_milliseconds) {
		super(startColor, Color.TRANSPARENT, duration_milliseconds);
	}
	
	
	FadingEffectProducer(JSONObject data) {
		this(Color.valueOf(data.getString("color")), data.getLong("duration"));
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>(4);
		h.put("color", startColor.toString());
		h.put("duration", String.valueOf(duration));
		return h;
	}
}
