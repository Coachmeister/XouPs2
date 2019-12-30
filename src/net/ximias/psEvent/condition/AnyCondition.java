package net.ximias.psEvent.condition;

import net.ximias.fileParser.Initializer;
import net.ximias.fileParser.JsonSerializable;
import org.json.JSONArray;
import org.json.JSONObject;

public class AnyCondition implements MultiCondition, JsonSerializable {
	private final EventCondition[] data;
	private final String name;
	
	public AnyCondition(String name, EventCondition... conditions) {
		data = conditions;
		this.name = name;
	}
	
	public AnyCondition(JSONObject json) {
		this(json.getString("name"), extractEventConditionsFromJson(json.getJSONArray("conditionData")));
	}
	
	static EventCondition[] extractEventConditionsFromJson(JSONArray json) {
		EventCondition[] conditions = new EventCondition[json.length()];
		for (int i = 0; i < json.length(); i++) {
			conditions[i] = (Initializer.eventConditionFromNameWhileInit(json.getString(i)));
		}
		return conditions;
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.put("name", name);
		ret.put("type", "Any");
		JSONArray array = new JSONArray();
		for (EventCondition cond : data) {
			array.put(cond.getName());
		}
		ret.put("conditionData", array);
		return ret;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public boolean evaluate(JSONObject payload) {
		for (EventCondition datum : data) {
			if (datum.evaluate(payload)) return true;
		}
		return false;
	}
	
	@Override
	public EventCondition[] getContainedConditions() {
		return data;
	}
}
