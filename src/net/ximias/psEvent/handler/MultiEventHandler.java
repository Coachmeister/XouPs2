package net.ximias.psEvent.handler;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectProducer;
import net.ximias.effects.EffectView;
import net.ximias.effects.EffectViews.ConsoleView;
import net.ximias.effects.impl.TimedColorEffectProducer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.Condition;
import net.ximias.psEvent.condition.ConditionDataSource;
import net.ximias.psEvent.condition.EventData;
import net.ximias.psEvent.condition.EventCondition;
import org.json.JSONObject;

public class MultiEventHandler extends Ps2EventHandler {
	/*
	Need to know:
	List of events to occur in order
	
	List of resetting events (in order?)
	
	Effect to apply.
	 */
	private boolean[] eventsHasBeenSatisfied;
	private Ps2EventHandler[] handlers;
	private Ps2EventHandler[] resetters;
	private boolean eventsInOrder;
	private boolean shouldFire = true;
	
	public MultiEventHandler(Ps2EventHandler[] handlers, Ps2EventHandler[] resetters, boolean eventsInOrder, EffectProducer effect, EffectView view) {
		super(view, effect);
		this.handlers = handlers;
		eventsHasBeenSatisfied = new boolean[handlers.length];
		this.resetters = resetters;
		this.eventsInOrder = eventsInOrder;
	}
	
	@Override
	protected boolean conditionIsSatisfied(JSONObject payload) {
		
		for (int i = 0; i < eventsHasBeenSatisfied.length; i++) {
			boolean b = eventsHasBeenSatisfied[i];
			if (!b) {
				eventsHasBeenSatisfied[i] = handlers[i].conditionIsSatisfied(payload);
				if (eventsInOrder) break;
			}
		}
		return !reset(payload) && shouldFire && isAllEventsSatisfied();
	}
	
	private boolean reset(JSONObject payload) {
		for (Ps2EventHandler resetter : resetters) {
			if (shouldFire && resetter == this && isAllEventsSatisfied()){
				eventsHasBeenSatisfied = new boolean[handlers.length];
				return false;
			}
			if (resetter.conditionIsSatisfied(payload)) {
				eventsHasBeenSatisfied = new boolean[handlers.length];
				shouldFire = true;
				return true;
			}
		}
		return false;
	}
	
	private boolean isAllEventsSatisfied() {
		for (boolean b : eventsHasBeenSatisfied) {
			if (!b) return false;
		}
		shouldFire = false;
		return true;
	}
	
	public static void main(String[] args) {
		ConsoleView view = new ConsoleView();
		TimedColorEffectProducer white = new TimedColorEffectProducer(2100, Color.WHITE);
		TimedColorEffectProducer red = new TimedColorEffectProducer(2100, Color.RED);
		
		EventData c13 = new EventData("13", ConditionDataSource.CONSTANT);
		EventData c1 = new EventData("1", ConditionDataSource.CONSTANT);
		EventData world = new EventData("world_id", ConditionDataSource.EVENT);
		
		EventCondition worldis1 = new EventCondition(Condition.EQUALS, c1, world);
		EventCondition worldis13 = new EventCondition(Condition.EQUALS, c13, world);
		
		SingleEventHandler world1 = new SingleEventHandler(view, white, worldis1);
		SingleEventHandler world13 = new SingleEventHandler(view, white, worldis13);
		
		Ps2EventHandler[] handlers = new Ps2EventHandler[] {world13, world13};
		Ps2EventHandler[] resetters = new Ps2EventHandler[] {world1};
		
		MultiEventHandler multiLogin = new MultiEventHandler(handlers, resetters, true, red, view);
		
		Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
		connection.subscribeWorldEvent("PlayerLogin", "13\",\"1", multiLogin);
	}
}