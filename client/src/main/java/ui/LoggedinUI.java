package ui;

import exception.ResponseException;
import model.*;
import serverfacade.ServerFacade;
import ui.ChessBoardDisplay;

import javax.print.attribute.SupportedValuesAttribute;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class LoggedinUI {
    private final ServerFacade server;
    private final String username;
    private final String auth;

    public LoggedinUI(ServerFacade serverFacade, String user, String authToken) {
        this.server = serverFacade;
        this .username = user;
        this.auth = authToken;
    }

    public void repl() {
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
    }

    private void printPrompt() {
        System.out.print(EscapeSequences.RESET + EscapeSequences.GREEN + '[' + username + "] >>> " + EscapeSequences.WHITE);
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
            System.out.println("Creating game with name: " + params[0]);
            var ret = server.createGame(params[0], auth);
            if (ret.getClass()== CreateGameResponse.class) {
                return "Game ID: " + ((CreateGameResponse) ret).gameID() + "\n";
            }
        }
        else {
            return EscapeSequences.RED + "This command requires 1 argument\n" + EscapeSequences.RESET;
        }
        return "Failed to create game";
    }

    public String list() throws ResponseException {
        var ret = server.listGames(auth);
        if (ret.getClass()==GameList.class) {
            List<GameData> list = ((GameList)ret).games();
            String response = "";
            for (int i = 0; i < list.size(); i++) {
                GameData game = list.get(i);
                response = response.concat("Game " + (i+1) + "\n\tGameName: " + game.gameName() + "\n");
                response = response.concat("\tWhiteUser: " + game.whiteUsername() + "\n\tBlackUser: " + game.blackUsername() + "\n");
            }
           return response;
        }
        return EscapeSequences.RED + "Failed to list games\n" + EscapeSequences.RESET;
    }

    public String join(String... params) throws ResponseException {
        if (params.length==2) {
            var ret = server.joinGame(params[0], params[1], auth);
            System.out.println(ret.getClass());
            if (ret!=Collections.emptyMap()) {
                var tmp = server.listGames(auth);
                List<GameData> list = ((GameList)tmp).games();
                ChessBoardDisplay.displayGame(list.get(Integer.parseInt(params[0]) - 1));
                return EscapeSequences.RESET + EscapeSequences.BLUE + "Joined game!" + "\n";
            } else {
                return EscapeSequences.RED + "I think this game doesn't exist\n" + EscapeSequences.RESET;

            }
        } else {
            return EscapeSequences.RED + "This command requires 2 arguments\n" + EscapeSequences.RESET;
        }
    }

    public String observe(String... params) throws ResponseException {
//        if (params.length==1) {
//            var ret = server.joinGame(Integer.toString((Integer.parseInt(params[0]) - 1)), auth);
//            if (ret==Collections.emptyMap()) {
//                return "Observing game!";
//            }
//        }
        return "Failed to observe game\n";
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
                   create <NAME> - a game
                   list - games
                   join <ID> [WHITE|BLACK] - a game
                   observe <ID> - a game
                   logout - when you are done
                   quit - playing chess
                   help - with possible commands
                """;
    }
}
