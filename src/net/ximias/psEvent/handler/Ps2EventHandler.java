package net.ximias.psEvent.handler;

import net.ximias.effects.Effect;
import net.ximias.effects.EffectProducer;
import net.ximias.effects.EffectView;
import net.ximias.fileParser.JsonSerializable;
import net.ximias.network.Ps2EventStreamingConnection;
import org.json.JSONObject;

/**
 * Handler for receiving Ps2 events from the EventStreamingConnection.
 */
public abstract class Ps2EventHandler extends JsonSerializable {
	
	private EffectView view;
	protected EffectProducer effect;
	protected String name;
	
	public Ps2EventHandler() {
	}
	
	Ps2EventHandler(EffectProducer effect){
		this.effect = effect;
	}
	
	public String getName() {
		return name;
	}
	
	public void eventReceived(JSONObject payload){
		if (view!=null && conditionIsSatisfied(payload) && effect != null){
			view.addEffect(effect.build());
		}
	}
	
	public void setView(EffectView view){
		this.view = view;
	}
	
	protected abstract boolean conditionIsSatisfied(JSONObject payload);
	
	protected abstract void register(Ps2EventStreamingConnection con);
	
	public abstract void registerOther(Ps2EventHandler handler, Ps2EventStreamingConnection con);
}
enum Ps2EventType{
	PLAYER,
	WORLD;
}
