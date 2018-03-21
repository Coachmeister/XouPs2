package net.ximias.psEvent.condition;

import org.json.JSONObject;

public interface EventCondition {
	boolean evaluate(JSONObject payload);
	
}
