import chess.*;
import dataaccess.DataAccessException;
import exception.ResponseException;
import server.Server;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.Service;


public class Main {
    public static void main(String[] args) throws ResponseException, DataAccessException {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        Server server = new Server();
        server.run(8081);

        System.out.println("Server started);");
    }
}