package com.vdom.api;

import com.vdom.core.MoveContext;
import com.vdom.core.Player;

public class GameEvent {
    public enum EventType {
        GameStarting, // A new game is starting, called at the start of each game when multiple are played
        GameOver, // Game completed
        Embargo, // Embargo added to card

        Status, // Sent before playing an action or buying a card for UI to show action/buy/coin status

        CantBuy, // Card that can't be bought (ie. named in playing Contraband)
        VictoryPoints, // VictoryPoints at the end of the game
        NewHand, // Player gets a NewHand
        TurnBegin, // Player begins a turn
        TurnEnd, // Player's turn ends

        PlayingCard, // Card is about to be played by a player.
        PlayedCard, // Card has just been played by a player.
        PlayingDurationAction, // Duration action card's next turn effects are about to occur for a player.
        BuyingCard, // Buying a card in the buy phase.
        OverpayForCard, // Overpaying for a Guilds card.
        GuildsTokenObtained, // Recieve a Guilds token
        GuildsTokenSpend, // Spend Guilds token
        NoBuy, // Player didn't buy a card
        DeckReplenished, // Discard pile shuffled to create a new deck for one of the players
        PlayerAttacking, // Player is attacking another player
        PlayerDefended, // Player defended an attack
        CardOnTopOfDeck, // Card was placed on top of deck
        CardObtained, // Card was obtained by a player through an effect of an action
        CardTrashed, // Card removed from the game
        CardRevealed, // Card revealed by an action
        CardRevealedFromHand, // Card revealed from the hand by an action
        CardNamed, // Card named by player

        CardDiscarded, // A card was discarded
        CardAddedToHand, // A new card has been added to a player's hand
        CardRemovedFromHand, // A card has been removed from a player's hand        
        CardSetAside, // A card was set aside (prince)
        CardSetAsideSummon, // A card was set aside (Summon)
        CardSetAsideGear, // A card was set aside (gear)
        CardSetAsideHaven, // A card was set aside (haven)
        CardSetAsideSave, // A card was set aside (save)
        CardSetAsideOnTavernMat, // A card was set aside on tavern mat
        CardSetAsideArchive, // A card was set aside (archive)
        CallingCard, // A card is about to be called from the tavern mat
        CalledCard, // A card was called from the tavern mat
        CardSetAsideOnIslandMat, // A card was set aside on island mat
        CardSetAsideInheritance, // A card was set aside with Inheritance
        DeckPutIntoDiscardPile, // Deck put into discard pile
        TravellerExchanged, // traveller exchanged
        TurnJourneyTokenFaceUp, // journey token turned face up
        TurnJourneyTokenFaceDown, // journey token turned face down
        MinusOneCoinTokenOn, // -1 Coin token placed
        MinusOneCoinTokenOff, // -1 Coin token removed
        MinusOneCardTokenOn, // -1 Card token put onto deck
        MinusOneCardTokenOff, // -1 Card token drawn from deck
        PlusOneCardTokenMoved, // +1 Card token moved to supply pile
        PlusOneActionTokenMoved, // +1 Action token moved to supply pile
        PlusOneBuyTokenMoved, // +1 Buy token moved to supply pile
        PlusOneCoinTokenMoved, // +1 Coin token moved to supply pile
        MinusTwoCostTokenMoved, // -2 Cost Token moved to supply pile
        TrashingTokenMoved, // Trashing token moved to supply pile
        DebtTokensObtained, // Obtained Debt tokens
        DebtTokensPaidOff, // Paid off Debt tokens
        DebtTokensPutOnPile, // Debt tokens put on a card pile
        DebtTokensTakenFromPile, // Debt tokens taken from a card pile
        VPTokensObtained, // VP tokens taken by a player
        VPTokensPutOnPile, // VP tokens put on a card pile
        VPTokensTakenFromPile, // VP tokens taken from a card pile
        MountainPassBid, // Someone placed a bid for Mountain Pass (or passed their bid)
        MountainPassWinner, // Mountain Pass bidding finished (winning bid/player) - 0 amount means no bids were placed
    }

    public EventType type;
    public MoveContext context;
    public Player player;

    // //////////////////////////////////////////////
    // Optional fields that may be field depending
    // on the type of event
    // //////////////////////////////////////////////

    public Card card;
    private boolean cardPrivate;
    public Card responsible;
    public Player attackedPlayer;
    public boolean newCard;
    private String comment;
    private int amount;
    

    // //////////////////////////////////////////////
    //
    // //////////////////////////////////////////////

    public GameEvent(EventType type, MoveContext context) {
        this.type = type;
        this.context = context;
        this.player = context == null ? null : context.getPlayer();
    }

    public EventType getType() {
        return type;
    }

    public MoveContext getContext() {
        return context;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * @param player the player to set
     */
    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getAttackedPlayer() {
        return attackedPlayer;
    }

    public Card getCard() {
        return card;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isCardPrivate() {
    	return cardPrivate;
    }
    
    public void setPrivate(boolean value) {
    	cardPrivate = value;
    }
    
    public int getAmount() {
    	return amount;
    }
    
    public void setAmount(int value) {
    	amount = value;
    }
    
}
