package net.ximias.psEventHandlers;

import javafx.scene.paint.Color;
import net.ximias.ColorEffect;
import net.ximias.Effect;
import net.ximias.EffectView;
import net.ximias.EffectViews.ConsoleView;
import net.ximias.Ps2EventStreamingConnection;
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
		SingleEventHandler event = new SingleEventHandler(new ConsoleView(), new ColorEffect(1600, Color.WHITE));
		
		Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
		connection.subscribeWorldEvent("PlayerLogin","13",event);
		
	}
}
