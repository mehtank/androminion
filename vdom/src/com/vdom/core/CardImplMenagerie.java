package com.vdom.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.vdom.api.Card;

public class CardImplMenagerie extends CardImpl {
	private static final long serialVersionUID = 1L;

	public CardImplMenagerie(CardImpl.Builder builder) {
		super(builder);
	}

	protected CardImplMenagerie() { }

	@Override
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer, boolean isThronedEffect) {
		switch(getKind()) {
		case BountyHunter:
			bountyHunter(game, context, currentPlayer);
			break;
		case Coven:
			coven(game, context, currentPlayer);
			break;
		case Horse:
			horse(game, context, currentPlayer);
			break;
		case Livery:
			livery(game, context, currentPlayer);
			break;
		case SnowyVillage:
			snowyVillage(game, context, currentPlayer);
			break;
		case Stockpile:
			stockpile(game, context, currentPlayer);
			break;
		case Supplies:
			supplies(game, context, currentPlayer);
			break;
		default:
			break;
		}
	}
	
	public void isBuying(MoveContext context) {
		super.isBuying(context);
        switch (this.getControlCard().getKind()) {
        case Alliance:
        	alliance(context);
        	break;
        case Commerce:
        	commerce(context);
        	break;
        case Populate:
        	populate(context);
        	break;
        case Toil:
        	toil(context);
        	break;
        default:
            break;
        }
        
        // test if prince lost track of any cards
        context.player.princeCardLeftThePlay(context.player, context);
    }
	
	@Override
	public void isTrashed(MoveContext context) {
		Cards.Kind trashKind = this.getControlCard().getKind();
    	if (this.getControlCard().equals(Cards.estate) && context.player.getInheritance() != null) {
    		trashKind = context.player.getInheritance().getKind();
    	}
    	
    	switch (trashKind) {
    	
        default:
            break;
	    }
	    
	    // card left play - stop any impersonations
	    this.getControlCard().stopImpersonatingCard();
	    this.getControlCard().stopInheritingCardAbilities();
	}
	
	private void bountyHunter(Game game, MoveContext context, Player player) {
		CardList hand = player.getHand();
		if(hand.size() == 0)
			return;
        Card exileCard = player.controlPlayer.bountyHunter_cardToExile(context);
        if (exileCard == null || !player.hand.contains(exileCard)) {
            Util.playerError(player, "Bounty Hunter card to exile invalid, picking one");
            exileCard = hand.get(0);
        }
        boolean hasCopyInExile = false;
        for (Card card : player.exile) {
        	if (card.equals(exileCard)){
        		hasCopyInExile = true;
        		return;
        	}
        }
        player.exileFromHand(exileCard, this.getControlCard(), context);
        if (!hasCopyInExile) {
        	context.addCoins(3, Cards.bountyHunter);
        }
	}
	
	private void coven(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
    	for (Player player : context.game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(context.game, player, this)) {
            	attackedPlayers.add(player);
            }
    	}
		
    	for (Player player : attackedPlayers) {
			player.attacked(this.getControlCard(), context);
            MoveContext playerContext = new MoveContext(game, player);
            playerContext.attackedPlayer = player;
            if (!player.exileFromSupply(Cards.curse, this.getControlCard(), playerContext)) {
            	ArrayList<Card> toDiscard = new ArrayList<Card>();
            	Iterator<Card> iterator = player.exile.iterator();
            	while(iterator.hasNext()) {
            		Card c = iterator.next();
            		if (c.equals(Cards.curse)) {
            			iterator.remove();
            			toDiscard.add(c);
            		}
            	}
            	for(Card card: toDiscard) {
            		player.discard(card, Cards.coven, playerContext);
            	}
            }
        }
	}
	
	private void horse(Game game, MoveContext context, Player player) {
		Card card = this.getControlCard();
    	int idx = player.playedCards.indexOf(card.getId());
    	if (idx == -1) return;
    	card = player.playedCards.remove(idx); 
    	CardPile pile = game.getGamePile(card);
        pile.addCard(card);	
	}
	
	private void livery(Game game, MoveContext context, Player player) {
		context.liveryEffects++;
	}
	
	private void snowyVillage(Game game, MoveContext context, Player player) {
		context.ignorePlusActions = true;
	}
	
	private void stockpile(Game game, MoveContext context, Player player) {
		player.exileFromPlay(this, Cards.stockpile, context);
	}
	
	private void supplies(Game game, MoveContext context, Player player) {
		context.getPlayer().gainNewCard(Cards.horse, this.getControlCard(), context);
	}
	
	private void alliance(MoveContext context) {
		context.getPlayer().gainNewCard(Cards.province, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.duchy, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.estate, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.gold, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.silver, this.getControlCard(), context);
		context.getPlayer().gainNewCard(Cards.copper, this.getControlCard(), context);
	}
		
	private void commerce(MoveContext context) {
		int numGolds = new HashSet<Card>(context.game.getCardsObtainedByPlayer()).size();
		for (int i = 0; i < numGolds; ++i)
			context.getPlayer().gainNewCard(Cards.gold, this.getControlCard(), context);
	}
	
	private void populate(MoveContext context) {
		Card[] actionPiles = context.game.getCardsInGame(GetCardsInGameOptions.Placeholders, true, Type.Action);
		for(Card card : actionPiles)
			context.getPlayer().gainNewCard(card, this.getControlCard(), context);
	}
	
	private void toil(MoveContext context) {
		Player player = context.getPlayer();
		ArrayList<Card> validCards = new ArrayList<Card>();
		for (Card c : player.hand) {
			if (c.is(Type.Action, player)) {
				validCards.add(c);
			}
		}
		if (validCards.isEmpty()) return;
		Card card = player.controlPlayer.toil_cardToPlay(context);
		if (card == null) return;
		if (!validCards.contains(card)) {
			Util.playerError(player, "Toil error, invalid card selected, ignoring");
			return;
		}
		context.freeActionInEffect++;
        card.play(context.game, context, true);
        context.freeActionInEffect--;
	}
}
