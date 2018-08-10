package nl.erikduisters.pathfinder.util.recyclerView;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import nl.erikduisters.pathfinder.R;
import timber.log.Timber;

/**
 * Created by Erik Duisters on 01-05-2017.
 */
public abstract class ExpandableRecyclerViewAdapter<T extends ExpandableRecyclerViewAdapter.Item<T>>
        extends RecyclerView.Adapter<ExpandableRecyclerViewAdapter.ViewHolder<T>>
        implements View.OnClickListener, View.OnTouchListener,
        View.OnLongClickListener, ExpansionControlClickListener<T> {

    private static final String LABEL_STATE = "ExpandableRecyclerViewAdapterState";
    private static final String LABEL_CHOICE_MODE = "ChoiceMode";
    private static final String LABEL_FIRST_VISIBLE_POSITION = "FirstVisiblePosition";
    private static final String LABEL_FIRST_VISIBLE_TOP = "FirstVisibleTop";
    private static final String LABEL_ITEM_STATE = "ItemState";

    public interface OnItemClickListener<T extends Item<T>> {
        void onItemClick(T item);
    }

    public interface OnItemLongClickListener<T extends Item<T>> {
        boolean onItemLongClick(T item);
    }

    public interface OnItemExpandListener<T extends Item<T>> {
        void onItemExpand(T item);
    }

    public interface OnItemCollapseListener<T extends Item<T>> {
        void onItemCollapse(T item);
    }

    @IntDef({ChoiceMode.CHOICE_MODE_NONE, ChoiceMode.CHOICE_MODE_SINGLE, ChoiceMode.CHOICE_MODE_MULTI})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChoiceMode {
        public static final int CHOICE_MODE_NONE = 0;
        public static final int CHOICE_MODE_SINGLE = 1;
        public static final int CHOICE_MODE_MULTI = 2;
    }

    public static
    @ChoiceMode
    int choiceModeFromInt(int mode) {
        if (mode >= ChoiceMode.CHOICE_MODE_NONE && mode <= ChoiceMode.CHOICE_MODE_MULTI) {
            return mode;
        } else {
            throw new IllegalArgumentException("mode is not a valid ChoiceMode");
        }
    }

    protected RecyclerView recyclerView;
    @ChoiceMode
    private int choiceMode = ChoiceMode.CHOICE_MODE_NONE;
    private ViewHolderFactory<T> viewHolderFactory;
    @NonNull private List<T> items;
    private int prevSelectedItemPosition;
    protected int numSelectedItems;

    private OnItemClickListener<T> itemClickListener;
    private OnItemLongClickListener<T> itemLongClickListener;
    private OnItemCollapseListener<T> itemCollapseListener;
    private OnItemExpandListener<T> itemExpandListener;

    @NonNull protected HashSet<ViewHolder<T>> boundViewHolders;

    protected ExpandableRecyclerViewAdapter() {
        items = new ArrayList<>();
        prevSelectedItemPosition = -1;
        numSelectedItems = 0;
        boundViewHolders = new HashSet<>();
    }

    public abstract static class Item<T extends Item<T>> {
        public static final String LABEL_SELECTED = "Selected";
        public static final String LABEL_IS_EXPANDED = "IsExpanded";
        static final String LABEL_CHILD_STATE = "ChildState";

        boolean selected;
        boolean canExpand;
        boolean isExpanded;
        @NonNull ArrayList<T> children;

        public Item() {
            this(false, false, false);
        }

        public Item(boolean selected, boolean canExpand, boolean isExpanded) {
            this.selected = selected;
            this.canExpand = canExpand;
            this.isExpanded = isExpanded;
            this.children = new ArrayList<>();
        }

        public Item(Item<T> item) {
            this.selected = item.selected;
            this.canExpand = item.canExpand;
            this.isExpanded = item.isExpanded;
            this.children = new ArrayList<>(item.children);
        }

        public abstract int getViewType();

        public void setIsSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setCanExpand(boolean canExpand) {
            this.canExpand = canExpand;
        }

        public boolean canExpand() {
            return canExpand && !children.isEmpty();
        }

        public void setIsExpanded(boolean isExpanded) {
            this.isExpanded = isExpanded;
        }

        public boolean isExpanded() {
            return isExpanded;
        }

        public T getChild(int i) {
            if (i < children.size()) {
                return children.get(i);
            }

            throw new RuntimeException("Index out of bounds exception");
        }

        public List<T> getChildren() {
            return children;
        }

        public void addChild(T child) {
            if (child.numChildren() > 0) {
                throw new RuntimeException("ChildItems cannot have children themselves");
            }

            children.add(child);
        }

        public void addChild(int position, T child) {
            children.add(position, child);
        }

        public void addChildren(int position, List<T> children) {
            this.children.addAll(position, children);
        }

        public int numChildren() {
            return children.size();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item<?> item = (Item<?>) o;

            if (selected != item.selected) return false;
            if (canExpand != item.canExpand) return false;
            if (isExpanded != item.isExpanded) return false;

            return children.equals(item.children);
        }

        @Override
        public int hashCode() {
            int result = (selected ? 1 : 0);

            result = 31 * result + (canExpand ? 1 : 0);
            result = 31 * result + (isExpanded ? 1 : 0);
            result = 31 * result + children.hashCode();

            return result;
        }

        @CallSuper
        @Nullable
        protected Parcelable onSaveInstanceState() {
            //Subclasses need to call super.onSaveInstanceState and use the result as argument to their new SavedState() call
            SavedState state = new SavedState();

            state.selected = selected;
            state.isExpanded = isExpanded;

            int numChildren = children.size();

            state.childState = new Parcelable[numChildren];

            for (int i = 0; i < numChildren; i++) {
                state.childState[i] = children.get(i).onSaveInstanceState();
            }

            return state;
        }

        @CallSuper
        public void onRestoreInstanceState(Parcelable state) {
            this.onRestoreInstanceState(state, true);
        }

        public void onRestoreInstanceState(Parcelable state, boolean restoreChildren) {
            if (state instanceof SavedState) {
                SavedState savedState = (SavedState) state;

                selected = savedState.selected;
                isExpanded = savedState.isExpanded;

                if (!restoreChildren) {
                    return;
                }

                int numChildren = savedState.childState.length;

                for (int i = 0; i < numChildren; i++) {
                    children.get(i).onRestoreInstanceState(savedState.childState[i], false);
                }
            }
        }

        @Nullable
        public Parcelable[] getChildState(Parcelable state) {
            if (state instanceof SavedState) {
                SavedState savedState = (SavedState) state;

                return savedState.childState;
            }

            return null;
        }

        public static class SavedState implements Parcelable {
            //public static final SavedState EMPTY_STATE = new SavedState();

            public boolean selected;
            protected boolean isExpanded;
            protected Parcelable[] childState;

            private final Parcelable superState;

            private SavedState() {
                superState = null;
            }

            protected SavedState(@NonNull Parcelable superState) {
                /*
                if (superState == null) {
                    throw new IllegalArgumentException("superState must not be null");
                }

                this.superState = superState != EMPTY_STATE ? superState : null;
                */
                this.superState = superState;
            }

            protected SavedState(Parcel source) {
                this.superState = source.readParcelable(getClass().getClassLoader());
                //this.superState = superState != null ? superState : EMPTY_STATE;

                selected = source.readByte() != 0;
                isExpanded = source.readByte() != 0;

                childState = source.readParcelableArray(getClass().getClassLoader());
            }

            @NonNull
            final public Parcelable getSuperState() {
                if (superState == null) {
                    throw new IllegalStateException("There is no superState");
                }

                return superState;
            }

            @CallSuper
            @Override
            public void writeToParcel(Parcel out, int flags) {
                out.writeParcelable(superState, flags);

                out.writeByte((byte) (selected ? 1 : 0));
                out.writeByte((byte) (isExpanded ? 1 : 0));

                out.writeParcelableArray(childState, flags);
            }

            @Override
            public int describeContents() {
                return 0;
            }

            public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
                @Override
                public SavedState createFromParcel(Parcel source) {
                    SavedState savedState = new SavedState(source);

                    if (savedState.superState != null) {
                        throw new IllegalStateException("superState must be null");
                    }

                    return savedState;
                }

                @Override
                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
        }
    }

    public abstract static class ViewHolder<T extends Item<T>> extends RecyclerView.ViewHolder {
        protected ExpansionControlClickListener<T> expansionControlClickListener;
        protected T item;

        public ViewHolder(View v) {
            super(v);
        }

        @CallSuper
        public void bind(T item) {
            this.item = item;
        }
        @CallSuper
        public void unbind() { this.item = null; }

        public boolean hasExpansionControl() {
            return false;
        }

        public void setExpansionControlClickListener(@NonNull ExpansionControlClickListener<T> listener) {
            expansionControlClickListener = listener;
        }

        public void setSelected(boolean selected) {
            itemView.setSelected(selected);
        }

        public T getItem() {
            return item;
        }

        public void onClick() {}
    }

    protected abstract static class ViewHolderFactory<T extends Item<T>> {
        abstract public ViewHolder<T> create(int ViewType, ViewGroup parent);
    }

    public void setViewHolderFactory(@NonNull ViewHolderFactory<T> factory) {
        viewHolderFactory = factory;
    }

    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        itemLongClickListener = listener;
    }

    public void setOnItemExpandListener(OnItemExpandListener<T> listener) {
        itemExpandListener = listener;
    }

    public void setOnItemCollapseListener(OnItemCollapseListener<T> listener) {
        itemCollapseListener = listener;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public int getNumSelectedItems() {
        return numSelectedItems;
    }

    @Override
    public ViewHolder<T> onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Timber.d("Creating ViewHolder for viewType: " + viewType);

        if (viewHolderFactory == null) {
            throw new RuntimeException("You must set a ViewHolderFactory.");
        }

        ViewHolder<T> viewHolder = viewHolderFactory.create(viewType, viewGroup);

        if (recyclerView.isClickable()) {
            viewHolder.itemView.setOnClickListener(this);
        } else {
            viewHolder.itemView.setClickable(false);
        }

        if (recyclerView.isLongClickable()) {
            viewHolder.itemView.setOnLongClickListener(this);
        } else {
            viewHolder.itemView.setLongClickable(false);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder<T> viewHolder, int i) {
        T item = getItemForAdapterPosition(i);

        viewHolder.bind(item);

        viewHolder.setSelected(item.isSelected());

        if (Build.VERSION.SDK_INT < 21) {
            viewHolder.itemView.setOnTouchListener(this);
        }

        if (item.canExpand() && viewHolder.hasExpansionControl()) {
            viewHolder.setExpansionControlClickListener(this);
        }

        boundViewHolders.add(viewHolder);
    }

    @Override
    public void onViewRecycled(ViewHolder<T> holder) {
        Timber.d("onViewRecycled(): pos = " + holder.getAdapterPosition() + " boundViewHolders.size() = " + (boundViewHolders.size() -1 ));
        holder.unbind();
        boundViewHolders.remove(holder);
    }

    @Nullable
    public T getItemForAdapterPosition(int position) {
        return getItemForAdapterPosition(position, items);
    }

    protected T getItemForAdapterPosition(int position, List<T> items) {
        if (position == RecyclerView.NO_POSITION) {
            return null;
        }

        int i = position;
        int curPos = 0;

        for (Item item : items) {
            if (i == 0) {
                return items.get(curPos);
            }

            if (item.canExpand() && item.isExpanded()) {
                int numChildren = item.numChildren();

                if (i <= numChildren) {
                    return items.get(curPos).getChild(i - 1);
                }

                i -= numChildren;
            }

            i--;
            curPos++;
        }

        throw new RuntimeException("Cannot find Item for position: " + position);
    }

    @Override
    public int getItemViewType(int position) {
        Item i = getItemForAdapterPosition(position);

        return i.getViewType();
    }

    @Override
    public int getItemCount() {
        return getItemCount(items);
    }

    protected int getItemCount(List<T> items) {
        int count = items.size();

        for (Item i : items) {
            if (i.canExpand() && i.isExpanded()) {
                count += i.numChildren();
            }
        }

        return count;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        this.recyclerView = null;
    }

    public int getAdapterPosition(Item item) {
        int adapterPos = 0;
        boolean found = false;

        findItem:
        for (Item parent : items) {
            if (parent == item) {
                found = true;
                break;
            }

            if (parent.canExpand() && parent.isExpanded()) {
                int childPos = 1;
                //noinspection unchecked
                for (Item child : (List<Item>) parent.getChildren()) {
                    if (child == item) {
                        adapterPos += childPos;
                        found = true;
                        break findItem;
                    }
                }
                adapterPos += parent.numChildren();
            } else if (parent.canExpand() && parent.getChildren().contains(item)) {
                break;
            }

            adapterPos++;
        }

        if (!found) {
            adapterPos = RecyclerView.NO_POSITION;
        }

        return adapterPos;
    }

    protected void setItems(List<T> items, DiffUtil.Callback callback) {
        this.setItems(items, callback, false);
    }

    protected void setItems(List<T> items, DiffUtil.Callback callback, boolean detectMoves) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback, detectMoves);

        this.items = items;

        result.dispatchUpdatesTo(this);
    }

    @NonNull
    protected List<T> getItems() {
        return items;
    }

    public void add(T item) {
        items.add(item);

        int adapterPos = getAdapterPosition(item);

        if (adapterPos != RecyclerView.NO_POSITION) {
            if (item.canExpand() && item.isExpanded()) {
                notifyItemRangeInserted(adapterPos, item.numChildren() + 1);
            } else {
                notifyItemInserted(adapterPos);
            }
        }
    }

    public void add(int position, T item) {
        items.add(position, item);

        int adapterPos = getAdapterPosition(item);

        if (adapterPos != RecyclerView.NO_POSITION) {
            if (item.canExpand() && item.isExpanded()) {
                notifyItemRangeInserted(adapterPos, item.numChildren() + 1);
            } else {
                notifyItemInserted(adapterPos);
            }
        }
    }

    public T get(int pos) {
        return items.get(pos);
    }

    public void setSelected(T item) {
        int adapterPos = 0;

        for (Item i : items) {
            if (i == item) {
                break;
            }

            if (i.canExpand() && i.isExpanded()) {
                if (i.children.contains(item)) {
                    adapterPos += i.children.indexOf(item) + 1;
                    break;
                } else {
                    adapterPos += i.numChildren();
                }
            }
            adapterPos++;
        }

        setSelected(item, adapterPos);
    }

    protected void setSelected(T item, int adapterPosition) {
        ViewHolder vh;

        switch (choiceMode) {
            case ChoiceMode.CHOICE_MODE_NONE:
                numSelectedItems = 0;
                //Do nothing
                break;
            case ChoiceMode.CHOICE_MODE_SINGLE:
                T prevSelectedItem = (prevSelectedItemPosition == -1) ? null : getItemForAdapterPosition(prevSelectedItemPosition);

                if (prevSelectedItem == null || prevSelectedItem != item) {
                    if (prevSelectedItem != null) {
                        prevSelectedItem.selected = false;

                        vh = (ViewHolder)recyclerView.findViewHolderForAdapterPosition(prevSelectedItemPosition);

                        if (vh != null) {
                            vh.setSelected(false);
                        }
                    }

                    item.selected = true;
                    numSelectedItems = 1;

                    vh = (ViewHolder)recyclerView.findViewHolderForAdapterPosition(adapterPosition);

                    if (vh != null) {
                        vh.setSelected(true);
                    }

                    prevSelectedItemPosition = adapterPosition;
                }
                break;
            case ChoiceMode.CHOICE_MODE_MULTI:
                if (prevSelectedItemPosition != -1) {
                    prevSelectedItemPosition = -1;
                }

                if (!items.contains(item)) {
                    for (Item parent : items) {
                        if (parent.numChildren() > 0 && parent.children.contains(item)) {
                            if (parent.selected) {
                                return;
                            }
                        }
                    }
                }

                item.selected = !item.selected;

                numSelectedItems += item.selected ? 1 : -1;

                vh = (ViewHolder)recyclerView.findViewHolderForAdapterPosition(adapterPosition);

                if (vh != null) {
                    vh.setSelected(item.selected);
                }

                if (item.numChildren() > 0) {
                    //noinspection unchecked
                    for (Item child : item.getChildren()) {
                        if (child.selected != item.selected) {
                            child.selected = item.selected;
                            numSelectedItems += item.selected ? 1 : -1;
                        }
                    }
                }


                if (item.isExpanded() && item.canExpand()) {
                    int i=1;

                    for ( T child : (List<T>) item.getChildren()) {
                        vh = (ViewHolder)recyclerView.findViewHolderForAdapterPosition(adapterPosition + i);
                        if (vh != null) {
                            vh.setSelected(child.selected);
                        }
                        i++;
                    }
                }

                break;
        }
    }

    public void clearSelectedItems() {
        int adapterPosition = 0;

        for (Item item : items) {
            if (item.selected) {
                item.selected = false;
                notifyItemChanged(adapterPosition);
            }

            if (item.canExpand()) {
                int numChildren = item.numChildren();

                for (int i = 0; i < numChildren; i++) {
                    Item child = item.getChild(i);

                    if (child.selected) {
                        child.selected = false;
                    }
                }

                if (item.isExpanded()) {
                    notifyItemRangeChanged(adapterPosition, item.numChildren() + 1);
                    adapterPosition += item.numChildren();
                }
            }

            adapterPosition++;
        }

        numSelectedItems = 0;
    }

    public void setChoiceMode(@ChoiceMode int mode) {
        if (mode == choiceMode) {
            return;
        }

        switch (mode) {
            case ChoiceMode.CHOICE_MODE_NONE:
                clearSelectedItems();
                break;
            case ChoiceMode.CHOICE_MODE_SINGLE:
                switch (choiceMode) {
                    case ChoiceMode.CHOICE_MODE_NONE:
                    case ChoiceMode.CHOICE_MODE_SINGLE:
                        break;
                    case ChoiceMode.CHOICE_MODE_MULTI:
                        if (numSelectedItems > 1) {
                            clearSelectedItems();
                        }
                        break;

                }
                break;
            case ChoiceMode.CHOICE_MODE_MULTI:
                //Do nothing
                break;
        }

        choiceMode = mode;
    }


    @Override
    public void onExpansionControlClicked(ViewHolder<T> viewHolder) {
        int position = viewHolder.getAdapterPosition();

        handleGroupExpansion(viewHolder.item, position);
    }

    @Override
    public void onClick(View v) {
        Timber.d("onClick()");
        ViewHolder vh = (ViewHolder) recyclerView.getChildViewHolder(v);

        vh.onClick();

        int position = vh.getAdapterPosition();

        T item = getItemForAdapterPosition(position);

        //TODO: If I don't have an expansion control the group will not get selected
        if (item.canExpand() && !vh.hasExpansionControl()) {
            handleGroupExpansion(item, position);
        } else {
            if (choiceMode != ChoiceMode.CHOICE_MODE_NONE){
                setSelected(item, position);
            }

            if (itemClickListener != null) {
                itemClickListener.onItemClick(item);
            }
        }
    }

    public void toggleExpansion(T item) {
        int adapterPos = getAdapterPosition(item);

        if (adapterPos != RecyclerView.NO_POSITION) {
            handleGroupExpansion(item, adapterPos);
        }
    }

    private void handleGroupExpansion(T item, int position) {
        item.setIsExpanded(!item.isExpanded());

        if (item.numChildren() != 0) {
            notifyItemChanged(position);
        }

        if (item.isExpanded() && item.numChildren() != 0) {
            notifyItemRangeInserted(position + 1, item.numChildren());
        } else if (!item.isExpanded() && item.numChildren() != 0) {
            notifyItemRangeRemoved(position + 1, item.numChildren());
        }

        if (item.isExpanded() && itemExpandListener != null) {
            itemExpandListener.onItemExpand(item);
        } else if (!item.isExpanded() && itemCollapseListener != null) {
            itemCollapseListener.onItemCollapse(item);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        ViewHolder vh = (ViewHolder) recyclerView.getChildViewHolder(v);

        if (itemLongClickListener != null) {
            return itemLongClickListener.onItemLongClick(getItemForAdapterPosition(vh.getAdapterPosition()));
        }

        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /* This is to animate the background drawable from pressed to long pressed state, borrowed from ListView */
        Drawable d = v.getBackground();

        if (d == null) {
            return false;
        }

        if (!(d instanceof LayerDrawable)) {
            return false;
        }

        d = ((LayerDrawable) d).findDrawableByLayerId(R.id.selectableItemBackground);

        if (d == null) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);

                if (d.getCurrent() instanceof TransitionDrawable) {
                    ((TransitionDrawable) d.getCurrent()).startTransition(ViewConfiguration.getLongPressTimeout());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                v.setPressed(false);
                if (d.getCurrent() instanceof TransitionDrawable) {
                    ((TransitionDrawable) d.getCurrent()).resetTransition();
                }
                break;
        }

        return false;
    }

    public void remove(T item) {
        int adapterPos = getAdapterPosition(item);

        if (adapterPos != RecyclerView.NO_POSITION) {
            removeItem(item, adapterPos);
        }
    }

    private void removeItem(T item, int adapterPosition) {
        Item parent = null;

        if (!items.contains(item)) {
            for (Item i : items) {
                if (i.isExpanded() && i.numChildren() > 0 && i.getChildren().contains(item)) {
                    parent = i;
                    break;
                }
            }
        }

        if (parent == null) {
            items.remove(item);

            if (item.canExpand() && item.isExpanded()) {
                notifyItemRangeRemoved(adapterPosition, item.numChildren() + 1);
            } else {
                notifyItemRemoved(adapterPosition);
            }
        } else {
            parent.getChildren().remove(item);

            notifyItemRemoved(adapterPosition);
        }
    }

    //TODO: If still needed when using ViewModels re-write and use Parcelable
    /*
    public void onSaveInstanceState(Bundle outState) throws JSONException {
        if (items.isEmpty()) {
            return;
        }

        JSONObject state = new JSONObject();
        state.put(LABEL_CHOICE_MODE, choiceMode);

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            int adapterPos = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

            state.put(LABEL_FIRST_VISIBLE_POSITION, adapterPos);

            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(adapterPos);

            if (vh != null) {
                state.put(LABEL_FIRST_VISIBLE_TOP, vh.itemView.getTop() - recyclerView.getPaddingTop());
            }
        }

        JSONArray stateArray = new JSONArray();

        for (Item item : items) {
            JSONObject jo = new JSONObject();

            jo.put(Item.LABEL_SELECTED, item.isSelected());
            jo.put(Item.LABEL_IS_EXPANDED, item.isExpanded());

            if (item.numChildren() > 0) {
                JSONArray children = new JSONArray();
                jo.put(Item.LABEL_CHILD_STATE, children);

                //noinspection unchecked
                for (Item child : (List<Item>) item.children) {
                    children.put(child.isSelected());
                }
            }

            stateArray.put(jo);
        }

        state.put(LABEL_ITEM_STATE, stateArray);

        outState.putString(LABEL_STATE, state.toString());
    }

    public void onRestoreInstanceState(Bundle savedState) throws JSONException {
        if (savedState == null || !savedState.containsKey(LABEL_STATE)) {
            return;
        }

        JSONObject state = new JSONObject(savedState.getString(LABEL_STATE));

        if (!items.isEmpty()) {
            restoreState(state);
        } else {
            throw new RuntimeException("onRestoreInstanceState can only be called after the adapter has been populated with items");
        }
    }

    private void restoreState(JSONObject state) throws JSONException {
        choiceMode = choiceModeFromInt(state.getInt(LABEL_CHOICE_MODE));

        int adapterPos = RecyclerView.NO_POSITION;
        int viewTop = 0;

        if (!state.isNull(LABEL_FIRST_VISIBLE_POSITION)) {
            adapterPos = state.getInt(LABEL_FIRST_VISIBLE_POSITION);
        }

        if (!state.isNull(LABEL_FIRST_VISIBLE_TOP)) {
            viewTop = state.getInt(LABEL_FIRST_VISIBLE_TOP);
        }

        JSONArray itemState = state.getJSONArray(LABEL_ITEM_STATE);

        for (int i = 0; i < itemState.length(); i++) {
            JSONObject jo = itemState.getJSONObject(i);
            Item item = items.get(i);

            item.selected = jo.getBoolean(Item.LABEL_SELECTED);
            item.isExpanded = jo.getBoolean(Item.LABEL_IS_EXPANDED);

            if (item.selected) {
                numSelectedItems++;
            }

            if (!jo.isNull(Item.LABEL_CHILD_STATE)) {
                JSONArray childState = jo.getJSONArray(Item.LABEL_CHILD_STATE);

                for (int j = 0; j < childState.length(); j++) {
                    Item child = (Item) item.children.get(j);
                    child.selected = childState.getBoolean(j);

                    if (child.selected) {
                        numSelectedItems++;
                    }
                }
            }
        }

        notifyDataSetChanged();

        if (adapterPos != RecyclerView.NO_POSITION) {
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(adapterPos, viewTop);
            }
        }
    }
    */
}
