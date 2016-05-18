package net.theunifyproject.lethalskillzz.activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import net.theunifyproject.lethalskillzz.R;
import net.theunifyproject.lethalskillzz.app.PrefManager;
import net.theunifyproject.lethalskillzz.fragment.BossoFragment;
import net.theunifyproject.lethalskillzz.fragment.GidanKwanoFragment;

public class TransitActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = TransitActivity.class.getSimpleName();
    private PrefManager pref;

    private TabLayout tabLayout;
    private FloatingActionButton btn_fab;
    private ViewPager viewPager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transit);

        Toolbar toolbar = (Toolbar) findViewById(R.id.transit_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = new PrefManager(getApplicationContext());

        btn_fab = (FloatingActionButton) findViewById(R.id.transit_fab);
        btn_fab.setOnClickListener(this);

        viewPager = (ViewPager) findViewById(R.id.transit_viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.transit_tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabLabels();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.transit_fab: {
                Intent intent = new Intent(this, PostTransitActivity.class);
                startActivity(intent);
            }
            break;
        }
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new BossoFragment(), "ONE");
        adapter.addFrag(new GidanKwanoFragment(), "TWO");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
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

    private void setupTabLabels() {
        tabLayout.getTabAt(0).setText(getString(R.string.bosso_prompt));
        tabLayout.getTabAt(1).setText(getString(R.string.gidan_kwano_prompt));

    }

}
