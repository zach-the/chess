package client;

import chess.ChessGame;
import dataaccess.DataAccessException;
import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import server.Server;
import serverfacade.ServerFacade;
import spark.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private String firstUserAuth;

    @BeforeAll
    public static void init() throws ResponseException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        serverFacade = new ServerFacade("http://localhost:" + port);
        serverFacade.clearDB();
    }

    @BeforeEach
    public void resetDB() throws ResponseException {
        serverFacade.clearDB();
        UserData user = new UserData("FirstGuy", "password", "test@example.com");
        Object result = serverFacade.registerUser(user);
        assertNotNull(result);
        RegisterResponse registerResponse = assertInstanceOf(RegisterResponse.class, result);
        firstUserAuth = registerResponse.authToken();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("Register: Positive")
    public void testRegisterPositive() throws ResponseException {
        UserData user = new UserData("testUser", "password", "test@example.com");
        Object result = serverFacade.registerUser(user);
        assertNotNull(result);
        RegisterResponse registerResponse = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("testUser", registerResponse.username(), "response did not give same username as user");
    }
    @Test
    @DisplayName("Register: Negative")
    public void testRegisterNegative() throws ResponseException, DataAccessException {
        UserData user = new UserData("secondUser", "secondPlaceisFirstLoser", null);
        try {
            serverFacade.registerUser(user);
        } catch (ResponseException e) {
            assertEquals("failure: 400\n", e.getMessage());
        }
    }
    @Test
    @DisplayName("Login: Positive")
    public void testLoginPositive() throws DataAccessException, ResponseException {
        UserData user = new UserData("firstUser", "secondPlaceisFirstLoser", "test@example.com");
        serverFacade.registerUser(user);
        LoginRequest loginRequest = new LoginRequest("firstUser", "secondPlaceisFirstLoser");
        Object result = serverFacade.userLogin(loginRequest);
        assertNotNull(result);
        RegisterResponse regResult = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("firstUser", regResult.username(), "response did not give same username as user");
    }
    @Test
    @DisplayName("Login: Negative")
    public void testLoginNegative() throws DataAccessException, ResponseException {
        LoginRequest loginRequest = new LoginRequest("firstuser", "secondPlaceIsAcceptable");
        try {
            serverFacade.userLogin(loginRequest);
        } catch (ResponseException e) {
            assertEquals("failure: 401\n", e.getMessage());
        }
    }
    @Test
    @DisplayName("Logout: Positive")
    public void testLogoutPositive() throws DataAccessException, ResponseException {
        UserData user = new UserData("firstUser", "secondPlaceisFirstLoser", "test@example.com");
        serverFacade.registerUser(user);
        LoginRequest loginRequest = new LoginRequest("firstuser", "secondPlaceisFirstLoser");
        Object response = serverFacade.userLogin(loginRequest);
        RegisterResponse registerResponse = assertInstanceOf(RegisterResponse.class, response);
        Object result = serverFacade.userLogout(registerResponse.authToken());
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }
    @Test
    @DisplayName("Logout: Negative")
    public void testLogoutNegative() throws DataAccessException, ResponseException {
        UserData user = new UserData("firstUser", "secondPlaceisFirstLoser", "test@example.com");
        serverFacade.registerUser(user);
        String fakeToken = UUID.randomUUID().toString();
        try {
            serverFacade.userLogout(fakeToken);
        } catch (ResponseException e) {
            assertEquals("failure: 401\n", e.getMessage());
        }
    }
    @Test
    @DisplayName("Create: Positive")
    public void createGamePositive() throws DataAccessException, ResponseException {
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        Object result = serverFacade.createGame(gameRequest.gameName(), gameRequest.authToken());
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());
    }
    @Test
    @DisplayName("Create: Negative")
    public void createGameNegative() throws DataAccessException, ResponseException {
        String fakeToken = UUID.randomUUID().toString();
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        try {
            serverFacade.createGame(gameRequest.gameName(), fakeToken);
        } catch (ResponseException e) {
            assertEquals("failure: 401\n", e.getMessage());
        }
    }
    @Test
    @DisplayName("Join: Positive")
    public void testJoinPositive() throws ResponseException {
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        Object result = serverFacade.createGame(gameRequest.gameName(), gameRequest.authToken());
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());

        Object joinResult = serverFacade.joinGame("1", "WHITE", firstUserAuth);
        assertNotNull(joinResult);
        assertEquals(Collections.emptyMap(), joinResult);
    }

    @Test
    @DisplayName("Join: Negative")
    public void testJoinNegative() throws ResponseException {
        try {
            serverFacade.joinGame("1", "WHITE", firstUserAuth);
        } catch (ResponseException e) {
            assertEquals("failure: 400\n", e.getMessage());
        }
    }

    @Test
    @DisplayName("List: Positive")
    public void testListPositive() throws DataAccessException, ResponseException {
        Object result = serverFacade.createGame("myNewGame", firstUserAuth);
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());

        result = serverFacade.createGame("mySecondGame", firstUserAuth);
        assertNotNull(result);
        gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(2, gameResult.gameID());

        List<GameData> gameData = new ArrayList<>();
        gameData.add(new GameData(1, null, null, "myNewGame", new ChessGame(), false));
        gameData.add(new GameData(2, null, null, "mySecondGame", new ChessGame(), false));
        GameList gameList = new GameList(gameData);

        Object list = serverFacade.listGames(firstUserAuth);
        assertNotNull(list);
        assertEquals(gameList, list);
    }
    @Test
    @DisplayName("List: Negative")
    public void testListNegative() throws DataAccessException, ResponseException {
        Object result = serverFacade.createGame("myNewGame", firstUserAuth);
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());

        result = serverFacade.createGame("mySecondGame", firstUserAuth);
        assertNotNull(result);
        gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(2, gameResult.gameID());

        List<GameData> gameData = new ArrayList<>();
        gameData.add(new GameData(1, null, null, "myNewGame", new ChessGame(), false));
        gameData.add(new GameData(2, null, null, "mySecondGame", new ChessGame(), false));
        GameList gameList = new GameList(gameData);

        String fakeToken = UUID.randomUUID().toString();

        try {
            serverFacade.listGames(fakeToken);
        } catch (ResponseException e) {
            assertEquals("failure: 401\n", e.getMessage());
        }
    }
}
