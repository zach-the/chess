package dataaccess;

import chess.ChessGame;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.GameList;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLDataAccessTests {
    private static MySQLDataAccess dataAccess;
    private static Server server;

    @BeforeAll
    public static void startServer() throws ResponseException, DataAccessException {
        dataAccess = new MySQLDataAccess();
    }

    @BeforeEach
    public void setUp() throws DataAccessException, ResponseException {
        dataAccess.deleteUserData();
        dataAccess.deleteGameData();
        dataAccess.deleteAuthData();
        UserData user = new UserData("existingName", "existingPassword", "existingEmail");
        dataAccess.addUser(user);
    }

    @AfterAll
    public static void stopDataAccess() throws DataAccessException {
        dataAccess.deleteUserData();
        dataAccess.deleteGameData();
        dataAccess.deleteAuthData();
    }

    @Test
    @DisplayName("addUser Positive")
    public void addUserPositiveTest() throws ResponseException, DataAccessException {
        UserData user = new UserData("name", "password", "email");
        dataAccess.addUser(user);
        assert(dataAccess.getUser("name").equals(user));
    }

    @Test
    @DisplayName("addUser Negative")
    public void addUserNegativeTest() throws ResponseException, DataAccessException {
        UserData newUser = new UserData("existingName", "newPassword", "newEmail");
        DataAccessException e = assertThrows(DataAccessException.class, () -> dataAccess.addUser(newUser));
        assertTrue(e.getMessage().contains("Duplicate entry"));
    }

    @Test
    @DisplayName("addAuth Positive")
    public void addAuthPositiveTest() throws ResponseException, DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData("existingName", token);
        dataAccess.addAuth(auth);
        assert(dataAccess.getAuth(token).username().equals("existingName"));
    }

    @Test
    @DisplayName("addAuth Negative")
    public void addAuthNegativeTest() throws ResponseException, DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData("existingName", token);
        dataAccess.addAuth(auth);
        assert(dataAccess.getAuth(token).username().equals("existingName"));

        AuthData newAuth = new AuthData("existingName", token);
        DataAccessException e = assertThrows(DataAccessException.class, () -> dataAccess.addAuth(newAuth));
        assertTrue(e.getMessage().contains("Duplicate entry"));
    }

    @Test
    @DisplayName("deleteAuth Positive")
    public void deleteAuthPositiveTest() throws ResponseException, DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData("existingName", token);
        dataAccess.addAuth(auth);
        assert(dataAccess.getAuth(token).username().equals("existingName"));
        dataAccess.deleteAuth(token);
        assert(dataAccess.getAuth(token) == null);
    }

    @Test
    @DisplayName("deleteAuth Negative")
    public void deleteAuthNegativeTest() throws ResponseException, DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData("existingName", token);
        dataAccess.addAuth(auth);
        assert(dataAccess.getAuth(token).username().equals("existingName"));
        String fakeToken = UUID.randomUUID().toString();
        dataAccess.deleteAuth(fakeToken);
        assert(dataAccess.getAuth(token).equals(auth));
    }

    @Test
    @DisplayName("getUser Positive")
    public void getUserPositiveTest() throws ResponseException, DataAccessException {
        UserData user = new UserData("name", "password", "email");
        dataAccess.addUser(user);
        assert(dataAccess.getUser("name").equals(user));
    }

    @Test
    @DisplayName("getUser Negative")
    public void getUserNegativeTest() throws ResponseException, DataAccessException {
        UserData user = new UserData("name", "password", "email");
        dataAccess.addUser(user);
        assertNull(dataAccess.getUser("wrongName"));
    }

    @Test
    @DisplayName("getAuth Positive")
    public void getAuthPositiveTest() throws ResponseException, DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData("existingName", token);
        dataAccess.addAuth(auth);
        assert(dataAccess.getAuth(token).username().equals("existingName"));
    }

    @Test
    @DisplayName("getAuth Negative")
    public void getAuthNegativeTest() throws ResponseException, DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData("existingName", token);
        dataAccess.addAuth(auth);
        String fakeToken = UUID.randomUUID().toString();
        assertNull(dataAccess.getAuth(fakeToken));
    }

    @Test
    @DisplayName("createGame Positive")
    public void createGamePositiveTest() throws ResponseException, DataAccessException {
        GameData newGame = new GameData(1, null, null, "game", new ChessGame());
        dataAccess.createGame(1, newGame);
        assert(dataAccess.getGame(1).equals(newGame));
    }

    @Test
    @DisplayName("createGame Negative")
    public void createGameNegativeTest() throws ResponseException, DataAccessException {
        GameData newGame = new GameData(1, null, null, "game", new ChessGame());
        dataAccess.createGame(1, newGame);
        GameData otherGame = new GameData(1, null, null, "otherGame", new ChessGame());
        DataAccessException e = assertThrows(DataAccessException.class, () -> dataAccess.createGame(1, otherGame));
        assertTrue(e.getMessage().contains("Duplicate"));
    }

    @Test
    @DisplayName("getGame Positive")
    public void getGamePositiveTest() throws ResponseException, DataAccessException {
        GameData newGame = new GameData(1, null, null, "game", new ChessGame());
        dataAccess.createGame(1, newGame);
        assert(dataAccess.getGame(1).equals(newGame));
    }

    @Test
    @DisplayName("getGame Negative")
    public void getGameNegativeTest() throws ResponseException, DataAccessException {
        assertNull(dataAccess.getGame(1));
    }

    @Test
    @DisplayName("updateGame Positive")
    public void updateGamePositiveTest() throws ResponseException, DataAccessException {
        GameData newGame = new GameData(1, null, null, "game", new ChessGame());
        dataAccess.createGame(1, newGame);
        GameData updatedGame = new GameData(1, null, "black", "game", new ChessGame());
        dataAccess.updateGame(1, updatedGame);
        assertEquals(dataAccess.getGame(1), updatedGame);
    }

    @Test
    @DisplayName("listGame Positive")
    public void listGamePositiveTest() throws ResponseException, DataAccessException {
        GameData newGame = new GameData(1, "white", null, "game", new ChessGame());
        dataAccess.createGame(1, newGame);
        GameData newNewGame = new GameData(2, null, "black", "game", new ChessGame());
        dataAccess.createGame(2, newNewGame);

        List<GameData> gameData = new ArrayList<>();
        gameData.add(newGame);
        gameData.add(newNewGame);
        GameList gameList = new GameList(gameData);

        Object list = dataAccess.listGames();
        assertNotNull(list);
        assertEquals(list, gameList);
    }

    @Test
    @DisplayName("listGame Negative")
    public void listGameNegativeTest() throws ResponseException, DataAccessException {
        List<GameData> gameData = new ArrayList<>();
        GameList gameList = new GameList(gameData);

        Object list = dataAccess.listGames();
        assertNotNull(list);
        assertEquals(list, gameList);
    }

    @Test
    @DisplayName("deleteGameData Positive")
    public void deleteGameDataPositiveTest() throws ResponseException, DataAccessException {
        GameData newGame = new GameData(1, null, null, "game", new ChessGame());
        dataAccess.createGame(1, newGame);
        assert(dataAccess.getGame(1).equals(newGame));

        GameData newNewGame = new GameData(2, null, null, "game", new ChessGame());
        dataAccess.createGame(2, newNewGame);
        assert(dataAccess.getGame(2).equals(newNewGame));

        dataAccess.deleteGameData();
        assertNull(dataAccess.getGame(1));
        assertNull(dataAccess.getGame(2));
    }

    @Test
    @DisplayName("deleteAuthData Positive")
    public void deleteAuthDataPositiveTest() throws ResponseException, DataAccessException {
        String token = UUID.randomUUID().toString();
        AuthData auth = new AuthData("existingName", token);
        dataAccess.addAuth(auth);
        assert(dataAccess.getAuth(token).username().equals("existingName"));

        String token2 = UUID.randomUUID().toString();
        AuthData auth2 = new AuthData("existingName2", token2);
        dataAccess.addAuth(auth2);
        assert(dataAccess.getAuth(token2).username().equals("existingName2"));

        dataAccess.deleteAuthData();
        assertNull(dataAccess.getAuth(token));
        assertNull(dataAccess.getAuth(token2));
    }

    @Test
    @DisplayName("deleteUserData Positive")
    public void deleteUserDataPositiveTest() throws ResponseException, DataAccessException {
        UserData user = new UserData("name", "password", "email");
        dataAccess.addUser(user);
        assert(dataAccess.getUser("name").equals(user));

        UserData user2 = new UserData("name2", "password2", "email2");
        dataAccess.addUser(user2);
        assert(dataAccess.getUser("name2").equals(user2));

        dataAccess.deleteUserData();
        assertNull(dataAccess.getUser("name"));
        assertNull(dataAccess.getUser("name2"));
    }
}
