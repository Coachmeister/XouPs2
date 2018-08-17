package net.ximias.datastructures.gui.data;

import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.handler.Ps2EventHandler;

public interface UnlinkedEvent {
	public Ps2EventHandler linkWithEffect(EffectProducer producer, EffectView view, Ps2EventStreamingConnection connection);
	Ps2EventHandler getBareHandler(EffectView view);
}
