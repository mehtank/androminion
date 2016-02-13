package com.vdom.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.EventCard;
import com.vdom.api.GameEvent;
import com.vdom.api.TreasureCard;
import com.vdom.core.Player.QuestOption;

public class EventCardImpl extends CardImpl implements EventCard {
    protected int addBuys;

	protected EventCardImpl(Builder builder) {
        super(builder);
        addBuys = builder.addBuys;
    }

    public static class Builder extends CardImpl.Builder {
        protected int addBuys;
        public Builder(Cards.Type type, int cost) {
            super(type, cost);
        }

        public Builder addBuys(int val) {
            addBuys = val;
            return this;
        }
        
        public CardImpl build() {
            return new EventCardImpl(this);
        }
    }

    public int getAddBuys() {
        return addBuys;
    }

    @Override
    public CardImpl instantiate() {
        checkInstantiateOK();
        EventCardImpl c = new EventCardImpl();
        copyValues(c);
        return c;
    }

    protected void copyValues(EventCardImpl c) {
        super.copyValues(c);
        c.addBuys = addBuys;
    }

    protected EventCardImpl() {
    }
    
    @Override
    public void isBuying(MoveContext context) {
        context.buys += addBuys;
        switch (this.controlCard.getType()) {
	        case Alms:
	        	alms(context);
                break;
	        case Ball:
	        	ball(context);
	        	break;
	        case Bonfire:
	        	bonfire(context);
                break;
	        case Borrow:
	        	borrow(context);
                break;
	        case Expedition:
	        	context.totalExpeditionBoughtThisTurn += 2;
                break;
	        case Ferry:
	        	ferry(context);
        		break;
	        case Inheritance:
	        	inheritance(context);
	        	break;
	        case LostArts:
	        	lostArts(context);
	        	break;
	        case Mission:
	        	mission(context);
	        	break;
	        case Pathfinding:
	        	pathfinding(context);
	        	break;
	        case Pilgrimage:
	        	pilgrimage(context);
	        	break;
	        case Plan:
	        	plan(context);
	        	break;
	        case Quest:
	        	quest(context);
	        	break;
	        case Raid:
	        	raid(context);
                break;
	        case Save:
	        	save(context);
                break;
            case ScoutingParty:
            	scoutingParty(context);
                break;
            case Seaway:
            	seaway(context);
            	break;
            case Summon:
            	summon(context);
            	break;
            case Trade:
            	trade(context);
            	break;
            case Training:
            	training(context);
            	break;
            case TravellingFair:
            	context.travellingFairBought = true;
            default:
                break;
        }
        
        // test if prince lost track of any cards
        context.player.princeCardLeftThePlay(context.player);
    }
    
    public void alms(MoveContext context) {
    	boolean noTreasureCard = true;
        for(Card card : context.player.playedCards) {
            if (card instanceof TreasureCard) {
            	noTreasureCard = false;
            	break;
            }
        }
        if (noTreasureCard) {
	        Card card = context.player.controlPlayer.alms_cardToObtain(context);
	        if (card != null) {
	            if (card.getCost(context) <= 4) {
	            	context.player.gainNewCard(card, this.controlCard, context);
	            }
	        }
        }
        context.cantBuy.add(this); //once per turn
    }
    
    public void ball(MoveContext context) {
    	Player player = context.getPlayer();
    	player.setMinusOneCoinToken(true, context);
    	for (int i = 0; i < 2; ++i) {
			Card card = player.controlPlayer.ball_cardToObtain(context);
			if (card != null) {
	            // check cost
	            if (card.getCost(context) <= 4) {
	            	player.gainNewCard(card, this, context);
	            }
	        }
		}
    }
    
    private void bonfire(MoveContext context) {
        Card[] cards = context.player.controlPlayer.bonfire_cardsToTrash(context);
        if (cards != null) {
            if (cards.length > 2) {
                Util.playerError(context.player, "Bonfire trash error, trying to trash too many cards, ignoring.");
            } else {
            	cardLoop:
                for (Card card : cards) {
                    for (int i = 0; i < context.player.playedCards.size(); i++) {
                        Card playedCard = context.player.playedCards.get(i);
                        if (playedCard.equals(card)) {
                            context.player.trash(context.player.playedCards.remove(i, false), this.controlCard, context);
                            continue cardLoop;
                        }
                    }
                    for (int i = 0; i < context.player.nextTurnCards.size(); i++) {
                        Card nextTurnCard = context.player.nextTurnCards.get(i);
                        if (nextTurnCard.equals(card)) {
                        	if (nextTurnCard.isDuration(context.player)) {
                        		((CardImpl)nextTurnCard).trashAfterPlay = true;
                                context.player.trash(nextTurnCard, this.controlCard, context);
                        	} else {
                        		context.player.trash(context.player.nextTurnCards.remove(i, false), this.controlCard, context);
                        	}
                        	continue cardLoop;
                        }
                    }
                }
            }
        }
    }
    
    protected void borrow(MoveContext context) {
    	if (!context.player.getMinusOneCardToken()) {
    		context.player.setMinusOneCardToken(true, context);
    		context.addCoins(1);
    	}
        context.cantBuy.add(this); //once per turn
    }
    
    private void ferry(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.ferry_actionCardPileToHaveToken(context);
    	if (card.isAction(null))
    		placeToken(context, card, PlayerSupplyToken.MinusTwoCost);
    }
    
    private void inheritance(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.inheritance_actionCardTosetAside(context);
    	if (card != null && card.isAction(null)) {
            if (card.getCost(context) <= 4 && !context.game.isPileEmpty(card) && !card.isVictory(context)) {
            	context.player.inheritance = context.game.takeFromPile(card, context);
            	GameEvent event = new GameEvent(GameEvent.Type.CardSetAsideInheritance, context);
                event.card = card;
                context.game.broadcastEvent(event);
            }
        }
    }
    
    private void lostArts(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.lostArts_actionCardPileToHaveToken(context);
    	if (card.isAction(null))
    		placeToken(context, card, PlayerSupplyToken.PlusOneAction);
    }
    
    private void mission(MoveContext context) {
    	context.missionBought = true;
    	context.cantBuy.add(this);
    }
    
    private void pathfinding(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.pathfinding_actionCardPileToHaveToken(context);
    	if (card.isAction(null))
    		placeToken(context, card, PlayerSupplyToken.PlusOneCard);
    }
    
    private void pilgrimage(MoveContext context) {
    	if(context.player.flipJourneyToken(context)) {
    		Card[] cards = context.player.controlPlayer.pilgrimage_cardsToGain(context);
    		if (cards != null) {
    			if (cards.length > 3) {
    				Util.playerError(context.player, "Pilgrimage gain error, trying to gain too many cards, ignoring.");
    			} else {
    				HashSet<Card> differentCards = new HashSet<Card>();
    				for (Card card : cards) {
    					differentCards.add(card);
    				}
    				for (Card card : differentCards) {
    					if(context.player.playedCards.contains(card) || 
    							context.player.nextTurnCards.contains(card)) {
    						context.player.gainNewCard(card, this.controlCard, context);
    					} else {
    						Util.playerError(context.player, "Pilgrimage gain error, card not in play, ignoring.");
    					}
    				}
    			}
    		}
    	}
    	context.cantBuy.add(this); //once per turn
	}
    
    private void plan(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.plan_actionCardPileToHaveToken(context);
    	if (card.isAction(null))
    		placeToken(context, card, PlayerSupplyToken.Trashing);
    }
    
    private void quest(MoveContext context) {
    	Player player = context.getPlayer();
    	CardList hand = player.getHand();
    	if (hand.size() == 0)
    		return;
    	QuestOption option = player.controlPlayer.quest_chooseOption(context);
    	if (option == null) {
    		return;
    	}
    	if (option == QuestOption.DiscardAttack) {
    		Set<Card> attackSet = new HashSet<Card>();
    		for (Card card : hand) {
    			if (card.behaveAsCard().isAttack(player)) {
    				attackSet.add(card);
    			}
    		}
    		if (attackSet.size() == 0) {
    			return;
    		} else if (attackSet.size() == 1) {
    			Card toDiscard = attackSet.toArray(new Card[0])[0];
    			hand.remove(toDiscard);
    			context.getPlayer().discard(toDiscard, this, context);
    		} else {
    			Card[] attacks = attackSet.toArray(new Card[0]);
    			Card toDiscard = player.controlPlayer.quest_attackCardToDiscard(context, attacks);
    			if (toDiscard == null || !attackSet.contains(toDiscard)) {
    				Util.playerError(player, "Quest error, picked attack didn't have. Choosing first attack.");
    				toDiscard = attacks[0];
    			}
    			hand.remove(toDiscard);
    			context.getPlayer().discard(toDiscard, this, context);
    		}
    	} else if (option == QuestOption.DiscardTwoCurses) {
    		int numCurses = 0;
    		for(int n = 0; n < 2; ++n) {
    			for (int i = 0; i < hand.size(); ++i) {
    				if (hand.get(i).equals(Cards.curse)) {
    					numCurses++;
    					player.discard(hand.remove(i), this, context);
    					break;
    				}
    			}
    		}
    		if (numCurses != 2)
    			return;
    	} else if (option == QuestOption.DiscardSixCards) {
    		if (hand.size() <= 6) {
    			int numCards = hand.size();
    			while (!hand.isEmpty()) {
    				player.discard(hand.remove(0), this, context);
    			}
    			if (numCards < 6)
    				return;
    		} else {
    			Card[] toDiscard = player.controlPlayer.quest_cardsToDiscard(context);
    			if (toDiscard.length != 6 || !Util.areCardsInHand(toDiscard, context)) {
    				Util.playerError(player, "Quest error, picked cards to discard player didn't have. Choosing first siz.");
    				for (int i = 0; i < 6; ++i) {
    					player.discard(hand.remove(0), Cards.quest, context);
    				}
    			} else {
    				for (Card card : toDiscard) {
    		            hand.remove(card);
    		            player.discard(card, this, context);
    		        }
    			}
    		}
    	}
    	player.gainNewCard(Cards.gold, this, context);
    }
    
	protected void raid(MoveContext context) {
        for(Card card : context.player.playedCards) {
            if (card.equals(Cards.silver)) {
                context.player.gainNewCard(Cards.silver, this, context);
            }
        }
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
            if (targetPlayer != context.player) {
                MoveContext targetContext = new MoveContext(context.game, targetPlayer);
            	targetPlayer.setMinusOneCardToken(true, targetContext);
            }
        }
    }
    
    private void save(MoveContext context) {
    	context.cantBuy.add(this); //once per turn
    	CardList hand = context.getPlayer().getHand();
    	if (hand.size() == 0)
    		return;
    	
        Card card = (hand.size() == 1) ? hand.get(0) : context.player.controlPlayer.save_cardToSetAside(context);
        if (card == null || !context.player.hand.contains(card)) {
            Util.playerError(context.player, "Save set aside card error, setting aside the first card in hand.");
            card = context.player.hand.get(0);
        }

    	context.player.hand.remove(card);
    	context.player.save = card;
    	GameEvent event = new GameEvent(GameEvent.Type.CardSetAsideSave, context);
        event.card = card;
        event.setPrivate(true);
        context.game.broadcastEvent(event);
    }
    
    private void scoutingParty(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < 5; i++) {
            Card card = context.game.draw(context, Cards.scoutingParty, 5 - i);
            if (card != null) {
                cards.add(card);
            }
        }

        if (cards.size() == 0) {
            return;
        }

        for (int i = 0; i < 3; i++) {
        	if(cards.size() > 0) {
	        	Card toDiscard = null;
	        	if(cards.size() > 3-i) {
	        		toDiscard = context.player.scoutingParty_cardToDiscard(context, cards.toArray(new Card[cards.size()]));
		        } else {
		            toDiscard = cards.get(0);
		        }
	        	
		        if (toDiscard == null || !cards.contains(toDiscard)) {
		            Util.playerError(context.player, "ScoutingParty discard error, just picking the first card.");
		            toDiscard = cards.get(0);
		        }
		
		        context.player.discard(toDiscard, this.controlCard, context);
		
		        cards.remove(toDiscard);
        	}
        }

        if (cards.size() > 0) {
        	Card[] order = context.player.controlPlayer.survivors_cardOrder(context, cards.toArray(new Card[cards.size()]));

	        // Check that they returned the right cards
	        boolean bad = false;
	
	        if (order == null) {
	            bad = true;
	        } else {
	            ArrayList<Card> copy = new ArrayList<Card>();
	            for (Card card : cards) {
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
	            Util.playerError(context.player, "Scouting Party order cards error, ignoring.");
	            order = cards.toArray(new Card[cards.size()]);
	        }
	
	        // Put the cards back on the deck
	        for (int i = order.length - 1; i >= 0; i--) {
	        	context.player.putOnTopOfDeck(order[i]);
	        }
        }        
    }
    
    private void seaway(MoveContext context) {
    	Card card = context.player.controlPlayer.seaway_cardToObtain(context);
        if (card != null && card.isAction(null)) {
            if (card.getCost(context) <= 4 && !context.game.isPileEmpty(card)) {
            	Card gainedCard = context.player.gainNewCard(card, this.controlCard, context);
            	if (card.equals(gainedCard)
            			|| (card.isRuins(null) && gainedCard.isRuins(null))
            			|| (card.isKnight(null) && gainedCard.isKnight(null)))
            		placeToken(context, card, PlayerSupplyToken.PlusOneBuy);
            }
        }
    	
    }
    
    private void summon(MoveContext context) {
    	Card card = context.player.controlPlayer.summon_cardToObtain(context);
        if (card != null && card.isAction(null)) {
            if (card.getCost(context) <= 4 && !context.game.isPileEmpty(card)) {
            	context.player.gainNewCard(card, this.controlCard, context);
            }
        }
    }
    
    private void training(MoveContext context) {
    	Card card = context.getPlayer().controlPlayer.training_actionCardPileToHaveToken(context);
    	if (card.isAction(null))
    		placeToken(context, card, PlayerSupplyToken.PlusOneCoin);
    }
    
    private void trade(MoveContext context) {
    	Card[] cards = context.player.controlPlayer.trade_cardsToTrash(context);
    	if (cards != null) {
    		if (cards.length > 2) {
    			Util.playerError(context.player, "Trade trash error, trying to trash too many cards, ignoring.");
    		} else {
    			for (Card card : cards) {
    				for (int i = 0; i < context.player.hand.size(); i++) {
    					Card inHand = context.player.hand.get(i);
    					if (inHand.equals(card)) {
    						context.player.trash(context.player.hand.remove(i, false), this.controlCard, context);
    						context.player.gainNewCard(Cards.silver, this, context);
    						break;
    					}
    				}
    			}
    		}
    	}
	}
    
}
