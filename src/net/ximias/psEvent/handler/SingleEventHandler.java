package net.ximias.psEvent.handler;

import javafx.scene.paint.Color;
import net.ximias.effects.impl.TimedColorEffect;
import net.ximias.effects.Effect;
import net.ximias.effects.EffectView;
import net.ximias.effects.EffectViews.ConsoleView;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.EventCondition;
import org.json.JSONObject;

public class SingleEventHandler extends Ps2EventHandler {
	/*
	Need to know:
	Event name
	Player or world event
	Effect to apply
	
	Condition for effect to apply or "all"
	
	 */
	
	Effect effect;
	
	EventCondition condition;
	
	public SingleEventHandler(EffectView view, Effect effect, EventCondition condition) {
		super(view);
		this.effect = effect;
		this.condition = condition;
	}
	public SingleEventHandler(EffectView view, Effect effect) {
		super(view);
		condition = null;
		this.effect = effect;
	}
	
	@Override
	public Effect processEvent(JSONObject payload) {
		if (condition==null || condition.evaluate(payload)){
			view.addEffect(effect);
		}
		return null;
	}
	
	public static void main(String[] args) {
		SingleEventHandler event = new SingleEventHandler(new ConsoleView(), new TimedColorEffect(1600, Color.WHITE));
		
		Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
		connection.subscribeWorldEvent("PlayerLogin","13",event);
		
	}
}
