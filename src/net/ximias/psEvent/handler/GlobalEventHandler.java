package net.ximias.psEvent.handler;

import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.fileParser.Initializer;
import net.ximias.logging.Logger;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.EventCondition;
import org.json.JSONObject;


public class GlobalEventHandler extends Ps2EventHandler implements ConditionedEventHandler {
	private final EventCondition condition;
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public GlobalEventHandler(EventCondition con, EffectProducer effect, EffectView view) {
		super(effect);
		condition = con;
		this.effect = effect;
		setView(view);
	}
	
	public GlobalEventHandler(JSONObject o) {
		condition = Initializer.eventConditionFromNameWhileInit(o.getString("condition"));
		
		if (o.has("effect")) effect = Initializer.effectProducerFromNameWhileInit(o.getString("effect"));
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		ret.put("type", "Global");
		ret.put("condition", condition.getName());
		if (effect != null) {
			ret.put("effect", effect.getName());
		}
		return ret;
	}
	
	public EventCondition getCondition() {
		return condition;
	}
	
	@Override
	protected boolean conditionIsSatisfied(JSONObject payload) {
		boolean evaluate = condition.evaluate(payload);
		if (evaluate) {
			logger.effects().fine("Condition is true. Source: " + payload.optString("event_name", "Not an event"));
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
