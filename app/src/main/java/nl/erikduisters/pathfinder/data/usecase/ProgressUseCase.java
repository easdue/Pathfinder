package nl.erikduisters.pathfinder.data.usecase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.async.Cancellable;
import nl.erikduisters.pathfinder.async.ProgressUseCaseJob;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public abstract class ProgressUseCase<I, P, R> {
    I requestInfo;
    Callback<P, R> callback;

    ProgressUseCase(@Nullable I requestInfo, @NonNull Callback<P, R> callback) {
        this.requestInfo = requestInfo;
        this.callback = callback;
    }

    public Callback<P, R> getCallback() {
        return callback;
    }

    public void setCallback(@NonNull Callback<P, R> callback) {
        this.callback = callback;
    }

    public interface Callback<P, R> extends UseCase.Callback<R> {
        void onResult(@Nullable R result);
        void onProgress(@NonNull P progress);
        void onError(@NonNull Throwable error);
        void onFinished();
    }

    abstract public void execute(Cancellable cancellable);

    public ProgressUseCaseJob<P, R, ?> getUseCaseJob() {
        return new ProgressUseCaseJob<>(this);
    }
}
