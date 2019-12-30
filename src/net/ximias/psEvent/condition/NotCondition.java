package net.ximias.psEvent.condition;

import net.ximias.fileParser.Initializer;
import net.ximias.fileParser.JsonSerializable;
import org.json.JSONObject;

public class NotCondition implements EventCondition, JsonSerializable {
	private final EventCondition condition;
	private final String name;
	
	public NotCondition(String name, EventCondition condition) {
		this.name = name;
		this.condition = condition;
	}
	
	@Override
	public boolean evaluate(JSONObject payload) {
		return !condition.evaluate(payload);
	}
	
	public NotCondition(JSONObject json) {
		this(json.getString("name"), Initializer.eventConditionFromNameWhileInit(json.getString("condition")));
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.put("name", name);
		ret.put("type", "Not");
		ret.put("condition", condition.getName());
		return ret;
	}
	
	@Override
	public String getName() {
		return name;
	}
}
