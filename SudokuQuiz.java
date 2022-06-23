import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class SudokuQuiz {
    private final int level; // Quiz level. (1 .. 7)
    private final int size; // Quiz size.
    private int[][] quiz; // Sudoku quiz.
    private int[][] board; // Current sudoku board.
    private Deque<Operation> operations; // List of operations.

    public SudokuQuiz(int size, int level) {
        if (level < 1 && level > 7) {
            throw new IllegalArgumentException("Level must be between 1 and 7.");
        }
        this.size = size;
        this.level = level;
        quiz = new int[size * 3][size * 3];
        board = new int[size * 3][size * 3];
        operations = new ArrayDeque<>();
    }

    /**
     * A utility function to print the board
     */
    public void dumpBoard() {
        dumpBoard(board);
    }

    public void dumpBoard(int[][] board) {
        String border;
        border = " ";
        for (int i = 0; i < size * 3; i++) {
            border += "--";
        }
        System.out.println(border);
        border = " ";
        for (int i = 0; i < size * 3; i++) {
            border += " " + (i % 10);
        }
        System.out.println(border);

        for (int row = 0; row < board.length; row++) {
            System.out.print(row + " ");
            for (int col = 0; col < board[row].length; col++) {
                int value = board[row][col];
                System.out.print((value == 0 ? "." : value) + " ");
            }
            System.out.println();
        }
    }

    /**
     * Get the quiz size.
     * 
     * @return The quiz size.
     */
    public int getSize() {
        return size;
    }

    /**
     * Get a copy of the current quiz.
     * 
     * @return quiz The quiz.
     */
    public int[][] getQuiz() {
        return deepCopy(quiz);
    }

    /**
     * Get a copy of the current board.
     * 
     * @return board The board.
     */
    public int[][] getBoard() {
        return deepCopy(board);
    }

    /**
     * Clone the quiz or board.
     */
    private int[][] deepCopy(int[][] matrix) {
        int[][] clone = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            clone[i] = matrix[i].clone();
        }
        return clone;
    }

    /**
     * Set value on the board.
     * 
     * @param row   The row.
     * @param col   The column.
     * @param value The value of the cell.
     * @return True if the value is valid.
     */
    public boolean setValue(int row, int col, int value) {
        if (!isFixed(row, col) && value >= 1 && value <= 9) {
            boolean possible = isPossible(row, col, value);
            operations.add(new Operation(row, col, board[row][col], value));
            board[row][col] = value;
            return possible;
        }
        return false;
    }

    /**
     * Get value of board.
     * 
     * @param row The row.
     * @param col The column.
     * @return The value of the cell.
     */
    public int getValue(int row, int col) {
        return board[row][col];
    }

    /**
     * Undo the last operation.
     * 
     * @return The last operation.
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
     * Check if board[row, col] is fixed.
     * 
     * @param row The row.
     * @param col The column.
     * @return true: fixed, false: editable.
     */
    public boolean isFixed(int row, int col) {
        if (0 <= row && row < size * 3 && 0 <= col && col < size * 3) {
            return quiz[row][col] != 0;
        }
        return true;
    }

    /**
     * Check if value is possible to put in the board[row, col].
     * 
     * @param row   The row.
     * @param col   The column.
     * @param value The value.
     * @return true: possible, false: not possible.
     */
    public boolean isPossible(int row, int col, int value) {
        return isPossible(board, row, col, value);
    }

    private boolean isPossible(int[][] board, int row, int col, int value) {
        if (!isFixed(row, col) && value >= 1 && value <= 9) {
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
        return false;
    }

    /**
     * Remaining blank cells.
     * 
     * @return the number of blank cells.
     */
    public int getRemaining() {
        int[] remainings = getRemainings();
        int count = 0;
        for (int value = 1; value <= 9; value++) {
            if (remainings[value - 1] > 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * Remaining blank number.
     * 
     * @return remaining blank number per number.
     */
    public int[] getRemainings() {
        int[] remainings = new int[9];
        Arrays.fill(remainings, size * size);
        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                if (board[row][col] != 0) {
                    remainings[board[row][col] - 1]--;
                }
            }
        }
        return remainings;
    }

    /**
     * Reset the board to the quiz.
     */
    public void resetQuiz() {
        board = deepCopy(quiz);
        operations.clear();
    }

    /**
     * Create the new quiz.
     */
    public void newQuiz() {
        while (true) {
            // Create a quiz by backtracking.
            quiz = new int[size * 3][size * 3];
            if (!solve(quiz)) {
                continue;
            }

            // Erase cells block by block.
            for (int l = level; l > 0; l--) {
                for (int b = 0; b < size * size; b++) {
                    while (true) {
                        int row = (int) (b / size * 3 + Math.random() * size);
                        int col = (int) (b % size * 3 + Math.random() * size);
                        if (quiz[row][col] != 0) {
                            quiz[row][col] = 0;
                            break;
                        }
                    }
                }
            }

            // Make sure the quiz is solvable, and if not, try again.
            if (solve(deepCopy(quiz))) {
                break;
            }
        }

        board = deepCopy(quiz);
        operations.clear();
    }

    /**
     * Solve the board with backtracking.
     * 
     * @param board The current board.
     * @return true: solved, false: cannot solve.
     */
    private boolean solve(int[][] board) {
        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                if (board[row][col] == 0) {
                    // To improve the quiz quality, randomize the order of numbers to be attempted.
                    for (int value : shaffle(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 })) {
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
     * Shaffle the numbers. (Fisher-Yates shuffle)
     * 
     * @param numbers The array of numbers can be *changed*.
     * @return The shuffled numbers.
     */
    private int[] shaffle(int[] numbers) {
        for (int i = numbers.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            int temp = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = temp;
        }
        return numbers;
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
