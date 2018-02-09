package net.ximias;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Connects to the event streaming part of the census API.
 * Events can be subscribed to by calling subscribePlayerEvent or subscribeWorldEvent.
 */
public class Ps2EventStreamingConnection {
	private HashMap<String, ArrayList<Ps2EventHandler>> subscribedEvents = new HashMap<>();
	private WebsocketClientEndpoint clientEndPoint;
	
	private ArrayList<Ps2EventHandler> playerEvents = new ArrayList<>(120);
	
	public Ps2EventStreamingConnection() {
		try {
			// open websocket
			clientEndPoint = new WebsocketClientEndpoint(new URI("wss://push.planetside2.com/streaming?environment=ps2&service-id=s:XouPs2"));
			
			// add debug
			clientEndPoint.addMessageHandler(System.out::println);
			
		} catch (URISyntaxException ex) {
			System.err.println("URISyntaxException exception: " + ex.getMessage());
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
			subscribedEvents.get(eventName).add(handler);
		}else{
			subscribedEvents.put(eventName,new ArrayList<>(10));
			
			clientEndPoint.sendMessage(
					"{" +
					"\"service\":\"event\"," +
					"\"action\":\"subscribe\"," +
					"\"characters\":[\""+playerId+"\"]," +
					"\"eventNames\":[\""+eventName+"\"]" +
					"}"
			);
		}
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
			subscribedEvents.get(eventName).add(handler);
		}else{
			subscribedEvents.put(eventName,new ArrayList<>(10));
			
			clientEndPoint.sendMessage(
					"{" +
					"\"service\":\"event\"," +
					"\"action\":\"subscribe\"," +
					"\"worlds\":[\""+worldId+"\"]," +
					"\"eventNames\":[\""+eventName+"\"]" +
					"}"
			);
		}
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