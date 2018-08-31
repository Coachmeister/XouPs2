package net.ximias.network;

import java.io.IOException;
import java.net.URI;
import net.ximias.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint
public class WebsocketClientEndpoint {
	
	private Session userSession = null;
	private MessageHandler messageHandler;
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public WebsocketClientEndpoint(URI endpointURI) {
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, endpointURI);
		} catch (Exception e) {
			
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Callback hook for Connection open events.
	 *
	 * @param userSession the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(Session userSession) {
		logger.network().info("Opening websocket");
		this.userSession = userSession;
	}
	
	/**
	 * Callback hook for Connection close events.
	 *
	 * @param userSession the userSession which is getting closed.
	 * @param reason the reason for connection close
	 */
	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		logger.network().info("Closing websocket");
		this.userSession = null;
	}
	
	/**
	 * Callback hook for Message Events. This method will be invoked when a client send a message.
	 *
	 * @param message The text message
	 */
	@OnMessage
	public void onMessage(String message) {
		if (this.messageHandler != null) {
			this.messageHandler.handleMessage(message);
		}
	}
	
	/**
	 * register message handler
	 *
	 * @param msgHandler
	 */
	public void addMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}
	
	/**
	 * Send a message.
	 *
	 * @param message
	 */
	public void sendMessage(String message) {
		this.userSession.getAsyncRemote().sendText(message);
	}
	
	public void sendBlockingMessage(String message){
		try {
			this.userSession.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Message handler.
	 *
	 * @author Jiji_Sasidharan
	 */
	public interface MessageHandler {
		
		void handleMessage(String message);
	}
}
