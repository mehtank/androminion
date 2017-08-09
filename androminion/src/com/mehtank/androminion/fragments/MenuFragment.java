package com.mehtank.androminion.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.mehtank.androminion.R;

public class MenuFragment extends Fragment {
    @SuppressWarnings("unused")
    private static final String TAG = "MenuFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ScrollView scrollView = (ScrollView) inflater.inflate(
                R.layout.fragment_menu, container);

        // No longer needen since moohtank changed the menu layout back to the old style
        //        // White text on Menu buttons on Light theme and Android 3.0+
        //        if (Build.VERSION.SDK_INT >= 11
        //                && !(PreferenceManager.getDefaultSharedPreferences(
        //                        this.getActivity()).getString("theme",
        //                        "androminion-dark").equals("androminion-dark"))) {
        //            Button butStart = (Button) linLayout.findViewById(R.id.but_start);
        //            Button butStats = (Button) linLayout.findViewById(R.id.but_stats);
        //            Button butSettings = (Button) linLayout
        //                    .findViewById(R.id.but_settings);
        //            Button butAbout = (Button) linLayout.findViewById(R.id.but_about);
        //
        //            butStart.setTextColor(getResources().getColor(
        //                    android.R.color.primary_text_dark));
        //            butStats.setTextColor(getResources().getColor(
        //                    android.R.color.primary_text_dark));
        //            butSettings.setTextColor(getResources().getColor(
        //                    android.R.color.primary_text_dark));
        //            butAbout.setTextColor(getResources().getColor(
        //                    android.R.color.primary_text_dark));
        //        }
        return scrollView;
    }
}
