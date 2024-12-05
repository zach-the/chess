package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketFacade {
    DataAccess data;
    public WebSocketFacade(DataAccess data) { this.data = data; }

    private static Map<Session, String> clients = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {
        System.out.println("New client connected: " + session.getRemoteAddress().getAddress());
        clients.put(session, session.getRemoteAddress().getAddress().toString());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Client disconnected: " + clients.get(session));
        clients.remove(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, SQLException, DataAccessException {
        System.out.println("Message from " + clients.get(session) + ": " + message);
        var cmd = new Gson().fromJson(message, UserGameCommand.class);
        switch (cmd.getCommandType()) {
            case CONNECT:
                connect(cmd.getAuthToken(), cmd.getGameID());
                break;
            case MAKE_MOVE:
                System.out.println("MAKE MOVE");
                break;
            case LEAVE:
                System.out.println("LEAVE");
                break;
            case RESIGN:
                System.out.println("RESIGN");
                break;
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("Error for client " + clients.get(session) + ": " + error.getMessage());
    }

    private void connect(String authToken, int gameID) throws SQLException, DataAccessException {
        System.out.println("CONNECT");
        System.out.println("authToken: " + authToken);
        System.out.println("authToken: " + gameID);
        AuthData authData = this.data.getAuth(authToken);

    }
}