package com.mehtank.androminion.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.settings.GameRulesFragment;
import com.mehtank.androminion.fragments.settings.GeneralFragment;
import com.mehtank.androminion.fragments.settings.LoggingTestingFragment;
import com.mehtank.androminion.fragments.settings.UserInterfaceFragment;
import com.mehtank.androminion.util.ThemeSetter;
import com.mehtank.androminion.util.compat.TabInfo;
import com.mehtank.androminion.util.compat.ViewPagerAdapter;

import java.util.ArrayList;

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
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSetter.setTheme(this, true);
        ThemeSetter.setLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        ArrayList<TabInfo> list = new ArrayList<>();
        list.add(new TabInfo(GeneralFragment.class, android.R.drawable.ic_menu_preferences, R.string.general_tab_pref_title));
        list.add(new TabInfo(GameRulesFragment.class, android.R.drawable.ic_menu_agenda, R.string.game_rules_tab_pref_title));
        list.add(new TabInfo(UserInterfaceFragment.class, android.R.drawable.ic_menu_today, R.string.ui_tab_pref_title));
        list.add(new TabInfo(LoggingTestingFragment.class, android.R.drawable.ic_menu_zoom, R.string.logging_testings_tab_pref_title));

        ViewPager2 mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(this, list));

        TabLayout mTabLayout = findViewById(R.id.tablayout);
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            tab.setIcon(list.get(position).getIconId());
            tab.setText(list.get(position).getTextId());
        }).attach();

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(R.string.settingsactivity_title);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeSetter.setTheme(this, true);
        ThemeSetter.setLanguage(this);
    }
}
