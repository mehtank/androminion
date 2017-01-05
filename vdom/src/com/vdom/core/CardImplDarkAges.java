package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.MoveContext.PileSelection;

public class CardImplDarkAges extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplDarkAges(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplDarkAges() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case Altar:
            altar(currentPlayer, context);
            break;
		case Armory:
            armory(currentPlayer, context);
            break;
		case BandOfMisfits:
            bandOfMisfits(game, context, currentPlayer);
            break;
		case BanditCamp:
            banditCamp(context, currentPlayer);
            break;
        case Beggar:
            beggar(currentPlayer, context);
            break;
        case Catacombs:
            catacombs(game, currentPlayer, context);
            break;
        case Count:
            count(currentPlayer, context);
            break;
        case Counterfeit:
        	multiPlayTreasure(context, game, currentPlayer);
        	break;
        case Cultist:
            cultist(context, game, currentPlayer);
            break;
        case DameAnna:
            dameAnna(context, currentPlayer);
            break;
        case DameJosephine:
        case DameMolly:
        	knight(context, currentPlayer);
        	break;
        case DameNatalie:
            dameNatalie(context, currentPlayer);
            break;
        case DameSylvia:
        	knight(context, currentPlayer);
        	break;
        case DeathCart:
            deathCart(currentPlayer, context);
            break;
        case Forager:
            forager(game, currentPlayer, context);
            break;
        case Graverobber:
            graverobber(game, currentPlayer, context);
            break;
        case Hermit:
            hermit(context, game, currentPlayer);
            break;
        case Ironmonger:
            ironmonger(game, currentPlayer, context);
            break;
        case JunkDealer:
            junkDealer(currentPlayer, context);
            break;
        case Madman:
            madman(context, game, currentPlayer);
            break;
        case Marauder:
            marauder(context, game, currentPlayer);
            break;
        case Mercenary:
            mercenary(context, game, currentPlayer);
            break;
        case Mystic:
            mystic(game, context, currentPlayer);
            break;
        case Pillage:
            pillage(game, context, currentPlayer);
            break;
        case PoorHouse:
            poorHouse(context, currentPlayer);
            break;
        case Procession:
        	throneRoomKingsCourt(game, context, currentPlayer);
        	break;
        case Rats:
            rats(context, currentPlayer);
            break;
        case Rebuild:
            rebuild(currentPlayer, context);
            break;
        case Rogue:
            rogue(game, context, currentPlayer);
            break;
        case Sage:
            sage(game, context, currentPlayer);
            break;
        case Scavenger:
            scavenger(game, context, currentPlayer);
            break;
        case SirBailey:
        case SirDestry:
        case SirMartin:
        	knight(context, currentPlayer);
            break;
        case SirMichael:
            sirMichael(context, game, currentPlayer);
            break;
        case SirVander:
            knight(context, currentPlayer);
            break;
        case Spoils:
        	spoils(game, currentPlayer);
        	break;
        case Squire:
            squire(context, currentPlayer);
            break;            
        case Storeroom:
            storeroom(game, context, currentPlayer);
            break;
        case Survivors:
            survivors(context, game, currentPlayer);
            break;
        case Urchin:
            urchin(context, game, currentPlayer);
            break;
        case Vagrant:
            vagrant(context, game, currentPlayer);
            break;
        case WanderingMinstrel:
            wanderingMinstrel(currentPlayer, context);
            break;
		default:
			break;
        
		}
	}
	
	@Override
	public void isTrashed(MoveContext context) {
		Cards.Kind trashKind = this.getKind();
    	if (this.getControlCard().equals(Cards.estate) && context.player.getInheritance() != null) {
    		trashKind = context.player.getInheritance().getKind();
    	}
    	
    	switch (trashKind) {
        case Rats:
            context.game.drawToHand(context, this, 1, true);
            break;
        case Squire:
            // Need to ensure that there is at least one Attack card that can be gained,
            // otherwise this.controlCard choice should be bypassed.
            boolean attackCardAvailable = false;

            for (Card c : context.game.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Attack))
            {
                if (context.game.getPile(c).getCount() > 0) {
                    attackCardAvailable = true;
                    break;
                }
            }

            if (attackCardAvailable)
            {
                Card s = context.player.controlPlayer.squire_cardToObtain(context);

                if (s != null) 
                {
                    context.player.controlPlayer.gainNewCard(s, this.getControlCard(), context);
                }
            }
            break;
        case Catacombs:
        	int cost = this.getControlCard().equals(Cards.estate) ? this.getControlCard().getCost(context) : this.getCost(context);
        	cost--;
        	if (cost >= 0) {
                Card c = context.player.controlPlayer.catacombs_cardToObtain(context, cost);
                if (c != null && c.getCost(context) <= cost && c.getDebtCost(context) == 0 && !c.costPotion()) {
                    context.player.controlPlayer.gainNewCard(c, this.getControlCard(), context);
                }
        	}
            break;
        case HuntingGrounds:
              // Wiki: If you trash Hunting Grounds and the Duchy pile is empty,
              // you can still choose Duchy (and gain nothing). 
            int duchyCount      = context.game.getPile(Cards.duchy).getCount();
            int estateCount     = context.game.getPile(Cards.estate).getCount();
            boolean gainDuchy   = false;
            boolean gainEstates = false;

            if (duchyCount > 0 || estateCount > 0)
            {
                Player.HuntingGroundsOption option = context.player.controlPlayer.huntingGrounds_chooseOption(context);
                if (option != null) {
                    switch (option) {
                        case GainDuchy:
                            gainDuchy = true;
                            break;
                        case GainEstates:
                            gainEstates = true;
                            break;
                        default:
                            break;
                    }
                }
            }
            
            if (gainDuchy)
            {
                context.player.controlPlayer.gainNewCard(Cards.duchy, this.getControlCard(), context);
            }
            else if (gainEstates)
            {
                context.player.controlPlayer.gainNewCard(Cards.estate, this.getControlCard(), context);
                context.player.controlPlayer.gainNewCard(Cards.estate, this.getControlCard(), context);
                context.player.controlPlayer.gainNewCard(Cards.estate, this.getControlCard(), context);
            }

            break;
        case Fortress:
        	//TODO: if Possessed, give choice of whether to put in hand or set aside card
            context.game.trashPile.remove(this.getControlCard());
            context.player.hand.add(this.getControlCard());
            break;
        case Cultist:
            context.game.drawToHand(context, this, 3, false);
            context.game.drawToHand(context, this, 2, false);
            context.game.drawToHand(context, this, 1, false);
            break;
        case SirVander:
            context.player.controlPlayer.gainNewCard(Cards.gold, this.getControlCard(), context);
            break;
        case OvergrownEstate:
            context.game.drawToHand(context, getControlCard(), 1);
            break;
        case Feodum:
            context.player.controlPlayer.gainNewCard(Cards.silver, this, context);
            context.player.controlPlayer.gainNewCard(Cards.silver, this, context);
            context.player.controlPlayer.gainNewCard(Cards.silver, this, context);
            break;
        default:
            break;
	    }
	    
	    // card left play - stop any impersonations
	    this.getControlCard().stopImpersonatingCard();
	    this.getControlCard().stopInheritingCardAbilities();
	}
	
	private void altar(Player currentPlayer, MoveContext context) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.altar_cardToTrash(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Altar trash error, trashing a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.getControlCard(), context);
        }

        Card card = currentPlayer.controlPlayer.altar_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 5 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
    }
	
	public void armory(Player currentPlayer, MoveContext context) {
        Card card = currentPlayer.controlPlayer.armory_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
    }
	
	private void bandOfMisfits(Game game, MoveContext context, Player currentPlayer) {
        // Already impersonating another card?
        if (!this.isImpersonatingAnotherCard()) {

            //Inherited Estates don't gain the cost from the card they inherit, but playing an Overlord as Band of Misfits have the cost of Band of Misfits.
            int cost = this.getControlCard().equals(Cards.estate) ? this.getControlCard().getCost(context) : this.getCost(context);
        	if (cost == 0) return;

            // Get card to impersonate
            Card cardToImpersonate = currentPlayer.controlPlayer.bandOfMisfits_actionCardToImpersonate(context, cost - 1);
            if (cardToImpersonate != null 
                && !game.isPileEmpty(cardToImpersonate)
                && Cards.isSupplyCard(cardToImpersonate)
                && cardToImpersonate.is(Type.Action, null)
                && cardToImpersonate.getCost(context) < cost
                && cardToImpersonate.getDebtCost(context) == 0
            	&& !cardToImpersonate.costPotion()
                && (context.golemInEffect == 0 || cardToImpersonate != Cards.golem)) {
                GameEvent event = new GameEvent(GameEvent.EventType.CardNamed, (MoveContext) context);
                event.card = cardToImpersonate;
                event.responsible = this;
                game.broadcastEvent(event);
                this.startImpersonatingCard(cardToImpersonate.getTemplateCard().instantiate());
            } else {
                Card[] cards = game.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Action);
                if (cards.length != 0 && cardToImpersonate != null) {
                    Util.playerError(currentPlayer, "Band of Misfits returned invalid card (" + cardToImpersonate.getName() + "), ignoring.");
                }
                return;
            }
        }

        // Play the impersonated card
        CardImpl cardToPlay = (CardImpl) this.behaveAsCard();
        context.freeActionInEffect++;
        cardToPlay.play(game, context, false);
        context.freeActionInEffect--;

        // impersonated card stays in play until next turn?
        if (cardToPlay.trashOnUse) {
            int idx = currentPlayer.playedCards.lastIndexOf(this.getControlCard());
            if (idx >= 0) currentPlayer.playedCards.remove(idx);
            currentPlayer.trash(this.getControlCard(), null, context);
        } else if (cardToPlay.is(Type.Duration, currentPlayer) && !cardToPlay.equals(Cards.outpost)) {
            if (!this.getControlCard().movedToNextTurnPile) {
                this.getControlCard().movedToNextTurnPile = true;
                int idx = currentPlayer.playedCards.lastIndexOf(this.getControlCard());
                if (idx >= 0) {
                    currentPlayer.playedCards.remove(idx);
                    currentPlayer.nextTurnCards.add(this.getControlCard());
                }
            }
        }
    }

	private void banditCamp(MoveContext context, Player currentPlayer)
    {
        // Gain a Spoils from the Spoils pile
        currentPlayer.gainNewCard(Cards.spoils, this.getControlCard(), context);
    }

    public void beggar(Player currentPlayer, MoveContext context) {
        currentPlayer.gainNewCard(Cards.copper, this.getControlCard(), context);
        currentPlayer.gainNewCard(Cards.copper, this.getControlCard(), context);
        currentPlayer.gainNewCard(Cards.copper, this.getControlCard(), context);
    }

    public void catacombs(Game game, Player currentPlayer, MoveContext context) {
        ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 3; i++) {
            Card card = game.draw(context, Cards.catacombs, 3 - i);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            if (currentPlayer.controlPlayer.catacombs_shouldDiscardTopCards(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]))) {
                while (!topOfTheDeck.isEmpty()) {
                    currentPlayer.discard(topOfTheDeck.remove(0), this.getControlCard(), context);
                }
                game.drawToHand(context, this, 3);
                game.drawToHand(context, this, 2);
                game.drawToHand(context, this, 1);
            } else {
                // Put the cards in hand
                for (Card c : topOfTheDeck) {
                    currentPlayer.hand.add(c);
                }
            }
        }
    }
    
    private void count(Player currentPlayer, MoveContext context) {
        Player.CountFirstOption option1 = currentPlayer.controlPlayer.count_chooseFirstOption(context);
        if (option1 == null) {
            Util.playerError(currentPlayer, "Count first option error, ignoring.");
        } else {
            switch (option1) {
                case Discard:
                    Card[] cards;
                    if (currentPlayer.hand.size() > 2) {
                        cards = currentPlayer.controlPlayer.count_cardsToDiscard(context);
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
                        Util.playerError(currentPlayer, "Count discard error, discarding first 2 cards.");
                        cards = new Card[2];

                        for (int i = 0; i < cards.length; i++) {
                            cards[i] = currentPlayer.hand.get(i);
                        }
                    }

                    for (int i = 0; i < cards.length; i++) {
                        currentPlayer.hand.remove(cards[i]);
                        currentPlayer.reveal(cards[i], this.getControlCard(), context);
                        currentPlayer.discard(cards[i], this.getControlCard(), null);
                    }
                    break;
                case PutOnDeck:
                    if (currentPlayer.getHand().size() > 0) {
                        Card card = currentPlayer.controlPlayer.count_cardToPutBackOnDeck(context);

                        if (card == null || !currentPlayer.hand.contains(card)) {
                            Util.playerError(currentPlayer, "Count error, just putting back a random card.");
                            card = Util.randomCard(currentPlayer.hand);
                        }

                        currentPlayer.hand.remove(card);
                        currentPlayer.putOnTopOfDeck(card);
                    }
                    break;
                case GainCopper:
                    currentPlayer.gainNewCard(Cards.copper, this.getControlCard(), context);
                    break;
            }
        }

        Player.CountSecondOption option2 = currentPlayer.controlPlayer.count_chooseSecondOption(context);
        if (option2 == null) {
            Util.playerError(currentPlayer, "Count second option error, ignoring.");
        } else {
            switch (option2) {
                case Coins:
                    context.addCoins(3);
                    break;
                case TrashHand:
                    if (currentPlayer.hand.size() > 0) {
                        Card[] temp = currentPlayer.hand.toArray();
                        for (Card c : temp) {
                            currentPlayer.hand.remove(c);
                            currentPlayer.trash(c, this.getControlCard(), context);
                        }
                    }
                    break;
                case GainDuchy:
                    currentPlayer.gainNewCard(Cards.duchy, this.getControlCard(), context);
                    break;
            }
        }
    }
    
    private void cultist(MoveContext context, Game game, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                player.gainNewCard(Cards.virtualRuins, this.getControlCard(), playerContext);
            }
        }

        if (currentPlayer.hand.contains(Cards.cultist) && currentPlayer.controlPlayer.cultist_shouldPlayNext(context)) {
            Card next = currentPlayer.hand.get(Cards.cultist);
            if (next != null) {
                context.freeActionInEffect++;

                next.play(game, context, true);

                context.freeActionInEffect--;
            }
        }
    }
    
    private void dameAnna(MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.dameAnna_cardsToTrash(context);
        if (cards != null) {
            if (cards.length > 2) {
                Util.playerError(currentPlayer, "Dame Anna trash error, trying to trash too many cards, ignoring.");
            } else {
                for (Card card : cards) {
                    for (int i = 0; i < currentPlayer.hand.size(); i++) {
                        Card playersCard = currentPlayer.hand.get(i);
                        if (playersCard.equals(card)) {
                            Card thisCard = currentPlayer.hand.remove(i);

                            currentPlayer.trash(thisCard, this.getControlCard(), context);
                            break;
                        }
                    }
                }
            }
        }

        knight(context, currentPlayer);
    }
    
    private void dameNatalie(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.dameNatalie_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 3 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            } else {
                Util.playerError(currentPlayer, "Dame Natalie error: chosen card that costs more then 3");
            }
        }

        knight(context, currentPlayer);
    }
    
    private void deathCart(Player currentPlayer, MoveContext context)
    {
        Card actionCardToTrash = currentPlayer.controlPlayer.deathCart_actionToTrash(context);
        if (actionCardToTrash != null)
        {
            currentPlayer.hand.remove(actionCardToTrash);
            currentPlayer.trash(actionCardToTrash, this.getControlCard(), context);
        }
        else if (!this.getControlCard().movedToNextTurnPile)
        {
            currentPlayer.playedCards.remove(this.getControlCard());
            currentPlayer.trash(this.getControlCard(), this.getControlCard(), context);
            this.getControlCard().movedToNextTurnPile = true;
        }
    }

    private void forager(Game game, Player currentPlayer, MoveContext context) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.forager_cardToTrash(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Forager trash error, trashing a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.getControlCard(), context);
        }

        HashSet<String> cardNames = new HashSet<String>();
        for (Card card : game.trashPile) {
            if (card == null) {
                break;
            }

            if (card.is(Type.Treasure, null)) {
                cardNames.add(card.getName());
            }
        }
        context.addCoins(cardNames.size());
    }

    private void graverobber(Game game, Player currentPlayer, MoveContext context) {
        Player.GraverobberOption option = currentPlayer.controlPlayer.graverobber_chooseOption(context);
        if (option == null) {
            Util.playerError(currentPlayer, "Graverobber option error, choosing automatically");
            option = Player.GraverobberOption.GainFromTrash;
        }

        Card toGain;

        switch (option) {
            case GainFromTrash:
                toGain = currentPlayer.controlPlayer.graverobber_cardToGainFromTrash(context);

                if (toGain == null || toGain.costPotion() || toGain.getCost(context) < 3 || toGain.getCost(context) > 6) {
                    Util.playerError(currentPlayer, "Graverobber gain card choice error, gaining nothing");
                    return;
                }

                context.graverobberGainedCardOnTop = true;
                toGain = game.trashPile.remove(game.trashPile.indexOf(toGain));
                currentPlayer.gainCardAlreadyInPlay(toGain, this.getControlCard(), context);

                break;

            case TrashActionCard:
                Card toTrash = currentPlayer.controlPlayer.graverobber_cardToTrash(context);

                if (toTrash == null || !currentPlayer.hand.contains(toTrash) || !(toTrash.is(Type.Action, currentPlayer))) {
                    Util.playerError(currentPlayer, "Graverobber trash error, trashing nothing.");
                    return;
                }

                currentPlayer.hand.remove(toTrash);
                currentPlayer.trash(toTrash, this.getControlCard(), context);

                context.graverobberGainedCardOnTop = false;
                toGain = currentPlayer.controlPlayer.graverobber_cardToReplace(context, 3 + toTrash.getCost(context), toTrash.getDebtCost(context), toTrash.costPotion());
                if (toGain != null && toGain.getCost(context) <= toTrash.getCost(context) + 3 && 
                		toGain.getDebtCost(context) <= toTrash.getDebtCost(context) && 
                		(!toGain.costPotion() || toTrash.costPotion()) &&
                		toGain.is(Type.Action)) {
                    currentPlayer.gainNewCard(toGain, this, context);
                }
                break;
        }
    }
    
    private void hermit(MoveContext context, Game game, Player currentPlayer)
    {
        ArrayList<Card> options = new ArrayList<Card>();

        Set<Card> inDiscard = new HashSet<Card>();
        for (Card c : currentPlayer.discard) {
            if (!(c.is(Type.Treasure, currentPlayer))) {
                inDiscard.add(c);
            }
        }
        options.addAll(inDiscard);
        Collections.sort(options, new Util.CardNameComparator());

        Set<Card> inHand = new HashSet<Card>();
        for (Card c: currentPlayer.hand) {
            if (!(c.is(Type.Treasure, currentPlayer))) {
                inHand.add(c);
            }
        }
        List<Card> handList = new ArrayList<Card>(inHand);
        Collections.sort(handList, new Util.CardNameComparator());
        options.addAll(handList);

        if (!options.isEmpty()) {
            // Offer the option to trash a non-treasure card
            context.hermitTrashCardPile = PileSelection.ANY;
            Card toTrash = currentPlayer.controlPlayer.hermit_cardToTrash(context,
                                                                          options,
                                                                          inDiscard.size());

            if (toTrash != null) {
                if (currentPlayer.discard.contains(toTrash) && (context.hermitTrashCardPile == PileSelection.ANY || context.hermitTrashCardPile == PileSelection.DISCARD)) {
                    currentPlayer.discard.remove(toTrash);
                    currentPlayer.trash(toTrash, this.getControlCard(), context);
                } else if (currentPlayer.hand.contains(toTrash) && (context.hermitTrashCardPile == PileSelection.ANY || context.hermitTrashCardPile == PileSelection.HAND)) {
                    currentPlayer.hand.remove(toTrash);
                    currentPlayer.trash(toTrash, this.getControlCard(), context);
                } else {
                    Util.playerError(currentPlayer, "Hermit trash error, chosen card to trash not in hand or discard, ignoring.");
                }
            }
        }

        // Gain a card costing up to 3 coins (no potion)
        Card c = currentPlayer.controlPlayer.hermit_cardToGain(context);
        if (c != null) {
            if (c.getCost(context, false) > 3 || c.getDebtCost(context) > 0 || c.costPotion() || !Cards.isSupplyCard(c)) {
                Util.playerError(currentPlayer, "Hermit card selection error, picking card from table.");
                c = (context.getCardsLeftInPile(Cards.silver) > 0) ? Cards.silver : Cards.copper; 
            }
            currentPlayer.controlPlayer.gainNewCard(c, this.getControlCard(), context);
        }
    }
    
    private void ironmonger(Game game, Player currentPlayer, MoveContext context) {
        Card card = game.draw(context, Cards.ironmonger, 1);
        
        if (card != null) {
            currentPlayer.reveal(card, this.getControlCard(), context);
            if (currentPlayer.controlPlayer.ironmonger_shouldDiscard(context, card)) {
                currentPlayer.discard(card, this.getControlCard(), context);
            } else {
                currentPlayer.putOnTopOfDeck(card, context, true);
            }
            
            if (card.is(Type.Action, currentPlayer)) {
                context.actions += 1;
            }
            if (card.is(Type.Treasure, currentPlayer)) {
                context.addCoins(1);
            }
            if (card.is(Type.Victory, currentPlayer)) {
                game.drawToHand(context, this, 1);
            }
        }
    }

    private void junkDealer(Player currentPlayer, MoveContext context) {
        if (!currentPlayer.hand.isEmpty()) {
            Card card = currentPlayer.controlPlayer.junkDealer_cardToTrash(context);
            if(card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Junk Dealer card to trash invalid, picking one");
                card = currentPlayer.hand.get(0);
            }
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.getControlCard(), context);
        }
    }
    
    private void madman(MoveContext context, Game game, Player currentPlayer) {
        if (currentPlayer.playedCards.contains(this.getControlCard())) {
            // Return to the Madman pile
            currentPlayer.playedCards.remove(this.getControlCard());
            CardPile pile = game.getPile(this.getControlCard());
            pile.addCard(this.getControlCard());

            int handSize = currentPlayer.hand.size();

            for (int i = 0; i < handSize; ++i) {
                game.drawToHand(context, this, handSize - i);
            }
        }
    }

    private void marauder(MoveContext context, Game game, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.spoils, this.getControlCard(), context);

        for (Player player : game.getPlayersInTurnOrder()) 
        {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) 
            {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                player.gainNewCard(Cards.virtualRuins, this.getControlCard(), playerContext);
            }
        }
    }
    
    private void mercenary(MoveContext context, Game game, Player currentPlayer) {
        int cardsTrashedCount = 0;

        Card[] cards = currentPlayer.controlPlayer.mercenary_cardsToTrash(context);

        if (cards != null) {
            if (cards.length > 2) {
                Util.playerError(currentPlayer, "Mercenary trash error, trying to trash too many cards, ignoring.");
            } else {
                if (cards.length > 1 || currentPlayer.hand.size() == 1) {
                    for (Card card : cards) {
                        for (int i = 0; i < currentPlayer.hand.size(); i++) {
                            Card playersCard = currentPlayer.hand.get(i);
                            if (playersCard.equals(card)) {
                                Card thisCard = currentPlayer.hand.remove(i);

                                currentPlayer.trash(thisCard, this.getControlCard(), context);
                                ++cardsTrashedCount;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (cardsTrashedCount == 2)
        {
            game.drawToHand(context, this, 2);
            game.drawToHand(context, this, 1);

            context.addCoins(2);

            for (Player player : game.getPlayersInTurnOrder()) {
                if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                    player.attacked(this.getControlCard(), context);
                    MoveContext playerContext = new MoveContext(game, player);
                    playerContext.attackedPlayer = player;

                    int keepCardCount = 3;
                    if (player.hand.size() > keepCardCount) {
                        Card[] cardsToKeep = player.controlPlayer.mercenary_attack_cardsToKeep(playerContext);
                        player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.getControlCard(), keepCardCount);
                    }
                }
            }
        }
    }
    
    private void mystic(Game game, MoveContext context, Player currentPlayer) {

        if (currentPlayer.deck.size() > 0 || currentPlayer.discard.size() > 0) {  // Only allow a guess if there are cards in the deck or discard pile

            // Create a list of all possible cards to guess, using the player's hand, discard pile, and deck 
            // (even though the player could technically name a card he doesn't have)
            ArrayList<Card> options = new ArrayList<Card>(currentPlayer.getDistinctCards());
            Collections.sort(options, new Util.CardNameComparator());

            if (options.size() > 0) {
                Card toName = currentPlayer.controlPlayer.mystic_cardGuess(context, options);
                currentPlayer.controlPlayer.namedCard(toName, this.getControlCard(), context);
                Card draw = game.draw(context, Cards.mystic, 1);

                if (draw != null) {
                    currentPlayer.reveal(draw, this.getControlCard(), context);

                    if (toName != null && toName.equals(draw)) {
                        currentPlayer.hand.add(draw);
                    } else {
                        currentPlayer.putOnTopOfDeck(draw, context, true);
                    }
                }
            }
        }
    }
    
    private void pillage(Game game, MoveContext context, Player currentPlayer) {

        // Each other player with 5 cards in hand reveals his hand and discards a card that you choose.
        for (Player targetPlayer : game.getPlayersInTurnOrder())
        {
            if (targetPlayer != currentPlayer &&
                !Util.isDefendedFromAttack(game, targetPlayer, this) &&
                targetPlayer.getHand().size() >= 5)
            {
                targetPlayer.attacked(this.getControlCard(), context);
                MoveContext targetContext = new MoveContext(context, game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                ArrayList<Card> cardsInHand = new ArrayList<Card>();

                for (Card card : targetPlayer.getHand())
                {
                    cardsInHand.add(card);
                    targetPlayer.reveal(card, this.getControlCard(), targetContext);
                }

                Card cardToDiscard = currentPlayer.controlPlayer.pillage_opponentCardToDiscard(targetContext, cardsInHand);

                if (cardToDiscard != null)
                {
                    targetPlayer.hand.remove(cardToDiscard);
                    targetPlayer.discard(cardToDiscard, this.getControlCard(), targetContext);
                }
            }
        }

        // Gain 2 Spoils from the Spoils pile
        currentPlayer.gainNewCard(Cards.spoils, this.getControlCard(), context);
        currentPlayer.gainNewCard(Cards.spoils, this.getControlCard(), context);
    }
    
    private void poorHouse(MoveContext context, Player currentPlayer) {
        int treasures = 0;

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            currentPlayer.reveal(card, this.getControlCard(), context);
            if (card.is(Type.Treasure, currentPlayer)) {
                treasures++;
            }
        }
        context.addCoins(-treasures);
    }
    
    private void rats(MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.rats, this.getControlCard(), context);

        if(currentPlayer.hand.size() > 0) {
            boolean hasother = false;
            for (Card c : currentPlayer.hand) {
                if (!c.equals(Cards.rats)) {
                    hasother = true;
                    break;
                }
            }
            if (!hasother) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card card = currentPlayer.hand.get(i);
                    currentPlayer.reveal(card, this.getControlCard(), context);
                }
            } else {
                Card card = currentPlayer.controlPlayer.rats_cardToTrash(context);
                if(card == null || card.equals(Cards.rats) || !currentPlayer.hand.contains(card)) {
                    Util.playerError(currentPlayer, "Rats card to trash invalid, picking one");
                    for (Card c : currentPlayer.hand){
                    	if (!c.equals(Cards.rats)) {
                    		card = c;
                    		break;
                    	}
                    }
                }

                currentPlayer.hand.remove(card);
                currentPlayer.trash(card, this.getControlCard(), context);
            }
        }
    }
    
    private void rebuild(Player currentPlayer, MoveContext context) {
        ArrayList<Card> allCards = new ArrayList<Card>(currentPlayer.getDistinctCards());
        ArrayList<Card> options = new ArrayList<Card>();
        for (Card c : allCards) {
            if(c.is(Type.Victory, currentPlayer))
                options.add(c);
        }
        Collections.sort(options, new Util.CardNameComparator());
        
        Card named = currentPlayer.controlPlayer.rebuild_cardToPick(context, options);        
        currentPlayer.controlPlayer.namedCard(named, this.getControlCard(), context);
        ArrayList<Card> cards = new ArrayList<Card>();
        Card last = null;

        // search for first Victory card that was not named
        while ((last = context.game.draw(context, Cards.rebuild, -1)) != null) {
            if (last.is(Type.Victory, currentPlayer) && !last.equals(named)) break;
            cards.add(last);
            currentPlayer.reveal(last, this.getControlCard(), context);
        }

        // Discard all other revealed cards
        for (Card c : cards) {
            currentPlayer.discard(c, this.getControlCard(), context);
        }

        if (last != null) {
            // Trash the found Victory card
            currentPlayer.trash(last, this.getControlCard(), context);

            // Gain Victory card that cost up to 3 more coins
            Card toGain = currentPlayer.controlPlayer.rebuild_cardToGain(context, 3 + last.getCost(context), last.getDebtCost(context), last.costPotion());
            if (toGain != null && toGain.getCost(context) <= last.getCost(context) + 3 &&
            		toGain.getDebtCost(context) <= last.getDebtCost(context) && (!toGain.costPotion() || last.costPotion())) {
                currentPlayer.gainNewCard(toGain, this.getControlCard(), context);
            }
        }
    }

    private void rogue(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> options = new ArrayList<Card>();
        for (Card c : game.trashPile) {
            if (!c.costPotion() && c.getCost(context) >= 3 && c.getCost(context) <= 6) {
                options.add(c);
            }
        }

        if (options.size() > 0) { // gain a card
            Card toGain = currentPlayer.controlPlayer.rogue_cardToGain(context);
            if (toGain == null) {
                Util.playerError(currentPlayer, "Rogue error, no card to gain selected, picking random");
                toGain = Util.randomCard(options);
            }

            game.trashPile.remove(toGain);
            currentPlayer.gainCardAlreadyInPlay(toGain, this.getControlCard(), context);
        } else { // Other players trash a card
            for (Player targetPlayer : game.getPlayersInTurnOrder()) {
                if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                    targetPlayer.attacked(this.getControlCard(), context);
                    MoveContext targetContext = new MoveContext(game, targetPlayer);
                    targetContext.attackedPlayer = targetPlayer;
                    ArrayList<Card> canTrash = new ArrayList<Card>();

                    List<Card> cardsToDiscard = new ArrayList<Card>();
                    for (int i = 0; i < 2; i++) {
                        Card card = game.draw(targetContext, Cards.rogue, 2 - i);

                        if (card != null) {
                            targetPlayer.reveal(card, this.getControlCard(), targetContext);
                            int cardCost = card.getCost(context);

                            if (!card.costPotion() && cardCost >= 3 && cardCost <= 6) {
                                canTrash.add(card);
                            } else {
                                cardsToDiscard.add(card);
                            }
                        }
                    }

                    for (Card c: cardsToDiscard) {
                        targetPlayer.discard(c, this.getControlCard(), targetContext);
                    }

                    Card cardToTrash = null;

                    if (canTrash.size() == 1) {
                        cardToTrash = canTrash.get(0);
                    } else if (canTrash.size() == 2) {
                        if (canTrash.get(0).equals(canTrash.get(1))) {
                            cardToTrash = canTrash.get(0);
                            targetPlayer.discard(canTrash.remove(1), this.getControlCard(), targetContext);
                        } else {
                            cardToTrash = targetPlayer.controlPlayer.rogue_cardToTrash(context, canTrash);
                        }

                        for (Card card : canTrash) {
                            if (!card.equals(cardToTrash)) {
                                targetPlayer.discard(card, this.getControlCard(), targetContext);
                            }
                        }
                    }

                    if (cardToTrash != null) {
                        targetPlayer.trash(cardToTrash, this.getControlCard(), targetContext);
                    }
                }
            }
        }
    }
    
    private void sage(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            cardNames.add(card.getName());
            //currentPlayer.reveal(card, this.getControlCard(), context);
        }

        ArrayList<Card> toDiscard = new ArrayList<Card>();

        Card draw = null;
        while ((draw = game.draw(context, Cards.sage, -1)) != null && draw.getCost(context) < 3) {
            currentPlayer.reveal(draw, this.getControlCard(), context);
            toDiscard.add(draw);
        }

        if (draw != null) {
            currentPlayer.reveal(draw, this.getControlCard(), context);
            currentPlayer.hand.add(draw);
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.getControlCard(), null);
        }
    }
    
    private void scavenger(Game game, MoveContext context, Player currentPlayer) {
        boolean discard = currentPlayer.getDeckSize() == 0 ? false : currentPlayer.controlPlayer.scavenger_shouldDiscardDeck(context);

        // Discard the entire deck if the player chose to do so
        if (discard)
        {
            GameEvent event = new GameEvent(GameEvent.EventType.DeckPutIntoDiscardPile, (MoveContext) context);
            game.broadcastEvent(event);
            while (currentPlayer.getDeckSize() > 0)
            {
                currentPlayer.discard(game.draw(context, Cards.scavenger, 0), this.getControlCard(), null, false, false);
            }
        }

        // Prompt to add a card from the discard pile back onto the deck, but only if at least one is available
        if (currentPlayer.getDiscardSize() > 0)
        {
            Card card = currentPlayer.controlPlayer.scavenger_cardToPutBackOnDeck(context);

            if (card != null)
            {
                currentPlayer.discard.remove(card);
                currentPlayer.putOnTopOfDeck(card);
                
                GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
                game.broadcastEvent(event);
            }
        }
    }
    
    private void sirMichael(MoveContext context, Game game, Player currentPlayer) {
        for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.sirMichael_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.getControlCard(), keepCardCount);
                }
            }
        }

        knight(context, currentPlayer);
    }
    
    private void spoils(Game game, Player currentPlayer) {
    	if (isInPlay(currentPlayer)) {
            CardPile pile = game.getPile(this);
            pile.addCard(currentPlayer.playedCards.remove(currentPlayer.playedCards.indexOf(this.getId())));
    	}
    }
    
    private void squire(MoveContext context, Player currentPlayer) {
        Player.SquireOption option = currentPlayer.controlPlayer.squire_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Squire option error, ignoring.");
        } else {
            if (option == Player.SquireOption.AddActions) {
                context.actions += 2;
            } else if (option == Player.SquireOption.AddBuys) {
                context.buys += 2;
            } else if (option == Player.SquireOption.GainSilver) {
                currentPlayer.gainNewCard(Cards.silver, this.getControlCard(), context);
            }
        }
    }
    
    private void storeroom(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.storeroom_cardsToDiscardForCards(context);
        if (cards != null) {
            int numberOfCards = 0;
            for (Card card : cards) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card playersCard = currentPlayer.hand.get(i);
                    if (playersCard.equals(card)) {
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.getControlCard(), context);
                        numberOfCards++;
                        break;
                    }
                }
            }

            if (numberOfCards != cards.length) {
                Util.playerError(currentPlayer, "Storeroom discard error, trying to discard cards not in hand, ignoring extra.");
            }

            for (int i = 0; i < numberOfCards; ++i) {
            	game.drawToHand(context, this, numberOfCards - i);
            }
        }

        cards = currentPlayer.controlPlayer.storeroom_cardsToDiscardForCoins(context);
        if (cards != null) {
            int numberOfCards = 0;
            for (Card card : cards) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card playersCard = currentPlayer.hand.get(i);
                    if (playersCard.equals(card)) {
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.getControlCard(), context);
                        numberOfCards++;
                        break;
                    }
                }
            }

            if (numberOfCards != cards.length) {
                Util.playerError(currentPlayer, "Storeroom discard error, trying to discard cards not in hand, ignoring extra.");
            }

            while (numberOfCards > 0) {
                numberOfCards--;
                context.addCoins(1);
            }
        }
    }
    
    private void survivors(MoveContext context, Game game, Player currentPlayer) {
        ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 2; i++) {
            Card card = game.draw(context, Cards.survivors, 2 - i);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            if (currentPlayer.controlPlayer.survivors_shouldDiscardTopCards(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]))) {
                while (!topOfTheDeck.isEmpty()) {
                    currentPlayer.discard(topOfTheDeck.remove(0), this.getControlCard(), context);
                }
            } else {
                Card[] order = currentPlayer.controlPlayer.survivors_cardOrder(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));

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
                    Util.playerError(currentPlayer, "Survivors order cards error, ignoring.");
                    order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
                }

                // Put the cards back on the deck
                for (int i = order.length - 1; i >= 0; i--) {
                    currentPlayer.putOnTopOfDeck(order[i]);
                }
            }
        }
    }

    private void urchin(MoveContext context, Game game, Player currentPlayer)   {       
        for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                int keepCardCount = 4;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.urchin_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.getControlCard(), keepCardCount);
                }
            }
        }
    }

    private void vagrant(MoveContext context, Game game, Player currentPlayer) {
        Card c = game.draw(context, Cards.vagrant, 1);
        if (c != null) {
            currentPlayer.reveal(c, this.getControlCard(), context);
            if (c.getKind() == Cards.Kind.Curse || c.is(Type.Shelter, currentPlayer) || (c.is(Type.Victory, currentPlayer)) || (c.is(Type.Ruins, currentPlayer))) {
                currentPlayer.hand.add(c);
            } else {
                currentPlayer.putOnTopOfDeck(c, context, true);
            }
        }
    }
    
    private void wanderingMinstrel(Player currentPlayer, MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 3; i++) {
            Card card = context.game.draw(context, Cards.wanderingMinstrel, 3 - i);
            if (card == null) {
                break;
            }
            if (!(card.is(Type.Action, currentPlayer)) ) {
                currentPlayer.discard(card, this.getControlCard(), context);
            } else {
                currentPlayer.reveal(card, this.getControlCard(), context);
                cards.add(card);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        Card[] orderedCards = currentPlayer.controlPlayer.topOfDeck_orderCards(context, cards.toArray(new Card[0]));

        for (int i = orderedCards.length - 1; i >= 0; i--) {
            currentPlayer.putOnTopOfDeck(orderedCards[i], context, true);
        }

    }
    
    
    
    
    
    
    private void knight(MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(context.game, targetPlayer, this)) {
                targetPlayer.attacked(this.getControlCard(), context);
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                ArrayList<Card> canTrash = new ArrayList<Card>();

                List<Card> cardsToDiscard = new ArrayList<Card>();
                for (int i = 0; i < 2; i++) {
                    Card card = context.game.draw(targetContext, this, 2 - i);

                    if (card != null) {
                        targetPlayer.reveal(card, this.getControlCard(), targetContext);
                        int cardCost = card.getCost(context);

                        if (!card.costPotion() && cardCost >= 3 && cardCost <= 6) {
                            canTrash.add(card);
                        } else {
                            cardsToDiscard.add(card);
                        }
                    }
                }

                for (Card c: cardsToDiscard) {
                    targetPlayer.discard(c, this.getControlCard(), targetContext);
                }

                Card cardToTrash = null;

                if (canTrash.size() == 1) {
                    cardToTrash = canTrash.get(0);
                } else if (canTrash.size() == 2) {
                    if (canTrash.get(0).equals(canTrash.get(1))) {
                        cardToTrash = canTrash.get(0);
                        targetPlayer.discard(canTrash.remove(1), this.getControlCard(), targetContext);
                    } else {
                        cardToTrash = targetPlayer.knight_cardToTrash(targetContext, canTrash);
                    }

                    for (Card card : canTrash) {
                        if (!card.equals(cardToTrash)) {
                            targetPlayer.discard(card, this.getControlCard(), targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.getControlCard(), targetContext);

                    // If the card trashed was a knight, the attacking knight should be trashed as well
                    if (cardToTrash.is(Type.Knight, targetPlayer) && currentPlayer.playedCards.contains(this.getControlCard()) && currentPlayer.playedCards.getLastCard() == this.getControlCard()) {
                        currentPlayer.trash(currentPlayer.playedCards.removeLastCard(), cardToTrash, context);
                    }
                }
            }
        }       
    }

}
