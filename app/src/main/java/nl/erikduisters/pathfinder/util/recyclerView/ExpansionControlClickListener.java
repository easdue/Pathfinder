package nl.erikduisters.pathfinder.util.recyclerView;

/**
 * Created by Erik Duisters on 01-05-2017.
 */
public interface ExpansionControlClickListener<T extends ExpandableRecyclerViewAdapter.Item<T>> {
    void onExpansionControlClicked(ExpandableRecyclerViewAdapter.ViewHolder<T> viewHolder);
}
