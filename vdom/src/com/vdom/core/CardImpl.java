package com.vdom.core;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.DurationCard;
import com.vdom.api.VictoryCard;

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
    
    boolean isAttack  = false;
    boolean isPrize   = false;
    boolean isShelter = false;
    boolean isRuins   = false;
    boolean isKnight  = false;
    boolean isLooter  = false;
    boolean isOverpay = false;  // can this card be overpaid for?
    boolean isEvent = false;
    boolean isReserve = false;
    boolean isTraveller = false;
    boolean isCastle = false;
    boolean isGathering = false;
    boolean isLandmark = false;
    
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
        isAttack    = builder.attack;
        isPrize     = builder.isPrize;
        isShelter   = builder.isShelter;
        isRuins     = builder.isRuins;
        isKnight    = builder.isKnight;
        isLooter    = builder.isLooter;
        isOverpay   = builder.isOverpay;
        isEvent     = builder.isEvent;
        isReserve   = builder.isReserve;
        isTraveller = builder.isTraveller;
        isCastle    = builder.isCastle;
        isGathering = builder.isGathering;
        isLandmark  = builder.isLandmark;
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
        
        protected boolean attack      = false;
        protected boolean isPrize     = false;
        protected boolean isShelter   = false;
        protected boolean isRuins     = false;
        protected boolean isKnight    = false;
        protected boolean isLooter    = false;
        protected boolean isOverpay   = false;
        protected boolean isEvent     = false;
        protected boolean isReserve   = false;
        protected boolean isTraveller = false;
        protected boolean isGathering = false;
        protected boolean isCastle    = false;
        protected boolean isLandmark  = false;
        
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

        public Builder isEvent() {
            isEvent = true;
            return this;
        }

        public Builder isLandmark() {
            isLandmark = true;
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
        CardImpl c = new CardImpl();
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
        c.isAttack = isAttack;
        c.isPrize = isPrize;
        c.isShelter = isShelter;
        c.isRuins = isRuins;
        c.isKnight = isKnight;
        c.isLooter = isLooter;
        c.isOverpay = isOverpay;
        c.isEvent = isEvent;
        c.isReserve = isReserve;
        c.isTraveller = isTraveller;
        c.isCastle = isCastle;
        c.isGathering = isGathering;
        c.isLandmark = isLandmark;
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
    	if (this.isEvent()) return cost; //Costs of Events are not affected by cards like Bridge Troll.
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
        costModifier -= this.isAction(null) ? (2 * context.countCardsInPlay(Cards.quarry)) : 0;
        costModifier -= context.countCardsInPlay(Cards.highway);
        costModifier -= currentPlayerContext.countCardsInPlay(Cards.bridgeTroll);
        costModifier -= 2 * context.countCardsInPlay(Cards.princess);
        //TODO: BUG if an inherited peddler is yours, its cost should lower during your buy phase 
        costModifier -= (buyPhase && this.equals(Cards.peddler)) ? (2 * context.countActionCardsInPlayThisTurn()) : 0;
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

    public boolean isVictory(MoveContext context) {
        if (context == null)
            return false;
        
        if (this.equals(Cards.virtualKnight))
            if(context.game.getTopKnightCard() != null && !context.game.getTopKnightCard().equals(Cards.virtualKnight))
                return (context.game.getTopKnightCard() instanceof VictoryCard); 

        return (this instanceof VictoryCard);
    }
    
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
    }
    
    @Override
    public void play(Game game, MoveContext context, boolean fromHand) {
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
    public boolean isDuration(Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return this instanceof DurationCard;
    	return player.getInheritance() instanceof DurationCard;
    }
    
    @Override
    public boolean isAction(Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return this instanceof ActionCard;
    	return player.getInheritance() instanceof ActionCard;
    }
    
    @Override
    public boolean isAttack(Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return isAttack;
    	return ((CardImpl)player.getInheritance()).isAttack;
    }    
    @Override
    public boolean isPrize() {
        return isPrize;
    }

    @Override
    public boolean isKnight(Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return isKnight;
    	return ((CardImpl)player.getInheritance()).isKnight;
    }
    
    @Override
    public boolean isLooter() {
        return isLooter;
    }
    
    @Override
    public boolean isOverpay(Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return isOverpay;
    	return ((CardImpl)player.getInheritance()).isOverpay;
    }
    
    @Override
    public boolean isEvent()
    {
        return isEvent;
    }
    
    @Override
    public boolean isReserve(Player player)
    {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return isReserve;
    	return ((CardImpl)player.getInheritance()).isReserve;
    }
    
    @Override
    public boolean isTraveller(Player player)
    {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return isTraveller;
    	return ((CardImpl)player.getInheritance()).isTraveller;
    }
    
    @Override
    public boolean isCastle(Player player)
    {
        return isCastle;
    }
    
    @Override
    public boolean isGathering(Player player)
    {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return isGathering;
    	return ((CardImpl)player.getInheritance()).isGathering;
    }
    
    @Override
    public boolean isLandmark()
    {
        return isLandmark;
    }

	@Override
    public void isBuying(MoveContext context) {
		if (is(Type.Victory, context.getPlayer())) {
			context.game.trashHovelsInHandOption(context.player, context, this);
		}
		if (is(Type.Event, null)) {
			context.buys += addBuys;
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
    public void isTrashed(MoveContext context) {
    	Cards.Kind trashKind = this.controlCard.getKind();
    	if (this.controlCard.equals(Cards.estate) && context.player.getInheritance() != null) {
    		trashKind = context.player.getInheritance().getKind();
    	}
    	
        switch (trashKind) {
            case Rats:
                context.game.drawToHand(context, this, 1, true);
                break;
            case Squire:
                // Need to ensure that there is at least one Attack card that can be gained,
                // otherwise this.controlCard choice should be bypassed.
                boolean attackCardAvailable = false;

                for (Card c : context.game.getCardsInGame())
                {
                    if (Cards.isSupplyCard(c) && c.isAttack(null) && context.game.getPile(c).getCount() > 0) {
                        attackCardAvailable = true;
                        break;
                    }
                }

                if (attackCardAvailable)
                {
                    Card s = context.player.controlPlayer.squire_cardToObtain(context);

                    if (s != null) 
                    {
                        context.player.controlPlayer.gainNewCard(s, this.controlCard, context);
                    }
                }
                break;
            case Catacombs:
            	int cost = this.controlCard.equals(Cards.estate) ? this.controlCard.getCost(context) : this.getCost(context);
            	cost--;
            	if (cost >= 0) {
	                Card c = context.player.controlPlayer.catacombs_cardToObtain(context, cost);
	                if (c != null) {
	                    context.player.controlPlayer.gainNewCard(c, this.controlCard, context);
	                }
            	}
                break;
            case HuntingGrounds:
                  // Wiki: If you trash Hunting Grounds and the Duchy pile is empty,
                  // you can still choose Duchy (and gain nothing). 
                int duchyCount      = context.game.getPile(Cards.duchy).getCount();
                int estateCount     = context.game.getPile(Cards.estate).getCount();
                boolean gainDuchy   = false;
                boolean gainEstates = false;

                if (duchyCount > 0 || estateCount > 0)
                {
                    Player.HuntingGroundsOption option = context.player.controlPlayer.huntingGrounds_chooseOption(context);
                    if (option != null) {
                        switch (option) {
                            case GainDuchy:
                                gainDuchy = true;
                                break;
                            case GainEstates:
                                gainEstates = true;
                                break;
                            default:
                                break;
                        }
                    }
                }
                
                if (gainDuchy)
                {
                    context.player.controlPlayer.gainNewCard(Cards.duchy, this.controlCard, context);
                }
                else if (gainEstates)
                {
                    context.player.controlPlayer.gainNewCard(Cards.estate, this.controlCard, context);
                    context.player.controlPlayer.gainNewCard(Cards.estate, this.controlCard, context);
                    context.player.controlPlayer.gainNewCard(Cards.estate, this.controlCard, context);
                }

                break;
            case Fortress:
            	//TODO: if Possessed, give choice of whether to put in hand or set aside card
                context.game.trashPile.remove(this.controlCard);
                context.player.hand.add(this.controlCard);
                break;
            case Cultist:
                context.game.drawToHand(context, this, 3, false);
                context.game.drawToHand(context, this, 2, false);
                context.game.drawToHand(context, this, 1, false);
                break;
            case SirVander:
                context.player.controlPlayer.gainNewCard(Cards.gold, this.controlCard, context);
                break;
            case OvergrownEstate:
                context.game.drawToHand(context, controlCard, 1);
                break;
            case Feodum:
                context.player.controlPlayer.gainNewCard(Cards.silver, this, context);
                context.player.controlPlayer.gainNewCard(Cards.silver, this, context);
                context.player.controlPlayer.gainNewCard(Cards.silver, this, context);
                break;
            case Rocks:
            	context.player.gainNewCard(Cards.silver, this, context);
            	break;
            case CrumblingCastle:
            	context.player.addVictoryTokens(context, 1);
            	context.player.gainNewCard(Cards.silver, this, context);
            default:
                break;
        }
        
        // card left play - stop any impersonations
        this.controlCard.stopImpersonatingCard();
        this.controlCard.stopInheritingCardAbilities();
    }

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

    /*@Override
    public void isGained(MoveContext context) {
        
    }*/
}
