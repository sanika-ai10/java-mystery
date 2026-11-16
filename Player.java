import java.util.ArrayList;

/** The player: name, score, hints used and an inventory of found clues. */
public class Player {
    private final String name;
    private int score;
    private int hintsUsed;
    private final ArrayList<String> inventory = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public void addScore(int points) {
        score += points;
    }

    /** Using a hint costs points, but the score never drops below 0. */
    public void useHint(int cost) {
        hintsUsed++;
        score = Math.max(0, score - cost);
    }

    public void addClue(String clue) {
        inventory.add(clue);
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public int getHintsUsed() { return hintsUsed; }
    public ArrayList<String> getInventory() { return inventory; }
}
