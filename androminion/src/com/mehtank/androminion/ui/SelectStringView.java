package com.mehtank.androminion.ui;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;
import com.vdom.comms.Event;

public class SelectStringView extends BottomInputView implements AdapterView.OnItemClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "SelectStringView";

    ListView lv;
    private Event.EType returnType;
    private List<String> options;

    public SelectStringView(GameActivity top,
                            String header,
                            String[] options,
                            Event.EType returnType) {
        super(top, header);
        this.returnType = returnType;
        this.options = Arrays.asList(options);
        lv.setAdapter(new ArrayAdapter<String>(top, R.layout.view_selectstring, options));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
        if (v instanceof TextView) {
            ((FrameLayout) this.getParent()).removeView(this);
            String s = (((TextView) v).getText().toString());
            if (returnType == Event.EType.OPTION) {
                top.handle(new Event(Event.EType.OPTION).setInteger(options.indexOf(s)));
            } else if (returnType == Event.EType.BOOLEAN) {
                // "True" event is always first.
                top.handle(new Event(Event.EType.BOOLEAN).setBoolean(options.indexOf(s) == 0));
            } else {
                throw new RuntimeException("Got an event type I don't know how to handle!");
            }
        }
    }

    @Override
    protected View makeContentView(GameActivity activity) {
        lv = new ListView(top);
        lv.setOnItemClickListener(this);
        lv.setBackgroundDrawable(getResources().getDrawable(R.drawable.bottominputviewborder));

        return lv;
    }
}
