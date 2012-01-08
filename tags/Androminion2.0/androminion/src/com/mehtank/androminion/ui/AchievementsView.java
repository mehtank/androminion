package com.mehtank.androminion.ui;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class AchievementsView extends FrameLayout {

    public AchievementsView(Context top, Achievements achievements) {
    	super(top);
    	
        ScrollView sv = new ScrollView(top);
        sv.setVerticalScrollBarEnabled(true);
        LinearLayout ll = new LinearLayout(top);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(0x66000000);
        
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
        addView(sv);
    }

}
