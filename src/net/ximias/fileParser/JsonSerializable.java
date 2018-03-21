package net.ximias.fileParser;

import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.util.HashMap;

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
	
	public abstract HashMap<String, String> toJson();
}
