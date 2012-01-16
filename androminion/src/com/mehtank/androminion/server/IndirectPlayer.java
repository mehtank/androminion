package com.mehtank.androminion.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.Context;

import com.mehtank.androminion.R;
import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.comms.SelectCardOptions;
import com.vdom.core.CardList;
import com.vdom.core.Cards;
import com.vdom.core.MoveContext;
import com.vdom.core.Player;
import com.vdom.core.QuickPlayPlayer;
/**
 * Class that you can use to play remotely.
 */
public abstract class IndirectPlayer extends QuickPlayPlayer {	

    static Context top;
    
	public abstract Card nameToCard(String o);
    public abstract int cardToInt(Card card);
    public abstract int[] cardArrToIntArr(Card[] cards);
	public Card nameToCard(String o, Card[] cards) {
		for (Card c : cards)
			if (c.getName().equals(o))
				return c;
		return null;
	}

	public Card localNameToCard(String o, Card[] cards) {
		for (Card c : cards)
			if (Strings.getCardName(c).equals(o))
				return c;
		return null;
	}
	
	abstract protected Card[] pickCards(MoveContext context, String header, SelectCardOptions sco, int count, boolean exact);
    abstract protected String selectString(MoveContext context, String header, String[] s); 
    abstract protected int[] orderCards(MoveContext context, int[] cards); 
    abstract protected int[] orderCards(MoveContext context, int[] cards, String header); 
    
    protected Card pickACard(MoveContext context, String header, SelectCardOptions sco) {
    	Card[] cs = pickCards(context, header, sco, 1, true);
    	if (cs == null)
    		return null;
    	return cs[0];
    }
    /*
    protected Card pickACard(InternalMoveContext context, String header, Card[] cards, String passString) {
    	SelectCardOptions sco = new SelectCardOptions();
        sco.setPassable(passString);

        for (Card card : cards)
    		sco.addValidCard(cardToInt(card));
    	
    	return pickACard(context, header, sco);
    }
    */

    final int ANYFROMHAND = 0;
    final int ACTIONFROMHAND = 1;
    final int TREASUREFROMHAND = 2;
    final int VICTORYFROMHAND = 3;
    final int NONTREASUREFROMHAND = 4;
    
    final String NOTPASSABLE = null;
    
    @Override
    public boolean isAi() {
        return false;
    }
    
    public String getString(int id) {
        return Strings.getString(id);
    }
    
    public String getCardName(Card card) {
        return Strings.getCardName(card);
    }

    public String getRevealString(Card cardResponsible) {
        return Strings.format(R.string.card_to_reveal, getCardName(cardResponsible));
    }
    
    public String getGainString(Card cardResponsible) {
        return Strings.format(R.string.card_to_gain, getCardName(cardResponsible));
    }
    
    public String getTrashString(Card cardResponsible) {
        return Strings.format(R.string.card_to_trash, getCardName(cardResponsible));
    }
    
    public String getDiscardString(Card cardResponsible) {
        return Strings.format(R.string.card_to_discard, getCardName(cardResponsible));
    }
    
    public String selectString(MoveContext context, Card cardResponsible, String[] s) {
        return selectString(context, Strings.format(R.string.card_options_header, getCardName(cardResponsible)), s);
    }
    
    public String selectString(MoveContext context, int resId, Card cardResponsible, String[] s) {
        return selectString(context, getString(resId) + " [" + getCardName(cardResponsible) + "]", s);
    }
    
    public Card[] getFromHand(MoveContext context, String header, String passString, int type, int count, boolean exact, boolean ordered) {
        return getFromHand(context, header, passString, type, count, exact, ordered, SelectCardOptions.SELECT);
    }
    
    public Card[] getFromHand(MoveContext context, String header, String passString, int type, int count, boolean exact, boolean ordered, String buttonText) {
        return getFromHand(context, header, passString, type, count, exact, ordered, buttonText, false);
    }
    
    public Card[] getFromHand(MoveContext context, String header, String passString, int type, int count, boolean exact, boolean ordered, String buttonText, boolean selectingActionToPlay) {
    	CardList hand = getHand();
        if (hand.size() == 0)
            return null;

		SelectCardOptions sco = new SelectCardOptions()
			.fromHand().buttonText(buttonText);
		
        sco.setPassable(passString);
//		if (passString != null && !passString.trim().equals(""))
//			sco.isPassable();

        if (ordered)
			sco.ordered();

		ArrayList<Card> handList = new ArrayList<Card>();
		
		boolean onlyThroneRoomsOrKingsCourts = true;
		
        for (Card card : hand) {
        	handList.add(card);
        	switch (type) {
        	case ACTIONFROMHAND:
        		if (card instanceof ActionCard) {
        		    if(onlyThroneRoomsOrKingsCourts && !card.equals(Cards.throneRoom) && !card.equals(Cards.kingsCourt)) {
        		        onlyThroneRoomsOrKingsCourts = false;
        		    }
        			sco.isAction().addValidCard(cardToInt(card));
        		}
            	break;
        	case TREASUREFROMHAND:
        		if (card instanceof TreasureCard)
        			sco.isTreasure().addValidCard(cardToInt(card));
            	break;
        	case VICTORYFROMHAND:
                if (card instanceof VictoryCard && !(card.equals(Cards.curse))) 
        			sco.isVictory().addValidCard(cardToInt(card));
            	break;
            case NONTREASUREFROMHAND:
                if (!(card instanceof TreasureCard))
                    sco.isNonTreasure().addValidCard(cardToInt(card));
                break;
            default:
        		sco.addValidCard(cardToInt(card));
        	}
        }
        
//        if(selectingActionToPlay && onlyThroneRoomsOrKingsCourts) {
//            return null;
//        }
        
        if (sco.allowedCards.size() == 0)
        	return null;

        String str = "";
        switch (type) {
            case ACTIONFROMHAND:
               if(count == 1)
                   str = Strings.format(R.string.select_one_action_from_hand, header);
               else if(exact) 
                   str = Strings.format(R.string.select_exactly_x_actions_from_hand, "" + count, header);
               else
                   str = Strings.format(R.string.select_up_to_x_actions_from_hand, "" + count, header);
               break;
            case TREASUREFROMHAND:
                if(count == 1)
                    str = Strings.format(R.string.select_one_treasure_from_hand, header);
                else if(exact) 
                    str = Strings.format(R.string.select_exactly_x_treasures_from_hand, "" + count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_treasures_from_hand, "" + count, header);
                break;
            case VICTORYFROMHAND:
                if(count == 1)
                    str = Strings.format(R.string.select_one_victory_from_hand, header);
                else if(exact) 
                    str = Strings.format(R.string.select_exactly_x_victorys_from_hand, "" + count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_victorys_from_hand, "" + count, header);
                break;
            case NONTREASUREFROMHAND:
                if(count == 1)
                    str = Strings.format(R.string.select_one_nontreasure_from_hand, header);
                else if(exact) 
                    str = Strings.format(R.string.select_exactly_x_nontreasures_from_hand, "" + count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_nontreasures_from_hand, "" + count, header);
                break;
            default:
                if(count == 1)
                    str = Strings.format(R.string.select_one_card_from_hand, header);
                else if(exact) 
                    str = Strings.format(R.string.select_exactly_x_cards_from_hand, "" + count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_cards_from_hand, "" + count, header);
        }

        Card[] tempCards = pickCards(context, str, sco, count, exact);

        if (tempCards == null)
        	return null;
        
        // Hack to notify that "All" was selected
        if(tempCards.length == 0) {
            return tempCards;
        }
        
        for (int i=0; i<tempCards.length; i++)
        	for (Card c : handList) 
        		if (c.equals(tempCards[i])) {
                	tempCards[i] = c;
                	handList.remove(c);
        			break;
        		}

        return tempCards;
    }
    
    public Card[] getAnyFromHand(MoveContext context, String header, String passString, int count, boolean exact, String buttonText) {
        return getFromHand(context, header, passString, ANYFROMHAND, count, exact, false, buttonText);
    }
    public Card[] getAnyFromHand(MoveContext context, String header, String passString, int count, boolean exact) {
    	return getFromHand(context, header, passString, ANYFROMHAND, count, exact, false);
    }
    public Card[] getOrderedFromHand(MoveContext context, String header, String passString, int count, boolean exact) {
    	return getFromHand(context, header, passString, ANYFROMHAND, count, exact, true);
    }
    public Card getAnyFromHand(MoveContext context, String header, String passString) {
        return getAnyFromHand(context, header, passString, SelectCardOptions.SELECT);

    }
    public Card getAnyFromHand(MoveContext context, String header, String passString, String buttonText) {     
        Card[] cs = getFromHand(context, header, passString, ANYFROMHAND, 1, true, false, buttonText);
        if (cs == null)
            return null;
        return cs[0];

    }
    public Card getActionFromHand(MoveContext context, String header, String passString, String buttonText) {
        Card[] cs = getFromHand(context, header, passString, ACTIONFROMHAND, 1, true, false, buttonText);
        if (cs == null)
            return null;
        return cs[0];
    }
    public Card getActionFromHand(MoveContext context, String header, String passString) {
    	Card[] cs = getFromHand(context, header, passString, ACTIONFROMHAND, 1, true, false, SelectCardOptions.SELECT);
    	if (cs == null)
    		return null;
    	return cs[0];
    }
    public Card getTreasureFromHand(MoveContext context, String header, String passString) {
        Card[] cs = getFromHand(context, header, passString, TREASUREFROMHAND, 1, true, false, SelectCardOptions.SELECT);
        if (cs == null)
            return null;
        return cs[0];
    }
    public Card getTreasureFromHand(MoveContext context, String header, String passString, String buttonText) {
    	Card[] cs = getFromHand(context, header, passString, TREASUREFROMHAND, 1, true, false, buttonText);
    	if (cs == null)
    		return null;
    	return cs[0];
    }
    public Card getNonTreasureFromHand(MoveContext context, String header, String passString, String buttonText) {
        Card[] cs = getFromHand(context, header, passString, NONTREASUREFROMHAND, 1, true, false, buttonText);
        if (cs == null)
            return null;
        return cs[0];
    }
    public Card getVictoryFromHand(MoveContext context, String header, String passString) {
    	Card[] cs = getFromHand(context, header, passString, VICTORYFROMHAND, 1, true, false);
    	if (cs == null)
    		return null;
    	return cs[0];
    }

    public Card getTreasureFromTable(MoveContext context, String header, int maxCost, String passString) {
    	return getTreasureFromTable(context, header, maxCost, passString, false);
    }
    
    public Card getTreasureFromTable(MoveContext context, String header, int maxCost, String passString, boolean potion) {
        Card[] cards = context.getCardsInPlay();
        SelectCardOptions sco = new SelectCardOptions()
        	.fromTable()
        	.isTreasure();
        
        sco.setPassable(passString);
//		if (passString != null && !passString.trim().equals(""))
//			sco.isPassable();
        
        for (Card card : cards)
        	if (card.getCost(context) <= maxCost)
        		if (potion || (!potion && !card.costPotion()))
        		  sco.addValidCard(cardToInt(card));

        if (sco.allowedCards.size() == 0)
        	return null;
        
        if (maxCost < Integer.MAX_VALUE)
        	maxCost -= context.cardCostModifier;

        sco.maxCost(maxCost);
        
        return pickACard(context, Strings.format(R.string.select_treasure, "" + maxCost + (potion? "p":""), header), sco);
    }
    
    public Card getFromTable(MoveContext context, String header, int maxCost, int minCost, boolean isBuy, String passString) {
        return getFromTable(context, header, maxCost, minCost, isBuy, passString, SelectCardOptions.SELECT);
    }
    
    public Card getNonVictoryFromTable(MoveContext context, String header, int maxCost, boolean isBuy, String passString, int potionCost) {
        return getFromTable(context, header, maxCost, Integer.MIN_VALUE, isBuy, passString, SelectCardOptions.SELECT, false, false, potionCost);
    }
    
    public ActionCard getActionFromTable(MoveContext context, String header, int maxCost, String passString) {
        return (ActionCard) getFromTable(context, header, maxCost, Integer.MIN_VALUE, false, passString, SelectCardOptions.SELECT, true, true);
    }
    
    public Card getFromTable(MoveContext context, String header, int maxCost, int minCost, boolean isBuy, String passString, String buttonText) {
        return getFromTable(context, header, maxCost, minCost, isBuy, passString, buttonText, false, true);
    }
    
    public Card getFromTable(MoveContext context, String header, int maxCost, int minCost, boolean isBuy, String passString, String buttonText, boolean actionOnly, boolean victoryAllowed) {
        return getFromTable(context, header, maxCost, minCost, isBuy, passString, buttonText, actionOnly, victoryAllowed, -1);
    }

    public Card getFromTable(MoveContext context, String header, int maxCost, int minCost, boolean isBuy, String passString, String buttonText, boolean actionOnly, boolean victoryAllowed, int potionCost) {
        return getFromTable(context, header, maxCost, minCost, isBuy, passString, buttonText, actionOnly, victoryAllowed, potionCost, false);
    }
    
    public Card getFromTable(MoveContext context, String header, int maxCost, int minCost, boolean isBuy, String passString, String buttonText, boolean actionOnly, boolean victoryAllowed, int potionCost, boolean includePrizes) {
        Card[] cards = context.getCardsInPlay();
        SelectCardOptions sco = new SelectCardOptions()
        	.fromTable();
        if (includePrizes) {
        	sco.fromPrizes();
        }
        
        if(isBuy)
            sco.buyPhase = true;

        if(context != null && context.getPlayedCards() != null)
            sco.actionsInPlay = context.getActionCardsInPlayThisTurn();

        sco.setPassable(passString);
        
//		if (passString != null && !passString.trim().equals(""))
//			sco.isPassable();
        
        if(actionOnly) {
            sco.isAction = true;
        }
        
        if(isBuy) {
            sco.buttonText = SelectCardOptions.BUY;
            potionCost = context.getPotions();
        }
        
		for (Card card : cards) {
            if ((!isBuy && card.getCost(context, false) <= maxCost) ||
        		(isBuy && context.canBuy(card, maxCost))) {
        		if (card.getCost(context) >= minCost) {
        		    if(victoryAllowed || !(card instanceof VictoryCard)) {
        		        if(potionCost == -1 || (potionCost == 0 && !card.costPotion()) || (potionCost > 0 && card.costPotion()) || (potionCost > 0 && !card.costPotion() && maxCost != minCost)) {
        		            sco.addValidCard(cardToInt(card));
        		        }
        		    }
        		}
        	}
		}

        if (sco.allowedCards.size() == 0)
        	return null;

        if (maxCost < Integer.MAX_VALUE)
        	maxCost -= context.cardCostModifier;
        if (minCost > 0)
        	minCost -= context.cardCostModifier;

        sco.maxCost(maxCost)
           .minCost(minCost)
           .potionCost(potionCost)
           .quarriesPlayed(context.getQuarriesPlayed());

        boolean quarries = (context.getQuarriesPlayed() > 0);
        String selectString;
        
        String potions = "";
        if (potionCost == 1) {
        	potions = "p";
        } else if (potionCost > 1) {
        	potions = "p" + potionCost;
        }
//        if(potionCost > 0) {
//            potions = "p";
//        }
//        for(int i = 0; i < potionCost; i++) {
//            potions += "p";
//        }
        
        if (minCost == maxCost)
            if(quarries)
                selectString = Strings.format(R.string.select_from_table_exact_quarries, "" + maxCost + potions, "" + (maxCost + (2 * context.getQuarriesPlayed())) + potions, header);
            else
                selectString = Strings.format(R.string.select_from_table_exact, "" + maxCost + potions, header);
        else if ((minCost <= 0) && (maxCost < Integer.MAX_VALUE))
            if(quarries)
                selectString = Strings.format(R.string.select_from_table_max_quarries, "" + maxCost + potions, "" + (maxCost + (2 * context.getQuarriesPlayed())) + potions, header);
            else
                selectString = Strings.format(R.string.select_from_table_max, "" + maxCost + potions, header);
        else if (maxCost < Integer.MAX_VALUE)
            if(quarries)
                selectString = Strings.format(R.string.select_from_table_between_quarries, "" + minCost + potions, "" + maxCost + potions, "" + (maxCost + (2 * context.getQuarriesPlayed())) + potions, header);
            else
                selectString = Strings.format(R.string.select_from_table_between, "" + minCost + potions, "" + maxCost + potions, header);
        else if (minCost > 0)
            selectString = Strings.format(R.string.select_from_table_min, "" + minCost + potions, header);
        else
            selectString = Strings.format(R.string.select_from_table, header);

        return pickACard(context, selectString, sco);
    }
    public Card getFromTable(MoveContext context, String header, int maxCost, int minCost, String passString, String buttonText) {
    	return getFromTable(context, header, maxCost, minCost, false, passString, buttonText);
    }
    public Card getFromTable(MoveContext context, String header, int maxCost, int minCost, String passString) {
        return getFromTable(context, header, maxCost, minCost, false, passString);
    }
    public Card getFromTable(MoveContext context, String header, int maxCost, boolean isBuy, String passString) {
    	return getFromTable(context, header, maxCost, Integer.MIN_VALUE, isBuy, passString, SelectCardOptions.SELECT);
    }
    public Card getFromTable(MoveContext context, String header, int maxCost, boolean isBuy, String passString, String buttonText) {
        return getFromTable(context, header, maxCost, Integer.MIN_VALUE, isBuy, passString, buttonText);
    }
    public Card getFromTable(MoveContext context, String header, int maxCost, String passString) {
    	return getFromTable(context, header, maxCost, Integer.MIN_VALUE, false, passString);
    }
    
    public int selectInt(MoveContext context, String header, int maxInt, int errVal) {
    	ArrayList<String> options = new ArrayList<String>();
    	for (int i=0; i<=maxInt; i++)
    		options.add("" + i);

    	String o = selectString(context, header, options.toArray(new String[0]));

		try {
			return Integer.parseInt(o);
		} catch (NumberFormatException e) { 
			return errVal;
		}
    }
    public boolean selectBoolean(MoveContext context, Card c, String strTrue, String strFalse) {
        String header = getCardName(c);
        return selectBoolean(context, header, strTrue, strFalse);
    }
    
    public boolean selectBoolean(MoveContext context, String header, String strTrue, String strFalse) {
    	String [] s = new String [] {strTrue, strFalse};
    	String r = selectString(context, header, s);
    	if (strTrue.equals(r))
    		return true;
    	return false;
    }
    public boolean selectBooleanWithCard(MoveContext context, String header, Card c, String strTrue, String strFalse) {
    	return selectBoolean(context, header + Strings.getCardName(c), strTrue, strFalse);
    }
    public boolean selectBooleanCardRevealed(MoveContext context, Card cardResponsible, Card cardRevealed, String strTrue, String strFalse) {
        String c1 = getCardName(cardResponsible);
        String c2 = getCardName(cardRevealed);
        String query = Strings.format(R.string.card_revealed, c1, c2);
        return selectBoolean(context, query, strTrue, strFalse);
    }
    
    public boolean selectBooleanCardRevealedAndPlayer(MoveContext context, Card cardResponsible, Card cardRevealed, Player p, String strTrue, String strFalse) {
        String c1 = getCardName(cardResponsible);
        String c2 = getCardName(cardRevealed);
        String query = Strings.format(R.string.card_revealed_from_player, p.getPlayerName(), c1, c2);
        return selectBoolean(context, query, strTrue, strFalse);
    }
    
    public Card doAction(MoveContext context) {
        Card[] cs = getFromHand(context, getString(R.string.part_play), getString(R.string.none), ACTIONFROMHAND, 1, true, false, SelectCardOptions.PLAY, true);
        if (cs == null)
            return null;
        return cs[0];
    }
    
    public Card doBuy(MoveContext context) {
    	return getFromTable(context, getString(R.string.part_buy), context.getCoinAvailableForBuy(), true, getString(R.string.end_turn));
    }

    // ////////////////////////////////////////////
    // Card interactions - cards from the base game
    // ////////////////////////////////////////////
    public Card workshop_cardToObtain(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_workshop_cardToObtain(context)) {
            return super.workshop_cardToObtain(context);
        }
    	return getFromTable(context, getGainString(Cards.workshop), 4, Integer.MIN_VALUE, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, 0);
    }

    public Card feast_cardToObtain(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_feast_cardToObtain(context)) {
            return super.feast_cardToObtain(context);
        }
    	return getFromTable(context, getGainString(Cards.feast), 5, Integer.MIN_VALUE, false, NOTPASSABLE, SelectCardOptions.SELECT, false ,true, 0);
    }

    public Card remodel_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_remodel_cardToTrash(context)) {
            return super.remodel_cardToTrash(context);
        }
		return getAnyFromHand(context, getTrashString(Cards.remodel), NOTPASSABLE, SelectCardOptions.UPGRADE);
	}

    @Override
    public Card remodel_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_remodel_cardToObtain(context, maxCost, potion)) {
            return super.remodel_cardToObtain(context, maxCost, potion);
        }
        
        return getFromTable(context, getGainString(Cards.remodel), maxCost, Integer.MIN_VALUE, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, potion?1:0);
    }

    public TreasureCard mine_treasureToObtain(MoveContext context, int maxCost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_mine_treasureToObtain(context, maxCost, potion)) {
            return super.mine_treasureToObtain(context, maxCost, potion);
        }
    	return (TreasureCard) getTreasureFromTable(context, getString(R.string.mine_part), maxCost, NOTPASSABLE, potion);
    }

    public Card[] militia_attack_cardsToKeep(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_militia_attack_cardsToKeep(context)) {
            return super.militia_attack_cardsToKeep(context);
        }
		return getAnyFromHand(context, getString(R.string.militia_part), NOTPASSABLE, 3, true, SelectCardOptions.KEEP);
	}

    public boolean chancellor_shouldDiscardDeck(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_chancellor_shouldDiscardDeck(context)) {
            return super.chancellor_shouldDiscardDeck(context);
        }
		return selectBoolean(context, getCardName(Cards.chancellor), getString(R.string.chancellor_query), getString(R.string.pass));
	}

    public TreasureCard mine_treasureFromHandToUpgrade(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_mine_treasureFromHandToUpgrade(context)) {
            return super.mine_treasureFromHandToUpgrade(context);
        }
		return (TreasureCard) getTreasureFromHand(context, getCardName(Cards.mine), NOTPASSABLE, SelectCardOptions.UPGRADE);
    }

    public Card[] chapel_cardsToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_chapel_cardsToTrash(context)) {
            return super.chapel_cardsToTrash(context);
        }
		return getAnyFromHand(context, getTrashString(Cards.chapel), getString(R.string.none), 4, false, SelectCardOptions.TRASH);
	}

    public Card[] cellar_cardsToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_cellar_cardsToDiscard(context)) {
            return super.cellar_cardsToDiscard(context);
        }
		return getAnyFromHand(context, getDiscardString(Cards.cellar), getString(R.string.none), getHand().size(), false, SelectCardOptions.DISCARD);
	}

    public boolean library_shouldKeepAction(MoveContext context, ActionCard action) {
        if(context.isQuickPlay() && shouldAutoPlay_library_shouldKeepAction(context, action)) {
            return super.library_shouldKeepAction(context, action);
        }
		return selectBooleanCardRevealed(context, Cards.library, action, getString(R.string.keep), getString(R.string.discard));
	}

    public boolean spy_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_spy_shouldDiscard(context, targetPlayer, card)) {
            return super.spy_shouldDiscard(context, targetPlayer, card);
        }
		return selectBooleanCardRevealedAndPlayer(context, Cards.spy, card, targetPlayer, getString(R.string.discard), getString(R.string.replace));
	}
    
    // ////////////////////////////////////////////
    // Card interactions - cards from the Intrigue
    // ////////////////////////////////////////////
    public Card[] secretChamber_cardsToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_secretChamber_cardsToDiscard(context)) {
            return super.secretChamber_cardsToDiscard(context);
        }
		return getAnyFromHand(context, getDiscardString(Cards.secretChamber), getString(R.string.none), getHand().size(), false, SelectCardOptions.DISCARD);
	}

    public PawnOption[] pawn_chooseOptions(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_pawn_chooseOptions(context)) {
            return super.pawn_chooseOptions(context);
        }
    	PawnOption[] os = new PawnOption[2];

        LinkedHashMap<String, PawnOption> h = new LinkedHashMap<String, PawnOption>();
    	
    	h.put(getString(R.string.pawn_one), PawnOption.AddCard);
    	h.put(getString(R.string.pawn_two), PawnOption.AddAction);
    	h.put(getString(R.string.pawn_three), PawnOption.AddBuy);
    	h.put(getString(R.string.pawn_four), PawnOption.AddGold);

		String o1 = selectString(context, getString(R.string.pawn_option_one), h.keySet().toArray(new String[0]));
		os[0] = h.get(o1);
		h.remove(o1);
		String o2 = selectString(context, getString(R.string.pawn_option_one), h.keySet().toArray(new String[0]));
		os[1] = h.get(o2);
		return os;
	}

    public TorturerOption torturer_attack_chooseOption(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_torturer_attack_chooseOption(context)) {
            return super.torturer_attack_chooseOption(context);
        }
        LinkedHashMap<String, TorturerOption> h = new LinkedHashMap<String, TorturerOption>();
    	h.put(getString(R.string.torturer_option_one), TorturerOption.TakeCurse);
    	h.put(getString(R.string.torturer_option_two), TorturerOption.DiscardTwoCards);

		return h.get(selectString(context, Cards.torturer, h.keySet().toArray(new String[0])));
	}

    public StewardOption steward_chooseOption(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_steward_chooseOption(context)) {
            return super.steward_chooseOption(context);
        }
        LinkedHashMap<String, StewardOption> h = new LinkedHashMap<String, StewardOption>();
    	
    	h.put(getString(R.string.steward_option_one), StewardOption.AddCards);
    	h.put(getString(R.string.steward_option_two), StewardOption.AddGold);
    	h.put(getString(R.string.steward_option_three), StewardOption.TrashCards);

		return h.get(selectString(context, Cards.steward, h.keySet().toArray(new String[0])));
	}

    public Card swindler_cardToSwitch(MoveContext context, int cost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_swindler_cardToSwitch(context, cost, potion)) {
            return super.swindler_cardToSwitch(context, cost, potion);
        }
		return getFromTable(context, Strings.format(R.string.swindler_part, "" + cost), cost, cost, false, NOTPASSABLE, SelectCardOptions.SWINDLE, false, true, potion? 1: 0);
	}

    public Card[] steward_cardsToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_steward_cardsToTrash(context)) {
            return super.steward_cardsToTrash(context);
        }
		return getAnyFromHand(context, getTrashString(Cards.steward), NOTPASSABLE, Math.min(2, getHand().size()), true, SelectCardOptions.TRASH);
	}

    public Card[] torturer_attack_cardsToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_torturer_attack_cardsToDiscard(context)) {
            return super.torturer_attack_cardsToDiscard(context);
        }
		return getAnyFromHand(context, getDiscardString(Cards.torturer), NOTPASSABLE, Math.min(2, getHand().size()), true, SelectCardOptions.DISCARD);
	}

    public Card courtyard_cardToPutBackOnDeck(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_courtyard_cardToPutBackOnDeck(context)) {
            return super.courtyard_cardToPutBackOnDeck(context);
        }
		return getAnyFromHand(context, Strings.format(R.string.courtyard_part_top_of_deck, getCardName(Cards.courtyard)), NOTPASSABLE);
	}

    public boolean baron_shouldDiscardEstate(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_baron_shouldDiscardEstate(context)) {
            return super.baron_shouldDiscardEstate(context);
        }
		return selectBoolean(context, getCardName(Cards.baron), getString(R.string.baron_option_one), getString(R.string.baron_option_two));
	}

    public Card ironworks_cardToObtain(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_ironworks_cardToObtain(context)) {
            return super.ironworks_cardToObtain(context);
        }
    	return getFromTable(context, getCardName(Cards.ironworks), 4, Integer.MIN_VALUE, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, 0);
	}

    public Card masquerade_cardToPass(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_masquerade_cardToPass(context)) {
            return super.masquerade_cardToPass(context);
        }
		return getAnyFromHand(context, getString(R.string.masquerade_part), NOTPASSABLE, SelectCardOptions.GIVE);
	}
    
    public VictoryCard bureaucrat_cardToReplace(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_bureaucrat_cardToReplace(context)) {
            return super.bureaucrat_cardToReplace(context);
        }
		return (VictoryCard) getVictoryFromHand(context, getString(R.string.bureaucrat_part), NOTPASSABLE);
	}

    public Card masquerade_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_masquerade_cardToTrash(context)) {
            return super.masquerade_cardToTrash(context);
        }
		return getAnyFromHand(context, getTrashString(Cards.masquerade), getString(R.string.none), SelectCardOptions.TRASH);
	}

    public boolean miningVillage_shouldTrashMiningVillage(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_miningVillage_shouldTrashMiningVillage(context)) {
            return super.miningVillage_shouldTrashMiningVillage(context);
        }
		return selectBoolean(context, getCardName(Cards.miningVillage), getString(R.string.mining_village_option_one), getString(R.string.keep));
	}

    @Override
    public Card saboteur_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_saboteur_cardToObtain(context, maxCost, potion)) {
            return super.saboteur_cardToObtain(context, maxCost, potion);
        }
    	return getFromTable(context, getString(R.string.saboteur_part), maxCost, Integer.MIN_VALUE, false, getString(R.string.none), SelectCardOptions.SELECT, false, true, potion? 1:0);
	}

    public Card[] scout_orderCards(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_scout_orderCards(context, cards)) {
            return super.scout_orderCards(context, cards);
        }
    	ArrayList<Card> orderedCards = new ArrayList<Card>();
    	int[] order = orderCards(context, cardArrToIntArr(cards));
    	for (int i : order) 
    		orderedCards.add(cards[i]);
    	return orderedCards.toArray(new Card[0]);
	}

    public NoblesOption nobles_chooseOptions(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_nobles_chooseOptions(context)) {
            return super.nobles_chooseOptions(context);
        }
        LinkedHashMap<String, NoblesOption> h = new LinkedHashMap<String, NoblesOption>();
    	
    	h.put(getString(R.string.nobles_option_one), NoblesOption.AddCards);
    	h.put(getString(R.string.nobles_option_two), NoblesOption.AddActions);

		return h.get(selectString(context, Cards.nobles, h.keySet().toArray(new String[0])));
	}

    // Either return two cards, or null if you do not want to trash any cards.
    public Card[] tradingPost_cardsToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_tradingPost_cardsToTrash(context)) {
            return super.tradingPost_cardsToTrash(context);
        }
		return getAnyFromHand(context, getTrashString(Cards.tradingPost), NOTPASSABLE, Math.min(2, getHand().size()), true, SelectCardOptions.TRASH);
	}

    public Card wishingWell_cardGuess(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_wishingWell_cardGuess(context)) {
            return super.wishingWell_cardGuess(context);
        }
    	return getFromTable(context, getString(R.string.wishing_well_part), Integer.MAX_VALUE, Integer.MIN_VALUE, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, -1, true);
	}

    public Card upgrade_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_upgrade_cardToTrash(context)) {
            return super.upgrade_cardToTrash(context);
        }
		return getAnyFromHand(context, getTrashString(Cards.upgrade), NOTPASSABLE, SelectCardOptions.TRASH);
	}

    @Override
    public Card upgrade_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_upgrade_cardToObtain(context, exactCost, potion)) {
            return super.upgrade_cardToObtain(context, exactCost, potion);
        }
        return getFromTable(context, getGainString(Cards.upgrade), exactCost, exactCost, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, potion?1:0);
	}

    public MinionOption minion_chooseOption(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_minion_chooseOption(context)) {
            return super.minion_chooseOption(context);
        }
        LinkedHashMap<String, MinionOption> h = new LinkedHashMap<String, MinionOption>();
    	
    	h.put(getString(R.string.minion_option_one), MinionOption.AddGold);
    	h.put(getString(R.string.minion_option_two), MinionOption.RolloverCards);

		return h.get(selectString(context, Cards.minion, h.keySet().toArray(new String[0])));
	}

    public Card[] secretChamber_cardsToPutOnDeck(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_secretChamber_cardsToPutOnDeck(context)) {
            return super.secretChamber_cardsToPutOnDeck(context);
        }
		return getOrderedFromHand(context, getString(R.string.secretchamber_part), NOTPASSABLE, Math.min(2, getHand().size()), true);
	}

    // ////////////////////////////////////////////
    // Card interactions - cards from the Seaside
    // ////////////////////////////////////////////
    public Card[] ghostShip_attack_cardsToPutBackOnDeck(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_ghostShip_attack_cardsToPutBackOnDeck(context)) {
            return super.ghostShip_attack_cardsToPutBackOnDeck(context);
        }
		return getOrderedFromHand(context, getString(R.string.ghostship_part), NOTPASSABLE, getHand().size() - 3, true);
	}

    public Card salvager_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_salvager_cardToTrash(context)) {
            return super.salvager_cardToTrash(context);
        }
		return getAnyFromHand(context, getTrashString(Cards.salvager), NOTPASSABLE, SelectCardOptions.TRASH);
	}

    public int treasury_putBackOnDeck(MoveContext context, int treasuryCardsInPlay) {
        if(context.isQuickPlay() && shouldAutoPlay_treasury_putBackOnDeck(context, treasuryCardsInPlay)) {
            return super.treasury_putBackOnDeck(context, treasuryCardsInPlay);
        }
    	return selectInt(context, getString(R.string.treasury_number), treasuryCardsInPlay, 0);
	}

    public Card[] warehouse_cardsToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_warehouse_cardsToDiscard(context)) {
            return super.warehouse_cardsToDiscard(context);
        }
		return getAnyFromHand(context, getDiscardString(Cards.warehouse), NOTPASSABLE, Math.min(3, getHand().size()), true, SelectCardOptions.DISCARD);
	}

    public boolean pirateShip_takeTreasure(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_pirateShip_takeTreasure(context)) {
            return super.pirateShip_takeTreasure(context);
        }
    	int t = this.getPirateShipTreasure();
		return selectBoolean(context, getCardName(Cards.pirateShip), Strings.format(R.string.pirate_ship_option_one, "" + t), getString(R.string.pirate_ship_option_two));
	}

    public boolean nativeVillage_takeCards(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_nativeVillage_takeCards(context)) {
            return super.nativeVillage_takeCards(context);
        }
		return selectBoolean(context, getCardName(Cards.nativeVillage), getString(R.string.native_village_option_one), getString(R.string.native_village_option_two));
	}

    public Card smugglers_cardToObtain(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_smugglers_cardToObtain(context)) {
            return super.smugglers_cardToObtain(context);
        }
    	ArrayList<String> options = new ArrayList<String>();
        Card[] cards = context.getCardsObtainedByLastPlayer().toArray(new Card[0]);
    	for (Card c : cards)
    		if (c.getCost(context) <= 6 && !c.isPrize())
    			options.add(Strings.getCardName(c));
    	
    	if (options.size() > 0) {
    		String o = selectString(context, getString(R.string.smuggle_query), options.toArray(new String[0]));	
            return localNameToCard(o, cards);
    	} else
    		return null;
	}

    public Card island_cardToSetAside(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_island_cardToSetAside(context)) {
            return super.island_cardToSetAside(context);
        }
		return getAnyFromHand(context, getCardName(Cards.island), NOTPASSABLE);
	}

    public Card haven_cardToSetAside(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_haven_cardToSetAside(context)) {
            return super.haven_cardToSetAside(context);
        }
		return getAnyFromHand(context, getCardName(Cards.haven), NOTPASSABLE);
	}

    public boolean navigator_shouldDiscardTopCards(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_navigator_shouldDiscardTopCards(context, cards)) {
            return super.navigator_shouldDiscardTopCards(context, cards);
        }
		String header = "";
		for (Card c : cards) 
			header += getCardName(c) + ", ";
		header += "--";
		header = header.replace(", --", "");
		header = Strings.format(R.string.navigator_header, header);

        String option1 = getString(R.string.discard);
        String option2 = getString(R.string.navigator_option_two);
        
    	return selectBoolean(context, header, option1, option2);
	}

    public Card[] navigator_cardOrder(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_navigator_cardOrder(context, cards)) {
            return super.navigator_cardOrder(context, cards);
        }
    	ArrayList<Card> orderedCards = new ArrayList<Card>();
    	int[] order = orderCards(context, cardArrToIntArr(cards));
    	for (int i : order) {
    		orderedCards.add(cards[i]);
    	}
    	return orderedCards.toArray(new Card[0]);
	}

    public Card embargo_supplyToEmbargo(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_embargo_supplyToEmbargo(context)) {
            return super.embargo_supplyToEmbargo(context);
        }
    	return getFromTable(context, getCardName(Cards.embargo), Integer.MAX_VALUE, NOTPASSABLE);
	}

    // Will be passed all three cards
    public Card lookout_cardToTrash(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_lookout_cardToTrash(context, cards)) {
            return super.lookout_cardToTrash(context, cards);
        }
    	ArrayList<String> options = new ArrayList<String>();
    	for (Card c : cards)
    		options.add(Strings.getCardName(c));
    	
    	String o = selectString(context, R.string.lookout_query_trash, Cards.lookout, options.toArray(new String[0]));	
    	return localNameToCard(o, cards);
    }

    // Will be passed the two cards leftover after trashing one
    public Card lookout_cardToDiscard(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_lookout_cardToDiscard(context, cards)) {
            return super.lookout_cardToDiscard(context, cards);
        }
    	ArrayList<String> options = new ArrayList<String>();
    	for (Card c : cards)
    		options.add(Strings.getCardName(c));
    	
    	String o = selectString(context, R.string.lookout_query_discard, Cards.lookout, options.toArray(new String[0]));	
    	return localNameToCard(o, cards);
	}

    public Card ambassador_revealedCard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_ambassador_revealedCard(context)) {
            return super.ambassador_revealedCard(context);
        }
		return getAnyFromHand(context, getRevealString(Cards.ambassador), NOTPASSABLE);
	}

    public int ambassador_returnToSupplyFromHand(MoveContext context, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_ambassador_returnToSupplyFromHand(context, card)) {
            return super.ambassador_returnToSupplyFromHand(context, card);
        }
    	int numCards = 0;
    	for (Card c : getHand()) 
    		if (c.equals(card))
    			numCards++;
    	    
    	return selectInt(context, Strings.format(R.string.ambassador_query, getCardName(card)), Math.min(2, numCards), 0);
	}

    public boolean pearlDiver_shouldMoveToTop(MoveContext context, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_pearlDiver_shouldMoveToTop(context, card)) {
            return super.pearlDiver_shouldMoveToTop(context, card);
        }
        
        String option1 = getString(R.string.pearldiver_option_one);
        String option2 = getString(R.string.pearldiver_option_two);
		return selectBooleanCardRevealed(context, Cards.pearlDiver, card, option1, option2);
	}
    
    @Override
    public boolean explorer_shouldRevealProvince(MoveContext context) {
        if (context.isQuickPlay() && shouldAutoPlay_explorer_shouldRevealProvince(context)) {
        	super.explorer_shouldRevealProvince(context);
        }
    	return selectBoolean(context, Cards.explorer, Strings.getString(R.string.explorer_reveal), Strings.getString(R.string.pass));
    }
    
    public Card transmute_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_transmute_cardToTrash(context)) {
            return super.transmute_cardToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.transmute), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    public ArrayList<Card> apothecary_cardsForDeck(MoveContext context, ArrayList<Card> cards) {
        if(context.isQuickPlay() && shouldAutoPlay_apothecary_cardsForDeck(context, cards)) {
            return super.apothecary_cardsForDeck(context, cards);
        }
        
        ArrayList<Card> orderedCards = new ArrayList<Card>();
        int[] order = orderCards(context, cardArrToIntArr(cards.toArray(new Card[0])));
        for (int i : order) 
            orderedCards.add(cards.get(i));
        return orderedCards;
    }
    
    public boolean alchemist_backOnDeck(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_alchemist_backOnDeck(context)) {
            return super.alchemist_backOnDeck(context);
        }
        String option1 = getString(R.string.alchemist_option_one);
        String option2 = getString(R.string.alchemist_option_two);
        return selectBoolean(context, Cards.alchemist, option1, option2);
    }
    
    public TreasureCard herbalist_backOnDeck(MoveContext context, TreasureCard[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_herbalist_backOnDeck(context, cards)) {
            return super.herbalist_backOnDeck(context, cards);
        }
        ArrayList<String> options = new ArrayList<String>();
        for (Card c : cards)
            options.add(Strings.getCardName(c));
            
//        String none = getString(R.string.none);
//        options.add(none);
        String o = selectString(context, R.string.herbalist_query, Cards.herbalist, options.toArray(new String[0]));   
//        if(o.equals(none)) {
//            return null;
//        }
        
        return (TreasureCard) localNameToCard(o, cards);
    }
    
    public Card apprentice_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_apprentice_cardToTrash(context)) {
            return super.apprentice_cardToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.apprentice), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    public ActionCard university_actionCardToObtain(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_university_actionCardToObtain(context)) {
            return super.university_actionCardToObtain(context);
        }
        return (ActionCard) getFromTable(context, getString(R.string.university_part), 5, Integer.MIN_VALUE, false, getString(R.string.none), SelectCardOptions.SELECT, true, true, 0);
    }
    
    public boolean scryingPool_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_scryingPool_shouldDiscard(context, targetPlayer, card)) {
            return super.scryingPool_shouldDiscard(context, targetPlayer, card);
        }
        return selectBooleanCardRevealedAndPlayer(context, Cards.scryingPool, card, targetPlayer, getString(R.string.discard), getString(R.string.replace));
    }
    
    public ActionCard[] golem_cardOrder(MoveContext context, ActionCard[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_golem_cardOrder(context, cards)) {
            return super.golem_cardOrder(context, cards);
        }
        
        if(cards == null || cards.length < 2) {
            return cards;
        }
        
        ArrayList<String> options = new ArrayList<String>();
        for (Card c : cards)
            options.add(Strings.getCardName(c));
        
        String o = selectString(context, R.string.golem_first_action, Cards.golem, options.toArray(new String[0]));   
        Card c = localNameToCard(o, cards);
        if(c.equals(cards[0])) {
            return cards;
        }
        return new ActionCard[]{ cards[1], cards[0] };
    }
    
    @Override
    public Card bishop_cardToTrashForVictoryTokens(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_bishop_cardToTrashForVictoryTokens(context)) {
            return super.bishop_cardToTrashForVictoryTokens(context);
        }
        return getAnyFromHand(context, getString(R.string.bishop_part), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    @Override
    public Card bishop_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_bishop_cardToTrash(context)) {
            return super.bishop_cardToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.bishop), getString(R.string.none), SelectCardOptions.TRASH);
    }
    
    public Card contraband_cardPlayerCantBuy(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_contraband_cardPlayerCantBuy(context)) {
            return super.contraband_cardPlayerCantBuy(context);
        }
        return getFromTable(context, getCardName(Cards.contraband), Integer.MAX_VALUE, getString(R.string.none));
    }
    
    public Card expand_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_expand_cardToTrash(context)) {
            return super.expand_cardToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.expand), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    public Card expand_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_expand_cardToObtain(context, maxCost, potion)) {
            return super.expand_cardToObtain(context, maxCost, potion);
        }
        return getFromTable(context, getGainString(Cards.expand), maxCost, Integer.MIN_VALUE, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, potion?1:0);
    }
    
    public Card[] forge_cardsToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_forge_cardsToTrash(context)) {
            return super.forge_cardsToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.forge), getString(R.string.none), getHand().size(), false, SelectCardOptions.TRASH);
    }
    
    @Override
    public Card forge_cardToObtain(MoveContext context, int exactCost) {
        if(context.isQuickPlay() && shouldAutoPlay_forge_cardToObtain(context, exactCost)) {
            return super.forge_cardToObtain(context, exactCost);
        }
        return getFromTable(context, getGainString(Cards.forge), exactCost, exactCost, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, 0);
    }
    
    public Card[] goons_attack_cardsToKeep(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_goons_attack_cardsToKeep(context)) {
            return super.goons_attack_cardsToKeep(context);
        }
        return getAnyFromHand(context, getString(R.string.goons_part), NOTPASSABLE, 3, true, SelectCardOptions.KEEP);
    }
    
    public ActionCard kingsCourt_cardToPlay(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_kingsCourt_cardToPlay(context)) {
            return super.kingsCourt_cardToPlay(context);
        }
        return (ActionCard) getActionFromHand(context, getCardName(Cards.kingsCourt), getString(R.string.none), SelectCardOptions.PLAY);
    }
    
    public ActionCard throneRoom_cardToPlay(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_throneRoom_cardToPlay(context)) {
            return super.throneRoom_cardToPlay(context);
        }
        return (ActionCard) getActionFromHand(context, getCardName(Cards.throneRoom), getString(R.string.none), SelectCardOptions.PLAY);
    }
    
    
    public boolean loan_shouldTrashTreasure(MoveContext context, TreasureCard treasure) {
        if(context.isQuickPlay() && shouldAutoPlay_loan_shouldTrashTreasure(context, treasure)) {
            return super.loan_shouldTrashTreasure(context, treasure);
        }
        return selectBooleanCardRevealed(context, Cards.loan, treasure, getString(R.string.trash), getString(R.string.discard));
    }
    
    public TreasureCard mint_treasureToMint(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_mint_treasureToMint(context)) {
            return super.mint_treasureToMint(context);
        }
        return (TreasureCard) getTreasureFromHand(context, getCardName(Cards.mint), NOTPASSABLE, SelectCardOptions.MINT);
    }
    
    public boolean mountebank_attack_shouldDiscardCurse(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_mountebank_attack_shouldDiscardCurse(context)) {
            return super.mountebank_attack_shouldDiscardCurse(context);
        }
        
        return selectBoolean(context, getString(R.string.mountebank_query), getString(R.string.mountebank_option_one), getString(R.string.mountebank_option_two));
    }
    
    public Card[] rabble_attack_cardOrder(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_rabble_attack_cardOrder(context, cards)) {
            return super.rabble_attack_cardOrder(context, cards);
        }
        ArrayList<Card> orderedCards = new ArrayList<Card>();
        int[] order = orderCards(context, cardArrToIntArr(cards));
        for (int i : order) 
            orderedCards.add(cards[i]);
        return orderedCards.toArray(new Card[0]);
    }
    
    public boolean royalSeal_shouldPutCardOnDeck(MoveContext context, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_royalSeal_shouldPutCardOnDeck(context, card)) {
            return super.royalSeal_shouldPutCardOnDeck(context, card);
        }
        return selectBooleanCardRevealed(context, Cards.royalSeal, card, getString(R.string.top_of_deck), getString(R.string.discard));
    }
    
    public Card tradeRoute_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_tradeRoute_cardToTrash(context)) {
            return super.tradeRoute_cardToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.tradeRoute), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    public Card[] vault_cardsToDiscardForGold(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_vault_cardsToDiscardForGold(context)) {
            return super.vault_cardsToDiscardForGold(context);
        }
        return getAnyFromHand(context, getString(R.string.vault_part_discard_for_gold), getString(R.string.none), getHand().size(), false, SelectCardOptions.DISCARD);
    }
    
    public Card[] vault_cardsToDiscardForCard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_vault_cardsToDiscardForCard(context)) {
            return super.vault_cardsToDiscardForCard(context);
        }
        return getAnyFromHand(context, getString(R.string.vault_part_discard_for_card), getString(R.string.none), 2, true, SelectCardOptions.DISCARD);
    }
    
    public WatchTowerOption watchTower_chooseOption(MoveContext context, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_watchTower_chooseOption(context, card)) {
            return super.watchTower_chooseOption(context, card);
        }
        LinkedHashMap<String, WatchTowerOption> h = new LinkedHashMap<String, WatchTowerOption>();
        
        h.put(getString(R.string.watch_tower_option_one), WatchTowerOption.Normal);
        h.put(getString(R.string.trash), WatchTowerOption.Trash);
        h.put(getString(R.string.watch_tower_option_three), WatchTowerOption.TopOfDeck);

        return h.get(selectString(context, Strings.format(R.string.watch_tower_query, getCardName(card)), h.keySet().toArray(new String[0])));
    }
    
    public ArrayList<TreasureCard> treasureCardsToPlayInOrder(MoveContext context) {
        if(context.isQuickPlay()) {
            return super.treasureCardsToPlayInOrder(context);
        }
        int treasureCount = 0;
        for (Card card : getHand()) {
            if (card instanceof TreasureCard) {
                treasureCount++;
            }
        }

        Card[] cards = getFromHand(context, getString(R.string.use_for_money), getString(R.string.none), TREASUREFROMHAND, treasureCount, false, true, SelectCardOptions.SELECT_WITH_ALL);
        if (cards == null) {
            return null;
        }
        
        // Hack that tells us that "All" was selected
        if(cards.length == 0) {
            return super.treasureCardsToPlayInOrder(context);
        }

        ArrayList<TreasureCard> treasures = new ArrayList<TreasureCard>();
        for (int i = 0; i < cards.length; i++) {
            treasures.add((TreasureCard) cards[i]);
        }

        return treasures;
    }
    
    public Card hamlet_cardToDiscardForAction(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_hamlet_cardToDiscardForAction(context)) {
            return super.hamlet_cardToDiscardForAction(context);
        }
        return getAnyFromHand(context, getString(R.string.hamlet_part_discard_for_action), getString(R.string.none), SelectCardOptions.DISCARD);
    }
    
    public Card hamlet_cardToDiscardForBuy(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_hamlet_cardToDiscardForBuy(context)) {
            return super.hamlet_cardToDiscardForBuy(context);
        }
        return getAnyFromHand(context, getString(R.string.hamlet_part_discard_for_buy), getString(R.string.none), SelectCardOptions.DISCARD);
    }
    
    public Card hornOfPlenty_cardToObtain(MoveContext context, int maxCost) {
        if(context.isQuickPlay() && shouldAutoPlay_hornOfPlenty_cardToObtain(context, maxCost)) {
            return super.hornOfPlenty_cardToObtain(context, maxCost);
        }
        return getFromTable(context, getGainString(Cards.hornOfPlenty), maxCost, Integer.MIN_VALUE, true, getString(R.string.none), SelectCardOptions.SELECT, false, true, 0);
    }
    
    public Card[] horseTraders_cardsToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_horseTraders_cardsToDiscard(context)) {
            return super.horseTraders_cardsToDiscard(context);
        }
        
        int count = Math.min(2, getHand().size());
        if(count == 0) {
            return null;
        }
        
        return getAnyFromHand(context, getDiscardString(Cards.horseTraders), NOTPASSABLE, count, true, SelectCardOptions.DISCARD);
    }
    
    public JesterOption jester_chooseOption(MoveContext context, Player targetPlayer, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_jester_chooseOption(context, targetPlayer, card)) {
            return super.jester_chooseOption(context, targetPlayer, card);
        }
        
        LinkedHashMap<String, JesterOption> h = new LinkedHashMap<String, JesterOption>();
        
        h.put(getString(R.string.jester_option_one), JesterOption.GainCopy);
        h.put(Strings.format(R.string.jester_option_two, targetPlayer.getPlayerName()), JesterOption.GiveCopy);

        String header = Strings.format(R.string.card_revealed, getCardName(Cards.jester), getCardName(card));
        return h.get(selectString(context, header, h.keySet().toArray(new String[0])));
    }
    
    public Card remake_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_remake_cardToTrash(context)) {
            return super.remake_cardToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.remake), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    public Card remake_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_remake_cardToObtain(context, exactCost, potion)) {
            return super.remake_cardToObtain(context, exactCost, potion);
        }
        return getFromTable(context, getGainString(Cards.remake), exactCost, exactCost, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, potion?1:0);
    }
    

    public boolean tournament_shouldRevealProvince(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_tournament_shouldRevealProvince(context)) {
            return super.tournament_shouldRevealProvince(context);
        }
        return selectBoolean(context, Cards.tournament, Strings.getString(R.string.tournament_reveal), Strings.getString(R.string.tournament_option_one));
    	
    }


    public TournamentOption tournament_chooseOption(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_tournament_chooseOption(context)) {
            return super.tournament_chooseOption(context);
        }
        LinkedHashMap<String, TournamentOption> h = new LinkedHashMap<String, TournamentOption>();
        
        h.put(getString(R.string.tournament_option_two), TournamentOption.GainPrize);
        h.put(getString(R.string.tournament_option_three), TournamentOption.GainDuchy);

        return h.get(selectString(context, Cards.tournament, h.keySet().toArray(new String[0])));
    }
    
    public Card tournament_choosePrize(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_tournament_choosePrize(context)) {
            return super.tournament_choosePrize(context);
        }

        SelectCardOptions sco = new SelectCardOptions()
            .fromPrizes()
            .buttonText(SelectCardOptions.SELECT)
            .setPassable(getString(R.string.none));

        for (Card card : context.getCardsInPlay()) {
            if (card.isPrize() && context.getPileSize(card) > 0) {
                sco.addValidCard(cardToInt(card));
            }
        }

        return pickACard(context, getString(R.string.select_prize), sco);
    }
    
    public Card[] youngWitch_cardsToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_youngWitch_cardsToDiscard(context)) {
            return super.youngWitch_cardsToDiscard(context);
        }
        return getAnyFromHand(context, getDiscardString(Cards.youngWitch), NOTPASSABLE, 2, true, SelectCardOptions.DISCARD);
    }
    
    public Card[] followers_attack_cardsToKeep(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_followers_attack_cardsToKeep(context)) {
            return super.followers_attack_cardsToKeep(context);
        }
        return getAnyFromHand(context, getString(R.string.followers_part), NOTPASSABLE, 3, true, SelectCardOptions.KEEP);
    }
    
    public TrustySteedOption[] trustySteed_chooseOptions(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_trustySteed_chooseOptions(context)) {
            return super.trustySteed_chooseOptions(context);
        }
        LinkedHashMap<String, TrustySteedOption> h = new LinkedHashMap<String, TrustySteedOption>();

        h.put(getString(R.string.trusty_steed_option_two), TrustySteedOption.AddCards);
        h.put(getString(R.string.trusty_steed_option_one), TrustySteedOption.AddActions);
        h.put(getString(R.string.trusty_steed_option_three), TrustySteedOption.AddGold);
        h.put(getString(R.string.trusty_steed_option_four), TrustySteedOption.GainSilvers);

        TrustySteedOption[] choices = new TrustySteedOption[2];
        choices[0] = h.remove(selectString(context, Cards.trustySteed, h.keySet().toArray(new String[0])));
        choices[1] = h.remove(selectString(context, Cards.trustySteed, h.keySet().toArray(new String[0])));
        return choices;
    }

    public TreasureCard thief_treasureToTrash(MoveContext context, TreasureCard[] treasures) {
        if(context.isQuickPlay() && shouldAutoPlay_thief_treasureToTrash(context, treasures)) {
            return super.thief_treasureToTrash(context, treasures);
        }
        ArrayList<String> options = new ArrayList<String>();
        for (TreasureCard c : treasures)
            options.add(Strings.getCardName(c));

        if (options.size() > 0) {
            String o = selectString(context, R.string.treasure_to_trash, Cards.thief, options.toArray(new String[0])); 
            return (TreasureCard) localNameToCard(o, treasures);
        } else {
            return null;
        }
    }

    public TreasureCard[] thief_treasuresToGain(MoveContext context, TreasureCard[] treasures) {
        if(context.isQuickPlay() && shouldAutoPlay_thief_treasuresToGain(context, treasures)) {
            return super.thief_treasuresToGain(context, treasures);
        }
        ArrayList<String> options = new ArrayList<String>();
        options.add(getString(R.string.none));
        for (TreasureCard c : treasures)
            options.add(Strings.getCardName(c));

        if (options.size() > 0) {
            ArrayList<TreasureCard> toGain = new ArrayList<TreasureCard>();
            String o = null;

            while (options.size() > 1 && !getString(R.string.none).equals(o = selectString(context, R.string.thief_query, Cards.thief, options.toArray(new String[0])))) {
                toGain.add((TreasureCard) localNameToCard(o, treasures));
                options.remove(o);
            }

            return toGain.toArray(new TreasureCard[0]);
        } else {
            return null;
        }
    }

    public TreasureCard pirateShip_treasureToTrash(MoveContext context, TreasureCard[] treasures) {
        if(context.isQuickPlay() && shouldAutoPlay_pirateShip_treasureToTrash(context, treasures)) {
            return super.pirateShip_treasureToTrash(context, treasures);
        }
        ArrayList<String> options = new ArrayList<String>();
        for (TreasureCard c : treasures)
            options.add(Strings.getCardName(c));

        if (options.size() > 0) {
            String o = selectString(context, R.string.treasure_to_trash, Cards.pirateShip, options.toArray(new String[0])); 
            return (TreasureCard) localNameToCard(o, treasures);
        } else {
            return null;
        }
    }

    @Override
    public boolean tunnel_shouldReveal(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_tunnel_shouldReveal(context)) {
            return super.tunnel_shouldReveal(context);
        }
        return selectBoolean(context, getString(R.string.tunnel_query), getString(R.string.tunnel_option_one), getString(R.string.pass));
    }
    
    @Override
    public boolean duchess_shouldGainBecauseOfDuchy(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_duchess_shouldGainBecauseOfDuchy(context)) {
            return super.duchess_shouldGainBecauseOfDuchy(context);
        }
        return selectBoolean(context, getString(R.string.duchess_query), getString(R.string.duchess_option_one), getString(R.string.pass));
    }
    
    @Override
    public boolean duchess_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_duchess_shouldDiscardCardFromTopOfDeck(context, card)) {
            return super.duchess_shouldDiscardCardFromTopOfDeck(context, card);
        }
        return !selectBooleanCardRevealed(context, Cards.duchess, card, getString(R.string.duchess_play_option_one), getString(R.string.discard));
    }
    
    @Override
    public boolean foolsGold_shouldTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_foolsGold_shouldTrash(context)) {
            return super.foolsGold_shouldTrash(context);
        }
        return selectBoolean(context, Cards.foolsGold, getString(R.string.fools_gold_option_one), getString(R.string.pass));
    }
    
    @Override
    public boolean trader_shouldGainSilverInstead(MoveContext context, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_trader_shouldGainSilverInstead(context, card)) {
            return super.trader_shouldGainSilverInstead(context, card);
        }
        return !selectBoolean(context, Cards.trader, Strings.format(R.string.trader_gain, getCardName(card)), Strings.format(R.string.trader_gain_instead_of, getCardName(Cards.silver), getCardName(card)));
    }
    
    @Override
    public Card trader_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_trader_cardToTrash(context)) {
            return super.trader_cardToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.trader), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    @Override
    public Card oasis_cardToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_oasis_cardToDiscard(context)) {
            return super.oasis_cardToDiscard(context);
        }
        return getAnyFromHand(context, getDiscardString(Cards.oasis), NOTPASSABLE, SelectCardOptions.DISCARD);
    }
    
    @Override
    public Card develop_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_develop_cardToTrash(context)) {
            return super.develop_cardToTrash(context);
        }
        
        return getAnyFromHand(context, getTrashString(Cards.develop), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    @Override
    public Card develop_lowCardToGain(MoveContext context, int cost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_develop_lowCardToGain(context, cost, potion)) {
            return super.develop_lowCardToGain(context, cost, potion);
        }
        return getFromTable(context, getGainString(Cards.develop), cost, cost, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, potion?1:0);
    }
    
    @Override
    public Card develop_highCardToGain(MoveContext context, int cost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_develop_highCardToGain(context, cost, potion)) {
            return super.develop_highCardToGain(context, cost, potion);
        }
        return getFromTable(context, getGainString(Cards.develop), cost, cost, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, potion?1:0);
    }
    
    @Override
    public Card[] develop_orderCards(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_develop_orderCards(context, cards)) {
            return super.develop_orderCards(context, cards);
        }
        ArrayList<Card> orderedCards = new ArrayList<Card>();
        int[] order = orderCards(context, cardArrToIntArr(cards), getString(R.string.card_order_on_deck));
        for (int i : order) 
            orderedCards.add(cards[i]);
        return orderedCards.toArray(new Card[0]);
    }
    
    @Override
    public TreasureCard nobleBrigand_silverOrGoldToTrash(MoveContext context, TreasureCard[] silverOrGoldCards) {
        if(context.isQuickPlay() && shouldAutoPlay_nobleBrigand_silverOrGoldToTrash(context, silverOrGoldCards)) {
            return super.nobleBrigand_silverOrGoldToTrash(context, silverOrGoldCards);
        }

        int highIndex;
        int lowIndex;
        if(silverOrGoldCards[0].getCost(context) >= silverOrGoldCards[1].getCost(context)) {
            highIndex = 0;
            lowIndex = 1;
        }
        else {
            highIndex = 1;
            lowIndex = 0;
        }
        
        if(selectBoolean(context, Strings.format(R.string.noble_brigand_query, context.getAttackedPlayer()), Strings.getCardName(silverOrGoldCards[lowIndex]), Strings.getCardName(silverOrGoldCards[highIndex]))) {
            return silverOrGoldCards[lowIndex];
        }
        else {
            return silverOrGoldCards[highIndex];
        }
    }
    
    @Override
    public boolean jackOfAllTrades_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
        if(context.isQuickPlay() && shouldAutoPlay_jackOfAllTrades_shouldDiscardCardFromTopOfDeck(context, card)) {
            super.jackOfAllTrades_shouldDiscardCardFromTopOfDeck(context, card);
        }
        return !selectBooleanCardRevealed(context, Cards.jackOfAllTrades, card, getString(R.string.jack_of_all_trades_option_one), getString(R.string.discard));
    }
    
    @Override
    public Card jackOfAllTrades_nonTreasureToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_jackOfAllTrades_nonTreasureToTrash(context)) {
            super.jackOfAllTrades_nonTreasureToTrash(context);
        }
        
        return getNonTreasureFromHand(context, getTrashString(Cards.jackOfAllTrades), getString(R.string.none), SelectCardOptions.TRASH);
    }
    
    @Override
    public TreasureCard spiceMerchant_treasureToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_spiceMerchant_treasureToTrash(context)) {
            return super.spiceMerchant_treasureToTrash(context);
        }
        return (TreasureCard) getTreasureFromHand(context, getTrashString(Cards.spiceMerchant), getString(R.string.none), SelectCardOptions.TRASH);
    }
    
    @Override
    public SpiceMerchantOption spiceMerchant_chooseOption(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_spiceMerchant_chooseOption(context)) {
            return super.spiceMerchant_chooseOption(context);
        }
        LinkedHashMap<String, SpiceMerchantOption> h = new LinkedHashMap<String, SpiceMerchantOption>();
        
        h.put(getString(R.string.spice_merchant_option_one), SpiceMerchantOption.AddCardsAndAction);
        h.put(getString(R.string.spice_merchant_option_two), SpiceMerchantOption.AddGoldAndBuy);
        
        String o = selectString(context, getCardName(Cards.spiceMerchant), h.keySet().toArray(new String[0]));
        return h.get(o);
    }
    
    @Override
    public Card[] embassy_cardsToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_embassy_cardsToDiscard(context)) {
            return super.embassy_cardsToDiscard(context);
        }
        return getAnyFromHand(context, getDiscardString(Cards.embassy), NOTPASSABLE, 3, true, SelectCardOptions.DISCARD);
    }
    
    @Override
    public Card[] cartographer_cardsFromTopOfDeckToDiscard(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_cartographer_cardsFromTopOfDeckToDiscard(context, cards)) {
            return super.cartographer_cardsFromTopOfDeckToDiscard(context, cards);
        }
        
        if(cards == null || cards.length == 0) {
            return cards;
        }

        ArrayList<Card> cardsToDiscard = new ArrayList<Card>();

        ArrayList<String> options = new ArrayList<String>();
        for (Card c : cards)
            options.add(Strings.getCardName(c));
        String none = getString(R.string.none);
        options.add(none);

		do {
	        String o = selectString(context, R.string.Cartographer_query, Cards.cartographer, options.toArray(new String[0]));
            if (o.equals(none)) {
                break;
            }
            cardsToDiscard.add((Card) localNameToCard(o, cards));
            options.remove(o);
        } while (options.size() > 1);

        return cardsToDiscard.toArray(new Card[0]);
    }
    
    @Override
    public Card[] cartographer_cardOrder(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_cartographer_cardOrder(context, cards)) {
            return super.cartographer_cardOrder(context, cards);
        }
        ArrayList<Card> orderedCards = new ArrayList<Card>();
        int[] order = orderCards(context, cardArrToIntArr(cards));
        for (int i : order) 
            orderedCards.add(cards[i]);
        return orderedCards.toArray(new Card[0]);
    }
    
    @Override
    public ActionCard scheme_actionToPutOnTopOfDeck(MoveContext context, ActionCard[] actions) {
        if(context.isQuickPlay() && shouldAutoPlay_scheme_actionToPutOnTopOfDeck(context, actions)) {
            return super.scheme_actionToPutOnTopOfDeck(context, actions);
        }
        ArrayList<String> options = new ArrayList<String>();
        for (ActionCard c : actions)
            options.add(Strings.getCardName(c));
        String none = getString(R.string.none);
        options.add(none);
        String o = selectString(context, R.string.scheme_query, Cards.scheme, options.toArray(new String[0]));
        if(o.equals(none)) {
            return null;
        }
        return (ActionCard) localNameToCard(o, actions);
    }
    
    @Override
    public boolean oracle_shouldDiscard(MoveContext context, Player player, ArrayList<Card> cards) {
        if(context.isQuickPlay() && shouldAutoPlay_oracle_shouldDiscard(context, player, cards)) {
            return super.oracle_shouldDiscard(context, player, cards);
        }
        String cardNames = "";
        boolean first = true;
        for(Card c : cards) {
            if(first)
                first = false;
            else
                cardNames += ", ";
            cardNames += Strings.getCardName(c);
        }
        String s = Strings.format(R.string.card_revealed, player.getPlayerName(), cardNames);
        return !selectBoolean(context, s, getString(R.string.top_of_deck), getString(R.string.discard));
    }
    
    @Override
    public Card[] oracle_orderCards(MoveContext context, Card[] cards) {
        if(context.isQuickPlay() && shouldAutoPlay_oracle_orderCards(context, cards)) {
            return super.oracle_orderCards(context, cards);
        }
        ArrayList<Card> orderedCards = new ArrayList<Card>();
        int[] order = orderCards(context, cardArrToIntArr(cards));
        for (int i : order) 
            orderedCards.add(cards[i]);
        return orderedCards.toArray(new Card[0]);
    }
    
    @Override
    public boolean illGottenGains_gainCopper(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_illGottenGains_gainCopper(context)) {
            return super.illGottenGains_gainCopper(context);
        }

        return selectBoolean(context, Cards.illGottenGains, getString(R.string.ill_gotten_gains_option_one), getString(R.string.pass));
    }

    @Override
    public Card haggler_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_haggler_cardToObtain(context, maxCost, potion)) {
            return super.haggler_cardToObtain(context, maxCost, potion);
        }
        return getNonVictoryFromTable(context, getGainString(Cards.haggler), maxCost, false, NOTPASSABLE, potion?1:0);
    }
    
    @Override
    public Card[] inn_cardsToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_inn_cardsToDiscard(context)) {
            return super.inn_cardsToDiscard(context);
        }
        return getAnyFromHand(context, getDiscardString(Cards.inn), NOTPASSABLE, Math.min(2, getHand().size()), true, SelectCardOptions.DISCARD);
    }
    
    @Override
    public boolean inn_shuffleCardBackIntoDeck(MoveContext context, ActionCard card) {
        if(context.isQuickPlay() && shouldAutoPlay_inn_shuffleCardBackIntoDeck(context, card)) {
            return super.inn_shuffleCardBackIntoDeck(context, card);
        }
        
        String option1 = getString(R.string.inn_option_one);
        String option2 = getString(R.string.inn_option_two);
        return selectBooleanCardRevealed(context, Cards.inn, card, option1, option2); 
    }
    
    @Override
    public Card borderVillage_cardToObtain(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_borderVillage_cardToObtain(context)) {
            return super.borderVillage_cardToObtain(context);
        }
        
        return getFromTable(context, getGainString(Cards.borderVillage), Cards.borderVillage.getCost(context) - 1, Integer.MIN_VALUE, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, 0);
    }
    
    @Override
    public Card farmland_cardToTrash(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_farmland_cardToTrash(context)) {
            return super.farmland_cardToTrash(context);
        }
        return getAnyFromHand(context, getTrashString(Cards.farmland), NOTPASSABLE, SelectCardOptions.TRASH);
    }
    
    @Override
    public Card farmland_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        if(context.isQuickPlay() && shouldAutoPlay_remodel_cardToObtain(context, exactCost, potion)) {
            return super.remodel_cardToObtain(context, exactCost, potion);
        }
        
        return getFromTable(context, getGainString(Cards.farmland), exactCost, exactCost, false, NOTPASSABLE, SelectCardOptions.SELECT, false, true, potion?1:0);
    }
    
    @Override
    public TreasureCard stables_treasureToDiscard(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_stables_treasureToDiscard(context)) {
            return super.stables_treasureToDiscard(context);
        }
        return (TreasureCard) getTreasureFromHand(context, getDiscardString(Cards.stables), getString(R.string.none), SelectCardOptions.DISCARD);
    }
    
    @Override
    public Card mandarin_cardToReplace(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_mandarin_cardToReplace(context)) {
            return super.mandarin_cardToReplace(context);
        }
        
        return getAnyFromHand(context, getString(R.string.mandarin_part), NOTPASSABLE);
    }
    
    @Override
    public Card[] mandarin_orderCards(MoveContext context, Card[] cards) {
        if (context.isQuickPlay() && shouldAutoPlay_mandarin_orderCards(context, cards)) {
            return super.mandarin_orderCards(context, cards);
        }
    	ArrayList<Card> orderedCards = new ArrayList<Card>();
    	int[] order = orderCards(context, cardArrToIntArr(cards));
    	for (int i : order) 
    		orderedCards.add(cards[i]);
    	return orderedCards.toArray(new Card[0]);
	}

    public Card[] margrave_attack_cardsToKeep(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_margrave_attack_cardsToKeep(context)) {
            return super.margrave_attack_cardsToKeep(context);
        }
        return getAnyFromHand(context, getString(R.string.margrave_part), NOTPASSABLE, 3, true, SelectCardOptions.KEEP);
    }
    
    @Override 
    public Card getAttackReaction(MoveContext context, Card responsible, boolean defended) {
    	Card[] reactionCards = getReactionCards(defended);
    	if (reactionCards.length > 0) {
            ArrayList<String> options = new ArrayList<String>();
            for (Card c : reactionCards)
                options.add(Strings.getCardName(c));
            String none = getString(R.string.none);
            options.add(none);
            String o = selectString(context, R.string.reaction_query, responsible, options.toArray(new String[0]));
            if(o.equals(none)) {
                return null;
            }
            return localNameToCard(o, reactionCards);
    	}
    	return null;
    }
    
    @Override
    public boolean revealBane(MoveContext context) {
        if(context.isQuickPlay() && shouldAutoPlay_revealBane(context)) {
            return super.revealBane(context);
        }
    	return selectBoolean(context, Cards.youngWitch, Strings.format(R.string.bane_option_one, Strings.getCardName(game.baneCard)), getString(R.string.pass));
    }
    
    @Override
    public PutBackOption selectPutBackOption(MoveContext context, List<PutBackOption> putBacks) {
        if(context.isQuickPlay() && shouldAutoPlay_selectPutBackOption(context, putBacks)) {
        	return super.selectPutBackOption(context, putBacks);
        }
        Collections.sort(putBacks);
        LinkedHashMap<String, PutBackOption> h = new LinkedHashMap<String, PutBackOption>();
		h.put(getCardName(Cards.treasury), PutBackOption.Treasury);
		h.put(getCardName(Cards.alchemist), PutBackOption.Alchemist);
		h.put(getString(R.string.putback_option_one), PutBackOption.Coin);
		h.put(getString(R.string.putback_option_two), PutBackOption.Action);
        h.put(getString(R.string.none), PutBackOption.None);
        List<String> options = new ArrayList<String>();
        for (PutBackOption putBack : putBacks) {
        	switch (putBack) {
        	case Treasury:
        		options.add(getCardName(Cards.treasury));
        		break;
        	case Alchemist:
        		options.add(getCardName(Cards.alchemist));
        		break;
        	case Coin:
        		options.add(getString(R.string.putback_option_one));
        		break;
        	case Action:
        		options.add(getString(R.string.putback_option_two));
        		break;
        	}
        }
        options.add(getString(R.string.none));
    	
		return h.get(selectString(context, getString(R.string.putback_query), options.toArray(new String[0])));
    }
}
