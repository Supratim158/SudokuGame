import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SudokuGame extends JFrame {
    private JTextField[][] cells = new JTextField[9][9];
    private int[][] board = new int[9][9];
    private int[][] solution = new int[9][9];
    private Timer timer;
    private JLabel timerLabel;
    private int secondsElapsed = 0;
    private JButton checkButton, newGameButton, resetButton;
    private JComboBox<String> difficultyComboBox;

    public SudokuGame() {
        setTitle("Sudoku Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize GUI components
        initializeBoard();
        initializeControlPanel();

        // Start timer
        timer = new Timer(1000, e -> {
            secondsElapsed++;
            timerLabel.setText("Time: " + formatTime(secondsElapsed));
        });
        timer.start();

        // Generate new puzzle
        generatePuzzle("Medium");
    }

    private void initializeBoard() {
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 5, 5));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JPanel subGrid = new JPanel(new GridLayout(3, 3, 2, 2));
                subGrid.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                for (int row = i * 3; row < i * 3 + 3; row++) {
                    for (int col = j * 3; col < j * 3 + 3; col++) {
                        cells[row][col] = new JTextField(1);
                        cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                        cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                        cells[row][col].addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyTyped(KeyEvent e) {
                                char c = e.getKeyChar();
                                if (!Character.isDigit(c) || c < '1' || c > '9') {
                                    e.consume();
                                }
                            }
                        });
                        subGrid.add(cells[row][col]);
                    }
                }
                boardPanel.add(subGrid);
            }
        }
        add(boardPanel, BorderLayout.CENTER);
    }

    private void initializeControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        timerLabel = new JLabel("Time: 00:00");
        checkButton = new JButton("Check");
        newGameButton = new JButton("New Game");
        resetButton = new JButton("Reset");
        difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        checkButton.addActionListener(e -> checkSolution());
        newGameButton.addActionListener(e -> {
            timer.stop();
            secondsElapsed = 0;
            timerLabel.setText("Time: 00:00");
            generatePuzzle((String) difficultyComboBox.getSelectedItem());
            timer.start();
        });
        resetButton.addActionListener(e -> resetBoard());

        controlPanel.add(timerLabel);
        controlPanel.add(checkButton);
        controlPanel.add(newGameButton);
        controlPanel.add(resetButton);
        controlPanel.add(new JLabel("Difficulty:"));
        controlPanel.add(difficultyComboBox);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void generatePuzzle(String difficulty) {
        // Clear board
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = 0;
                solution[i][j] = 0;
                cells[i][j].setText("");
                cells[i][j].setEditable(true);
                cells[i][j].setBackground(Color.WHITE);
            }
        }

        // Fill diagonal 3x3 boxes
        Random rand = new Random();
        for (int box = 0; box < 9; box += 4) {
            fillBox(box / 3 * 3, box % 3 * 3, rand);
        }

        // Solve the board to get a complete solution
        solveBoard(0, 0);
        copyBoard(solution, board);

        // Remove numbers based on difficulty
        int cellsToRemove;
        switch (difficulty) {
            case "Easy":
                cellsToRemove = 30;
                break;
            case "Medium":
                cellsToRemove = 40;
                break;
            case "Hard":
                cellsToRemove = 50;
                break;
            default:
                cellsToRemove = 40;
        }

        while (cellsToRemove > 0) {
            int row = rand.nextInt(9);
            int col = rand.nextInt(9);
            if (board[row][col] != 0) {
                board[row][col] = 0;
                cellsToRemove--;
            }
        }

        // Update GUI
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    cells[i][j].setText(String.valueOf(board[i][j]));
                    cells[i][j].setEditable(false);
                    cells[i][j].setBackground(Color.LIGHT_GRAY);
                }
            }
        }
    }

    private void fillBox(int row, int col, Random rand) {
        int num;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                do {
                    num = rand.nextInt(9) + 1;
                } while (!isValid(board, row + i, col + j, num));
                board[row + i][col + j] = num;
            }
        }
    }

    private boolean solveBoard(int row, int col) {
        if (row == 9) {
            copyBoard(board, solution);
            return true;
        }
        if (col == 9) {
            return solveBoard(row + 1, 0);
        }
        if (board[row][col] != 0) {
            return solveBoard(row, col + 1);
        }
        Random rand = new Random();
        int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        shuffleArray(numbers, rand);
        for (int num : numbers) {
            if (isValid(board, row, col, num)) {
                board[row][col] = num;
                if (solveBoard(row, col + 1)) {
                    return true;
                }
                board[row][col] = 0;
            }
        }
        return false;
    }

    private void shuffleArray(int[] arr, Random rand) {
        for (int i = arr.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            int temp = arr[index];
            arr[index] = arr[i];
            arr[i] = temp;
        }
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        // Check row
        for (int j = 0; j < 9; j++) {
            if (board[row][j] == num) return false;
        }
        // Check column
        for (int i = 0; i < 9; i++) {
            if (board[i][col] == num) return false;
        }
        // Check 3x3 box
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[startRow + i][startCol + j] == num) return false;
            }
        }
        return true;
    }

    private void copyBoard(int[][] src, int[][] dest) {
        for (int i = 0; i < 9; i++) {
            System.arraycopy(src[i], 0, dest[i], 0, 9);
        }
    }

    private void checkSolution() {
        int[][] current = new int[9][9];
        boolean isComplete = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText();
                if (text.isEmpty()) {
                    isComplete = false;
                    current[i][j] = 0;
                } else {
                    current[i][j] = Integer.parseInt(text);
                }
            }
        }

        boolean isValid = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (current[i][j] != 0) {
                    int temp = current[i][j];
                    current[i][j] = 0;
                    if (!isValid(current, i, j, temp)) {
                        isValid = false;
                        cells[i][j].setBackground(Color.RED);
                    } else {
                        cells[i][j].setBackground(cells[i][j].isEditable() ? Color.WHITE : Color.LIGHT_GRAY);
                    }
                    current[i][j] = temp;
                }
            }
        }

        if (isValid && isComplete) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! Puzzle solved in " + formatTime(secondsElapsed) + "!");
        } else if (!isValid) {
            JOptionPane.showMessageDialog(this, "There are errors in the puzzle.");
        } else {
            JOptionPane.showMessageDialog(this, "Puzzle is incomplete but valid so far.");
        }
    }

    private void resetBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].isEditable()) {
                    cells[i][j].setText("");
                    cells[i][j].setBackground(Color.WHITE);
                }
            }
        }
    }

    private String formatTime(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SudokuGame().setVisible(true));
    }
}
