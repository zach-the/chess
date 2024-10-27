package service;
import chess.ChessGame;
import dataaccess.*;
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
    public static void init() {
        firstUser = new UserData("firstUser", "secondPlaceisFirstLoser", "winnersonly@victory.com");
        DataAccess dataAccess = new MemoryDataAccess();
        service = new Service(dataAccess);
    }

    @BeforeEach
    public void setup() {
        service.clearDB();

        Object result = service.registerUser(firstUser);
        assertNotNull(result);
        AuthData authResult = assertInstanceOf(AuthData.class, result);
        assertEquals("firstUser", authResult.username());
        firstUserAuth = authResult.authToken();
    }

    @Test
    @Order(1)
    @DisplayName("register User: Successs")
    public void testRegisterUserSuccess() {
        UserData user = new UserData("testUser", "password", "test@example.com");
        Object result = service.registerUser(user);
        assertNotNull(result);
        AuthData authResult = assertInstanceOf(AuthData.class, result);
        assertEquals("testUser", authResult.username(), "response did not give same username as user");
    }

    @Test
    @Order(2)
    @DisplayName("registerUser: No Email Given")
    public void testRegisterUserNoEmail() {
        UserData user = new UserData("secondUser", "secondPlaceisFirstLoser", null);
        Object result = service.registerUser(user);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: bad request", errorResult.message(), "system did not detect invalid email");
    }

    @Test
    @Order(3)
    @DisplayName("userLogin: Success")
    public void testUserLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest("firstUser", "secondPlaceisFirstLoser");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        RegisterResponse regResult = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("firstUser", regResult.username(), "response did not give same username as user");
    }

    @Test
    @Order(4)
    @DisplayName("userLogin: Incorrect Password")
    public void testUserLoginIncorrectPassword() {
        LoginRequest loginRequest = new LoginRequest("firstuser", "secondPlaceIsAcceptable");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message(), "system did not detect incorrect password");
    }

    @Test
    @Order(5)
    @DisplayName("userLogout: Success")
    public void testUserLogoutSuccess() {
        Object result = service.userLogout(firstUserAuth);
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    @Order(6)
    @DisplayName("userLogout: Invalid Token")
    public void testuserLogoutInvalidToken() {
        String fakeToken = UUID.randomUUID().toString();
        Object result = service.userLogout(fakeToken);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message());
    }

    @Test
    @Order(7)
    @DisplayName("createGame: Success")
    public void testCreateGameSuccess() {
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        Object result = service.createGame(gameRequest);
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());
    }

    @Test
    @Order(8)
    @DisplayName("createGame: Invalid Token")
    public void testCreateGameInvalidToken() {
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
    public void testClearDBSuccess() {
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
    public void testJoinGameSuccess() {
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
    public void testJoinGameNoGame() {
        JoinGameReqeust joinGameReqeust = new JoinGameReqeust(firstUserAuth, "WHITE", 1);
        Object result = service.joinGame(joinGameReqeust);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: bad request", errorResult.message());
    }

    @Test
    @Order(12)
    @DisplayName("listGames: Success")
    public void testListGamesSuccess() {
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
        gameData.add(new GameData(1, null, null, "myNewGame", new ChessGame()));
        gameData.add(new GameData(2, null, null, "mySecondGame", new ChessGame()));
        GameList gameList = new GameList(gameData);

        Object list = service.listGames(firstUserAuth);
        assertNotNull(list);
        assertEquals(gameList, list);
    }

    @Test
    @Order(13)
    @DisplayName("listGames: Invalid Token")
    public void testListGamesInvalidToken() {
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
        gameData.add(new GameData(1, null, null, "myNewGame", new ChessGame()));
        gameData.add(new GameData(2, null, null, "mySecondGame", new ChessGame()));
        GameList gameList = new GameList(gameData);
        String fakeToken = UUID.randomUUID().toString();

        Object list = service.listGames(fakeToken);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, list);
        assertEquals("Error: unauthorized", errorResult.message());
    }
}
