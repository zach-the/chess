package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import serverfacade.ServerFacade;
import websocket.MakeMoveStruct;
import websocket.WebSocketClient;
import websocket.commands.UserGameCommand;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;

public class GameplayUI {
    private final String username;
    private final String auth;
    private final WebSocketClient client;
    private final int gameID;
    private Map<Integer, Integer> gameNumbers;
    ChessGame.TeamColor perspective = null;
    boolean player;

    public GameplayUI(String user, String authToken, GameData currentGameData, ChessGame.TeamColor perspective, int gameID, boolean player) {
        this.username = user;
        this.auth = authToken;
        this.perspective = perspective;
        this.gameID = gameID;
        this.client = new WebSocketClient(username, auth, gameID, currentGameData.game(), perspective);
        this.player = player;


        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://localhost:8081/ws";
            container.connectToServer(client, new URI(uri));
        } catch (Exception e) {
            System.out.println("Something went wrong in the GameplayUI init");
            System.out.println(e.getMessage());
        }
        client.sendMessage(new Gson().toJson(new UserGameCommand(UserGameCommand.CommandType.CONNECT, auth, gameID)));
    }

    public String repl() {
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while(!result.equals("Leaving Game...")){
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(EscapeSequences.BLUE + result);
                if (result.equals("Leaving Game...")) { break; }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(EscapeSequences.RED + msg + EscapeSequences.RESET);
            }
            printPrompt(username);
        }
        System.out.println();
        return EscapeSequences.BLUE + result + EscapeSequences.RESET;
    }

    public static void printPrompt(String username) {
        System.out.print(EscapeSequences.RESET + EscapeSequences.GREEN + '[' + username + "] >>> " + EscapeSequences.WHITE);
    }

    public String eval(String input) {
//        try {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "redraw" -> redraw(perspective);
            case "leave" -> leave();
            case "move" -> move(params);
            case "resign" -> resign();
//                case "highlight" -> highlight(params);
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

    private String leave() {
        client.sendMessage(new Gson().toJson(new UserGameCommand(UserGameCommand.CommandType.LEAVE, auth, gameID)));
        return "Leaving Game...";
    }

    private String resign() {
        client.sendMessage(new Gson().toJson(new UserGameCommand(UserGameCommand.CommandType.RESIGN, auth, gameID)));
        return "";
    }

    int verifySize(int in) throws Exception {
        System.out.println(in);
        if (in < 9 && in > 0) return in;
        else throw new Exception("bad");
    }

    int handleChar(String in) throws Exception {
        switch (in) {
            case "a" -> { return 1; }
            case "b" -> { return 2; }
            case "c" -> { return 3; }
            case "d" -> { return 4; }
            case "e" -> { return 5; }
            case "f" -> { return 6; }
            case "g" -> { return 7; }
            case "h" -> { return 8; }
            default -> throw new Exception("really bad");
        }
    }

    private String move(String... params) {
        if (!player) {
            return EscapeSequences.RED + "You cannot make moves as an observer\n" + EscapeSequences.RESET;
        }
        if (params.length == 4) {
            ChessPosition startPosition, endPosition;
            try {
                startPosition = new ChessPosition(verifySize(Integer.parseInt(params[1])), handleChar(params[0]));
                endPosition = new ChessPosition(verifySize(Integer.parseInt(params[3])), handleChar(params[2]));
            } catch (Exception e) {
                return EscapeSequences.RED + "the first four inputs must be integers between 1 and 8, and letters between a and h\n" + EscapeSequences.RED;
            }
            ChessMove move = new ChessMove(startPosition, endPosition, null);
            client.sendMessage(new Gson().toJson(new MakeMoveStruct("MAKE_MOVE", auth, gameID, move)));
        } else if (params.length == 5) {
            return EscapeSequences.RED + "I have yet to implement promotions\n" + EscapeSequences.RESET;
        } else {
            return EscapeSequences.RED + "Move requires 4 or 5 inputs: use 'help' for more\n" + EscapeSequences.RESET;
        }




        return "";
    }


    public String help() {
        System.out.print(EscapeSequences.BLUE);
        return """
                   redraw - redraw chess board
                   move <COL> <ROW> <COL> <ROW> <PROMOTIONPIECE>- move from ROW, COL to ROW, COL
                        <COL> must be between a and h
                        <ROW> must be between 1 and 8
                        can become <PROMOTIONPIECE> if pawn is eligible for promotion
                   resign - forfeits the game
                   leave - leave the game
                   highlight <ROW (1-8)> <COL (1-8)>- highlight legal moves for a given piece
                   help - display this help dialog
                """;
    }
}

