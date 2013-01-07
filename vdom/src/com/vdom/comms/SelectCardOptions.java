package com.vdom.comms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.Cards;
import com.vdom.core.MoveContext;

/**
 * Gives information about cards that are selected by the player from the table (piles, hand, play)
 *
 * This clas gives information about the constrains set on the selection of cards, e.g. what type, cost, from which place
 *
 */
public class SelectCardOptions implements Serializable {
	private static final long serialVersionUID = -1473106875075390348L;

    public enum ActionType {
        REVEAL, DISCARD, DISCARDFORCOIN, DISCARDFORCARD, GAIN, TRASH, NAMECARD, OPPONENTDISCARD
    }

    public enum PickType {
        SELECT(""),
        SELECT_WITH_ALL(" T"), 
        SELECT_IN_ORDER(" T"),
        PLAY (""),
        PLAY_IN_ORDER(" 1"),
		BUY (""),
		DISCARD (" D"),
		KEEP (" K"),
		GIVE (" P"),
		TRASH (" X"),
		UPGRADE ("X"),
		MINT ("M"),
		SWINDLE ("");

	    private final String indicator;
	    PickType(String indicator) {
	        this.indicator= indicator;
	    }
	    public String indicator() { return indicator; }
	}
	
    public PickType pickType = PickType.SELECT;

	public int defaultCardSelected = -1;
	public boolean fromHand = false;
	public int count = Integer.MAX_VALUE;
	public boolean exactCount = false;
	public boolean ordered = false;
	public boolean isNonRats = false;

	public boolean fromTable = false;
	public boolean isBuyPhase = false;
	public boolean allowEmpty = false;
	public int minCost = Integer.MIN_VALUE;
	public int maxCost = Integer.MAX_VALUE;
	public int maxCostWithoutPotion = Integer.MAX_VALUE;
	public int potionCost = -1;
	public boolean fromPrizes = false;

	public boolean isAction = false;
	public boolean isReaction = false;
	public boolean isTreasure = false;
    public boolean isNonTreasure = false;
	public boolean isVictory = false;
	public boolean isNonVictory = false;
	public boolean isAttack = false;
	public boolean isNonShelter = false;
	public String passString = null;
	public String header = null;
	public ArrayList<Integer> allowedCards = new ArrayList<Integer>();

//	public SelectCardOptions setType(SelectType s) {selectType = s; return this;}
	public SelectCardOptions setHeader(String s) {header = s; return this;}
	public SelectCardOptions setPassable(String s) {passString = s; return this;}
    public SelectCardOptions setPickType(PickType pickType) {this.pickType = pickType;return this;}
	public SelectCardOptions fromHand() {fromHand = true; return this;}
	public SelectCardOptions defaultCardSelected(int c) {defaultCardSelected = c; return this;}
	public SelectCardOptions isNonShelter() {isNonShelter = true; return this;}
	public SelectCardOptions isNonRats() {isNonRats = true; return this;}
	public SelectCardOptions ordered() {ordered = true; this.pickType = PickType.SELECT_IN_ORDER; return this;}
	public SelectCardOptions setCount(int c) {count = c; return this;}
	public SelectCardOptions exactCount() {exactCount = true; return this;}

	public SelectCardOptions fromTable() {fromTable = true;isNonShelter=true;count=1;exactCount=true; return this;}
	public SelectCardOptions isBuy() {isBuyPhase= true; this.pickType = PickType.BUY; return this;}
	public SelectCardOptions allowEmpty() {allowEmpty = true; return this;}
    public SelectCardOptions fromPrizes() {fromPrizes = true; return this;}
	public SelectCardOptions minCost(int c) {minCost = c; return this;}
	public SelectCardOptions maxCost(int c) {maxCost = c; maxCostWithoutPotion = c; return this;}
	public SelectCardOptions exactCost(int c) {minCost = c; maxCost = c; maxCostWithoutPotion = c; return this;}
	public SelectCardOptions potionCost(int c) {potionCost = c; return this;}
	public SelectCardOptions maxCostWithoutPotion() {maxCostWithoutPotion = maxCost + (maxCost < Integer.MAX_VALUE && potionCost > 0 ? 1 : 0); return this;}

    public SelectCardOptions isAction() {isAction = true; return this;}
	public SelectCardOptions isReaction() {isReaction = true; return this;}
	public SelectCardOptions isTreasure() {isTreasure = true; return this;}
    public SelectCardOptions isNonTreasure() {isNonTreasure = true; return this;}
	public SelectCardOptions isVictory() {isVictory = true; return this;}
	public SelectCardOptions isNonVictory() {isNonVictory = true; return this;}
	public SelectCardOptions isAttack() {isAttack = true; return this;}

	public SelectCardOptions allowedCards(int[] is) {
		for (int i : is)
			addValidCard(i);
		return this;
	}
	
    public PickType getPickType() {
        return pickType;
    }
	
	public boolean cardInList(int card) {
		if (allowedCards.size() == 0)
			return true;
		return allowedCards.contains(new Integer(card));
	}
	
	// Return the number of cards that have matched the filter
	public int getAllowedCardCount() {
		return allowedCards.size();
	}
	
	public void addValidCard(int card) {
		allowedCards.add(new Integer(card));
	}
	
	public boolean checkValid(MyCard c) {
		return checkValid(c, 0);
	}
	
	public boolean checkValid(MyCard c, int cost) {

		if ((maxCost >= 0) && (cost > maxCost )) return false;
		if ((minCost >= 0) && (cost < minCost)) return false;

		if (isAction && !c.isAction) return false;
		if (isReaction && !c.isReaction) return false;
		if (isTreasure && !c.isTreasure) return false;
		if (isNonTreasure && c.isTreasure) return false;
		if (isVictory && !c.isVictory) return false;
		if (isNonVictory && c.isVictory) return false;
		if (isAttack && !c.isAttack) return false;
		if (isNonShelter && c.isShelter) return false;
		if (isReaction && !c.isReaction) return false;
		if (fromPrizes && !c.isPrize) return false; 
		//if (fromPrizes && !c.isPrize && !fromTable) return false; 
		if (potionCost == 0 && c.costPotion) return false;
		if (maxCost == minCost && potionCost > 0 && !c.costPotion) return false;
		if (!cardInList(c.id)) return false;

		return true;
	}

	public boolean checkValid(Card c) {
		return checkValid(c, 0);
	}

	public boolean checkValid(Card c, int cost) {

		if ((maxCost >= 0) && (cost > (c.costPotion() ? maxCost : maxCostWithoutPotion))) return false;
		if ((minCost >= 0) && (cost < minCost)) return false;
		
		if (isReaction && !(Cards.isReaction(c))) return false;
		if (isTreasure && !(c instanceof TreasureCard)) return false;
		if (isNonTreasure && (c instanceof TreasureCard)) return false;
		if (isVictory && !(c instanceof VictoryCard)) return false;
		if (isNonVictory && (c instanceof VictoryCard)) return false;
		if (fromPrizes && !c.isPrize()) return false; 
		if (potionCost == 0 && c.costPotion()) return false;
		if (maxCost == minCost && potionCost > 0 && !c.costPotion()) return false;
		if (isNonRats && c.equals(Cards.rats)) return false;
		if (isNonShelter && c.isShelter()) return false;
		
		if (c instanceof ActionCard) {
			if (isAttack && !(((ActionCard) c).isAttack())) return false;
		} else {
			if (isAction || isAttack) return false;
		}
		if (isBuyPhase && !Cards.isSupplyCard(c)) return false; 

		return true;
	}
	public boolean isPassable() {
		return (passString != null && !passString.trim().equals(""));
	}

	public String potionString() {
		String potionString = "";
		if (potionCost == 1) {
        	potionString = "p";
        } else if (potionCost > 1) {
        	potionString = "p" + potionCost;
        }
	    return potionString;
	}
}
