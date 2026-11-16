import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Starts a small web server (built into the JDK, nothing to install) on your
 * own computer, serves web/index.html and answers the page's game requests.
 * It listens on localhost only, so nobody else on your network can reach it.
 */
public class WebServer {
    private final GameEngine engine = new GameEngine();

    public void start() throws IOException {
        Path webFolder = findWebFolder();
        if (webFolder == null) {
            System.out.println("Could not find web/index.html next to the Java files.");
            System.out.println("Open the whole JavaMystery folder in VS Code (File > Open Folder) and run again.");
            return;
        }

        HttpServer server = null;
        int port = 8080;
        for (; port <= 8090 && server == null; port++) {
            try {
                server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
            } catch (BindException e) {
                // port busy, try the next one
            }
        }
        port--;
        if (server == null) {
            System.out.println("Ports 8080-8090 are all busy. Close other programs and try again.");
            return;
        }

        server.createContext("/api/", this::handleApi);
        server.createContext("/", exchange -> serveIndex(exchange, webFolder));
        server.start();

        String url = "http://localhost:" + port + "/";
        System.out.println("=====================================");
        System.out.println("   JAVA MYSTERY is running");
        System.out.println("   Open: " + url);
        System.out.println("   (your browser should open by itself)");
        System.out.println("   Press the red Stop button or Ctrl+C here to quit.");
        System.out.println("=====================================");
        openBrowser(url);
    }

    // ------------------------------------------------------------------ API

    private void handleApi(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath().substring("/api/".length());
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String json;
            switch (path) {
                case "state":  json = engine.currentState(); break;
                case "scores": json = "{\"scores\":" + engine.scoresJson() + "}"; break;
                case "start":  json = engine.start(Json.field(body, "name")); break;
                case "enter":  json = engine.enter(); break;
                case "answer": json = engine.answer(Json.field(body, "answer")); break;
                case "hint":   json = engine.hint(); break;
                case "clues":  json = engine.clues(); break;
                case "door":   json = engine.door(Json.field(body, "code")); break;
                case "quit":   json = engine.quit(); break;
                default:
                    send(ex, 404, "application/json; charset=utf-8", "{\"error\":\"unknown route\"}");
                    return;
            }
            send(ex, 200, "application/json; charset=utf-8", json);
        } catch (RuntimeException e) {
            send(ex, 500, "application/json; charset=utf-8", "{\"error\":" + Json.str(String.valueOf(e)) + "}");
        }
    }

    // ------------------------------------------------------------- the page

    private void serveIndex(HttpExchange ex, Path webFolder) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (!path.equals("/") && !path.equals("/index.html")) {
            send(ex, 404, "text/plain; charset=utf-8", "Not found");
            return;
        }
        byte[] page = Files.readAllBytes(webFolder.resolve("index.html"));
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(200, page.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(page);
        }
    }

    private void send(HttpExchange ex, int status, String type, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    // -------------------------------------------------------------- helpers

    /** Looks for web/index.html in the run folder, then next to the compiled classes. */
    private Path findWebFolder() {
        java.util.ArrayList<Path> candidates = new java.util.ArrayList<>();
        candidates.add(Paths.get("web"));
        try {
            Path classes = Paths.get(WebServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            candidates.add(classes.resolve("web"));
            if (classes.getParent() != null) {
                candidates.add(classes.getParent().resolve("web"));
            }
        } catch (Exception e) {
            // ignore: fall back to the run folder only
        }
        for (Path p : candidates) {
            if (Files.isRegularFile(p.resolve("index.html"))) {
                return p;
            }
        }
        return null;
    }

    private void openBrowser(String url) {
        // 1) Java's own way
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return;
            }
        } catch (Exception e) {
            // try the operating system command below
        }
        // 2) The operating system's own "open" command (Mac, Windows, Linux)
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
            return;
        } catch (Exception e) {
            // fall through to the manual message
        }
        System.out.println("Could not open the browser automatically. Copy the link above into Chrome or Safari.");
    }
}
