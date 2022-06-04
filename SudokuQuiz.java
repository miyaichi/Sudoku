import java.util.ArrayDeque;
import java.util.Deque;

public class SudokuQuiz {
    private int level; // Quiz level. (1 .. 7)
    private int size; // Quiz size.
    private int[][] quiz; // Sudoku quiz.
    private int[][] board; // Current sudoku board.
    private Deque<Operation> operations; // List of operations.

    public SudokuQuiz(int size, int level) {
        this.size = size;
        this.level = level;
        quiz = new int[size * 3][size * 3];
        board = new int[size * 3][size * 3];
        operations = new ArrayDeque<>();
    }

    /**
     * A utility function to print the board
     * 
     * @param board
     */
    public void dumpBoard() {
        dumpBoard(board);
    }

    public void dumpBoard(int[][] board) {
        System.out.println("-------------------");
        System.out.println("  0 1 2 3 4 5 6 7 8");
        for (int row = 0; row < board.length; row++) {
            System.out.print(row + " ");
            for (int col = 0; col < board[row].length; col++) {
                int value = board[row][col];
                System.out.print((value == 0 ? "." : value) + " ");
            }
            System.out.println();
        }
        System.out.println("-------------------");
    }

    /**
     * Get the quiz size.
     * 
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * Get current quiz.
     * 
     * @return quiz
     */
    public int[][] getQuiz() {
        return quiz;
    }

    /**
     * Get current board.
     * 
     * @return board
     */
    public int[][] getBoard() {
        return board;
    }

    /**
     * Clone the board.
     * 
     * @param board
     * @return
     */
    public int[][] cloneBoard(int[][] board) {
        int[][] clone = new int[board.length][];
        for (int i = 0; i < board.length; i++) {
            clone[i] = board[i].clone();
        }
        return clone;
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

    public boolean isPossible(int[][] board, int row, int col, int value) {
        for (int i = 0; i < size * 3; i++) {
            if (board[row][i] == value || board[i][col] == value) {
                return false;
            }
        }
        int rowStart = row / 3 * 3;
        int colStart = col / 3 * 3;
        for (int i = rowStart; i < rowStart + 3; i++) {
            for (int j = colStart; j < colStart + 3; j++) {
                if (board[i][j] == value) {
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

            // Clear quiz.
            for (row = 0; row < quiz.length; row++) {
                for (col = 0; col < quiz[row].length; col++) {
                    quiz[row][col] = 0;
                }
            }

            // Generate solvable quiz.
            row = (int) (Math.random() * size * 3);
            col = (int) (Math.random() * size * 3);
            quiz[row][col] = (int) (Math.random() * 9 + 1);
            if (!solve(quiz)) {
                continue;
            }

            // Erase cells block by block.
            for (int l = level; l > 0; l--) {
                for (int b = 0; b < size * size; b++) {
                    while (true) {
                        row = (int) (b / size * 3 + Math.random() * size);
                        col = (int) (b % size * 3 + Math.random() * size);
                        if (quiz[row][col] != 0) {
                            quiz[row][col] = 0;
                            break;
                        }
                    }
                }
            }

            // Check if the quiz is solvable.
            if (solve(cloneBoard(quiz))) {
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
     * Solve the board with backtracking.
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

    public boolean solve(int[][] board) {
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == 0) {
                    for (int value = 1; value <= size * 3; value++) {
                        if (isPossible(board, row, col, value)) {
                            board[row][col] = value;
                            if (solve(board)) {
                                return true;
                            }
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Class for operation history.
     */
    public class Operation {
        public final int row, col; // row and column of cell.
        public final int oldValue; // old value of cell.
        public final int newValue; // new value of cell.

        Operation(int row, int col, int oldValue, int newValue) {
            this.row = row;
            this.col = col;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
}
