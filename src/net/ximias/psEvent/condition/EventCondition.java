package net.ximias.psEvent.condition;

import org.json.JSONObject;

public interface EventCondition {
	public boolean evaluate(JSONObject payload);
	
}
