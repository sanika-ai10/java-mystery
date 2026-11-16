/** Java question: the answer is a single keyword/term (letters only). */
public class CodeLockPuzzle extends Puzzle {

    public CodeLockPuzzle(String question, String answer, String hint, int clueDigit, int attempts) {
        super(question, answer, hint, clueDigit, attempts);
    }

    @Override
    protected boolean checkAnswer(String input) throws InvalidAnswerException {
        if (input == null || !input.trim().matches("[A-Za-z]+")) {
            throw new InvalidAnswerException("Use a single Java word, letters only (no attempt used).");
        }
        return isAnswer(input.trim());
    }

    @Override
    public String getType() {
        return "Java Code Lock";
    }
}
