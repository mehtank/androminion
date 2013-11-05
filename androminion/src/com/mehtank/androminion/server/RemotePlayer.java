package com.mehtank.androminion.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mehtank.androminion.R;
import com.mehtank.androminion.ui.Strings;
import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CurseCard;
import com.vdom.api.DurationCard;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEvent.Type;
import com.vdom.api.GameEventListener;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.comms.Comms;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EType;
import com.vdom.comms.Event.EventObject;
import com.vdom.comms.EventHandler;
import com.vdom.comms.GameStatus;
import com.vdom.comms.MyCard;
import com.vdom.comms.NewGame;
import com.vdom.comms.SelectCardOptions;
import com.vdom.core.CardList;
import com.vdom.core.Cards;
import com.vdom.core.ExitException;
import com.vdom.core.Game;
import com.vdom.core.IndirectPlayer;
import com.vdom.core.MoveContext;
import com.vdom.core.Player;
import com.vdom.core.Util;

/**
 * Class that you can use to play remotely.
 * This seems to be the human player
 */
public class RemotePlayer extends IndirectPlayer implements GameEventListener, EventHandler {
    @SuppressWarnings("unused")
    private static final String TAG = "RemotePlayer";

    static int nextPort = 2255;
    static final int NUM_RETRIES = 3; // times to try anything before giving up.
    static int maxPause = 300000; // Maximum time to wait for new player to connect = 5 minutes in ms;
    private static VDomServer vdomServer = null; // points to the VDomServer object

    // private static final String DISTINCT_CARDS = "Distinct Cards";

    Comms comm = null;
    // communication thread handled internally now
    // Thread commThread;
    private int myPort = 0;

    protected String name;
    private HashMap<String, Integer> cardNamesInPlay = new HashMap<String, Integer>();
    private ArrayList<Card> cardsInPlay = new ArrayList<Card>();
    private ArrayList<Player> allPlayers = new ArrayList<Player>();
    private MyCard[] myCardsInPlay;

    private ArrayList<Card> playedCards = new ArrayList<Card>();
    private ArrayList<Boolean> playedCardsNew = new ArrayList<Boolean>();

    private boolean hasJoined = false;
    private Object hasJoinedMonitor;

    long whenStarted = 0;

    private Thread gameThread = null; // vdom-engine-thread
    private int dieTries = 0; // How often we tried to kill the vdom-thread

    public void waitForJoin() {
        synchronized(hasJoinedMonitor) {
            long startTime = System.currentTimeMillis();
            while (!hasJoined ) {
                debug("Waiting for " + maxPause + " ms...");
                try {
                    hasJoinedMonitor.wait(maxPause);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                debug("Done waiting. hasJoined: " + (hasJoined?"True":"False"));
                if ((System.currentTimeMillis() - startTime) > maxPause) {
                    debug("Timed out waiting for player to join.");
                    break;
                }
            }
        }
    }
    public void playerJoined(){
        synchronized(hasJoinedMonitor) {
            hasJoined = true;
            hasJoinedMonitor.notify();
        }
    }

    public static void setVdomserver(VDomServer vdomserver) {
        RemotePlayer.vdomServer = vdomserver;
        maxPause = VDomServer.maxPause;
    }
    public static VDomServer getVdomserver() {
        return vdomServer;
    }
    public int getPort() {
        return myPort;
    }
    public boolean hasJoined() {
        return hasJoined;
    }

    public static MyCard makeMyCard(Card c, int index, boolean isBane){
        MyCard card = new MyCard(index, Strings.getCardName(c), c.getSafeName(), c.getName());
        card.desc = Strings.getFullCardDescription(c);
        card.expansion = Strings.getCardExpansion(c);
        card.originalExpansion = c.getExpansion();
        card.cost = c.getCost(null);
        card.costPotion = c.costPotion();
        card.isBane = isBane;
        card.isShelter = c.isShelter();
        card.isLooter = c.isLooter();
        card.isOverpay = c.isOverpay();
        if (c.equals(Cards.virtualRuins))
            card.isRuins = true;
        else
            card.isRuins = c.isRuins();


        card.pile = MyCard.SUPPLYPILE;

        if ((c.equals(Cards.bagOfGold)) ||
            (c.equals(Cards.diadem)) ||
            (c.equals(Cards.followers)) ||
            (c.equals(Cards.princess)) ||
            (c.equals(Cards.trustySteed))) {

            card.pile = MyCard.PRIZEPILE;
            card.isPrize = true;
        }

        if (c.equals(Cards.spoils) ||
            c.equals(Cards.mercenary) ||
            c.equals(Cards.madman))
        {
            card.pile = MyCard.NON_SUPPLY_PILE;
        }

        if (c.equals(Cards.necropolis) ||
            c.equals(Cards.overgrownEstate) ||
            c.equals(Cards.hovel))
        {
            card.pile = MyCard.SHELTER_PILES;
        }

        if ((c.equals(Cards.copper)) ||
            (c.equals(Cards.silver)) ||
            (c.equals(Cards.potion)) ||
            (c.equals(Cards.gold)) ||
            (c.equals(Cards.platinum))) card.pile = MyCard.MONEYPILE;

        if ((c.equals(Cards.estate)) ||
            (c.equals(Cards.duchy)) ||
            (c.equals(Cards.province)) ||
            (c.equals(Cards.colony)) ||
            (c.equals(Cards.curse))) card.pile = MyCard.VPPILE;

        if (Cards.ruinsCards.contains(c))
            card.pile = MyCard.RUINS_PILES;

        if (Cards.knightsCards.contains(c))
            card.pile = MyCard.KNIGHTS_PILES;

        if (c.equals(Cards.potion)) card.isPotion = true;
        if (c.equals(Cards.curse)) {
            card.isCurse = true;
            card.vp = ((CurseCard) c).getVictoryPoints();
        }
        if (c instanceof VictoryCard) {
            card.isVictory = true;
            card.vp = ((VictoryCard) c).getVictoryPoints();
        }
        if (c instanceof TreasureCard) {
            card.isTreasure = true;
            card.gold = ((TreasureCard) c).getValue();
        }
        if (c instanceof ActionCard) {
            ActionCard ac = (ActionCard) c;
            card.isAction = true;
            if (c instanceof DurationCard) {
                card.isDuration = true;
            } else
                if (ac.isAttack() || c.equals(Cards.virtualKnight))
                    card.isAttack = true;
        }
        if (Cards.isReaction(c))
            card.isReaction = true;

        return card;
    }

    public Card intToCard(int i) {
        return cardsInPlay.get(i);
    }
    public Card[] intArrToCardArr(int[] cards) {
        Card[] cs = new Card[cards.length];
        for (int i = 0; i < cards.length; i++) {
            cs[i] = intToCard(cards[i]);
        }
        return cs;
    }
    public Card nameToCard(String o) {
        return intToCard(cardNamesInPlay.get(o));
    }
    @Override
    public int cardToInt(Card card) {
        // TODO:  NullPointerException for tournament prizes
        if (cardNamesInPlay.containsKey(card.getName()))
            return cardNamesInPlay.get(card.getName());
        else
            return -1;
    }

    public int[] cardArrToIntArr(Card[] cards) {
        int[] is = new int[cards.length];
        for (int i = 0; i < cards.length; i++) {
            is[i] = cardToInt(cards[i]);
        }
        return is;
    }

    public int[] arrayListToIntArr(ArrayList<Card> cards) {
        int[] is = new int[cards.size()];

        for (int i = 0; i < cards.size(); ++i) {
            is[i] = cardToInt((Card)cards.get(i));
        }

        return is;
    }

    public void setupCardsInPlay(MoveContext context) {
        ArrayList<MyCard> myCardsInPlayList = new ArrayList<MyCard>();

        int index = 0;

        // ensure Copper is card #0
        Card cop = Cards.copper;
        MyCard mc = makeMyCard(cop, index, false);
        myCardsInPlayList.add(mc);
        cardNamesInPlay.put(cop.getName(), index);
        cardsInPlay.add(index, cop);
        index++;

        for (Card c : context.getCardsInGame()) {
            if (c.getSafeName().equals(Cards.copper.getSafeName()))
                continue;

            if (context.game.baneCard == null) {
                mc = makeMyCard(c, index, false);
            } else {
                mc = makeMyCard(c, index, c.getSafeName().equals(context.game.baneCard.getSafeName()));
            }
            myCardsInPlayList.add(mc);

            cardNamesInPlay.put(c.getName(), index);
            cardsInPlay.add(index, c);
            index++;
        }
        myCardsInPlay = myCardsInPlayList.toArray(new MyCard[0]);
    }

    private String getVPOutput(Player player) {

        final Map<Object, Integer> counts = player.getVictoryCardCounts();
        final Map<Card, Integer> totals = player.getVictoryPointTotals(counts);

        final StringBuilder sb
                = new StringBuilder()
                .append(player.getPlayerName())
                .append(": ")
                .append(this.getVPs(totals))
                .append(" ")
                .append(Strings.getString(R.string.game_over_vps))
                .append('\n');

        sb.append(this.getCardText(counts, totals, Cards.estate));
        sb.append(this.getCardText(counts, totals, Cards.duchy));
        sb.append(this.getCardText(counts, totals, Cards.province));
        if(counts.containsKey(Cards.colony)) {
            sb.append(this.getCardText(counts, totals, Cards.colony));
        }

        // display victory cards from sets

        for(Card card : totals.keySet()) {
            if(!Cards.nonKingdomCards.contains(card)) {
                sb.append(this.getCardText(counts, totals, card));
            }
        }

        sb.append(this.getCardText(counts, totals, Cards.curse));

        sb
                .append("\tVictory Tokens: ")
                .append(totals.get(Cards.victoryTokens))
                .append('\n');

        return sb.toString();
    }

    private String getCardText(final Map<Object, Integer> counts, final Map<Card, Integer> totals, final Card card) {
        final StringBuilder sb = new StringBuilder()
                .append('\t')
                .append(card.getName())
                .append(" x")
                .append(counts.get(card))
                .append(": ")
                .append(totals.get(card))
                .append(" ")
                .append(Strings.getString(R.string.game_over_vps))
                .append('\n');

        return sb.toString();
    }

    public Event fullStatusPacket(MoveContext context, Player player, boolean isFinal) {
        if (player == null)
            player = context.getPlayer();

        int[] supplySizes = new int[cardsInPlay.size()];
        int[] embargos = new int[cardsInPlay.size()];
        int[] costs = new int[cardsInPlay.size()];

        for (int i = 0; i < cardsInPlay.size(); i++) {
            if (!isFinal)
                supplySizes[i] = context.getCardsLeftInPile(intToCard(i));
            else
                supplySizes[i] = player.getMyCardCount(cardsInPlay.get(i));
            embargos[i] = context.getEmbargos(intToCard(i));
            costs[i] = intToCard(i).getCost(context);
        }

        // show opponent hand if possessed
        CardList shownHand = (player.isPossessed()) ? player.getHand() : getHand();

        // ArrayList<Card> playedCards = context.getPlayedCards();

        if (!allPlayers.contains(player))
            allPlayers.add(player);
        int numPlayers = allPlayers.size();

        int curPlayerIndex = allPlayers.indexOf(player);

        int numCards[] = new int[numPlayers];
        int turnCounts[] = new int[numPlayers];
        int deckSizes[] = new int[numPlayers];
        int discardSizes[] = new int[numPlayers];
        int handSizes[] = new int[numPlayers];
        int pirates[] = new int[numPlayers];
        int victoryTokens[] = new int[numPlayers];
        int guildsCoinTokens[] = new int[numPlayers];
        String realNames[] = new String[numPlayers];

        for (int i=0; i<numPlayers; i++) {
            Player p = allPlayers.get(i);
            if (!isFinal)
                handSizes[i] = p.getHand().size();
            else
                handSizes[i] = p.getVPs();
            turnCounts[i] = p.getTurnCount();
            deckSizes[i] = p.getDeckSize();
            discardSizes[i] = p.getDiscardSize();
            numCards[i] = p.getAllCards().size();

            pirates[i] = p.getPirateShipTreasure();
            victoryTokens[i] = p.getVictoryTokens();
            guildsCoinTokens[i] = p.getGuildsCoinTokenCount();
            realNames[i] = p.getPlayerName(false);
        }

        GameStatus gs = new GameStatus();

        int[] playedArray = new int[playedCards.size()];
        for (int i = 0; i < playedCards.size(); i++) {
            Card c = playedCards.get(i);
            boolean newcard = playedCardsNew.get(i).booleanValue();
            playedArray[i] = (cardToInt(c) * (newcard ? 1 : -1));
        }

        gs.setTurnStatus(new int[] {context.getActionsLeft(),
            context.getBuysLeft(),
                context.getCoinForStatus(),
                context.countThroneRoomsInEffect()
        })
        .setFinal(isFinal)
                .setPossessed(player.isPossessed())
                .setTurnCounts(turnCounts)
                .setSupplySizes(supplySizes)
                .setEmbargos(embargos)
                .setCosts(costs)
                .setHand(cardArrToIntArr(Game.sortCards ? shownHand.sort(new Util.CardHandComparator()) : shownHand.toArray()))
                .setPlayedCards(playedArray)
                .setCurPlayer(curPlayerIndex)
                .setCurName(player.getPlayerName(!isFinal && game.maskPlayerNames))
                .setRealNames(realNames)
                .setHandSizes(handSizes)
                .setDeckSizes(deckSizes)
                .setNumCards(numCards)
                .setPirates(pirates)
                .setVictoryTokens(victoryTokens)
                .setGuildsCoinTokens(guildsCoinTokens)
                .setCardCostModifier(context.cardCostModifier)
                .setPotions(context.getPotionsForStatus(player))
                .setIsland(cardArrToIntArr(player.getIsland().toArray()))
                .setVillage(cardArrToIntArr(player.getNativeVillage().toArray()))
                .setTrash(arrayListToIntArr(player.game.GetTrashPile()));

        Card topRuins = game.getTopRuinsCard();
        if (topRuins == null) {
            topRuins = Cards.virtualRuins;
        }
        gs.setRuinsTopCard(cardToInt(Cards.virtualRuins), topRuins);
        Card topKnight = game.getTopKnightCard();
        if (topKnight == null) {
            topKnight = Cards.virtualKnight;
        }
        gs.setKnightTopCard(cardToInt(Cards.virtualKnight), topKnight, topKnight.getCost(context));

        Event p = new Event(EType.STATUS)
                .setObject(new EventObject(gs));

        return p;
    }

    @Override
    public void newGame(MoveContext context) {
        hasJoinedMonitor = new Object(); // every game needs a different monitor, otherwise we wake up threads that are supposed to be dead.
        context.addGameListener(this);
        setupCardsInPlay(context);
        gameThread = Thread.currentThread();

        allPlayers.clear();
        myPort = connect();
        if (vdomServer != null)
            vdomServer.registerRemotePlayer(this);
        if (myPort == 0)
            quit("Could not create server.");
    }

    public Event sendWithAck(Event tosend, EType resp) throws IOException, NullPointerException {
        Event p;

        for (int i = 0; i < NUM_RETRIES; i++) {
            comm.put_ts(tosend);
            p = comm.get_ts();
            if (p == null)
                throw new IOException();
            else if (p.t == resp)
                return p;
        }

        throw new IOException();
    }

    @Override
    public void sendErrorHandler(Exception e) {
        e.printStackTrace();
        comm.injectNullReceived(); // This causes sendWithAck to receive a null and therefore throw an error, which we want.
    }

    private void achievement(MoveContext context, String achievement) {
        Event status = fullStatusPacket(curContext == null ? context : curContext, curPlayer, false).setString(achievement);
        try {
            sendWithAck(status.setType(EType.ACHIEVEMENT).setString(achievement), EType.Success);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Event query(MoveContext context, Event tosend, EType resp) {
        Event reply;
        for (int connections = 0; connections < NUM_RETRIES; connections++) {
            try {
                //sendWithAck(fullStatusPacket(context, null, false), EType.Success);
                comm.put_ts(fullStatusPacket(context, null, false));
                reply = sendWithAck(tosend, resp);
            } catch (IOException e) {
                reply = null;
            }
            if (reply != null)
                return reply;

            reconnect("Could not complete query.");
            waitForJoin();
            if (!hasJoined)
                quit(Strings.getString(R.string.response_timed_out));
        }
        quit("Could not complete query.");
        return null;
    }

    Player curPlayer = null;
    MoveContext curContext = null;
    boolean gameOver = false;
    @Override
    public void gameEvent(GameEvent event) {
        super.gameEvent(event);

        MoveContext context = event.getContext();

        boolean sendEvent = true;

        String strEvent = "";
        boolean playerNameIncluded = false;
        if (event.getPlayer() != null && event.getPlayer().getPlayerName() != null) {
            strEvent += event.getPlayer().getPlayerName() + ": ";
            playerNameIncluded = true;
        }

        if(event.getType() == GameEvent.Type.Status) {
            String coin = "" + context.getCoinAvailableForBuy();
            if(context.potions > 0)
                coin += "p";
            coin = "(" + coin + ")"; // <" + String.valueOf(event.player.discard.size()) + ">";
            strEvent += Strings.format(R.string.action_buys_coin, context.getActionsLeft(), context.getBuysLeft(), coin);
        }
        else {
            switch(event.getType()) {
                case GameStarting:

                    strEvent += Strings.getString(R.string.GameStarting);
                    break;
                case GameOver:
                    // Check for achievements
                    int provinces = 0;
                    for(Card c : getAllCards()) {
                        if(c.equals(Cards.province)) {
                            provinces++;
                        }
                    }
                    if(provinces == 8 && Game.players.length == 2) {
                        achievement(context, "2players8provinces");
                    }
                    if(provinces >= 10 && Game.players.length >= 3) {
                        achievement(context, "3or4players10provinces");
                    }
                    int vp = this.getVPs();
                    if(vp >= 100) {
                        achievement(context, "score100");
                    }

                    boolean beatBy50 = true;
                    boolean skunk = false;
                    boolean beatBy1 = false;
                    boolean mostVp = true;
                    for(Player opp : context.game.getPlayersInTurnOrder()) {
                        if(opp != this) {
                            int oppVP = opp.getVPs();
                            if(oppVP > vp) {
                                mostVp = false;
                            }

                            if(oppVP <= 0) {
                                skunk = true;
                            }
                            if(vp == oppVP + 1) {
                                beatBy1 = true;
                            }
                            if(vp < oppVP + 50) {
                                beatBy50 = false;
                            }
                        }
                    }
                    if(mostVp && beatBy50) {
                        achievement(context, "score50more");
                    }
                    if(mostVp && skunk) {
                        achievement(context, "skunk");
                    }
                    if(mostVp && beatBy1) {
                        achievement(context, "score1more");
                    }

                    if(mostVp && !achievementSingleCardFailed) {
                        achievement(context, "singlecard");
                    }

                    strEvent += Strings.getString(R.string.GameOver);
                    break;
                case Embargo:
                    strEvent += Strings.getString(R.string.Embargo);
                    break;
                case Status:
                    strEvent += Strings.getString(R.string.Status);
                    break;
                case CantBuy:
                    String cards = "";
                    boolean first = true;
                    for(Card card : context.getCantBuy()) {
                        if(first) {
                            first = false;
                        }
                        else {
                            cards += ", ";
                        }
                        cards += Strings.getCardName(card);
                    }
                    strEvent += Strings.format(R.string.CantBuy, cards);
                    break;
                case VictoryPoints:
                    strEvent += Strings.getString(R.string.VictoryPoints);
                    break;
                case NewHand:
                    strEvent += Strings.getString(R.string.NewHand);
                    break;
                case TurnBegin:
                    strEvent += Strings.getString(R.string.TurnBegin);
                    break;
                case TurnEnd:
                    if(context != null && context.getPlayer() == this && context.vpsGainedThisTurn > 30) {
                        achievement(context, "gainmorethan30inaturn");
                    }
                    strEvent += Strings.getString(R.string.TurnEnd);
                    break;
                case PlayingAction:
                    strEvent += Strings.getString(R.string.PlayingAction);
                    break;
                case PlayedAction:
                    strEvent += Strings.getString(R.string.PlayedAction);
                    break;
                case PlayingDurationAction:
                    strEvent += Strings.getString(R.string.PlayingDurationAction);
                    break;
                case PlayingCoin:
                    strEvent += Strings.getString(R.string.PlayingCoin);
                    break;
                case BuyingCard:
                    strEvent += Strings.getString(R.string.BuyingCard);
                    break;
                case OverpayForCard:
                    if (context != null && context.overpayAmount >= 10)
                    {
                        achievement(context, "overpayby10ormore");
                    }
                    break;
                case GuildsTokenObtained:
                    if (context != null && getGuildsCoinTokenCount() >= 50)
                    {
                        achievement(context, "stockpile50tokens");
                    }
                    break;
                case NoBuy:
                    strEvent += Strings.getString(R.string.NoBuy);
                    break;
                case DeckReplenished:
                    strEvent += Strings.getString(R.string.DeckReplenished);
                    break;
                case PlayerAttacking:
                    strEvent += Strings.getString(R.string.PlayerAttacking);
                    break;
                case PlayerDefended:
                    strEvent += Strings.getString(R.string.PlayerDefended);
                    break;
                case CardOnTopOfDeck:
                    strEvent += Strings.getString(R.string.CardOnTopOfDeck);
                    break;
                case CardObtained:
                    strEvent += Strings.getString(R.string.CardObtained);
                    break;
                case CardTrashed:
                    if(context != null && context.getPlayer() == this && context.cardsTrashedThisTurn > 5) {
                        achievement(context, "trash5inaturn");
                    }
                    strEvent += Strings.getString(R.string.CardTrashed);
                    break;
                case CardRevealed:
                    strEvent += Strings.getString(R.string.CardRevealed);
                    break;
                case CardRevealedFromHand:
                    strEvent += Strings.getString(R.string.CardRevealedFromHand);
                    break;
                case CardDiscarded:
                    strEvent += Strings.getString(R.string.CardDiscarded);
                    break;
                case CardAddedToHand:
                    strEvent += Strings.getString(R.string.CardAddedToHand);
                    break;
                case CardRemovedFromHand:
                    strEvent += Strings.getString(R.string.CardRemovedFromHand);
                    break;
                default:
                    strEvent += event.getType().toString();
                    break;
            }
        }

        if (event.getCard() != null && event.getType() != Type.CardAddedToHand && event.getType() != Type.PlayerAttacking)
            strEvent += " " + Strings.getCardName(event.getCard()) + " ";
        if (event.getType() == Type.TurnBegin && event.getPlayer().isPossessed())
            strEvent += " possessed by " + event.getPlayer().controlPlayer.getPlayerName() + "!";
        if (event.getAttackedPlayer() != null)
            strEvent += " (" + event.getAttackedPlayer().getPlayerName() + ") ";
        if (context != null && context.getMessage() != null) {
            strEvent += "\n" + context.getMessage();
        }

        debug("												GAME EVENT - " + strEvent);

        boolean newTurn = false;
        boolean isFinal = false;

        switch (event.getType()) {
            case VictoryPoints:
                sendEvent = false;
                break;
            case GameStarting:
                if (event.getPlayer() == this) {
                    waitForJoin();
                    if (!hasJoined)
                        quit(Strings.getString(R.string.join_timed_out));
                }
                whenStarted = System.currentTimeMillis();
                playedCards.clear();
                gameOver = false;

                // Only send the event if its the first game starting, which doesn't include the player
                // name, so that the "Chance for plat/colony" shows up only once and so that only one
                // GameStarting event gets shown in the status area.
                if(playerNameIncluded) {
                    sendEvent = false;
                }
                break;
            case TurnBegin:
                curPlayer = event.getPlayer();
                curContext = context;
                newTurn = true;
                playedCards.clear();
                playedCardsNew.clear();
                break;
            case TurnEnd:
                playedCards.clear();
                playedCardsNew.clear();
                break;
            case CantBuy:
                break;
            case PlayingAction:
            case PlayingDurationAction:
                playedCards.add(event.getCard());
                playedCardsNew.add(event.newCard);
                break;
            case PlayingCoin:
                playedCards.add(event.getCard());
                playedCardsNew.add(true);
                break;
            case CardObtained:
                if (event.responsible.equals(Cards.hornOfPlenty) && event.card instanceof VictoryCard) {
                    int index = playedCards.indexOf(event.responsible);
                    playedCards.remove(index);
                    playedCardsNew.remove(index);
                }
                break;
            case GameOver:
                curPlayer = event.getPlayer();
                curContext = context;
                isFinal = true;

                strEvent = getVPOutput(curPlayer);
                if (!gameOver) {
                    String time = Strings.getString(R.string.game_over_status);
                    time += " ";
                    long duration = System.currentTimeMillis() - whenStarted;
                    if (duration > 1000 * 60 * 60)
                        time += (duration / (1000 * 60 * 60)) + "h ";
                    duration = duration % (1000 * 60 * 60);
                    if (duration > 1000 * 60)
                        time += (duration / (1000 * 60)) + "m ";
                    duration = duration % (1000 * 60);
                    time += (duration / (1000)) + "s.\n";
                    if(!event.getContext().cardsSpecifiedOnStartup()) {
                        time += Strings.getGameTypeName(event.getContext().getGameType());
                    }

                    time += "\n\n";

                    strEvent = time + strEvent;
                    gameOver = true;
                    newTurn = true;
                }
                break;
            case BuyingCard:
                break;
            case CardAddedToHand:
                break;
            case CardDiscarded:
                break;
            case CardOnTopOfDeck:
                break;
            case CardRemovedFromHand:
                break;
            case CardRevealed:
                break;
            case CardTrashed:
                break;
            case DeckReplenished:
                break;
            case Embargo:
                break;
            case NewHand:
                break;
            case NoBuy:
                break;
            case PlayedAction:
                break;
            case PlayerAttacking:
                break;
            case PlayerDefended:
                break;
            case Status:
                break;
            default:
                break;
        }

        Event status = fullStatusPacket(curContext == null ? context : curContext, curPlayer, isFinal)
                .setString(strEvent)
                .setBoolean(newTurn);
        String playerInt = "" + allPlayers.indexOf(event.getPlayer());


        if (event.getPlayer() != null) {
            switch (event.getType()) {
                case BuyingCard:
                case CardObtained:
                    comm.put_ts(status.setType(EType.CARDOBTAINED).setString(playerInt).setInteger(cardToInt(event.getCard())));

                    break;
                case CardTrashed:
                    comm.put_ts(status.setType(EType.CARDTRASHED).setString(playerInt).setInteger(cardToInt(event.getCard())));

                    break;
                case CardRevealed:
                    comm.put_ts(status.setType(EType.CARDREVEALED).setString(playerInt).setInteger(cardToInt(event.getCard())));

                    break;
                case PlayerDefended:
                    comm.put_ts(status);
                    comm.put_ts(status.setType(EType.CARDREVEALED).setString(playerInt).setInteger(cardToInt(event.getCard())));

                    break;
                default:
                    if(sendEvent)
                        comm.put_ts(status);
            }
            comm.put_ts(new Event(EType.SLEEP).setInteger(100));
        }
    }


    @Override
    public String getPlayerName() {
        return name;
    }

    @Override
    public String getPlayerName(boolean maskName) {
        return getPlayerName();
    }

    @Override
    protected Card[] pickCards(MoveContext context, SelectCardOptions sco, int count, boolean exact) {
        if (sco.allowedCards.size() == 0)
            return null;

        Event p = new Event(EType.GETCARD)
                .setInteger(count)
                .setBoolean(exact)
                .setObject(new EventObject(sco));

        p = query(context, p, EType.CARD);
        if (p == null)
            return null;
        else if (p.i == 0)
            return null;
        else if (p.i == 1 && p.o.is[0] == -1)
            // Hack to notify that "All" was selected
            return new Card[0];
        else
            return intArrToCardArr(p.o.is);
    }

    // If I were designing this from scratch, I may have picked an API that treated this
    // selectBoolean method and the selectOption method the same.  But I'm not designing this from
    // scratch, I'm just trying to cut the strings out of an existing API while minimizing my
    // effort, so we get something that's a little bit disjointed.  Oh well...

    @Override
    public boolean selectBoolean(MoveContext context, Card cardResponsible, Object[] extras) {
        Event p = new Event(EType.GETBOOLEAN)
                .setCard(cardResponsible)
                .setObject(new EventObject(extras));
        p = query(context, p, EType.BOOLEAN);
        if (p == null)
            return false;
        else
            return p.b;
    }

    @Override
    public int selectOption(MoveContext context, Card card, Object[] options) {
        Event p = new Event(EType.GETOPTION)
                .setCard(card)
                .setObject(new EventObject(options));
        p = query(context, p, EType.OPTION);
        if (p == null)
            return -1;
        else
            return p.i;
    }

    @Override
    protected int[] orderCards(MoveContext context, int[] cards) {
        if(cards != null && cards.length == 1) {
            return new int[]{ 0 };
        }

        Event p = new Event(EType.ORDERCARDS)
                .setObject(new EventObject(cards));

        p = query(context, p, EType.CARDORDER);
        if (p == null)
            return null;
        else
            return p.o.is;
    }

    @Override
    public boolean handle(Event e) {
        if (e.t == EType.HELLO) {
            name = (e.s == "" ? "Remote player" : e.s);
            debug("Name set: " + name);
            String[] players = new String[allPlayers.size()];
            for (Player p : allPlayers)
                players[allPlayers.indexOf(p)] = p.getPlayerName();

            //	try {
            comm.put_ts(new Event(EType.NEWGAME).setObject(new EventObject(new NewGame(myCardsInPlay, players))));
            playerJoined();
            //	} catch (Exception e1) {
            // TODO:Because put_ts is asynchronous, this will not work the way it was intended. Is that bad?
            // Probably not; if the connection is lost right after receiving a HELLO, we will notice soon enough.
            // Maybe we should implement synchronous sending though.
            //		debug("Could not send NEWGAME -- ignoring, but not setting hasJoined");
            //	}
            return true;
        }
        if (e.t == EType.SAY) {
            vdomServer.say(name + ": " + e.s);
            return true;
        }
        //		if (e.t == EType.DISCONNECT) {
        //			debug("Comms issued disconnect");
        //			comm.doWait(); // clear notification
        //			reconnect("Comms issued disconnect.");
        //		}
        return false;
    }

    private int connect() {
        int port = 0;
        hasJoined = false;
        for (int connections = 0; connections < NUM_RETRIES; connections++) {
            try {
                comm = new Comms(this, nextPort++);
                port = comm.getPort();
                return port;
            } catch (IOException e) {
                //				comm = null; // can cause NullPointerExceptions in different threads
                e.printStackTrace();
                debug ("Could not open a server for remote player... attempt " + (connections + 1));
            }
        }
        return port;
    }
    private void disconnect() {
        if (comm != null)
            comm.stop();
        //		comm = null; // can cause NullPointerExceptions in different threads
        hasJoined = false;
        myPort = 0;
    }
    private void reconnect(String s) {
        if (vdomServer != null) {
            // TODO reconnect
            debug("Reconnecting... " + s);
            disconnect();
            myPort = connect();
            if (myPort == 0)
                quit(s + "; Could not recreate server");
        } else {
            quit(s);
        }
    }

    public void sendQuit(String s) {
        String time = "\n\n";
        time += Strings.getString(R.string.game_length_status);
        time += " ";
        long duration = System.currentTimeMillis() - whenStarted;
        if (duration > 1000 * 60 * 60)
            time += (duration / (1000 * 60 * 60)) + "h ";
        duration = duration % (1000 * 60 * 60);
        if (duration > 1000 * 60)
            time += (duration / (1000 * 60)) + "m ";
        duration = duration % (1000 * 60);
        time += (duration / (1000)) + "s.";

        // Try-Catch block made obsolete by sendErrorHandler
        //		try {
        comm.put_ts(new Event(EType.QUIT).setString(s + time));
        //		} catch (Exception e) {
        //			// Whatever.
        //		}
        disconnect();
    }


    private void quit(String s) {
        debug("!!! Quitting: " + s + " !!!");
        if (vdomServer != null)
            vdomServer.endGame(name + " : " + s);
        else
            die();
    }

    private void die() {
        if (gameThread == null) {
            debug("die() called, but game thread already dead.");
            kill_game();
            return;
        }
        if (Thread.currentThread() == gameThread) {
            gameThread = null;
            throw new ExitException();
        } else {
            debug("die() called from outside vdom-thread");
            if (dieTries > 4) {
                debug("Could not kill vdom-thread");
                return;
            }
            dieTries++;
            kill_game();
        }
    }

    public void kill_game() {
        // Make main-thread throw an ExitException.
        vdomServer = null;
        playerJoined(); // Hack: Need thread to wake up
        if (comm != null) {
            comm.stop();
        } else {
            try
            {
                Thread.sleep(500); // HACK: wait for comm to be created
            } catch (InterruptedException e) { }
            if (comm != null) {
                comm.stop();
            } else {
                debug("Could not kill vdom thread");
            }
        }
    }
}
