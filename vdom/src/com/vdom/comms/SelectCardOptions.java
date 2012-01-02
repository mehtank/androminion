package com.vdom.comms;

import java.io.Serializable;
import java.util.ArrayList;

public class SelectCardOptions implements Serializable {
	private static final long serialVersionUID = -1473106875075390348L;

//	public enum SelectType {
//		GETACTION, GETBUY, DOACTION, ATTACK
//	};
//
//	public SelectType selectType;
	
    // Select button text constants
	public static final String SELECT = "Select"; 
    public static final String BUY = "  Buy  "; 
    public static final String PLAY = " Play "; 
    public static final String DISCARD = "Discard"; 
    public static final String KEEP = "Keep"; 
    public static final String GIVE = "Give"; 
    public static final String TRASH = "Trash"; 
    public static final String UPGRADE = "Upgrade"; 
    public static final String MINT = "Mint"; 
    public static final String SWINDLE = "Swindle"; 

    public static final String SELECT_WITH_ALL = "All";
    
    public String buttonText = SELECT;

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
	public int quarriesPlayed = 0;
	public boolean buyPhase = false;
	public int actionsPlayed = 0;
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
	public SelectCardOptions quarriesPlayed(int i) {quarriesPlayed = i; return this;}
	public SelectCardOptions allowedCards(int[] is) {
		for (int i : is)
			addValidCard(i);
		return this;
	}
	
	public SelectCardOptions buttonText(String buttonText) { this.buttonText = buttonText; return this; };
    public String getButtonText() { return buttonText; };
	
	public boolean cardInList(int card) {
		if (allowedCards.size() == 0)
			return true;
		return allowedCards.contains(new Integer(card));
	}
	
	public void addValidCard(int card) {
		allowedCards.add(new Integer(card));
	}
	
	public boolean checkValid(MyCard c) {
	    int costModifier = 0;
	    costModifier += (c.isAction ? (2 * quarriesPlayed) : 0);
        
	    if(buyPhase && c.name.equals("Peddler")) {
	        costModifier += actionsPlayed * 2;
	    }
		if ((maxCost >= 0) && (c.cost > maxCost + costModifier)) return false;
		if ((minCost >= 0) && (c.cost < minCost + costModifier)) return false;
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
