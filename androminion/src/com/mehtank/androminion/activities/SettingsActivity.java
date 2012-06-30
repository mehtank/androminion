package com.mehtank.androminion.activities;

import com.mehtank.androminion.R;
import com.mehtank.androminion.util.ThemeSetter;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	ThemeSetter.set(this);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
