package nl.erikduisters.pathfinder.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

/**
 * Created by Erik Duisters on 21-06-2018.
 */
public class MainThreadExecutor implements Executor {
    private final Handler handler;

    public MainThreadExecutor() {
        handler = new Handler(Looper.getMainLooper());
    }


    @Override
    public void execute(@NonNull Runnable runnable) {
        handler.post(runnable);
    }

    public void executeDelayed(@NonNull Runnable runnable, long delayMillis) {
        handler.postDelayed(runnable, delayMillis);
    }

    public void cancelDelayed(@NonNull Runnable runnable) {
        handler.removeCallbacks(runnable);
    }
}
