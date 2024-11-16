package ui;
import exception.ResponseException;
import model.LoginRequest;
import model.RegisterResponse;
import model.UserData;
import serverfacade.*;

import java.util.Arrays;
import java.util.Scanner;

public class PreloginUI {
    private final ServerFacade server;

    public PreloginUI(String serverURL) {
        server = new ServerFacade(serverURL);
    }

    public void repl() {
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while(!result.equals("quit")){
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                if (!result.equals("Logging Out...") && !result.equals("quit")) {
                    System.out.print(EscapeSequences.BLUE + result);
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(EscapeSequences.RED + msg + EscapeSequences.RESET);
            }
        }
        System.out.print("");
    }

    private void printPrompt() {
        System.out.print(EscapeSequences.RESET + EscapeSequences.GREEN + "[LOGGED OUT] >>> " + EscapeSequences.WHITE + EscapeSequences.RESET);
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> "quit";
                default -> help();
            };

        } catch (ResponseException e) {
            return e.getMessage();
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length==2) {
            LoginRequest loginRequest = new LoginRequest(params[0], params[1]);
            Object ret = null;
            try {
                ret = server.userLogin(loginRequest);
            } catch (ResponseException e) {
                if (e.getMessage().equals("failure: 401\n")){
                    return EscapeSequences.RED + "Incorrect password\n" + EscapeSequences.RESET;
                }
                return EscapeSequences.RED + "Cannot login\n" + EscapeSequences.RESET;
            }
            if (ret.getClass() == RegisterResponse.class) {
                System.out.println(EscapeSequences.GREEN + "Logged in as " + ((RegisterResponse) ret).username());
                return new LoggedinUI(server, ((RegisterResponse) ret).username(), ((RegisterResponse) ret).authToken()).repl();
            }
        }
        return EscapeSequences.RED + "This command requires 2 arguments\n" + EscapeSequences.RESET;
    }

    public String register(String... params) throws ResponseException {
        if (params.length==3) {
            UserData user = new UserData(params[0], params[1], params[2]);
            Object ret = null;
            try {
                ret = server.registerUser(user);
            } catch (ResponseException e) {
                if (e.getMessage().equals("failure: 403\n")){
                    return EscapeSequences.RED + "Cannot register. Username already taken\n" + EscapeSequences.RESET;
                }
                return EscapeSequences.RED + "Cannot register\n" + EscapeSequences.RESET;
            }
            if (ret != null) {
                RegisterResponse retInfo = (RegisterResponse) ret;
                System.out.println(EscapeSequences.GREEN + "Logged in as " + retInfo.username());
                return new LoggedinUI(server, retInfo.username(), retInfo.authToken()).repl();
            }
        }
        return EscapeSequences.RED + "This command requires 3 arguments\n" + EscapeSequences.RESET;
    }

    public String help() {
        return """
                   register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                   login <USERNAME> <PASSWORD> - to play chess
                   quit - playing chess
                   help - with possible commands
                """;
    }
}
