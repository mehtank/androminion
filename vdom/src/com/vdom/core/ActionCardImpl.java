package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CurseCard;
import com.vdom.api.DurationCard;
import com.vdom.api.GameEvent;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.Player.JesterOption;
import com.vdom.core.Player.SpiceMerchantOption;
import com.vdom.core.Player.TournamentOption;
import com.vdom.core.Player.TrustySteedOption;

public class ActionCardImpl extends CardImpl implements ActionCard {
    protected int addActions;
    protected int addBuys;
    protected int addCards;
    protected int addGold;
    protected int addVictoryTokens;
    protected boolean attack;

    public ActionCardImpl(Builder builder) {
        super(builder);
        addActions = builder.addActions;
        addBuys = builder.addBuys;
        addCards = builder.addCards;
        addGold = builder.addGold;
        addVictoryTokens = builder.addVictoryTokens;
        attack = builder.attack;
        trashOnUse = builder.trashOnUse;
    }

    public static class Builder extends CardImpl.Builder{
	    protected int addActions;
	    protected int addBuys;
	    protected int addCards;
	    protected int addGold;
	    protected int addVictoryTokens;
	    protected boolean attack;
	    protected boolean trashOnUse;

        public Builder(String name, int cost) {
            super(name, cost);
        }

        public Builder addActions(int val) {
            addActions = val;
            return this;
        }

        public Builder addBuys(int val) {
            addBuys = val;
            return this;
        }

        public Builder addCards(int val) {
            addCards = val;
            return this;
        }

        public Builder addGold(int val) {
            addGold = val;
            return this;
        }
        
        public Builder addVictoryTokens(int val) {
            addVictoryTokens = val;
            return this;
        }

        public Builder attack() {
            attack = true;
            return this;
        }

        public Builder trashOnUse() {
            trashOnUse = true;
            return this;
        }

        public ActionCardImpl build() {
            return new ActionCardImpl(this);
        }

    }

    public int getAddActions() {
        return addActions;
    }

    public int getAddBuys() {
        return addBuys;
    }

    public int getAddCards() {
        return addCards;
    }

    public int getAddGold() {
        return addGold;
    }
    
    public int getAddVictoryTokens() {
        return addVictoryTokens;
    }

    public boolean isAttack() {
        return attack;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        ActionCardImpl c = new ActionCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(ActionCardImpl c) {
        super.copyValues(c);
        c.addActions = addActions;
        c.addBuys = addBuys;
        c.addCards = addCards;
        c.addGold = addGold;
        c.addVictoryTokens = addVictoryTokens;
        c.attack = attack;
        c.trashOnUse = trashOnUse;
    }

    protected ActionCardImpl() {
    }

    public void play(Game game, MoveContext context) {
        super.play(game, context);

        Player currentPlayer = context.getPlayer();

        if (context.numberTimesAlreadyPlayed == 0) {
            currentPlayer.hand.remove(this);
            if (trashOnUse) {
                currentPlayer.trash(this, null, context);
            } else if (this instanceof DurationCard) {
                currentPlayer.nextTurnCards.add((DurationCard) this);
            } else {
                context.playedCards.add(this);
            }
        }

        GameEvent event = new GameEvent(GameEvent.Type.PlayingAction, (MoveContext) context);
        event.card = this;
        game.broadcastEvent(event);

        // playing an action
        context.actionsPlayedSoFar++;
        if (context.freeActionInEffect == 0) {
            context.actions--;
        }

        context.actions += addActions;
        context.buys += addBuys;
        context.addGold += addGold;
        currentPlayer.addVictoryTokens(context, addVictoryTokens);

        int cardsToDraw = addCards;
        while (cardsToDraw > 0) {
            cardsToDraw--;
            game.drawToHand(currentPlayer, this);
        }

        additionalCardActions(game, context, currentPlayer);

        event = new GameEvent(GameEvent.Type.PlayedAction, (MoveContext) context);
        event.card = this;
        game.broadcastEvent(event);
    }

    protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
        if (this.equals(Cards.workshop)) {
            workshop(currentPlayer, context);
        } else if (this.equals(Cards.moneyLender)) {
            moneyLender(context, currentPlayer);
        } else if (this.equals(Cards.chancellor)) {
            chancellor(game, context, currentPlayer);
        } else if (this.equals(Cards.feast)) {
            feast(context, currentPlayer);
        } else if (equals(Cards.kingsCourt) || equals(Cards.throneRoom)) {
            throneRoomKingsCourt(game, context, currentPlayer);
        } else if (equals(Cards.smugglers)) {
            smugglers(context, currentPlayer);
        } else if (equals(Cards.pirateShip)) {
            pirateShip(game, context, currentPlayer); 
        } else if (equals(Cards.haven)) {
            haven(context, currentPlayer);
        } else if (equals(Cards.chapel)) {
            chapel(context, currentPlayer);
        } else if (equals(Cards.library)) {
            library(game, context, currentPlayer);
        } else if (equals(Cards.adventurer)) {
            adventurer(game, context, currentPlayer);
        } else if (equals(Cards.golem)) {
            golem(game, context, currentPlayer);
        } else if (equals(Cards.bureaucrat)) {
            bureaucrat(game, context, currentPlayer);
        } else if (equals(Cards.secretChamber)) {
            secretChamber(context, currentPlayer);
        } else if (equals(Cards.cellar)) {
            cellar(game, context, currentPlayer);
        } else if (equals(Cards.remodel)) {
            remodel(context, currentPlayer);
        } else if (equals(Cards.militia)) {
            militia(game, context, currentPlayer);
        } else if (equals(Cards.thief)) {
            thief(game, context, currentPlayer);
        } else if (equals(Cards.conspirator)) {
            consiprator(game, context, currentPlayer);
        } else if (equals(Cards.spy) || equals(Cards.scryingPool)) {
            spyAndScryingPool(game, context, currentPlayer);
        } else if (equals(Cards.courtyard)) {
            courtyard(context, currentPlayer);
        } else if (equals(Cards.baron)) {
            baron(context, currentPlayer);
        } else if (equals(Cards.swindler)) {
            swindler(game, context, currentPlayer);
        } else if (equals(Cards.steward)) {
            steward(game, context, currentPlayer);
        } else if (equals(Cards.scout)) {
            scout(game, context, currentPlayer);
        } else if (equals(Cards.shantyTown)) {
            shantyTown(game, context, currentPlayer);
        } else if (equals(Cards.saboteur)) {
            saboteur(game, context, currentPlayer);
        } else if (equals(Cards.pawn)) {
            pawn(game, context, currentPlayer);
        } else if (equals(Cards.minion)) {
            minion(game, context, currentPlayer);
        } else if (equals(Cards.miningVillage)) {
            miningVillage(context, currentPlayer);
        } else if (equals(Cards.masquerade)) {
            masquerade(game, context, currentPlayer);
        } else if (equals(Cards.ironworks)) {
            ironworks(game, context, currentPlayer);
        } else if (equals(Cards.nobles)) {
            nobles(game, context, currentPlayer);
        } else if (equals(Cards.coppersmith)) {
            copperSmith(context);
        } else if (equals(Cards.tribute)) {
            tribute(game, context, currentPlayer);
        } else if (equals(Cards.upgrade)) {
            upgrade(context, currentPlayer);
        } else if (equals(Cards.wishingWell)) {
            wishingWell(game, context, currentPlayer);
        } else if (equals(Cards.bridge)) {
            context.cardCostModifier -= 1;
        } else if (equals(Cards.tradingPost)) {
            tradingPost(context, currentPlayer);
        } else if (equals(Cards.torturer)) {
            torturer(game, context, currentPlayer);
        } else if (equals(Cards.tactician)) {
            tactician(context, currentPlayer);
        } else if (equals(Cards.familiar) || equals(Cards.witch)) {
            witchFamiliar(game, context, currentPlayer);
        } else if (equals(Cards.apothecary)) {
            apothecary(game, context, currentPlayer);
        } else if (equals(Cards.transmute)) {
            transmute(context, currentPlayer);
        } else if (equals(Cards.apprentice)) {
            apprentice(game, context, currentPlayer);
        } else if (equals(Cards.university)) {
            university(context, currentPlayer);
        } else if (equals(Cards.mine)) {
            mine(context, currentPlayer);
        } else if (equals(Cards.councilRoom)) {
            councilRoom(game, context);
        } else if (equals(Cards.seaHag)) {
            seaHag(game, context, currentPlayer);
        } else if (equals(Cards.nativeVillage)) {
            nativeVillage(game, context, currentPlayer);
        } else if (equals(Cards.island)) {
            island(context, currentPlayer);
        } else if (equals(Cards.pearlDiver)) {
            pearlDiver(context, currentPlayer);
        } else if (equals(Cards.lookout)) {
            lookout(game, context, currentPlayer);
        } else if (equals(Cards.navigator)) {
            navigator(game, context, currentPlayer);
        } else if (equals(Cards.embargo)) {
            embargo(game, context, currentPlayer);
        } else if (equals(Cards.treasureMap)) {
            treasureMap(context, currentPlayer);
        } else if (equals(Cards.explorer)) {
            explorer(context, currentPlayer);
        } else if (equals(Cards.cutpurse)) {
            cutpurse(game, context, currentPlayer);
        } else if (equals(Cards.warehouse)) {
            warehouse(context, currentPlayer);
        } else if (equals(Cards.ambassador)) {
            ambassador(game, context, currentPlayer);
        } else if (equals(Cards.salvager)) {
            salvager(context, currentPlayer);
        } else if (equals(Cards.ghostShip)) {
            ghostShip(game, context, currentPlayer);
        } else if (equals(Cards.bishop)) {
            bishop(game, context, currentPlayer);
        } else if (equals(Cards.city)) {
            city(game, context, currentPlayer);
        } else if (equals(Cards.countingHouse)) {
            countingHouse(context, currentPlayer);
        } else if (equals(Cards.expand)) {
            expand(context, currentPlayer);
        } else if (equals(Cards.forge)) {
            forge(context, currentPlayer);
        } else if (equals(Cards.goons)) {
            goons(game, context, currentPlayer);
        } else if (equals(Cards.mint)) {
            mint(context, currentPlayer);
        } else if (equals(Cards.mountebank)) {
            mountebank(game, context, currentPlayer);
        } else if (equals(Cards.rabble)) {
            rabble(game, context, currentPlayer);
        } else if (equals(Cards.tradeRoute)) {
            tradeRoute(game, context, currentPlayer);
        } else if (equals(Cards.vault)) {
            vault(game, context, currentPlayer);
        } else if (equals(Cards.watchTower)) {
            watchTower(game, currentPlayer);
        } else if (equals(Cards.farmingVillage)) {
            farmingVillage(game, context, currentPlayer);
        } else if (equals(Cards.fortuneTeller)) {
            fortuneTeller(game, context, currentPlayer);
        } else if (equals(Cards.hamlet)) {
            hamlet(context, currentPlayer);
        } else if (equals(Cards.harvest)) {
            harvest(game, context, currentPlayer);
        } else if (equals(Cards.horseTraders)) {
            horseTraders(context, currentPlayer);
        } else if (equals(Cards.huntingParty)) {
            huntingParty(game, context, currentPlayer);
        } else if (equals(Cards.jester)) {
            jester(game, context, currentPlayer);
        } else if (equals(Cards.menagerie)) {
            menagerie(game, context, currentPlayer);
        } else if (equals(Cards.remake)) {
            remake(context, currentPlayer);
        } else if (equals(Cards.tournament)) {
            tournament(game, context, currentPlayer);
        } else if (equals(Cards.youngWitch)) {
            youngwitch(game, context, currentPlayer);
        } else if (equals(Cards.bagOfGold)) {
            currentPlayer.gainNewCard(Cards.gold, this, context);
        } else if (equals(Cards.followers)) {
            followers(game, context, currentPlayer);
        } else if (equals(Cards.princess)) {
            if (context.numberTimesAlreadyPlayed == 0) {
            	context.cardCostModifier -= 2;
            }
        } else if (equals(Cards.trustySteed)) {
            trustySteed(game, context, currentPlayer);
        } else if (equals(Cards.crossroads)) {
            crossroads(game, context, currentPlayer);
        } else if (equals(Cards.duchess)) {
            duchess(game);
        } else if (equals(Cards.develop)) {
            develop(context, currentPlayer);            
        } else if (equals(Cards.oasis)) {
            oasis(context, currentPlayer);
        } else if (equals(Cards.jackOfAllTrades)) {
            jackOfAllTrades(game, context, currentPlayer);
        } else if (equals(Cards.nobleBrigand)) {
            game.nobleBrigandAttack(context, this, true);
        } else if (equals(Cards.spiceMerchant)) {
            spiceMerchant(game, context, currentPlayer);
        } else if (equals(Cards.oracle)) {
            oracle(game, context, currentPlayer);
        } else if (equals(Cards.scheme)) {
            context.schemesPlayed++;
        } else if (equals(Cards.trader)) {
            trader(context, currentPlayer);
        } else if (equals(Cards.cartographer)) {
            cartographer(game, context, currentPlayer);
        } else if (equals(Cards.embassy)) {
            embassy(context, currentPlayer);
        } else if (equals(Cards.highway)) {
            if (context.numberTimesAlreadyPlayed == 0) {
                context.cardCostModifier -= 1;
            }
        } else if (equals(Cards.inn)) {
            inn(context, currentPlayer);
        } else if (equals(Cards.mandarin)) {
            mandarin(context, currentPlayer);
        } else if (equals(Cards.margrave)) {
            margrave(game, context, currentPlayer);
        } else if (equals(Cards.stables)) {
            stables(game, context, currentPlayer);
        } else if (equals(Cards.possession)) {
            possession(context);
        }
    }

    private void warehouse(MoveContext context, Player currentPlayer) {
        if (currentPlayer.hand.size() == 0) {
            return;
        }

        Card[] cards;
        if (currentPlayer.hand.size() > 3) {
            cards = currentPlayer.warehouse_cardsToDiscard(context);
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
            Util.playerError(currentPlayer, "Warehouse discard error, discarding first 3 cards.");
            cards = new Card[3];

            for (int i = 0; i < cards.length; i++) {
                cards[i] = currentPlayer.hand.get(i);
            }
        }

        for (int i = 0; i < cards.length; i++) {
            currentPlayer.hand.remove(cards[i]);
            currentPlayer.reveal(cards[i], this, context);
            currentPlayer.discard(cards[i], this, null);
        }
    }

    private void cutpurse(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                if (player.hand.contains(Cards.copper)) {
                    Card card = player.hand.get(Cards.copper);
                    player.hand.remove(Cards.copper);
                    player.discard(card, this, playerContext);
                } else {
                    for (Card card : player.getHand()) {
                        player.reveal(card, this, playerContext);
                    }
                }
            }
        }
    }

    private void explorer(MoveContext context, Player currentPlayer) {
        boolean province = false;
        for (Card card : currentPlayer.hand) {
            if (card.equals(Cards.province)) {
                province = true;
                break;
            }
        }

        Card treasure;
        if (province) {
            treasure = Cards.gold;
        } else {
            treasure = Cards.silver;
        }

        currentPlayer.gainNewCard(treasure, this, context);
    }

    private void treasureMap(MoveContext context, Player currentPlayer) {
        boolean anotherMap = false;
        for (Card card : currentPlayer.hand) {
            if (card.equals(Cards.treasureMap)) {
                anotherMap = true;
                break;
            }
        }

        if (anotherMap) {
            // This card will already be trashed, so just trash one more
            currentPlayer.hand.remove(Cards.treasureMap);

            // Send notifications for both the map played and the other one.
            for (int i = 0; i < 2; i++) {
                currentPlayer.trash(Cards.treasureMap, null, context);
            }

            for (int i = 0; i < 4; i++) {
                currentPlayer.gainNewCard(Cards.gold, this, context);
            }
        } else {
            // Send notification that the map played was trashed (as per rules
            // when a single map is played)
            currentPlayer.hand.remove(Cards.treasureMap);
            currentPlayer.trash(Cards.treasureMap, null, context);
        }
    }

    private void embargo(Game game, MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.embargo_supplyToEmbargo(context);
        if (card == null || !game.isCardInGame(card)) {
            Util.playerError(currentPlayer, "Embargo error, adding embargo to random card.");
            card = Util.randomCard(context.getCardsInPlay());
        }

        game.addEmbargo(card.getName());

        GameEvent event = new GameEvent(GameEvent.Type.Embargo, context);
        event.card = card;
        game.broadcastEvent(event);
    }

    private void navigator(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) {
            Card card = game.draw(currentPlayer);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            if (currentPlayer.navigator_shouldDiscardTopCards(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]))) {
                while (!topOfTheDeck.isEmpty()) {
                    currentPlayer.discard(topOfTheDeck.remove(0), this, null);
                }
            } else {
                Card[] order = currentPlayer.navigator_cardOrder(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));

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
                    Util.playerError(currentPlayer, "Navigator order cards error, ignoring.");
                    order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
                }

                // Put the cards back on the deck
                for (int i = order.length - 1; i >= 0; i--) {
                    currentPlayer.putOnTopOfDeck(order[i]);
                }
            }
        }
    }

    private void lookout(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 3; i++) {
            Card card = game.draw(currentPlayer);
            if (card != null) {
                cards.add(card);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        Card toTrash;

        if (cards.size() > 1) {
            toTrash = currentPlayer.lookout_cardToTrash(context, cards.toArray(new Card[cards.size()]));
        } else {
            toTrash = cards.get(0);
        }

        if (toTrash == null || !cards.contains(toTrash)) {
            Util.playerError(currentPlayer, "Lookout trash error, just picking the first card.");
            toTrash = cards.get(0);
        }

        currentPlayer.trash(toTrash, this, context);

        cards.remove(toTrash);
        if (cards.size() == 0) {
            return;
        }

        Card toDiscard;

        if (cards.size() > 1) {
            toDiscard = currentPlayer.lookout_cardToDiscard(context, cards.toArray(new Card[cards.size()]));
        } else {
            toDiscard = cards.get(0);
        }
        if (toDiscard == null || !cards.contains(toDiscard)) {
            Util.playerError(currentPlayer, "Lookout discard error, just picking the first card.");
            toDiscard = cards.get(0);
        }

        currentPlayer.discard(toDiscard, this, context);

        cards.remove(toDiscard);

        if (cards.size() > 0) {
            currentPlayer.putOnTopOfDeck(cards.get(0));
        }
    }

    private void pearlDiver(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getDeckSize() <= 1 && currentPlayer.discard.size() + currentPlayer.getDeckSize() > 1) {
            context.game.replenishDeck(currentPlayer);
        }

        if (currentPlayer.getDeckSize() > 1) {
            Card card = currentPlayer.peekAtDeckBottom();
            if (currentPlayer.pearlDiver_shouldMoveToTop(context, card)) {
                currentPlayer.removeFromDeckBottom();
                currentPlayer.putOnTopOfDeck(card);
            }
        }
    }

    private void island(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.island_cardToSetAside(context);
        if (card != null && !currentPlayer.hand.contains(card)) {
            Util.playerError(currentPlayer, "Island set aside card error, just setting aside island.");
            card = null;
        }

        // Check to see if the island has already been set aside (in case island was throne roomed)
        boolean alreadySetAside = false;
        for (Card islandCard : currentPlayer.island) {
            if (islandCard == this) {
                alreadySetAside = true;
                break;
            }
        }
        if (!alreadySetAside) {
            currentPlayer.island.add(this);
        }

        if (card != null) {
            currentPlayer.hand.remove(card);
            currentPlayer.island.add(card);
        }
    }

    private void nativeVillage(Game game, MoveContext context, Player currentPlayer) {
        if (currentPlayer.nativeVillage_takeCards(context)) {
            while (!currentPlayer.nativeVillage.isEmpty()) {
                currentPlayer.hand.add(currentPlayer.nativeVillage.remove(0));
            }
        } else {
            Card draw = game.draw(currentPlayer);
            if (draw != null) {
                currentPlayer.nativeVillage.add(draw);
                Util.sensitiveDebug(currentPlayer, "Added to Native Village:" + draw.getName(), true);
            }
        }
    }

    private void seaHag(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                Card draw = game.draw(player);
                if (draw != null) {
                    player.discard(draw, this, playerContext);
                }

                player.gainNewCard(Cards.curse, this, playerContext);
            }
        }
    }

    private void councilRoom(Game game, MoveContext context) {
        for (Player player : getAllPlayers()) {
            if (player != context.getPlayer()) {
                drawToHand(game, player, this);
            }
        }
    }

    private void mine(MoveContext context, Player currentPlayer) {
        TreasureCard cardToUpgrade = currentPlayer.mine_treasureFromHandToUpgrade(context);
        if (cardToUpgrade != null) {
            CardList hand = currentPlayer.getHand();
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);

                if (cardToUpgrade.equals(card)) {
                    Card thisCard = removeFromHand(currentPlayer, i);
                    currentPlayer.trash(thisCard, this, context);

                    TreasureCard newCard = currentPlayer.mine_treasureToObtain(context, card.getCost(context) + 3, card.costPotion());
                    if (newCard != null) {
                        if (newCard.getCost(context) <= card.getCost(context) + 3) {
                            currentPlayer.gainNewCard(newCard, this, context);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void university(MoveContext context, Player currentPlayer) {
        ActionCard cardToObtain = currentPlayer.university_actionCardToObtain(context);
        if (cardToObtain != null && cardToObtain instanceof ActionCard && cardToObtain.getCost(context) <= 5 && !cardToObtain.costPotion()) {
            currentPlayer.gainNewCard(cardToObtain, this, context);
        }
    }

    private void apprentice(Game game, MoveContext context, Player currentPlayer) {
    	if(currentPlayer.hand.size() > 0) {
	        Card cardToTrash = currentPlayer.apprentice_cardToTrash(context);
	        if (cardToTrash == null || !currentPlayer.hand.contains(cardToTrash)) {
	            Util.playerError(currentPlayer, "Apprentice card to trash was invalid, trashing random card.");
	            cardToTrash = Util.randomCard(currentPlayer.hand);
	        }
            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this, context);

            for (int i = 1; i <= (cardToTrash.getCost(context) + (cardToTrash.costPotion() ? 2 : 0)); i++) {
                game.drawToHand(currentPlayer, this);
            }
    	}
    }

    private void transmute(MoveContext context, Player currentPlayer) {
        Card cardToTrash = currentPlayer.transmute_cardToTrash(context);
        if (cardToTrash == null) {
            Util.playerError(currentPlayer, "Transmute card to trash was null, not trashing anything.");
        } else if (!currentPlayer.hand.contains(cardToTrash)) {
            Util.playerError(currentPlayer, "Transmute card to trash is not in your hand, not trashing anything.");
        } else {
            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this, context);
            if (cardToTrash instanceof ActionCard) {
                currentPlayer.gainNewCard(Cards.duchy, this, context);
            }
            if (cardToTrash instanceof TreasureCard) {
                currentPlayer.gainNewCard(Cards.transmute, this, context);
            }
            if (cardToTrash instanceof VictoryCard) {
                currentPlayer.gainNewCard(Cards.gold, this, context);
            } 
        }
    }

    private void apothecary(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> residue = new ArrayList<Card>();
        for (int i = 1; i <= 4; i++) {
            Card card = game.draw(currentPlayer);
            if (card == null) {
            } else if (card.equals(Cards.copper) || card.equals(Cards.potion)) {
                currentPlayer.hand.add(card);
            } else {
                residue.add(card);
            }
        }

        if (residue.size() > 0) {
            ArrayList<Card> playerCards = currentPlayer.apothecary_cardsForDeck(context, residue);
            if (playerCards == null) {
                playerCards = residue;
            }
            for (int i = playerCards.size() - 1; i >= 0; i--) {
                Card card = playerCards.get(i);
                currentPlayer.putOnTopOfDeck(card);
            }
        }
    }

    private void witchFamiliar(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                player.gainNewCard(Cards.curse, this, new MoveContext(game, player));
            }
        }
    }

    private void tactician(MoveContext context, Player currentPlayer) {
        // This card handles putting it where it needs to go after this turn since
        // it depends on your hand. The Game class knows about this card
        // specifically and knows not to treat it like other cards in this respect.
        if (currentPlayer.hand.size() > 0) {
            while (!currentPlayer.hand.isEmpty()) {
                currentPlayer.discard(currentPlayer.hand.remove(0), this, null);
            }
            currentPlayer.nextTurnCards.add((DurationCard) this);
        } else {
            context.playedCards.add(this);
        }
    }

    private void torturer(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, targetPlayer);

                Player.TorturerOption option;
                try {
                    option = (targetPlayer).torturer_attack_chooseOption(context);
                } catch (NoSuchFieldError e) {
                    Util.playerError(targetPlayer, "'Take three cards' version of torturer attack no longer supported.");
                    option = null;
                }

                if (option == null) {
                    Util.playerError(targetPlayer, "Torturer option error, taking curse card.");
                    option = Player.TorturerOption.TakeCurse;
                }

                if (option == Player.TorturerOption.TakeCurse) {
                    targetPlayer.gainNewCard(Cards.curse, this, playerContext);
                } else {
                    Card[] cardsToDiscard = (targetPlayer).torturer_attack_cardsToDiscard(context);

                    boolean bad = false;
                    if (cardsToDiscard == null) {
                        bad = true;
                    } else if (targetPlayer.hand.size() < 2 && cardsToDiscard.length != targetPlayer.hand.size()) {
                        bad = true;
                    } else if (cardsToDiscard.length != 2) {
                        bad = true;
                    } else {
                        ArrayList<Card> copy = Util.copy(targetPlayer.hand);
                        for (Card cardToKeep : cardsToDiscard) {
                            if (!copy.remove(cardToKeep)) {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad) {
                        if (targetPlayer.hand.size() >= 2) {
                            Util.playerError(targetPlayer, "Torturer discard error, just discarding the first 2.");
                        }
                        cardsToDiscard = new Card[Math.min(2, targetPlayer.hand.size())];
                        for (int i = 0; i < cardsToDiscard.length; i++) {
                            cardsToDiscard[i] = targetPlayer.hand.get(i);
                        }
                    }

                    for (Card card : cardsToDiscard) {
                        targetPlayer.hand.remove(card);
                        targetPlayer.discard(card, this, playerContext);
                    }
                }
            }
        }
    }

    private void scout(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 4; i++) {
            Card card = game.draw(currentPlayer);
            if (card == null) {
                break;
            }
            if (card instanceof VictoryCard ) {
                currentPlayer.hand.add(card);
            } else {
                cards.add(card);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        for (Card card : cards) {
            currentPlayer.reveal(card, this, context);
        }

        Card[] order = currentPlayer.scout_orderCards(context, cards.toArray(new Card[cards.size()]));
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
            Util.playerError(currentPlayer, "Scout order cards error, ignoring.");
            order = cards.toArray(new Card[cards.size()]);
        }

        for (int i = order.length - 1; i >= 0; i--) {
            currentPlayer.putOnTopOfDeck(order[i]);
        }
    }

    private void ambassador(Game game, MoveContext context, Player currentPlayer) {
        if (currentPlayer.hand.size() == 0) {
            return;
        }

        Card card = currentPlayer.ambassador_revealedCard(context);

        if (card == null) {
            return;
        }

        if (!currentPlayer.hand.contains(card)) {
            Util.playerError(currentPlayer, "Ambassador revealed card error, picking random card.");
            card = Util.randomCard(currentPlayer.hand);
        }

        currentPlayer.reveal(card, this, context);
        context.debug("Ambassador revealed card:" + card.getName());
        int returnCount = currentPlayer.ambassador_returnToSupplyFromHand(context, card);

        if (returnCount < 0 || returnCount > 2) {
            Util.playerError(currentPlayer, "Ambassador return to supply error, ignoring.");
            returnCount = 0;
        }

        for (int i = 0; i < returnCount; i++) {
            if (currentPlayer.hand.contains(card)) {
                currentPlayer.hand.remove(card);
                CardPile pile = game.piles.get(card.getName());
                // Card thisCard = pile.removeCard();
                pile.addCard(card);
            } else {
                Util.playerError(currentPlayer, "Ambassador return to supply error, just returning those available.");
                break;
            }
        }

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                player.gainNewCard(card, this, new MoveContext(game, player));
            }
        }
    }

    private void ghostShip(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                if (player.hand.size() >= 4) {
                    Card[] cards = (player).ghostShip_attack_cardsToPutBackOnDeck(playerContext);
                    boolean bad = false;
                    if (cards == null || cards.length != player.hand.size() - 3) {
                        bad = true;
                    } else {
                        ArrayList<Card> copy = Util.copy(player.hand);
                        for (Card card : cards) {
                            if (!copy.remove(card)) {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad) {
                        Util.playerError(player, "Ghost Ship put back cards error, putting back the first " + (player.hand.size() - 3) + " cards.");

                        cards = new Card[player.hand.size() - 3];
                        for (int i = 0; i < player.hand.size() - 3; i++) {
                            cards[i] = player.hand.get(i);
                        }
                    }

                    for (int i = cards.length - 1; i >= 0; i--) {
                        player.hand.remove(cards[i]);
                        player.putOnTopOfDeck(cards[i]);
                    }
                }
            }
        }
    }

    private void salvager(MoveContext context, Player currentPlayer) {
        if (currentPlayer.hand.size() == 0) {
            return;
        }

        Card card = currentPlayer.salvager_cardToTrash(context);

        if (card == null || !currentPlayer.hand.contains(card)) {
            Util.playerError(currentPlayer, "Salvager trash error, trashing first card.");
            card = currentPlayer.hand.get(0);
        }

        context.addGold += card.getCost(context);
        currentPlayer.hand.remove(card);
        currentPlayer.trash(card, this, context);
    }

    private void bishop(Game game, MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.bishop_cardToTrashForVictoryTokens(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Bishop trash error, trashing a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this, context);
            currentPlayer.addVictoryTokens(context, card.getCost(context) / 2);
        }

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer) {
                Card card = (player).bishop_cardToTrash(context);

                if (card != null && player.hand.contains(card)) {
                    player.hand.remove(card);
                    player.trash(card, this, new MoveContext(game, player));
                }
            }
        }
    }

    private void city(Game game, MoveContext context, Player currentPlayer) {
        if (game.emptyPiles() > 0) {
            game.drawToHand(currentPlayer, this);
        }
        if (game.emptyPiles() > 1) {
            context.buys++;
            context.addGold++;
        }
    }

    private void countingHouse(MoveContext context, Player currentPlayer) {
        if (!currentPlayer.discard.isEmpty()) {
            for (Iterator<Card> it = currentPlayer.discard.iterator(); it.hasNext();) {
                Card card = it.next();

                if (Cards.copper.equals(card)) {
                    currentPlayer.reveal(card, this, context);
                    it.remove();
                    currentPlayer.hand.add(card);
                }
            }
        }
    }

    private void expand(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() == 0) {
            return;
        }

        Card card = currentPlayer.expand_cardToTrash(context);
        if (card == null || !currentPlayer.hand.contains(card)) {
            Util.playerError(currentPlayer, "Expand trash error, expanding a random card.");
            card = Util.randomCard(currentPlayer.hand);
        }

        int maxCost = card.getCost(context) + 3;
        boolean potion = card.costPotion();
        currentPlayer.hand.remove(card);
        currentPlayer.trash(card, this, context);

        card = currentPlayer.expand_cardToObtain(context, maxCost, potion);
        if (card != null) {
            if (card.getCost(context) > maxCost) {
                Util.playerError(currentPlayer, "Expand error, new card costs too much.");
            } else if(card.costPotion() && !potion) {
                Util.playerError(currentPlayer, "Expand error, new card costs potion and trashed card does not.");
            } else {
                if(!currentPlayer.gainNewCard(card, this, context)) {
                    Util.playerError(currentPlayer, "Expand error, pile is empty or card is not in the game.");
                }
            }
        }
    }

    private void forge(MoveContext context, Player currentPlayer) {
        int totalCost = 0;
        Card[] cards = currentPlayer.forge_cardsToTrash(context);

        if (cards != null) {
            for (Card card : cards) {
                if (card != null && currentPlayer.hand.contains(card)) {
                    totalCost += card.getCost(context);
                    currentPlayer.hand.remove(card);
                    currentPlayer.trash(card, this, context);
                }
            }
        }

        Card card = currentPlayer.forge_cardToObtain(context, totalCost);
        if (card != null) {
            if (card.getCost(context) != totalCost || card.costPotion() || card.isPrize() || card.equals(Cards.curse)) {
                Util.playerError(currentPlayer, "Forge returned invalid card, ignoring.");
            } else {
                if(!currentPlayer.gainNewCard(card, this, context)) {
                    Util.playerError(currentPlayer, "Forge error, pile is empty or card is not in the game.");
                }
            }
        }
    }

    private void throneRoomKingsCourt(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> actionCards = new ArrayList<Card>();
        for (Card card : currentPlayer.hand) {
            if (card instanceof ActionCard) {
                actionCards.add(card);
            }
        }

        if (!actionCards.isEmpty()) {
            ActionCardImpl cardToPlay = (ActionCardImpl) (this.equals(Cards.throneRoom)?
            		currentPlayer.throneRoom_cardToPlay(context):
            		currentPlayer.kingsCourt_cardToPlay(context));

            if (cardToPlay != null) {
                if(!actionCards.contains(cardToPlay)) {
                    Util.playerError(currentPlayer, this.name.toString() + " card selection error, ignoring");
                } else {
                    context.freeActionInEffect++;
   
                    cardToPlay.cloneCount = (equals(Cards.kingsCourt) ? 3 : 2);
                    for (int i = 0; i < cardToPlay.cloneCount;) {
                        context.numberTimesAlreadyPlayed = i++;
                        cardToPlay.play(game, context);
                    }

                    if (cardToPlay instanceof DurationCard) {
                        // Need to move throning card to NextTurnCards first
                        int idx = context.playedCards.lastIndexOf(this);
                        int ntidx = currentPlayer.nextTurnCards.size() - 1;
                        if (idx >= 0 && ntidx >= 0) {
                            context.playedCards.remove(idx);
                            currentPlayer.nextTurnCards.add(ntidx, this);
                        }
                    }

                    context.freeActionInEffect--;
                    context.numberTimesAlreadyPlayed = 0;
                    
                }
            }
        }
    }

    private void mint(MoveContext context, Player currentPlayer) {
        TreasureCard cardToMint = currentPlayer.mint_treasureToMint(context);

        if (cardToMint == null || !currentPlayer.hand.contains(cardToMint)) {
            Util.playerError(currentPlayer, "Mint treasure selection error, picking first treasure card to mint.");
            for (Card card : currentPlayer.hand) {
                if (card instanceof TreasureCard) {
                    cardToMint = (TreasureCard) card;
                    break;
                }
            }
        }

        if (cardToMint != null) {
            currentPlayer.reveal(cardToMint, this, context);
            currentPlayer.gainNewCard(cardToMint, this, context);
        }
    }

    private void mountebank(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                Card curseCard = null;
                for (Card card : player.hand) {
                    if (Cards.curse.equals(card)) {
                        curseCard = card;
                        break;
                    }
                }

                if (curseCard != null && (player).mountebank_attack_shouldDiscardCurse(playerContext)) {
                    player.hand.remove(curseCard);
                    player.discard(curseCard, this, playerContext);
                } else {
                    player.gainNewCard(Cards.curse, this, playerContext);
                    player.gainNewCard(Cards.copper, this, playerContext);
                }
            }
        }
    }

    private void rabble(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                ArrayList<Card> topOfTheDeck = new ArrayList<Card>();

                for (int i = 0; i < 3; i++) {
                    Card card = game.draw(player);
                    if (card != null) {
                    player.reveal(card, this, playerContext);

                    if (card instanceof TreasureCard || card instanceof ActionCard) {
                        player.discard(card, this, playerContext);
                    } else {
                        topOfTheDeck.add(card);
                    }
                }
                }

                if (!topOfTheDeck.isEmpty()) {
                    Card[] order = (player).rabble_attack_cardOrder(playerContext, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));

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
                        Util.playerError(currentPlayer, "Rabble order cards error, ignoring.");
                        order = topOfTheDeck.toArray(new Card[topOfTheDeck.size()]);
                    }

                    // Put the cards back on the deck
                    for (int i = order.length - 1; i >= 0; i--) {
                        player.putOnTopOfDeck(order[i]);
                    }
                }
            }
        }
    }

    private void tradeRoute(Game game, MoveContext context, Player currentPlayer) {
        if (!currentPlayer.hand.isEmpty()) {
            Card card = currentPlayer.tradeRoute_cardToTrash(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Trade Route card selection error, no card selected or card not in hand, choosing random card to trash");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this, context);
        }

        int victoryCardPileSize = 12;

        if (Game.numPlayers == 2) {
            victoryCardPileSize = 8;
        }

        for (CardPile pile : game.piles.values()) {
            if (pile.card instanceof VictoryCard) {
                if (pile.getCount() < victoryCardPileSize) {
                    context.addGold++;
                }
            }
        }
    }

    private void vault(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.vault_cardsToDiscardForGold(context);
        if (cards != null) {
            int numberOfCardsDiscarded = 0;
            for (Card card : cards) {
                if (currentPlayer.hand.remove(card)) {
                    currentPlayer.discard(card, this, context);
                    numberOfCardsDiscarded++;
                }
            }

            if (numberOfCardsDiscarded != cards.length) {
                Util.playerError(currentPlayer, "Vault discard error, trying to discard cards not in hand, ignoring extra.");
            }

            context.addGold += numberOfCardsDiscarded;
        }

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer) {
                cards = (player).vault_cardsToDiscardForCard(context);

                if (cards != null) {
                    int numberOfCardsDiscarded = 0;

                    if (cards.length > 1 || player.hand.size() == 1) {
                        for (Card card : cards) {
                            if (numberOfCardsDiscarded < 2 && player.hand.remove(card)) {
                                player.discard(card, this, null);
                                numberOfCardsDiscarded++;
                            }
                        }
                    }

                    if (numberOfCardsDiscarded != cards.length) {
                        if (cards.length > 2) {
                            Util.playerError(player, "Vault discard error, trying to discard more than 2 cards, discarding first 2");
                        } else if (cards.length < 2) {
                            Util.playerError(player, "Vault discard error, trying to discard only 1 card, discarding none");
                        } else {
                            Util.playerError(player, "Vault discard error, trying to discard cards not in hand, ignoring extra.");
                        }
                    }

                    if (numberOfCardsDiscarded == 2) {
                        game.drawToHand(player, this);
                    }
                }
            }
        }
    }

    private void watchTower(Game game, Player currentPlayer) {
        while (currentPlayer.hand.size() < 6) {
            Card draw = game.draw(currentPlayer);
            if (draw == null) {
                break;
            }

            currentPlayer.hand.add(draw);
        }
    }

    private void farmingVillage(Game game, MoveContext context, Player currentPlayer) {
        Card draw = null;
        
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        
        while ((draw = game.draw(currentPlayer)) != null && !(draw instanceof ActionCard) && !(draw instanceof TreasureCard)) {
            toDiscard.add(draw); 
        }
        
        while (!toDiscard.isEmpty()) {
            Card c = toDiscard.remove(0);
            currentPlayer.reveal(c, this, context);
            currentPlayer.discard(c, this, context);
        }

        if (draw != null) {
            currentPlayer.reveal(draw, this, context);
            currentPlayer.hand.add(draw);
        }
    }

    private void fortuneTeller(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                ArrayList<Card> cardsToDiscard = new ArrayList<Card>();
                
                Card draw = null;
                while ((draw = game.draw(player)) != null && !(draw instanceof VictoryCard) && !(draw instanceof CurseCard)) {
                    player.reveal(draw, this, playerContext);
                    cardsToDiscard.add(draw);
                }

                if (draw != null) {
                    player.reveal(draw, this, playerContext);
                    player.putOnTopOfDeck(draw);
                }

                for(Card card : cardsToDiscard) {
                    player.discard(card, this, playerContext);
                }
            }
        }
    }

    private void hamlet(MoveContext context, Player currentPlayer) {
        Card forAction = currentPlayer.hamlet_cardToDiscardForAction(context);
        if (forAction != null) {
            currentPlayer.hand.remove(forAction);
            currentPlayer.discard(forAction, this, context);
            context.actions++;
        }

        Card forBuy = currentPlayer.hamlet_cardToDiscardForBuy(context);
        if (forBuy != null) {
            currentPlayer.hand.remove(forBuy);
            currentPlayer.discard(forBuy, this, context);
            context.buys++;
        }
    }

    private void harvest(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();
        for (int i = 0; i < 4; i++) {
            Card draw = game.draw(currentPlayer);
            if (draw == null) {
                break;
            }

            cardNames.add(draw.getName());
            currentPlayer.reveal(draw, this, context);
            currentPlayer.discard(draw, this, context);
        }

        context.addGold += cardNames.size();
    }

    private void horseTraders(MoveContext context, Player currentPlayer) {
        Card[] cardsToDiscard = currentPlayer.horseTraders_cardsToDiscard(context);
        boolean bad = false;

        if (cardsToDiscard == null) {
            bad = true;
        } else if (currentPlayer.hand.size() < 2 && cardsToDiscard.length != currentPlayer.hand.size()) {
            bad = true;
        } else if (cardsToDiscard.length != 2) {
            bad = true;
        } else {
            ArrayList<Card> copy = Util.copy(currentPlayer.hand);
            for (Card cardToKeep : cardsToDiscard) {
                if (!copy.remove(cardToKeep)) {
                    bad = true;
                    break;
                }
            }
        }

        if (bad) {
            if (currentPlayer.hand.size() >= 2) {
                Util.playerError(currentPlayer, "Horse Traders discard error, just discarding the first 2.");
            }
            cardsToDiscard = new Card[Math.min(2, currentPlayer.hand.size())];
            for (int i = 0; i < cardsToDiscard.length; i++) {
                cardsToDiscard[i] = currentPlayer.hand.get(i);
            }
        }

        for (Card card : cardsToDiscard) {
            currentPlayer.hand.remove(card);
            currentPlayer.discard(card, this, null);
        }
    }

    private void jester(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this, context);

                Card draw = game.draw(targetPlayer);
                if (draw == null) {
                    continue;
                }
                
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetPlayer.reveal(draw, this, targetContext);
                targetPlayer.discard(draw, this, targetContext);

                MoveContext toGainContext = null;

                if (draw instanceof VictoryCard) {
                    targetPlayer.gainNewCard(Cards.curse, this, targetContext);
                } else {
                    if (!game.isPileEmpty(draw)) {
                        JesterOption option = currentPlayer.jester_chooseOption(context, targetPlayer, draw);
                        toGainContext = JesterOption.GainCopy.equals(option) ? context : targetContext;
                        toGainContext.getPlayer().gainNewCard(draw, this, toGainContext);
                    }
                }
            }
        }
    }

    private void huntingParty(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            cardNames.add(card.getName());
            currentPlayer.reveal(card, this, context);
        }

        ArrayList<Card> toDiscard = new ArrayList<Card>();
        
        Card draw = null;
        while ((draw = game.draw(currentPlayer)) != null && cardNames.contains(draw.getName())) {
            currentPlayer.reveal(draw, this, context);
            toDiscard.add(draw);
        }

        if (draw != null) {
            currentPlayer. reveal(draw, this, context);
            currentPlayer.hand.add(draw);
        }
        
        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this, null);
        }
    }

    private void crossroads(Game game, MoveContext context, Player currentPlayer) {
        int victoryCards = 0;
        for(Card c : currentPlayer.getHand()) {
            if(c instanceof VictoryCard) {
                victoryCards++;
            }
        }
        
        for(int i=0; i < victoryCards; i++) {
            game.drawToHand(currentPlayer, this);
        }
        
        int crossroadsPlayed = 0;
        for (Card c : context.getPlayedCards()) {
            if (c.equals(Cards.crossroads)) {
                crossroadsPlayed++;
            }
        }

        if (crossroadsPlayed <= 1) {
            context.actions += 3;
        }
    }

    private void trustySteed(Game game, MoveContext context, Player currentPlayer) {
        TrustySteedOption[] options = currentPlayer.trustySteed_chooseOptions(context);
        if (options == null || options.length != 2 || options[0] == options[1]) {
            Util.playerError(currentPlayer, "Trusty Steed options error, ignoring.");
        } else {
        	// Trusty Steed must do options in the order listed.
        	Arrays.sort(options);
            for (TrustySteedOption option : options) {
                if (option == TrustySteedOption.AddActions) {
                    context.actions += 2;
                } else if (option == TrustySteedOption.AddCards) {
                    for (int i = 0; i < 2; i++) {
                        game.drawToHand(currentPlayer, this);
                    }
                } else if (option == TrustySteedOption.AddGold) {
                    context.addGold += 2;
                } else if (option == TrustySteedOption.GainSilvers) {
                    for (int i = 0; i < 4; i++) {
                        if(!currentPlayer.gainNewCard(Cards.silver, this, context)) {
                            break;
                        }
                    }

                    while (currentPlayer.getDeckSize() > 0) {
                    	currentPlayer.discard(currentPlayer.deck.remove(0), this, null);
                    }
                }
            }
        }
    }

    private void duchess(Game game) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            Card c = game.draw(targetPlayer);
            if(c != null) {
                MoveContext targetPlayerContext = new MoveContext(game, targetPlayer);
                boolean discard = (targetPlayer).duchess_shouldDiscardCardFromTopOfDeck(targetPlayerContext, c);
                if(discard) {
                    targetPlayer.discard(c, this, targetPlayerContext);
                } else {
                    targetPlayer.putOnTopOfDeck(c);
                }
            }
        }
    }

    private void spiceMerchant(Game game, MoveContext context, Player currentPlayer) {
        boolean handContainsTreasure = false;
        for(Card c : currentPlayer.hand) {
            if(c instanceof TreasureCard) {
                handContainsTreasure = true;
                break;
            }
        }
        
        if(handContainsTreasure) {
            TreasureCard treasure = currentPlayer.spiceMerchant_treasureToTrash(context);
            if(treasure != null) {
                if(!currentPlayer.hand.contains(treasure)) {
                    Util.playerError(currentPlayer, "Spice Merchant returned invalid card to trash from hand, ignoring.");
                }
                else {
                    currentPlayer.hand.remove(treasure);
                    currentPlayer.trash(treasure, this, context);
                    
                    SpiceMerchantOption option = currentPlayer.spiceMerchant_chooseOption(context);
                    if(option == SpiceMerchantOption.AddCardsAndAction) {
                        game.drawToHand(currentPlayer, this);
                        game.drawToHand(currentPlayer, this);
                        context.actions += 1;
                    }
                    else {
                        context.addGold += 2;
                        context.buys += 1;
                    }
                }
            }
        }
    }

    private void trader(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card card = currentPlayer.trader_cardToTrash(context);
            if(card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Trader card to trash invalid, picking one");
                card = currentPlayer.hand.get(0);
            }
            
            int cost = card.getCost(context);
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this, context);
            for(int i=0; i < cost; i++) {
                if(!currentPlayer.gainNewCard(Cards.silver, this, context)) {
                    break;
                }
            }
        }
    }

    private void cartographer(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 4; i++) {
            Card card = game.draw(currentPlayer);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            Card[] cardsToDiscard = currentPlayer.cartographer_cardsFromTopOfDeckToDiscard(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
            if(cardsToDiscard != null) {
                for(Card toDiscard : cardsToDiscard) {
                    if(topOfTheDeck.remove(toDiscard)) {
                        currentPlayer.discard(toDiscard, this, null);
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
                    order = currentPlayer.cartographer_cardOrder(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
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

    private void embassy(MoveContext context, Player currentPlayer) {
        if (currentPlayer.hand.size() == 0) {
            return;
        }

        Card[] cards;
        if (currentPlayer.hand.size() > 3) {
            cards = currentPlayer.embassy_cardsToDiscard(context);
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
            currentPlayer.reveal(cards[i], this, context);
            currentPlayer.discard(cards[i], this, null);
        }
    }

    private void inn(MoveContext context, Player currentPlayer) {
        Card[] cards;
        if (currentPlayer.hand.size() > 2) {
            cards = currentPlayer.inn_cardsToDiscard(context);
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
            currentPlayer.reveal(cards[i], this, context);
            currentPlayer.discard(cards[i], this, null);
        }
    }

    private void mandarin(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card toTopOfDeck = currentPlayer.mandarin_cardToReplace(context);

            if (toTopOfDeck == null) {
                Util.playerError(currentPlayer, "No card selected for Mandarin, returning random card to the top of the deck.");
                toTopOfDeck = Util.randomCard(currentPlayer.hand);
            }
            
            currentPlayer.reveal(toTopOfDeck, this, context);
            currentPlayer.hand.remove(toTopOfDeck);
            currentPlayer.putOnTopOfDeck(toTopOfDeck);
        }
    }

    private void possession(MoveContext context) {
        // TODO: Temp hack to prevent AI from playing possession, even though human player can, since it only half works 
        //       (AI will make decisions while possessed, but will try to make "good" ones)
        if(!context.getPlayer().isAi())
            context.possessionsToProcess++;
    }

    private void steward(Game game, MoveContext context, Player currentPlayer) {
        Player.StewardOption option = currentPlayer.steward_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Steward option error, ignoring.");
        } else {
            if (option == Player.StewardOption.AddGold) {
                context.addGold += 2;
            } else if (option == Player.StewardOption.AddCards) {
                game.drawToHand(currentPlayer, this);
                game.drawToHand(currentPlayer, this);
            } else if (option == Player.StewardOption.TrashCards) {
                CardList hand = currentPlayer.getHand();
                if (hand.size() == 0) {
                    return;
                }

                Card[] cards = currentPlayer.steward_cardsToTrash(context);
                boolean bad = false;
                if (cards == null) {
                    bad = true;
                } else if (cards.length != 2) {
                    if (hand.size() >= 2 || cards.length != hand.size()) {
                        bad = true;
                    }
                } else {
                    ArrayList<Card> copy = Util.copy(currentPlayer.hand);
                    for (Card card : cards) {
                        if (!copy.remove(card)) {
                            bad = true;
                            break;
                        }
                    }
                }

                if (bad) {
                    Util.playerError(currentPlayer, "Steward trash error, picking first two cards.");

                    if (hand.size() >= 2) {
                        cards = new Card[2];
                    } else {
                        cards = new Card[hand.size()];
                    }
                    for (int i = 0; i < cards.length; i++) {
                        cards[i] = hand.get(i);
                    }
                }

                for (Card card : cards) {
                    currentPlayer.hand.remove(card);
                    currentPlayer.trash(card, this, context);
                }
            }
        }
    }

    private void thief(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<TreasureCard> trashed = new ArrayList<TreasureCard>();

        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this, context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                ArrayList<TreasureCard> treasures = new ArrayList<TreasureCard>();

                for (int i = 0; i < 2; i++) {
                    Card card = game.draw(targetPlayer);

                    if (card != null) {
                        targetPlayer.reveal(card, this, targetContext);

                        if (card instanceof TreasureCard) {
                            treasures.add((TreasureCard) card);
                        } else {
                            targetPlayer.discard(card, this, targetContext);
                        }
                    }
                }

                TreasureCard cardToTrash = null;

                if (treasures.size() == 1) {
                    cardToTrash = treasures.get(0);
                } else if (treasures.size() == 2) {
                    if (treasures.get(0).equals(treasures.get(1))) {
                        cardToTrash = treasures.get(0);
                    } else {
                        cardToTrash = currentPlayer.thief_treasureToTrash(context, treasures.toArray(new TreasureCard[]{}));
                    }

                    for (TreasureCard treasure : treasures) {
                        if (!treasure.equals(cardToTrash)) {
                            targetPlayer.discard(treasure, this, targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this, targetContext);
                    trashed.add(cardToTrash);
                }
            }
        }
        
        if (trashed.size() > 0) {
            TreasureCard[] treasuresToGain = currentPlayer.thief_treasuresToGain(context, trashed.toArray(new TreasureCard[]{}));
            
            if (treasuresToGain != null) {
                for (TreasureCard treasure : treasuresToGain) {
                    currentPlayer.gainCardAlreadyInPlay(treasure, this, context);
                }
            }
        }
    }

    private void militia(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                if (player.hand.size() > 3) {
                    Card[] cardsToKeep = (player).militia_attack_cardsToKeep(playerContext);

                    boolean bad = false;
                    if (cardsToKeep == null || cardsToKeep.length != 3) {
                        bad = true;
                    } else {
                        ArrayList<Card> handCopy = Util.copy(player.hand);
                        for (Card cardToKeep : cardsToKeep) {
                            if (!handCopy.remove(cardToKeep)) {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad) {
                        Util.playerError(player, "Militia discard error, just keeping first 3.");
                        cardsToKeep = new Card[3];
                        cardsToKeep[0] = player.hand.get(0);
                        cardsToKeep[1] = player.hand.get(1);
                        cardsToKeep[2] = player.hand.get(2);
                    }

                    // Remove cards to keep from the hand temporarily
                    for (Card card : cardsToKeep) {
                        player.hand.remove(card);
                    }

                    // Discard all of the cards left
                    for (Card card : player.hand) {
                        player.discard(card, this, playerContext);
                    }

                    // Clear out the hand
                    player.hand.clear();

                    // Put the cardsToKeep back
                    for (Card card : cardsToKeep) {
                        player.hand.add(card);
                    }
                }
            }
        }
    }

    private void remodel(MoveContext context, Player currentPlayer) {
        if(currentPlayer.getHand().size() > 0) {
            Card cardToTrash = currentPlayer.remodel_cardToTrash(context);
   
            if (cardToTrash == null) {
                Util.playerError(currentPlayer, "Remodel did not return a card to trash, trashing random card.");
                cardToTrash = Util.randomCard(currentPlayer.getHand());
            }
   
            int cost = -1;
            boolean potion = false;
            for (int i = 0; i < currentPlayer.hand.size(); i++) {
                Card playersCard = currentPlayer.hand.get(i);
                if (playersCard.equals(cardToTrash)) {
                    cost = playersCard.getCost(context);
                    potion = playersCard.costPotion();
                    playersCard = currentPlayer.hand.remove(i);
   
                    currentPlayer.trash(playersCard, this, context);
                    break;
                }
            }
   
            if (cost == -1) {
                Util.playerError(currentPlayer, "Remodel returned invalid card, ignoring.");
                return;
            }
   
            cost += 2;
   
            Card card = currentPlayer.remodel_cardToObtain(context, cost, potion);
            if (card != null) {
                // check cost
                if (card.getCost(context) > cost) {
                    Util.playerError(currentPlayer, "Remodel new card costs too much, ignoring.");
                }
                else if (card.costPotion() && !potion) {
                    Util.playerError(currentPlayer, "Remodel new card costs potion, ignoring.");
                }
                else
                {
                    if(!currentPlayer.gainNewCard(card, this, context)) {
                        Util.playerError(currentPlayer, "Remodel new card is invalid, ignoring.");
                    }
                }
            }
        }
    }

    private void remake(MoveContext context, Player currentPlayer) {
        for (int i = 0; i < 2; i++) {
            if (currentPlayer.getHand().size() == 0) {
                return;
            }

            Card card = currentPlayer.remake_cardToTrash(context);
            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Remake trash error, remaking a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            int value = card.getCost(context) + 1;
            boolean potion = card.costPotion();
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this, context);

            card = currentPlayer.remake_cardToObtain(context, value, potion);
            if (card != null) {
                if (card.getCost(context) != value || card.costPotion() != potion) {
                    Util.playerError(currentPlayer, "Remake error, new card must cost exactly " + value + ".");
                } else {
                    if(!currentPlayer.gainNewCard(card, this, context)) {
                        Util.playerError(currentPlayer, "Remake error, pile is empty or card is not in the game.");
                    }
                }
            }
        }
    }

    private void menagerie(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();
        boolean distinct = true;

        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            currentPlayer.reveal(card, this, context);
            distinct &= cardNames.add(card.getName());
        }

        int numCards = distinct ? 3 : 1;
        for (int i = 0; i < numCards; i++) {
            Card draw = game.draw(currentPlayer);
            if (draw != null) {
                currentPlayer.hand.add(draw);
            }
        }
    }

    private void tournament(Game game, MoveContext context, Player currentPlayer) {
        boolean opponentsRevealedProvince = false;

        for (Player player : game.getPlayersInTurnOrder()) {
            Card province = null;
            for (Card card : player.hand) {
                if (card.equals(Cards.province)) {
                    province = card;
                    break;
                }
            }

            if (province != null) {
                if (player == currentPlayer) {
                    TournamentOption option = currentPlayer.tournament_chooseOption(context);

                    if (option != TournamentOption.DontRevealProvince) {
                        currentPlayer.reveal(province, this, context);
                        player.hand.remove(province);
                        currentPlayer.discard(province, this, context);

                        if (option == TournamentOption.GainPrize) {
                            Card prize = currentPlayer.tournament_choosePrize(context);

                            if (prize != null && prize.isPrize()) {
                                player.gainNewCard(prize, this, context);
                            } else {
                                Util.playerError(player, "Tournament error, invalid prize");
                            }
                        } else if (option == TournamentOption.GainDuchy) {
                            player.gainNewCard(Cards.duchy, this, context);
                        }
                    }
                } else {
                    player.reveal(province, this, new MoveContext(game, player));
                    opponentsRevealedProvince = true;
                }
            }
        }

        if (!opponentsRevealedProvince) {
            Card draw = game.draw(currentPlayer);
            if (draw != null) {
                currentPlayer.hand.add(draw);
            }

            context.addGold++;
        }
    }

    private void tradingPost(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() < 2) {
            return;
        }
        Card[] cardsToTrash = currentPlayer.tradingPost_cardsToTrash(context);
        // TODO is trash forced? Should we not allow null here?
        if (cardsToTrash != null) {
            boolean bad = false;
            if (cardsToTrash.length != 2) {
                bad = true;
            } else {
                // TODO Check for null in individual elements of other arrays returned by players as
                // well...
                if (cardsToTrash[0] == null || cardsToTrash[1] == null) {
                    bad = true;
                } else {
                    ArrayList<Card> handCopy = Util.copy(currentPlayer.hand);

                    if (!handCopy.remove(cardsToTrash[0]) || !handCopy.remove(cardsToTrash[1])) {
                        bad = true;
                    } else {
                        for (int i = 0; i < 2; i++) {
                            currentPlayer.hand.remove(cardsToTrash[i]);
                            currentPlayer.trash(cardsToTrash[i], this, context);
                        }
                        currentPlayer.gainNewCard(Cards.silver, this, context);
                    }
                }
            }

            // TODO is trash forced? should we just discard the first two cards in hand?
            if (bad) {
                Util.playerError(currentPlayer, "Trading Post error, ignoring.");
            }
        }
    }

    private void masquerade(Game game, MoveContext context, Player currentPlayer) {
        Card[] passedCards = new Card[Game.players.length];

        for (int i = 0; i < Game.players.length; i++) {
            Player player = Game.players[i];
            if (player.getHand().size() == 0) {
                continue;
            }
            Card card = player.masquerade_cardToPass(new MoveContext(game, player));
            if (card == null || !(player).hand.contains(card)) {
                Util.playerError(player, "Masquerade pass card error, picking random card to pass.");
                card = Util.randomCard(player.getHand());
            }

            // TODO Should this send some new type of event, not trashed, but passed maybe?
            if (card != null) {
                (player).hand.remove(card);
                passedCards[i] = card;
            }
        }

        for (int i = 0; i < Game.players.length; i++) {
            int next = i + 1;
            if (next >= Game.players.length) {
                next = 0;
            }

            Player nextPlayer = Game.players[next];

            Card card = passedCards[i];
            if (card != null) {
                nextPlayer.gainCardAlreadyInPlay(card, this, new MoveContext(game, nextPlayer));
            }
        }

        Card toTrash = currentPlayer.masquerade_cardToTrash(context);
        if (toTrash != null) {
            if (currentPlayer.hand.contains(toTrash)) {
                currentPlayer.hand.remove(toTrash);

                currentPlayer.trash(toTrash, this, context);
            } else {
                Util.playerError(currentPlayer, "Masquerade trash error, card not in hand, ignoring.");
            }
        }
    }

    private void miningVillage(MoveContext context, Player currentPlayer) {
        if (!this.trashed) {
            if (currentPlayer.miningVillage_shouldTrashMiningVillage(context)) {
                context.addGold += 2;
                context.playedCards.remove(this);
                currentPlayer.trash(this, null, context);
            }
        }
    }

    private void minion(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Player> playersToAttack = new ArrayList<Player>();
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player == currentPlayer || !Util.isDefendedFromAttack(game, player, this)) {
                playersToAttack.add(player);
                if (player != currentPlayer) {
                    player.attacked(this, context);
                }
            }
        }
        
        Player.MinionOption option = currentPlayer.minion_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Minion option error, choosing to add gold.");
            option = Player.MinionOption.AddGold;
        }

        if (option == Player.MinionOption.AddGold) {
            context.addGold += 2;
        } else if (option == Player.MinionOption.RolloverCards) {
            for (Player player : playersToAttack) {
                if (player == currentPlayer || player.hand.size() >= 5) {
                    MoveContext targetContext = new MoveContext(game, player);
                    while (!player.hand.isEmpty()) {
                        player.discard(player.hand.remove(0), this, targetContext);
                    }

                    game.drawToHand(player, this);
                    game.drawToHand(player, this);
                    game.drawToHand(player, this);
                    game.drawToHand(player, this);
                }
            }
        }
    }

    private void pawn(Game game, MoveContext context, Player currentPlayer) {
        Player.PawnOption[] options = currentPlayer.pawn_chooseOptions(context);
        if (options == null || options.length != 2 || options[0] == options[1]) {
            Util.playerError(currentPlayer, "Pawn options error, ignoring.");
        } else {
            for (Player.PawnOption option : options) {
                if (option == Player.PawnOption.AddAction) {
                    context.actions++;
                } else if (option == Player.PawnOption.AddBuy) {
                    context.buys++;
                } else if (option == Player.PawnOption.AddCard) {
                    game.drawToHand(currentPlayer, this);
                } else if (option == Player.PawnOption.AddGold) {
                    context.addGold++;
                }
            }
        }
    }

    private void saboteur(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                ArrayList<Card> toDiscard = new ArrayList<Card>();
                Card draw;

                while ((draw = game.draw(player)) != null) {
                    if (draw.getCost(context) >= 3) {
                        int value = draw.getCost(context);
                        value -= 2;
                        if (value < 0) {
                            value = 0;
                        }
                        
                        boolean potion = draw.costPotion();

                        player.trash(draw, this, playerContext);

                        Card card = (player).saboteur_cardToObtain(playerContext, value, potion);
                        if (card != null) {
                            if(card.getCost(context) > value || (card.costPotion() && !potion) || card.isPrize() || card.equals(Cards.curse)) {
                                Util.playerError(currentPlayer, "Saboteur obtain error, ignoring.");
                            }
                            else {
                                if(!player.gainNewCard(card, this, playerContext)) {
                                    Util.playerError(currentPlayer, "Saboteur obtain error, ignoring.");
                                }
                            }
                        }

                        break;
                    } else {
                        player.reveal(draw, this, playerContext);
                        toDiscard.add(draw);
                    }
                }

                while (!toDiscard.isEmpty()) {
                    player.discard(toDiscard.remove(0), this, null);
                }
            }
        }
    }

    private void shantyTown(Game game, MoveContext context, Player currentPlayer) {
        boolean actions = false;
        for (Card card : currentPlayer.hand) {
            currentPlayer.reveal(card, this, context);

            if (card instanceof ActionCard) {
                actions = true;
            }
        }

        if (!actions) {
            game.drawToHand(currentPlayer, this);
            game.drawToHand(currentPlayer, this);
        }
    }

    private void baron(MoveContext context, Player currentPlayer) {
        boolean discard = false;
        for (Card cardToCheck : currentPlayer.hand) {
            if (cardToCheck.equals(Cards.estate)) {
                discard = currentPlayer.baron_shouldDiscardEstate(context);
                break;
            }
        }

        if (discard) {
            Card card = currentPlayer.hand.get(Cards.estate);
            currentPlayer.hand.remove(Cards.estate);
            currentPlayer.discard(card, this, null);
            context.addGold += 4;
        } else {
            currentPlayer.gainNewCard(Cards.estate, this, context);
        }
    }

    private void courtyard(MoveContext context, Player currentPlayer) {
        // TODO do this check at the top of the block for EVERY Util...
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.courtyard_cardToPutBackOnDeck(context);
   
            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Courtyard error, just putting back a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }
   
            currentPlayer.hand.remove(card);
            currentPlayer.putOnTopOfDeck(card);
        }
    }

    private void spyAndScryingPool(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            // Note that this is the opposite check of other attacks, the spy/scrying pool lets
            // the current player look at their own deck which is a good thing, so always
            // allow that
            if (player == currentPlayer || (!Util.isDefendedFromAttack(game, player, this))) {
                if (player != currentPlayer) {
                    player.attacked(this, context);
                }

                MoveContext playerContext = new MoveContext(game, player);
                Card card = game.draw(player);

                if (card != null) {
                    player.reveal(card, this, playerContext);

                    boolean discard = false;
                    
                    if(equals(Cards.spy)) {
                        discard = currentPlayer.spy_shouldDiscard(context, player, card); 
                    } else if (equals(Cards.scryingPool)) {
                        discard = currentPlayer.scryingPool_shouldDiscard(context, player, card); 
                    }
                    
                    if (discard) {
                        player.discard(card, this, playerContext);
                    } else {
                        // put it back
                        player.putOnTopOfDeck(card);
                    }
                }
            }
        }
        
        if(equals(Cards.scryingPool)) {
            ArrayList<Card> cardsToPutInHand = new ArrayList<Card>();
            
            Card draw = null;
            while ((draw = game.draw(currentPlayer)) != null) {
                currentPlayer.reveal(draw, this, new MoveContext(game, currentPlayer));
                cardsToPutInHand.add(draw);
                if(!(draw instanceof ActionCard)) {
                    break;
                }
            }
            
            for(Card card : cardsToPutInHand) {
                currentPlayer.hand.add(card);
            }
        }
    }

    private void consiprator(Game game, MoveContext context, Player currentPlayer) {
        if (context.actionsPlayedSoFar >= 3) {
            context.actions++;
            game.drawToHand(currentPlayer, this);
        }
    }

    private void wishingWell(Game game, MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.wishingWell_cardGuess(context);
        Card draw = game.draw(currentPlayer);
        if (card != null && draw != null) {
            currentPlayer.reveal(draw, this, context);

            if (card.equals(draw)) {
                currentPlayer.hand.add(draw);
            } else {
                currentPlayer.putOnTopOfDeck(draw);
            }
        }
    }

    private void upgrade(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.upgrade_cardToTrash(context);
            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Upgrade trash error, upgrading a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }
   
            int value = card.getCost(context) + 1;
            boolean potion = card.costPotion();
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this, context);
   
            card = currentPlayer.upgrade_cardToObtain(context, value, potion);
            if (card != null) {
                if (card.getCost(context) != value || card.costPotion() != potion) {
                    Util.playerError(currentPlayer, "Upgrade error, new card does not cost value of the old card +1.");
                } else {
                    if(!currentPlayer.gainNewCard(card, this, context)) {
                        Util.playerError(currentPlayer, "Upgrade error, pile is empty or card is not in the game.");
                    }
                }
            }
        }
    }

    private void ironworks(Game game, MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.ironworks_cardToObtain(context);
        if (card != null && card.getCost(context) <= 4 && !card.costPotion()) {
            if (currentPlayer.gainNewCard(card, this, context)) {
                if (card instanceof ActionCard) {
                    context.actions++;
                }
                if (card instanceof TreasureCard) {
                    context.addGold++;
                }
                if (card instanceof VictoryCard) {
                    game.drawToHand(currentPlayer, this);
                }
            }
        } 
    }

    private void nobles(Game game, MoveContext context, Player currentPlayer) {
        Player.NoblesOption option = currentPlayer.nobles_chooseOptions(context);
        if (option == null) {
            Util.playerError(currentPlayer, "Nobles option error, ignoring.");
        } else {
            if (option == Player.NoblesOption.AddActions) {
                context.actions += 2;
            } else if (option == Player.NoblesOption.AddCards) {
                game.drawToHand(currentPlayer, this);
                game.drawToHand(currentPlayer, this);
                game.drawToHand(currentPlayer, this);
            }
        }
    }

    private void tribute(Game game, MoveContext context, Player currentPlayer) {
        Card[] revealedCards = new Card[2];
        Player nextPlayer = game.getNextPlayer();
        revealedCards[0] = game.draw(nextPlayer);
        revealedCards[1] = game.draw(nextPlayer);

        if (revealedCards[0] != null) {
            nextPlayer.reveal(revealedCards[0], this, new MoveContext(game, nextPlayer));
            (nextPlayer).discard(revealedCards[0], this, null);

        }
        if (revealedCards[1] != null) {
            nextPlayer.reveal(revealedCards[1], this, new MoveContext(game, nextPlayer));
            (nextPlayer).discard(revealedCards[1], this, null);
        }

        // "For each differently named card revealed..."
        if (revealedCards[0] != null && revealedCards[0].equals(revealedCards[1])) {
            revealedCards[1] = null;
        }

        for (Card card : revealedCards) {
            if (card != null && !card.equals(Cards.curse)) {
                if (card instanceof ActionCard) {
                    context.actions += 2;
                }
                if (card instanceof TreasureCard) {
                    context.addGold += 2;
                }
                if (card instanceof VictoryCard) {
                    game.drawToHand(currentPlayer, this);
                    game.drawToHand(currentPlayer, this);
                }
            }
        }
    }

    private void copperSmith(MoveContext context) {
        context.coppersmithsPlayed++;
    }

    private void swindler(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                Card draw = game.draw(player);
                if (draw != null) {
                    player.trash(draw, this, playerContext);

                    Card card = currentPlayer.swindler_cardToSwitch(context, draw.getCost(context), draw.costPotion());

                    boolean bad = false;
                    if (card == null) {
                        // Check that there are no cards that are possible to trade for...
                        for (Card thisCard : context.getCardsInPlay()) {
                            if (!thisCard.isPrize() && game.pileSize(thisCard) > 0 && thisCard.getCost(context) == draw.getCost(context) && thisCard.costPotion() == draw.costPotion()) {
                                bad = true;
                                break;
                            }
                        }
                    } else if (context.getCardsLeft(card) == 0) {
                        bad = true;
                    } else if (card.isPrize() || card.getCost(context) != draw.getCost(context) || card.costPotion() != draw.costPotion()) {
                        bad = true;
                    }

                    if (bad) {
                        Util.playerError(currentPlayer, "Swindler swap card error, picking a random card.");

                        ArrayList<Card> possible = new ArrayList<Card>();
                        for (Card thisCard : context.getCardsInPlay()) {
                            if (!thisCard.isPrize() && game.pileSize(thisCard) > 0 && thisCard.getCost(context) == draw.getCost(context) && thisCard.costPotion() == draw.costPotion()) {
                                possible.add(thisCard);
                            }
                        }

                        card = Util.randomCard(possible);
                    }

                    if (card != null) {
                        player.gainNewCard(card, this, playerContext);
                    }
                }
            }
        }
    }

    private void goons(Game game, MoveContext context, Player currentPlayer) {
        if (context.numberTimesAlreadyPlayed == 0) {
            context.goonsPlayed++;
        }

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                if (player.hand.size() > 3) {
                    Card[] cardsToKeep = (player).goons_attack_cardsToKeep(playerContext);

                    boolean bad = false;
                    if (cardsToKeep == null || cardsToKeep.length != 3) {
                        bad = true;
                    } else {
                        ArrayList<Card> handCopy = Util.copy(player.hand);
                        for (Card cardToKeep : cardsToKeep) {
                            if (!handCopy.remove(cardToKeep)) {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad) {
                        Util.playerError(player, "Goons discard error, just keeping first 3.");
                        cardsToKeep = new Card[3];
                        cardsToKeep[0] = player.hand.get(0);
                        cardsToKeep[1] = player.hand.get(1);
                        cardsToKeep[2] = player.hand.get(2);
                    }

                    // Remove cards to keep from the hand temporarily
                    for (Card card : cardsToKeep) {
                        player.hand.remove(card);
                    }

                    // Discard all of the cards left
                    for (Card card : player.hand) {
                        player.discard(card, this, playerContext);
                    }

                    // Clear out the hand
                    player.hand.clear();

                    // Put the cardsToKeep back
                    for (Card card : cardsToKeep) {
                        player.hand.add(card);
                    }
                }
            }
        }
    }

    private void youngwitch(Game game, MoveContext context, Player currentPlayer) {
        Card[] cardsToDiscard = currentPlayer.youngWitch_cardsToDiscard(context);
        boolean bad = false;

        if (cardsToDiscard == null) {
            bad = true;
        } else if (currentPlayer.hand.size() < 2 && cardsToDiscard.length != currentPlayer.hand.size()) {
            bad = true;
        } else if (cardsToDiscard.length != 2) {
            bad = true;
        } else {
            ArrayList<Card> copy = Util.copy(currentPlayer.hand);
            for (Card cardToKeep : cardsToDiscard) {
                if (!copy.remove(cardToKeep)) {
                    bad = true;
                    break;
                }
            }
        }

        if (bad) {
            if (currentPlayer.hand.size() >= 2) {
                Util.playerError(currentPlayer, "Young Witch discard error, just discarding the first 2.");
            }
            cardsToDiscard = new Card[Math.min(2, currentPlayer.hand.size())];
            for (int i = 0; i < cardsToDiscard.length; i++) {
                cardsToDiscard[i] = currentPlayer.hand.get(i);
            }
        }

        for (Card card : cardsToDiscard) {
            currentPlayer.discard(card, this, null);
            currentPlayer.hand.remove(card);
        }

        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !isDefendedFromAttack(game, targetPlayer, this)) {
                if (targetPlayer.hand.contains(game.baneCard) && targetPlayer.revealBane(context)) {
                    Card bane = null;
                    for (Card card : targetPlayer.hand) {
                        if (card.equals(game.baneCard)) {
                            bane = card;
                            break;
                        }
                    }

                    targetPlayer.reveal(bane, this, new MoveContext(game, targetPlayer));
                } else {
                    targetPlayer.attacked(this, context);
                    targetPlayer.gainNewCard(Cards.curse, this, new MoveContext(game, targetPlayer));
                }
            }
        }
    }

    private void followers(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.estate, this, context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);
                player.gainNewCard(Cards.curse, this, playerContext);
                if (player.hand.size() > 3) {
                    Card[] cardsToKeep = (player).followers_attack_cardsToKeep(playerContext);

                    boolean bad = false;
                    if (cardsToKeep == null || cardsToKeep.length != 3) {
                        bad = true;
                    } else {
                        ArrayList<Card> handCopy = Util.copy(player.hand);
                        for (Card cardToKeep : cardsToKeep) {
                            if (!handCopy.remove(cardToKeep)) {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad) {
                        Util.playerError(player, "Followers discard error, just keeping first 3.");
                        cardsToKeep = new Card[3];
                        cardsToKeep[0] = player.hand.get(0);
                        cardsToKeep[1] = player.hand.get(1);
                        cardsToKeep[2] = player.hand.get(2);
                    }

                    // Remove cards to keep from the hand temporarily
                    for (Card card : cardsToKeep) {
                        player.hand.remove(card);
                    }

                    // Discard all of the cards left
                    for (Card card : player.hand) {
                        player.discard(card, this, playerContext);
                    }

                    // Clear out the hand
                    player.hand.clear();

                    // Put the cardsToKeep back
                    for (Card card : cardsToKeep) {
                        player.hand.add(card);
                    }
                }
            }
        }
    }

    private void stables(Game game, MoveContext context, Player currentPlayer) {
        boolean valid = false;
        for(Card c : currentPlayer.hand) {
            if(c instanceof TreasureCard) {
                valid = true;
            }
        }
        
        if(valid) {
            TreasureCard toDiscard = currentPlayer.stables_treasureToDiscard(context);
   
            // This is optional, so ignore it if it's null or invalid
            if (toDiscard != null && currentPlayer.hand.contains(toDiscard)) {
                currentPlayer.hand.remove(toDiscard);
                currentPlayer.reveal(toDiscard, this, context);
                currentPlayer.discard(toDiscard, this, context);
                context.actions++;
   
                for (int i = 0; i < 3; i++) {
                    if(!game.drawToHand(currentPlayer, this)) {
                        break;
                    }
                }
            }
        }
    }

    private void margrave(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);
                game.drawToHand(player, this);
                
                if (player.hand.size() > 3) {
                    Card[] cardsToKeep = (player).margrave_attack_cardsToKeep(playerContext);

                    boolean bad = false;
                    if (cardsToKeep == null || cardsToKeep.length != 3) {
                        bad = true;
                    } else {
                        ArrayList<Card> handCopy = Util.copy(player.hand);
                        for (Card cardToKeep : cardsToKeep) {
                            if (!handCopy.remove(cardToKeep)) {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad) {
                        Util.playerError(player, "Margrave discard error, just keeping first 3.");
                        cardsToKeep = new Card[3];
                        cardsToKeep[0] = player.hand.get(0);
                        cardsToKeep[1] = player.hand.get(1);
                        cardsToKeep[2] = player.hand.get(2);
                    }

                    // Remove cards to keep from the hand temporarily
                    for (Card card : cardsToKeep) {
                        player.hand.remove(card);
                    }

                    // Discard all of the cards left
                    for (Card card : player.hand) {
                        player.discard(card, this, playerContext);
                    }

                    // Clear out the hand
                    player.hand.clear();

                    // Put the cardsToKeep back
                    for (Card card : cardsToKeep) {
                        player.hand.add(card);
                    }
                }
            }
        }
    }

    private void oracle(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer == currentPlayer || !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this, context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                ArrayList<Card> cards = new ArrayList<Card>();
                for(int i=0; i < 2; i++) {
                    Card c = game.draw(targetPlayer);
                    if(c == null) {
                        break;
                    }
                    cards.add(c);
                }
                
                if(cards.size() > 0) {
                    if(currentPlayer.oracle_shouldDiscard(context, targetPlayer, cards)) {
                        for(Card c : cards) {
                            targetPlayer.discard(c, this, targetContext);
                        }
                    }
                    else {
                        Card[] order = (targetPlayer).oracle_orderCards(context, cards.toArray(new Card[cards.size()]));
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
            game.drawToHand(currentPlayer, this);
        }
    }

    private void oasis(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card cardToDiscard = currentPlayer.oasis_cardToDiscard(context);
            if(cardToDiscard == null || !currentPlayer.hand.contains(cardToDiscard)) {
                Util.playerError(currentPlayer, "Returned an invalid card to discard with Oasis, picking one for you.");
                cardToDiscard = currentPlayer.hand.get(0);
            }
            
            currentPlayer.hand.remove(cardToDiscard);
            currentPlayer.reveal(cardToDiscard, this, context);
            currentPlayer.discard(cardToDiscard, this, null);
        }
    }

    private void jackOfAllTrades(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.silver, this, context);
        
        Card c = game.draw(currentPlayer);
        if(c != null) {
            boolean discard = currentPlayer.jackOfAllTrades_shouldDiscardCardFromTopOfDeck(context, c);
            if(discard) {
                currentPlayer.discard(c, this, context);
            } else {
                currentPlayer.putOnTopOfDeck(c);
            }
        }

        while(currentPlayer.hand.size() < 5) {
            if(!game.drawToHand(currentPlayer, this)) {
                break;
            }
        }
        
        Card cardToTrash = currentPlayer.jackOfAllTrades_nonTreasureToTrash(context);
        if(cardToTrash != null) {
            if(!currentPlayer.hand.contains(cardToTrash) || cardToTrash instanceof TreasureCard) {
                Util.playerError(currentPlayer, "Jack of All Trades returned invalid card to trash from hand, ignoring.");
            }
            else {
                currentPlayer.hand.remove(cardToTrash);
                currentPlayer.trash(cardToTrash, this, context);
            }
        }
    }

    private void develop(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card cardToTrash = currentPlayer.develop_cardToTrash(context);

            if(!currentPlayer.hand.contains(cardToTrash)) {
                Util.playerError(currentPlayer, "Returned an invalid card to trash with Develop, picking one for you.");
                cardToTrash = currentPlayer.hand.get(0);
            }
            
            int trashedCardCost = cardToTrash.getCost(context);
            
            Card lowCardToGain = null;
            Card highCardToGain = null;
            
            if(isNewCardAvailable(context, trashedCardCost - 1)) {
                lowCardToGain = currentPlayer.develop_lowCardToGain(context, trashedCardCost - 1);
            }
            
            if(isNewCardAvailable(context, trashedCardCost + 1)) {
                highCardToGain = currentPlayer.develop_highCardToGain(context, trashedCardCost + 1);
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
                    cardsToGain = currentPlayer.develop_orderCards(context, cardsToGain);
                }
            }
            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this, context);
            
            if(cardsToGain == null) {
                cardsToGain = new Card[0];
            }
            boolean bad = false;
            
            if(cardsToGain.length == 0) {
                for(Card c : context.getCardsInPlay()) {
                    if((c.getCost(context) == trashedCardCost - 1 || c.getCost(context) == trashedCardCost + 1) && context.getCardsLeft(c) > 0) {
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
                    
                    for(Card c : context.getCardsInPlay()) {
                        if(c.getCost(context) == costToCheck && context.getCardsLeft(c) > 0) {
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
                if(context.getCardsLeft(c) == 0) {
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

    private void cellar(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.cellar_cardsToDiscard(context);
        if (cards != null) {
            int numberOfCards = 0;
            for (Card card : cards) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card playersCard = currentPlayer.hand.get(i);
                    if (playersCard.equals(card)) {
                        currentPlayer.discard(currentPlayer.hand.remove(i), this, context);
                        numberOfCards++;
                        break;
                    }
                }
            }

            if (numberOfCards != cards.length) {
                Util.playerError(currentPlayer, "Cellar discard error, trying to discard cards not in hand, ignoring extra.");
            }

            while (numberOfCards > 0) {
                numberOfCards--;
                game.drawToHand(currentPlayer, this);
            }
        }
    }

    private void secretChamber(MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.secretChamber_cardsToDiscard(context);
        if (cards != null) {
            int numberOfCardsDiscarded = 0;
            for (Card card : cards) {
                if (currentPlayer.hand.remove(card)) {
                    currentPlayer.discard(card, this, null);
                    numberOfCardsDiscarded++;
                }
            }

            if (numberOfCardsDiscarded != cards.length) {
                Util.playerError(currentPlayer, "Secret chamber discard error, trying to discard cards not in hand, ignoring extra.");
            }

            context.addGold += numberOfCardsDiscarded;
        }
    }

    private void bureaucrat(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.silver, this, context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext playerContext = new MoveContext(game, player);

                ArrayList<VictoryCard> victoryCards = new ArrayList<VictoryCard>();

                for (Card card : player.hand) {
                    if (card instanceof VictoryCard) {
                        victoryCards.add((VictoryCard) card);
                    }
                }

                if (victoryCards.size() == 0) {
                    for (int i = 0; i < player.hand.size(); i++) {
                        Card card = player.hand.get(i);
                        player.reveal(card, this, playerContext);
                    }
                } else {
                    VictoryCard toTopOfDeck = null;

                    if (victoryCards.size() == 1) {
                        toTopOfDeck = victoryCards.get(0);
                    } else {
                        toTopOfDeck = (player).bureaucrat_cardToReplace(playerContext);

                        if (toTopOfDeck == null) {
                            Util.playerError(player, "No Victory Card selected for Bureaucrat, using first Victory Card in hand");
                            toTopOfDeck = victoryCards.get(0);
                        }
                    }

                    player.reveal(toTopOfDeck, this, playerContext);
                    player.hand.remove(toTopOfDeck);
                    player.putOnTopOfDeck(toTopOfDeck);
                }
            }
        }
    }

    private void golem(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        ArrayList<Card> toOrder = new ArrayList<Card>();

        while (toOrder.size() < 2) {
            Card draw = game.draw(currentPlayer);
            if (draw == null) {
                break;
            }

            currentPlayer.reveal(draw, this, context);

            if (draw instanceof ActionCard && !draw.equals(Cards.golem)) {
                toOrder.add(draw);
                // currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this, null);
        }

        if (!toOrder.isEmpty()) {
            Card[] toPlay;

            if (toOrder.size() == 1) {
                toPlay = toOrder.toArray(new Card[toOrder.size()]);
            } else {
                ActionCard[] playOrder = currentPlayer.golem_cardOrder(context, toOrder.toArray(new ActionCard[toOrder.size()]));

                if (playOrder == null) {
                    Util.playerError(currentPlayer, "Nothing back from golem_cardOrder, using order found");
                    toPlay = toOrder.toArray(new Card[toOrder.size()]);
                } else if (playOrder.length != toOrder.size()) {
                    Util.playerError(currentPlayer, "Bad playOrder size (" + playOrder.length + " != " + toOrder.size()
                        + " from golem_cardOrder, using order found");
                    toPlay = toOrder.toArray(new Card[toOrder.size()]);
                } else {
                    boolean bad = false;
                    for (int i = 0; i < toOrder.size(); i++) {
                        Card candidate = playOrder[i];
                        if (!toOrder.contains(candidate)) {
                            Util.playerError(currentPlayer, candidate + " wasn't in your toOrder list (" + toOrder.toString() + ")");
                            bad = true;
                        }
                    }

                    toPlay = bad ? toOrder.toArray(new Card[toOrder.size()]) : playOrder;
                }
            }

            context.freeActionInEffect++;
            for (Card card : toPlay) {
                currentPlayer.hand.add(card, false);
                ((ActionCardImpl) card).play(game, context);
            }
            context.freeActionInEffect--;
        }
    }

    private void adventurer(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        int treasureCardsRevealed = 0;

        while (treasureCardsRevealed < 2) {
            Card draw = game.draw(currentPlayer);
            if (draw == null) {
                break;
            }
            currentPlayer.reveal(draw, this, context);

            if (draw instanceof TreasureCard) {
                treasureCardsRevealed++;
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this, context);
        }
    }

    private void library(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        while (currentPlayer.hand.size() < 7) {
            Card draw = game.draw(currentPlayer);
            if (draw == null) {
                break;
            }

            boolean shouldKeep = true;
            if (draw instanceof ActionCard) {
                shouldKeep = currentPlayer.library_shouldKeepAction(context, (ActionCard) draw);
            }

            if (shouldKeep) {
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this, null);
        }
    }

    private void chapel(MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.chapel_cardsToTrash(context);
        if (cards != null) {
            if (cards.length > 4) {
                Util.playerError(currentPlayer, "Chapel trash error, trying to trash too many cards, ignoring.");
            } else {
                for (Card card : cards) {
                    for (int i = 0; i < currentPlayer.hand.size(); i++) {
                        Card playersCard = currentPlayer.hand.get(i);
                        if (playersCard.equals(card)) {
                            Card thisCard = currentPlayer.hand.remove(i);

                            currentPlayer.trash(thisCard, this, context);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void haven(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.haven_cardToSetAside(context);
        if ((card == null && hand(currentPlayer).size() > 0) || (card != null && !hand(currentPlayer).contains(card))) {
            Util.playerError(currentPlayer, "Haven set aside card error, setting aside the first card in hand.");
            card = hand(currentPlayer).get(0);
        }

        if (card != null) {
            hand(currentPlayer).remove(card);
            haven(currentPlayer).add(card);
        }
    }

    private void pirateShip(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Player> playersToAttack = new ArrayList<Player>();
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                playersToAttack.add(targetPlayer);
                targetPlayer.attacked(this, context);
            }
        }

        if (currentPlayer.pirateShip_takeTreasure(context)) {
            takePirateShipTreasure(context);
        } else {
            boolean treasureFound = false;
            for (Player targetPlayer : playersToAttack) {
                MoveContext targetContext = new MoveContext(game, targetPlayer);

                ArrayList<TreasureCard> treasures = new ArrayList<TreasureCard>();

                for (int i = 0; i < 2; i++) {
                    Card card = game.draw(targetPlayer);

                    if (card != null) {
                        targetPlayer.reveal(card, this, targetContext);

                        if (card instanceof TreasureCard) {
                            treasures.add((TreasureCard) card);
                        } else {
                            targetPlayer.discard(card, this, targetContext);
                        }
                    }
                }

                TreasureCard cardToTrash = null;

                if (treasures.size() == 1) {
                    cardToTrash = treasures.get(0);
                } else if (treasures.size() == 2) {
                    if (treasures.get(0).equals(treasures.get(1))) {
                        cardToTrash = treasures.get(0);
                    } else {
                        cardToTrash = currentPlayer.pirateShip_treasureToTrash(context, treasures.toArray(new TreasureCard[]{}));
                    }

                    for (TreasureCard treasure : treasures) {
                        if (!treasure.equals(cardToTrash)) {
                            targetPlayer.discard(treasure, this, targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this, targetContext);
                    treasureFound = true;
                }
            }

            if (treasureFound) {
                increasePirateShipTreasure(currentPlayer);
            }
        }
    }

    private void smugglers(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.smugglers_cardToObtain(context);
        if (card != null) {
            if (card.getCost(context) > 6 || card.isPrize()) {
                Util.playerError(currentPlayer, "Smugglers card error, ignoring.");
                card = null;
            } else {
                boolean found = false;

                for (Card cardToCheck : context.getCardsObtainedByLastPlayer()) {
                    if (cardToCheck == card) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Util.playerError(currentPlayer, "Smugglers card error, ignoring.");
                    card = null;
                }
            }
        }

        if (card != null) {
            if (!currentPlayer.gainNewCard(card, this, context)) {
                // TODO do this error output everywhere
                Util.playerError(currentPlayer, "Smugglers card error, no more cards left of that type, ignoring.");
            }
        }
    }

    private void feast(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.feast_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 5) {
                currentPlayer.gainNewCard(card, this, context);
            }
        }
    }

    private void chancellor(Game game, MoveContext context, Player currentPlayer) {
        boolean discard = currentPlayer.chancellor_shouldDiscardDeck(context);
        if (discard) {
            while (currentPlayer.getDeckSize() > 0) {
                currentPlayer.discard(game.draw(currentPlayer), this, null, false);
            }
        }
    }

    private void moneyLender(MoveContext context, Player currentPlayer) {
        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            if (card.equals(Cards.copper)) {
                Card thisCard = currentPlayer.hand.remove(i);
                context.addGold += 3;
                currentPlayer.trash(thisCard, this, context);
                break;
            }
        }
    }

    Player getNextPlayer(Player player) {
        int next = -1;
        for (int i = 0; i < Game.players.length; i++) {
            if (player == Game.players[i]) {
                next = i + 1;
                if (next >= Game.players.length) {
                    next = 0;
                }
                break;
            }
        }

        if (next == -1) {
            Util.log("ERROR:getNextPlayer() could not find current player:" + player.getPlayerName());
            return null;
        }

        return Game.players[next];
    }

    @Override
    public String getStats() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.getStats());

        if (addActions > 0 || addBuys > 0 || addCards > 0 || addGold > 0) {
            sb.append(" ");

            boolean start = true;
            if (addActions > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addActions + " Action");
                if (addActions > 1) {
                    sb.append("s");
                }
            }
            if (addBuys > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addBuys + " Buy");
                if (addBuys > 1) {
                    sb.append("s");
                }
            }
            if (addGold > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addGold + " Gold");
            }
            if (addCards > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addCards + " Card");
                if (addCards > 1) {
                    sb.append("s");
                }
            }
        }

        return sb.toString();
    }

    protected boolean isDefendedFromAttack(Game game, Player player, Card responsible) {
        return Util.isDefendedFromAttack(game, player, responsible);
    }

    protected void increasePirateShipTreasure(Player player) {
        (player).pirateShipTreasure++;
    }

    protected void takePirateShipTreasure(MoveContext context) {
        ((MoveContext) context).addGold += (context.getPlayer()).pirateShipTreasure;
    }

    // TODO better way to do, possible security hole
    protected CardList hand(Player player) {
        return player.hand;
    }

    protected CardList haven(Player player) {
        return player.haven;
    }

    protected void drawToHand(Game game, Player player, Card responsible) {
        game.drawToHand(player, responsible);
    }

    protected void addToHand(Player player, Card card) {
        player.hand.add(card);
    }

    protected Card draw(Game game, Player player) {
        return game.draw(player);
    }

    protected Player[] getAllPlayers() {
        return Game.players;
    }

    protected Card removeFromHand(Player player, int i) {
        return player.hand.remove(i);
    }
    
    protected boolean isNewCardAvailable(MoveContext context, int cost) {
        for(Card c : context.getCardsInPlay()) {
            if(c.getCost(context) == cost && context.getCardsLeft(c) > 0) {
                return true;
            }
        }
        
        return false;
    }
    
    public void workshop(Player currentPlayer, MoveContext context) {
        Card card = currentPlayer.workshop_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 4) {
                currentPlayer.gainNewCard(card, this, context);
            }
        }
    }

}
