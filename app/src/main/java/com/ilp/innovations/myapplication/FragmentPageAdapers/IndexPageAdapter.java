package com.ilp.innovations.myapplication.FragmentPageAdapers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ilp.innovations.myapplication.Fragments.IndexFragment;

public class IndexPageAdapter extends FragmentPagerAdapter{

    private IndexFragment indexFragment;

    public IndexPageAdapter(FragmentManager fm) {
        super(fm);
        indexFragment = new IndexFragment();
    }

    @Override
    public Fragment getItem(int position) {

        return indexFragment.newInstance();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "FLOOR LIST";
    }


}
