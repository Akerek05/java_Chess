import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.List;


public class ChessGUI extends JFrame {
	private Board board;
    private JButton[][] boardButtons;
    private boolean whiteTurn = true; // White always starts
    private int selectedX = -1, selectedY = -1;
    private JButton undoButton, saveButton, surrenderButton; // Save button added
    private static final String PIECES_PATH = "./pieces/";
    

    public ChessGUI() {
        setupMainMenu();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 800); // Setting size for the window
        setVisible(true);
    }

    private void setupMainMenu() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Chess Game");
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton singlePlayerButton = new JButton("Single Player");
        JButton twoPlayerButton = new JButton("Two Player Hotseat");
        JButton loadGameButton = new JButton("Load Game"); // Load button added
        JButton exitButton = new JButton("Exit");

        getContentPane().removeAll();

        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(title);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        menuPanel.add(singlePlayerButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(twoPlayerButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(loadGameButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        menuPanel.add(exitButton);

        add(menuPanel);
        revalidate();

        singlePlayerButton.addActionListener(e -> setupSinglePlayerMenu());
        twoPlayerButton.addActionListener(e -> startGame(false, true));
        loadGameButton.addActionListener(e -> loadGame()); // Load button logic
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void setupSinglePlayerMenu() {
        JPanel singlePlayerPanel = new JPanel();
        singlePlayerPanel.setLayout(new BoxLayout(singlePlayerPanel, BoxLayout.Y_AXIS));

        JLabel question = new JLabel("Do you want to play as White or Black?");
        question.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton whiteButton = new JButton("Play as White");
        JButton blackButton = new JButton("Play as Black");

        singlePlayerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        singlePlayerPanel.add(question);
        singlePlayerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        singlePlayerPanel.add(whiteButton);
        singlePlayerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        singlePlayerPanel.add(blackButton);

        setContentPane(singlePlayerPanel);
        revalidate();

        whiteButton.addActionListener(e -> startGame(true, true));
        blackButton.addActionListener(e -> startGame(true, false));
    }

    private boolean onePlayerMode = false;
    private boolean playerPlaysWhite = true;

    private void startGame(boolean singlePlayer, boolean playerPlaysWhite) {
        this.onePlayerMode = singlePlayer;
        this.playerPlaysWhite = playerPlaysWhite;

        board = new Board();
        board.setupBoard();
        setupChessBoard();

        // Start the game in a separate thread for non-blocking execution
        Executors.newSingleThreadExecutor().submit(() -> Game());
    }

    private void setupChessBoard() {
        JPanel chessBoardPanel = new JPanel(new BorderLayout());

        JPanel chessGridPanel = new JPanel(new GridLayout(8, 8));
        boardButtons = new JButton[8][8];

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                JButton button = new JButton();
                button.setBackground((x + y) % 2 == 0 ? Color.WHITE : Color.GRAY);
                final int fx = x, fy = y;
                button.addActionListener(e -> handleSquareClick(fx, fy));
                boardButtons[x][y] = button;
                chessGridPanel.add(button);
            }
        }

        undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> handleUndo());

        saveButton = new JButton("Save Game"); // Save button added
        saveButton.addActionListener(e -> saveGame());

        surrenderButton = new JButton("Surrender");
        surrenderButton.addActionListener(e -> handleSurrender());

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.add(undoButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(saveButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(surrenderButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chessGridPanel, BorderLayout.CENTER);
        mainPanel.add(sidePanel, BorderLayout.EAST);

        chessBoardPanel.add(mainPanel, BorderLayout.CENTER);
        chessBoardPanel.setPreferredSize(new Dimension(800, 800));

        setContentPane(chessBoardPanel);
        revalidate();
        updateBoard();
    }


    private void handleSurrender() {
        // Handle the surrender logic
        int response = JOptionPane.showConfirmDialog(this, "Are you sure you want to surrender?", 
                "Surrender", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            String winner = whiteTurn ? "Black" : "White"; // The opponent wins
            JOptionPane.showMessageDialog(this, "You have surrendered. " + winner + " wins!");
            resetGame();
            setupMainMenu(); // Return to the main menu after surrender
        }
    }

    private void Game() {
        while (true) {
            if (board.isCheckmate(whiteTurn)) {
                String winner = whiteTurn ? "Black" : "White"; // The opponent wins
                JOptionPane.showMessageDialog(this, winner + " wins by checkmate!");
                resetGame();
                setupMainMenu(); // Return to the main menu after checkmate
                return;
            }

            if (onePlayerMode) {
                if (whiteTurn == playerPlaysWhite) {
                    // Player's turn
                    waitForPlayerMove(true);
                    promotePawnsIfNeeded();
                } else {
                    // AI's turn
                    board.makeComputerMove(whiteTurn);
                    updateBoard();
                    promotePawnsIfNeeded();  // Check if AI's pawn needs promotion
                }
            } else {
                // Two-player mode
                waitForPlayerMove(whiteTurn);
                promotePawnsIfNeeded();
            }

            whiteTurn = !whiteTurn; // Switch turns
        }
    }
    private void promotePawnsIfNeeded() {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = board.getPieces()[x][y];
                if (piece instanceof Pawn && (x == 0 || x == 7)) {
                    if (onePlayerMode) {
                        if (whiteTurn == playerPlaysWhite) {
                            showPromotionDialog(x, y); // Player's turn
                        } else {
                            board.promotePawn(x, y, 'Q'); // AI automatically promotes to Queen
                            updateBoard();
                        }
                    } else {
                        showPromotionDialog(x, y); // Two player mode
                    }
                }
            }
        }
    }


    private synchronized void waitForPlayerMove(boolean isWhiteTurn) {
        try {
            while (true) {
                wait(); // Wait for player move to complete
                break;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleSquareClick(int x, int y) {
        if (selectedX == -1 && selectedY == -1) {
            Piece piece = board.getPieces()[x][y];
            if (piece.isWhite() == whiteTurn && piece.isNotEmpty()) {
                selectedX = x;
                selectedY = y;
                highlightPossibleMoves(x, y);
            }
        } else {
            Move.MoveType moveType = board.getMoveType(selectedX, selectedY, x, y);
            Move move = new Move(selectedX, selectedY, x, y, moveType);

            if (board.filterMoves(selectedX, selectedY).contains(move) && board.move(selectedX, selectedY, move)) {
                if (isPawnPromotion(x, y)) {
                    if (whiteTurn) {
                        showPromotionDialog(x, y); // Player chooses promotion
                    } else {
                        board.promotePawn(x, y, 'Q'); // AI automatically promotes to queen
                    }
                }
                updateBoard();
                resetSelection();
                synchronized (this) {
                    notify(); // Notify the game thread that the move is complete
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid move. Try again.");
                resetSelection();
            }
        }
    }


    private boolean isPawnPromotion(int x, int y) {
        Piece piece = board.getPieces()[x][y];
        return piece.isNotEmpty() && piece.type() == Piece.PieceType.PAWN &&
               ((piece.isWhite() && x == 0) || (!piece.isWhite() && x == 7));
    }

    private void showPromotionDialog(int x, int y) {
        // Ensure dialog is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            JDialog promotionDialog = new JDialog(this, "Pawn Promotion", true); // Modal dialog
            promotionDialog.setLayout(new GridLayout(1, 4));
            promotionDialog.setSize(300, 100);

            String[] options = {"Queen", "Rook", "Bishop", "Knight"};
            for (String option : options) {
                JButton button = new JButton(option);
                button.addActionListener(e -> {
                    char promotionType = option.charAt(0); // First letter of the option
                    board.promotePawn(x, y, promotionType); // Update board with the selected piece
                    promotionDialog.dispose(); // Close the dialog
                    updateBoard(); // Refresh the board UI
                });
                promotionDialog.add(button);
            }

            promotionDialog.setLocationRelativeTo(this); // Center the dialog relative to the main window
            promotionDialog.setVisible(true); // Show the dialog
        });
    }



    private void highlightPossibleMoves(int x, int y) {
        List<Move> moves = board.filterMoves(x, y);
        for (Move move : moves) {
            int targetX = move.getX();
            int targetY = move.getY();
            boardButtons[targetX][targetY].setBackground(Color.GREEN);
        }
    }

    private void resetSelection() {
        selectedX = -1;
        selectedY = -1;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                boardButtons[x][y].setBackground((x + y) % 2 == 0 ? Color.WHITE : Color.GRAY);
            }
        }
    }

    private void updateBoard() {
        Piece[][] pieces = board.getPieces();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = pieces[x][y];
                if (piece.isNotEmpty()) {
                    String imageFileName = getImageFileName(piece);
                    ImageIcon pieceIcon = new ImageIcon(imageFileName);
                    boardButtons[x][y].setIcon(pieceIcon);
                } else {
                    boardButtons[x][y].setIcon(null); // No piece on this square
                }
            }
        }
    }

    private String getImageFileName(Piece piece) {
        String colorPrefix = piece.isWhite() ? "white" : "black";
        String pieceName = piece.type().toString().toLowerCase();
        return PIECES_PATH + colorPrefix + "-" + pieceName + ".png";
    }

    private void handleUndo() {
        board.restorePreviousState(); // Restore the previous board state
        updateBoard(); // Update the UI to reflect the restored state
        whiteTurn = !whiteTurn; // Switch the turn back to the other player
    }

    private void resetGame() {
        board = new Board();
        board.setupBoard();
        setupChessBoard();
        whiteTurn = true; // Reset turn to white
    }
    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(board); // Save board
                oos.writeBoolean(whiteTurn); // Save current turn
                JOptionPane.showMessageDialog(this, "Game saved successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to save the game: " + e.getMessage());
            }
        }
    }

    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                board = (Board) ois.readObject(); // Load board
                whiteTurn = ois.readBoolean(); // Load current turn
                setupChessBoard();
                JOptionPane.showMessageDialog(this, "Game loaded successfully!");
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Failed to load the game: " + e.getMessage());
            }
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}
