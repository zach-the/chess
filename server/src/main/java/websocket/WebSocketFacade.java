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
    public void onClose(Session session, int statusCode, String reason) {
        connections.remove(session);
    }

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
                leave(cmd.getAuthToken(), cmd.getGameID(), session);
                break;
            case RESIGN:
                resign(cmd.getAuthToken(), cmd.getGameID(), session);
                break;
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.out.println("Error for client " + connections.get(session) + ": " + error.getMessage());
    }

    private void stupidExceptionDuplicate(Exception e, Session session) throws IOException {
        String msg;
        if (e.getMessage().contains("gameData")) {
            System.out.println("gameData error");
            System.out.println(e.getMessage());
            msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: invalid gameID"));
            session.getRemote().sendString(msg);
        } else if (e.getMessage().contains("authData")) {
            System.out.println("authData error");
            msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: invalid authToken"));
            session.getRemote().sendString(msg);
        } else {
            System.out.println(e.getMessage());
        }
    }

    private void broadcast(String username, String msg, int gameID, boolean notifyAll) throws IOException {
        for (Map.Entry<Session, ConnectionStruct> entry : connections.entrySet()) {
            if (entry.getValue().gameID() == gameID) {
                if (notifyAll || !entry.getValue().username().equals(username)) {
                    try {
                        entry.getKey().getRemote().sendString(msg);
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
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
                System.out.println("gameData error");
                System.out.println(e.getMessage());
                msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: invalid gameID"));
                session.getRemote().sendString(msg);
            } else if (e.getMessage().contains("authData")) {
                System.out.println("authData error");
                msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: invalid authToken"));
                session.getRemote().sendString(msg);
            }
            else {
                System.out.println(e.getMessage());
            }
//                // MY PROBLEM IS HERE
//                msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: Unknown"));
//            }
//            System.out.println(msg);
//            session.getRemote().sendString(msg);
        }
    }

    private void makeMove(String authToken, int gameID, ChessMove move, Session session) throws SQLException, DataAccessException, IOException {
        System.out.println("MAKE MOVE");
        // figure out if move is valid
        AuthData authData = this.data.getAuth(authToken);
        GameData gameData = this.data.getGame(gameID);
        ChessGame game = gameData.game();
        ChessGame.TeamColor color = game.getBoard().getPiece(move.getStartPosition()).getTeamColor();
        try {
            if ((gameData.blackUsername() == null || gameData.whiteUsername() == null) || (!authData.username().equals(gameData.blackUsername()) && !authData.username().equals(gameData.whiteUsername()))){
                throw new InvalidMoveException("Cannot move if not player");
            }
            // send updated game to server
            game.makeMove(move);
            GameData updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game, gameData.finished());
            this.data.updateGame(gameID, updatedGameData);
            // send load_game to all clients, with updated game
            if ((gameData.whiteUsername().equals(authData.username()) && color.equals(ChessGame.TeamColor.BLACK)) || (gameData.blackUsername().equals(authData.username()) && color.equals(ChessGame.TeamColor.WHITE)) ) {
                throw new InvalidMoveException("Cannot move opposite team's pieces");
            }
            String msg = new Gson().toJson(new LoadGameStruct(ServerMessage.ServerMessageType.LOAD_GAME, game));
            broadcast(authData.username(), msg, gameID, true);
            // broadcast notification informing what move was made
            msg = new Gson().toJson(new NotificationStruct(ServerMessage.ServerMessageType.NOTIFICATION, "Move was made"));
            broadcast(authData.username(), msg, gameID, false);
            // if move results in check, checkmate or stalemate send a notification to all clients
            color = (color == ChessGame.TeamColor.BLACK) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            String teamColor = (color == ChessGame.TeamColor.BLACK) ? "black" : "white";
            boolean event = false;
            if (game.isInCheck(color)) {
                event = true;
                msg = new Gson().toJson(new NotificationStruct(ServerMessage.ServerMessageType.NOTIFICATION, "Move puts " + teamColor + " in check"));
            } else if (game.isInStalemate(color)) {
                event = true;
                msg = new Gson().toJson(new NotificationStruct(ServerMessage.ServerMessageType.NOTIFICATION, "Move puts " + teamColor + " in stalemate"));
                updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game, true);
                this.data.updateGame(gameID, updatedGameData);
            } else if (game.isInCheckmate(color)) {
                event = true;
                msg = new Gson().toJson(new NotificationStruct(ServerMessage.ServerMessageType.NOTIFICATION, "Move puts " + teamColor + " in checkmate"));
                updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game, true);
                this.data.updateGame(gameID, updatedGameData);
            }
            if (event) {
                broadcast(authData.username(), msg, gameID, true);
            }
        } catch (InvalidMoveException e) {
            if (e.getMessage().contains("out of")) {
                String msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: cannot move out of turn"));
                session.getRemote().sendString(msg);
            } else if (e.getMessage().contains("opposite")) {
                String msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: cannot move opposite team's pieces"));
                session.getRemote().sendString(msg);
            } else if (e.getMessage().contains("observer")) {
                String msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, e.getMessage()));
                session.getRemote().sendString(msg);
            } else {
                String msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: invalid move"));
                session.getRemote().sendString(msg);
            }
            System.out.println("INVALID MOVE");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            stupidExceptionDuplicate(e, session);
        }
    }

    private void resign(String authToken, int gameID, Session session) throws SQLException, DataAccessException, IOException {


        // STILL NEED TO FIGURE OUT HOW TO MARK THE GAME AS 'FINISHED' - LIKELY WILL INVOLVE CHANGING GAMEDATA TO HAVE A 'FINISHED' BOOLEAN VALUE


        System.out.println("RESIGN");
        AuthData authData = this.data.getAuth(authToken);
        GameData gameData = this.data.getGame(gameID);
        if (!authToken.equals(authData.authToken())) {
            String msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: invalid authToken"));
            session.getRemote().sendString(msg);
            return;
        } else if (gameData.finished()) {
            String msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: cannot resign if game is over"));
            session.getRemote().sendString(msg);
            return;
        }
        try {
            String username = authData.username();
            if (username.equals(gameData.blackUsername())) {
                GameData updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game(), true);
                this.data.updateGame(gameID, updatedGameData);
                String msg = new Gson().toJson(new NotificationStruct(ServerMessage.ServerMessageType.NOTIFICATION, username.concat(" has resigned from the game")));
                broadcast(username, msg, gameID, true);
                connections.remove(session);
            } else if (username.equals(gameData.whiteUsername())) {
                GameData updatedGameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game(), true);
                this.data.updateGame(gameID, updatedGameData);
                String msg = new Gson().toJson(new NotificationStruct(ServerMessage.ServerMessageType.NOTIFICATION, username.concat(" has resigned from the game")));
                broadcast(username, msg, gameID, true);
                connections.remove(session);
            } else {
                String msg = new Gson().toJson(new ErrorStruct(ServerMessage.ServerMessageType.ERROR, "Error: cannot resign if you are not playing"));
                session.getRemote().sendString(msg);
            }
        } catch (Exception e) {
            stupidExceptionDuplicate(e, session);
        }
    }

    private void leave(String authToken, int gameID, Session session) throws IOException, SQLException, DataAccessException {
        System.out.println("LEAVE");
        AuthData authData = this.data.getAuth(authToken);
        GameData gameData = this.data.getGame(gameID);
        try {
            String username = authData.username();
            System.out.println(gameData.blackUsername());

            if (username.equals(gameData.blackUsername())) {
                System.out.println("\tblack");
                GameData updatedGameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game(), gameData.finished());
                this.data.updateGame(gameID, updatedGameData);
            } else if (username.equals(gameData.whiteUsername())) {
                System.out.println("\twhite");
                GameData updatedGameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game(), gameData.finished());
                this.data.updateGame(gameID, updatedGameData);
            }
            String msg = new Gson().toJson(new NotificationStruct(ServerMessage.ServerMessageType.NOTIFICATION, username.concat(" has left the game")));
            broadcast(username, msg, gameID, false);
            connections.remove(session);
        } catch (Exception e) {
            stupidExceptionDuplicate(e, session);
        }
    }

}