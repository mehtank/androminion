package com.vdom.core;

import java.util.ArrayList;
import java.util.HashSet;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;

public class TreasureCardImpl extends CardImpl implements TreasureCard {
    int value;
    boolean providePotion;
    
    public TreasureCardImpl(String name, int cost, int value) {
        super(name, cost);
        this.value = value;
    }

    public TreasureCardImpl(Builder builder) {
        super(builder);
        value = builder.value;
        providePotion = builder.providePotion;
    }

    public static class Builder extends CardImpl.Builder {
        protected int value;
        protected boolean providePotion = false;

        public Builder(String name, int cost, int value) {
            super(name, cost);
            this.value = value;
        }

        public Builder providePotion() {
            providePotion = true;
            return this;
        }

        public TreasureCardImpl build() {
            return new TreasureCardImpl(this);
        }

    }

    protected TreasureCardImpl() {
    }
    
    public TreasureCardImpl(String name, int cost, int value2, boolean costPotion, boolean b) {
    }

    public int getValue() {
        return value;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        TreasureCardImpl c = new TreasureCardImpl();
        copyValues(c);
        return c;
    }

    public boolean providePotion() {
        return providePotion;
    }

    protected void copyValues(TreasureCardImpl c) {
        super.copyValues(c);
        c.value = value;
        c.providePotion = providePotion;
    }

    @Override
    // return true if Treasure cards should be re-evaluated since might affect
    // coin play order
    public boolean playTreasure(MoveContext context) {
        boolean reevaluateTreasures = false;
        Player player = context.player;
        Game game = context.game;

        GameEvent event = new GameEvent(GameEvent.Type.PlayingCoin, (MoveContext) context);
        event.card = this;
        game.broadcastEvent(event);

        player.hand.remove(this);
        context.playedCards.add(this);

        context.treasuresPlayedSoFar++;
        context.gold += getValue();
        if (providePotion()) {
            context.potions++;
        }

        // Special cards
        if (equals(Cards.foolsGold)) {
            context.foolsGoldPlayed++;
            if (context.foolsGoldPlayed > 1) {
                context.gold += 3;
            }
        } else if (equals(Cards.quarry)) {
            context.quarriesPlayed++;
        } else if (equals(Cards.philosophersStone)) {
            context.gold += (player.getDeckSize() + player.getDiscardSize()) / 5;
        } else if (equals(Cards.royalSeal)) {
            context.royalSealPlayed = true;
        } else if (equals(Cards.talisman)) {
            context.talismansPlayed++;
        } else if (equals(Cards.diadem)) {
            context.gold += context.getActionsLeft();
        } else if (equals(Cards.copper)) {
            context.copperPlayed = true;
            context.gold += context.coppersmithsPlayed;
        } else if (equals(Cards.bank)) {
            context.gold += context.treasuresPlayedSoFar;
        } else if (equals(Cards.hoard)) {
            context.hoardsPlayed++;
        } else if (equals(Cards.contraband)) {
            context.buys++;
            Card cantBuyCard = game.getNextPlayer().contraband_cardPlayerCantBuy(context);

            if (cantBuyCard != null && !context.cantBuy.contains(cantBuyCard)) {
                context.cantBuy.add(cantBuyCard);
                GameEvent e = new GameEvent(GameEvent.Type.CantBuy, (MoveContext) context);
                game.broadcastEvent(e);
            }
            reevaluateTreasures = true;
        } else if (equals(Cards.loan) || equals(Cards.venture)) {
            ArrayList<Card> toDiscard = new ArrayList<Card>();
            TreasureCard treasureCardFound = null;

            while (treasureCardFound == null) {
                Card draw = game.draw(player);
                if (draw == null) {
                    break;
                }

                event = new GameEvent(GameEvent.Type.CardRevealed, context);
                event.card = draw;
                game.broadcastEvent(event);

                if (draw instanceof TreasureCard) {
                    treasureCardFound = (TreasureCard) draw;
                } else {
                    toDiscard.add(draw);
                }
            }

            if (treasureCardFound != null) {
                if (equals(Cards.loan)) {
                    if (player.loan_shouldTrashTreasure(context, treasureCardFound)) {
                        player.trash(treasureCardFound, this, context);
                    } else {
                        player.discard(treasureCardFound, this, null);
                    }
                } else if (equals(Cards.venture)) {
                    player.hand.add(treasureCardFound);
                    treasureCardFound.playTreasure(context);
                    reevaluateTreasures = true;
                }
            }

            while (!toDiscard.isEmpty()) {
                player.discard(toDiscard.remove(0), this, null);
            }

        } else if (equals(Cards.hornOfPlenty)) {
            HashSet<String> distinctCardsInPlay = new HashSet<String>();
            distinctCardsInPlay.add(getName());

            for (Card cardInPlay : context.playedCards) {
                distinctCardsInPlay.add(cardInPlay.getName());
            }
            for (Card cardInPlay : player.nextTurnCards) {
                distinctCardsInPlay.add(cardInPlay.getName());
            }

            int maxCost = distinctCardsInPlay.size();
            Card toObtain = player.hornOfPlenty_cardToObtain(context, maxCost);
            if (toObtain != null) {
                // check cost
                if (toObtain.getCost(context) <= maxCost) {
                    toObtain = game.takeFromPile(toObtain);
                    // could still be null here if the pile is empty.
                    if (toObtain != null) {
                        event = new GameEvent(GameEvent.Type.CardObtained, context);
                        event.card = toObtain;
                        event.responsible = this;
                        game.broadcastEvent(event);
                        
                        if (toObtain instanceof VictoryCard) {
                            context.playedCards.remove(this);
                            context.cardsTrashedThisTurn++;
                            event = new GameEvent(GameEvent.Type.CardTrashed, context);
                            event.card = this;
                            game.broadcastEvent(event);
                        }
                    }
                }
            }
        } else if (equals(Cards.illGottenGains)) {
            if (context.getCardsLeft(Cards.copper) > 0) {
                if (player.illGottenGains_gainCopper(context)) {
                    player.gainNewCard(Cards.copper, this, context);
                    reevaluateTreasures = true;
                }
            }
        }

        return reevaluateTreasures;
    }
}
