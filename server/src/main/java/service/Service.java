package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import exception.ResponseException;
import model.*;

import dataaccess.DataAccess;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Collections;
import java.util.UUID;


public class Service {
    private final DataAccess dataAccess;
    int gameCount = 0;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }


    public Object registerUser(UserData user) throws ResponseException, DataAccessException {
        try {
            UserData result = this.dataAccess.getUser(user.username());
            if (user.username() == null || user.password() == null || user.email() == null) {
                return new ErrorResponse("Error: bad request");
            } else if (result != null && result.username().equals(user.username())) {
                System.out.println("THIS HAPPENED: " + result.username() + " " + user.username());
                return new ErrorResponse("Error: already taken");
            } else {
                UserData hashedUser = new UserData(user.username(), BCrypt.hashpw(user.password(), BCrypt.gensalt()), user.email());
                this.dataAccess.addUser(hashedUser);
                String auth = UUID.randomUUID().toString();
                AuthData authData = new AuthData(user.username(), auth);
                this.dataAccess.addAuth(authData);
                return new RegisterResponse(user.username(), auth);
            }
        }
        catch (DataAccessException e) {
            if (e.toString().contains("Duplicate entry")) {
                return new ErrorResponse("Error: already taken");
            }
            else {
                throw new DataAccessException(e.toString());
            }
        }

    }

    public Object userLogin(LoginRequest loginRequest) throws DataAccessException {
        UserData user = dataAccess.getUser(loginRequest.username());
        if (user != null) {
            if (BCrypt.checkpw(loginRequest.password(), user.password())){
                String auth = UUID.randomUUID().toString();
                AuthData authData = new AuthData(loginRequest.username(), auth);
                dataAccess.addAuth(authData);
                return new RegisterResponse(loginRequest.username(), auth);
            }
        }
        return new ErrorResponse("Error: unauthorized");
    }

    public Object userLogout(String authToken) throws DataAccessException {
        AuthData auth = null;
        try {
            auth = this.dataAccess.getAuth(authToken);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        if (auth == null || !auth.authToken().equals(authToken)) {
            return new ErrorResponse("Error: unauthorized");
        } else {
            this.dataAccess.deleteAuth(authToken);
            return Collections.emptyMap();
        }
    }

    public Object createGame(CreateGameRequest gameRequest) throws DataAccessException {
        AuthData auth = null;
        try {
            auth = this.dataAccess.getAuth(gameRequest.authToken());
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        if (auth == null) {
            return new ErrorResponse("Error: unauthorized");
        }
        GameData newGame = new GameData(++gameCount, null, null, gameRequest.gameName(), new ChessGame(), false);
        this.dataAccess.createGame(gameCount, newGame);
        return new CreateGameResponse(gameCount);
    }

    public Object joinGame(JoinGameReqeust joinGameRequest) throws DataAccessException {
        System.out.println("from Service: joinGameRequest: " + joinGameRequest);
        AuthData auth = null;
        try {
            auth = this.dataAccess.getAuth(joinGameRequest.authToken());
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        if (auth == null || !auth.authToken().equals(joinGameRequest.authToken())) {
            return new ErrorResponse("Error: unauthorized");
        }
        System.out.println(listGames(auth.authToken()));
        GameData game = this.dataAccess.getGame(joinGameRequest.gameID());
        if (game == null || joinGameRequest.playerColor() == null || joinGameRequest.gameID() == null) {
            if (game != null) System.out.println(game); else System.out.println("game was null");
            if (joinGameRequest.playerColor() != null) System.out.println(joinGameRequest.playerColor()); else System.out.println("color was null");
            if (joinGameRequest.gameID() != null) System.out.println(joinGameRequest.gameID()); else System.out.println("id was null");
            return new ErrorResponse("Error: bad request");
        }
        if (joinGameRequest.playerColor().equals("WHITE") || joinGameRequest.playerColor().equals("white")) {
            if (game.whiteUsername() == null || game.whiteUsername().equals("null")) {
                GameData newGame = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game(), false);
                this.dataAccess.updateGame(joinGameRequest.gameID(), newGame);
            } else {
                return new ErrorResponse("Error: already taken");
            }
        } else if (joinGameRequest.playerColor().equals("BLACK") || joinGameRequest.playerColor().equals("black")) {
            if (game.blackUsername() == null || game.blackUsername().equals("null")) {
                GameData newGame = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game(), false);
                this.dataAccess.updateGame(joinGameRequest.gameID(), newGame);
            } else {
                return new ErrorResponse("Error: already taken");
            }
        }
        return Collections.emptyMap();
    }

    public Object listGames(String authToken) throws DataAccessException {
        AuthData auth = null;
        try {
            auth = this.dataAccess.getAuth(authToken);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        if (auth == null) {
            System.out.println("This is what happened");
            return new ErrorResponse("Error: unauthorized");
        }
        return this.dataAccess.listGames();
    }

    public Object clearDB() throws DataAccessException {
        dataAccess.deleteUserData();
        dataAccess.deleteAuthData();
        dataAccess.deleteGameData();
        gameCount = 0;
        return Collections.emptyMap();
    }
}
