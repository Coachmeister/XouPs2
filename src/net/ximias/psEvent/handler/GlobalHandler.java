package net.ximias.psEvent.handler;

import net.ximias.effects.EffectProducer;
import net.ximias.effects.EffectView;
import net.ximias.fileParser.Initializer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.EventCondition;
import net.ximias.psEvent.condition.SingleCondition;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.logging.Logger;

public class GlobalHandler extends Ps2EventHandler {
	private final EventCondition condition;
	private Ps2EventType type;
	private EffectProducer effect;
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public GlobalHandler(EventCondition con, EffectProducer effect, EffectView view){
		super(effect);
		condition = con;
		this.effect = effect;
		setView(view);
	}
	
	public GlobalHandler(JSONObject o) {
		if (o.getString("condition").equals("all")) condition = null;
		else condition = new SingleCondition(o.getJSONObject("condition"));
		
		if (o.getString("type").equals("player")) type = Ps2EventType.PLAYER;
		else type = Ps2EventType.WORLD;
		
		if (o.has("effect")) effect = Initializer.effectProducerFromNameWhileInit(o.getString("effect"));
	}
	
	@Override
	public HashMap<String, String> toJson() {
		throw new UnsupportedOperationException("Not yet implemented!");
	}
	
	@Override
	protected boolean conditionIsSatisfied(JSONObject payload) {
		boolean evaluate = condition.evaluate(payload);
		if (evaluate){
			logger.info("Condition is true. Source: "+payload.optString("event_name","Not an event"));
		}
		return evaluate;
	}
	
	@Override
	public void register(Ps2EventStreamingConnection con) {
		con.registerGlobalEventListener(this);
	}
	
	@Override
	public void registerOther(Ps2EventHandler handler, Ps2EventStreamingConnection con) {
		//Do nothing, as to not overwhelm multiEventListener with events.
	}
}
