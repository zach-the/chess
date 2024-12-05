package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import static java.util.Objects.isNull;

@WebSocket
public class WebSocketFacade {
    DataAccess data;
    public WebSocketFacade(DataAccess data) { this.data = data; }

    private static HashMap<Session, ConnectionStruct> connections = new HashMap<>();

    @OnWebSocketConnect
    public void onConnect(Session session) throws Exception {}

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {}

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException, SQLException, DataAccessException {
//        System.out.println("Message from " + connections.get(session) + ": " + message);
        var cmd = new Gson().fromJson(message, UserGameCommand.class);
        switch (cmd.getCommandType()) {
            case CONNECT:
                connect(cmd.getAuthToken(), cmd.getGameID(), session);
                break;
            case MAKE_MOVE:
                var makeMove = new Gson().fromJson(message, MakeMoveStruct.class);
                System.out.println(makeMove.move());
                makeMove(makeMove.authToken(), makeMove.gameID(), makeMove.move(), session);
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
        System.out.println("Error for client " + connections.get(session) + ": " + error.getMessage());
    }

    private void broadcast(String username, String msg, int gameID, boolean notify) throws IOException {
        for (Map.Entry<Session, ConnectionStruct> entry : connections.entrySet()) {
            if (entry.getValue().gameID() == gameID) {
                if (notify || !entry.getValue().username().equals(username)) {
                    entry.getKey().getRemote().sendString(msg);
                }
            }
        }
    }

    private void connect(String authToken, int gameID, Session session) throws SQLException, DataAccessException, IOException {
        System.out.println("CONNECT");
//        System.out.println("authToken: " + authToken);
//        System.out.println("authToken: " + gameID);
        AuthData authData = this.data.getAuth(authToken);
        GameData gameData = this.data.getGame(gameID);

        try {
            connections.put(session, new ConnectionStruct(gameData.gameID(), authData.username()));
            String msg = new Gson().toJson(new LoadGameStruct(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game()));
            session.getRemote().sendString(msg);
            msg = new Gson().toJson(new NotificationStruct(ServerMessage.ServerMessageType.NOTIFICATION, authData.username() + " nice"));
            broadcast(authData.username(), msg, gameID, false);
        } catch (Exception e) {
            String msg;
            if(e.getMessage().contains("gameData")){
                msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: invalid gameID"));
            } else if (e.getMessage().contains("authData")) {
                msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: invalid authToken"));
            } else {
                msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: Unknown"));
            }
//            System.out.println(msg);
            session.getRemote().sendString(msg);
        }
    }

    private void makeMove(String authToken, int gameID, ChessMove move, Session session) throws SQLException, DataAccessException {
        System.out.println("MAKE MOVE");
        // figure out if move is valid
        AuthData authData = this.data.getAuth(authToken);
        GameData gameData = this.data.getGame(gameID);
        ChessGame game = gameData.game();
        try {
            // send updated game to server
            game.makeMove(move);
            GameData updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            this.data.updateGame(gameID, updatedGameData);
            // send load_game to all clients, with updated game
            String msg = new Gson().toJson(new LoadGameStruct(ServerMessage.ServerMessageType.LOAD_GAME, game));
            session.getRemote().sendString(msg);

        } catch (InvalidMoveException e) {
            System.out.println("INVALID MOVE");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // send load_game to all clients, with updated game
        // broadcast notification informing what move was made
        // if move results in check, checkmate or stalemate send a notification to all clients
    }


}