package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.Card;
import com.vdom.core.Cards.Kind;

public class CardImplAlchemy extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplAlchemy(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplAlchemy() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
		switch(getKind()) {
		case Apothecary:
            apothecary(game, context, currentPlayer);
            break;
		case Apprentice:
            apprentice(game, context, currentPlayer);
            break;
		case Familiar:
            witchFamiliar(game, context, currentPlayer);
            break;
        case Golem:
            golem(game, context, currentPlayer);
            break;
        case PhilosophersStone:
        	context.addCoins((currentPlayer.getDeckSize() + currentPlayer.getDiscardSize()) / 5);
        	break;
        case Possession:
            possession(context);
            break;
        case ScryingPool:
            spyAndScryingPool(game, context, currentPlayer);
            break;
        case Transmute:
            transmute(context, currentPlayer);
            break;
        case University:
            university(context, currentPlayer);
            break;
		default:
			break;
		}
	}
	
	private void apothecary(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> residue = new ArrayList<Card>();
        for (int i = 1; i <= 4; i++) {
            Card card = game.draw(context, Cards.apothecary, 4 - i);
            if (card != null) {
                currentPlayer.reveal(card, this.getControlCard(), context);
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
	
	private void apprentice(Game game, MoveContext context, Player currentPlayer) {
        if(currentPlayer.hand.size() > 0) {
            Card cardToTrash = currentPlayer.controlPlayer.apprentice_cardToTrash(context);
            if (cardToTrash == null || !currentPlayer.hand.contains(cardToTrash)) {
                Util.playerError(currentPlayer, "Apprentice card to trash was invalid, trashing random card.");
                cardToTrash = Util.randomCard(currentPlayer.hand);
            }
            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this.getControlCard(), context);

            int cardsToDraw = (cardToTrash.getCost(context) + (cardToTrash.costPotion() ? 2 : 0));
            for (int i = 1; i <= cardsToDraw; i++) {
                game.drawToHand(context, this, cardsToDraw - i);
            }
        }
    }
	
	private void golem(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        ArrayList<Card> toOrder = new ArrayList<Card>();

        while (toOrder.size() < 2) {
            Card draw = game.draw(context, Cards.golem, -1);
            if (draw == null) {
                break;
            }

            currentPlayer.reveal(draw, this.getControlCard(), context);

            if (draw.is(Type.Action, currentPlayer) && !draw.equals(Cards.golem)) {
                toOrder.add(draw);
                // currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.getControlCard(), null);
        }

        if (!toOrder.isEmpty()) {
            Card[] toPlay;

            if (toOrder.size() == 1) {
                toPlay = toOrder.toArray(new Card[toOrder.size()]);
            } else {
                Card[] playOrder = currentPlayer.controlPlayer.golem_cardOrder(context, toOrder.toArray(new Card[toOrder.size()]));

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
                card.play(game, context, false);
            }
            context.freeActionInEffect--;context.golemInEffect--;
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
    
    private void transmute(MoveContext context, Player currentPlayer) {
        Card cardToTrash = currentPlayer.controlPlayer.transmute_cardToTrash(context);
        if (cardToTrash == null) {
            Util.playerError(currentPlayer, "Transmute card to trash was null, not trashing anything.");
        } else if (!currentPlayer.hand.contains(cardToTrash)) {
            Util.playerError(currentPlayer, "Transmute card to trash is not in your hand, not trashing anything.");
        } else {
            currentPlayer.hand.remove(cardToTrash);
            currentPlayer.trash(cardToTrash, this.getControlCard(), context);
            if (cardToTrash.is(Type.Action, cardToTrash.behaveAsCard().getKind() == Kind.Fortress ? currentPlayer : null )) {
            	//Condition is wrong for when player is being possessed and Fortress is set aside
                currentPlayer.gainNewCard(Cards.duchy, this.getControlCard(), context);
            }
            if (cardToTrash.is(Type.Treasure)) {	
                currentPlayer.gainNewCard(Cards.transmute, this.getControlCard(), context);
            }
            if (cardToTrash.is(Type.Victory)) {
                currentPlayer.gainNewCard(Cards.gold, this.getControlCard(), context);
            }
        }
    }
    
    private void university(MoveContext context, Player currentPlayer) {
        Card cardToObtain = currentPlayer.controlPlayer.university_actionCardToObtain(context);
        if (cardToObtain != null && cardToObtain.is(Type.Action, null) && cardToObtain.getCost(context) <= 5 && !cardToObtain.costPotion()) {
            currentPlayer.gainNewCard(cardToObtain, this.getControlCard(), context);
        }
    }
}
