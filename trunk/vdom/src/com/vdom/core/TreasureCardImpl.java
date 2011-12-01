package com.vdom.core;

import java.util.ArrayList;
import java.util.HashSet;

import com.vdom.api.Card;
import com.vdom.api.Cards;
import com.vdom.api.GameEvent;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;

public class TreasureCardImpl extends CardImpl implements TreasureCard {
    public TreasureCardImpl(String name, int cost, int value, boolean costPotion, boolean providesPotion) {
        super(name, cost);
        this.value = value;
        this.costPotion = costPotion;
        this.providesPotion = providesPotion;
    }

    protected TreasureCardImpl() {
    }
    
    public TreasureCardImpl setIsPrize() {
        isPrize = true;
        return this;
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

    public boolean providesPotion() {
        return providesPotion;
    }

    protected void copyValues(TreasureCardImpl c) {
        super.copyValues(c);
        c.value = value;
        c.providesPotion = providesPotion;
    }

    int value;
    boolean providesPotion;
    
    @Override
    public void playTreasure(MoveContext context) {
        Player player = context.player;
        context.treasuresPlayedSoFar++;
        Game game = context.game;

        
        if (equals(Cards.copper)) {
            context.copperPlayed = true;
            context.gold += context.coppersmithsPlayed;
        } else if (equals(Cards.foolsGold)) {
            context.foolsGoldPlayed++;
        } else if (equals(Cards.philosophersStone)) {
            context.gold += (player.getDeckSize() + player.getDiscardSize()) / 5;
        } else if (equals(Cards.bank)) {
            context.gold += context.treasuresPlayedSoFar;
        } else if (equals(Cards.hoard)) {
            context.hoardsPlayed++;
        } else if (equals(Cards.loan)) {
            ArrayList<Card> toDiscard = new ArrayList<Card>();
            TreasureCard treasureCardFound = null;

            while (treasureCardFound == null) {
                Card draw = game.draw(player);
                if (draw == null) {
                    break;
                }

                GameEvent event = new GameEvent(GameEvent.Type.CardRevealed, context);
                event.card = draw;
                game.broadcastEvent(event);

                if (draw instanceof TreasureCard) {
                    treasureCardFound = (TreasureCard) draw;
                } else {
                    toDiscard.add(draw);
                }
            }

            if (treasureCardFound != null) {
                if (player.loan_shouldTrashTreasure(context, treasureCardFound)) {
                    context.cardsTrashedThisTurn++;
                    GameEvent event = new GameEvent(GameEvent.Type.CardTrashed, context);
                    event.card = treasureCardFound;
                    event.responsible = this;
                    game.broadcastEvent(event);
                } else {
                    player.discard(treasureCardFound, this, null);
                }
            }

            while (!toDiscard.isEmpty()) {
                player.discard(toDiscard.remove(0), this, null);
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
        }

        if (providesPotion()) {
            context.potions++;
        }

        context.gold += getValue();

        if (equals(Cards.foolsGold) && context.foolsGoldPlayed > 1) {
            context.gold += 3;
        }
        
        player.hand.remove(this);
        context.playedCards.add(this);

        GameEvent event = new GameEvent(GameEvent.Type.PlayingCoin, (MoveContext) context);
        event.card = this;
        game.broadcastEvent(event);

        if (equals(Cards.contraband)) {
            context.buys++;
            Card cantBuyCard = game.getNextPlayer().contraband_cardPlayerCantBuy(context);

            if (cantBuyCard != null && !context.cantBuy.contains(cantBuyCard)) {
                context.cantBuy.add(cantBuyCard);
                GameEvent e = new GameEvent(GameEvent.Type.CantBuy, (MoveContext) context);
                game.broadcastEvent(e);
            }
        } else if (equals(Cards.venture)) {
            ArrayList<Card> toDiscard = new ArrayList<Card>();
            TreasureCard treasureCardFound = null;

            while (treasureCardFound == null) {
                Card draw = game.draw(player);
                if (draw == null) {
                    break;
                }

                if (draw instanceof TreasureCard) {
                    treasureCardFound = (TreasureCard) draw;
                } else {
                    toDiscard.add(draw);
                }
            }

            if (treasureCardFound != null) {
                player.hand.add(treasureCardFound);
                event = new GameEvent(GameEvent.Type.CardRevealed, context);
                event.card = treasureCardFound;
                game.broadcastEvent(event);
                treasureCardFound.playTreasure(context);
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
            if(context.getCardsLeft(Cards.copper) > 0) {
                if(player.illGottenGains_gainCopper(context)) {
                    player.gainNewCard(Cards.copper, this, context);
                }
            }
        }
    }
}
