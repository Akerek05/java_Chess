public class PieceMoveCommand implements MoveCommand {
    private final Board board;
    private final int startX, startY;
    private final int endX, endY;
    private final Piece movingPiece;
    private final Piece capturedPiece;

    public PieceMoveCommand(Board board, int startX, int startY, Move move) {
        this.board = board;
        this.startX = startX;
        this.startY = startY;
        this.endX = move.getX();
        this.endY = move.getY();
        this.movingPiece = board.getPieces()[startX][startY];
        this.capturedPiece = board.getPieces()[endX][endY];
    }

    @Override
    public void execute() {
        Piece[][] pieces = board.getPieces();
        pieces[endX][endY] = movingPiece;
        pieces[startX][startY] = new Empty();
    }

    @Override
    public void undo() {
        Piece[][] pieces = board.getPieces();
        pieces[startX][startY] = movingPiece;
        pieces[endX][endY] = capturedPiece;
    }
}