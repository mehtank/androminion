package com.mehtank.androminion.util.compat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter  extends FragmentStateAdapter {
    private final ArrayList<TabInfo> list;

    public ViewPagerAdapter(AppCompatActivity activity, ArrayList<TabInfo> list) {
        super(activity);
        this.list = list;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            return (Fragment) list.get(position).getFragmentClass().getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
