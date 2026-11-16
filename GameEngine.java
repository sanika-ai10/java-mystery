import java.util.ArrayList;

/**
 * The game rules for the web version. It uses the SAME classes as the
 * terminal game (Room, Puzzle, Player, ScoreManager, the exceptions).
 * The web page only draws what this engine returns as JSON.
 */
public class GameEngine {
    private static final int HINT_COST = 5;
    private static final int PUZZLE_POINTS = 100;
    private static final int ATTEMPT_BONUS = 10;
    private static final int DOOR_POINTS = 50;
    private static final String[] THEMES = {"study", "library", "vault"};

    private enum Stage { INTRO, SPLASH, PUZZLE, DOOR, END }

    private final ScoreManager scoreManager = new ScoreManager("highscores.txt");
    private Stage stage = Stage.INTRO;
    private ArrayList<Room> rooms = new ArrayList<>();
    private Player player;
    private int roomIndex;
    private int puzzleIndex;
    private int roomsCleared;
    private boolean hintShown;
    private long startedAt;

    private boolean endWin;
    private boolean endQuit;
    private String endMessage = "";
    private int endPosition = -1;
    private long endSeconds;

    public GameEngine() {
        scoreManager.load();
    }

    // ---------------------------------------------------------------- actions

    public synchronized String start(String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.length() > 20) {
            clean = clean.substring(0, 20);
        }
        player = new Player(clean.isEmpty() ? "Player" : clean);
        rooms = Levels.build();
        roomIndex = 0;
        puzzleIndex = 0;
        roomsCleared = 0;
        hintShown = false;
        startedAt = System.currentTimeMillis();
        stage = Stage.SPLASH;
        return state("null");
    }

    public synchronized String enter() {
        if (stage == Stage.SPLASH) {
            stage = Stage.PUZZLE;
            puzzleIndex = 0;
            hintShown = false;
        }
        return state("null");
    }

    public synchronized String currentState() {
        return state("null");
    }

    public synchronized String answer(String raw) {
        if (player == null || stage != Stage.PUZZLE) {
            return state(event("error", "Nothing to answer right now.", ""));
        }
        String cmd = raw == null ? "" : raw.trim().toLowerCase();
        if (cmd.equals("hint")) {
            return hint();
        }
        if (cmd.equals("clues")) {
            return clues();
        }
        if (cmd.equals("quit")) {
            return quit();
        }

        Room room = rooms.get(roomIndex);
        Puzzle puzzle = room.getPuzzles().get(puzzleIndex);
        try {
            if (puzzle.solve(raw)) {
                int points = PUZZLE_POINTS + puzzle.getAttemptsLeft() * ATTEMPT_BONUS;
                player.addScore(points);
                player.addClue(room.getName() + " puzzle " + (puzzleIndex + 1) + " -> digit " + puzzle.getClueDigit());
                int slot = puzzleIndex;
                puzzleIndex++;
                hintShown = false;
                if (puzzleIndex >= room.getPuzzles().size()) {
                    stage = Stage.DOOR;
                }
                return state(event("correct",
                        "Correct! +" + points + " points. Clue digit: " + puzzle.getClueDigit(),
                        ",\"points\":" + points + ",\"digit\":" + puzzle.getClueDigit()
                                + ",\"slot\":" + slot + ",\"attemptsLeft\":" + puzzle.getAttemptsLeft()));
            }
            int left = puzzle.getAttemptsLeft();
            return state(event("wrong",
                    "Wrong answer. " + left + (left == 1 ? " attempt" : " attempts") + " left.",
                    ",\"attemptsLeft\":" + left));
        } catch (InvalidAnswerException e) {
            return state(event("invalid", e.getMessage(), ""));          // no attempt used
        } catch (GameOverException e) {
            return endGame(false, e.getMessage(), false, event("gameover", e.getMessage(), ""));
        }
    }

    public synchronized String hint() {
        if (player == null || (stage != Stage.PUZZLE && stage != Stage.DOOR)) {
            return state(event("error", "No game in progress.", ""));
        }
        if (stage == Stage.DOOR) {
            return state(event("info", "No hint for the door: use your clue digits in order.", ""));
        }
        boolean charged = false;
        if (!hintShown) {
            player.useHint(HINT_COST);
            hintShown = true;
            charged = true;
        }
        Puzzle puzzle = rooms.get(roomIndex).getPuzzles().get(puzzleIndex);
        return state(event("hint", "Hint: " + puzzle.getHint(),
                ",\"charged\":" + charged + ",\"cost\":" + HINT_COST));
    }

    public synchronized String clues() {
        if (player == null || (stage != Stage.PUZZLE && stage != Stage.DOOR)) {
            return state(event("error", "No game in progress.", ""));
        }
        StringBuilder sb = new StringBuilder("Clues so far: ");
        for (Puzzle p : rooms.get(roomIndex).getPuzzles()) {
            sb.append(p.isSolved() ? String.valueOf(p.getClueDigit()) : "?").append("  ");
        }
        return state(event("clues", sb.toString().trim(), ""));
    }

    public synchronized String door(String code) {
        if (player == null || stage != Stage.DOOR) {
            return state(event("error", "The door is not ready yet.", ""));
        }
        Room room = rooms.get(roomIndex);
        try {
            if (room.tryDoor(code)) {
                player.addScore(DOOR_POINTS);
                roomsCleared++;
                String opened = ",\"points\":" + DOOR_POINTS + ",\"theme\":" + Json.str(themeOf(roomIndex));
                String openEvent = event("open", "Click! The door opens. +" + DOOR_POINTS + " points.", opened);
                if (roomIndex < rooms.size() - 1) {
                    roomIndex++;
                    puzzleIndex = 0;
                    hintShown = false;
                    stage = Stage.SPLASH;
                    return state(openEvent);
                }
                return endGame(true, "", false, openEvent);
            }
            int left = room.getDoorAttemptsLeft();
            return state(event("wrong",
                    "The lock does not budge. " + left + (left == 1 ? " attempt" : " attempts") + " left.",
                    ",\"attemptsLeft\":" + left));
        } catch (InvalidAnswerException e) {
            return state(event("invalid", e.getMessage(), ""));
        } catch (GameOverException e) {
            return endGame(false, e.getMessage(), false, event("gameover", e.getMessage(), ""));
        }
    }

    public synchronized String quit() {
        if (player == null || stage == Stage.INTRO || stage == Stage.END) {
            return state("null");
        }
        return endGame(false, "You left the game.", true, event("gameover", "You left the game.", ""));
    }

    public synchronized String scoresJson() {
        return scoresArray();
    }

    // ---------------------------------------------------------------- helpers

    private String endGame(boolean win, String message, boolean quit, String eventJson) {
        stage = Stage.END;
        endWin = win;
        endQuit = quit;
        endMessage = message;
        endSeconds = (System.currentTimeMillis() - startedAt) / 1000;
        endPosition = scoreManager.addScore(player.getName(), player.getScore());
        scoreManager.save();                                    // file handling: highscores.txt
        return state(eventJson);
    }

    private String themeOf(int index) {
        return THEMES[Math.min(index, THEMES.length - 1)];
    }

    private String event(String kind, String message, String extra) {
        return "{\"kind\":" + Json.str(kind) + ",\"message\":" + Json.str(message) + extra + "}";
    }

    private String scoresArray() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < scoreManager.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{\"name\":").append(Json.str(scoreManager.getName(i)))
              .append(",\"score\":").append(scoreManager.getScore(i)).append("}");
        }
        return sb.append("]").toString();
    }

    /** Everything the page needs to draw the current screen. */
    private String state(String eventJson) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"stage\":").append(Json.str(stage.name().toLowerCase()));
        if (player != null) {
            sb.append(",\"player\":{\"name\":").append(Json.str(player.getName()))
              .append(",\"score\":").append(player.getScore())
              .append(",\"hints\":").append(player.getHintsUsed()).append("}");
        }
        if (player != null && stage != Stage.INTRO && stage != Stage.END) {
            Room room = rooms.get(roomIndex);
            sb.append(",\"room\":{\"level\":").append(room.getLevel())
              .append(",\"name\":").append(Json.str(room.getName()))
              .append(",\"description\":").append(Json.str(room.getDescription()))
              .append(",\"theme\":").append(Json.str(themeOf(roomIndex)))
              .append(",\"maxAttempts\":").append(room.getMaxAttempts())
              .append(",\"total\":").append(room.getPuzzles().size()).append("}");
            sb.append(",\"clues\":[");
            for (int i = 0; i < room.getPuzzles().size(); i++) {
                Puzzle p = room.getPuzzles().get(i);
                sb.append(i > 0 ? "," : "").append(p.isSolved() ? Json.str(String.valueOf(p.getClueDigit())) : "null");
            }
            sb.append("]");
            if (stage == Stage.PUZZLE) {
                Puzzle p = room.getPuzzles().get(puzzleIndex);
                sb.append(",\"puzzle\":{\"index\":").append(puzzleIndex)
                  .append(",\"total\":").append(room.getPuzzles().size())
                  .append(",\"type\":").append(Json.str(p.getType()))
                  .append(",\"question\":").append(Json.str(p.getQuestion()))
                  .append(",\"attemptsLeft\":").append(p.getAttemptsLeft())
                  .append(",\"hint\":").append(hintShown ? Json.str(p.getHint()) : "null")
                  .append("}");
            }
            if (stage == Stage.DOOR) {
                sb.append(",\"door\":{\"attemptsLeft\":").append(room.getDoorAttemptsLeft())
                  .append(",\"digits\":").append(room.getPuzzles().size()).append("}");
            }
        }
        if (stage == Stage.END) {
            sb.append(",\"end\":{\"win\":").append(endWin)
              .append(",\"quit\":").append(endQuit)
              .append(",\"message\":").append(Json.str(endMessage))
              .append(",\"score\":").append(player.getScore())
              .append(",\"hints\":").append(player.getHintsUsed())
              .append(",\"roomsCleared\":").append(roomsCleared)
              .append(",\"seconds\":").append(endSeconds)
              .append(",\"position\":").append(endPosition).append("}");
        }
        sb.append(",\"event\":").append(eventJson);
        sb.append(",\"scores\":").append(scoresArray());
        return sb.append("}").toString();
    }
}
