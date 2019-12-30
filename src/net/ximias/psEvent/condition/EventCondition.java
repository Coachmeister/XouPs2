package net.ximias.psEvent.condition;

import net.ximias.fileParser.JsonSerializable;
import org.json.JSONObject;

public interface EventCondition extends JsonSerializable {
	boolean evaluate(JSONObject payload);
	
	String getName();
}
