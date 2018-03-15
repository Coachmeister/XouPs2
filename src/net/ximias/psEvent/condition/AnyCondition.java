package net.ximias.psEvent.condition;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AnyCondition implements EventCondition {
	EventCondition[] conditions;
	
	public AnyCondition(EventCondition... eventConditions) {
		conditions = eventConditions;
	}
	
	@Override
	public boolean evaluate(JSONObject payload) {
		for (EventCondition condition : conditions) {
			if (condition.evaluate(payload)) return true;
		}
		return false;
	}
}
