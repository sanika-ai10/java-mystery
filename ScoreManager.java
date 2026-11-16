import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * File handling: keeps the top 5 scores in highscores.txt
 * (one "name,score" per line).
 */
public class ScoreManager {
    private static final int MAX_ENTRIES = 5;
    private final String fileName;
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<Integer> scores = new ArrayList<>();

    public ScoreManager(String fileName) {
        this.fileName = fileName;
    }

    /** Reads the file. A missing file just means no scores yet. */
    public void load() {
        names.clear();
        scores.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    try {
                        scores.add(Integer.parseInt(parts[1].trim()));
                        names.add(parts[0].trim());
                    } catch (NumberFormatException ignored) {
                        // skip corrupted line
                    }
                }
            }
        } catch (IOException e) {
            // file not created yet - fine
        }
    }

    /**
     * Inserts a score in sorted order (highest first) and trims to the top 5.
     * Returns the 0-based rank of the new score, or -1 if it did not make the top 5.
     */
    public int addScore(String name, int score) {
        int pos = 0;
        while (pos < scores.size() && scores.get(pos) >= score) {
            pos++;
        }
        names.add(pos, name.replace(",", " "));
        scores.add(pos, score);
        while (scores.size() > MAX_ENTRIES) {
            scores.remove(scores.size() - 1);
            names.remove(names.size() - 1);
        }
        return pos < MAX_ENTRIES ? pos : -1;
    }

    public int size() {
        return scores.size();
    }

    public String getName(int i) {
        return names.get(i);
    }

    public int getScore(int i) {
        return scores.get(i);
    }

    public void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            for (int i = 0; i < scores.size(); i++) {
                pw.println(names.get(i) + "," + scores.get(i));
            }
        } catch (IOException e) {
            System.out.println("Could not save high scores: " + e.getMessage());
        }
    }

    public void display() {
        System.out.println("\n=== TOP " + MAX_ENTRIES + " SCORES ===");
        if (scores.isEmpty()) {
            System.out.println("(none yet)");
        }
        for (int i = 0; i < scores.size(); i++) {
            System.out.printf("%d. %-15s %d%n", i + 1, names.get(i), scores.get(i));
        }
    }
}
