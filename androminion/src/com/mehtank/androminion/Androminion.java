package com.mehtank.androminion;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mehtank.androminion.activities.AboutActivity;
import com.mehtank.androminion.activities.CombinedStatsActivity;
import com.mehtank.androminion.activities.SettingsActivity;
import com.mehtank.androminion.activities.StartGameActivity;
import com.mehtank.androminion.ui.GameTable;
import com.mehtank.androminion.ui.HostDialog;
import com.mehtank.androminion.ui.JoinGameDialog;
import com.mehtank.androminion.util.HapticFeedback;
import com.mehtank.androminion.util.HapticFeedback.AlertType;
import com.vdom.comms.Comms;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EType;
import com.vdom.comms.Event.EventObject;
import com.vdom.comms.EventHandler;
import com.vdom.comms.GameStatus;
import com.vdom.comms.MyCard;
import com.vdom.comms.NewGame;
import com.vdom.core.Game;

public class Androminion extends Activity implements EventHandler {
	private static final boolean DEBUGGING = false;
	public static boolean NOTOASTS = false;

	static final boolean MULTIPLAYER = false;

	String[] cardsPassedInExtras;
	
	protected Androminion top = this;
	
	FrameLayout topView;

	GameTable gt;
	View splash;

	boolean gameRunning = false;
	long lastBackClick = 0;

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

		splash = getLayoutInflater().inflate(R.layout.splashview, null);
		topView.addView(splash);

		setContentView(topView);

		prefs = PreferenceManager.getDefaultSharedPreferences(top);
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
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
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
		switch (item.getItemId()) {
		case R.id.startgame_menu:
			quickstart();
			return true;
		case R.id.help_menu:
			gt.showHelp(1);
			break;
		case R.id.settings_menu:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.about_menu:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
        case R.id.stats_menu:
        	startActivity(new Intent(this, CombinedStatsActivity.class));
        	return true;
		case R.id.quit_menu:
			finish();
			return true;
/*        case MENU_MAIN:
            break;
        case MENU_RESTART:
            break;
		case MENU_REMOTE_START:
			new HostDialog(this, "", DEFAULT_PORT);
			break;
		case MENU_INVITE:
			// TODO: alert("Server status:", ds.test());
			invite();
			break;*/
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
    				if (!NOTOASTS) Toast.makeText(top, getString(R.string.toast_quitconfirm), Toast.LENGTH_SHORT)
    						.show();
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
			splash.setVisibility(LinearLayout.INVISIBLE);
	}

	public void splash() {
		if (splash != null)
			splash.setVisibility(LinearLayout.VISIBLE);
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
	public static final String BASEDIR = Environment.getExternalStorageDirectory().getPath() + "/Androminion";

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

				int index = topView.indexOfChild(gt);
				topView.removeView(gt);
				gt = new GameTable(top);
				
				FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.FILL_PARENT,
						FrameLayout.LayoutParams.FILL_PARENT);

				topView.addView(gt, index, p);
				splash();
				
				saveLastCards(ng.cards);
				gt.newGame(ng.cards, ng.players);
				gameRunning = true;
				
		        PreferenceManager.getDefaultSharedPreferences(top).registerOnSharedPreferenceChangeListener(gt);
				break;

			// during game
			case CHAT:
				HapticFeedback.vibrate(top, AlertType.CHAT);
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
		commThread = null;
		gameRunning = false;
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
			
			put( new Event( EType.HELLO ).setString( name ) );
            final Event e = comm.get();
            if (e == null) {
                throw new IOException( "No response received. Expected " + EType.GAMESTATS + " or " + EType.NEWGAME );
            }

            if (e.t == EType.GAMESTATS || e.t == EType.NEWGAME) {
                handle( e );
            }
            else {
                throw new IOException( "Unknown response received. Event was " + e );
            }

            commThread = new Thread( comm );
            commThread.start();
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
			if (!Androminion.NOTOASTS) Toast.makeText(top, top.getString(R.string.toast_starting), Toast.LENGTH_SHORT).show();
		}
	}
}