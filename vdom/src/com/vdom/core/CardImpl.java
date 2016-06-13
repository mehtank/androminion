package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;

public class CardImpl implements Card {
	private static final long serialVersionUID = 1L;
    // Template (immutable)
    Cards.Kind kind;
    CardImpl templateCard;
    String name;
    String safeName;
    int cost;
    int debtCost;
    boolean costPotion = false;

    String description = "";
    Expansion expansion = null;
    protected int vp;
    private Type[] types;
    protected int addActions;
    protected int addBuys;
    protected int addCards;
    protected int addGold;
    protected int addVictoryTokens;
    boolean trashForced = false;
    boolean providePotion;
    protected int addActionsNextTurn;
    protected int addBuysNextTurn;
    protected int addCardsNextTurn;
    protected int addGoldNextTurn;
    protected boolean takeAnotherTurn;
    protected int takeAnotherTurnCardCount;
    
    boolean isOverpay = false;  // can this card be overpaid for?
    
    boolean trashOnUse = false;
    
    protected boolean callableWhenCardGained;
    protected boolean callableWhenActionResolved;
    protected boolean actionStillNeedsToBeInPlay;
    protected boolean callableWhenTurnStarts;
    protected int callableWhenCardGainedMaxCost = -1;

    static int maxNameLen;    // across all cards
    
    // Implementation (mutable)
    private Integer id;
    boolean movedToNextTurnPile = false;
    boolean trashAfterPlay = false;
    int numberTimesAlreadyPlayed = 0;
    int cloneCount = 1;
    CardImpl impersonatingCard = null;
    CardImpl inheritingAbilitiesCard = null;
    CardImpl controlCard = this;

    protected CardImpl(Cards.Kind kind, int cost) {
        this.kind = kind;
        this.name = kind.toString();
        if (maxNameLen < name.length()) {
            maxNameLen = name.length();
        }
        this.cost = cost;
    }
    
    public CardImpl(Builder builder) {
        this(builder.kind, builder.cost);
        costPotion = builder.costPotion;
        debtCost = builder.debtCost;
        vp = builder.vp;
        description = builder.description;
        expansion   = builder.expansion;
        types       = builder.types;
        addActions       = builder.addActions;
        addBuys          = builder.addBuys;
        addCards         = builder.addCards;
        addGold          = builder.addGold;
        addVictoryTokens = builder.addVictoryTokens;
        trashForced      = builder.trashForced;
        providePotion = builder.providePotion;
        addActionsNextTurn = builder.addActionsNextTurn;
        addBuysNextTurn = builder.addBuysNextTurn;
        addCardsNextTurn = builder.addCardsNextTurn;
        addGoldNextTurn = builder.addGoldNextTurn;
        takeAnotherTurn = builder.takeAnotherTurn;
        takeAnotherTurnCardCount = builder.takeAnotherTurnCardCount;
        isOverpay   = builder.isOverpay;
        trashOnUse   = builder.trashOnUse;
        
        callableWhenCardGained = builder.callableWhenCardGained;
        callableWhenActionResolved = builder.callableWhenActionResolved;
        actionStillNeedsToBeInPlay = builder.actionStillNeedsToBeInPlay;
        callableWhenTurnStarts = builder.callableWhenTurnStarts;
        callableWhenCardGainedMaxCost = builder.callableWhenCardGainedMaxCost;
    }

    public static class Builder {
        protected Cards.Kind kind;
        protected String name;
        protected int cost;
        protected int debtCost;
        protected int vp = 0;

        protected boolean costPotion = false;
        protected String description = "";
        protected Expansion expansion = null;
        protected Type[] types = null;
        
        protected int addActions;
        protected int addBuys;
        protected int addCards;
        protected int addGold;
        protected int addVictoryTokens;
        protected boolean trashForced = false;
        protected boolean providePotion = false;
        protected int addActionsNextTurn;
        protected int addBuysNextTurn;
        protected int addCardsNextTurn;
        protected int addGoldNextTurn;
        protected boolean takeAnotherTurn;
        protected int takeAnotherTurnCardCount;
        
        protected boolean callableWhenCardGained;
        protected boolean callableWhenActionResolved;
        protected boolean actionStillNeedsToBeInPlay;
        protected boolean callableWhenTurnStarts;
        protected int callableWhenCardGainedMaxCost;
        
        protected boolean isOverpay   = false;
        
        protected boolean trashOnUse  = false;


        public Builder(Cards.Kind kind, int cost) {
            this.kind = kind;
            this.name = kind.toString();
            this.cost = cost;
        }
        
        public Builder(Cards.Kind kind, Type... type) {
            this.kind = kind;
            this.name = kind.toString();
            this.cost = 0;
            this.types = type;
        }
        
        public Builder(Cards.Kind kind, int cost, Type... type) {
            this.kind = kind;
            this.name = kind.toString();
            this.cost = cost;
            this.types = type;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder expansion(Expansion val) {
            expansion = val;
            return this;
        }

        public Builder costPotion() {
            costPotion = true;
            return this;
        }
        
        public Builder costDebt(int val) {
        	debtCost = val;
        	return this;
        }

        public Builder vp(int val) {
            vp = val;
            return this;
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

        public Builder trashForced() {
            trashForced = true;
            return this;
        }
        
        public Builder providePotion() {
            providePotion = true;
            return this;
        }
        
        public Builder addActionsNextTurn(int val) {
            addActionsNextTurn = val;
            return this;
        }

        public Builder addBuysNextTurn(int val) {
            addBuysNextTurn = val;
            return this;
        }

        public Builder addCardsNextTurn(int val) {
            addCardsNextTurn = val;
            return this;
        }

        public Builder addGoldNextTurn(int val) {
            addGoldNextTurn = val;
            return this;
        }

        public Builder takeAnotherTurn(int val) {
            takeAnotherTurn = true;
            takeAnotherTurnCardCount = val;
            return this;
        }
        
        public Builder callWhenTurnStarts() {
        	callableWhenTurnStarts = true;
            return this;
        }

        public Builder callWhenActionResolved() {
        	callableWhenActionResolved = true;
            return this;
        }
        
        public Builder callWhenActionResolved(boolean mustBeInPlay) {
        	callableWhenActionResolved = true;
        	actionStillNeedsToBeInPlay = mustBeInPlay;
            return this;
        }

        public Builder callWhenGainCard(int maxCost) {
            callableWhenCardGained = true;
            callableWhenCardGainedMaxCost = maxCost;
            return this;
        }

        public Builder isOverpay()
        {
            isOverpay = true;
            return this;
        }

        public Builder trashOnUse() {
            trashOnUse = true;
            return this;
        }

        public CardImpl build() {
        	if (expansion == null) {
        		return new CardImpl(this);
        	}
        	switch (expansion) {
        	case Base:
        		return new CardImplBase(this);
        	case Intrigue:
        		return new CardImplIntrigue(this);
        	case Seaside:
        		return new CardImplSeaside(this);
        	case Alchemy:
        		return new CardImplAlchemy(this);
        	case Prosperity:
        		return new CardImplProsperity(this);
        	case Cornucopia:
        		return new CardImplCornucopia(this);
        	case Hinterlands:
        		return new CardImplHinterlands(this);
        	case DarkAges:
        		return new CardImplDarkAges(this);
        	case Guilds:
        		return new CardImplGuilds(this);
        	case Adventures:
        		return new CardImplAdventures(this);
        	case Empires:
        		return new CardImplEmpires(this);
        	case Promo:
        		return new CardImplPromo(this);
        	default:
        		return new CardImpl(this);
        	}
        }

    }
    protected CardImpl() {
    }

    public String getSafeName() {
        return name;
    }

    public boolean isTemplateCard() {
        return templateCard == null;
    }

    public CardImpl getTemplateCard() {
        return templateCard == null ? this : templateCard;
    }

    protected void checkInstantiateOK() {
        if (!isTemplateCard()) {
            Thread.dumpStack();
            Util.debug("Trying to create a real card from a real card instead of a template");
        }
    }

    public CardImpl instantiate() {
        checkInstantiateOK();
        CardImpl c = null;
        if (expansion == null) {
    		c = new CardImpl();
    	} else {
	    	switch (expansion) {
	    	case Base:
	    		c = new CardImplBase();
	    		break;
	    	case Intrigue:
	    		c = new CardImplIntrigue();
	    		break;
	    	case Seaside:
	    		c = new CardImplSeaside();
	    		break;
	    	case Alchemy:
	    		c = new CardImplAlchemy();
	    		break;
	    	case Prosperity:
	    		c = new CardImplProsperity();
	    		break;
	    	case Cornucopia:
	    		c = new CardImplCornucopia();
	    		break;
	    	case Hinterlands:
	    		c = new CardImplHinterlands();
	    		break;
	    	case DarkAges:
	    		c = new CardImplDarkAges();
	    		break;
	    	case Guilds:
	    		c = new CardImplGuilds();
	    		break;
	    	case Adventures:
	    		c = new CardImplAdventures();
	    		break;
	    	case Empires:
	    		c = new CardImplEmpires();
	    		break;
	    	case Promo:
	    		c = new CardImplPromo();
	    		break;
	    	default:
	    		c = new CardImpl();
	    		break;
	    	}
    	}
        copyValues(c);
        return c;
    }

    protected void copyValues(CardImpl c) {
        c.templateCard = this;
        c.id = Game.cardSequence++;

        c.kind = kind;
        c.name = name;
        c.cost = cost;
        c.costPotion = costPotion;
        c.debtCost = debtCost;
        c.description = description;
        c.expansion = expansion;
        c.types = types;
        c.providePotion = providePotion;
        c.addActions = addActions;
        c.addBuys = addBuys;
        c.addCards = addCards;
        c.addGold = addGold;
        c.addVictoryTokens = addVictoryTokens;
        c.trashForced = trashForced;
        c.addActionsNextTurn = addActionsNextTurn;
        c.addBuysNextTurn = addBuysNextTurn;
        c.addCardsNextTurn = addCardsNextTurn;
        c.addGoldNextTurn = addGoldNextTurn;
        c.takeAnotherTurn = takeAnotherTurn;
        c.takeAnotherTurnCardCount = takeAnotherTurnCardCount;
        c.isOverpay = isOverpay;
        c.vp = vp;
        c.trashOnUse = trashOnUse;
        
        c.callableWhenCardGained = callableWhenCardGained;
        c.callableWhenActionResolved = callableWhenActionResolved;
        c.callableWhenTurnStarts = callableWhenTurnStarts;
        c.callableWhenCardGainedMaxCost = callableWhenCardGainedMaxCost;
        c.actionStillNeedsToBeInPlay = actionStillNeedsToBeInPlay;
    }
    
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {}
    

    public Cards.Kind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public boolean is(Type t) {
    	return is(t, null);
    }
    
    @Override
    public boolean is(Type t, Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate)) {
	    	for (int i = 0; i < types.length; ++i) {
	    		if (types[i] == t) return true;
	    	}
	    	return false;
    	}
    	return player.getInheritance().is(t, null);
    }

    public int getCost(MoveContext context) {
        if (context == null)
            return cost;
        return getCost(context, context.buyPhase);
    }

    public int getCost(MoveContext context, boolean buyPhase) {
    	if (this.is(Type.Event, null)) return cost; //Costs of Events are not affected by cards like Bridge Troll.
        if (this.equals(Cards.virtualKnight))
            if(context.game.getTopKnightCard() != null && !context.game.getTopKnightCard().equals(Cards.virtualKnight))
                return context.game.getTopKnightCard().getCost(context,buyPhase); 

        if (controlCard != null && controlCard != this && controlCard.inheritingAbilitiesCard != null) {
        	return controlCard.getCost(context, buyPhase);
        }
        
        MoveContext currentPlayerContext = context; 
        if (context.game.getCurrentPlayer() != context.getPlayer()) {
        	currentPlayerContext = new MoveContext(context.game, context.game.getCurrentPlayer());
        }
        
        int costModifier = 0;
        //TODO: BUG this isAction call for Quarry should be player-specific sometimes 
        costModifier -= this.is(Type.Action, null) ? (2 * context.countCardsInPlay(Cards.quarry)) : 0;
        costModifier -= context.countCardsInPlay(Cards.highway);
        costModifier -= currentPlayerContext.countCardsInPlay(Cards.bridgeTroll);
        costModifier -= 2 * context.countCardsInPlay(Cards.princess);
        //TODO: BUG if an inherited peddler is yours, its cost should lower during your buy phase 
        costModifier -= (buyPhase && this.equals(Cards.peddler)) ? (2 * context.countActionCardsInPlay()) : 0;
        costModifier -= (context.game.isPlayerSupplyTokenOnPile(this.controlCard.equals(Cards.estate) ? this.controlCard : this, 
        		context.game.getCurrentPlayer(), PlayerSupplyToken.MinusTwoCost)) ? 2 : 0;
        
        return Math.max(0, cost + costModifier + context.cardCostModifier/*bridge*/);
    }
    
    public int getDebtCost(MoveContext context) {
    	return debtCost;
    }
    
    @Override
    public int getVictoryPoints() {
    	return vp;
    };

    @Override
    public int getAddActions() {
        return addActions;
    }

    @Override
    public int getAddBuys() {
        return addBuys;
    }

    @Override
    public int getAddCards() {
        return addCards;
    }

    @Override
    public int getAddGold() {
        return addGold;
    }

    @Override
    public int getAddVictoryTokens() {
        return addVictoryTokens;
    }

    @Override
    public boolean trashForced() {
        return trashForced;
    }
    
    @Override
    public boolean providePotion() {
        return providePotion;
    }
    
    @Override
    public int getAddActionsNextTurn() {
        return addActionsNextTurn;
    }

    @Override
    public int getAddBuysNextTurn() {
        return addBuysNextTurn;
    }

    @Override
    public int getAddCardsNextTurn() {
        return addCardsNextTurn;
    }

    @Override
    public int getAddGoldNextTurn() {
        return addGoldNextTurn;
    }

    @Override
    public boolean takeAnotherTurn() {
        return takeAnotherTurn;
    }

    @Override
    public int takeAnotherTurnCardCount() {
        return takeAnotherTurnCardCount;
    }
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    @Override
    public void play(Game game, MoveContext context) {
    	play(game, context, true);
    }
    
    @Override
    public void play(Game game, MoveContext context, boolean fromHand) {
        Player currentPlayer = context.getPlayer();
        boolean newCard = false;
        Card actualCard = (this.getControlCard() != null ? this.getControlCard() : this);
        boolean isInheritedAbility = actualCard.equals(Cards.estate);
        Card inheritedCard = isInheritedAbility ? context.player.getInheritance() : null;
        Card playedCard = isInheritedAbility ? actualCard : this;
        boolean isAction = playedCard.is(Type.Action, currentPlayer);
        boolean enchantressEffect = isAction && !context.enchantressAlreadyAffected && game.enchantressAttacks(currentPlayer);
        if (enchantressEffect) context.enchantressAlreadyAffected = true;
                
        if (!isInheritedAbility)
        	context.actions += game.countChampionsInPlay(currentPlayer);
        
        if (is(Type.Attack, currentPlayer))
            attackPlayed(context, game, currentPlayer);
        
        if (this.numberTimesAlreadyPlayed == 0 && this == actualCard) {
            newCard = true;
            this.movedToNextTurnPile = false;
            if (fromHand)
                currentPlayer.hand.remove(this);
            if (!enchantressEffect && trashOnUse) {
                currentPlayer.trash(this, null, context);
            } else if (!enchantressEffect && this.is(Type.Duration, currentPlayer)) {
                currentPlayer.nextTurnCards.add(this);
            } else {
                currentPlayer.playedCards.add(this);
            }
        }
        
        GameEvent event;
        event = new GameEvent(GameEvent.EventType.PlayingCard, (MoveContext) context);
        event.card = playedCard;
        event.newCard = newCard;
        game.broadcastEvent(event);
        
        // playing an action
        if (isAction) {
	        if (this == actualCard) 
	            context.actionsPlayedSoFar++;
	        if (isAction && context.freeActionInEffect == 0) {
	            context.actions--;
	        }
        }
        
        Card tokenPile = isInheritedAbility ? actualCard : this;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneAction))
        	context.actions += 1;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneBuy))
        	context.buys += 1;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneCoin))
        	context.addCoins(1);
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneCard))
        	game.drawToHand(context, actualCard, 1 + addCards);
        
        if (enchantressEffect) {
        	//allow reaction to playing an attack card with Enchantress effect
        	if (is(Type.Attack, currentPlayer)) {
        		 for (Player player : game.getPlayersInTurnOrder()) {
    	            if (player != currentPlayer) Util.isDefendedFromAttack(game, player, this);
	            }
        	}
        	context.actions += 1;
        	game.drawToHand(context, this, 1);
        } else {
        	context.actions += addActions;
            context.buys += addBuys;
            if (this.equals(Cards.copper)) {
            	context.addCoins(addGold + context.coppersmithsPlayed);
            } else {
            	context.addCoins(addGold);
            }
            currentPlayer.addVictoryTokens(context, addVictoryTokens);
            if (providePotion()) {
                context.potions++;
            }
            
            for (int i = 0; i < addCards; ++i) {
            	game.drawToHand(context, this, addCards - i);
            }

            if (inheritedCard == null) {
            	additionalCardActions(game, context, currentPlayer);
            } else {
            	// Play the inheritance virtual card
            	this.startInheritingCardAbilities(inheritedCard.getTemplateCard().instantiate());
            	CardImpl cardToPlay = (CardImpl) this.behaveAsCard();
		        context.freeActionInEffect++;
		        cardToPlay.play(game, context, false);
		        context.freeActionInEffect--;
            }
        }
    
        event = new GameEvent(GameEvent.EventType.PlayedCard, (MoveContext) context);
        event.card = playedCard;
        game.broadcastEvent(event);
        

        // test if any prince card left the play
        currentPlayer.princeCardLeftThePlay(currentPlayer);
        
        // check for cards to call after resolving action
        boolean isActionInPlay = isInPlay(currentPlayer);
        ArrayList<Card> callableCards = new ArrayList<Card>();
        Card toCall = null;
        for (Card c : currentPlayer.tavern) {
        	if (c.behaveAsCard().isCallableWhenActionResolved()) {
        		if (c.behaveAsCard().doesActionStillNeedToBeInPlayToCall() && !isActionInPlay) {
        			continue;
        		}
        		callableCards.add(c);
        	}
        }
        if (!callableCards.isEmpty()) {
        	Collections.sort(callableCards, new Util.CardCostComparator());
	        do {
	        	toCall = null;
	        	// we want null entry at the end for None
	        	Card[] cardsAsArray = callableCards.toArray(new Card[callableCards.size() + 1]);
	        	//ask player which card to call
	        	toCall = currentPlayer.controlPlayer.call_whenActionResolveCardToCall(context, playedCard, cardsAsArray);
	        	if (toCall != null && callableCards.contains(toCall)) {
	        		callableCards.remove(toCall);
	        		toCall.behaveAsCard().callWhenActionResolved(context, playedCard);
	        	}
		        // loop while we still have cards to call
		        // NOTE: we have a hack here to prevent asking for duplicate calls on an unused Royal Carriage
		        //   since technically you can ask for more and action re-played by royal carriage will ask as well
	        } while (toCall != null && toCall.equals(Cards.coinOfTheRealm) && !callableCards.isEmpty());
        }
    }

    public String getStats() {
        StringBuilder sb = new StringBuilder();
        	String costString = "(" + cost + (costPotion ? "p": "") + (debtCost > 0 ? "d" + debtCost : "") + ") ";
            sb.append (costString);
        if (vp > 0) {
            sb.append(", " + vp + " victory point");
            if (vp > 1) {
                sb.append("s");
            }
        }
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
        if (addActionsNextTurn > 0 || addBuysNextTurn > 0 || addGoldNextTurn > 0 || addCardsNextTurn > 0) {
            sb.append(" (");

            boolean start = true;
            if (addActionsNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addActionsNextTurn + " Action");
                if (addActionsNextTurn > 1) {
                    sb.append("s");
                }
            }
            if (addBuysNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addBuysNextTurn + " Buy");
                if (addBuysNextTurn > 1) {
                    sb.append("s");
                }
            }
            if (addGoldNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addGoldNextTurn + " Coin");
            }
            if (addCardsNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addCardsNextTurn + " Card");
                if (addCardsNextTurn > 1) {
                    sb.append("s");
                }
            }

            sb.append(" next turn)");
        }
        return sb.toString();
    }

    public Expansion getExpansion() {
        return expansion;
    }

    public String getDescription() {
        return description;
    }
    
    public String toString() {
        return name; // + " (id=" + id + ")";
    }

    public boolean equals(Object object) {
        return (object != null) && name.equals(((Card) object).getName()); // took away typecheck
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean costPotion() {
        return costPotion;
    }
    
    public int debtCost() {
    	return debtCost;
    }
    
    @Override
    public boolean isOverpay(Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return isOverpay;
    	return ((CardImpl)player.getInheritance()).isOverpay;
    }
    
	@Override
    public void isBuying(MoveContext context) {
		if (is(Type.Victory, context.getPlayer())) {
			context.game.trashHovelsInHandOption(context.player, context, this);
		}
		if (is(Type.Event, null)) {
			context.buys += addBuys;
		}
		if (this.equals(Cards.estate)) {
        	Card inheritance = context.getPlayer().getInheritance();
        	if (inheritance != null) {
        		inheritance.isBuying(context);
        	}
        }
    }
    
    @Override
    public void isBought(MoveContext context) {
    }
    
    @Override
    public boolean isCallableWhenCardGained() {
    	return false;
    }
    
    @Override
    public int getCallableWhenGainedMaxCost() {
    	return 6;
    }
    
    @Override
    public boolean isCallableWhenActionResolved() {
    	return false;
    }
    
    @Override
    public boolean doesActionStillNeedToBeInPlayToCall() {
    	return false;
    }
    
    @Override
    public boolean isCallableWhenTurnStarts() {
    	return false;
    }
    
    @Override
    public void callWhenCardGained(MoveContext context, Card cardToGain) {	
    }
    
    @Override
    public void callWhenActionResolved(MoveContext context, Card resolvedAction) {
    }
    
    @Override
    public void callAtStartOfTurn(MoveContext context) {
    }
    
    @Override
    public void isTrashed(MoveContext context) { }

    public boolean isImpersonatingAnotherCard() {
        return !(this.impersonatingCard == null);
    }

    public Card behaveAsCard() {
        if (impersonatingCard != null)
        	return impersonatingCard;
        if (inheritingAbilitiesCard != null)
        	return inheritingAbilitiesCard;
        return this;
    }

//    CardImpl getImpersonatingCard() {
//        return impersonatingCard;
//    }
    
    void startInheritingCardAbilities(CardImpl inheritingCard) {
    	inheritingCard.setControlCard(this);
    	this.inheritingAbilitiesCard = inheritingCard; 
    }

    void startImpersonatingCard(CardImpl impersonatingCard) {
        impersonatingCard.setControlCard(this);
        this.impersonatingCard = impersonatingCard;
    }

    void stopImpersonatingCard() {
        this.impersonatingCard = null;
    }
    
    void stopInheritingCardAbilities() {
    	this.inheritingAbilitiesCard = null;
    }

    @Override
    public CardImpl getControlCard() {
        return controlCard;
    }

    void setControlCard(CardImpl controlCard) {
        this.controlCard = controlCard;
    }
    
    protected void placeToken(MoveContext context, Card card, PlayerSupplyToken token) {
    	if (card == null) {
    		Card[] cards = context.game.getActionsInGame();
    		if (cards.length != 0) {
                Util.playerError(context.getPlayer(), getName() + " error: did not pick a valid pile, ignoring.");
            }
            return;
    	}
    	if (!context.game.cardInGame(card) ||
    			!Cards.isSupplyCard(card)) {
    		Util.playerError(context.getPlayer(), getName() + " error: Invalid pile chosen, ignoring");
    	}
    	
    	context.game.movePlayerSupplyToken(card, context.getPlayer(), token);
	}
    
    protected void attackPlayed(MoveContext context, Game game, Player currentPlayer) {
        // If an Urchin has been played, offer the player the option to trash it for a Mercenary
        for (int i = currentPlayer.playedCards.size() - 1; i >= 0 ; --i) {
            Card c = currentPlayer.playedCards.get(i);
            if (c.behaveAsCard().getKind() == Cards.Kind.Urchin && currentPlayer.controlPlayer.urchin_shouldTrashForMercenary(context)) {
                currentPlayer.trash(c.getControlCard(), this, context);
                currentPlayer.gainNewCard(Cards.mercenary, this, context);
                currentPlayer.playedCards.remove(i);
            }
        }
    }
    
    protected boolean isInPlay(Player currentPlayer) {
		for (Card c : currentPlayer.playedCards) {
			if (this == c)
				return true;
		}
		for (Card c : currentPlayer.nextTurnCards) {
			if (c instanceof CardImpl && !((CardImpl)c).trashAfterPlay && this == c)
			if (this == c)
				return true;
		}
		return false;
	}
    
    protected void durationAttack(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
        	if (targetPlayer != currentPlayer) {
        		if (!Util.isDefendedFromAttack(game, targetPlayer, this)) {
        			targetPlayer.attacked(this.controlCard, context);
        			currentPlayer.addDurationEffectOnOtherPlayer(targetPlayer, this.kind);
        		}
        	}
        }
    }
    
    protected void witchFamiliar(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.controlCard, context);
                MoveContext targetContext = new MoveContext(game, player);
                targetContext.attackedPlayer = player;
                player.gainNewCard(Cards.curse, this.controlCard, targetContext);
            }
        }
    }

    protected Card throneRoomKingsCourt(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> actionCards = new ArrayList<Card>();
        CardImpl cardToPlay = null;
        for (Card card : currentPlayer.hand) {
            if (card.is(Type.Action, currentPlayer)) {
                actionCards.add(card);
            }
        }

        if (!actionCards.isEmpty()) {
            switch (this.kind) {
                case ThroneRoom:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.throneRoom_cardToPlay(context);
                    break;
                case Disciple:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.disciple_cardToPlay(context);
                    break;
                case KingsCourt:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.kingsCourt_cardToPlay(context);
                    break;
                case Procession:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.procession_cardToPlay(context);
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
                    int cloneCount = cardToPlay.cloneCount;
                    cardToPlay.numberTimesAlreadyPlayed = -1;
                    for (int i = 0; i < cloneCount; ++i) {
                        cardToPlay.numberTimesAlreadyPlayed++;
                        cardToPlay.play(game, context, cardToPlay.numberTimesAlreadyPlayed == 0 ? true : false);
                    }

                    cardToPlay.numberTimesAlreadyPlayed = 0;
                    context.freeActionInEffect--;
                    // If the cardToPlay was a knight, and was trashed, reset clonecount
                    if (cardToPlay.is(Type.Knight, currentPlayer) && !currentPlayer.playedCards.contains(cardToPlay) && game.trashPile.contains(cardToPlay)) {
                        cardToPlay.cloneCount = 1;
                    }

                    if (cardToPlay.is(Type.Duration, currentPlayer) && !cardToPlay.equals(Cards.tactician)) {
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

                if (this.kind == Cards.Kind.Procession) {
                    if (!cardToPlay.trashOnUse) {
                        currentPlayer.trash(cardToPlay, this.controlCard, context);
                        if (currentPlayer.playedCards.getLastCard() == cardToPlay) { 
                            currentPlayer.playedCards.remove(cardToPlay);
                        } 
                        if (currentPlayer.nextTurnCards.contains(cardToPlay)) { 
                            ((CardImpl) cardToPlay).trashAfterPlay = true;
                        }
                    }

                    Card cardToGain = currentPlayer.controlPlayer.procession_cardToGain(context, 1 + cardToPlay.getCost(context), cardToPlay.getDebtCost(context), cardToPlay.costPotion());
                    if ((cardToGain != null) && (cardToPlay.getCost(context) + 1) == cardToGain.getCost(context) && 
                    		cardToPlay.getDebtCost(context) == cardToGain.getDebtCost(context) && 
                    		cardToPlay.costPotion() == cardToGain.costPotion()) {
                        currentPlayer.gainNewCard(cardToGain, this.controlCard, context);
                    }
                }
            }
        }
        return cardToPlay;
    }

    protected void discardMultiple(MoveContext context, Player currentPlayer, int numToDiscard) {
    	CardList hand = currentPlayer.getHand();
    	if (hand.size() == 0)
    		return;
    	Card[] cardsToDiscard;
    	if (hand.size() <= numToDiscard) {
    		cardsToDiscard = new Card[currentPlayer.getHand().size()];
    		for (int i = 0; i < cardsToDiscard.length; ++i) {
    			cardsToDiscard[i] = hand.get(i);
    		}
    	} else {
    		cardsToDiscard = currentPlayer.controlPlayer.discardMultiple_cardsToDiscard(context, this, numToDiscard);
            if (cardsToDiscard == null || cardsToDiscard.length != numToDiscard || !Util.areCardsInHand(cardsToDiscard, context)) {
                if (currentPlayer.hand.size() >= numToDiscard) {
                    Util.playerError(currentPlayer, "Discard error, just discarding the first " + numToDiscard + ".");
                }
                cardsToDiscard = new Card[Math.min(numToDiscard, currentPlayer.hand.size())];
                for (int i = 0; i < cardsToDiscard.length; i++) {
                    cardsToDiscard[i] = currentPlayer.hand.get(i);
                }
            }
    	}
        
        for (Card card : cardsToDiscard) {
            currentPlayer.hand.remove(card);
            currentPlayer.discard(card, this.controlCard, context);
        }
    }

    protected void spyAndScryingPool(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            // Note that this.controlCard is the opposite check of other attacks, the spy/scrying pool lets
            // the current player look at their own deck which is a good thing, so always
            // allow that
            if (player == currentPlayer || (!Util.isDefendedFromAttack(game, player, this))) {
                if (player != currentPlayer) {
                    player.attacked(this.controlCard, context);
                }

                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                Card card = game.draw(playerContext, this, 1);

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
                        player.putOnTopOfDeck(card, playerContext, true);
                    }
                }
            }
        }

        if(equals(Cards.scryingPool)) {
            ArrayList<Card> cardsToPutInHand = new ArrayList<Card>();

            Card draw = null;
            while ((draw = game.draw(context, Cards.scryingPool, -1)) != null) {
                currentPlayer.reveal(draw, this.controlCard, new MoveContext(context, game, currentPlayer));
                cardsToPutInHand.add(draw);
                if(!(draw.is(Type.Action, currentPlayer))) {
                    break;
                }
            }

            for(Card card : cardsToPutInHand) {
                currentPlayer.hand.add(card);
            }
        }
    }
}
