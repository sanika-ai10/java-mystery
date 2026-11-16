import java.util.ArrayList;

/**
 * All puzzle content lives here, so the terminal game and the web game
 * use exactly the same rooms. Add puzzles or a 4th level in build().
 * In an answer, "a|b|c" means any of a, b or c is accepted.
 */
public class Levels {

    public static ArrayList<Room> build() {
        ArrayList<Room> rooms = new ArrayList<>();

        // Level 1: 5 attempts per puzzle -> code 317
        Room study = new Room("Study Room", "Dusty books and a flickering desk lamp. A locked oak door waits.", 1, 5);
        study.addPuzzle(new RiddlePuzzle("I have keys but open no locks, and space but no room. What am I?",
                "keyboard", "You type on me.", 3, 5));
        study.addPuzzle(new NumberPuzzle("What is 6 x 7?", 42, "Six sevens.", 1, 5));
        study.addPuzzle(new NumberPuzzle("What comes next: 2, 4, 8, 16, ?", 32, "Each number doubles.", 7, 5));
        rooms.add(study);

        // Level 2: 4 attempts per puzzle -> code 528
        Room library = new Room("Old Library", "Towering shelves and the smell of ancient paper.", 2, 4);
        library.addPuzzle(new RiddlePuzzle("The more of me you take, the more you leave behind. What am I?",
                "footsteps|footstep|steps|step", "Think about walking.", 5, 4));
        library.addPuzzle(new CodeLockPuzzle(
                "Java: which OOP concept hides data by making fields private and exposing getters and setters?",
                "encapsulation", "It starts with 'en' and sounds like a capsule.", 2, 4));
        library.addPuzzle(new NumberPuzzle("Java: int a = 7 / 2;  What is the value of a?", 3,
                "Integer division drops the decimal part.", 8, 4));
        rooms.add(library);

        // Level 3: 3 attempts per puzzle -> code 946
        Room vault = new Room("Secret Vault", "A cold steel chamber. One final door glows red.", 3, 3);
        vault.addPuzzle(new CodeLockPuzzle("Java: which keyword makes one class inherit from another?",
                "extends", "Child class ____ Parent class.", 9, 3));
        vault.addPuzzle(new NumberPuzzle("What is 2 to the power of 10?", 1024, "Double it ten times, starting from 1.", 4, 3));
        vault.addPuzzle(new CodeLockPuzzle("Java: which keyword creates a new object?",
                "new", "Three letters. Student s = ____ Student();", 6, 3));
        rooms.add(vault);

        return rooms;
    }
}
