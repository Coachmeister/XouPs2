package net.ximias.psEvent.handler;

import javafx.scene.paint.Color;
import net.ximias.effects.EffectProducer;
import net.ximias.effects.EffectView;
import net.ximias.effects.EffectViews.ConsoleView;
import net.ximias.effects.impl.TimedEffectProducer;
import net.ximias.fileParser.InitializationException;
import net.ximias.fileParser.Initializer;
import net.ximias.network.CurrentPlayer;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.Condition;
import net.ximias.psEvent.condition.ConditionDataSource;
import net.ximias.psEvent.condition.EventData;
import net.ximias.psEvent.condition.EventCondition;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

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
	private boolean repeat;
	
	public MultiEventHandler(Ps2EventHandler[] handlers, Ps2EventHandler[] resetters, boolean eventsInOrder, boolean repeat, EffectProducer effect, EffectView view, String name) {
		super(effect);
		this.name = name;
		this.handlers = handlers;
		eventsHasBeenSatisfied = new boolean[handlers.length];
		this.resetters = resetters;
		this.eventsInOrder = eventsInOrder;
		setView(view);
	}
	
	public MultiEventHandler(JSONObject o) {
		checkParameters(o);
		if (o.has("effect")) effect = Initializer.effectProducerFromNameWhileInit(o.getString("effect"));
		name = o.getString("name");
		eventsInOrder = o.optBoolean("inOrder",false);
		repeat = o.optBoolean("repeat", false);
		
		JSONArray events =o.getJSONArray("events");
		eventsHasBeenSatisfied = new boolean[events.length()];
		handlers = new Ps2EventHandler[events.length()];
		for (int i = 0; i < events.length(); i++) {
			Object event = events.get(i);
			if (event instanceof JSONObject){
				handlers[i] = new SingleEventHandler((JSONObject) event);
			}else if (event instanceof String){
				handlers[i] = Initializer.eventHandlerFromNameWhileInit((String)event);
			}
		}
		
		JSONArray reevents = o.getJSONArray("resetters");
		resetters = new Ps2EventHandler[reevents.length()];
		for (int i = 0; i < reevents.length(); i++) {
			Object r =reevents.get(i);
			if (r instanceof JSONObject){
				resetters[i] = new SingleEventHandler((JSONObject) r);
			}else if (r instanceof String){
				resetters[i] = Initializer.eventHandlerFromNameWhileInit((String)r);
			}
		}
	}
	
	private void checkParameters(JSONObject o){
		if (o.has("type")&&o.has("condition")&&o.has("events")&&o.has("resetters")){
			if (!o.getString("type").equals("player") && !o.getString("type").equals("world")) {
				throw new InitializationException("Invalid type. Allowed types are player and world");
			}
		}else {
			throw new InitializationException("Missing parameters. Required parameters are: type, condition, events, and resetters.");
		}
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
	
	@Override
	protected void register(Ps2EventStreamingConnection con) {
		for (Ps2EventHandler handler : handlers) {
			handler.registerOther(this, con);
		}
		for (Ps2EventHandler resetter : resetters) {
			resetter.registerOther(this, con);
		}
	}
	
	@Override
	public void registerOther(Ps2EventHandler handler, Ps2EventStreamingConnection con) {
		for (Ps2EventHandler ps2EventHandler : handlers) {
			ps2EventHandler.registerOther(handler, con);
		}
		for (Ps2EventHandler resetter : resetters) {
			resetter.registerOther(handler, con);
		}
	}
	
	
	private boolean reset(JSONObject payload) {
		for (Ps2EventHandler resetter : resetters) {
			if (shouldFire && repeat && isAllEventsSatisfied()){
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
		CurrentPlayer.getInstance().setPlayerID("8287548916321388337");
		ConsoleView view = new ConsoleView();
		TimedEffectProducer white = new TimedEffectProducer(2100, Color.WHITE);
		TimedEffectProducer red = new TimedEffectProducer(2100, Color.RED);
		
		EventData c13 = new EventData("13", ConditionDataSource.CONSTANT);
		EventData c1 = new EventData("1", ConditionDataSource.CONSTANT);
		EventData world = new EventData("world_id", ConditionDataSource.EVENT);
		
		EventCondition worldis1 = new EventCondition(Condition.EQUALS, c1, world);
		EventCondition worldis13 = new EventCondition(Condition.EQUALS, c13, world);
		
		SingleEventHandler world1 = new SingleEventHandler(view, white, worldis1, Ps2EventType.WORLD, "PlayerLogin", "is1");
		SingleEventHandler world13 = new SingleEventHandler(view, white, worldis13, Ps2EventType.WORLD, "PlayerLogin", "is13");
		
		Ps2EventHandler[] handlers = new Ps2EventHandler[] {world13, world13};
		Ps2EventHandler[] resetters = new Ps2EventHandler[] {world1};
		
		MultiEventHandler multiLogin = new MultiEventHandler(handlers, resetters, true,false, red, view, "two13");
		
		Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
		multiLogin.register(connection);
	}
	
	@Override
	public HashMap<String, String> toJson() {
		return null;
	}
}