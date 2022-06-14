import javax.swing.SwingUtilities;

public class Sudoku {
    public static void main(String[] argv) {
        final String usage = "Usage: java Sudoku [repl | swing]\n" +
                "  repl: run in cui repl mode\n" +
                "  swing: run in gui swing mode";
        final int level = 5; // Quiz level. (1 .. 7)
        final int size = 3; // Size of the quiz.

        if (argv.length != 1) {
            System.out.println(usage);
            System.exit(1);
        }
        switch (argv[0]) {
            case "repl":
                repl(size, level);
                break;
            case "swing":
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        new SudokuBoard(size, level);
                    }
                });
                break;
            default:
                System.out.println(usage);
                System.exit(1);
        }
    }

    /**
     * Sudoku repl mode.
     * 
     * @param size  The size of the quiz.
     * @param level The level of the quiz.
     */
    static public void repl(int size, int level) {
        final String PROMPT = "> ";
        final String help = "Available commands:\n" +
                " 1. new\n" +
                " 2. reset\n" +
                " 3. solve\n" +
                " 4. hint\n" +
                " 5. set <row> <col> <value>\n" +
                " 6. undo\n" +
                " 7. quit\n";

        SudokuQuiz quiz = new SudokuQuiz(size, level);
        quiz.newQuiz();
        while (true) {
            int row, col, value;

            quiz.dumpBoard();
            System.out.print(PROMPT);
            String line = System.console().readLine();
            if (line == null)
                break;

            String[] tokens = line.split(" ");
            switch (tokens[0]) {
                case "new":
                    quiz.newQuiz();
                    break;
                case "reset":
                    quiz.resetQuiz();
                    break;
                case "solve":
                    if (quiz.getRemaining() > 0) {
                        SudokuSolver solver = new SudokuSolver(quiz);
                        SudokuSolver.Hint[] hints = solver.getHints();
                        for (SudokuSolver.Hint hint : hints) {
                            quiz.setValue(hint.row, hint.col, hint.value);
                        }
                        if (quiz.getRemaining() > 0) {
                            System.out.println("Can’t solve it any more.");
                        }
                    }
                    break;
                case "hint":
                    if (quiz.getRemaining() > 0) {
                        SudokuSolver solver = new SudokuSolver(quiz);
                        SudokuSolver.Hint[] hints = solver.getHints();
                        if (hints.length == 0) {
                            System.out.println("No hints.");
                        } else {
                            System.out.println(
                                    "row: " + hints[0].row + ", col: " + hints[0].col + ", value: " + hints[0].value);
                            System.out.println("There are other " + (hints.length - 1) + " hints.");
                        }
                    }
                    break;
                case "set":
                    try {
                        row = Integer.parseInt(tokens[1]);
                        col = Integer.parseInt(tokens[2]);
                        value = Integer.parseInt(tokens[3]);
                        if (row < 0 || row > 8 || col < 0 || col > 8 || value < 0 || value > 9) {
                            throw new RuntimeException("Invalid row or column or value");
                        }
                        if (quiz.isFixed(row, col)) {
                            System.out.println("row = " + row + " col = " + col + " is fixed.");
                        } else {
                            boolean valid = quiz.setValue(row, col, value);
                            System.out.println("Set " + row + " " + col + " " + value + " returns " + valid);
                            if (valid && quiz.getRemaining() == 0) {
                                System.out.println("Solved!");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Usage: set <row> <col> <value>");
                    }
                    break;
                case "undo":
                    SudokuQuiz.Operation operation = quiz.undo();
                    if (operation == null) {
                        System.out.println("No more undo");
                    } else {
                        System.out.println("Undo " + operation.row + " " + operation.col + " "
                                + operation.newValue);
                    }
                    break;
                case "quit":
                    System.exit(0);
                    break;
                default:
                    System.out.println(help);
            }
        }
    }
}
