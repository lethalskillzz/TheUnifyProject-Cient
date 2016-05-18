package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.konifar.fab_transformation.FabTransformation;

import net.android.volley.toolbox.ImageLoader;
import net.android.volley.toolbox.RoundNetworkImageView;
import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.AppConfig;
import net.theunifyproject.lethalskillzz.app.AppController;
import net.theunifyproject.lethalskillzz.fragment.DiscoverFragment;
import net.theunifyproject.lethalskillzz.fragment.FeedFragment;
import net.theunifyproject.lethalskillzz.fragment.NavDrawerFragment;
import net.theunifyproject.lethalskillzz.fragment.NotificationFragment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.fragment.SearchFragment;

public class MainActivity extends AppCompatActivity implements NavDrawerFragment.FragmentDrawerListener {

    private static String TAG = MainActivity.class.getSimpleName();
    private PrefManager pref;
    HashMap<String, String> profile;

    private NavDrawerFragment drawerFragment;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private RoundNetworkImageView drawerProfilePic;
    private TextView drawerProfileName, drawerUsername;
    public static TabLayout tabLayout;
    private RelativeLayout nav_header;
    private FloatingActionButton btn_fab;
    private View overlay;
    private LinearLayout extra_view;
    private ViewPager viewPager;

    private int[] tabIcons = {
            R.drawable.ic_tab_feed,
            R.drawable.ic_tab_notification,
            R.drawable.ic_tab_discover
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new PrefManager(getApplicationContext());
        // Checking if user session
        // if not logged in, take user to sms screen

        profile = pref.getUserDetails();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Typeface mTf = Typeface.createFromAsset(getAssets(), "OpenSans-Semibold.ttf");
        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            TextView titleTextView = (TextView) f.get(toolbar);
            titleTextView.setTypeface(mTf);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }


        drawerFragment = (NavDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), toolbar);
        drawerFragment.setDrawerListener(this);

        nav_header = (RelativeLayout)findViewById(R.id.nav_header_container);

        drawerProfilePic = (RoundNetworkImageView) findViewById(R.id.nav_drawer_profilePic);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        drawerProfilePic.setImageUrl(AppConfig.URL_PROFILE_PIC + pref.getUsername() + ".png"+AppConfig.AUTO_REF_HACK(), imageLoader);
        drawerProfilePic.setDefaultImageResId(R.drawable.ic_user);

        drawerProfileName = (TextView) findViewById(R.id.nav_drawer_profileName);
        drawerUsername = (TextView) findViewById(R.id.nav_drawer_username);
        drawerProfileName.setText(profile.get("name"));
        drawerUsername.setText("@"+profile.get("username"));


        btn_fab = (FloatingActionButton) findViewById(R.id.fab);
        overlay = (View) findViewById(R.id.overlay);
        extra_view = (LinearLayout) findViewById(R.id.extra);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
      /*  tabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tabLayout.getTabAt(position).getIcon().setAlpha(255);
            }
        });*/

        setupTabIcons();

        Intent intent = getIntent();
        if(intent.getStringExtra("isNotify") != null) {
            viewPager.setCurrentItem(1);
        }


    }

    @Override
    public void onResume() {
        super.onResume();

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        drawerProfilePic.setImageUrl(AppConfig.URL_PROFILE_PIC + pref.getUsername() + ".png" + AppConfig.AUTO_REF_HACK(), imageLoader);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {

            case R.id.toolbar_post_feed:
                goto_post_feed();
                break;

            case R.id.toolbar_search:
                onClickSearch();
                break;

            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FeedFragment(), "ONE");
        adapter.addFrag(new NotificationFragment(), "TWO");
        adapter.addFrag(new DiscoverFragment(), "THREE");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return null;
        }
        /*@Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }*/

    }

    @Override
    public void onDrawerItemSelected(View view, int position) {

    }

    public void onClickFab(View v) {
        if (btn_fab.getVisibility() == View.VISIBLE) {
            FabTransformation.with(v).setOverlay(overlay).transformTo(extra_view);
        }
    }


    public void onClickOverlay(View v) {
        if (btn_fab.getVisibility() != View.VISIBLE) {
            FabTransformation.with(btn_fab).setOverlay(overlay).transformFrom(extra_view);
        }
    }

    @Override
    public void onBackPressed() {
        if (btn_fab.getVisibility() != View.VISIBLE) {
            FabTransformation.with(btn_fab).setOverlay(overlay).transformFrom(extra_view);
            return;
        }
        super.onBackPressed();
    }


    public void onClickRepo(View v) {
        Intent intent = new Intent(getApplicationContext(), RepositoryActivity.class);
        startActivity(intent);
    }

    public void onClickShop(View v) {
        Intent intent = new Intent(getApplicationContext(), ShoppingActivity.class);
        startActivity(intent);
    }

    public void onClickDigest(View v) {
        Intent intent = new Intent(getApplicationContext(), DigestActivity.class);
        startActivity(intent);
    }

    public void onClickTransit(View v) {
        Intent intent = new Intent(getApplicationContext(), TransitActivity.class);
        startActivity(intent);
    }


    public void goto_profile(View v) {

        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra("username", profile.get("username"));
        startActivity(intent);
    }


    private void goto_post_feed()
    {
        Intent intent = new Intent(MainActivity.this, PostFeedActivity.class);
        intent.putExtra("intent_type","post");
        startActivity(intent);
    }

    private void onClickSearch()
    {
        if (findViewById(R.id.drawer_layout) != null) {

            SearchFragment search_fragment = new SearchFragment();

            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                    .add(R.id.drawer_layout, search_fragment).commit();

        }
    }



}