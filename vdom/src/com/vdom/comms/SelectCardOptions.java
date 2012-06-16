package com.vdom.comms;

import java.io.Serializable;
import java.util.ArrayList;

import com.vdom.core.Cards;

public class SelectCardOptions implements Serializable {
	private static final long serialVersionUID = -1473106875075390348L;

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

	public boolean fromHand = false;
	public boolean fromTable = false;
	public boolean fromPrizes = false;
	public int maxCost = -1;
	public int minCost = -1;
	public int potionCost = -1;
	public boolean isAction = false;
	public boolean isReaction = false;
	public boolean isTreasure = false;
    public boolean isNonTreasure = false;
	public boolean isVictory = false;
	public String passString = null;
	public boolean ordered = false;
	public ArrayList<Integer> allowedCards = new ArrayList<Integer>();

//	public SelectCardOptions setType(SelectType s) {selectType = s; return this;}
	public SelectCardOptions fromHand() {fromHand = true; return this;}
	public SelectCardOptions fromTable() {fromTable = true; return this;}
    public SelectCardOptions fromPrizes() {fromPrizes = true; return this;}
	public SelectCardOptions isAction() {isAction = true; return this;}
	public SelectCardOptions isReaction() {isReaction = true; return this;}
	public SelectCardOptions isTreasure() {isTreasure = true; return this;}
    public SelectCardOptions isNonTreasure() {isNonTreasure = true; return this;}
	public SelectCardOptions isVictory() {isVictory = true; return this;}
	public SelectCardOptions setPassable(String s) {passString = s; return this;}
	public SelectCardOptions ordered() {ordered = true; return this;}
	public SelectCardOptions maxCost(int c) {maxCost = c; return this;}
	public SelectCardOptions minCost(int c) {minCost = c; return this;}
	public SelectCardOptions potionCost(int c) {potionCost = c; return this;}
	public SelectCardOptions allowedCards(int[] is) {
		for (int i : is)
			addValidCard(i);
		return this;
	}
	
    public SelectCardOptions setPickType(PickType pickType) {
        this.pickType = pickType;
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
	
	public void addValidCard(int card) {
		allowedCards.add(new Integer(card));
	}
	
	public boolean checkValid(MyCard c, int cost) {
		if ((maxCost >= 0) && (cost > maxCost)) return false;
		if ((minCost >= 0) && (cost < minCost)) return false;
		if (isAction && !c.isAction) return false;
		if (isReaction && !c.isReaction) return false;
		if (isTreasure && !c.isTreasure) return false;
		if (isNonTreasure && c.isTreasure) return false;
		if (isVictory && !c.isVictory) return false;
		if (isReaction && !c.isReaction) return false;
		if (fromPrizes && !c.isPrize && !fromTable) return false; 
		if (potionCost == 0 && c.costPotion) return false;
		if (potionCost > 0 && maxCost == minCost && !c.costPotion) return false;
		if (!cardInList(c.id)) return false;

		return true;
	}
	public boolean isPassable() {
		return (passString != null && !passString.trim().equals(""));
	}
}
