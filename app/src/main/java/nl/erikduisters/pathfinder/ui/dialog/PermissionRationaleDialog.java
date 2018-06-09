package nl.erikduisters.pathfinder.ui.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import nl.erikduisters.pathfinder.ui.fragment.runtime_permission.RuntimePermissionRequest;

/**
 * Created by Erik Duisters on 09-06-2018.
 */

public class PermissionRationaleDialog extends YesNoMessageDialog {
    private static final String KEY_PERMISSION_REQUEST = "PermissionRequest";
    public interface Listener {
        void onPermissionRationaleAccepted(@NonNull RuntimePermissionRequest request);
        void onPermissionRationaleDenied(@NonNull RuntimePermissionRequest request);
    }

    private Listener listener;
    private RuntimePermissionRequest request;

    public PermissionRationaleDialog() {}

    public static PermissionRationaleDialog newInstance(@NonNull RuntimePermissionRequest request) {
        PermissionRationaleDialog dialog = new PermissionRationaleDialog();

        dialog.setMessage(request.getPermissionRationaleMessage());

        Bundle args = dialog.getArguments();
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
    void onYesClicked() {
        if (listener != null) {
            listener.onPermissionRationaleAccepted(request);
        }
    }

    @Override
    void onNoClicked() {
        if (listener != null) {
            listener.onPermissionRationaleDenied(request);
        }
    }
}
