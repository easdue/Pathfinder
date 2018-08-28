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

package nl.erikduisters.pathfinder.ui.dialog.import_settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.TrackActivityType;
import nl.erikduisters.pathfinder.util.menu.MyMenu;
import nl.erikduisters.pathfinder.util.recyclerView.ExpandableRecyclerViewAdapter;

class ImportSettingsAdapterData {

    private final List<Group> groupList;

    abstract static class Item extends ExpandableRecyclerViewAdapter.Item<Item> {
        static final int VIEW_TYPE_GROUP = 0;
        static final int VIEW_TYPE_CHECKBOX = 1;
        static final int VIEW_TYPE_SEEKBAR = 2;
        static final int VIEW_TYPE_RADIOGROUP = 3;
        static final int VIEW_TYPE_SPINNER = 4;
        static final int VIEW_TYPE_TRACK_ACTIVITY_TYPES = 5;
        static final int VIEW_TYPE_TRACK_TYPE = 6;
        static final int VIEW_TYPE_TRACK_LENGTH = 7;

        @IntDef({ItemType.GROUP, ItemType.GROUP_ENTRY})
        @Retention(RetentionPolicy.SOURCE)
        @interface ItemType {
            int GROUP = 0;
            int GROUP_ENTRY = 1;
        }

        private final @ItemType int itemType;

        Item(@ItemType int itemType) {
            this.itemType = itemType;
        }

        Item(Item item) {
            super(item);

            this.itemType = item.itemType;
        }

        @ItemType
        int itemType() {
            return itemType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Item item = (Item) o;

            return itemType == item.itemType;
        }

        @Override
        public int hashCode() {
            return itemType;
        }
    }

    abstract static class GroupEntry extends ImportSettingsAdapterData.Item {
        @IntDef({GroupEntryType.TYPE_CHECKBOX, GroupEntryType.TYPE_FILE,
                GroupEntryType.TYPE_SEEKBAR, GroupEntryType.TYPE_RADIOGROUP, GroupEntryType.TYPE_SPINNER,
                GroupEntryType.TYPE_TRACK_ACTIVITY_TYPES, GroupEntryType.TYPE_TRACK_TYPE, GroupEntryType.TYPE_TRACK_LENGTH})
        @Retention(RetentionPolicy.SOURCE)
        @interface GroupEntryType {
            int TYPE_CHECKBOX = 0;
            int TYPE_FILE = 1;
            int TYPE_SEEKBAR = 2;
            int TYPE_RADIOGROUP = 3;
            int TYPE_SPINNER = 4;
            int TYPE_TRACK_ACTIVITY_TYPES = 5;
            int TYPE_TRACK_TYPE =6;
            int TYPE_TRACK_LENGTH = 7;
        }

        @StringRes
        protected final int labelResId;
        protected final boolean isEnabled;
        private final boolean reportChange;

        GroupEntry(@StringRes int labelResId, boolean reportChange, boolean isEnabled) {
            super(ItemType.GROUP_ENTRY);

            this.labelResId = labelResId;
            this.reportChange = reportChange;
            this.isEnabled = isEnabled;
        }

        GroupEntry(GroupEntry other, boolean isEnabled) {
            super(other.itemType());

            this.labelResId = other.labelResId;
            this.reportChange = other.reportChange;
            this.isEnabled = isEnabled;
        }

        @NonNull
        static GroupEntry createFrom(@NonNull GroupEntry other, boolean isEnabled) {
            switch (other.getGroupEntryType()) {
                case GroupEntryType.TYPE_CHECKBOX:
                    return new GroupEntryCheckbox((GroupEntryCheckbox) other, isEnabled);
                case GroupEntryType.TYPE_FILE:
                    return new GroupEntryFile((GroupEntryFile) other, isEnabled);
                case GroupEntryType.TYPE_SEEKBAR:
                    return new GroupEntrySeekbar((GroupEntrySeekbar) other, isEnabled);
                case GroupEntryType.TYPE_RADIOGROUP:
                    return new GroupEntryRadiogroup((GroupEntryRadiogroup) other, isEnabled);
                case GroupEntryType.TYPE_SPINNER:
                    return new GroupEntrySpinner((GroupEntrySpinner) other, isEnabled);
                case GroupEntryType.TYPE_TRACK_ACTIVITY_TYPES:
                    return new GroupEntryTrackActivityTypes((GroupEntryTrackActivityTypes) other, isEnabled);
                case GroupEntryType.TYPE_TRACK_TYPE:
                    return new GroupEntryTrackType((GroupEntryTrackType) other, isEnabled);
                case GroupEntryType.TYPE_TRACK_LENGTH:
                    return new GroupEntryTrackLength((GroupEntryTrackLength) other, isEnabled);
                default:
                    throw new RuntimeException("Do not know how to handle the provided GroupEntry");
            }
        }

        @GroupEntryType
        abstract int getGroupEntryType();

        public boolean shouldReportChange() { return reportChange; }

        @StringRes
        int labelResId() {
            return labelResId;
        }

        @NonNull
        String getLabel(Context context) {
            if (labelResId > 0) {
                return context.getString(labelResId);
            }

            return "";
        }

        boolean isEnabled() { return isEnabled; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntry that = (GroupEntry) o;

            //if (groupEntryType != that.groupEntryType) return false;
            if (labelResId != that.labelResId) return false;

            return isEnabled == that.isEnabled;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            //result = 31 * result + groupEntryType;
            result = 31 * result + labelResId;
            result = 31 * result + (isEnabled ? 1 : 0);
            return result;
        }
    }

    static class GroupEntryCheckbox extends GroupEntry {
        private boolean isChecked;
        private @StringRes int subLabelResId;
        private Object[] subLabelArgs;

        GroupEntryCheckbox(@StringRes int labelResId, boolean isChecked, boolean reportChange) {
            this(labelResId, isChecked, reportChange, true);
        }

        GroupEntryCheckbox(@StringRes int labelResId, boolean isChecked, boolean reportChange, boolean isEnabled) {
            super(labelResId, reportChange, isEnabled);

            this.isChecked = isChecked;
            this.subLabelResId = R.string.empty;
        }

        GroupEntryCheckbox(GroupEntryCheckbox other, boolean isEnabled) {
            super(other, isEnabled);

            this.isChecked = other.isChecked;
            this.subLabelResId = other.subLabelResId;
            this.subLabelArgs = other.subLabelArgs;
        }

        @Override
        int getGroupEntryType() {
            return GroupEntryType.TYPE_CHECKBOX;
        }

        String getSubLabel(Context context) {
            return String.format(context.getString(subLabelResId), subLabelArgs);
        }

        @StringRes int getSubLabelResId() {
            return subLabelResId;
        }

        Object[] getSubLabelArgs() {
            return subLabelArgs;
        }

        boolean isChecked() {
            return isChecked;
        }

        void toggle() {
            isChecked = !isChecked;
        }

        @Override
        public int getViewType() {
            return Item.VIEW_TYPE_CHECKBOX;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntryCheckbox that = (GroupEntryCheckbox) o;

            if (isChecked != that.isChecked) return false;
            return subLabelResId == that.subLabelResId;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (isChecked ? 1 : 0);
            result = 31 * result + subLabelResId;
            return result;
        }

        @NonNull
        @Override
        protected Parcelable onSaveInstanceState() {
            Parcelable superState = super.onSaveInstanceState();

            SavedState savedState = new SavedState(superState);
            savedState.isChecked = isChecked;

            return savedState;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            if (!(state instanceof SavedState)) {
                super.onRestoreInstanceState(state);
                return;
            }

            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());

            isChecked = savedState.isChecked;
        }

        public static class SavedState extends Item.SavedState {
            boolean isChecked;

            protected SavedState(@NonNull Parcelable superState) {
                super(superState);
            }

            protected SavedState(Parcel source) {
                super(source);

                isChecked = source.readByte() != 0;
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);

                out.writeByte((byte) (isChecked ? 1 : 0));
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        public SavedState createFromParcel(Parcel in) {
                            return new SavedState(in);
                        }

                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };
        }
    }

    static class GroupEntryFile extends GroupEntryCheckbox {
        @NonNull
        private final File file;

        GroupEntryFile(int sequenceNumber, @NonNull File file, boolean selected, boolean reportChange) {
            this(sequenceNumber, file, selected, reportChange, true);
        }

        GroupEntryFile(int sequenceNumber, @NonNull File file, boolean selected, boolean reportChange, boolean isEnabled) {
            super(sequenceNumber, selected, reportChange, isEnabled);

            this.file = file;
        }

        GroupEntryFile(GroupEntryFile other, boolean isEnabled) {
            super(other, isEnabled);

            this.file = other.file;
        }

        @Override
        int getGroupEntryType() {
            return GroupEntryType.TYPE_FILE;
        }

        @NonNull
        File getFile() {
            return file;
        }

        @Override
        @NonNull
        String getLabel(Context context) {
            return file.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntryFile that = (GroupEntryFile) o;

            return file.equals(that.file);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + file.hashCode();
            return result;
        }
    }

    static class GroupEntrySeekbar extends GroupEntry {
        private int position;
        private int step;
        private int min;
        private int max;
        @StringRes
        private int valueResId;

        /**
         * @param labelResId The String resourceId to use as our label
         * @param step       The step size to use
         * @param min        The Minimum value this seekbar can represent. Must be a multiple of step or 0
         * @param max        The Maximum value this seekbar can represent. Does not have to be a multiple of step
         * @param valueResId  The string resource id of the unit this seekbar represents (eg. "%d km", "%d mi" etc)
         */
        GroupEntrySeekbar(int labelResId, int position, int step, int min, int max, @StringRes int valueResId) {
            this(labelResId, position, step, min, max, valueResId, false);
        }

        GroupEntrySeekbar(int labelResId, int position, int step, int min, int max, @StringRes int valueResId, boolean reportChange) {
            this(labelResId, position, step, min, max, valueResId, reportChange, true);
        }

        GroupEntrySeekbar(int labelResId, int position, int step, int min, int max, @StringRes int valueResId, boolean reportChange, boolean isEnabled) {
            super(labelResId, reportChange, isEnabled);

            this.step = step;
            this.min = min;
            this.max = max;
            this.valueResId = valueResId;
            this.position = position;

            if (this.min % this.step > 0) {
                throw new IllegalArgumentException("min must be a multiple of step or 0");
            }
        }

        GroupEntrySeekbar(GroupEntrySeekbar other, boolean isEnabled) {
            super(other, isEnabled);

            this.position = other.position;
            this.step = other.step;
            this.min = other.min;
            this.max = other.max;
            this.valueResId = other.valueResId;
        }

        @Override
        int getGroupEntryType() {
            return GroupEntryType.TYPE_SEEKBAR;
        }

        int position() {
            return position;
        }

        void setPosition(int pos) {
            position = pos;
        }

        void setMin(int min) {
            if (min % step > 0) {
                throw new IllegalArgumentException("min must be a multiple of step or 0");
            }
            this.min = min;
        }

        void setMax(int max) {
            this.max = max;

            if ( (min + (position * step)) > this.max) {
                position = maxProgress();
            }
        }

        /**
         * Calculates the maximum value to use in a Seekbar
         *
         * @return The maximum value to us in Seekbar.setMax()
         */
        int maxProgress() {
            if ((max - min) % step == 0) {
                return (max - min) / step;
            } else {
                return ((max - min) / step) + 1;
            }
        }

        @StringRes
        int valueResId() {
            return valueResId;
        }

        /**
         * Calculates the current value indicated by the seekbar
         *
         * @return The value this seekbar represents
         */
        int getValue() {
            int value = min + (position * step);

            if (value > max) {
                value = max;
            }

            return value;
        }

        @Override
        public int getViewType() {
            return Item.VIEW_TYPE_SEEKBAR;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntrySeekbar that = (GroupEntrySeekbar) o;

            if (position != that.position) return false;
            if (step != that.step) return false;
            if (min != that.min) return false;
            if (max != that.max) return false;
            return valueResId == that.valueResId;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + position;
            result = 31 * result + step;
            result = 31 * result + min;
            result = 31 * result + max;
            result = 31 * result + valueResId;
            return result;
        }

        @NonNull
        @Override
        protected Parcelable onSaveInstanceState() {
            Parcelable superState = super.onSaveInstanceState();

            SavedState savedState = new SavedState(superState);
            savedState.position = position;

            return savedState;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            if (!(state instanceof SavedState)) {
                super.onRestoreInstanceState(state);
                return;
            }

            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            position = savedState.position;
        }

        public static class SavedState extends Item.SavedState {
            int position;

            protected SavedState(@NonNull Parcelable superState) {
                super(superState);
            }

            protected SavedState(Parcel source) {
                super(source);

                position = source.readInt();
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);

                out.writeInt(position);
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        public SavedState createFromParcel(Parcel in) {
                            return new SavedState(in);
                        }

                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };

        }
    }

    static class GroupEntryRadiogroup extends GroupEntry {
        @StringRes
        private final int button1LabelResId;
        @StringRes
        private final int button2LabelResId;
        private int selectedButton;
        private boolean tristate;

        GroupEntryRadiogroup(int labelResId, @StringRes int button1LabelResId, @StringRes int button2LabelResId, int selectedButton, boolean tristate, boolean reportChange) {
            this(labelResId, button1LabelResId, button2LabelResId, selectedButton, tristate, reportChange, true);
        }

        GroupEntryRadiogroup(int labelResId, @StringRes int button1LabelResId, @StringRes int button2LabelResId, int selectedButton, boolean tristate, boolean reportChange, boolean isEnabled) {
            super(labelResId, reportChange, isEnabled);

            this.button1LabelResId = button1LabelResId;
            this.button2LabelResId = button2LabelResId;
            this.selectedButton = selectedButton;
            this.tristate = tristate;
        }

        GroupEntryRadiogroup(GroupEntryRadiogroup other, boolean isEnabled) {
            super(other, isEnabled);

            this.button1LabelResId = other.button1LabelResId;
            this.button2LabelResId = other.button2LabelResId;
            this.selectedButton = other.selectedButton;
            this.tristate = other.tristate;
        }

        @Override
        int getGroupEntryType() {
            return GroupEntryType.TYPE_RADIOGROUP;
        }

        int selectedButton() {
            return selectedButton;
        }

        void toggleSelection() {
            if (!isEnabled()) {
                return;
            }

            selectedButton++;

            if (selectedButton > 2) {
                if (tristate) {
                    selectedButton = 0;
                } else {
                    selectedButton = 1;
                }
            }
        }

        @StringRes
        int button1LabelResId() {
            return button1LabelResId;
        }

        @StringRes
        int button2LlabelResId() {
            return button2LabelResId;
        }

        @Override
        public int getViewType() {
            return Item.VIEW_TYPE_RADIOGROUP;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntryRadiogroup that = (GroupEntryRadiogroup) o;

            if (button1LabelResId != that.button1LabelResId) return false;
            if (button2LabelResId != that.button2LabelResId) return false;
            if (selectedButton != that.selectedButton) return false;
            return tristate == that.tristate;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + button1LabelResId;
            result = 31 * result + button2LabelResId;
            result = 31 * result + selectedButton;
            result = 31 * result + (tristate ? 1 : 0);
            return result;
        }

        @NonNull
        @Override
        protected Parcelable onSaveInstanceState() {
            Parcelable superState = super.onSaveInstanceState();

            SavedState savedState = new SavedState(superState);
            savedState.selectedButton = selectedButton;

            return savedState;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            if (!(state instanceof SavedState)) {
                super.onRestoreInstanceState(state);
                return;
            }

            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            selectedButton = savedState.selectedButton;
        }

        public static class SavedState extends Item.SavedState {
            int selectedButton;

            protected SavedState(@NonNull Parcelable superState) {
                super(superState);
            }

            protected SavedState(Parcel source) {
                super(source);

                selectedButton = source.readInt();
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);

                out.writeInt(selectedButton);
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        public SavedState createFromParcel(Parcel in) {
                            return new SavedState(in);
                        }

                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };

        }
    }

    static class GroupEntrySpinner extends GroupEntry {
        @MenuRes
        private int menuResId;
        @NonNull
        private MyMenu menu;
        private @IdRes int selectedMenuItemId;

        GroupEntrySpinner(@StringRes int labelResId, @MenuRes int menuResId, @NonNull MyMenu menu, @IdRes int selectedMenuItemId, boolean reportChange) {
            this(labelResId, menuResId, menu, selectedMenuItemId, reportChange, true);
        }

        GroupEntrySpinner(@StringRes int labelResId, @MenuRes int menuResId, @NonNull MyMenu menu, @IdRes int selectedMenuItemId, boolean reportChange, boolean isEnabled) {
            super(labelResId, reportChange, isEnabled);

            this.menuResId = menuResId;
            this.menu = menu;
            this.selectedMenuItemId = selectedMenuItemId;
        }

        GroupEntrySpinner(GroupEntrySpinner other, boolean isEnabled) {
            super(other, isEnabled);

            this.menuResId = other.menuResId;
            this.menu = other.menu;
            this.selectedMenuItemId = other.selectedMenuItemId;
        }

        @Override
        int getGroupEntryType() {
            return GroupEntryType.TYPE_SPINNER;
        }

        @MenuRes
        int getMenuResId() {
            return menuResId;
        }

        @NonNull
        MyMenu getMenu() {
            return menu;
        }

        @IdRes int selectedMenuItemId() {
            return selectedMenuItemId;
        }

        void setSelectedMenuItemId(@IdRes int selectedMenuItemId) {
            this.selectedMenuItemId = selectedMenuItemId;
        }

        @Override
        public int getViewType() {
            return Item.VIEW_TYPE_SPINNER;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntrySpinner that = (GroupEntrySpinner) o;

            if (menuResId != that.menuResId) return false;
            if (selectedMenuItemId != that.selectedMenuItemId) return false;
            return menu.equals(that.menu);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + menu.hashCode();
            result = 31 * result + selectedMenuItemId;
            result = 31 * result + menuResId;
            return result;
        }

        @NonNull
        @Override
        protected Parcelable onSaveInstanceState() {
            Parcelable superState = super.onSaveInstanceState();

            SavedState savedState = new SavedState(superState);
            savedState.selectedMenuItemId = selectedMenuItemId;

            return savedState;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            if (!(state instanceof SavedState)) {
                super.onRestoreInstanceState(state);
                return;
            }

            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            selectedMenuItemId = savedState.selectedMenuItemId;
        }

        public static class SavedState extends Item.SavedState {
            @IdRes int selectedMenuItemId;

            protected SavedState(@NonNull Parcelable superState) {
                super(superState);
            }

            protected SavedState(Parcel source) {
                super(source);

                selectedMenuItemId = source.readInt();
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);

                out.writeInt(selectedMenuItemId);
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        public SavedState createFromParcel(Parcel in) {
                            return new SavedState(in);
                        }

                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };

        }
    }

    static class GroupEntryTrackActivityTypes extends GroupEntry {
        private boolean[] trackActivityTypeIncluded;

        GroupEntryTrackActivityTypes(int labelResId, boolean reportChange) {
            this(labelResId, reportChange, true);
        }

        GroupEntryTrackActivityTypes(int labelResId, boolean reportChange, boolean isEnabled) {
            super(labelResId, reportChange, isEnabled);

            int numTrackTypes = TrackActivityType.values().length;

            trackActivityTypeIncluded = new boolean[numTrackTypes];

            for (int i = 0; i < numTrackTypes; i++) {
                trackActivityTypeIncluded[i] = true;
            }
        }

        GroupEntryTrackActivityTypes(GroupEntryTrackActivityTypes other, boolean isEnabled) {
            super(other, isEnabled);

            this.trackActivityTypeIncluded = other.trackActivityTypeIncluded;
        }

        @Override
        int getGroupEntryType() {
            return GroupEntryType.TYPE_TRACK_ACTIVITY_TYPES;
        }

        void setIncluded(TrackActivityType trackActivityType, boolean included) {
            trackActivityTypeIncluded[trackActivityType.code()] = included;
        }

        boolean isIncluded(TrackActivityType trackActivityType) {
            return trackActivityTypeIncluded[trackActivityType.code()];
        }

        boolean areAllTrackActivityTypesExcluded() {
            for (int i = 0; i < trackActivityTypeIncluded.length; i++) {
                if (trackActivityTypeIncluded[i]) {
                    return false;
                }
            }

            return true;
        }

        List<TrackActivityType> getIncludedTrackActivityTypes() {
            List<TrackActivityType> includedTrackActivityTypes = new ArrayList<>();

            for (TrackActivityType activityType : TrackActivityType.values()) {
                if (trackActivityTypeIncluded[activityType.code()]) {
                    includedTrackActivityTypes.add(activityType);
                }
            }

            return includedTrackActivityTypes;
        }

        @Override
        public int getViewType() {
            return Item.VIEW_TYPE_TRACK_ACTIVITY_TYPES;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntryTrackActivityTypes that = (GroupEntryTrackActivityTypes) o;

            return Arrays.equals(trackActivityTypeIncluded, that.trackActivityTypeIncluded);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Arrays.hashCode(trackActivityTypeIncluded);
            return result;
        }

        @NonNull
        @Override
        protected Parcelable onSaveInstanceState() {
            Parcelable superState = super.onSaveInstanceState();

            SavedState savedState = new SavedState(superState);
            savedState.trackActivityTypeIncluded = trackActivityTypeIncluded;

            return savedState;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            if (!(state instanceof SavedState)) {
                super.onRestoreInstanceState(state);
                return;
            }

            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            trackActivityTypeIncluded = savedState.trackActivityTypeIncluded;
        }

        public static class SavedState extends Item.SavedState {
            boolean[] trackActivityTypeIncluded;

            protected SavedState(@NonNull Parcelable superState) {
                super(superState);
            }

            protected SavedState(Parcel source) {
                super(source);

                trackActivityTypeIncluded = source.createBooleanArray();
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);

                out.writeBooleanArray(trackActivityTypeIncluded);
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        public SavedState createFromParcel(Parcel in) {
                            return new SavedState(in);
                        }

                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };

        }
    }

    static class GroupEntryTrackType extends GroupEntry {
        boolean roundTripChecked;
        boolean oneWayChecked;

        GroupEntryTrackType(@StringRes int labelResId, boolean roundTripChecked, boolean oneWayChecked) {
            this(labelResId, roundTripChecked, oneWayChecked, false);
        }

        GroupEntryTrackType(@StringRes int labelResId, boolean roundTripChecked, boolean oneWayChecked, boolean reportChange) {
            this(labelResId, roundTripChecked, oneWayChecked, reportChange, true);
        }

        GroupEntryTrackType(@StringRes int labelResId, boolean roundTripChecked, boolean oneWayChecked, boolean reportChange, boolean isEnabled) {
            super(labelResId, reportChange, isEnabled);

            this.roundTripChecked = roundTripChecked;
            this.oneWayChecked = oneWayChecked;
        }

        GroupEntryTrackType(GroupEntryTrackType other, boolean isEnabled) {
            super(other, isEnabled);

            this.roundTripChecked = other.roundTripChecked;
            this.oneWayChecked = other.oneWayChecked;
        }

        @Override
        int getGroupEntryType() {
            return GroupEntryType.TYPE_TRACK_TYPE;
        }

        boolean isRoundTripChecked() { return roundTripChecked; }
        boolean isOneWayChecked() { return oneWayChecked; }

        @Override
        public int getViewType() {
            return Item.VIEW_TYPE_TRACK_TYPE;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntryTrackType that = (GroupEntryTrackType) o;

            if (roundTripChecked != that.roundTripChecked) return false;
            return oneWayChecked == that.oneWayChecked;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (roundTripChecked ? 1 : 0);
            result = 31 * result + (oneWayChecked ? 1 : 0);
            return result;
        }

        @NonNull
        @Override
        protected Parcelable onSaveInstanceState() {
            Parcelable superState = super.onSaveInstanceState();

            SavedState savedState = new SavedState(superState);
            savedState.roundTripChecked = roundTripChecked;
            savedState.oneWayChecked = oneWayChecked;

            return savedState;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            if (!(state instanceof SavedState)) {
                super.onRestoreInstanceState(state);
                return;
            }

            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            roundTripChecked = savedState.roundTripChecked;
            oneWayChecked = savedState.oneWayChecked;
        }

        public static class SavedState extends Item.SavedState {
            boolean roundTripChecked;
            boolean oneWayChecked;

            protected SavedState(@NonNull Parcelable superState) {
                super(superState);
            }

            protected SavedState(Parcel source) {
                super(source);

                roundTripChecked = source.readByte() != 0;
                oneWayChecked = source.readByte() != 0;
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);

                out.writeByte((byte) (roundTripChecked ? 1 : 0));
                out.writeByte((byte) (oneWayChecked ? 1 : 0));
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        public SavedState createFromParcel(Parcel in) {
                            return new SavedState(in);
                        }

                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };

        }
    }

    static class GroupEntryTrackLength extends GroupEntry {
        private final int INDEX_MIN = 0;
        private final int INDEX_MAX = 1;

        @StringRes int minLabelResId;
        @StringRes int maxLabelResId;

        private int step;
        private int min;
        private int max;
        @StringRes
        private int valueResId;

        private int[] positions;

        /**
         * @param labelResId The String resourceId to use as our label
         * @param step       The step size to use
         * @param min        The Minimum value this seekbar can represent. Must be a multiple of step or 0
         * @param max        The Maximum value this seekbar can represent. Does not have to be a multiple of step
         * @param valueResId  The string resource id of the unit this seekbar represents (eg. "%d km", "%d mi" etc)
         */
        GroupEntryTrackLength(@StringRes int labelResId, @StringRes int minLabelResId, @StringRes int maxLabelResId, int step, int min, int max, @StringRes int valueResId) {
            this(labelResId, minLabelResId, maxLabelResId, step, min, max, valueResId, false);
        }

        GroupEntryTrackLength(@StringRes int labelResId, @StringRes int minLabelResId, @StringRes int maxLabelResId, int step, int min, int max, @StringRes int valueResId, boolean reportChange) {
            this(labelResId, minLabelResId, maxLabelResId, step, min, max, valueResId, reportChange, true);

            setPositionForValue(INDEX_MIN, min);
            setPositionForValue(INDEX_MAX, max);
        }

        GroupEntryTrackLength(@StringRes int labelResId, @StringRes int minLabelResId, @StringRes int maxLabelResId, int step, int min, int max, @StringRes int valueResId, boolean reportChange, boolean enabled) {
            this(labelResId, minLabelResId, maxLabelResId, step, min, max, valueResId, 0, 0, reportChange, enabled);

            setPositionForValue(INDEX_MIN, min);
            setPositionForValue(INDEX_MAX, max);
        }

        GroupEntryTrackLength(@StringRes int labelResId, @StringRes int minLabelResId, @StringRes int maxLabelResId, int step, int min, int max, @StringRes int valueResId, int minLengthPosition, int maxLengthPosition, boolean reportChange, boolean isEnabled) {
            super(labelResId, reportChange, isEnabled);

            positions = new int[2];

            this.minLabelResId = minLabelResId;
            this.maxLabelResId = maxLabelResId;

            this.step = step;
            this.min = min;
            this.max = max;
            this.valueResId = valueResId;
            this.positions[INDEX_MIN] = minLengthPosition;
            this.positions[INDEX_MAX] = maxLengthPosition;

            if (this.min % this.step > 0) {
                throw new IllegalArgumentException("min must be a multiple of step or 0");
            }
        }

        GroupEntryTrackLength(GroupEntryTrackLength other, boolean isEnabled) {
            super(other, isEnabled);

            this.step = other.step;
            this.min = other.min;
            this.max = other.max;
            this.valueResId = other.valueResId;
            this.positions = Arrays.copyOf(other.positions, other.positions.length);
        }

        @Override
        int getGroupEntryType() {
            return GroupEntryType.TYPE_TRACK_LENGTH;
        }

        private int getPosition(int index) {
            return positions[index];
        }
        public int getMinPosition() { return getPosition(INDEX_MIN); }
        public int getMaxPosition() { return getPosition(INDEX_MAX); }

        private void setPosition(int index, int pos) {
            positions[index] = pos;
        }
        public void setMinPosition(int pos) { setPosition(INDEX_MIN, pos); }
        public void setMaxPosition(int pos) { setPosition(INDEX_MAX, pos); }

        void setPositionForValue(int index, int value) {
            if (value < min) value = min;
            else if (value > max) value = max;

            if (value % step > 0) {
                throw new IllegalArgumentException("value must be a multiple of step");
            }

            positions[index] = (value - min) / step;
        }

        void setMin(int min) {
            if (min % step > 0) {
                throw new IllegalArgumentException("min must be a multiple of step or 0");
            }
            this.min = min;
        }

        void setMax(int max) {
            this.max = max;

            for (int index = INDEX_MIN; index <= INDEX_MAX; index++) {
                if ( (min + (positions[index] * step)) > this.max) {
                    positions[index] = maxProgress();
                }
            }
        }

        /**
         * Calculates the maximum value to use in a Seekbar
         *
         * @return The maximum value to us in Seekbar.setMax()
         */
        int maxProgress() {
            if ((max - min) % step == 0) {
                return (max - min) / step;
            } else {
                return ((max - min) / step) + 1;
            }
        }

        @StringRes
        int valueResId() {
            return valueResId;
        }

        /**
         * Calculates the current value indicated by the seekbar
         *
         * @return The value this seekbar represents
         */
        private int getValue(int index) {
            int value = min + (positions[index] * step);

            if (value > max) {
                value = max;
            }

            return value;
        }

        int getMinValue() { return getValue(INDEX_MIN); }
        int getMaxValue() { return getValue(INDEX_MAX); }

        @Override
        public int getViewType() {
            return Item.VIEW_TYPE_TRACK_LENGTH;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            GroupEntryTrackLength that = (GroupEntryTrackLength) o;

            if (step != that.step) return false;
            if (min != that.min) return false;
            if (max != that.max) return false;
            if (valueResId != that.valueResId) return false;
            return Arrays.equals(positions, that.positions);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + step;
            result = 31 * result + min;
            result = 31 * result + max;
            result = 31 * result + valueResId;
            result = 31 * result + Arrays.hashCode(positions);
            return result;
        }

        @NonNull
        @Override
        protected Parcelable onSaveInstanceState() {
            Parcelable superState = super.onSaveInstanceState();

            SavedState savedState = new SavedState(superState);
            savedState.positions = positions;

            return savedState;
        }

        @Override
        public void onRestoreInstanceState(Parcelable state) {
            if (!(state instanceof SavedState)) {
                super.onRestoreInstanceState(state);
                return;
            }

            SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            positions = savedState.positions;
        }

        public static class SavedState extends Item.SavedState {
            int[] positions;

            protected SavedState(@NonNull Parcelable superState) {
                super(superState);
            }

            protected SavedState(Parcel source) {
                super(source);

                positions = source.createIntArray();
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);

                out.writeIntArray(positions);
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        public SavedState createFromParcel(Parcel in) {
                            return new SavedState(in);
                        }

                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };

        }
    }

    static abstract class Group extends Item {
        @IntDef({GroupType.TYPE_SEARCH_TRACKS_NEARBY, GroupType.TYPE_IMPORT_LOCAL_FILES})
        @Retention(RetentionPolicy.SOURCE)
        @interface GroupType {
            int TYPE_SEARCH_TRACKS_NEARBY = 0;
            int TYPE_IMPORT_LOCAL_FILES = 1;
        }

        @StringRes
        private final int nameResId;
        private final @GroupType int groupType;
        private boolean isEnabled;

        Group(@GroupType int groupType, @StringRes int nameResId, boolean isEnabled) {
            super(ItemType.GROUP);

            this.groupType = groupType;
            this.nameResId = nameResId;
            this.isEnabled = isEnabled;
        }

        Group(Group oldGroup) {
            super(oldGroup);

            this.groupType = oldGroup.groupType;
            this.nameResId = oldGroup.nameResId;
            this.isEnabled = oldGroup.isEnabled;
        }

        @StringRes
        int nameResId() {
            return nameResId;
        }

        @GroupType
        int getGroupType() {
            return groupType;
        }

        boolean isEnabled() {
            return isEnabled;
        }

        void setEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        @NonNull
        <T extends GroupEntry> T findGroupEntryByLabel(@StringRes int labelResId) {
            for (Item item : getChildren()) {
                GroupEntry groupEntry = (GroupEntry) item;
                if (groupEntry.labelResId == labelResId) {
                    return (T) groupEntry;
                }
            }

            throw new IllegalStateException("No GroupEntry with the requested label exists");
        }

        @Override
        public int getViewType() {
            return Item.VIEW_TYPE_GROUP;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            Group group = (Group) o;

            if (nameResId != group.nameResId) return false;
            if (groupType != group.groupType) return false;
            //Children don't count for DiffUtil.calculateDiff
            return isEnabled == group.isEnabled;
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + nameResId;
            result = 31 * result + groupType;
            result = 31 * result + (isEnabled ? 1 : 0);
            return result;
        }

        protected Parcelable onSaveInstanceState() {
            Parcelable superState = super.onSaveInstanceState();

            SavedState savedState = new SavedState(superState);

            int numChildren = numChildren();

            savedState.childLabelResIds = new int[numChildren];

            for (int i = 0; i < numChildren; i++) {
                savedState.childLabelResIds[i] = ((GroupEntry)getChild(i)).labelResId;
            }

            return savedState;
        }

        @SuppressLint("MissingSuperCall")
        @Override
        public void onRestoreInstanceState(Parcelable parcelable) {
            //super.onRestoreInstanceState(parcelable);
            SavedState savedState = (SavedState) parcelable;

            Parcelable superState = savedState.getSuperState();

            super.onRestoreInstanceState(superState, false);

            Parcelable[] childState = super.getChildState(superState);

            int numChildren = childState.length;

            for (int i = 0; i < numChildren; i++) {
                for (Item item : getChildren()) {
                    GroupEntry groupEntry = (GroupEntry) item;

                    if (groupEntry.labelResId == savedState.childLabelResIds[i]) {
                        item.onRestoreInstanceState(childState[i]);
                        break;
                    }
                }
            }
        }

        public static class SavedState extends Item.SavedState {
            int[] childLabelResIds;

            SavedState(@NonNull Parcelable superState) {
                super(superState);
            }

            protected SavedState(Parcel source) {
                super(source);

                childLabelResIds = source.createIntArray();
            }

            @Override
            public void writeToParcel(Parcel out, int flags) {
                super.writeToParcel(out, flags);

                out.writeIntArray(childLabelResIds);
            }

            public static final Parcelable.Creator<SavedState> CREATOR =
                    new Parcelable.Creator<SavedState>() {
                        public SavedState createFromParcel(Parcel in) {
                            return new SavedState(in);
                        }

                        public SavedState[] newArray(int size) {
                            return new SavedState[size];
                        }
                    };

        }
    }

    static class SearchTracksOnGpsiesGroup extends Group {
        SearchTracksOnGpsiesGroup(@StringRes int nameResId, boolean isEnabled) {
            super(GroupType.TYPE_SEARCH_TRACKS_NEARBY, nameResId, isEnabled);
        }

        SearchTracksOnGpsiesGroup(SearchTracksOnGpsiesGroup other) {
            super(other);
        }
    }

    static class ImportLocalFilesGroup extends Group {
        ImportLocalFilesGroup(@StringRes int nameResId, boolean isEnabled) {
            super(GroupType.TYPE_IMPORT_LOCAL_FILES, nameResId, isEnabled);
        }

        ImportLocalFilesGroup(ImportLocalFilesGroup other) {
            super(other);
        }
    }

    ImportSettingsAdapterData() {
        groupList = new ArrayList<>();
    }

    ImportSettingsAdapterData(ImportSettingsAdapterData other) {
        groupList = other.groupList;
    }

    @NonNull
    Group getGroupOfType(@Group.GroupType int type) {
        for (Group group : groupList) {
            if (group.getGroupType() == type) {
                return group;
            }
        }

        throw new IllegalStateException("No group with the requested type exists");
    }

    boolean add(Group group) {
        for (Group g : groupList) {
            if (g.getGroupType() == group.getGroupType()) {
                return false;
            }
        }

        groupList.add(group);

        return true;
    }

    Group get(int group) {
        if (group < groupList.size()) {
            return groupList.get(group);
        }

        return null;
    }

    List<Group> getGroups() {
        return groupList;
    }

    List<Item> getItems() {
        return new ArrayList<>(groupList);
    }

    public SavedState onSaveInstanceState() {
        SavedState savedState = new SavedState();

        savedState.groupStates = new Parcelable[groupList.size()];

        for(int i = 0; i < groupList.size(); i++) {
            savedState.groupStates[i] = groupList.get(i).onSaveInstanceState();
        }

        return savedState;
    }

    public void onRestoreInstanceState(SavedState state) {
        SavedState savedState = (SavedState) state;

        for (int i = 0; i < savedState.groupStates.length; i++) {
            groupList.get(i).onRestoreInstanceState(savedState.groupStates[i]);
        }
    }

    static class SavedState implements Parcelable {
        // Per group save each groupEntry's state
        Parcelable[] groupStates;

        private SavedState() {}

        protected SavedState(Parcel source) {
            groupStates = source.readParcelableArray(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelableArray(groupStates, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                SavedState savedState = new SavedState(source);

                return savedState;
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}