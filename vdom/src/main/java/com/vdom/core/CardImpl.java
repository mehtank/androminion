package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.MoveContext.TurnPhase;

public class CardImpl implements Card, Comparable<Card>{
	private static final long serialVersionUID = 1L;
    // Template (immutable)
    Cards.Kind kind;
    CardImpl templateCard;
    String name;
    String safeName;
    int cost;
    int debtCost;
    boolean costPotion = false;

    boolean isPlaceholderCard = false;

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
    protected int addCoffers;
    protected int addVillagers;
    protected boolean takeAnotherTurn;
    protected int takeAnotherTurnCardCount;
    protected boolean hasPlusCoin;
    
    boolean isOverpay = false;  // can this card be overpaid for?
    
    protected Card heirloom;
    protected Card[] linkedStates;
    
    protected boolean callableWhenCardGained;
    protected boolean callableWhenActionResolved;
    protected boolean actionStillNeedsToBeInPlay;
    protected boolean callableWhenTurnStarts;
    protected int callableWhenCardGainedMaxCost = -1;

    protected PileCreator pileCreator = null;

    static int maxNameLen;    // across all cards
    
    private ArrayList<Card> multiplyingCards;
    
    // Implementation (mutable)
    private Integer id;

    protected CardImpl(Cards.Kind kind, int cost) {
        this.kind = kind;
        this.name = kind.toString();
        if (maxNameLen < name.length()) {
            maxNameLen = name.length();
        }
        this.cost = cost;
        this.types = new Type[0];
    }
    
    public CardImpl(Builder builder) {
        this(builder.kind, builder.cost);
        costPotion = builder.costPotion;
        debtCost = builder.debtCost;
        vp = builder.vp;
        description = builder.description;
        expansion   = builder.expansion;
        types       = builder.types != null ? builder.types : new Type[0];
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
        addCoffers = builder.addCoffers;
        addVillagers = builder.addVillagers;
        takeAnotherTurn = builder.takeAnotherTurn;
        takeAnotherTurnCardCount = builder.takeAnotherTurnCardCount;
        isOverpay   = builder.isOverpay;
        heirloom = builder.heirloom;
        linkedStates = builder.linkedStates.toArray(new Card[0]);
        hasPlusCoin = builder.hasPlusCoin;
                
        callableWhenCardGained = builder.callableWhenCardGained;
        callableWhenActionResolved = builder.callableWhenActionResolved;
        actionStillNeedsToBeInPlay = builder.actionStillNeedsToBeInPlay;
        callableWhenTurnStarts = builder.callableWhenTurnStarts;
        callableWhenCardGainedMaxCost = builder.callableWhenCardGainedMaxCost;
        
        pileCreator = builder.pileCreator;
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
        protected boolean hasPlusCoin = false;
        protected int addVictoryTokens;
        protected boolean trashForced = false;
        protected boolean providePotion = false;
        protected int addActionsNextTurn;
        protected int addBuysNextTurn;
        protected int addCardsNextTurn;
        protected int addGoldNextTurn;
        protected int addCoffers;
        protected int addVillagers;
        protected boolean takeAnotherTurn;
        protected int takeAnotherTurnCardCount;
        
        protected boolean callableWhenCardGained;
        protected boolean callableWhenActionResolved;
        protected boolean actionStillNeedsToBeInPlay;
        protected boolean callableWhenTurnStarts;
        protected int callableWhenCardGainedMaxCost;
        
        protected ArrayList<Card> linkedStates = new ArrayList<Card>();

        protected PileCreator  pileCreator = null;
        
        protected boolean isOverpay   = false;
                
        protected Card heirloom;

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
            hasPlusCoin = true;
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
            hasPlusCoin = true;
            return this;
        }
        
        public Builder addCoffers(int val) {
        	addCoffers = val;
        	return this;
        }
        
        public Builder addVillagers(int val) {
        	addVillagers = val;
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

        public Builder heirloom(Card val) {
        	heirloom = val;
        	return this;
        }
        
        public Builder linkedState(Card val) {
        	linkedStates.add(val);
        	return this;
        }
        

        public Builder hasPlusCoin() {
        	hasPlusCoin = true;
        	return this;
        }

        public Builder pileCreator(PileCreator creator) {
            pileCreator = creator;
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
        	case Nocturne:
        		return new CardImplNocturne(this);
        	case Renaissance:
        		return new CardImplRenaissance(this);
        	case Menagerie:
        		return new CardImplMenagerie(this);
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

    public boolean isPlaceholderCard() {return isPlaceholderCard; }
    public void setPlaceholderCard() { isPlaceholderCard = true; }

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
	    	case Nocturne:
	    		c = new CardImplNocturne();
	    		break;
	    	case Renaissance:
	    		c = new CardImplRenaissance();
	    		break;
	    	case Menagerie:
	    		c = new CardImplMenagerie();
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
        c.addCoffers = addCoffers;
        c.addVillagers = addVillagers;
        c.takeAnotherTurn = takeAnotherTurn;
        c.takeAnotherTurnCardCount = takeAnotherTurnCardCount;
        c.isOverpay = isOverpay;
        c.vp = vp;
        c.heirloom = heirloom;
        c.linkedStates = linkedStates;
        c.hasPlusCoin = hasPlusCoin;
        
        c.callableWhenCardGained = callableWhenCardGained;
        c.callableWhenActionResolved = callableWhenActionResolved;
        c.callableWhenTurnStarts = callableWhenTurnStarts;
        c.callableWhenCardGainedMaxCost = callableWhenCardGainedMaxCost;
        c.actionStillNeedsToBeInPlay = actionStillNeedsToBeInPlay;
    }

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
    	return is(t, player, null);
    }
    
    @Override
    public boolean is(Type t, Player player, MoveContext context) {
    	player = player != null ? player : (context != null ? context.player : null);
    	Type[] types = getTypes(player);
    	for (int i = 0; i < types.length; ++i) {
    		if (types[i] == t) return true;
    	}
    	return false;
    }

    public int getNumberOfTypes(Player player) {
    	return getTypes(player).length;
    }
    
    public Type[] getTypes() {
    	return getTypes(null);
    }
        
    public Type[] getTypes(Player player) {
        if (player == null || (!player.hasProject(Cards.capitalism) && (player.getInheritance() == null || !this.equals(Cards.estate)))) {
            return types;
        }
        Set<Type> typeSet = new HashSet<Type>();
        typeSet.addAll(Arrays.asList(types));
        boolean isPlayersTurn = player.game.getCurrentPlayer() == player;
        if (this.equals(Cards.estate) && isPlayersTurn) {
        	typeSet.add(Type.Action);
        }
        if (player.hasProject(Cards.capitalism) && this.hasPlusCoin() && typeSet.contains(Type.Action) && isPlayersTurn) {
        	typeSet.add(Type.Treasure);
        }
        
        Type[] result = new Type[typeSet.size()];
        int i = 0;
        for (Type t : typeSet) {
        	result[i++] = t;
        }
        return result;
    }

    public int getCost(MoveContext context) {
        if (context == null)
            return cost;
        return getCost(context, context.phase == TurnPhase.Buy);
    }

    public int getCost(MoveContext context, boolean buyPhase) {
    	if (this.is(Type.Event) || this.is(Type.Project)) return cost; //Costs of Events/Projects are not affected by cards like Bridge Troll.

        //If it's a variable card pile, and it's not empty, return the cost of the top card
        if (this.isPlaceholderCard()) {
            CardPile pile = context.game.getPile(this);
            if (!pile.isEmpty()) {
                return context.game.getPile(this).topCard().getCost(context, buyPhase);
            }
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
        costModifier -= (context.game.isPlayerSupplyTokenOnPile(this,
        		context.game.getCurrentPlayer(), PlayerSupplyToken.MinusTwoCost)) ? 2 : 0;
        costModifier -= context.game.getCurrentPlayer().hasProject(Cards.canal) ? 1 : 0;
        
        int finalCost = Math.max(0, cost + costModifier + context.cardCostModifier/*bridge*/);
        
        if (this.equals(Cards.destrier) && context.phase != null) {
        	finalCost = Math.max(0, finalCost - context.getNumCardsGainedThisTurn());
        }
        
        if (this.equals(Cards.fisherman) && context.phase != null) {
        	if (context.game.getCurrentPlayer().getDiscardSize() == 0) return Math.max(0, finalCost - 3);
        }
        
        if (this.equals(Cards.wayfarer)) {
        	Card nonWayfarer = getLastNonMatching(Cards.wayfarer, context.game.getCardsObtainedThisTurn());
        	if (nonWayfarer != null) {
        		return nonWayfarer.getCost(context, buyPhase);
        	}
        }
        
        return finalCost;
    }
    
    private Card getLastNonMatching(Card matchMe, ArrayList<Card> cards) {
		for (int i = cards.size() - 1; i >= 0; i--) {
			Card c = cards.get(i);
			if (c.equals(matchMe)) {
				continue;
			}
			return c;
		}
		return null;
	}

	public int getDebtCost(MoveContext context) {
		if (this.equals(Cards.wayfarer) && context != null) {
        	Card nonWayfarer = getLastNonMatching(Cards.wayfarer, context.game.getCardsObtainedThisTurn());
        	if (nonWayfarer != null) {
        		return nonWayfarer.getDebtCost(context);
        	}
        }
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
    public int getAddCoffers() {
    	return addCoffers;
    }
    
    @Override
    public int getAddVillagers() {
    	return addVillagers;
    }

    @Override
    public boolean takeAnotherTurn() {
        return takeAnotherTurn;
    }

    @Override
    public int takeAnotherTurnCardCount() {
        return takeAnotherTurnCardCount;
    }
    
    @Override
    public Card getHeirloom() {
    	return heirloom;
    }
    
    @Override
    public Card[] getLinkedStates() {
    	return linkedStates;
    }
    
    @Override
    public boolean hasPlusCoin() {
        return hasPlusCoin;
    }
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    @Override
    public void play(Game game, MoveContext context, boolean fromHand) {
    	play(game, context, fromHand, false, false);
    }

    @Override
    public void followInstructions(Game game, MoveContext context, Card responsible, Player currentPlayer, boolean isThronedEffect) {
        followInstructions(game, context, responsible, currentPlayer, isThronedEffect, false);
    }

    @Override
    public void followInstructions(Game game, MoveContext context, Card responsible, Player currentPlayer, boolean isThronedEffect, boolean usingChameleon) {
        //Events for Boon/Hex
        //TODO: !(is(Type.State) || is(Type.Project) || is(Type.State) || is(Type.Artifact))
        //TODO: What event to fire for other instructions on non-boon/hex abilities (e.g. Key)?
        //  Should this be shown in the play area as an ability like Durations?
        if (is(Type.Boon) || is(Type.Hex)) {
            GameEvent event;
            event = new GameEvent(GameEvent.EventType.ReceivingBoonHex, (MoveContext) context);
            event.card = this;
            event.newCard = !isThronedEffect;
            game.broadcastEvent(event);
        }

        //Standardized instructions
        //+Card
        for (int i = 0; i < addCards; ++i) {
            game.drawToHand(context, this, addCards - i);
        }
        //+Action
        context.addActions(addActions, this);
        //+Buy
        context.buys += addBuys;
        //+Coin
        if (this.equals(Cards.copper)) {
            context.addCoins(addGold + context.coppersmithsPlayed);
        } else if (this.equals(Cards.silver) || this.equals(Cards.gold)) {
            context.addCoins(context.envious ? 1 : addGold);
        } else {
            context.addCoins(addGold);
        }
        //+Potion
        if (providePotion()) {
            context.potions++;
        }
        //+VP
        currentPlayer.addVictoryTokens(context, addVictoryTokens, this);
        //Coffers
        context.player.gainGuildsCoinTokens(addCoffers, context, this);
        //+Villagers
        context.player.takeVillagers(addVillagers, context, this);
        //TODO: +Favors

        // Vanilla next-turn bonuses
        if (addCardsNextTurn > 0 || addBuysNextTurn > 0 || addActionsNextTurn > 0 || addGoldNextTurn > 0) {
            currentPlayer.addStartTurnDurationEffect(this, 1, isThronedEffect);
        }

        // Estate for Inheritance
        switch (this.getKind()) {
            case Estate:
                if (currentPlayer.getInheritance() != null) {
                    currentPlayer.getInheritance().play(game, context, false, true, false);
                }
                break;
        }

        // Done receiving event
        if (is(Type.Boon) || is(Type.Hex)) {
            GameEvent event;
            event = new GameEvent(GameEvent.EventType.ReceivedBoonHex, (MoveContext) context);
            event.card = this;
            game.broadcastEvent(event);
        }
    }

    @Override
    public void play(Game game, MoveContext context, boolean fromHand, boolean leaveThere, boolean isThronedPlay) {
        Player currentPlayer = context.getPlayer();

        boolean isAction = this.is(Type.Action, currentPlayer);
        boolean enchantressEffect = isAction && !context.enchantressAlreadyAffected && game.enchantressAttacks(currentPlayer);
        if (enchantressEffect) context.enchantressAlreadyAffected = true;
        
        int handIdx = currentPlayer.hand.indexOf(this.getId());
        if (fromHand && handIdx >= 0)
            currentPlayer.hand.remove(handIdx);
        if (!leaveThere && !isThronedPlay) {
            currentPlayer.playedCards.add(this);
        }

        GameEvent event;
        event = new GameEvent(GameEvent.EventType.PlayingCard, (MoveContext) context);
        event.card = this;
        event.newCard = !isThronedPlay;
        game.broadcastEvent(event);

        if (isAction) {
            context.addActions(currentPlayer.championEffects, Cards.champion);
        }
        if (context.kilnEffect) {
            context.kilnEffect = false;
            if (currentPlayer.controlPlayer.kiln_shouldGainCopy(context, this))
                currentPlayer.gainNewCard(this, Cards.kiln, context);
        }
        if (equals(Cards.silver)) {
            silverPlayed(context, game, currentPlayer);
        }
        if (is(Type.Attack, currentPlayer))
            attackPlayed(context, game, currentPlayer);
        
        // playing an action
        boolean playAgainWithCitadel = false;
        if (isAction) {
            context.actionsPlayedSoFar++;
            context.actionsPlayedThisTurnStillInPlay.add(this);
	        if (context.actionsPlayedSoFar == 1 && context.player.hasProject(Cards.citadel)
        		 && game.getCurrentPlayer() == context.player)
	        	playAgainWithCitadel = true;
        }
        
        Card tokenPile = this;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneAction))
        	context.addActions(1, this);
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneBuy))
        	context.buys += 1;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneCoin))
        	context.addCoins(1);
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneCard))
        	game.drawToHand(context, this, 1 + addCards);
        
        Card playUsingWay = selectWayToPlay(context, this);
        
        if (playUsingWay != null) {
        	//play way ability...
            GameEvent wayEvent;
            wayEvent = new GameEvent(GameEvent.EventType.UsedWay, (MoveContext) context);
            wayEvent.card = playUsingWay;
            wayEvent.newCard = false;
            game.broadcastEvent(wayEvent);
            playUsingWay.followInstructions(game, context, this, currentPlayer, isThronedPlay);
        } else if (enchantressEffect) {
        	//allow reaction to playing an attack card with Enchantress effect
        	if (is(Type.Attack, currentPlayer)) {
        		 for (Player player : game.getPlayersInTurnOrder()) {
    	            if (player != currentPlayer) Util.isDefendedFromAttack(game, player, this);
	            }
        	}
        	context.addActions(1, Cards.enchantress);
        	game.drawToHand(context, this, 1);
        } else {
        	followInstructions(game, context, this, currentPlayer, isThronedPlay);
        }
    
        if (isAction || is(Type.Night)) {
        	// Don't broadcast card played event for non-action Treasures
	        event = new GameEvent(GameEvent.EventType.PlayedCard, (MoveContext) context);
	        event.card = this;
	        game.broadcastEvent(event);
        }

        // test if any cards left play
        currentPlayer.checkForCardsLeftPlay(currentPlayer, context);
        
        // Citadel
        if (playAgainWithCitadel) {
        	this.play(game, context, false, true,true);
        }
        
        // check for cards to call after resolving action
        if (isAction) {
	        boolean isActionInPlay = currentPlayer.isInPlay(this);
	        ArrayList<Card> callableCards = new ArrayList<Card>();
	        Card toCall = null;
	        for (Card c : currentPlayer.tavern) {
	        	if (c.isCallableWhenActionResolved()) {
	        		if (c.doesActionStillNeedToBeInPlayToCall() && !isActionInPlay) {
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
		        	toCall = currentPlayer.controlPlayer.call_whenActionResolveCardToCall(context, this, cardsAsArray);
		        	if (toCall != null && callableCards.contains(toCall)) {
		        		callableCards.remove(toCall);
		        		toCall.callWhenActionResolved(context, this);
		        	}
			        // loop while we still have cards to call
			        // NOTE: we have a hack here to prevent asking for duplicate calls on an unused Royal Carriage
			        //   since technically you can ask for more and action re-played by royal carriage will ask as well
		        } while (toCall != null && toCall.equals(Cards.coinOfTheRealm) && !callableCards.isEmpty());
	        }
        }
    }

    private Card selectWayToPlay(MoveContext context, Card playedCard) {
        //Avoid needlessly asking to play Way of the Mouse set aside card using Way of the Mouse
        if (playedCard.equals(context.game.wayOfTheMouseCard)) return null;
    	if (!playedCard.is(Type.Action, context.player)) return null;
		List<Card> ways = Arrays.asList(context.game.getCardsInGame(GetCardsInGameOptions.Templates, false, Type.Way));
		if (!ways.isEmpty()) {
			Card way = context.player.controlPlayer.action_playUsingWay(context, playedCard);
			if (way == null) return null;
			if (!ways.contains(way)) {
				Util.playerError(context.player, "Way error: not playing as Way");
				return null;
			}
			return way;
		}
		return null;
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
        if (addActions > 0 || addBuys > 0 || addCards > 0 || addGold > 0 || addCoffers > 0 || addVillagers > 0) {
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
            if (addCoffers > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addCoffers + " Coffers");
            }
            if (addVillagers > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addVillagers + " Villager" + (addVillagers != 1 ? "s" : ""));
            }
            if (addGold > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addGold + " Coin");
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
    	if (this.equals(Cards.wayfarer) && Game.players != null && Game.players[Game.playersTurn] != null) {
        	Card nonWayfarer = getLastNonMatching(Cards.wayfarer, Game.players[Game.playersTurn].game.getCardsObtainedThisTurn());
        	if (nonWayfarer != null) {
        		return nonWayfarer.costPotion();
        	}
        }
        return costPotion;
    }
    
    public int debtCost() {
    	return debtCost;
    }
    
    @Override
    public boolean isOverpay(Player player) {
    	return isOverpay;
    }
    
	@Override
    public void isBuying(MoveContext context) {
		if (is(Type.Victory, context.getPlayer())) {
			context.game.trashHovelsInHandOption(context.player, context, this);
		}
		if (is(Type.Event)) {
			context.buys += addBuys;
			context.getPlayer().addVictoryTokens(context, addVictoryTokens, this);
		}
    }

    @Override
    public void isTrashed(MoveContext context) {

    }
        
    @Override
    public boolean isCallableWhenCardGained() {
    	return callableWhenCardGained;
    }
    
    @Override
    public int getCallableWhenGainedMaxCost() {
    	return callableWhenCardGainedMaxCost;
    }
    
    @Override
    public boolean isCallableWhenActionResolved() {
    	return callableWhenActionResolved;
    }
    
    @Override
    public boolean doesActionStillNeedToBeInPlayToCall() {
    	return actionStillNeedsToBeInPlay;
    }
    
    @Override
    public boolean isCallableWhenTurnStarts() {
    	return callableWhenTurnStarts;
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

    public void multiplyCard(Card toMultiply) {
    	if (multiplyingCards == null) multiplyingCards = new ArrayList<Card>();
    	multiplyingCards.add(toMultiply);
    }
    
    public List<Card> getMultiplyingCards() {
    	return multiplyingCards;
    }
    
    public void clearMultiplyingCards() {
    	if (multiplyingCards != null)
    		multiplyingCards.clear();
    	multiplyingCards = null;
    }
    
    protected void placeToken(MoveContext context, Card card, PlayerSupplyToken token) {
    	Card[] possiblePiles = context.game.getCardsInGame(GetCardsInGameOptions.Placeholders, true, Type.Action);
    	if (possiblePiles.length == 0) return;
    	
    	if (card == null) {
    		Util.playerError(context.getPlayer(), getName() + " error: did not pick a pile, picking first.");
            card  = possiblePiles[0];
    	}
    	if (!Arrays.asList(possiblePiles).contains(card)) {
    		Util.playerError(context.getPlayer(), getName() + " error: Invalid pile chosen, picking first");
    		card = possiblePiles[0];
    	}
    	    	
    	context.game.movePlayerSupplyToken(card, context.getPlayer(), token);
	}
    
    protected void attackPlayed(MoveContext context, Game game, Player currentPlayer) {
        // If an Urchin has been played, offer the player the option to trash it for a Mercenary
        for (int i = currentPlayer.playedCards.size() - 1; i >= 0 ; --i) {
            Card c = currentPlayer.playedCards.get(i);
            if (!(c == this) && c.getKind() == Cards.Kind.Urchin && currentPlayer.controlPlayer.urchin_shouldTrashForMercenary(context, c)) {
                currentPlayer.trashSelfFromPlay(c, context);
                currentPlayer.gainNewCard(Cards.mercenary, this, context);
            }
        }
    }
    
    protected void silverPlayed(MoveContext context, Game game, Player player) {
    	context.silversPlayed += 1;
    	if (context.silversPlayed == 1) {
    		context.addCoins(context.merchantsPlayed);
    	}
    	int saunasInPlay = context.countCardsInPlay(Cards.sauna);
    	for (int i = 0; i < saunasInPlay; ++i) {
    		if (player.getHand().size() > 0) {
                Card cardToTrash = player.controlPlayer.sauna_cardToTrash(context);
                if (cardToTrash != null) {
                	if (!player.getHand().contains(cardToTrash)) {
                		Util.playerError(player, "Sauna error, invalid card to trash, ignoring.");
                	} else {
                		cardToTrash = player.hand.get(cardToTrash);
                		player.trashFromHand(cardToTrash, Cards.sauna, context);
                	}
                } else {
                	break;
                }
        	}
    	}
    }
        
    protected void durationAttack(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
        	if (targetPlayer != currentPlayer) {
        		if (!Util.isDefendedFromAttack(game, targetPlayer, this)) {
        			targetPlayer.attacked(this, context);
        			currentPlayer.addDurationEffectOnOtherPlayer(targetPlayer, this.kind);
        		}
        	}
        }
    }
    
    protected void witchFamiliar(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this, context);
                MoveContext targetContext = new MoveContext(game, player);
                targetContext.attackedPlayer = player;
                player.gainNewCard(Cards.curse, this, targetContext);
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
                case Crown:
                	cardToPlay = (CardImpl) currentPlayer.controlPlayer.crown_actionToPlay(context);
                    break;
                default:
                    break;
            }

            if (cardToPlay != null) {
                if(!actionCards.contains(cardToPlay) || (this.kind == Cards.Kind.Procession && cardToPlay.is(Type.Duration))) {
                    Util.playerError(currentPlayer, this.name.toString() + " card selection error, ignoring");
                } else {
                    int timesToPlay = equals(Cards.kingsCourt) ? 3 : 2;
                    for (int i = 0; i < timesToPlay; ++i) {
                        cardToPlay.play(game, context, i == 0, i > 0, i > 0);
                    }
                    if (cardToPlay.is(Type.Duration, currentPlayer)) {
                    	((CardImpl)this).multiplyCard(cardToPlay);
                    }
                }

                if (this.kind == Cards.Kind.Procession) {
                    currentPlayer.trashFromPlay(cardToPlay, this, context);
                    Card cardToGain = currentPlayer.controlPlayer.procession_cardToGain(context, 1 + cardToPlay.getCost(context), cardToPlay.getDebtCost(context), cardToPlay.costPotion());
                    if ((cardToGain != null) && (cardToPlay.getCost(context) + 1) == cardToGain.getCost(context) && 
                    		cardToPlay.getDebtCost(context) == cardToGain.getDebtCost(context) && 
                    		cardToPlay.costPotion() == cardToGain.costPotion()) {
                        currentPlayer.gainNewCard(cardToGain, this, context);
                    }
                }
            }
        }
        return cardToPlay;
    }

    protected void multiPlayTreasure(MoveContext context, Game game, Player currentPlayer) {
    	Card treasure = null;
    	
    	switch (this.kind) {
        case Counterfeit:
            treasure = currentPlayer.controlPlayer.counterfeit_cardToPlay(context);
            break;
        case Crown:
            treasure = currentPlayer.controlPlayer.crown_treasureToPlay(context);
            break;
        default:
        	break;
    	}
        
    	if (treasure != null && treasure.is(Type.Treasure, currentPlayer, context) && currentPlayer.getHand().contains(treasure)) {
    		CardImpl cardToPlay = (CardImpl) treasure;
            for (int i = 0; i < 2; ++i) {
                cardToPlay.play(context.game, context, true, false, i > 0);
            }
            
            if (this.kind == Cards.Kind.Counterfeit) {
                currentPlayer.trashFromPlay(treasure, this, context);
            }
    	}
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
            currentPlayer.discard(card, this, context);
        }
    }

    protected void spyAndScryingPool(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player == currentPlayer || (!Util.isDefendedFromAttack(game, player, this))) {
                if (player != currentPlayer) {
                    player.attacked(this, context);
                }

                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                Card card = game.draw(playerContext, this, 1);

                if (card != null) {
                    player.reveal(card, this, playerContext);

                    boolean discard = false;

                    if(equals(Cards.spy)) {
                        discard = currentPlayer.controlPlayer.spy_shouldDiscard(context, player, card);
                    } else if (equals(Cards.scryingPool)) {
                        discard = currentPlayer.controlPlayer.scryingPool_shouldDiscard(context, player, card);
                    }

                    if (discard) {
                        player.discard(card, this, playerContext);
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
                currentPlayer.reveal(draw, this, new MoveContext(context, game, currentPlayer));
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

    public PileCreator getPileCreator() {
        if (pileCreator == null) {
            return new DefaultPileCreator();
        } else {
            return this.pileCreator;
        }
    }

    public int compareTo(Card other) {
        return getName().compareTo(other.getName());
    }

}
