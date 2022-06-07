import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class SudokuBoard {
    private static final int SIZE = 3; // Size of the quiz.
    private static final int LEVEL = 5; // Quiz level. (1 .. 7)
    private SudokuQuiz quiz = new SudokuQuiz(SIZE, LEVEL);

    private final JFrame frame; // The main frame.
    private Cell[][] cells; // Cells of the board.
    private Cell selectedCell = null; // Currently selected cell.

    private final Color selectedCellColor = new Color(135, 206, 250); // Light skyblue
    private final Color fixedCellColor = new Color(149, 186, 238); // Blue onix
    private final Color editableCellColor = new Color(250, 250, 210); // Light goldenrod yellow

    private final Color fixedValueColor = Color.BLACK;
    private final Color validValueColor = Color.BLUE;
    private final Color invalidValueColor = Color.RED;

    public SudokuBoard() {
        frame = new JFrame("Sudoku");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        buildFrame();

        quiz.newQuiz();
        updateBoard();

        frame.pack();
        frame.setVisible(true);
    }

    private void buildFrame() {
        final int GAP = 4; // Gaps between boardPanels.

        JPanel commandPanel = new JPanel();
        frame.add(commandPanel, BorderLayout.NORTH);

        CommandPanelListener commandPanelListener = new CommandPanelListener();
        for (String s : new String[] { "New", "Hint", "Solve", "Reset", "Undo" }) {
            JButton button = new JButton(s);
            button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            button.addActionListener(commandPanelListener);
            commandPanel.add(button);
        }

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

        Number[] numbers = new Number[9];
        NumberPanelListener numberPanelListener = new NumberPanelListener();
        for (int i = 1; i <= 9; i++) {
            Number number = new Number(i);
            number.addActionListener(numberPanelListener);
            numbers[i - 1] = number;
            numberPanel.add(number);
        }
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] reminings = quiz.getRemainings();
                for (int i = 1; i <= 9; i++) {
                    numbers[i - 1].setEnabled(reminings[i - 1] != 0);
                }
            }
        });
        timer.start();
    }

    /**
     * Updates the board with the current state of the quiz.
     */
    private void updateBoard() {
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                Cell cell = cells[row][col];
                int value = quiz.getValue(row, col);
                if (quiz.isFixed(row, col)) {
                    cell.setValue(value, fixedValueColor);
                    cell.setEnabled(false);
                    cell.setBackground(fixedCellColor);
                } else {
                    cell.setValue(value, validValueColor);
                    cell.setEnabled(true);
                    cell.setBackground(editableCellColor);
                }
            }
        }
    }

    /**
     * Class for Sudoku cells.
     */
    class Cell extends JButton {
        private final Dimension dimension = new Dimension(40, 40); // size of cell.
        private final int row, col; // row and column of cell.
        private int value; // value of cell.

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.value = 0;

            setBorder(BorderFactory.createLineBorder(Color.GRAY));
            setPreferredSize(dimension);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
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

        public void select() {
            setBackground(selectedCellColor);
        }

        public void deselect() {
            setBackground(editableCellColor);
        }
    }

    /**
     * Class for number buttons.
     */
    class Number extends JButton {
        private final Dimension dimension = new Dimension(40, 40); // size of buttons.
        private final int value; // value of button.

        Number(int value) {
            super(String.valueOf(value));
            this.value = value;

            setPreferredSize(dimension);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * ActionListener for the command buttons.
     */
    public class CommandPanelListener implements ActionListener {
        /**
         * Executes the command of the button.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "New":
                    quiz.newQuiz();
                    updateBoard();
                    break;
                case "Hint":
                    SudokuSolver solver = new SudokuSolver(quiz);
                    SudokuSolver.Hint[] hints = solver.getHints();
                    if (hints.length == 0) {
                        JOptionPane.showMessageDialog(frame, "No hints available.", "Hint", JOptionPane.PLAIN_MESSAGE);
                    } else {
                        int row = hints[0].row;
                        int col = hints[0].col;
                        int value = hints[0].value;
                        boolean possible = quiz.setValue(row, col, value);

                        if (selectedCell != null) {
                            selectedCell.deselect();
                            selectedCell = null;
                        }
                        Cell cell = cells[row][col];
                        cell.setValue(value, possible ? validValueColor : invalidValueColor);
                        cell.select();
                        selectedCell = cell;
                    }
                    break;
                case "Solve":
                    if (quiz.solve()) {
                        updateBoard();
                    } else {
                        JOptionPane.showMessageDialog(frame, "No solution found.", "Solve", JOptionPane.PLAIN_MESSAGE);
                    }
                    break;
                case "Reset":
                    quiz.resetQuiz();
                    updateBoard();
                    break;
                case "Undo":
                    SudokuQuiz.Operation operation = quiz.undo();
                    if (operation != null) {
                        if (selectedCell != null) {
                            selectedCell.deselect();
                            selectedCell = null;
                        }
                        Cell cell = cells[operation.row][operation.col];
                        cell.setValue(operation.oldValue,
                                quiz.isPossible(operation.row, operation.col, operation.oldValue)
                                        ? validValueColor
                                        : invalidValueColor);
                        cell.select();
                        selectedCell = cell;
                    }
                    break;
            }
        }
    }

    /**
     * Action listener for the cells.
     */
    public class BoardPanelListener implements ActionListener {
        /**
         * Select cell to set value.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedCell != null) {
                selectedCell.deselect();
            }
            Cell cell = (Cell) e.getSource();
            cell.select();
            selectedCell = cell;
        }
    }

    /**
     * Action listener for the number buttons.
     */
    public class NumberPanelListener implements ActionListener {
        /**
         * If cell is selected, set the value of the cell to the number.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedCell != null) {
                Number number = (Number) e.getSource();
                int value = number.getValue();
                boolean possible = quiz.setValue(selectedCell.getRow(), selectedCell.getCol(), value);
                selectedCell.setValue(value, possible ? validValueColor : invalidValueColor);
                if (possible && quiz.getRemaining() == 0) {
                    JOptionPane.showMessageDialog(frame, "Congratuation, you solved the quiz!", "Sudoku",
                            JOptionPane.PLAIN_MESSAGE);
                }
            }
        }
    }
}
