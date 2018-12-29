package de.digisocken.pilp_com;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class MyPagerAdapter extends FragmentPagerAdapter {
    public static final String[] titles = {"Clk","Who","Msg","Area","News"};

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ClockFragment.newInstance(position, titles[position]);
            case 1:
                return ContactFragment.newInstance(position, titles[position]);
            case 2:
                return MsgFragment.newInstance(position, titles[position]);
            case 3:
                return AreaFragment.newInstance(position, titles[position]);
            case 4:
                return NewsFragment.newInstance(position, titles[position]);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
