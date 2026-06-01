import javax.swing.*;
import java.util.List;
import java.util.concurrent.Executors;

public class GameController {
    private final Board board;
    private final ChessGUI view;
    private boolean whiteTurn = true;
    private boolean onePlayerMode = false;
    private boolean playerPlaysWhite = true;
    private int selectedX = -1, selectedY = -1;

    public GameController(Board board, ChessGUI view) {
        this.board = board;
        this.view = view;
    }

    public void startNewGame(boolean singlePlayer, boolean playerPlaysWhite) {
        this.onePlayerMode = singlePlayer;
        this.playerPlaysWhite = playerPlaysWhite;
        this.whiteTurn = true;
        this.selectedX = -1;
        this.selectedY = -1;
        
        board.setupBoard();
        view.updateBoard(board.getPieces());
        
        Executors.newSingleThreadExecutor().submit(this::gameLoop);
    }

    private void gameLoop() {
        while (true) {
            if (board.isCheckmate(whiteTurn)) {
                String winner = whiteTurn ? "Black" : "White";
                view.showMessage(winner + " wins by checkmate!");
                view.returnToMainMenu();
                return;
            }

            if (onePlayerMode && whiteTurn != playerPlaysWhite) {
                // AI's turn
                board.makeComputerMove(whiteTurn);
                view.updateBoard(board.getPieces());
                promotePawnsIfNeeded();
                whiteTurn = !whiteTurn;
            } else {
                // Player's turn (or wait for player in 2-player mode)
                waitForPlayerMove();
                promotePawnsIfNeeded();
                whiteTurn = !whiteTurn;
            }
        }
    }

    private synchronized void waitForPlayerMove() {
        try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void handleSquareClick(int x, int y) {
        if (selectedX == -1 && selectedY == -1) {
            Piece piece = board.getPieces()[x][y];
            if (piece.isWhite() == whiteTurn && piece.isNotEmpty()) {
                selectedX = x;
                selectedY = y;
                view.highlightMoves(board.filterMoves(x, y));
            }
        } else {
            Move.MoveType moveType = board.getMoveType(selectedX, selectedY, x, y);
            Move move = new Move(selectedX, selectedY, x, y, moveType);

            if (board.filterMoves(selectedX, selectedY).contains(move) && board.move(selectedX, selectedY, move)) {
                if (isPawnPromotion(x, y)) {
                    handlePawnPromotion(x, y);
                }
                view.updateBoard(board.getPieces());
                resetSelection();
                notify(); // Notify game loop
            } else {
                view.showMessage("Invalid move. Try again.");
                resetSelection();
            }
        }
    }

    private void resetSelection() {
        selectedX = -1;
        selectedY = -1;
        view.clearHighlights();
    }

    private boolean isPawnPromotion(int x, int y) {
        Piece piece = board.getPieces()[x][y];
        return piece.isNotEmpty() && piece.type() == Piece.PieceType.PAWN &&
               ((piece.isWhite() && x == Board.SIZE - 1) || (!piece.isWhite() && x == 0));
    }

    private void handlePawnPromotion(int x, int y) {
        if (onePlayerMode && whiteTurn != playerPlaysWhite) {
            board.promotePawn(x, y, 'Q');
        } else {
            char type = view.showPromotionDialog();
            board.promotePawn(x, y, type);
        }
    }

    private void promotePawnsIfNeeded() {
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                if (isPawnPromotion(x, y)) {
                    handlePawnPromotion(x, y);
                    view.updateBoard(board.getPieces());
                }
            }
        }
    }

    public void handleUndo() {
        board.undoLastMove();
        whiteTurn = !whiteTurn;
        view.updateBoard(board.getPieces());
    }

    public void handleSurrender() {
        String winner = whiteTurn ? "Black" : "White";
        view.showMessage("You have surrendered. " + winner + " wins!");
        view.returnToMainMenu();
    }
    
    public Board getBoard() {
        return board;
    }
    
    public boolean getWhiteTurn() {
        return whiteTurn;
    }
    
    public void setWhiteTurn(boolean whiteTurn) {
        this.whiteTurn = whiteTurn;
    }
}