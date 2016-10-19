package com.vdom.core;

import java.util.ArrayList;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.core.Player.SpiceMerchantOption;

public class CardImplHinterlands extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplHinterlands(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplHinterlands() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case Cartographer:
            cartographer(game, context, currentPlayer);
            break;
		case Crossroads:
            crossroads(game, context, currentPlayer);
            break;
		case Develop:
            develop(context, currentPlayer);
            break;
		case Duchess:
            duchess(game, context);
            break;
		case Embassy:
            embassy(context, currentPlayer);
            break;
		case FoolsGold:
			foolsGold(context);
			break;
		case IllGottenGains:
			illGottenGains(context, currentPlayer);
			break;
		case Inn:
            inn(context, currentPlayer);
            break;
		case JackofallTrades:
            jackOfAllTrades(game, context, currentPlayer);
            break;
		case Mandarin:
            mandarin(context, currentPlayer);
            break;
        case Margrave:
            margrave(game, context, currentPlayer);
            break;
        case NobleBrigand:
            nobleBrigandAttack(context, true);
            break;
        case Oasis:
        	oasis(context, currentPlayer);
        	break;
        case Oracle:
            oracle(game, context, currentPlayer);
            break;
		case Scheme:
            context.schemesPlayed++;
            break;
		case SpiceMerchant:
            spiceMerchant(game, context, currentPlayer);
            break;
        case Stables:
            stables(game, context, currentPlayer);
            break;
        case Trader:
            trader(context, currentPlayer);
            break;
		default:
			break;
		}
	}
	
	@Override
    public void isBuying(MoveContext context) {
		super.isBuying(context);
        switch (this.getControlCard().getKind()) {
            case NobleBrigand:
                nobleBrigandAttack(context, false);
                break;
            case Farmland:
            	Player player = context.getPlayer();
                if(player.getHand().size() > 0) {
                    Card cardToTrash = player.controlPlayer.farmland_cardToTrash((MoveContext) context);

                    if (cardToTrash == null) {
                        Util.playerError(player, "Farmland did not return a card to trash, trashing random card.");
                        cardToTrash = Util.randomCard(player.hand);
                    }

                    int cost = -1;
                    int debt = 0;
                    boolean potion = false;
                    for (int i = 0; i < player.hand.size(); i++) {
                        Card playersCard = player.hand.get(i);
                        if (playersCard.equals(cardToTrash)) {
                            cost = playersCard.getCost(context);
                            debt = playersCard.getDebtCost(context);
                            potion = playersCard.costPotion();
                            playersCard = player.hand.remove(i);

                            player.trash(playersCard, this, (MoveContext) context);
                            break;
                        }
                    }

                    if (cost == -1) {
                        Util.playerError(player, "Farmland returned invalid card, ignoring.");
                    } else {
                        cost += 2;

                        boolean validCard = false;
                        
                        for(Card c : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true)) {
                            if(Cards.isSupplyCard(c) && c.getCost(context) == cost && c.costPotion() == potion && context.isCardOnTop(c)) {
                                validCard = true;
                                break;
                            }
                        }

                        if(validCard) {
                            Card card = player.controlPlayer.farmland_cardToObtain((MoveContext) context, cost, debt, potion);
                            if (card != null) {
                                // check cost
                                if (card.getCost(context) != cost || card.getDebtCost(context) != debt || card.costPotion() != potion) {
                                    Util.playerError(player, "Farmland card to obtain returned an invalid card, ignoring.");
                                } else {
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
            	break;
            default:
                break;
        }
    }
	
	private void cartographer(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 4; i++) {
            Card card = game.draw(context, Cards.cartographer, 4 - i);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            Card[] cardsToDiscard = currentPlayer.controlPlayer.cartographer_cardsFromTopOfDeckToDiscard(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
            if(cardsToDiscard != null) {
                for(Card toDiscard : cardsToDiscard) {
                    if(topOfTheDeck.remove(toDiscard)) {
                        currentPlayer.discard(toDiscard, this.getControlCard(), null);
                    }
                    else {
                        Util.playerError(currentPlayer, "Cartographer returned invalid card to discard, ignoring");
                    }
                }
            }
            if (topOfTheDeck.size() > 0) {
                Card[] order;

                if(topOfTheDeck.size() == 1) {
                    order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
                }
                else {
                    order = currentPlayer.controlPlayer.cartographer_cardOrder(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
                    // Check that they returned the right cards
                    boolean bad = false;

                    if (order == null) {
                        bad = true;
                    } else {
                        ArrayList<Card> copy = new ArrayList<Card>();
                        for (Card card : topOfTheDeck) {
                            copy.add(card);
                        }

                        for (Card card : order) {
                            if (!copy.remove(card)) {
                                bad = true;
                                break;
                            }
                        }

                        if (!copy.isEmpty()) {
                            bad = true;
                        }
                    }

                    if (bad) {
                        Util.playerError(currentPlayer, "Cartographer order cards error, ignoring.");
                        order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
                    }
                }

                // Put the cards back on the deck
                for (int i = order.length - 1; i >= 0; i--) {
                    currentPlayer.putOnTopOfDeck(order[i]);
                }
            }
        }
    }
	
	private void crossroads(Game game, MoveContext context, Player currentPlayer) {
        int victoryCards = 0;
        for(Card c : currentPlayer.getHand()) {
            currentPlayer.reveal(c, this.getControlCard(), context);
            if(c.is(Type.Victory, currentPlayer)) {
                victoryCards++;
            }
        }

        for(int i=0; i < victoryCards; i++) {
            game.drawToHand(context, this, victoryCards - i);
        }

        if (!this.getControlCard().equals(Cards.estate)) {
	        int crossroadsPlayed = this.getControlCard().numberTimesAlreadyPlayed;
	        for(Card c : context.getPlayedCards()) {
	            if(c.equals(Cards.crossroads)) {
	                crossroadsPlayed++;
	            }
	        }
	        
	        if (crossroadsPlayed <= 1) {
	            context.actions += 3;
	        }
        }
    }
	
	private void develop(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card cardToTrash = currentPlayer.controlPlayer.develop_cardToTrash(context);

            if(!currentPlayer.hand.contains(cardToTrash)) {
                Util.playerError(currentPlayer, "Returned an invalid card to trash with Develop, picking one for you.");
                cardToTrash = currentPlayer.hand.get(0);
            }

            int trashedCardCost = cardToTrash.getCost(context);
            int trashedCardDebt = cardToTrash.getDebtCost(context);
            boolean trashedCardPotion = cardToTrash.costPotion();

            Card lowCardToGain = null;
            Card highCardToGain = null;

            if(context.isNewCardAvailable(trashedCardCost - 1, trashedCardDebt, trashedCardPotion)) {
                lowCardToGain = currentPlayer.controlPlayer.develop_lowCardToGain(context, trashedCardCost - 1, trashedCardDebt, trashedCardPotion);
                if (lowCardToGain == null) {
                    lowCardToGain = Util.randomCard(context.getAvailableCards(trashedCardCost - 1, trashedCardPotion));
                }
            }

            if(context.isNewCardAvailable(trashedCardCost + 1, trashedCardDebt, trashedCardPotion)) {
                highCardToGain = currentPlayer.controlPlayer.develop_highCardToGain(context, trashedCardCost + 1, trashedCardDebt, trashedCardPotion);
                if (highCardToGain == null) {
                    highCardToGain = Util.randomCard(context.getAvailableCards(trashedCardCost + 1, trashedCardPotion));
                }
            }

            ArrayList<Card> cards = new ArrayList<Card>();

            if(lowCardToGain != null) {
                cards.add(lowCardToGain);
            }

            if(highCardToGain != null) {
                cards.add(highCardToGain);
            }

            Card[] cardsToGain = null;
            if(cards.size() > 0) {
                cardsToGain = cards.toArray(new Card[cards.size()]);
                if(cards.size() > 1) {
                    cardsToGain = currentPlayer.controlPlayer.develop_orderCards(context, cardsToGain);
                }
            }
            if(cardsToGain == null) {
                cardsToGain = new Card[0];
            }

            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this.getControlCard(), context);

            boolean bad = false;

            if(cardsToGain.length == 0) {
                for(Card c : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true)) {
                    if(Cards.isSupplyCard(c) && (c.getCost(context) == trashedCardCost - 1 || c.getCost(context) == trashedCardCost + 1) && context.isCardOnTop(c)) {
                        bad = true;
                    }
                }
            }
            else if(cardsToGain.length == 1) {
                if(cardsToGain[0].getCost(context) != trashedCardCost -1 && cardsToGain[0].getCost(context) != trashedCardCost + 1) {
                    bad = true;
                }
                else {
                    int costToCheck;
                    if(cardsToGain[0].getCost(context) == trashedCardCost - 1) {
                        costToCheck = trashedCardCost + 1;
                    }
                    else {
                        costToCheck = trashedCardCost - 1;
                    }

                    for(Card c : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true)) {
                        if(Cards.isSupplyCard(c) && c.getCost(context) == costToCheck && context.isCardOnTop(c)) {
                            bad = true;
                        }
                    }
                }
            }
            else if(cardsToGain.length == 2) {
                Card lowCard = (cardsToGain[0].getCost(context) <= cardsToGain[1].getCost(context))?cardsToGain[0]:cardsToGain[1];
                Card highCard = (cardsToGain[0].getCost(context) > cardsToGain[1].getCost(context))?cardsToGain[0]:cardsToGain[1];
                if(lowCard.getCost(context) != trashedCardCost -1 && highCard.getCost(context) != trashedCardCost + 1) {
                    bad = true;
                }
            }
            else {
                bad = true;
            }

            for(Card c : cardsToGain) {
                if(!Cards.isSupplyCard(c) || !context.isCardOnTop(c)) {
                    bad = true;
                }
            }

            if(bad) {
                //TODO: should just gain random cards, if there are any valid ones
                Util.playerError(currentPlayer, "Returned invalid cards to gain with Develop, doing nothing.");
            }
            else {
                for (int i = cardsToGain.length - 1; i >= 0; i--) {
                    Card c = cardsToGain[i];
                    currentPlayer.gainNewCard(c, this, context);
                }
            }
        }
    }
	
	private void duchess(Game game, MoveContext context) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
        	MoveContext targetPlayerContext = new MoveContext(context, game, targetPlayer);
            Card c = game.draw(targetPlayerContext, Cards.duchess, 1);
            if(c != null) {
                boolean discard = (targetPlayer).controlPlayer.duchess_shouldDiscardCardFromTopOfDeck(targetPlayerContext, c);
                if(discard) {
                    targetPlayer.discard(c, this.getControlCard(), targetPlayerContext);
                } else {
                    targetPlayer.putOnTopOfDeck(c);
                }
            }
        }
    }

	private void embassy(MoveContext context, Player currentPlayer) {
        if (currentPlayer.hand.size() == 0) {
            return;
        }

        Card[] cards;
        if (currentPlayer.hand.size() > 3) {
            cards = currentPlayer.controlPlayer.embassy_cardsToDiscard(context);
        } else {
            cards = currentPlayer.getHand().toArray();
        }
        boolean bad = false;
        if (cards == null) {
            bad = true;
        } else if (cards.length > 3) {
            bad = true;
        } else {
            ArrayList<Card> handCopy = Util.copy(currentPlayer.hand);
            for (Card card : cards) {
                if (!handCopy.remove(card)) {
                    bad = true;
                    break;
                }
            }
        }

        if (bad) {
            Util.playerError(currentPlayer, "Embassy discard error, discarding first 3 cards.");
            cards = new Card[3];

            for (int i = 0; i < cards.length; i++) {
                cards[i] = currentPlayer.hand.get(i);
            }
        }

        for (int i = 0; i < cards.length; i++) {
            currentPlayer.hand.remove(cards[i]);
            currentPlayer.reveal(cards[i], this.getControlCard(), context);
            currentPlayer.discard(cards[i], this.getControlCard(), null);
        }
    }
	
	protected void foolsGold(MoveContext context) {
        context.foolsGoldPlayed++;
        if (context.foolsGoldPlayed > 1) {
            context.addCoins(3);
        }
    }
	
	private void illGottenGains(MoveContext context, Player player) {
        if (context.getCardsLeftInPile(Cards.copper) > 0) {
            if (player.controlPlayer.illGottenGains_gainCopper(context)) {
                player.gainNewCard(Cards.copper, this, context);
            }
        }
    }
	
    private void inn(MoveContext context, Player currentPlayer) {
        Card[] cards;
        if (currentPlayer.hand.size() > 2) {
            cards = currentPlayer.controlPlayer.inn_cardsToDiscard(context);
        } else {
            cards = currentPlayer.getHand().toArray();
        }
        boolean bad = false;
        if (cards == null) {
            bad = true;
        } else if (cards.length > 2) {
            bad = true;
        } else {
            ArrayList<Card> handCopy = Util.copy(currentPlayer.hand);
            for (Card card : cards) {
                if (!handCopy.remove(card)) {
                    bad = true;
                    break;
                }
            }
        }

        if (bad) {
            Util.playerError(currentPlayer, "Inn discard error, discarding first 2 cards.");
            cards = new Card[2];

            for (int i = 0; i < cards.length; i++) {
                cards[i] = currentPlayer.hand.get(i);
            }
        }

        for (int i = 0; i < cards.length; i++) {
            currentPlayer.hand.remove(cards[i]);
            currentPlayer.reveal(cards[i], this.getControlCard(), context);
            currentPlayer.discard(cards[i], this.getControlCard(), context);
        }
    }
    
    private void jackOfAllTrades(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.silver, this.getControlCard(), context);

        Card c = game.draw(context, Cards.jackOfAllTrades, 1);
        if(c != null) {
            boolean discard = currentPlayer.controlPlayer.jackOfAllTrades_shouldDiscardCardFromTopOfDeck(context, c);
            if(discard) {
                currentPlayer.discard(c, this.getControlCard(), context);
            } else {
                currentPlayer.putOnTopOfDeck(c);
            }
        }
        
        int cardsToDraw = 5 - currentPlayer.hand.size();
        if (cardsToDraw > 0 && currentPlayer.getMinusOneCardToken()) {
        	game.drawToHand(context, this, -1);
        }
        for (int i = 0; i < cardsToDraw; ++i) {
        	if(!game.drawToHand(context, this, cardsToDraw - i)) {
                break;
            }
        }
        
        Card cardToTrash = currentPlayer.controlPlayer.jackOfAllTrades_nonTreasureToTrash(context);
        if(cardToTrash != null) {
            if(!currentPlayer.hand.contains(cardToTrash) || cardToTrash.is(Type.Treasure, currentPlayer)) {
                Util.playerError(currentPlayer, "Jack of All Trades returned invalid card to trash from hand, ignoring.");
            }
            else {
                currentPlayer.hand.remove(cardToTrash);
                currentPlayer.trash(cardToTrash, this.getControlCard(), context);
            }
        }
    }

    private void mandarin(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card toTopOfDeck = currentPlayer.controlPlayer.mandarin_cardToReplace(context);

            if (toTopOfDeck == null) {
                Util.playerError(currentPlayer, "No card selected for Mandarin, returning random card to the top of the deck.");
                toTopOfDeck = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.reveal(toTopOfDeck, this.getControlCard(), context);
            currentPlayer.hand.remove(toTopOfDeck);
            currentPlayer.putOnTopOfDeck(toTopOfDeck);
        }
    }

    private void margrave(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                game.drawToHand(playerContext, this.getControlCard(), 1);

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.margrave_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.getControlCard(), keepCardCount);
                }
            }
        }
    }
    
    private void nobleBrigandAttack(MoveContext moveContext, boolean defensible) {
        MoveContext context = moveContext;
        Player player = context.getPlayer();
        ArrayList<Card> trashed = new ArrayList<Card>();
        boolean[] gainCopper = new boolean[context.game.getPlayersInTurnOrder().length];

        int i = 0;
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            // Hinterlands card details in the rules states that noble brigand is not defensible when triggered from a buy
            if (targetPlayer != player && (!defensible || !Util.isDefendedFromAttack(context.game, targetPlayer, this))) {
                targetPlayer.attacked(this.getControlCard(), moveContext);
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                boolean treasureRevealed = false;
                ArrayList<Card> silverOrGold = new ArrayList<Card>();

                List<Card> cardsToDiscard = new ArrayList<Card>();
                for (int j = 0; j < 2; j++) {
                    Card card = context.game.draw(targetContext, Cards.nobleBrigand, 2 - j);
                    if(card == null) {
                        break;
                    }
                    targetPlayer.reveal(card, this.getControlCard(), targetContext);

                    if (card.is(Type.Treasure, targetPlayer)) {
                        treasureRevealed = true;
                    }

                    if(card.equals(Cards.silver) || card.equals(Cards.gold)) {
                        silverOrGold.add(card);
                    } else {
                        cardsToDiscard.add(card);
                    }
                }

                for (Card c: cardsToDiscard) {
                    targetPlayer.discard(c, this.getControlCard(), targetContext);
                }

                if(!treasureRevealed) {
                    gainCopper[i] = true;
                }

                Card cardToTrash = null;

                if (silverOrGold.size() == 1) {
                    cardToTrash = silverOrGold.get(0);
                } else if (silverOrGold.size() == 2) {
                    if (silverOrGold.get(0).equals(silverOrGold.get(1))) {
                        cardToTrash = silverOrGold.get(0);
                        targetPlayer.discard(silverOrGold.get(1), this.getControlCard(), targetContext);
                    } else {
                        cardToTrash = (player).controlPlayer.nobleBrigand_silverOrGoldToTrash(moveContext, silverOrGold.toArray(new Card[] {}));
                        for (Card c : silverOrGold) {
                            if (!c.equals(cardToTrash)) {
                                targetPlayer.discard(c, this.getControlCard(), targetContext);
                            }
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.getControlCard(), targetContext);
                    trashed.add(cardToTrash);
                }
            }
            i++;
        }

        i = 0;
        for(Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            if(gainCopper[i]) {
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
                targetPlayer.gainNewCard(Cards.copper, this.getControlCard(), targetContext);
            }
            i++;
        }

        if (trashed.size() > 0) {
            for (Card c : trashed) {
                player.controlPlayer.gainCardAlreadyInPlay(c, this.getControlCard(), moveContext);
                context.game.trashPile.remove(c);
            }
        }
    }
    
    private void oasis(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card cardToDiscard = currentPlayer.controlPlayer.oasis_cardToDiscard(context);
            if(cardToDiscard == null || !currentPlayer.hand.contains(cardToDiscard)) {
                Util.playerError(currentPlayer, "Returned an invalid card to discard with Oasis, picking one for you.");
                cardToDiscard = currentPlayer.hand.get(0);
            }

            currentPlayer.hand.remove(cardToDiscard);
            currentPlayer.reveal(cardToDiscard, this.getControlCard(), context);
            currentPlayer.discard(cardToDiscard, this.getControlCard(), null);
        }
    }
    
    private void oracle(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer == currentPlayer || !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this.getControlCard(), context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                ArrayList<Card> cards = new ArrayList<Card>();
                for(int i=0; i < 2; i++) {
                    Card c = game.draw(targetContext, Cards.oracle, 2 - i);
                    if(c == null) {
                        break;
                    }
                    targetPlayer.reveal(c, this.getControlCard(), targetContext);
                    cards.add(c);
                }

                if(cards.size() > 0) {
                    if(currentPlayer.controlPlayer.oracle_shouldDiscard(context, targetPlayer, cards)) {
                        for(Card c : cards) {
                            targetPlayer.discard(c, this.getControlCard(), targetContext);
                        }
                    }
                    else {
                        Card[] order = (targetPlayer).controlPlayer.oracle_orderCards(context, cards.toArray(new Card[cards.size()]));
                        boolean bad = false;
                        if (order == null || order.length != cards.size()) {
                            bad = true;
                        } else {
                            ArrayList<Card> orderArray = new ArrayList<Card>();
                            for (Card card : order) {
                                orderArray.add(card);
                                if (!cards.contains(card)) {
                                    bad = true;
                                }
                            }

                            for (Card card : cards) {
                                if (!orderArray.contains(card)) {
                                    bad = true;
                                }
                            }
                        }

                        if (bad) {
                            Util.playerError(targetPlayer, "Oracle order cards error, ignoring.");
                            order = cards.toArray(new Card[cards.size()]);
                        }

                        for (int i = order.length - 1; i >= 0; i--) {
                            targetPlayer.putOnTopOfDeck(order[i]);
                        }
                    }
                }
            }
        }

        for(int i=0; i < 2; i++) {
            game.drawToHand(context, this, 2 - i);
        }
    }
    
    private void spiceMerchant(Game game, MoveContext context, Player currentPlayer) {
        boolean handContainsTreasure = false;
        for(Card c : currentPlayer.hand) {
            if(c.is(Type.Treasure, currentPlayer)) {
                handContainsTreasure = true;
                break;
            }
        }

        if(handContainsTreasure) {
            Card treasure = currentPlayer.controlPlayer.spiceMerchant_treasureToTrash(context);
            if(treasure != null) {
                if(!currentPlayer.hand.contains(treasure) || !treasure.is(Type.Treasure, currentPlayer)) {
                    Util.playerError(currentPlayer, "Spice Merchant returned invalid card to trash from hand, ignoring.");
                }
                else {
                    currentPlayer.hand.remove(treasure);
                    currentPlayer.trash(treasure, this.getControlCard(), context);

                    SpiceMerchantOption option = currentPlayer.controlPlayer.spiceMerchant_chooseOption(context);
                    if(option == SpiceMerchantOption.AddCardsAndAction) {
                        game.drawToHand(context, this, 2);
                        game.drawToHand(context, this, 1);
                        context.actions += 1;
                    }
                    else {
                        context.addCoins(2);
                        context.buys += 1;
                    }
                }
            }
        }
    }
    
    private void stables(Game game, MoveContext context, Player currentPlayer) {
        boolean valid = false;
        for(Card c : currentPlayer.hand) {
            if(c.is(Type.Treasure, currentPlayer)) {
                valid = true;
            }
        }

        if(valid) {
            Card toDiscard = currentPlayer.controlPlayer.stables_treasureToDiscard(context);

            // this.getControlCard() is optional, so ignore it if it's null or invalid
            if (toDiscard != null && currentPlayer.hand.contains(toDiscard) && toDiscard.is(Type.Treasure, currentPlayer)) {
                currentPlayer.hand.remove(toDiscard);
                currentPlayer.reveal(toDiscard, this.getControlCard(), context);
                currentPlayer.discard(toDiscard, this.getControlCard(), context);
                context.actions++;

                for (int i = 0; i < 3; i++) {
                    game.drawToHand(context, this, 3 - i);
                }
            }
        }
    }

    private void trader(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card card = currentPlayer.controlPlayer.trader_cardToTrash(context);
            if(card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Trader card to trash invalid, picking one");
                card = currentPlayer.hand.get(0);
            }

            int cost = card.getCost(context);
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.getControlCard(), context);
            for(int i=0; i < cost; i++) {
                if(currentPlayer.gainNewCard(Cards.silver, this.getControlCard(), context) == null) {
                    break;
                }
            }
        }
    }
}
