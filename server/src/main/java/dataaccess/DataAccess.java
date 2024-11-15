package dataaccess;
import exception.ResponseException;
import model.*;

import java.sql.SQLException;

public interface DataAccess {
    void addUser(UserData user) throws ResponseException, DataAccessException;
    void addAuth(AuthData auth) throws DataAccessException;
    void deleteAuth(String authToken) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    AuthData getAuth(String authToken) throws DataAccessException, SQLException;
    void createGame(Integer gameID, GameData gameData) throws DataAccessException;
    GameData getGame(Integer gameID) throws DataAccessException;
    void updateGame(Integer gameID, GameData gameData) throws DataAccessException;
    GameList listGames() throws DataAccessException;

    void deleteUserData() throws DataAccessException;
    void deleteAuthData() throws DataAccessException;
    void deleteGameData() throws DataAccessException;
}
