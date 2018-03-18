package net.ximias.network;

import net.ximias.psEvent.handler.GlobalHandler;
import net.ximias.psEvent.handler.Ps2EventHandler;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Connects to the event streaming part of the census API.
 * Events can be subscribed to by calling subscribePlayerEvent or subscribeWorldEvent.
 */
public class Ps2EventStreamingConnection {
	private HashMap<String, ArrayList<Ps2EventHandler>> subscribedEvents = new HashMap<>();
	private HashMap<String, ArrayList<String>> eventsIds = new HashMap<>();
	private ArrayList<Ps2EventHandler> globalHandlers = new ArrayList<>(12);
	private WebsocketClientEndpoint clientEndPoint;
	
	private ArrayList<Ps2EventHandler> playerEvents = new ArrayList<>(120);
	
	public Ps2EventStreamingConnection() {
		try {
			// open websocket
			clientEndPoint = new WebsocketClientEndpoint(new URI("wss://push.planetside2.com/streaming?environment=ps2&service-id=s:XouPs2"));
			
			// add debug
			clientEndPoint.addMessageHandler(System.out::println);
			clientEndPoint.addMessageHandler(message -> {
				JSONObject response = new JSONObject(message);
				System.out.println(response);
				if(!response.has("payload")) return;
				JSONObject payload = response.getJSONObject("payload");
				System.out.println(payload);
				globalListenerActions(payload);
				globalHandlers.forEach(it->it.eventReceived(payload));
				subscribedEvents.get(payload.getString("event_name")).parallelStream().forEach(it -> it.eventReceived(payload));
				System.out.println(subscribedEvents.get(payload.getString("event_name")).size()+" Handlers");
			});
		} catch (URISyntaxException ex) {
			System.err.println("URISyntaxException exception: " + ex.getMessage());
		}
	}
	
	private void globalListenerActions(JSONObject payload) {
		if (payload.has("character_id") && payload.has("zone_id")){
			try{
				if (payload.getString("character_id").equals(CurrentPlayer.getInstance().getPlayerID())){
					CurrentPlayer.getInstance().setZoneId(Integer.parseInt(payload.getString("zone_id")));
					if (payload.getString("event_name").equals("PlayerLogout")){
						CurrentPlayer.getInstance().setZoneId(-1);
					}
				}
			}catch (NumberFormatException e){
				CurrentPlayer.getInstance().setZoneId(-1);
			}
		}
	}
	
	/**
	 * Subscribe to a player centric event in the streaming API.
	 * SIDE EFFECT! SUBSCRIBING TO THE SAME EVENT NAME MULTIPLE TIMES, ONLY APPLIES TO THE FIRST PLAYER ID
	 * @param eventName the name of the event.
	 * @param playerId the ID of the player.
	 * @param handler the handler to receive the request.
	 */
	public void subscribePlayerEvent(String eventName, String playerId, Ps2EventHandler handler){
		if (subscribedEvents.containsKey(eventName)){
			if (!eventsIds.get(eventName).contains(playerId)){
				subscribeNewPlayerEvent(eventName, playerId, handler);
			}
			if (!subscribedEvents.get(eventName).contains(handler)){
				subscribedEvents.get(eventName).add(handler);
			}
		}else{
			subscribedEvents.put(eventName,new ArrayList<>(10));
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
						"\"characters\":[\""+playerId+"\"]," +
						"\"eventNames\":[\""+eventName+"\"]" +
						"}"
		);
	}
	/**
	 * Subscribe to a world-centric event in the streaming API.
	 * SIDE EFFECT! SUBSCRIBING TO THE SAME EVENT NAME MULTIPLE TIMES, ONLY APPLIES TO THE FIRST WORLD ID
 	 * @param eventName the name of the event.
	 * @param worldId the id of the world. Limits received events to this world ID.
	 * @param handler the handler to receive any events.
	 */
	public void subscribeWorldEvent(String eventName, String worldId, Ps2EventHandler handler){
		if (subscribedEvents.containsKey(eventName)){
			if (!eventsIds.get(eventName).contains(worldId)){
				subscribeNewWorldEvent(eventName, worldId, handler);
			}
			if (!subscribedEvents.get(eventName).contains(handler)){
				subscribedEvents.get(eventName).add(handler);
			}
		}else{
			subscribedEvents.put(eventName,new ArrayList<>(10));
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
						"\"worlds\":[\""+worldId+"\"]," +
						"\"eventNames\":[\""+eventName+"\"]" +
						"}"
		);
	}
	
	public void registerGlobalEventListener(Ps2EventHandler globalHandler) {
		System.out.println("Global added");
		globalHandlers.add(globalHandler);
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