package dataaccess;

import model.RegisterResponse;
import model.UserData;
import model.ErrorResponse;
import model.LoginRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess{

    final private HashMap<String, UserData> users = new HashMap<>();

    public Object addUser(UserData user) {
        user = new UserData(user.username(), user.password(), user.email());
        if (user.username() == null || user.password() == null || user.email() == null) {
            return new ErrorResponse("Error: bad request");
        } else if (users.get(user.username()) == null) {
            users.put(user.username(), user);
            return new RegisterResponse(user.username(), UUID.randomUUID().toString());
        } else {
            return new ErrorResponse("Error: already taken");
        }
    }

    // THE FOLLOWING FUNCTIONS
    public Object deleteUserData(){ return Collections.emptyMap(); }
    public Object deleteAuthData(){ return Collections.emptyMap(); }
    public Object deleteGameData(){ return Collections.emptyMap(); }
    // NEED ACTUAL IMPLEMENTATION

    public Object userLogin(LoginRequest loginRequest) {
        if (users.get(loginRequest.username()) != null) {
            if (Objects.equals(users.get(loginRequest.username()).password(), loginRequest.password())) {
                return new RegisterResponse(loginRequest.username(), UUID.randomUUID().toString());
            }
            else {
                // failure: wrong password
                return new ErrorResponse("Error: unauthorized");
            }
        } else {
            // failure: wrong username
            return new ErrorResponse("Error: unauthorized");
        }
    }

}
