package nl.erikduisters.pathfinder.ui.activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

import nl.erikduisters.pathfinder.ui.fragment.ViewPagerFragment;

/**
 * Created by Erik Duisters on 27-06-2018.
 */

public class FragmentAdapter extends FragmentPagerAdapter {
    public static class TabItem {
        @StringRes private final int nameResId;
        private boolean enabled;
        private final FragmentProvider fragmentProvider;
        private ViewPagerFragment fragment;

        public TabItem(@StringRes int nameResId, boolean enabled, FragmentProvider provider) {
            this.nameResId = nameResId;
            this.enabled = enabled;
            this.fragmentProvider = provider;
        }
    }

    private final ArrayList<TabItem> tabList;
    private final Context context;

    public FragmentAdapter(FragmentManager fm, Context ctx) {
        super(fm);

        context = ctx;
        tabList = new ArrayList<>();
    }

    public void addTab(TabItem item) {
        tabList.add(item);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int index) {
        TabItem item = tabList.get(index);

        item.fragment = item.fragmentProvider.provideFragment();
        return (Fragment) item.fragment;
    }

    @NonNull
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //On rotation change getItem is never called because the fragment is still available in the fragment manager
        TabItem item = tabList.get(position);
        item.fragment = (ViewPagerFragment) super.instantiateItem(container, position);

        return item.fragment;
    }

    @Override
    public int getCount() {
        return tabList.size();
    }

    @Override
    public CharSequence getPageTitle(int index) {
        int resId = tabList.get(index).nameResId;

        return resId == 0 ? "" : context.getString(tabList.get(index).nameResId);
    }

    public @StringRes int getPageTitleResId(int index) {
        return tabList.get(index).nameResId;
    }

    /* FragmentPagerAdapter only detaches fragments but it never removes them
     * If I ever change to FragmentStatePagerAdapter I need to implement this
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        tabList.get(position).vpiFragment=null;
    }
    */

    @Nullable
    public ViewPagerFragment getFragment(int index) {
        return index >= tabList.size() ? null : tabList.get(index).fragment;
    }

    @Nullable
    public <T extends ViewPagerFragment> T getFragment(FragmentProvider provider) {
        for (TabItem tabItem : tabList) {
            if (tabItem.fragmentProvider == provider) {
                //noinspection unchecked
                return (T) tabItem.fragment;
            }
        }

        return null;
    }
}
