package websocket;

import chess.ChessGame;
import chess.ChessPosition;
import com.google.gson.Gson;
import model.GameData;
import ui.ChessBoardDisplay;
import ui.EscapeSequences;
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
                for (int i = 0; i < username.length(); i++) {
                    System.out.print("\b");
                }
                System.out.println("\b\b\b\b\b\b\b");
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

    public void highlight(ChessGame.TeamColor perspective, ChessPosition highlightThis) {
        if (currentGame.validMoves(highlightThis) != null)
            ChessBoardDisplay.highlightGame(currentGame, perspective, highlightThis);
        else {
            System.out.println(EscapeSequences.RED + "You must select a space with a piece on it" + EscapeSequences.RESET);
        }
        GameplayUI.printPrompt(username);
    }

    private void handler(String message) {
        ServerMessage msg = new Gson().fromJson(message, ServerMessage.class);
        switch (msg.getServerMessageType()) {
            case LOAD_GAME:
                LoadGameStruct loadGameStruct = new Gson().fromJson(message, LoadGameStruct.class);
                updateGame(loadGameStruct.game());
                ChessBoardDisplay.displayGame(currentGame, color);
                GameplayUI.printPrompt(username);
                break;
            case NOTIFICATION:
                NotificationStruct notificationStruct = new Gson().fromJson(message, NotificationStruct.class);
                System.out.println(EscapeSequences.PURPLE + notificationStruct.message() + EscapeSequences.RESET);
                GameplayUI.printPrompt(username);
                break;
            case ERROR:
                ErrorStruct errorStruct = new Gson().fromJson(message, ErrorStruct.class);
                System.out.println(EscapeSequences.RED + errorStruct.errorMessage() + EscapeSequences.RESET);
                GameplayUI.printPrompt(username);
                break;
            default:
                System.out.println(EscapeSequences.RED + "Something went wrong" + EscapeSequences.RESET);
        }
    }
}


