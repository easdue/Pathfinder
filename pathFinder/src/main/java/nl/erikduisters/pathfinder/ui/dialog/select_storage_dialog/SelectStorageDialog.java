package nl.erikduisters.pathfinder.ui.dialog.select_storage_dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.fragment.init_storage.Storage;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 04-06-2018.
 */
public class SelectStorageDialog extends DialogFragment implements View.OnClickListener, StorageAdapter.OnItemClickListener {
    public static final String KEY_STORAGE_LIST="StorageList";

    public interface OnStorageSelectedListener {
        void onStorageSelected(Storage selectedStorage);
    }

    @BindView(R.id.ssd_title) TextView title;
    @BindView(R.id.ssd_recyclerView) RecyclerView recyclerView;
    @BindView(R.id.adbb_neutralButton) Button neutralButton;
    @BindView(R.id.adbb_negativeButton) Button negativeButton;
    @BindView(R.id.adbb_positiveButton) Button positiveButton;

    private StorageAdapter adapter;
    private ArrayList<Storage> storageList;
    private OnStorageSelectedListener onStorageSelectedListener;

    public SelectStorageDialog() {
        storageList = new ArrayList<>();
    }

    public static SelectStorageDialog newInstance(ArrayList<Storage> storageList) {
        Timber.e("SelectStorageDialog()");

        Bundle args = new Bundle();

        args.putParcelableArrayList(KEY_STORAGE_LIST, storageList);

        SelectStorageDialog dialog = new SelectStorageDialog();
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.e("onCreate(savedInstanceState=%s)", savedInstanceState == null ? "null" : "not null");

        setStyle(DialogFragment.STYLE_NO_TITLE, 0);

        Bundle args = getArguments();

        if (!args.containsKey(KEY_STORAGE_LIST)) {
            throw new RuntimeException("You must instantiate a new SelectStorageDialog using SelectStorageDialog.newInstance()");
        }

        storageList = args.getParcelableArrayList(KEY_STORAGE_LIST);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Timber.e("onCreateView(savedInstanceState=%s)", savedInstanceState == null ? "null" : "not null");
        View v = inflater.inflate(R.layout.select_storage_dialog, container, false);

        ButterKnife.bind(this, v);

        title.setText(R.string.select_storage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new StorageAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setStorageList(storageList, savedInstanceState);
        adapter.setOnItemClickListener(this);

        recyclerView.setHasFixedSize(true);

        neutralButton.setVisibility(View.INVISIBLE);
        negativeButton.setVisibility(View.INVISIBLE);
        positiveButton.setText(R.string.ok);
        positiveButton.setEnabled(adapter.getSelectedStoragePos() != -1);
        positiveButton.setOnClickListener(this);

        return v;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.e("onCreateDialog(savedInstanceState=%s)", savedInstanceState == null ? "null" : "not null");
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setCanceledOnTouchOutside(false);

        //TODO: Re-enable this after review because this requirement makes no sense for this dialog
        //this.setCancelable(false);

        return dialog;
    }

    public void setOnStorageSelectedListener(OnStorageSelectedListener onStorageSelectedListener) {
        this.onStorageSelectedListener = onStorageSelectedListener;
    }

    @Override
    public void onClick(View view) {
        if (onStorageSelectedListener != null) {
            int selectedPosition = adapter.getSelectedStoragePos();

            onStorageSelectedListener.onStorageSelected(storageList.get(selectedPosition));
        }

        dismiss();
    }

    //TODO: remove
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        onStorageSelectedListener.onStorageSelected(storageList.get(1));
    }

    @Override
    public void onItemClick(int position) {
        positiveButton.setEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Timber.e("onSaveInstanceState()");
        super.onSaveInstanceState(outState);

        adapter.saveInstanceState(outState);
    }
}
