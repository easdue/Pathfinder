package nl.erikduisters.pathfinder.data.usecase;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.async.Cancellable;
import nl.erikduisters.pathfinder.async.UseCaseJob;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public abstract class UseCase<I, R> {
    protected I requestInfo;
    protected Callback<R> callback;

    public UseCase(@NonNull I requestInfo, @NonNull Callback<R> callback) {
        this.requestInfo = requestInfo;
        this.callback = callback;
    }

    public I getRequestInfo() {
        return requestInfo;
    }

    public Callback<R> getCallback() {
        return callback;
    }

    public void setCallBack(@NonNull Callback<R> callback) {
        this.callback = callback;
    }

    public interface Callback<R> {
        void onResult(@Nullable R result);
        void onError(@NonNull Throwable error);
    }

    abstract public void execute(Cancellable cancellable);

    public UseCaseJob<R, ?> getUseCaseJob() {
        return new UseCaseJob<>(this);
    }
}
