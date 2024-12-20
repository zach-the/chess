package serverfacade;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ServerFacade {
    private final String serverURL;

    public ServerFacade(String url) {
        serverURL = url;
    }

    public Object registerUser(UserData user) throws ResponseException {
        return this.makeRequest("POST", "/user", user, RegisterResponse.class, "");
    }

    public void clearDB() throws ResponseException {
        this.makeRequest("DELETE", "/db", null, null, "");
    }

    public Object userLogin(LoginRequest loginRequest) throws ResponseException {
        return this.makeRequest("POST", "/session", loginRequest, RegisterResponse.class, "");
    }

    public Object userLogout(String authToken) throws ResponseException {
        return this.makeRequest("DELETE", "/session", null, Map.class, authToken);
    }

    public Object createGame(String gameName, String authToken) throws ResponseException {
        Map<String, String> request = new HashMap<>();
        request.put("gameName", gameName);
        return this.makeRequest("POST", "/game", request, CreateGameResponse.class, authToken);
    }

    public Object joinGame(String gameID, String playerColor, String authToken) throws ResponseException {
        Map<String, String> request = new HashMap<>();
        request.put("gameID", gameID);
        request.put("playerColor", playerColor);
        return this.makeRequest("PUT", "/game", request, Map.class, authToken);
    }

    public Object listGames(String authToken) throws ResponseException {
        return this.makeRequest("GET", "/game", null, GameList.class, authToken);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(serverURL + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if(!authToken.isEmpty()) { http.addRequestProperty("authorization", authToken); }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new ResponseException(ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            throw new ResponseException("failure: " + status + "\n");
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
