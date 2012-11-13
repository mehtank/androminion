package com.vdom.players;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.GameEvent;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.api.CurseCard;
import com.vdom.core.AbstractCardPile;
import com.vdom.core.BasePlayer;
import com.vdom.core.Cards;
import com.vdom.core.Game;
import com.vdom.core.MoveContext;
import com.vdom.core.Player;
import com.vdom.core.Util;

/**
 * @author buralien
 *
 */
public class VDomPlayerPatrick extends BasePlayer {
	
	Random rand = new Random(System.currentTimeMillis());
	
	private enum DiscardOption {
        Destructive,
        SemiDestructive,
        NonDestructive
    }
	
	private enum StrategyOption {
		Nothing,
		NoAction,
		SingleAction,
		DoubleAction,
		MultiAction,
		Mirror,
		Minion
	}
	
	private class Opponent {
		public Opponent(int id) {
			actionCards = new ArrayList<Card>();
			VP = -1000;
			isAttacking = false;
			playerID = id;
		}

		private ArrayList<Card> actionCards;
		private int VP;
		private boolean isAttacking;
		private int playerID;
		
		public int getVP() {
			return this.VP + Game.players[this.playerID].getVictoryTokens();
		}
		
		public boolean getIsAttacking() {
			return this.isAttacking;
		}
		
		public ArrayList<Card> getActionCards() {
			return this.actionCards;
		}
		
		public void setVP(int vP) {
			this.VP = vP;
		}
		
		public void addVP(int vP) {
			this.VP += vP;
		}

		public void setAttacking(boolean isAttacking) {
			this.isAttacking = isAttacking;
		}

		public void putActionCard(Card card) {
			this.actionCards.add(card);
		}

		@Override
		public String toString() {
			return "Opponent [actionCards=" + actionCards + ", VP=" + VP
					+ ", isAttacking=" + isAttacking + "]";
		}
	}
	
	private class OpponentList extends HashMap<Integer,Opponent> {
		private static final long serialVersionUID = -9007482931936952794L;
		private HashMap<Integer,Opponent> opponents;
		
		public OpponentList() {
			this.opponents = new HashMap<Integer,Opponent>();
		}
		
		@Override
		public Opponent get(Object key) {
			return this.opponents.get(key);
		}

		public int maxVP() {
			int ret = -1000;
			for (Opponent o : this.opponents.values()) {
				if (o.getVP() > ret) {
					ret = o.getVP();
				}
			}
			if (ret > -1000) {
				return ret;
			} else {
				return -1;
			}
		}
		
		public ArrayList<Card> getActionCards() {
			ArrayList<Card> ret = new ArrayList<Card>();
			for (Opponent o : this.opponents.values()) {
				for (Card c : o.getActionCards()) {
					ret.add(c);
				}
			}
			return ret;
		}
		
		public boolean getIsAttacking() {
			boolean ret = false;
			for (Opponent o : this.opponents.values()) {
				if (o.getIsAttacking()) {
					ret = true;
				}
			}
			return ret;
		}

		@Override
		public String toString() {
			return "OpponentList [opponents=" + opponents + "]";
		}

		@Override
		public void clear() {
			this.opponents.clear();
		}

		@Override
		public boolean isEmpty() {
			return this.opponents.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return this.opponents.containsKey(key);
		}

		@Override
		public Opponent put(Integer key, Opponent value) {
			return this.opponents.put(key, value);
		}

		@Override
		public int size() {
			return this.opponents.size();
		}
	}

	private OpponentList opponents = new OpponentList();
	
	private boolean redefineStrategy = false;
	
	private static ArrayList<Card> specialCards = new ArrayList<Card>();
	private static ArrayList<Card> specialTreasureCards = new ArrayList<Card>();
	private static ArrayList<Card> specialVictoryCards = new ArrayList<Card>();
	private static ArrayList<Card> specialActionCards = new ArrayList<Card>();
	
	static { // specialCards
		specialTreasureCards.add(Cards.foolsGold);
		specialTreasureCards.add(Cards.loan);
		specialTreasureCards.add(Cards.hoard);
		specialTreasureCards.add(Cards.royalSeal);
		specialTreasureCards.add(Cards.venture);
		specialTreasureCards.add(Cards.bank);
		specialTreasureCards.add(Cards.contraband);
		specialTreasureCards.add(Cards.potion);
		
		specialVictoryCards.add(Cards.harem);
		specialVictoryCards.add(Cards.farmland);
		specialVictoryCards.add(Cards.feodum);
		
		//populate 
		for (Card c : specialTreasureCards) {
			specialCards.add(c);
		}
		for (Card c : specialVictoryCards) {
			specialCards.add(c);
		}
		for (Card c : specialActionCards) {
			specialCards.add(c);
		}
	}
	
	private static ArrayList<Card> knownCards = new ArrayList<Card>();
	private static ArrayList<Card> knownActionCards = new ArrayList<Card>();
	private static ArrayList<Card> knownSingleActionCards = new ArrayList<Card>(); // just one in deck is enough (trashing, etc.)
	private static ArrayList<Card> knownDoubleActionCards = new ArrayList<Card>(); // two in deck is ok (mostly attacks and other good terminals)
	private static ArrayList<Card> knownMultiActionCards = new ArrayList<Card>(); // cantrips of which we can have any number without terminal colision
	private static ArrayList<Card> knownComboActionCards = new ArrayList<Card>(); // cantrips and other cards which don't work on their own but need other cards
	private static ArrayList<Card> knownDefenseCards = new ArrayList<Card>(); // can be bought as reaction to aggressive opponent, normally no
	private static ArrayList<Card> knownCursingCards = new ArrayList<Card>(); // cards that add curses to opponent's deck
	private static ArrayList<Card> knownTrashingCards = new ArrayList<Card>(); // cards that allow trashing of Curse by playing them from hand
	private static ArrayList<Card> knownTier3Cards = new ArrayList<Card>(); // cards that can be played without any additional implementation, but are not so good
	private static ArrayList<Card> knownPrizeCards = new ArrayList<Card>(); // prize cards that we know how to play
	private static ArrayList<Card> knownGood52Cards = new ArrayList<Card>(); // cards that play well with 5/2 start
	
	static { // knownActionCards
		knownSingleActionCards.add(Cards.smithy);
		knownSingleActionCards.add(Cards.councilRoom);
		knownSingleActionCards.add(Cards.woodcutter);
		knownSingleActionCards.add(Cards.moneyLender);
		knownSingleActionCards.add(Cards.chapel);
		knownSingleActionCards.add(Cards.nomadCamp);
		knownSingleActionCards.add(Cards.steward);
		knownSingleActionCards.add(Cards.bishop);
		knownSingleActionCards.add(Cards.library);
		knownSingleActionCards.add(Cards.haggler);
		knownSingleActionCards.add(Cards.monument);
		knownSingleActionCards.add(Cards.vault);
		knownSingleActionCards.add(Cards.merchantShip);
		knownSingleActionCards.add(Cards.jackOfAllTrades);
		knownSingleActionCards.add(Cards.bridge);
		knownSingleActionCards.add(Cards.harvest);
		knownSingleActionCards.add(Cards.tactician);
		knownSingleActionCards.add(Cards.tournament);
		knownSingleActionCards.add(Cards.nobleBrigand);
		knownSingleActionCards.add(Cards.tradeRoute);
		
		knownDoubleActionCards.add(Cards.wharf);
		knownDoubleActionCards.add(Cards.jackOfAllTrades);
		knownDoubleActionCards.add(Cards.ghostShip);
		knownDoubleActionCards.add(Cards.courtyard);
		knownDoubleActionCards.add(Cards.witch);
		knownDoubleActionCards.add(Cards.mountebank);
		knownDoubleActionCards.add(Cards.seaHag);
		knownDoubleActionCards.add(Cards.militia);
		knownDoubleActionCards.add(Cards.rabble);
		knownDoubleActionCards.add(Cards.margrave);
		knownDoubleActionCards.add(Cards.familiar);
		knownDoubleActionCards.add(Cards.torturer);
		knownDoubleActionCards.add(Cards.ambassador);
		knownDoubleActionCards.add(Cards.saboteur);
		knownDoubleActionCards.add(Cards.minion);
		knownDoubleActionCards.add(Cards.masquerade);
		knownDoubleActionCards.add(Cards.rogue);
		knownDoubleActionCards.add(Cards.pillage);
		
		knownMultiActionCards.add(Cards.laboratory);
		knownMultiActionCards.add(Cards.market);
		knownMultiActionCards.add(Cards.bazaar);
		knownMultiActionCards.add(Cards.treasury);
		knownMultiActionCards.add(Cards.miningVillage);
		knownMultiActionCards.add(Cards.caravan);
		knownMultiActionCards.add(Cards.alchemist);
		knownMultiActionCards.add(Cards.scryingPool);
		
		knownComboActionCards.add(Cards.throneRoom);
		knownComboActionCards.add(Cards.kingsCourt);
		knownComboActionCards.add(Cards.huntingParty);
		knownComboActionCards.add(Cards.peddler);
		knownComboActionCards.add(Cards.city);
		knownComboActionCards.add(Cards.grandMarket);
		knownComboActionCards.add(Cards.village);
		knownComboActionCards.add(Cards.workersVillage);
		knownComboActionCards.add(Cards.fishingVillage);
		knownComboActionCards.add(Cards.farmingVillage);
		knownComboActionCards.add(Cards.borderVillage);
		knownComboActionCards.add(Cards.shantyTown);
		knownComboActionCards.add(Cards.highway);
		knownComboActionCards.add(Cards.festival);
		knownComboActionCards.add(Cards.sage);
		knownComboActionCards.add(Cards.fortress);
		knownComboActionCards.add(Cards.banditCamp);
		knownComboActionCards.add(Cards.marketSquare);
		knownComboActionCards.add(Cards.wanderingMinstrel);
		
		knownTier3Cards.add(Cards.bureaucrat);
		knownTier3Cards.add(Cards.adventurer);
		knownTier3Cards.add(Cards.conspirator);
		knownTier3Cards.add(Cards.coppersmith);
		knownTier3Cards.add(Cards.scout);
		knownTier3Cards.add(Cards.tribute);
		knownTier3Cards.add(Cards.lighthouse);
		knownTier3Cards.add(Cards.cutpurse);
		knownTier3Cards.add(Cards.outpost);
		knownTier3Cards.add(Cards.apothecary);
		knownTier3Cards.add(Cards.countingHouse);
		knownTier3Cards.add(Cards.fortuneTeller);
		knownTier3Cards.add(Cards.menagerie);
		knownTier3Cards.add(Cards.crossroads);
		knownTier3Cards.add(Cards.ironworks);
		knownTier3Cards.add(Cards.duchess);
		knownTier3Cards.add(Cards.watchTower);
		knownTier3Cards.add(Cards.lookout);
		knownTier3Cards.add(Cards.rebuild);
		
		// knownPrizeCards should be sorted according to importance
		knownPrizeCards.add(Cards.followers);
		knownPrizeCards.add(Cards.diadem);
		knownPrizeCards.add(Cards.princess);
		knownPrizeCards.add(Cards.bagOfGold);

		// implemented separately
		//knownMultiActionCards.add(Cards.golem);
		
		//populate 
		for (Card c : knownSingleActionCards) {
			knownActionCards.add(c);
		}
		for (Card c : knownDoubleActionCards) {
			knownActionCards.add(c);
		}
		for (Card c : knownMultiActionCards) {
			knownActionCards.add(c);
		}

		knownDefenseCards.add(Cards.watchTower);
		knownDefenseCards.add(Cards.moat);
		
		knownCursingCards.add(Cards.witch);
		knownCursingCards.add(Cards.seaHag);
		knownCursingCards.add(Cards.youngWitch);
		knownCursingCards.add(Cards.mountebank);
		knownCursingCards.add(Cards.torturer);
		knownCursingCards.add(Cards.jester);
		knownCursingCards.add(Cards.familiar);
		//knownCursingCards.add(Cards.followers);
		
		knownTrashingCards.add(Cards.chapel);
		knownTrashingCards.add(Cards.remodel);
		knownTrashingCards.add(Cards.masquerade);
		knownTrashingCards.add(Cards.steward);
		knownTrashingCards.add(Cards.tradingPost);
		knownTrashingCards.add(Cards.upgrade);
		knownTrashingCards.add(Cards.salvager);
		knownTrashingCards.add(Cards.apprentice);
		knownTrashingCards.add(Cards.transmute);
		knownTrashingCards.add(Cards.tradeRoute);
		knownTrashingCards.add(Cards.bishop);
		knownTrashingCards.add(Cards.expand);
		knownTrashingCards.add(Cards.forge);
		knownTrashingCards.add(Cards.remake);
		knownTrashingCards.add(Cards.develop);
		knownTrashingCards.add(Cards.jackOfAllTrades);
		knownTrashingCards.add(Cards.trader);
		knownTrashingCards.add(Cards.ambassador); // it is not actually trashing cards, but uses similar mechanism to get rid of them
		knownTrashingCards.add(Cards.altar);
		knownTrashingCards.add(Cards.count);
		knownTrashingCards.add(Cards.counterfeit);
		knownTrashingCards.add(Cards.forager);
		knownTrashingCards.add(Cards.graverobber);
		knownTrashingCards.add(Cards.junkDealer);
		knownTrashingCards.add(Cards.procession);
		knownTrashingCards.add(Cards.rats);
		knownTrashingCards.add(Cards.rebuild);
		
		knownGood52Cards.add(Cards.wharf);
		knownGood52Cards.add(Cards.jackOfAllTrades);
		knownGood52Cards.add(Cards.ghostShip);

		for (Card c : knownActionCards) {
			knownCards.add(c);
		}
		for (Card c : specialTreasureCards) {
			knownCards.add(c);
		}
		for (Card c : specialVictoryCards) {
			knownCards.add(c);
		}


	}
	
	private boolean debug = Game.debug;
	private StrategyOption strategy = StrategyOption.Nothing;
	private ActionCard strategyCard = null;
	private ArrayList<ActionCard> strategyPossibleCards = new ArrayList<ActionCard>();
	private ActionCard strategyMultiCardTerminal = null;
	//private ComboCards combo;
	
	
	/**
	 * @param s	text to log via System.out (if debug enabled)
	 */
	private void log(String s) {
		if (debug) {
			System.out.println("<AI> " + s);
		}
	}
	
	
	
	private boolean isCantrip(ActionCard card) {
		if (card == null) {
			return false;
		}
		if (card.equals(Cards.scryingPool)) {
			return true;
		}
		if ((card.getAddActions() > 0) && (card.getAddCards() > 0)) {
			return true;
		}
		return false;
	}
	
	@Override
	public Card doAction(MoveContext context) {
		return this.advisorPlayAction(this.hand.toArrayListClone());
	}
	
	private int getCardsToEndGame() {
		int prov_col = 1000;
		int min1 = 1000;
		int min2 = 1000;
		int min3 = 1000;
		
		for (AbstractCardPile pile : game.piles.values()) {
			if ((pile.card().equals(Cards.province)) || (pile.card().equals(Cards.colony))) {
				if (pile.getCount() < prov_col) {
					prov_col = pile.getCount();
				}
			} else {
				if (pile.getCount() < min1) {
					min1 = pile.getCount();
				} else if (pile.getCount() < min2) {
					min2 = pile.getCount();
				} else if (pile.getCount() < min3) {
					min3 = pile.getCount();
				} 
			}
		}
		
		return Math.min(prov_col, (min1 + min2 + min3));
	}
	
	@Override
	public Card doBuy(MoveContext context) {
		int gold = context.getCoinAvailableForBuy();
		
		return this.advisorGeneral(context, gold, false, false);
	}

	@Override
	public String getPlayerName() {
		return "Patrick";
	}

	public boolean isAi() {
        return true;
    }
	
	public boolean foolsGold_shouldTrash(MoveContext context) {
		this.log("foolsGold_shouldTrash");
		ArrayList<Card> temphand = this.hand.toArrayListClone();
		Card keep = advisorGeneral(context, this.getCurrencyTotal(temphand), false, true);
		temphand.remove(Cards.foolsGold);
		Card trash = advisorGeneral(context, this.getCurrencyTotal(temphand), false, true);
		
		if (keep.equals(trash)) {
			// card to be obtained with -1 Fools Gold is the same
			return true;
		}

		return (inHandCount(Cards.foolsGold) <= 2);
	}
		
	public Card workshop_cardToObtain(MoveContext context) {
		this.log("workshop_cardToObtain");
		return this.advisorGeneral(context, 4, false, true);
    }
	
	/**
	 * @param context	context
	 * @return			number of players in the game
	 */
	private int numPlayers() {
		return game.getPlayersInTurnOrder().length;
	}
	
	/**
	 * @param context	context
	 * @return			how many turns will each player have before the end of the game
	 */
	private int guessTurnsToEnd() {
		return Math.round(getCardsToEndGame() / numPlayers()) + 1;
	}
	
	/**
	 * @param context	context
	 * @return			how many turns before the deck will be reshuffled
	 */
	private int guessTurnsToReshuffle() {
		return Math.round(getDeckSize(deck.toArrayList()) / 5);
	}
	
	/**
	 * @param context	context
	 * @return			how many times the deck will be reshuffled before the end of the game
	 */
	private int guessReshufflesToEnd(MoveContext context) {
		int t = guessTurnsToEnd() - guessTurnsToReshuffle();
		return (t / (getDeckSize(context) / 5)) + 1;
	}
	
	/**
	 * This function calculates the total amount of coin available to a player in his deck
	 * The resulting amount is somehow approximated for cards like Venture, Bank, Fool's Gold
	 * 
	 * @param context	context
	 * @return			approximate value of coin in whole deck 
	 */
	public int getCurrencyTotal (MoveContext context) {
		return guessCurrencyTotal(this.getAllCards());
	}
	/**
	 * This function calculates the amount of coins available in the list.
	 * It will return the amount as if all treasure cards have been played.
	 * For cards with variable value (Venture), it only calculates the minimum guaranteed value.
	 * Usually used to evaluate treasure in hand.
	 * 
	 * @param list 	list of cards
	 * @return		value in coin
	 */
	private int getCurrencyTotal(ArrayList<Card> list) {
		int money = 0;
		
		for (Card card : list) {
			if (card instanceof TreasureCard) {
				money += ((TreasureCard) card).getValue();
			}
			if (card.equals(Cards.venture)) {
				money += 1;
			}
			if (card.equals(Cards.bank)) {
				// money += 1 + this.getMoneyPerCard(list);
				money += this.getTotalTreasureCards(list) - 1;
			}
			if (card instanceof ActionCard) {
				money += ((ActionCard) card).getAddGold();
			}
		}

		if (Util.getCardCount(list, Cards.foolsGold) > 1) {
			money += (Util.getCardCount(list, Cards.foolsGold) - 1) * 3;
		}

		return money;
	}
	/**
	 * This function calculates the total amount of coin available to a player in his deck
	 * The resulting amount is somehow approximated for cards like Venture, Bank, Fool's Gold
	 * This function should be used to evaluate treasure in deck (or other similar lists of cards)
	 * 
	 * @param list
	 * @return
	 */
	private int guessCurrencyTotal(ArrayList<Card> list) {
		int money = 0;
		
		for (Card card : list) {
			if (card instanceof TreasureCard) {
				money += ((TreasureCard) card).getValue();
			}
			if (card instanceof ActionCard) {
				ActionCard ac = (ActionCard)card;
				money += ac.getAddGold();
				//money += ac.getAddCards();
			}
			if (card.equals(Cards.venture)) {
				//TODO maybe there is some way of incorporating the avg money per card without creating an infinite loop?
				money += 1;
			}
			if (card.equals(Cards.philosophersStone)) {
				money += (list.size()/20);
			}
			if (card.equals(Cards.bank)) {
				//TODO maybe there is some way of incorporating the avg money per card without creating an infinite loop?
				money += 2;
			}
			if (card.equals(Cards.foolsGold)) {
				// we add +1 coin value to every Fools Gold beyond the first
				money += (Util.getCardCount(list, Cards.foolsGold) - 1);
			}
		}
		
		if (this.strategy == StrategyOption.Minion) {
			money += Math.round(1.6*Util.getCardCount(list, Cards.minion));
		}

		return money;
	}
	/**
	 * Function calculates the total amount of VPs for all the cards in the list.
	 * Note that for cards with variable amount of VPs (Vineyard, Silk Road), the amount
	 * of VP they provide is calculated based on the list only, not based on whole deck.
	 * 
	 * Use getVPTotalValue(MoveContext context) for calculation in whole deck
	 * 
	 * @param list	list of cards
	 * @return		amount of VPs provided by the cards
	 */
	private int getVPTotalValue (ArrayList<Card> list) {
		int vps = this.getVictoryTokens();
		
		for (Card card : list) {
			if (card instanceof VictoryCard) {
				vps += ((VictoryCard) card).getVictoryPoints();
			}
			if (this.isCurse(card)) {
				vps += ((CurseCard) card).getVictoryPoints();
			}
			if (card.equals(Cards.duke)) {
				vps += Util.getCardCount(list, Cards.duchy);
			}
			if (card.equals(Cards.gardens)) {
				vps += list.size() / 10;
			}
			if (card.equals(Cards.vineyard)) {
				vps += Math.round(this.getCardCount(ActionCard.class, list) / 3);
			}
			if (card.equals(Cards.fairgrounds)) {
				vps += Math.round(this.getCardNameCount(list) / 5);
			}
			if (card.equals(Cards.silkRoad)) {
				vps += Math.round(this.getCardCount(VictoryCard.class, list) / 4);
			}
			if (card.equals(Cards.feodum)) {
				vps += Math.round(Util.getCardCount(list, Cards.silver) / 3);
			}
			//consider also VP token making cards? 
		}
		
		return vps;
	}
	
	
	
	/**
	 * @param context	context
	 * @return			number of treasure cards player owns
	 */
	private int getTotalTreasureCards (ArrayList<Card> list) {
		int moneycards = 0;
		for (Card card : list) {
			if (card instanceof TreasureCard) {
				moneycards++;
			}
		}
		if (this.strategy == StrategyOption.Minion) {
			moneycards += Math.round(1.6*Util.getCardCount(list, Cards.minion));
		}
		return moneycards;
	}
	
	private double getMoneyPerCard(ArrayList<Card> list) {
		if (!list.isEmpty()) {
			return ( (double) (this.guessCurrencyTotal(list)) / (double) getDeckSize(list));
		}
		return (double) 0.0;
	}

	private double getMoneyPerCard(ArrayList<Card> list, int plustreasure, int pluscards) {
		return (double) (guessCurrencyTotal(list) + plustreasure) / (double) (getDeckSize(list) + pluscards);
	}
	
//	private Card getCardToDiscard(DiscardOption destructive) {
//		return getCardToDiscard(this.hand.toArrayListClone(), destructive);
//	}

	/**
	 * @param list			list of cards
	 * @param destructive	whether this is a forced discard, or optional
	 * @return				best candidate for discarding
	 */
	private Card getCardToDiscard(ArrayList<Card> list, DiscardOption destructive) {
		// Tunnel
		if (list.contains(Cards.tunnel)) {
			return list.get(list.indexOf(Cards.tunnel));
		}

		// Curse
		if (list.contains(Cards.curse)) {
			int trashit = 0;
			for (Card c : list) {
				trashit += (knownTrashingCards.contains(c) ? 1 : 0);
			}
			if (trashit < Util.getCardCount(list, Cards.curse)) {
				return list.get(list.indexOf(Cards.curse));
			}
		}
		
		// Victory cards with no other function
		for (Card card : list) {
			if (isOnlyVictory(card)) {
				return card;
			}
		}
		
		if ((list.contains(Cards.potion)) && (!list.contains(Cards.alchemist))) {
			return list.get(list.indexOf(Cards.potion));
		}
		
		
		switch (this.strategy) {
		case NoAction:
			for (Card card : list) {
				if (card instanceof ActionCard) {
					return card;
				}
			}
			break;
			
		case SingleAction:
		case DoubleAction:
		case MultiAction:
		case Mirror:
			for (Card card : list) {
				if (!this.strategyPossibleCards.contains(card)) {
					return card;
				}
			}
			break;
		default:
			break;
		}
		
		// This is as far as we go with useless cards
		if (destructive == DiscardOption.NonDestructive) {
			return null;
		}
		
		// Overgrown Estate
		if (list.contains(Cards.overgrownEstate)) {
			return list.get(list.indexOf(Cards.overgrownEstate));
		}
		
		// Hovel
		if (list.contains(Cards.hovel)) {
			return list.get(list.indexOf(Cards.hovel));
		}
		
		// Necropolis if playing only few actions
		if (this.strategy != StrategyOption.MultiAction) {
			if (list.contains(Cards.necropolis)) {
				return list.get(list.indexOf(Cards.necropolis));
			}
		}
		
		// Copper
		if (list.contains(Cards.copper)) {
			return list.get(list.indexOf(Cards.copper));
		}

		// This is as far as we go with semi-useless cards
		if (destructive == DiscardOption.SemiDestructive) {
			return null;
		}
		
		// Action cards
		for (Card card : list) {
			if (card instanceof ActionCard) {
				return card;
			}
		}
		
		if (list.contains(Cards.illGottenGains)) {
			return list.get(list.indexOf(Cards.illGottenGains));
		}
		if (list.contains(Cards.loan)) {
			return list.get(list.indexOf(Cards.loan));
		}
		
		

		// Fool's Gold if only one in hand
		if (Util.getCardCount(list, Cards.foolsGold) == 1) { 
			return list.get(list.indexOf(Cards.foolsGold));
		}

		// 2 coin treasures
		if (list.contains(Cards.silver)) {
			return list.get(list.indexOf(Cards.silver));
		}
		if (list.contains(Cards.harem)) {
			return list.get(list.indexOf(Cards.harem));
		}
		if (list.contains(Cards.hoard)) {
			return list.get(list.indexOf(Cards.hoard));
		}
		if (list.contains(Cards.royalSeal)) {
			return list.get(list.indexOf(Cards.royalSeal));
		}
		if (list.contains(Cards.venture)) {
			return list.get(list.indexOf(Cards.venture));
		}

		if (list.contains(Cards.contraband)) {
			return list.get(list.indexOf(Cards.contraband));
		}


		if (!list.isEmpty()) {
			return list.get(0);
		}
		
		return null;
	}
	
	/**
	 * @param hand			list of cards in players hand
	 * @param number		how many cards to discard
	 * @param destructive	discard is mandatory?
	 * @return				list of cards that can be discarded
	 */
	private ArrayList<Card> getCardsToDiscard(ArrayList<Card> list, int number, DiscardOption destructive) {
		ArrayList<Card> ret = new ArrayList<Card>();
		int discarded = 0;
		Card dcard = null;
		
		while ((!list.isEmpty()) && (discarded < number)) {
			dcard = this.getCardToDiscard(list, destructive);
			if (dcard != null) {
				ret.add(discarded, dcard);
				list.remove(ret.get(discarded));
			} else {
				break;
			}
			discarded++;
		}
		
		if (ret.size() == number) {
			Collections.sort(ret, new CardCostComparator());
			return ret;
		}
		
		return new ArrayList<Card>();
	}
	
	private Card getCardToTrash(ArrayList<Card> list, DiscardOption destructive) {
		return getCardToTrash(list, new ArrayList<Card>(), destructive);
	}
	
	/**
	 * @param context	context
	 * @param list		cards in hand
	 * @param destructive	based on this, return only "useless" cards, or null if no such choice
	 * @return			best candidate for trashing
	 */
	private Card getCardToTrash(ArrayList<Card> list, ArrayList<Card> deck, DiscardOption destructive) {
		// Curse, Overgrown Estate and Hovel should be trashed at any time
		if (list.contains(Cards.curse)) {
			return list.get(list.indexOf(Cards.curse));
		}
		if (list.contains(Cards.overgrownEstate)) {
			return list.get(list.indexOf(Cards.overgrownEstate));
		}
		if (list.contains(Cards.hovel)) {
			return list.get(list.indexOf(Cards.hovel));
		}
		
		// Potions should be trashed if they are not useful anymore
		if (list.contains(Cards.potion)) {
			switch (strategy) {
			case NoAction:
				return list.get(list.indexOf(Cards.potion));
			case SingleAction:
				if (Util.getCardCount(deck, strategyCard) > 0) {
					return list.get(list.indexOf(Cards.potion));
				}
				break;
			case DoubleAction:
				if (Util.getCardCount(deck, strategyCard) > 1) {
					return list.get(list.indexOf(Cards.potion));
				}
				break;
			case MultiAction:
				if ((game.pileSize(Cards.alchemist) < 1) && (Util.getCardCount(deck, Cards.alchemist) > 0)) {
					return list.get(list.indexOf(Cards.potion));
				}
				break;
			default:
					break;
			}
		}

		
		// Estate can be trashed when we have less then 2 province/colony
		if (list.contains(Cards.estate)) {
			if ((game.pileSize(Cards.province) > 3) || (game.pileSize(Cards.colony) > 3)) {
				return list.get(list.indexOf(Cards.estate));
			}
		}
		
		// Copper can be trashed if we have high money/card ratio
		if ((this.getMoneyPerCard(deck) >= 1) || (this.strategyCard.equals(Cards.chapel))) {
			if (list.contains(Cards.copper)) {
				return list.get(list.indexOf(Cards.copper));
			}
		}
		
		if (destructive == DiscardOption.NonDestructive) {
			return null;
		}
		
		if (list.contains(Cards.estate)) {
			return list.get(list.indexOf(Cards.estate));
		}
		
		if (list.contains(Cards.copper)) {
			return list.get(list.indexOf(Cards.copper));
		}
		
		if (destructive == DiscardOption.SemiDestructive) {
			return null;
		}
		
		Card ret = Cards.colony;
		for (Card c : list) {
			if (c.getCost(null) < ret.getCost(null)) {
				ret = list.get(list.indexOf(c));
			}
		}
		return ((destructive == DiscardOption.Destructive) ? (list.isEmpty() ? null : ret) : null);
	}
	
	private boolean inDeck(MoveContext context, Card card) {
		return (inDeckCount(context, card) > 0);
	}
	
	private int inDeckCount(MoveContext context, Card card) {
		return Util.getCardCount(this.getAllCards(), card);
	}
	
	@Override
	public Card[] militia_attack_cardsToKeep(MoveContext context) {
        ArrayList<Card> cards2keep = this.hand.toArrayListClone();
        Card card2discard = null;
        
        while (cards2keep.size() > 3) {
        	card2discard = getCardToDiscard(cards2keep, DiscardOption.Destructive);
        	cards2keep.remove(card2discard);
        }
        
        return cards2keep.toArray(new Card[3]);
	}

	@Override
	public Card saboteur_cardToObtain(MoveContext context, int maxCost, boolean potion) {
		this.log("saboteur_cardToObtain");
	    return advisorGeneral(context, maxCost, false, true);
	}

	@Override
	public Card[] torturer_attack_cardsToDiscard(MoveContext context) {
		//TODO test
		return this.getCardsToDiscard(this.hand.toArrayListClone(), 2, DiscardOption.Destructive).toArray(new Card[2]);
	}

	@Override
	public Card[] vault_cardsToDiscardForGold(MoveContext context) {
		//TODO test
		ArrayList<Card> temphand = this.hand.toArrayListClone();
		ArrayList<Card> ret = new ArrayList<Card>(); 
		Card card = null;
		
		while (ret.size() < 2) {
			card = this.getCardToDiscard(temphand, DiscardOption.SemiDestructive);
			if (card == null) {
				break;
			} 
			temphand.remove(card);
			ret.add(card);
		}
		
		while (ret.size() < 2) {
			if (getMyAddActions() == 0) {
				for (Card acard : temphand) {
					if (acard instanceof ActionCard) {
						ret.add(acard);
					}
				}
			}
		}
		
		while (ret.size() < 2) {
			for (Card vCard : temphand) {
				if (vCard instanceof VictoryCard) {
					ret.add(vCard);
				}
			}
		}
		
		while (ret.size() < 2) {
			for (Card tCard : temphand) {
				if (!(tCard instanceof TreasureCard)) {
					ret.add(tCard);
				}
			}
		}
		
		while (ret.size() < 2) {
			for (Card tCard : temphand) {
				ret.add(tCard);
			}
		}
        this.log("vault: chosen " + ret);
        
        if (ret.size() > 0) {
        	return ret.toArray(new Card[ret.size()]);
        } 
    	return null;
	}

	@Override
	public Card[] goons_attack_cardsToKeep(MoveContext context) {
	    return militia_attack_cardsToKeep(context);
	}

	@Override
	public Card[] followers_attack_cardsToKeep(MoveContext context) {
	    return militia_attack_cardsToKeep(context);
	}

	@Override
	public Card[] margrave_attack_cardsToKeep(MoveContext context) {
	    return militia_attack_cardsToKeep(context);
	}

	@Override
	public Card[] ghostShip_attack_cardsToPutBackOnDeck(MoveContext context) {
		//TODO rewrite
	    ArrayList<Card> cards = new ArrayList<Card>();
	    for (int i = 0; i < context.getPlayer().getHand().size() - 3; i++) {
	        cards.add(context.getPlayer().getHand().get(i));
	    }
	
	    return cards.toArray(new Card[cards.size()]);
	}

	@Override
	public boolean miningVillage_shouldTrashMiningVillage(MoveContext context) {
		this.log("miningVillage_shouldTrashMiningVillage: keep");
		Card normal = advisorGeneral(context, getCurrencyTotal(hand.toArrayListClone()), false, true);
		
		this.log("miningVillage_shouldTrashMiningVillage: trash");
		Card extra = advisorGeneral(context, getCurrencyTotal(hand.toArrayListClone()) + 2, false, true);
		
		//TODO should compare resulting decks
		if (normal.equals(extra)) {
			// card to be obtained with +2 coin is the same
			return false;
		}
		return true;
	}

	@Override
	public Card island_cardToSetAside(MoveContext context) {
		Card ret = null;
		ArrayList<Card> temphand = this.hand.toArrayListClone();
		
		while (temphand.size() > 0) {
			ret = getCardToDiscard(temphand, DiscardOption.Destructive);
			temphand.remove(ret);
			if (this.isOnlyVictory(ret)) {
				return ret;
			}
		}
		
	    return getCardToDiscard(temphand, DiscardOption.Destructive);
	}

	@Override
	public Card farmland_cardToObtain(MoveContext context, int exactCost, boolean potion) {
		//TODO test
		this.log("farmland_cardToObtain");
	    return this.advisorGeneral(context, exactCost, true, true);
	}

	@Override
	public Card farmland_cardToTrash(MoveContext context) {
	    return getCardToTrash(DiscardOption.Destructive);
	}

	@Override
	public Card borderVillage_cardToObtain(MoveContext context) {
		this.log("borderVillage_cardToObtain");
	    return this.advisorGeneral(context, 5, false, true);
	}

	@Override
	public TorturerOption torturer_attack_chooseOption(MoveContext context) {
		if (game.pileSize(Cards.curse) <= 0) {
			return Player.TorturerOption.TakeCurse;
		}
		
        if(inHand(Cards.watchTower) || inHand(Cards.trader)) {
            return Player.TorturerOption.TakeCurse;
	    }
	    
        int discarded = 2;
		ArrayList<Card> temphand = this.hand.toArrayListClone();
        ArrayList<Card> toDiscard = this.getCardsToDiscard(temphand, discarded, DiscardOption.NonDestructive);
        if (toDiscard.size() >= discarded) {
        	return Player.TorturerOption.DiscardTwoCards;
        }
        
		for (Card c : toDiscard) {
			temphand.remove(c);
			discarded--;
		}

		this.log("torturer_attack_chooseOption: keep");
		Card keep = advisorGeneral(context, this.getCurrencyTotal(temphand), false, false);
		
//		this.log("torturer_attack_chooseOption: discard");
//		Card discard = advisorGeneral(context, this.getCurrencyTotal(temphand), false, false);
//
//		if (keep != null && discard !=null && keep.equals(discard)) {
//			return Player.TorturerOption.DiscardTwoCards;
//		}
		
		ArrayList<Card> preffered = new ArrayList<Card>();
		if (game.isPlatInGame()) {
			preffered.add(Cards.colony);
			preffered.add(Cards.platinum);
		} else {
			preffered.add(Cards.province);
			preffered.add(Cards.gold);
		}
		
		if (preffered.contains(keep)) {
			return Player.TorturerOption.TakeCurse;
		}
		        
	    return Player.TorturerOption.DiscardTwoCards;
	}
	
	/**
	 * @param orig_set	original list of cards
	 * @param new_set	new list of cards
	 * @return			quality difference between each list (positive if new is better)
	 */
//	public double compareCards(ArrayList<Card> orig_set, ArrayList<Card> new_set) {
//		int vps = this.getVPTotalValue(new_set) - this.getVPTotalValue(orig_set);
//		double mpc = this.getMoneyPerCard(new_set) - this.getMoneyPerCard(orig_set);
//		int coins = this.getCurrencyTotal(new_set) - this.getCurrencyTotal(orig_set);
//		this.log("vps: " + vps + "; coins: " + coins + "; mpc: " + mpc );
//		
//		return (double) (((vps*0.5) * Math.abs(vps*0.5)) + ((coins*0.8) * Math.abs(vps*0.8)));
//	}

	public boolean shouldBuyPotion() {
		boolean ret = false;
		
		for (ActionCard c : this.strategyPossibleCards) {
			ret = ret | c.costPotion(); 
		}
		return ret;
	}

	public boolean shouldDiscard(Card card) {
		ArrayList<Card> c = new ArrayList<Card>();
		c.add(card);
	    return (this.getCardToDiscard(c, DiscardOption.NonDestructive) != null);
	}

	public boolean shouldTrash(Card card) {
		ArrayList<Card> c = new ArrayList<Card>();
		c.add(card);
	    return (this.getCardToTrash(c, DiscardOption.NonDestructive) != null);
	}

	@Override
	public boolean loan_shouldTrashTreasure(MoveContext context, TreasureCard treasure) {
		if ((treasure.equals(Cards.copper)) || (treasure.equals(Cards.illGottenGains))) {
			if ((getMoneyPerCard(getAllCards(), -1, -1) > 0.7) && (getCurrencyTotal(context) > 6)) {
				return true;
			}
		}
		
		if (treasure.equals(Cards.loan)) {
			return true;
		}
		
		if ((this.getMoneyPerCard(this.getAllCards(), (0 - treasure.getValue()), -1) > (Math.pow(treasure.getValue(), 2) * 0.7)) && (getCurrencyTotal(context) > 6)) {
			return true;
		}
	
	    return false;
	}

	/**
	 * Function calculates the total amount of VPs for all the cards in the deck.
	 *  
	 * @param context	context
	 * @return			amount of VPs in the deck
	 */
	private int getVPTotalValue (MoveContext context) {
		return this.getVPTotalValue(this.getAllCards());
	}

	/**
	 * @param context		context
	 * @param gold			coins available to buy
	 * @param exact			card must cost exactly the value
	 * @param mandatory		must return a card (not nothing)
	 * @return				best card to gain
	 */
	//@SuppressWarnings("unused")
	private Card advisorGeneral(MoveContext context, int gold, boolean exact, boolean mandatory) {
		if (this.shouldReEvaluateStrategy()) {
			advisorAction();
			this.log("strategy options: " + this.strategyPossibleCards);
			this.redefineStrategy = false;
 		}
		
		double maxmpc = -1;
		int maxvp = -1;

		TreasureCard maxMPC_card = null;
		VictoryCard maxVP_card = null;
		ActionCard action_card = null;
		ArrayList<Card> special_cards = new ArrayList<Card>();
		ArrayList<Card> deck = this.getAllCards();
		ArrayList<Card> potentialBuys = new ArrayList<Card>();
		
		double mpc = this.getMoneyPerCard(deck);
		float cph = this.getCardsPerHand(context);
		
		int allowedTerminals = Math.max(1, Math.round((float)getDeckSize(context) / ((1 + cph) * 2)));
		switch (strategy) {
		case Minion:
			allowedTerminals = 1;
		case SingleAction:
			allowedTerminals = (int) Math.round(allowedTerminals / 1.5);
			break;
		case NoAction:
			allowedTerminals = 0;
			break;
		default:
			break;
		}
		
		this.log("allowedTerminals: " + getDeckSize(context) + " / " + ((1 + cph) * 2) + " = " + allowedTerminals);
		this.log("getStrategyCardsInDeck: " + getStrategyCardsInDeck(context, true));
		this.log("getTerminalsInDeck: " + getTerminalsInDeck(context));
		
		// here we check each card available for buy
		// the goal is to find the best VP card, best treasure and best action card
		for (AbstractCardPile pile : game.piles.values()) {
			Card card = pile.card();
			
			if (!exact || card.getCost(context) == gold) {
				if (game.isValidBuy(context, card, gold)) {
					
					if ((card instanceof VictoryCard) && (!specialCards.contains(card))) {
						VictoryCard vc = (VictoryCard) card;
						
						int vp = vc.getVictoryPoints();
						if (vc.equals(Cards.gardens)) {
							vp += Math.floor(deck.size() / 10);
						}
						if (vc.equals(Cards.duke)) {
							vp += Util.getCardCount(deck, Cards.duchy);
						}
						if (vc.equals(Cards.duchy)) {
							vp += Util.getCardCount(deck, Cards.duke);
						}
						if (vc.equals(Cards.silkRoad)) {
							vp += Math.floor(this.getCardCount(VictoryCard.class, deck) / 4);
						}
						if (vc.equals(Cards.feodum)) {
							vp += Math.floor(Util.getCardCount(this.getAllCards(), Cards.silver) / 3);
						}
						if (game.embargos.containsKey(vc.getName())) {
							vp -= (game.embargos.get(vc.getName()) + this.guessReshufflesToEnd(context));
						}
						
						// don't end the game if losing and the last card will not secure a win
						// this is not really good with +buys, as it will prevent buying multiple cards to secure a win
						// TODO review, because with >1 buys, will buy the last card anyway
						if ((willEndGameGaining(card)) && (winningBy(context) < (0 - vp)) && (context.buys == 1)) {
							this.log("can't recommend " + card + ", would lose game by " + (winningBy(context) + (vp * (vp > 0 ? 1 : -1))));
							continue;
						}

						if (vp >= maxvp) {
							maxvp = vp;
							maxVP_card = vc;
						}
					} // victory
					
					// don't end the game if losing
					if ((willEndGameGaining(card)) && (this.winningBy(context) < 0)  && (context.buys == 1)) {
						this.log("can't recommend " + card + ", would lose game by " + Math.abs(winningBy(context)) + " VP");
						continue;
					}
					
					if ((card instanceof TreasureCard) && (!specialCards.contains(card))) {
						TreasureCard tc = (TreasureCard) card;
						ArrayList<Card> tempdeck = new ArrayList<Card>(deck);
						tempdeck.add(tc);
						
						if (card.equals(Cards.cache)) {
							tempdeck.add(Cards.copper);
							tempdeck.add(Cards.copper);
						}
						if (game.embargos.containsKey(card.getName())) {
							for (int i = 0; i < game.embargos.get(card.getName()); i++) {
								tempdeck.add(Cards.curse);
							}
						}
						
						double tmpc = this.getMoneyPerCard(tempdeck);

						if (tmpc > maxmpc) {
							maxmpc = tmpc;
							maxMPC_card = tc;
						}
					} // treasure
					
					if ((card.equals(Cards.golem)) && (this.getCardCount(ActionCard.class, deck) > 1) && (this.strategy != StrategyOption.NoAction)) {
						this.log("action: Golem (have " + this.getCardCount(ActionCard.class, deck) + " actions)");
						potentialBuys.add(Cards.golem);
					}
					
					// action cards
					if ((this.strategyPossibleCards.contains(card)) && ((game.pileSize(Cards.curse) > 3) || (!knownCursingCards.contains(card)))) {
						ActionCard ac = (ActionCard) card;
						
						// we can buy another piece of "single" card only when the deck is big enough
						
						switch (this.strategy) {
						case Minion:
							this.log("action: " + card + " (have " + (this.inDeckCount(context, card) + ")"));
							potentialBuys.add(card);
							break;
						case DoubleAction:
							if ((allowedTerminals < 2) && (Util.getCardCount(deck, Cards.gold) > 0)) {
								allowedTerminals = 2;
							}

						case SingleAction:
							if (this.strategyPossibleCards.contains(card)) {
								if ((getStrategyCardsInDeck(context, false).size() < allowedTerminals) || (isCantrip(ac))) {
									this.log("action: " + card + " (have " + (this.inDeckCount(context, card) + ")"));
									potentialBuys.add(card);
									potentialBuys.add(card);
								}
							}
							
							if ((knownMultiActionCards.contains(card)) && (this.rand.nextInt(4) == 1)) {
								//this.log("action: extra " + card);
								potentialBuys.add(card);
							}
							break;
							
						case MultiAction:
							// choose at every opportunity
							if (this.strategyPossibleCards.contains(card)) {
								this.log("action: evaluating another " + card);
								if ((!VDomPlayerPatrick.knownComboActionCards.contains(card)) || (this.rand.nextBoolean()) || (card.costPotion())) {
									ArrayList<Card> temp = new ArrayList<Card>(getAllCards());
									temp.retainAll(knownComboActionCards);
									//this.log(card + (isCantrip(ac) ? " is " : " isn't ") + "cantrip");
									if (temp.size()*2 <= this.getStrategyCardsInDeck(context, false).size()+(isCantrip(ac) ? 1 : 0)) {
										potentialBuys.add(card);
									}
								}
							} 
							if ((knownSingleActionCards.contains(card) || knownDoubleActionCards.contains(card)) && (getTerminalsInDeck(context).size() < allowedTerminals)) {
									//this.log("action: terminal " + card);
									potentialBuys.add(card);
							}
							break;
							
						case Mirror:
							if (this.opponents.getActionCards().contains(card)) {
								potentialBuys.add(ac);
								//this.log("action: same " + card);
							}
						default:
							break;
						}
					}
					
					if (specialCards.contains(card)) {
						special_cards.add(card);
					}
				}
			}
		}
		
		this.log("VPs: " + this.winningBy(context));
		this.log("best basic mpc: " + maxMPC_card);
		this.log("best vp: " + maxVP_card);
		this.log("specials: " + special_cards);
		this.log("potential actions: " + potentialBuys);
		
		int embargopiles = 0;
		
		while (potentialBuys.size() > 0) {
			action_card = (ActionCard) Util.randomCard(potentialBuys);
			potentialBuys.remove(action_card);
			if (game.embargos.containsKey(action_card.getName())) {
				this.log("action " + action_card + " is embargoed, skipping");
				action_card = null;
				embargopiles++;
			}
		}
		this.log("picked action: " + action_card);
		if ((action_card == null) && (embargopiles > 0)) {
			this.redefineStrategy = true;
		}
		
		if (!special_cards.isEmpty()) {
		
			Card scard = Cards.loan;
			if (special_cards.contains(scard)) {
				// we have no loan in deck and significantly more copper then other trasures
				if ((this.inDeck(context, scard)) || (this.inDeckCount(context, Cards.copper)*2 <= this.getCurrencyTotal(context))) {
					// buying only when there are a lot of coppers and only 1 piece
					this.log("Loan not good, " + this.inDeckCount(context, Cards.copper) 
							+ " coppers in deck and total treasure value " + getCurrencyTotal(context));
					special_cards.remove(scard);
				}
			}
			
			scard = Cards.bank;
			if (special_cards.contains(scard)) {
				// TODO tpc counted badly, to check again
				double tpc = (double) (this.getCardCount(TreasureCard.class) / getDeckSize(deck));
				if (tpc * 5.0 < 3.0) {
					this.log("Bank not good, tpc is " + tpc);
					special_cards.remove(scard);
				}
			} 
	
			scard = Cards.hoard;
			if (special_cards.contains(scard)) {
				if ((Util.getCardCount(deck, Cards.gold) <= Util.getCardCount(deck, Cards.hoard)) || game.isPlatInGame()) {				
					this.log("Hoard not good, either platinum in play or have " 
							+ Util.getCardCount(deck, Cards.gold) + " gold and " 
							+ Util.getCardCount(deck, Cards.hoard) + " hoards");
					special_cards.remove(scard);
				}
			}
			
			// here are the generic "Better then Silver" cards based on value
	
			scard = Cards.harem;
			if (special_cards.contains(scard)) {
				if ((mpc < 1.2) || (game.isPlatInGame())) {
					this.log("Harem not good, mpc = " + mpc);
					special_cards.remove(scard);
				} 
			} 
			
			if (maxMPC_card != null) {
				scard = Cards.venture;
				if (special_cards.contains(scard)) {
					if (maxMPC_card.getValue() > 2) {
						special_cards.remove(scard);
					} 
				} 
		
				scard = Cards.royalSeal;
				if (special_cards.contains(scard)) {
					if (maxMPC_card.getValue() > 2) {
						special_cards.remove(scard);
					} 
				} 
		
				scard = Cards.foolsGold;
				if (special_cards.contains(scard)) {
					if (maxMPC_card.getValue() > 1) {
						special_cards.remove(scard);
					} 
				}
			}
			
			scard = Cards.contraband;
			if (special_cards.contains(scard)) {
				special_cards.remove(scard);
			}
			
			scard = Cards.potion;
			if ((special_cards.contains(scard)) && (!this.needMorePotion(deck))) {
				special_cards.remove(scard);
			}

			scard = Cards.farmland;
			if (special_cards.contains(scard)) {
				if (maxVP_card != null && maxVP_card.getVictoryPoints() < 4) {
					if (!(hand.contains(Cards.curse))) {
						special_cards.remove(scard);
					}
				}
			} 
		}

		this.log("specials : " + special_cards);
		
		if (!special_cards.isEmpty()) {
			int scost = -1;
			int svalue = -1;
			//int spoints = -1;
			
			
			for (Card c : special_cards) {
				
				// potion is special because it can't be compared based on value
				if (c.equals(Cards.potion)) {
					this.log("potion: have " + Util.getCardCount(deck, Cards.potion) +
							" and " + Util.getCardCount(deck, Cards.alchemist) +
							" Alchemist(s) in " + deck.size() + " cards");
					if (this.needMorePotion(deck)) {
						switch (this.strategy) {
						case SingleAction:
							if ((Util.getCardCount(deck, Cards.potion) < 1) && (Util.getCardCount(deck, this.strategyCard) < 1)) {
								maxMPC_card = (TreasureCard) c;
								scost = 1000;
								svalue = 1000;
							}
							break;
						case DoubleAction:
							if ((Util.getCardCount(deck, Cards.potion) < 1) && (Util.getCardCount(deck, this.strategyCard) < 2)) {
								maxMPC_card = (TreasureCard) c;
								scost = 1000;
								svalue = 1000;
							}
							break;
						case MultiAction:
							if ((Util.getCardCount(deck, Cards.potion) * Math.max(10,(20 - (Util.getCardCount(deck, Cards.alchemist) * 2)))) < deck.size()) {
								maxMPC_card = (TreasureCard) c;
								scost = 1000;
								svalue = 1000;
							}
							break;
						case Mirror:
							for (Card op : this.opponents.getActionCards()) {
								if (op.costPotion()) {
									if ((Util.getCardCount(deck, Cards.potion) * Math.max(10,(20 - (Util.getCardCount(deck, Cards.alchemist) * 2)))) < deck.size()) {
										maxMPC_card = (TreasureCard) c;
										scost = 1000;
										svalue = 1000;
									} else if (Util.getCardCount(deck, Cards.potion) < 1) {
										maxMPC_card = (TreasureCard) c;
										scost = 1000;
										svalue = 1000;
									}
								} 
							}
							break;
						default:
							break;
						}
					}
				} // potion 
				else if (specialTreasureCards.contains(c)) {
					TreasureCard tc = (TreasureCard) c;
					if ((tc.getValue() > svalue) && (tc.getCost(context) > scost)) {
						scost = tc.getCost(context);
						svalue = tc.getValue();
						maxMPC_card = tc;
					} 
				} else if (specialVictoryCards.contains(c)) {
					if (c.equals(Cards.farmland)) {
						if ((hand.contains(Cards.curse)) && (maxVP_card.getVictoryPoints() <= 4)) {
							maxVP_card = (VictoryCard) c;
						}
					}
				}
			}
		}
		
		this.log("best final mpc: " + maxMPC_card);
		this.log("best final vp: " + maxVP_card);

		if (maxMPC_card != null) {
			if (maxMPC_card.equals(Cards.copper)) {
				if (mpc * 5 > 3) {
					maxMPC_card = null;
				}
			}
		}
		
		this.log("current deck mpc: " + mpc);
		this.log("guessTurnsToReshuffle(): " + guessTurnsToReshuffle());
		this.log("guessTurnsToEnd(): " + guessTurnsToEnd());
		this.log("best action: " + action_card);
		
		if (action_card != null && maxVP_card != null && (maxVP_card.getVictoryPoints() < 6)) {
			this.log("choosing action");

			switch (strategy) {
			case Minion:
				return action_card;
			case Mirror:
				this.opponents.getActionCards().remove(action_card);
				return action_card;

			case SingleAction:
			case DoubleAction:
				if (this.strategyPossibleCards.contains(action_card)) {
					return action_card;
				}
				break;

			case MultiAction:
				int acvalue = Math.max(action_card.getCost(context), 3) + action_card.getAddGold() + action_card.getAddActions();
				
				if (action_card.costPotion()) {
					acvalue += 2;
				}
				
				this.log("action card value: " + acvalue);
				
				if (acvalue >= gold) {
					if (!knownMultiActionCards.contains(action_card)) {
						this.strategyMultiCardTerminal = action_card;
						this.log("multi action terminal: " + this.strategyMultiCardTerminal);
					}
					if (this.guessCurrencyTotal(getAllCards()) > 7 + this.getActionCardCount()) {
						return action_card;
					}
				}
				break;
			default:
				break;
			}
		}
		
		if (maxVP_card != null) {
			if (!exact || maxVP_card.getCost(context) == gold) {
				if ((mpc > (1.9 - (maxVP_card.getVictoryPoints() * 0.15 * (context.countCardsInPlay(Cards.hoard) + 1)))) || ((guessTurnsToReshuffle() > guessTurnsToEnd()) && (maxVP_card.getVictoryPoints() > 1))) {
					this.log("choosing victory (hoards: " + context.countCardsInPlay(Cards.hoard) + ")");
					return maxVP_card;
				}
			}
		}
		
		if (maxMPC_card != null) {
			if (!exact || maxMPC_card.getCost(context) == gold) {
				this.log("choosing treasure");
				return maxMPC_card;
			}
		}
		
		if (mandatory) {
			this.log("must choose a card");
			for (AbstractCardPile pile : game.piles.values()) {
				Card card = pile.card();
				if (!exact || card.getCost(context) == gold) {
					if ((game.isValidBuy(context, card, gold)) && !(card.equals(Cards.curse))) {
						return card;
					}
				}
			}
		}
		
		this.log("choosing nothing");
		return null;
	}
	
	private boolean willEndGameGaining(Card card) {
		if (game.emptyPiles() > 2) {
			return true;
		}
		if ((card.equals(Cards.province)) && (game.pileSize(card) == 1)) {
			return true;
		}
		if ((card.equals(Cards.colony)) && (game.pileSize(card) == 1)) {
			return true;
		}
		if (game.emptyPiles() == 2) { 
			if (game.pileSize(card) == 1) {
				return true;
			}
			if ((knownCursingCards.contains(card)) && (game.pileSize(Cards.curse) == 1)) {
				return true;
			}
			if ((card.equals(Cards.cache)) && (game.pileSize(Cards.copper) <= 2) && (game.pileSize(Cards.copper) > 0) ) {
				return true;
			}
		}
		
		return false;
	}
	
//	public double compareDeckOptions(ArrayList<Card> deck, Card option1, Card option2, int hoardsPlayed) {
//		ArrayList<Card> o1 = new ArrayList<Card>();
//		ArrayList<Card> o2 = new ArrayList<Card>();
//		
//		if (option1 != null) {
//			o1.add(option1);
//		}
//		
//		if (option2 != null) {
//			o2.add(option2);
//		}
//		
//		return compareDeckOptions(deck, o1, o2, hoardsPlayed);
//	}
	/**
	 * @param deck			
	 * @param option1		
	 * @param option2		
	 * @param hoardsPlayed	
	 * @return
	 */
//	public double compareDeckOptions(ArrayList<Card> deck, ArrayList<Card> option1, ArrayList<Card> option2, int hoardsPlayed) {
//		ArrayList<Card> deck1 = new ArrayList<Card>();
//		ArrayList<Card> deck2 = new ArrayList<Card>();
//		
//		for (Card c : deck) {
//			deck1.add(c);
//			deck2.add(c);
//		}
//		
//		for (Card c : option1) {
//			deck1.add(c);
//			if (c.equals(Cards.cache)) {
//				deck1.add(Cards.copper);
//				deck1.add(Cards.copper);
//			}
//			if (c.equals(Cards.farmland)) {
//				deck1.remove(Cards.curse);
//				deck1.add(Cards.estate);
//			}
//			if (c instanceof VictoryCard) {
//				for (int i = 0; i < hoardsPlayed; i++) {
//					deck1.add(Cards.gold);
//				}
//			}
//			for (int e = 0; e < game.getEmbargos(c.toString()); e++) {
//				deck1.add(Cards.curse);
//			}
//		}
//		for (Card c : option2) {
//			deck2.add(c);
//			if (c.equals(Cards.cache)) {
//				deck2.add(Cards.copper);
//				deck2.add(Cards.copper);
//			}
//			if (c.equals(Cards.farmland)) {
//				deck2.remove(Cards.curse);
//				deck2.add(Cards.estate);
//			}
//			if (c instanceof VictoryCard) {
//				for (int i = 0; i < hoardsPlayed; i++) {
//					deck2.add(Cards.gold);
//				}
//			}
//			for (int e = 0; e < game.getEmbargos(c.toString()); e++) {
//				deck2.add(Cards.curse);
//			}
//
//		}
//		
//		return compareCards(deck1, deck2);
//	}



	@Override
	public void newGame(MoveContext context) {
	    // When multiple games are played in one session, the same Player object
	    // is used, so reset any fields in this method.
		super.newGame(context);
		
		
		redefineStrategy = false;
		strategy = StrategyOption.Nothing;
		strategyCard = null;
		strategyMultiCardTerminal = null;
		strategyPossibleCards = new ArrayList<ActionCard>();
		
		//opponentActionCards = new ArrayList<Card>();
		//opponentVP = -1000;
		//opponentIsAttacking = false;
		this.opponents = new OpponentList();
	}
	
	private void advisorAction() {
		ArrayList<ActionCard> cards = new ArrayList<ActionCard>();
		boolean shouldReCurse = false;
		this.strategyPossibleCards.clear();
		
		for (Card card : this.opponents.getActionCards()) {
			if ((knownCursingCards.contains(card)) && (game.pileSize(Cards.curse) > (this.guessTurnsToReshuffle() + 2))) {
				shouldReCurse = true;
			}
		}
		
		for (AbstractCardPile pile : game.piles.values()) {
			if ((knownActionCards.contains(pile.card())) && (pile.getCount() > 2)) {
				if ((knownCursingCards.contains(pile.card())) || (!shouldReCurse)) {
					if (!game.embargos.containsKey(pile.card().getName())) {
						cards.add((ActionCard) pile.card());
					} else {
						this.log("advisorAction: skipped " + pile.card() + " due to embargo");
					}
				}
			}
		}
		
		this.log("advisorAction: considering " + cards.size() + " action cards out of " + knownActionCards.size() + " total known cards");
		
		ArrayList<Card> tier1 = new ArrayList<Card>(cards);
		tier1.retainAll(VDomPlayerPatrick.knownDoubleActionCards);
		if (tier1.size() > 0) {
			this.log("advisorAction: found Tier1 cards " + tier1);
			cards.clear();
			for (Card c : tier1) {
				if (game.pileSize(c) > 2) {
					cards.add((ActionCard) c);
				}
			}
		}
		
		if (cards.size() > 0) {
			// pick random card to base strategy on
			this.strategyCard = null;
			while ((cards.size() > 0) && (this.strategyCard == null)) {
				this.strategyCard = cards.get(this.rand.nextInt(cards.size()));
				cards.remove(this.strategyCard);
				if (game.pileSize(this.strategyCard) < 3) {
					this.strategyCard = null;
				}
			}
			if (this.strategyCard != null) {
				this.strategyPossibleCards.add(this.strategyCard);
			} else
				return;
			
			if (this.strategyCard.equals(Cards.minion)) {
				this.strategy = StrategyOption.Minion;
				this.log("advisorAction: " + this.strategyCard);
			} else if (VDomPlayerPatrick.knownMultiActionCards.contains(this.strategyCard)) {
				this.strategy = StrategyOption.MultiAction;
				this.log("advisorAction: multiple cantrips and combo cards (via " + this.strategyCard + ")");

				for (AbstractCardPile pile : game.piles.values()) {
					if (VDomPlayerPatrick.knownMultiActionCards.contains(pile.card())) {
						this.strategyPossibleCards.add((ActionCard) pile.card());
					}
				}
				for (AbstractCardPile pile : game.piles.values()) {
					if (VDomPlayerPatrick.knownComboActionCards.contains(pile.card())) {
						this.strategyPossibleCards.add((ActionCard) pile.card());
					}
				}
			} else if (knownDoubleActionCards.contains(this.strategyCard)) {
				this.strategy = StrategyOption.DoubleAction;
				this.log("advisorAction: double " + this.strategyCard);
			} else if (knownSingleActionCards.contains(this.strategyCard)) {
				this.strategy = StrategyOption.SingleAction;
				this.log("advisorAction: single " + this.strategyCard);
			}
		} else {
			this.strategy = StrategyOption.NoAction;
			this.log("advisorAction: pure big money");
		}
		
		
	}
	
	private int getDeckSize(MoveContext context) {
		return getDeckSize(getAllCards());
	}
	
	private int getDeckSize(ArrayList<Card> deck) {
		int size = 0;
		for (Card card : deck) {
			size++;
			if (card instanceof ActionCard) {
				if(isCantrip((ActionCard) card)) {
					size--;
				}
			}
		}
		return size;
	}



	@Override
	public Card jackOfAllTrades_nonTreasureToTrash(MoveContext context) {
		ArrayList<Card> temphand = hand.toArrayListClone();
		while (temphand.size() > 0) {
			Card tcard = this.getCardToTrash(temphand, DiscardOption.SemiDestructive);
			temphand.remove(tcard);
			temphand.trimToSize();
			if (tcard == null) {
				return null;
			}
			if (tcard instanceof TreasureCard) {
				tcard = null;
			}
			if (tcard != null) {
				return tcard;
			}
		}
	    
	    return null;
	}



	@Override
	public boolean jackOfAllTrades_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
		ArrayList<Card> a = new ArrayList<Card>();
		a.add(card);
	    if(card.equals(getCardToDiscard(a, DiscardOption.NonDestructive))) {
	        return true;
	    }
	    return false;
	}


	@Override
	public void gameEvent(GameEvent event) {
       super.gameEvent(event);
       
	   if (event.player.equals(this)) {
		   return; // we keep track of our own events, thank you very much
	   }
	   
	   if (this.opponents.isEmpty()) {
		   this.opponents.put(event.player.playerNumber, new Opponent(event.player.playerNumber));
	   } else if (!this.opponents.containsKey(event.player.playerNumber)) {
		   this.opponents.put(event.player.playerNumber, new Opponent(event.player.playerNumber));
	   }
	   
	   if (this.opponents.get(event.player.playerNumber).getVP() == -1000) {
		   if (game.sheltersInPlay) {
			   this.opponents.get(event.player.playerNumber).setVP(0);
		   } else {
			   this.opponents.get(event.player.playerNumber).setVP(3);
		   }
	   }

	   if ((event.getType() == GameEvent.Type.BuyingCard) || (event.getType() == GameEvent.Type.CardObtained)) {
    	   Card card = event.getCard();
    	   
    	   if (card instanceof ActionCard) {
    		   ActionCard ac = (ActionCard) card;
    		   this.opponents.get(event.player.playerNumber).putActionCard(card);
    		   if (ac.isAttack()) { 
	    		   this.opponents.get(event.player.playerNumber).setAttacking(true);
	    	   }
    	   }
    	   
    	   if (card instanceof VictoryCard) {
    		   this.opponents.get(event.player.playerNumber).addVP(((VictoryCard)card).getVictoryPoints());
    	   }
    	   
    	   if (card.equals(Cards.curse)) {
    		   this.opponents.get(event.player.playerNumber).addVP(-1);
    	   }
    	   
       } 

	   if (event.getType() == GameEvent.Type.CardTrashed) {
    	   Card card = event.getCard();
    	   
    	   if (card instanceof VictoryCard) {
    		   this.opponents.get(event.player.playerNumber).addVP(0-((VictoryCard)card).getVictoryPoints());
    	   }
    	   
    	   if (card.equals(Cards.curse)) {
    		   this.opponents.get(event.player.playerNumber).addVP(1);
    	   }
       }
   }



	@Override
	public Card[] chapel_cardsToTrash(MoveContext context) {
	    ArrayList<Card> ret = new ArrayList<Card>();
	    ArrayList<Card> temphand = this.hand.toArrayListClone();
	    
	    
	    for (int i = 0; i < 4; i++) {
	    	Card card = this.getCardToTrash(temphand, DiscardOption.NonDestructive);
	    	if (card != null) {
	    		ret.add(card);
	    		temphand.remove(card);
	    	} else {
	    		break;
	    	}
	    }
	    
	    if (ret.size() > 0) {
	    	return ret.toArray(new Card[ret.size()]);
	    }
	    
	    return null;
	}



	@Override
	public Card[] vault_cardsToDiscardForCard(MoveContext context) {
		return this.getCardsToDiscard(hand.toArrayListClone(), 2, DiscardOption.NonDestructive).toArray(new Card[2]);
	}


	@Override
	public Card trader_cardToTrash(MoveContext context) {
	    return this.getCardToTrash(DiscardOption.Destructive);
	}



	@Override
	public boolean trader_shouldGainSilverInstead(MoveContext context, Card card) {
		ArrayList<Card> temp = new ArrayList<Card>();
		temp.add(card);
		if (card == this.getCardToTrash(temp, DiscardOption.SemiDestructive)) {
			return true;
		}
	    return false;
	}



	@Override
	public WatchTowerOption watchTower_chooseOption(MoveContext context, Card card) {
	    if(this.shouldTrash(card)) {
	        return WatchTowerOption.Trash;
	    }
	    
	    if(isOnlyVictory(card)) {
	        return WatchTowerOption.Normal;
	    }
	    
	    return WatchTowerOption.TopOfDeck;
	}
	
	private int getCardNameCount(ArrayList<Card> deck) {
		ArrayList<String> names = new ArrayList<String>();
		
		for (Card card : deck) {
			if (!names.contains(card.toString())) {
				names.add(card.toString());
			}
			
		}
		
		return names.size();
	}
	
	private boolean needMorePotion(ArrayList<Card> deck) {
		if (this.strategyPossibleCards.size() > 0) {
			if (this.strategyCard.costPotion()) {
				switch (strategy) {
				case SingleAction:
				case DoubleAction:
					return (Util.getCardCount(deck, Cards.potion) == 0);
				case NoAction:
					return false;
				case MultiAction:
					if (Util.getCardCount(deck, Cards.potion) < 1) {
						return true;
					}
					if ((Util.getCardCount(deck, Cards.alchemist) > 1) && (deck.size() / 5 > Util.getCardCount(deck, Cards.potion))) {
						return true;
					}
					break;
				default: 
					break;
				}
			}
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private int needPotion() { // TODO must be tested
		float needpotion = 0;
		
		if (this.strategyPossibleCards.isEmpty()) {
			return 0;
		}
		
		for (ActionCard ac : this.strategyPossibleCards) {
			if (ac.costPotion() && needpotion < 1) {
				needpotion = 1;
			}
			if (ac.equals(Cards.alchemist)) {
				needpotion = (getAllCards().size() / (6 + (Util.getCardCount(getAllCards(), Cards.alchemist) * 3)));
			}
		}
		return Math.round(needpotion);
	}
	
	@Override
	public Card[] steward_cardsToTrash(MoveContext context) {
		ArrayList<Card> temphand = this.hand.toArrayListClone();
		
		Card[] ret = new Card[2];
		
		ret[0] = this.getCardToTrash(temphand, this.getAllCards(), DiscardOption.Destructive);
		temphand.remove(ret[0]);
		
		ret[1] = this.getCardToTrash(temphand, this.getAllCards(), DiscardOption.Destructive);
		
		return ret;
	}

	@Override
	public StewardOption steward_chooseOption(MoveContext context) {
		ArrayList<Card> temphand = hand.toArrayListClone();
		
		Card[] ret = { null, null };
		
		ret[0] = this.getCardToTrash(temphand, DiscardOption.SemiDestructive);
		if (ret[0] != null ) {
			temphand.remove(ret[0]);
			
			ret[1] = this.getCardToTrash(temphand, DiscardOption.SemiDestructive);
			
			if (ret[1] != null) {
				return StewardOption.TrashCards;
			}
		}

		if ((getMyAddActions() > 1) || (this.getMoneyPerCard(deck.toArrayList()) > 1)) {
			return StewardOption.AddCards;
		}

		return StewardOption.AddGold;
	}

	private int winningBy(MoveContext context) {
		int maxVP = this.opponents.maxVP();
		return ( maxVP > -1000 ? this.getVPTotalValue(context) - maxVP : 0);
	}

	@Override
	public boolean duchess_shouldGainBecauseOfDuchy(MoveContext context) {
	    return this.strategyPossibleCards.contains(Cards.duchess);
	}

	@Override
	public Card bishop_cardToTrash(MoveContext context) {
	    return this.getCardToTrash(DiscardOption.NonDestructive);
	}



	@Override
	public Card bishop_cardToTrashForVictoryTokens(MoveContext context) {
		if (inHand(Cards.curse)) {
			return Cards.curse;
		}
		if (inHand(Cards.estate)) {
			return Cards.estate;
		}
	    return this.getCardToTrash(DiscardOption.Destructive);
	}

	private Card getCardToTrash(DiscardOption destructive) {
		return getCardToTrash(this.hand.toArrayListClone(), this.getAllCards(), destructive);
	}

	@Override
	public int ambassador_returnToSupplyFromHand(MoveContext context, Card card) {
	    ArrayList<Card> temphand = this.hand.toArrayListClone();
	    
	    this.log("ambassador_returnToSupplyFromHand: current hand");
	    Card zero = this.advisorGeneral(context, this.getCurrencyTotal(temphand), false, false);
	    
	    temphand.remove(this.getCardToTrash(temphand, DiscardOption.Destructive));
	    this.log("ambassador_returnToSupplyFromHand: -1 card");
	    Card one = this.advisorGeneral(context, this.getCurrencyTotal(temphand), false, false);
	    
	    temphand.remove(this.getCardToTrash(temphand, DiscardOption.Destructive));
	    this.log("ambassador_returnToSupplyFromHand: -2 cards");
	    Card two = this.advisorGeneral(context, this.getCurrencyTotal(temphand), false, false);
	    
	    if (zero != null && two != null && zero.equals(two)) {
	    	return 2;
	    } else if (zero != null && one != null && zero.equals(one)) {
    		return 1;
    	} else if (one != null && two != null && one.equals(two)) {
    		return 2;
    	} else {
    		return 0;
    	}
	}



	@Override
	public Card ambassador_revealedCard(MoveContext context) {
	    return this.getCardToTrash(DiscardOption.Destructive);
	}

	public Card lookout_cardToDiscard(MoveContext context, Card[] cards) {
		ArrayList<Card> temp = new ArrayList<Card>();
		
		for (Card c : cards) {
			temp.add(c);
		}
		this.log("lookout_cardToDiscard: " + temp);
		
		return this.getCardToDiscard(temp, DiscardOption.Destructive);
	}



	public Card lookout_cardToTrash(MoveContext context, Card[] cards) {
		ArrayList<Card> temp = new ArrayList<Card>();
		
		for (Card c : cards) {
			temp.add(c);
		}
		
		this.log("lookout_cardToTrash: " + temp);
		return this.getCardToTrash(temp, DiscardOption.Destructive);
	}
	
	private boolean shouldReEvaluateStrategy() {
		if (this.strategy == StrategyOption.Nothing) {
			return true;
		}
		
		if (this.redefineStrategy) {
			return true;
		}
		
		if (this.opponents != null && this.opponents.getIsAttacking() && this.strategyCard != null && !this.strategyCard.isAttack()) {
			return true;
		}
		
		if (this.strategy == StrategyOption.Minion) {
			return false;
		}
		
		int available = 0;
		for (Card c : this.strategyPossibleCards) {
			available += game.pileSize(c);
		}
		return (available == 0);
	}
	
	private ArrayList<Card> getStrategyCardsInDeck(MoveContext context, boolean onlyTerminals) {
		
		ArrayList<Card> ret = new ArrayList<Card>();
		
		for (Card card : getAllCards()) {
			if (this.strategyPossibleCards.contains(card)) {
				if (!isCantrip((ActionCard) card) || !onlyTerminals) {
					ret.add(card);
				}
			}
		}
		
		return ret;
	}
	
	private ArrayList<Card> getTerminalsInDeck(MoveContext context) {
		ArrayList<Card> ret = new ArrayList<Card>();
		
		for (Card card : getAllCards()) {
			if (card instanceof ActionCard) {
				if (((ActionCard) card).getAddActions() <= 0) {
					ret.add(card);
				}
			}
		}
		
		return ret;
	}
	
	private float getCardsPerHand(MoveContext context) {
		int addcards = 0;
		
		for (Card card : getAllCards()) {
			if (card instanceof ActionCard) {
				addcards += ((ActionCard) card).getAddCards();
			}
		}
		
		return (addcards / 5) + 5;
	}



	@Override
	public Card haggler_cardToObtain(MoveContext context, int maxCost, boolean potion) {
		this.log("haggler_cardToObtain");
	    if (maxCost < 0)
	        return null;
	    else
	    	return this.advisorGeneral(context, maxCost, false, true);
	}
	
	private Card advisorPlayAction(ArrayList<Card> hand) {
		this.log("advisorPlayAction: " + hand);
		ArrayList<ActionCard> ac = new ArrayList<ActionCard>();
		for (Card card : hand) {
			if (card instanceof ActionCard) {
				ac.add((ActionCard) card);
			}
		}
		ArrayList<Card> temphand = new ArrayList<Card>(hand);
		
		if (ac.size() > 0) {
			if (ac.contains(Cards.kingsCourt)) {
				temphand.remove(Cards.kingsCourt);
				Card temp = advisorPlayAction(temphand);
				if (temp != null) {
					return hand.get(hand.indexOf(Cards.kingsCourt));
				}
			}
			if (ac.contains(Cards.throneRoom)) {
				temphand.remove(Cards.throneRoom);
				Card temp = advisorPlayAction(temphand);
				if (temp != null) {
					return hand.get(hand.indexOf(Cards.throneRoom));
				}
			}
			
			for (ActionCard a : ac) {
				if (knownTrashingCards.contains(ac)) {
					if ((this.getDeckSize() < 6) || (getCardToTrash(DiscardOption.SemiDestructive) == null) || (a.equals(Cards.masquerade))) {
						ac.remove(a);
					}
				}
			}
			
			for (ActionCard a : ac) {
				if (isCantrip(a)) {
					return a;
				}
			}

			for (ActionCard a : ac) {
				if (a.getAddActions() > 0) {
					return a;
				}
			}
			
			ActionCard bestcoin = (ActionCard) Cards.village;
			ActionCard bestcards = (ActionCard) Cards.militia;
			
			for (ActionCard a : ac) {
				if (a.getAddGold() > bestcoin.getAddGold()) {
					bestcoin = a;
				}
				if (a.getAddCards() > bestcards.getAddCards()) {
					bestcards = a;
				}
			}

			if ((bestcoin.getAddGold() > (bestcards.getAddCards() * getMoneyPerCard(this.deck.toArrayList()))) && (!bestcoin.equals(Cards.village))) {
				return bestcoin;
			}
			if (!bestcards.equals(Cards.militia)) {
				return bestcards;
			}
			
			return ac.get(rand.nextInt(ac.size()));
		}
		
		return null;
	}



	@Override
	public boolean illGottenGains_gainCopper(MoveContext context) {
		int gold = this.getCurrencyTotal(this.hand.toArrayListClone());
		this.log("illGottenGains_gainCopper: evaluating with " + gold + " gold");
	    return (this.advisorGeneral(context, gold, false, false) != this.advisorGeneral(context, gold+1, false, false));
	}
	
	@Override
	public TournamentOption tournament_chooseOption(MoveContext context) {
	    for(Card c : VDomPlayerPatrick.knownPrizeCards) {
	        if(c.isPrize() && context.getPileSize(c) > 0) {
	        	this.log("tournament_chooseOption: prize");
	            return TournamentOption.GainPrize;
	        }
	    }
    	this.log("tournament_chooseOption: duchy");
	    return TournamentOption.GainDuchy;
	}



	@Override
	public Card tournament_choosePrize(MoveContext context) {
	    for(Card c : VDomPlayerPatrick.knownPrizeCards) {
	        if(c.isPrize() && context.getPileSize(c) > 0) {
	        	this.log("tournament_choosePrize: " + c);
	            return c;
	        }
	    }
		this.log("tournament_choosePrize: nothing");
	    return null;
	}



	public Card courtyard_cardToPutBackOnDeck(MoveContext context) {
		HashMap<Card, Card> options = new HashMap<Card, Card>(); // this list will contain card to discard as key and best card to obtain with the rest as value
		ArrayList<Card> list = this.hand.toArrayListClone();
		Card ret = this.hand.get(0);
		
		// let's see what we can buy with each of the cards in hand removed
		for (Card c : list) { 
			ArrayList<Card> temp = new ArrayList<Card>(list);
			temp.remove(c);
			Card a = this.advisorGeneral(context, this.getCurrencyTotal(temp) + context.getCoinAvailableForBuy(), false, false);
			if (a != null) {
				options.put(a, c);
			}
		}
		
		this.log("courtyard_cardToPutBackOnDeck: " + options);

		
		// if we can buy a good card with one of the cards removed, we will do that
		if (options.containsKey(Cards.colony)) {
			this.log("courtyard_cardToPutBackOnDeck: can buy colony without " + options.get(Cards.colony));
			return this.hand.get(options.get(Cards.colony));
			//return options.get(Cards.colony);
		}
		if (options.containsKey(Cards.platinum)) {
			this.log("courtyard_cardToPutBackOnDeck: can buy platinum without " + options.get(Cards.platinum));
			return this.hand.get(options.get(Cards.platinum));
		}
		if (options.containsKey(Cards.province)) {
			this.log("courtyard_cardToPutBackOnDeck: can buy province without " + options.get(Cards.province));
			return this.hand.get(options.get(Cards.province));
		}
		if (options.containsKey(Cards.gold)) {
			this.log("courtyard_cardToPutBackOnDeck: can buy gold without " + options.get(Cards.gold));
			return this.hand.get(options.get(Cards.gold));
		}

		
	    if (this.hand.contains(Cards.gold)) {
	    	this.log("courtyard_cardToPutBackOnDeck: gold");
	    	return this.hand.get(Cards.gold);
	    }
	    if (this.hand.contains(Cards.silver)) {
	    	this.log("courtyard_cardToPutBackOnDeck: silver");
	    	return this.hand.get(Cards.silver);
	    }
	    if (this.hand.contains(Cards.copper)) {
	    	this.log("courtyard_cardToPutBackOnDeck: copper");
	    	return this.hand.get(Cards.copper);
	    }
	    
	    return ret;
	}
	
	@Override
	public boolean scryingPool_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
	    boolean discard = this.shouldDiscard(card);
	    
	    if (targetPlayer == this) {
	    	this.log("scryingPool_shouldDiscard: " + (discard ? "discard " : "keep ") + "my " + card );
	    	return discard;
	    } else {
	    	this.log("scryingPool_shouldDiscard: " + (!discard ? "discard " : "keep ") + "opponents " + card );
	    	return !discard;
	    }
	}



	@Override
	public MinionOption minion_chooseOption(MoveContext context) {
		int inhand = Util.getCardCount(hand, Cards.minion);
		if (this.strategy == StrategyOption.Minion) {
			int coins = this.getCurrencyTotal(this.hand.toArrayListClone()) + context.getCoinAvailableForBuy();
			int limit = 8;
			if (game.pileSize(Cards.minion) > 0) {
				limit = 5;
			}
			this.log("minion_chooseOption: inhand=" + inhand + "; coins: " + coins);
			
			if (inhand > 0) {
				return Player.MinionOption.AddGold;
			}
			
			if (inhand*2 + coins + 2 >= limit) {
				return Player.MinionOption.AddGold;
			} else {
				return Player.MinionOption.RolloverCards;
			}
		}
		
		if (inhand > 0) {
			return Player.MinionOption.AddGold;
		}
		
	    if (context.getCoinAvailableForBuy() >= 5) {
	        return Player.MinionOption.AddGold;
	    }
	
	    if (context.getPlayer().getHand().size() < 3) {
	        return Player.MinionOption.RolloverCards;
	    }
	    return Player.MinionOption.AddGold;
	}



	@Override
	public Card masquerade_cardToPass(MoveContext context) {
	    return this.getCardToTrash(DiscardOption.Destructive);
	}



	@Override
	public Card masquerade_cardToTrash(MoveContext context) {
	    return this.getCardToTrash(DiscardOption.NonDestructive);
	}



	@Override
	public Card ironworks_cardToObtain(MoveContext context) {
	    return this.advisorGeneral(context, 4, false, true);
	}



	@Override
	public boolean duchess_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
	    return this.shouldDiscard(card);
	}



	@Override
	public Card tradeRoute_cardToTrash(MoveContext context) {
	    return this.getCardToTrash(DiscardOption.Destructive);
	}


	@Override
	public Card rogue_cardToGain(MoveContext context) {
		
		
		
		// TODO Auto-generated method stub
		return super.rogue_cardToGain(context);
	}



	@Override
	public Card rogue_cardToTrash(MoveContext context, ArrayList<Card> canTrash) {
		return this.getCardToTrash(canTrash, DiscardOption.Destructive);
	}



	@Override
	public Card pillage_opponentCardToDiscard(MoveContext context, ArrayList<Card> handCards) {
		// TODO Auto-generated method stub
		return Util.randomCard(handCards);
	}
}