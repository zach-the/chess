package websocket;

import javax.websocket.*;
import java.net.URI;

public class ChatClient extends Endpoint {
    private Session session;

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        System.out.println("Connected to server");

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                System.out.println("Received message: " + message);
            }
        });
    }

    public void sendMessage(String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://localhost:8080/chat";
            ChatClient client = new ChatClient();
            container.connectToServer(client, new URI(uri));
            Thread.sleep(1000);
            // Example: Send a message
            client.sendMessage("Hello, World!");

            // Keep the program running to receive messages
            Thread.sleep(60000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

