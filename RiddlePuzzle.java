/** Word riddle: the answer is a word, compared ignoring case. */
public class RiddlePuzzle extends Puzzle {

    public RiddlePuzzle(String question, String answer, String hint, int clueDigit, int attempts) {
        super(question, answer, hint, clueDigit, attempts);
    }

    @Override
    protected boolean checkAnswer(String input) throws InvalidAnswerException {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidAnswerException("Answer cannot be empty.");
        }
        String text = input.trim().toLowerCase().replaceFirst("^(a|an|the)\\s+", "");
        return isAnswer(text);
    }

    @Override
    public String getType() {
        return "Riddle";
    }
}
