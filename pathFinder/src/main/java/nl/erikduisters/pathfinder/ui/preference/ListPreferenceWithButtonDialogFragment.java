/*
 * Copyright (c) 2018 Erik Duisters
 *
 * This file is part of Pathfinder
 *
 * Pathfinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pathfinder. If not, see <https://www.gnu.org/licenses/>.
 */

package nl.erikduisters.pathfinder.ui.preference;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.xpece.android.support.preference.ListPreference;
import net.xpece.android.support.preference.XpListPreferenceDialogFragment;
import net.xpece.android.support.preference.XpPreferenceDialogFragment;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 24-07-2018.
 */

//TODO: Create a style that enables setting button text and empty text in xml
public class ListPreferenceWithButtonDialogFragment
        extends XpListPreferenceDialogFragment
        implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {

    public interface ButtonClickListener {
        void onButtonClicked(ListPreferenceWithButton preference);
    }

    public static final String ARG_BUTTON_TEXT = "ButtonText";
    public static final String ARG_EMPTY_TEXT = "EmptyText";

    private Button button;
    private TextView textView;
    private ListView listView;
    private boolean hideListView;
    private ButtonClickListener listener;

    public static ListPreferenceWithButtonDialogFragment newInstance(String preferenceKey, @StringRes int buttonText, @StringRes int emptyText) {
        ListPreferenceWithButtonDialogFragment fragment = new ListPreferenceWithButtonDialogFragment();

        Bundle args = new Bundle();
        args.putString(XpPreferenceDialogFragment.ARG_KEY, preferenceKey);
        args.putInt(ARG_BUTTON_TEXT, buttonText);
        args.putInt(ARG_EMPTY_TEXT, emptyText);

        fragment.setArguments(args);

        return fragment;
    }

    public ListPreferenceWithButtonDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if (args == null || !args.containsKey(ARG_BUTTON_TEXT) || !args.containsKey(ARG_EMPTY_TEXT)) {
            throw new IllegalStateException("You have to instantiate a new ListPreferenceWithButtonDialogFragment by calling newInstance(..)");
        }
    }

    public void setButtonClickListener(ButtonClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = (AlertDialog) super.onCreateDialog(savedInstanceState);
        listView = dialog.getListView();
        listView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        button.setText(getArguments().getInt(ARG_BUTTON_TEXT));
        button.setOnClickListener(this);

        ListPreference listPreference = (ListPreference) getPreference();

        if (listPreference.getEntries().length == 0) {
            textView.setText(getArguments().getInt(ARG_EMPTY_TEXT));
            textView.setVisibility(View.VISIBLE);
            hideListView = true;
        } else {
            textView.setVisibility(View.GONE);
            hideListView = false;
        }

        return dialog;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        button = view.findViewById(R.id.button);
        textView = view.findViewById(android.R.id.empty);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onButtonClicked((ListPreferenceWithButton) getPreference());

            dismiss();
        }
    }

    @Override
    public void onGlobalLayout() {
        if (Build.VERSION.SDK_INT >= 16) {
            listView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        } else {
            listView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }

        ViewParent viewParent = listView.getParent();

        /*
         * If you want something done right you have to do it yourself.
         * The RecyclerViews containing FrameLayout has height:wrap_content but the RecyclerView as height:match_parent
         */
        FrameLayout frameLayout = (FrameLayout) listView.getParent();
        frameLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, 0, 1.0f));
        frameLayout.requestLayout();

        if (viewParent != null && viewParent instanceof View) {
            ((View) listView.getParent()).setVisibility(hideListView ? View.GONE : View.VISIBLE);
        }
    }
}
