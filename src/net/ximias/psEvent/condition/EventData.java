package net.ximias.psEvent.condition;

import net.ximias.fileParser.JsonSerializable;
import net.ximias.network.CurrentPlayer;
import org.json.JSONObject;

import java.util.HashMap;

public class EventData extends ConditionData {
	String value;
	ConditionDataSource source;
	
	public EventData(String value, ConditionDataSource source) {
		this.value = value;
		this.source = source;
	}
	
	public EventData(JSONObject data) {
		String dataType = data.getString("source");
		ConditionDataSource type;
		if (dataType.equalsIgnoreCase("constant")) {
			type = ConditionDataSource.CONSTANT;
		} else if (dataType.equalsIgnoreCase("event")) {
			type = ConditionDataSource.EVENT;
		} else if (dataType.equalsIgnoreCase("player")){
			type = ConditionDataSource.PLAYER;
		}else{
			throw new Error("Bad JSON");
		}
		new EventData(data.getString("value") ,type);
	}
	
	@Override
	public String get(JSONObject payload) {
		switch (source) {
			case CONSTANT:
				return value;
			case EVENT:
				return payload.getString(value);
			case PLAYER:
				return CurrentPlayer.getInstance().getValue(value);
		}
		throw new Error("Type not found.");
	}
	
	@Override
	public HashMap<String, String> toJson() {
		HashMap<String, String> h = new HashMap<>(6);
		h.put("source",source.name().toLowerCase());
		h.put("value", value);
		return h;
	}
}
