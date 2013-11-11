package com.mehtank.androminion.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import com.mehtank.androminion.ui.Strings;
import com.vdom.api.GameType;
import com.vdom.comms.Comms;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EType;
import com.vdom.comms.Event.EventObject;
import com.vdom.comms.EventHandler;
import com.vdom.core.ExitException;
import com.vdom.core.Game;
import com.vdom.core.Player;

public class VDomServer implements EventHandler {
    @SuppressWarnings("unused")
    private static final String TAG = "VDomServer";

    private final boolean DEBUGGING = true;

    private CountDownLatch waitingPlayers = new CountDownLatch(0);

    /**
     * This class will be run when a new game was created,
     * it is given all the arguments from the game preceded with a -
     * as well as all the player names
     *
     */

    private class GameStarter implements Runnable {
        @SuppressWarnings("unused")
        private static final String TAG = "GameStarter";

        private String[] args;
        public GameStarter(String[] args) {
            this.args = args;
        }
        @Override
        public void run() {
            isRunning = true;
            try {
                Game.go(args, false); // don't call main(), which is only for commandline calling and catches the ExitException which /we want to handle here/.
            } catch (ExitException e) {
                debug("Game exception!");
                e.printStackTrace();
                //clean = "ExitException in Game.java\n" + e.toString();
            }
            debug("Game ended!");
            isRunning = false;
            endGame();
        }
    }

    static final String remotePlayerString = "Human Player";
    /**
     * List of all players name => classname
     */
    static final HashMap<String, String> allPlayers = new HashMap<String, String> ();
    static {
        allPlayers.put(remotePlayerString, "com.mehtank.androminion.server.RemotePlayer");
        // allPlayers.put("Drew's VDom player (AI)", "com.vdom.drew.VDomPlayer@http://www.delvegames.com/DrewsVDomPlayer.jar");
        // allPlayers.put("Best yet (AI)", "net.spack.vdom.BestYet@http://earlcahill.com/myVdom.jar");
    };

    public static VDomServer me;  // will be the only instance of this class
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

    @Override
    public void debug(String str) {
        if (DEBUGGING)
            System.out.println(str);
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

    /**
     * initialize one instance of VDomServer.
     *
     * This one instance is stored in 'me'. Also
     * run me.start().
     *
     * @param args list of players 'name' 'classname'
     */
    public static void main(String[] args) {
        ArrayList<String> gameArray = new ArrayList<String>();  //all the card collection names AND ai players

        if (args != null)
            for (int i=0; i<args.length-1; i += 2)
                allPlayers.put(args[i], args[i+1]);

        if (args[args.length-1].endsWith("debug"))
            debugOutput = true;

        for (GameType g : GameType.values())
            gameArray.add(Strings.getGameTypeName(g));
        numGameTypes = gameArray.size();
        Collections.sort(gameArray);

        gameArray.addAll(allPlayers.keySet());  // names of the ai players
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
            System.exit(-1); // TODO: This is not the android way
        }
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

    /**
     * returns game stats as an event
     * is sent as a reply to HELLO
     * @return
     */
    private Event gameStats() {
        Event e = new Event(EType.GAMESTATS)
                .setBoolean(isStarted);

        if (isStarted) {
            //    while (!isRunning); // WE SOLVE THIS WITH THE COUNTDOWN LATCH
            while (true) {
                try {
                    waitingPlayers.await();
                    break;
                } catch (InterruptedException ie) {}
            }
            int currentHuman = 0;
            ArrayList<String> runningStrings = new ArrayList<String>();

            for (String s : gamePlayers) {  // We infer gamePlayers from the Game.java-command line arguments we received via the STARTGAME event.
                if (s.equals(remotePlayerString)) {   // the player is human
                    //while (remotePlayers.size() <= currentHuman); // WE SOLVE THIS WITH THE COUNTDOWN LATCH
                    RemotePlayer rp = remotePlayers.get(currentHuman++);
                    String name = "Human player";
                    if (rp.getPlayerName() != "")
                        name += ": " + rp.getPlayerName(); // name is now "Human player: <chosen player name>"
                    if (rp.hasJoined())
                        runningStrings.add(name + " (playing)");
                    else if (rp.getPort() > 0)
                        runningStrings.add(name + " (seat open)||" + rp.getPort());
                    else
                        runningStrings.add(name + " (not connected)");

                } else
                    runningStrings.add(s);
            }
            // runningStrings: list of strings "Human player: <player name> (playing|seat open|not connected)"
            // (not connected) happens only in race conditions, since the RemotePlayer thread sets its port shortly after setting
            e.setString(gameType)
                    .setObject(new EventObject(runningStrings.toArray(new String[0])));
        } else
            e.setInteger(numGameTypes)
                    .setObject(new EventObject(gameStrings));
        return e;
    }

    /**
     * Gets executed in response to the STARTGAME event
     * runs Game.main(args) as a new thread
     * @param args the event's eventobject string list
     * args[0]: gameType
     */
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
        waitingPlayers = new CountDownLatch(1); // TODO: how many humans are there?
        isStarted = true;
        gt = new Thread(new GameStarter(gameArgs.toArray(new String[0])));
        gt.start();
        //    try {
        //        Thread.sleep(1000); // Made obsolete by CountDownLatch
        //    } catch (InterruptedException e) {}
    }

    public void registerRemotePlayer(RemotePlayer rp) {
        remotePlayers.add(rp);
        waitingPlayers.countDown();
    }
    @Override
    public boolean handle(Event e) {
        boolean reconnect = true;
        switch (e.t) {
            case STARTGAME:
                startGame(e.o.ss);  // execute Game.main()
                //$FALL-THROUGH$
                // !!!!!!!! NO BREAK !!!!!!!!!!!
            case HELLO:
                /*
                 * The following try/catch block is made obsolete by the addition of sendErrorHandler.
                 */
                //            try {
                comm.put_ts(gameStats());
                reconnect = false;
                //            } catch (IOException e1) {
                //                debug("Error sending game stats, restarting server.");
                //            }
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

    @Override
    public void sendErrorHandler(Exception e) {
        debug("Error while sending something; restarting server.");
        reconnect(); // Yes, this is thread-safe, so it may be executed from here, even though
        // sendErrorHandler is executed in a different thread from the rest.
    }

    public void endGame() {
        if (isStarted) {
            for (RemotePlayer rp : remotePlayers) {
                try {
                    rp.sendQuit();
                } catch (Exception e) {
                    // whatever
                }
            }

            if (isRunning) {
                remotePlayers.get(0).kill_game(); // What we actually /want/ to do here: kill the vdom-thread.
            }
            gamePlayers.clear();
            remotePlayers.clear();
            gt = null;

            isStarted = false;
        }
    }

    public void quit() {
        endGame();
        disconnect();
    }
    public void say(String string) {
        for (RemotePlayer rp : remotePlayers) {
            /*
             * The following try/catch block is made obsolete by the addition of
             * sendErrorHandler to the EventHandler interface.
             */
            //            try {
            rp.comm.put_ts(new Event(Event.EType.CHAT).setString(string));
            //            } catch (Exception e) {
            //                // whatever
            //            }
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
