package service;

import chess.ChessGame;
import model.*;

import dataaccess.DataAccess;

import java.util.Collections;
import java.util.UUID;


public class Service {
    private final DataAccess dataAccess;
    int gameCount = 0;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public Object registerUser(UserData user) {
        UserData result = this.dataAccess.getUser(user.username());
        if (user.username() == null || user.password() == null || user.email() == null) {
            return new ErrorResponse("Error: bad request");
        } else if (result != null && result.username().equals(user.username())) {
            return new ErrorResponse("Error: already taken");
        } else {
            this.dataAccess.addUser(user);
            AuthData auth = new AuthData(user.username(), UUID.randomUUID().toString());
            this.dataAccess.addAuth(auth);
            return auth;
        }
    }

    public Object userLogin(LoginRequest loginRequest) {
        UserData user = dataAccess.getUser(loginRequest.username());
        if (user != null) {
            if (user.password().equals(loginRequest.password())){
                String auth = UUID.randomUUID().toString();
                AuthData authData = new AuthData(loginRequest.username(), auth);
                dataAccess.addAuth(authData);
                return new RegisterResponse(loginRequest.username(), auth);
            }
        }
        return new ErrorResponse("Error: unauthorized");
    }

    public Object userLogout(String authToken) {
        AuthData auth = this.dataAccess.getAuth(authToken);
        if (auth == null || !auth.authToken().equals(authToken)) {
            return new ErrorResponse("Error: unauthorized");
        } else {
            this.dataAccess.deleteAuth(authToken);
            return Collections.emptyMap();
        }
    }

    public Object createGame(CreateGameRequest gameRequest) {
        AuthData auth = this.dataAccess.getAuth(gameRequest.authToken());
        if (auth == null) {
            return new ErrorResponse("Error: unauthorized");
        }
        GameData newGame = new GameData(++gameCount, null, null, gameRequest.gameName(), new ChessGame());
        this.dataAccess.createGame(gameCount, newGame);
        return new CreateGameResponse(gameCount);
    }

    public Object clearDB() {
        dataAccess.deleteUserData();
        dataAccess.deleteAuthData();
        dataAccess.deleteGameData();
        return Collections.emptyMap();
    }

    public Object joinGame(JoinGameReqeust joinGameReqeust){
        AuthData auth = this.dataAccess.getAuth(joinGameReqeust.authToken());
        this.dataAccess.printAllAuths();
        System.out.println("My authtoken: " + joinGameReqeust.authToken());
        if (auth == null) {
            System.out.println("this happened");
            return new ErrorResponse("Error: unauthorized");
        }
        GameData game = this.dataAccess.getGame(joinGameReqeust.gameID());
        if (game == null || joinGameReqeust.playerColor() == null || joinGameReqeust.gameID() == null) {
            return new ErrorResponse("Error: bad request");
        }
        if (joinGameReqeust.playerColor().equals("WHITE")) {
            if (game.whiteUsername() == null) {
                GameData newGame = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
                this.dataAccess.updateGame(joinGameReqeust.gameID(), newGame);
            } else {
                return new ErrorResponse("Error: already taken");
            }
        } else if (joinGameReqeust.playerColor().equals("BLACK")){
            if (game.blackUsername() == null) {
                GameData newGame = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
                this.dataAccess.updateGame(joinGameReqeust.gameID(), newGame);
            } else {
                return new ErrorResponse("Error: already taken");
            }
        }
        return Collections.emptyMap();
    }

    public Object listGames(String authToken) {
        AuthData auth = this.dataAccess.getAuth(authToken);
        if (auth == null) {
            return new ErrorResponse("Error: unauthorized");
        }
        return this.dataAccess.listGames();
    }

}
