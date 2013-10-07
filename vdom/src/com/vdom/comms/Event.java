package com.vdom.comms;

import java.io.Serializable;

import com.vdom.api.Card;


/**
 * Event class: This object makes up the communication protocol between RemotePlayer, running in vdom, and the game activity. <br>
 * The content is:
 * <ul>
 * <li><b>t</b>: Type of the event, determines meaning of other class members.</li>
 * <li><b>s</b>: String</li>
 * <li><b>b</b>: boolean</li>
 * <li><b>i</b>: int</li>
 * <li><b>o</b>: EventObject</li>
 * </ul>
 * The EventObject in turn contains:
 * <ul>
 * <li><b>gs</b>: GameStatus - <i>Contains all the information a player sees at a point in time. Whose turn it is, number of cards out etc.</i> </li>
 * <li><b>ss</b>: String[]</li>
 * <li><b>is</b>: int[]</li>
 * <li><b>ng</b>: NewGame - <i>Information about a game setup: Name of players, card piles used</i></li>
 * <li><b>sco</b>: SelectCardOptions - <i>Information about what card has to be picked, and where</i></li>
 * </ul>
 */

public class Event implements Serializable{
    private static final long serialVersionUID = -590316466543954582L;

    public static class EventObject implements Serializable {
        private static final long serialVersionUID = 6832528588406201248L;

        public GameStatus gs; // game status
        public String[] ss; // description (?)
        public Card[] cs; // for choosing from a set of cards
        public int[] is;
        public Object[] os;
        public NewGame ng; // new game
        public SelectCardOptions sco; // select card options

        // some setters


        public EventObject(GameStatus o) {
            this.gs = o;
        }
        public EventObject(String[] o) {
            this.ss = o;
        }
        public EventObject(Card[] o) {
            this.cs = o;
        }
        public EventObject(int[] o) {
            this.is = o;
        }
        public EventObject(NewGame o) {
            this.ng = o;
        }
        public EventObject(SelectCardOptions o) {
            this.sco = o;
        }
        // A little ugly, but this is so we can put an enum in here without having to
        // handle lots of different enum types.  TODO(matt): maybe move all of the card
        // option enums into a single class, so we can accept that as a parameter instead
        // of Object?
        public EventObject(Object[] o) {
            this.os = o;
        }
    }

    // all the possible events
    public enum EType {
        /**
         * GETSERVER
         *
         * Prompt the opposing side to send a SERVER message, containing its host and port. <br>
         *
         * Apparently this used to be used by GameActivity.
         */
        GETSERVER,
        /**
         * SERVER
         *
         * Reply to GETSERVER, generated inside Comms.java. Gives information about the senders hostname and port.<br>
         * Yes, this is pointless.
         *
         * @param s hostname
         * @param i port
         */
        SERVER,

        /**
         * HELLO
         * <p>
         * Sent from GameActivity to RemotePlayer upon connecting to it. VDomServer also handles it.
         * Would accept a NEWGAME or a GAMESTATS for an answer. <br>
         * VDomServer responds with GAMESTATS (and does nothing more) <br>
         * RemotePlayer responds with NEWGAME and updates its own player name.
         * </p><p>
         * GameActivity also handles this event and connects to the default VDomServer port, sending it a HELLO.
         * This event may be sent from JoinGameDialog.
         * </p>
         *
         * @param s Player name
         */
        HELLO,
        /**
         * GAMESTATS
         *
         * Sent from VDomServer upon receiving a HELLO or STARTGAME message.
         *
         * @param b True if the game has started, false otherwise.
         * @param s <i>Only if game is running</i>: Game type name
         * @param o Has its String[] set to available game types <i>if game is not running</i> or, <i>if the game is running</i>: "&lt;internal player name&gt;: &lt;chosen player name&gt; (&lt;status&gt;)
         * <br>Notes:
         * <ul>
         * <li>The way things are now, &lt;chosen player name&gt; will be 'null' or the name chosen for the last game, since RemotePlayer
         * learns its name only upon connecting.</li>
         * <li>&lt;status&gt; is one out of 'playing', 'seat open' and 'not connected'. 'not connected' would be the result of a race condition
         * (see comments in source in VDomServer's gameStats()). 'seat open' is followed by a ||&lt;RemotePlayer::getPort&gt;; <b>This is how
         * GameActivity learns about RemotePlayer's port!</b></li>
         * </ul>
         *
         */
        GAMESTATS,
        /**
         * NEWGAME
         *
         * Sent from RemotePLayer upon receiving HELLO from GameActivity.
         *
         * @param o Has its NewGame object set to information about the game which GameActivity has joined
         */
        NEWGAME,

        /**
         * STARTGAME
         *
         * GameActivity sends this to <i>itself</i> to cause it to connect to VDomServer. Then it sends it on to VDomServer.
         * VDomServer reacts to it by creating a vdom-thread
         *
         * @param o contains String[] of parameters that are sent to Game.java's go()
         *
         */
        STARTGAME,
        /**
         * JOINGAME
         *
         * <p>
         * Sent from JoinGameDialog to GameActivity and causes it to connect to a RemotePlayer (or, in principle, a
         * VDomServer would be possible).
         * </p><p>
         * GameActivity receives GAMESTATS from VDomServer, and with it a list of RemotePlayer instances it can connect to.
         * JoinGameDialog (Which the user only gets to see if there is actually a choice of player seats to join) generates
         * the information about available RemotePlayer instances.
         * </p>
         *
         * @param s Name the player will be using (is fetched from stored options or (?)prompted(?))
         * @param i Port of RemotePlayer instance to connect to
         */
        JOINGAME,

        /**
         * STATUS
         *
         * Sent from RemotePlayer to GameActivity, informing it about the current status of the game. It has the
         * EventObject::GameStatus parameter set. This is sent from RemotePlayer
         * <ol>
         * <li>Whenever a game-event happened (message from vdom), unless a CARDOBTAINED, CARDTRASHED, or CARDREVEALED is sent</li>
         * <li>Whenever GameActivity is asked for a response it is first updated about the game state with this</li>
         * </ol>
         * <p><b>needs EType.Success ack</b></p>
         *
         * @param o has its GameStatus parameter set
         * @param s not set most cases, unless a GameOver message was given from vdom for the first time. Then
         * it is a list of victory points and victory cards each player
         * @param b only true if TurnBegin message was given from vdom, or when GameOver was given for the first time. 
         */
        STATUS,

        /**
         * GETNAME
         *
         * NOT USED
         */
        GETNAME,
        /**
         * SETNAME
         *
         * NOT USED
         */
        SETNAME,
        /**
         * SETHOST
         *
         * Tells GameActivity to start a game with the given <i>RemotePlayer</i>-host.
         * Sent from HostDialog.java
         *
         * @param s host name
         * @param i port
         *
         */
        SETHOST,

        /**
         * GETCARD
         *
         * Sent from RemotePlayer when the player needs to choose cards. <p><b>needs EType.CARD response</b></p>
         *
         * @param i number of cards to pick
         * @param b True if this is the exact necessary number, False if the user may choose less
         * @param s Prompt to display
         * @param o has SelectCardOptions set
         */
        GETCARD,
        /**
         * CARD
         *
         * Sent from GameTable to GameActivity, which forwards it to RemotePlayer, as a response to GETCARD.
         *
         * @param i Number of cards selected. If this is 1 and o.is[0] is -1, it means 'all'
         * @param o Has int[] set to the indices of cards that were selected.
         */
        CARD,
        /**
         * GETOPTION
         *
         * Sent from RemotePlayer when the player needs to choose an option.
         * Meant to replace GETSTRING, which is too coupled to the android framework.
         * <p><b>needs EType.OPTION response</b></p>
         *
         * @param c Card requesting the option.
         * @param o has its Object[] set to the available options
         */
        GETOPTION,
        /**
         * OPTION
         *
         * Generic event returned by a client when selecting an option from an enum array.
         *
         * @param i is set to the index of the option selected.
         */
        OPTION,
        /**
         * GETSTRING
         *
         * Sent from RemotePlayer when the player needs to enter a string. This is the prompt of what option of
         * an action card to perform. <p><b>needs EType.STRING response</b></p>
         * @param s prompt to display
         * @param o has its String[] set to the available options
         */
        GETSTRING,
        /**
         * STRING
         *
         * Sent from SelectStringView to GameActivity, which forwards it to RemotePlayer, as a response to GetString.
         *
         * @param s The selected string.
         */
        STRING,
        /**
         * ORDERCARDS
         *
         * Sent from RemotePlayer when the player needs to put cards in an order (to put them on the deck mainly)
         * <p><b>needs EType.CARDORDER response</b></p>
         *
         * @param s prompt to display
         * @param o has its int[] set to the card indices (into gameTable.cardsInPlay) that are to be ordered.
         */
        ORDERCARDS,
        /**
         * CARDORDER
         *
         * Sent from OrderCardsView to GameActivity, which forwards it to RemotePlayer, as a response to ORDERCARDS.
         *
         * @param o Has int[] set to the indices (into gameTable.cardsInPlay) of cards in desired order.
         */
        CARDORDER,

        /**
         * CARDOBTAINED
         *
         * Sent from RemotePlayer to GameActivity when a new card was obtained.
         *
         * <p><b>needs EType.Success ack</b></p>
         *
         * @param o has its GameStatus parameter set
         * @param s player index number, converted to string
         * @param i card number
         */
        CARDOBTAINED,
        /**
         * CARDTRASHED
         *
         * Sent from RemotePlayer to GameActivity when a card was trashed.
         *
         * <p><b>needs EType.Success ack</b></p>
         *
         * @param o has its GameStatus parameter set
         * @param s player index number, converted to string
         * @param i card number
         */
        CARDTRASHED,
        /**
         * CARDREVEALED
         *
         * Sent from RemotePlayer to GameActivity when a card was revealed.
         *
         * <p><b>needs EType.Success ack</b></p>
         *
         * @param o has its GameStatus parameter set
         * @param s player index number, converted to string
         * @param i card number
         */
        CARDREVEALED,

        /**
         * SAY
         *
         * Broadcast a chat message.
         * When GameActivity receives this (from TalkView), it forwards it to RemotePlayer.
         * VDomServer then causes all remote players to send back a CHAT message.
         *
         * @param s message
         */
        SAY,
        /**
         * CHAT
         *
         * Sent to GameActivity to display a chat message.
         *
         * @params s message
         */
        CHAT,

        /**
         * Success
         *
         * Sent as ack-signal from GameActivity to RemotePlayer.
         */
        Success,
        /**
         * QUIT
         *
         * Sent from RemotePlayer to GameActivity when the game was quit. (Via function sendQuit() which is called
         * by RemotePlayer). This causes GameActivity to disconnect and ignore DISCONNECT messages until it
         * connects again with some other server.
         *
         * @param s Quit message from VDomServer + play duration
         */
        QUIT,

        /**
         * ACHIEVEMENT
         *
         * Sent from RemotePlayer to GameActivity upon receiving an achievement.
         *
         * @param o has its GameStatus parameter set
         * @param s achievement name
         *
         * <p><b>needs EType.Success ack</b></p>
         */
        ACHIEVEMENT,

        /**
         * DEBUG
         *
         * Handled by GameActivity
         *
         * @param s Debug message to be printed / logged / called debug() upon
         */
        DEBUG,
        /**
         * PING
         *
         * Request a PONG or disconnect. PING is caused by Comms.java whenever the receiving socket timed out (pretty often).
         * If the other party does not answer with PONG (this may be because of a race condition...), handle a received DISCONNECT event
         * and terminate the network threads.
         */
        PING,
        /**
         * PONG
         *
         * Comms.java replies this automatically to a PING message.
         */
        PONG,
        /**
         * DISCONNECT
         *
         * Sent from Comms.java when a PING failed. Causes GameActivity to display the "Connection Lost" message,
         * VDomServer to reset its listening port, and RemotePlayer to do nothing.
         */
        DISCONNECT,
        /**
         * KILLSENDER
         *
         * Used internally by Domms: causes the sendingThread to shut down and the receiving thread to return null once
         */
        KILLSENDER,
        /**
         * SLEEP
         *
         * Make the receiving thread sleep for the amount of time
         *
         * @param i time in ms
         */
        SLEEP,
    }
    public EType t; // event type
    public String s; // event string
    public Card c; // event card
    public boolean b; // some bool
    public int i; // some int
    public EventObject o; // reverence an event object (public child class)

    public Event(EType t) {
        this.t = t;
    }

    public Event setType(EType r) {
        this.t = r;
        return this;
    }
    public Event setString(String s) {
        this.s = s;
        return this;
    }
    public Event setCard(Card c) {
        this.c = c;
        return this;
    }
    public Event setBoolean(boolean b) {
        this.b = b;
        return this;
    }
    public Event setInteger(int i) {
        this.i = i;
        return this;
    }
    public Event setObject(EventObject o) {
        this.o = o;
        return this;
    }

    public String toString() {
        String str = t.toString();

        str += ", int = " + i;
        str += ", str =  " + s;
        str += ", bool = " + b;
        if (o != null)
            str += ", obj = " + o.toString();

        return str;
    }
}
