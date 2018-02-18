package net.ximias.psEvent.handler;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectProducer;
import net.ximias.effects.impl.TimedColorEffectProducer;
import net.ximias.effects.EffectView;
import net.ximias.effects.EffectViews.ConsoleView;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.*;
import org.json.JSONObject;

import java.util.ArrayList;

public class SingleEventHandler extends Ps2EventHandler {
	/*
	Need to know:
	Event name
	Player or world event
	Effect to apply
	
	Condition for effect to apply or "all"
	
	 */
	
	EffectProducer effect;
	
	EventCondition condition;
	
	public SingleEventHandler(EffectView view, EffectProducer effect, EventCondition condition) {
		super(view, effect);
		this.condition = condition;
	}
	public SingleEventHandler(EffectView view, EffectProducer effect) {
		super(view, effect);
		condition = null;
		this.effect = effect;
	}
	
	@Override
	public boolean conditionIsSatisfied(JSONObject payload) {
		return (condition==null || condition.evaluate(payload));
	}
	
	public static void main(String[] args) {
		ArrayList<ConditionData> data= new ArrayList<>(5);
		data.add(new EventData("13", ConditionDataSource.CONSTANT));
		data.add(new EventData("world_id", ConditionDataSource.EVENT));
		
		EventCondition worldIdIs13 = new EventCondition(data,Condition.EQUALS);
		SingleEventHandler event = new SingleEventHandler(new ConsoleView(), new TimedColorEffectProducer(1600, Color.WHITE),worldIdIs13);
		
		Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
		connection.subscribeWorldEvent("PlayerLogin","13\",\"1",event);
		
	}
}
