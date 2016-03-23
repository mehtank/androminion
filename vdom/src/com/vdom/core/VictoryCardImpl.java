package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.api.VictoryCard;

public class VictoryCardImpl extends CardImpl implements VictoryCard {
    public VictoryCardImpl(Cards.Type type, int cost, int vp) {
        super(type, cost);
        this.vp = vp;
    }

    protected VictoryCardImpl(Builder builder) {
        super(builder);
    }

    public static class Builder extends CardImpl.Builder {
        public Builder(Cards.Type type, int cost, int vp) {
            super(type, cost);
            this.vp = vp;
        }

        public VictoryCardImpl build() {
            return new VictoryCardImpl(this);
        }

    }

    @Override
    public int getVictoryPoints() {
        return vp;
    }
    
    @Override
    public void play(Game game, MoveContext context) {
    	play(game, context, true);
    }
    
    @Override
    public void play(Game game, MoveContext context, boolean fromHand) {
    	super.play(game, context, fromHand);
    	Player currentPlayer = context.getPlayer();
    	switch (this.getType()) {
    	case Estate:
    		Card inheritedCard = context.player.getInheritance();
    		if (inheritedCard != null) {
    	    	// TODO
    			boolean newCard = false;
    			 if (this.numberTimesAlreadyPlayed == 0) {
		            newCard = true;
		            this.movedToNextTurnPile = false;
		            if (fromHand)
		                currentPlayer.hand.remove(this);
		           currentPlayer.playedCards.add(this);
		        }
    			
			 	GameEvent event = new GameEvent(GameEvent.Type.PlayingAction, (MoveContext) context);
		 		event.card = this;
		 		event.newCard = newCard;
		 		game.broadcastEvent(event); 
    			
	 			context.actionsPlayedSoFar++;
		        if (context.freeActionInEffect == 0) {
		            context.actions--;
		        }
		        
		        this.startInheritingCardAbilities(inheritedCard.getTemplateCard().instantiate());
		        // Play the inheritance virtual card
		        CardImpl cardToPlay = (CardImpl) this.behaveAsCard();
		        context.freeActionInEffect++;
		        cardToPlay.play(game, context, false);
		        context.freeActionInEffect--;

		        // impersonated card stays in play until next turn?
		        if (cardToPlay.trashOnUse) {
		            int idx = currentPlayer.playedCards.lastIndexOf(this);
		            if (idx >= 0) currentPlayer.playedCards.remove(idx);
		            currentPlayer.trash(this, null, context);
		        } else if (cardToPlay.isDuration(currentPlayer) && !cardToPlay.equals(Cards.outpost)) {
		            if (!this.controlCard.movedToNextTurnPile) {
		                this.controlCard.movedToNextTurnPile = true;
		                int idx = currentPlayer.playedCards.lastIndexOf(this);
		                if (idx >= 0) {
		                    currentPlayer.playedCards.remove(idx);
		                    currentPlayer.nextTurnCards.add(this);
		                }
		            }
		        }
		        
		        event = new GameEvent(GameEvent.Type.PlayedAction, (MoveContext) context);
		        event.card = this;
		        game.broadcastEvent(event);
		        
		        // test if any prince card left the play
		        currentPlayer.princeCardLeftThePlay(currentPlayer);
		        
		        // check for cards to call after resolving action
		        boolean isActionInPlay = isInPlay(currentPlayer);
		        ArrayList<Card> callableCards = new ArrayList<Card>();
		        Card toCall = null;
		        for (Card c : currentPlayer.tavern) {
		        	if (c.behaveAsCard().isCallableWhenActionResolved()) {
		        		if (c.behaveAsCard().doesActionStillNeedToBeInPlayToCall() && !isActionInPlay) {
		        			continue;
		        		}
		        		callableCards.add(c);
		        	}
		        }
		        if (!callableCards.isEmpty()) {
		        	Collections.sort(callableCards, new Util.CardCostComparator());
			        do {
			        	toCall = null;
			        	// we want null entry at the end for None
			        	Card[] cardsAsArray = callableCards.toArray(new Card[callableCards.size() + 1]);
			        	//ask player which card to call
			        	toCall = currentPlayer.controlPlayer.call_whenActionResolveCardToCall(context, this, cardsAsArray);
			        	if (toCall != null && callableCards.contains(toCall)) {
			        		callableCards.remove(toCall);
			        		toCall.behaveAsCard().callWhenActionResolved(context, this);
			        	}
				        // loop while we still have cards to call
				        // NOTE: we have a hack here to prevent asking for duplicate calls on an unused Royal Carriage
				        //   since technically you can ask for more and action re-played by royal carriage will ask as well
			        } while (toCall != null && toCall.equals(Cards.coinOfTheRealm) && !callableCards.isEmpty());
		        }
		 		
    		}
    		break;
    	default:
    		break;
    	}
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        VictoryCardImpl c = new VictoryCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(VictoryCardImpl c) {
        super.copyValues(c);
    }

    protected VictoryCardImpl() {
    }

    @Override
    public void isBuying(MoveContext context) {
        context.game.trashHovelsInHandOption(context.player, context, this);
        
        if (this.equals(Cards.estate)) {
        	Card inheritance = context.getPlayer().getInheritance();
        	if (inheritance != null) {
        		inheritance.isBuying(context);
        	}
        } else if (this.equals(Cards.farmland)) {
            Player player = context.getPlayer();
            if(player.getHand().size() > 0) {
                Card cardToTrash = player.controlPlayer.farmland_cardToTrash((MoveContext) context);

                if (cardToTrash == null) {
                    Util.playerError(player, "Farmland did not return a card to trash, trashing random card.");
                    cardToTrash = Util.randomCard(player.hand);
                }

                int cost = -1;
                boolean potion = false;
                for (int i = 0; i < player.hand.size(); i++) {
                    Card playersCard = player.hand.get(i);
                    if (playersCard.equals(cardToTrash)) {
                        cost = playersCard.getCost(context);
                        potion = playersCard.costPotion();
                        playersCard = player.hand.remove(i);

                        player.trash(playersCard, this, (MoveContext) context);
                        break;
                    }
                }

                if (cost == -1) {
                    Util.playerError(player, "Farmland returned invalid card, ignoring.");
                }
                else {
                    cost += 2;

                    boolean validCard = false;
                    
                    for(Card c : context.getCardsInGame()) {
                        if(Cards.isSupplyCard(c) && c.getCost(context) == cost && c.costPotion() == potion && context.getCardsLeftInPile(c) > 0) {
                            validCard = true;
                            break;
                        }
                    }

                    if(validCard) {
                        Card card = player.controlPlayer.farmland_cardToObtain((MoveContext) context, cost, potion);
                        if (card != null) {
                            // check cost
                            if (card.getCost(context) != cost || card.costPotion() != potion) {
                                Util.playerError(player, "Farmland card to obtain returned an invalid card, ignoring.");
                            }
                            else
                            {
                                if(player.gainNewCard(card, this, (MoveContext) context) == null) {
                                    Util.playerError(player, "Farmland new card is invalid, ignoring.");
                                }
                            }
                        }
                        else {
                            //TODO: handle...
                        }
                    }
                }
            }
        }
    }
}
