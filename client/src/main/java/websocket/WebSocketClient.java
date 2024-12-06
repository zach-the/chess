package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import ui.ChessBoardDisplay;
import ui.GameplayUI;
import websocket.messages.ServerMessage;

import javax.websocket.*;
import java.net.URI;

public class WebSocketClient extends Endpoint {
    private ChessGame currentGame;
    private Session session;
    private String username;
    private String auth;
    private int gameID;
    private ChessGame.TeamColor color;

    public WebSocketClient(String username, String auth, int gameID, ChessGame currentGame, ChessGame.TeamColor color) {
        this.username = username;
        this.auth = auth;
        this.gameID = gameID;
        this.color = color;
        this.currentGame = currentGame;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                handler(message);
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

    public void updateGame(ChessGame data) {
        currentGame = data;
    }

    public void redraw(ChessGame.TeamColor perspective) {
        ChessBoardDisplay.displayGame(currentGame, perspective);
    }

    private void handler(String message) {
        ServerMessage msg = new Gson().fromJson(message, ServerMessage.class);
        switch (msg.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameStruct loadGameStruct = new Gson().fromJson(message, LoadGameStruct.class);
                updateGame(loadGameStruct.game());
                System.out.println();
                ChessBoardDisplay.displayGame(currentGame, color);
                GameplayUI.printPrompt(username);
            case NOTIFICATION:
            case ERROR:
        }
    }
}


