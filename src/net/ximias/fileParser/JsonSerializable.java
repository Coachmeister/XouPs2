package net.ximias.fileParser;

import javafx.scene.paint.Color;
import org.json.JSONObject;

public interface JsonSerializable {
	default Color getColor(JSONObject data, String key) {
		return Color.valueOf(data.getString(key));
	}
	
	JSONObject toJson();
}
