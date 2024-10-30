package dataaccess;

import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.GameList;
import model.UserData;

import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;


public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    // CLASS OVERRIDES START HERE
    public void addUser(UserData user) {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        var id = executeUpdate(statement, user.username(), user.password(), user.email());
    }
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

    private int executeUpdate(String statement, Object... params) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new ResponseException(500, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  users (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`username`),
              INDEX(password),
              INDEX(email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
//            ,
//            """
//            CREATE TABLE IF NOT EXISTS  auths (
//              `auth` varchar(256) NOT NULL,
//              `username` varchar(256) NOT NULL,
//              `json` TEXT DEFAULT NULL,
//              PRIMARY KEY (`auth`),
//              INDEX(username)
//            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
//            """
//            ,
//            """
//            CREATE TABLE IF NOT EXISTS  games (
//            `id` int NOT NULL AUTO_INCREMENT,
//            `whiteUsername` varchar(256) NOT NULL,
//            `blackUsername` varchar(256) NOT NULL,
//            `gameName` varchar(256) NOT NULL,
//            `json` TEXT DEFAULT NULL,
//            PRIMARY KEY (`auth`),
//            INDEX(username)
//            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
//            """
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
