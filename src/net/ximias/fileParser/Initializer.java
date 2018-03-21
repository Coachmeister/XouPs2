package net.ximias.fileParser;

import net.ximias.effects.EffectProducer;
import net.ximias.psEvent.handler.MultiEventHandler;
import net.ximias.psEvent.handler.Ps2EventHandler;
import net.ximias.psEvent.handler.SingleEventHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Initializer {
	
	private static HashMap<String, EffectProducer> producers;
	private static HashMap<String, SingleEventHandler> singleEvents;
	private static HashMap<String, MultiEventHandler> multiEvents;
	
	/**
	 * Used to obtain an effectProducer while under initialization.
	 * If not under initialization, use a different method.
	 * @param name the name to look up
	 * @return the EffectProducer mapped to by the name.
	 * @throws InitializationException if no effect could be found.
	 */
	public static EffectProducer effectProducerFromNameWhileInit(String name){
		if (!producers.containsKey(name)) throw new InitializationException("Could not find effect "+name);
		return producers.get(name);
	}
	
	/**
	 * Used to obtain an eventHandler while under initialization.
	 * If not under initialization, use a different method
	 * @param name The name to look up.
	 * @return the EventHandler mapped to by the name.
	 * @throws InitializationException if no EventHandler could be found.
	 */
	public static Ps2EventHandler eventHandlerFromNameWhileInit(String name) {
		SingleEventHandler s = singleEvents.get(name);
		if (s != null) {
			return s;
		}else {
			if (!multiEvents.containsKey(name)) throw new InitializationException("Could not find event "+name);
			return multiEvents.get(name);
		}
	}
	
	private void initFromFile(File file){
		JSONObject json = loadJsonFromFile(file);
		if (json.has("effects")) initEffectsFromJSON(json.getJSONArray("effects"));
		if (json.has("events")) initEventsFromJSON(json.getJSONArray("events"));
		if (json.has("multiEvents")) initMultiEventsFromJSON(json.getJSONArray("multiEvents"));
	}
	
	private JSONObject loadJsonFromFile(File file){
		try {
			Scanner scanner = new Scanner(file);
			StringBuilder lines = new StringBuilder();
			while (scanner.hasNextLine()){
				lines.append(scanner.nextLine());
			}
			return new JSONObject(lines.toString());
		} catch (FileNotFoundException e) {
			throw new Error("File not found "+e);
		}
	}
	
	private void initEffectsFromJSON(JSONArray effects){
		ArrayList<JSONObject> multiEffects = new ArrayList<>(40);
		
		for (Object effect : effects) {
			if (!(effect instanceof JSONObject)) continue;
			
			JSONObject effectObject = ((JSONObject) effect);
			String type = effectObject.getString("effectType");
			
			if (type.equals("multi")) {
				multiEffects.add(effectObject);
			} else {
				try {
					EffectProducer prod = initEffect(effectObject.getJSONObject("properties"));
					producers.put(effectObject.optString("name","ID"+producers.size()),prod);
				}catch (InitializationException e){
					// Skip instantiation.
				}
			}
		}
		
		// Remove successfully instantiated effects and re-instantiate any failures. Repeat while successes occur.
		do{
			initMultiEffects(multiEffects);
		}while(multiEffects.removeIf(it-> producers.containsKey(it.getString("name"))));
	}
	
	private EffectProducer initEffect(JSONObject effectObject) {
		String effectType = effectObject.getString("effectType");
		try {
			Class c = Class.forName("net.ximias.effects.impl"+effectType+"EffectProducer");
			if (EffectProducer.class.isAssignableFrom(c)){
				return (EffectProducer) findJsonConstructor(c).newInstance(effectObject);
			}
		} catch (ClassNotFoundException e) {
			throw new InitializationException("The effect type with the name: "+effectType+" could not be loaded.");
		} catch (IllegalAccessException e) {
			throw new InitializationException("The effect type class: "+effectType+"EffectProducer does have public access");
		} catch (InstantiationException e) {
			throw new InitializationException("The effect type class: "+effectType+"EffectProducer is not instantiatable");
		} catch (InvocationTargetException e) {
			throw new InitializationException("The effect type class: "+effectType+"EffectProducer threw an error"+e.getMessage());
		}
		throw new InitializationException("The effect type class: "+effectType+"EffectProducer does not extend EffectProducer");
	}
	
	private void initMultiEffects(ArrayList<JSONObject> multiEffects) {
		for (JSONObject multiEffect : multiEffects) {
			try{
				if (!multiEffect.has("name")) multiEffect.put("name", producers.size()+"ID");
				EffectProducer prod = initEffect(multiEffect.getJSONObject("properties"));
				producers.put(multiEffect.getString("name"), prod);
			}catch (InitializationException e){
				//Skip instantiation as it might contain an uninitialized reference name.
			}
		}
	}
	
	private Constructor<?> findJsonConstructor(Class<?> aClass) {
		for (int i = 0; i < aClass.getConstructors().length; i++) {
			Constructor<?> constructor = aClass.getConstructors()[i];
			if (constructor.getParameterTypes()[0].equals(JSONObject.class)) return constructor;
		}
		throw new InitializationException("The class "+aClass.getSimpleName()+" does not contain a constructor with a single JSONObject parameter");
	}
	
	private void initEventsFromJSON(JSONArray events){
		for (Object event : events) {
			if(!(event instanceof JSONObject)) continue;
			
			JSONObject eventObject = (JSONObject) event;
			singleEvents.put(eventObject.optString("name", "ID"+singleEvents.size()), initEvent(eventObject));
		}
	}
	
	private SingleEventHandler initEvent(JSONObject jsonObject) {
		try {
			return (SingleEventHandler) findJsonConstructor(Class.forName("net.ximias.psEvent.handler.SingleEventHandler")).newInstance(jsonObject);
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			throw new Error("SingleEventHandler could not be instantiated: "+e);
		} catch (InvocationTargetException e) {
			throw new InitializationException("The singleEvent with name "+jsonObject.getString("name")+" threw an error.");
		}
	}
	
	private void initMultiEventsFromJSON(JSONArray jmultiEvents){
		for (Object event : jmultiEvents) {
			if(!(event instanceof JSONObject)) continue;
			
			JSONObject eventObject = (JSONObject) event;
			multiEvents.put(eventObject.optString("name","ID:"+multiEvents.size()), initMultiEvent(eventObject));
		}
	}
	
	private MultiEventHandler initMultiEvent(JSONObject eventObject) {
		try{
			return (MultiEventHandler) findJsonConstructor(Class.forName("net.ximias.psEvent.handler.MultiEventHandler")).newInstance(eventObject);
		} catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			throw new InitializationException("The multiEvent with name "+eventObject.getString("name")+" threw an error.");
		}
		
		return null;
	}
}

