/**
 * Run this file.
 *   java Main              -> starts the web game and opens it in your browser
 *   java Main --terminal   -> plays the text version in the terminal
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--terminal")) {
            new Game().play();
        } else {
            new WebServer().start();
        }
    }
}
