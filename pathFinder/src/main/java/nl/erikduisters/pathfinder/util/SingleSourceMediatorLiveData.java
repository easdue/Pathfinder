package nl.erikduisters.pathfinder.util;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Erik Duisters on 23-08-2018.
 */
public class SingleSourceMediatorLiveData<T> extends MediatorLiveData<T> {
    public interface Listener {
        void onActive();
        void onInactive();
    }

    private LiveData<?> currentLiveData;
    private @Nullable Listener listener;

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
    }

    @Override
    public <S> void addSource(@NonNull LiveData<S> source, @NonNull Observer<S> onChanged) {
        if (currentLiveData != null && currentLiveData != source) {
            removeSource(currentLiveData);
        }

        super.addSource(source, onChanged);

        currentLiveData = source;
    }

    @Override
    protected void onActive() {
        super.onActive();

        if (listener != null) {
            listener.onActive();
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();

        if (listener != null) {
            listener.onInactive();
        }
    }
}
