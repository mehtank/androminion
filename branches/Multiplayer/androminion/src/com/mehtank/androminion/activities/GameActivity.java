package com.mehtank.androminion.activities;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mehtank.androminion.R;
import com.mehtank.androminion.ui.GameTable;
import com.mehtank.androminion.ui.HostDialog;
import com.mehtank.androminion.ui.JoinGameDialog;
import com.mehtank.androminion.util.HapticFeedback;
import com.mehtank.androminion.util.HapticFeedback.AlertType;
import com.mehtank.androminion.util.ThemeSetter;
import com.vdom.comms.Comms;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EType;
import com.vdom.comms.Event.EventObject;
import com.vdom.comms.EventHandler;
import com.vdom.comms.GameStatus;
import com.vdom.comms.MyCard;
import com.vdom.comms.NewGame;
import com.vdom.core.Game;

/**
 * How all this works:
 * 
 * if this activity is onCreated without saved instance state and without "command"-"extra",
 * it spawns the startgameactivity, which in turn runs the startgamefragment. the startgamefragment
 * asks the user about parameters of the new game and returns to the startgameactivity, which runs
 * *this* activity again, this time *with* "command".
 * 
 * On creation, we had also created a server intent (DominionServer), which instantiated VDomServer, which
 * started a vdom.comms server and listened on a hardcoded port. when we are run with "command", we send ourselves
 * the STARTGAME event directly, which causes us to connect to the server with the given port, and
 * send it the STARTGAME event. This causes the server to create a thread which contains Game.main()
 * with the arguments we had in this activity's "command" List of Strings. The server sends back
 * a GAMESTATS event.
 * 
 * When the server ran the Game.main(), it gave it a string referencing RemotePlayer.java. RemotePlayer.java
 * is executed by the vdom engine, and it gets all the information that a player would usually get. RemotePlayer
 * also listens on a custom port for someone to connect. This port is reported to us here with the GAMESTATS
 * event. In the function handshake(), we create a class JoinGameDialog, which parses the GAMESTATS and
 * would ask us which position we wanted to join if we had more than one option. We don't, so it uses the
 * option we have and sends us a JOINGAME event. This gives us the port to connect to the RemotePlayer, and
 * the name.
 * 
 * This makes us /disconnect/ from the vdomserver and /connect/ to the RemotePlayer.
 * 
 * We send it a HELLO message, which the RemotePlayer responds to by giving the list of other players
 * and cards. We create a GameTable object, which gives us the game view.
 * 
 * Everything that happens in the game from then on is sent to us as an event, which we handle and respond to.
 * The response contains the information about what we are doing.
 */

public class GameActivity extends SherlockActivity implements EventHandler {
    @SuppressWarnings("unused")
    private static final String TAG = "GameActivity";

    static final boolean MULTIPLAYER = false;

    private GameActivity top = this;

    private FrameLayout topView;
    private GameTable gt;
    private View splash;
    private TextView miniactionbar;

    private boolean gameRunning = false;
    private long lastBackClick = 0;

    private Comms comm;
    private volatile boolean readyForMessages = true; //This is set to true when we may receive messages. 

    private boolean gotQuit = false;

    public static final String DEFAULT_NAME = "You";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 1251;

    private String name;  // Name the player has in the game
    private String host;
    private int port;

    //	// for invites
    //	private String serverName;
    //	private String serverHost;
    //	private int serverPort;

    private final boolean DEBUGGING = false;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(top);
        ThemeSetter.setTheme(this, prefs.getBoolean("show_action_bar", "true".equals(getString(R.string.pref_showactionbar_default))));
        ThemeSetter.setLanguage(this);
        super.onCreate(savedInstanceState);


        topView = (FrameLayout) getLayoutInflater().inflate(R.layout.activity_game, null);
        setContentView(topView);

        ActionBar bar = getSupportActionBar();
        if (bar == null) {
            miniactionbar = (TextView) topView.findViewById(R.id.miniactionbar);
            miniactionbar.setVisibility(TextView.VISIBLE);
        } else {
            bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(R.string.app_name);
        }

        /*
         * Disable Strict mode (quick fix to make it run with targetSDKversion
         * 16). Should be properly fixed by putting all network dependent
         * behavior in a seperate thread.
         */
        //		if (Build.VERSION.SDK_INT >= 11) {
        //			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //			StrictMode.setThreadPolicy(policy);
        //		}

        gt = (GameTable) findViewById(R.id.gameTable);
        splash = findViewById(R.id.splash);

        name = prefs.getString("name", DEFAULT_NAME);

        startServer();

        Bundle extras = getIntent().getExtras();
        if(extras !=null) {
            host = extras.getString("host");
            port = extras.getInt("port");
            if (host != null && port != 0) {
                // handle(new Event(Event.EType.SETHOST).setString(host).setInteger(port));
                debug("Wants to connect to dom://" + host + ":" + port);
                new HostDialog(this, host, port);
                return;
            }
        }

        host = prefs.getString("host", DEFAULT_HOST);
        port = prefs.getInt("port", DEFAULT_PORT);

        if (!prefs.getString("LastVersion", "None").equals(getString(R.string.version))) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString("LastVersion", getString(R.string.version));
            edit.commit();

            //new AboutDialog(this, true);
        }

        // ds = new DominionServer(top);
        // quickstart();

        if(savedInstanceState == null) {
            if(getIntent().hasExtra("command")) {
                ArrayList<String> strs = getIntent().getStringArrayListExtra("command");
                handle(new Event(Event.EType.STARTGAME)
                       .setObject(new EventObject(strs.toArray(new String[0]))));
            } else if (getIntent().hasExtra("cards")) {
                Intent i = new Intent(this,StartGameActivity.class);
                i.putExtras(getIntent());
                startActivityForResult(i, 0);
            }
        }
        if (gameRunning) {
            Game.processUserPrefArgs(getUserPrefs().toArray(new String[0]));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(top);
        ThemeSetter.setTheme(this, prefs.getBoolean("show_action_bar", "true".equals(getString(R.string.pref_showactionbar_default))));
        ThemeSetter.setLanguage(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!prefs.getBoolean("show_statusbar", true)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        if (gameRunning) {
            Game.processUserPrefArgs(getUserPrefs().toArray(new String[0]));
        }
    }

    boolean getPref(String prefName) {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(top);
        return prefs.getBoolean(prefName, false);
    }

    ArrayList<String> getUserPrefs() {
        ArrayList<String> strs = new ArrayList<String>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(top);

        if(prefs.getBoolean("plat_colony", false)) {
            strs.add("-platcolony");
        }

        if (prefs.getBoolean("use_shelters", false))
        {
            strs.add("-useshelters");
        }

        if(prefs.getBoolean("quick_play", false)) {
            strs.add("-quickplay");
        }

        if(prefs.getBoolean("mask_names", false)) {
            strs.add("-masknames");
        }

        if(prefs.getBoolean("sort_cards", false)) {
            strs.add("-sortcards");
        }

        if (prefs.getBoolean("action_chains", false)) {
            strs.add("-actionchains");
        }

        if (prefs.getBoolean("suppress_redundant_reactions", true)) {
            strs.add("-suppressredundantreactions");
        }

        if (prefs.getBoolean("equal_start_hands", false)) {
            strs.add("-equalstarthands");
        }

        return strs;
    }

    void startServer() {
        startService(new Intent("com.mehtank.androminion.SERVER"));
    }
    void stopServer() {
        stopService(new Intent("com.mehtank.androminion.SERVER"));
    }

    public void quickstart() {
        // startServer();
        host = "localhost";
        startGame(port);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.game_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(gameRunning) {
            menu.findItem(R.id.startgame_menu).setVisible(false);
            menu.findItem(R.id.help_menu).setVisible(true);
        } else {
            menu.findItem(R.id.startgame_menu).setVisible(true);
            menu.findItem(R.id.help_menu).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Code from somewhere above, redundant
            if (getPref("exitonback")) {
                long now = System.currentTimeMillis();
                if (now - lastBackClick < 3000) // 3 seconds
                    finish();
                else {
                    lastBackClick = now;
                    Toast.makeText(top, getString(R.string.toast_quitconfirm), Toast.LENGTH_SHORT).show();
                }
                return true;
            } else {
                return false;
            }
        } else if (id == R.id.startgame_menu) {
            quickstart();
            return true;
        } else if (id == R.id.help_menu) {
            gt.showHelp(1);
        } else if (id == R.id.settings_menu) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.about_menu) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.stats_menu) {
            startActivity(new Intent(this, StatisticsActivity.class));
            return true;
        } else if (id == R.id.quit_menu) {
            finish();
            return true;
        }
        return false;
    }

    protected void invite() {};

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getPref("exitonback")) {
                long now = System.currentTimeMillis();
                if (now - lastBackClick < 3000) // 3 seconds
                    finish();
                else {
                    lastBackClick = now;
                    Toast.makeText(top, getString(R.string.toast_quitconfirm), Toast.LENGTH_SHORT).show();
                }
                return true;
            } else {
                return false;
            }
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            gt.logToggle();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        disconnect();
        stopServer();
        super.onDestroy();
        //System.exit(0);
    }

    public void addView(View v) {
        topView.addView(v);
    }

    public void nosplash() {
        if (splash != null)
            splash.setVisibility(View.INVISIBLE);
    }

    public void splash() {
        if (splash != null)
            splash.setVisibility(View.VISIBLE);
    }



    protected void alert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                                   new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog,
                                                           int whichButton) {
                                           dialog.dismiss();
                                       }
                                   }).show();
    }

    static final int MESSAGE_EVENT = 0;
    static final int MESSAGE_LOSTCONNECTION = 1;
    public static final String BASEDIRFROMEXT = "/Androminion";
    public static final String BASEDIR = Environment.getExternalStorageDirectory().getPath() + BASEDIRFROMEXT;

    @Override
    public void debug(String s) {
        if (DEBUGGING)
            Log.d("Androminion", s);
    }



    @Override
    public boolean handle(Event e) {
        if (!readyForMessages) { // This if false if we expect to be using doWait or get_ts. Then we must return false here so the message is forwarded there.
            return false;
        }
        Message m = Message.obtain(mHandler, MESSAGE_EVENT, e);
        mHandler.sendMessage(m);
        return true;
    }

    final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == MESSAGE_LOSTCONNECTION) {
                lostConnection();
                return;
            }
            if (msg.what != MESSAGE_EVENT)
                return;
            Event e = (Event) msg.obj;
            debug("Handling message: " + e.toString());
            boolean ack = false;

            switch (e.t) {
                // FROM RADIO --
                // startup
                case GAMESTATS:
                    splash();
                    handshake(e);
                    disconnect();
                    break;

                case NEWGAME:
                    NewGame ng = e.o.ng;
                    splash();

                    saveLastCards(ng.cards);
                    gt.newGame(ng.cards, ng.players);
                    gameRunning = true;

                    //PreferenceManager.getDefaultSharedPreferences(top).registerOnSharedPreferenceChangeListener(gt);
                    break;

                    // during game
                case CHAT: // received chat message
                    HapticFeedback.vibrate(top, AlertType.CHAT);
                    Toast.makeText(top, e.s, Toast.LENGTH_LONG).show();
                    break;

                case CARDOBTAINED: // a player got a card
                    setStatus(e.setString(gt.cardObtained(e.i, e.s)));
                    // ack = true;
                    break;

                case CARDTRASHED: // a player trashed a card
                    setStatus(e.setString(gt.cardTrashed(e.i, e.s)));
                    // ack = true;
                    break;

                case CARDREVEALED: // a player revealed a card
                    setStatus(e.setString(gt.cardRevealed(e.i, e.s)));
                    // ack = true;
                    break;

                case STATUS:  //RemotePlayer sent us the current status of the game, which we update
                    setStatus(e);
                    // ack = true;
                    break;

                case GETCARD:  //RemotePlayer wants us to choose a card and send an EType.CARD event back
                    gt.selectCard(e.o.sco, e.s, e.i, e.b);
                    break;

                case GETOPTION:  // RemotePlayer wants us to choose an option (like the old GETSTRING, but we have to figure out the strings first)
                    gt.selectOption(e);
                    break;

                case GETBOOLEAN:
                    gt.selectBoolean(e.c, e.o.os);
                    break;

                case ORDERCARDS:
                    gt.orderCards(e.o.is);
                    break;

                    // FROM DIALOGS --
                case HELLO: // received from JoinGameDialog; connect to standard port (which would be VDomServer)
                    startGame(port);
                    break;

                case JOINGAME: // received from JoinGameDialog; connect to received port (would be a RemotePlayer instance)
                    name = e.s;
                    startGame(e.i);
                    break;

                case SETHOST: // sent from HostDialog
                    if (e.s != null)
                        host = e.s;
                    if (e.i > 0)
                        port = e.i;
                    startGame(port);
                    break;

                case STARTGAME: // Sent from GameActivity to itself to connect to VDomServer
                    if (start(port))
                        put(e);
                    break;

                    // Whenever the user made a choice and sent this to GameTable (or various prompting dialogs), we receive it here.
                    // and send it on to RemotePlayer
                case SAY:
                case CARD:
                case CARDORDER:
                case OPTION:
                case BOOLEAN:
                    put(e);
                    break;

                case ACHIEVEMENT: // got an achievement
                    gt.achieved(e.s);
                    ack = true;
                    break;

                case DEBUG: // write something to debug log
                    debug(e.s);
                    break;

                case DISCONNECT: // lost connection; sent by Comms
                    if (!gotQuit)
                        lostConnection();
                    break;

                case QUIT: // Server quit us
                    gotQuit = true;
                    disconnect();
                case GETNAME:
                    break;
                case GETSERVER:
                    break;
                case KILLSENDER:
                    break;
                case PING:
                    break;
                case PONG:
                    break;
                case SERVER:
                    break;
                case SETNAME:
                    break;
                case SLEEP:
                    break;
                case Success:
                    break;
                default:
                    break;
            }
            if (ack)
                put(new Event(EType.Success));
        }

        /**
         * Could eventually skip setting the bar titles over and over again for
         * better performance, but works
         * 
         * @param e
         */
        private void setStatus(Event e) {
            GameStatus gs = e.o.gs;
            gt.setStatus(gs, e);
            String name = gt.getPlayerAdapter().getItem(gs.whoseTurn).name;
            String subtitle = buildHintString(gs, e.s, e.b);
            ActionBar bar = getSupportActionBar();
            if (bar == null) {
                miniactionbar.setText(name + ": " + subtitle);
            } else {
                bar.setSubtitle(subtitle);
                bar.setTitle(getResources().getString(R.string.currentplayer) + ": " + name);
            }
        }

        /**
         * This is just a quick try, duplicate to code in TurnView. Should be
         * fixed sooner or later...
         * 
         * @param gs
         * @param s
         * @param newTurn
         * @return
         */
        private String buildHintString(GameStatus gs, String s, boolean newTurn) {

            String actions;
            if (gs.turnStatus[0] == 1)
                actions = top.getString(R.string.action_single, "" + gs.turnStatus[0]);
            else
                actions = top.getString(R.string.action_multiple, "" + gs.turnStatus[0]);
            String buys;
            if (gs.turnStatus[1] == 1)
                buys = top.getString(R.string.buy_single, "" + gs.turnStatus[1]);
            else
                buys = top.getString(R.string.buy_multiple, "" + gs.turnStatus[1]);

            //		            String coinStr = "" + is[2] + ((potions > 0)?"p":"");
            String coinStr = "" + gs.turnStatus[2];
            if (gs.potions == 1) {
                coinStr += "p";
            } else if (gs.potions > 1) {
                coinStr += "p" + gs.potions;
            }
            //		            for(int i=0; i < potions; i++) {
            //		                coinStr += "p";
            //		            }
            String coinsStr = top.getString(R.string.coins, coinStr);
            String baseStr = top.getString(R.string.actions_buys_coins, actions, buys, coinsStr);

            return baseStr;
        }
    };

    private void saveLastCards(MyCard[] cards) {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(top);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("LastCardCount", cards.length);

        int i=0;
        for (MyCard c : cards)
            edit.putString("LastCard" + i++, (c.isBane ? Game.BANE : "") + c.originalSafeName);

        edit.commit();
    }

    private void disconnect() {
        if (comm != null)
            comm.stop();
        comm = null;

        gameRunning = false;
    }

    private boolean connect(int p) {
        disconnect();
        gotQuit = false;

        try {
            comm = new Comms(this, host, p);
            debug("New Comms connected to " + host + " on port " + p);
            return true;
        } catch (StreamCorruptedException e) {
            alert("Connection failed!", "Stream Corrupted.");
            e.printStackTrace();
        } catch (UnknownHostException e) {
            alert("Connection failed!", "Unknown Host.  Configure settings.");
        } catch (SocketException e) {
            alert("Connection failed!",
                  "Network unreachable or server refused connection.");
        } catch (IOException e) {
            alert("Connection failed!", "IO Error.");
            e.printStackTrace();
        }
        return false;
    }

    private void lostConnection() {
        alert("Error!", "Connection lost... Use the menu to try to reconnect.");
        disconnect();
    }

    private void put(Event e) {
        /*
         * The following try/catch block is made obsolete by the addition of
         * sendErrorHandler to the EventHandler-interface.
         * 
         * except it isn't: still getting NullPointerExceptions here [ 8/21/12 ]
         */

        // maybe this will help?
        if (comm == null) {
            if (!connect(port)) {
                lostConnection();
                return;
            }
        }
        //		try {
        comm.put_ts(e);
        //		} catch (Exception e1) {
        //			lostConnection();
        //		}
    }

    @Override
    public void sendErrorHandler(Exception e) {
        // need to asynchronously tell the UI-thread to call lostConnection().
        Message m = Message.obtain(mHandler, MESSAGE_EVENT);
        mHandler.sendMessage(m);
    }

    private boolean start(int p) {
        if (!connect(p))
            return false;

        return true;
    }

    //	private void saveHostPort() {
    //		SharedPreferences prefs;
    //		prefs = PreferenceManager.getDefaultSharedPreferences(top);
    //
    //		SharedPreferences.Editor edit = prefs.edit();
    //		edit.putString("host", host);
    //		edit.putInt("port", port);
    //		edit.commit();
    //	}

    /**
     * Connect to a VDomServer <b>or</b> a RemotePlayer instance.
     * 
     * Here we disconnect from wherever we were connected before, connect to 
     * host (host is the class variable) at port p and send it HELLO, on which
     * RemotePlayer responds (ideally) with NEWGAME, VDomServer with GAMESTATS
     * 
     * @param p port to connect to
     */
    private void startGame(int p) {

        try {            
            if (!connect(p))
                return;
            // Toast.makeText(top, "Loading game...", Toast.LENGTH_SHORT).show();

            /*
               put(new Event(EType.GETSERVER));
               Event e = comm.get();
               if (e == null)
               throw (new IOException());
               if (e.t == EType.SERVER) {
               serverName = name;
               serverHost = e.s;
               serverPort = e.i;
               } else
               throw (new IOException());
               */
            //			serverName = name;
            //			serverHost = getLocalIpAddress();
            //			serverPort = DEFAULT_PORT;

            readyForMessages = false; //HACK
            put( new Event( EType.HELLO ).setString( name ) );
            Event e = comm.doWait(); // Wait for response within timeout

            if (e == null) {
                throw new IOException( "No response received. Expected " + EType.GAMESTATS + " or " + EType.NEWGAME );
            }

            if (e.t == EType.GAMESTATS || e.t == EType.NEWGAME) {
                Message m = Message.obtain(mHandler, MESSAGE_EVENT, e);
                mHandler.handleMessage( m );
            }
            else {
                throw new IOException( "Unknown response received. Event was " + e );
            }
            readyForMessages = true;
            if (comm == null) { // This is the case when we connect to VDomServer
                return;
            }
            while ((e = comm.poll()) != null) {
                handle( e );
            }



        }
        catch (final IOException e) {
            alert( "Connection failed!", "Reported error was: " +e.getLocalizedMessage() );
        }
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                     enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("DroidServer", ex.toString());
        }
        return null;
    }

    /**
     * 
     * @param e GAMESTATS event
     */
    protected void handshake(Event e) {
        if (e.b) // true if the game is started
            new JoinGameDialog(top, e);
        else
            startNewGame(e);
    }

    protected void startNewGame(Event e) {
        //		if (!NOTOASTS) Toast.makeText(top, "Starting game...", Toast.LENGTH_SHORT).show();
        //		handle(new Event(Event.EType.STARTGAME)
        //			.setObject(new String[] { "Random", "Human Player",
        //					"Drew (AI)", "Earl (AI)" }));

        Intent i = new Intent(this,StartGameActivity.class);
        if(getIntent().hasExtra("cards")) {
            i.putExtras(getIntent());
        }
        startActivityForResult(i, 0);
        //new StartGameDialog(top, e, MULTIPLAYER, cardsPassedInExtras);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Result of StartGameActivity
        if(requestCode == 0 && resultCode == RESULT_OK && data.hasExtra("command")) {
            ArrayList<String> strs = data.getStringArrayListExtra("command");
            handle(new Event(Event.EType.STARTGAME)
                   .setObject(new EventObject(strs.toArray(new String[0]))));
            Toast.makeText(top, top.getString(R.string.toast_starting), Toast.LENGTH_SHORT).show();
        }
    }

}
