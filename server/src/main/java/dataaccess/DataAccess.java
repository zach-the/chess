package dataaccess;
import model.*;

public interface DataAccess {
    void addUser(UserData user);
    void addAuth(AuthData auth);
    void deleteAuth(String authToken);
    UserData getUser(String username);
    AuthData getAuth(String authToken);
    void createGame(Integer gameID, GameData gameData);
    GameData getGame(Integer gameID);
    void updateGame(Integer gameID, GameData gameData);
    void printAllAuths();
    GameList listGames();

    void deleteUserData();
    void deleteAuthData();
    void deleteGameData();

    Object userLogin(LoginRequest loginRequest);
}
