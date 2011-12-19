package com.mehtank.androminion.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;

public class AchievementsDialog {

    public AchievementsDialog(Context top) {
        FrameLayout fv = new FrameLayout (top);
        ScrollView sv = new ScrollView(top);
        sv.setVerticalScrollBarEnabled(true);
        LinearLayout ll = new LinearLayout(top);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(0x66000000);
        Achievements achievements = new Achievements((Androminion) top);
        
        for(int i=0; i < Achievements.keys.length; i++) {
            final boolean b = achievements.hasAchieved(Achievements.keys[i]);
            final CheckBox cb = new CheckBox(top);
            cb.setTextSize(cb.getTextSize() * .75f);
            cb.setText(Achievements.text[i]);
            cb.setChecked(b);
            cb.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    cb.setChecked(b);
                }
            });
            ll.addView(cb);
        }
        
        sv.addView(ll);
        fv.addView(sv);
        
        new AlertDialog.Builder(top)
//            .setIcon(R.drawable.logo)
            .setTitle(R.string.achievements_menu)
            .setView(fv)
            .setPositiveButton(android.R.string.ok, null)
            .show();        

    }

}
