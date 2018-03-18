package net.ximias.psEvent.condition;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class NoneCondition implements EventCondition {
	ArrayList<EventCondition> data;
	
	public NoneCondition(EventCondition... conditions) {
		data = new ArrayList<>(24);
		data.addAll(Arrays.asList(conditions));
	}
	
	@Override
	public boolean evaluate(JSONObject payload) {
		for (EventCondition datum : data) {
			if (datum.evaluate(payload)) return false;
		}
		return true;
	}
}