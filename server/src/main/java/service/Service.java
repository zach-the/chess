package service;

import model.LoginRequest;
import model.UserData;
import dataaccess.DataAccess;

import java.util.Collections;


public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Object addUser(UserData user){
        return dataAccess.addUser(user);
    }

    public Object clearDB() {
        dataAccess.deleteUserData();
        dataAccess.deleteAuthData();
        dataAccess.deleteGameData();
        return Collections.emptyMap();
    }

    public Object userLogin(LoginRequest loginRequest) {
        return dataAccess.userLogin(loginRequest);
    }
}
