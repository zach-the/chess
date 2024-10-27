package service;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MyTestCases {
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
    @DisplayName("register User: Successs")
    public void testRegisterUserSuccess() {
        UserData user = new UserData("testUser", "password", "test@example.com");
        Object result = service.registerUser(user);
        assertNotNull(result);
        AuthData authResult = assertInstanceOf(AuthData.class, result);
        assertEquals("testUser", authResult.username(), "response did not give same username as user");
    }

    @Test
    @DisplayName("registerUser: No Email Given")
    public void testRegisterUserNoEmail() {
        UserData user = new UserData("secondUser", "secondPlaceisFirstLoser", null);
        Object result = service.registerUser(user);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: bad request", errorResult.message(), "system did not detect invalid email");
    }

    @Test
    @DisplayName("userLogin: Success")
    public void testUserLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest("firstUser", "secondPlaceisFirstLoser");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        RegisterResponse regResult = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("firstUser", regResult.username(), "response did not give same username as user");
    }

    @Test
    @DisplayName("userLogin: Incorrect Password")
    public void testUserLoginIncorrectPassword() {
        LoginRequest loginRequest = new LoginRequest("firstuser", "secondPlaceIsAcceptable");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message(), "system did not detect incorrect password");
    }

    @Test
    @DisplayName("userLogout: Success")
    public void testUserLogoutSuccess() {
        Object result = service.userLogout(firstUserAuth);
        assertNotNull(result);
        assertEquals(Collections.emptyMap(), result);
    }

    @Test
    @DisplayName("userLogout: Invalid Token")
    public void testuserLogoutInvalidToken() {
        String fakeToken = UUID.randomUUID().toString();
        Object result = service.userLogout(fakeToken);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message());
    }

    @Test
    @DisplayName("createGame: Success")
    public void testCreateGameSuccess() {
        CreateGameRequest gameRequest = new CreateGameRequest("myNewGame", firstUserAuth);
        Object result = service.createGame(gameRequest);
        assertNotNull(result);
        CreateGameResponse gameResult = assertInstanceOf(CreateGameResponse.class, result);
        assertEquals(1, gameResult.gameID());
    }

    @Test
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


}
