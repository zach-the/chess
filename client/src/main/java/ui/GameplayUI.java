package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
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
                if (!result.equals("noPrint")) System.out.print(EscapeSequences.BLUE + result);
                if (result.equals("Leaving Game...")) { break; }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(EscapeSequences.RED + msg + EscapeSequences.RESET);
            }
            if (!result.equals("noPrint")) printPrompt(username);
        }
        System.out.println();
        return EscapeSequences.BLUE + result + EscapeSequences.RESET + EscapeSequences.WHITE;
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
            case "highlight" -> highlight(params);
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
        if (in < 9 && in > 0) return in;
        else throw new Exception("bad");
    }

    int handleChar(char in) throws Exception {
        in = Character.toLowerCase(in);
        switch (in) {
            case 'a' -> { return 1; }
            case 'b' -> { return 2; }
            case 'c' -> { return 3; }
            case 'd' -> { return 4; }
            case 'e' -> { return 5; }
            case 'f' -> { return 6; }
            case 'g' -> { return 7; }
            case 'h' -> { return 8; }
            default -> throw new Exception("really bad");
        }
    }

    private ChessPiece.PieceType handlePromotion(String inString) throws Exception {
//        inString = toLowerCase(inString);
        return switch (inString) {
            case "queen" -> ChessPiece.PieceType.QUEEN;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "pawn" -> ChessPiece.PieceType.PAWN;
            default -> throw new Exception("Invalid piece");
        };
    }

    private String move(String... params) {
        if (!player) {
            return EscapeSequences.RED + "You cannot make moves as an observer\n" + EscapeSequences.RESET;
        }
        if (params.length == 2 || params.length == 3) {
            ChessPosition startPosition, endPosition;
            try {
                startPosition = new ChessPosition(verifySize(params[0].charAt(1)-'0'), handleChar(params[0].charAt(0)));
                endPosition = new ChessPosition(verifySize(params[1].charAt(1)-'0'), handleChar(params[1].charAt(0)));
            } catch (Exception e) {
                return EscapeSequences.RED + "the first two inputs must be coordinates as follows: colRow. example: c4 c5\n" + EscapeSequences.RED;
            }
            ChessPiece.PieceType promotion = null;
            if (params.length == 3) {
                try {
                    promotion = handlePromotion(params[2]);
                } catch (Exception e) {
                    return EscapeSequences.RED + "Invalid promotion piece\n" + EscapeSequences.RESET;
                }
            }
            ChessMove move = new ChessMove(startPosition, endPosition, promotion);
            client.sendMessage(new Gson().toJson(new MakeMoveStruct("MAKE_MOVE", auth, gameID, move)));
            return "noPrint";
        } else {
            return EscapeSequences.RED + "Move requires 2 or 3 inputs: use 'help' for more\n" + EscapeSequences.RESET;
        }
    }

    private String highlight(String... params) {
        if (params.length == 1) {
            ChessPosition highlightThis;
            try {
                highlightThis = new ChessPosition(verifySize(params[0].charAt(1)-'0'), handleChar(params[0].charAt(0)));
            } catch (Exception e) {
                return EscapeSequences.RED + "the input coordinate must be as follows: colRow. example: c4\n" + EscapeSequences.RESET;
            }
            client.highlight(perspective, highlightThis);
            return "noPrint";
        } else {
            return EscapeSequences.RED + "Highlight requires 1 coordinate input: use 'help' for more\n" + EscapeSequences.RESET;
        }
    }


    public String help() {
        System.out.print(EscapeSequences.BLUE);
        return """
                    redraw - redraw chess board
                    resign - forfeits the game
                    leave - leave the game
                    highlight <POSITION>- highlight legal moves for a given piece
                        example:
                            highlight c3
                    move <START> <FINISH> <PROMOTION PIECE> - move from START coordinate to FINISH coordinate,
                        can become <PROMOTION PIECE> if pawn is eligible for promotion
                        coordinates are arranged as follows: COLROW
                        COL must be a single character between a and h
                        ROW must be a single digit between 1 and 8
                        examples:
                            move c4 b5
                            move b7 b8 knight
                    help - display this help dialog
                """;
    }
}

