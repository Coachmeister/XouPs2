package net.ximias.gui.tabs.editor.datastructures;

import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.EventCondition;
import net.ximias.psEvent.handler.Ps2EventHandler;
import net.ximias.psEvent.handler.Ps2EventType;
import net.ximias.psEvent.handler.PsEvent;
import net.ximias.psEvent.handler.SingleEventHandler;

public class UnlinkedSingleEvent implements UnlinkedEvent{
	private Ps2EventType type;
	private PsEvent eventName;
	private EventCondition condition;
	
	private String name;
	
	public UnlinkedSingleEvent(Ps2EventType type, PsEvent eventName, EventCondition condition, String name) {
		this.type = type;
		this.eventName = eventName;
		this.condition = condition;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public Ps2EventHandler linkWithEffect(EffectProducer producer, EffectView view, Ps2EventStreamingConnection connection) {
		SingleEventHandler singleEventHandler = new SingleEventHandler(view, producer, condition, type, eventName.getName(), name);
		singleEventHandler.register(connection);
		return singleEventHandler;
	}
	
	@Override
	public Ps2EventHandler getBareHandler(EffectView view) {
		return new SingleEventHandler(view, null, condition, type, eventName.getName(), name);
	}
}
