package net.ximias.psEvent.condition;

import net.ximias.fileParser.JsonSerializable;
import org.json.JSONObject;

public abstract class ConditionData implements JsonSerializable {
	public abstract String get(JSONObject payload);
}
