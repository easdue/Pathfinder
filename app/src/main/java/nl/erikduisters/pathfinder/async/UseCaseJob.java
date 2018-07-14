package nl.erikduisters.pathfinder.async;

import android.support.annotation.NonNull;

import nl.erikduisters.pathfinder.data.usecase.UseCase;

/**
 * Created by Erik Duisters on 16-06-2018.
 */

public class UseCaseJob<R, U extends UseCase<?, R>> extends BackgroundJob implements UseCase.Callback<R> {
    private final U useCase;
    private UseCase.Callback<R> callback;

    public UseCaseJob(@NonNull U usecase) {
        super();

        this.useCase = usecase;
        this.callback = usecase.getCallback();

        useCase.setCallBack(this);
    }

    @Override
    public void run() {
        useCase.execute(this);

        if (canceled) {
            finish();
        }
    }

    public U getUseCase() {
        return useCase;
    }

    @Override
    public void onResult(@NonNull final R result) {
        backgroundJobHandler.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.onResult(result);
            }
        });
        finish();
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
}
