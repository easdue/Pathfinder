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

import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.view.menu.MyPopupMenu;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.data.model.TrackActivityType;
import nl.erikduisters.pathfinder.ui.widget.SvgView;
import nl.erikduisters.pathfinder.util.menu.MyMenuItem;
import nl.erikduisters.pathfinder.util.recyclerView.ExpandableRecyclerViewAdapter;
import nl.erikduisters.pathfinder.util.recyclerView.ExpansionControlClickListener;
import timber.log.Timber;

//TODO: Save state for when app is killed
class ImportSettingsAdapter extends ExpandableRecyclerViewAdapter<ImportSettingsAdapterData.Item> implements
        SvgView.SVGViewLoadedListener {

    interface OnChangedListener {
        void onChanged(ImportSettingsAdapterData.GroupEntry changedGroupEntry);
    }

    private int numSvgsToLoad;
    private OnChangedListener onChangedListener;

    ImportSettingsAdapter(@NonNull Context context) {
        super();

        int width = context.getResources().getDimensionPixelSize(R.dimen.small_button_width);
        int height = context.getResources().getDimensionPixelSize(R.dimen.small_button_height);

        numSvgsToLoad = 0;

        SvgView expanderLessSvgView = new SvgView(context);
        expanderLessSvgView.setListener(this);
        expanderLessSvgView.setSize(width, height);
        expanderLessSvgView.setSvgResourceId(R.raw.ic_expand_less);

        numSvgsToLoad++;

        SvgView expanderMoreSvgView = new SvgView(context);
        expanderMoreSvgView.setListener(this);
        expanderMoreSvgView.setSize(width, height);
        expanderMoreSvgView.setSvgResourceId(R.raw.ic_expand_more);

        numSvgsToLoad++;

        setViewHolderFactory(new MyViewHolderFactory(expanderLessSvgView, expanderMoreSvgView));
        setHasStableIds(false);
        setChoiceMode(ChoiceMode.CHOICE_MODE_NONE);
    }

    void setOnChangedListener(OnChangedListener onChangedListener) {
        this.onChangedListener = onChangedListener;
    }

    void setData(@NonNull ImportSettingsAdapterData data) {
        List<ImportSettingsAdapterData.Item> prevItems = getItems();
        List<ImportSettingsAdapterData.Item> newItems = data.getItems();

        Callback callback = new Callback(prevItems, newItems);

        setItems(newItems, callback);
    }

    @Override
    public void onClick(View v) {
        ViewHolder vh = (ViewHolder) recyclerView.getChildViewHolder(v);

        ImportSettingsAdapterData.Item item = getItemForAdapterPosition(vh.getAdapterPosition());

        if (item.itemType() == ImportSettingsAdapterData.Item.ItemType.GROUP && item.canExpand()) {
            this.onExpansionControlClicked(vh);
        } else {
            super.onClick(v);
        }
    }

    @Override
    public void onSvgRendered(SvgView v) {
        numSvgsToLoad--;

        if (numSvgsToLoad == 0) {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onSvgRenderingFailed(Throwable e) {

    }

    class GroupViewHolder extends ExpandableRecyclerViewAdapter.ViewHolder<ImportSettingsAdapterData.Item> implements OnClickListener {
        private final SvgView expanderLessSvgView;
        private final SvgView expanderMoreSvgView;

        @BindView(R.id.name) TextView name;
        @BindView(R.id.expander) ImageView expander;

        GroupViewHolder(View v, SvgView expanderLessSvgView, SvgView expanderMoreSvgView) {
            super(v);

            this.expanderLessSvgView = expanderLessSvgView;
            this.expanderMoreSvgView = expanderMoreSvgView;

            ButterKnife.bind(this, v);
        }

        @Override
        public void setExpansionControlClickListener(@NonNull ExpansionControlClickListener listener) {
            super.setExpansionControlClickListener(listener);

            expander.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (expansionControlClickListener != null) {
                expansionControlClickListener.onExpansionControlClicked(this);
            }
        }

        @Override
        public boolean hasExpansionControl() {
            return true;
        }

        @Override
        public void bind(ImportSettingsAdapterData.Item item) {
            super.bind(item);

            ImportSettingsAdapterData.Group group = (ImportSettingsAdapterData.Group) item;

            name.setText(group.nameResId());

            itemView.setEnabled(group.isEnabled());

            name.setEnabled(group.isEnabled());
            expander.setEnabled(group.isEnabled());
            expander.setVisibility(group.isEnabled() ? View.VISIBLE : View.GONE);

            if (item.isExpanded() && expanderLessSvgView.isRendered()) {
                expander.setImageBitmap(expanderLessSvgView.getBitmap());
            } else if (!item.isExpanded() && expanderMoreSvgView.isRendered()) {
                expander.setImageBitmap(expanderMoreSvgView.getBitmap());
            }
        }
    }

    class GroupEntryCheckBoxViewHolder extends ExpandableRecyclerViewAdapter.ViewHolder<ImportSettingsAdapterData.Item> {
        @BindView(R.id.checkBox) CheckBox checkBox;
        @BindView(R.id.label) TextView label;
        @BindView(R.id.subLabel) TextView subLabel;

        GroupEntryCheckBoxViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);
        }

        @Override
        public void bind(ImportSettingsAdapterData.Item item) {
            super.bind(item);

            ImportSettingsAdapterData.GroupEntryCheckbox entry = (ImportSettingsAdapterData.GroupEntryCheckbox) item;

            checkBox.setChecked(entry.isChecked());

            label.setText(entry.getLabel(itemView.getContext()));

            String subLabel = entry.getSubLabel(itemView.getContext());

            this.subLabel.setText(subLabel);
            this.subLabel.setVisibility(subLabel.isEmpty() ? View.GONE : View.VISIBLE);

            checkBox.setEnabled(entry.isEnabled);
            checkBox.setChecked(entry.isChecked());
        }

        @Override
        public void onClick() {
            Timber.d("GroupEntryCheckBoxViewHolder.onClick()");

            ImportSettingsAdapterData.GroupEntryCheckbox entry = (ImportSettingsAdapterData.GroupEntryCheckbox) getItem();

            if (entry.isEnabled) {
                entry.toggle();

                checkBox.setChecked(entry.isChecked());

                if (entry.shouldReportChange() && onChangedListener != null) {
                    onChangedListener.onChanged(entry);
                }
            }
        }
    }

    class GroupEntrySeekbarViewHolder extends ExpandableRecyclerViewAdapter.ViewHolder<ImportSettingsAdapterData.Item> {
        @BindView(R.id.label) TextView label;
        @BindView(R.id.seekBar) SeekBar seekBar;
        @BindView(R.id.seekbarValue) TextView seekbarValue;

        GroupEntrySeekbarViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);
        }

        @Override
        public void bind(ImportSettingsAdapterData.Item item) {
            super.bind(item);

            ImportSettingsAdapterData.GroupEntrySeekbar entry = (ImportSettingsAdapterData.GroupEntrySeekbar) item;
            /*
             * First unset the onSeekBarChangeListener because otherwise setMax() will result in it
             * being called but with the wrong tag.
             */
            seekBar.setOnSeekBarChangeListener(null);
            label.setText(entry.labelResId());
            seekBar.setMax(entry.maxProgress());
            seekBar.setProgress(entry.position());
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        entry.setPosition(progress);
                        seekbarValue.setText(seekbarValue.getContext().getString(entry.valueResId(), entry.getValue()));

                        if (entry.shouldReportChange() && onChangedListener != null) {
                            onChangedListener.onChanged(entry);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            seekBar.setEnabled(entry.isEnabled());

            seekbarValue.setText(seekbarValue.getContext().getString(entry.valueResId(), entry.getValue()));
        }
    }

    class GroupEntryRadioGroupViewHolder extends ExpandableRecyclerViewAdapter.ViewHolder<ImportSettingsAdapterData.Item> {
        @BindView(R.id.radioGroup) RadioGroup radioGroup;
        @BindView(R.id.label) TextView label;
        @BindView(R.id.radio1) RadioButton radioButton1;
        @BindView(R.id.radio2) RadioButton radioButton2;

        GroupEntryRadioGroupViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);
        }

        @Override
        public void bind(ImportSettingsAdapterData.Item item) {
            super.bind(item);

            ImportSettingsAdapterData.GroupEntryRadiogroup entry = (ImportSettingsAdapterData.GroupEntryRadiogroup) item;

            if (entry.selectedButton() == 1) {
                radioGroup.check(R.id.radio1);
            } else if (entry.selectedButton() == 2) {
                radioGroup.check(R.id.radio2);
            } else {
                radioGroup.clearCheck();
            }

            label.setText(entry.labelResId());

            radioButton1.setText(entry.button1LabelResId());
            radioButton1.setEnabled(entry.isEnabled());

            radioButton2.setText(entry.button2LlabelResId());
            radioButton2.setEnabled(entry.isEnabled());
        }

        @Override
        public void onClick() {
            Timber.d("GroupEntryRadioGroupViewHolder.onClick()");
            ImportSettingsAdapterData.GroupEntryRadiogroup entry = (ImportSettingsAdapterData.GroupEntryRadiogroup) getItem();

            if (!entry.isEnabled) {
                return;
            }

            entry.toggleSelection();

            if (entry.selectedButton() == 1) {
                radioGroup.check(R.id.radio1);
            } else if (entry.selectedButton() == 2) {
                radioGroup.check(R.id.radio2);
            } else {
                radioGroup.clearCheck();
            }

            if (entry.shouldReportChange() && onChangedListener != null) {
                onChangedListener.onChanged(entry);
            }
        }
    }

    class GroupEntrySpinnerViewHolder
            extends ExpandableRecyclerViewAdapter.ViewHolder<ImportSettingsAdapterData.Item>
            implements OnClickListener, MyPopupMenu.OnMenuItemClickListener {

        @BindView(R.id.text_input_layout) TextInputLayout label;
        @BindView(R.id.edittext) TextInputEditText editText;
        //TODO: figure out why bind is called 2x for every visible item on orientation change

        private MyPopupMenu popupMenu;

        GroupEntrySpinnerViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

            editText.setOnClickListener(this);
            editText.setKeyListener(null);
            editText.setFocusable(false);

            popupMenu = new MyPopupMenu(editText.getContext(), label, Gravity.END);
            popupMenu.setOnMenuItemClickListener(this);
        }

        @Override
        public void bind(ImportSettingsAdapterData.Item item) {
            super.bind(item);

            ImportSettingsAdapterData.GroupEntrySpinner entry = (ImportSettingsAdapterData.GroupEntrySpinner) item;

            popupMenu.inflate(entry.getMenuResId());

            Menu menu = popupMenu.getMenu();

            entry.getMenu().updateAndroidMenu(menu, label.getContext());

            label.setHint(entry.getLabel(label.getContext()));

            MenuItem selectedItem = menu.findItem(entry.getMenu().findItem(entry.selectedMenuItemId()).getId());

            editText.setText(selectedItem.getTitle());
            editText.setEnabled(entry.isEnabled);
        }

        @Override
        public void unbind() {
            super.unbind();

            popupMenu.getMenu().clear();
        }

        @Override
        public void onClick(View view) {
            Timber.d("GroupEntrySpinnerViewHolder.onClick()");

            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            Timber.d("GroupEntrySpinnerViewHolder.onMenuItemClick()");
            ImportSettingsAdapterData.GroupEntrySpinner entry = (ImportSettingsAdapterData.GroupEntrySpinner) getItem();

            MyMenuItem selectedMenuItem = entry.getMenu().findItem(menuItem.getItemId());

            if (entry.selectedMenuItemId() != selectedMenuItem.getId()) {
                entry.setSelectedMenuItemId(selectedMenuItem.getId());
                editText.setText(menuItem.getTitle());

                if (entry.shouldReportChange() && onChangedListener != null) {
                    onChangedListener.onChanged(entry);
                }
            }

            return true;
        }
    }

    //TODO: I now treat everything selected the same as nothing selected. Maybe disable SEARCH when nothing is selected
    class GroupEntryTrackActivityTypesViewHolder
            extends ExpandableRecyclerViewAdapter.ViewHolder<ImportSettingsAdapterData.Item>
            implements OnClickListener, View.OnLongClickListener{

        @BindView(R.id.label) TextView label;
        @BindView(R.id.gridLayout) GridLayout gridLayout;

        private ImageView[] imageViews;
        private final ColorMatrixColorFilter colorMatrixColorFilter;

        GroupEntryTrackActivityTypesViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

            Context context = v.getContext();

            imageViews = new ImageView[TrackActivityType.values().length];
            int width = context.getResources().getDimensionPixelSize(R.dimen.track_activity_type_image_width);
            int height = context.getResources().getDimensionPixelSize(R.dimen.track_activity_type_image_height);
            int padding = context.getResources().getDimensionPixelOffset(R.dimen.track_activity_type_image_padding);

            for (TrackActivityType trackActivityType : TrackActivityType.values()) {
                ImageView imageView = new ImageView(context);

                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                layoutParams.bottomMargin = GridLayout.UNDEFINED;
                layoutParams.topMargin = GridLayout.UNDEFINED;
                layoutParams.leftMargin = GridLayout.UNDEFINED;
                layoutParams.rightMargin = GridLayout.UNDEFINED;

                imageView.setLayoutParams(layoutParams);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setImageResource(trackActivityType.getDrawableResId());
                imageView.setBackgroundResource(R.drawable.btn_small);
                imageView.setPadding(padding, padding, padding, padding);
                imageView.setContentDescription(context.getString(trackActivityType.getNameResId()));
                imageView.setOnClickListener(this);
                imageView.setOnLongClickListener(this);
                imageView.setTag(trackActivityType);

                gridLayout.addView(imageView);
                gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
                gridLayout.setUseDefaultMargins(true);

                imageViews[trackActivityType.code()] = imageView;
            }

            float[] matrix = {
                    0.3f, 0.49f, 0.3f, 0, 0,
                    0.3f, 0.49f, 0.3f, 0, 0,
                    0.3f, 0.49f, 0.3f, 0, 0,
                    0, 0, 0, 0.4f, 0
            };

            colorMatrixColorFilter = new ColorMatrixColorFilter(matrix);
        }

        @Override
        public void bind(ImportSettingsAdapterData.Item item) {
            super.bind(item);

            ImportSettingsAdapterData.GroupEntryTrackActivityTypes entry = (ImportSettingsAdapterData.GroupEntryTrackActivityTypes) item;

            label.setText(entry.labelResId);

            for (TrackActivityType trackActivityType : TrackActivityType.values()) {
                bind(imageViews[trackActivityType.code()], trackActivityType, entry);
            }

            itemView.setOnLongClickListener(entry.isEnabled() ? this : null);
        }

        private void bind(ImageView imageView, TrackActivityType trackActivityType, ImportSettingsAdapterData.GroupEntryTrackActivityTypes entry) {
            setColorFilter(imageView, !entry.isIncluded(trackActivityType) || !entry.isEnabled());
            imageView.setEnabled(entry.isEnabled);
        }

        private void setColorFilter(ImageView v, boolean set) {
            if (set) {
                v.setColorFilter(colorMatrixColorFilter);
            } else {
                v.setColorFilter(null);
            }
        }

        @Override
        public void onClick(View view) {
            if (!(view.getTag() instanceof TrackActivityType)) {
                return;
            }

            TrackActivityType trackActivityType = (TrackActivityType) view.getTag();

            ImportSettingsAdapterData.Item item = getItemForAdapterPosition(getAdapterPosition());
            ImportSettingsAdapterData.GroupEntryTrackActivityTypes entry = (ImportSettingsAdapterData.GroupEntryTrackActivityTypes) item;


            invertInclusion(entry, trackActivityType, imageViews[trackActivityType.code()]);

            if (entry.shouldReportChange() && onChangedListener != null) {
                onChangedListener.onChanged(entry);
            }
        }

        private void invertInclusion(ImportSettingsAdapterData.GroupEntryTrackActivityTypes entry, TrackActivityType trackActivityType, ImageView imageView) {
            boolean included = !entry.isIncluded(trackActivityType);
            entry.setIncluded(trackActivityType, included);
            setColorFilter(imageView, !included);
        }

        @Override
        public boolean onLongClick(View view) {
            ImportSettingsAdapterData.Item item = getItemForAdapterPosition(getAdapterPosition());

            ImportSettingsAdapterData.GroupEntryTrackActivityTypes entry = (ImportSettingsAdapterData.GroupEntryTrackActivityTypes) item;

            for (TrackActivityType trackActivityType : TrackActivityType.values()) {
                invertInclusion(entry, trackActivityType, imageViews[trackActivityType.code()]);
            }

            if (entry.shouldReportChange() && onChangedListener != null) {
                onChangedListener.onChanged(entry);
            }

            return true;
        }
    }

    class GroupEntryTrackTypeViewHolder extends ExpandableRecyclerViewAdapter.ViewHolder<ImportSettingsAdapterData.Item> implements CompoundButton.OnCheckedChangeListener {
        @BindView(R.id.label) TextView label;
        @BindView(R.id.roundTrip) CheckBox roundTrip;
        @BindView(R.id.oneWay) CheckBox oneWay;

        GroupEntryTrackTypeViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);

            roundTrip.setOnCheckedChangeListener(this);
            oneWay.setOnCheckedChangeListener(this);
        }

        @Override
        public void bind(ImportSettingsAdapterData.Item item) {
            super.bind(item);

            ImportSettingsAdapterData.GroupEntryTrackType entry = (ImportSettingsAdapterData.GroupEntryTrackType) item;

            label.setText(entry.labelResId);
            roundTrip.setChecked(entry.roundTripChecked);
            roundTrip.setEnabled(entry.isEnabled);
            oneWay.setChecked(entry.oneWayChecked);
            oneWay.setEnabled(entry.isEnabled);

            setClickable(entry);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ImportSettingsAdapterData.Item item = getItemForAdapterPosition(getAdapterPosition());

            ImportSettingsAdapterData.GroupEntryTrackType entry = (ImportSettingsAdapterData.GroupEntryTrackType) item;

            if (buttonView == roundTrip) {
                entry.roundTripChecked = isChecked;
            } else if (buttonView == oneWay) {
                entry.oneWayChecked = isChecked;
            }

            setClickable(entry);

            if (entry.shouldReportChange() && onChangedListener != null) {
                onChangedListener.onChanged(entry);
            }
        }

        private void setClickable(ImportSettingsAdapterData.GroupEntryTrackType entry) {
            if (entry.roundTripChecked) {
                roundTrip.setClickable(entry.oneWayChecked);
            }

            if (entry.oneWayChecked) {
                oneWay.setClickable(entry.roundTripChecked);
            }
        }
    }

    class GroupEntryTrackLengthViewHolder extends ExpandableRecyclerViewAdapter.ViewHolder<ImportSettingsAdapterData.Item> implements View.OnTouchListener {
        @BindView(R.id.label) TextView label;
        @BindView(R.id.minLabel) TextView minLabel;
        @BindView(R.id.maxLabel) TextView maxLabel;
        @BindView(R.id.minSeekBar) SeekBar minSeekBar;
        @BindView(R.id.maxSeekBar) SeekBar maxSeekBar;
        @BindView(R.id.seekBarValueAligner) TextView seekBarValueAligner;
        @BindView(R.id.minSeekBarValue) TextView minSeekBarValue;
        @BindView(R.id.maxSeekBarValue) TextView maxSeekBarValue;

        GroupEntryTrackLengthViewHolder(View v) {
            super(v);

            ButterKnife.bind(this, v);
        }

        @Override
        public void bind(ImportSettingsAdapterData.Item item) {
            super.bind(item);

            ImportSettingsAdapterData.GroupEntryTrackLength entry = (ImportSettingsAdapterData.GroupEntryTrackLength) item;

            /*
             * First unset the onSeekBarChangeListener because otherwise setMax() will result in it
             * being called but with the wrong tag.
             */
            minSeekBar.setOnSeekBarChangeListener(null);
            minSeekBar.setOnSeekBarChangeListener(null);
            label.setText(entry.labelResId());
            minLabel.setText(entry.minLabelResId);
            maxLabel.setText(entry.maxLabelResId);

            minSeekBar.setMax(entry.maxProgress());
            minSeekBar.setProgress(entry.getMinPosition());
            minSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    entry.setMinPosition(progress);
                    minSeekBarValue.setText(minSeekBarValue.getContext().getString(entry.valueResId(), entry.getMinValue()));

                    if (entry.shouldReportChange() && onChangedListener != null) {
                        onChangedListener.onChanged(entry);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            minSeekBar.setEnabled(entry.isEnabled());
            minSeekBar.setOnTouchListener(this);
            minSeekBarValue.setText(minSeekBarValue.getContext().getString(entry.valueResId(), entry.getMinValue()));

            maxSeekBar.setMax(entry.maxProgress());
            maxSeekBar.setProgress(entry.getMaxPosition());
            maxSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    entry.setMaxPosition(progress);

                    String max =  maxSeekBar.getContext().getString(entry.valueResId(), entry.getMaxValue());

                    seekBarValueAligner.setText(max);
                    maxSeekBarValue.setText(max);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            maxSeekBar.setEnabled(entry.isEnabled());
            maxSeekBar.setOnTouchListener(this);

            String max =  maxSeekBar.getContext().getString(entry.valueResId(), entry.getMaxValue());

            seekBarValueAligner.setText(max);
            maxSeekBarValue.setText(max);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!(v instanceof SeekBar)) {
                return false;
            }

            v.onTouchEvent(event);

            if (v == minSeekBar) {
                if (minSeekBar.getProgress() > maxSeekBar.getProgress()) {
                    maxSeekBar.setProgress(minSeekBar.getProgress());
                }
            } else {
                if (maxSeekBar.getProgress() < minSeekBar.getProgress()) {
                    minSeekBar.setProgress(maxSeekBar.getProgress());
                }
            }

            return true;
        }
    }

    class MyViewHolderFactory extends ExpandableRecyclerViewAdapter.ViewHolderFactory<ImportSettingsAdapterData.Item> {
        @NonNull final SvgView expanderLessSvgView;
        @NonNull final SvgView expanderMoreSvgView;

        MyViewHolderFactory(@NonNull SvgView expanderLessSvgView, @NonNull SvgView expanderMoreSvgView) {
            this.expanderLessSvgView = expanderLessSvgView;
            this.expanderMoreSvgView = expanderMoreSvgView;
        }

        @Override
        public ViewHolder create(int viewType, ViewGroup parent) {
            View v;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            switch (viewType) {
                case ImportSettingsAdapterData.Item.VIEW_TYPE_GROUP:
                    v = inflater.inflate(R.layout.import_settings_dialog_group, parent, false);
                    return new GroupViewHolder(v, expanderLessSvgView, expanderMoreSvgView);
                case ImportSettingsAdapterData.Item.VIEW_TYPE_CHECKBOX:
                    v = inflater.inflate(R.layout.import_settings_dialog_child_checkbox, parent, false);
                    return new GroupEntryCheckBoxViewHolder(v);
                case ImportSettingsAdapterData.Item.VIEW_TYPE_SEEKBAR:
                    v = inflater.inflate(R.layout.import_settings_dialog_child_seekbar, parent, false);
                    return new GroupEntrySeekbarViewHolder(v);
                case ImportSettingsAdapterData.Item.VIEW_TYPE_RADIOGROUP:
                    v = inflater.inflate(R.layout.import_settings_dialog_child_radiogroup, parent, false);
                    return new GroupEntryRadioGroupViewHolder(v);
                case ImportSettingsAdapterData.Item.VIEW_TYPE_SPINNER:
                    v = inflater.inflate(R.layout.import_settings_dialog_child_edittext_menu, parent, false);
                    return new GroupEntrySpinnerViewHolder(v);
                case ImportSettingsAdapterData.Item.VIEW_TYPE_TRACK_ACTIVITY_TYPES:
                    v = inflater.inflate(R.layout.import_settings_dialog_child_track_activity_type, parent, false);
                    return new GroupEntryTrackActivityTypesViewHolder(v);
                case ImportSettingsAdapterData.Item.VIEW_TYPE_TRACK_TYPE:
                    v = inflater.inflate(R.layout.import_settings_dialog_child_track_type, parent, false);
                    return new GroupEntryTrackTypeViewHolder(v);
                case ImportSettingsAdapterData.Item.VIEW_TYPE_TRACK_LENGTH:
                    v = inflater.inflate(R.layout.import_settings_dialog_child_track_length, parent, false);
                    return new GroupEntryTrackLengthViewHolder(v);
                default:
                    throw new RuntimeException("Don't know how to handle viewType:" + viewType);
            }
        }
    }

    private class Callback extends DiffUtil.Callback {
        List<ImportSettingsAdapterData.Item> oldItems;
        List<ImportSettingsAdapterData.Item> newItems;

        public Callback(@NonNull List<ImportSettingsAdapterData.Item> oldItems, @NonNull List<ImportSettingsAdapterData.Item> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            Timber.d("getOldListSize: %d", getItemCount(oldItems));
            return getItemCount(oldItems);
        }

        @Override
        public int getNewListSize() {
            Timber.d("getNewListSize: %d", getItemCount(newItems));
            return getItemCount(newItems);
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            ImportSettingsAdapterData.Item oldItem = getItemForAdapterPosition(oldItemPosition, oldItems);
            ImportSettingsAdapterData.Item newItem = getItemForAdapterPosition(newItemPosition, newItems);

            if (newItem.itemType() == oldItem.itemType()) {
                if (oldItem.itemType() == ImportSettingsAdapterData.Item.ItemType.GROUP) {
                    ImportSettingsAdapterData.Group oldGroup = (ImportSettingsAdapterData.Group) oldItem;
                    ImportSettingsAdapterData.Group newGroup = (ImportSettingsAdapterData.Group) newItem;

                    if (oldGroup.getGroupType() == newGroup.getGroupType()) {
                        Timber.e("areItemsTheSame(%d, %d) returning: true", oldItemPosition, newItemPosition);
                        return true;
                    }
                } else {
                    ImportSettingsAdapterData.GroupEntry oldEntry = (ImportSettingsAdapterData.GroupEntry) oldItem;
                    ImportSettingsAdapterData.GroupEntry newEntry = (ImportSettingsAdapterData.GroupEntry) newItem;

                    if (oldEntry.getGroupEntryType() == newEntry.getGroupEntryType()) {
                        Timber.e("areItemsTheSame(%d, %d) returning: true", oldItemPosition, newItemPosition);
                        return true;
                    }
                }
            }

            Timber.e( "areItemsTheSame(%d, %d) returning: false",oldItemPosition, newItemPosition);
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            ImportSettingsAdapterData.Item oldItem = getItemForAdapterPosition(oldItemPosition, oldItems);
            ImportSettingsAdapterData.Item newItem = getItemForAdapterPosition(newItemPosition, newItems);

            Timber.e("areContentsTheSame(%d, %d) returning: %s", oldItemPosition, newItemPosition, oldItem.equals(newItem));
            return oldItem.equals(newItem);
        }
    }

    /*
     * PopupMenu leaks activity when popupMenu is showing and an orientation change happens
     * https://issuetracker.google.com/issues/64796458 reported by me on: Aug 17, 2017
     */
    void closePopups() {
        for (ViewHolder viewHolder : boundViewHolders) {
            if (viewHolder.getItemViewType() == ImportSettingsAdapterData.Item.VIEW_TYPE_SPINNER) {
                GroupEntrySpinnerViewHolder groupEntrySpinnerViewHolder = (GroupEntrySpinnerViewHolder) viewHolder;

                if (groupEntrySpinnerViewHolder.popupMenu.isShowing()) {
                    Timber.d("closePopups() - Closing popup for: %s", ((ImportSettingsAdapterData.GroupEntrySpinner)groupEntrySpinnerViewHolder.getItem()).getLabel(getRecyclerView().getContext()));

                    groupEntrySpinnerViewHolder.popupMenu.setOnDismissListener(null);
                    groupEntrySpinnerViewHolder.popupMenu.setOnMenuItemClickListener(null);
                    groupEntrySpinnerViewHolder.popupMenu.dismissImmediate();
                    groupEntrySpinnerViewHolder.popupMenu = null;
                }
            }
        }
    }
}