package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;

public class CardImplGuilds extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplGuilds(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplGuilds() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch (getKind()) {
		case Advisor:
            advisor(game, context, currentPlayer);
            break;
		case Baker:
            baker(game, context, currentPlayer);
            break;
        case Butcher:
            butcher(game, context, currentPlayer);
            break;
        case CandlestickMaker:
            candlestickMaker(game, context, currentPlayer);
            break;
        case Doctor:
            doctor(game, context, currentPlayer);
            break;
        case Herald:
            herald(game, context, currentPlayer);
            break;
        case Journeyman:
            journeyman(game, context, currentPlayer);
            break;
        case Plaza:
            plaza(game, context, currentPlayer);
            break;
        case Soothsayer:
            soothsayer(game, context, currentPlayer);
            break;
        case StoneMason:
            stonemason(game, context, currentPlayer);
            break;
        case Taxman:
            taxman(game, context, currentPlayer);
            break;
		default:
			break;
		}
	}
	
	@Override
    public void isBuying(MoveContext context) {
		super.isBuying(context);
        switch (this.getControlCard().getKind()) {
        case Doctor:
            doctorOverpay(context);
            break;
        case Herald:
            heraldOverpay(context, context.getPlayer());
            break;
        case StoneMason:
            stoneMasonOverpay(context);
            break;
        case Masterpiece:
            masterpiece(context);
            break;
        default:
            break;
        }
    }
	
	private void advisor(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> cards = new ArrayList<Card>();
        Player nextPlayer     = game.getNextPlayer();

        for (int i = 0; i < 3; ++i) {
            Card card = game.draw(context, Cards.advisor, 3 - i);

            if (card != null) 
            {
                cards.add(card);
                currentPlayer.reveal(card, this.getControlCard(), context);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        Card toDiscard;

        if (cards.size() > 1) {
            toDiscard = nextPlayer.controlPlayer.advisor_cardToDiscard(context, cards.toArray(new Card[cards.size()]));
        } else {
            toDiscard = cards.get(0);
        }

        if (toDiscard == null || !cards.contains(toDiscard)) {
            Util.playerError(currentPlayer, "Advisor discard error, just picking the first card.");
            toDiscard = cards.get(0);
        }

        currentPlayer.discard(toDiscard, this.getControlCard(), context);

        cards.remove(toDiscard);

        if (cards.size() > 0) {
            for(Card c : cards) {
                currentPlayer.hand.add(c);
            }
        }
    }
	
	private void baker(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainGuildsCoinTokens(1);
        sendGuildsTokenObtainedEvent(game, context);
    }
	
	private void butcher(Game game, MoveContext context, Player currentPlayer) {
        
        currentPlayer.gainGuildsCoinTokens(2);
        sendGuildsTokenObtainedEvent(game, context);
        sendGuildsTokenObtainedEvent(game, context);

        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.butcher_cardToTrash(context);

            if (card != null) {
                currentPlayer.hand.remove(card);
                currentPlayer.trash(card, this.getControlCard(), context);

                int value = card.getCost(context);
                int debt = card.getDebtCost(context);
                boolean potion = card.costPotion();

                int coinTokenTotal = currentPlayer.getGuildsCoinTokenCount();
                if (coinTokenTotal > 0) {
                    // Offer the player the option of "spending" Guilds coin tokens prior to gaining a card
                    int numTokensToSpend = currentPlayer.controlPlayer.numGuildsCoinTokensToSpend(context, coinTokenTotal, true/*butcher*/);
                    
                    if (numTokensToSpend > 0 && numTokensToSpend <= coinTokenTotal) {
                        currentPlayer.spendGuildsCoinTokens(numTokensToSpend);
                        value += numTokensToSpend;
                        if (numTokensToSpend > 0) {
                            GameEvent event = new GameEvent(GameEvent.EventType.GuildsTokenSpend, context);
                            event.setComment(": " + numTokensToSpend);
                            game.broadcastEvent(event);
                        }
                    }

                    card = currentPlayer.controlPlayer.butcher_cardToObtain(context, value, debt, potion);

                    if (card != null) {
                        if (card.getCost(context) > value || card.getDebtCost(context) > debt || (card.costPotion() && !potion)) {
                            Util.playerError(currentPlayer, "Butcher error, new card does not have appropriate cost");
                        }
                        else {
                            if (currentPlayer.gainNewCard(card, this.getControlCard(), context) == null) {
                                Util.playerError(currentPlayer, "Butcher error, pile is empty or card is not in the game.");
                            }
                        }
                    }
                }
            }
        }
    }

	private void candlestickMaker(Game game, MoveContext context, Player currentPlayer) {
	    currentPlayer.gainGuildsCoinTokens(1);
	    sendGuildsTokenObtainedEvent(game, context);
	}

    private void doctor(Game game, MoveContext context, Player currentPlayer) {

        List<Card> options = new ArrayList<Card>(currentPlayer.getDistinctCards());
        Collections.sort(options, new Util.CardNameComparator());
        Card named = currentPlayer.controlPlayer.doctor_cardToPick(context, options);
        currentPlayer.controlPlayer.namedCard(named, this.getControlCard(), context);

        ArrayList<Card> revealedCards = new ArrayList<Card>();

        for (int i = 0; i < 3; ++i) {
            Card card = game.draw(context, Cards.doctor, 3 - i);
            if (card != null) {
                currentPlayer.reveal(card, this.getControlCard(), context);

                if (card.equals(named)) {
                    currentPlayer.trash(card, this.getControlCard(), context);
                }
                else {
                    revealedCards.add(card);
                }
            }
        }

        if (revealedCards.size() > 0) {
            ArrayList<Card> orderedCards = currentPlayer.controlPlayer.doctor_cardsForDeck(context, revealedCards);

            // Put the cards back on the deck
            for (int i = orderedCards.size() - 1; i >= 0; i--) {
                currentPlayer.putOnTopOfDeck(orderedCards.get(i));
            }
        }
    }
    
    private void doctorOverpay(MoveContext context) {
        for (int i = 0; i < context.overpayAmount; ++i) {
            Card card = context.game.draw(context, this.getControlCard(), context.overpayAmount - i);

            if (card != null) {
                Player.DoctorOverpayOption doo = context.player.doctor_chooseOption(context, card);

                switch(doo) {
                    case TrashIt:
                        context.player.trash(card, this.getControlCard(), context);
                        break;
                    case DiscardIt:
                        context.player.discard(card, this.getControlCard(), context);
                        break;
                    case PutItBack:
                        context.player.putOnTopOfDeck(card);
                        break;
                    default:
                        break;
                }
            }
        }
    }
    
    private void herald(Game game, MoveContext context, Player currentPlayer) {
        Card draw = game.draw(context, Cards.herald, 1);

        if (draw != null) {
            currentPlayer.reveal(draw, this.getControlCard(), context);

            if (draw.is(Type.Action, currentPlayer)) {
                context.freeActionInEffect++;
                draw.play(game, context, false);
                context.freeActionInEffect--;
            } else {
                context.player.putOnTopOfDeck(draw, context, true);
            }
        }
    }
    
    private void heraldOverpay(MoveContext context, Player currentPlayer) {      
        for (int i = 0; i < context.overpayAmount; ++i) {
            // Only allow a choice if there are cards in the discard pile
            if (currentPlayer.discard.size() > 0) {      
                // Create a list of all cards in the player's discard pile 
                ArrayList<Card> options = currentPlayer.getDiscard().toArrayList();

                Collections.sort(options, new Util.CardNameComparator());

                if (options.size() > 0) {
                    Card cardToTopDeck = context.player.herald_cardTopDeck(context, options.toArray(new Card[options.size()]));

                    if (cardToTopDeck != null) {
                        currentPlayer.discard.remove(cardToTopDeck);
                        currentPlayer.putOnTopOfDeck(cardToTopDeck);
                    }
                }
            }
        }
    }
	
    private void journeyman(Game game, MoveContext context, Player currentPlayer) {
        List<Card> options = new ArrayList<Card>(currentPlayer.getDistinctCards());
        Collections.sort(options, new Util.CardNameComparator());
        Card named = currentPlayer.controlPlayer.journeyman_cardToPick(context, options);
        currentPlayer.controlPlayer.namedCard(named, this.getControlCard(), context);

        ArrayList<Card> cardsToKeep = new ArrayList<Card>();
        ArrayList<Card> cardsToDiscard = new ArrayList<Card>();
        Card last = null;
        int diffCardsFound = 0;

        // search for the first 3 cards that were not named
        while (diffCardsFound < 3 && (last = context.game.draw(context, Cards.journeyman, -1)) != null) {
            if (!last.equals(named)) {
                ++diffCardsFound;
                cardsToKeep.add(last);
            }
            else {
                cardsToDiscard.add(last);
            }

            currentPlayer.reveal(last, this.getControlCard(), context);
        }

        // Discard all matches
        for (Card c : cardsToDiscard) {
            currentPlayer.discard(c, this.getControlCard(), context);
        }

        // Place all other cards into the Player's hand
        for (Card c : cardsToKeep) {
            currentPlayer.hand.add(c);
        }
    }
    
    public void masterpiece(MoveContext context) {
        for (int i = 0; i < context.overpayAmount; ++i) {
            if(context.getPlayer().gainNewCard(Cards.silver, this.getControlCard(), context) == null)  {
                break;
            }
        }
    }
    
    private void plaza(Game game, MoveContext context, Player currentPlayer) {
        boolean valid = false;

        for (Card c : currentPlayer.hand) {
            if (c.is(Type.Treasure, currentPlayer)) {
                valid = true;
            }
        }

        if (valid) {
            Card toDiscard = currentPlayer.controlPlayer.plaza_treasureToDiscard(context);

            if (toDiscard != null && currentPlayer.hand.contains(toDiscard) && toDiscard.is(Type.Treasure, currentPlayer)) {
                currentPlayer.hand.remove(toDiscard);
                currentPlayer.reveal(toDiscard, this.getControlCard(), context);
                currentPlayer.discard(toDiscard, this.getControlCard(), context);
                currentPlayer.gainGuildsCoinTokens(1);
                sendGuildsTokenObtainedEvent(game, context);
                Util.debug(currentPlayer, "Gained a Guild coin token via Plaza");
            }
        }
    }
    
    private void soothsayer(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.gold, this.getControlCard(), context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                Card curseGained = player.gainNewCard(Cards.curse, this.getControlCard(), playerContext);

                if (Cards.curse.equals(curseGained)) {
                    game.drawToHand(playerContext, this, 1);
                }
            }
        }
    }
    
    private void stonemason(Game game, MoveContext context, Player currentPlayer) {
        
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.stonemason_cardToTrash(context);

            if (card != null) {
                currentPlayer.hand.remove(card);
                currentPlayer.trash(card, this.getControlCard(), context);

                int value = card.getCost(context);
                int debt = card.getDebtCost(context);
                boolean potion = card.costPotion();
                if (value >= 0 || debt >= 0 || potion) {
                    for (int i = 0; i < 2; ++i) {
                    	card = currentPlayer.controlPlayer.stonemason_cardToGain(context, value, debt, potion);
                    	if (card == null || !(card.getCost(context) < value || card.getDebtCost(context) < debt || (!card.costPotion() && potion) &&
                        		card.getCost(context) <= value && card.getDebtCost(context) <= value && (!card.costPotion() || potion))) {
                        	Util.playerError(currentPlayer, "Stone Mason card gain #" + i + " error, card does not cost less.");
                        } else if (card != null) {
                            if (currentPlayer.gainNewCard(card, this.getControlCard(), context) == null) {
                                Util.playerError(currentPlayer, "Stone Mason card gain #" + i + " error, pile is empty or card is not in the game.");
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void stoneMasonOverpay(MoveContext context) {
        if (context.overpayAmount > 0 || context.overpayPotions > 0) {
            // Gain two action cards each costing the amount overpaid
        	for (int i = 1; i <= 2; ++i) {
	            Card c = context.player.stonemason_cardToGainOverpay(context, context.overpayAmount, (context.overpayPotions > 0 ? true : false));
	            if (c == null || c.getCost(context) != context.overpayAmount || c.getDebtCost(context) != 0 || (c.costPotion() || !(context.overpayPotions > 0))) {
	            	Util.playerError(context.player, "Stone Mason overpay gain #" + i + " error, card is wrong cost.");
	            }
	            if (c != null) {
		            if (context.player.gainNewCard(c, this.getControlCard(), context) == null) {
		                Util.playerError(context.player, "Stone Mason overpay gain #" + i + " error, pile is empty or card is not in the game.");
		            }
	            }
        	}
        }
    }
    
    private void taxman(Game game, MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.taxman_treasureToTrash(context);

            if (card != null && card.is(Type.Treasure, currentPlayer)) {
                currentPlayer.hand.remove(card);
                currentPlayer.trash(card, this.getControlCard(), context);

                for (Player player : game.getPlayersInTurnOrder()) {
                    if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                        player.attacked(this.getControlCard(), context);
                        MoveContext playerContext = new MoveContext(game, player);
                        playerContext.attackedPlayer = player;

                        if (player.hand.size() >= 5) {
                            if (player.hand.contains(card)) {
                                Card c2 = player.hand.get(card);
                                player.hand.remove(c2);
                                player.discard(c2, this.getControlCard(), playerContext);
                            } else {
                                for (Card c : player.getHand()) {
                                    player.reveal(c, this.getControlCard(), playerContext);
                                }   
                            }
                        }
                    }
                }

                Card newCard = currentPlayer.controlPlayer.taxman_treasureToObtain(context, card.getCost(context) + 3, card.getDebtCost(context), card.costPotion());

                if (newCard != null && Cards.isSupplyCard(newCard) && newCard.is(Type.Treasure, null) 
                		&& newCard.getCost(context) <= card.getCost(context) + 3
                		&& newCard.getDebtCost(context) <= card.getDebtCost(context)
                		&& (!newCard.costPotion() || card.costPotion())
                		&& context.isCardOnTop(newCard)) {
                    currentPlayer.gainNewCard(newCard, this, context);
                }
            }
        }
    }
    
    
    
    
    
    
    
	
	
	
    private void sendGuildsTokenObtainedEvent(Game game, MoveContext context) {
        GameEvent event = new GameEvent(GameEvent.EventType.GuildsTokenObtained, context);
        game.broadcastEvent(event);
    }
	
}
