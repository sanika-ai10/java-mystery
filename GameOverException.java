/**
 * Custom checked exception: thrown when the player runs out of attempts
 * (or types "quit"). Caught in Game.play() to end the game.
 */
public class GameOverException extends Exception {
    private final boolean quit;

    public GameOverException(String message) {
        this(message, false);
    }

    public GameOverException(String message, boolean quit) {
        super(message);
        this.quit = quit;
    }

    public boolean isQuit() {
        return quit;
    }
}
