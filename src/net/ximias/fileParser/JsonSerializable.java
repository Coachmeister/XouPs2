package net.ximias.fileParser;

import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public abstract class JsonSerializable {
	protected Color getColor(JSONObject data, String key) {
		return Color.valueOf(data.getString(key));
	}
	
	public JSONObject getJsonObject() {
		HashMap<String, String> values = toJson();
		JSONObject jsonObject = new JSONObject();
		values.forEach(jsonObject::put);
		return jsonObject;
	}
	
	public abstract JsonSerializable fromJson(JSONObject data);
	
	public abstract HashMap<String, String> toJson();
}
