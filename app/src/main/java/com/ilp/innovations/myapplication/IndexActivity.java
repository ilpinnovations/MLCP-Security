package com.ilp.innovations.myapplication;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.ilp.innovations.myapplication.FragmentPageAdapers.IndexPageAdapter;


public class IndexActivity extends ActionBarActivity implements ActionBar.TabListener {

    IndexPageAdapter indexPageAdapter;

    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bg_main_head)));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.bg_tab)));

        indexPageAdapter = new IndexPageAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(indexPageAdapter);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);


        actionBar.addTab(
                actionBar.newTab()
                        .setText(indexPageAdapter.getPageTitle(0))
                        .setTabListener(this)
        );

    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }
}
