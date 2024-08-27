package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.MoveContext.TurnPhase;

public class CardImplPromo extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplPromo(CardImpl.Builder builder) {
		super(builder);
	}
	
	protected CardImplPromo() { }

	@Override
    public void followInstructions(Game game, MoveContext context, Card responsible, Player currentPlayer, boolean isThronedEffect, PlayContext playContext) {
        super.followInstructions(game, context, responsible, currentPlayer, isThronedEffect, playContext);
		switch(getKind()) {
		case Avanto:
			avanto(game, context, currentPlayer);
			break;
		case BlackMarket:
            blackMarket(game, context, currentPlayer);
            break;
		case Captain:
			captain(game, context, currentPlayer, isThronedEffect);
			break;
		case Church:
			church(game, context, currentPlayer, isThronedEffect);
			break;
		case Dismantle:
			dismantle(game, context, currentPlayer);
			break;
		case Envoy:
            envoy(game, context, currentPlayer);
            break;
		case Governor:
            governor(game, context, currentPlayer, playContext);
            break;
		case Prince:
            prince(game, context, currentPlayer, isThronedEffect);
            break;
		case Sauna:
			sauna(game, context, currentPlayer);
			break;
		default:
			break;
		}
	}
	
	@Override
    public void isBuying(MoveContext context) {
		super.isBuying(context);
        switch (this.getKind()) {
        case Summon:
        	summon(context);
        	break;
        default:
            break;
        }
    }
	
	private void avanto(Game game, MoveContext context, Player currentPlayer) {
		if (currentPlayer.hand.contains(Cards.sauna) && currentPlayer.controlPlayer.avanto_shouldPlaySauna(context)) {
            Card next = currentPlayer.hand.get(Cards.sauna);
            if (next != null) {
                next.play(game, context, true);
            }
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
            currentPlayer.reveal(c, this, context);
        }
        
        // play treasures 
        context.game.playTreasures(currentPlayer, context, -1, this);
        
        // can trade in Coffers if in Buy phase (e.g. Capitalism)
        if (context.phase == TurnPhase.Buy) {
        	game.playGuildsTokens(currentPlayer, context);
        }
        
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
	
	                    if (Cards.silver.equals(context.game.playBuy(context, card))) {
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
	
	private void captain(Game game, MoveContext context, Player player, boolean isThronedEffect) {
		player.addStartTurnDurationEffect(this, 1, isThronedEffect);
		captainEffect(game, context, player);
    }
	
	public void captainEffect(Game game, MoveContext context, Player player) {
		Card cardToPlay = player.controlPlayer.captain_cardToPlay(context);
        if (cardToPlay != null 
	            && !game.isPileEmpty(cardToPlay)
	            && Cards.isSupplyCard(cardToPlay)
	            && cardToPlay.is(Type.Action, null)
	            && !cardToPlay.is(Type.Duration, null)
	            && !cardToPlay.is(Type.Command, null)
	            && cardToPlay.getCost(context) <= 4
	            && cardToPlay.getDebtCost(context) == 0
	        	&& !cardToPlay.costPotion()) {
            cardToPlay.play(game, context, false, true, false);
        } else {
            Card[] cards = game.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Action);
            if (cards.length != 0 && cardToPlay != null) {
                Util.playerError(player, "Captain returned invalid card (" + cardToPlay.getName() + "), ignoring.");
            }
            return;
        }
	}
	
	private void church(Game game, MoveContext context, Player player, boolean isThronedEffect) {
        Card[] cards = player.getHand().size() == 0 ? null : player.controlPlayer.church_cardsToSetAside(context);
        if (cards != null && cards.length > 3) {
        	Util.playerError(player, "Church: Tried to set aside too many cards. Setting aside zero.");
        	cards = null;
        } 
        if (cards != null && !Util.areCardsInHand(cards, context)) {
        	Util.playerError(player, "Church: Tried to set aside cards not in hand. Setting aside zero.");
        	cards = null;
        }
        
        if (cards == null) cards = new Card[0];
    	ArrayList<Card> churchCards = new ArrayList<Card>();
        for (Card card : cards) {
            if (card != null) {
                player.getHand().remove(card);
                churchCards.add(card);
                GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsidePrivate, (MoveContext) context);
                event.card = card;
                event.responsible = this;
                event.setPrivate(true);
                context.game.broadcastEvent(event);
            }
        }
        
        player.church.add(churchCards);
        player.addStartTurnDurationEffect(this, 1, isThronedEffect);
	}
	
	private void dismantle(Game game, MoveContext context, Player player) {
		CardList hand = player.getHand();
		if(hand.size() == 0)
			return;
        Card trashCard = player.controlPlayer.dismantle_cardToTrash(context);
        if (trashCard == null || !player.hand.contains(trashCard)) {
            Util.playerError(player, "Dismantle card to trash invalid, picking one");
            trashCard = hand.get(0);
        }

        player.trashFromHand(trashCard, this, context);
        int cost = trashCard.getCost(context);
        int debt = trashCard.getDebtCost(context);
        boolean potion = trashCard.costPotion();
        int potionCost = potion ? 1 : 0;
        if (cost < 1)
        	return;
        
        ArrayList<Card> validCards = new ArrayList<Card>();
        for (Card card : game.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true)) {
            int gainCardCost = card.getCost(context);
            int gainCardPotionCost = card.costPotion() ? 1 : 0;
            int gainCardDebt = card.getDebtCost(context);

            if ((gainCardCost < cost || gainCardDebt < debt || gainCardPotionCost < potionCost) && 
            		(gainCardCost <= cost && gainCardDebt <= debt && gainCardPotionCost <= potionCost)) {
                validCards.add(card);
            }
        }
        if (validCards.size() > 0) {
        	Card toGain = context.getPlayer().controlPlayer.dismantle_cardToObtain(context, cost, debt, potion);
            if (toGain == null || !validCards.contains(toGain)) {
                Util.playerError(context.getPlayer(), "Invalid card returned from Dismantle, picking one.");
                toGain = validCards.get(0);
            }
            context.getPlayer().gainNewCard(toGain, Cards.dismantle, context);
        }
        context.getPlayer().gainNewCard(Cards.gold, Cards.dismantle, context);
	}
	
	private void envoy(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> cards = new ArrayList<Card>();
        Player nextPlayer = game.getNextPlayer();
        for (int i = 0; i < 5; i++) {
            Card card = game.draw(context, Cards.envoy, 5 - i);
            if (card != null) {
                cards.add(card);
                currentPlayer.reveal(card, this, context);
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

        currentPlayer.discard(toDiscard, this, context);

        cards.remove(toDiscard);

        if (cards.size() > 0) {
            for(Card c : cards) {
                currentPlayer.hand.add(c);
            }
        }
    }
	
	private void governor(Game game, MoveContext context, Player currentPlayer, PlayContext playContext) {
        Player.GovernorOption option = currentPlayer.controlPlayer.governor_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Governor option error, ignoring.");
        } else {
            if (option == Player.GovernorOption.AddCards) {
                game.drawToHand(context, this, 3, playContext);
                game.drawToHand(context, this, 2, playContext);
                game.drawToHand(context, this, 1, playContext);
                for (Player player : game.getPlayersInTurnOrder()) {
                    if (player != context.getPlayer()) {
                        game.drawToHand(new MoveContext(game, player), this, 1, playContext);
                    }
                }
            } else if (option == Player.GovernorOption.GainTreasure) {
                currentPlayer.gainNewCard(Cards.gold, this, context);
                for (Player player : game.getPlayersInTurnOrder()) {
                    if (player != context.getPlayer()) {
                        player.gainNewCard(Cards.silver, this, new MoveContext(game, player));
                    }
                }
            } else if (option == Player.GovernorOption.Upgrade) {
                if (currentPlayer.getHand().size() > 0) {
                    Card card = currentPlayer.controlPlayer.governor_cardToTrash(context);
                    /*You MAY trash a card*/
                    if (card != null)
                    {
                       int value = card.getCost(context) + 2;
                       int debt = card.getDebtCost(context);
                       boolean potion = card.costPotion();
                       currentPlayer.trashFromHand(card, this, context);

                       card = currentPlayer.controlPlayer.governor_cardToObtain(context, value, debt, potion);
                       if (card != null) {
                           if (card.getCost(context) != value || card.getDebtCost(context) != debt || card.costPotion() != potion) {
                               Util.playerError(currentPlayer, "Governor error, new card does not cost value of the old card +2.");
                           } else {
                               if(currentPlayer.gainNewCard(card, this, context) == null) {
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
                               int debt = card.getDebtCost(playerContext);
                               boolean potion = card.costPotion();
                               player.trashFromHand(card, this, playerContext);

                               card = player.controlPlayer.governor_cardToObtain(playerContext, value, debt, potion);
                               if (card != null) {
                                   if (card.getCost(playerContext) != value || card.getDebtCost(playerContext) != debt || card.costPotion() != potion) {
                                       Util.playerError(player, "Governor error, new card does not cost value of the old card +1.");
                                   } else {
                                       if(player.gainNewCard(card, this, playerContext) == null) {
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
	
	private void prince(Game game, MoveContext context, Player currentPlayer, boolean isThronedEffect) {
            ArrayList<Card> possibleCards = new ArrayList<Card>();
        	for (Card c : currentPlayer.hand) {
        		if (c.is(Type.Action, currentPlayer) && !c.is(Type.Duration, currentPlayer) && c.getCost(context) <= 4 && c.getDebtCost(context) == 0 && !c.costPotion()) {
        			possibleCards.add(c);
        		}
        	}
        	if (possibleCards.size() == 0) return;
        	Card card = possibleCards.get(0);
        	if (possibleCards.size() > 1) {
        		card = currentPlayer.controlPlayer.prince_cardToSetAside(context);
                if (card == null || !currentPlayer.hand.contains(card) || !card.is(Type.Action, currentPlayer) || card.is(Type.Duration, currentPlayer) || card.getCost(context) > 4 || card.getDebtCost(context) != 0 || card.costPotion()) {
                    Util.playerError(currentPlayer, "Prince set aside card error, setting aside first card.");
                    card = possibleCards.get(0);
                }
        	}
        	
        	currentPlayer.hand.remove(card);
            currentPlayer.prince.add(card);

            currentPlayer.addStartTurnDurationEffect(this, -1, isThronedEffect);

            GameEvent event = new GameEvent(GameEvent.EventType.CardSetAside, context);
            event.card = card;
            event.responsible = this;
            game.broadcastEvent(event);
    }
	
	private void sauna(Game game, MoveContext context, Player currentPlayer) {
		if (currentPlayer.hand.contains(Cards.avanto) && currentPlayer.controlPlayer.sauna_shouldPlayAvanto(context)) {
            Card next = currentPlayer.hand.get(Cards.avanto);
            if (next != null) {
                next.play(game, context, true);
            }
        }
	}


	//Events
	
	private void summon(MoveContext context) {
    	Card card = context.player.controlPlayer.summon_cardToObtain(context);
        if (card != null && card.is(Type.Action, null)) {
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion() && !context.game.isPileEmpty(card)) {
            	context.player.gainNewCard(card, this, context);
            }
        }
    }
}
