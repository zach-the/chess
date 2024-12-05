package ui;

import chess.ChessGame;
import exception.ResponseException;
import model.GameData;
import serverfacade.ServerFacade;
import websocket.WebSocketClient;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class GameplayUI {
    private final String username;
    private final String auth;
    private final WebSocketClient client = new WebSocketClient();
    private Map<Integer, Integer> gameNumbers;
    ChessGame.TeamColor perspective = null;

    public GameplayUI(String user, String authToken, GameData currentGameData, ChessGame.TeamColor perspective) {
        System.out.println("HERE");
        this.username = user;
        this.auth = authToken;
        this.perspective = perspective;
        client.updateGameData(currentGameData);
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://localhost:8081/ws";
            container.connectToServer(client, new URI(uri));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String repl() {
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while(!result.equals("quit") && !result.equals("Logging Out...")){
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(EscapeSequences.BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(EscapeSequences.RED + msg + EscapeSequences.RESET);
            }
        }
        System.out.println();
        return result;
    }

    private void printPrompt() {
        System.out.print(EscapeSequences.RESET + EscapeSequences.GREEN + '[' + username + "] >>> " + EscapeSequences.WHITE);
    }

    public String eval(String input) {
//        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "redraw" -> redraw(perspective);
//                case "leave" -> leave();
//                case "move" -> move(params);
//                case "resign" -> resign();
//                case "highlight" -> highlight();
                default -> help();
            };

//        }
//        catch (ResponseException e) {
//            return e.getMessage();
//        }
    }

    private String redraw(ChessGame.TeamColor perspective) {
        client.redraw(perspective);
        return "";
    }


    public String help() {
        System.out.print(EscapeSequences.BLUE);
        return """
                   redraw - redraw chess board
                   move <ROW (1-8)> <COL (1-8)> <ROW (1-8)> <COL (1-8)> - move from ROW, COL to ROW, COL
                   resign - forfeits the game
                   leave - leave the game
                   highlight <ROW (1-8)> <COL (1-8)>- highlight legal moves for a given piece
                   help - display this help dialog
                """;
    }
}
