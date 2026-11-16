/**
 * Custom checked exception: thrown when the player's input is not a valid
 * answer (blank, letters in a number puzzle, wrong door-code format...).
 * An invalid answer does NOT use up an attempt.
 */
public class InvalidAnswerException extends Exception {
    public InvalidAnswerException(String message) {
        super(message);
    }
}
