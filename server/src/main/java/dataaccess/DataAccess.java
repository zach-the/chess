package dataaccess;
import model.UserData;

public interface DataAccess {
    Object addUser(UserData user);

    Object deleteUserData();
    Object deleteAuthData();
    Object deleteGameData();
}
