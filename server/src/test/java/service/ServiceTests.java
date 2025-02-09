package service;
import chess.ChessGame;
import dataaccess.*;
import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {
    private static Service service;
    private static UserData firstUser;
    private String firstUserAuth;

    @BeforeAll
    public static void init() throws DataAccessException {
        firstUser = new UserData("firstUser", "secondPlaceisFirstLoser", "winnersonly@victory.com");
        DataAccess dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
    }

    @BeforeEach
    public void setup() throws ResponseException, DataAccessException {
        service.clearDB();

        Object result = service.registerUser(firstUser);
        assertNotNull(result);
        RegisterResponse registerResponse = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("firstUser", registerResponse.username());
        firstUserAuth = registerResponse.authToken();
    }

    @Test
    @Order(1)
    @DisplayName("register User: Successs")
    public void testRegisterUserSuccess() throws ResponseException, DataAccessException {
        UserData user = new UserData("testUser", "password", "test@example.com");
        Object result = service.registerUser(user);
        assertNotNull(result);
        RegisterResponse registerResponse = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("testUser", registerResponse.username(), "response did not give same username as user");
    }

    @Test
    @Order(2)
    @DisplayName("registerUser: No Email Given")
    public void testRegisterUserNoEmail() throws ResponseException, DataAccessException {
        UserData user = new UserData("secondUser", "secondPlaceisFirstLoser", null);
        Object result = service.registerUser(user);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: bad request", errorResult.message(), "system did not detect invalid email");
    }

    @Test
    @Order(3)
    @DisplayName("userLogin: Success")
    public void testUserLoginSuccess() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("firstUser", "secondPlaceisFirstLoser");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        RegisterResponse regResult = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("firstUser", regResult.username(), "response did not give same username as user");
    }

    @Test
    @Order(4)
    @DisplayName("userLogin: Incorrect Password")
    public void testUserLoginIncorrectPassword() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("firstuser", "secondPlaceIsAcceptable");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message(), "system did not detect incorrect password");
    }

    @Test
    @Order(5)
    @DisplayName("userLogout: Success")
    public void testUserLogoutSuccess() throws DataAccessException {
        Object result = service.userLogout(firstUserAuth);
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    @Order(6)
    @DisplayName("userLogout: Invalid Token")
    public void testuserLogoutInvalidToken() throws DataAccessException {
        String fakeToken = UUID.randomUUID().toString();
        Object result = service.userLogout(fakeToken);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message());
    }

    @Test
    @Order(7)
    @DisplayName("createGame: Success")
    public void testCreateGameSuccess() throws DataAccessException {
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        Object result = service.createGame(gameRequest);
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());
    }

    @Test
    @Order(8)
    @DisplayName("createGame: Invalid Token")
    public void testCreateGameInvalidToken() throws DataAccessException {
        String fakeToken = UUID.randomUUID().toString();
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", fakeToken);
        Object result = service.createGame(gameRequest);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message());
    }

    @Test
    @Order(9)
    @DisplayName("clearDB: Success")
    public void testClearDBSuccess() throws DataAccessException {
        LoginRequest loginRequest = new LoginRequest("firstUser", "secondPlaceisFirstLoser");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        RegisterResponse regResult = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("firstUser", regResult.username(), "response did not give same username as user");

        service.clearDB();

        result = service.userLogin(loginRequest);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message(), "response did not give same username as user");
    }

    @Test
    @Order(10)
    @DisplayName("joinGame: Success")
    public void testJoinGameSuccess() throws DataAccessException {
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        Object result = service.createGame(gameRequest);
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());

        JoinGameReqeust joinGameReqeust = new JoinGameReqeust(firstUserAuth, "WHITE", 1);
        Object joinResult = service.joinGame(joinGameReqeust);
        assertNotNull(joinResult);
        assertEquals(Collections.emptyMap(), joinResult);
    }

    @Test
    @Order(11)
    @DisplayName("joinGame: No Game")
    public void testJoinGameNoGame() throws DataAccessException {
        JoinGameReqeust joinGameReqeust = new JoinGameReqeust(firstUserAuth, "WHITE", 1);
        Object result = service.joinGame(joinGameReqeust);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: bad request", errorResult.message());
    }

    @Test
    @Order(12)
    @DisplayName("listGames: Success")
    public void testListGamesSuccess() throws DataAccessException {
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        Object result = service.createGame(gameRequest);
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());

        gameRequest = new CreateGameRequest("mySecondGame", firstUserAuth);
        result = service.createGame(gameRequest);
        assertNotNull(result);
        gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(2, gameResult.gameID());

        List<GameData> gameData = new ArrayList<>();
        gameData.add(new GameData(1, null, null, "myNewGame", new ChessGame(), false));
        gameData.add(new GameData(2, null, null, "mySecondGame", new ChessGame(), false));
        GameList gameList = new GameList(gameData);

        Object list = service.listGames(firstUserAuth);
        assertNotNull(list);
        assertEquals(gameList, list);
    }

    @Test
    @Order(13)
    @DisplayName("listGames: Invalid Token")
    public void testListGamesInvalidToken() throws DataAccessException {
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        Object result = service.createGame(gameRequest);
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());

        gameRequest = new CreateGameRequest("mySecondGame", firstUserAuth);
        result = service.createGame(gameRequest);
        assertNotNull(result);
        gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(2, gameResult.gameID());

        List<GameData> gameData = new ArrayList<>();
        gameData.add(new GameData(1, null, null, "myNewGame", new ChessGame(), false));
        gameData.add(new GameData(2, null, null, "mySecondGame", new ChessGame(), false));
        GameList gameList = new GameList(gameData);
        String fakeToken = UUID.randomUUID().toString();

        Object list = service.listGames(fakeToken);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, list);
        assertEquals("Error: unauthorized", errorResult.message());
    }
}
