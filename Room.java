import java.util.ArrayList;

/**
 * A Room HAS puzzles (composition). Solving all puzzles reveals clue digits;
 * the digits in order form the 3-digit door lock.
 */
public class Room {
    private final String name;
    private final String description;
    private final int level;
    private final ArrayList<Puzzle> puzzles = new ArrayList<>();
    private final int maxAttempts;
    private int doorAttemptsLeft;

    public Room(String name, String description, int level, int attempts) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.maxAttempts = attempts;
        this.doorAttemptsLeft = attempts;
    }

    public void addPuzzle(Puzzle p) {
        puzzles.add(p);
    }

    /** Door code = clue digits of the puzzles, in order. */
    public String getLockCode() {
        StringBuilder sb = new StringBuilder();
        for (Puzzle p : puzzles) {
            sb.append(p.getClueDigit());
        }
        return sb.toString();
    }

    public boolean tryDoor(String input) throws InvalidAnswerException, GameOverException {
        if (input == null || !input.trim().matches("\\d{" + puzzles.size() + "}")) {
            throw new InvalidAnswerException("The lock needs exactly " + puzzles.size() + " digits (no attempt used).");
        }
        if (input.trim().equals(getLockCode())) {
            return true;
        }
        doorAttemptsLeft--;
        if (doorAttemptsLeft <= 0) {
            throw new GameOverException("The lock jams forever. The code was " + getLockCode() + ".");
        }
        return false;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getLevel() { return level; }
    public ArrayList<Puzzle> getPuzzles() { return puzzles; }
    public int getDoorAttemptsLeft() { return doorAttemptsLeft; }
    public int getMaxAttempts() { return maxAttempts; }
}
