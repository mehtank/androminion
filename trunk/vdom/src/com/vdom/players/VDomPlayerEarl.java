 package com.vdom.players;
 
 import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CurseCard;
import com.vdom.api.GameEvent;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.BasePlayer;
import com.vdom.core.Cards;
import com.vdom.core.Game;
import com.vdom.core.MoveContext;
import com.vdom.core.Player;
import com.vdom.core.Util;
 
public class VDomPlayerEarl extends BasePlayer
 {
    public boolean isAi() {
        return true;
    }
    
   Random rand = new Random(System.currentTimeMillis());
    // int turnCount = 0;
   private int treasureMapsBought = 0;
 
   private int silverTurnCount = 0;
 
   private ArrayList<HistoryItem> historyItems = new ArrayList<HistoryItem>();
 
   static Card[] HAND = new Card[0];
   static int THRONE_ROOM_PLAYS = 0;
   static int THRONE_ROOM_DUDS = 0;
 
   static HashSet<Card> DEFENDABLE_ATTACK_CARDS = new HashSet<Card>();
 
   static { Card[] attackCards = { Cards.cutpurse, Cards.ghostShip, Cards.militia, Cards.pirateShip/*, Cards.saboteur */, Cards.seaHag, Cards.thief, Cards.torturer, 
       Cards.witch };
     for (Card card : attackCards)
       DEFENDABLE_ATTACK_CARDS.add(card);
   }
 
   public void newGame(MoveContext context)
   {
     super.newGame(context);
   }
 
   public void gameEvent(GameEvent event)
   {
       super.gameEvent(event);
        if (Game.debug) {
     if ((event.getPlayer() != this) || 
       (event.getType() == GameEvent.Type.GameStarting)) return;
     if (event.getType() == GameEvent.Type.TurnBegin) {
                // this.turnCount += 1;
       HAND = getHand().toArray();
       for (Card card : HAND)
         this.historyItems.add(new HistoryItem(this.turnCount, card, 0, HistoryItem.Action.IN_HAND));
     }
     else if ((event.getType() == GameEvent.Type.BuyingCard) || (event.getType() == GameEvent.Type.CardObtained)) {
       Card card = event.getCard();
       this.historyItems.add(new HistoryItem(this.turnCount, card, 0, HistoryItem.Action.BOUGHT));
 
       if (card instanceof VictoryCard) {
         VictoryCard victoryCard = (VictoryCard)card;
         for (Card thisCard : HAND)
           this.historyItems.add(new HistoryItem(this.turnCount, thisCard, victoryCard.getVictoryPoints(), HistoryItem.Action.VICTORY_HELPER));
       } else if (card instanceof CurseCard) {
           CurseCard curseCard = (CurseCard)card;
           for (Card thisCard : HAND)
                    this.historyItems.add(new HistoryItem(this.turnCount, thisCard, curseCard.getVictoryPoints(), HistoryItem.Action.VICTORY_HELPER));
       }
     }
     else if (event.getType() == GameEvent.Type.PlayingAction) {
       this.historyItems.add(new HistoryItem(this.turnCount, event.getCard(), 0, HistoryItem.Action.PLAYED));
     } else if (event.getType() == GameEvent.Type.GameOver) {
       Player player = event.getContext().getPlayer();
       if (!(player.getWin()))
         calculateStats(this.historyItems);
     }
        }
   }
 
   private HashMap<Integer, Integer> getStat(ArrayList<HistoryItem> historyItems, HistoryItem.Action action)
   {
     HashMap<Integer, Integer> stat = new HashMap<Integer, Integer>();
     for (HistoryItem historyItem : historyItems) {
       if (historyItem.getAction() != action) {
         continue;
       }
       Integer count = null;
       Card card = historyItem.getCard();
       Integer id = card.getId();
       if (stat.containsKey(id))
         count = stat.get(id);
       else {
         count = Integer.valueOf(0);
       }
       if (card instanceof VictoryCard)
         stat.put(id, Integer.valueOf(count.intValue() + ((VictoryCard)card).getVictoryPoints()));
       else if (card instanceof CurseCard)
           stat.put(id, Integer.valueOf(count.intValue() + ((CurseCard)card).getVictoryPoints()));
       else {
         stat.put(id, count = Integer.valueOf(count.intValue() + 1));
       }
     }
 
     return stat;
   }
 
   private HashMap<Integer, Integer> getVictoryStat(ArrayList<HistoryItem> historyItems) {
     HashMap<Integer, Integer> stat = new HashMap<Integer, Integer>();
     for (HistoryItem historyItem : historyItems) {
       Integer count = null;
       Card card = historyItem.getCard();
       Integer id = card.getId();
       if (stat.containsKey(id))
         count = stat.get(id);
       else {
         count = Integer.valueOf(0);
       }
       if (card instanceof VictoryCard)
         stat.put(id, Integer.valueOf(count.intValue() + ((VictoryCard)card).getVictoryPoints()));
       else if (card instanceof CurseCard)
                stat.put(id, Integer.valueOf(count.intValue() + ((CurseCard) card).getVictoryPoints()));
       else {
         stat.put(id, count = Integer.valueOf(count.intValue() + 1));
       }
     }
 
     return stat;
   }
 
   private void calculateStats(ArrayList<HistoryItem> historyItems) {
     HashMap<String, Integer> allMyCards = new HashMap<String, Integer>();
     int total = 0;
     for (Card card : getAllCards()) {
       String name = card.getName();
       int count;
       if (allMyCards.containsKey(name))
         count = allMyCards.get(name).intValue();
       else {
         count = 0;
       }
 
       allMyCards.put(name, Integer.valueOf(++count));
       ++total;
     }
     debug(allMyCards.toString());
     debug("total cards: " + total);
     debug("total turns: " + this.turnCount);
     debug(getStat(historyItems, HistoryItem.Action.BOUGHT).toString());
     HashMap<Integer, Integer> played = getStat(historyItems, HistoryItem.Action.PLAYED);
     HashMap<Integer, Integer> inHand = getStat(historyItems, HistoryItem.Action.IN_HAND);
     HashMap<Integer, Integer> victoryHelpers = getVictoryStat(historyItems);
 
     for (HistoryItem historyItem : historyItems) {
       if ((historyItem.getAction() == HistoryItem.Action.BOUGHT) && (historyItem.getCard() instanceof ActionCard)) {
         debug(historyItem.toString() + ", was in hand " + inHand.get(historyItem.getCard().getId()) + " times, played " + 
           played.get(historyItem.getCard().getId()) + " times, and saw " + victoryHelpers.get(historyItem.getCard().getId()) + " vps");
       }
     }
     debug(getStat(historyItems, HistoryItem.Action.PLAYED).toString());
   }
 
   @Override
   public String getPlayerName() {
   	return getPlayerName(game.maskPlayerNames);
   }
   
   @Override
   public String getPlayerName(boolean maskName) {
   	return maskName ? "Player " + (playerNumber + 1) : "Earl";
   }
 
   public Card doAction(MoveContext context) {
     Card card = fromHand(calculateAction(this.turnCount, getHand().toArray(), context.countThroneRoomsInEffect(), context));
        debug("myAction: " + Arrays.toString(getHand().toArray()) + " -> " + card);
 
     return card;
   }
 
   private Card calculateAction(int myTurnCount, Card[] hand, int throneRoomsInEffect, MoveContext context) {
     if (inHandCount(Cards.treasureMap) >= 2) {
       return Cards.treasureMap;
     }
 
     if ((inHand(Cards.nobles)) && 
       (context.getActionsLeft() > 1)) {
       return Cards.nobles;
     }
 
     boolean hasThroneRoom = inHand(Cards.throneRoom);
     ArrayList<Card> dontPlay = new ArrayList<Card>();
 
     int actionCards = 0;
     for (Card card : hand) {
       if (card instanceof ActionCard && !card.equals(Cards.rats)) {
         ++actionCards;
       }
     }
 
     if ((actionCards == 0) || (actionCards == inHandCount(Cards.throneRoom))) {
       return null;
     }
 
     if (throneRoomsInEffect > 0) {
       if (inHand( Cards.throneRoom)) {
         return Cards.throneRoom;
       }
 
       if (inHand( Cards.feast)) {
         return Cards.feast;
       }
 
       if ((inHand( Cards.mine)) && (mineableCards(hand) > 1)) {
         return Cards.mine;
       }
 
       if ((inHand( Cards.moneyLender)) && (inHandCount(Cards.copper) > 1)) {
         return Cards.moneyLender;
       }
 
       if (inHand( Cards.bureaucrat)) {
         return Cards.bureaucrat;
       }
 
       if (inHand( Cards.workshop)) {
         return Cards.workshop;
       }
 
       if (inHand( Cards.militia)) {
         return Cards.militia;
       }
     }
 
     if (hasThroneRoom) {
       if (inHand( Cards.feast)) {
         THRONE_ROOM_PLAYS += 1;
         return Cards.throneRoom;
       }
 
       if ((inHand( Cards.mine)) && (mineableCards(hand) > 1)) {
         THRONE_ROOM_PLAYS += 1;
         return Cards.throneRoom;
       }
 
       if ((inHand( Cards.moneyLender)) && (inHandCount(Cards.copper) > 1)) {
         THRONE_ROOM_PLAYS += 1;
         return Cards.throneRoom;
       }
 
       if (inHand( Cards.bureaucrat)) {
         THRONE_ROOM_PLAYS += 1;
         return Cards.throneRoom;
       }
 
       Card bestCard = getBestAddingAction(hasThroneRoom);
       if (bestCard != null) {
         return Cards.throneRoom;
       }
 
       if (inHand( Cards.workshop)) {
         return Cards.throneRoom;
       }
 
       dontPlay.add(Cards.throneRoom);
       THRONE_ROOM_DUDS += 1;
     }
 
     Card bestCard = getBestAddingAction(hasThroneRoom);
     if (bestCard != null) {
       return bestCard;
     }
 
     if ((inHand( Cards.mine)) && (mineableCards(hand) > 0)) {
       return Cards.mine;
     }
     dontPlay.add(Cards.mine);
 
     if ((inHand( Cards.moneyLender)) && (inHandCount(Cards.copper) >= 1)) {
       return Cards.moneyLender;
     }
     dontPlay.add(Cards.moneyLender);
 
     Card[] attackCards = { Cards.seaHag, Cards.cutpurse, Cards.militia };
     for (Card card : attackCards) {
       if (inHand( card)) {
         return card;
       }
     }
 
     Card[] cards = { Cards.militia, Cards.bureaucrat, Cards.library };
     for (Card card : cards) {
       if (inHand( card)) {
         return card;
       }
     }
 
     for (Card card : getHand().toArray()) {
       if (card.equals(Cards.treasureMap)) {
         continue;
       }
       if (card instanceof ActionCard) {
         if (dontPlay.contains(card)) {
           continue;
         }
 
         ActionCard action = (ActionCard)card;
         if (context.canPlay(action)) {
           return action;
         }
       }
     }
     return null;
   }
 
   private ActionCard getBestAddActionAction(Card[] hand) {
     ActionCard bestAction = null;
     int bestAddActions = 0;
 
     for (Card card : hand) {
       if (card instanceof ActionCard) {
         ActionCard action = (ActionCard)card;
         if (action.getAddActions() > 0) {
           int addCards = action.getAddCards();
 
           if (addCards > bestAddActions) {
             bestAction = action;
             bestAddActions = addCards;
           } else if ((addCards == 0) && (bestAddActions == 0)) {
             bestAction = action;
           }
         }
       }
     }
 
     return bestAction;
   }
 
   private ActionCard getBestAddActionCard(Card[] hand) {
     ActionCard bestAction = null;
     int bestAddCards = 0;
 
     for (Card card : hand) {
       if (card instanceof ActionCard) {
         ActionCard action = (ActionCard)card;
         int thisAddCards = action.getAddCards();
         if (thisAddCards > bestAddCards) {
           bestAction = action;
           bestAddCards = thisAddCards;
         }
       }
     }
 
     return bestAction;
   }
 
   private Card getBestAddingAction(boolean hasThroneRoom) {
     Card[] hand = getHand().toArray();
 
     Card bestAction = getBestAddActionAction(hand);
 
     if (bestAction == null) {
       bestAction = getBestAddActionCard(hand);
     }
 
     if (bestAction != null) {
       return ((hasThroneRoom) ? Cards.throneRoom : bestAction);
     }
 
     return bestAction;
   }
 
   private Card handleEightGold(MoveContext context, Card cardToBuy) {
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
        if (context.canBuy(Cards.colony)) {
            return Cards.colony;
        }

        if (context.canBuy(Cards.platinum) && turnCount < 15 && game.pileSize(Cards.province) > 4) {
            return Cards.platinum;
        }

        if (context.canBuy(Cards.province) && (!game.buyWouldEndGame(Cards.province) || calculateLead(Cards.province) >= 0)) {
       return Cards.province;
     }
 
     return null;
   }
 
   private Card handleSevenGold(MoveContext context, Card cardToBuy) {
     return handleSixGold(context, cardToBuy);
   }
 
   private Card handleSixGold(MoveContext context, Card cardToBuy) {
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
        if ((this.turnCount > 15 || game.pileSize(Cards.province) < 4) && (context.canBuy(Cards.duchy))) {
       return Cards.duchy;
     }
 
     if ((context.canBuy(Cards.adventurer)) && (getMyCardCount(Cards.gold) >= 3) && (getMyCardCount(Cards.adventurer) < 2)) {
       return Cards.adventurer;
     }
 
        if (context.canBuy(Cards.nobles) && getMyCardCount(Cards.gold) >= 3)
            return Cards.nobles;
 
     if (context.canBuy(Cards.gold)) {
       return Cards.gold;
     }

        return null;
   }
 
   private Card handleFiveGold(MoveContext context, Card cardToBuy) {
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
     if ((this.turnCount > 15) && (context.canBuy(Cards.duchy))) {
       return Cards.duchy;
     }
 
     Card[] cards = { Cards.market, Cards.mine/*, Cards.saboteur */};
     Card thisCard = pickBalancedBuyable(context, cards, Integer.valueOf(2));
     if (thisCard != null) {
       return thisCard;
     }
 
     if ((this.turnCount < 5) && (context.canBuy(Cards.mine)) && (getMyCardCount(Cards.mine) < 2)) {
       return Cards.mine;
     }
 
     Card[] cards2 = { Cards.festival, Cards.market };
     thisCard = pickBalancedBuyable(context, cards2, Integer.valueOf(3));
     if (thisCard != null) {
       return thisCard;
     }
 
     Card[] cards3 = { Cards.festival, Cards.market, Cards.laboratory, Cards.witch, Cards.library };
     thisCard = pickBalancedBuyable(context, cards3);
     if (thisCard != null) {
       return thisCard;
     }
 
     return null;
   }
 
   private ArrayList<Card> getBuyableCards(MoveContext context, Card[] cards) {
     ArrayList<Card> buyable = new ArrayList<Card>();
 
     for (Card card : cards) {
       if (context.canBuy(card)) {
         buyable.add(card);
       }
     }
 
     return buyable;
   }
 
   private Card pickBalancedBuyable(MoveContext context, Card[] cards, Integer max) {
     return pickBalancedActual(context, getBuyableCards(context, cards), max);
   }
 
   private Card pickBalancedBuyable(MoveContext context, Card[] cards) {
     return pickBalancedBuyable(context, cards, null);
   }
 
   private Card pickBalancedActual(MoveContext context, List<Card> available, Integer max) {
     Integer low = null;
 
     for (Card card : available)
     {
       int thisCount = getMyCardCount(card);
       if (low == null) {
         low = Integer.valueOf(thisCount);
       }
 
       if (thisCount < low.intValue()) {
         low = Integer.valueOf(thisCount);
       }
     }
 
     if ((max != null) && (low == max)) {
       return null;
     }
 
     for (Card thisCard : available) {
       if (getMyCardCount(thisCard) <= low.intValue()) {
         return thisCard;
       }
     }
     return null;
   }
 
   private Card handleFourGold(MoveContext context, Card cardToBuy) {
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
     if ((this.turnCount < 5) && (!(isChapelGame(context))) && (context.canBuy(Cards.moneyLender)) && (getMyCardCount(Cards.moneyLender) < 1)) {
       return Cards.moneyLender;
     }
 
     if (shouldBuyGardens(context)) {
       return Cards.gardens;
     }
 
     if ((isChapelGame(context)) && (context.canBuy(Cards.bureaucrat)) && (getMyCardCount(Cards.bureaucrat) < 3)) {
       return Cards.bureaucrat;
     }
 
     if (this.turnCount > this.silverTurnCount) {
       return Cards.silver;
     }
 
     Card[] cards = { Cards.militia, Cards.bureaucrat, Cards.throneRoom, Cards.seaHag, Cards.cutpurse, Cards.miningVillage };
     Card thisCard = pickBalancedBuyable(context, cards, Integer.valueOf(2));
     if (thisCard != null) {
       return thisCard;
     }
 
     return null;
   }
 
   private Card handleThreeGold(MoveContext context, Card cardToBuy) {
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
     if (shouldBuyChapel(context)) {
       return Cards.chapel;
     }
 
     if (this.turnCount > this.silverTurnCount) {
       return Cards.silver;
     }
     ArrayList<Card> cards = new ArrayList<Card>();
     cards.add(Cards.village);
     cards.add(Cards.fishingVillage);
     if ((shouldBuyGardens(context)) && (getMyCardCount(Cards.workshop) < 4)) {
       cards.add(Cards.workshop);
     }
     if ((context.canBuy(Cards.swindler)) && (getMyCardCount(Cards.swindler) < 2)) {
       cards.add(Cards.swindler);
     }
     Card thisCard = pickBalancedBuyable(context, cards.toArray(new Card[0]), Integer.valueOf(5));
 
     if (thisCard != null) {
       return thisCard;
     }
 
     return Cards.silver;
   }
 
   private int attackingCardsInPlay(MoveContext context) {
     int attackingCardsInPlay = 0;
     for (Card card : context.getCardsInGame()) {
       if (DEFENDABLE_ATTACK_CARDS.contains(card)) {
         ++attackingCardsInPlay;
       }
     }
 
     return attackingCardsInPlay;
   }
 
   private Card handleTwoGold(MoveContext context, Card cardToBuy) {
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
     if ((attackingCardsInPlay(context) > 0) && (cardInPlay(context, Cards.moat))) {
       return Cards.moat;
     }
 
     if ((context.canBuy(Cards.courtyard)) && (inHandCount(Cards.courtyard) < 2)) {
       return Cards.courtyard;
     }
 
     if (this.turnCount > 20)
       return Cards.estate;
     if ((context.canBuy(Cards.cellar)) && (getMyCardCount(Cards.cellar) < 2))
       return Cards.cellar;
     if (!(isChapelGame(context))) {
       return Cards.copper;
     }
 
     return null;
   }
 
   private Card handleOneGold(MoveContext context, Card cardToBuy) {
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
     return null;
   }
 
   private Card handleZeroGold(MoveContext context, Card cardToBuy) {
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
     if (shouldBuyGardens(context)) {
       return Cards.copper;
     }
 
     return null;
   }
 
   private boolean willWin(Card card)
   {
     boolean willWin = false;
 
     return willWin;
   }
 
   private boolean wouldLose(Card card)
   {
     boolean wouldLose = false;
 
     return wouldLose;
   }
 
   public Card doBuy(MoveContext context) {
     Card card = calculateBuy(context, context.getCoinAvailableForBuy());
 
     if (willWin(card))
     {
       return card; }
     if (wouldLose(card)) {
       if (card.equals(Cards.province))
         card = Cards.estate;
       else if (context.getBuysLeft() == 0) {
         card = null;
       }
     }
 
     return card;
   }
 
   private boolean cardInPlay(MoveContext context, Card card) {
     boolean cardInPlay = false;
     for (Card thisCard : context.getCardsInGame()) {
       if (thisCard.equals(card)) {
         cardInPlay = true;
         break;
       }
     }
     return cardInPlay;
   }
 
   private boolean isChapelGame(MoveContext context) {
     return ((!(shouldBuyGardens(context))) && (cardInPlay(context, Cards.chapel)) && (cardInPlay(context, Cards.bureaucrat)));
   }
 
   private boolean shouldBuyChapel(MoveContext context) {
     int goldAvailable = context.getCoinAvailableForBuy();
     return ((isChapelGame(context)) && (this.turnCount < 3) && (((goldAvailable == 2) || (goldAvailable == 3))) && (getMyCardCount(Cards.chapel) < 1));
   }
 
   private boolean shouldBuyGardens(MoveContext context) {
     return cardInPlay(context, Cards.gardens);
   }
 
   private Card calculateBuy(MoveContext context, int goldAvailable) {
     Card[] cards = context.getCardsInGame();
 
     if (shouldBuyChapel(context)) {
       return Cards.chapel;
     }
 
     if ((((goldAvailable == 4) || (goldAvailable == 5))) && (cardInPlay(context, Cards.treasureMap)) && (getMyCardCount(Cards.treasureMap) < 2) && 
       (this.treasureMapsBought < 2)) {
       this.treasureMapsBought += 1;
       return Cards.treasureMap;
     }
     Card cardToBuy = null;
 
     cardToBuy = handleEightGold(context, cardToBuy);
 
     switch (goldAvailable)
     {
     case 7:
       cardToBuy = handleSevenGold(context, cardToBuy);
     case 6:
       cardToBuy = handleSixGold(context, cardToBuy);
     case 5:
       cardToBuy = handleFiveGold(context, cardToBuy);
     case 4:
       cardToBuy = handleFourGold(context, cardToBuy);
     case 3:
       cardToBuy = handleThreeGold(context, cardToBuy);
     case 2:
       cardToBuy = handleTwoGold(context, cardToBuy);
     case 1:
       cardToBuy = handleOneGold(context, cardToBuy);
     case 0:
       cardToBuy = handleZeroGold(context, cardToBuy);
     }
 
     if (cardToBuy != null) {
       return cardToBuy;
     }
 
     if (goldAvailable <= 2) {
       if (this.turnCount < 15) {
         return Cards.copper;
       }
       return null;
     }
 
     int tries = 40;
 
     while (tries-- > 0) {
       Card card = cards[this.rand.nextInt(cards.length)];
       if (context.canBuy(card)) {
         return card;
       }
     }
 
     return null;
   }
 
   public Card workshop_cardToGet(MoveContext context)
   {
     if (shouldBuyGardens(context)) {
       return Cards.gardens;
     }
     return Cards.silver;
   }
 
   private Card pickBalancedAvailable(MoveContext context, Card[] cards) {
     return pickBalancedAvailable(context, Arrays.asList(cards), null);
   }
 
   private Card pickBalancedAvailable(MoveContext context, List<Card> cards, Integer max) {
     ArrayList<Card> available = new ArrayList<Card>();
 
     HashMap<String, Integer> cardCounts = context.getCardCounts();
     for (Card card : cards)
     {
       if (cardCounts.containsKey(card.getName())) {
         int count = ((Integer)cardCounts.get(card.getName())).intValue();
         if (count > 0) {
           available.add(card);
         }
       }
     }
 
     return pickBalancedActual(context, available, max);
   }
 
   public Card feast_cardToGet(MoveContext context) {
     Card[] cards = { Cards.market, Cards.laboratory, Cards.mine };
     Card card = pickBalancedAvailable(context, cards);
 
     return card;
   }
 
   public Card remodel_cardToTrash(MoveContext context) {
     for (Card card : getHand()) {
       if (card.getCost(context) == 6) {
         return card;
       }
     }
     for (Card card : getHand()) {
       if (card.equals(Cards.curse)) {
         return card;
       }
     }
     for (Card card : getHand()) {
       if (card.equals(Cards.swindler)) {
         return card;
       }
     }
     for (Card card : getHand()) {
       if (card.equals(Cards.copper) || card.equals(Cards.rats)) {
         return card;
       }
     }
     return null;
   }
 
   public Card remodel_cardToGet(MoveContext context, int maxCost) {
     return calculateBuy(context, maxCost);
   }
 
   public Card[] militia_attack_cardsToKeep(MoveContext context) {
     ArrayList<Card> cards = new ArrayList<Card>();
     for (Card card : getHand()) {
       if (!(card instanceof VictoryCard) && !(card instanceof CurseCard)) {
         cards.add(card);
       }
     }
 
     while (cards.size() > 3) {
       cards.remove(0);
     }
 
     if (cards.size() < 3) {
       cards.clear();
       Card[] hand = getHand().toArray();
       for (int j = 0; j < 3; ++j) {
         cards.add(hand[j]);
       }
     }
 
     return cards.toArray(new Card[0]);
   }
 
   public boolean moneylender_shouldTrashCopper(MoveContext context) {
     return (getCurrencyTotal(context) >= 4);
   }
 
   public boolean chancellor_shouldDiscardDeck(MoveContext context) {
     return false;
   }
 
   public TreasureCard mine_treasureFromHandToUpgrade(MoveContext context) {
     Card[] hand = getHand().toArray();
     int silvers = 0;
 
     for (Card card : hand) {
       if (card.equals(Cards.copper))
         return ((TreasureCard)card);
       if (card.equals(Cards.silver)) {
         ++silvers;
       }
     }
 
     if (silvers > 0) {
       return ((TreasureCard)Cards.silver);
     }
 
     return null;
   }
 
   public Card[] chapel_cardsToTrash(MoveContext context) {
     ArrayList<Card> cards = new ArrayList<Card>();
 
     for (Card card : getHand()) {
         if (card.equals(Cards.estate) || card.equals(Cards.curse)) {
             cards.add(card);
         }
     }
  
     if (getCurrencyTotal(context) >= 3) {
       for (Card card : getHand()) {
         if (card.equals(Cards.copper)) {
           cards.add(card);
         }
       }
     }

     while (cards.size() > 4) {
       cards.remove(cards.size() - 1);
     }
 
     return cards.toArray(new Card[0]);
   }
 
   public Card[] cellar_cardsToDiscard(MoveContext context) {
     ArrayList<Card> cards = new ArrayList<Card>();
 
     Card[] hand = getHand().toArray();
     for (Card card : hand) {
       if (card instanceof VictoryCard || card instanceof CurseCard) {
         cards.add(card);
       }
     }
 
     if ((cards.size() == 0) && (inHand( Cards.throneRoom))) {
       cards.add(Cards.throneRoom);
     }
 
     return cards.toArray(new Card[0]);
   }
 
   public boolean spy_shouldDiscard(MoveContext context, Card card)
   {
     return ((!(card instanceof VictoryCard)) && (!(card.equals(Cards.copper)) && !(card instanceof CurseCard)));
   }
 
   public Card courtyard_cardToPutBackOnDeck(MoveContext context)
   {
     if (inHandCount(Cards.treasureMap) == 1) {
       return Cards.treasureMap;
     }
 
     return super.courtyard_cardToPutBackOnDeck(context);
   }
 
   public Card embargo_supplyToEmbargo(MoveContext context)
   {
     return null;
   }
 
   public Card feast_cardToObtain(MoveContext context)
   {
     return null;
   }
 
   public Card[] ghostShip_attack_cardsToPutBackOnDeck(MoveContext context)
   {
     ArrayList<Card> cards = new ArrayList<Card>();
     ArrayList<Card> h = Util.copy(getHand());
 
     if (inHandCount(Cards.treasureMap) == 1) {
       for (Card card : getHand()) {
         if (card.equals(Cards.treasureMap)) {
             if (h.remove(card))
            	 cards.add(card);
         }
       }
     }
 
     for (Card card : getHand()) {
       if (card instanceof VictoryCard || card instanceof CurseCard) {
           if (h.remove(card))
          	 cards.add(card);
       }
       if (cards.size() == 2) {
         break;
       }
     }
 
     if (cards.size() < 2) {
       for (Card card : getHand()) {
         if (card.equals(Cards.copper)) {
             if (h.remove(card))
            	 cards.add(card);
         }
         if (cards.size() == 2) {
           break;
         }
       }
     }
 
     int lowCost = 100;
     Card lowCard = null;
 
     if (cards.size() < 2) {
       for (Card card : getHand()) {
         if (card.getCost(context) < lowCost) {
           lowCost = card.getCost(context);
           lowCard = card;
         }
       }
 
       if (lowCard != null) {
           if (h.remove(lowCard))
          	 cards.add(lowCard);
       }
     }
 
     return cards.toArray(new Card[0]);
   }
 
 
public Card masquerade_cardToPass(MoveContext context)
{
        if (getHand().size() == 0)
            return null;

        Card c = getHand().get(0);
        for (Card card : getHand()) {
            if (card.getCost(context) < c.getCost(context))
                c = card;
        }
        return c;
}
 
   private int nonNobleActionCardCount() {
     int count = 0;
     for (Card card : getHand()) {
       if (card.equals(Cards.nobles)) {
         continue;
       }
       if (card instanceof ActionCard) {
         ++count;
       }
     }
 
     return count;
   }
 
   public Player.NoblesOption nobles_chooseOptions(MoveContext context)
   {
     if (context.getActionsLeft() > 1)
       return Player.NoblesOption.AddCards;
     if (inHandCount(Cards.nobles) > 2)
       return Player.NoblesOption.AddCards;
     if (inHandCount(Cards.nobles) == 2)
       return Player.NoblesOption.AddActions;
     if (nonNobleActionCardCount() > 0) {
       return Player.NoblesOption.AddActions;
     }
     return Player.NoblesOption.AddCards;
   }
 
 
   public Card[] secretChamber_cardsToDiscard(MoveContext context)
   {
     ArrayList<Card> cards = new ArrayList<Card>();
     for (Card card : getHand()) {
       if (card instanceof VictoryCard || card instanceof CurseCard) {
         cards.add(card);
       }
     }
     return cards.toArray(new Card[0]);
   }
 
   @Override
   public Card swindler_cardToSwitch(MoveContext context, int cost, boolean potion)
   {
     if (cost == 0 && !potion) {
       return Cards.curse;
     }
 
     if (cost == 2 && !potion) {
       return Cards.estate;
     }
 
     if (cost == 3 && !potion) {
       return Cards.silver;
     }
     if (cost == 5 && !potion) {
       return Cards.duchy;
     }
 
     Card[] cards = context.getCardsInGame();
     ArrayList<Card> randList = new ArrayList<Card>();
     for (Card card : cards) {
       if (Cards.isSupplyCard(card) && (card.getCost(context) == cost) && (context.getCardsLeftInPile(card) > 0) && card.costPotion() == potion) {
         randList.add(card);
       }
     }
 
     if (randList.size() > 0) {
       return randList.get(this.rand.nextInt(randList.size()));
     }
 
     return null;
   }

 
 
   static class HistoryItem {
       private int turn;
       private Card card;
       private int victoryPoints;
       private Action action;

       public HistoryItem(int turn, Card card, int victoryPoints, Action action) {
           this.turn = turn;
           this.card = card;
           this.victoryPoints = victoryPoints;
           this.action = action;
       }

       public int getTurn() {
           return this.turn;
       }

       public Card getCard() {
           return this.card;
       }

       public int getVictoryPoints() {
           return this.victoryPoints;
       }

       public Action getAction() {
           return this.action;
       }

       public String toString() {
           return this.turn + " - " + this.card.toString() + " - " + this.action.toString();
       }

       enum Action {
           BOUGHT, PLAYED, IN_HAND, VICTORY_HELPER;
       }
   }

}
