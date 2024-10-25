import chess.*;
import server.Server;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.Service;


public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        Server server = new Server();
        server.run(8080);

        System.out.println("Server started);");
    }
}