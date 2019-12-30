package net.ximias.datastructures.gui.data;

import javafx.scene.paint.Color;
import net.ximias.effect.EffectProducer;
import net.ximias.effect.EffectView;
import net.ximias.effect.producers.TimedEffectProducer;
import net.ximias.effect.views.scenes.DefaultScene;
import net.ximias.effect.views.scenes.EffectScene;
import net.ximias.effect.views.scenes.JSONScene;
import net.ximias.effect.views.scenes.PlayStateBackground;
import net.ximias.logging.Logger;
import net.ximias.network.Ps2EventStreamingConnection;
import net.ximias.psEvent.condition.EventCondition;
import net.ximias.psEvent.handler.Ps2EventHandler;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;


public class EffectData {
	// Maybe..
	private final HashMap<String, UnlinkedEvent> availableEvents = new HashMap<>();
	
	private final HashMap<String, EffectProducer> effects = new HashMap<>();
	private final HashMap<String, EventCondition> conditions = new HashMap<>();
	private final HashMap<String, Ps2EventHandler> eventHandlers = new HashMap<>();
	
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final EffectView view;
	
	private PlayStateBackground background;
	
	private final Ps2EventStreamingConnection connection = new Ps2EventStreamingConnection();
	private final EffectScene scene;
	
	public EffectData(EffectView view, File effectDataFile) {
		this.view = view;
		
		if (effectDataFile != null && effectDataFile.exists()) {
			scene = new JSONScene(view, connection, effectDataFile);
		} else {
			scene = new DefaultScene(view, connection);
		}
		logger.general().info("Serializing all events to json...");
		serializeAllEventsToFile("test2.json");
		logger.general().info("Done serialising!");
		
		connection.hasDisconnectedProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue) {
				view.addEffect(new TimedEffectProducer("DisconnectedEffect", Color.BLACK, 200).build());
			}
		});
	}
	
	public void serializeAllEventsToFile(String filename) {
		LinkedList<String> lines = new LinkedList<>();
		JSONObject serialized = connection.serializeToJSON();
		lines.add(serialized.toString());
		try {
			Files.write(Paths.get(filename), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			throw new Error("Failed to write file", e);
		}
	}
	
	public void intensityChanged(double brightness, double intensity) {
		if (background == null) {
			background = new PlayStateBackground(view, connection, intensity, brightness);
		}
		background.intensityChanged(brightness, intensity);
	}
	
	public void updateBackground() {
		background.updateHandlers();
	}
	
	public void playerIDUpdated() {
		connection.resubscribeAllEvents();
	}
}
