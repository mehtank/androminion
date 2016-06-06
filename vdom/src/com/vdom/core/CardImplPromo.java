package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;

public class CardImplPromo extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplPromo(CardImpl.Builder builder) {
		super(builder);
	}
	
	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case BlackMarket:
            blackMarket(game, context, currentPlayer);
            break;
		case Envoy:
            envoy(game, context, currentPlayer);
            break;
		case Governor:
            governor(game, context, currentPlayer);
            break;
		case Prince:
            prince(game, context, currentPlayer);
            break;
		default:
			break;
		}
	}
	
	@Override
    public void isBuying(MoveContext context) {
		super.isBuying(context);
        switch (this.controlCard.getKind()) {
        case Summon:
        	summon(context);
        	break;
        default:
            break;
        }
    }
	
	private void blackMarket(Game game, MoveContext context, Player currentPlayer) {
        context.blackMarketBuyPhase = true;
        
        // reveal 3 cards from BlackMarket pile
        ArrayList<Card> cards = new ArrayList<Card>();
        int count = Math.min(context.game.blackMarketPileShuffled.size(), 3);
        for (int i = 0; i < count; i++) {
            Card c = context.game.blackMarketPileShuffled.remove(0);
            cards.add(c);
            context.game.blackMarketPile.remove(c);
            context.game.blackMarketPile.add(i, c);
            currentPlayer.reveal(c, this.controlCard, context);
        }
        
        // play treasures 
        context.game.playTreasures(currentPlayer, context, -1, this.controlCard);
        
        if (currentPlayer.getDebtTokenCount() == 0) {
	        // get one buy from BlackMarkt pile
	        ArrayList<Card> canBuy = new ArrayList<Card>();
	        for (int i = 0; i < cards.size(); i++) {
	            if (context.game.isValidBuy(context, cards.get(i), context.getCoinAvailableForBuy())) {
	                canBuy.add(cards.get(i));
	            }
	        }
	        if (canBuy.size() > 0) {
	            Card card = currentPlayer.controlPlayer.blackMarket_chooseCard(context, canBuy);
	            if (card != null) {
	                //see playerBuy()
	                if (context.game.isValidBuy(context, card, context.getCoinAvailableForBuy())) {
	                    GameEvent statusEvent = new GameEvent(GameEvent.EventType.Status, (MoveContext) context);
	                    context.game.broadcastEvent(statusEvent);
	
	                    if (context.game.playBuy(context, card).equals(Cards.silver)) {
	                        // trader swapped card in silver
	                        // Wiki: Put bought card on top of BlackMarket deck
	                        context.game.blackMarketPileShuffled.add(0, card);
	                        cards.remove(card);
	                    }
	                    else {
	                        cards.remove(card);
	                        context.game.blackMarketPile.remove(card);
	                    }
	                }
	            }
	        }
        }
        
        Collections.sort(context.game.blackMarketPile, new Util.CardCostNameComparator());
        
        // put rest back
        if (cards.size() > 0) {
            Card[] order = currentPlayer.controlPlayer.blackMarket_orderCards(context, cards.toArray(new Card[cards.size()]));
            boolean bad = false;
            if (order == null || order.length != cards.size()) {
                bad = true;
            } else {
                ArrayList<Card> orderArray = new ArrayList<Card>();
                for (Card c : order) {
                    orderArray.add(c);
                    if (!cards.contains(c)) {
                        bad = true;
                    }
                }

                for (Card c : cards) {
                    if (!orderArray.contains(c)) {
                        bad = true;
                    }
                }
            }
            if (bad) {
                Util.playerError(currentPlayer, "Black Market order cards error, ignoring.");
                order = cards.toArray(new Card[cards.size()]);
            }
            for (int i = 0; i < order.length; i++) {
                context.game.blackMarketPileShuffled.add(order[i]);
            }
        }
        
        context.blackMarketBuyPhase = false;
    }
	
	private void envoy(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> cards = new ArrayList<Card>();
        Player nextPlayer = game.getNextPlayer();
        for (int i = 0; i < 5; i++) {
            Card card = game.draw(context, Cards.envoy, 5 - i);
            if (card != null) {
                cards.add(card);
                currentPlayer.reveal(card, this.controlCard, context);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        Card toDiscard;

        if (cards.size() > 1) {
            toDiscard = nextPlayer.controlPlayer.envoy_cardToDiscard(context, cards.toArray(new Card[cards.size()]));
        } else {
            toDiscard = cards.get(0);
        }
        if (toDiscard == null || !cards.contains(toDiscard)) {
            Util.playerError(currentPlayer, "Envoy discard error, just picking the first card.");
            toDiscard = cards.get(0);
        }

        currentPlayer.discard(toDiscard, this.controlCard, context);

        cards.remove(toDiscard);

        if (cards.size() > 0) {
            for(Card c : cards) {
                currentPlayer.hand.add(c);
            }
        }
    }
	
	private void governor(Game game, MoveContext context, Player currentPlayer) {
        Player.GovernorOption option = currentPlayer.controlPlayer.governor_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Governor option error, ignoring.");
        } else {
            if (option == Player.GovernorOption.AddCards) {
                game.drawToHand(context, this, 3);
                game.drawToHand(context, this, 2);
                game.drawToHand(context, this, 1);
                for (Player player : game.getPlayersInTurnOrder()) {
                    if (player != context.getPlayer()) {
                        game.drawToHand(new MoveContext(game, player), this, 1);
                    }
                }
            } else if (option == Player.GovernorOption.GainTreasure) {
                currentPlayer.gainNewCard(Cards.gold, this.controlCard, context);
                for (Player player : game.getPlayersInTurnOrder()) {
                    if (player != context.getPlayer()) {
                        player.gainNewCard(Cards.silver, this.controlCard, new MoveContext(game, player));
                    }
                }
            } else if (option == Player.GovernorOption.Upgrade) {
                if (currentPlayer.getHand().size() > 0) {
                    Card card = currentPlayer.controlPlayer.governor_cardToTrash(context);
                    /*You MAY trash a card*/
                    if (card != null)
                    {
                       int value = card.getCost(context) + 2;
                       boolean potion = card.costPotion();
                       currentPlayer.hand.remove(card);
                       currentPlayer.trash(card, this.controlCard, context);

                       card = currentPlayer.controlPlayer.governor_cardToObtain(context, value, potion);
                       if (card != null) {
                           if (card.getCost(context) != value || card.costPotion() != potion) {
                               Util.playerError(currentPlayer, "Governor error, new card does not cost value of the old card +2.");
                           } else {
                               if(currentPlayer.gainNewCard(card, this.controlCard, context) == null) {
                                   Util.playerError(currentPlayer, "Governor error, pile is empty or card is not in the game.");
                               }
                           }
                       }
                    }
                }
                for (Player player : game.getPlayersInTurnOrder()) {
                    if (player != context.getPlayer()) {
                        MoveContext playerContext = new MoveContext(game, player);
                        if (player.getHand().size() > 0) {
                            Card card = player.controlPlayer.governor_cardToTrash(playerContext);
                            /*You MAY trash a card*/
                            if (card != null)
                            {
                               int value = card.getCost(playerContext) + 1;
                               boolean potion = card.costPotion();
                               player.hand.remove(card);
                               player.trash(card, this.controlCard, playerContext);

                               card = player.controlPlayer.governor_cardToObtain(playerContext, value, potion);
                               if (card != null) {
                                   if (card.getCost(playerContext) != value || card.costPotion() != potion) {
                                       Util.playerError(player, "Governor error, new card does not cost value of the old card +1.");
                                   } else {
                                       if(player.gainNewCard(card, this.controlCard, playerContext) == null) {
                                           Util.playerError(player, "Governor error, pile is empty or card is not in the game.");
                                       }
                                   }
                               }
                            }
                        }
                    }
                }
            }
        }
    }
	
	private void prince(Game game, MoveContext context, Player currentPlayer) {
        // throneroom has no effect since prince is already set aside
        if (this.controlCard.numberTimesAlreadyPlayed == 0) {
            Card card = currentPlayer.controlPlayer.prince_cardToSetAside(context);
            if (card != null && !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Prince set aside card error, setting aside nothing.");
                card = null;
            }
            
            if (card != null && card.is(Type.Action, currentPlayer) && card.getCost(context) <= 4 && !card.costPotion()) {
                currentPlayer.prince.add(currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf((Card) this.controlCard)));
                this.controlCard.stopImpersonatingCard();
                
                currentPlayer.hand.remove(card);
                currentPlayer.prince.add(card);
                
                GameEvent event = new GameEvent(GameEvent.EventType.CardSetAside, (MoveContext) context);
                event.card = card;
                game.broadcastEvent(event);
            }
        } else {
            // reset clone count
            this.getControlCard().cloneCount = 1;
        }
    }


	//Events
	
	private void summon(MoveContext context) {
    	Card card = context.player.controlPlayer.summon_cardToObtain(context);
        if (card != null && card.is(Type.Action, null)) {
            if (card.getCost(context) <= 4 && !context.game.isPileEmpty(card)) {
            	context.player.gainNewCard(card, this.controlCard, context);
            }
        }
    }
}
