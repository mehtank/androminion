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
        return "Chuck";
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
        ActionCard action;
        for (final Card card : getHand()) {
            if (context.canPlay(card)) {
                action = (ActionCard) card;
                if (action.getAddActions() > 0) {
                    return action;
                }
            }
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
                if (!context.canPlay(card) || card.equals(Cards.treasureMap)) {
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
        
        if(context.canBuy(Cards.province)) {
            return Cards.province;
        }
        
        if (turnCount > midGame && context.canBuy(Cards.duchy)) {
            if(context.getEmbargos(Cards.duchy) == 0) {
                return Cards.duchy;
            }
        }
        
        if(rand.nextDouble() < 0.25) {
            if(context.canBuy(Cards.gold) && context.getEmbargos(Cards.gold) == 0) {
                return Cards.gold;
            }

            if(context.canBuy(Cards.silver) && context.getEmbargos(Cards.silver) == 0) {
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
                        card.equals(Cards.copper) || 
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
                
                if(context.getEmbargos(card) > 0) {
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
        
        if (randList.size() > 0) {
            return randList.get(rand.nextInt(randList.size()));
        }

        if(context.canBuy(Cards.gold)) {
            return Cards.gold;
        }

        if(context.canBuy(Cards.silver) && context.getEmbargos(Cards.silver) == 0) {
            return Cards.silver;
        }
        
        if(context.canBuy(Cards.estate) && turnCount > midGame && context.getEmbargos(Cards.silver) == 0) {
            return Cards.estate;
        }
        
        return null;
    }

    @Override
    public Card[] getTrashCards() {
        return new Card[]{ Cards.curse, Cards.estate, Cards.copper };
    }

	@Override
	public Card getAttackReaction(MoveContext context, Card responsible, boolean defended, Card lastCard) {
		Card[] reactionCards = getReactionCards(defended);
		for (Card c : reactionCards) {
			if (c.equals(Cards.moat) && !reactedMoat) {
				reactedMoat = true;
				return c;
			}
			if (c.equals(Cards.secretChamber) && !reactedSecretChamber) {
				reactedSecretChamber = true;
				return c;
			}
			if (c.equals(Cards.horseTraders))
				return c;
		}
		return null;
	}
}
