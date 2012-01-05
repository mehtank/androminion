package com.vdom.core;

import java.util.ArrayList;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;

public abstract class Player {
    Random rand = new Random(System.currentTimeMillis());

    // Only used by InteractivePlayer currently
    public String name;
    public int playerNumber;
    public CardList hand;
    public CardList deck;
    public CardList discard;
    public int vps;
    public boolean win = false;
    public int pirateShipTreasure;
    private int victoryTokens;
    public CardList nextTurnCards;
    public CardList nativeVillage;
    public CardList island;
    public CardList haven;
    public CardList horseTraders;
    public Game game;
    
    public boolean achievementSingleCardFailed;
    public Card achievementSingleCardFirstKingdomCardBought;
    
    public void addVictoryTokens(MoveContext context, int vt) {
        victoryTokens += vt;
        context.vpsGainedThisTurn += vt;
    }
    
    public int getVictoryCardsBoughtThisTurn(MoveContext context) {
        return context.getVictoryCardsBoughtThisTurn();
    }

    public int getTotalCardsBoughtThisTurn(MoveContext context) {
        return context.getTotalCardsBoughtThisTurn();
    }

    public boolean isAi() {
        return true;
    }
    
    public void setName(String name) {
        this.name = name.replace("_", " ");
    }
    
    public int getCurrencyTotal(MoveContext context) {
        return getMyCardCount(Cards.copper) + getMyCardCount(Cards.silver) * 2 + getMyCardCount(Cards.gold) * 3 + getMyCardCount(Cards.platinum) * 5;
    }

    public ArrayList<Card> getActionCards(Card[] cards) {
        ArrayList<Card> actionCards = new ArrayList<Card>();
        for (Card card : cards) {
            if (card instanceof ActionCardImpl) {
                actionCards.add(card);
            }
        }

        return actionCards;
    }

    public int getActionCardCount(Card[] cards) {
        return getActionCards(cards).size();
    }

    public int getMyAddActionCardCount() {
        int addActionsCards = 0;
        for (Card card : getAllCards()) {
            if (card instanceof ActionCard) {
                if (((ActionCard) card).getAddActions() > 0) {
                    addActionsCards++;
                }
            }
        }

        return addActionsCards;
    }

    public int getMyAddCardCardCount() {
        int addCards = 0;
        for (Card card : getAllCards()) {
            if (card instanceof ActionCard) {
                if (((ActionCard) card).getAddActions() > 0) {
                    addCards++;
                }
            }
        }

        return addCards;
    }

    public int getMyAddActions() {
        int addActions = 0;
        for (Card card : getAllCards()) {
            if (card instanceof ActionCard) {
                addActions += ((ActionCard) card).getAddActions();
            }
        }

        return addActions;
    }

    public int getMyAddCards() {
        int addCards = 0;
        for (Card card : getAllCards()) {
            if (card instanceof ActionCard) {
                addCards += ((ActionCard) card).getAddCards();
            }
        }

        return addCards;
    }

    public int getMyAddBuys() {
        int addBuys = 0;
        for (Card card : getAllCards()) {
            if (card instanceof ActionCard) {
                addBuys += ((ActionCard) card).getAddBuys();
            }
        }

        return addBuys;
    }

    public boolean inHand(Card card) {
        for (Card thisCard : hand) {
            if (thisCard.equals(card)) {
                return true;
            }
        }

        return false;
    }

    public int mineableCards(Card[] hand) {
        int mineableCards = 0;
        for (Card card : hand) {
            if (card.equals(Cards.copper) || card.equals(Cards.silver) || card.equals(Cards.gold)) {
                mineableCards++;
            }
        }

        return mineableCards;
    }

    public int inHandCount(Card card) {
        return Util.getCardCount(getHand(), card);
    }

    public Card fromHand(Card card) {
        for (Card thisCard : getHand()) {
            if (thisCard.equals(card)) {
                return thisCard;
            }
        }

        return null;
    }
    
    public boolean getWin() {
        return win;
    }

    public void initCards() {
        hand = new CardList(this, "Hand");
        deck = new CardList(this, "Deck");
        discard = new CardList(this, "Discard");
        nextTurnCards = new CardList(this, "Duration");
        nativeVillage = new CardList(this, "Native Village");
        island = new CardList(this, "Island");
        haven = new CardList(this, "Haven");
        horseTraders = new CardList(this, "Horse Traders");
    }

    protected void cleanup(MoveContext context) {
        // /////////////////////////////////
        // Discard hand
        // /////////////////////////////////

        while (getHand().size() > 0) {
            discard(hand.remove(0, false), null, null, false);
        }

        // /////////////////////////////////
        // Discard played cards
        // /////////////////////////////////
        int treasuryCardsToSave = 0;
        int treasuryCardsInPlay = 0;
        
        for (Card card : context.playedCards) {
            if (card.equals(Cards.treasury)) {
                treasuryCardsInPlay++;
            }
        }

        if (getVictoryCardsBoughtThisTurn(context) == 0 && treasuryCardsInPlay > 0) {
            treasuryCardsToSave = treasury_putBackOnDeck(context, treasuryCardsInPlay);
        }

        if (treasuryCardsToSave < 0 || treasuryCardsToSave > treasuryCardsInPlay) {
            Util.playerError(this, "Treasury put back cards error, ignoring.");
            treasuryCardsToSave = 0;
        }
        
        for (Card card : context.playedCards) {
            if (card.equals(Cards.treasury)) {
                treasuryCardsInPlay++;
            }
        }
        
        int herbalistCount = 0;
        for (Card card : context.playedCards) {
            if (card.equals(Cards.herbalist)) {
                herbalistCount++;
            }
        }
        while(herbalistCount-- > 0) {
            ArrayList<TreasureCard> treasureCards = new ArrayList<TreasureCard>();
            for(Card card : context.playedCards) {
                if(card instanceof TreasureCard) {
                    treasureCards.add((TreasureCard) card);
                }
            }
            
            if(treasureCards.size() > 0) {
                TreasureCard treasureCard = herbalist_backOnDeck(context, treasureCards.toArray(new TreasureCard[0]));
                if(treasureCard != null && context.playedCards.contains(treasureCard)) {
                    context.playedCards.remove(treasureCard);
                    putOnTopOfDeck(treasureCard);
                }
            }
        }
        
        boolean alchemistPlayed = true;
        boolean potionPlayed = true;
        Card thisAlchemist = null;
        while(alchemistPlayed && potionPlayed) {
            potionPlayed = false;
            alchemistPlayed = false;
            
            for (Card card : context.playedCards) {
                if (card.equals(Cards.alchemist)) {
                    alchemistPlayed = true;
                    thisAlchemist = card;
                }
                if (card.equals(Cards.potion)) {
                    potionPlayed = true;
                }
            }
            
            if(alchemistPlayed && potionPlayed && thisAlchemist != null) {
                context.playedCards.remove(thisAlchemist);
                boolean putBackAlchemist = alchemist_backOnDeck(context);
                if (putBackAlchemist)
                    putOnTopOfDeck(thisAlchemist);
                else
                    discard(thisAlchemist, null, null, false);
                thisAlchemist = null;
            }
        }
        
        while (treasuryCardsToSave-- > 0) {
            int index = context.playedCards.indexOf(Cards.treasury);
            if(index == -1) {
                break;
            }
            Card card = context.playedCards.remove(index);
            putOnTopOfDeck(card);
        }
        
        while(context.schemesPlayed-- > 0) {
            ArrayList<Card> actions = new ArrayList<Card>();
            for(Card c : context.playedCards) {
                if(c instanceof ActionCard) {
                    actions.add(c);
                }
            }
            if(actions.size() == 0) {
                break;
            }
            
            ActionCard actionToPutBack = scheme_actionToPutOnTopOfDeck(((MoveContext) context), actions.toArray(new ActionCard[0]));
            if(actionToPutBack == null) {
                break;
            }
            int index = context.playedCards.indexOf(actionToPutBack);
            if(index == -1) {
                Util.playerError(this, "Scheme returned invalid card to put back on top of deck, ignoring");
                break;
            }
            Card card = context.playedCards.remove(index);
            putOnTopOfDeck(card);
            
        }

        while (!context.playedCards.isEmpty()) {
            discard(context.playedCards.remove(0), null, null, false);
        }
        
        if(context.getPossessedBy() != null) {
            while (!context.possessedTrashPile.isEmpty()) {
                discard(context.possessedTrashPile.remove(0), null, null, false);
            }
        }
        // /////////////////////////////////
        // Double check that deck/discard/hand all have valid cards.
        // /////////////////////////////////
        checkCardsValid();
        
    }
    
    public void debug(String msg) {
        Util.debug(this, msg, false);
    }

    public CardList getHand() {
        return hand;
    }

    public int getDeckSize() {
        return deck.size();
    }

    public int getDiscardSize() {
        return discard.size();
    }

    public CardList getNativeVillage() {
        return nativeVillage;
    }

    public CardList getIsland() {
        return island;
    }

    public int getPirateShipTreasure() {
        return pirateShipTreasure;
    }

    public int getVictoryTokens() {
        return victoryTokens;
    }

    public ArrayList<Card> getAllCards() {
        return getAllCards(null);
    }

    public ArrayList<Card> getAllCards(MoveContext context) {
        ArrayList<Card> allCards = new ArrayList<Card>();
        if (context != null) {
            for (Card card : context.getPlayedCards()) {
                allCards.add(card);
            }
        }
        for (Card card : hand) {
            allCards.add(card);
        }
        for (Card card : discard) {
            allCards.add(card);
        }
        for (Card card : deck) {
            allCards.add(card);
        }
        for (Card card : nextTurnCards) {
            allCards.add(card);
        }
        for (Card card : nativeVillage) {
            allCards.add(card);
        }
        for (Card card : haven) {
            allCards.add(card);
        }
        for (Card card : island) {
            allCards.add(card);
        }
        return allCards;
    }

    public Card peekAtDeckBottom() {
        return deck.get(deck.size() - 1);
    }

    public void removeFromDeckBottom() {
        deck.remove(deck.size() - 1);
    }

    public void putOnTopOfDeck(Card card) {
        deck.add(0, card);
    }

    public void replenishDeck() {
        while (discard.size() > 0) {
            deck.add(discard.remove(Game.rand.nextInt(discard.size())));
        }
    }
    
    public void shuffleDeck() {
        ArrayList<Card> tempDeck = new ArrayList<Card>();
        while (deck.size() > 0) {
            tempDeck.add(deck.remove(Game.rand.nextInt(deck.size())));
        }
        for(Card c : tempDeck) {
            deck.add(c);
        }
    }

    public void checkCardsValid() {
        hand.checkValid();
        discard.checkValid();
        deck.checkValid();
    }

    public void discard(Card card, Card responsible, MoveContext context) {
        discard(card, responsible, context, true);
    }
    
    
    // TODO make similar way to put cards back on the deck (remove as well?)
    public void discard(Card card, Card responsible, MoveContext context, boolean commandedDiscard) { // See rules explanation of Tunnel for what commandedDiscard means.
        if(commandedDiscard && card.equals(Cards.tunnel)) {
            MoveContext tunnelContext = context;
            if(tunnelContext == null) {
                tunnelContext = new MoveContext(game, this);
            }
            if((this).tunnel_shouldReveal(tunnelContext)) {
                reveal(card, card, tunnelContext);
                gainNewCard(Cards.gold, card, tunnelContext);
            }
        }
        discard.add(card);

        if(context != null) {
            GameEvent event = new GameEvent(GameEvent.Type.CardDiscarded, context);
            event.card = card;
            event.responsible = responsible;
            context.game.broadcastEvent(event);
        }
    }
    
    public boolean gainNewCard(Card cardToGain, Card responsible, MoveContext context) {
        Card card = game.takeFromPileCheckTrader(cardToGain, context);
        if(card != null) {
            GameEvent gainEvent = new GameEvent(GameEvent.Type.CardObtained, (MoveContext) context);
            gainEvent.card = card;
            gainEvent.responsible = responsible;
            gainEvent.newCard = true;
            
            // Check if Trader swapped the card, so it can be made responsible, putting the card in the discard
            // pile rather than were it would go otherwise (according to faq)
            if(!cardToGain.equals(card) && card.equals(Cards.silver)) {
                gainEvent.responsible = Cards.trader;
            }
            
            context.game.broadcastEvent(gainEvent);
            return true;
        }
        return false;
    }
    
    public void gainCardAlreadyInPlay(Card card, Card responsible, MoveContext context) {
        if (context != null) {
            GameEvent event = new GameEvent(GameEvent.Type.CardObtained, context);
            event.card = card;
            event.responsible = responsible;
            event.newCard = false;
            context.game.broadcastEvent(event);
        }
    }
    
    public void broadcastEvent(GameEvent event) {
        game.broadcastEvent(event);
    }

    public Card takeFromPile(Card card) {
        return game.takeFromPile(card);
    }
    
    public void trash(Card card, Card responsible, MoveContext context) {
        ((CardImpl) card).trashed = true;
        if(context != null) {
            // TODO: Track in main game event listener instead
            context.cardsTrashedThisTurn++;
        }
        GameEvent event = new GameEvent(GameEvent.Type.CardTrashed, context);
        event.card = card;
        event.responsible = responsible;
        context.game.broadcastEvent(event);
    }

    public void reveal(Card card, Card responsible, MoveContext context) {
        GameEvent event = new GameEvent(GameEvent.Type.CardRevealed, context);
        event.card = card;
        event.responsible = responsible;
        context.game.broadcastEvent(event);
    }
    
    public void attacked(Card card, MoveContext context) {
        GameEvent event = new GameEvent(GameEvent.Type.PlayerAttacking, context);
        event.attackedPlayer = this;
        event.card = card;
        context.game.broadcastEvent(event);
    }

    public static enum NoblesOption {
        AddCards,
        AddActions
    }

    public static enum TorturerOption {
        TakeCurse,
        DiscardTwoCards
    }

    public static enum MinionOption {
        AddGold,
        RolloverCards
    }

    public static enum PawnOption {
        AddCard,
        AddAction,
        AddBuy,
        AddGold
    }

    public static enum StewardOption {
        AddCards,
        AddGold,
        TrashCards
    }

    public static enum WatchTowerOption {
        TopOfDeck,
        Trash,
        Normal
    }

    public static enum JesterOption {
        GainCopy,
        GiveCopy
    }

    public static enum TournamentOption {
        DontRevealProvince,
        GainPrize,
        GainDuchy
    }

    public static enum TrustySteedOption {
        AddCards,
        AddActions,
        AddGold,
        GainSilvers
    }
    
    public static enum SpiceMerchantOption {
        AddCardsAndAction,
        AddGoldAndBuy
    }

    // Context is passed for the player to add a GameEventListener
    // if they want or to see what cards the game has, etc.
    public void newGame(MoveContext context) {
    }

    public ArrayList<TreasureCard> getTreasuresInHand() {
    	ArrayList<TreasureCard> treasures = new ArrayList<TreasureCard>();
    	
    	for (Card c : getHand())
    		if (c instanceof TreasureCard)
    			treasures.add((TreasureCard) c);
    	
    	return treasures;
    }
    
    public abstract String getPlayerName();

    public abstract Card doAction(MoveContext context);

    public abstract Card doBuy(MoveContext context);

    // ////////////////////////////////////////////
    // Card interactions - cards from the base game
    // ////////////////////////////////////////////
    public abstract Card workshop_cardToObtain(MoveContext context);

    public abstract Card feast_cardToObtain(MoveContext context);

    public abstract Card remodel_cardToTrash(MoveContext context);

    public abstract Card remodel_cardToObtain(MoveContext context, int maxCost, boolean potion);

    public abstract Card[] militia_attack_cardsToKeep(MoveContext context);

    public abstract TreasureCard thief_treasureToTrash(MoveContext context, TreasureCard[] treasures);
    
    public abstract TreasureCard[] thief_treasuresToGain(MoveContext context, TreasureCard[] treasures);

    public abstract boolean chancellor_shouldDiscardDeck(MoveContext context);

    public abstract TreasureCard mine_treasureFromHandToUpgrade(MoveContext context);

    public abstract TreasureCard mine_treasureToObtain(MoveContext context, int maxCost, boolean potion);

    public abstract Card[] chapel_cardsToTrash(MoveContext context);

    public abstract Card[] cellar_cardsToDiscard(MoveContext context);

    public abstract boolean library_shouldKeepAction(MoveContext context, ActionCard action);

    public abstract boolean spy_shouldDiscard(MoveContext context, Player targetPlayer, Card card);

    public abstract VictoryCard bureaucrat_cardToReplace(MoveContext context);

    // ////////////////////////////////////////////
    // Card interactions - cards from Intrigue
    // ////////////////////////////////////////////
    public abstract Card[] secretChamber_cardsToDiscard(MoveContext context);

    public abstract PawnOption[] pawn_chooseOptions(MoveContext context);

    public abstract TorturerOption torturer_attack_chooseOption(MoveContext context);

    public abstract StewardOption steward_chooseOption(MoveContext context);

    public abstract Card swindler_cardToSwitch(MoveContext context, int cost, boolean potion);

    public abstract Card[] steward_cardsToTrash(MoveContext context);

    public abstract Card[] torturer_attack_cardsToDiscard(MoveContext context);

    public abstract Card courtyard_cardToPutBackOnDeck(MoveContext context);

    public abstract boolean baron_shouldDiscardEstate(MoveContext context);

    public abstract Card ironworks_cardToObtain(MoveContext context);

    public abstract Card masquerade_cardToPass(MoveContext context);

    public abstract Card masquerade_cardToTrash(MoveContext context);

    public abstract boolean miningVillage_shouldTrashMiningVillage(MoveContext context);

    public abstract Card saboteur_cardToObtain(MoveContext context, int maxCost, boolean potion);

    public abstract Card[] scout_orderCards(MoveContext context, Card[] cards);

    public abstract Card[] mandarin_orderCards(MoveContext context, Card[] cards);

    public abstract NoblesOption nobles_chooseOptions(MoveContext context);

    // Either return two cards, or null if you do not want to trash any cards.
    public abstract Card[] tradingPost_cardsToTrash(MoveContext context);

    public abstract Card wishingWell_cardGuess(MoveContext context);

    public abstract Card upgrade_cardToTrash(MoveContext context);

    public abstract Card upgrade_cardToObtain(MoveContext context, int exactCost, boolean potion);

    public abstract MinionOption minion_chooseOption(MoveContext context);

    public abstract Card[] secretChamber_cardsToPutOnDeck(MoveContext context);

    // ////////////////////////////////////////////
    // Card interactions - cards from Seaside
    // ////////////////////////////////////////////
    public abstract Card[] ghostShip_attack_cardsToPutBackOnDeck(MoveContext context);

    public abstract Card salvager_cardToTrash(MoveContext context);

    public abstract int treasury_putBackOnDeck(MoveContext context, int treasuryCardsInPlay);

    public abstract Card[] warehouse_cardsToDiscard(MoveContext context);

    public abstract boolean pirateShip_takeTreasure(MoveContext context);

    public abstract TreasureCard pirateShip_treasureToTrash(MoveContext context, TreasureCard[] treasures);

    public abstract boolean nativeVillage_takeCards(MoveContext context);

    public abstract Card smugglers_cardToObtain(MoveContext context);

    public abstract Card island_cardToSetAside(MoveContext context);

    public abstract Card haven_cardToSetAside(MoveContext context);

    public abstract boolean navigator_shouldDiscardTopCards(MoveContext context, Card[] cards);

    public abstract Card[] navigator_cardOrder(MoveContext context, Card[] cards);

    public abstract Card embargo_supplyToEmbargo(MoveContext context);

    // Will be passed all three cards
    public abstract Card lookout_cardToTrash(MoveContext context, Card[] cards);

    // Will be passed the two cards leftover after trashing one
    public abstract Card lookout_cardToDiscard(MoveContext context, Card[] cards);

    public abstract Card ambassador_revealedCard(MoveContext context);

    public abstract int ambassador_returnToSupplyFromHand(MoveContext context, Card card);

    public abstract boolean pearlDiver_shouldMoveToTop(MoveContext context, Card card);

    // ////////////////////////////////////////////
    // Card interactions - cards from Alchemy
    // ////////////////////////////////////////////

    public abstract Card transmute_cardToTrash(MoveContext context);

    public abstract ArrayList<Card> apothecary_cardsForDeck(MoveContext context, ArrayList<Card> cards);

    public abstract boolean alchemist_backOnDeck(MoveContext context);

    public abstract TreasureCard herbalist_backOnDeck(MoveContext context, TreasureCard[] cards);

    public abstract Card apprentice_cardToTrash(MoveContext context);

    public abstract ActionCard university_actionCardToObtain(MoveContext context);

    public abstract boolean scryingPool_shouldDiscard(MoveContext context, Player targetPlayer, Card card);

    public abstract ActionCard[] golem_cardOrder(MoveContext context, ActionCard[] cards);

    // ////////////////////////////////////////////
    // Card interactions - cards from Prosperity
    // ////////////////////////////////////////////
    public abstract Card bishop_cardToTrashForVictoryTokens(MoveContext context);

    public abstract Card bishop_cardToTrash(MoveContext context);

    public abstract Card contraband_cardPlayerCantBuy(MoveContext context);

    public abstract Card expand_cardToTrash(MoveContext context);

    public abstract Card expand_cardToObtain(MoveContext context, int maxCost, boolean potion);

    public abstract Card[] forge_cardsToTrash(MoveContext context);

    public abstract Card forge_cardToObtain(MoveContext context, int exactCost);

    public abstract Card[] goons_attack_cardsToKeep(MoveContext context);

    public abstract ActionCard kingsCourt_cardToPlay(MoveContext context);
    
    public abstract ActionCard throneRoom_cardToPlay(MoveContext context);   

    public abstract boolean loan_shouldTrashTreasure(MoveContext context, TreasureCard treasure);

    public abstract TreasureCard mint_treasureToMint(MoveContext context);

    public abstract boolean mountebank_attack_shouldDiscardCurse(MoveContext context);

    public abstract Card[] rabble_attack_cardOrder(MoveContext context, Card[] cards);

    public abstract boolean royalSeal_shouldPutCardOnDeck(MoveContext context, Card card);

    public abstract Card tradeRoute_cardToTrash(MoveContext context);

    public abstract Card[] vault_cardsToDiscardForGold(MoveContext context);

    public abstract Card[] vault_cardsToDiscardForCard(MoveContext context);

    public abstract WatchTowerOption watchTower_chooseOption(MoveContext context, Card card);

    public abstract ArrayList<TreasureCard> treasureCardsToPlayInOrder(MoveContext context);

    // ////////////////////////////////////////////
    // Card interactions - cards from Cornucopia
    // ////////////////////////////////////////////
    public abstract Card hamlet_cardToDiscardForAction(MoveContext context);

    public abstract Card hamlet_cardToDiscardForBuy(MoveContext context);

    public abstract Card hornOfPlenty_cardToObtain(MoveContext context, int maxCost);

    public abstract Card[] horseTraders_cardsToDiscard(MoveContext context);

    public abstract JesterOption jester_chooseOption(MoveContext context, Player targetPlayer, Card card);

    public abstract Card remake_cardToTrash(MoveContext context);

    public abstract Card remake_cardToObtain(MoveContext context, int exactCost, boolean potion);

    public abstract TournamentOption tournament_chooseOption(MoveContext context);

    public abstract Card tournament_choosePrize(MoveContext context);

    public abstract Card[] youngWitch_cardsToDiscard(MoveContext context);

    public abstract Card[] followers_attack_cardsToKeep(MoveContext context);

    public abstract TrustySteedOption[] trustySteed_chooseOptions(MoveContext context);

    // ////////////////////////////////////////////
    // Card interactions - cards from Hinterlands
    // ////////////////////////////////////////////
    public abstract Card borderVillage_cardToObtain(MoveContext context);

    public abstract Card farmland_cardToTrash(MoveContext context);

    public abstract Card farmland_cardToObtain(MoveContext context, int cost, boolean potion);
    
    public abstract TreasureCard stables_treasureToDiscard(MoveContext context);
    
    public abstract boolean duchess_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card);
    
    public abstract boolean duchess_shouldGainBecauseOfDuchy(MoveContext context);
    
    public abstract Card develop_cardToTrash(MoveContext context);
    
    public abstract Card develop_lowCardToGain(MoveContext context, int cost);
    
    public abstract Card develop_highCardToGain(MoveContext context, int cost);
    
    public abstract Card[] develop_orderCards(MoveContext context, Card[] cards);
    
    public abstract Card oasis_cardToDiscard(MoveContext context);
    
    public abstract boolean foolsGold_shouldTrash(MoveContext context);

    public abstract TreasureCard nobleBrigand_silverOrGoldToTrash(MoveContext context, TreasureCard[] silverOrGoldCards);

    public abstract boolean jackOfAllTrades_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card);
    
    public abstract Card jackOfAllTrades_nonTreasureToTrash(MoveContext context);
    
    public abstract TreasureCard spiceMerchant_treasureToTrash(MoveContext context);    

    public abstract SpiceMerchantOption spiceMerchant_chooseOption(MoveContext context);    

    public abstract Card[] embassy_cardsToDiscard(MoveContext context);

    public abstract Card[] cartographer_cardsFromTopOfDeckToDiscard(MoveContext context, Card[] cards);

    public abstract Card[] cartographer_cardOrder(MoveContext context, Card[] cards);
    
    public abstract ActionCard scheme_actionToPutOnTopOfDeck(MoveContext context, ActionCard[] actions);
    
    public abstract boolean tunnel_shouldReveal(MoveContext context);

    public abstract boolean trader_shouldGainSilverInstead(MoveContext context, Card card);
    
    public abstract Card trader_cardToTrash(MoveContext context);
    
    public abstract boolean oracle_shouldDiscard(MoveContext context, Player player, ArrayList<Card> cards);

    public abstract Card[] oracle_orderCards(MoveContext context, Card[] cards);
    
    public abstract boolean illGottenGains_gainCopper(MoveContext context);
    
    public abstract Card haggler_cardToObtain(MoveContext context, int maxCost, boolean potion);
    
    public abstract Card[] inn_cardsToDiscard(MoveContext context);
    
    public abstract boolean inn_shuffleCardBackIntoDeck(MoveContext context, ActionCard card);

    public abstract Card mandarin_cardToReplace(MoveContext context);
    
    public abstract Card[] margrave_attack_cardsToKeep(MoveContext context);

    public int getMyCardCount(Card card) {
        return Util.getCardCount(getAllCards(), card);
    }

	public abstract Card getAttackReaction(MoveContext context, Card responsible);
	
	public abstract boolean revealBane(MoveContext context);

}