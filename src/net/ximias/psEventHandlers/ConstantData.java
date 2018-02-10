package net.ximias.psEventHandlers;

import org.json.JSONObject;

public class ConstantData implements ConditionData {
	String value;
	ConditionDataType type;
	
	public ConstantData(String value, ConditionDataType type) {
		this.value = value;
		this.type = type;
	}
	
	@Override
	public String get(JSONObject payload) {
		switch (type){
			case CONSTANT_DATA:
				return value;
			case EVENT_VARIABLE:
				return payload.getString(value);
			case PLAYER_VARIABLE:
				return CurrentPlayer.getInstance().getValue(value);
		}
		throw new Error("Type not found.");
	}
}
