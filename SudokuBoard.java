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
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SudokuBoard {
    private final int size; // Size of the quiz.
    private final int level; // Quiz level. (1 .. 7)
    private SudokuQuiz quiz; // Quiz.

    private final JFrame frame; // The main frame.
    private Cell[][] cells; // Cells of the board.
    private Cell selectedCell = null; // Currently selected cell.

    private boolean solving = false; // Only one can solve at a time.

    private final Color selectedCellColor = new Color(135, 206, 250); // Light skyblue
    private final Color fixedCellColor = new Color(149, 186, 238); // Blue onix
    private final Color editableCellColor = new Color(250, 250, 210); // Light goldenrod yellow

    private final Color fixedValueColor = Color.BLACK;
    private final Color validValueColor = Color.BLUE;
    private final Color invalidValueColor = Color.RED;

    /**
     * Constructor.
     * 
     * @param size  Size of the quiz.
     * @param level Quiz level. (1 .. 7)
     */
    public SudokuBoard(int size, int level) {
        this.size = size;
        this.level = level;
        this.quiz = new SudokuQuiz(this.size, this.level);

        frame = new JFrame("Sudoku");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        buildFrame();
        newQuiz();

        frame.pack();
        frame.setVisible(true);
    }

    private void buildFrame() {
        final int GAP = 4; // Gaps between boardPanels.

        JPanel commandPanel = new JPanel();
        frame.add(commandPanel, BorderLayout.NORTH);

        for (String s : new String[] { "New", "Hint", "Solve", "Reset", "Undo" }) {
            JButton button = new JButton(s);
            button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    switch (e.getActionCommand()) {
                        case "New":
                            newQuiz();
                            break;
                        case "Hint":
                            hint();
                            break;
                        case "Solve":
                            solveQuiz();
                            break;
                        case "Reset":
                            resetQuiz();
                            break;
                        case "Undo":
                            undo();
                            break;
                    }
                }
            });
            commandPanel.add(button);
        }

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(size, size, GAP, GAP));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        frame.add(boardPanel, BorderLayout.CENTER);

        JPanel[][] blocks = new JPanel[size][size];
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

        cells = new Cell[size * 3][size * 3];
        for (int row = 0; row < cells.length; row++) {
            for (int col = 0; col < cells[row].length; col++) {
                Cell cell = new Cell(row, col);
                cell.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Cell cell = (Cell) e.getSource();
                        selectCell(cell.getRow(), cell.getCol());
                    }
                });
                cells[row][col] = cell;
                blocks[row / 3][col / 3].add(cell);
            }
        }

        JPanel numberPanel = new JPanel();
        frame.add(numberPanel, BorderLayout.SOUTH);

        Number[] numbers = new Number[9];
        for (int i = 1; i <= 9; i++) {
            Number number = new Number(i);
            number.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Number number = (Number) e.getSource();
                    setValue(number.getValue());
                }
            });
            numbers[i - 1] = number;
            numberPanel.add(number);
        }

        Timer timer = new Timer(500, new ActionListener() {
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
     * Set the board with the current quiz.
     */
    private void setBoard() {
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
        selectedCell = null;
    }

    /**
     * New quiz.
     */
    public void newQuiz() {
        quiz.newQuiz();
        setBoard();
    }

    /**
     * Resets the quiz.
     */
    public void resetQuiz() {
        quiz.resetQuiz();
        setBoard();
    }

    /**
     * Provides a hint.
     */
    public void hint() {
        SudokuSolver solver = new SudokuSolver(quiz);
        SudokuSolver.Hint[] hints = solver.getHints();
        if (hints.length == 0) {
            JOptionPane.showMessageDialog(frame, "No hints available.", "Hint", JOptionPane.PLAIN_MESSAGE);
        } else {
            selectCell(hints[0].row, hints[0].col);
            setValue(hints[0].value);
        }
    }

    /**
     * Solves the quiz.
     */
    public void solveQuiz() {
        if (!solving) { // Only one thread can solve at a time.
            SudokuSolver solver = new SudokuSolver(quiz);
            SudokuSolver.Hint[] hints = solver.getHints();

            solving = true;
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (SudokuSolver.Hint hint : hints) {
                        if (quiz.isFixed(hint.row, hint.col) || !quiz.isPossible(hint.row, hint.col, hint.value)) {
                            break; // Someone changed the quiz.
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                selectCell(hint.row, hint.col);
                                setValue(hint.value);
                            }
                        });
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (quiz.getRemaining() > 0) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(frame, "Can’t solve it any more.", "Solve",
                                        JOptionPane.PLAIN_MESSAGE);
                            }
                        });
                    }
                    solving = false;
                }
            };
            thread.start();
        }
    }

    /**
     * Undo the last change.
     */
    public void undo() {
        SudokuQuiz.Operation operation = quiz.undo();
        if (operation != null) {
            if (selectedCell != null) {
                selectedCell.unselect();
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
    }

    /**
     * Selects a cell.
     *
     * @param row the row of the cell.
     * @param col the column of the cell.
     */
    public void selectCell(int row, int col) {
        if (!quiz.isFixed(row, col)) {
            if (selectedCell != null) {
                selectedCell.unselect();
            }
            selectedCell = cells[row][col];
            selectedCell.select();
        }
    }

    /**
     * Sets the value of the selected cell.
     *
     * @param value the value to set.
     */
    public void setValue(int value) {
        if (selectedCell != null && 1 <= value && value <= 9) {
            boolean possible = quiz.setValue(selectedCell.getRow(), selectedCell.getCol(), value);
            selectedCell.setValue(value, possible ? validValueColor : invalidValueColor);
            if (possible && quiz.getRemaining() == 0) {
                JOptionPane.showMessageDialog(frame, "Congratuation, you solved the quiz!", "Sudoku",
                        JOptionPane.PLAIN_MESSAGE);
            }
        }
    }

    /**
     * Class for Sudoku cells.
     */
    private class Cell extends JButton {
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

        public void setValue(int value, Color color) {
            this.value = value;
            setForeground(color);
            setText(this.value == 0 ? "" : String.valueOf(this.value));
        }

        public void select() {
            setBackground(selectedCellColor);
        }

        public void unselect() {
            setBackground(editableCellColor);
        }
    }

    /**
     * Class for number buttons.
     */
    private class Number extends JButton {
        private final Dimension dimension = new Dimension(40, 40); // size of buttons.
        private final int value; // value of button.

        Number(int value) {
            super(String.valueOf(value));
            this.value = value;

            setPreferredSize(dimension);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        }

        public int getValue() {
            return value;
        }
    }
}