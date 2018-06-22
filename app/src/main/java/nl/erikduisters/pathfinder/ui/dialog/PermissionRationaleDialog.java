package nl.erikduisters.pathfinder.ui.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;

/**
 * Created by Erik Duisters on 09-06-2018.
 */

public class PermissionRationaleDialog extends PositiveNegativeButtonMessageDialog {
    private static final String KEY_PERMISSION_REQUEST = "PermissionRequest";
    public interface Listener {
        void onPermissionRationaleAccepted(@NonNull RuntimePermissionRequest request);
        void onPermissionRationaleDenied(@NonNull RuntimePermissionRequest request);
    }

    private Listener listener;
    private RuntimePermissionRequest request;

    public PermissionRationaleDialog() {}

    public static PermissionRationaleDialog newInstance(@NonNull RuntimePermissionRequest request) {
        PermissionRationaleDialog dialog = (PermissionRationaleDialog) new PositiveNegativeButtonMessageDialog();

        Bundle args = dialog.getArguments();
        if (args == null) {
            args = new Bundle();
        }

        args.putParcelable(KEY_PERMISSION_REQUEST, request);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            request = getArguments().getParcelable(KEY_PERMISSION_REQUEST);
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    void onPositiveButtonClicked() {
        if (listener != null) {
            listener.onPermissionRationaleAccepted(request);
        }
    }

    @Override
    void onNegativeButtonClicked() {
        if (listener != null) {
            listener.onPermissionRationaleDenied(request);
        }
    }
}
