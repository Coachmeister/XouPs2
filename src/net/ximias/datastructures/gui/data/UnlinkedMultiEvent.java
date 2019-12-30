package net.ximias.datastructures.gui.data;

import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.handler.MultiEventHandler;
import net.ximias.psEvent.handler.Ps2EventHandler;

import java.util.ArrayList;

public class UnlinkedMultiEvent implements UnlinkedEvent {
	private ArrayList<UnlinkedEvent> handlers;
	private ArrayList<UnlinkedEvent> resetters;
	private boolean inOrder;
	private boolean repeat;
	private String name;
	private Ps2EventHandler[] handlerEvents;
	private Ps2EventHandler[] resetterEvents;
	
	public UnlinkedMultiEvent(ArrayList<UnlinkedEvent> handlers, ArrayList<UnlinkedEvent> resetters, boolean inOrder, boolean repeat, String name) {
		this.handlers = handlers;
		this.resetters = resetters;
		this.inOrder = inOrder;
		this.repeat = repeat;
		this.name = name;
	}
	
	public void addHandler(UnlinkedEvent handler) {
		handlers.add(handler);
	}
	
	public void addResetter(UnlinkedEvent event) {
		resetters.add(event);
	}
	
	@Override
	public Ps2EventHandler linkWithEffect(EffectProducer producer, EffectView view, Ps2EventStreamingConnection connection) {
		generateContainedEvents(view);
		
		MultiEventHandler result = new MultiEventHandler(handlerEvents, resetterEvents, inOrder, repeat, producer, view, name);
		result.register(connection);
		return result;
	}
	
	private void generateContainedEvents(EffectView view) {
		handlerEvents = new Ps2EventHandler[handlers.size()];
		resetterEvents = new Ps2EventHandler[resetters.size()];
		
		for (int i = 0; i < handlerEvents.length; i++) {
			handlerEvents[i] = handlers.get(i).getBareHandler(view);
		}
		for (int i = 0; i < resetterEvents.length; i++) {
			resetterEvents[i] = resetters.get(i).getBareHandler(view);
		}
	}
	
	@Override
	public Ps2EventHandler getBareHandler(EffectView view) {
		generateContainedEvents(view);
		return new MultiEventHandler(handlerEvents, resetterEvents, inOrder, repeat, null, view, name);
	}
}
