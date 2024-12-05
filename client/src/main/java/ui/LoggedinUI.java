package ui;

import chess.ChessGame;
import exception.ResponseException;
import model.*;
import serverfacade.ServerFacade;
import ui.ChessBoardDisplay;
import websocket.WebSocketClient;

import javax.print.attribute.SupportedValuesAttribute;
import java.util.*;

public class LoggedinUI {
    private final ServerFacade server;
    private final String username;
    private final String auth;
    private Map<Integer, Integer> gameNumbers;

    public LoggedinUI(ServerFacade serverFacade, String user, String authToken) {
        this.server = serverFacade;
        this .username = user;
        this.auth = authToken;
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
        System.out.print(EscapeSequences.RESET + EscapeSequences.GREEN + '[' + username + "] >>> " + EscapeSequences.RESET + EscapeSequences.WHITE);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "create" -> create(params);
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };

        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String create(String... params) throws ResponseException {
        if (params.length==1) {
            var ret = server.createGame(params[0], auth);
            if (ret.getClass()== CreateGameResponse.class) {
                return "Game ID: " + "Created game with name: " + params[0] + "\n";
            }
        }
        else {
            return EscapeSequences.RED + "This command requires 1 argument: <NAME> \n" + EscapeSequences.RESET;
        }
        return "Failed to create game";
    }

    public String list() throws ResponseException {
        var ret = server.listGames(auth);
        if (ret.getClass()==GameList.class) {
            List<GameData> list = ((GameList)ret).games();
            String response = "";
            Map <Integer, Integer> nums = new HashMap<>();
            for (int i = 0; i < list.size(); i++) {
                GameData game = list.get(i);
                nums.put(i + 1, game.gameID());
                response = response.concat("Game " + (i+1) + "\n\tGameName: " + game.gameName() + "\n");
                response = response.concat("\tWhiteUser: " + game.whiteUsername() + "\n\tBlackUser: " + game.blackUsername() + "\n");
            }
            this.gameNumbers = nums;
           return response;
        }
        return EscapeSequences.RED + "Failed to list games\n" + EscapeSequences.RESET;
    }

    public String join(String... params) throws ResponseException {
        int gameID;
        try {
            gameNumbers.get(0);
        } catch (Exception e) {
            return EscapeSequences.RED + "You must call list before you can join a game\n" + EscapeSequences.RESET;
        }
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return EscapeSequences.RED + "You must input a game number to join\n" + EscapeSequences.RESET;
        }
        if (gameNumbers.get(gameID) == null) {
            return EscapeSequences.RED + "Invalid game number\n" + EscapeSequences.RESET;
        }
        if (params.length==2) {
            String color = params[1];
            if (!color.equals("black") && !color.equals("BLACK") && !color.equals("white") && !color.equals("WHITE")){
                return EscapeSequences.RED + "Color must be BLACK or WHITE\n" + EscapeSequences.RESET;
            }
            Object ret;
            try {
                ret = server.joinGame(params[0], color, auth);
            } catch (ResponseException e) {
                if (e.getMessage().equals("failure: 403\n")) {
                    return EscapeSequences.RED + "Cannot join game. Team taken\n" + EscapeSequences.RESET;
                }
                return EscapeSequences.RED + "Failed to join game\n" + EscapeSequences.RESET;
            }
            if (ret!=Collections.emptyMap()) {
                var tmp = server.listGames(auth);
                int gameNum = this.gameNumbers.get(Integer.parseInt(params[0])) - 1;
                List<GameData> list = ((GameList)tmp).games();
                ChessGame.TeamColor teamColor;
                if (color.equals("black") || color.equals("BLACK")) { teamColor = ChessGame.TeamColor.BLACK; }
                else { teamColor = ChessGame.TeamColor.WHITE; }
                switch(color) {
                    case "black":
                    case "BLACK":
                        ChessBoardDisplay.displayGame(list.get(gameNum), ChessGame.TeamColor.BLACK);
                        break;
                    case "white":
                    case "WHITE":
                        ChessBoardDisplay.displayGame(list.get(gameNum), ChessGame.TeamColor.WHITE);
                        break;
                }
                System.out.println(EscapeSequences.RESET + EscapeSequences.BLUE + "Joined game!\n");
                return new GameplayUI(this.username, this.auth, list.get(gameNum), teamColor, gameNum).repl();

            } else {
                return EscapeSequences.RED + "I think this game doesn't exist\n" + EscapeSequences.RESET;
            }
        } else {
            return EscapeSequences.RED + "This command requires 2 arguments: <ID> [WHITE|BLACK]\n" + EscapeSequences.RESET;
        }
    }

    public String observe(String... params) throws ResponseException {
        try {
            gameNumbers.get(0);
        } catch (Exception e) {
            return EscapeSequences.RED + "You must call list before you can join a game\n" + EscapeSequences.RESET;
        }
        if (params.length!=1) {
            return EscapeSequences.RED + "You must input a game number to observe\n" + EscapeSequences.RESET;
        }
        int gameID;
        try {
            gameID = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return EscapeSequences.RED + "You must input a game number to observe\n" + EscapeSequences.RESET;
        }
        if (gameNumbers.get(gameID) == null) {
            return EscapeSequences.RED + "Invalid game number\n" + EscapeSequences.RESET;
        }
        System.out.println("Observing game " + params[0]);
        var tmp = server.listGames(auth);
        List<GameData> list = ((GameList)tmp).games();
        System.out.println(EscapeSequences.RESET + EscapeSequences.BLUE + "Observing game!\n");
        int gameNum = this.gameNumbers.get(gameID) - 1;
        ChessBoardDisplay.displayGame(list.get(gameNum), ChessGame.TeamColor.WHITE);
        new GameplayUI(this.username, this.auth, list.get(gameNum), ChessGame.TeamColor.WHITE, gameNum).repl();
        return "this";
    }

    public String logout() throws ResponseException {
        if (server.userLogout(auth).equals(Collections.emptyMap())) {
            return "Logging Out...";
        }
        return EscapeSequences.RED + "Somehow we failed to log out\n" + EscapeSequences.RESET;
    }

    public String help() {
        System.out.print(EscapeSequences.BLUE);
        return """
                   create <NAME> - create a game
                   list - list games
                   join <ID> [WHITE|BLACK] - join game #ID as [WHITE|BLACK]
                   observe <ID> - observe game #ID
                   logout - logout of program
                   quit - quits program
                   help - display this help message
                """;
    }
}
