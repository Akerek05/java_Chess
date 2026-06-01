import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BoardShould {
	
	private Board board;
	
	@BeforeEach
    void setUp() {
        board = new Board();
        board.setupEmptyBoard();
    }

	@Test
    void testBoardSetup() {
        board.setupBoard();
        Piece[][] pieces = board.getPieces();

        // Check pawns
        for (int i = 0; i < Board.SIZE; i++) {
            assertEquals(Piece.PieceType.PAWN, pieces[1][i].type());
            assertEquals(Piece.PieceType.PAWN, pieces[6][i].type());
            assertTrue(pieces[1][i].isWhite());
            assertFalse(pieces[6][i].isWhite());
        }

        // Check rooks
        assertEquals(Piece.PieceType.ROOK, pieces[0][0].type());
        assertEquals(Piece.PieceType.ROOK, pieces[0][7].type());
        assertTrue(pieces[0][0].isWhite());
        assertTrue(pieces[0][7].isWhite());
        
        assertEquals(Piece.PieceType.ROOK, pieces[7][0].type());
        assertEquals(Piece.PieceType.ROOK, pieces[7][7].type());
        assertFalse(pieces[7][0].isWhite());
        assertFalse(pieces[7][7].isWhite());

        // Check knights
        assertEquals(Piece.PieceType.KNIGHT, pieces[0][1].type());
        assertEquals(Piece.PieceType.KNIGHT, pieces[0][6].type());
        assertTrue(pieces[0][1].isWhite());
        assertTrue(pieces[0][6].isWhite());
        
        assertEquals(Piece.PieceType.KNIGHT, pieces[7][1].type());
        assertEquals(Piece.PieceType.KNIGHT, pieces[7][6].type());
        assertFalse(pieces[7][1].isWhite());
        assertFalse(pieces[7][6].isWhite());

        // Check bishops
        assertEquals(Piece.PieceType.BISHOP, pieces[0][2].type());
        assertEquals(Piece.PieceType.BISHOP, pieces[0][5].type());
        assertTrue(pieces[0][2].isWhite());
        assertTrue(pieces[0][5].isWhite());
        
        assertEquals(Piece.PieceType.BISHOP, pieces[7][2].type());
        assertEquals(Piece.PieceType.BISHOP, pieces[7][5].type());
        assertFalse(pieces[7][2].isWhite());
        assertFalse(pieces[7][5].isWhite());

        // Check queens
        assertEquals(Piece.PieceType.QUEEN, pieces[0][3].type());
        assertTrue(pieces[0][3].isWhite());
        
        assertEquals(Piece.PieceType.QUEEN, pieces[7][3].type());
        assertFalse(pieces[7][3].isWhite());

        // Check kings
        assertEquals(Piece.PieceType.KING, pieces[0][4].type());
        assertTrue(pieces[0][4].isWhite());
        
        assertEquals(Piece.PieceType.KING, pieces[7][4].type());
        assertFalse(pieces[7][4].isWhite());

        // Check empty squares in the middle of the board
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                assertEquals(Piece.PieceType.EMPTY, pieces[i][j].type());
            }
        }
    }
	
    @Test
    void testRestorePreviousState() {
        // Place initial pieces
        board.getPieces()[0][0] = new King(true);
        board.getPieces()[1][0] = new Rook(false);
        
        // Execute a move: King captures Rook at (1,0)
        Move move1 = new Move(0, 0, 1, 0, Move.MoveType.ATTACK);
        assertTrue(board.move(0, 0, move1));

        // Execute another move: King moves to (1,1)
        Move move2 = new Move(1, 0, 1, 1, Move.MoveType.MOVE);
        assertTrue(board.move(1, 0, move2));

        // State after 2 moves
        assertEquals(Piece.PieceType.KING, board.getPieces()[1][1].type());
        assertEquals(Piece.PieceType.EMPTY, board.getPieces()[1][0].type());

        // Undo move 2
        board.restorePreviousState();
        assertEquals(Piece.PieceType.KING, board.getPieces()[1][0].type());
        assertEquals(Piece.PieceType.EMPTY, board.getPieces()[1][1].type());

        // Undo move 1
        board.restorePreviousState();
        assertEquals(Piece.PieceType.KING, board.getPieces()[0][0].type());
        assertEquals(Piece.PieceType.ROOK, board.getPieces()[1][0].type());
        assertFalse(board.getPieces()[1][0].isWhite());
    }

    @Test
    void testInfiniteUndoCapability() {
        board.getPieces()[0][0] = new King(true);
        
        // Make 10 moves
        for (int i = 0; i < 7; i++) {
            board.move(i, 0, new Move(i, 0, i+1, 0, Move.MoveType.MOVE));
        }
        
        assertEquals(Piece.PieceType.KING, board.getPieces()[7][0].type());
        
        // Undo all 7 moves
        for (int i = 0; i < 7; i++) {
            board.undoLastMove();
        }
        
        assertEquals(Piece.PieceType.KING, board.getPieces()[0][0].type());
    }
}
