package net.ximias.effect.views.scenes;

import net.ximias.effect.EffectView;
import net.ximias.network.Ps2EventStreamingConnection;

public abstract class EffectScene {
	protected final EffectView view;
	protected final Ps2EventStreamingConnection connection;
	
	protected EffectScene(EffectView view, Ps2EventStreamingConnection connection) {
		this.view = view;
		this.connection = connection;
	}
}
