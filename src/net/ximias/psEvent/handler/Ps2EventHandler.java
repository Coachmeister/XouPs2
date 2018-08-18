package net.ximias.psEvent.handler;

import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.fileParser.JsonSerializable;
import net.ximias.network.Ps2EventStreamingConnection;
import org.json.JSONObject;
import net.ximias.logging.Logger;


/**
 * Handler for receiving Ps2 events from the EventStreamingConnection.
 */
public abstract class Ps2EventHandler extends JsonSerializable {
	
	private EffectView view;
	protected EffectProducer effect;
	protected String name;
	private final Logger logger = Logger.getLogger(getClass().getName());
	
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
			logger.effects().fine("Condition evaluated to true.");
			view.addEffect(effect.build());
		}
	}
	
	public void setView(EffectView view){
		this.view = view;
	}
	
	protected abstract boolean conditionIsSatisfied(JSONObject payload);
	
	public abstract void register(Ps2EventStreamingConnection con);
	
	public abstract void registerOther(Ps2EventHandler handler, Ps2EventStreamingConnection con);
}
