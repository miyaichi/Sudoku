import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class SudokuBoard {

    private static final int SIZE = 3; // Size of the game.
    SudokuQuiz quiz = new SudokuQuiz(SIZE);

    private final JFrame frame;
    private Cell[][] cells;
    private Cell selectedCell = null;
    private Color selectedColor = new Color(109, 204, 218); // light blue
    private Color defaultColor = Color.WHITE;
    private Color validColor = Color.BLUE;
    private Color invalidColor = Color.RED;

    public SudokuBoard() {
        frame = new JFrame("Sudoku");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        buildBoard();

        quiz.newQuiz();
        setBoard(quiz.getBoard());

        frame.pack();
        frame.setVisible(true);
    }

    void buildBoard() {
        final int GAP = 2; // Gaps between boardPanels.

        JPanel commandPanel = new JPanel();
        CommandPanelListener commandPanelListener = new CommandPanelListener();
        for (String s : new String[] { "New", "Hint", "Solve", "Reset", "Del" }) {
            JButton button = new JButton(s);
            button.addActionListener(commandPanelListener);
            commandPanel.add(button);
        }
        frame.add(commandPanel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(SIZE, SIZE, GAP, GAP));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        frame.add(boardPanel, BorderLayout.CENTER);

        JPanel[][] blocks = new JPanel[SIZE][SIZE];
        for (int row = 0; row < blocks.length; row++) {
            for (int col = 0; col < blocks[row].length; col++) {
                JPanel block = new JPanel();
                block = new JPanel();
                block.setLayout(new GridLayout(3, 3, 0, 0));
                block.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                blocks[row][col] = block;
                boardPanel.add(block);
            }
        }

        cells = new Cell[SIZE * 3][SIZE * 3];
        BoardPanelListener boardPanelListener = new BoardPanelListener();
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                Cell cell = new Cell(row, col);
                cell.addActionListener(boardPanelListener);
                cells[row][col] = cell;
                blocks[row / 3][col / 3].add(cell);
            }
        }

        JPanel numberPanel = new JPanel();
        frame.add(numberPanel, BorderLayout.SOUTH);

        NumberPanelListener numberPanelListener = new NumberPanelListener();
        Dimension dimension = new Dimension(40, 40); // size of buttons.
        for (int i = 1; i <= 9; i++) {
            JButton button = new JButton(String.valueOf(i));
            button.addActionListener(numberPanelListener);
            button.setPreferredSize(dimension);
            numberPanel.add(button);
        }
    }

    public void setBoard(int[][] board) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Cell cell = cells[row][col];
                int value = board[row][col];
                cell.setValue(value, Color.BLACK);
                if (value == 0) {
                    cell.setEnabled(true);
                    cell.setBackground(Color.WHITE);
                } else {
                    cell.setEnabled(false);
                    cell.setBackground(Color.LIGHT_GRAY);
                }
            }
        }
    }

    public void updateBoard(int[][] board) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                Cell cell = cells[row][col];
                int value = board[row][col];
                cell.setValue(value, Color.BLACK);
            }
        }
    }

    class Cell extends JButton {
        private Dimension dimension = new Dimension(40, 40); // size of cell.
        private int row, col; // row and column of cell.
        private int value; // value of cell.

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.value = 0;

            setBackground(defaultColor);
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(dimension);
            setOpaque(true);
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value, Color color) {
            this.value = value;
            setForeground(color);
            setText(this.value == 0 ? "" : String.valueOf(this.value));
        }
    }

    public class CommandPanelListener implements ActionListener {
        /**
         * Executes the command of the button.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("New")) {
                quiz.newQuiz();
                setBoard(quiz.getBoard());
            } else if (command.equals("Hint")) {
                if (selectedCell == null) {
                    JOptionPane.showMessageDialog(frame, "Select a cell first.");
                } else {
                    int value = quiz.getHint(selectedCell.getRow(), selectedCell.getCol());
                    if (value == 0) {
                        JOptionPane.showMessageDialog(frame, "No hint available.");
                    } else {
                        quiz.setValue(selectedCell.getRow(), selectedCell.getCol(), value);
                        selectedCell.setValue(value, validColor);
                    }
                }
            } else if (command.equals("Solve")) {
                if (quiz.solve()) {
                    updateBoard(quiz.getBoard());
                } else {
                    JOptionPane.showMessageDialog(frame, "No solution found.");
                }
            } else if (command.equals("Reset")) {
                quiz.resetQuiz();
                setBoard(quiz.getBoard());
            } else if (command.equals("Del")) {
                if (selectedCell == null) {
                    JOptionPane.showMessageDialog(frame, "Select a cell first.");
                } else {
                    selectedCell.setValue(0, validColor);
                }
            }
        }
    }

    public class BoardPanelListener implements ActionListener {
        /**
         * Select cell to set value.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedCell != null) {
                selectedCell.setBackground(defaultColor);
            }
            Cell cell = (Cell) e.getSource();
            cell.setBackground(selectedColor);
            selectedCell = cell;
        }
    }

    public class NumberPanelListener implements ActionListener {
        /**
         * If cell is selected, set the value of the cell to the number.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedCell != null) {
                JButton button = (JButton) e.getSource();
                int value = Integer.parseInt(button.getText());
                boolean possible = quiz.setValue(selectedCell.getRow(), selectedCell.getCol(), value);
                selectedCell.setValue(value, possible ? validColor : invalidColor);
                if (possible && quiz.isSolved()) {
                    JOptionPane.showMessageDialog(null, "Congratulations, you win!");
                }
            }
        }
    }
}