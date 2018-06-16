package nl.erikduisters.pathfinder.async;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public interface Cancellable {
    boolean isCancelled();
    void cancel();
}
