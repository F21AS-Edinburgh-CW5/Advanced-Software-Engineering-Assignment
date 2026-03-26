package coffeeshop.logging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton event logger using Bill Pugh (static inner holder) pattern.
 * Thread-safe lazy initialisation without synchronized or volatile.
 *
 * @author Lin Yi (Member D)
 */
public final class EventLogger {

    private EventLogger() {}

    // JVM class-loading guarantees thread-safe single initialisation
    private static final class Holder {
        private static final EventLogger INSTANCE = new EventLogger();
    }

    public static EventLogger getInstance() {
        return Holder.INSTANCE;
    }

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // All access guarded by synchronized(events)
    private final List<String> events = new ArrayList<>();

    /** Appends a timestamped event. Thread-safe. */
    public void log(String message) {
        String entry = "[" + LocalDateTime.now().format(FMT) + "] " + message;
        synchronized (events) {
            events.add(entry);
        }
        System.out.println(entry);
    }

    /** Writes all events to file at shutdown. */
    public void writeToFile(String filename) throws IOException {
        List<String> snapshot;
        synchronized (events) {
            snapshot = new ArrayList<>(events);
        }
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filename))) {
            for (String e : snapshot) {
                w.write(e);
                w.newLine();
            }
        }
        System.out.println("[LOGGER] Written to " + filename + " (" + snapshot.size() + " entries)");
    }

    /** Prints all events to console (Iter 1 convenience). */
    public void writeToConsole() {
        List<String> snapshot;
        synchronized (events) {
            snapshot = new ArrayList<>(events);
        }
        System.out.println("========== EVENT LOG (" + snapshot.size() + " entries) ==========");
        for (String e : snapshot) {
            System.out.println(e);
        }
        System.out.println("========== END ==========");
    }

    /** Returns an unmodifiable copy of all logged events. */
    public List<String> getEvents() {
        synchronized (events) {
            return Collections.unmodifiableList(new ArrayList<>(events));
        }
    }

    /** Clears all events (for testing). */
    public void clear() {
        synchronized (events) {
            events.clear();
        }
    }
}
