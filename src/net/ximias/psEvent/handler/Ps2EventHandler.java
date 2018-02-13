package net.ximias.psEvent.handler;

import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;
import net.ximias.effects.EffectView;
import org.json.JSONObject;

/**
 * Handler for receiving Ps2 events from the EventStreamingConnection.
 */
public abstract class Ps2EventHandler {
	
	private EffectView view;
	private EffectProducer effect;
	
	Ps2EventHandler(EffectView view, EffectProducer effect){
		this.view = view;
		this.effect = effect;
	}
	
	public void eventReceived(JSONObject payload){
		if (conditionIsSatisfied(payload)){
			view.addEffect(effect.build());
		}
	}
	
	protected abstract boolean conditionIsSatisfied(JSONObject payload);
}
enum Ps2EventType{
	PLAYER,
	WORLD;
}
