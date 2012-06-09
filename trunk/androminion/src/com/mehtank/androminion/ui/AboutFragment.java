package com.mehtank.androminion.ui;

import com.mehtank.androminion.R;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

public class AboutFragment extends Fragment {
	View mView;
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
        mView = inflater.inflate(R.layout.aboutfragment, null);

        TabHost th=(TabHost)mView;
        th.setup();
        
        //set background
        View content = th.findViewById(android.R.id.tabcontent);
        content.setBackgroundResource(R.drawable.bigicon_background);
        content.getBackground().setAlpha(48);

        // create tab1
        TabHost.TabSpec spec1 = th.newTabSpec("tab1");
        spec1.setIndicator(getString(R.string.about_menu), getResources().getDrawable(android.R.drawable.ic_menu_info_details));
        spec1.setContent(R.id.abouttab1);
        th.addTab(spec1);
        //create tab2
        TabHost.TabSpec spec2 = th.newTabSpec("tab2");
        spec2.setIndicator(getString(R.string.whatsnew_menu), getResources().getDrawable(android.R.drawable.ic_menu_view));
        spec2.setContent(R.id.abouttab2);
        th.addTab(spec2);
        //create tab3
        TabHost.TabSpec spec3 = th.newTabSpec("tab3");
        spec3.setIndicator(getString(R.string.contrib_menu), getResources().getDrawable(android.R.drawable.ic_menu_my_calendar));
        spec3.setContent(R.id.abouttab3);
        th.addTab(spec3);
        
        // "render" HTML
        TextView tv = (TextView) mView.findViewById(R.id.whatsnew);
        tv.setText( Html.fromHtml(getString(R.string.whatsnew)));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        
        // insert version name
        String vname = "";
        try {
        	vname = getString(R.string.version, getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {};
        tv = (TextView) mView.findViewById(R.id.version);
        tv.setText(vname);
        return mView;
	}
}
