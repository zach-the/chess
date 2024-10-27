package dataaccess;

import model.*;

import java.util.*;

public class MemoryDataAccess implements DataAccess{

    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, AuthData> auths = new HashMap<>();
    final private HashMap<Integer, GameData> games = new HashMap<>();

    private void printAllAuths() { auths.forEach((key, value) -> System.out.println(key + " " + value)); }
    private void printAllGames() { games.forEach((key, value) -> System.out.println(key + " " + value.gameID())); }

    // USER FUNCTIONS
    public void addUser(UserData user) { users.put(user.username(), user); }
    public UserData getUser(String username) { return users.get(username); }

    // AUTH FUNCTIONS
    public void addAuth(AuthData auth) { auths.put(auth.authToken(), auth); }
    public AuthData getAuth(String authToken) { return auths.get(authToken); }
    public void deleteAuth(String authToken) { auths.remove(authToken); }

    // GAME FUNCTIONS
    public void createGame(Integer gameID, GameData gameData) { games.putIfAbsent(gameID, gameData); }
    public GameData getGame(Integer gameID) { return games.get(gameID); }
    public void updateGame(Integer gameID, GameData game) { games.replace(gameID, game); }
    public GameList listGames() {
        List<GameData> gameDataList = new ArrayList<>();
        for (Integer gameID : games.keySet()) {
            gameDataList.add(games.get(gameID));
        }
        return new GameList(gameDataList);
    }

    // CLEAR FUNCTIONS
    public void deleteUserData(){ users.clear(); }
    public void deleteAuthData(){ auths.clear(); }
    public void deleteGameData(){ games.clear(); }

}
