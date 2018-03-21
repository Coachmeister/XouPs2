package net.ximias.psEvent.condition;

import org.json.JSONObject;

public class NotCondition implements EventCondition {
	EventCondition condition;
	
	public NotCondition(EventCondition condition) {
		this.condition = condition;
	}
	
	@Override
	public boolean evaluate(JSONObject payload) {
		return !condition.evaluate(payload);
	}
}
