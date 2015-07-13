package com.ilp.innovations.myapplication.FragmentPageAdapers;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ilp.innovations.myapplication.Fragments.ConfirmedSlotsFragment;
import com.ilp.innovations.myapplication.Fragments.PlaceholderFragment;
import com.ilp.innovations.myapplication.Fragments.ReservedSlotsFragment;
import com.ilp.innovations.myapplication.Fragments.SlotViewFragment;
import com.ilp.innovations.myapplication.Fragments.UnAllocatedSlotsFragment;

import java.util.Locale;


public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private SlotViewFragment slotViewFragment;
    private PlaceholderFragment placeholderFragment;
    private ReservedSlotsFragment reservedSlotsFragment;
    private ConfirmedSlotsFragment confirmedSlotsFragment;
    private String[] headLines;
    private int tabCount;


    public SectionsPagerAdapter(FragmentManager fm, String[] headLines) {
        super(fm);
        this.headLines = headLines;
        tabCount = headLines.length;
        slotViewFragment = new SlotViewFragment();
        reservedSlotsFragment = new ReservedSlotsFragment();
        placeholderFragment = new PlaceholderFragment();
        confirmedSlotsFragment = new ConfirmedSlotsFragment();
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return slotViewFragment.newInstance();
            case 1:
                return placeholderFragment.newInstance();
            case 2:
                return reservedSlotsFragment.newInstance();
            case 3:
                return confirmedSlotsFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        if(tabCount>=position)
            return this.headLines[position].toUpperCase();
        else
            return null;
    }

}
