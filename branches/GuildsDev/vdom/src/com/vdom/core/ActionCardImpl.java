package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CurseCard;
import com.vdom.api.DurationCard;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEventListener;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.MoveContext.PileSelection;
import com.vdom.core.Player.JesterOption;
import com.vdom.core.Player.SpiceMerchantOption;
import com.vdom.core.Player.TournamentOption;
import com.vdom.core.Player.TrustySteedOption;

public class ActionCardImpl extends CardImpl implements ActionCard {
	// template (immutable)
	protected int addActions;
    protected int addBuys;
    protected int addCards;
    protected int addGold;
    protected int addVictoryTokens;
    protected boolean attack;
    protected boolean looter;
    boolean trashForced = false;
    boolean trashOnUse = false;

    public ActionCardImpl(Builder builder) {
        super(builder);
        addActions = builder.addActions;
        addBuys = builder.addBuys;
        addCards = builder.addCards;
        addGold = builder.addGold;
        addVictoryTokens = builder.addVictoryTokens;
        attack = builder.attack;
        looter = builder.looter;
        trashOnUse = builder.trashOnUse;
        trashForced = builder.trashForced;
    }

    public static class Builder extends CardImpl.Builder{
	    protected int addActions;
	    protected int addBuys;
	    protected int addCards;
	    protected int addGold;
	    protected int addVictoryTokens;
	    protected boolean attack;
	    protected boolean looter;
	    protected boolean trashOnUse;
        protected boolean trashForced = false;

        public Builder(Cards.Type type, int cost) {
            super(type, cost);
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

        public Builder looter() {
            looter = true;
            return this;
        }

        public Builder trashOnUse() {
            trashOnUse = true;
            return this;
        }

        public Builder trashForced() {
            trashForced = true;
            return this;
        }
        public ActionCardImpl build() {
            return new ActionCardImpl(this);
        }

		@Override
		public Builder isShelter() {
			this.isShelter = true;
			return this;
		}

		@Override
		public Builder isRuins() {
			this.isRuins = true;
			return this;
		}

		public Builder isKnight() {
			this.isKnight = true;
			this.attack = true;
			return this;
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

    public boolean trashForced() {
        return trashForced;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        ActionCardImpl c = new ActionCardImpl();
        copyValues(c);
        return c;
//        return instantiate(null);
    }
    
//    protected CardImpl impersonateAnotherCard(Card bandOfMisfits) {
//        ActionCardImpl c = new ActionCardImpl();
//        copyValues(c);
//        c.setBandOfMisfitsCard(bandOfMisfits);
//        return c;
//    }

    protected void copyValues(ActionCardImpl c) {
        super.copyValues(c);
        c.addActions = addActions;
        c.addBuys = addBuys;
        c.addCards = addCards;
        c.addGold = addGold;
        c.addVictoryTokens = addVictoryTokens;
        c.attack = attack;
        c.looter = looter;
        c.trashOnUse = trashOnUse;
    }

    protected ActionCardImpl() {
    }

    public void play(Game game, MoveContext context) {
        play(game, context, true);
    }

    public void play(Game game, MoveContext context, boolean fromHand) {
        super.play(game, context);

        Player currentPlayer = context.getPlayer();
        boolean newCard = false;
        ActionCardImpl actualCard = (this.getControlCard() != null ? (ActionCardImpl) this.getControlCard() : this);

        if (this.numberTimesAlreadyPlayed == 0 && this == actualCard) {
        	newCard = true;
            this.movedToNextTurnPile = false;
            if (fromHand)
                currentPlayer.hand.remove(this);
            if (trashOnUse) {
                currentPlayer.trash(this, null, context);
            } else if (this instanceof DurationCard) {
              	currentPlayer.nextTurnCards.add((DurationCard) this);
            } else {
            	currentPlayer.playedCards.add(this);
            }
        }

        GameEvent event = new GameEvent(GameEvent.Type.PlayingAction, (MoveContext) context);
        event.card = this;
        event.newCard = newCard;
        game.broadcastEvent(event);

        // playing an action
        if (this == actualCard) 
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
        
        if (isAttack())
        	attackPlayed(context, game, currentPlayer);

        additionalCardActions(game, context, currentPlayer);

        event = new GameEvent(GameEvent.Type.PlayedAction, (MoveContext) context);
        event.card = this;
        game.broadcastEvent(event);
    }

    protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
        switch (this.getType()) {
        case BandOfMisfits:
        	bandOfMisfits(game, context, currentPlayer);
            break;
        case MoneyLender:
            moneyLender(context, currentPlayer);
            break;
        case Chancellor:
            chancellor(game, context, currentPlayer);
            break;
        case Workshop:
            workshop(currentPlayer, context);
            break;
        case Feast:
            feast(context, currentPlayer);
            break;
        case KingsCourt:
        case ThroneRoom:
        case Procession:
            throneRoomKingsCourt(game, context, currentPlayer);
            break;
        case Smugglers:
            smugglers(context, currentPlayer);
            break;
        case PirateShip:
            pirateShip(game, context, currentPlayer);
            break;
        case Haven:
            haven(context, currentPlayer);
            break;
        case Chapel:
            chapel(context, currentPlayer);
            break;
        case Library:
            library(game, context, currentPlayer);
            break;
        case Adventurer:
            adventurer(game, context, currentPlayer);
            break;
        case Golem:
            golem(game, context, currentPlayer);
            break;
        case Bureaucrat:
            bureaucrat(game, context, currentPlayer);
            break;
        case SecretChamber:
            secretChamber(context, currentPlayer);
            break;
        case Cellar:
            cellar(game, context, currentPlayer);
            break;
        case Remodel:
            remodel(context, currentPlayer);
            break;
        case Militia:
            militia(game, context, currentPlayer);
            break;
        case Thief:
            thief(game, context, currentPlayer);
            break;
        case Conspirator:
            conspirator(game, context, currentPlayer);
            break;
        case Spy:
        case ScryingPool:
            spyAndScryingPool(game, context, currentPlayer);
            break;
        case Courtyard:
            courtyard(context, currentPlayer);
            break;
        case Baron:
            baron(context, currentPlayer);
            break;
        case Swindler:
            swindler(game, context, currentPlayer);
            break;
        case Steward:
            steward(game, context, currentPlayer);
            break;
        case Scout:
            scout(game, context, currentPlayer);
            break;
        case ShantyTown:
            shantyTown(game, context, currentPlayer);
            break;
        case Saboteur:
            saboteur(game, context, currentPlayer);
            break;
        case Pawn:
            pawn(game, context, currentPlayer);
            break;
        case Minion:
            minion(game, context, currentPlayer);
            break;
        case MiningVillage:
            miningVillage(context, currentPlayer);
            break;
        case Masquerade:
            masquerade(game, context, currentPlayer);
            break;
        case Ironworks:
            ironworks(game, context, currentPlayer);
            break;
        case Nobles:
            nobles(game, context, currentPlayer);
            break;
        case Coppersmith:
            copperSmith(context);
            break;
        case Tribute:
            tribute(game, context, currentPlayer);
            break;
        case Upgrade:
            upgrade(context, currentPlayer);
            break;
        case WishingWell:
            wishingWell(game, context, currentPlayer);
            break;
        case Bridge:
            context.cardCostModifier -= 1;
            break;
        case TradingPost:
            tradingPost(context, currentPlayer);
            break;
        case Torturer:
            torturer(game, context, currentPlayer);
            break;
        case Tactician:
            tactician(context, currentPlayer);
            break;
        case Familiar:
        case Witch:
            witchFamiliar(game, context, currentPlayer);
            break;
        case Apothecary:
            apothecary(game, context, currentPlayer);
            break;
        case Transmute:
            transmute(context, currentPlayer);
            break;
        case Apprentice:
            apprentice(game, context, currentPlayer);
            break;
        case University:
            university(context, currentPlayer);
            break;
        case Mine:
            mine(context, currentPlayer);
            break;
        case CouncilRoom:
            councilRoom(game, context);
            break;
        case SeaHag:
            seaHag(game, context, currentPlayer);
            break;
        case NativeVillage:
            nativeVillage(game, context, currentPlayer);
            break;
        case Island:
            island(context, currentPlayer);
            break;
        case PearlDiver:
            pearlDiver(context, currentPlayer);
            break;
        case Lookout:
            lookout(game, context, currentPlayer);
            break;
        case Navigator:
            navigator(game, context, currentPlayer);
            break;
        case Embargo:
            embargo(game, context, currentPlayer);
            break;
        case TreasureMap:
            treasureMap(context, currentPlayer);
            break;
        case Explorer:
            explorer(context, currentPlayer);
            break;
        case Cutpurse:
            cutpurse(game, context, currentPlayer);
            break;
        case Warehouse:
            warehouse(context, currentPlayer);
            break;
        case Ambassador:
            ambassador(game, context, currentPlayer);
            break;
        case Salvager:
            salvager(context, currentPlayer);
            break;
        case GhostShip:
            ghostShip(game, context, currentPlayer);
            break;
        case Bishop:
            bishop(game, context, currentPlayer);
            break;
        case City:
            city(game, context, currentPlayer);
            break;
        case CountingHouse:
            countingHouse(context, currentPlayer);
            break;
        case Expand:
            expand(context, currentPlayer);
            break;
        case Forge:
            forge(context, currentPlayer);
            break;
        case Goons:
            goons(game, context, currentPlayer);
            break;
        case Mint:
            mint(context, currentPlayer);
            break;
        case Mountebank:
            mountebank(game, context, currentPlayer);
            break;
        case Rabble:
            rabble(game, context, currentPlayer);
            break;
        case TradeRoute:
            tradeRoute(game, context, currentPlayer);
            break;
        case Vault:
            vault(game, context, currentPlayer);
            break;
        case WatchTower:
            watchTower(game, currentPlayer);
            break;
        case FarmingVillage:
            farmingVillage(game, context, currentPlayer);
            break;
        case FortuneTeller:
            fortuneTeller(game, context, currentPlayer);
            break;
        case Hamlet:
            hamlet(context, currentPlayer);
            break;
        case Harvest:
            harvest(game, context, currentPlayer);
            break;
        case HorseTraders:
            horseTraders(context, currentPlayer);
            break;
        case HuntingParty:
            huntingParty(game, context, currentPlayer);
            break;
        case Jester:
            jester(game, context, currentPlayer);
            break;
        case Menagerie:
            menagerie(game, context, currentPlayer);
            break;
        case Remake:
            remake(context, currentPlayer);
            break;
        case Tournament:
            tournament(game, context, currentPlayer);
            break;
        case YoungWitch:
            youngwitch(game, context, currentPlayer);
            break;
        case BagofGold:
            currentPlayer.gainNewCard(Cards.gold, this.controlCard, context);
            break;
        case Followers:
            followers(game, context, currentPlayer);
            break;
        case Princess:
            if (this.controlCard.numberTimesAlreadyPlayed == 0) {
                context.cardCostModifier -= 2;
            }
            break;
        case TrustySteed:
            trustySteed(game, context, currentPlayer);
            break;
        case Crossroads:
            crossroads(game, context, currentPlayer);
            break;
        case Duchess:
            duchess(game);
            break;
        case Develop:
            develop(context, currentPlayer);
            break;
        case Oasis:
            oasis(context, currentPlayer);
            break;
        case JackofallTrades:
            jackOfAllTrades(game, context, currentPlayer);
            break;
        case NobleBrigand:
            nobleBrigandAttack(context, true);
            break;
        case SpiceMerchant:
            spiceMerchant(game, context, currentPlayer);
            break;
        case Oracle:
            oracle(game, context, currentPlayer);
            break;
        case Scheme:
            context.schemesPlayed++;
            break;
        case Trader:
            trader(context, currentPlayer);
            break;
        case Cartographer:
            cartographer(game, context, currentPlayer);
            break;
        case Embassy:
            embassy(context, currentPlayer);
            break;
        case Inn:
            inn(context, currentPlayer);
            break;
        case Mandarin:
            mandarin(context, currentPlayer);
            break;
        case Margrave:
            margrave(game, context, currentPlayer);
            break;
        case Stables:
            stables(game, context, currentPlayer);
            break;
        case Possession:
            possession(context);
            break;
        case PoorHouse:
        	poorHouse(context, currentPlayer);
        	break;
        case Sage:
        	sage(game, context, currentPlayer);
        	break;
        case Rats:
        	rats(context, currentPlayer);
        	break;
        case Squire:
        	squire(context, currentPlayer);
        	break;
        case Armory:
        	armory(currentPlayer, context);
        	break;
        case Altar:
        	altar(currentPlayer, context);
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
        case DeathCart:
        	deathCart(currentPlayer, context);
        	break;
        case Forager:
        	forager(game, currentPlayer, context);
        	break;
        case Graverobber:
        	graverobber(game, currentPlayer, context);
        	break;
        case Ironmonger:
        	ironmonger(game, currentPlayer, context);
        	break;
        case JunkDealer:
        	junkDealer(currentPlayer, context);
        	break;
        case Mystic:
        	mystic(game, context, currentPlayer);
        	break;
        case Scavenger:
        	scavenger(game, context, currentPlayer);
        	break;
        case Storeroom:
        	storeroom(game, context, currentPlayer);
        	break;
        case WanderingMinstrel:
        	wanderingMinstrel(currentPlayer, context);
        	break;
        case Rebuild:
        	rebuild(currentPlayer, context);
        	break;
        case Rogue:
        	rogue(game, context, currentPlayer);
        	break;
        case Pillage:
        	pillage(game, context, currentPlayer);
        	break;
        case Governor:
        	governor(game, context, currentPlayer);
        	break;
        case Envoy:
        	envoy(game, context, currentPlayer);
        	break;
        case Survivors:
        	survivors(context, game, currentPlayer);
        	break;
        case Cultist:
        	cultist(context, game, currentPlayer);
        	break;
        case Urchin:
        	urchin(context, game, currentPlayer);
        	break;
        case Mercenary:
        	mercenary(context, game, currentPlayer);
        	break;
        case Marauder:
        	marauder(context, game, currentPlayer);
        	break;
        case Hermit:
        	hermit(context, game, currentPlayer);
        	break;
        case Madman:
        	madman(context, game, currentPlayer);
        	break;
        case Vagrant:
        	vagrant(context, game, currentPlayer);
        	break;
        case DameAnna:
        	dameAnna(context, currentPlayer);
        	break;
        case DameNatalie:
        	dameNatalie(context, currentPlayer);
        	break;
        case SirMichael:
        	sirMichael(context, currentPlayer);
        	break;
        case DameJosephine:
        case DameMolly:
        case DameSylvia:
        case SirBailey:
        case SirDestry:
        case SirMartin:
        case SirVander:
        	knight(context, currentPlayer);
        	break;
        default:
            break;
        }
    }

	private void warehouse(MoveContext context, Player currentPlayer) {
        if (currentPlayer.hand.size() == 0) {
            return;
        }

        Card[] cards;
        if (currentPlayer.hand.size() > 3) {
            cards = currentPlayer.controlPlayer.warehouse_cardsToDiscard(context);
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
            currentPlayer.reveal(cards[i], this.controlCard, context);
            currentPlayer.discard(cards[i], this.controlCard, null);
        }
    }

    private void cutpurse(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);

                if (player.hand.contains(Cards.copper)) {
                    Card card = player.hand.get(Cards.copper);
                    player.hand.remove(Cards.copper);
                    player.discard(card, this.controlCard, playerContext);
                } else {
                    for (Card card : player.getHand()) {
                        player.reveal(card, this.controlCard, playerContext);
                    }
                }
            }
        }
    }

    private void explorer(MoveContext context, Player currentPlayer) {
        Card province = null;
        for (Card card : currentPlayer.hand) {
            if (card.equals(Cards.province)) {
                province = card;
                break;
            }
        }

        Card treasure;
        if (province != null && currentPlayer.controlPlayer.explorer_shouldRevealProvince(context)) {
        	currentPlayer.reveal(province, this.controlCard, context);
            treasure = Cards.gold;
        } else {
            treasure = Cards.silver;
        }

        currentPlayer.gainNewCard(treasure, this.controlCard, context);
    }

    private void treasureMap(MoveContext context, Player currentPlayer) {
        // Check for Treasure Map in hand
        Card anotherMap = null;
        for (Card card : currentPlayer.hand) {
            if (card.equals(this)) {
                anotherMap = card;
                break;
            }
        }

        // Treasure Map still trashes extra Treasure Maps in hand on throneRoom
        if (this.numberTimesAlreadyPlayed == 0) {
            if (anotherMap != null) {
                // going to get the gold so trash map played and map from hand
                currentPlayer.trash(currentPlayer.playedCards.removeCard(this.controlCard), null, context);
                currentPlayer.trash(currentPlayer.hand.removeCard(anotherMap), null, context);
                for (int i = 0; i < 4; i++) {
                    currentPlayer.gainNewCard(Cards.gold, this, context);
                }

            } else {
                // Send notification that the map played was trashed (as per rules
                // when a single map is played)
                currentPlayer.trash(currentPlayer.playedCards.removeCard(this.controlCard), null, context);
            }
        } else {
            if (anotherMap != null) {
                // trash it, but no gold
                currentPlayer.trash(currentPlayer.hand.removeCard(anotherMap), null, context);
            }
        }
    }

    private void embargo(Game game, MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.embargo_supplyToEmbargo(context);
        while (game.addEmbargo(card) == null) {
            Util.playerError(currentPlayer, "Embargo error, adding embargo to random card.");
            while (true) {
            	card = Util.randomCard(context.getCardsInGame());
            	if (game.isValidEmbargoPile(card)) break;
            }
        }

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
            if (currentPlayer.controlPlayer.navigator_shouldDiscardTopCards(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]))) {
                while (!topOfTheDeck.isEmpty()) {
                    currentPlayer.discard(topOfTheDeck.remove(0), this.controlCard, null);
                }
            } else {
                Card[] order = currentPlayer.controlPlayer.navigator_cardOrder(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));

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
            toTrash = currentPlayer.controlPlayer.lookout_cardToTrash(context, cards.toArray(new Card[cards.size()]));
        } else {
            toTrash = cards.get(0);
        }

        if (toTrash == null || !cards.contains(toTrash)) {
            Util.playerError(currentPlayer, "Lookout trash error, just picking the first card.");
            toTrash = cards.get(0);
        }

        currentPlayer.trash(toTrash, this.controlCard, context);

        cards.remove(toTrash);
        if (cards.size() == 0) {
            return;
        }

        Card toDiscard;

        if (cards.size() > 1) {
            toDiscard = currentPlayer.controlPlayer.lookout_cardToDiscard(context, cards.toArray(new Card[cards.size()]));
        } else {
            toDiscard = cards.get(0);
        }
        if (toDiscard == null || !cards.contains(toDiscard)) {
            Util.playerError(currentPlayer, "Lookout discard error, just picking the first card.");
            toDiscard = cards.get(0);
        }

        currentPlayer.discard(toDiscard, this.controlCard, context);

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
            if (currentPlayer.controlPlayer.pearlDiver_shouldMoveToTop(context, card)) {
                currentPlayer.removeFromDeckBottom();
                currentPlayer.putOnTopOfDeck(card);
            }
        }
    }

    private void island(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.island_cardToSetAside(context);
        if (card != null && !currentPlayer.hand.contains(card)) {
            Util.playerError(currentPlayer, "Island set aside card error, just setting aside island.");
            card = null;
        }

        // Move to island mat if not already played
        if (this.controlCard.numberTimesAlreadyPlayed == 0) {
        	currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf((Card) this.controlCard));
            currentPlayer.island.add(this.controlCard);
            this.controlCard.stopImpersonatingCard();
        }

        if (card != null) {
            currentPlayer.hand.remove(card);
            currentPlayer.island.add(card);
        }
    }

    private void nativeVillage(Game game, MoveContext context, Player currentPlayer) {
        if (currentPlayer.controlPlayer.nativeVillage_takeCards(context)) {
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
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);

                Card draw = game.draw(player);
                if (draw != null) {
                    player.discard(draw, this.controlCard, playerContext);
                }

                player.gainNewCard(Cards.curse, this.controlCard, playerContext);
            }
        }
    }

    private void councilRoom(Game game, MoveContext context) {
        for (Player player : getAllPlayers()) {
            if (player != context.getPlayer()) {
                drawToHand(game, player, this.controlCard);
            }
        }
    }

    private void mine(MoveContext context, Player currentPlayer) {
        TreasureCard cardToUpgrade = currentPlayer.controlPlayer.mine_treasureFromHandToUpgrade(context);
        if (cardToUpgrade == null) {
            Card[] cards = currentPlayer.getTreasuresInHand().toArray(new Card[] {});
	        if (cards.length != 0) {
	            Util.playerError(currentPlayer, "Mine card to upgrade was invalid, picking treasure from hand.");
	            cardToUpgrade = (TreasureCard) Util.randomCard(cards);
	        }
        }

        if (cardToUpgrade != null) {
            CardList hand = currentPlayer.getHand();
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);

                if (cardToUpgrade.equals(card)) {
                    Card thisCard = removeFromHand(currentPlayer, i);
                    currentPlayer.trash(thisCard, this.controlCard, context);

                    TreasureCard newCard = currentPlayer.controlPlayer.mine_treasureToObtain(context, card.getCost(context) + 3, card.costPotion());
                    if (!(newCard != null && newCard.getCost(context) <= card.getCost(context) + 3 && context.getCardsLeftInPile(newCard) > 0)) {
                        Util.playerError(currentPlayer, "Mine treasure to obtain was invalid, picking random treasure from table.");
                        for (Card treasureCard : context.getTreasureCardsInGame()) {
                            if (context.getCardsLeftInPile(treasureCard) > 0 && treasureCard.getCost(context) <= card.getCost(context) + 3)
                                if (!treasureCard.costPotion() || card.costPotion())
                                    newCard = (TreasureCard) treasureCard;
                        }
                    }

                    if (newCard != null && newCard.getCost(context) <= card.getCost(context) + 3 && context.getCardsLeftInPile(newCard) > 0)
                        currentPlayer.gainNewCard(newCard, this.controlCard, context);
                    break;
                }
            }
        }
    }

    private void university(MoveContext context, Player currentPlayer) {
        ActionCard cardToObtain = currentPlayer.controlPlayer.university_actionCardToObtain(context);
        if (cardToObtain != null && cardToObtain instanceof ActionCard && cardToObtain.getCost(context) <= 5 && !cardToObtain.costPotion()) {
            currentPlayer.gainNewCard(cardToObtain, this.controlCard, context);
        }
    }

    private void apprentice(Game game, MoveContext context, Player currentPlayer) {
    	if(currentPlayer.hand.size() > 0) {
            Card cardToTrash = currentPlayer.controlPlayer.apprentice_cardToTrash(context);
	        if (cardToTrash == null || !currentPlayer.hand.contains(cardToTrash)) {
	            Util.playerError(currentPlayer, "Apprentice card to trash was invalid, trashing random card.");
	            cardToTrash = Util.randomCard(currentPlayer.hand);
	        }
            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this.controlCard, context);

            for (int i = 1; i <= (cardToTrash.getCost(context) + (cardToTrash.costPotion() ? 2 : 0)); i++) {
                game.drawToHand(currentPlayer, this.controlCard);
            }
    	}
    }

    private void transmute(MoveContext context, Player currentPlayer) {
        Card cardToTrash = currentPlayer.controlPlayer.transmute_cardToTrash(context);
        if (cardToTrash == null) {
            Util.playerError(currentPlayer, "Transmute card to trash was null, not trashing anything.");
        } else if (!currentPlayer.hand.contains(cardToTrash)) {
            Util.playerError(currentPlayer, "Transmute card to trash is not in your hand, not trashing anything.");
        } else {
            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this.controlCard, context);
            if (cardToTrash instanceof ActionCard) {
                currentPlayer.gainNewCard(Cards.duchy, this.controlCard, context);
            }
            if (cardToTrash instanceof TreasureCard) {
                currentPlayer.gainNewCard(Cards.transmute, this.controlCard, context);
            }
            if (cardToTrash instanceof VictoryCard) {
                currentPlayer.gainNewCard(Cards.gold, this.controlCard, context);
            }
        }
    }

    private void apothecary(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> residue = new ArrayList<Card>();
        for (int i = 1; i <= 4; i++) {
            Card card = game.draw(currentPlayer);
            if (card != null) {
            	currentPlayer.reveal(card, this.controlCard, context);
            	if (card.equals(Cards.copper) || card.equals(Cards.potion)) {
            		currentPlayer.hand.add(card);
	            } else {
	                residue.add(card);
	            }
            }
        }

        if (residue.size() > 0) {
            ArrayList<Card> playerCards = currentPlayer.controlPlayer.apothecary_cardsForDeck(context, residue);
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
            if (player != currentPlayer && !isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                player.gainNewCard(Cards.curse, this.controlCard, new MoveContext(game, player));
            }
        }
    }

    private void tactician(MoveContext context, Player currentPlayer) {
        // throneroom has no effect since hand is already empty
        if (this.controlCard.numberTimesAlreadyPlayed == 0) {
            // Only works if at least one card discarded
            if (currentPlayer.hand.size() > 0) {
                while (!currentPlayer.hand.isEmpty()) {
                    currentPlayer.discard(currentPlayer.hand.remove(0), this.controlCard, null);
                }
            } else {
                currentPlayer.nextTurnCards.remove(this.controlCard);
                currentPlayer.playedCards.add(this.controlCard);
            }
        } else {
            // reset clone count
            this.controlCard.cloneCount = 1;
        }
    }

    private void torturer(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this.controlCard)) {
                targetPlayer.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, targetPlayer);
                Player.TorturerOption option;
                try {
                    option = (targetPlayer).controlPlayer.torturer_attack_chooseOption(context);
                } catch (NoSuchFieldError e) {
                    Util.playerError(targetPlayer, "'Take three cards' version of torturer attack no longer supported.");
                    option = null;
                }

                if (option == null) {
                    Util.playerError(targetPlayer, "Torturer option error, taking curse card.");
                    option = Player.TorturerOption.TakeCurse;
                }

                if (option == Player.TorturerOption.TakeCurse) {
                    targetPlayer.gainNewCard(Cards.curse, this.controlCard, playerContext);
                } else {
                    ArrayList<Card> handCopy = Util.copy(targetPlayer.getHand());
                	Card[] cardsToDiscard = (targetPlayer).controlPlayer.torturer_attack_cardsToDiscard(context);

                    boolean bad = false;
                    if (cardsToDiscard == null) {
                        bad = true;
                    } else if (handCopy.size() < 2 && cardsToDiscard.length != handCopy.size()) {
                        bad = true;
                    } else if (cardsToDiscard.length != 2) {
                        bad = true;
                    } else {
                        ArrayList<Card> copyForDiscard = Util.copy(targetPlayer.getHand());
                        for (Card cardToKeep : cardsToDiscard) {
                            if (!copyForDiscard.remove(cardToKeep)) {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad) {
                        if (handCopy.size() >= 2) {
                            Util.playerError(targetPlayer, "Torturer discard error, just discarding the first 2.");
                        }
                        cardsToDiscard = new Card[Math.min(2, handCopy.size())];
                        for (int i = 0; i < cardsToDiscard.length; i++) {
                            cardsToDiscard[i] = handCopy.get(i);
                        }
                    }

                    for (Card card : cardsToDiscard) {
                        targetPlayer.hand.remove(card);
                        targetPlayer.discard(card, this.controlCard, playerContext);
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
            currentPlayer.reveal(card, this.controlCard, context);
        }

        Card[] order = currentPlayer.controlPlayer.scout_orderCards(context, cards.toArray(new Card[cards.size()]));
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
        
        Card card = currentPlayer.controlPlayer.ambassador_revealedCard(context);

        if (card == null) {
            card = Util.randomCard(currentPlayer.hand);
        } else if (!currentPlayer.hand.contains(card)) {
            Util.playerError(currentPlayer, "Ambassador revealed card error, picking random card.");
            card = Util.randomCard(currentPlayer.hand);
        }
        Card origCard = card;
        Card virtCard = card;
        
        AbstractCardPile pile;
        if (card.isKnight()) {
        	virtCard = Cards.virtualKnight;
        } else if (card.isRuins()) {
        	virtCard = Cards.virtualRuins;
        }
        pile = game.getPile(virtCard);
    
        currentPlayer.reveal(origCard, this.controlCard, context);
        //Util.log("Ambassador revealed card:" + origCard.getName());

        int returnCount = -1;
        if (!pile.isSupply()) {
            Util.playerError(currentPlayer, "Ambassador revealed card not in supply, returning 0.");
        } else {
            returnCount = currentPlayer.controlPlayer.ambassador_returnToSupplyFromHand(context, origCard);
            if (returnCount > 2) {
            	Util.playerError(currentPlayer, "Ambassador return to supply error (more than 2 cards), returning 2.");
            	returnCount = 2;
            } else {
            	int inHandCount = currentPlayer.inHandCount(origCard);
            	if (returnCount > inHandCount) {
            		Util.playerError(currentPlayer, "Ambassador return to supply error (more than cards in hand), returning " + inHandCount);
            		returnCount = inHandCount;
            	}
            }
        }

        for (int i = 0; i < returnCount; i++) {
        	int idx = currentPlayer.hand.indexOf(origCard);
        	if (idx > -1) {
                pile.addCard(currentPlayer.hand.remove(idx));
            } else {
                Util.playerError(currentPlayer, "Ambassador return to supply error, just returning those available.");
                break;
            }
        }

        if (returnCount > -1) {
	        for (Player player : game.getPlayersInTurnOrder()) {
	            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
	                player.attacked(this.controlCard, context);
	                
	                
	                if (pile.getType() == AbstractCardPile.PileType.SingleCardPile || origCard.equals(pile.card()) ) {
	                	player.gainNewCard(virtCard, this.controlCard, new MoveContext(game, player));
	                }
	            }
	        }
        }
    }

    private void ghostShip(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
//                MoveContext playerContext = new MoveContext(game, player);

                if (player.hand.size() >= 4) {
                    Card[] cards = player.controlPlayer.ghostShip_attack_cardsToPutBackOnDeck(context);
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

        Card card = currentPlayer.controlPlayer.salvager_cardToTrash(context);

        if (card == null || !currentPlayer.hand.contains(card)) {
            Util.playerError(currentPlayer, "Salvager trash error, trashing first card.");
            card = currentPlayer.hand.get(0);
        }

        context.addGold += card.getCost(context);
        currentPlayer.hand.remove(card);
        currentPlayer.trash(card, this.controlCard, context);
    }

    private void bishop(Game game, MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.bishop_cardToTrashForVictoryTokens(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Bishop trash error, trashing a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.controlCard, context);
            currentPlayer.addVictoryTokens(context, card.getCost(context) / 2);
        }

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer) {
                Card card = (player).controlPlayer.bishop_cardToTrash(context);

                if (card != null && player.hand.contains(card)) {
                    player.hand.remove(card);
                    player.trash(card, this.controlCard, new MoveContext(game, player));
                }
            }
        }
    }

    private void city(Game game, MoveContext context, Player currentPlayer) {
        if (game.emptyPiles() > 0) {
            game.drawToHand(currentPlayer, this.controlCard);
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
                    currentPlayer.reveal(card, this.controlCard, context);
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

        Card card = currentPlayer.controlPlayer.expand_cardToTrash(context);
        if (card == null || !currentPlayer.hand.contains(card)) {
            Util.playerError(currentPlayer, "Expand trash error, expanding a random card.");
            card = Util.randomCard(currentPlayer.hand);
        }

        int maxCost = card.getCost(context) + 3;
        boolean potion = card.costPotion();
        currentPlayer.hand.remove(card);
        currentPlayer.trash(card, this.controlCard, context);

        card = currentPlayer.controlPlayer.expand_cardToObtain(context, maxCost, potion);
        if (card != null) {
            if (card.getCost(context) > maxCost) {
                Util.playerError(currentPlayer, "Expand error, new card costs too much.");
            } else if(card.costPotion() && !potion) {
                Util.playerError(currentPlayer, "Expand error, new card costs potion and trashed card does not.");
            } else {
                if(!currentPlayer.gainNewCard(card, this.controlCard, context)) {
                    Util.playerError(currentPlayer, "Expand error, pile is empty or card is not in the game.");
                }
            }
        }
    }

    private void forge(MoveContext context, Player currentPlayer) {
        int totalCost = 0;
        Card[] cards = currentPlayer.controlPlayer.forge_cardsToTrash(context);

        if (cards != null) {
            for (Card card : cards) {
                if (card != null && currentPlayer.hand.contains(card)) {
                    totalCost += card.getCost(context);
                    currentPlayer.hand.remove(card);
                    currentPlayer.trash(card, this.controlCard, context);
                }
            }
        }

        Card card = currentPlayer.controlPlayer.forge_cardToObtain(context, totalCost);
        if (card != null) {
            if (card.getCost(context) != totalCost || card.costPotion() || !Cards.isSupplyCard(card) || card.equals(Cards.curse)) {
                Util.playerError(currentPlayer, "Forge returned invalid card, ignoring.");
            } else {
                if(!currentPlayer.gainNewCard(card, this.controlCard, context)) {
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
        	ActionCardImpl cardToPlay = null;
        	switch (this.type) {
        	case ThroneRoom:
                cardToPlay = (ActionCardImpl) currentPlayer.controlPlayer.throneRoom_cardToPlay(context);
        		break;
        	case KingsCourt:
        		cardToPlay = (ActionCardImpl) currentPlayer.controlPlayer.kingsCourt_cardToPlay(context);
        		break;
        	case Procession:
        		cardToPlay = (ActionCardImpl) currentPlayer.controlPlayer.procession_cardToPlay(context);
        		break;
			default:
				break;
        	}

            if (cardToPlay != null) {
                if(!actionCards.contains(cardToPlay)) {
                    Util.playerError(currentPlayer, this.controlCard.name.toString() + " card selection error, ignoring");
                } else {
                    context.freeActionInEffect++;

                    cardToPlay.cloneCount = (equals(Cards.kingsCourt) ? 3 : 2);
                    for (int i = 0; i < cardToPlay.cloneCount;) {
                        cardToPlay.numberTimesAlreadyPlayed = i++;
                        cardToPlay.play(game, context, cardToPlay.numberTimesAlreadyPlayed == 0 ? true : false);
                    }

                    cardToPlay.numberTimesAlreadyPlayed = 0;
                    context.freeActionInEffect--;
                    // If the cardToPlay was a knight, and was trashed, reset clonecount
                    if (cardToPlay.isKnight() && !currentPlayer.playedCards.contains(cardToPlay) && game.trashPile.contains(cardToPlay)) {
                    	cardToPlay.cloneCount = 1;
                    }

                    if (cardToPlay instanceof DurationCard && !cardToPlay.equals(Cards.tactician)) {
                        // Need to move throning card to NextTurnCards first
                        // (but does not play)
                        if (!this.controlCard.movedToNextTurnPile) {
                            this.controlCard.movedToNextTurnPile = true;
                            int idx = currentPlayer.playedCards.lastIndexOf(this.controlCard);
                            int ntidx = currentPlayer.nextTurnCards.size() - 1;
                            if (idx >= 0 && ntidx >= 0) {
                            	currentPlayer.playedCards.remove(idx);
                                currentPlayer.nextTurnCards.add(ntidx, this.controlCard);
                            }
                        }
                    }
                }

                if (this.type == Cards.Type.Procession) {
                	if (!cardToPlay.trashOnUse) {
                		currentPlayer.trash(cardToPlay, this.controlCard, context);
                		if (currentPlayer.playedCards.getLastCard() == cardToPlay) { 
                			currentPlayer.playedCards.remove(cardToPlay);
                		} 
                		if (currentPlayer.nextTurnCards.contains(cardToPlay)) { 
                			((CardImpl) cardToPlay).trashAfterPlay = true;
                		}
                	}

                	Card cardToGain = currentPlayer.controlPlayer.procession_cardToGain(context, 1 + cardToPlay.getCost(context), cardToPlay.costPotion());
                	if ((cardToGain != null) && (cardToPlay.getCost(context) + 1) == cardToGain.getCost(context)) {
                		currentPlayer.gainNewCard(cardToGain, this.controlCard, context);
                	}
                }
            }
        }
    }

    private void mint(MoveContext context, Player currentPlayer) {
        TreasureCard cardToMint = currentPlayer.controlPlayer.mint_treasureToMint(context);

        if (cardToMint != null && !currentPlayer.hand.contains(cardToMint)) {
            Util.playerError(currentPlayer, "Mint treasure selection error, not minting anything.");
        }

        if (cardToMint != null) {
            currentPlayer.reveal(cardToMint, this.controlCard, context);
            currentPlayer.gainNewCard(cardToMint, this.controlCard, context);
        }
    }

    private void mountebank(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);

                Card curseCard = null;
                for (Card card : player.hand) {
                    if (Cards.curse.equals(card)) {
                        curseCard = card;
                        break;
                    }
                }

                if (curseCard != null && (player).controlPlayer.mountebank_attack_shouldDiscardCurse(playerContext)) {
                    player.hand.remove(curseCard);
                    player.discard(curseCard, this.controlCard, playerContext);
                } else {
                    player.gainNewCard(Cards.curse, this.controlCard, playerContext);
                    player.gainNewCard(Cards.copper, this.controlCard, playerContext);
                }
            }
        }
    }

    private void rabble(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);

                ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
                List<Card> cardToDiscard = new ArrayList<Card>();

                for (int i = 0; i < 3; i++) {
                    Card card = game.draw(player);
                    if (card != null) {
                    player.reveal(card, this.controlCard, playerContext);

                    if (card instanceof TreasureCard || card instanceof ActionCard) {
                    	cardToDiscard.add(card);
                    } else {
                        topOfTheDeck.add(card);
                    }
                }
                }
                for (Card c: cardToDiscard) {
                	player.discard(c, this.controlCard, playerContext);
                }


                if (!topOfTheDeck.isEmpty()) {
                    Card[] order = (player).controlPlayer.rabble_attack_cardOrder(playerContext, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));

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
            Card card = currentPlayer.controlPlayer.tradeRoute_cardToTrash(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Trade Route card selection error, no card selected or card not in hand, choosing random card to trash");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.controlCard, context);
        }

        context.addGold += game.tradeRouteValue;
    }

    private void vault(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.vault_cardsToDiscardForGold(context);
        if (cards != null) {
            int numberOfCardsDiscarded = 0;
            for (Card card : cards) {
                if (currentPlayer.hand.remove(card)) {
                    currentPlayer.discard(card, this.controlCard, context);
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
                cards = (player).controlPlayer.vault_cardsToDiscardForCard(context);

                if (cards != null) {
                    int numberOfCardsDiscarded = 0;

                    if (cards.length > 1 || player.hand.size() == 1) {
                        for (Card card : cards) {
                            if (numberOfCardsDiscarded < 2 && player.hand.remove(card)) {
                                player.discard(card, this.controlCard, null);
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
                        game.drawToHand(player, this.controlCard);
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
            currentPlayer.reveal(c, this.controlCard, context);
            currentPlayer.discard(c, this.controlCard, context);
        }

        if (draw != null) {
            currentPlayer.reveal(draw, this.controlCard, context);
            currentPlayer.hand.add(draw);
        }
    }

    private void fortuneTeller(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);

                ArrayList<Card> cardsToDiscard = new ArrayList<Card>();

                Card draw = null;
                while ((draw = game.draw(player)) != null && !(draw instanceof VictoryCard) && !(draw instanceof CurseCard)) {
                    player.reveal(draw, this.controlCard, playerContext);
                    cardsToDiscard.add(draw);
                }

                if (draw != null) {
                    player.reveal(draw, this.controlCard, playerContext);
                    player.putOnTopOfDeck(draw);
                }

                for(Card card : cardsToDiscard) {
                    player.discard(card, this.controlCard, playerContext);
                }
            }
        }
    }

    private void hamlet(MoveContext context, Player currentPlayer) {
        Card forAction = currentPlayer.controlPlayer.hamlet_cardToDiscardForAction(context);
        if (forAction != null) {
            currentPlayer.hand.remove(forAction);
            currentPlayer.discard(forAction, this.controlCard, context);
            context.actions++;
        }

        Card forBuy = currentPlayer.controlPlayer.hamlet_cardToDiscardForBuy(context);
        if (forBuy != null) {
            currentPlayer.hand.remove(forBuy);
            currentPlayer.discard(forBuy, this.controlCard, context);
            context.buys++;
        }
    }

    private void harvest(Game game, MoveContext context, Player currentPlayer) {
        HashSet<String> cardNames = new HashSet<String>();
        List<Card> cardToDiscard = new ArrayList<Card>();
        for (int i = 0; i < 4; i++) {
            Card draw = game.draw(currentPlayer);
            if (draw == null) {
                break;
            }

            cardNames.add(draw.getName());
            currentPlayer.reveal(draw, this.controlCard, context);
            cardToDiscard.add(draw);
        }
        for (Card c: cardToDiscard) {
            currentPlayer.discard(c, this.controlCard, context);
        }

        context.addGold += cardNames.size();
    }

    private void horseTraders(MoveContext context, Player currentPlayer) {
        Card[] cardsToDiscard = currentPlayer.controlPlayer.horseTraders_cardsToDiscard(context);
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
            currentPlayer.discard(card, this.controlCard, null);
        }
    }

    private void jester(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this.controlCard)) {
                targetPlayer.attacked(this.controlCard, context);

                Card draw = game.draw(targetPlayer);
                if (draw == null) {
                    continue;
                }

                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetPlayer.reveal(draw, this.controlCard, targetContext);
                targetPlayer.discard(draw, this.controlCard, targetContext);

                MoveContext toGainContext = null;

                if (draw instanceof VictoryCard) {
                    targetPlayer.gainNewCard(Cards.curse, this.controlCard, targetContext);
                } else {
                    if (!game.isPileEmpty(draw)) {
                        JesterOption option = currentPlayer.controlPlayer.controlPlayer.jester_chooseOption(context, targetPlayer, draw);
                        toGainContext = JesterOption.GainCopy.equals(option) ? context : targetContext;
                        toGainContext.getPlayer().gainNewCard(draw, this.controlCard, toGainContext);
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
            currentPlayer.revealFromHand(card, this.controlCard, context);
        }

        ArrayList<Card> toDiscard = new ArrayList<Card>();

        Card draw = null;
        while ((draw = game.draw(currentPlayer)) != null && cardNames.contains(draw.getName())) {
            currentPlayer.reveal(draw, this.controlCard, context);
            toDiscard.add(draw);
        }

        if (draw != null) {
            currentPlayer.reveal(draw, this.controlCard, context);
            currentPlayer.hand.add(draw);
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.controlCard, null);
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
            game.drawToHand(currentPlayer, this.controlCard);
        }

        int crossroadsPlayed = this.controlCard.numberTimesAlreadyPlayed;
        crossroadsPlayed += context.countCardsInPlay(Cards.crossroads);

        if (crossroadsPlayed <= 1) {
            context.actions += 3;
        }
    }

    private void trustySteed(Game game, MoveContext context, Player currentPlayer) {
        TrustySteedOption[] options = currentPlayer.controlPlayer.trustySteed_chooseOptions(context);
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
                        game.drawToHand(currentPlayer, this.controlCard);
                    }
                } else if (option == TrustySteedOption.AddGold) {
                    context.addGold += 2;
                } else if (option == TrustySteedOption.GainSilvers) {
                    for (int i = 0; i < 4; i++) {
                        if(!currentPlayer.gainNewCard(Cards.silver, this.controlCard, context)) {
                            break;
                        }
                    }

                    while (currentPlayer.getDeckSize() > 0) {
                    	currentPlayer.discard(currentPlayer.deck.remove(0), this.controlCard, null);
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
                boolean discard = (targetPlayer).controlPlayer.duchess_shouldDiscardCardFromTopOfDeck(targetPlayerContext, c);
                if(discard) {
                    targetPlayer.discard(c, this.controlCard, targetPlayerContext);
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
            TreasureCard treasure = currentPlayer.controlPlayer.spiceMerchant_treasureToTrash(context);
            if(treasure != null) {
                if(!currentPlayer.hand.contains(treasure)) {
                    Util.playerError(currentPlayer, "Spice Merchant returned invalid card to trash from hand, ignoring.");
                }
                else {
                    currentPlayer.hand.remove(treasure);
                    currentPlayer.trash(treasure, this.controlCard, context);

                    SpiceMerchantOption option = currentPlayer.controlPlayer.spiceMerchant_chooseOption(context);
                    if(option == SpiceMerchantOption.AddCardsAndAction) {
                        game.drawToHand(currentPlayer, this.controlCard);
                        game.drawToHand(currentPlayer, this.controlCard);
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
            Card card = currentPlayer.controlPlayer.trader_cardToTrash(context);
            if(card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Trader card to trash invalid, picking one");
                card = currentPlayer.hand.get(0);
            }

            int cost = card.getCost(context);
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.controlCard, context);
            for(int i=0; i < cost; i++) {
                if(!currentPlayer.gainNewCard(Cards.silver, this.controlCard, context)) {
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
            Card[] cardsToDiscard = currentPlayer.controlPlayer.cartographer_cardsFromTopOfDeckToDiscard(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
            if(cardsToDiscard != null) {
                for(Card toDiscard : cardsToDiscard) {
                    if(topOfTheDeck.remove(toDiscard)) {
                        currentPlayer.discard(toDiscard, this.controlCard, null);
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
            currentPlayer.reveal(cards[i], this.controlCard, context);
            currentPlayer.discard(cards[i], this.controlCard, null);
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
            currentPlayer.reveal(cards[i], this.controlCard, context);
            currentPlayer.discard(cards[i], this.controlCard, null);
        }
    }

    private void mandarin(MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card toTopOfDeck = currentPlayer.controlPlayer.mandarin_cardToReplace(context);

            if (toTopOfDeck == null) {
                Util.playerError(currentPlayer, "No card selected for Mandarin, returning random card to the top of the deck.");
                toTopOfDeck = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.reveal(toTopOfDeck, this.controlCard, context);
            currentPlayer.hand.remove(toTopOfDeck);
            currentPlayer.putOnTopOfDeck(toTopOfDeck);
        }
    }

    private void possession(MoveContext context) {
        // TODO: Temp hack to prevent AI from playing possession, even though human player can, since it only half works
        //       (AI will make decisions while possessed, but will try to make "good" ones)
        if (context.player.isPossessed()) {
            context.game.nextPossessionsToProcess++;
            context.game.nextPossessingPlayer = context.player;
        } else {
            context.game.possessionsToProcess++;
            context.game.possessingPlayer = context.player;
        }
    }

    private void steward(Game game, MoveContext context, Player currentPlayer) {
        Player.StewardOption option = currentPlayer.controlPlayer.steward_chooseOption(context);

        if (option == null) {
            Util.playerError(currentPlayer, "Steward option error, ignoring.");
        } else {
            if (option == Player.StewardOption.AddGold) {
                context.addGold += 2;
            } else if (option == Player.StewardOption.AddCards) {
                game.drawToHand(currentPlayer, this.controlCard);
                game.drawToHand(currentPlayer, this.controlCard);
            } else if (option == Player.StewardOption.TrashCards) {
                CardList hand = currentPlayer.getHand();
                if (hand.size() == 0) {
                    return;
                }

                Card[] cards = currentPlayer.controlPlayer.steward_cardsToTrash(context);
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
                    currentPlayer.trash(card, this.controlCard, context);
                }
            }
        }
    }

    private void thief(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<TreasureCard> trashed = new ArrayList<TreasureCard>();

        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this.controlCard)) {
                targetPlayer.attacked(this.controlCard, context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                ArrayList<TreasureCard> treasures = new ArrayList<TreasureCard>();

                List<Card> cardsToDiscard = new ArrayList<Card>();
                for (int i = 0; i < 2; i++) {
                    Card card = game.draw(targetPlayer);

                    if (card != null) {
                        targetPlayer.reveal(card, this.controlCard, targetContext);

                        if (card instanceof TreasureCard) {
                            treasures.add((TreasureCard) card);
                        } else {
                        	cardsToDiscard.add(card);
                        }
                    }
                }

                for (Card c: cardsToDiscard) {
                    targetPlayer.discard(c, this.controlCard, targetContext);
                }

                TreasureCard cardToTrash = null;

                if (treasures.size() == 1) {
                    cardToTrash = treasures.get(0);
                } else if (treasures.size() == 2) {
                    if (treasures.get(0).equals(treasures.get(1))) {
                        cardToTrash = treasures.get(0);
                        targetPlayer.discard(treasures.remove(1), this.controlCard, targetContext);
                    } else {
                        cardToTrash = currentPlayer.controlPlayer.thief_treasureToTrash(context, treasures.toArray(new TreasureCard[] {}));
                    }

                    for (TreasureCard treasure : treasures) {
                        if (!treasure.equals(cardToTrash)) {
                            targetPlayer.discard(treasure, this.controlCard, targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.controlCard, targetContext);
                    trashed.add(cardToTrash);
                }
            }
        }

        if (trashed.size() > 0) {
            TreasureCard[] treasuresToGain = currentPlayer.controlPlayer.thief_treasuresToGain(context, trashed.toArray(new TreasureCard[] {}));

            if (treasuresToGain != null) {
                for (TreasureCard treasure : treasuresToGain) {
                    currentPlayer.gainCardAlreadyInPlay(treasure, this.controlCard, context);
                    game.trashPile.remove(treasure);
                }
            }
        }
    }

    private void militia(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.militia_attack_cardsToKeep(context);
                    player.discardRemainingCardsFromHand(context, cardsToKeep, this.controlCard, keepCardCount);
                }
            }
        }
    }

    private void remodel(MoveContext context, Player currentPlayer) {
        if(currentPlayer.getHand().size() > 0) {
            Card cardToTrash = currentPlayer.controlPlayer.remodel_cardToTrash(context);

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

                    currentPlayer.trash(playersCard, this.controlCard, context);
                    break;
                }
            }

            if (cost == -1) {
                Util.playerError(currentPlayer, "Remodel returned invalid card, ignoring.");
                return;
            }

            cost += 2;

            Card card = currentPlayer.controlPlayer.remodel_cardToObtain(context, cost, potion);
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
                    if(!currentPlayer.gainNewCard(card, this.controlCard, context)) {
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

            Card card = currentPlayer.controlPlayer.remake_cardToTrash(context);
            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Remake trash error, remaking a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            int value = card.getCost(context) + 1;
            boolean potion = card.costPotion();
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.controlCard, context);

            card = currentPlayer.controlPlayer.remake_cardToObtain(context, value, potion);
            if (card != null) {
                if (card.getCost(context) != value || card.costPotion() != potion) {
                    Util.playerError(currentPlayer, "Remake error, new card must cost exactly " + value + ".");
                } else {
                    if(!currentPlayer.gainNewCard(card, this.controlCard, context)) {
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
            currentPlayer.reveal(card, this.controlCard, context);
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
    	boolean currentPlayerRevealed = false;

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
    				currentPlayerRevealed = currentPlayer.controlPlayer.tournament_shouldRevealProvince(context);
    				if (currentPlayerRevealed) {
    					currentPlayer.reveal(province, this.controlCard, context);
    					player.hand.remove(province);
    					currentPlayer.discard(province, this.controlCard, context);
    				}
    			} else {
                    if (player.controlPlayer.tournament_shouldRevealProvince(context)) {
    					player.reveal(province, this.controlCard, new MoveContext(game, player));
    					opponentsRevealedProvince = true;
    				}
    			}
    		}
    	}

    	if (currentPlayerRevealed) {
    		TournamentOption option = currentPlayer.controlPlayer.tournament_chooseOption(context);

    		if (option == TournamentOption.GainPrize) {
    			Card prize = currentPlayer.controlPlayer.tournament_choosePrize(context);
    			if (prize != null && prize.isPrize()) {
    				currentPlayer.gainNewCard(prize, this.controlCard, context);
    			} else {
    				Util.playerError(currentPlayer, "Tournament error, invalid prize");
    			}
    		} else if (option == TournamentOption.GainDuchy) {
    			currentPlayer.gainNewCard(Cards.duchy, this.controlCard, context);
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
        if (currentPlayer.getHand().size() == 0) {
            return;
        }
        ArrayList<Card> handCopy = Util.copy(currentPlayer.getHand());
        Card[] cardsToTrash = currentPlayer.controlPlayer.tradingPost_cardsToTrash(context);
        // Trash forced, pick cards randomly if not selected
        boolean bad = false;
        if (cardsToTrash == null) {
            bad = true;
        } else if (handCopy.size() < 2 && cardsToTrash.length != handCopy.size()) {
            bad = true;
        } else if (handCopy.size() >= 2 && cardsToTrash.length != 2) {
            bad = true;
        } else {
            ArrayList<Card> copyForTrash = Util.copy(currentPlayer.getHand());
            for (Card cardToKeep : cardsToTrash) {
                if (!copyForTrash.remove(cardToKeep)) {
                    bad = true;
                    break;
                }
            }
        }

        if (bad) {
            if (handCopy.size() >= 2) {
                Util.playerError(currentPlayer, "TradingPost trash error, just trashing the first 2.");
            }
            cardsToTrash = new Card[Math.min(2, handCopy.size())];
            for (int i = 0; i < cardsToTrash.length; i++) {
                cardsToTrash[i] = handCopy.get(i);
            }
        }

        for (int i = cardsToTrash.length - 1; i >= 0 ; i--) {
            currentPlayer.hand.remove(cardsToTrash[i]);
            currentPlayer.trash(cardsToTrash[i], this.controlCard, context);
        }
        if (cardsToTrash.length == 2) {
            currentPlayer.gainNewCard(Cards.silver, this.controlCard, context);
		}
    }

    private void masquerade(Game game, MoveContext context, Player currentPlayer) {
        Card[] passedCards = new Card[Game.players.length];

        for (int i = 0; i < Game.players.length; i++) {
            Player player = Game.players[i];
            if (player.getHand().size() == 0) {
                continue;
            }
            Card card = player.controlPlayer.masquerade_cardToPass(new MoveContext(game, player));
            if (card == null || !(player).hand.contains(card)) {
                Util.playerError(player, "Masquerade pass card error, picking random card to pass.");
                card = Util.randomCard(player.getHand());
            }

            // TODO Should this.controlCard send some new type of event, not trashed, but passed maybe?
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
            	nextPlayer.hand.add(card);
            	if (nextPlayer instanceof GameEventListener) {
	                GameEvent event = new GameEvent(GameEvent.Type.CardObtained, new MoveContext(game, nextPlayer));
	                event.card = card;
	                event.responsible = this.controlCard;
	                event.newCard = false;
	                ((GameEventListener) nextPlayer).gameEvent(event);
            	}

                // nextPlayer.gainCardAlreadyInPlay(card, this.controlCard, new MoveContext(game, nextPlayer));
            }
        }

        Card toTrash = currentPlayer.controlPlayer.masquerade_cardToTrash(context);
        if (toTrash != null) {
            if (currentPlayer.hand.contains(toTrash)) {
                currentPlayer.hand.remove(toTrash);

                currentPlayer.trash(toTrash, this.controlCard, context);
            } else {
                Util.playerError(currentPlayer, "Masquerade trash error, card not in hand, ignoring.");
            }
        }
    }

    private void miningVillage(MoveContext context, Player currentPlayer) {
        if (!this.controlCard.movedToNextTurnPile) {
            if (currentPlayer.controlPlayer.miningVillage_shouldTrashMiningVillage(context)) {
                context.addGold += 2;
                this.controlCard.movedToNextTurnPile = true;
                currentPlayer.trash(this.controlCard, null, context);
                currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf(this.controlCard));
            }
        }
    }

    private void minion(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Player> playersToAttack = new ArrayList<Player>();
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player == currentPlayer || !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                playersToAttack.add(player);
                if (player != currentPlayer) {
                    player.attacked(this.controlCard, context);
                }
            }
        }

        Player.MinionOption option = currentPlayer.controlPlayer.minion_chooseOption(context);

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
                        player.discard(player.hand.remove(0), this.controlCard, targetContext);
                    }

                    game.drawToHand(player, this.controlCard);
                    game.drawToHand(player, this.controlCard);
                    game.drawToHand(player, this.controlCard);
                    game.drawToHand(player, this.controlCard);
                }
            }
        }
    }

    private void pawn(Game game, MoveContext context, Player currentPlayer) {
        Player.PawnOption[] options = currentPlayer.controlPlayer.pawn_chooseOptions(context);
        if (options == null || options.length != 2 || options[0] == options[1]) {
            Util.playerError(currentPlayer, "Pawn options error, ignoring.");
        } else {
            for (Player.PawnOption option : options) {
                if (option == Player.PawnOption.AddAction) {
                    context.actions++;
                } else if (option == Player.PawnOption.AddBuy) {
                    context.buys++;
                } else if (option == Player.PawnOption.AddCard) {
                    game.drawToHand(currentPlayer, this.controlCard);
                } else if (option == Player.PawnOption.AddGold) {
                    context.addGold++;
                }
            }
        }
    }

    private void saboteur(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.cardCostModifier = context.cardCostModifier;

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

                        player.trash(draw, this.controlCard, playerContext);

                        Card card = (player).controlPlayer.saboteur_cardToObtain(playerContext, value, potion);
                        if (card != null) {
                            if (card.getCost(context) > value || (card.costPotion() && !potion) || !Cards.isSupplyCard(card)) {
                                Util.playerError(currentPlayer, "Saboteur obtain error, ignoring.");
                            }
                            else {
                                if(!player.gainNewCard(card, this.controlCard, playerContext)) {
                                    Util.playerError(currentPlayer, "Saboteur obtain error, ignoring.");
                                }
                            }
                        }

                        break;
                    } else {
                        player.reveal(draw, this.controlCard, playerContext);
                        toDiscard.add(draw);
                    }
                }

                while (!toDiscard.isEmpty()) {
                    player.discard(toDiscard.remove(0), this.controlCard, null);
                }
            }
        }
    }

    private void shantyTown(Game game, MoveContext context, Player currentPlayer) {
        boolean actions = false;
        for (Card card : currentPlayer.hand) {
            currentPlayer.reveal(card, this.controlCard, context);

            if (card instanceof ActionCard) {
                actions = true;
            }
        }

        if (!actions) {
            game.drawToHand(currentPlayer, this.controlCard);
            game.drawToHand(currentPlayer, this.controlCard);
        }
    }

    private void baron(MoveContext context, Player currentPlayer) {
        boolean discard = false;
        for (Card cardToCheck : currentPlayer.hand) {
            if (cardToCheck.equals(Cards.estate)) {
                discard = currentPlayer.controlPlayer.baron_shouldDiscardEstate(context);
                break;
            }
        }

        if (discard) {
            Card card = currentPlayer.hand.get(Cards.estate);
            currentPlayer.hand.remove(Cards.estate);
            currentPlayer.discard(card, this.controlCard, null);
            context.addGold += 4;
        } else {
            currentPlayer.gainNewCard(Cards.estate, this.controlCard, context);
        }
    }

    private void courtyard(MoveContext context, Player currentPlayer) {
        // TODO do this.controlCard check at the top of the block for EVERY Util...
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.courtyard_cardToPutBackOnDeck(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Courtyard error, just putting back a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.putOnTopOfDeck(currentPlayer.hand.remove(currentPlayer.hand.indexOf(card)));
        }
    }

    private void spyAndScryingPool(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            // Note that this.controlCard is the opposite check of other attacks, the spy/scrying pool lets
            // the current player look at their own deck which is a good thing, so always
            // allow that
            if (player == currentPlayer || (!Util.isDefendedFromAttack(game, player, this.controlCard))) {
                if (player != currentPlayer) {
                    player.attacked(this.controlCard, context);
                }

                MoveContext playerContext = new MoveContext(game, player);
                Card card = game.draw(player);

                if (card != null) {
                    player.reveal(card, this.controlCard, playerContext);

                    boolean discard = false;

                    if(equals(Cards.spy)) {
                        discard = currentPlayer.controlPlayer.spy_shouldDiscard(context, player, card);
                    } else if (equals(Cards.scryingPool)) {
                        discard = currentPlayer.controlPlayer.scryingPool_shouldDiscard(context, player, card);
                    }

                    if (discard) {
                        player.discard(card, this.controlCard, playerContext);
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
                currentPlayer.reveal(draw, this.controlCard, new MoveContext(game, currentPlayer));
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

    private void conspirator(Game game, MoveContext context, Player currentPlayer) {
        if (context.actionsPlayedSoFar >= 3) {
            context.actions++;
            game.drawToHand(currentPlayer, this.controlCard);
        }
    }

    private void wishingWell(Game game, MoveContext context, Player currentPlayer) {
    	
    	if (currentPlayer.deck.size() > 0 || currentPlayer.discard.size() > 0) {  // Only allow a guess if there are cards in the deck or discard pile
			
    		// Create a list of possible cards to guess, using the player's hand, discard pile, and deck 
    		// (even though the player could technically name a card he doesn't have)
			ArrayList<Card> options = currentPlayer.getAllCards();
			Collections.sort(options, new Util.CardNameComparator());

			if (!options.isEmpty()) {
				Card card = currentPlayer.controlPlayer.wishingWell_cardGuess(context, options);
				currentPlayer.controlPlayer.namedCard(card, this.controlCard, context);
		        Card draw = game.draw(currentPlayer);
		        if (card != null && draw != null) {
		            currentPlayer.reveal(draw, this.controlCard, context);
		
		            if (card.equals(draw)) {
		                currentPlayer.hand.add(draw);
		            } else {
		                currentPlayer.putOnTopOfDeck(draw);
		            }
		        }
			}
		}
    }

    private void upgrade(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.upgrade_cardToTrash(context);
            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Upgrade trash error, upgrading a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            int value = card.getCost(context) + 1;
            boolean potion = card.costPotion();
            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.controlCard, context);

            card = currentPlayer.controlPlayer.upgrade_cardToObtain(context, value, potion);
            if (card != null) {
                if (card.getCost(context) != value || card.costPotion() != potion) {
                    Util.playerError(currentPlayer, "Upgrade error, new card does not cost value of the old card +1.");
                } else {
                    if(!currentPlayer.gainNewCard(card, this.controlCard, context)) {
                        Util.playerError(currentPlayer, "Upgrade error, pile is empty or card is not in the game.");
                    }
                }
            }
        }
    }

    private void ironworks(Game game, MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.ironworks_cardToObtain(context);
        if (card != null && card.getCost(context) <= 4 && !card.costPotion()) {
            if (currentPlayer.gainNewCard(card, this.controlCard, context)) {
                if (card instanceof ActionCard) {
                    context.actions++;
                }
                if (card instanceof TreasureCard) {
                    context.addGold++;
                }
                if (card instanceof VictoryCard) {
                    game.drawToHand(currentPlayer, this.controlCard);
                }
            }
        }
    }

    private void nobles(Game game, MoveContext context, Player currentPlayer) {
        Player.NoblesOption option = currentPlayer.controlPlayer.nobles_chooseOptions(context);
        if (option == null) {
            Util.playerError(currentPlayer, "Nobles option error, ignoring.");
        } else {
            if (option == Player.NoblesOption.AddActions) {
                context.actions += 2;
            } else if (option == Player.NoblesOption.AddCards) {
                game.drawToHand(currentPlayer, this.controlCard);
                game.drawToHand(currentPlayer, this.controlCard);
                game.drawToHand(currentPlayer, this.controlCard);
            }
        }
    }

    private void tribute(Game game, MoveContext context, Player currentPlayer) {
        Card[] revealedCards = new Card[2];
        Player nextPlayer = game.getNextPlayer();
        revealedCards[0] = game.draw(nextPlayer);
        revealedCards[1] = game.draw(nextPlayer);

        if (revealedCards[0] != null) {
            nextPlayer.reveal(revealedCards[0], this.controlCard, new MoveContext(game, nextPlayer));
            (nextPlayer).discard(revealedCards[0], this.controlCard, null);

        }
        if (revealedCards[1] != null) {
            nextPlayer.reveal(revealedCards[1], this.controlCard, new MoveContext(game, nextPlayer));
            (nextPlayer).discard(revealedCards[1], this.controlCard, null);
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
                    game.drawToHand(currentPlayer, this.controlCard);
                    game.drawToHand(currentPlayer, this.controlCard);
                }
            }
        }
    }

    private void copperSmith(MoveContext context) {
        context.coppersmithsPlayed++;
    }

    private void swindler(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);

                Card draw = game.draw(player);
                if (draw != null) {
                    player.trash(draw, this.controlCard, playerContext);

                    Card card = currentPlayer.controlPlayer.swindler_cardToSwitch(context, draw.getCost(context), draw.costPotion());

                    boolean bad = false;
                    if (card == null) {
                        // Check that there are no cards that are possible to trade for...
                        for (Card thisCard : context.getCardsInGame()) {
                            if (Cards.isSupplyCard(thisCard) && !game.isPileEmpty(thisCard) && thisCard.getCost(context) == draw.getCost(context) && thisCard.costPotion() == draw.costPotion()) {
                                bad = true;
                                break;
                            }
                        }
                    } else if (!Cards.isSupplyCard(card) || game.isPileEmpty(card)  || card.getCost(context) != draw.getCost(context) || card.costPotion() != draw.costPotion()) {
                        bad = true;
                    }

                    if (bad) {
                        Util.playerError(currentPlayer, "Swindler swap card error, picking a random card.");

                        ArrayList<Card> possible = new ArrayList<Card>();
                        for (Card thisCard : context.getCardsInGame()) {
                            if (Cards.isSupplyCard(thisCard) && !game.isPileEmpty(thisCard) && thisCard.getCost(context) == draw.getCost(context) && thisCard.costPotion() == draw.costPotion()) {
                                possible.add(thisCard);
                            }
                        }

                        card = Util.randomCard(possible);
                    }

                    if (card != null) {
                        player.gainNewCard(card, this.controlCard, playerContext);
                    }
                }
            }
        }
    }

    private void goons(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.goons_attack_cardsToKeep(context);
                    player.discardRemainingCardsFromHand(context, cardsToKeep, this.controlCard, keepCardCount);
                }

            }
        }
    }

    private void youngwitch(Game game, MoveContext context, Player currentPlayer) {
        Card[] cardsToDiscard = currentPlayer.controlPlayer.youngWitch_cardsToDiscard(context);
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
            currentPlayer.discard(card, this.controlCard, null);
            currentPlayer.hand.remove(card);
        }

        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !isDefendedFromAttack(game, targetPlayer, this.controlCard)) {
                if (targetPlayer.hand.contains(game.baneCard) && game.pileSize(Cards.curse) > 0 && targetPlayer.revealBane(context)) {
                    targetPlayer.reveal(game.baneCard, this.controlCard, new MoveContext(game, targetPlayer));
                } else {
                    targetPlayer.attacked(this.controlCard, context);
                    targetPlayer.gainNewCard(Cards.curse, this.controlCard, new MoveContext(game, targetPlayer));
                }
            }
        }
    }

    private void followers(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.estate, this.controlCard, context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);
                player.gainNewCard(Cards.curse, this.controlCard, playerContext);

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.followers_attack_cardsToKeep(context);
                    player.discardRemainingCardsFromHand(context, cardsToKeep, this.controlCard, keepCardCount);
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
            TreasureCard toDiscard = currentPlayer.controlPlayer.stables_treasureToDiscard(context);

            // this.controlCard is optional, so ignore it if it's null or invalid
            if (toDiscard != null && currentPlayer.hand.contains(toDiscard)) {
                currentPlayer.hand.remove(toDiscard);
                currentPlayer.reveal(toDiscard, this.controlCard, context);
                currentPlayer.discard(toDiscard, this.controlCard, context);
                context.actions++;

                for (int i = 0; i < 3; i++) {
                    if(!game.drawToHand(currentPlayer, this.controlCard)) {
                        break;
                    }
                }
            }
        }
    }

    private void margrave(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                game.drawToHand(player, this.controlCard);

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.margrave_attack_cardsToKeep(context);
                    player.discardRemainingCardsFromHand(context, cardsToKeep, this.controlCard, keepCardCount);
                }
            }
        }
    }

    private void oracle(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer == currentPlayer || !Util.isDefendedFromAttack(game, targetPlayer, this.controlCard)) {
                targetPlayer.attacked(this.controlCard, context);
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
                    if(currentPlayer.controlPlayer.oracle_shouldDiscard(context, targetPlayer, cards)) {
                        for(Card c : cards) {
                            targetPlayer.discard(c, this.controlCard, targetContext);
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
            game.drawToHand(currentPlayer, this.controlCard);
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
            currentPlayer.reveal(cardToDiscard, this.controlCard, context);
            currentPlayer.discard(cardToDiscard, this.controlCard, null);
        }
    }

    private void jackOfAllTrades(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.silver, this.controlCard, context);

        Card c = game.draw(currentPlayer);
        if(c != null) {
            boolean discard = currentPlayer.controlPlayer.jackOfAllTrades_shouldDiscardCardFromTopOfDeck(context, c);
            if(discard) {
                currentPlayer.discard(c, this.controlCard, context);
            } else {
                currentPlayer.putOnTopOfDeck(c);
            }
        }

        while(currentPlayer.hand.size() < 5) {
            if(!game.drawToHand(currentPlayer, this.controlCard)) {
                break;
            }
        }

        Card cardToTrash = currentPlayer.controlPlayer.jackOfAllTrades_nonTreasureToTrash(context);
        if(cardToTrash != null) {
            if(!currentPlayer.hand.contains(cardToTrash) || cardToTrash instanceof TreasureCard) {
                Util.playerError(currentPlayer, "Jack of All Trades returned invalid card to trash from hand, ignoring.");
            }
            else {
                currentPlayer.hand.remove(cardToTrash);
                currentPlayer.trash(cardToTrash, this.controlCard, context);
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
            boolean trashedCardPotion = cardToTrash.costPotion();

            Card lowCardToGain = null;
            Card highCardToGain = null;

            if(context.isNewCardAvailable(trashedCardCost - 1, trashedCardPotion)) {
                lowCardToGain = currentPlayer.controlPlayer.develop_lowCardToGain(context, trashedCardCost - 1, trashedCardPotion);
                if (lowCardToGain == null) {
                	lowCardToGain = Util.randomCard(context.getAvailableCards(trashedCardCost - 1, trashedCardPotion));
                }
            }

            if(context.isNewCardAvailable(trashedCardCost + 1, trashedCardPotion)) {
                highCardToGain = currentPlayer.controlPlayer.develop_highCardToGain(context, trashedCardCost + 1, trashedCardPotion);
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
            currentPlayer.trash(cardToTrash, this.controlCard, context);

            boolean bad = false;

            if(cardsToGain.length == 0) {
                for(Card c : context.getCardsInGame()) {
                    if((c.getCost(context) == trashedCardCost - 1 || c.getCost(context) == trashedCardCost + 1) && context.getCardsLeftInPile(c) > 0) {
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

                    for(Card c : context.getCardsInGame()) {
                        if(c.getCost(context) == costToCheck && context.getCardsLeftInPile(c) > 0) {
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
                if(context.getCardsLeftInPile(c) == 0) {
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
                    currentPlayer.gainNewCard(c, this.controlCard, context);
                }
            }
        }
    }

    private void cellar(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.cellar_cardsToDiscard(context);
        if (cards != null) {
            int numberOfCards = 0;
            for (Card card : cards) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card playersCard = currentPlayer.hand.get(i);
                    if (playersCard.equals(card)) {
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.controlCard, context);
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
                game.drawToHand(currentPlayer, this.controlCard);
            }
        }
    }

    private void secretChamber(MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.secretChamber_cardsToDiscard(context);
        if (cards != null) {
            int numberOfCardsDiscarded = 0;
            for (Card card : cards) {
                if (currentPlayer.hand.remove(card)) {
                    currentPlayer.discard(card, this.controlCard, null);
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
        currentPlayer.gainNewCard(Cards.silver, this.controlCard, context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
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
                        player.reveal(card, this.controlCard, playerContext);
                    }
                } else {
                    VictoryCard toTopOfDeck = null;

                    if (victoryCards.size() == 1) {
                        toTopOfDeck = victoryCards.get(0);
                    } else {
                        toTopOfDeck = (player).controlPlayer.bureaucrat_cardToReplace(playerContext);

                        if (toTopOfDeck == null) {
                            Util.playerError(player, "No Victory Card selected for Bureaucrat, using first Victory Card in hand");
                            toTopOfDeck = victoryCards.get(0);
                        }
                    }

                    player.reveal(toTopOfDeck, this.controlCard, playerContext);
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

            currentPlayer.reveal(draw, this.controlCard, context);

            if (draw instanceof ActionCard && !draw.equals(Cards.golem)) {
                toOrder.add(draw);
                // currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.controlCard, null);
        }

        if (!toOrder.isEmpty()) {
            Card[] toPlay;

            if (toOrder.size() == 1) {
                toPlay = toOrder.toArray(new Card[toOrder.size()]);
            } else {
                ActionCard[] playOrder = currentPlayer.controlPlayer.golem_cardOrder(context, toOrder.toArray(new ActionCard[toOrder.size()]));

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

            context.freeActionInEffect++;context.golemInEffect++;
            for (Card card : toPlay) {
                ((ActionCardImpl) card).play(game, context, false);
            }
            context.freeActionInEffect--;context.golemInEffect--;
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
            currentPlayer.reveal(draw, this.controlCard, context);

            if (draw instanceof TreasureCard) {
                treasureCardsRevealed++;
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.controlCard, context);
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
                shouldKeep = currentPlayer.controlPlayer.library_shouldKeepAction(context, (ActionCard) draw);
            }

            if (shouldKeep) {
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.controlCard, null);
        }
    }

    private void chapel(MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.chapel_cardsToTrash(context);
        if (cards != null) {
            if (cards.length > 4) {
                Util.playerError(currentPlayer, "Chapel trash error, trying to trash too many cards, ignoring.");
            } else {
                for (Card card : cards) {
                    for (int i = 0; i < currentPlayer.hand.size(); i++) {
                        Card playersCard = currentPlayer.hand.get(i);
                        if (playersCard.equals(card)) {
                            Card thisCard = currentPlayer.hand.remove(i, false);
                            currentPlayer.trash(thisCard, this.controlCard, context);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void haven(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.haven_cardToSetAside(context);
        if ((card == null && hand(currentPlayer).size() > 0) || (card != null && !hand(currentPlayer).contains(card))) {
            Util.playerError(currentPlayer, "Haven set aside card error, setting aside the first card in hand.");
            card = hand(currentPlayer).get(0);
        }

        if (card != null) {
            hand(currentPlayer).remove(card);
            haven(currentPlayer).add(card);
        } else if (this.controlCard.cloneCount == 1) {
            currentPlayer.nextTurnCards.remove(this.controlCard);
            currentPlayer.playedCards.add(this.controlCard);
        }
    }

    private void pirateShip(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Player> playersToAttack = new ArrayList<Player>();
        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this.controlCard)) {
                playersToAttack.add(targetPlayer);
                targetPlayer.attacked(this.controlCard, context);
            }
        }

        if (currentPlayer.controlPlayer.pirateShip_takeTreasure(context)) {
            takePirateShipTreasure(context);
        } else {
            boolean treasureFound = false;
            for (Player targetPlayer : playersToAttack) {
                MoveContext targetContext = new MoveContext(game, targetPlayer);

                ArrayList<TreasureCard> treasures = new ArrayList<TreasureCard>();
                List<Card> cardToDiscard = new ArrayList<Card>();

                for (int i = 0; i < 2; i++) {
                    Card card = game.draw(targetPlayer);

                    if (card != null) {
                        targetPlayer.reveal(card, this.controlCard, targetContext);

                        if (card instanceof TreasureCard) {
                            treasures.add((TreasureCard) card);
                        } else {
                        	cardToDiscard.add(card);
                        }
                    }
                }
                for (Card c: cardToDiscard) {
                	targetPlayer.discard(c, this.controlCard, targetContext);
                }

                TreasureCard cardToTrash = null;

                if (treasures.size() == 1) {
                    cardToTrash = treasures.get(0);
                } else if (treasures.size() == 2) {
                    if (treasures.get(0).equals(treasures.get(1))) {
                        cardToTrash = treasures.get(0);
                        targetPlayer.discard(treasures.get(1), this.controlCard, targetContext);
                    } else {
                        cardToTrash = currentPlayer.controlPlayer.pirateShip_treasureToTrash(context, treasures.toArray(new TreasureCard[] {}));
                    }

                    for (TreasureCard treasure : treasures) {
                        if (!treasure.equals(cardToTrash)) {
                            targetPlayer.discard(treasure, this.controlCard, targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.controlCard, targetContext);
                    treasureFound = true;
                }
            }

            if (treasureFound) {
                increasePirateShipTreasure(currentPlayer);
            }
        }
    }

    private void smugglers(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.smugglers_cardToObtain(context);
        if (card != null) {
            if (card.getCost(context) > 6 || !Cards.isSupplyCard(card) || card.costPotion()) {
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
            if (!currentPlayer.gainNewCard(card, this.controlCard, context)) {
                // TODO do this.controlCard error output everywhere
                Util.playerError(currentPlayer, "Smugglers card error, no more cards left of that type, ignoring.");
            }
        }
    }

    private void feast(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.feast_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 5) {
                currentPlayer.gainNewCard(card, this.controlCard, context);
            }
        }
    }

    private void chancellor(Game game, MoveContext context, Player currentPlayer) {
        boolean discard = currentPlayer.controlPlayer.chancellor_shouldDiscardDeck(context);
        if (discard) {
            while (currentPlayer.getDeckSize() > 0) {
                currentPlayer.discard(game.draw(currentPlayer), this.controlCard, null, false);
            }
        }
    }

    private void moneyLender(MoveContext context, Player currentPlayer) {
        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            if (card.equals(Cards.copper)) {
                Card thisCard = currentPlayer.hand.remove(i);
                context.addGold += 3;
                currentPlayer.trash(thisCard, this.controlCard, context);
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
                sb.append("+" + addGold + " Coin");
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

    public void workshop(Player currentPlayer, MoveContext context) {
        Card card = currentPlayer.controlPlayer.workshop_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 4) {
                currentPlayer.gainNewCard(card, this.controlCard, context);
            }
        }
    }

    @Override
    public void isBought(MoveContext context) {
        switch (this.controlCard.getType()) {
        case NobleBrigand:
        	nobleBrigandAttack(context, false);
        	break;
        case Mint:
            for (Iterator<Card> it = context.player.playedCards.iterator(); it.hasNext();) {
                Card playedCard = it.next();
                if (playedCard instanceof TreasureCard) {
                    context.player.trash(playedCard, this.controlCard, context);
                    it.remove();
                }
            }
            break;
        default:
            break;
        }
    }

    public void nobleBrigandAttack(MoveContext moveContext, boolean defensible) {
        MoveContext context = moveContext;
        Player player = context.getPlayer();
        ArrayList<TreasureCard> trashed = new ArrayList<TreasureCard>();
        boolean[] gainCopper = new boolean[context.game.getPlayersInTurnOrder().length];

        int i = 0;
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            // Hinterlands card details in the rules states that noble brigand is not defensible when triggered from a buy
            if (targetPlayer != player && (!defensible || !Util.isDefendedFromAttack(context.game, targetPlayer, this.controlCard))) {
                targetPlayer.attacked(this.controlCard, moveContext);
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
                boolean treasureRevealed = false;
                ArrayList<TreasureCard> silverOrGold = new ArrayList<TreasureCard>();

                List<Card> cardsToDiscard = new ArrayList<Card>();
                for (int j = 0; j < 2; j++) {
                    Card card = context.game.draw(targetPlayer);
                    if(card == null) {
                        break;
                    }
                    targetPlayer.reveal(card, this.controlCard, targetContext);

                    if (card instanceof TreasureCard) {
                        treasureRevealed = true;
                    }

                    if(card.equals(Cards.silver) || card.equals(Cards.gold)) {
                        silverOrGold.add((TreasureCard) card);
                    } else {
                    	cardsToDiscard.add(card);
                    }
                }

                for (Card c: cardsToDiscard) {
                	targetPlayer.discard(c, this.controlCard, targetContext);
                }

                if(!treasureRevealed) {
                    gainCopper[i] = true;
                }

                TreasureCard cardToTrash = null;

                if (silverOrGold.size() == 1) {
                    cardToTrash = silverOrGold.get(0);
                } else if (silverOrGold.size() == 2) {
                    if (silverOrGold.get(0).equals(silverOrGold.get(1))) {
                        cardToTrash = silverOrGold.get(0);
                        targetPlayer.discard(silverOrGold.get(1), this.controlCard, targetContext);
                    } else {
                        cardToTrash = (player).controlPlayer.nobleBrigand_silverOrGoldToTrash(moveContext, silverOrGold.toArray(new TreasureCard[] {}));
                        for (TreasureCard c : silverOrGold) {
                            if (!c.equals(cardToTrash)) {
                                targetPlayer.discard(c, this.controlCard, targetContext);
                            }
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.controlCard, targetContext);
                    trashed.add(cardToTrash);
                }
            }
            i++;
        }

        i = 0;
        for(Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            if(gainCopper[i]) {
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
                targetPlayer.gainNewCard(Cards.copper, this.controlCard, targetContext);
            }
            i++;
        }

        if (trashed.size() > 0) {
            for (Card c : trashed) {
                player.controlPlayer.gainCardAlreadyInPlay(c, this.controlCard, moveContext);
                context.game.trashPile.remove(c);
            }
        }
    }

	private void poorHouse(MoveContext context, Player currentPlayer) {
	    int treasures = 0;

	    for (int i = 0; i < currentPlayer.hand.size(); i++) {
	        Card card = currentPlayer.hand.get(i);
	        currentPlayer.reveal(card, this.controlCard, context);
	        if (card instanceof TreasureCard) {
	        	treasures++;
	        }
	    }
    	context.addGold = Math.max(0, context.addGold-treasures);
	}

	private void sage(Game game, MoveContext context, Player currentPlayer) {
	    HashSet<String> cardNames = new HashSet<String>();

	    for (int i = 0; i < currentPlayer.hand.size(); i++) {
	        Card card = currentPlayer.hand.get(i);
	        cardNames.add(card.getName());
	        //currentPlayer.reveal(card, this.controlCard, context);
	    }

	    ArrayList<Card> toDiscard = new ArrayList<Card>();

	    Card draw = null;
	    while ((draw = game.draw(currentPlayer)) != null && draw.getCost(context) < 3) {
    		currentPlayer.reveal(draw, this.controlCard, context);
    		toDiscard.add(draw);
	    }

	    if (draw != null) {
	        currentPlayer.reveal(draw, this.controlCard, context);
	        currentPlayer.hand.add(draw);
	    }

	    while (!toDiscard.isEmpty()) {
	        currentPlayer.discard(toDiscard.remove(0), this.controlCard, null);
	    }
	}

	private void rats(MoveContext context, Player currentPlayer) {
		currentPlayer.gainNewCard(Cards.rats, this.controlCard, context);

        if(currentPlayer.hand.size() > 0) {
        	boolean hasother = false;
        	for (Card c : currentPlayer.hand) {
        		if (!c.equals(Cards.rats)) {
        			hasother = true;
        		}
        	}
        	if (!hasother) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card card = currentPlayer.hand.get(i);
                    currentPlayer.reveal(card, this.controlCard, context);
                }
        	} else {
	            Card card = currentPlayer.controlPlayer.rats_cardToTrash(context);
	            if(card == null || !currentPlayer.hand.contains(card)) {
	                Util.playerError(currentPlayer, "Rats card to trash invalid, picking one");
	                card = currentPlayer.hand.get(0);
	            }

	            currentPlayer.hand.remove(card);
	            currentPlayer.trash(card, this.controlCard, context);
        	}
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
	        	currentPlayer.gainNewCard(Cards.silver, this.controlCard, context);
	        }
	    }
	}

	public void armory(Player currentPlayer, MoveContext context) {
	    Card card = currentPlayer.controlPlayer.armory_cardToObtain(context);
	    if (card != null) {
	        // check cost
	        if (card.getCost(context) <= 5) {
	            currentPlayer.gainNewCard(card, this.controlCard, context);
	            //currentPlayer.discard.remove(Cards.armory);
	            //currentPlayer.putOnTopOfDeck(Cards.armory);

	        }
	    }
	}

	private void altar(Player currentPlayer, MoveContext context) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.altar_cardToTrash(context);

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Altar trash error, trashing a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.hand.remove(card);
            currentPlayer.trash(card, this.controlCard, context);
        }

		Card card = currentPlayer.controlPlayer.altar_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 5) {
                currentPlayer.gainNewCard(card, this.controlCard, context);
            }
        }
	}

    private void banditCamp(MoveContext context, Player currentPlayer)
    {
        // Gain a Spoils from the Spoils pile
        currentPlayer.gainNewCard(Cards.spoils, this.controlCard, context);
    }

	public void beggar(Player currentPlayer, MoveContext context) {
		currentPlayer.gainNewCard(Cards.copper, this.controlCard, context);
		currentPlayer.gainNewCard(Cards.copper, this.controlCard, context);
		currentPlayer.gainNewCard(Cards.copper, this.controlCard, context);
	}

	public void catacombs(Game game, Player currentPlayer, MoveContext context) {
        ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 3; i++) {
            Card card = game.draw(currentPlayer);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            if (currentPlayer.controlPlayer.catacombs_shouldDiscardTopCards(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]))) {
                while (!topOfTheDeck.isEmpty()) {
                    currentPlayer.discard(topOfTheDeck.remove(0), this.controlCard, null);
                }
                game.drawToHand(currentPlayer, this.controlCard);
                game.drawToHand(currentPlayer, this.controlCard);
                game.drawToHand(currentPlayer, this.controlCard);
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
	                currentPlayer.reveal(cards[i], this.controlCard, context);
	                currentPlayer.discard(cards[i], this.controlCard, null);
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
	    		currentPlayer.gainNewCard(Cards.copper, this.controlCard, context);
	    		break;
	    	}
	    }

		Player.CountSecondOption option2 = currentPlayer.controlPlayer.count_chooseSecondOption(context);
		if (option2 == null) {
	        Util.playerError(currentPlayer, "Count second option error, ignoring.");
	    } else {
	    	switch (option2) {
	    	case Coins:
	    		context.addGold += 3;
	    		break;
	    	case TrashHand:
	    		if (currentPlayer.hand.size() > 0) {
		    		Card[] temp = currentPlayer.hand.toArray();
		    		for (Card c : temp) {
		    			currentPlayer.hand.remove(c);
		    			currentPlayer.trash(c, this.controlCard, context);
		    		}
	    		}
	    		break;
	    	case GainDuchy:
	    		currentPlayer.gainNewCard(Cards.duchy, this.controlCard, context);
	    		break;
	    	}
	    }
	}
	private void deathCart(Player currentPlayer, MoveContext context)
	{
		Card actionCardToTrash = currentPlayer.controlPlayer.deathCart_actionToTrash(context);
		if (actionCardToTrash != null)
		{
			currentPlayer.hand.remove(actionCardToTrash);
            currentPlayer.trash(actionCardToTrash, this.controlCard, context);
		}
		else if (!this.controlCard.movedToNextTurnPile)
		{
			currentPlayer.playedCards.remove(this.controlCard);      
            currentPlayer.trash(this.controlCard, this.controlCard, context);
            this.controlCard.movedToNextTurnPile = true;
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
            currentPlayer.trash(card, this.controlCard, context);
        }

        HashSet<String> cardNames = new HashSet<String>();
        for (Card card : game.trashPile) {
            if (card == null) {
                break;
            }

            if (card instanceof TreasureCard) {
                cardNames.add(card.getName());
            }
        }
        context.addGold += cardNames.size();
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

			toGain = game.trashPile.remove(game.trashPile.indexOf(toGain));
            currentPlayer.gainCardAlreadyInPlay(toGain, this.controlCard, context);

			break;

		case TrashActionCard:
			Card toTrash = currentPlayer.controlPlayer.graverobber_cardToTrash(context);

			if (toTrash == null || !currentPlayer.hand.contains(toTrash) || !(toTrash instanceof ActionCard)) {
                Util.playerError(currentPlayer, "Graverobber trash error, trashing nothing.");
                return;
            }

			currentPlayer.hand.remove(toTrash);
			currentPlayer.trash(toTrash, this.controlCard, context);

			toGain = currentPlayer.controlPlayer.graverobber_cardToReplace(context, 3 + toTrash.getCost(context), toTrash.costPotion());
			if (toGain != null) {
				currentPlayer.gainNewCard(toGain, this.controlCard, context);
			}
			break;
		}
	}

	private void ironmonger(Game game, Player currentPlayer, MoveContext context) {
		Card card = game.draw(currentPlayer);

		if (card != null) {
			if (currentPlayer.controlPlayer.ironmonger_shouldDiscard(context, card)) {
				currentPlayer.discard(card, this.controlCard, context);
			} else {
				currentPlayer.putOnTopOfDeck(card);
			}
	
			if (card instanceof ActionCard) {
				context.actions += 1;
			}
			if (card instanceof TreasureCard) {
				context.addGold++;
			}
			if (card instanceof VictoryCard) {
				game.drawToHand(currentPlayer, this.controlCard);
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
	        currentPlayer.trash(card, this.controlCard, context);
		}
	}

	private void mystic(Game game, MoveContext context, Player currentPlayer) {
		
		if (currentPlayer.deck.size() > 0 || currentPlayer.discard.size() > 0) {  // Only allow a guess if there are cards in the deck or discard pile
			
    		// Create a list of all possible cards to guess, using the player's hand, discard pile, and deck 
    		// (even though the player could technically name a card he doesn't have)
			ArrayList<Card> options = currentPlayer.getAllCards();
			Collections.sort(options, new Util.CardNameComparator());
	
			if (options.size() > 0) {
				Card toName = currentPlayer.controlPlayer.mystic_cardGuess(context, options);
				currentPlayer.controlPlayer.namedCard(toName, this.controlCard, context);
				Card draw   = game.draw(currentPlayer);
			    
			    if (toName != null && draw != null) {
			        currentPlayer.reveal(draw, this.controlCard, context);
		
			        if (toName.equals(draw)) {
			            currentPlayer.hand.add(draw);
			        } else {
			            currentPlayer.putOnTopOfDeck(draw);
			        }
			    }
			}
		}
	}

    private void scavenger(Game game, MoveContext context, Player currentPlayer)
    {
        boolean discard = currentPlayer.controlPlayer.scavenger_shouldDiscardDeck(context);

        // Discard the entire deck if the player chose to do so
        if (discard)
        {
            while (currentPlayer.getDeckSize() > 0)
            {
                currentPlayer.discard(game.draw(currentPlayer), this.controlCard, null, false);
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
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.controlCard, context);
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
                game.drawToHand(currentPlayer, this.controlCard);
            }
        }

        cards = currentPlayer.controlPlayer.storeroom_cardsToDiscardForCoins(context);
        if (cards != null) {
            int numberOfCards = 0;
            for (Card card : cards) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card playersCard = currentPlayer.hand.get(i);
                    if (playersCard.equals(card)) {
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.controlCard, context);
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
                context.addGold++;
            }
        }
    }

    private void wanderingMinstrel(Player currentPlayer, MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 3; i++) {
            Card card = context.game.draw(currentPlayer);
            if (card == null) {
                break;
            }
            if (!(card instanceof ActionCard) ) {
	            currentPlayer.discard(card, this.controlCard, context);
            } else {
	            currentPlayer.reveal(card, this.controlCard, context);
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
    
	private void rebuild(Player currentPlayer, MoveContext context) {
		Card named = currentPlayer.controlPlayer.rebuild_cardToPick(context);
		currentPlayer.controlPlayer.namedCard(named, this.controlCard, context);
		ArrayList<Card> cards = new ArrayList<Card>();
		Card last = null;

		// search for first Victory card that was not named
		while ((last = context.game.draw(currentPlayer)) != null) {
			if (last instanceof VictoryCard && !last.equals(named)) break;
			cards.add(last);
			currentPlayer.reveal(last, this.controlCard, context);
		}

		// Discard all other revealed cards
		for (Card c : cards) {
			currentPlayer.discard(c, this.controlCard, context);
		}

		if (last != null) {
			// Trash the found Victory card
			currentPlayer.trash(last, this.controlCard, context);
	
			// Gain Victory card that cost up to 3 more coins
			Card toGain = currentPlayer.controlPlayer.rebuild_cardToGain(context, 3 + last.getCost(context), last.costPotion());
			if (toGain != null) {
				currentPlayer.gainNewCard(toGain, this.controlCard, context);
			}
		}
	}

	private void rogue(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Card> options = new ArrayList<Card>();
		for (Card c : game.trashPile) {
			if (c.getCost(context) >= 3 && c.getCost(context) <= 6) {
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
			currentPlayer.gainCardAlreadyInPlay(toGain, this.controlCard, context);
		} else { // Other players trash a card
	        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
	            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this.controlCard)) {
	                targetPlayer.attacked(this.controlCard, context);
	                MoveContext targetContext = new MoveContext(game, targetPlayer);
	                ArrayList<Card> canTrash = new ArrayList<Card>();

	                List<Card> cardsToDiscard = new ArrayList<Card>();
	                for (int i = 0; i < 2; i++) {
	                    Card card = game.draw(targetPlayer);

	                    if (card != null) {
	                        targetPlayer.reveal(card, this.controlCard, targetContext);
	                        int cardCost = card.getCost(context);

	                        if (cardCost >= 3 && cardCost <= 6) {
	                            canTrash.add(card);
	                        } else {
	                        	cardsToDiscard.add(card);
	                        }
	                    }
	                }

	                for (Card c: cardsToDiscard) {
	                    targetPlayer.discard(c, this.controlCard, targetContext);
	                }

	                Card cardToTrash = null;

	                if (canTrash.size() == 1) {
	                    cardToTrash = canTrash.get(0);
	                } else if (canTrash.size() == 2) {
	                    if (canTrash.get(0).equals(canTrash.get(1))) {
	                        cardToTrash = canTrash.get(0);
	                        targetPlayer.discard(canTrash.remove(1), this.controlCard, targetContext);
	                    } else {
	                        cardToTrash = targetPlayer.controlPlayer.rogue_cardToTrash(context, canTrash);
	                    }

	                    for (Card card : canTrash) {
	                        if (!card.equals(cardToTrash)) {
	                            targetPlayer.discard(card, this.controlCard, targetContext);
	                        }
	                    }
	                }

	                if (cardToTrash != null) {
	                    targetPlayer.trash(cardToTrash, this.controlCard, targetContext);
	                }
	            }
	        }
		}
	}

	private void pillage(Game game, MoveContext context, Player currentPlayer)
	{

		// Each other player with 5 cards in hand reveals his hand and discards a card that you choose.
		for (Player targetPlayer : game.getPlayersInTurnOrder())
		{
            if (targetPlayer != currentPlayer &&
            	targetPlayer.getHand().size() >= 5 &&
            	!Util.isDefendedFromAttack(game, targetPlayer, this.controlCard))
            {
                targetPlayer.attacked(this.controlCard, context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                ArrayList<Card> cardsInHand = new ArrayList<Card>();

                for (Card card : targetPlayer.getHand())
                {
                	cardsInHand.add(card);
                    targetPlayer.reveal(card, this.controlCard, targetContext);
                }

                Card cardToDiscard = currentPlayer.controlPlayer.pillage_opponentCardToDiscard(targetContext, cardsInHand);

                if (cardToDiscard != null)
                {
                	targetPlayer.hand.remove(cardToDiscard);
                    targetPlayer.discard(cardToDiscard, this.controlCard, targetContext);
                }
            }
        }

		// Gain 2 Spoils from the Spoils pile
		currentPlayer.gainNewCard(Cards.spoils, this.controlCard, context);
		currentPlayer.gainNewCard(Cards.spoils, this.controlCard, context);
	}
	
	private void governor(Game game, MoveContext context, Player currentPlayer) {
	    Player.GovernorOption option = currentPlayer.controlPlayer.governor_chooseOption(context);

		if (option == null) {
	        Util.playerError(currentPlayer, "Governor option error, ignoring.");
	    } else {
	        if (option == Player.GovernorOption.AddCards) {
	            game.drawToHand(currentPlayer, this.controlCard);
	            game.drawToHand(currentPlayer, this.controlCard);
	            game.drawToHand(currentPlayer, this.controlCard);
	            for (Player player : getAllPlayers()) {
	                if (player != context.getPlayer()) {
	                    drawToHand(game, player, this.controlCard);
	                }
	            }
	        } else if (option == Player.GovernorOption.GainTreasure) {
	        	currentPlayer.gainNewCard(Cards.gold, this.controlCard, context);
	        	for (Player player : getAllPlayers()) {
	        		if (player != context.getPlayer()) {
	        			player.gainNewCard(Cards.silver, this.controlCard, new MoveContext(game, player));
	        		}
	        	}
	        } else if (option == Player.GovernorOption.Upgrade) {
	        	if (currentPlayer.getHand().size() > 0) {
	                Card card = currentPlayer.controlPlayer.governor_cardToTrash(context);
	                if (card == null || !currentPlayer.hand.contains(card)) {
	                    Util.playerError(currentPlayer, "Governor trash error, upgrading a random card.");
	                    card = Util.randomCard(currentPlayer.hand);
	                }

	                int value = card.getCost(context) + 2;
	                boolean potion = card.costPotion();
	                currentPlayer.hand.remove(card);
	                currentPlayer.trash(card, this.controlCard, context);

	                card = currentPlayer.controlPlayer.governor_cardToObtain(context, value, potion);
	                if (card != null) {
	                    if (card.getCost(context) != value || card.costPotion() != potion) {
	                        Util.playerError(currentPlayer, "Governor error, new card does not cost value of the old card +2.");
	                    } else {
	                        if(!currentPlayer.gainNewCard(card, this.controlCard, context)) {
	                            Util.playerError(currentPlayer, "Governor error, pile is empty or card is not in the game.");
	                        }
	                    }
	                }
	            }
	        	for (Player player : getAllPlayers()) {
	                if (player != context.getPlayer()) {
	                	MoveContext playerContext = new MoveContext(game, player);
	                	if (player.getHand().size() > 0) {
	    	                Card card = player.controlPlayer.governor_cardToTrash(playerContext);
	    	                if (card == null || !player.hand.contains(card)) {
	    	                    Util.playerError(player, "Governor trash error, upgrading a random card.");
	    	                    card = Util.randomCard(player.hand);
	    	                }

	    	                int value = card.getCost(playerContext) + 1;
	    	                boolean potion = card.costPotion();
	    	                player.hand.remove(card);
	    	                player.trash(card, this.controlCard, playerContext);

	    	                card = player.controlPlayer.governor_cardToObtain(playerContext, value, potion);
	    	                if (card != null) {
	    	                    if (card.getCost(playerContext) != value || card.costPotion() != potion) {
	    	                        Util.playerError(player, "Governor error, new card does not cost value of the old card +1.");
	    	                    } else {
	    	                        if(!player.gainNewCard(card, this.controlCard, playerContext)) {
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

	private void envoy(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Card> cards = new ArrayList<Card>();
		Player nextPlayer = game.getNextPlayer();
        for (int i = 0; i < 5; i++) {
            Card card = game.draw(currentPlayer);
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

        GameEvent event = new GameEvent(GameEvent.Type.CardDiscarded, (MoveContext) context);
        event.card = toDiscard;
        event.responsible = this.controlCard;
        game.broadcastEvent(event);

        if (cards.size() > 0) {
        	for(Card c : cards) {
        		currentPlayer.hand.add(c);
        	}
        }

	}

	@Override
	public void isTrashed(MoveContext context) {
		switch (this.controlCard.behaveAsCard().getType()) {
		case Rats:
			context.game.drawToHand(context.player, this.controlCard, true);
			break;
		case Squire:
			// Need to ensure that there is at least one Attack card that can be gained,
			// otherwise this.controlCard choice should be bypassed.
			boolean attackCardAvailable = false;
			
			for (Card c : context.game.getCardsInGame())
			{
				if (c instanceof ActionCard)
				{
					if (((ActionCard)c).isAttack() && context.game.getPile(c).getCount() > 0)
					{
						attackCardAvailable = true;
						break;
					}
				}
			}
			
			if (attackCardAvailable)
			{
				Card s = context.player.controlPlayer.squire_cardToObtain(context);
        	
				if (s != null) 
				{
					context.player.controlPlayer.gainNewCard(s, this.controlCard, context);
				}
			}
        	break;
		case Catacombs:
			Card c = context.player.controlPlayer.catacombs_cardToObtain(context);
        	if (c != null) {
        		context.player.controlPlayer.gainNewCard(c, this.controlCard, context);
        	}
        	break;
		case HuntingGrounds:
			// Make sure there are Estates and/or Duchies available when trashing Hunting Grounds.
			// If there isn't a choice to be made, the player gets the option that is still available,
			// or nothing at all if both piles are empty
			int duchyCount      = context.game.getPile(Cards.duchy).getCount();
			int estateCount     = context.game.getPile(Cards.estate).getCount();
			boolean gainDuchy   = false;
			boolean gainEstates = false;
			
			if (duchyCount > 0 && estateCount > 0)
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
			else if (duchyCount > 0)
			{
				gainDuchy = true;
			}
			else if (estateCount > 0)
			{
    			gainEstates = true;
			}
			
			if (gainDuchy)
			{
				context.player.controlPlayer.gainNewCard(Cards.duchy, this.controlCard, context);
			}
			else if (gainEstates)
			{
				context.player.controlPlayer.gainNewCard(Cards.estate, this.controlCard, context);
    			context.player.controlPlayer.gainNewCard(Cards.estate, this.controlCard, context);
    			context.player.controlPlayer.gainNewCard(Cards.estate, this.controlCard, context);
			}
			
			break;
		case Fortress:
			context.game.trashPile.remove(this.controlCard);
        	context.player.hand.add(this.controlCard);
        	break;
		case Cultist:
			context.game.drawToHand(context.player, this.controlCard, false);
			context.game.drawToHand(context.player, this.controlCard, false);
			context.game.drawToHand(context.player, this.controlCard, false);
			break;
		case SirVander:
			context.player.controlPlayer.gainNewCard(Cards.gold, this.controlCard, context);
			break;
		default:
			break;
		}
		
    	// card left play - stop any impersonations
    	this.controlCard.stopImpersonatingCard();
	}
	
	/*@Override
	public void isGained(MoveContext context) {
		super.isGained(context);
		switch (this.controlCard.type) {
		case DeathCart:
			context.player.controlPlayer.gainNewCard(Cards.virtualRuins, this.controlCard, context);
			context.player.controlPlayer.gainNewCard(Cards.virtualRuins, this.controlCard, context);
			break;
		default:
			break;
		}
	}*/

	@Override
	public boolean isRuins() {
		switch (this.controlCard.getType()) {
		case AbandonedMine:
		case RuinedLibrary:
		case RuinedMarket:
		case RuinedVillage:
		case Survivors:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean isKnight() {
		return isKnight;
	}

	@Override
	public boolean isLooter() {
		switch (this.controlCard.getType()) {
		case Cultist:
		case DeathCart:
		case Marauder:
			return true;
		default:
			return false;
		}
	}

	private void survivors(MoveContext context, Game game, Player currentPlayer) {
        ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 2; i++) {
            Card card = game.draw(currentPlayer);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        if (topOfTheDeck.size() > 0) {
            if (currentPlayer.controlPlayer.survivors_shouldDiscardTopCards(context, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]))) {
                while (!topOfTheDeck.isEmpty()) {
                    currentPlayer.discard(topOfTheDeck.remove(0), this.controlCard, null);
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

	private void cultist(MoveContext context, Game game, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !isDefendedFromAttack(game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);
                player.gainNewCard(Cards.virtualRuins, this.controlCard, playerContext);
            }
        }
        
        if (currentPlayer.hand.contains(Cards.cultist) && currentPlayer.controlPlayer.cultist_shouldPlayNext(context)) {
        	ActionCardImpl next = (ActionCardImpl) currentPlayer.hand.get(Cards.cultist);
            if (next != null) {
                context.freeActionInEffect++;

                next.play(game, context, true);

                context.freeActionInEffect--;
            }
        }
	}

	private void urchin(MoveContext context, Game game, Player currentPlayer) 	{		
        for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);

                int keepCardCount = 4;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.urchin_attack_cardsToKeep(context);
                    player.discardRemainingCardsFromHand(context, cardsToKeep, this.controlCard, keepCardCount);
                }
            }
        }
	}

	private void attackPlayed(MoveContext context, Game game, Player currentPlayer)	{
		// If an Urchin has been played, offer the player the option to trash it for a Mercenary
		for (int i = currentPlayer.playedCards.size() - 1; i > 0 ; ) {
			Card c = currentPlayer.playedCards.get(--i);
			if (c.behaveAsCard().getType() == Cards.Type.Urchin && currentPlayer.controlPlayer.urchin_shouldTrashForMercenary(context)) {
				currentPlayer.trash(c.getControlCard(), this, context);
				currentPlayer.gainNewCard(Cards.mercenary, this, context);
				currentPlayer.playedCards.remove(i);
			}
		}
	}
	
	private void mercenary(MoveContext context, Game game, Player currentPlayer) 
	{
		int cardsTrashedCount = 0;
		
		Card[] cards = currentPlayer.controlPlayer.mercenary_cardsToTrash(context);
		
        if (cards != null) {
            if (cards.length > 2) {
                Util.playerError(currentPlayer, "Mercenary trash error, trying to trash too many cards, ignoring.");
            } else {
                for (Card card : cards) {
                    for (int i = 0; i < currentPlayer.hand.size(); i++) {
                        Card playersCard = currentPlayer.hand.get(i);
                        if (playersCard.equals(card)) {
                            Card thisCard = currentPlayer.hand.remove(i);

                            currentPlayer.trash(thisCard, this.controlCard, context);
                            ++cardsTrashedCount;
                            break;
                        }
                    }
                }
            }
        }
        
        if (cardsTrashedCount == 2)
        {
        	game.drawToHand(currentPlayer, this.controlCard);
        	game.drawToHand(currentPlayer, this.controlCard);
        	
        	context.addGold += 2;
        	
            for (Player player : game.getPlayersInTurnOrder()) {
                if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this.controlCard)) {
                    player.attacked(this.controlCard, context);

                    int keepCardCount = 3;
                    if (player.hand.size() > keepCardCount) {
                        Card[] cardsToKeep = player.controlPlayer.mercenary_attack_cardsToKeep(context);
                        player.discardRemainingCardsFromHand(context, cardsToKeep, this.controlCard, keepCardCount);
                    }
                }
            }
        }
	}

	private void marauder(MoveContext context, Game game, Player currentPlayer) 
	{
		currentPlayer.gainNewCard(Cards.spoils, this.controlCard, context);
		
		for (Player player : game.getPlayersInTurnOrder()) 
		{
            if (player != currentPlayer && !isDefendedFromAttack(game, player, this.controlCard)) 
            {
                player.attacked(this.controlCard, context);
                MoveContext playerContext = new MoveContext(game, player);
                player.gainNewCard(Cards.virtualRuins, this.controlCard, playerContext);
            }
        }
	}
	
	private void hermit(MoveContext context, Game game, Player currentPlayer)
	{
		ArrayList<Card> options = new ArrayList<Card>();
		int nonTreasureCountInDiscard = 0;
		
		for (Card c : currentPlayer.discard) {
			if (!(c instanceof TreasureCard)) {
				options.add(c);
				++nonTreasureCountInDiscard;	// Keep track of which cards are in the discard pile so that the player 
												// can tell if they are trashing from discard or hand
			}
		}
		if (!options.isEmpty())
			Collections.sort(options, new Util.CardNameComparator());
		
		ArrayList<Card> options2 = new ArrayList<Card>();
		for (Card c: currentPlayer.hand) {
			if (!(c instanceof TreasureCard)) {
				options2.add(c);
			}
		}
		if (!options2.isEmpty()) {
			Collections.sort(options2, new Util.CardNameComparator());
			options.addAll(options2);
		}

		if (!options.isEmpty()) {
			// Offer the option to trash a non-treasure card
			context.hermitTrashCardPile = PileSelection.ANY; 
			Card toTrash = currentPlayer.controlPlayer.hermit_cardToTrash(context, options, nonTreasureCountInDiscard);
			
			if (toTrash != null) {
				if (currentPlayer.discard.contains(toTrash) && (context.hermitTrashCardPile == PileSelection.ANY || context.hermitTrashCardPile == PileSelection.DISCARD)) {
					currentPlayer.discard.remove(toTrash);
					currentPlayer.trash(toTrash, this.controlCard, context);
				} else if (currentPlayer.hand.contains(toTrash) && (context.hermitTrashCardPile == PileSelection.ANY || context.hermitTrashCardPile == PileSelection.HAND)) {
					currentPlayer.hand.remove(toTrash);
					currentPlayer.trash(toTrash, this.controlCard, context);
				} else {
					Util.playerError(currentPlayer, "Hermit trash error, chosen card to trash not in hand or discard, ignoring.");
				}
			}
		}
		
		// Gain a card costing up to 3 coins (no potion)
        Card c = currentPlayer.controlPlayer.hermit_cardToGain(context);
    	if (c != null) {
    		if (c.getCost(context, false) > 3 || c.costPotion()) {
	            Util.playerError(currentPlayer, "Hermit card selection error, picking card from table.");
	            c = (context.getCardsLeftInPile(Cards.silver) > 0) ? Cards.silver : Cards.copper; 
			}
    		currentPlayer.controlPlayer.gainNewCard(c, this.controlCard, context);
    	}
	}
	
	private void madman(MoveContext context, Game game, Player currentPlayer) {
		if (currentPlayer.playedCards.contains(this.controlCard)) {
			// Return to the Madman pile
            currentPlayer.playedCards.remove(this.controlCard);
            SingleCardPile pile = (SingleCardPile) game.getPile(this.controlCard);
            pile.addCard(this.controlCard);
            
            int handSize = currentPlayer.hand.size();
            
            for (int i = 0; i < handSize; ++i) {
            	game.drawToHand(currentPlayer, this.controlCard);
            }
		}
	}
	
	private void vagrant(MoveContext context, Game game, Player currentPlayer)
	{
		Card c = game.draw(currentPlayer);
		if (c != null) {
			currentPlayer.reveal(c, this.controlCard, context);
			if (c.getType() == Cards.Type.Curse || c.isShelter() || (c instanceof VictoryCard) || (c.isRuins())) {
				currentPlayer.hand.add(c);
			} else {
				currentPlayer.putOnTopOfDeck(c);
			}
		}
	}
	
	private void knight(MoveContext context, Player currentPlayer) {
		for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(context.game, targetPlayer, this.controlCard)) {
                targetPlayer.attacked(this.controlCard, context);
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
                ArrayList<Card> canTrash = new ArrayList<Card>();

                List<Card> cardsToDiscard = new ArrayList<Card>();
                for (int i = 0; i < 2; i++) {
                    Card card = context.game.draw(targetPlayer);

                    if (card != null) {
                        targetPlayer.reveal(card, this.controlCard, targetContext);
                        int cardCost = card.getCost(context);

                        if (!card.costPotion() && cardCost >= 3 && cardCost <= 6) {
                            canTrash.add(card);
                        } else {
                        	cardsToDiscard.add(card);
                        }
                    }
                }

                for (Card c: cardsToDiscard) {
                    targetPlayer.discard(c, this.controlCard, targetContext);
                }

                Card cardToTrash = null;

                if (canTrash.size() == 1) {
                    cardToTrash = canTrash.get(0);
                } else if (canTrash.size() == 2) {
                    if (canTrash.get(0).equals(canTrash.get(1))) {
                        cardToTrash = canTrash.get(0);
                        targetPlayer.discard(canTrash.remove(1), this.controlCard, targetContext);
                    } else {
                        cardToTrash = targetPlayer.knight_cardToTrash(context, canTrash);
                    }

                    for (Card card : canTrash) {
                        if (!card.equals(cardToTrash)) {
                            targetPlayer.discard(card, this.controlCard, targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.controlCard, targetContext);
                    
                    // If the card trashed was a knight, the attacking knight should be trashed as well
                    if (cardToTrash.isKnight() && currentPlayer.playedCards.contains(this.controlCard) && currentPlayer.playedCards.getLastCard() == this.controlCard) {
                    	currentPlayer.trash(currentPlayer.playedCards.removeLastCard(), cardToTrash, context);
                    }
                }
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

                            currentPlayer.trash(thisCard, this.controlCard, context);
                            break;
                        }
                    }
                }
            }
        }
        
        knight(context, currentPlayer);
	}
	
	private void sirMichael(MoveContext context, Player currentPlayer) {
        for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this.controlCard)) {
                player.attacked(this.controlCard, context);

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.sirMichael_attack_cardsToKeep(context);
                    player.discardRemainingCardsFromHand(context, cardsToKeep, this.controlCard, keepCardCount);
                }
            }
        }
        
        knight(context, currentPlayer);
	}
	
	private void dameNatalie(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.dameNatalie_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 3) {
                currentPlayer.gainNewCard(card, this.controlCard, context);
            } else {
            	Util.playerError(currentPlayer, "Dame Natalie error: chosen card that costs more then 3");
            }
        }
        
        knight(context, currentPlayer);
	}

    private void bandOfMisfits(Game game, MoveContext context, Player currentPlayer) {
    	// Already impersonating another card?
    	if (!this.isImpersonatingAnotherCard()) {
	    	// Get card to impersonate
	        ActionCard cardToImpersonate = currentPlayer.controlPlayer.bandOfMisfits_actionCardToImpersonate(context);
	        if (cardToImpersonate != null 
	        		&& !game.isPileEmpty(cardToImpersonate) 
	        		&& cardToImpersonate instanceof ActionCard 
	        		&& cardToImpersonate.getCost(context) < this.controlCard.getCost(context) 
	        		&& (context.golemInEffect == 0 || cardToImpersonate != Cards.golem)) {
	        	GameEvent event = new GameEvent(GameEvent.Type.CardNamed, (MoveContext) context);
	            event.card = cardToImpersonate;
	            event.responsible = this;
	            game.broadcastEvent(event);
	            this.startImpersonatingCard(cardToImpersonate.getTemplateCard().instantiate());
	        } else {
	            Card[] cards = game.getActionsInGame();
		        if (cards.length != 0 && cardToImpersonate != null) {
		        	Util.playerError(currentPlayer, "Band of Misfits returned invalid card (" + cardToImpersonate.getName() + "), ignoring.");
		        }
	        	return;
	        }
    	}
    	
    	// Play the impersonated card
        ActionCardImpl cardToPlay = (ActionCardImpl) this.impersonatingCard;
        context.freeActionInEffect++;
        cardToPlay.play(game, context, false);
        context.freeActionInEffect--;

        // impersonated card stays in play until next turn?
        if (cardToPlay.trashOnUse) {
            int idx = currentPlayer.playedCards.lastIndexOf(this);
            if (idx >= 0) currentPlayer.playedCards.remove(idx);
            currentPlayer.trash(this, null, context);
        } else if (cardToPlay instanceof DurationCard && !cardToPlay.equals(Cards.outpost)) {
            if (!this.controlCard.movedToNextTurnPile) {
                this.controlCard.movedToNextTurnPile = true;
                int idx = currentPlayer.playedCards.lastIndexOf(this);
                if (idx >= 0) {
                	currentPlayer.playedCards.remove(idx);
                    currentPlayer.nextTurnCards.add(this);
                }
            }
        }
    }

}
