import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SudokuSolver {
    private int size; // Quiz size.
    private int[][] quiz; // Sudoku quiz.
    private int[][] board; // Quiz board.
    private boolean[][][] candidates; // candidates[row][col][value - 1] = true if value is a candidate for (row,
                                      // col).

    public SudokuSolver(SudokuQuiz quiz) {
        this.size = quiz.getSize();
        this.quiz = quiz.getQuiz();
        this.board = quiz.cloneBoard(quiz.getBoard());

        candidates = new boolean[size * 3][size * 3][9];
        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                for (int value = 1; value <= 9; value++) {
                    candidates[row][col][value - 1] = true;
                }
            }
        }
        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                int value = board[row][col];
                if (value != 0) {
                    fixCandidate(row, col, value);
                }
            }
        }
    }

    /**
     * A utility function to print the candidates
     */
    public void dumpCandidates() {
        System.out.println("+---+---+---+---+---+---+---+---+---+");
        for (int row = 0; row < size * 3; row++) {
            for (int r = 0; r < 3; r++) {
                System.out.print("|");
                for (int col = 0; col < size * 3; col++) {
                    for (int c = 0; c < 3; c++) {
                        int value = r * 3 + c + 1;
                        System.out.print(candidates[row][col][value - 1] ? value : quiz[row][col] == 0 ? "." : " ");
                    }
                    System.out.print("|");
                }
                System.out.println();
            }
            System.out.println("+---+---+---+---+---+---+---+---+---+");
        }
    }

    /**
     * Get the hints for solving the quiz.
     * 
     * @return Array of hint.
     */
    public Hint[] getHints() {
        List<Hint> hints = new ArrayList<Hint>();

        while (true) {
            Hint hint = findNackedSingle();
            if (hint != null) {
                board[hint.row][hint.col] = hint.value;
                fixCandidate(hint.row, hint.col, hint.value);
                hints.add(hint);
                continue;
            }

            // Hidden Single reduction.
            if (hiddenSingleReduction()) {
                continue;
            }

            // Nacked Pairs, Triplets, Quads reduction.
            if (nakidsReduction()) {
                continue;
            }

            // Box/Line Reduction.
            if (boxLineReduction()) {
                continue;
            }

            // TO-DO: Implement for other solution methods.

            break;
        }

        dumpCandidates();
        return hints.toArray(new Hint[hints.size()]);
    }

    /**
     * Get the candidates for the cell.
     * 
     * @param row
     * @param col
     * @return candidates
     */
    private int[] getCandidates(int row, int col) {
        int count = 0;
        for (int value = 1; value <= 9; value++) {
            if (candidates[row][col][value - 1]) {
                count++;
            }
        }

        int[] ca = new int[count];
        count = 0;
        for (int value = 1; value <= 9; value++) {
            if (candidates[row][col][value - 1]) {
                ca[count++] = value;
            }
        }
        return ca;
    }

    /**
     * Fix the candidate for the cell.
     * 
     * @param row
     * @param col
     * @param value
     */
    private void fixCandidate(int row, int col, int value) {
        for (int v = 1; v <= 9; v++) {
            candidates[row][col][v - 1] = v == value;
        }

        for (int r = 0; r < size * 3; r++) {
            if (r != row) {
                candidates[r][col][value - 1] = false;
            }
        }

        for (int c = 0; c < size * 3; c++) {
            if (c != col) {
                candidates[row][c][value - 1] = false;
            }
        }

        int blockRow = row / size;
        int blockCol = col / size;
        for (int r = blockRow * size; r < (blockRow + 1) * size; r++) {
            for (int c = blockCol * size; c < (blockCol + 1) * size; c++) {
                if (r != row && c != col) {
                    candidates[r][c][value - 1] = false;
                }
            }
        }
    }

    /**
     * Find the Nacked Single.
     * 
     * @return position of the Nacked Single.
     */
    private Hint findNackedSingle() {
        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                if (board[row][col] == 0) {
                    int count = 0;
                    for (int value = 1; value <= 9; value++) {
                        if (candidates[row][col][value - 1]) {
                            count++;
                        }
                    }
                    if (count == 1) {
                        return new Hint(row, col, getCandidates(row, col)[0]);
                    }
                }
            }
        }
        return null;
    }

    /*
     * Hidden Single reduction.
     * 
     * @return true if the candidate reduction is successful.
     */
    private boolean hiddenSingleReduction() {
        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                int[] ca = getCandidates(row, col);
                if (ca.length > 1) {
                    for (int value : ca) {
                        int count;

                        count = 0;
                        for (int r = 0; r < size * 3; r++) {
                            if (candidates[r][col][value - 1]) {
                                count++;
                            }
                        }
                        if (count == 1) {
                            candidates[row][col] = new boolean[9];
                            candidates[row][col][value - 1] = true;
                            return true;
                        }

                        count = 0;
                        for (int c = 0; c < size * 3; c++) {
                            if (candidates[row][c][value - 1]) {
                                count++;
                            }
                        }
                        if (count == 1) {
                            candidates[row][col] = new boolean[9];
                            candidates[row][col][value - 1] = true;
                            return true;
                        }

                        count = 0;
                        int blockRow = row / size;
                        int blockCol = col / size;
                        for (int r = blockRow * size; r < (blockRow + 1) * size; r++) {
                            for (int c = blockCol * size; c < (blockCol + 1) * size; c++) {
                                if (candidates[r][c][value - 1]) {
                                    count++;
                                }
                            }
                        }
                        if (count == 1) {
                            candidates[row][col] = new boolean[9];
                            candidates[row][col][value - 1] = true;
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Nacked Pairs, Triplets, Quads reduction.
     * If the pairs, triples, and quads appear in the row, column, and block, it can
     * reduce candidates.
     * 
     * @return true if the candidate reduction is successful.
     */
    private boolean nakidsReduction() {
        boolean reduceCandidates = false;

        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                int[] ca = getCandidates(row, col);
                if (ca.length >= 2 && ca.length <= 4) {
                    int count;

                    count = ca.length - 1;
                    for (int r = row + 1; r < size * 3; r++) {
                        if (Arrays.equals(candidates[row][col], candidates[r][col])) {
                            count--;
                        }
                    }
                    if (count == 0) {
                        for (int r = 0; r < size * 3; r++) {
                            if (!Arrays.equals(candidates[row][col], candidates[r][col])) {
                                for (int i = 0; i < ca.length; i++) {
                                    if (candidates[r][col][ca[i] - 1]) {
                                        candidates[r][col][ca[i] - 1] = false;
                                        reduceCandidates = true;
                                    }
                                }
                            }
                        }
                    }

                    count = ca.length - 1;
                    for (int c = col + 1; c < size * 3; c++) {
                        if (Arrays.equals(candidates[row][col], candidates[row][c])) {
                            count--;
                        }
                    }
                    if (count == 0) {
                        for (int c = 0; c < size * 3; c++) {
                            if (!Arrays.equals(candidates[row][col], candidates[row][c])) {
                                for (int i = 0; i < ca.length; i++) {
                                    if (candidates[row][c][ca[i] - 1]) {
                                        candidates[row][c][ca[i] - 1] = false;
                                        reduceCandidates = true;
                                    }
                                }
                            }
                        }
                    }

                    count = ca.length - 1;
                    int blockRow = row / size;
                    int blockCol = col / size;
                    for (int r = row + 1; r < (blockRow + 1) * size; r++) {
                        for (int c = col + 1; c < (blockCol + 1) * size; c++) {
                            if (r != row && c != col) {
                                if (Arrays.equals(candidates[row][col], candidates[r][c])) {
                                    count--;
                                }
                            }
                        }
                    }
                    if (count == 0) {
                        for (int r = 0; r < size * 3; r++) {
                            for (int c = 0; c < size * 3; c++) {
                                if (!Arrays.equals(candidates[row][col], candidates[r][c])) {
                                    for (int i = 0; i < ca.length; i++) {
                                        if (candidates[r][c][ca[i] - 1]) {
                                            candidates[r][c][ca[i] - 1] = false;
                                            reduceCandidates = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return reduceCandidates;
    }

    /**
     * Box/Line Reduction.
     * If a candidate only appears two or three times in a row or column and is all
     * in the same block, it can reduce candidates.
     * 
     * @return true if the candidate reduction is successful.
     */
    private boolean boxLineReduction() {
        boolean reduceCandidates = false;

        for (int row = 0; row < size * 3; row++) {
            int[][] ca = new int[size][9];
            for (int col = 0; col < size * 3; col++) {
                for (int value = 1; value <= 9; value++) {
                    if (candidates[row][col][value - 1]) {
                        ca[col / size][value - 1]++;
                    }
                }
            }

            for (int value = 1; value <= 9; value++) {
                int count = 0;
                for (int blockCol = 0; blockCol < size; blockCol++) {
                    if (ca[blockCol][value - 1] > 0) {
                        count++;
                    }
                }
                if (count == 1) {
                    for (int blockCol = 0; blockCol < size; blockCol++) {
                        if (ca[blockCol][value - 1] > 1) {
                            int blockRow = row / size;
                            for (int r = blockRow * size; r < (blockRow + 1) * size; r++) {
                                if (r != row) {
                                    for (int c = blockCol * size; c < (blockCol + 1) * size; c++) {
                                        if (candidates[r][c][value - 1]) {
                                            candidates[r][c][value - 1] = false;
                                            reduceCandidates = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for (int col = 0; col < size * 3; col++) {
            int[][] ca = new int[size][9];
            for (int row = 0; row < size * 3; row++) {
                for (int value = 1; value <= 9; value++) {
                    if (candidates[row][col][value - 1]) {
                        ca[row / size][value - 1]++;
                    }
                }
            }

            for (int value = 1; value <= 9; value++) {
                int count = 0;
                for (int blockRow = 0; blockRow < size; blockRow++) {
                    if (ca[blockRow][value - 1] > 0) {
                        count++;
                    }
                }
                if (count == 1) {
                    for (int blockRow = 0; blockRow < size; blockRow++) {
                        if (ca[blockRow][value - 1] > 1) {
                            int blockCol = col / size;
                            for (int c = blockCol * size; c < (blockCol + 1) * 3; c++) {
                                if (c != col) {
                                    for (int r = blockRow * size; r < (blockRow + 1) * size; r++) {
                                        if (candidates[r][c][value - 1]) {
                                            candidates[r][c][value - 1] = false;
                                            reduceCandidates = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return reduceCandidates;
    }

    /**
     * Class for hint.
     */
    public class Hint {
        public final int row, col; // row and column of hint position.
        public final int value; // hint value.

        public Hint(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }
}