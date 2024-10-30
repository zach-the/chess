package dataaccess;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.GameList;
import model.UserData;

import java.sql.SQLException;


public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    // CLASS OVERRIDES START HERE
    public void addUser(UserData user) {}
    public void addAuth(AuthData auth) {}
    public void deleteAuth(String authToken) {}
    public UserData getUser(String username) { return null; }
    public AuthData getAuth(String authToken) { return null; }
    public void createGame(Integer gameID, GameData gameData) {}
    public GameData getGame(Integer gameID) { return null; }
    public void updateGame(Integer gameID, GameData gameData) {}
    public GameList listGames() { return null; }
    public void deleteUserData() {}
    public void deleteAuthData() {}
    public void deleteGameData() {}
    // CLASS OVERRIDES END HERE

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  pet (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(256) NOT NULL,
              `type` ENUM('CAT', 'DOG', 'FISH', 'FROG', 'ROCK') DEFAULT 'CAT',
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(type),
              INDEX(name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    private void configureDatabase() throws DataAccessException, ResponseException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(500, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
