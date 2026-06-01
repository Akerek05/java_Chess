import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

public class ChessGUI extends JFrame {
    private GameController controller;
    private JButton[][] boardButtons;
    private static final String PIECES_PATH = "./pieces/";
    private JPanel mainPanel;

    public ChessGUI() {
        this.controller = new GameController(new Board(), this);
        setupMainMenu();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850, 800);
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
        JButton loadGameButton = new JButton("Load Game");
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
        repaint();

        singlePlayerButton.addActionListener(e -> setupSinglePlayerMenu());
        twoPlayerButton.addActionListener(e -> startGame(false, true));
        loadGameButton.addActionListener(e -> loadGame());
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

    private void startGame(boolean singlePlayer, boolean playerPlaysWhite) {
        setupChessBoard();
        controller.startNewGame(singlePlayer, playerPlaysWhite);
    }

    private void setupChessBoard() {
        JPanel chessBoardPanel = new JPanel(new BorderLayout());
        JPanel chessGridPanel = new JPanel(new GridLayout(Board.SIZE, Board.SIZE));
        boardButtons = new JButton[Board.SIZE][Board.SIZE];

        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                JButton button = new JButton();
                button.setBackground((x + y) % 2 == 0 ? Color.WHITE : Color.GRAY);
                final int fx = x, fy = y;
                button.addActionListener(e -> controller.handleSquareClick(fx, fy));
                boardButtons[x][y] = button;
                chessGridPanel.add(button);
            }
        }

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> controller.handleUndo());

        JButton saveButton = new JButton("Save Game");
        saveButton.addActionListener(e -> saveGame());

        JButton surrenderButton = new JButton("Surrender");
        surrenderButton.addActionListener(e -> controller.handleSurrender());

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.add(undoButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(saveButton);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidePanel.add(surrenderButton);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chessGridPanel, BorderLayout.CENTER);
        mainPanel.add(sidePanel, BorderLayout.EAST);

        chessBoardPanel.add(mainPanel, BorderLayout.CENTER);
        chessBoardPanel.setPreferredSize(new Dimension(800, 800));

        setContentPane(chessBoardPanel);
        revalidate();
    }

    public void updateBoard(Piece[][] pieces) {
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                Piece piece = pieces[x][y];
                if (piece.isNotEmpty()) {
                    String imageFileName = getImageFileName(piece);
                    boardButtons[x][y].setIcon(new ImageIcon(imageFileName));
                } else {
                    boardButtons[x][y].setIcon(null);
                }
            }
        }
    }

    private String getImageFileName(Piece piece) {
        String colorPrefix = piece.isWhite() ? "white" : "black";
        String pieceName = piece.type().toString().toLowerCase();
        return PIECES_PATH + colorPrefix + "-" + pieceName + ".png";
    }

    public void highlightMoves(List<Move> moves) {
        clearHighlights();
        for (Move move : moves) {
            boardButtons[move.getX()][move.getY()].setBackground(Color.GREEN);
        }
    }

    public void clearHighlights() {
        for (int x = 0; x < Board.SIZE; x++) {
            for (int y = 0; y < Board.SIZE; y++) {
                boardButtons[x][y].setBackground((x + y) % 2 == 0 ? Color.WHITE : Color.GRAY);
            }
        }
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public void returnToMainMenu() {
        setupMainMenu();
    }

    public char showPromotionDialog() {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int selection = JOptionPane.showOptionDialog(this, "Choose piece for promotion:", 
                "Pawn Promotion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, options, options[0]);
        if (selection == -1) return 'Q';
        return options[selection].charAt(0);
    }

    private void saveGame() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(controller.getBoard());
                oos.writeBoolean(controller.getWhiteTurn());
                showMessage("Game saved successfully!");
            } catch (IOException e) {
                showMessage("Failed to save: " + e.getMessage());
            }
        }
    }

    private void loadGame() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Board board = (Board) ois.readObject();
                boolean whiteTurn = ois.readBoolean();
                
                setupChessBoard();
                this.controller = new GameController(board, this);
                this.controller.setWhiteTurn(whiteTurn);
                updateBoard(board.getPieces());
                showMessage("Game loaded successfully!");
            } catch (IOException | ClassNotFoundException e) {
                showMessage("Failed to load: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}