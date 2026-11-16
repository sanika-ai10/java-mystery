/**
 * Abstract base class for every puzzle (abstraction).
 * Subclasses must override checkAnswer() - that is the polymorphic part.
 * All fields are private and exposed only through getters (encapsulation).
 */
public abstract class Puzzle implements Hintable {
    private final String question;
    private final String answer;
    private final String[] acceptable;
    private final String hint;
    private final int clueDigit;
    private int attemptsLeft;
    private boolean solved;

    protected Puzzle(String question, String answer, String hint, int clueDigit, int attempts) {
        this.question = question;
        this.acceptable = answer.split("\\|");   // "a|b" accepts a or b
        this.answer = acceptable[0];
        this.hint = hint;
        this.clueDigit = clueDigit;
        this.attemptsLeft = attempts;
        this.solved = false;
    }

    /**
     * Each puzzle type validates and checks its own answer.
     * @throws InvalidAnswerException if the input is malformed (no attempt used)
     * @return true if the answer is correct
     */
    protected abstract boolean checkAnswer(String input) throws InvalidAnswerException;

    /**
     * Template method shared by all puzzles: checks the answer and
     * handles the attempt counter. Throws GameOverException at 0 attempts.
     */
    public final boolean solve(String input) throws InvalidAnswerException, GameOverException {
        boolean correct = checkAnswer(input);   // may throw InvalidAnswerException first
        if (correct) {
            solved = true;
            return true;
        }
        attemptsLeft--;
        if (attemptsLeft <= 0) {
            throw new GameOverException("You ran out of attempts on this puzzle. The correct answer was: " + answer);
        }
        return false;
    }

    @Override
    public String getHint() {
        return hint;
    }

    public String getQuestion() {
        return question;
    }

    protected String getAnswer() {
        return answer;
    }

    /** True if the text equals any accepted answer (ignoring case). */
    protected boolean isAnswer(String text) {
        for (String a : acceptable) {
            if (a.equalsIgnoreCase(text)) {
                return true;
            }
        }
        return false;
    }

    public int getClueDigit() {
        return clueDigit;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public boolean isSolved() {
        return solved;
    }

    public abstract String getType();
}
