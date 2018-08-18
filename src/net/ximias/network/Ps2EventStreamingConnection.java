package net.ximias.network;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.ximias.logging.Logger;
import net.ximias.psEvent.handler.Ps2EventHandler;
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
				messageHandlers.forEach(it->it.handleMessage(message));
				JSONObject response = new JSONObject(message);
				logger.network().info(response.toString());
				
				if (!response.has("payload")) {
					delegateNonPayloadedResponse(response);
				}else{
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
		Timer connectionTimeout = new Timer("Ps connection timeout detector",true);
		connectionTimeout.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (System.currentTimeMillis()> lastMessageTime + CONNECTION_TIMEOUT&&isAvailable.get()){
					logger.network().warning("Event streaming connection unresponsive, reconnecting...");
					hasDisconnected.set(true);
					lastMessageTime = System.currentTimeMillis();
					initClientEndpoint();
					resubscribeAllEvents();
					hasDisconnected.set(false);
				}
			}
		},20_000,20_000);
	}
	
	/**
	 * Used to delegate a payload to all subscribed events.
	 * Called by the backup polling service, if streaming service is unavailable.
	 * @param payload the event received.
	 */
	void delegatePayload(JSONObject payload) {
		logger.network().fine(payload.toString());
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
	
	public void resubscribeAllEvents(){
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
					ArrayList<ArrayList<Ps2EventHandler>> eventsToSubscribe = new ArrayList<>();
					eventsToSubscribe.addAll(subscribedEvents.values());
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
	
	public void removeMessageHandler(WebsocketClientEndpoint.MessageHandler messageHandler){
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
		subscribedEvents.values().forEach(it->it.remove(ps2EventHandler));
	}
	
	public void registerGlobalEventListener(Ps2EventHandler globalHandler) {
		logger.effects().fine("Global handler added: " + globalHandlers.size());
		globalHandlers.remove(globalHandler);
		globalHandlers.add(globalHandler);
	}
	
	private void delegateNonPayloadedResponse(JSONObject response) {
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
		String responseWorldID = responseWorld.substring(responseWorld.lastIndexOf("_")+1);
		if (playerWorldID.equals(responseWorldID)) {
			serviceUnavailable(response.getString("online").equals("false"));
		}
	}
	
	private void serviceUnavailable(boolean isUnavailable) {
		isAvailable.set(!isUnavailable);
		if (isUnavailable){
			logger.general().severe("Planetside streaming servers are offline. Event streaming is unavailable until servers are back. The census and keybindings will remain functional.");
			pollingService.start();
		}else {
			pollingService.stop();
		}
		
		
	}
	
	public ReadOnlyBooleanProperty hasDisconnectedProperty(){
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