package server;

import model.*;

import service.Service;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import spark.*;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.Map;
import java.util.SequencedMap;


public class Server {
    DataAccess dataAccess = new MemoryDataAccess();
    private final Service service = new Service(dataAccess);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.delete("/db", this::clearDB);
        Spark.post("/session", this::userLogin);
        Spark.delete("/session", this::userLogout);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.get("/game", this::listGames);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object registerUser(Request req, Response res) {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var ret = service.registerUser(user);
        if (ret.equals(new ErrorResponse("Error: bad request"))) {
            res.status(400);
        } else if (ret.equals(new ErrorResponse("Error: already taken"))) {
            res.status(403);
        } else if (ret.equals(new ErrorResponse("Error: invalid email"))) {
            res.status(400);
        } else {
            res.status(200);
        }
        return new Gson().toJson(ret);
    }

    private Object clearDB(Request req, Response res) {
        return new Gson().toJson(service.clearDB());
    }

    private Object userLogin(Request req, Response res) {
        var loginRequest = new Gson().fromJson(req.body(), LoginRequest.class);
        var ret = service.userLogin(loginRequest);
        if (ret.equals(new ErrorResponse("Error: unauthorized"))){
            res.status(401);
        }else {
            res.status(200);
        }
        return new Gson().toJson(ret);
    }

    private Object userLogout(Request req, Response res) {
        String authToken = req.headers("authorization");
        var ret = service.userLogout(authToken);
        if (ret.equals(new ErrorResponse("Error: unauthorized"))) {
            res.status(401);
        } else if (ret.equals(Collections.emptyMap())) {
            res.status(200);
        } else {
            res.status(500);
        }
        return new Gson().toJson(ret);
    }

    private Object createGame(Request req, Response res) {
        String authToken = req.headers("authorization");
        String gameName = (String) (new Gson().fromJson(req.body(), Map.class)).get("gameName");
        var ret = service.createGame(new CreateGameRequest(gameName, authToken));
        if (ret.equals(new ErrorResponse("Error: unauthorized"))) {
            res.status(401);
        } else {
            res.status(200);
        }
        return new Gson().toJson(ret);
    }

    private Object joinGame(Request req, Response res) {
        String authToken = req.headers("authorization");
        Object gameIDObject = (new Gson().fromJson(req.body(), Map.class)).get("gameID");
        int gameID;
        if (gameIDObject instanceof Double) {
            gameID = (int)Math.round((Double)gameIDObject);
        } else if (gameIDObject instanceof Integer) {
            gameID = (int)gameIDObject;
        } else {
            gameID = 999;
            System.out.println("Something went seriously wrong");
        }
        String playerColor = (String) (new Gson().fromJson(req.body(), Map.class)).get("playerColor");
        JoinGameReqeust joinGameReqeust = new JoinGameReqeust(authToken, playerColor,gameID);
        var ret = service.joinGame(joinGameReqeust);
        if (ret.equals(Collections.emptyMap())){
            res.status(200);
        } else if (ret.equals(new ErrorResponse("Error: bad request"))){
            res.status(400);
        } else if (ret.equals(new ErrorResponse("Error: unauthorized"))){
            res.status(401);
        } else if (ret.equals(new ErrorResponse("Error: already taken"))){
            res.status(403);
        }
        return new Gson().toJson(ret);
    }

    private Object listGames(Request req, Response res) {
        String authToken = req.headers("authorization");
        var ret = service.listGames(authToken);
        if (ret.equals(new ErrorResponse("Error: unauthorized"))){
            res.status(401);
        } else {
            res.status(200);
        }
        return new Gson().toJson(ret);
    }



}
