package dataaccess;

import model.*;

import java.util.*;

public class MemoryDataAccess implements DataAccess{

    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, AuthData> auths = new HashMap<>();
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public void printAllAuths() {
        System.out.println("Printing all auths:");
        auths.forEach((key, value) -> System.out.println(key + " " + value));
    }
    private void printAllGames() {
        games.forEach((key, value) -> System.out.println(key + " " + value.gameID()));
    }
    public void addUser(UserData user) {
        users.put(user.username(), user);
    }
    public UserData getUser(String username) {
        return users.get(username);
    }
    public void addAuth(AuthData auth) {
        auths.put(auth.authToken(), auth);
    }
    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }
    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }
    public void createGame(Integer gameID, GameData gameData) {
        games.putIfAbsent(gameID, gameData);
    }
    public GameData getGame(Integer gameID) {
        printAllGames();
        return games.get(gameID);
    }
    public void updateGame(Integer gameID, GameData game) {
        games.replace(gameID, game);
    }

    public void deleteUserData(){ users.clear(); }
    public void deleteAuthData(){ auths.clear(); }
    public void deleteGameData(){ games.clear(); }

    public Object userLogin(LoginRequest loginRequest) {
        if (users.get(loginRequest.username()) != null) {
            if (Objects.equals(users.get(loginRequest.username()).password(), loginRequest.password())) {
                String auth = UUID.randomUUID().toString();
                auths.put(auth, new AuthData(loginRequest.username(), auth));
                return new RegisterResponse(loginRequest.username(), auth);
            }
            else { // failure: wrong password
                return new ErrorResponse("Error: unauthorized");
            }
        } else { // failure: wrong username
            return new ErrorResponse("Error: unauthorized");
        }
    }

    public GameList listGames() {
        List<GameData> gameDataList = new ArrayList<>();
        for (Integer gameID : games.keySet()) {
            gameDataList.add(games.get(gameID));
        }
        return new GameList(gameDataList);
    }

}
