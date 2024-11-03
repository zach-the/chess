package dataaccess;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.GameList;
import model.UserData;
import org.eclipse.jetty.server.Authentication;

import javax.xml.crypto.Data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;


public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() throws ResponseException, DataAccessException {
        configureDatabase();
    }

    // CLASS OVERRIDES START HERE
    public void addUser(UserData user) throws ResponseException, DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES (?, ?, ?)");
            statement.setString(1, user.username());
            statement.setString(2, user.password());
            statement.setString(3, user.email());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public void addAuth(AuthData auth) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("INSERT INTO auths (auth, username) VALUES (?, ?)");
            statement.setString(1, auth.authToken());
            statement.setString(2, auth.username());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("DELETE FROM auths WHERE auth = ?");
            statement.setString(1, authToken);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            statement.setString(1, username);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new UserData(result.getString("username"), result.getString("password"), result.getString("email"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("SELECT * FROM auths WHERE auth = ?");
            statement.setString(1, authToken);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new AuthData(result.getString("username"), result.getString("auth"));
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public void createGame(Integer gameID, GameData gameData) throws DataAccessException {
        if (getGame(gameID) != null) {
            throw new DataAccessException("Duplicate game");
        }
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("INSERT INTO games (id, json) VALUES (?, ?)");
            statement.setString(1, gameID.toString());
            statement.setString(2, new Gson().toJson(gameData));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public GameData getGame(Integer gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("SELECT * FROM games WHERE id = ?");
            statement.setString(1, gameID.toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new Gson().fromJson(result.getString("json"), GameData.class);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public void updateGame(Integer gameID, GameData gameData) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("DELETE FROM games WHERE id = ?");
            statement.setString(1, gameID.toString());
            statement.executeUpdate();
            createGame(gameID, gameData);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public GameList listGames() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("SELECT * FROM games");
            ResultSet result = statement.executeQuery();
            List<GameData> games = new ArrayList<>();
            if (result.next()) {
                do {
                    games.add(new Gson().fromJson(result.getString("json"), GameData.class));
                } while (result.next());
            }
            return new GameList(games);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public void deleteUserData() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("DELETE FROM users");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public void deleteAuthData() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("DELETE FROM auths");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    public void deleteGameData() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = conn.prepareStatement("DELETE FROM games");
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    // CLASS OVERRIDES END HERE

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
            ,
            """
            CREATE TABLE IF NOT EXISTS  auths (
              `auth` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`auth`),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
            ,
            """
            CREATE TABLE IF NOT EXISTS  games (
            `id` int NOT NULL AUTO_INCREMENT,
            `json` TEXT DEFAULT NULL,
            INDEX(id)
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
