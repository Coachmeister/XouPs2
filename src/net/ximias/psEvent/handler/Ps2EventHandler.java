package net.ximias.psEvent.handler;

import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;
import org.json.JSONObject;

/**
 * Handler for receiving Ps2 events from the EventStreamingConnection.
 */
public abstract class Ps2EventHandler {
	
	protected EffectView view;
	
	Ps2EventHandler(EffectView view){
		this.view = view;
	}
	
	public void eventReceived(JSONObject payload){
		view.addEffect(processEvent(payload));
	}
	
	protected abstract Effect processEvent(JSONObject payload);
}
enum Ps2EventType{
	PLAYER,
	WORLD;
}
