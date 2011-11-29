package com.vdom.core;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CardCostComparator;
import com.vdom.api.Cards;
import com.vdom.api.DurationCard;
import com.vdom.api.FrameworkEvent;
import com.vdom.api.FrameworkEventHelper;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEventListener;
import com.vdom.api.GameType;
import com.vdom.api.InteractivePlayer;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.Player.WatchTowerOption;

public class Game {
    public static Integer cardSequence = 1;
    public static HashMap<String, Double> GAME_TYPE_WINS = new HashMap<String, Double>();
    
    public static HashMap<String, Integer> winStats = new HashMap<String, Integer>();
    public static final String QUICK_PLAY = "(QuickPlay)";
    public static final String BANE = "bane+";
    
    public static String[] cardsSpecifiedAtLaunch;
    public static ArrayList<String> unfoundCards = new ArrayList<String>();
    public static HashSet<Card> nonKingdomCards = new HashSet<Card>();

    static {
        nonKingdomCards.add(Cards.copper);
        nonKingdomCards.add(Cards.silver);
        nonKingdomCards.add(Cards.gold);
        nonKingdomCards.add(Cards.platinum);
        nonKingdomCards.add(Cards.potion);
        nonKingdomCards.add(Cards.estate);
        nonKingdomCards.add(Cards.duchy);
        nonKingdomCards.add(Cards.province);
        nonKingdomCards.add(Cards.colony);
        nonKingdomCards.add(Cards.curse);
    }
    
    /**
     * Player classes and optionally the url to the jar on the web that the class is located in. Player class
     * is in index [0] of the array and the jar is in index [1]. Index [1] is null (but still there) if the
     * default class loader should be used.
     */
    static ArrayList<String[]> playerClassesAndJars = new ArrayList<String[]>();

    /**
     * The card set to use for the game.
     * 
     * @see com.vdom.api.GameType
     */
    public static GameType gameType = GameType.Random;

    // TODO: better way to do...
    public static int bridgesInEffect = 0;

    public static boolean alwaysIncludePlatColony = false; 
    public static boolean platColonyPassedIn = false; 
    public static boolean quickPlay = false;
    
    public static boolean showUI = false;

    public static boolean debug = false;
    public static final HashSet<GameEvent.Type> showEvents = new HashSet<GameEvent.Type>();
    public static final HashSet<String> showPlayers = new HashSet<String>();
    static boolean interactive = false;
    static boolean test = false;
    static boolean ignoreAllPlayerErrors = false;
    static boolean ignoreSomePlayerErrors = false;
    static HashSet<String> ignoreList = new HashSet<String>();

    static ArrayList<GameStats> gameTypeStats = new ArrayList<GameStats>();

    static int numGames = -1;
    public ArrayList<GameEventListener> listeners = new ArrayList<GameEventListener>();
    public GameEventListener gameListener;
    static boolean forceDownload = false;
    static HashMap<String, Double> overallWins = new HashMap<String, Double>();

    public static Random rand = new Random(System.currentTimeMillis());
    public HashMap<String, Pile> piles = new HashMap<String, Pile>();
    public HashMap<String, Integer> embargos = new HashMap<String, Integer>();
    public Card baneCard = null;

    public static ArrayList<Card> actionCardsBaseGame = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsIntrigue = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsSeaside = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsAlchemy = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsProsperity = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsCornucopia = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsHinterlands = new ArrayList<Card>();
    public static ArrayList<Card> actionCards = new ArrayList<Card>();

    private static final int kingdomCardPileSize = 10;
    private static int victoryCardPileSize = 12;

    ArrayList<Card>[] cardsObtainedLastTurn;
    static int playersTurn;

//    public UI ui;
    int turnCount;

    public static HashMap<String, Player> cachedPlayers = new HashMap<String, Player>();
    public static HashMap<String, Class<?>> cachedPlayerClasses = new HashMap<String, Class<?>>();
    public static Player[] players;

    public boolean platInPlay = false;
    public boolean colonyInPlay = false;

    public int possessionsToProcess = 0;
    public Player possessingPlayer = null;
    
    static int numPlayers;
    boolean gameOver = false;

    private static HashMap<String, Player> playerCache = new HashMap<String, Player>();

    static {

        ((CardImpl) Cards.moat).description = "When another player plays an Attack card, you may reveal this from your hand. If you do, you are unaffected by that Attack.";
        ((CardImpl) Cards.adventurer).description = "Reveal cards from your deck until you reveal 2 Treasure cards. Put those Treasure cards into your hand and discard the other revealed cards.";
        ((CardImpl) Cards.bureaucrat).description = "Gain a Silver card; put it on top of your deck. Each other player reveals a Victory card from his hand and puts it on his deck (or reveals a hand with no Victory cards).";
        ((CardImpl) Cards.cellar).description = "Discard any number of cards. +1 Card per card discarded.";
        ((CardImpl) Cards.chancellor).description = "You may immediately put your deck into your discard pile.";
        ((CardImpl) Cards.chapel).description = "Trash up to 4 cards from your hand.";
        ((CardImpl) Cards.councilRoom).description = "Each other player draws a card.";
        ((CardImpl) Cards.feast).description = "Trash this card. Gain a card costing up to 5 coin.";
        ((CardImpl) Cards.festival).description = "";
        ((CardImpl) Cards.laboratory).description = "";
        ((CardImpl) Cards.library).description = "Draw until you have 7 cards in hand. You may set aside any Action cards drawn this way, as you draw them; discard the set aside cards after you finish drawing.";
        ((CardImpl) Cards.market).description = "";
        ((CardImpl) Cards.militia).description = "Each other player discards down to 3 cards in his hand.";
        ((CardImpl) Cards.mine).description = "Trash a Treasure card from your hand. Gain a Treasure card costing up to 3 Coins more; put it into your hand.";
        ((CardImpl) Cards.moneyLender).description = "Trash a Copper card from your hand. If you do, +3 Coins.";
        ((CardImpl) Cards.remodel).description = "Trash a card from your hand. Gain a card costing up to 2 Coins more than the trashed card.";
        ((CardImpl) Cards.smithy).description = "";
        ((CardImpl) Cards.spy).description = "Each player (including you) reveals the top card of his deck and either discards it or puts it back, your choice.";
        ((CardImpl) Cards.thief).description = "Each other player reveals the top 2 cards of his deck. If they revelaed any Treasure cards, they trash one of them that you choose. You may gain any or all of these trashed cards. They discard the other revealed cards.";
        ((CardImpl) Cards.throneRoom).description = "Choose an Action card in you hand. Play it twice.";
        ((CardImpl) Cards.village).description = "";
        ((CardImpl) Cards.witch).description = "Each other player gains a Curse card.";
        ((CardImpl) Cards.woodcutter).description = "";
        ((CardImpl) Cards.workshop).description = "Gain a card costing up to 4 Coins.";
        ((CardImpl) Cards.gardens).description = "Worth 1 Victory Point for every 10 cards in your deck (rounded down).";

        ((CardImpl) Cards.torturer).description = "Each other player chooses one: he discards 2 cards; or he gains a Curse card, putting it in his hand.";
        ((CardImpl) Cards.secretChamber).description = "Discard any number of cards. +1 Coin per card discarded. When another player plays an Attack card, you may reveal this from your hand. If you do, +2 Cards, then put 2 cards from your hand on top of your deck.";
        ((CardImpl) Cards.nobles).description = "Choose one: +3 Cards; or +2 Actions.";
        ((CardImpl) Cards.coppersmith).description = "Copper produces an extra 1 coin this turn.";
        ((CardImpl) Cards.courtyard).description = "Put a card from your hand on top of your deck.";
        ((CardImpl) Cards.harem).description = "";
        ((CardImpl) Cards.baron).description = "You may discard an Estate card. If you do, +4 Coins. Otherwise, gain an Estate card.";
        ((CardImpl) Cards.bridge).description = "All cards (including cards in players' hands) cost 1 Coin less this turn, but not less than 0.";
        ((CardImpl) Cards.conspirator).description = "If you've played 3 or more Actions this turn (counting this): +1 Card, +1 Action.";
        ((CardImpl) Cards.ironworks).description = "Gain a card costing up to 4 Coins. If it is an... Action card, +1 Action. Treasure card, +1 Coin. Victory card, +1 Card.";
        ((CardImpl) Cards.masquerade).description = "Each player passes a card from his hand to the left at once. Then you may trash a card from your hand.";
        ((CardImpl) Cards.miningVillage).description = "You may trash this card immediately. If you do, +2 Coins.";
        ((CardImpl) Cards.minion).description = "Choose one: +2 Coins; or discard your hand, +4 Cards, and each other player with at least 5 cards in hand discards his hand and draws 4 cards.";
        ((CardImpl) Cards.pawn).description = "Choose two: +1 Card; +1 Action; +1 Buy; +1 Coin. (The choices must be different.)";
        ((CardImpl) Cards.saboteur).description = "Each other player reveals cards from the top of his deck until revealing one costing 3 Coins or more. He trashes that card and may gain a card costing at most 2 Coins less than it. He discards the other revealed cards.";
        ((CardImpl) Cards.shantyTown).description = "Reveal you hand. If you have no Action cards in hand, +2 Cards.";
        ((CardImpl) Cards.scout).description = "Reveal the top 4 cards of your deck. Put the revealed Victory cards into your hand. Put the other cards on top of your deck in any order.";
        ((CardImpl) Cards.steward).description = "Choose one: +2 Cards; or +2 Coins; or trash 2 cards from your hand.";
        ((CardImpl) Cards.swindler).description = "Each other player trashes the top card of his deck and gains a card with the same cost that you choose.";
        ((CardImpl) Cards.tradingPost).description = "Trash 2 cards from your hand. If you do, gain a Silver card; put it into your hand.";
        ((CardImpl) Cards.wishingWell).description = "Name a card. Reveal the top card of your deck. If it's the named card, put it into your hand.";
        ((CardImpl) Cards.upgrade).description = "Trash a card from your hand. Gain a card costing exactly 1 Coin more than it.";
        ((CardImpl) Cards.tribute).description = "The player to your left reveals then discards the top 2 cards of his deck. For each differently named card revealed, if it is an... Action Card, +2 Actions. Treasure Card, +2 Coins. Victory Card, +2 Cards.";
        ((CardImpl) Cards.greatHall).description = "";
        ((CardImpl) Cards.duke).description = "Worth 1 Victory Point per Duchy you have.";

        ((CardImpl) Cards.haven).description = "Set aside a card from your hand face down. At the start of your next turn, put it into your hand.";
        ((CardImpl) Cards.seaHag).description = "Each other player discards the top card of his deck, then gains a Curse card, putting it on top of his deck.";
        ((CardImpl) Cards.tactician).description = "Discard your hand. If you discarded any cards this way, then at the start of your next turn, +5 Cards, +1 Buy, and +1 Action.";
        ((CardImpl) Cards.caravan).description = "";
        ((CardImpl) Cards.lighthouse).description = "While this is in play, when another player plays an Attack card, it doesn't affect you.";
        ((CardImpl) Cards.fishingVillage).description = "";
        ((CardImpl) Cards.wharf).description = "";
        ((CardImpl) Cards.merchantShip).description = "";
        ((CardImpl) Cards.outpost).description = "You only draw 3 cards (instead of 5) in this turn's Clean-up phase. Take an extra turn after this one. This can't cause you to take more than two consecutive turns.";
        ((CardImpl) Cards.ghostShip).description = "Each other player with 4 or more cards in hand puts cards from his hand on top of his deck until he has 3 cards in his hand.";
        ((CardImpl) Cards.salvager).description = "Trash a card from your hand. + Coins equal to its cost.";
        ((CardImpl) Cards.pirateShip).description = "Choose one: Each other player reveals the top 2 cards of his deck, trashes a revealed Treasure that you choose, discards the rest, and if anyone trashed a Treasure you take a Coin token; or, +1 Coin per Coin token you've taken with Pirate Ships this game.";
        ((CardImpl) Cards.nativeVillage).description = "Choose one: Set aside the top card of your deck face down on your Native Village mat; or put all the cards from your mat into your hand.";
        ((CardImpl) Cards.island).description = "Set aside this and another card from your hand. Return them to your deck at the end of the game.";
        ((CardImpl) Cards.cutpurse).description = "Each other player discards a Copper card (or reveals a hand with no Copper).";
        ((CardImpl) Cards.bazaar).description = "";
        ((CardImpl) Cards.smugglers).description = "Gain a copy of a card costing up to 6 Coins that the player to your right gained on his last turn.";
        ((CardImpl) Cards.explorer).description = "You may reveal a Province card from your hand. If you do, gain a Gold card, putting it into your hand. Otherwise, gain a Silver card, putting it into your hand.";
        ((CardImpl) Cards.pearlDiver).description = "Look at the bottom card of your deck. You may put it on top.";
        ((CardImpl) Cards.treasureMap).description = "Trash this and another copy of Treasure Map from your hand. If you do trash two Treasure Maps, gain 4 Gold cards putting them on top of your deck.";
        ((CardImpl) Cards.navigator).description = "Look at the top 5 cards of your deck. Either discard all of them, or put them back on top of your deck in any order.";
        ((CardImpl) Cards.treasury).description = "When you discard this from play, if you didn't buy a Victory card this turn, you may put this on top of your deck.";
        ((CardImpl) Cards.lookout).description = "Look at the top 3 cards of your deck. Trash one of them. Discard one of them. Put the other one on top of your deck.";
        ((CardImpl) Cards.ambassador).description = "Reveal a card from your hand. Return up to 2 copies of it from your hand to the Supply. Then each other player gains a copy of it.";
        ((CardImpl) Cards.warehouse).description = "Discard 3 cards.";
        ((CardImpl) Cards.embargo).description = "Trash this card. Put an Embargo token on top of a Supply pile. When a player buys a card, he gains a Curse card per Embargo token on that pile.";

        ((CardImpl) Cards.alchemist).description = "When you discard this from play, you may put this on top of your deck if you have a Potion in play.";
        ((CardImpl) Cards.apothecary).description = "Reveal the top 4 cards of your deck.  Put the revealed Coppers and Potions into your hand.  Put the other cards back on top of your deck in any order.";
        ((CardImpl) Cards.apprentice).description = "Trash a card from your hand.  +1 Card per Coin it costs.  +2 Cards if it has a Potion in its cost.";
        ((CardImpl) Cards.familiar).description = "Each other player gains a Curse.";
        ((CardImpl) Cards.golem).description = "Reveal cards from your deck until you reveal 2 Action cards other than Golem cards.  Discard the other cards, then play the Action cards in either order.";
        ((CardImpl) Cards.herbalist).description = "When you discard this from play, you may put one of your Treasures from play on top of your deck.";
        ((CardImpl) Cards.philosophersStone).description = "When you play this, count your deck and discard pile.  Worth (1) coin per 5 cards total between them (rounded down).";
        ((CardImpl) Cards.possession).description = "The player to your left takes an extra turn after this one, in which you can see all cards he can and make all decisions for him. Any cards he would gain on that turn, you gain instead; any cards of his that are trashed are set aside and returned to his discard pile at end of turn.";
        ((CardImpl) Cards.scryingPool).description = "Each player (including you) reveals the top card of his deck and either discards it or puts it back, your choice.  Then reveal cards from the top of your deck until you reveal one that is not an Action.  Put all of your revealed cards into your hand.";
        ((CardImpl) Cards.transmute).description = "Trash a card from your hand.  If it is an . . . Action card, gain a Duchy; Treasure card, gain a Transmute; Victory card gain a Gold.";
        ((CardImpl) Cards.university).description = "You may gain an Action card costing up to 5.";
        ((CardImpl) Cards.vineyard).description = "Worth 1 Victory Point for every 3 Action cards in your deck (rounded down).";

        ((CardImpl) Cards.bank).description = "When you play this, it's worth 1 coin per Treasure card you have in play (counting this).";
        ((CardImpl) Cards.bishop).description = "Trash a card from your hand.  Gain Victory tokens equal to half its cost in coins, rounded down.  Each other player may trash a card from his hand.";
        ((CardImpl) Cards.city).description = "If there are one or more empty Supply piles, +1 Card.  If there are two or more, +1 Coin and +1 Buy.";
        ((CardImpl) Cards.contraband).description = "When you play his, the player to your left names a card.  You can't buy that card this turn.";
        ((CardImpl) Cards.countingHouse).description = "Look through your discard pile, reveal any number of Copper cards from it, and put them into your hand.";
        ((CardImpl) Cards.expand).description = "Trash a card from your hand.  Gain a card costing up to 3 coins more than the trashed card.";
        ((CardImpl) Cards.forge).description = "Trash any number of cards from your hand.  Gain a card with cost exactly equal to the total cost in coins of the trashed cards.";
        ((CardImpl) Cards.goons).description = "Eash other player discards down to 3 cards in hand.  While this is in play, when you buy a card, +1 Victory token.";
        ((CardImpl) Cards.grandMarket).description = "You can't buy this if you have any Copper in play.";
        ((CardImpl) Cards.hoard).description = "While this is in play, when you buy a Victory card, gain a Gold.";
        ((CardImpl) Cards.kingsCourt).description = "You may choose an Action card in your hand.  Play it three times.";
        ((CardImpl) Cards.loan).description = "When you play this, reveal cards from your deck until you reveal a Treasure.  Discard it or trash it.  Discard the other cards.";
        ((CardImpl) Cards.mint).description = "You may reveal a Treasure card from your hand.  Gain a copy of it.  When you buy this, trash all Treasures you have in play.";
        ((CardImpl) Cards.monument).description = "";
        ((CardImpl) Cards.mountebank).description = "Each other player may discard a Curse.  If he doesn't, he gains a Curse and a Copper.";
        ((CardImpl) Cards.peddler).description = "During your Buy phase, this costs 2 coins less per Action card you have in play, but not less that 0 coins.";
        ((CardImpl) Cards.quarry).description = "While this is in play, Action cards cost 2 coins less, but not less than 0 coins.";
        ((CardImpl) Cards.rabble).description = "Each other player reveals the top 3 cards of his deck, discards the revealed Actions and Treasures, and puts the rest back on top in any order he chooses.";
        ((CardImpl) Cards.royalSeal).description = "While this is in play, when you gain a card, you may put that card on top of your deck.";
        ((CardImpl) Cards.talisman).description = "While this is in play, when you buy a card costing 4 coins or less that is not a Victory card, gain a copy of it.";
        ((CardImpl) Cards.tradeRoute).description = "+1 Coin per token on the Trade Route mat.  Trash a card from your hand.  Setup: Put a token on each Victory card Supply pile.  When a card is gained from that pile, move the token to the Trade Route mat.";
        ((CardImpl) Cards.vault).description = "Discard any number of cards.  +1 coin per card discarded.  Each other player may discard 2 cards.  If he does, he draws a card.";
        ((CardImpl) Cards.venture).description = "When you play this, reveal cards from your deck until you reveal a Treasure.  Discard the other cards.  Play that Treasure.";
        ((CardImpl) Cards.watchTower).description = "Draw until you have 6 cards in hand.  When you gain a card, you may reveal this from your hand.  If you do, either trash that card, or put it on top of your deck.";

        ((CardImpl) Cards.fairgrounds).description = "Worth 2 points for every 5 differently named cards in your deck (round down).";
        ((CardImpl) Cards.farmingVillage).description = "Reveal cards from the top of your deck until you reveal an Action or Treasure card.  Put that card into your hand and discard the other cards.";
        ((CardImpl) Cards.fortuneTeller).description = "Each other player reveals cards from the top of his deck until he reveals a Victory or Curse card.  He puts it on top and discards the other revealed cards.";
        ((CardImpl) Cards.hamlet).description = "You may discard a card; if you do +1 Action.  You may discard a card; if you do +1 Buy.";
        ((CardImpl) Cards.harvest).description = "Reveal the top 4 cards of your deck, then discard them.  +1 coin per differently named card revealed.";
        ((CardImpl) Cards.hornOfPlenty).description = "When you play this, gain a card costing up to 1 coin per differently named card you have in play, counting this.  If it's a Victory card, trash this.";
        ((CardImpl) Cards.horseTraders).description = "Discard 2 cards.  When another player plays an Attack card, you may set this aside from your hand.  If you do, then at the start of your next turn, +1 Card and return this to your hand.";
        ((CardImpl) Cards.huntingParty).description = "Reveal your hand.  Reveal cards from your deck until you reveal a card that isn't a duplicate of one in your hand.  Put it into your hand and discard the rest.";
        ((CardImpl) Cards.jester).description = "Each other player discards the top card of his deck.  If it's a Victory card, he gains a Curse.  Otherwise either he gains a copy of the discarded card or you do, your choice.";
        ((CardImpl) Cards.menagerie).description = "Reveal your hand.  If there are no duplicate cards in it, +3 Cards.  Otherwise, +1 Card.";
        ((CardImpl) Cards.remake).description = "Do this twice.  Trash a card from your hand, then gain a card costing exactly 1 coin more than the trashed card.";
        ((CardImpl) Cards.tournament).description = "Each player may reveal a Province from his hand.  If you do, discard it and gain a Prize (from the Prize pile) or a Duchy, putting it on top of your deck.  If no one else does, +1 Card, +1 coin.";
        ((CardImpl) Cards.youngWitch).description = "Discard 2 cards.  Each other player may reveal a Bane card from his hand.  If he doesn't, he gains a Curse.  Setup:  Add an extra Kingdom card pile costing 2 or 3 coins to the Supply.  Cards from that pile are Bane cards.";

        ((CardImpl) Cards.bagOfGold).description = "Gain a Gold, putting it on top of your deck.  (This is not in the Supply.)";
        ((CardImpl) Cards.diadem).description = "When you play this, +1 coin per unused Action you have (Action, not Action card).  (This is not in the Supply.)";
        ((CardImpl) Cards.followers).description = "Gain an Estate.  Each other player gains a Curse and discards down to 3 cards in hand.  (This is not in the Supply.)";
        ((CardImpl) Cards.princess).description = "While this is in play, cards cost 2 coins less, but not less than 0.  (This is not in the Supply.)";
        ((CardImpl) Cards.trustySteed).description = "Choose two:  +2 Cards; +2 Actions; +2 coins; gain 4 silvers and put your deck into your discard pile.  (The choices must be different.)  (This is not in the Supply.)";
        
        ((CardImpl) Cards.borderVillage).description = "When you gain this, gain a card costing less than this.";
        ((CardImpl) Cards.cache).description = "When you gain this, gain two Coppers.";
        ((CardImpl) Cards.cartographer).description = "Look at the top 4 cards of your deck. Discard any number of them. Put the rest back on top in any order.";
        ((CardImpl) Cards.crossroads).description = "Reveal your hand. +1 Card per Victory card revealed. If this is the first time you played a Crossroads this turn, +3 Actions.";
        ((CardImpl) Cards.develop).description = "Trash a card from your hand. Gain a card costing exactly 1 coin more than it and a card costing exactly 1 less than it, in either order, putting them on top of your deck.";
        ((CardImpl) Cards.duchess).description = "Each player (including you) looks at the top card of his deck, and discards it or puts it back - In games using this, when you gain a Duchy, you may gain a Duchess.";
        ((CardImpl) Cards.embassy).description = "Discard 3 cards - When you gain this, each other player gains a Silver.";
        ((CardImpl) Cards.farmland).description = "When you buy this, trash a card from your hand. Gain a card costing exactly 2 coins more than the trashed card.";
        ((CardImpl) Cards.foolsGold).description = "If this is the first time you played a Fool's Gold this turn, this is worth 1 coin, otherwise it's worth 4 coins - When another player gains a Province, you may trash this from your hand. If you do, gain a Gold, putting it on your deck.";
        ((CardImpl) Cards.haggler).description = "While this is in play, when you buy a card, gain a card costing less than it that is not a Victory card.";
        ((CardImpl) Cards.highway).description = "While this is in play, cards cost 1 coin less, but not less than 0 coin.";
        ((CardImpl) Cards.illGottenGains).description = "When you play this, you may gain a Copper, putting it into your hand - When you gain this, each other player gains a Curse.";
        ((CardImpl) Cards.inn).description = "Discard 2 cards - When you gain this, look through your discard pile (including this), reveal any number of Action cards from it, and shuffle them into your deck.";
        ((CardImpl) Cards.jackOfAllTrades).description = "Gain a Silver. Look at the top card of your deck; discard it or put it back. Draw until you have 5 cards in hand. You may trash a card from your hand that is not a Treasure.";
        ((CardImpl) Cards.mandarin).description = "Put a card from your hand on top of your deck - When you gain this, put all Treasures you have in play on top of your deck in any order.";
        ((CardImpl) Cards.margrave).description = "Each other player draws a card, then discards down to 3 cards in hand.";
        ((CardImpl) Cards.nobleBrigand).description = "When you buy this or play it, each other player reveals the top 2 cards of his deck, trashes a revealed Silver or Gold you choose, and discards the rest. If he didn't reveal a Treasure, he gains a Copper. You gain the trashed cards.";
        ((CardImpl) Cards.nomadCamp).description = "When you gain this, put it on top of your deck.";
        ((CardImpl) Cards.oasis).description = "Discard a card.";
        ((CardImpl) Cards.oracle).description = "Each player (including you) reveals the top 2 cards of his deck, and you choose one: either he discards them, or he puts them back on top in an order he chooses.\n+2 Cards";
        ((CardImpl) Cards.scheme).description = "At the start of Clean-up this turn, you may choose an Action card you have in play. If you discard it from play this turn, put it on your deck.";
        ((CardImpl) Cards.silkRoad).description = "Worth 1 VP for every 4 Victory cards in your deck (round down).";
        ((CardImpl) Cards.spiceMerchant).description = "You may trash a Treasure from your hand. If you do, choose one: +2 Cards and +1 Action; or +2 Coin and +1 Buy.";
        ((CardImpl) Cards.stables).description = "You may discard a Treasure. If you do, +3 Cards and +1 Action.";
        ((CardImpl) Cards.trader).description = "Trash a card from your hand. Gain a number of Silvers equal to its cost in coins - When you would gain a card, you may reveal this from your hand. If you do, instead, gain a silver.";
        ((CardImpl) Cards.tunnel).description = "When you discard this other than during a Clean-up phase, you may reveal it. If you do, gain a Gold.";
        
        actionCardsBaseGame.add(Cards.moat);
        actionCardsBaseGame.add(Cards.adventurer);
        actionCardsBaseGame.add(Cards.bureaucrat);
        actionCardsBaseGame.add(Cards.cellar);
        actionCardsBaseGame.add(Cards.chancellor);
        actionCardsBaseGame.add(Cards.chapel);
        actionCardsBaseGame.add(Cards.councilRoom);
        actionCardsBaseGame.add(Cards.feast);
        actionCardsBaseGame.add(Cards.festival);
        actionCardsBaseGame.add(Cards.laboratory);
        actionCardsBaseGame.add(Cards.library);
        actionCardsBaseGame.add(Cards.market);
        actionCardsBaseGame.add(Cards.militia);
        actionCardsBaseGame.add(Cards.mine);
        actionCardsBaseGame.add(Cards.moneyLender);
        actionCardsBaseGame.add(Cards.remodel);
        actionCardsBaseGame.add(Cards.smithy);
        actionCardsBaseGame.add(Cards.spy);
        actionCardsBaseGame.add(Cards.thief);
        actionCardsBaseGame.add(Cards.throneRoom);
        actionCardsBaseGame.add(Cards.village);
        actionCardsBaseGame.add(Cards.witch);
        actionCardsBaseGame.add(Cards.woodcutter);
        actionCardsBaseGame.add(Cards.workshop);
        actionCardsBaseGame.add(Cards.gardens);
        for(Card c : actionCardsBaseGame) {
            ((CardImpl) c).expansion = "Base";
        }

        actionCardsIntrigue.add(Cards.torturer);
        actionCardsIntrigue.add(Cards.secretChamber);
        actionCardsIntrigue.add(Cards.nobles);
        actionCardsIntrigue.add(Cards.coppersmith);
        actionCardsIntrigue.add(Cards.courtyard);
        actionCardsIntrigue.add(Cards.harem);
        actionCardsIntrigue.add(Cards.baron);
        actionCardsIntrigue.add(Cards.bridge);
        actionCardsIntrigue.add(Cards.conspirator);
        actionCardsIntrigue.add(Cards.ironworks);
        actionCardsIntrigue.add(Cards.masquerade);
        actionCardsIntrigue.add(Cards.miningVillage);
        actionCardsIntrigue.add(Cards.minion);
        actionCardsIntrigue.add(Cards.pawn);
        actionCardsIntrigue.add(Cards.saboteur);
        actionCardsIntrigue.add(Cards.shantyTown);
        actionCardsIntrigue.add(Cards.scout);
        actionCardsIntrigue.add(Cards.steward);
        actionCardsIntrigue.add(Cards.swindler);
        actionCardsIntrigue.add(Cards.tradingPost);
        actionCardsIntrigue.add(Cards.wishingWell);
        actionCardsIntrigue.add(Cards.upgrade);
        actionCardsIntrigue.add(Cards.tribute);
        actionCardsIntrigue.add(Cards.greatHall);
        actionCardsIntrigue.add(Cards.duke);
        
        for(Card c : actionCardsIntrigue) {
            ((CardImpl) c).expansion = "Intrigue";
        }
        
        actionCardsSeaside.add(Cards.haven);
        actionCardsSeaside.add(Cards.seaHag);
        actionCardsSeaside.add(Cards.tactician);
        actionCardsSeaside.add(Cards.caravan);
        actionCardsSeaside.add(Cards.lighthouse);
        actionCardsSeaside.add(Cards.fishingVillage);
        actionCardsSeaside.add(Cards.wharf);
        actionCardsSeaside.add(Cards.merchantShip);
        actionCardsSeaside.add(Cards.outpost);
        actionCardsSeaside.add(Cards.ghostShip);
        actionCardsSeaside.add(Cards.salvager);
        actionCardsSeaside.add(Cards.pirateShip);
        actionCardsSeaside.add(Cards.nativeVillage);
        actionCardsSeaside.add(Cards.island);
        actionCardsSeaside.add(Cards.cutpurse);
        actionCardsSeaside.add(Cards.bazaar);
        actionCardsSeaside.add(Cards.smugglers);
        actionCardsSeaside.add(Cards.explorer);
        actionCardsSeaside.add(Cards.pearlDiver);
        actionCardsSeaside.add(Cards.treasureMap);
        actionCardsSeaside.add(Cards.navigator);
        actionCardsSeaside.add(Cards.treasury);
        actionCardsSeaside.add(Cards.lookout);
        actionCardsSeaside.add(Cards.ambassador);
        actionCardsSeaside.add(Cards.warehouse);
        actionCardsSeaside.add(Cards.embargo);

        for(Card c : actionCardsSeaside) {
            ((CardImpl) c).expansion = "Seaside";
        }
        
        actionCardsProsperity.add(Cards.bank);
        actionCardsProsperity.add(Cards.bishop);
        actionCardsProsperity.add(Cards.city);
        actionCardsProsperity.add(Cards.contraband);
        actionCardsProsperity.add(Cards.countingHouse);
        actionCardsProsperity.add(Cards.expand);
        actionCardsProsperity.add(Cards.forge);
        actionCardsProsperity.add(Cards.goons);
        actionCardsProsperity.add(Cards.grandMarket);
        actionCardsProsperity.add(Cards.hoard);
        actionCardsProsperity.add(Cards.kingsCourt);
        actionCardsProsperity.add(Cards.loan);
        actionCardsProsperity.add(Cards.mint);
        actionCardsProsperity.add(Cards.monument);
        actionCardsProsperity.add(Cards.mountebank);
        actionCardsProsperity.add(Cards.peddler);
        actionCardsProsperity.add(Cards.quarry);
        actionCardsProsperity.add(Cards.rabble);
        actionCardsProsperity.add(Cards.royalSeal);
        actionCardsProsperity.add(Cards.talisman);
        actionCardsProsperity.add(Cards.tradeRoute);
        actionCardsProsperity.add(Cards.vault);
        actionCardsProsperity.add(Cards.venture);
        actionCardsProsperity.add(Cards.watchTower);
        actionCardsProsperity.add(Cards.workersVillage);

        for(Card c : actionCardsProsperity) {
            ((CardImpl) c).expansion = "Prosperity";
        }
        
        actionCardsAlchemy.add(Cards.alchemist);
        actionCardsAlchemy.add(Cards.apothecary);
        actionCardsAlchemy.add(Cards.apprentice);
        actionCardsAlchemy.add(Cards.familiar);
        actionCardsAlchemy.add(Cards.golem);
        actionCardsAlchemy.add(Cards.herbalist);
        actionCardsAlchemy.add(Cards.philosophersStone);
        actionCardsAlchemy.add(Cards.possession);
        actionCardsAlchemy.add(Cards.scryingPool);
        actionCardsAlchemy.add(Cards.transmute);
        actionCardsAlchemy.add(Cards.university);
        actionCardsAlchemy.add(Cards.vineyard);

        for(Card c : actionCardsAlchemy) {
            ((CardImpl) c).expansion = "Alchemy";
        }

        actionCardsCornucopia.add(Cards.fairgrounds);
        actionCardsCornucopia.add(Cards.farmingVillage);
        actionCardsCornucopia.add(Cards.fortuneTeller);
        actionCardsCornucopia.add(Cards.hamlet);
        actionCardsCornucopia.add(Cards.harvest);
        actionCardsCornucopia.add(Cards.hornOfPlenty);
        actionCardsCornucopia.add(Cards.horseTraders);
        actionCardsCornucopia.add(Cards.huntingParty);
        actionCardsCornucopia.add(Cards.jester);
        actionCardsCornucopia.add(Cards.menagerie);
        actionCardsCornucopia.add(Cards.remake);
        actionCardsCornucopia.add(Cards.tournament);
        actionCardsCornucopia.add(Cards.youngWitch);

        for(Card c : actionCardsCornucopia) {
            ((CardImpl) c).expansion = "Cornucopia";
        }
        
        actionCardsHinterlands.add(Cards.borderVillage);
        actionCardsHinterlands.add(Cards.cache);
        actionCardsHinterlands.add(Cards.cartographer);
        actionCardsHinterlands.add(Cards.crossroads);
        actionCardsHinterlands.add(Cards.develop);
        actionCardsHinterlands.add(Cards.duchess);
        actionCardsHinterlands.add(Cards.embassy);
        actionCardsHinterlands.add(Cards.farmland);
        actionCardsHinterlands.add(Cards.foolsGold);
        actionCardsHinterlands.add(Cards.haggler);
        actionCardsHinterlands.add(Cards.highway);
        actionCardsHinterlands.add(Cards.illGottenGains);
        actionCardsHinterlands.add(Cards.inn);
        actionCardsHinterlands.add(Cards.jackOfAllTrades);
        actionCardsHinterlands.add(Cards.mandarin);
        actionCardsHinterlands.add(Cards.margrave);
        actionCardsHinterlands.add(Cards.nobleBrigand);
        actionCardsHinterlands.add(Cards.nomadCamp);
        actionCardsHinterlands.add(Cards.oasis);
        actionCardsHinterlands.add(Cards.oracle);
        actionCardsHinterlands.add(Cards.scheme);
        actionCardsHinterlands.add(Cards.silkRoad);
        actionCardsHinterlands.add(Cards.spiceMerchant);
        actionCardsHinterlands.add(Cards.stables);
        actionCardsHinterlands.add(Cards.trader);
        actionCardsHinterlands.add(Cards.tunnel);

        for(Card c : actionCardsHinterlands) {
            ((CardImpl) c).expansion = "Hinterlands";
        }
        
        for (Card card : actionCardsBaseGame) {
            actionCards.add(card);
        }
        for (Card card : actionCardsIntrigue) {
            actionCards.add(card);
        }
        for (Card card : actionCardsSeaside) {
            actionCards.add(card);
        }
        for (Card card : actionCardsAlchemy) {
            actionCards.add(card);
        }
        for (Card card : actionCardsProsperity) {
            actionCards.add(card);
        }
        for (Card card : actionCardsCornucopia) {
            actionCards.add(card);
        }
        for (Card card : actionCardsHinterlands) {
            actionCards.add(card);
        }
    }

    public static void main(String[] args) {
        go(args, false);
    }

    public static void go(String[] args, boolean html) {
        cardsSpecifiedAtLaunch = null;
        overallWins.clear();
        cardSequence = 1;
        GAME_TYPE_WINS.clear();
        playerClassesAndJars.clear();
        gameTypeStats.clear();
        playerCache.clear();

        try {
            String gameCountArg = "-count";
            String debugArg = "-debug";
            String showEventsArg = "-showevents";
            String showPlayersArg = "-showplayers";
            String gameTypeArg = "-type";
            String gameTypeStatsArg = "-test";
            String ignorePlayerErrorsArg = "-ignore";
            String showUIArg = "-ui";
            String siteArg = "-site=";
            String platColonyArg = "-platcolony";
            String quickPlayArg = "-quickplay";
            String cardArg = "-cards=";
            String gameTypeStr = null;

            boolean showUsage = false;
            for (String arg : args) {
                if(arg == null) {
                    continue;
                }
                
                if (arg.startsWith("#")) {
                    continue;
                }

                if (arg.startsWith("-")) {
                    if (arg.toLowerCase().equals(debugArg)) {
                        debug = true;
                        if (showEvents.isEmpty()) {
                            for (GameEvent.Type eventType : GameEvent.Type.values()) {
                                showEvents.add(eventType);
                            }
                        }
                    } else if (arg.toLowerCase().startsWith(showEventsArg)) {
                        String showEventsString = arg.substring(showEventsArg.length() + 1);
                        for (String event : showEventsString.split(",")) {
                            showEvents.add(GameEvent.Type.valueOf(event));
                        }
                    } else if (arg.toLowerCase().startsWith(showPlayersArg)) {
                        String showPlayersString = arg.substring(showPlayersArg.length() + 1);
                        for (String player : showPlayersString.split(",")) {
                            showPlayers.add(player);
                        }
                    } else if (arg.toLowerCase().startsWith(ignorePlayerErrorsArg)) {
                        if (arg.trim().toLowerCase().equals(ignorePlayerErrorsArg)) {
                            ignoreAllPlayerErrors = true;
                        } else {
                            ignoreSomePlayerErrors = true;

                            try {
                                String player = arg.substring(ignorePlayerErrorsArg.length()).trim();
                                ignoreList.add(player);
                            } catch (Exception e) {
                                Util.log(e);
                                throw new ExitException();
                            }
                        }
                    } else if (arg.toLowerCase().equals(gameTypeStatsArg)) {
                        test = true;
                    } else if (arg.toLowerCase().startsWith(gameCountArg)) {
                        try {
                            numGames = Integer.parseInt(arg.substring(gameCountArg.length()));
                        } catch (Exception e) {
                            Util.log(e);
                            throw new ExitException();
                        }
                    } else if (arg.toLowerCase().startsWith(showUIArg)) {
                        showUI = true;
                    } else if (arg.toLowerCase().startsWith(cardArg)) {
                        try {
                            cardsSpecifiedAtLaunch = arg.substring(cardArg.length()).split("-");
                        } catch (Exception e) {
                            Util.log(e);
                            throw new ExitException();
                        }
                    } else if (arg.toLowerCase().startsWith(siteArg)) {
                        try {
//                            UI.downloadSite = arg.substring(siteArg.length());
                        } catch (Exception e) {
                            Util.log(e);
                            throw new ExitException();
                        }
                    } else if (arg.toLowerCase().startsWith(gameTypeArg)) {
                        try {
                            gameTypeStr = arg.substring(gameTypeArg.length());
                        } catch (Exception e) {
                            Util.log(e);
                            throw new ExitException();
                        }
                    } else if (arg.toLowerCase().equals(platColonyArg)) {
                        alwaysIncludePlatColony = true;
                    } else if (arg.toLowerCase().equals(quickPlayArg)) {
                        quickPlay = true;
                    } else {
                        Util.log("Invalid arg:" + arg);
                        showUsage = true;
                    }
                } else {
                    String options = "";
                    String name = null;
                    int starIndex = arg.indexOf("*");
                    if(starIndex != -1) {
                        name = arg.substring(starIndex + 1);
                        arg = arg.substring(0, starIndex);
                    }
                    if(arg.endsWith(QUICK_PLAY)) {
                        arg = arg.substring(0, arg.length() - QUICK_PLAY.length());
                        options += "q";
                    }
                    int atIndex = arg.indexOf("@");
                    String className = arg;
                    String jar = null;
                    if (atIndex != -1) {
                        className = className.substring(0, atIndex);
                        jar = arg.substring(atIndex + 1);
                    }
                    playerClassesAndJars.add(new String[] { className, jar, name, options });
                }
            }

//            if (test) {
//                while (playerClassesAndJars.size() == 0) {
//                    playerClassesAndJars.add(new String[] { "com.vdom.core.RegularPlayer1", null });
//                    playerClassesAndJars.add(new String[] { "com.vdom.core.RegularPlayer2", null });
//                    if (numGames == -1) {
//                        numGames = 100;
//                    }
//                }
//            }

            if (playerClassesAndJars.size() < 2 || playerClassesAndJars.size() > 4 || showUsage) {
                Util.log("Usage: [-debug][-ignore(playername)][-count(# of Games)][-type(Game type)] class1 class2 [class3] [class4]");
                throw new ExitException();
            }

            numPlayers = playerClassesAndJars.size();

            checkForInteractive();

            if (gameTypeStr == null) {
                if (interactive) {
                    gameTypeStr = "Random";
                } else if (debug) {
                    gameTypeStr = "FirstGame";
                }
            }

            if (numGames == -1) {
                if (debug || interactive || showUI) {
                    numGames = 1;
                } else {
                    numGames = 20;
                }
            }

            Util.log("");

            if (gameTypeStr != null) {
                gameType = GameType.fromName(gameTypeStr);
                new Game().start();
            } else if (showUI) {
                GameType[] gameTypes = GameType.values();
                gameType = gameTypes[rand.nextInt(gameTypes.length)];
                new Game().start();
            } else {
                for (String[] className : playerClassesAndJars) {
                    overallWins.put(className[0], 0.0);
                }

                for (GameType p : GameType.values()) {
                    gameType = p;
                    new Game().start();
                }

                if (test) {
                    for (int i = 0; i < 5; i++) {
                        gameType = GameType.Random;
                        new Game().start();
                    }
                    for (int i = 0; i < 5; i++) {
                        gameType = GameType.RandomBaseGame;
                        new Game().start();
                    }
                    for (int i = 0; i < 5; i++) {
                        gameType = GameType.RandomIntrigue;
                        new Game().start();
                    }
                    for (int i = 0; i < 5; i++) {
                        gameType = GameType.RandomSeaside;
                        new Game().start();
                    }
                }
                if (!debug && !interactive && !test) {
                    Util.log("----------------------------------------------------");
                }
                printStats(overallWins, numGames * GameType.values().length, "Total");
                printStats(GAME_TYPE_WINS, GameType.values().length, "Types");
            }
            if (test) {
                printGameTypeStats();
            }
        } catch (ExitException e) {
            // Ignore...
        }

        FrameworkEvent frameworkEvent = new FrameworkEvent(FrameworkEvent.Type.AllDone);
        FrameworkEventHelper.broadcastEvent(frameworkEvent);
    }

    private void markWinner(HashMap<String, Double> gameTypeSpecificWins) {
        double highWins = 0;
        int winners = 0;

        for (String player : gameTypeSpecificWins.keySet()) {
            if (gameTypeSpecificWins.get(player) > highWins) {
                highWins = gameTypeSpecificWins.get(player);
            }
        }

        for (String player : gameTypeSpecificWins.keySet()) {
            if (gameTypeSpecificWins.get(player) == highWins) {
                winners++;
            }
        }

        double points = 1.0 / winners;
        for (String player : gameTypeSpecificWins.keySet()) {
            if (gameTypeSpecificWins.get(player) == highWins) {
                double playerWins = 0;
                if (GAME_TYPE_WINS.containsKey(player)) {
                    playerWins = GAME_TYPE_WINS.get(player);
                }
                playerWins += points;

                GAME_TYPE_WINS.put(player, playerWins);
            }
        }

        GAME_TYPE_WINS.toString();
    }
    
    void start() throws ExitException {
        HashMap<String, Double> gameTypeSpecificWins = new HashMap<String, Double>();

        for (String[] className : playerClassesAndJars) {
            gameTypeSpecificWins.put(className[0], 0.0);
            if (!GAME_TYPE_WINS.containsKey(className[0])) {
                GAME_TYPE_WINS.put(className[0], 0.0);
            }
        }

        long turnCountTotal = 0;
        long vpTotal = 0;
        long numCardsTotal = 0;

        // if (test) {
        // System.out.print(gameType + " ");
        // }
        for (int gameCount = 0; gameCount < numGames; gameCount++) {
            // if (test) {
            // System.out.print(gameCount + " ");
            // System.out.flush();
            // }
            Util.debug("---------------------", false);

            Util.debug("New Game:" + gameType);
            init();

            playersTurn = 0;
            turnCount = 1;
            Util.debug("Turn " + turnCount);

            int consecutiveTurns = 0;
            while (!gameOver) {
                consecutiveTurns++;
                cardsObtainedLastTurn[playersTurn].clear();

                Player player = players[playersTurn];

                MoveContext context = new MoveContext(this, player);

                GameEvent gevent = new GameEvent(GameEvent.Type.TurnBegin, context);
                broadcastEvent(gevent);

                int durationThroneRooms = 0;
                for (Card card : player.nextTurnCards) {
                    if (card.equals(Cards.throneRoom))
                        durationThroneRooms++;
                    else {
                        if (card instanceof DurationCard) {
                            DurationCard thisCard = (DurationCard) card;

                            GameEvent event = new GameEvent(GameEvent.Type.PlayingDurationAction, (MoveContext) context);
                            event.card = thisCard;
                            broadcastEvent(event);

                            context.actions += thisCard.getAddActionsNextTurn();
                            context.addGold += thisCard.getAddGoldNextTurn();
                            context.buys += thisCard.getAddBuysNextTurn();

                            for (int i = 0; i < thisCard.getAddCardsNextTurn(); i++) {
                                drawToHand(player, thisCard, true);
                            }
                            if (durationThroneRooms > 0) {
                                durationThroneRooms--;
                                broadcastEvent(event);

                                context.actions += thisCard.getAddActionsNextTurn();
                                context.addGold += thisCard.getAddGoldNextTurn();
                                context.buys += thisCard.getAddBuysNextTurn();

                                for (int i = 0; i < thisCard.getAddCardsNextTurn(); i++) {
                                    drawToHand(player, thisCard, true);
                                }
                            }
                        } else {
                            System.out.println("Bad duration card: " + card);
                        }
                    }
                }

                while (!player.nextTurnCards.isEmpty()) {
                    context.playedCards.add(player.nextTurnCards.remove(0));
                }

                while (!player.haven.isEmpty()) {
                    player.hand.add(player.haven.remove(0));
                }

                while (!player.horseTraders.isEmpty()) {
                    Card horseTrader = player.horseTraders.remove(0);
                    player.hand.add(horseTrader);
                    drawToHand(player, horseTrader);
                }

                // TODO move this check to action and buy (and others?)
                // if(player.hand.size() > 0)
                // /////////////////////////////////
                // Actions
                // /////////////////////////////////
                Card action = null;
                do {
                    action = player.doAction(context);

                    if (isValidAction(context, action)) {
                        if (action != null) {
                            GameEvent event = new GameEvent(GameEvent.Type.Status, (MoveContext) context);
                            broadcastEvent(event);
                            
                            context.actions--;
                            playAction(context, action);
                        }
                    } else {
                        Util.log("Error:");
                        action = null;
                    }
                } while (context.actions > 0 && action != null);

                // Set the turn gold to the correct amt
                context.gold = context.addGold;
                context.addGold = 0;
                context.potions = 0;

                ArrayList<TreasureCard> treasures = null;
                
                boolean selectingCoins = playerShouldSelectCoinsToPlay(context, context.getPlayer().getHand());

                treasures = context.getPlayer().getTreasuresInHand();
                if(selectingCoins && treasures.size() > 0)
                    treasures = player.treasureCardsToPlayInOrder(context);

                while (treasures != null && treasures.size() > 0) {
                    for (TreasureCard card : treasures)
                        card.playTreasure(context);

                    treasures = context.getPlayer().getTreasuresInHand();
                    if(selectingCoins && treasures.size() > 0)
                        treasures = player.treasureCardsToPlayInOrder(context);
                }

                // /////////////////////////////////
                // Buys
                // /////////////////////////////////
                boolean victoryCardBought = false;
                Card buy = null;
                boolean boughtACard = false;
                do {
                    try {
                        buy = player.doBuy(context);
                    } catch (Throwable t) {
                        Util.playerError(player, t);
                    }

                    if (buy != null) {
                        if (isValidBuy(context, buy)) {
                            GameEvent statusEvent = new GameEvent(GameEvent.Type.Status, (MoveContext) context);
                            broadcastEvent(statusEvent);
                            
                            context.buys--;
                            playBuy(context, buy);
                            player.addVictoryTokens(context, context.goonsPlayed);

                            boughtACard = true;

                            if (buy instanceof VictoryCard) {
                                victoryCardBought = true;

                                for (int i = 0; i < context.hoardsPlayed; i++) {
                                    player.gainNewCard(Cards.gold, Cards.hoard, context);
                                }
                            } else if (buy.equals(Cards.mint)) {
                                ArrayList<Card> toTrash = new ArrayList<Card>();
                                for (Card card : context.playedCards) 
                                    if (card instanceof TreasureCard) 
                                        toTrash.add(card);
                                
                                for (Card card : toTrash) {
                                    context.playedCards.remove(card);
                                    context.cardsTrashedThisTurn++;
                                    GameEvent event = new GameEvent(GameEvent.Type.CardTrashed, context);
                                    event.card = card;
                                    broadcastEvent(event);                                   
                                }
                            }

                            int embargos = getEmbargos(buy.getName());

                            for (int i = 0; i < embargos; i++) {
                                player.gainNewCard(Cards.curse, Cards.embargo, context);
                            }
                        } else {
                            // TODO report?
                            buy = null;
                        }
                    }
                } while (context.buys > 0 && buy != null);

                if (!boughtACard) {
                    GameEvent event = new GameEvent(GameEvent.Type.NoBuy, context);
                    broadcastEvent(event);
                    Util.debug(player.getPlayerName() + " did not buy a card with coins:" + context.getCoinAvailableForBuy());
                }

                // /////////////////////////////////
                // Discard leftovers
                // /////////////////////////////////
                // TODO move to Player
                while (player.getHand().size() > 0) {
                    player.discard(player.hand.remove(0, false), null, null, false);
                }
                while (context.throneRoomsInEffect.size() > 0)
                    player.discard(context.throneRoomsInEffect.remove(0), null, null, false);

                // /////////////////////////////////
                // Discard played cards
                // /////////////////////////////////
                int treasuryCardsToSave = 0;
                int treasuryCardsInPlay = 0;
                
                for (Card card : context.playedCards) {
                    if (card.equals(Cards.treasury)) {
                        treasuryCardsInPlay++;
                    }
                }

                if (!victoryCardBought && treasuryCardsInPlay > 0) {
                    treasuryCardsToSave = player.treasury_putBackOnDeck(context, treasuryCardsInPlay);
                }

                if (treasuryCardsToSave < 0 || treasuryCardsToSave > treasuryCardsInPlay) {
                    Util.playerError(player, "Treasury put back cards error, ignoring.");
                    treasuryCardsToSave = 0;
                }
                
                for (Card card : context.playedCards) {
                    if (card.equals(Cards.treasury)) {
                        treasuryCardsInPlay++;
                    }
                }
                
                int herbalistCount = 0;
                for (Card card : context.playedCards) {
                    if (card.equals(Cards.herbalist)) {
                        herbalistCount++;
                    }
                }
                while(herbalistCount-- > 0) {
                    ArrayList<TreasureCard> treasureCards = new ArrayList<TreasureCard>();
                    for(Card card : context.playedCards) {
                        if(card instanceof TreasureCard) {
                            treasureCards.add((TreasureCard) card);
                        }
                    }
                    
                    if(treasureCards.size() > 0) {
                        TreasureCard treasureCard = player.herbalist_backOnDeck(context, treasureCards.toArray(new TreasureCard[0]));
                        if(treasureCard != null && context.playedCards.contains(treasureCard)) {
                            context.playedCards.remove(treasureCard);
                            player.putOnTopOfDeck(treasureCard);
                        }
                    }
                }
                
                boolean alchemistPlayed = true;
                boolean potionPlayed = true;
                Card thisAlchemist = null;
                while(alchemistPlayed && potionPlayed) {
                    potionPlayed = false;
                    alchemistPlayed = false;
                    
                    for (Card card : context.playedCards) {
                        if (card.equals(Cards.alchemist)) {
                            alchemistPlayed = true;
                            thisAlchemist = card;
                        }
                        if (card.equals(Cards.potion)) {
                            potionPlayed = true;
                        }
                    }
                    
                    if(alchemistPlayed && potionPlayed && thisAlchemist != null) {
                        context.playedCards.remove(thisAlchemist);
                        boolean putBackAlchemist = player.alchemist_backOnDeck(context);
                        if (putBackAlchemist)
                            player.putOnTopOfDeck(thisAlchemist);
                        else
                            player.discard(thisAlchemist, null, null, false);
                        thisAlchemist = null;
                    }
                }
                
                while (treasuryCardsToSave-- > 0) {
                    int index = context.playedCards.indexOf(Cards.treasury);
                    if(index == -1) {
                        break;
                    }
                    Card card = context.playedCards.remove(index);
                    player.putOnTopOfDeck(card);
                }
                
                while(context.schemesPlayed-- > 0) {
                    ArrayList<Card> actions = new ArrayList<Card>();
                    for(Card c : context.playedCards) {
                        if(c instanceof ActionCard) {
                            actions.add(c);
                        }
                    }
                    if(actions.size() == 0) {
                        break;
                    }
                    
                    ActionCard actionToPutBack = player.scheme_actionToPutOnTopOfDeck(((MoveContext) context), actions.toArray(new ActionCard[0]));
                    if(actionToPutBack == null) {
                        break;
                    }
                    int index = context.playedCards.indexOf(actionToPutBack);
                    if(index == -1) {
                        Util.playerError(player, "Scheme returned invalid card to put back on top of deck, ignoring");
                        break;
                    }
                    Card card = context.playedCards.remove(index);
                    player.putOnTopOfDeck(card);
                    
                }

                while (!context.playedCards.isEmpty()) {
                    player.discard(context.playedCards.remove(0), null, null, false);
                }
                
                if(context.getPossessedBy() != null) {
                    while (!context.possessedTrashPile.isEmpty()) {
                        player.discard(context.possessedTrashPile.remove(0), null, null, false);
                    }
                }
                // /////////////////////////////////
                // Double check that deck/discard/hand all have valid cards.
                // /////////////////////////////////
                player.checkCardsValid();

                // /////////////////////////////////
                // Draw new hand
                // /////////////////////////////////

                int handCount = 5;

                boolean takeAnotherTurn = false;
                // Can only have at most two consecutive turns
                for (Card card : player.nextTurnCards) {
                    if ((card instanceof DurationCard) && ((DurationCard) card).takeAnotherTurn()) {
                        handCount = 3;
                        if (consecutiveTurns == 1) {
                            takeAnotherTurn = true;
                            break;
                        }
                    }
                }

                for (int i = 0; i < handCount; i++) {
                    drawToHand(player, null, false);
                }
                
                // /////////////////////////////////
                // Reset context for status update
                // /////////////////////////////////
                context.actions = 1;
                context.buys = 1;
                context.addGold = 0;
                context.coppersmithsPlayed = 0;
                context.gold = context.getCoinAvailableForBuy();

                GameEvent event = new GameEvent(GameEvent.Type.NewHand, context);
                broadcastEvent(event);
                event = null;

                // /////////////////////////////////
                // Turn End
                // /////////////////////////////////
                event = new GameEvent(GameEvent.Type.TurnEnd, context);
                broadcastEvent(event);

                bridgesInEffect = 0;

                gameOver = checkGameOver();
                
                if (!gameOver) {
                    if (!takeAnotherTurn) {

                        if(possessionsToProcess > 0) {
                            if(--possessionsToProcess == 0) {
                                possessingPlayer = null;
                            }
                        }
                        else if(possessionsToProcess == 0) {
                            playersTurn++;
                            possessionsToProcess = context.possessionsToProcess;
                            if(possessionsToProcess > 0) {
                                possessingPlayer = player;
                            }
                        }
                        
                        consecutiveTurns = 0;
                        if (playersTurn >= numPlayers) {
                            playersTurn = 0;
                            if (interactive) {
                                Util.debug("---", true);
                            }
                            Util.debug("Turn " + ++turnCount, true);
                        }
                    }
                }
            }

            if (debug || interactive) {
                for (Player player : players) {
                    Util.debug("", true);
                    ArrayList<Card> allCards = player.getAllCards();
                    StringBuilder msg = new StringBuilder();
                    msg.append(" " + allCards.size() + " Cards: ");

                    final HashMap<String, Integer> cardCounts = new HashMap<String, Integer>();
                    for (Card card : allCards) {
                        String key = card.getName() + " -> " + card.getDescription();
                        Integer count = cardCounts.get(key);
                        if (count == null) {
                            cardCounts.put(key, 1);
                        } else {
                            cardCounts.put(key, count + 1);
                        }
                    }

                    ArrayList<Card> removeDuplicates = new ArrayList<Card>();
                    for (Card card : allCards) {
                        if (!removeDuplicates.contains(card)) {
                            removeDuplicates.add(card);
                        }
                    }
                    allCards = removeDuplicates;

                    Collections.sort(allCards, new Comparator<Card>() {
                        public int compare(Card o1, Card o2) {
                            String keyOne = o1.getName() + " -> " + o1.getDescription();
                            String keyTwo = o2.getName() + " -> " + o2.getDescription();
                            return cardCounts.get(keyTwo) - cardCounts.get(keyOne);
                        }
                    });

                    boolean first = true;
                    for (Card card : allCards) {
                        String key = card.getName() + " -> " + card.getDescription();
                        if (first) {
                            first = false;
                        } else {
                            msg.append(", ");
                        }
                        msg.append("" + cardCounts.get(key) + " " + card.getName());
                    }

                    Util.debug(player.getPlayerName() + ":" + msg, true);
                }
                Util.debug("", true);
            }

            int[] vps = calculateVps();

            for (int i = 0; i < numPlayers; i++) {
                int tieCount = 0;
                boolean loss = false;
                for (int j = 0; j < numPlayers; j++) {
                    if (i == j) {
                        continue;
                    }
                    if (vps[i] < vps[j]) {
                        loss = true;
                        break;
                    }
                    if (vps[i] == vps[j]) {
                        tieCount++;
                    }
                }

                if (!loss) {
                    double num = gameTypeSpecificWins.get(players[i].getClass().getName());
                    Double overall = overallWins.get(players[i].getClass().getName());
                    boolean trackOverall = (overall != null);
                    if (tieCount == 0) {
                        num += 1.0;
                        if (trackOverall) {
                            overall += 1.0;
                        }
                    } else {
                        num += 1.0 / (tieCount + 1);
                        if (trackOverall) {
                            overall += 1.0 / (tieCount + 1);
                        }
                    }
                    gameTypeSpecificWins.put(players[i].getClass().getName(), num);
                    if (trackOverall) {
                        overallWins.put(players[i].getClass().getName(), overall);
                    }
                }

                if (gameOver) {
                    Player player = players[i];
                    player.vps = vps[i];
                    player.win = !loss;
                    MoveContext context = new MoveContext(this, player);
                    broadcastEvent(new GameEvent(GameEvent.Type.GameOver, context));
                }

            }
            int index = 0;
            for (Player player : players) {
                int vp = vps[index++];
                Util.debug(player.getPlayerName() + ":Victory Points=" + vp, true);
                GameEvent event = new GameEvent(GameEvent.Type.VictoryPoints, null);
                event.setPlayer(player);
                event.setComment(":" + vp);
                broadcastEvent(event);
            }

            if (test) {
                // Compute game stats
                turnCountTotal += turnCount;
                for (int i = 0; i < vps.length; i++) {
                    vpTotal += vps[i];
                    numCardsTotal += players[i].getAllCards().size();
                }
            }
        }

        if (!debug && !interactive) {
            markWinner(gameTypeSpecificWins);
            printStats(gameTypeSpecificWins, numGames, gameType.toString());

            // Util.log("---------------------");
        }

        if (test) {
            // System.out.println();

            ArrayList<Card> gameCards = new ArrayList<Card>();
            for (Pile pile : piles.values()) {
                Card card = pile.card;
                if (!card.equals(Cards.copper) && !card.equals(Cards.silver) && !card.equals(Cards.gold) && !card.equals(Cards.platinum)
                    && !card.equals(Cards.estate) && !card.equals(Cards.duchy) && !card.equals(Cards.province) && !card.equals(Cards.colony)
                    && !card.equals(Cards.curse)) {
                    gameCards.add(card);
                }
            }

            GameStats stats = new GameStats();
            stats.gameType = gameType;
            stats.cards = gameCards.toArray(new Card[0]);
            stats.aveTurns = (int) (turnCountTotal / numGames);
            stats.aveNumCards = (int) (numCardsTotal / (numGames * numPlayers));
            stats.aveVictoryPoints = (int) (vpTotal / (numGames * numPlayers));

            gameTypeStats.add(stats);
        }

        FrameworkEvent frameworkEvent = new FrameworkEvent(FrameworkEvent.Type.GameTypeOver);
        frameworkEvent.setGameType(gameType);
        frameworkEvent.setGameTypeWins(gameTypeSpecificWins);
        FrameworkEventHelper.broadcastEvent(frameworkEvent);
    }

    private static void printStats(HashMap<String, Double> wins, int gameCount, String gameType) {
        if (test) {
            return;
        }

        StringBuilder sb = new StringBuilder();

        String s = gameType + ":";

        String start = "" + gameCount;

        if (gameCount > 1) {
            s = start + (gameType.equals("Types") ? " types " : " games ") + s;
        }

        if (!debug && !interactive) {
            while (s.length() < 30) { // (24 + start.length())) {
                s += " ";
            }
        }
        sb.append(s);
        String winner = null;
        double high = 0.0;
        Iterator<String> keyIter = wins.keySet().iterator();

        while (keyIter.hasNext()) {
            String className = keyIter.next();
            double num = wins.get(className);
            double val = Math.round((num * 100 / gameCount));
            if (val > high) {
                high = val;
                winner = className;
            }
        }

        keyIter = wins.keySet().iterator();
        while (keyIter.hasNext()) {
            String className = keyIter.next();
            double num = wins.get(className);

            String name;
            try {
                name = playerCache.get(className).getPlayerName();
            } catch (Exception e) {
                name = className;
            }

            double val = Math.round((num * 100 / gameCount));
            String numStr = "" + (int) val;
            while (numStr.length() < 3) {
                numStr += " ";
            }

            sb.append(" ");
            if (className.equals(winner)) {
                sb.append("*");
            } else {
                sb.append(" ");
            }
            sb.append(name + " %" + numStr);

            winStats.put(name, Integer.parseInt(numStr.trim()));
        }

        Util.log(sb.toString());
    }

    private static void printGameTypeStats() {
        Collections.sort(gameTypeStats, new Comparator<GameStats>() {
            public int compare(GameStats gs1, GameStats gs2) {
                return gs1.aveTurns - gs2.aveTurns;
            }
        });

        for (GameStats stats : gameTypeStats) {
            StringBuilder sb = new StringBuilder();
            sb.append(stats.gameType);
            if (stats.gameType.toString().length() < 8) {
                sb.append("\t");
            }
            if (stats.gameType.toString().length() < 16) {
                sb.append("\t");
            }
            sb.append("\tvp=" + stats.aveVictoryPoints + "\t\tcards=" + stats.aveNumCards + "\tturns=" + stats.aveTurns + "\t"
                + Util.cardArrayToString(stats.cards));
            Util.log(sb.toString());
        }
    }

    public int calculateLead(Player player) {
        int playerVictoryPoints = calculateVps(player);

        Integer otherHigh = null;

        int[] vps = calculateVps();
        for (int i = 0; i < vps.length; i++) {
            if (players[i].equals(player)) {
                continue;
            }

            if (otherHigh == null || vps[i] > otherHigh) {
                otherHigh = vps[i];
            }
        }

        return playerVictoryPoints - otherHigh;
    }

    private static int[] calculateVps() {
        int[] vps = new int[numPlayers];

        for (int i = 0; i < players.length; i++) {
            Player player = players[i];
            vps[i] = calculateVps(player);
        }

        return vps;
    }

    static int calculateVps(Player player) {
        ArrayList<Card> allCards = player.getAllCards();
        HashSet<String> distinctNames = new HashSet<String>();

        int vp = player.getVictoryTokens();
        int gardens = 0;
        int dukes = 0;
        int duchys = 0;
        int vineyards = 0;
        int fairgrounds = 0;
        int actionCards = 0;
        int silkRoads = 0;
        int totalCards = 0;
        int victoryCards = 0;
        
        for (Card card : allCards) {
            distinctNames.add(card.getName());
            totalCards++;
            if (card instanceof ActionCard) {
                actionCards++;
            }
            if (card instanceof VictoryCard) {
                victoryCards++;
                vp += ((VictoryCard) card).getVictoryPoints();
                if (card.equals(Cards.duchy)) {
                    duchys++;
                } else if (card.equals(Cards.duke)) {
                    dukes++;
                } else if (card.equals(Cards.gardens)) {
                    gardens++;
                } else if (card.equals(Cards.vineyard)) {
                    vineyards++;
                } else if (card.equals(Cards.fairgrounds)) {
                    fairgrounds++;
                } else if (card.equals(Cards.silkRoad)) {
                    silkRoads++;
                }
            }
        }

        vp += gardens * (totalCards / 10);
        vp += dukes * duchys;
        vp += vineyards * (actionCards / 3);
        vp += fairgrounds * 2 * (distinctNames.size() / 5);
        vp += silkRoads * (victoryCards / 4);
        
        return vp;
    }

    public boolean isValidAction(MoveContext context, Card action) {
        if (action == null) {
            return true;
        }

        if (!(action instanceof ActionCard)) {
            return false;
        }

        for (Card card : context.getPlayer().hand) {
            if (action.equals(card)) {
                return true;
            }
        }

        return false;
    }

    void playAction(MoveContext context, Card card) {
        Player player = context.getPlayer();
        player.hand.remove(card);
        boolean throneRoomed = (context.throneRoomsInEffect.size() > 0);

        GameEvent event = new GameEvent(GameEvent.Type.PlayingAction, (MoveContext) context);
        event.card = card;
        broadcastEvent(event);

        try {
            ((ActionCardImpl) card).play(this, (MoveContext) context);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (throneRoomed && !card.equals(Cards.treasureMap)) {
            Card throneRoom = context.throneRoomsInEffect.remove(context.throneRoomsInEffect.size() - 1);

            event = new GameEvent(GameEvent.Type.PlayingAction, (MoveContext) context);
            event.card = card;
            broadcastEvent(event);

            ((ActionCardImpl) card).play(this, (MoveContext) context);

            if (!((ActionCardImpl) card).dontAutoRecycleOnUse && !(card instanceof DurationCard)) {
                context.playedCards.add(throneRoom);
                context.playedCards.add(card);
            } else if (card instanceof DurationCard) {
                if (!(card instanceof ActionDurationCardImpl) || !((ActionDurationCardImpl) card).dontAutoRecycleOnUse) {
                    player.nextTurnCards.add(throneRoom);
                    player.nextTurnCards.add((DurationCard) card);
                }
            } else if (card != throneRoom)
                context.playedCards.add(throneRoom); // don't trash TR if TR'ed card was trashed

            return;
        }

        if (!((ActionCardImpl) card).dontAutoRecycleOnUse && !(card instanceof DurationCard)) {
            context.playedCards.add(card);
        }

        if (card instanceof DurationCard) {
            if (!(card instanceof ActionDurationCardImpl) || !((ActionDurationCardImpl) card).dontAutoRecycleOnUse) {
                // TODO pretty sure throne room should move up to here as well and effect
                // card...
                player.nextTurnCards.add((DurationCard) card);
            }
        }

        event = new GameEvent(GameEvent.Type.PlayedAction, (MoveContext) context);
        event.card = card;
        broadcastEvent(event);
    }

    
    public boolean isValidBuy(MoveContext context, Card card) {
        return isValidBuy(context, card, context.getCoinAvailableForBuy());
    }

    public boolean isValidBuy(MoveContext context, Card card, int gold) {
        if (card == null) {
            return true;
        }
        
        // TODO: Temp hack to prevent AI from buying possession, even though human player can, since it only half works 
        //       (AI will make decisions while possessed, but will try to make "good" ones)
        if(card.equals(Cards.possession) && context != null && context.getPlayer() != null && context.getPlayer().isAi()) {
            return false;
        }

        if (card.equals(Cards.curse) || card.isPrize()) {
            return false;
        }

        if (isPileEmpty(card)) {
            return false;
        }

        if (context.cantBuy.contains(card)) {
            return false;
        }

        if (context.copperPlayed && card.equals(Cards.grandMarket)) {
            return false;
        }

        int cost = card.getCost();
        
        // Adjust cost based on any cards played or card being bought
        if (context.quarriesPlayed > 0 && card instanceof ActionCard) {
            cost -= (context.quarriesPlayed * 2);
        } 
        if (card.equals(Cards.peddler)) {
            for(Card c : context.getPlayedCards()) {
                if(c instanceof ActionCard) {
                    cost -= 2;
                }
            }
        } 
        if (context.princessPlayed) {
            cost -= 2;
        } 
        cost -= context.highwaysPlayed;

        cost = (cost < 0 ? 0 : cost);
        int potions = 0;
        for (Card thisCard : context.getPlayedCards()) {
            if (thisCard instanceof TreasureCard && ((TreasureCard) thisCard).providesPotion()) {
                potions++;
            }
        }
        if (cost <= gold && (!card.costPotion() || potions > 0)) {
            return true;
        }

        return false;
    }

    void playBuy(MoveContext context, Card buy) {
        Card card = takeFromPileCheckTrader(buy, context);

        GameEvent event = new GameEvent(GameEvent.Type.BuyingCard, (MoveContext) context);
        event.card = card;
        event.newCard = true;
        broadcastEvent(event);

        int cost = card.getCost();
        
        // Adjust cost based on any cards played or card being bought
        if (context.quarriesPlayed > 0 && card instanceof ActionCard) {
            cost -= (context.quarriesPlayed * 2);
        } 
        if (card.equals(Cards.peddler)) {
            for(Card c : ((MoveContext) context).getPlayedCards()) {
                if(c instanceof ActionCard) {
                    cost -= 2;
                }
            }
        } 
        if (context.princessPlayed) {
            cost -= 2;
        } 
        cost -= context.highwaysPlayed;
        
        cost = (cost < 0 ? 0 : cost);
        context.gold -= cost;
        
        if (card.costPotion()) {
            context.potions--;
        } else if (!(card instanceof VictoryCard) && context.talismansPlayed > 0 && cost < 5) {
            for (int i = 0; i < context.talismansPlayed; i++) {
                context.getPlayer().gainNewCard(card, Cards.talisman, context);
            }
        }
    }

    public boolean buyWouldEndGame(Card card) {
        if (colonyInPlay && card.equals(Cards.colony)) {
            if (pileSize(card) <= 1) {
                return true;
            }
        }

        if (card.equals(Cards.province)) {
            if (pileSize(card) <= 1) {
                return true;
            }
        }
        
        if (emptyPiles() >= 2 && pileSize(card) <= 1) {
            return true;
        }

        return false;
    }

    private boolean checkGameOver() {
        if (colonyInPlay && isPileEmpty(Cards.colony)) {
            return true;
        }

        if (isPileEmpty(Cards.province)) {
            return true;
        }

        if (emptyPiles() >= 3) {
            return true;
        }

        return false;
    }

    public int emptyPiles() {
        int emptyPiles = 0;
        for (Pile pile : piles.values()) {
            if (pile.getCount() <= 0 && !pile.card.isPrize()) {
                emptyPiles++;
            }
        }
        return emptyPiles;
    }

    // TODO: all calls should use this but initial turn draws...
    boolean drawToHand(Player player, Card responsible) {
        return drawToHand(player, responsible, true);
    }

    boolean drawToHand(Player player, Card responsible, boolean showUI) {
        Card card = draw(player);
        if (card != null) {
            player.hand.add(card, showUI);
        }

        if (card != null && responsible != null) {
            Util.debug(player, responsible.getName() + " draw:" + card.getName(), true);
        }
        
        return (card != null);
    }

    Card draw(Player player) {
        if (player.discard.size() > 0 && player.getDeckSize() == 0) {
            replenishDeck(player);
        }

        if (player.getDeckSize() == 0) {
            return null;
        }

        return player.deck.remove(0);
    }

    public void replenishDeck(Player player) {
        player.replenishDeck();

        MoveContext context = new MoveContext(this, player);
        GameEvent event = new GameEvent(GameEvent.Type.DeckReplenished, context);
        broadcastEvent(event);
    }
    
    private boolean cardInPlay(Card c) {
        for (Game.Pile pile : piles.values()) {
            if(pile.card.equals(c)) {
                return true;
            }
        }
        return false;
    }

    private void handleShowEvent(GameEvent event) {
        if (showEvents.contains(event.getType())) {
            Player player = event.getPlayer();

            if (player == null || (player != null && !showPlayers.isEmpty() && !showPlayers.contains(player.getPlayerName()))) {
                return;
            }

            if (event.getType() == GameEvent.Type.TurnEnd) {
                System.out.println();
                return;
            }

            StringBuilder msg = new StringBuilder();
            msg.append(player.getPlayerName() + "::" + turnCount + ":" + event.getType());

            if (event.getType() == GameEvent.Type.BuyingCard) {
                msg.append(":" + event.getContext().getCoinAvailableForBuy() + " gold");
                if (event.getContext().getBuysLeft() > 0) {
                    msg.append(", buys remaining: " + event.getContext().getBuysLeft() + ")");
                }
            } else if (event.getType() == GameEvent.Type.PlayingAction || event.getType() == GameEvent.Type.TurnBegin
                || event.getType() == GameEvent.Type.NoBuy) {
                msg.append(":" + getHandString(player));
            } else if (event.attackedPlayer != null) {
                msg.append(":" + event.attackedPlayer.getPlayerName() + " with " + event.card.getName());
            }

            if (event.card != null) {
                msg.append(" -> " + event.card.getName());
            }

            if (event.getComment() != null) {
                msg.append(event.getComment());
            }

            System.out.println(msg.toString());
        }
    }

    @SuppressWarnings("unchecked")
    void init() throws ExitException {
        cardSequence = 1;
        baneCard = null;

        listeners.clear();

        gameListener = new GameEventListener() {

            public void gameEvent(GameEvent event) {
                handleShowEvent(event);

                if (event.getType() == GameEvent.Type.GameStarting || event.getType() == GameEvent.Type.GameOver) {
                    return;
                }
                
                if (event.getType() == GameEvent.Type.CardTrashed && event.context.getPossessedBy() != null) {
                    event.context.addToPossessedTrashPile(event.getCard());
                }

                if (event.getType() == GameEvent.Type.CardObtained || event.getType() == GameEvent.Type.BuyingCard) {
                    
                    MoveContext context = event.getContext();
                    
                    if(context != null && event.card instanceof VictoryCard) {
                        context.vpsGainedThisTurn += ((VictoryCard) event.card).getVictoryPoints();
                    }
                    
                    Player player = context.getPlayer();
                    if(context.getPossessedBy() != null) {
                        player = context.getPossessedBy();
                    }
                    
                    // See rules explanation of Tunnel for what commandedDiscard means.
                    boolean commandedDiscard = true;
                    if(event.getType() == GameEvent.Type.BuyingCard) {
                        commandedDiscard = false;
                    }
                    if(event.responsible != null) {
                        Card r = event.responsible;
                        if(r.equals(Cards.borderVillage) || 
                           r.equals(Cards.feast) ||
                           r.equals(Cards.remodel) ||
                           r.equals(Cards.swindler) ||
                           r.equals(Cards.ironworks) ||
                           r.equals(Cards.saboteur) ||
                           r.equals(Cards.upgrade) ||
                           r.equals(Cards.ambassador) ||
                           r.equals(Cards.smugglers) ||
                           r.equals(Cards.possession) ||
                           r.equals(Cards.talisman) ||
                           r.equals(Cards.expand) ||
                           r.equals(Cards.forge) ||
                           r.equals(Cards.remake) ||
                           r.equals(Cards.hornOfPlenty) ||
                           r.equals(Cards.jester) ||
                           r.equals(Cards.develop) ||
                           r.equals(Cards.haggler) ||
                           r.equals(Cards.workshop))
                        {
                            commandedDiscard = false;
                        }
                    }
                    boolean handled = false;
                    
                    //Not sure if this is exactly right for the Trader, but it seems to be based on detailed card explanation in the rules
                    //The handling for new cards is done before taking the card from the pile in a different method below.
                    if(!event.newCard) {
                        if(player.hand.contains(Cards.trader) && !event.card.equals(Cards.silver)) {
                            if((player).trader_shouldGainSilverInstead((MoveContext) context, event.card)) {
                                player.trash(event.card, Cards.trader, (MoveContext) context);
                                event.card = Cards.silver;
                                player.gainNewCard(Cards.silver, Cards.trader, context);
                                return;
                            }
                        }
                    }
                    
                    if (event.getPlayer() == players[playersTurn]) { // || (context.possessedBy != null && context.possessedBy == event.getPlayer())) {
                        cardsObtainedLastTurn[playersTurn].add(event.card);
                    }

                    if (player.hand.contains(Cards.watchTower)) {
                        WatchTowerOption choice = context.player.watchTower_chooseOption((MoveContext) context, event.card);
    
                        if (choice == WatchTowerOption.TopOfDeck) {
                            handled = true;
                            player.putOnTopOfDeck(event.card);
                        } else if (choice == WatchTowerOption.Trash) {
                            handled = true;
                            context.cardsTrashedThisTurn++;
                            GameEvent gameEvent = new GameEvent(GameEvent.Type.CardTrashed, context);
                            event.card = event.card;
                            event.responsible = Cards.watchTower;
                            broadcastEvent(gameEvent);
                        } 
                    }

                    if(!handled) {
                        if (context.royalSealPlayed && context.player.royalSeal_shouldPutCardOnDeck((MoveContext) context, event.card)) {
                            player.putOnTopOfDeck(event.card);
                        } else if (event.card.equals(Cards.nomadCamp)) {
                            player.putOnTopOfDeck(event.card);
                        } else if (event.responsible != null) {
                            Card r = event.responsible;
                            if (r.equals(Cards.bagOfGold) || r.equals(Cards.bureaucrat) || r.equals(Cards.seaHag) || r.equals(Cards.treasureMap) || r.equals(Cards.tournament) || r.equals(Cards.foolsGold)) {
                                player.putOnTopOfDeck(event.card);
                            } else if (r.equals(Cards.masquerade) || r.equals(Cards.tradingPost) || r.equals(Cards.mine) || r.equals(Cards.explorer) || r.equals(Cards.torturer)) {
                                player.hand.add(event.card);
                            } else if (r.equals(Cards.illGottenGains) && event.card.equals(Cards.copper)) {
                                player.hand.add(event.card);
                            } else {
                                player.discard(event.card, null, null, commandedDiscard);
                            }
                        } else {
                            player.discard(event.card, null, null, commandedDiscard);
                        }
                    } 
                    
                    if(event.card.equals(Cards.illGottenGains)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                MoveContext targetContext = new MoveContext(Game.this, targetPlayer);
                                // TODO: Is this really not an attack? Doesn't seem to be based on card, but not sure...
                                // targetPlayer.attacked(Cards.illGottenGains, targetContext);
                                targetPlayer.gainNewCard(Cards.curse, event.card, targetContext);
                            }
                        }
                    } else if(event.card.equals(Cards.province)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                int foolsGoldCount = 0;
                                // Check all of the cards, not just for existence, since there may be more than 1
                                for(Card c : targetPlayer.hand) {
                                    if(c.equals(Cards.foolsGold)) {
                                        foolsGoldCount++;
                                    }
                                }
                                
                                while(foolsGoldCount-- > 0) {
                                    MoveContext targetContext = new MoveContext(Game.this, targetPlayer);
                                    if((targetPlayer).foolsGold_shouldTrash(targetContext)) {
                                        targetPlayer.hand.remove(Cards.foolsGold);
                                        targetPlayer.trash(Cards.foolsGold, Cards.foolsGold, targetContext);
                                        targetPlayer.gainNewCard(Cards.gold, Cards.foolsGold, targetContext);
                                    }
                                }
                            }
                        }
                    } else if(event.card.equals(Cards.duchy)) {
                        if(((MoveContext) context).getCardsLeft(Cards.duchess) > 0) {
                            if((player).duchess_shouldGainBecauseOfDuchy((MoveContext) context)) {
                                player.gainNewCard(Cards.duchess, Cards.duchess, context);
                            }
                        }
                    } else if(event.card.equals(Cards.embassy)) {
                        for(Player targetPlayer : getPlayersInTurnOrder()) {
                            if(targetPlayer != player) {
                                MoveContext targetContext = new MoveContext(Game.this, targetPlayer);
                                targetPlayer.gainNewCard(Cards.silver, event.card, targetContext);
                            }
                        }
                    } else if(event.card.equals(Cards.cache)) {
                        for(int i=0; i < 2; i++) {
                            player.gainNewCard(Cards.copper, event.card, context);
                        }
                    } else if(event.card.equals(Cards.inn)) {
                        ArrayList<Card> cards = new ArrayList<Card>();
                        for(int i=player.discard.size() - 1; i >= 0; i--) {
                            Card c = player.discard.get(i);
                            if(c instanceof ActionCard) {
                                if((player).inn_shuffleCardBackIntoDeck(event.getContext(), (ActionCard) c)) {
                                    cards.add(c);
                                }
                            }
                        }
                        
                        if(cards.size() > 0) {
                            for(Card c : cards) {
                                player.discard.remove(c);
                                player.deck.add(c);
                            }
                            player.shuffleDeck();
                        }
                    } else if (event.card.equals(Cards.borderVillage)) {
                        boolean validCard = false;
                        
                        for(Card c : event.context.getCardsInPlay()) {
                            if(c.getCost() < 6 && !c.costPotion() && event.context.getCardsLeft(c) > 0) {
                                validCard = true;
                                break;
                            }
                        }
                        
                        if(validCard) {
                            Card card = context.player.borderVillage_cardToObtain((MoveContext) context);
                            if (card != null) {
                                if(card.getCost() < 6 && !card.costPotion()) {                            
                                    player.gainNewCard(card, event.card, (MoveContext) context);
                                }
                                else {
                                    Util.playerError(player, "Border Village returned invalid card, ignoring.");
                                }
                            }
                        }
                    } else if(event.card.equals(Cards.mandarin)) {
                        //TODO: ask for order
                        
                        ArrayList<Card> playedCards = ((MoveContext) context).getPlayedCards();
                        ArrayList<Card> treasureCardsInPlay = new ArrayList<Card>();
                        
                        for(Card c : playedCards) {
                            if(c instanceof TreasureCard) {
                                treasureCardsInPlay.add(c);
                            }
                        }
                        
                        for(Card c : treasureCardsInPlay) {
                            player.putOnTopOfDeck(c);
                            playedCards.remove(c);
                        }
                    } else if (event.card.equals(Cards.farmland) && event.getType() == GameEvent.Type.BuyingCard) {
                        if(event.player.getHand().size() > 0) {
                            Card cardToTrash = event.player.farmland_cardToTrash((MoveContext) context);
    
                            if (cardToTrash == null) {
                                Util.playerError(player, "Farmland did not return a card to trash, trashing random card.");
                                cardToTrash = Util.randomCard(player.hand);
                            }
                
                            int cost = -1;
                            boolean potion = false;
                            for (int i = 0; i < player.hand.size(); i++) {
                                Card playersCard = player.hand.get(i);
                                if (playersCard.equals(cardToTrash)) {
                                    cost = playersCard.getCost();
                                    potion = playersCard.costPotion();
                                    playersCard = player.hand.remove(i);
                
                                   player.trash(playersCard, event.card, (MoveContext) context);
                                    break;
                                }
                            }
                
                            if (cost == -1) {
                                Util.playerError(player, "Farmland returned invalid card, ignoring.");
                            }
                            else {
                                cost += 2;
        
                                boolean validCard = false;
                                
                                for(Card c : event.context.getCardsInPlay()) {
                                    if(c.getCost() == cost && c.costPotion() == potion && event.context.getCardsLeft(c) > 0) {
                                        validCard = true;
                                        break;
                                    }
                                }

                                if(validCard) {
                                    Card card = event.player.farmland_cardToObtain((MoveContext) context, cost, potion);
                                    if (card != null) {
                                        // check cost
                                        if (card.getCost() != cost || card.costPotion() != potion) {
                                            Util.playerError(event.player, "Farmland card to obtain returned an invalid card, ignoring.");
                                        }
                                        else
                                        {
                                            if(!player.gainNewCard(card, event.card, (MoveContext) context)) {
                                                Util.playerError(event.player, "Farmland new card is invalid, ignoring.");
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
                    
                    // Achievement check...
                    if(event.getType() == GameEvent.Type.BuyingCard && !player.achievementSingleCardFailed) {
                        if(isKingdomCard(event.getCard())) {
                            if(player.achievementSingleCardFirstKingdomCardBought == null) {
                                player.achievementSingleCardFirstKingdomCardBought = event.getCard();
                            }
                            else {
                                if(!player.achievementSingleCardFirstKingdomCardBought.equals(event.getCard())) {
                                    player.achievementSingleCardFailed = true;
                                    player.achievementSingleCardFirstKingdomCardBought = null;
                                }
                            }
                        }
                    }
                }
                
                if(event.getType() == GameEvent.Type.BuyingCard && event.card.equals(Cards.nobleBrigand)) {
                    nobleBrigandAttack(event.getContext(), event.getCard(), false);
                }

                if(event.getType() == GameEvent.Type.BuyingCard && event.getContext() != null) {
                    MoveContext context = event.getContext();
                    Player player = context.getPlayer();
                    if(context.getPossessedBy() != null) {
                        player = context.getPossessedBy();
                    }
                    
                    for(Card c : event.getContext().getPlayedCards()) {
                        if(c.equals(Cards.haggler)) {
                            int cost = event.getCard().getCost();
                            boolean potion = event.getCard().costPotion();
                            boolean found = false;
                            for(Card cardInPlay : event.getContext().getCardsInPlay()) {
                                if(cardInPlay.getCost() < cost && (potion || !cardInPlay.costPotion()) && event.getContext().getCardsLeft(cardInPlay) > 0 && !(cardInPlay instanceof VictoryCard)) {
                                    found = true;
                                    break;
                                }
                            }
                                
                            if(found) {
                                Card toGain = player.haggler_cardToObtain(event.getContext(), cost - 1, potion);
                                if(toGain != null) {
                                    if(toGain.getCost() >= cost || (!potion && toGain.costPotion()) || event.getContext().getCardsLeft(toGain) == 0 || (toGain instanceof VictoryCard)) { 
                                        Util.playerError(event.getPlayer(), "Invalid card returned from Haggler, ignoring.");
                                    }
                                    else {
                                        player.gainNewCard(toGain, Cards.haggler, event.getContext());
                                    }
                                }
                            }
                        }
                    }
                    
                    
                }

                boolean shouldShow = debug;
                if (!shouldShow && interactive) {
                    if (event.getType() != GameEvent.Type.TurnBegin && event.getType() != GameEvent.Type.TurnEnd
                        && event.getType() != GameEvent.Type.DeckReplenished && event.getType() != GameEvent.Type.GameStarting) {
                        shouldShow = true;
                    }
                }

                if (!showEvents.contains(event.getType()) && shouldShow) {
                    StringBuilder msg = new StringBuilder();
                    msg.append(event.getPlayer().getPlayerName() + ":" + event.getType());
                    if (event.card != null) {
                        msg.append(":" + event.card.getName());
                    }
                    if (event.attackedPlayer != null) {
                        msg.append(", attacking:" + event.attackedPlayer.getPlayerName());
                    }

                    if (event.getType() == GameEvent.Type.BuyingCard) {
                        msg.append(" (with gold: " + event.getContext().getCoinAvailableForBuy() + ", buys remaining: " + event.getContext().getBuysLeft());
                    }
                    Util.debug(msg.toString(), true);
                }

                if (interactive) {
                    if (event.getType() == GameEvent.Type.TurnEnd && !(event.getPlayer() instanceof InteractivePlayer)) {
                        Util.hitEnter(event.getContext());
                    }
                    if (event.getType() == GameEvent.Type.TurnBegin) {
                        Util.log("---");
                    }
                }
            }

        };

        piles.clear();
        embargos.clear();

//        addPile(Cards.platinum, 12);
        addPile(Cards.gold, 30);
        addPile(Cards.silver, 40);
        addPile(Cards.copper, 60);

        if (numPlayers == 2) {
            victoryCardPileSize = 8;
        }
//        addPile(Cards.colony);
        addPile(Cards.province);
        addPile(Cards.duchy);
        addPile(Cards.estate, victoryCardPileSize + 3 * numPlayers);

        int curseCount = 10;
        if (numPlayers == 3) {
            curseCount = 20;
        } else if (numPlayers == 4) {
            curseCount = 30;
        }

        addPile(Cards.curse, curseCount);

        unfoundCards.clear();
        int added = 0;

        if(cardsSpecifiedAtLaunch != null) {
            for(String s : cardsSpecifiedAtLaunch) {
                Card card = null;
                boolean bane = false;
                if(s.startsWith(BANE)) {
                    bane = true;
                    s = s.substring(BANE.length());
                }
                for(Card c : actionCards) {
                    if(c.getSafeName().equalsIgnoreCase(s)) {
                        card = c;
                        break;
                    }
                }
                if(card != null && bane) {
                    baneCard = card;
                }
                
                if(card != null
                    && !card.equals(Cards.possession)
                    && !card.equals(Cards.golem)
                    ) {
                    addPile(card);
                    added += 1;
                }
                else {
                    unfoundCards.add(s);
                    Util.log("ERROR::Could not find card:" + s);
                }
            }
            
            for(String s : unfoundCards) {
                Card c = null;
                int replacementCost = -1;
                if(s.equalsIgnoreCase("blackmarket")) {
                    replacementCost = 3;
                }
                else if(s.equalsIgnoreCase("envoy")) {
                    replacementCost = 4;
                }
                else if(s.equalsIgnoreCase("stash")) {
                    replacementCost = 5;
                }
                else if(s.equalsIgnoreCase("walledvillage")) {
                    replacementCost = 4;
                }
                else if(s.equalsIgnoreCase("possession")) {
                    // Not exact, since it requires potion as well, but good enough...
                    replacementCost = 6;
                }
                else if(s.equalsIgnoreCase("golem")) {
                    replacementCost = 4;
                }
                
                if(replacementCost != -1) {
                    ArrayList<Card> cardsWithSameCost = new ArrayList<Card>();
                    for(Card card : actionCards) {
                        if(card.getCost() == replacementCost && !cardInPlay(card)) {
                            cardsWithSameCost.add(card);
                        }
                    }
                    
                    if(cardsWithSameCost.size() > 0) {
                        c = cardsWithSameCost.get(rand.nextInt(cardsWithSameCost.size()));
                    }
                }
            
                while(c == null) {
                    c = actionCards.get(rand.nextInt(actionCards.size()));
                    if(cardInPlay(c)) {
                        c = null;
                    }
                }
                
                addPile(c);
                added += 1;
            }

            gameType = GameType.Random;
        }
        
        if (gameType == GameType.Random) {
                // ///////////////////////
                // To test specific cards.
    
                // addPile(Cards.warehouse);
                // added++;
                //
                // addPile(Cards.courtyard);
                // added++;
                //
                // addPile(Cards.fishingVillage);
                // added++;
    
                //
                // ///////////////////////
                while (added < 10) {
                    Card card;
                    do {
                        card = actionCards.get(rand.nextInt(actionCards.size()));
                        if (piles.get(card.getName()) != null) {
                            card = null;
                        }
                    } while (card == null);
    
                    addPile(card);
                    added++;
                }
            } else if (gameType == GameType.RandomBaseGame) {
                for (int i = 0; i < 10; i++) {
                    Card card;
                    do {
                        card = actionCardsBaseGame.get(rand.nextInt(actionCardsBaseGame.size()));
                        if (piles.get(card.getName()) != null) {
                            card = null;
                        }
                    } while (card == null);
    
                    addPile(card);
                }
            } else if (gameType == GameType.RandomIntrigue) {
                for (int i = 0; i < 10; i++) {
                    Card card;
                    do {
                        card = actionCardsIntrigue.get(rand.nextInt(actionCardsIntrigue.size()));
                        if (piles.get(card.getName()) != null) {
                            card = null;
                        }
                    } while (card == null);
    
                    addPile(card);
                }
            } else if (gameType == GameType.RandomSeaside) {
                for (int i = 0; i < 10; i++) {
                    Card card;
                    do {
                        card = actionCardsSeaside.get(rand.nextInt(actionCardsSeaside.size()));
                        if (piles.get(card.getName()) != null) {
                            card = null;
                        }
                    } while (card == null);
    
                    addPile(card);
                }
            } else if (gameType == GameType.RandomAlchemy) {
                for (int i = 0; i < 10; i++) {
                    Card card;
                    do {
                        card = actionCardsAlchemy.get(rand.nextInt(actionCardsAlchemy.size()));
                        if (piles.get(card.getName()) != null) {
                            card = null;
                        }
                    } while (card == null);
    
                    addPile(card);
                }
            } else if (gameType == GameType.RandomProsperity) {
                for (int i = 0; i < 10; i++) {
                    Card card;
                    do {
                        card = actionCardsProsperity.get(rand.nextInt(actionCardsProsperity.size()));
                        if (piles.get(card.getName()) != null) {
                            card = null;
                        }
                    } while (card == null);
    
                    addPile(card);
                }
            } else if (gameType == GameType.RandomCornucopia) {
                for (int i = 0; i < 10; i++) {
                    Card card;
                    do {
                        card = actionCardsCornucopia.get(rand.nextInt(actionCardsCornucopia.size()));
                        if (piles.get(card.getName()) != null) {
                            card = null;
                        }
                    } while (card == null);
    
                    addPile(card);
                }
            } else if (gameType == GameType.RandomHinterlands) {
                for (int i = 0; i < 10; i++) {
                    Card card;
                    do {
                        card = actionCardsHinterlands.get(rand.nextInt(actionCardsHinterlands.size()));
                        if (piles.get(card.getName()) != null) {
                            card = null;
                        }
                    } while (card == null);
    
                    addPile(card);
                }
            } else if (gameType == GameType.ForbiddenArts) {
                addPile(Cards.apprentice);
                addPile(Cards.familiar);
                addPile(Cards.possession);
                addPile(Cards.university);
                addPile(Cards.cellar);
                addPile(Cards.councilRoom);
                addPile(Cards.gardens);
                addPile(Cards.laboratory);
                addPile(Cards.thief);
                addPile(Cards.throneRoom);
            } else if (gameType == GameType.PotionMixers) {
                addPile(Cards.alchemist);
                addPile(Cards.apothecary);
                addPile(Cards.golem);
                addPile(Cards.herbalist);
                addPile(Cards.transmute);
                addPile(Cards.cellar);
                addPile(Cards.chancellor);
                addPile(Cards.festival);
                addPile(Cards.militia);
                addPile(Cards.smithy);
            } else if (gameType == GameType.ChemistryLesson) {
                addPile(Cards.alchemist);
                addPile(Cards.golem);
                addPile(Cards.philosophersStone);
                addPile(Cards.university);
                addPile(Cards.bureaucrat);
                addPile(Cards.market);
                addPile(Cards.moat);
                addPile(Cards.remodel);
                addPile(Cards.witch);
                addPile(Cards.woodcutter);
            } else if (gameType == GameType.Servants) {
                addPile(Cards.golem);
                addPile(Cards.possession);
                addPile(Cards.scryingPool);
                addPile(Cards.transmute);
                addPile(Cards.vineyard);
                addPile(Cards.conspirator);
                addPile(Cards.greatHall);
                addPile(Cards.minion);
                addPile(Cards.pawn);
                addPile(Cards.steward);
            } else if (gameType == GameType.SecretResearch) {
                addPile(Cards.familiar);
                addPile(Cards.herbalist);
                addPile(Cards.philosophersStone);
                addPile(Cards.university);
                addPile(Cards.bridge);
                addPile(Cards.masquerade);
                addPile(Cards.minion);
                addPile(Cards.nobles);
                addPile(Cards.shantyTown);
                addPile(Cards.torturer);
            } else if (gameType == GameType.PoolsToolsAndFools) {
                addPile(Cards.apothecary);
                addPile(Cards.apprentice);
                addPile(Cards.golem);
                addPile(Cards.scryingPool);
                addPile(Cards.baron);
                addPile(Cards.coppersmith);
                addPile(Cards.ironworks);
                addPile(Cards.nobles);
                addPile(Cards.tradingPost);
                addPile(Cards.wishingWell);
            } else if (gameType == GameType.FirstGame) {
                addPile(Cards.cellar);
                addPile(Cards.market);
                addPile(Cards.militia);
                addPile(Cards.mine);
                addPile(Cards.moat);
                addPile(Cards.remodel);
                addPile(Cards.smithy);
                addPile(Cards.village);
                addPile(Cards.woodcutter);
                addPile(Cards.workshop);
            } else if (gameType == GameType.BigMoney) {
                addPile(Cards.adventurer);
                addPile(Cards.bureaucrat);
                addPile(Cards.chancellor);
                addPile(Cards.chapel);
                addPile(Cards.feast);
                addPile(Cards.laboratory);
                addPile(Cards.market);
                addPile(Cards.mine);
                addPile(Cards.moneyLender);
                addPile(Cards.throneRoom);
            } else if (gameType == GameType.Interaction) {
                addPile(Cards.bureaucrat);
                addPile(Cards.chancellor);
                addPile(Cards.councilRoom);
                addPile(Cards.festival);
                addPile(Cards.library);
                addPile(Cards.militia);
                addPile(Cards.moat);
                addPile(Cards.spy);
                addPile(Cards.thief);
                addPile(Cards.village);
            } else if (gameType == GameType.SizeDistortion) {
                addPile(Cards.cellar);
                addPile(Cards.chapel);
                addPile(Cards.feast);
                addPile(Cards.gardens);
                addPile(Cards.laboratory);
                addPile(Cards.thief);
                addPile(Cards.village);
                addPile(Cards.witch);
                addPile(Cards.woodcutter);
                addPile(Cards.workshop);
            } else if (gameType == GameType.VillageSquare) {
                addPile(Cards.bureaucrat);
                addPile(Cards.cellar);
                addPile(Cards.festival);
                addPile(Cards.library);
                addPile(Cards.market);
                addPile(Cards.remodel);
                addPile(Cards.smithy);
                addPile(Cards.throneRoom);
                addPile(Cards.village);
                addPile(Cards.woodcutter);
            } else if (gameType == GameType.VictoryDance) {
                addPile(Cards.bridge);
                addPile(Cards.duke);
                addPile(Cards.greatHall);
                addPile(Cards.harem);
                addPile(Cards.ironworks);
                addPile(Cards.masquerade);
                addPile(Cards.nobles);
                addPile(Cards.pawn);
                addPile(Cards.scout);
                addPile(Cards.upgrade);
            } else if (gameType == GameType.SecretSchemes) {
                addPile(Cards.conspirator);
                addPile(Cards.harem);
                addPile(Cards.ironworks);
                addPile(Cards.pawn);
                addPile(Cards.saboteur);
                addPile(Cards.shantyTown);
                addPile(Cards.steward);
                addPile(Cards.swindler);
                addPile(Cards.tradingPost);
                addPile(Cards.tribute);
            } else if (gameType == GameType.BestWishes) {
                addPile(Cards.coppersmith);
                addPile(Cards.courtyard);
                addPile(Cards.masquerade);
                addPile(Cards.scout);
                addPile(Cards.shantyTown);
                addPile(Cards.steward);
                addPile(Cards.torturer);
                addPile(Cards.tradingPost);
                addPile(Cards.upgrade);
                addPile(Cards.wishingWell);
            } else if (gameType == GameType.Deconstruction) {
                addPile(Cards.bridge);
                addPile(Cards.miningVillage);
                addPile(Cards.remodel);
                addPile(Cards.saboteur);
                addPile(Cards.secretChamber);
                addPile(Cards.spy);
                addPile(Cards.swindler);
                addPile(Cards.thief);
                addPile(Cards.throneRoom);
                addPile(Cards.torturer);
            } else if (gameType == GameType.HandMadness) {
                addPile(Cards.bureaucrat);
                addPile(Cards.chancellor);
                addPile(Cards.councilRoom);
                addPile(Cards.courtyard);
                addPile(Cards.mine);
                addPile(Cards.militia);
                addPile(Cards.minion);
                addPile(Cards.nobles);
                addPile(Cards.steward);
                addPile(Cards.torturer);
            } else if (gameType == GameType.Underlings) {
                addPile(Cards.baron);
                addPile(Cards.cellar);
                addPile(Cards.festival);
                addPile(Cards.library);
                addPile(Cards.masquerade);
                addPile(Cards.minion);
                addPile(Cards.nobles);
                addPile(Cards.pawn);
                addPile(Cards.steward);
                addPile(Cards.witch);
            } else if (gameType == GameType.HighSeas) {
                addPile(Cards.bazaar);
                addPile(Cards.caravan);
                addPile(Cards.embargo);
                addPile(Cards.explorer);
                addPile(Cards.haven);
                addPile(Cards.island);
                addPile(Cards.lookout);
                addPile(Cards.pirateShip);
                addPile(Cards.smugglers);
                addPile(Cards.wharf);
            } else if (gameType == GameType.BuriedTreasure) {
                addPile(Cards.ambassador);
                addPile(Cards.cutpurse);
                addPile(Cards.fishingVillage);
                addPile(Cards.lighthouse);
                addPile(Cards.outpost);
                addPile(Cards.pearlDiver);
                addPile(Cards.tactician);
                addPile(Cards.treasureMap);
                addPile(Cards.warehouse);
                addPile(Cards.wharf);
            } else if (gameType == GameType.Shipwrecks) {
                addPile(Cards.ghostShip);
                addPile(Cards.merchantShip);
                addPile(Cards.nativeVillage);
                addPile(Cards.navigator);
                addPile(Cards.pearlDiver);
                addPile(Cards.salvager);
                addPile(Cards.seaHag);
                addPile(Cards.smugglers);
                addPile(Cards.treasury);
                addPile(Cards.warehouse);
            } else if (gameType == GameType.ReachForTomorrow) {
                addPile(Cards.adventurer);
                addPile(Cards.cellar);
                addPile(Cards.councilRoom);
                addPile(Cards.cutpurse);
                addPile(Cards.ghostShip);
                addPile(Cards.lookout);
                addPile(Cards.seaHag);
                addPile(Cards.spy);
                addPile(Cards.treasureMap);
                addPile(Cards.village);
            } else if (gameType == GameType.Repetition) {
                addPile(Cards.caravan);
                addPile(Cards.chancellor);
                addPile(Cards.explorer);
                addPile(Cards.festival);
                addPile(Cards.militia);
                addPile(Cards.outpost);
                addPile(Cards.pearlDiver);
                addPile(Cards.pirateShip);
                addPile(Cards.treasury);
                addPile(Cards.workshop);
            } else if (gameType == GameType.GiveAndTake) {
                addPile(Cards.ambassador);
                addPile(Cards.fishingVillage);
                addPile(Cards.haven);
                addPile(Cards.island);
                addPile(Cards.library);
                addPile(Cards.market);
                addPile(Cards.moneyLender);
                addPile(Cards.salvager);
                addPile(Cards.smugglers);
                addPile(Cards.witch);
            } else if (gameType == GameType.Beginners) {
                addPile(Cards.bank);
                addPile(Cards.countingHouse);
                addPile(Cards.expand);
                addPile(Cards.goons);
                addPile(Cards.monument);
                addPile(Cards.rabble);
                addPile(Cards.royalSeal);
                addPile(Cards.venture);
                addPile(Cards.watchTower);
                addPile(Cards.workersVillage);
            } else if (gameType == GameType.FriendlyInteractive) {
                addPile(Cards.bishop);
                addPile(Cards.city);
                addPile(Cards.contraband);
                addPile(Cards.forge);
                addPile(Cards.hoard);
                addPile(Cards.peddler);
                addPile(Cards.royalSeal);
                addPile(Cards.tradeRoute);
                addPile(Cards.vault);
                addPile(Cards.workersVillage);
            } else if (gameType == GameType.BigActions) {
                addPile(Cards.city);
                addPile(Cards.expand);
                addPile(Cards.grandMarket);
                addPile(Cards.kingsCourt);
                addPile(Cards.loan);
                addPile(Cards.mint);
                addPile(Cards.quarry);
                addPile(Cards.rabble);
                addPile(Cards.talisman);
                addPile(Cards.vault);
            } else if (gameType == GameType.BiggestMoney) {
                addPile(Cards.bank);
                addPile(Cards.grandMarket);
                addPile(Cards.mint);
                addPile(Cards.royalSeal);
                addPile(Cards.venture);
                addPile(Cards.adventurer);
                addPile(Cards.laboratory);
                addPile(Cards.mine);
                addPile(Cards.moneyLender);
                addPile(Cards.spy);
            } else if (gameType == GameType.TheKingsArmy) {
                addPile(Cards.expand);
                addPile(Cards.goons);
                addPile(Cards.kingsCourt);
                addPile(Cards.rabble);
                addPile(Cards.vault);
                addPile(Cards.bureaucrat);
                addPile(Cards.councilRoom);
                addPile(Cards.moat);
                addPile(Cards.spy);
                addPile(Cards.village);
            } else if (gameType == GameType.TheGoodLife) {
                addPile(Cards.contraband);
                addPile(Cards.countingHouse);
                addPile(Cards.hoard);
                addPile(Cards.monument);
                addPile(Cards.mountebank);
                addPile(Cards.bureaucrat);
                addPile(Cards.cellar);
                addPile(Cards.chancellor);
                addPile(Cards.gardens);
                addPile(Cards.village);
            } else if (gameType == GameType.PathToVictory) {
                addPile(Cards.bishop);
                addPile(Cards.countingHouse);
                addPile(Cards.goons);
                addPile(Cards.monument);
                addPile(Cards.peddler);
                addPile(Cards.baron);
                addPile(Cards.harem);
                addPile(Cards.pawn);
                addPile(Cards.shantyTown);
                addPile(Cards.upgrade);
            } else if (gameType == GameType.AllAlongTheWatchtower) {
                addPile(Cards.hoard);
                addPile(Cards.talisman);
                addPile(Cards.tradeRoute);
                addPile(Cards.vault);
                addPile(Cards.watchTower);
                addPile(Cards.bridge);
                addPile(Cards.greatHall);
                addPile(Cards.miningVillage);
                addPile(Cards.pawn);
                addPile(Cards.torturer);
            } else if (gameType == GameType.LuckySeven) {
                addPile(Cards.bank);
                addPile(Cards.expand);
                addPile(Cards.forge);
                addPile(Cards.kingsCourt);
                addPile(Cards.vault);
                addPile(Cards.bridge);
                addPile(Cards.coppersmith);
                addPile(Cards.swindler);
                addPile(Cards.tribute);
                addPile(Cards.wishingWell);
            } else if (gameType == GameType.BountyOfTheHunt) {
                addPile(Cards.harvest);
                addPile(Cards.hornOfPlenty);
                addPile(Cards.huntingParty);
                addPile(Cards.menagerie);
                addPile(Cards.tournament);
                addPile(Cards.cellar);
                addPile(Cards.festival);
                addPile(Cards.militia);
                addPile(Cards.moneyLender);
                addPile(Cards.smithy);
            } else if (gameType == GameType.BadOmens) {
                addPile(Cards.fortuneTeller);
                addPile(Cards.hamlet);
                addPile(Cards.hornOfPlenty);
                addPile(Cards.jester);
                addPile(Cards.remake);
                addPile(Cards.adventurer);
                addPile(Cards.bureaucrat);
                addPile(Cards.laboratory);
                addPile(Cards.spy);
                addPile(Cards.throneRoom);
            } else if (gameType == GameType.TheJestersWorkshop) {
                addPile(Cards.fairgrounds);
                addPile(Cards.farmingVillage);
                addPile(Cards.horseTraders);
                addPile(Cards.jester);
                addPile(Cards.youngWitch);
                addPile(Cards.feast);
                addPile(Cards.laboratory);
                addPile(Cards.market);
                addPile(Cards.remodel);
                addPile(Cards.workshop);
                addPile(baneCard = Cards.chancellor);
            } else if (gameType == GameType.LastLaughs) {
                addPile(Cards.farmingVillage);
                addPile(Cards.harvest);
                addPile(Cards.horseTraders);
                addPile(Cards.huntingParty);
                addPile(Cards.jester);
                addPile(Cards.minion);
                addPile(Cards.nobles);
                addPile(Cards.pawn);
                addPile(Cards.steward);
                addPile(Cards.swindler);
            } else if (gameType == GameType.TheSpiceOfLife) {
                addPile(Cards.fairgrounds);
                addPile(Cards.hornOfPlenty);
                addPile(Cards.remake);
                addPile(Cards.tournament);
                addPile(Cards.youngWitch);
                addPile(Cards.coppersmith);
                addPile(Cards.courtyard);
                addPile(Cards.greatHall);
                addPile(Cards.miningVillage);
                addPile(Cards.tribute);
                addPile(baneCard = Cards.wishingWell);
            } else if (gameType == GameType.SmallVictories) {
                addPile(Cards.fortuneTeller);
                addPile(Cards.hamlet);
                addPile(Cards.huntingParty);
                addPile(Cards.remake);
                addPile(Cards.tournament);
                addPile(Cards.conspirator);
                addPile(Cards.duke);
                addPile(Cards.greatHall);
                addPile(Cards.harem);
                addPile(Cards.pawn);
            } else if (gameType == GameType.HinterlandsIntro) {
                addPile(Cards.cache);
                addPile(Cards.crossroads);
                addPile(Cards.develop);
                addPile(Cards.haggler);
                addPile(Cards.jackOfAllTrades);
                addPile(Cards.margrave);
                addPile(Cards.nomadCamp);
                addPile(Cards.oasis);
                addPile(Cards.spiceMerchant);
                addPile(Cards.stables);
            } else if (gameType == GameType.FairTrades) {
                addPile(Cards.borderVillage);
                addPile(Cards.cartographer);
                addPile(Cards.develop);
                addPile(Cards.duchess);
                addPile(Cards.farmland);
                addPile(Cards.illGottenGains);
                addPile(Cards.nobleBrigand);
                addPile(Cards.silkRoad);
                addPile(Cards.stables);
                addPile(Cards.trader);
            } else if (gameType == GameType.Bargains) {
                addPile(Cards.borderVillage);
                addPile(Cards.cache);
                addPile(Cards.duchess);
                addPile(Cards.foolsGold);
                addPile(Cards.haggler);
                addPile(Cards.highway);
                addPile(Cards.nomadCamp);
                addPile(Cards.scheme);
                addPile(Cards.spiceMerchant);
                addPile(Cards.trader);
            } else if (gameType == GameType.Gambits) {
                addPile(Cards.cartographer);
                addPile(Cards.crossroads);
                addPile(Cards.embassy);
                addPile(Cards.inn);
                addPile(Cards.jackOfAllTrades);
                addPile(Cards.mandarin);
                addPile(Cards.nomadCamp);
                addPile(Cards.oasis);
                addPile(Cards.oracle);
                addPile(Cards.tunnel);
            } else if (gameType == GameType.HighwayRobbery) {
                addPile(Cards.cellar);
                addPile(Cards.library);
                addPile(Cards.moneyLender);
                addPile(Cards.throneRoom);
                addPile(Cards.workshop);
                addPile(Cards.highway);
                addPile(Cards.inn);
                addPile(Cards.margrave);
                addPile(Cards.nobleBrigand);
                addPile(Cards.oasis);
            } else if (gameType == GameType.AdventuresAbroad) {
                addPile(Cards.adventurer);
                addPile(Cards.chancellor);
                addPile(Cards.festival);
                addPile(Cards.laboratory);
                addPile(Cards.remodel);
                addPile(Cards.crossroads);
                addPile(Cards.farmland);
                addPile(Cards.foolsGold);
                addPile(Cards.oracle);
                addPile(Cards.spiceMerchant);
            } else if (gameType == GameType.MoneyForNothing) {
                addPile(Cards.coppersmith);
                addPile(Cards.greatHall);
                addPile(Cards.pawn);
                addPile(Cards.shantyTown);
                addPile(Cards.torturer);
                addPile(Cards.cache);
                addPile(Cards.cartographer);
                addPile(Cards.jackOfAllTrades);
                addPile(Cards.silkRoad);
                addPile(Cards.tunnel);
            } else if (gameType == GameType.TheDukesBall) {
                addPile(Cards.conspirator);
                addPile(Cards.duke);
                addPile(Cards.harem);
                addPile(Cards.masquerade);
                addPile(Cards.upgrade);
                addPile(Cards.duchess);
                addPile(Cards.haggler);
                addPile(Cards.inn);
                addPile(Cards.nobleBrigand);
                addPile(Cards.scheme);
            } else if (gameType == GameType.Travelers) {
                addPile(Cards.cutpurse);
                addPile(Cards.island);
                addPile(Cards.lookout);
                addPile(Cards.merchantShip);
                addPile(Cards.warehouse);
                addPile(Cards.cartographer);
                addPile(Cards.crossroads);
                addPile(Cards.farmland);
                addPile(Cards.silkRoad);
                addPile(Cards.stables);
            } else if (gameType == GameType.Diplomacy) {
                addPile(Cards.ambassador);
                addPile(Cards.bazaar);
                addPile(Cards.caravan);
                addPile(Cards.embargo);
                addPile(Cards.smugglers);
                addPile(Cards.embassy);
                addPile(Cards.farmland);
                addPile(Cards.illGottenGains);
                addPile(Cards.nobleBrigand);
                addPile(Cards.trader);
            } else if (gameType == GameType.SchemesAndDreams) {
                addPile(Cards.apothecary);
                addPile(Cards.apprentice);
                addPile(Cards.herbalist);
                addPile(Cards.philosophersStone);
                addPile(Cards.transmute);
                addPile(Cards.duchess);
                addPile(Cards.foolsGold);
                addPile(Cards.illGottenGains);
                addPile(Cards.jackOfAllTrades);
                addPile(Cards.scheme);
            } else if (gameType == GameType.WineCountry) {
                addPile(Cards.apprentice);
                addPile(Cards.familiar);
                addPile(Cards.golem);
                addPile(Cards.university);
                addPile(Cards.vineyard);
                addPile(Cards.crossroads);
                addPile(Cards.farmland);
                addPile(Cards.haggler);
                addPile(Cards.highway);
                addPile(Cards.nomadCamp);
            } else if (gameType == GameType.InstantGratification) {
                addPile(Cards.bishop);
                addPile(Cards.expand);
                addPile(Cards.hoard);
                addPile(Cards.mint);
                addPile(Cards.watchTower);
                addPile(Cards.farmland);
                addPile(Cards.haggler);
                addPile(Cards.illGottenGains);
                addPile(Cards.nobleBrigand);
                addPile(Cards.trader);
            } else if (gameType == GameType.TreasureTrove) {
                addPile(Cards.bank);
                addPile(Cards.monument);
                addPile(Cards.royalSeal);
                addPile(Cards.tradeRoute);
                addPile(Cards.venture);
                addPile(Cards.cache);
                addPile(Cards.develop);
                addPile(Cards.foolsGold);
                addPile(Cards.illGottenGains);
                addPile(Cards.mandarin);
            } else if (gameType == GameType.BlueHarvest) {
                addPile(Cards.hamlet);
                addPile(Cards.hornOfPlenty);
                addPile(Cards.horseTraders);
                addPile(Cards.jester);
                addPile(Cards.tournament);
                addPile(Cards.foolsGold);
                addPile(Cards.mandarin);
                addPile(Cards.nobleBrigand);
                addPile(Cards.trader);
                addPile(Cards.tunnel);
            } else if (gameType == GameType.TravelingCircus) {
                addPile(Cards.fairgrounds);
                addPile(Cards.farmingVillage);
                addPile(Cards.huntingParty);
                addPile(Cards.jester);
                addPile(Cards.menagerie);
                addPile(Cards.borderVillage);
                addPile(Cards.embassy);
                addPile(Cards.foolsGold);
                addPile(Cards.nomadCamp);
                addPile(Cards.oasis);
            }

        double chanceForPlatColony = 0;
        
        if(alwaysIncludePlatColony) {
            platInPlay = true;
            colonyInPlay = true;
        } else {
            platInPlay = false;
            colonyInPlay = false;
            
            for(Pile pile : piles.values()) {
                if(pile != null && pile.card != null && pile.card.getExpansion() != null && pile.card.getExpansion().equals("Prosperity")) {
                    chanceForPlatColony += 0.1;
                }
            }
            
            if(rand.nextDouble() < chanceForPlatColony) {
                platInPlay = true;
                colonyInPlay = true;
            }
        }
        
        for(String s : unfoundCards) {
            if(s.equalsIgnoreCase("platinum")) {
                platInPlay = true;
                platColonyPassedIn = true;
            }
            else if(s.equalsIgnoreCase("colony")) {
                colonyInPlay = true;
                platColonyPassedIn = true;
            }
        }
        
        
//        MoveContext context = new MoveContext(this, null);
//        context.message = "" + ((int) chance * 100) + "% - " + (platInPlay?"Yes":"No");
//        broadcastEvent(new GameEvent(GameEvent.Type.PlatAndColonyChance, context));

        MoveContext context = null;
        
        if(platInPlay) {
            addPile(Cards.platinum, 12);
        }
        if(colonyInPlay)
            addPile(Cards.colony);
        

        if (piles.containsKey(Cards.youngWitch.getName()) && baneCard == null) {
            Card card = null;
            ArrayList<Card> cardList = gameType == GameType.RandomCornucopia ? actionCardsCornucopia : actionCards;
            boolean avail = true;
            
            if(gameType == GameType.RandomCornucopia) {
                avail = false;
                for(Card c : cardList) {
                    if (piles.get(c.getName()) == null && c.getCost() <= 3 && c.getCost() >= 2 && !c.costPotion()) {
                        avail = true;
                        break;
                    }
                }
                if(!avail) {
                    do {
                        card = cardList.get(rand.nextInt(cardList.size()));
                        // find a bane card that has already been added
                        if (piles.get(card.getName()) == null || card.getCost() > 3 || card.getCost() < 2 || card.costPotion()) {
                            card = null;
                        }
                    } while (card == null);

                    Card cardToAdd = null;
                    // now add another card
                    do {
                        cardToAdd = cardList.get(rand.nextInt(cardList.size()));
                        if (piles.get(cardToAdd.getName()) != null) {
                            cardToAdd = null;
                        }
                    } while (cardToAdd == null);
                    addPile(cardToAdd);
                }
            }

            if(avail) {
                do {
                    card = cardList.get(rand.nextInt(cardList.size()));
                    if (piles.get(card.getName()) != null || card.getCost() > 3 || card.getCost() < 2 || card.costPotion()) {
                        card = null;
                    }
                } while (card == null);
                addPile(card);
            }

            baneCard = card;
        }
        
        // Add the potion if there are any cards that need them.
        for (Pile pile : piles.values()) {
            if (pile.card.costPotion()) {
                addPile(Cards.potion, 30);
                break;
            }
        }
        
        if (piles.containsKey(Cards.tournament.getName()) && !piles.containsKey(Cards.bagOfGold.getName())) {
            addPile(Cards.bagOfGold, 1);
            addPile(Cards.diadem, 1);
            addPile(Cards.followers, 1);
            addPile(Cards.princess, 1);
            addPile(Cards.trustySteed, 1);
        }

        boolean oldDebug = debug;
        if (!debug && !showEvents.isEmpty()) {
            debug = true;
        }
        Util.debug("");
        Util.debug("Actions in Play", true);
        Util.debug("---------------", true);
        // Util.debug(Cards.copper.getName());
        // Util.debug(Cards.silver.getName());
        // Util.debug(Cards.gold.getName());
        // Util.debug(Cards.estate.getName());
        // Util.debug(Cards.duchy.getName());
        // Util.debug(Cards.province.getName());
        // Util.debug(Cards.curse.getName());
        // Util.debug("");
        //

        int cost = 0;
        while (cost < 10) {
            for (Pile pile : piles.values()) {
                if (!nonKingdomCards.contains(pile.card)) {
                    if (pile.card.getCost() == cost) {
                        Util.debug(Util.getShortText(pile.card), true);
                    }
                }
            }

            cost++;
        }
        if (baneCard != null) {
            Util.debug("(Bane) " + Util.getShortText(baneCard), true);
        }
        Util.debug("");
        debug = oldDebug;

        players = new Player[numPlayers];
        cardsObtainedLastTurn = new ArrayList[numPlayers];
        for (int i = 0; i < numPlayers; i++) {
            cardsObtainedLastTurn[i] = new ArrayList<Card>();
        }

        ArrayList<String[]> randomize = new ArrayList<String[]>();

        while (!playerClassesAndJars.isEmpty()) {
            randomize.add(playerClassesAndJars.remove(rand.nextInt(playerClassesAndJars.size())));
        }

        playerClassesAndJars = randomize;
        playerCache.clear();

        for (int i = 0; i < numPlayers; i++) {
            try {
                String[] playerStartupInfo = playerClassesAndJars.get(i);
                if (playerStartupInfo[1] == null) {
                    players[i] = (Player) Class.forName(playerStartupInfo[0]).newInstance();
                } else {
                    String key = playerStartupInfo[0] + "::" + playerStartupInfo[1];
                    // players[i] = cachedPlayers.get(key);
                    //
                    // if(players[i] == null) {
                    // URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(classAndJar[1]) });
                    // players[i] = classLoader.loadClass(classAndJar[0]).newInstance();
                    // cachedPlayers.put(key, players[i]);
                    // }

                    Class<?> playerClass = cachedPlayerClasses.get(key);

                    if (playerClass == null) {
                        URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(playerStartupInfo[1]) });
                        playerClass = classLoader.loadClass(playerStartupInfo[0]);
                        cachedPlayerClasses.put(key, playerClass);
                    }

                    players[i] = (Player) playerClass.newInstance();
                }
                if(playerStartupInfo[2] != null) {
                    players[i].setName(playerStartupInfo[2]);
                }
                String options = playerStartupInfo[3];
                if(options.contains("q") && players[i] instanceof InteractivePlayer) {
                    ((InteractivePlayer) players[i]).quickPlay = true;
                }
                playerCache.put(playerStartupInfo[0], players[i]);
            } catch (Exception e) {
                Util.log(e);
                throw new ExitException();
            }

            Player player = players[i];
            player.game = this;
            players[i] = player;
            player.playerNumber = i;

            // Interactive player needs this called once for each player on startup so internal counts work
            // properly.
            players[i].getPlayerName();

            context = new MoveContext(this, players[i]);
            player.initCards();
            players[i].newGame(context);

            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);
            player.discard(takeFromPile(Cards.copper), null, null);

            player.discard(takeFromPile(Cards.estate), null, null);
            player.discard(takeFromPile(Cards.estate), null, null);
            player.discard(takeFromPile(Cards.estate), null, null);

            while (player.hand.size() < 5) {
                drawToHand(players[i], null, false);
            }
        }

        String unfoundCardText = "";
        if (unfoundCards != null && unfoundCards.size() > 0) {
            unfoundCardText += "\n";
            String cardList = "";
            boolean first = true;
            for (String s : unfoundCards) {
                if (first) {
                    first = false;
                } else {
                    cardList += "\n";
                }
                cardList += s;
            }
            cardList += "\n\n";
            unfoundCardText += "The following cards are not \navailable, so replacements \nhave been used:\n" + cardList;
        }
        
        for (int i = 0; i < numPlayers; i++) {
            context = new MoveContext(this, players[i]);
            String s = "";
            if(!alwaysIncludePlatColony && !platColonyPassedIn) {
                s += "Chance for Platinum/Colony\n   " + (Math.round(chanceForPlatColony * 100)) + "% ... " + (platInPlay?"included":"not included" + "\n");
            }
            if(baneCard != null) {
                s += "Bane card: " + baneCard.getName() + "\n";
            }
            s += unfoundCardText;
            context.message = s;
            broadcastEvent(new GameEvent(GameEvent.Type.GameStarting, context));
        }
        
//        context = new MoveContext(this, null);
//        context.message = "" + ((int) chance * 100) + "% - " + (platInPlay?"Yes":"No");
//        broadcastEvent(new GameEvent(GameEvent.Type.PlatAndColonyChance, context));

        gameOver = false;

        if (showUI) {
//            ui = new UI();
//            ui.init(this);
//            listeners.add(ui);
        }
    }

    boolean hasMoat(Player player) {
        for (Card card : player.hand) {
            if (card.equals(Cards.moat)) {
                return true;
            }
        }

        return false;
    }

    boolean hasLighthouse(Player player) {
        for (Card card : player.nextTurnCards) {
            if (card.equals(Cards.lighthouse)) {
                return true;
            }
        }

        return false;
    }

    // TODO privatize
    public int getEmbargos(String name) {
        Integer count = embargos.get(name);
        if (count == null) {
            return 0;
        }

        return count;
    }

    void addEmbargo(String name) {
        Pile pile = piles.get(name);
        // Don't embargo cards not in the game
        if (pile == null) {
            return;
        }

        // TODO ok to embargo any card (gold, victory, etc.)?
        Integer count = embargos.get(name);
        if (count == null) {
            embargos.put(name, 1);
        } else {
            embargos.put(name, count + 1);
        }
    }

    // Only is valid for cards in play...
    Card readCard(String name) {
        Pile pile = piles.get(name);
        if (pile == null || pile.getCount() <= 0) {
            return null;
        }
        return pile.card;
    }

    public Card takeFromPile(Card card) {
        Pile pile = piles.get(card.getName());
        if (pile == null || pile.getCount() <= 0) {
            return null;
        }

        Card thisCard = pile.removeCard();

        return thisCard;
    }
    
    public Card takeFromPileCheckTrader(Card cardToGain, MoveContext context) {
        if(context.getPlayer().hand.contains(Cards.trader) && !cardToGain.equals(Cards.silver)) {
            if(context.player.trader_shouldGainSilverInstead((MoveContext) context, cardToGain)) {
                cardToGain = Cards.silver;
            }
        }
        
        return takeFromPile(cardToGain);
    }

    public int pileSize(Card card) {
        Pile pile = piles.get(card.getName());
        if (pile == null) {
            return -1;
        }

        return pile.getCount();
    }

    boolean isPileEmpty(Card card) {
        return pileSize(card) <= 0;
    }

    boolean isCardInGame(Card card) {
        Pile pile = piles.get(card.getName());
        if (pile == null) {
            return false;
        }
        return true;
    }

    void addPile(Card card) {
        if (card instanceof VictoryCard) {
            addPile(card, victoryCardPileSize);
        } else {
            addPile(card, kingdomCardPileSize);
        }
    }

    void addPile(Card card, int count) {
        Pile pile = new Pile(card, count);
        piles.put(card.getName(), pile);
    }
    
    void addPile(Pile pile) {
        piles.put(pile.card.getName(), pile);
    }

    public Card[] getCardsObtainedByLastPlayer() {
        int playerOnRight = playersTurn - 1;
        if (playerOnRight < 0) {
            playerOnRight = numPlayers - 1;
        }

        return cardsObtainedLastTurn[playerOnRight].toArray(new Card[0]);
    }

    public Player getNextPlayer() {
        int next = playersTurn + 1;
        if (next >= numPlayers) {
            next = 0;
        }

        return players[next];
    }

    public Player[] getPlayersInTurnOrder() {
        Player[] ordered = new Player[numPlayers];

        int at = playersTurn;
        for (int i = 0; i < numPlayers; i++) {
            ordered[i] = players[at];
            at++;
            if (at >= numPlayers) {
                at = 0;
            }
        }

        return ordered;
    }

    public void broadcastEvent(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.gameEvent(event);
        }
        // notify this class' listener last for proper action/logging order
        if(gameListener != null)
            gameListener.gameEvent(event);
    }

    String getHandString(Player player) {
        String handString = null;
        Card[] hand = player.getHand().toArray();
        Arrays.sort(hand, new CardCostComparator());
        for (Card card : hand) {
            if (card == null) {
                continue;
            }
            if (handString == null) {
                handString = card.getName();
            } else {
                handString += ", " + card.getName();
            }
        }

        return handString;
    }
    
    public boolean playerShouldSelectCoinsToPlay(MoveContext context, CardList cards) {
        if(!quickPlay) {
            return true;
        }
        
        if(cards == null) {
            return false;
        }
        
        for(Card card : cards) {
            if (
                card.equals(Cards.philosophersStone) ||
                card.equals(Cards.bank) ||
                card.equals(Cards.contraband) ||
                card.equals(Cards.loan) ||
                card.equals(Cards.quarry) ||
                card.equals(Cards.talisman) ||
                card.equals(Cards.hornOfPlenty) ||
                card.equals(Cards.diadem)
                ) 
            {
                return true;
            }
        }
        
        return false;
    }

    static boolean checkForInteractive() throws ExitException {
        for (int i = 0; i < numPlayers; i++) {
            Player player;
            try {
                String[] classAndJar = playerClassesAndJars.get(i);
                if (classAndJar[1] == null) {
                    player = (Player) Class.forName(classAndJar[0]).newInstance();
                } else {
                    URLClassLoader classLoader = new URLClassLoader(new URL[] { new URL(classAndJar[1]) });
                    player = (Player) classLoader.loadClass(classAndJar[0]).newInstance();
                }
                if(classAndJar[2] != null) {
                    player.setName(classAndJar[2]);
                }
            } catch (Exception e) {
                Util.log(e);
                throw new ExitException();
            }

            if (player instanceof InteractivePlayer) {
                interactive = true;
            }
        }

        if (interactive) {
            Util.log("");
            Util.log("Enter \"`\" at anytime when prompted for keyboard input during the game to exit.");
            Util.log("Enter \"/\" to turn Quick Play on and off");
            Util.log("Enter \".\" to see additional game details (card texts, number of cards left, etc.)");
        }

        return interactive;
    }

    public static class Pile {
        public Card card;
        private ArrayList<Card> cards = new ArrayList<Card>();

        public Pile(Card card, int count) {
            this.card = card;

            for (int i = 1; i <= count; i++) {
                // TODO: put in checks to make sure template card is never "put into play".
                CardImpl thisCard = ((CardImpl) card).instantiate();
                cards.add(thisCard);
            }
        }

        /**
         * @return the count
         */
        public int getCount() {
            return cards.size();
        }

        public void addCard(Card card) {
            cards.add(card);
        }

        public Card removeCard() {
            return cards.remove(cards.size() - 1);
        }
    }
    
    public boolean isKingdomCard(Card c) {
        return !nonKingdomCards.contains(c);
    }
    
    public void nobleBrigandAttack(MoveContext moveContext, Card nobleBrigandCard, boolean defensible) {
        MoveContext context = moveContext;
        Player player = context.getPlayer();
        ArrayList<TreasureCard> trashed = new ArrayList<TreasureCard>();
        boolean[] gainCopper = new boolean[getPlayersInTurnOrder().length];

        int i = 0;
        for (Player targetPlayer : getPlayersInTurnOrder()) {
            // Hinterlands card details in the rules states that noble brigand is not defensible when triggered from a buy
            if (targetPlayer != player && (!defensible || !Util.isDefendedFromAttack(this, targetPlayer, nobleBrigandCard))) {
                targetPlayer.attacked(nobleBrigandCard, moveContext);
                MoveContext targetContext = new MoveContext(this, targetPlayer);
                boolean treasureRevealed = false;
                ArrayList<TreasureCard> silverOrGold = new ArrayList<TreasureCard>();

                for (int j = 0; j < 2; j++) {
                    Card card = draw(targetPlayer);
                    if(card == null) {
                        break;
                    }
                    targetPlayer.reveal(card, nobleBrigandCard, targetContext);

                    if (card instanceof TreasureCard) {
                        treasureRevealed = true;
                    }
                    
                    if(card.equals(Cards.silver) || card.equals(Cards.gold)) {
                        silverOrGold.add((TreasureCard) card);
                    } else {
                        targetPlayer.discard(card, nobleBrigandCard, targetContext);
                    }
                }
                
                if(!treasureRevealed) {
                    gainCopper[i] = true;
                }

                TreasureCard cardToTrash = null;

                if (silverOrGold.size() == 1) {
                    cardToTrash = silverOrGold.get(0);
                } else if (silverOrGold.size() == 2) {
                    if (silverOrGold.get(0).equals(silverOrGold.get(1))) {
                        cardToTrash = silverOrGold.get(0);
                    } else {
                        moveContext.attackedPlayer = targetPlayer;
                        cardToTrash = (player).nobleBrigand_silverOrGoldToTrash(moveContext, silverOrGold.toArray(new TreasureCard[]{}));
                        moveContext.attackedPlayer = null;
                    }

                    for (TreasureCard c : silverOrGold) {
                        if (!c.equals(cardToTrash)) {
                            targetPlayer.discard(c, nobleBrigandCard, targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, nobleBrigandCard, targetContext);
                    trashed.add(cardToTrash);
                }
            }
            i++;
        }

        i = 0;
        for(Player targetPlayer : getPlayersInTurnOrder()) {
            if(gainCopper[i]) {
                MoveContext targetContext = new MoveContext(this, targetPlayer);
                targetPlayer.gainNewCard(Cards.copper, nobleBrigandCard, targetContext);
            }
            i++;
        }
        
        if (trashed.size() > 0) {
            for (Card c : trashed) {
                player.gainCardAlreadyInPlay(c, nobleBrigandCard, moveContext);
            }
        }
    }
}
