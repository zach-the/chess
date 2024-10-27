package service;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class myTestCases {
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
    public void testRegisterUser_Success() {
        UserData user = new UserData("testUser", "password", "test@example.com");
        Object result = service.registerUser(user);
        assertNotNull(result);
        AuthData authResult = assertInstanceOf(AuthData.class, result);
        assertEquals("testUser", authResult.username(), "response did not give same username as user");
    }

    @Test
    @DisplayName("registerUser: Invalid Email Given")
    public void testRegisterUser_InvalidEmail() {
        UserData user = new UserData("secondUser", "secondPlaceisFirstLoser", "justarandomstring");
        Object result = service.registerUser(user);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: invalid email", errorResult.message(), "system did not detect invalid email");
    }

    @Test
    @DisplayName("userLogin: Success")
    public void testUserLogin_Success() {
        LoginRequest loginRequest = new LoginRequest("firstUser", "secondPlaceisFirstLoser");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        RegisterResponse regResult = assertInstanceOf(RegisterResponse.class, result);
        assertEquals("firstUser", regResult.username(), "response did not give same username as user");
    }

    @Test
    @DisplayName("userLogin: Incorrect Password")
    public void testUserLogin_IncorrectPassword() {
        LoginRequest loginRequest = new LoginRequest("firstuser", "secondPlaceIsAcceptable");
        Object result = service.userLogin(loginRequest);
        assertNotNull(result);
        ErrorResponse errorResult = assertInstanceOf(ErrorResponse.class, result);
        assertEquals("Error: unauthorized", errorResult.message(), "system did not detect incorrect password");
    }

    @Test
    @DisplayName("userLogout: Success")
    public void testUserLogout_Success() {

    }

    // FUNCTIONS THAT NEED TESTING:
        // userLogout
        // createGame
        // clearDB
        // joinGame
        // listGames
}
