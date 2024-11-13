import com.google.gson.Gson;
import exception.ResponseException;
import model.LoginRequest;
import model.RegisterResponse;
import model.UserData;

import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public Object registerUser(UserData user) throws ResponseException {
        return this.makeRequest("POST", "/user", user, UserData.class);
    }

    public void clearDB() throws ResponseException {
        this.makeRequest("DELETE", "/db", null, null);
    }

    public Object userLogin(LoginRequest loginRequest) throws ResponseException {
        return this.makeRequest("POST", "/session", loginRequest, RegisterResponse.class);
    }

    public Object userLogout(String authToken) throws ResponseException {
        return this.makeRequest("DELETE", "/session", authToken, Map.class);
    }

//    public Object createGame(String gameName, )       HOW DO I ADD HEADERS???????

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

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
            throw new ResponseException("failure: " + status);
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
