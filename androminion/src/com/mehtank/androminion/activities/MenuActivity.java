package com.mehtank.androminion.activities;

import java.util.ArrayList;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.mehtank.androminion.R;
import com.mehtank.androminion.fragments.StartGameFragment;
import com.mehtank.androminion.fragments.StartGameFragment.OnStartGameListener;
import com.mehtank.androminion.util.ThemeSetter;

/**
 * Start screen of the application, showing a menu.
 *
 */
public class MenuActivity extends SherlockFragmentActivity implements
OnStartGameListener {
    private static final String TAG = "MenuActivity";

    private boolean mTwoColums = false; // Two-Column-Layout, possibly tablet
    private int mState = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeSetter.setTheme(this, true);
        ThemeSetter.setLanguage(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Fix so Androminion doesn't crash when updating
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        Log.d(TAG,
              "Theme is set to "
              + pref.getString("theme", "androminion-dark"));
        if (pref.getString("theme", getString(R.string.pref_theme_default)).equals("androminion")) {
            // Settings from previous Androminion version exist
            Log.d(TAG, "Resetting theme setting to default value");
            Editor editor = pref.edit();
            editor.remove("theme");
            editor.commit();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        if (findViewById(R.id.fragment_content) != null) {
            mTwoColums = true;
            if (savedInstanceState == null
                || getSupportFragmentManager().findFragmentById(
                        R.id.fragment_content) == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_content, createStartGameFragment())
                        .commit();
                mState = R.id.but_start;
            }
            if (savedInstanceState != null) {
                mState = savedInstanceState.getInt("mState");
            }
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getString("LastVersion", "None").equals(getString(R.string.version))) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("LastVersion", getString(R.string.version));
            edit.commit();

            LayoutInflater inflator = LayoutInflater.from(getApplicationContext());
            View v = inflator.inflate(R.layout.fragment_whatsnew, null);
            TextView tv = (TextView) v.findViewById(R.id.whatsnew);
            tv.setText(Html.fromHtml(getString(R.string.whatsnew)));
            tv.setMovementMethod(LinkMovementMethod.getInstance());

            new AlertDialog.Builder(this)
                    .setView(v)
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(R.string.app_name)
                    .create()
                    .show();
        }
    }

    private SherlockFragment createStartGameFragment() {
        SherlockFragment f = new StartGameFragment();
        if (getIntent().hasExtra("cards")) {
            f.setArguments(getIntent().getExtras());
        }
        return f;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("mState", mState);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onClickStartGame(View view) {
        if (mTwoColums) {
            if (mState != R.id.but_start) {
                mState = R.id.but_start;
                changeFragment(createStartGameFragment());
            }
        } else {
            Intent i = new Intent(this, StartGameActivity.class);
            if (getIntent().hasExtra("cards")) {
                i.putExtras(getIntent());
            }
            startActivityForResult(i, 0);
        }
    }

    public void onClickStats(View view) {
        startActivity(new Intent(this, StatisticsActivity.class));
    }

    public void onClickSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onClickAbout(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void onClickJoinGame(View view) {
        // TODO(matt): what in the world do I do here?  We need to have the user enter a host name
        // and port, and pass that host name and port to the GameActivity, so that the GameActivity
        // sends a HELLO event to the RemotePlayer that's listening at that port.
        //
        // Ok, to do this we need to call something like startActivityForResult(some_intent,
        // some_result_code).  Then let that activity get us a host and port, and the activity
        // calls setResult(RESULT_ok, data); finish(); (as in
        // StartGameActivity.onStartGameClick()).  That will send us back here to
        // onActivityResult(), where we need to add an additional check for the request code.

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        LinearLayout vg = new LinearLayout(this);
        vg.setOrientation(LinearLayout.VERTICAL);
        final EditText name = new EditText(this);
        name.setSingleLine();
        name.setText(prefs.getString("name", GameActivity.DEFAULT_NAME));
        TextView nameView = new TextView(this);
        nameView.setText("\n"+getString(R.string.your_name));
        vg.addView(nameView);
        vg.addView(name);
        final EditText host = new EditText(this);
        host.setSingleLine();
        host.setText(prefs.getString("host", "localhost"));
        TextView hostView = new TextView(this);
        hostView.setText("\n"+getString(R.string.host));
        vg.addView(hostView);
        vg.addView(host);
        final EditText port = new EditText(this);
        port.setSingleLine();
        port.setText(prefs.getString("port", "2255"));
        TextView portView = new TextView(this);
        portView.setText("\n"+getString(R.string.port));
        vg.addView(portView);
        vg.addView(port);
        new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setView(vg)
                .setTitle(getString(R.string.enter_host_and_port))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(MenuActivity.this, GameActivity.class);
                        i.putExtra("name", name.getText().toString());
                        i.putExtra("host", host.getText().toString());
                        i.putExtra("port", Integer.valueOf(port.getText().toString()));
                        startActivity(i);
                    }
                })
                .create()
                .show();
    }

    private void changeFragment(SherlockFragment newFragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_content, newFragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Intent i = new Intent(this, GameActivity.class);
            i.putExtras(data);
            startActivity(i);
        }
    }

    @Override
    public void onStartGameClick(ArrayList<String> values) {
        Intent i = new Intent(this, GameActivity.class);
        i.putStringArrayListExtra("command", values);
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        ThemeSetter.setTheme(this, true);
        ThemeSetter.setLanguage(this);
    }
}
