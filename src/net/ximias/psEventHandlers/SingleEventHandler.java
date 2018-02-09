package net.ximias.psEventHandlers;

import net.ximias.Effect;
import net.ximias.EffectView;
import org.json.JSONObject;

public class SingleEventHandler extends Ps2EventHandler {
	/*
	Need to know:
	Event name
	Player or world event
	Effect to apply
	
	Condition for effect to apply or "all"
	
	 */
	
	SingleEventHandler(EffectView view) {
		super(view);
	}
	
	@Override
	public Effect processEvent(JSONObject payload) {
		return null;
	}
}
