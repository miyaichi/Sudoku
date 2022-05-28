import java.util.Deque;
import java.util.ArrayDeque;

public class SudokuQuiz {
    private int size; // Game size.
    private int[][] quiz; // Sudoku quiz.
    private int[][] board; // Current sudoku board.

    /**
     * Class for operation history.
     */
    public class Operation {
        private int row, col; // row and column of cell.
        private int oldValue; // old value of cell.
        private int newValue; // new value of cell.

        Operation(int row, int col, int oldValue, int newValue) {
            this.row = row;
            this.col = col;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getOldValue() {
            return oldValue;
        }

        public int getNewValue() {
            return newValue;
        }
    }

    private Deque<Operation> operations; // List of operations.

    public SudokuQuiz(int size) {
        this.size = size;
        quiz = new int[size * 3][size * 3];
        board = new int[size * 3][size * 3];
        operations = new ArrayDeque<>();
    }

    /**
     * A utility function to print the board
     * 
     * @param matrix
     */
    public void dumpBoard() {
        dumpBoard(board);
    }

    public void dumpBoard(int[][] matrix) {
        System.out.println("-----+-----+-----+");
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                int value = matrix[row][col];
                System.out.print((value == 0 ? "." : value) + " ");
            }
            System.out.println();
        }
        System.out.println("-----+-----+-----+");
    }

    /**
     * Set value on the board.
     * 
     * @param row
     * @param col
     * @param value
     */
    public boolean setValue(int row, int col, int value) {
        if (!isEditable(row, col)) {
            return false;
        }
        boolean possible = isPossible(row, col, value);
        operations.add(new Operation(row, col, board[row][col], value));
        board[row][col] = value;
        return possible;
    }

    /**
     * Get value of board.
     * 
     * @param row
     * @param col
     * @return
     */
    public int getValue(int row, int col) {
        return board[row][col];
    }

    /**
     * Undo the last operation.
     */
    public Operation undo() {
        if (operations.isEmpty()) {
            return null;
        }
        Operation operation = operations.removeLast();
        board[operation.row][operation.col] = operation.oldValue;
        return operation;
    }

    /**
     * Check if board[row, col] is editable.
     * 
     * @param row
     * @param col
     * @return true: editable, false: not editable
     */
    public boolean isEditable(int row, int col) {
        return quiz[row][col] == 0;
    }

    /**
     * Check if value is possible to put in the board[row, col].
     * 
     * @param row
     * @param col
     * @param value
     */
    public boolean isPossible(int row, int col, int value) {
        return isPossible(board, row, col, value);
    }

    public boolean isPossible(int[][] matrix, int row, int col, int value) {
        for (int i = 0; i < size * 3; i++) {
            if (matrix[row][i] == value || matrix[i][col] == value) {
                return false;
            }
        }
        int rowStart = row / 3 * 3;
        int colStart = col / 3 * 3;
        for (int i = rowStart; i < rowStart + 3; i++) {
            for (int j = colStart; j < colStart + 3; j++) {
                if (matrix[i][j] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if the board is solved.
     * 
     * @return true: solved, false: there are blank cells.
     */
    public boolean isSolved() {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Reset the board to the quiz.
     */
    public void resetQuiz() {
        for (int row = 0; row < quiz.length; row++) {
            for (int col = 0; col < quiz[row].length; col++) {
                board[row][col] = quiz[row][col];
            }
        }
        operations.clear();
    }

    /**
     * Create the new quiz.
     */
    public void newQuiz() {
        while (true) {
            int row, col;

            // Clear quiz
            for (row = 0; row < quiz.length; row++) {
                for (col = 0; col < quiz[row].length; col++) {
                    quiz[row][col] = 0;
                }
            }

            // Generate sloable quiz
            row = (int) (Math.random() * size * 3);
            col = (int) (Math.random() * size * 3);
            quiz[row][col] = (int) (Math.random() * 9 + 1);
            if (!solve(quiz)) {
                continue;
            }

            // Blank cells at random
            for (int i = 0; i < 52; i++) {
                while (true) {
                    row = (int) (Math.random() * size * 3);
                    col = (int) (Math.random() * size * 3);
                    if (quiz[row][col] != 0) {
                        break;
                    }
                }
                quiz[row][col] = 0;
            }

            // Check if the quiz is solvable.
            int[][] temp = new int[quiz.length][];
            for (int i = 0; i < quiz.length; i++) {
                temp[i] = quiz[i].clone();
            }
            if (solve(temp)) {
                break;
            }
        }

        for (int row = 0; row < quiz.length; row++) {
            for (int col = 0; col < quiz[row].length; col++) {
                board[row][col] = quiz[row][col];
            }
        }
        operations.clear();
    }

    /**
     * Provide the hint for the next cell.
     * 
     * @param row
     * @param col
     * @return value
     */
    public int getHint(int row, int col) {
        return getHint(board, row, col);
    }

    public int getHint(int[][] matrix, int row, int col) {
        for (int value = 1; value <= 9; value++) {
            if (isPossible(matrix, row, col, value)) {
                int[][] temp = new int[matrix.length][];
                for (int i = 0; i < matrix.length; i++) {
                    temp[i] = matrix[i].clone();
                }
                temp[row][col] = value;
                if (solve(temp)) {
                    return value;
                }
            }
        }
        return 0;
    }

    /**
     * Solve the board.
     * 
     * @return true: solved, false: cannot solve.
     */
    public boolean solve() {
        boolean solved = solve(board);
        if (solved) {
            operations.clear();
        }
        return solved;
    }

    public boolean solve(int[][] matrix) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                if (matrix[row][col] == 0) {
                    for (int value = 1; value <= size * 3; value++) {
                        if (isPossible(matrix, row, col, value)) {
                            matrix[row][col] = value;
                            if (solve(matrix)) {
                                return true;
                            }
                            matrix[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
}