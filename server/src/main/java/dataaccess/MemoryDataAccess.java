package dataaccess;

import model.RegisterResponse;
import model.UserData;
import model.ErrorResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess{

    final private HashMap<String, UserData> users = new HashMap<>();

    public Object addUser(UserData user) {
        user = new UserData(user.username(), user.password(), user.email());
        if (user.username() == null || user.password() == null || user.email() == null) {
            return new ErrorResponse("Error: bad request");
        } else if (users.get(user.username()) == null) {
            users.put(user.username(), user);
            String authToken = UUID.randomUUID().toString();
            return new RegisterResponse(user.username(), UUID.randomUUID().toString());
        } else {
            return new ErrorResponse("Error: already taken");
        }
    }

    public Object deleteUserData(){ return Collections.emptyMap(); }
    public Object deleteAuthData(){ return Collections.emptyMap(); }
    public Object deleteGameData(){ return Collections.emptyMap(); }
}
