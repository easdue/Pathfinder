package nl.erikduisters.pathfinder.data.usecase;

import android.support.annotation.NonNull;

import org.oscim.theme.IRenderTheme;
import org.oscim.theme.ThemeFile;
import org.oscim.theme.ThemeLoader;

import nl.erikduisters.pathfinder.async.Cancellable;

/**
 * Created by Erik Duisters on 12-07-2018.
 */
public class LoadRenderTheme extends UseCase<ThemeFile, IRenderTheme> {
    public LoadRenderTheme(@NonNull ThemeFile requestInfo, @NonNull Callback<IRenderTheme> callback) {
        super(requestInfo, callback);
    }

    @Override
    public void execute(Cancellable cancellable) {
        try {
            IRenderTheme renderTheme = ThemeLoader.load(requestInfo);
            callback.onResult(renderTheme);
        } catch(IRenderTheme.ThemeException e) {
            callback.onError(e);
        }
    }
}
