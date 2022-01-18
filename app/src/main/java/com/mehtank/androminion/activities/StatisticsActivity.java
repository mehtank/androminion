package com.mehtank.androminion.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.AchievementsFragment;
import com.mehtank.androminion.fragments.WinlossFragment;
import com.mehtank.androminion.util.Achievements;
import com.mehtank.androminion.util.ThemeSetter;
import com.mehtank.androminion.util.compat.TabInfo;
import com.mehtank.androminion.util.compat.ViewPagerAdapter;

import java.util.ArrayList;

/**
 * This activity just shows two tabs: statistics and achievements.
 * 
 * Rewrite to support actionbar, tabs and swipe gestures (backwards compatible
 * to API8).
 */
public class StatisticsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSetter.setTheme(this, true);
        ThemeSetter.setLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(R.string.statisticsactivity_title);
        }

        ArrayList<TabInfo> list = new ArrayList<>();
        list.add(new TabInfo(WinlossFragment.class, android.R.drawable.ic_menu_myplaces, R.string.win_loss_menu));
        list.add(new TabInfo(AchievementsFragment.class, android.R.drawable.ic_menu_agenda, R.string.achievements_menu));

        ViewPager2 mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(this, list));

        TabLayout mTabLayout = findViewById(R.id.tablayout);
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            tab.setIcon(list.get(position).getIconId());
            tab.setText(list.get(position).getTextId());
        }).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_statistics, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        } else if (id == R.id.resetstatistics_menu) {
            buildResetDialog(this).show();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private AlertDialog buildResetDialog(final Context context) {
        final boolean[] choices = {true, true};
        class choiceListenerClass implements DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnClickListener {
            private boolean resetStats = choices[0];
            private boolean resetAchievements = choices[1];
            private AlertDialog mDialog;

            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (which == 0) {
                    resetStats = isChecked;
                } else if (which == 1) {
                    resetAchievements = isChecked;
                }
                Button ResetButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                ResetButton.setEnabled(resetStats || resetAchievements);
            }

            @Override
            public void onClick(DialogInterface dialog, int i) {
                final Achievements achievements = new Achievements(context);
                if (resetStats) {
                    achievements.resetStats();
                }
                if (resetAchievements) {
                    achievements.resetAchievements();
                }
            }

            public void setDialog(AlertDialog dialog) {
                mDialog = dialog;
            }
        }

        final choiceListenerClass choiceListener = new choiceListenerClass();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.reset)
                .setNegativeButton(android.R.string.cancel, null)
                .setMultiChoiceItems(R.array.reset_choices, choices, choiceListener)
                .setPositiveButton(R.string.reset, choiceListener);
        AlertDialog dialog = builder.create();
        choiceListener.setDialog(dialog);
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeSetter.setTheme(this, true);
        ThemeSetter.setLanguage(this);
    }
}
