package net.ximias.psEventHandlers;

import net.ximias.Effect;
import net.ximias.EffectView;
import org.json.JSONObject;

/**
 * Handler for receiving Ps2 events from the EventStreamingConnection.
 */
public abstract class Ps2EventHandler {
	
	protected EffectView view;
	
	Ps2EventHandler(EffectView view){
		this.view = view;
	}
	
	public void eventRecieved(JSONObject payload){
		view.addEffect(processEvent(payload));
	}
	
	public abstract Effect processEvent(JSONObject payload);
}
