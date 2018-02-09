package net.ximias;

import org.json.JSONObject;

/**
 * Handler for receiving Ps2 events from the EventStreamingConnection.
 */
public abstract class Ps2EventHandler {
	
	EffectView view;
	
	Ps2EventHandler(EffectView view){
		this.view = view;
	}
	
	public void handleEffect(JSONObject payload){
		view.addEffect(processEffect(payload));
	}
	
	public abstract Effect processEffect(JSONObject payload);
}
