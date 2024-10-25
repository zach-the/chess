package server;

import model.UserData;
import service.Service;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import spark.*;
import com.google.gson.Gson;
import model.ErrorResponse;
import model.LoginRequest;

public class Server {
    DataAccess dataAccess = new MemoryDataAccess();
    private final Service service = new Service(dataAccess);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::addUser);
        Spark.delete("/db", this::clearDB);
        Spark.post("/session", this::userLogin);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    //////////////////////
    // ENTERING HANDLER //
    //////////////////////

    private Object addUser(Request req, Response res) {
        var user = new Gson().fromJson(req.body(), UserData.class);
        var ret = service.addUser(user);
        if (ret.equals(new ErrorResponse("Error: bad request"))) {
            res.status(400);
            return new Gson().toJson(ret);
        } else if (ret.equals(new ErrorResponse("Error: already taken"))){
            res.status(403);
            return new Gson().toJson(ret);
        } else {
            res.status(200);
            return new Gson().toJson(ret);
        }
    }

    private Object clearDB(Request req, Response res) {
        res.type("application/json");
        return new Gson().toJson(service.clearDB());
    }

    private Object userLogin(Request req, Response res) {
        var user = new Gson().fromJson(req.body(), LoginRequest.class);
    }
}
