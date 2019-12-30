package net.ximias.effect.producers;

import javafx.scene.paint.Color;
import org.json.JSONObject;

public class FadingEffectProducer extends BlendingEffectProducer {
	
	public FadingEffectProducer(String name, Color startColor, long duration_milliseconds) {
		super(name, startColor, Color.TRANSPARENT, duration_milliseconds);
	}
	
	
	public FadingEffectProducer(JSONObject data) {
		this(data.getString("name"), Color.valueOf(data.getString("color")), data.getLong("duration"));
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.put("name", name);
		ret.put("type", "Fading");
		ret.put("color", startColor.toString());
		ret.put("duration", String.valueOf(duration));
		return ret;
	}
}
