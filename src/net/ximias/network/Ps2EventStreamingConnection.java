package net.ximias.network;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.ximias.effect.EffectProducer;
import net.ximias.effect.producers.MultiEffectProducer;
import net.ximias.logging.Logger;
import net.ximias.psEvent.condition.EventCondition;
import net.ximias.psEvent.condition.MultiCondition;
import net.ximias.psEvent.handler.ConditionedEventHandler;
import net.ximias.psEvent.handler.MultiEventHandler;
import net.ximias.psEvent.handler.Ps2EventHandler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Connects to the event streaming part of the census API.
 * Events can be subscribed to by calling subscribePlayerEvent or subscribeWorldEvent.
 */
public class Ps2EventStreamingConnection {
	public static final int CONNECTION_TIMEOUT = 90_000;
	private final HashMap<String, ArrayList<Ps2EventHandler>> subscribedEvents = new HashMap<>();
	private final HashMap<String, ArrayList<String>> eventsIds = new HashMap<>();
	private final ArrayList<Ps2EventHandler> globalHandlers = new ArrayList<>(12);
	private WebsocketClientEndpoint clientEndPoint;
	
	private final Logger logger = Logger.getLogger(getClass().getName());
	private final SimpleBooleanProperty isAvailable = new SimpleBooleanProperty(true);
	private final SimpleBooleanProperty hasDisconnected = new SimpleBooleanProperty(false);
	private final Ps2BackupPollingService pollingService = new Ps2BackupPollingService(this);
	private final ArrayList<WebsocketClientEndpoint.MessageHandler> messageHandlers = new ArrayList<>();
	private static Ps2EventStreamingConnection instance;
	
	private long lastMessageTime = System.currentTimeMillis();
	
	public Ps2EventStreamingConnection() {
		instance = this;
		initClientEndpoint();
		initConnectionFailCheck();
	}
	
	/**
	 * Opens the websocket.
	 */
	private void initClientEndpoint() {
		try {
			// open websocket
			clientEndPoint = new WebsocketClientEndpoint(new URI("wss://push.planetside2.com/streaming?environment=ps2&service-id=s:XouPs2"));
			
			clientEndPoint.addMessageHandler(message -> {
				lastMessageTime = System.currentTimeMillis();
				messageHandlers.forEach(it -> it.handleMessage(message));
				JSONObject response = new JSONObject(message);
				
				if (!response.has("payload")) {
					delegateNonPayloadedResponse(response);
				} else {
					JSONObject payload = response.getJSONObject("payload");
					delegatePayload(payload);
				}
			});
		} catch (URISyntaxException ex) {
			System.err.println("URISyntaxException exception: " + ex.getMessage());
		}
	}
	
	/**
	 * Starts a timer for monitoring when the last received message happened.
	 * Restarts the connection, if the message happened after a certain threshold.
	 */
	private void initConnectionFailCheck() {
		Timer connectionTimeout = new Timer("Ps connection timeout detector", true);
		connectionTimeout.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (System.currentTimeMillis() > lastMessageTime + CONNECTION_TIMEOUT && isAvailable.get()) {
					logger.network().warning("Event streaming connection unresponsive, reconnecting...");
					hasDisconnected.set(true);
					lastMessageTime = System.currentTimeMillis();
					initClientEndpoint();
					resubscribeAllEvents();
					hasDisconnected.set(false);
				}
			}
		}, 20_000, 20_000);
	}
	
	/**
	 * Used to delegate a payload to all subscribed events.
	 * Also Called by the backup polling service, if streaming service is unavailable.
	 *
	 * @param payload the event received.
	 */
	void delegatePayload(JSONObject payload) {
		if (!payload.getString("event_name").equals("PlayerLogout")) {
			logger.network().info(payload.toString());
		}
		globalListenerActions(payload);
		globalHandlers.forEach(it -> it.eventReceived(payload));
		subscribedEvents.get(payload.getString("event_name")).parallelStream().forEach(it -> it.eventReceived(payload));
		logger.network().fine("Affected: " + subscribedEvents.get(payload.getString("event_name")).size() + " Handlers");
	}
	
	private void globalListenerActions(JSONObject payload) {
		if (payload.has("character_id")) {
			if (payload.getString("character_id").equals(CurrentPlayer.getInstance().getPlayerID())) {
				
				if (payload.getString("event_name").equals("PlayerLogout")) {
					CurrentPlayer.getInstance().setZoneId(-1);
					return;
				}
				if (payload.has("zone_id")) {
					try {
						CurrentPlayer.getInstance().setZoneId(Integer.parseInt(payload.getString("zone_id")));
					} catch (NumberFormatException e) {
						CurrentPlayer.getInstance().setZoneId(-1);
					}
				}
			}
		}
	}
	
	public void resubscribeAllEvents() {
		logger.network().warning("Clearing subscriptions");
		clientEndPoint.sendMessage(
				"{" +
				"\"action\":\"clearSubscribe\"," +
				"\"all\":\"true\"," +
				"\"service\":\"event\"" +
				"}"
		);
		addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
			@Override
			public void handleMessage(String message) {
				JSONObject response = new JSONObject(message);
				if (response.has("subscription") && response.getJSONObject("subscription").getInt("characterCount") == 0) {
					removeMessageHandler(this);
					logger.network().warning("Subscriptions cleared");
					ArrayList<ArrayList<Ps2EventHandler>> eventsToSubscribe = new ArrayList<>(subscribedEvents.values());
					eventsToSubscribe.add(globalHandlers);
					subscribedEvents.clear();
					globalHandlers.clear();
					eventsIds.clear();
					eventsToSubscribe.forEach(it -> it.forEach(that -> {
						that.register(instance);
					}));
				}
			}
		});
	}
	
	private void addMessageHandler(WebsocketClientEndpoint.MessageHandler messageHandler) {
		messageHandlers.add(messageHandler);
	}
	
	public void removeMessageHandler(WebsocketClientEndpoint.MessageHandler messageHandler) {
		messageHandlers.remove(messageHandler);
	}
	
	/**
	 * Subscribe to a player centric event in the streaming API.
	 * SIDE EFFECT! SUBSCRIBING TO THE SAME EVENT NAME MULTIPLE TIMES, ONLY APPLIES TO THE FIRST PLAYER ID
	 *
	 * @param eventName the name of the event.
	 * @param playerId  the ID of the player.
	 * @param handler   the handler to receive the request.
	 */
	public void subscribePlayerEvent(String eventName, String playerId, Ps2EventHandler handler) {
		if (subscribedEvents.containsKey(eventName)) {
			if (!eventsIds.get(eventName).contains(playerId)) {
				subscribeNewPlayerEvent(eventName, playerId, handler);
			}
			if (!subscribedEvents.get(eventName).contains(handler)) {
				subscribedEvents.get(eventName).add(handler);
			}
		} else {
			subscribedEvents.put(eventName, new ArrayList<>(10));
			eventsIds.put(eventName, new ArrayList<>(10));
			
			subscribeNewPlayerEvent(eventName, playerId, handler);
		}
	}
	
	private void subscribeNewPlayerEvent(String eventName, String playerId, Ps2EventHandler handler) {
		eventsIds.get(eventName).add(playerId);
		subscribedEvents.get(eventName).add(handler);
		clientEndPoint.sendMessage(
				"{" +
				"\"service\":\"event\"," +
				"\"action\":\"subscribe\"," +
				"\"characters\":[\"" + playerId + "\"]," +
				"\"worlds\":[\"" + CurrentPlayer.getInstance().getWorld() + "\"]," +
				"\"eventNames\":[\"" + eventName + "\"]" +
				",\"logicalAndCharactersWithWorlds\":true" +
				"}"
		);
	}
	
	/**
	 * Subscribe to a world-centric event in the streaming API.
	 * SIDE EFFECT! SUBSCRIBING TO THE SAME EVENT NAME MULTIPLE TIMES, ONLY APPLIES TO THE FIRST WORLD ID
	 *
	 * @param eventName the name of the event.
	 * @param worldId   the id of the world. Limits received events to this world ID.
	 * @param handler   the handler to receive any events.
	 */
	public void subscribeWorldEvent(String eventName, String worldId, Ps2EventHandler handler) {
		if (subscribedEvents.containsKey(eventName)) {
			if (!eventsIds.get(eventName).contains(worldId)) {
				subscribeNewWorldEvent(eventName, worldId, handler);
			}
			if (!subscribedEvents.get(eventName).contains(handler)) {
				subscribedEvents.get(eventName).add(handler);
			}
		} else {
			subscribedEvents.put(eventName, new ArrayList<>(10));
			eventsIds.put(eventName, new ArrayList<>(10));
			subscribeNewWorldEvent(eventName, worldId, handler);
		}
	}
	
	private void subscribeNewWorldEvent(String eventName, String worldId, Ps2EventHandler handler) {
		eventsIds.get(eventName).add(worldId);
		subscribedEvents.get(eventName).add(handler);
		clientEndPoint.sendMessage(
				"{" +
				"\"service\":\"event\"," +
				"\"action\":\"subscribe\"," +
				"\"worlds\":[\"" + worldId + "\"]," +
				"\"eventNames\":[\"" + eventName + "\"]" +
				",\"logicalAndCharactersWithWorlds\":true" +
				"}"
		);
	}
	
	public void removeEvent(Ps2EventHandler ps2EventHandler) {
		subscribedEvents.values().forEach(it -> it.remove(ps2EventHandler));
	}
	
	public void registerGlobalEventListener(Ps2EventHandler globalHandler) {
		logger.effects().fine("Global handler added: " + globalHandlers.size());
		globalHandlers.remove(globalHandler);
		globalHandlers.add(globalHandler);
	}
	
	private void delegateNonPayloadedResponse(JSONObject response) {
		logger.network().info(response.toString());
		if (responseIsEventWithType(response)) {
			String eventType = response.getString("type");
			if (eventType.equals("serviceStateChanged")) {
				serverState(response);
			} else if (eventType.equals("heartbeat")) {
				heartbeat(response);
			}
		}
		
	}
	
	private boolean responseIsEventWithType(JSONObject response) {
		return response.has("service") && response.getString("service").equals("event") && response.has("type");
	}
	
	private void serverState(JSONObject response) {
		String playerWorldID = CurrentPlayer.getInstance().getValue("world_id");
		String responseWorld = response.getString("detail");
		String responseWorldID = responseWorld.substring(responseWorld.lastIndexOf("_") + 1);
		if (playerWorldID.equals(responseWorldID)) {
			serviceUnavailable(response.getString("online").equals("false"));
		}
	}
	
	private void serviceUnavailable(boolean isUnavailable) {
		isAvailable.set(!isUnavailable);
		if (isUnavailable) {
			logger.general().severe("Planetside streaming servers are offline. Event streaming is unavailable until servers are back. The census and keybindings will remain functional.");
			pollingService.start();
		} else {
			pollingService.stop();
		}
		
		
	}
	
	public JSONObject serializeToJSON() {
		JSONObject ret = new JSONObject();
		
		// Used for removing duplicates.
		HashMap<String, JSONObject> JSONEffects = new HashMap<>();
		HashMap<String, JSONObject> JSONEvents = new HashMap<>();
		HashMap<String, JSONObject> JSONConditions = new HashMap<>();
		
		JSONArray effects = new JSONArray();
		JSONArray events = new JSONArray();
		JSONArray conditions = new JSONArray();
		LinkedList<Ps2EventHandler> handlersToProcess = getAllContainedHandlersRecursively();
		
		for (Ps2EventHandler handler : handlersToProcess) {
			JSONEvents.put(handler.getName(), handler.toJson());
			addHandlerConditionToSet(JSONConditions, handler);
			addHandlerEffectToSet(JSONEffects, handler);
		}
		
		JSONEffects.values().forEach(effects::put);
		JSONEvents.values().forEach(events::put);
		JSONConditions.values().forEach(conditions::put);
		
		ret.put("effects", effects);
		ret.put("events", events);
		ret.put("conditions", conditions);
		return ret;
	}
	
	private LinkedList<Ps2EventHandler> getAllContainedHandlersRecursively() {
		LinkedList<Ps2EventHandler> current = new LinkedList<>();
		subscribedEvents.values().forEach(current::addAll);
		LinkedList<Ps2EventHandler> next = new LinkedList<>();
		LinkedList<Ps2EventHandler> handlersToProcess = new LinkedList<>();
		int cyclicalCheck = 0;
		
		while (!current.isEmpty()) {
			for (Ps2EventHandler handler : current) {
				if (handler instanceof MultiEventHandler) {
					next.addAll(((MultiEventHandler) handler).getAllHandlers());
				}
			}
			handlersToProcess.addAll(current);
			current.clear();
			current.addAll(next);
			next.clear();
			if (cyclicalCheck++ > 1000) {
				throw new Error("Cyclical references detected! It appears that a MultiEventHandler is referencing itself somehow");
			}
		}
		return handlersToProcess;
	}
	
	
	private void addHandlerConditionToSet(HashMap<String, JSONObject> map, Ps2EventHandler handler) {
		if (handler instanceof ConditionedEventHandler) {
			EventCondition condition = ((ConditionedEventHandler) handler).getCondition();
			addConditionToMap(map, condition);
		}
	}
	
	private void addConditionToMap(HashMap<String, JSONObject> map, EventCondition condition) {
		if (condition instanceof MultiCondition) {
			for (EventCondition containedCondition : ((MultiCondition) condition).getContainedConditions()) {
				addConditionToMap(map, containedCondition);
			}
		}
		map.put(condition.getName(), condition.toJson());
	}
	
	private void addHandlerEffectToSet(HashMap<String, JSONObject> map, Ps2EventHandler handler) {
		EffectProducer effect = handler.getEffect();
		if (effect != null) {
			addEffectToMap(map, effect);
		}
	}
	
	private void addEffectToMap(HashMap<String, JSONObject> map, EffectProducer effect) {
		if (effect instanceof MultiEffectProducer) {
			for (EffectProducer producer : ((MultiEffectProducer) effect).getContainedEffects()) {
				addEffectToMap(map, producer);
			}
		}
		map.put(effect.getName(), effect.toJson());
	}
	
	public ReadOnlyBooleanProperty hasDisconnectedProperty() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(hasDisconnected);
	}
	
	public ReadOnlyBooleanProperty isAvailableProperty() {
		return ReadOnlyBooleanProperty.readOnlyBooleanProperty(isAvailable);
	}
	
	private void heartbeat(JSONObject response) {
	
	}
}

//{"service":"event","action":"subscribe","worlds":["13"],"eventNames":["PlayerLogin"]}

/*

{
"service":"event",
"action":"subscribe",
"characters":["5428010618015189713"],
"eventNames":["Death"]
}

{
"service":"event",
"action":"subscribe",
"worlds":["1"],
"eventNames":["FacilityControl"]
}

 */