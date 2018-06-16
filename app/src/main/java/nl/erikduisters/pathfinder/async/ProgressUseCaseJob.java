package nl.erikduisters.pathfinder.async;

import android.support.annotation.NonNull;

import nl.erikduisters.pathfinder.data.usecase.ProgressUseCase;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public class ProgressUseCaseJob<P, R, U extends ProgressUseCase<?, P, R>> extends BackgroundJob implements ProgressUseCase.Callback<P, R> {
    final U useCase;
    ProgressUseCase.Callback<P, R> callback;

    public ProgressUseCaseJob(@NonNull U usecase) {
        this.useCase = usecase;
        this.callback = useCase.getCallback();

        this.useCase.setCallback(this);
    }

    @Override
    public void run() {
        useCase.execute(this);

        if (canceled) {
            finish();
        }
    }

    @Override
    public void onResult(@NonNull final R result) {
        backgroundJobHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onResult(result);
            }
        });
    }

    @Override
    public void onProgress(final P progress) {
        backgroundJobHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onProgress(progress);
            }
        });
    }

    @Override
    public void onError(@NonNull final Throwable error) {
        backgroundJobHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onError(error);
            }
        });
        finish();
    }

    @Override
    public void onFinished() {
        backgroundJobHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onFinished();
            }
        });

        finish();
    }
}
