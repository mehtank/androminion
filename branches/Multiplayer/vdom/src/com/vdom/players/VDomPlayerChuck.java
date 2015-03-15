package com.vdom.players;

import java.util.ArrayList;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.TreasureCard;
import com.vdom.core.BasePlayer;
import com.vdom.core.Cards;
import com.vdom.core.MoveContext;

public class VDomPlayerChuck extends BasePlayer  {
    protected final Random rand = new Random(System.currentTimeMillis());
    
    protected static final int ACTION_CARDS_MAX = 5;

    @Override
    public void newGame(MoveContext context) {
        super.newGame(context);
        midGame = 11;
    }

    @Override
    public String getPlayerName() {
        return getPlayerName(game.maskPlayerNames);
    }
    
    @Override
    public String getPlayerName(boolean maskName) {
        return maskName ? "Player " + (playerNumber + 1) : "Chuck";
    }
    
    @Override
    public boolean isAi() {
        return true;
    }

    @Override
    public Card doAction(MoveContext context) {
        int treasureMapCount = 0;

        for (final Card card : getHand()) {
            if (card.equals(Cards.treasureMap)) {
                treasureMapCount++;
            }
        }

        if (treasureMapCount >= 2) {
            return fromHand(Cards.treasureMap);
        }
        
        // play prince if action card candidate available
        Card[] princeCards;
        if (getHand().contains(Cards.prince)) {
            ArrayList<Card> cardList = new ArrayList<Card>();
            for (Card c : getHand()) {
                cardList.add(c);
            }
            princeCards = prince_cardCandidates(context, cardList, false);
        }
        else {
            princeCards = new Card[0];
        }
                
        ActionCard action;
        for (final Card card : getHand()) {
            if (context.canPlay(card)) {
                action = (ActionCard) card;
                if (action.getAddActions() > 0 && !isInCardArray(card, princeCards)) {
                    return action;
                }
            }
        }


        if (princeCards.length != 0) {
            return fromHand(Cards.prince);
        }
            
        if(inHand(Cards.throneRoom) && context.canPlay(Cards.throneRoom)) {
            return fromHand(Cards.throneRoom);
        }
        
        //TODO: ...
        //if(context.getKingsCourtsInEffect() == 0) {
            if(inHand(Cards.kingsCourt) && context.canPlay(Cards.kingsCourt)) {
                return fromHand(Cards.kingsCourt);
            }
        //}
        
        int cost = COST_MAX;
        while (cost >= 0) {
            final ArrayList<Card> randList = new ArrayList<Card>();
            Card[] arrayOfCard2;
            final int actions = (arrayOfCard2 = getHand().toArray()).length;
            for (int localActionCard1 = 0; localActionCard1 < actions; localActionCard1++) {
                final Card card = arrayOfCard2[localActionCard1];
                if (   !context.canPlay(card)
                    || card.equals(Cards.treasureMap)
                    || card.equals(Cards.tactician) && context.countCardsInPlay(Cards.tactician) > 0
                   ) {
                    continue;
                }
                
                if(card.getCost(context) == cost) {
                    randList.add(card);
                }
            }

            if (randList.size() > 0) {
                return randList.get(rand.nextInt(randList.size()));
            }

            cost--;
        }

        return null;
    }

    @Override
    public Card doBuy(MoveContext context) {
        final int coinAvailableForBuy = context.getCoinAvailableForBuy();

        if (coinAvailableForBuy == 0) {
            return null;
        }
        
        if(context.canBuy(Cards.colony)) {
            return Cards.colony;
        }
        
        if(context.canBuy(Cards.platinum) && turnCount < midGame) {
            return Cards.platinum;
        }
        
        if(context.canBuy(Cards.prince) && turnCount < midGame && context.cardInGame(Cards.colony) && getMyCardCount(Cards.prince) < 2) {
            ArrayList<Card> allCards = new ArrayList<Card>(getAllCards());
            if (prince_cardCandidates(context, allCards, false).length >= 2 + 2*getMyCardCount(Cards.prince))
                return Cards.prince;
        }
        
        if(context.canBuy(Cards.province)) {
            return Cards.province;
        }
        
        if (turnCount > midGame && context.canBuy(Cards.vineyard) && actionCardCount >=9 ) {
            if(context.getEmbargosIfCursesLeft(Cards.vineyard) == 0) {
                return Cards.vineyard;
            }
        }
        if (turnCount > midGame && context.canBuy(Cards.duchy)) {
            if(context.getEmbargosIfCursesLeft(Cards.duchy) == 0) {
                return Cards.duchy;
            }
        }
        
        //try cards with potion before silver 
        if (turnCount > midGame && context.canBuy(Cards.vineyard) && actionCardCount >=6 ) {
            if(context.getEmbargosIfCursesLeft(Cards.vineyard) == 0) {
                return Cards.vineyard;
            }
        }
        if (context.getPotions() > 0) {
            //buy in this order
            final Card[] POTION_CARDS = new Card[] { Cards.possession, Cards.golem, Cards.familiar, Cards.alchemist, Cards.philosophersStone, Cards.scryingPool, Cards.apothecary, Cards.university };
            for (Card card : POTION_CARDS) {
                if (context.canBuy(card)) {
                    if (   getMyCardCount(card) >= 2
                        && !(card.equals(Cards.alchemist) || card.equals(Cards.philosophersStone) || card.equals(Cards.scryingPool) ) ) {
                        continue;
                    }
                    if (card.equals(Cards.familiar) && (context.game.pileSize(Cards.curse) <= 3 || turnCount > midGame)) {
                        continue;
                    }
                    if (context.getEmbargosIfCursesLeft(card) > 0) {
                        continue;
                    }
                    if (coinAvailableForBuy >= card.getCost(context) + 3) {
                        continue;
                    }
                    return card;
                }
            }
        }

        if(rand.nextDouble() < 0.25) {
            if(context.canBuy(Cards.gold) && context.getEmbargosIfCursesLeft(Cards.gold) == 0) {
                return Cards.gold;
            }

            if(context.canBuy(Cards.silver) && context.getEmbargosIfCursesLeft(Cards.silver) == 0) {
                return Cards.silver;
            }
        }

        int cost = coinAvailableForBuy;
        int highestCost = 0;
        final ArrayList<Card> randList = new ArrayList<Card>();

        while (cost >= 0) {
            for (final Card card : context.getCardsInGame()) {
                if (
                        card.getCost(context) != cost || 
                        !context.canBuy(card) || 
                        card.equals(Cards.curse) || 
                        card.equals(Cards.virtualRuins) || 
                        card.equals(Cards.copper) || 
                        card.equals(Cards.rats) || 
                        card.equals(Cards.potion) && !shouldBuyPotion() ||
                        card.equals(Cards.throneRoom) && throneRoomAndKingsCourtCount >= 2 ||
                        card.equals(Cards.kingsCourt) && throneRoomAndKingsCourtCount >= 2 ||
                        !(card instanceof ActionCard) && !(card instanceof TreasureCard)
                   ) {
                    continue;
                }
                
                if (card instanceof ActionCard && actionCardCount >= ACTION_CARDS_MAX) {
                    continue;
                }
                
                if(context.getEmbargosIfCursesLeft(card) > 0) {
                    continue;
                }
                            
                if(highestCost == 0) {
                    highestCost = card.getCost(context);
                }
                randList.add(card);
            }

            if(--cost < highestCost - 2) {
                break;
            }
        }

        // prefer silver instead of masterpiece if you can't overpay by 2
        if (randList.contains(Cards.masterpiece) && randList.contains(Cards.silver) && coinAvailableForBuy < 5) {
            randList.remove(Cards.masterpiece);
        }
        
        if (randList.size() > 0) {
            return randList.get(rand.nextInt(randList.size()));
        }

        if(context.canBuy(Cards.gold)) {
            return Cards.gold;
        }

        if(context.canBuy(Cards.silver) && context.getEmbargosIfCursesLeft(Cards.silver) == 0) {
            return Cards.silver;
        }
        
        if(context.canBuy(Cards.estate) && turnCount > midGame && context.getEmbargosIfCursesLeft(Cards.estate) == 0) {
            return Cards.estate;
        }
        
        return null;
    }

    @Override
    public Card[] getTrashCards() {
        //trash in this order!
        return new Card[] { Cards.curse, Cards.rats, Cards.overgrownEstate, Cards.ruinedVillage, Cards.ruinedMarket, Cards.survivors, Cards.ruinedLibrary, Cards.abandonedMine, Cards.virtualRuins, Cards.hovel, Cards.estate, Cards.copper, Cards.masterpiece };
    }

}
