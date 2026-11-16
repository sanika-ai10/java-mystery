/** Maths / sequence puzzle: the answer must be a whole number. */
public class NumberPuzzle extends Puzzle {

    public NumberPuzzle(String question, int answer, String hint, int clueDigit, int attempts) {
        super(question, String.valueOf(answer), hint, clueDigit, attempts);
    }

    @Override
    protected boolean checkAnswer(String input) throws InvalidAnswerException {
        try {
            int guess = Integer.parseInt(input.trim());
            return guess == Integer.parseInt(getAnswer());
        } catch (NumberFormatException e) {
            // Convert the built-in exception into our custom one.
            throw new InvalidAnswerException("That is not a number. Type digits only (no attempt used).");
        }
    }

    @Override
    public String getType() {
        return "Number";
    }
}
