package net.ximias.psEvent.handler;

import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.fileParser.Initializer;
import net.ximias.network.Ps2EventStreamingConnection;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.LinkedList;

public class MultiEventHandler extends Ps2EventHandler {
	/*
	Need to know:
	List of events to occur in order
	
	List of resetting events (in order?)
	
	Effect to apply.
	 */
	private boolean[] eventsHasBeenSatisfied;
	private final Ps2EventHandler[] handlers;
	private final Ps2EventHandler[] resetters;
	private final boolean eventsInOrder;
	private boolean shouldFire = true;
	private boolean repeat;
	
	public MultiEventHandler(Ps2EventHandler[] handlers, Ps2EventHandler[] resetters, boolean eventsInOrder, boolean repeat, EffectProducer effect, EffectView view, String name) {
		super(effect);
		this.name = name;
		this.handlers = handlers;
		eventsHasBeenSatisfied = new boolean[handlers.length];
		this.resetters = resetters;
		this.eventsInOrder = eventsInOrder;
		this.repeat = repeat;
		setView(view);
	}
	
	public MultiEventHandler(JSONObject o) {
		if (o.has("effect")) effect = Initializer.effectProducerFromNameWhileInit(o.getString("effect"));
		name = o.getString("name");
		eventsInOrder = o.optBoolean("inOrder", false);
		repeat = o.optBoolean("repeat", false);
		
		JSONArray events = o.getJSONArray("events");
		eventsHasBeenSatisfied = new boolean[events.length()];
		handlers = new Ps2EventHandler[events.length()];
		for (int i = 0; i < events.length(); i++) {
			Object event = events.get(i);
			handlers[i] = Initializer.eventHandlerFromNameWhileInit((String) event);
		}
		
		JSONArray reevents = o.getJSONArray("resetters");
		resetters = new Ps2EventHandler[reevents.length()];
		for (int i = 0; i < reevents.length(); i++) {
			Object r = reevents.get(i);
			resetters[i] = Initializer.eventHandlerFromNameWhileInit((String) r);
		}
	}
	
	@Override
	public JSONObject toJson() {
		JSONObject ret = new JSONObject();
		if (effect != null) ret.put("effect", effect.getName());
		ret.put("type", "Multi");
		ret.put("name", name);
		ret.put("inOrder", eventsInOrder);
		ret.put("repeat", eventsInOrder);
		JSONArray events = new JSONArray();
		for (Ps2EventHandler handler : handlers) {
			events.put(handler.name);
		}
		JSONArray resetEvents = new JSONArray();
		for (Ps2EventHandler resetter : resetters) {
			resetEvents.put(resetter.name);
		}
		ret.put("events", events);
		ret.put("resetters", resetEvents);
		return ret;
	}
	
	public LinkedList<Ps2EventHandler> getAllHandlers() {
		LinkedList<Ps2EventHandler> ret = new LinkedList<>();
		LinkedList<Ps2EventHandler> handlersToProcess = new LinkedList<>();
		Collections.addAll(handlersToProcess, handlers);
		Collections.addAll(handlersToProcess, resetters);
		for (Ps2EventHandler handler : handlersToProcess) {
			if (handler instanceof MultiEventHandler) {
				ret.addAll(((MultiEventHandler) handler).getAllHandlers());
			}
			ret.add(handler);
		}
		return ret;
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
	public void register(Ps2EventStreamingConnection con) {
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
			if (shouldFire && repeat && isAllEventsSatisfied()) {
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
}