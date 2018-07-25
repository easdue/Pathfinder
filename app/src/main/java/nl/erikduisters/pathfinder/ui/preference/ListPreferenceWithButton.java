package nl.erikduisters.pathfinder.ui.preference;

import android.content.Context;
import android.util.AttributeSet;

import net.xpece.android.support.preference.ListPreference;

import nl.erikduisters.pathfinder.R;

/**
 * Created by Erik Duisters on 24-07-2018.
 */
public class ListPreferenceWithButton extends ListPreference {
    public ListPreferenceWithButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        if (getDialogLayoutResource() == 0) {
            setDialogLayoutResource(R.layout.preference_list_fragment_with_button);
        }
    }

    public ListPreferenceWithButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPreferenceWithButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public ListPreferenceWithButton(Context context) {
        this(context, null);
    }
}
