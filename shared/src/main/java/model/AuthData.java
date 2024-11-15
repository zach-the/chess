package model;

public record AuthData(String username, String authToken){
    public boolean exists() { return (username != null && authToken != null); }
}
