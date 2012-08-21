package com.vdom.core;

import com.vdom.api.Card;
import com.vdom.api.VictoryCard;

public class VictoryCardImpl extends CardImpl implements VictoryCard {
    public VictoryCardImpl(Cards.Type type, int cost, int vp) {
        super(type, cost);
        this.vp = vp;
    }

    protected VictoryCardImpl(Builder builder) {
        super(builder);
    }

    public static class Builder extends CardImpl.Builder {
        public Builder(Cards.Type type, int cost, int vp) {
            super(type, cost);
            this.vp = vp;
        }

        public VictoryCardImpl build() {
            return new VictoryCardImpl(this);
        }

    }

    public int getVictoryPoints() {
        return vp;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        VictoryCardImpl c = new VictoryCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(VictoryCardImpl c) {
        super.copyValues(c);
    }

    protected VictoryCardImpl() {
    }

    @Override
    public void isBought(MoveContext context) {
    	if (this.equals(Cards.farmland)) {
            Player player = context.getPlayer();
        	if(player.getHand().size() > 0) {
                Card cardToTrash = player.farmland_cardToTrash((MoveContext) context);

                if (cardToTrash == null) {
                    Util.playerError(player, "Farmland did not return a card to trash, trashing random card.");
                    cardToTrash = Util.randomCard(player.hand);
                }

                int cost = -1;
                boolean potion = false;
                for (int i = 0; i < player.hand.size(); i++) {
                    Card playersCard = player.hand.get(i);
                    if (playersCard.equals(cardToTrash)) {
                        cost = playersCard.getCost(context);
                        potion = playersCard.costPotion();
                        playersCard = player.hand.remove(i);

                        player.trash(playersCard, this, (MoveContext) context);
                        break;
                    }
                }

                if (cost == -1) {
                    Util.playerError(player, "Farmland returned invalid card, ignoring.");
                }
                else {
                    cost += 2;

                    boolean validCard = false;
                    
                    for(Card c : context.getCardsInGame()) {
                        if(c.getCost(context) == cost && c.costPotion() == potion && context.getCardsLeftInPile(c) > 0) {
                            validCard = true;
                            break;
                        }
                    }

                    if(validCard) {
                        Card card = player.farmland_cardToObtain((MoveContext) context, cost, potion);
                        if (card != null) {
                            // check cost
                            if (card.getCost(context) != cost || card.costPotion() != potion) {
                                Util.playerError(player, "Farmland card to obtain returned an invalid card, ignoring.");
                            }
                            else
                            {
                                if(!player.gainNewCard(card, this, (MoveContext) context)) {
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
    	}
    }
}
