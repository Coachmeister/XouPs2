package net.ximias.effect.views.scenes;

import net.ximias.effect.EffectView;
import net.ximias.fileParser.Initializer;
import net.ximias.logging.Logger;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.handler.Ps2EventHandler;

import java.io.File;

public class JSONScene extends EffectScene {
	Logger logger = Logger.getLogger(getClass().getName());
	
	public JSONScene(EffectView view, Ps2EventStreamingConnection connection, File jsonFile) {
		super(view, connection);
		Initializer init = new Initializer();
		init.initFromFile(jsonFile);
		for (Ps2EventHandler handler : init.getEventHandlers()) {
			if (handler.getEffect() != null) {
				handler.setView(view);
				handler.register(connection);
				logger.effects().info("Registering handler: " + handler.getName());
			}
		}
		logger.effects().info("Handlers registered!");
	}
	
	
}
