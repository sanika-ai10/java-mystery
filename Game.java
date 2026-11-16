import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Main game loop. A Game HAS rooms (composition) and one Player. */
public class Game {
    private static final int HINT_COST = 5;
    private static final int PUZZLE_POINTS = 100;
    private static final int ATTEMPT_BONUS = 10;
    private static final int DOOR_POINTS = 50;

    private final Scanner scanner = new Scanner(System.in);
    private final ArrayList<Room> rooms = new ArrayList<>();
    private final ScoreManager scoreManager = new ScoreManager("highscores.txt");
    private Player player;

    public void play() {
        rooms.addAll(Levels.build());
        scoreManager.load();

        System.out.println("=====================================");
        System.out.println("        JAVA MYSTERY ESCAPE");
        System.out.println("=====================================");
        System.out.println("Solve 3 puzzles per room to get 3 clue digits,");
        System.out.println("then enter them IN ORDER to open the door.");
        System.out.println("Commands: hint (-" + HINT_COST + " pts) | clues | quit\n");

        System.out.print("Enter your name: ");
        String name = readLine().trim();
        player = new Player(name.isEmpty() ? "Player" : name);

        boolean escaped = false;
        try {
            for (Room room : rooms) {
                playRoom(room);
            }
            escaped = true;
        } catch (GameOverException e) {
            System.out.println("\n*** GAME OVER ***");
            System.out.println(e.getMessage());
        }

        if (escaped) {
            System.out.println("\n*** YOU ESCAPED THE JAVA MYSTERY! ***");
        }
        System.out.println(player.getName() + ", final score: " + player.getScore()
                + " (hints used: " + player.getHintsUsed() + ")");

        scoreManager.addScore(player.getName(), player.getScore());
        scoreManager.save();
        scoreManager.display();
    }

    private void playRoom(Room room) throws GameOverException {
        System.out.println("\n--------- LEVEL " + room.getLevel() + ": " + room.getName().toUpperCase() + " ---------");
        System.out.println(room.getDescription());

        int number = 1;
        for (Puzzle puzzle : room.getPuzzles()) {
            System.out.println("\nPuzzle " + number + "/" + room.getPuzzles().size()
                    + " [" + puzzle.getType() + "]");
            System.out.println(puzzle.getQuestion());
            while (!puzzle.isSolved()) {
                String input = prompt("Answer (attempts left: " + puzzle.getAttemptsLeft() + ")");
                if (handleCommand(input, puzzle)) {
                    continue;
                }
                try {
                    if (puzzle.solve(input)) {
                        int points = PUZZLE_POINTS + puzzle.getAttemptsLeft() * ATTEMPT_BONUS;
                        player.addScore(points);
                        player.addClue(room.getName() + " puzzle " + number + " -> digit " + puzzle.getClueDigit());
                        System.out.println("Correct! +" + points + " points. Clue digit found: " + puzzle.getClueDigit());
                    } else {
                        System.out.println("Wrong answer.");
                    }
                } catch (InvalidAnswerException e) {
                    System.out.println(e.getMessage());
                }
            }
            number++;
        }

        System.out.println("\nAll puzzles solved! Enter the " + room.getPuzzles().size()
                + "-digit door code using your clue digits in order.");
        boolean open = false;
        while (!open) {
            String input = prompt("Door code (attempts left: " + room.getDoorAttemptsLeft() + ")");
            if (handleCommand(input, null)) {
                continue;
            }
            try {
                open = room.tryDoor(input);
                if (open) {
                    player.addScore(DOOR_POINTS);
                    System.out.println("Click! The door opens. +" + DOOR_POINTS + " points.");
                } else {
                    System.out.println("The lock does not budge.");
                }
            } catch (InvalidAnswerException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /** Handles hint / clues / quit. Returns true if the input was a command. */
    private boolean handleCommand(String input, Puzzle puzzle) throws GameOverException {
        String cmd = input.trim().toLowerCase();
        switch (cmd) {
            case "quit":
                throw new GameOverException("You left the game.", true);
            case "clues":
                if (player.getInventory().isEmpty()) {
                    System.out.println("No clues found yet.");
                } else {
                    System.out.println("Clues found:");
                    for (String clue : player.getInventory()) {
                        System.out.println("  - " + clue);
                    }
                }
                return true;
            case "hint":
                if (puzzle == null) {
                    System.out.println("No hint for the door: use your clue digits in order. (type 'clues')");
                } else {
                    player.useHint(HINT_COST);
                    System.out.println("HINT (-" + HINT_COST + " pts): " + puzzle.getHint()
                            + "  [score: " + player.getScore() + "]");
                }
                return true;
            default:
                return false;
        }
    }

    private String prompt(String label) throws GameOverException {
        System.out.print(label + " > ");
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            throw new GameOverException("Input ended.", true);
        }
    }

    private String readLine() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) {
            return "";
        }
    }
}
