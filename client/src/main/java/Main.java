import chess.*;
import ui.EscapeSequences;
import ui.PreloginUI;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        var serverURL = "http://localhost:8081";
        if (args.length ==1) {
            serverURL = args[0];
        }
        System.out.println(EscapeSequences.PURPLE + "♕ Welcome to CS 240 Chess! ♕" + EscapeSequences.BLUE);
        new PreloginUI(serverURL).repl();
    }
}