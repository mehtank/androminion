package com.vdom.api;

import com.vdom.core.MoveContext;
import com.vdom.core.Player;

public class GameEvent {
    public enum Type {
        GameStarting, // A new game is starting, called at the start of each game when multiple are played
        GameOver, // Game completed
        Embargo, // Embargo added to card

        Status, // Sent before playing an action or buying a card for UI to show action/buy/coin status

        CantBuy, // Card that can't be bought (ie. named in playing Contraband)
        VictoryPoints, // VictoryPoints at the end of the game
        NewHand, // Player gets a NewHand
        TurnBegin, // Player begins a turn
        TurnEnd, // Player's turn ends

        PlayingAction, // Action card is about to be played by a player.
        PlayedAction, // Action card has just been played by a player.
        PlayingDurationAction, // Duration action card's next turn effects are about to occur for a player.
        PlayingCoin, // Coin card is about to be played by a player.
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
        CardSetAsideOnIslandMat, // A card was set aside on island mat
        DeckPutIntoDiscardPile, // Deck put into discard pile
    }

    public Type type;
    public MoveContext context;
    public Player player;

    // //////////////////////////////////////////////
    // Optional fields that may be field depending
    // on the type of event
    // //////////////////////////////////////////////

    public Card card;
    public Card responsible;
    public Player attackedPlayer;
    public boolean newCard;
    private String comment;

    // //////////////////////////////////////////////
    //
    // //////////////////////////////////////////////

    public GameEvent(Type type, MoveContext context) {
        this.type = type;
        this.context = context;
        this.player = context == null ? null : context.getPlayer();
    }

    public Type getType() {
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


}
