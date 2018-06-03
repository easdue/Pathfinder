package nl.erikduisters.pathfinder.ui.activity.main_activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import nl.erikduisters.pathfinder.R;
import nl.erikduisters.pathfinder.ui.BaseActivity;

public class MainActivity extends BaseActivity<MainActivityViewModel>
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.nav_view) NavigationView navigationView;
    CircleImageView avatar;
    TextView username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        avatar = headerView.findViewById(R.id.gpsies_avatar);
        username = headerView.findViewById(R.id.gpsies_username);

        //TODO: Move this to render
        VectorDrawableCompat avatarVectorDrawable = VectorDrawableCompat.create(getResources(), R.drawable.vector_drawable_ic_missing_avatar, null);
        avatar.setImageDrawable(avatarVectorDrawable);
        username.setText("Please login");
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected Class<MainActivityViewModel> getViewModelClass() {
        return MainActivityViewModel.class;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_import:
                break;
            case R.id.nav_login_register:
                break;
            case R.id.nav_gps_status:
                break;
            case R.id.nav_settings:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
    }
}
