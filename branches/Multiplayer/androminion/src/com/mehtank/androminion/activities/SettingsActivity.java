package com.mehtank.androminion.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mehtank.androminion.R;
import com.mehtank.androminion.util.ThemeSetter;

/**
 * This activity shows the settings menu.
 *
 * Rewrite to support actionbar (backwards compatible to API7).
 *
 * Could be even better by supporting a modern layout on tablets in landscreen
 * mode with PreferenceFragment I guess, but seems to be a bit more complicated
 * and provides almost no use since preferences are not accessed very often.
 *
 * For example how to do it right:
 * https://github.com/commonsguy/cw-omnibus/tree/master/Prefs/FragmentsBC
 */
public class SettingsActivity extends SherlockPreferenceActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "SettingsActivity";

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSetter.setTheme(this, true);
        ThemeSetter.setLanguage(this);
        super.onCreate(savedInstanceState);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(R.string.settingsactivity_title);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeSetter.setTheme(this, true);
        ThemeSetter.setLanguage(this);
    }
}
