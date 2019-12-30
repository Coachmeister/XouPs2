package net.ximias.fileParser;

import net.ximias.effect.EffectProducer;
import net.ximias.logging.Logger;
import net.ximias.psEvent.condition.EventCondition;
import net.ximias.psEvent.handler.Ps2EventHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

public class Initializer {
	
	private static HashMap<String, EffectProducer> producers = new HashMap<>();
	private static HashMap<String, Ps2EventHandler> events = new HashMap<>();
	private static HashMap<String, EventCondition> conditions = new HashMap<>();
	private static final Logger logger = Logger.getLogger(Initializer.class.getName());
	
	/**
	 * Used to obtain an effectProducer while under initialization.
	 * If not under initialization, use a different method.
	 *
	 * @param name the name to look up
	 * @return the EffectProducer mapped to by the name.
	 * @throws InitializationException if no effect could be found.
	 */
	public static EffectProducer effectProducerFromNameWhileInit(String name) {
		if (!producers.containsKey(name)) {
			throw new InitializationException("Could not find effect " + name);
		}
		return producers.get(name);
	}
	
	/**
	 * Used to obtain an eventHandler while under initialization.
	 * If not under initialization, use a different method
	 *
	 * @param name The name to look up.
	 * @return the EventHandler mapped to by the name.
	 * @throws InitializationException if no EventHandler could be found.
	 */
	public static Ps2EventHandler eventHandlerFromNameWhileInit(String name) {
		if (!events.containsKey(name)) throw new InitializationException("Could not find event " + name);
		return events.get(name);
	}
	
	/**
	 * Used to obtain an eventCondition while under initialization.
	 * If not under initialization, use a different method
	 *
	 * @param name The name to look up.
	 * @return the EventCondition mapped to by the name.
	 * @throws InitializationException if no EventHandler could be found.
	 */
	public static EventCondition eventConditionFromNameWhileInit(String name) {
		if (!conditions.containsKey(name)) throw new InitializationException("Could not find condition: " + name);
		return conditions.get(name);
	}
	
	public void initFromFile(File file) {
		JSONObject json = loadJsonFromFile(file);
		if (json.has("effects")) initEffectsFromJSON(json.getJSONArray("effects"));
		if (json.has("conditions")) initConditionsFromJson(json.getJSONArray("conditions"));
		if (json.has("events")) initEventsFromJSON(json.getJSONArray("events"));
	}
	
	public Collection<EffectProducer> getEffectProducers() {
		return producers.values();
	}
	
	public Collection<Ps2EventHandler> getEventHandlers() {
		return events.values();
	}
	
	public Collection<EventCondition> getEventConditions() {
		return conditions.values();
	}
	
	private JSONObject loadJsonFromFile(File file) {
		try {
			Scanner scanner = new Scanner(file);
			StringBuilder lines = new StringBuilder();
			while (scanner.hasNextLine()) {
				lines.append(scanner.nextLine());
			}
			return new JSONObject(lines.toString());
		} catch (FileNotFoundException e) {
			throw new Error("File not found " + e);
		}
	}
	
	private void initEffectsFromJSON(JSONArray effects) {
		ArrayList<JSONObject> effectsList = JsonArrayToList(effects);
		do {
			for (JSONObject it : effectsList) {
				try {
					producers.put(it.getString("name"), initEffect(it));
					logger.effects().fine("Successfully initialised effect: " + it.getString("name"));
				} catch (InitializationException e) {
					logger.effects().info("Delaying initialization of effect with name: " + it.getString("name") + " Due to: " + e);
				}
			}
		} while (effectsList.removeIf(it -> producers.containsKey(it.getString("name"))));
		if (!effectsList.isEmpty()) {
			logger.effects().severe("One or more effects failed to be initialized:");
			effectsList.forEach(it -> logger.effects().info(it.getString("name")));
		} else {
			logger.effects().info("All effects initialized successfully!");
		}
	}
	
	private EffectProducer initEffect(JSONObject effectObject) {
		String effectType = effectObject.getString("type");
		try {
			Class c = Class.forName("net.ximias.effect.producers." + effectType + "EffectProducer");
			if (EffectProducer.class.isAssignableFrom(c)) {
				return (EffectProducer) findJsonConstructor(c).newInstance(effectObject);
			}
		} catch (ClassNotFoundException e) {
			throw new InitializationException("The effect type with the name: " + effectType + " could not be loaded: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new InitializationException("The effect type class: " + effectType + "EffectProducer does not have public access: " + e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new InitializationException("The effect type class: " + effectType + "EffectProducer is not instantiatable: " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new InitializationException("The effect type class: " + effectType + "EffectProducer threw an error: " + e.getTargetException().getMessage(), e);
		}
		throw new InitializationException("The effect type class: " + effectType + "EffectProducer does not extend EffectProducer");
	}
	
	private Constructor<?> findJsonConstructor(Class<?> aClass) {
		for (int i = 0; i < aClass.getConstructors().length; i++) {
			Constructor<?> constructor = aClass.getConstructors()[i];
			if (constructor.getParameterTypes()[0].equals(JSONObject.class)) return constructor;
		}
		throw new InitializationException("The class " + aClass.getSimpleName() + " does not contain a constructor with a single JSONObject parameter");
	}
	
	private void initConditionsFromJson(JSONArray conditionsArray) {
		ArrayList<JSONObject> conds = JsonArrayToList(conditionsArray);
		do {
			for (JSONObject it : conds) {
				try {
					conditions.put(it.getString("name"), initCondition(it));
					logger.effects().fine("Successfully initialised condition: " + it.getString("name"));
				} catch (InitializationException e) {
					logger.effects().info("Delaying initialization of condition with name: " + it.getString("name") + " Due to: " + e);
				}
			}
		} while (conds.removeIf(it -> conditions.containsKey(it.getString("name"))));
		if (!conds.isEmpty()) {
			logger.effects().severe("One or more conditions failed to be initialized:");
			conds.forEach(it -> logger.effects().info(it.getString("name")));
		} else {
			logger.effects().info("All conditions initialized successfully!");
		}
	}
	
	private EventCondition initCondition(JSONObject conditionObject) {
		String conditionType = conditionObject.getString("type");
		try {
			Class c = Class.forName("net.ximias.psEvent.condition." + conditionType + "Condition");
			if (EventCondition.class.isAssignableFrom(c)) {
				return (EventCondition) findJsonConstructor(c).newInstance(conditionObject);
			}
		} catch (ClassNotFoundException e) {
			throw new InitializationException("The condition type with the name: " + conditionType + " could not be loaded: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new InitializationException("The condition type class: " + conditionType + "Condition does not have public access: " + e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new InitializationException("The condition type class: " + conditionType + "Condition is not instantiatable: " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new InitializationException("The condition type class: " + conditionType + "Condition threw an error: " + e.getTargetException().getMessage(), e);
		}
		throw new InitializationException("The condition type class: " + conditionType + "Condition does not extend EventCondition");
	}
	
	private void initEventsFromJSON(JSONArray JSONEvents) {
		ArrayList<JSONObject> evts = JsonArrayToList(JSONEvents);
		do {
			for (JSONObject it : evts) {
				try {
					events.put(it.getString("name"), initEvent(it));
					logger.effects().fine("Successfully initialised event: " + it.getString("name"));
				} catch (InitializationException e) {
					logger.effects().info("Delaying initialization of event with name: " + it.getString("name") + " Due to: " + e);
				}
			}
		} while (evts.removeIf(it -> events.containsKey(it.getString("name"))));
		if (!evts.isEmpty()) {
			logger.effects().severe("One or more events failed to be initialized:");
			evts.forEach(it -> logger.effects().info(it.getString("name")));
		} else {
			logger.effects().info("All events initialized successfully");
		}
	}
	
	private ArrayList<JSONObject> JsonArrayToList(JSONArray JsonArray) {
		ArrayList<JSONObject> list = new ArrayList<>(40);
		for (Object jsonObject : JsonArray) {
			if (!(jsonObject instanceof JSONObject)) continue;
			JSONObject eventObject = (JSONObject) jsonObject;
			list.add(eventObject);
		}
		return list;
	}
	
	private Ps2EventHandler initEvent(JSONObject jsonObject) {
		String type = jsonObject.getString("type");
		try {
			Class c = Class.forName("net.ximias.psEvent.handler." + type + "EventHandler");
			if (Ps2EventHandler.class.isAssignableFrom(c)) {
				return (Ps2EventHandler) findJsonConstructor(c).newInstance(jsonObject);
			}
		} catch (ClassNotFoundException e) {
			throw new InitializationException("The ps2EventHandler type with the name: " + type + "EventHandler could not be loaded: " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new InitializationException("The ps2EventHandler type class: " + type + "EventHandler does not have public access: " + e.getMessage(), e);
		} catch (InstantiationException e) {
			throw new InitializationException("The ps2EventHandler type class: " + type + "EventHandler is not instantiatable: " + e.getMessage(), e);
		} catch (InvocationTargetException e) {
			throw new InitializationException("The ps2EventHandler type class: " + type + "EventHandler threw an error: " + e.getTargetException().getMessage(), e);
		}
		throw new InitializationException("The ps2EventHandler type class: " + type + "EventHandler does not extend EventCondition");
	}
}

