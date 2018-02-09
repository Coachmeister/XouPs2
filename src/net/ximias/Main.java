package net.ximias;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		try {
			// open websocket
			final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("wss://push.planetside2.com/streaming?environment=ps2&service-id=s:XouPs2"));
			
			// add listener
			clientEndPoint.addMessageHandler(System.out::println);
			
			Scanner scanner = new Scanner(System.in);
			String line;
			do{
				line = scanner.nextLine();
				clientEndPoint.sendMessage(line);
			}while (!line.equals("exit"));
			
		} catch (URISyntaxException ex) {
			System.err.println("URISyntaxException exception: " + ex.getMessage());
		}
	}
}

/*

{"service":"event","action":"subscribe","worlds":["13"],"eventNames":["PlayerLogin"]}


 */