package net.ximias.psEvent.condition;

import net.ximias.network.CurrentPlayer;
import org.json.JSONObject;

public class EventData extends ConditionData {
	private String value;
	private ConditionDataSource source;
	
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
		} else if (dataType.equalsIgnoreCase("player")) {
			type = ConditionDataSource.PLAYER;
		} else {
			throw new Error("Bad JSON");
		}
		this.value = data.getString("value");
		this.source = type;
	}
	
	@Override
	public String get(JSONObject payload) {
		switch (source) {
			case CONSTANT:
				return value;
			case EVENT:
				if (!payload.has(value)) return "";
				return payload.getString(value);
			case PLAYER:
				return CurrentPlayer.getInstance().getValue(value);
		}
		throw new Error("Type not found.");
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.put("source", source.name().toLowerCase());
		ret.put("value", value);
		return ret;
	}
}
