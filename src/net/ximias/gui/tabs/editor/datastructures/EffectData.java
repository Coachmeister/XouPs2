package net.ximias.gui.tabs.editor.datastructures;

import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.effect.views.scenes.PlayStateScene;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.EventCondition;
import net.ximias.psEvent.handler.Ps2EventHandler;

import java.util.HashMap;
import java.util.logging.Logger;

public class EffectData {
	private HashMap<String, EffectProducer> effects = new HashMap<>();
	private HashMap<String, UnlinkedEvent> availableEvents = new HashMap<>();
	private HashMap<String, String> linkedEventNames = new HashMap<>();
	private HashMap<String, Ps2EventHandler> linkedEvents = new HashMap<>();
	private HashMap<String, EventCondition> conditions = new HashMap<>();
	private Logger logger = Logger.getLogger(getClass().getName());
	private EffectView view;
	
	private final Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
	private final PlayStateScene scene;
	
	public EffectData(EffectView view) {
		this.view = view;
		scene = new PlayStateScene(view, connection);
	}
	
	public void linkEffectWithEvent(String eventName, String effectName){
		if (!effects.containsKey(effectName) || !availableEvents.containsKey(eventName)) {
			logger.warning("Effect could not be linked. One or more of the names does not exist");
			return;
		}
		if (linkedEvents.containsKey(eventName)){
			logger.warning("Effect was already linked. Removing old link..");
			connection.removeEvent(linkedEvents.get(eventName));
		}
		linkedEvents.put(eventName,availableEvents.get(eventName).linkWithEffect(effects.get(effectName),view, connection));
		linkedEventNames.put(eventName, effectName);
	}
	
	public void addAvailableEvent(String name, UnlinkedEvent unlinkedEvent){
	    availableEvents.put(name, unlinkedEvent);
	}
	
	public void removeAvailableEvent(String name){
	    availableEvents.remove(name);
	}
	
	public void addEffect(String name, EffectProducer producer){
	    effects.put(name, producer);
	}
	
	public void removeEffect(String name){
	    effects.remove(name);
	}
	
	public void addCondition(String name, EventCondition condition){
	    conditions.put(name, condition);
	}
	
	public void removeCondition(String name){
	    conditions.remove(name);
	}
	
	public HashMap<String, EventCondition> getConditions() {
		return conditions;
	}
	
	public EventCondition getConditionByName(String name){
		return conditions.get(name);
	}
	
	public HashMap<String, String> getLinkedEventNames() {
		return linkedEventNames;
	}
	
	public void intensityChanged(double brightness, double transparency) {
		scene.intensityChanged(brightness, transparency);
	}
	
	public void updateBackground() {
		scene.updateBackground();
	}
	
	public void playerIDUpdated() {
		connection.resubscribeAllEvents();
	}
}
