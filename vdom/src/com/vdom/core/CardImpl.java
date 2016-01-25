package com.vdom.core;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.DurationCard;
import com.vdom.api.VictoryCard;

public class CardImpl implements Card {
    // Template (immutable)
    Cards.Type type;
    CardImpl templateCard;
    String name;
    String safeName;
    int cost;
    boolean costPotion = false;

    String description = "";
    String expansion = "";
    protected int vp;
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

    static int maxNameLen;    // across all cards

    // Implementation (mutable)
    private Integer id;
    boolean movedToNextTurnPile = false;
    boolean trashAfterPlay = false;
    int numberTimesAlreadyPlayed = 0;
    int cloneCount = 1;
    CardImpl impersonatingCard = null;
    CardImpl controlCard = this;

    protected CardImpl(Cards.Type type, int cost) {
        this.type = type;
        this.name = type.toString();
        if (maxNameLen < name.length()) {
            maxNameLen = name.length();
        }
        this.cost = cost;
    }
    
    public CardImpl(Builder builder) {
        this(builder.type, builder.cost);
        costPotion = builder.costPotion;
        vp = builder.vp;
        description = builder.description;
        expansion   = builder.expansion;
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
    }

    public static class Builder {
        protected Cards.Type type;
        protected String name;
        protected int cost;
        protected int vp = 0;

        protected boolean costPotion = false;
        protected String description = "";
        protected String expansion = "";

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


        public Builder(Cards.Type type, int cost) {
            this.type = type;
            this.name = type.toString();
            this.cost = cost;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder expansion(String val) {
            expansion = val;
            return this;
        }

        public Builder costPotion() {
            costPotion = true;
            return this;
        }

        public Builder vp(int val) {
            vp = val;
            return this;
        }

        public Builder attack() {
            attack = true;
            return this;
        }

        public Builder isPrize() {
            isPrize = true;
            return this;
        }
        
        public Builder isShelter() {
            isShelter = true;
            return this;
        }

        public Builder isRuins() {
            isRuins = true;
            return this;
        }
        
        public Builder isLooter() {
            isLooter = true;
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

        public Builder isReserve() {
            isReserve = true;
            return this;
        }

        public CardImpl build() {
            return new CardImpl(this);
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

        c.type = type;
        c.name = name;
        c.cost = cost;
        c.costPotion = costPotion;
        c.description = description;
        c.expansion = expansion;
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
        c.vp = vp;
    }

    public Cards.Type getType() {
        return type;
    }

    public String getName() {
        return name;
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

        int costModifier = 0;
        costModifier -= this.isAction() ? (2 * context.countCardsInPlay(Cards.quarry)) : 0;
        costModifier -= context.countCardsInPlay(Cards.highway);
        costModifier -= context.countCardsInPlay(Cards.bridgeTroll);
        costModifier -= context.countCardsInNextTurn(Cards.bridgeTroll);
        costModifier -= 2 * context.countCardsInPlay(Cards.princess);
        costModifier -= (buyPhase && this.equals(Cards.peddler)) ? (2 * context.countActionCardsInPlayThisTurn()) : 0;
        costModifier -= (context.game.isPlayerSupplyTokenOnPile(this, context.game.getCurrentPlayer(), PlayerSupplyToken.MinusTwoCost)) ? 2 : 0;
        //costModifier -= (this.isKnight ? (cost - game. (2 * context.countCardsInPlay(Cards.quarry)) : 0;

        return Math.max(0, cost + costModifier + context.cardCostModifier/*bridge*/);
    }

    public boolean isVictory(MoveContext context) {
        if (context == null)
            return false;
        
        if (this.equals(Cards.virtualKnight))
            if(context.game.getTopKnightCard() != null && !context.game.getTopKnightCard().equals(Cards.virtualKnight))
                return (context.game.getTopKnightCard() instanceof VictoryCard); 

        return (this instanceof VictoryCard);
    }
    
    @Override
    public int getAddCards() {
    	return 0;
    }
    
    @Override
    public int getAddActions() {
    	return 0;
    }
    
    @Override
    public int getAddGold() {
    	return 0;
    }
    
    @Override
    public int getAddBuys() {
    	return 0;
    }
    
    @Override
    public int getAddVictoryTokens() {
    	return 0;
    }
    
    @Override
    public int getAddCardsNextTurn() {
    	return 0;
    }
    
    @Override
    public int getAddActionsNextTurn() {
    	return 0;
    }
    
    @Override
    public int getAddGoldNextTurn() {
    	return 0;
    }
    
    @Override
    public int getAddBuysNextTurn() {
    	return 0;
    }
    
    @Override
    public boolean takeAnotherTurn() {
    	return false;
    }
    
    @Override
    public int takeAnotherTurnCardCount() {
    	return 0;
    }
    
    @Override
    public boolean trashForced() {
    	return false;
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
            sb.append ("(" + cost + (costPotion ? "p)" : ") "));
        if (vp > 0) {
            sb.append(", " + vp + " victory point");
            if (vp > 1) {
                sb.append("s");
            }
        }
        return sb.toString();
    }

    public String getExpansion() {
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
    
    public boolean isAction() {
    	return this instanceof ActionCard;
    }
    
    @Override
    public boolean isDuration(Player player) {
    	if (player == null || player.getInheritance() == null)
    		return this instanceof DurationCard;
    	return player.getInheritance() instanceof DurationCard;
    }
    
    @Override
    public boolean isAction(Player player) {
    	//TODO: redo?
    	return player.getInheritance() != null && this.getType() == player.getInheritance().getType();
    }
    
    @Override
    public boolean isAttack(Player player) {
    	if (player == null || player.getInheritance() == null)
    		return isAttack;
    	return ((CardImpl)player.getInheritance()).isAttack;
    }    
    @Override
    public boolean isPrize() {
        return isPrize;
    }
    @Override
    public boolean isShelter() {
        return isShelter;
    }
    @Override
    public boolean isRuins() {
        return isRuins;
    }
    @Override
    public boolean isKnight() {
        return isKnight;
    }
    
    @Override
    public boolean isLooter() {
        return isLooter;
    }
    
    @Override
    public boolean isOverpay()
    {
        return isOverpay;
    }
    
    @Override
    public boolean isEvent()
    {
        return isEvent;
    }
    
    @Override
    public boolean isReserve()
    {
        return isReserve;
    }
    
    @Override
    public boolean isTraveller()
    {
        return isTraveller;
    }
    
    @Override
    public void isBuying(MoveContext context) {
    }
    
    @Override
    public void isBought(MoveContext context) {
    }
    
    @Override
    public void isTrashed(MoveContext context) {
    }

    public boolean isImpersonatingAnotherCard() {
        return !(this.impersonatingCard == null);
    }

    public Card behaveAsCard() {
        return (this.impersonatingCard == null ? this : this.impersonatingCard);
    }

//    CardImpl getImpersonatingCard() {
//        return impersonatingCard;
//    }

    void startImpersonatingCard(CardImpl impersonatingCard) {
        impersonatingCard.setControlCard(this);
        this.impersonatingCard = impersonatingCard;
        }

    void stopImpersonatingCard() {
        this.impersonatingCard = null;
        }

    @Override
    public CardImpl getControlCard() {
        return controlCard;
    }

    void setControlCard(CardImpl controlCard) {
        this.controlCard = controlCard;
    }
    
    protected void placeToken(MoveContext context, ActionCard card, PlayerSupplyToken token) {
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
        for (int i = currentPlayer.playedCards.size() - 1; i > 0 ; ) {
            Card c = currentPlayer.playedCards.get(--i);
            if (c.behaveAsCard().getType() == Cards.Type.Urchin && currentPlayer.controlPlayer.urchin_shouldTrashForMercenary(context)) {
                currentPlayer.trash(c.getControlCard(), this, context);
                currentPlayer.gainNewCard(Cards.mercenary, this, context);
                currentPlayer.playedCards.remove(i);
            }
        }
    }

    /*@Override
    public void isGained(MoveContext context) {
        
    }*/
}
