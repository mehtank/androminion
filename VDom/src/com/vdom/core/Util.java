package com.vdom.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.Cards;
import com.vdom.api.GameEvent;
import com.vdom.api.InteractivePlayer;

public class Util {
    public static String cardArrayToString(Card[] cards) {
        String str = "";
        boolean first = true;
        for (Card card : cards) {
            if (first) {
                first = false;
            } else {
                str += ", ";
            }
            str += card.getName();
        }

        if (str.equals("")) {
            return "(empty)";
        }

        return str;
    }

    public static String cardArrayToString(CardList cards) {
        String str = "";
        boolean first = true;
        for (Card card : cards) {
            if (first) {
                first = false;
            } else {
                str += ", ";
            }
            str += card.getName();
        }

        if (str.equals("")) {
            return "(empty)";
        }

        return str;
    }
    
    public static void log(String s) {
        System.out.println(s);
    }

    public static void log(Throwable t) {
        t.printStackTrace();
    }

    /**
     * Player did something invalid.
     */
    public static void playerError(Player player, String err) {
        playerError(player, err, false);
    }

    /**
     * Player did something invalid.
     */
    public static void playerError(Player player, String err, boolean dumpStack) {
        if (!Game.ignoreAllPlayerErrors) {
            if (Game.ignoreSomePlayerErrors) {
                if (Game.ignoreList.contains(player.getPlayerName())) {
                    return;
                }
            }

            log(player.getPlayerName() + ":ERROR: " + err);
            if (dumpStack) {
                Thread.dumpStack();
            }
        }
    }

    /**
     * Player did something invalid.
     */
    public static void playerError(Player player, Throwable t) {
        if (!Game.ignoreAllPlayerErrors) {
            if (Game.ignoreSomePlayerErrors) {
                if (Game.ignoreList.contains(player.getPlayerName())) {
                    return;
                }
            }

            log(player.getPlayerName() + ":ERROR: " + "Exception during player call");
            t.printStackTrace();
        }

    }

    /**
     * Print out a message prefixed by the player's name if in debug mode.
     */
    public static void debug(Player player, String msg) {
        debug(player, msg, false);
    }

    /**
     * Print out a message prefixed by the player's name if either in debug mode, or in interactive mode and
     * interactiveAsWell is true. As with debug(msg, showInteractive), this is not always a "debug" message,
     * but it still seems to make sense to use the term.
     */
    public static void debug(Player player, String msg, boolean interactiveAsWell) {
        debug(player.getPlayerName() + ":" + msg, interactiveAsWell);
    }

    /**
     * Print out a message if in debug mode.
     */
    public static void debug(String msg) {
        debug(msg, false);
    }

    /**
     * Print out a message if either in debug mode, or in interactive mode and interactiveAsWell is true. This
     * is not always a "debug" message, but it still seems to make sense to use the term.
     */
    public static void debug(String msg, boolean interactiveAsWell) {
        if (Game.debug || (interactiveAsWell && Game.interactive)) {
            log(msg);
        }
    }

    /**
     * Show a debug out only if the game is not being played interactively, or if the player is the current
     * player. Used for "private" information that an interactive player needs to know about for themselves,
     * but shouldn't know about other players.
     */
    public static void sensitiveDebug(Player player, String msg, boolean interactiveAsWell) {
        if (!Game.interactive || (player == Game.players[Game.playersTurn] && player instanceof InteractivePlayer)) {
            debug(player, msg, interactiveAsWell);
        }
    }

    /**
     * Make the user hit enter. If they enter a dot, then show the current game state and card details. Used
     * when playing in interactive mode.
     */
    static void hitEnter(MoveContext context) {
        boolean valid = false;

        while (!valid) {
            valid = true;
            boolean dumpState = false;
            System.out.print("Hit enter >");
            try {
                do {
                    int input = System.in.read();
                    if (input == '.') {
                        dumpState = true;
                    }
                    if (input == '`') {
                        System.exit(0);
                    }
                } while (System.in.available() != 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (dumpState && context != null) {
                valid = false;
                dumpGameState(context);
            }
        }
    }

    public static String getInput(MoveContext context, Player player) {

        String choice = "";

        if(context != null && player != null) {
            System.out.println();
            if(!player.getPlayerName().equals("You")) {
                System.out.println("<" + player.getPlayerName() + "> ");
            }
            if(player instanceof InteractivePlayer && ((InteractivePlayer) player).quickPlay) {
                System.out.print("(Quick Play) ");
            }
            System.out.println("Hand:" + cardArrayToString(player.getHand()));
    
            int gold = context.getCoinAvailableForBuy();
            int tr = context.getThroneRoomsInEffect();
            System.out.print("Actions:" + context.getActionsLeft() + " Buys:" + context.getBuysLeft() + " Gold:" + gold + ((tr > 0) ? " Throne rooms:" + tr : "")
                + " >");
        }
        else {
            System.out.print(">");
        }

        choice = readString();
        if (choice.startsWith(".") && context != null && player != null) {
            dumpGameState(context);
            hitEnter(context);
            return null;
        }

        if (choice.startsWith("/") && context != null && player != null) {
            InteractivePlayer ip = (InteractivePlayer) player;
            ip.quickPlay = !ip.quickPlay;
            System.out.println();
            if(ip.quickPlay) {
                System.out.println("Quick Play is on (takes effect next turn)");
            }
            else {
                System.out.println("Quick Play is off");
            }
            return null;
        }
        
        if (choice.startsWith("`")) {
            System.exit(0);
        }

        return choice;
    }

    protected static String readString() {
        StringBuilder sb = new StringBuilder();
        try {
            do {
                int input = System.in.read();
                if (input != -1 && input != 10 && input != 13) {
                    sb.append((char) input);
                }
            } while (System.in.available() != 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static void dumpGameState(MoveContext context) {
        Player player = context.getPlayer();
        log("");

        // Card[] cards = new Card[] { Cards.estate, Cards.duchy, Cards.province, Cards.curse };
        // for (Card card : cards) {
        // log("" + context.getCardsLeft(card) + ":" + card.getName() + " (" +
        // card.getCost() + ")");
        // }
        //
        // cards = context.getActionsInPlay();
        // int cost = 0;
        // while (cost < 10) {
        // for (Card card : cards) {
        // if (card.getCost() == cost) {
        // log("" + context.getCardsLeft(card) + ":" + card.getName() + " (" +
        // card.getCost() + ")");
        // }
        // }
        //
        // cost++;
        // }

        Card[] cards = context.getCardsInPlay();
        int cost = 0;
        while (cost < 10) {
            for (Card card : cards) {
                if (card.getCost() == cost) {
                    log("" + context.getCardsLeft(card) + ":" + getLongText(card));
                    log("");
                }
            }

            cost++;
        }
        log("");
        log("Deck:" + player.getDeckSize() + " PirateShip:" + player.getPirateShipTreasure() + " NativeVillage:" + cardArrayToString(player.getNativeVillage())
            + " Island:" + cardArrayToString(player.getIsland()));
        log("");
    }

    public static String getLongText(Card card) {
        String cardText = card.getName() + " " + card.getStats();
        String descr = card.getDescription();
        if (descr != null && !descr.equals("")) {
            cardText += " \"" + descr + "\"";
        }

        return cardText;
    }

    public static String getShortText(Card card) {
        StringBuilder cardText = new StringBuilder();
        cardText.append(card.getName());
        int padding = CardImpl.maxNameLen - card.getName().length();
        for (int i = 0; i < padding; i++) {
            cardText.append(" ");
        }
        cardText.append("\t");
        cardText.append(card.getStats());

        String descr = card.getDescription();
        if (descr != null && !descr.equals("")) {
            cardText.append(" (...)");
        }

        return cardText.toString();
    }

    static boolean isDefendedFromAttack(Game game, Player player, Card responsible) {
        Card card = null;
        boolean defended = false;

        doSecretChamber(game, player, responsible);
        doHorseTraders(game, player, responsible);

        if (game.hasLighthouse(player)) {
            card = Cards.lighthouse;
            defended = true;
        } else if (game.hasMoat(player)) {
            card = Cards.moat;
            defended = true;
        }

        if (defended) {
            GameEvent event = new GameEvent(GameEvent.Type.PlayerDefended, new MoveContext(game, player));
            event.card = card;
            game.broadcastEvent(event);
        }

        return defended;
    }

    static boolean doSecretChamber(Game game, Player player, Card responsible) {

        boolean found = false;
        for (Card card : player.hand) {
            if (card.equals(Cards.secretChamber)) {
                found = true;
            }
        }

        if (found) {
            MoveContext context = new MoveContext(game, player);
            GameEvent event = new GameEvent(GameEvent.Type.PlayingAction, context);
            event.card = Cards.secretChamber;
            game.broadcastEvent(event);

            event = new GameEvent(GameEvent.Type.CardRevealed, context);
            event.card = Cards.secretChamber;
            game.broadcastEvent(event);

            game.drawToHand(player, responsible);
            game.drawToHand(player, responsible);

            if (player.hand.size() > 0) {
                Card[] cards = player.secretChamber_cardsToPutOnDeck(context);
                boolean bad = false;
                if (cards == null || cards.length > 2 || (cards.length < 2 && cards.length != player.hand.size())) {
                    bad = true;
                } else {
                    ArrayList<Card> copy = copy(player.hand);
                    for (Card card : cards) {
                        if (card == null || !copy.remove(card)) {
                            bad = true;
                        }
                    }
                }

                if (bad) {
                    playerError(player, "Secret Chamber cards to put on deck error, putting first two cards in hand back.", false);
                    if (player.hand.size() < 2) {
                        cards = new Card[player.hand.size()];
                    } else {
                        cards = new Card[2];
                    }

                    for (int i = 0; i < cards.length; i++) {
                        cards[i] = player.hand.get(i);
                    }
                }

                for (int i = cards.length - 1; i >= 0; i--) {
                    player.putOnTopOfDeck(cards[i]);
                    player.hand.remove(cards[i]);
                }
            }
        }

        return found;
    }

    static boolean doHorseTraders(Game game, Player player, Card responsible) {

        Card horseTraders = null;
        for (Card card : player.hand) {
            if (card.equals(Cards.horseTraders)) {
                horseTraders = card;
            }
        }

        if (horseTraders != null) {
            MoveContext context = new MoveContext(game, player);
            GameEvent event = new GameEvent(GameEvent.Type.PlayingAction, context);
            event.card = Cards.horseTraders;
            game.broadcastEvent(event);

            event = new GameEvent(GameEvent.Type.CardRevealed, context);
            event.card = Cards.horseTraders;
            game.broadcastEvent(event);

            player.hand.remove(horseTraders);
            player.horseTraders.add(horseTraders);

            return true;
        }

        return false;
    }

    static ArrayList<Card> copy(CardList cards) {
        if (cards == null) {
            return null;
        }

        ArrayList<Card> copy = new ArrayList<Card>();
        for (Card card : cards) {
            copy.add(card);
        }

        return copy;
    }

    public static int getCardCount(CardList cards, Card card) {
        int count = 0;
        for (Card thisCard : cards) {
            if (thisCard.equals(card)) {
                count++;
            }
        }

        return count;
    }

    public static int getCardCount(ArrayList<Card> cards, Card card) {
        int count = 0;
        for (Card thisCard : cards) {
            if (thisCard.equals(card)) {
                count++;
            }
        }

        return count;
    }
    
    public static Card getLeastExpensiveCard(Card[] cards) {
        if (cards == null || cards.length == 0) {
            return null;
        }

        Arrays.sort(cards, new CardCostComparator());
        return cards[cards.length - 1];
    }

    public static Card getMostExpensiveCard(Card[] cards) {
        if (cards == null || cards.length == 0) {
            return null;
        }

        Arrays.sort(cards, new CardCostComparator());
        return cards[0];
    }
    
    public static Card randomCard(ArrayList<Card> list) {
        if(list == null || list.size() == 0) {
            return null;
        }
        return list.get(Game.rand.nextInt(list.size()));
    }

    public static Card randomCard(CardList list) {
        if(list == null || list.size() == 0) {
            return null;
        }
        return list.get(Game.rand.nextInt(list.size()));
    }
    
    public static Card randomCard(Card[] list) {
        if(list == null || list.length == 0) {
            return null;
        }
        return list[Game.rand.nextInt(list.length)];
    }
}
