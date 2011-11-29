package com.mehtank.androminion;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mehtank.androminion.comms.Comms;
import com.mehtank.androminion.comms.Event;
import com.mehtank.androminion.comms.GameStatus;
import com.mehtank.androminion.comms.NewGame;
import com.mehtank.androminion.comms.EventHandler;
import com.mehtank.androminion.comms.Event.EType;
import com.mehtank.androminion.ui.AboutDialog;
import com.mehtank.androminion.ui.AchievementsDialog;
import com.mehtank.androminion.ui.GameTable;
import com.mehtank.androminion.ui.HostDialog;
import com.mehtank.androminion.ui.JoinGameDialog;
import com.mehtank.androminion.ui.SplashView;
import com.mehtank.androminion.ui.StartGameDialog;
import com.mehtank.androminion.ui.StatsDialog;

public class Androminion extends Activity implements EventHandler {
	protected static final int MENU_LOCAL_START = 31;
	protected static final int MENU_REMOTE_START = 32;
	protected static final int MENU_INVITE = 33;
	protected static final int MENU_SETTINGS = 40;
	protected static final int MENU_HELP = 50;
	protected static final int MENU_ABOUT = 60;
    protected static final int MENU_RESTART = 70;
    protected static final int MENU_MAIN = 80;
	protected static final int MENU_QUIT = 90;
	protected static final int MENU_ACHIEVEMENTS = 100;
    protected static final int MENU_STATS = 110;
	
	private static final boolean DEBUGGING = false;
	public static boolean NOTOASTS = false;

	static final boolean MULTIPLAYER = false;

	String[] cardsPassedInExtras;
	
	protected Androminion top = this;
	
	FrameLayout topView;

	Vibrator v;

	GameTable gt;
	SplashView splash;

	boolean gameRunning = false;

	Comms comm;
	Thread commThread;
	boolean gotQuit = false;

	public static final String DEFAULT_NAME = "You";
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 1251;
	String name;
	String host;
	int port;
	
	// for invites
	protected String serverName;
	protected String serverHost;
	protected int serverPort;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs;
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if(intent != null) {
		    Bundle extras = intent.getExtras();
		    if(extras != null) {
		        cardsPassedInExtras = getIntent().getExtras().getStringArray("cards");
		    }
		}

		debug("Dominion onCreate called!");

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		topView = new FrameLayout(this);

		gt = new GameTable(this);
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.FILL_PARENT);
		topView.addView(gt, p);

		splash = new SplashView(top);
		topView.addView(splash, p);

		setContentView(topView);

		v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		prefs = PreferenceManager.getDefaultSharedPreferences(top);
		name = prefs.getString("name", DEFAULT_NAME);
		
		startServer();

		Bundle extras = getIntent().getExtras(); 
		if(extras !=null) {
			host = extras.getString("host");
			port = extras.getInt("port");
			if (host != null && port != 0) {
				// handle(new Event(Event.EType.SETHOST).setString(host).setInteger(port));
				System.out.println("Wants to connect to dom://" + host + ":" + port);
				new HostDialog(this, host, port);
				return;
			}
		}
		
		host = prefs.getString("host", DEFAULT_HOST);
		port = prefs.getInt("port", DEFAULT_PORT);

		// ds = new DominionServer(top);
		// quickstart();
	}
	
	@Override 
	public void onResume() {
		super.onResume();
		debug("onResume called");
		
		NOTOASTS = getPref("toastsoff");
		
		if (!getPref("statusbar"))
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	boolean getPref(String name) {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(top);
		return prefs.getBoolean(name, false);
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		createMenu(menu);
		return super.onCreateOptionsMenu(menu);
	}

    public void createMenu(Menu menu) {
        if (gameRunning) {
            MenuItem helpMenu = menu.add(Menu.NONE, MENU_HELP, Menu.NONE, "Help");
            helpMenu.setIcon(android.R.drawable.ic_menu_help);
        } else {
            MenuItem localMenu = menu.add(Menu.NONE, MENU_LOCAL_START, Menu.NONE, "Start Game");
            localMenu.setIcon(android.R.drawable.ic_menu_slideshow);
        }
        
        MenuItem settingsMenu = menu.add(Menu.NONE, MENU_SETTINGS, Menu.NONE, "Settings");
        settingsMenu.setIcon(android.R.drawable.ic_menu_preferences);

        MenuItem aboutMenu = menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE,
            "About"); 
        aboutMenu.setIcon(android.R.drawable.ic_menu_info_details);

        MenuItem achievmentsMenu = menu.add(Menu.NONE, MENU_ACHIEVEMENTS, Menu.NONE,
            "Achievements"); 
        achievmentsMenu.setIcon(android.R.drawable.ic_menu_myplaces);
        
        MenuItem statsMenu = menu.add(Menu.NONE, MENU_STATS, Menu.NONE,
            "Win/Loss"); 
        statsMenu.setIcon(android.R.drawable.ic_menu_view);
        
        if (gameRunning) {
//            MenuItem restartMenu = menu.add(Menu.NONE, MENU_RESTART, Menu.NONE,
//                    "Restart"); 
//            restartMenu.setIcon(android.R.drawable.ic_menu_rotate);
//    
//            MenuItem mainMenuMenu = menu.add(Menu.NONE, MENU_MAIN, Menu.NONE,
//                    "Main Menu");
//            mainMenuMenu.setIcon(android.R.drawable.ic_menu_revert);
        }
        
        MenuItem quitMenu = menu.add(Menu.NONE, MENU_QUIT, Menu.NONE,
                "Quit");
        quitMenu.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
    } 

    protected void invite() {};

	@Override
	public boolean onMenuItemSelected(int panelId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_LOCAL_START:
			quickstart();
			break;
		case MENU_REMOTE_START:
			new HostDialog(this, "", DEFAULT_PORT);
			break;
		case MENU_INVITE:
			// TODO: alert("Server status:", ds.test());
			invite();
			break;
		case MENU_ABOUT:
			// alert("About Androminion:", "It's awesome.");
			new AboutDialog(this);
			break;
        case MENU_ACHIEVEMENTS:
            new AchievementsDialog(this);
            break;
        case MENU_STATS:
            new StatsDialog(this);
            break;
		case MENU_HELP:
			 // Uri uri = Uri.parse("http://android.mehtank.com/help.html");
			 // Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			 // startActivity(intent);				
			gt.showHelp(1);
			break;
        case MENU_MAIN:
            break;
        case MENU_RESTART:
            break;
		case MENU_QUIT:
			// TODO: alert("Disconnected! (Hopefully)", ds.test());
			onDestroy();
			break;
			
		case MENU_SETTINGS:
			// Launch Prefs activity
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			debug("Settings called");
			break;
		}
		return true;
	}

	long lastBackClick = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (getPref("exitonback") && keyCode == KeyEvent.KEYCODE_BACK) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			long now = System.currentTimeMillis();
			if (now - lastBackClick < 3000) // 3 seconds
				onDestroy();
			else {
				lastBackClick = now;
				if (!NOTOASTS) Toast.makeText(top, "Click again to quit.", Toast.LENGTH_SHORT)
						.show();
			}
			return true;
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
		System.exit(0);
	}

	public void addView(View v) {
		topView.addView(v);
	}

	public void nosplash() {
		if (splash != null)
			splash.setVisibility(LinearLayout.INVISIBLE);
	}

	public void splash() {
		if (splash != null)
			splash.setVisibility(LinearLayout.VISIBLE);
	}

	public static enum AlertType {
		CHAT, TURNBEGIN, SELECT, CLICK, LONGCLICK, FINAL,
	};

	public void alert(AlertType t) {
		if (!getPref("allvibeson"))
			return;
		
		switch (t) {
		case CHAT:
			if (getPref("chatvibeon"))
				v.vibrate(new long[] { 0, 40, 100, 40 }, -1);
			break;
		case TURNBEGIN:
			if (getPref("turnvibeon"))
				v.vibrate(new long[] { 0, 50, 20, 40, 20, 30 }, -1);
			break;
		case SELECT:
			if (getPref("actionvibeon"))
				v.vibrate(75);
			break;
		case CLICK:
			if (getPref("clickvibeon"))
				v.vibrate(20);
			break;
		case LONGCLICK:
			if (getPref("clickvibeon"))
				v.vibrate(40);
			break;
		case FINAL:
			if (getPref("gamevibeon"))
				v.vibrate(250);
			break;
		}
	}

	protected void alert(String title, String message) {
		new AlertDialog.Builder(this)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	static final int MESSAGE_EVENT = 0;

	@Override
	public void debug(String s) {
		if (DEBUGGING)
			System.err.println(s);
		// Log.w("Dominion", s);
	}

	@Override
	public boolean handle(Event e) {
		Message m = Message.obtain(mHandler, MESSAGE_EVENT, e);
		mHandler.sendMessage(m);
		return true;
	}

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what != MESSAGE_EVENT)
				return;
			Event e = (Event) msg.obj;
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

				topView.removeView(gt);
				gt = new GameTable(top);
				
				topView.removeView(splash);
				splash = new SplashView(top);
				
				FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.FILL_PARENT,
						FrameLayout.LayoutParams.FILL_PARENT);

				topView.addView(gt, p);
				topView.addView(splash, p);
				splash();
				
				gt.newGame(ng.cards, ng.players);
				gameRunning = true;
				
		        PreferenceManager.getDefaultSharedPreferences(top).registerOnSharedPreferenceChangeListener(gt);
				break;

			// during game
			case CHAT:
				alert(AlertType.CHAT);
				if (!NOTOASTS) Toast.makeText(top, e.s, Toast.LENGTH_LONG).show();
				break;

			case CARDOBTAINED:
				setStatus(e.setString(gt.cardObtained(e.i, e.s)));
				ack = true;
				break;

			case CARDTRASHED:
				setStatus(e.setString(gt.cardTrashed(e.i, e.s)));
				ack = true;
				break;

			case CARDREVEALED:
				setStatus(e.setString(gt.cardRevealed(e.i, e.s)));
				ack = true;
				break;

			case STATUS:
				setStatus(e);
				ack = true;
				break;

			case GETCARD:
				gt.selectCard(e.o.sco, e.s, e.i, e.b);
				break;

			case GETSTRING:
				gt.selectString(e.s, e.o.ss);
				break;

			case ORDERCARDS:
				gt.orderCards(e.s, e.o.is);
				break;

			// FROM DIALOGS --
			case HELLO:
				startGame(port);
				break;

			case JOINGAME:
				name = e.s;
				startGame(e.i);
				break;

			case SETHOST:
				if (e.s != null)
					host = e.s;
				if (e.i > 0)
					port = e.i;
				startGame(port);
				break;

			case STARTGAME:
				if (start(port))
					put(e);
				break;

			// FROM GAME TABLE --
			case SAY:
			case STRING:
			case CARD:
			case CARDORDER:
				put(e);
				break;

			case ACHIEVEMENT:
			    gt.achieved(e.s);
			    ack = true;
			    break;
			    
			case DEBUG:
				debug(e.s);
				break;

			case DISCONNECT:
				if (!gotQuit)
					lostConnection();
				break;

			case QUIT:
				nag(e.s);
				gotQuit = true;
				disconnect();
			}
			if (ack)
				put(new Event(EType.Success));
		}

		private void setStatus(Event e) {
			GameStatus gs = e.o.gs;
			gt.setStatus(gs, e.s, e.b);
		}
	};

	private void disconnect() {
		if (comm != null)
			comm.stop();
		comm = null;
		commThread = null;
		gameRunning = false;
	}

	protected void nag(String s) {
		alert("", s);
		// new BuyDialog(this, s);
	}

	private boolean connect(int p) {
		disconnect();
		gotQuit = false;
		comm = new Comms(this, host, p);
		try {
			comm.connect();
			debug("New Comms connected to " + host + " on port " + port);
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
		try {
			comm.put(e);
		} catch (Exception e1) {
			lostConnection();
		}
	}

	private boolean start(int p) {
		if (!connect(p))
			return false;

		commThread = new Thread(comm);
		commThread.start();
		return true;
	}

	private void saveHostPort() {
		SharedPreferences prefs;
		prefs = PreferenceManager.getDefaultSharedPreferences(top);
		
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString("host", host);
		edit.putInt("port", port);
		edit.commit();
	}
	
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
			serverName = name;
			serverHost = getLocalIpAddress();
			serverPort = DEFAULT_PORT;
			
			put(new Event(EType.HELLO).setString(name));
			Event e = comm.get();
			if (e == null)
				throw (new IOException());

			if ((e.t == EType.GAMESTATS) || (e.t == EType.NEWGAME))
				handle(e);
			else
				throw (new IOException());

			commThread = new Thread(comm);
			commThread.start();
		} catch (IOException e) {
			alert("Connection failed!", "Invalid Data.");
			e.printStackTrace();
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

	protected void handshake(Event e) {
        if (e.b)
            new JoinGameDialog(top, e);
        else
        	startNewGame(e);
	}

	protected void startNewGame(Event e) {
//		if (!NOTOASTS) Toast.makeText(top, "Starting game...", Toast.LENGTH_SHORT).show();
//		handle(new Event(Event.EType.STARTGAME)
//			.setObject(new String[] { "Random", "Human Player",
//					"Drew (AI)", "Earl (AI)" }));

	    
	       new StartGameDialog(top, e, MULTIPLAYER, cardsPassedInExtras);
	}
}