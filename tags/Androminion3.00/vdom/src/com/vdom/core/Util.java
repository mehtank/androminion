package com.vdom.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CurseCard;
import com.vdom.api.GameEvent;
import com.vdom.api.TreasureCard;
import com.vdom.core.Cards.Type;

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
        System.out.println("<VDOM CORE> " + s);
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
        if (Game.debug || Game.junit) {
            log(msg);
        }
    }

    /**
     * Show a debug out only if the game is not being played interactively, or if the player is the current
     * player. Used for "private" information that an interactive player needs to know about for themselves,
     * but shouldn't know about other players.
     */
    public static void sensitiveDebug(Player player, String msg, boolean interactiveAsWell) {
//        if (!Game.interactive || (player == Game.players[Game.playersTurn]) {
            debug(player, msg, interactiveAsWell);
//        }
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

        Card[] cards = context.getCardsInGame();
        int cost = 0;
        while (cost < 10) {
            for (Card card : cards) {
                if (card.getCost(context) == cost) {
                    log("" + context.getCardsLeftInPile(card) + ":" + getLongText(card));
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
        boolean defended = false;

        // TODO - pass some context about attack?
        MoveContext context = new MoveContext(game, player);

        if (game.hasLighthouse(player)) {
            defended = true;
            
            GameEvent event = new GameEvent(GameEvent.Type.PlayerDefended, context);
            event.card = Cards.lighthouse;
            game.broadcastEvent(event);
        }
        
        Card reactionCard = null;
        while ((reactionCard = player.getAttackReaction(context, responsible, defended, reactionCard)) != null) {
            GameEvent event = new GameEvent(GameEvent.Type.CardRevealed, context);
            event.card = reactionCard;
            game.broadcastEvent(event);

        	if (reactionCard.equals(Cards.secretChamber))
                doSecretChamber(context, game, player, responsible);
        	else if (reactionCard.equals(Cards.horseTraders))
                doHorseTraders(context, game, player, responsible);
        	else if (reactionCard.equals(Cards.moat)) {
                defended = true;
                
                event = new GameEvent(GameEvent.Type.PlayerDefended, context);
                event.card = reactionCard;
                game.broadcastEvent(event);
        	}
        }

        return defended;
    }

    static boolean doSecretChamber(MoveContext context, Game game, Player player, Card responsible) {

        boolean found = false;
        for (Card card : player.hand) {
            if (card.equals(Cards.secretChamber)) {
                found = true;
            }
        }

        if (found) {
//            GameEvent event = new GameEvent(GameEvent.Type.PlayingAction, context);
//            event.card = Cards.secretChamber;
//            game.broadcastEvent(event);
//
//            event = new GameEvent(GameEvent.Type.CardRevealed, context);
//            event.card = Cards.secretChamber;
//            game.broadcastEvent(event);

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

    static boolean doHorseTraders(MoveContext context, Game game, Player player, Card responsible) {

        Card horseTraders = null;
        for (Card card : player.hand) {
            if (card.equals(Cards.horseTraders)) {
                horseTraders = card;
            }
        }

        if (horseTraders != null) {
//            GameEvent event = new GameEvent(GameEvent.Type.PlayingAction, context);
//            event.card = Cards.horseTraders;
//            game.broadcastEvent(event);
//
//            event = new GameEvent(GameEvent.Type.CardRevealed, context);
//            event.card = Cards.horseTraders;
//            game.broadcastEvent(event);

            player.hand.remove(horseTraders);
            player.horseTraders.add(horseTraders);

            return true;
        }

        return false;
    }

    public static ArrayList<Card> copy(CardList cards) {
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

	/**
	 * Comparator for sorting by multiple attributes:
	 * Compares with first Comparator if not equal return result
	 * if equal use second one and repeat.
	 * Repeat this pattern until last Comparator tried.
	 */
    static public class MultilevelComparator<T> implements Comparator<T> {
    	private List<Comparator<T>> comps;
    	
    	public MultilevelComparator(List<Comparator<T>> comparators) {
    		comps = comparators;
    	}
    	
		@Override
		public int compare(T arg0, T arg1) {
			int ret = 0;
			for(Comparator<T> cmp: comps) {
				ret = cmp.compare(arg0, arg1);
				if(ret != 0) {
					return ret;
				}
			}
			return ret;
		}
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> 
    	sortByValue( Map<K, V> map )
	{
	    List<Map.Entry<K, V>> list =
	        new LinkedList<Map.Entry<K, V>>( map.entrySet() );
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>()
	    {
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
	        {
	            return (o1.getValue()).compareTo( o2.getValue() );
	        }
	    } );
	
	    Map<K, V> result = new LinkedHashMap<K, V>();
	    for (Map.Entry<K, V> entry : list)
	    {
	        result.put( entry.getKey(), entry.getValue() );
	    }
	    return result;
	}

	static public class CardNameComparator implements Comparator<Card> {
		@Override
		public int compare(Card card0, Card card1) {
			return card0.getName().compareTo(card1.getName());
		}
	}

	static public class CardCostComparator implements Comparator<Card> {
		@Override
		public int compare(Card card0, Card card1) {
			if(card0.getCost(null) < card1.getCost(null)) {
				return -1;
			} else if(card0.getCost(null) > card1.getCost(null)) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	static public class CardCostComparatorDesc implements Comparator<Card> {
		@Override
		public int compare(Card card0, Card card1) {
			CardCostComparator comp = new CardCostComparator();
			return comp.compare(card1, card0);
		}
	}
	
	static public class CardValueComparator implements Comparator<Card> {
		@Override
		public int compare(Card card0, Card card1) {
			if ( !(card0 instanceof TreasureCard) || !(card1 instanceof TreasureCard) ) 
				return 0;

			TreasureCard tcard0 = (TreasureCard) card0;
			TreasureCard tcard1 = (TreasureCard) card1;
			
			if (tcard0.getValue() < tcard1.getValue()) {
				return -1;
			} else if(tcard0.getValue() > tcard1.getValue()) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	static public class CardValueComparatorDesc implements Comparator<Card> {
		@Override
		public int compare(Card card0, Card card1) {
			CardValueComparator comp = new CardValueComparator();
			return comp.compare(card1, card0);
		}
	}
	
	static public class CardPotionComparator implements Comparator<Card> {
		@Override
		public int compare(Card card0, Card card1) {
			if(card0.costPotion()) {
				if(card1.costPotion()) {
					return 0;
				} else {
					return 1;
				}
			} else if(card1.costPotion()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	static public class CardTypeComparator implements Comparator<Card> {
		@Override
		public int compare(Card card0, Card card1) {
			if(card0 instanceof ActionCard) {
				if(card1 instanceof ActionCard) {
					return 0;
				} else {
					return -1;
				}
			} else if(card1 instanceof ActionCard) {
				return 1;
			} else if(card0 instanceof TreasureCard || card0.getType() == Type.Potion) {
				if(card1 instanceof TreasureCard || card1.getType() == Type.Potion) {
					return 0;
				} else {
					return -1;
				}
			} else if(card1 instanceof TreasureCard || card1.getType() == Type.Potion) {
				return 1;
			} else if(card0 instanceof CurseCard) {
				if(card1 instanceof CurseCard) {
					return 0;
				} else {
					return -1;
				}
			} else if(card1 instanceof CurseCard) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	/**
	 * Comparator for sorting cards by cost and then by name
	 * Used for sorting on table
	 */
	static public class CardCostNameComparator extends MultilevelComparator<Card> {
		private static final ArrayList<Comparator<Card>> cmps = new ArrayList<Comparator<Card>>();
		static {
			cmps.add(new CardCostComparator());
			cmps.add(new CardNameComparator());
		}
		public CardCostNameComparator() {
			super(cmps);
		}
	}
	
	/**
	 * Comparator for sorting cards in hand.
	 * Sort by type then by cost and last by name
	 */
	static public class CardHandComparator extends MultilevelComparator<Card> {
		private static final ArrayList<Comparator<Card>> cmps = new ArrayList<Comparator<Card>>();
		static {
			cmps.add(new CardTypeComparator());
			cmps.add(new CardValueComparatorDesc());
			cmps.add(new CardCostComparatorDesc());
			cmps.add(new CardNameComparator());
		}
		public CardHandComparator() {
			super(cmps);
		}
	}
}
