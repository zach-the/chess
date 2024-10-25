package dataaccess;
import model.LoginRequest;
import model.UserData;

public interface DataAccess {
    Object addUser(UserData user);

    Object deleteUserData();
    Object deleteAuthData();
    Object deleteGameData();

    Object userLogin(LoginRequest loginRequest);
}
