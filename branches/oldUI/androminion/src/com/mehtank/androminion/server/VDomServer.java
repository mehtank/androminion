package com.mehtank.androminion.server;

import java.io.IOException;
import java.util.*;

import com.vdom.api.GameType;
import com.vdom.comms.Comms;
import com.vdom.comms.Event;
import com.vdom.comms.EventHandler;
import com.vdom.comms.Event.EType;
import com.vdom.comms.Event.EventObject;
import com.vdom.core.Game;
import com.vdom.core.Player;

public class VDomServer implements EventHandler {

	private class GameStarter implements Runnable {
	    
		private String[] args;
		public GameStarter(String[] args) {
			this.args = args;
		}
		@Override 
		public void run() {
			String clean = "Game over!";
			isRunning = true;
			try {
				Game.main(args);
			} catch (Exception e) {
				debug("Game exception!");
				e.printStackTrace();
				clean = e.toString();
			}
			debug("Game ended!");
			isRunning = false;
			endGame(clean);
		}
	}

	static final String remotePlayerString = "Human Player";
	static final HashMap<String, String> allPlayers = new HashMap<String, String> ();
	static {
		allPlayers.put(remotePlayerString, "com.mehtank.androminion.server.RemotePlayer");
		// allPlayers.put("Drew's VDom player (AI)", "com.vdom.drew.VDomPlayer@http://www.delvegames.com/DrewsVDomPlayer.jar");
		// allPlayers.put("Best yet (AI)", "net.spack.vdom.BestYet@http://earlcahill.com/myVdom.jar");
	};

	public static VDomServer me;
	public static int maxPause = 300000; // 5 minutes in ms
	static int numGameTypes = 0;
	static String[] gameStrings;
	static boolean debugOutput = false;
		
	String gameType;
	ArrayList<String> gamePlayers = new ArrayList<String>();
	ArrayList<RemotePlayer> remotePlayers = new ArrayList<RemotePlayer>();
	
	Thread gt;
	boolean isStarted = false;
	boolean isRunning = false;
	Comms comm;
	Thread commThread;
	
	public void debug(String str) {
		// System.out.println(str);
	}
	public void error(String str) {
		// System.err.println(str);
	}

	public int getPort() {
		if (comm == null)
			return 0;
		return comm.getPort();
	}
	public String getHost() {
		if (comm == null)
			return null;
		return comm.getHost();
	}

	public static void main(String[] args) {
		ArrayList<String> gameArray = new ArrayList<String>();
		
		if (args != null)
			for (int i=0; i<args.length-1; i += 2)
				allPlayers.put(args[i], args[i+1]);
		
		if (args[args.length-1].endsWith("debug"))
			debugOutput = true;
		
		for (GameType g : GameType.values())
			gameArray.add(Strings.getGameTypeName(g));
		numGameTypes = gameArray.size();
		Collections.sort(gameArray);
		
		gameArray.addAll(allPlayers.keySet());
		gameStrings = gameArray.toArray(new String[0]);

		me = new VDomServer();
		me.start();
	}

	private void start() {
		RemotePlayer.setVdomserver(this);
		connect();
	}	
	private void connect() {
		try {
			comm = new Comms(this, 1251);
		} catch (IOException e) {
			e.printStackTrace();
			error("Could not start server!");
			System.exit(-1);
		}
		commThread = new Thread(comm);
		commThread.start();
	}
	private void disconnect() {
		if (comm == null) {
			start();
			return;
		}
		
		comm.stop();
		comm = null;
		commThread = null;
	}
	private void reconnect() {
		disconnect();
		connect();
	}

	private Event gameStats() {
		Event e = new Event(EType.GAMESTATS)
			.setBoolean(isStarted);
		
		if (isStarted) {
			while (!isRunning);
			int currentHuman = 0;
			ArrayList<String> runningStrings = new ArrayList<String>();
			
			for (String s : gamePlayers) {
				if (s.equals(remotePlayerString)) {
					while (remotePlayers.size() <= currentHuman);
					RemotePlayer rp = remotePlayers.get(currentHuman++);
					String name = "Human player";
					if (rp.getPlayerName() != "")
						name += ": " + rp.getPlayerName();
					if (rp.hasJoined()) 
						runningStrings.add(name + " (playing)");
					else if (rp.getPort() > 0) 
						runningStrings.add(name + " (seat open)||" + rp.getPort());
					else 
						runningStrings.add(name + " (not connected)");

				} else
					runningStrings.add(s);
			}
			e.setString(gameType)
			 .setObject(new EventObject(runningStrings.toArray(new String[0])));
		} else
			e.setInteger(numGameTypes)
			 .setObject(new EventObject(gameStrings));
		return e;
	}
	private void startGame(String[] args) {
		/*
		Cards cs = new Cards();
		int cardNum = 0;
		for (Field f : cs.getClass().getFields()) {
			try {
				Object o = f.get(null);
				if (o instanceof Card) {
					Card c = (Card) o;
					System.out.println(c.getName());
					comm.send(RemotePlayer.cardPacket(c, cardNum++));
				}
			} catch (Exception e) {}
		}
		*/
		gamePlayers.clear();
		remotePlayers.clear();
		
		ArrayList<String> gameArgs = new ArrayList<String>();
		
		gameType = args[0];
		gameArgs.add("-type" + args[0]);
		if (debugOutput)
			gameArgs.add("-debug");
		
		for (int i=1; i < args.length; i++) {
		    if(!args[i].startsWith("-")) {
				if(args[i].equalsIgnoreCase(Player.RANDOM_AI)) {
					args[i] = this.getRandomAI(args);
				}

				gamePlayers.add(args[i]);

    			gameArgs.add(allPlayers.get(args[i]));
		    } else {
	              gameArgs.add(args[i]);
		    }
		}
		
		isStarted = true;
		gt = new Thread(new GameStarter(gameArgs.toArray(new String[0])));
		gt.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
	}
	
	public void registerRemotePlayer(RemotePlayer rp) {
		remotePlayers.add(rp);
	}
	@Override
	public boolean handle(Event e) {
		boolean reconnect = true;
		switch (e.t) {
        case STARTGAME:
            startGame(e.o.ss);
            
        case HELLO:
			try {
				comm.put(gameStats());
				reconnect = false;
			} catch (IOException e1) {
				debug("Error sending game stats, restarting server.");
			}
			break;
			
        case DISCONNECT:
        	reconnect = true;
        	break;
        	
        default:
        	reconnect = false;
		}
		if (!reconnect) 
			return true;
		
		reconnect();
		return true;
	}
	
	public void endGame(String s) {
		if (isStarted) {
			for (RemotePlayer rp : remotePlayers) {
				try {
					rp.sendQuit(s);
				} catch (Exception e) {
					// whatever
				}
			}
			
			if (isRunning)
				remotePlayers.get(0).die();
			gamePlayers.clear();
			remotePlayers.clear();
			gt = null;
			
			isStarted = false;
		}
	}
	
	public void quit() {
		endGame("Server killed game.");
		disconnect();
	}
	public void say(String string) {
		for (RemotePlayer rp : remotePlayers) {
			try {
				rp.comm.put(new Event(Event.EType.CHAT).setString(string));
			} catch (Exception e) {
				// whatever
			}
		}		
	}
	
	public String getRandomAI(final String[] args) {
		final List<String> playersInGame = new ArrayList<String>();
		final List<String> randomPlayers = new ArrayList<String>();
		
		for(String arg : args) {
			if(arg.contains(" (AI)")) {
				playersInGame.add(arg);
			}
		}
		
		for(String player : allPlayers.keySet()) {
			if(player.contains(" (AI)") && !playersInGame.contains(player)) {
				randomPlayers.add(player);
			}
		}
		
		final Random rand = new Random(System.currentTimeMillis());

		return randomPlayers.get(rand.nextInt(randomPlayers.size()));
	}

}
