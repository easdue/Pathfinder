package nl.erikduisters.pathfinder.async;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.data.usecase.ProgressUseCase;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public class ProgressUseCaseJob<P, R, U extends ProgressUseCase<?, P, R>> extends BackgroundJob implements ProgressUseCase.Callback<P, R> {
    final U useCase;
    ProgressUseCase.Callback<P, R> callback;

    public ProgressUseCaseJob(@NonNull U usecase) {
        Timber.d("New %s created %s", usecase.getClass().getSimpleName(), this);

        this.useCase = usecase;
        this.callback = useCase.getCallback();

        this.useCase.setCallback(this);
    }

    public U getUseCase() { return useCase; }

    @Override
    public void run() {
        Timber.d("%s@%s.run()", useCase.getClass().getSimpleName(), this);
        useCase.execute(this);

        if (canceled) {
            finish();
        }
    }

    @Override
    public void onResult(@Nullable final R result) {
        backgroundJobHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onResult(result);
            }
        });
        finish();
    }

    @Override
    public void onProgress(@NonNull final P progress) {
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
    void finish() {
        super.finish();

        Timber.d("%s@%s Finished", useCase.getClass().getSimpleName(), this);
    }

    @Override
    public void cancel() {
        super.cancel();

        Timber.d("%s@%s Cancelled", getClass().getSimpleName(), this);
    }
}
