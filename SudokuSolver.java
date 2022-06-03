import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SudokuSolver {
    private int size; // Quiz size.
    private int[][] board; // Quiz board.
    private boolean[][][] candidates; // candidates[row][col][value - 1] = true if value is a candidate for (row,
                                      // col).

    public SudokuSolver(SudokuQuiz quiz) {
        size = quiz.getSize();
        board = quiz.cloneBoard(quiz.getBoard());

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

        dumpCandidates();
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
                        System.out.print(candidates[row][col][value - 1] ? value : board[row][col] == 0 ? "." : " ");
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
        Hint hint;

        while (true) {
            hint = findNackedSingle();
            if (hint != null) {
                board[hint.row][hint.col] = hint.value;
                fixCandidate(hint.row, hint.col, hint.value);
                hints.add(hint);
                continue;
            }

            hint = findHiddenSingle();
            if (hint != null) {
                board[hint.row][hint.col] = hint.value;
                fixCandidate(hint.row, hint.col, hint.value);
                hints.add(hint);
                continue;
            }

            if (checkNackedPair()) {
                continue;
            }

            // TO-DO: Implement for other solution methods.

            break;
        }
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
     * Find the nacked single.
     * 
     * @return position of the nacked single.
     */
    private Hint findNackedSingle() {
        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                if (board[row][col] == 0) {
                    if (isNackedSingle(row, col)) {
                        return new Hint(row, col, getCandidates(row, col)[0]);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find the hidden single.
     * 
     * @return position of the hidden single.
     */
    private Hint findHiddenSingle() {
        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                if (board[row][col] == 0) {
                    int[] candidate = getCandidates(row, col);
                    for (int i = 0; i < candidate.length; i++) {
                        if (isHiddenSingle(row, col, candidate[i])) {
                            return new Hint(row, col, candidate[i]);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Check if the cell is a nacked single, update the candidates.
     * 
     * return true if the candidate is updated.
     */
    private boolean checkNackedPair() {
        boolean updateCandidates = false;

        for (int row = 0; row < size * 3; row++) {
            for (int col = 0; col < size * 3; col++) {
                int[] ca = getCandidates(row, col);
                if (ca.length != 2) {
                    continue;
                }

                for (int r = row + 1; r < size * 3; r++) {
                    if (Arrays.equals(ca, getCandidates(r, col))) {
                        for (int i = 0; i < size * 3; i++) {
                            if (i != row && i != r) {
                                if (candidates[i][col][ca[0] - 1] || candidates[i][col][ca[1] - 1]) {
                                    candidates[i][col][ca[0] - 1] = false;
                                    candidates[i][col][ca[1] - 1] = false;
                                    updateCandidates = true;
                                }
                            }
                        }
                    }
                }

                for (int c = col + 1; c < size * 3; c++) {
                    if (Arrays.equals(ca, getCandidates(row, c))) {
                        for (int i = 0; i < size * 3; i++) {
                            if (i != col && i != c) {
                                if (candidates[row][i][ca[0] - 1] || candidates[row][i][ca[1] - 1]) {
                                    candidates[row][i][ca[0] - 1] = false;
                                    candidates[row][i][ca[1] - 1] = false;
                                    updateCandidates = true;
                                }
                            }
                        }
                    }
                }

                int blockRow = row / size;
                int blockCol = col / size;
                for (int r = row + 1; r < (blockRow + 1) * size; r++) {
                    for (int c = col + 1; c < (blockCol + 1) * size; c++) {
                        if (Arrays.equals(ca, getCandidates(r, c))) {
                            for (int i = blockRow * size; i < (blockRow + 1) * size; i++) {
                                for (int j = blockCol * size; j < (blockCol + 1) * size; j++) {
                                    if (i != row && j != col && i != r && j != c) {
                                        if (candidates[i][j][ca[0] - 1] || candidates[i][j][ca[1] - 1]) {
                                            candidates[i][j][ca[0] - 1] = false;
                                            candidates[i][j][ca[1] - 1] = false;
                                            updateCandidates = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (updateCandidates) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if the cell at (row, col) is a naked single.
     * 
     * @param row
     * @param col
     * @return true if the cell at (row, col) is a naked single.
     */
    private boolean isNackedSingle(int row, int col) {
        int count = 0;
        for (int value = 1; value <= 9; value++) {
            if (candidates[row][col][value - 1]) {
                count++;
            }
        }
        return count == 1;
    }

    /**
     * Check if the cell at (row, col) is a hidden single.
     * 
     * @param row
     * @param col
     * @return true if the cell at (row, col) is a hidden single.
     */
    private boolean isHiddenSingle(int row, int col, int value) {
        for (int r = 0; r < size * 3; r++) {
            if (r != row && candidates[r][col][value - 1]) {
                return false;
            }
        }

        for (int c = 0; c < size * 3; c++) {
            if (c != col && candidates[row][c][value - 1]) {
                return false;
            }
        }

        int blockRow = row / size;
        int blockCol = col / size;
        for (int r = blockRow * size; r < (blockRow + 1) * size; r++) {
            for (int c = blockCol * size; c < (blockCol + 1) * size; c++) {
                if (r != row && c != col && candidates[r][c][value - 1]) {
                    return false;
                }
            }
        }
        return true;
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