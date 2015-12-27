package com.vdom.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.vdom.api.Card;

public class Cards {
    public static HashSet<Card> nonKingdomCards = new HashSet<Card>();
    public static ArrayList<Card> actionCardsBaseGame = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsIntrigue = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsSeaside = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsAlchemy = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsProsperity = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsCornucopia = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsHinterlands = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsDarkAges = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsGuilds = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsAdventures = new ArrayList<Card>();
    public static ArrayList<Card> actionCardsPromo = new ArrayList<Card>();
    public static ArrayList<Card> actionCards = new ArrayList<Card>();
    public static ArrayList<Card> prizeCards = new ArrayList<Card>();
    public static ArrayList<Card> nonSupplyCards = new ArrayList<Card>();
    public static ArrayList<Card> ruinsCards = new ArrayList<Card>();
    public static ArrayList<Card> knightsCards = new ArrayList<Card>();
    public static ArrayList<Card> eventsCards = new ArrayList<Card>();
    public static ArrayList<Card> travellersCards = new ArrayList<Card>();
    
    public static HashMap<String, Card> actionCardsMap = new HashMap<String, Card>();

    static final String KNIGHTS_TEXT = "Each other player reveals the top 2 cards of his deck, trashes one of them costing from 3 to 6 coins, and discards the rest. If a Knight is trashed by this, trash this card.";
    static final String TRAVELLERS_TEXT = "TRAVELLERS_TEXT.";
    
    public enum Type {
        // Kingdom Cards
        Platinum, Gold, Silver, Copper, Potion, Colony, Province, Duchy, Estate, Curse,
        // Base Set
        Gardens, Moat, Adventurer, Bureaucrat, Cellar, Chancellor, Chapel, CouncilRoom, Feast, Festival, Laboratory, Library, Market, Militia, Mine, MoneyLender, Remodel, Smithy, Spy, Thief, ThroneRoom, Village, Witch, Woodcutter, Workshop,
        // Intrigue Expansion
        Duke, SecretChamber, Nobles, Coppersmith, Courtyard, Torturer, Harem, Baron, Bridge, Conspirator, Ironworks, Masquerade, MiningVillage, Minion, Pawn, Saboteur, ShantyTown, Scout, Steward, Swindler, TradingPost, WishingWell, Upgrade, Tribute, GreatHall,
        // Seaside Expansion
        Haven, SeaHag, Tactician, Caravan, Lighthouse, FishingVillage, Wharf, MerchantShip, Outpost, GhostShip, Salvager, PirateShip, NativeVillage, Island, Cutpurse, Bazaar, Smugglers, Explorer, PearlDiver, TreasureMap, Navigator, Treasury, Lookout, Ambassador, Warehouse, Embargo,
        // Alchemy Expansion
        Alchemist, Apothecary, Apprentice, Familiar, Golem, Herbalist, PhilosophersStone, Possession, ScryingPool, Transmute, University, Vineyard,
        // Prosperity Expansion
        Bank, Bishop, City, Contraband, CountingHouse, Expand, Forge, Goons, GrandMarket, Hoard, KingsCourt, Loan, Mint, Monument, Mountebank, Peddler, Quarry, Rabble, RoyalSeal, Talisman, TradeRoute, Vault, Venture, WatchTower, WorkersVillage,
        // Cornucopia Expansion
        HornofPlenty, Fairgrounds, FarmingVillage, FortuneTeller, Hamlet, Harvest, HorseTraders, HuntingParty, Jester, Menagerie, Remake, Tournament, YoungWitch, BagofGold, Diadem, Followers, Princess, TrustySteed,
        // Hinterlands Expansion
        BorderVillage, Cache, Cartographer, Crossroads, Develop, Duchess, Embassy, Farmland, FoolsGold, Haggler, Highway, IllGottenGains, Inn, JackofallTrades, Mandarin, Margrave, NobleBrigand, NomadCamp, Oasis, Oracle, Scheme, SilkRoad, SpiceMerchant, Stables, Trader, Tunnel,
        // Dark Ages Expansion
        BandOfMisfits,
        Altar, Armory, BanditCamp, Beggar, Catacombs, Count, Counterfeit, DeathCart, Feodum, Forager, Fortress, Graverobber, HuntingGrounds, Ironmonger, JunkDealer, MarketSquare, Mystic, Pillage, PoorHouse, Procession, Rats, Rebuild, Rogue, Sage, Scavenger, Spoils, Squire, Storeroom, WanderingMinstrel,
        Necropolis, Hovel, OvergrownEstate, AbandonedMine, RuinedLibrary, RuinedMarket, RuinedVillage, Survivors, Cultist, Urchin, Mercenary, Marauder, Hermit, Madman, Vagrant,
        DameAnna, DameJosephine, DameMolly, DameNatalie, DameSylvia, SirBailey, SirDestry, SirMartin, SirMichael, SirVander,
        VirtualRuins, VirtualKnight,
        // Guilds Expansion
        Advisor, Baker, Butcher, CandlestickMaker, Doctor, Herald, Journeyman, Masterpiece, MerchantGuild, Plaza, StoneMason, Soothsayer, Taxman,

        // Adventures Expansion
        CoinoftheRealm, Page, Peasant, Ratcatcher, Raze, Amulet, CaravanGuard, Dungeon, Gear, Guide, Duplicate, Magpie, Messenger, Miser, Port, Ranger, Transmogrify, Artificer, BridgeTroll, DistantLands, Giant, HauntedWoods, LostCity, Relic, RoyalCarriage, Storyteller, SwampHag, TreasureTrove, WineMerchant, Hireling, 
        Soldier, TreasureHunter, Fugitive, Warrior, Disciple, Hero, Champion, Teacher, 
        Alms, Borrow, Quest, Save, ScoutingParty, TravellingFair, Bonfire, Expedition, Ferry, Plan, Mission, Pilgrimage, Ball, Raid, Seaway, Trade, LostArts, Training, Inheritance, Pathfinding,
        VirtualTraveller, VirtualEvents,
        
        // Promo Cards
        Envoy, Governor, WalledVillage,
        // Promo Cards (not yet implemented)
        // BlackMarket, Stash
        // Victory Token card container
        VictoryTokens
    }

    public static final Card platinum;
    public static final Card gold;
    public static final Card silver;
    public static final Card copper;
    public static final Card potion;

    public static final Card victoryTokens;
    public static final Card colony;
    public static final Card province;
    public static final Card duchy;
    public static final Card estate;

    public static final Card curse;

    // Dominion base
    public static final Card gardens;
    public static final Card moat;
    public static final Card adventurer;
    public static final Card bureaucrat;
    public static final Card cellar;
    public static final Card chancellor;
    public static final Card chapel;
    public static final Card councilRoom;
    public static final Card feast;
    public static final Card festival;
    public static final Card laboratory;
    public static final Card library;
    public static final Card market;
    public static final Card militia;
    public static final Card mine;
    public static final Card moneyLender;
    public static final Card remodel;
    public static final Card smithy;
    public static final Card spy;
    public static final Card thief;
    public static final Card throneRoom;
    public static final Card village;
    public static final Card witch;
    public static final Card woodcutter;
    public static final Card workshop;

    // Intrigue expansion
    public static final Card duke;
    public static final Card secretChamber;
    public static final Card nobles;
    public static final Card coppersmith;
    public static final Card courtyard;
    public static final Card torturer;
    public static final Card harem;
    public static final Card baron;
    public static final Card bridge;
    public static final Card conspirator;
    public static final Card ironworks;
    public static final Card masquerade;
    public static final Card miningVillage;
    public static final Card minion;
    public static final Card pawn;
    public static final Card saboteur;
    public static final Card shantyTown;
    public static final Card scout;
    public static final Card steward;
    public static final Card swindler;
    public static final Card tradingPost;
    public static final Card wishingWell;
    public static final Card upgrade;
    public static final Card tribute;
    public static final Card greatHall;

    // Seaside expansion
    public static final Card haven;
    public static final Card seaHag;
    public static final Card tactician;
    public static final Card caravan;
    public static final Card lighthouse;
    public static final Card fishingVillage;
    public static final Card wharf;
    public static final Card merchantShip;
    public static final Card outpost;
    public static final Card ghostShip;
    public static final Card salvager;
    public static final Card pirateShip;
    public static final Card nativeVillage;
    public static final Card island;
    public static final Card cutpurse;
    public static final Card bazaar;
    public static final Card smugglers;
    public static final Card explorer;
    public static final Card pearlDiver;
    public static final Card treasureMap;
    public static final Card navigator;
    public static final Card treasury;
    public static final Card lookout;
    public static final Card ambassador;
    public static final Card warehouse;
    public static final Card embargo;

    // Alchemy expansion
    public static final Card alchemist;
    public static final Card apothecary;
    public static final Card apprentice;
    public static final Card familiar;
    public static final Card golem;
    public static final Card herbalist;
    public static final Card philosophersStone;
    public static final Card possession;
    public static final Card scryingPool;
    public static final Card transmute;
    public static final Card university;
    public static final Card vineyard;

    // Prosperity expansion
    public static final Card bank;
    public static final Card bishop;
    public static final Card city;
    public static final Card contraband;
    public static final Card countingHouse;
    public static final Card expand;
    public static final Card forge;
    public static final Card goons;
    public static final Card grandMarket;
    public static final Card hoard;
    public static final Card kingsCourt;
    public static final Card loan;
    public static final Card mint;
    public static final Card monument;
    public static final Card mountebank;
    public static final Card peddler;
    public static final Card quarry;
    public static final Card rabble;
    public static final Card royalSeal;
    public static final Card talisman;
    public static final Card tradeRoute;
    public static final Card vault;
    public static final Card venture;
    public static final Card watchTower;
    public static final Card workersVillage;
    
    // Cornucopia expansion
    public static final Card hornOfPlenty;
    public static final Card fairgrounds;
    public static final Card farmingVillage;
    public static final Card fortuneTeller;
    public static final Card hamlet;
    public static final Card harvest;
    public static final Card horseTraders;
    public static final Card huntingParty;
    public static final Card jester;
    public static final Card menagerie;
    public static final Card remake;
    public static final Card tournament;
    public static final Card youngWitch;
    public static final Card bagOfGold;
    public static final Card diadem;
    public static final Card followers;
    public static final Card princess;
    public static final Card trustySteed;
    
    // Hinterlands expansion
    public static final Card borderVillage;
    public static final Card cache;
    public static final Card cartographer;
    public static final Card crossroads;
    public static final Card develop;
    public static final Card duchess;
    public static final Card embassy;
    public static final Card farmland;
    public static final Card foolsGold;
    public static final Card haggler;
    public static final Card highway;
    public static final Card illGottenGains;
    public static final Card inn;
    public static final Card jackOfAllTrades;
    public static final Card mandarin;
    public static final Card margrave;
    public static final Card nobleBrigand;
    public static final Card nomadCamp;
    public static final Card oasis;
    public static final Card oracle;
    public static final Card scheme;
    public static final Card silkRoad;
    public static final Card spiceMerchant;
    public static final Card stables;
    public static final Card trader;
    public static final Card tunnel;

    // Dark Ages expansion - INCOMPLETE at the moment
    //TODO: implement rest of DA cards
    public static final Card altar;
    public static final Card armory;
    public static final Card banditCamp;
    public static final Card bandOfMisfits;
    public static final Card beggar;
    public static final Card deathCart;
    public static final Card feodum;
    public static final Card fortress;
    public static final Card poorHouse;
    public static final Card rats;
    public static final Card sage;
    public static final Card squire;
    public static final Card catacombs;
    public static final Card count;
    public static final Card forager;
    public static final Card graverobber;
    public static final Card huntingGrounds;
    public static final Card ironmonger;
    public static final Card junkDealer;
    public static final Card marketSquare;
    public static final Card mystic;
    public static final Card scavenger;
    public static final Card storeroom;
    public static final Card wanderingMinstrel;
    public static final Card procession;
    public static final Card rebuild;
    public static final Card rogue;
    public static final Card spoils;
    public static final Card counterfeit;
    public static final Card pillage;
    public static final Card cultist;
    public static final Card urchin;
    public static final Card mercenary;
    public static final Card marauder;
    public static final Card hermit;
    public static final Card madman;
    public static final Card vagrant;
    
    public static final Card dameAnna;
    public static final Card dameJosephine;
    public static final Card dameMolly;
    public static final Card dameSylvia;
    public static final Card dameNatalie;
    public static final Card sirBailey;
    public static final Card sirDestry;
    public static final Card sirMartin;
    public static final Card sirMichael;
    public static final Card sirVander;
    public static final Card virtualKnight;
    
    public static final Card necropolis;
    public static final Card hovel;
    public static final Card overgrownEstate;
    
    public static final Card abandonedMine;
    public static final Card ruinedLibrary;
    public static final Card ruinedMarket;
    public static final Card ruinedVillage;
    public static final Card survivors;
    public static final Card virtualRuins;
    
    // Guilds expansion
    public static final Card advisor;
    public static final Card baker;
    public static final Card butcher;
    public static final Card candlestickMaker;
    public static final Card doctor;
    public static final Card herald;
    public static final Card journeyman;
    public static final Card masterpiece;
    public static final Card merchantGuild;
    public static final Card plaza;
    public static final Card soothsayer;
    public static final Card stonemason;
    public static final Card taxman;

    // Adventures expansion
    public static final Card coinoftheRealm;
    public static final Card page;
    public static final Card peasant;
    public static final Card ratcatcher;
    public static final Card raze;
    public static final Card amulet;
    public static final Card caravanGuard;
    public static final Card dungeon;
    public static final Card gear;
    public static final Card guide;
    public static final Card duplicate;
    public static final Card magpie;
    public static final Card messenger;
    public static final Card miser;
    public static final Card port;
    public static final Card ranger;
    public static final Card transmogrify;
    public static final Card artificer;
    public static final Card bridgeTroll;
    public static final Card distantLands;
    public static final Card giant;
    public static final Card hauntedWoods;
    public static final Card lostCity;
    public static final Card relic;
    public static final Card royalCarriage;
    public static final Card storyteller;
    public static final Card swampHag;
    public static final Card treasureTrove;
    public static final Card wineMerchant;
    public static final Card hireling;

    public static final Card soldier;
    public static final Card treasureHunter;
    public static final Card fugitive;
    public static final Card warrior;
    public static final Card disciple;
    public static final Card hero;
    public static final Card champion;
    public static final Card teacher;
    public static final Card virtualTravellers;

    public static final Card alms;
    public static final Card borrow;
    public static final Card quest;
    public static final Card save;
    public static final Card scoutingParty;
    public static final Card travellingFair;
    public static final Card bonfire;
    public static final Card expedition;
    public static final Card ferry;
    public static final Card plan;
    public static final Card mission;
    public static final Card pilgrimage;
    public static final Card ball;
    public static final Card raid;
    public static final Card seaway;
    public static final Card trade;
    public static final Card lostArts;
    public static final Card training;
    public static final Card inheritance;
    public static final Card pathfinding;
    public static final Card virtualEvents;
    
    // Promo Cards (Incomplete)
    // TODO:Implement Rest of promo cards
    public static final Card walledVillage;
    public static final Card governor;
    public static final Card envoy;

    static {
        // nonKingdomCards
        nonKingdomCards.add(platinum = new TreasureCardImpl(Cards.Type.Platinum, 9, 5));
        nonKingdomCards.add(gold = new TreasureCardImpl(Cards.Type.Gold, 6, 3));
        nonKingdomCards.add(silver = new TreasureCardImpl(Cards.Type.Silver, 3, 2));
        nonKingdomCards.add(copper = new TreasureCardImpl(Cards.Type.Copper, 0, 1));
        nonKingdomCards.add(potion = new TreasureCardImpl.Builder(Cards.Type.Potion, 4, 0).providePotion().build());

        nonKingdomCards.add(curse = new CurseCardImpl(Cards.Type.Curse, 0, -1));

        nonKingdomCards.add(victoryTokens = new CardImpl(Cards.Type.VictoryTokens, 0));
        nonKingdomCards.add(colony = new VictoryCardImpl(Cards.Type.Colony, 11, 10));
        nonKingdomCards.add(province = new VictoryCardImpl(Cards.Type.Province, 8, 6));
        nonKingdomCards.add(duchy = new VictoryCardImpl(Cards.Type.Duchy, 5, 3));
        nonKingdomCards.add(estate = new VictoryCardImpl(Cards.Type.Estate, 2, 1));

        // Base Game
        actionCardsBaseGame.add(adventurer = new ActionCardImpl.Builder(Cards.Type.Adventurer, 6).description("Reveal cards from your deck until you reveal 2 Treasure cards. Put those Treasure cards into your hand and discard the other revealed cards.").expansion("Base").build());
        actionCardsBaseGame.add(bureaucrat = new ActionCardImpl.Builder(Cards.Type.Bureaucrat, 4).attack().description("Gain a Silver card; put it on top of your deck. Each other player reveals a Victory card from his hand and puts it on his deck (or reveals a hand with no Victory cards).").expansion("Base").build());
        actionCardsBaseGame.add(cellar = new ActionCardImpl.Builder(Cards.Type.Cellar, 2).addActions(1).description("Discard any number of cards. +1 Card per card discarded.").expansion("Base").build());
        actionCardsBaseGame.add(chancellor = new ActionCardImpl.Builder(Cards.Type.Chancellor, 3).addGold(2).description("You may immediately put your deck into your discard pile.").expansion("Base").build());
        actionCardsBaseGame.add(chapel = new ActionCardImpl.Builder(Cards.Type.Chapel, 2).description("Trash up to 4 cards from your hand.").expansion("Base").build());
        actionCardsBaseGame.add(councilRoom = new ActionCardImpl.Builder(Cards.Type.CouncilRoom, 5).addCards(4).addBuys(1).description("Each other player draws a card.").expansion("Base").build());
        actionCardsBaseGame.add(feast = new ActionCardImpl.Builder(Cards.Type.Feast, 4).trashOnUse().description("Trash this card. Gain a card costing up to 5 coin.").expansion("Base").build());
        actionCardsBaseGame.add(festival = new ActionCardImpl.Builder(Cards.Type.Festival, 5).addActions(2).addBuys(1).addGold(2).expansion("Base").build());
        actionCardsBaseGame.add(gardens = new VictoryCardImpl.Builder(Cards.Type.Gardens, 4, 0).description("Worth 1 Victory Point for every 10 cards in your deck (rounded down).").expansion("Base").build());
        actionCardsBaseGame.add(laboratory = new ActionCardImpl.Builder(Cards.Type.Laboratory, 5).addActions(1).addCards(2).expansion("Base").build());
        actionCardsBaseGame.add(library = new ActionCardImpl.Builder(Cards.Type.Library, 5).description("Draw until you have 7 cards in hand. You may set aside any Action cards drawn this way, as you draw them; discard the set aside cards after you finish drawing.").expansion("Base").build());
        actionCardsBaseGame.add(market = new ActionCardImpl.Builder(Cards.Type.Market, 5).addActions(1).addBuys(1).addGold(1).addCards(1).expansion("Base").build());
        actionCardsBaseGame.add(militia = new ActionCardImpl.Builder(Cards.Type.Militia, 4).addGold(2).attack().description("Each other player discards down to 3 cards in his hand.").expansion("Base").build());
        actionCardsBaseGame.add(mine = new ActionCardImpl.Builder(Cards.Type.Mine, 5).description("Trash a Treasure card from your hand. Gain a Treasure card costing up to 3 Coins more; put it into your hand.").expansion("Base").build());
        actionCardsBaseGame.add(moat = new ActionCardImpl.Builder(Cards.Type.Moat, 2).addCards(2).description("When another player plays an Attack card, you may reveal this from your hand. If you do, you are unaffected by that Attack.").description("When another player plays an Attack card, you may reveal this from your hand. If you do, you are unaffected by that Attack.").expansion("Base").build());
        actionCardsBaseGame.add(moneyLender = new ActionCardImpl.Builder(Cards.Type.MoneyLender, 4).description("Trash a Copper card from your hand. If you do, +3 Coins.").expansion("Base").build());
        actionCardsBaseGame.add(remodel = new ActionCardImpl.Builder(Cards.Type.Remodel, 4).trashForced().description("Trash a card from your hand. Gain a card costing up to 2 Coins more than the trashed card.").expansion("Base").build());
        actionCardsBaseGame.add(smithy = new ActionCardImpl.Builder(Cards.Type.Smithy, 4).addCards(3).expansion("Base").build());
        actionCardsBaseGame.add(spy = new ActionCardImpl.Builder(Cards.Type.Spy, 4).addCards(1).addActions(1).attack().description("Each player (including you) reveals the top card of his deck and either discards it or puts it back, your choice.").expansion("Base").build());
        actionCardsBaseGame.add(thief = new ActionCardImpl.Builder(Cards.Type.Thief, 4).attack().description("Each other player reveals the top 2 cards of his deck. If they revelaed any Treasure cards, they trash one of them that you choose. You may gain any or all of these trashed cards. They discard the other revealed cards.").expansion("Base").build());
        actionCardsBaseGame.add(throneRoom = new ActionCardImpl.Builder(Cards.Type.ThroneRoom, 4).description("Choose an Action card in your hand. Play it twice.").expansion("Base").build());
        actionCardsBaseGame.add(village = new ActionCardImpl.Builder(Cards.Type.Village, 3).addCards(1).addActions(2).expansion("Base").build());
        actionCardsBaseGame.add(witch = new ActionCardImpl.Builder(Cards.Type.Witch, 5).addCards(2).attack().description("Each other player gains a Curse card.").expansion("Base").build());
        actionCardsBaseGame.add(woodcutter = new ActionCardImpl.Builder(Cards.Type.Woodcutter, 3).addBuys(1).addGold(2).expansion("Base").build());
        actionCardsBaseGame.add(workshop = new ActionCardImpl.Builder(Cards.Type.Workshop, 3).description("Gain a card costing up to 4 Coins.").expansion("Base").build());

        // Intrigue
        actionCardsIntrigue.add(baron = new ActionCardImpl.Builder(Cards.Type.Baron, 4).addBuys(1).description("You may discard an Estate card. If you do, +4 Coins. Otherwise, gain an Estate card.").expansion("Intrigue").build());
        actionCardsIntrigue.add(bridge = new ActionCardImpl.Builder(Cards.Type.Bridge, 4).addBuys(1).addGold(1).description("All cards (including cards in players' hands) cost 1 Coin less this turn, but not less than 0.").expansion("Intrigue").build());
        actionCardsIntrigue.add(conspirator = new ActionCardImpl.Builder(Cards.Type.Conspirator, 4).addGold(2).description("If you've played 3 or more Actions this turn (counting this): +1 Card, +1 Action.").expansion("Intrigue").build());
        actionCardsIntrigue.add(coppersmith = new ActionCardImpl.Builder(Cards.Type.Coppersmith, 4).description("Copper produces an extra 1 coin this turn.").expansion("Intrigue").build());
        actionCardsIntrigue.add(courtyard = new ActionCardImpl.Builder(Cards.Type.Courtyard, 2).addCards(3).description("Put a card from your hand on top of your deck.").expansion("Intrigue").build());
        actionCardsIntrigue.add(duke = new VictoryCardImpl.Builder(Cards.Type.Duke, 5, 0).description("Worth 1 Victory Point per Duchy you have.").expansion("Intrigue").build());
        actionCardsIntrigue.add(greatHall = new ActionVictoryCardImpl.Builder(Cards.Type.GreatHall, 3).addCards(1).addActions(1).vp(1).expansion("Intrigue").build());
        actionCardsIntrigue.add(harem = new TreasureVictoryCardImpl.Builder(Cards.Type.Harem, 6, 2, 2).vp(2).expansion("Intrigue").build());
        actionCardsIntrigue.add(ironworks = new ActionCardImpl.Builder(Cards.Type.Ironworks, 4).description("Gain a card costing up to 4 Coins. If it is an... Action card, +1 Action. Treasure card, +1 Coin. Victory card, +1 Card.").expansion("Intrigue").build());
        actionCardsIntrigue.add(masquerade = new ActionCardImpl.Builder(Cards.Type.Masquerade, 3).addCards(2).description("Each player passes a card from his hand to the left at once. Then you may trash a card from your hand.").expansion("Intrigue").build());
        actionCardsIntrigue.add(miningVillage = new ActionCardImpl.Builder(Cards.Type.MiningVillage, 4).addCards(1).addActions(2).description("You may trash this card immediately. If you do, +2 Coins.").expansion("Intrigue").build());
        actionCardsIntrigue.add(minion = new ActionCardImpl.Builder(Cards.Type.Minion, 5).addActions(1).attack().description("Choose one: +2 Coins; or discard your hand, +4 Cards, and each other player with at least 5 cards in hand discards his hand and draws 4 cards.").expansion("Intrigue").build());
        actionCardsIntrigue.add(nobles = new ActionVictoryCardImpl.Builder(Cards.Type.Nobles, 6).vp(2).description("Choose one: +3 Cards; or +2 Actions.").expansion("Intrigue").build());
        actionCardsIntrigue.add(pawn = new ActionCardImpl.Builder(Cards.Type.Pawn, 2).description("Choose two: +1 Card; +1 Action; +1 Buy; +1 Coin. (The choices must be different.)").expansion("Intrigue").build());
        actionCardsIntrigue.add(saboteur = new ActionCardImpl.Builder(Cards.Type.Saboteur, 5).attack().description("Each other player reveals cards from the top of his deck until revealing one costing 3 Coins or more. He trashes that card and may gain a card costing at most 2 Coins less than it. He discards the other revealed cards.").expansion("Intrigue").build());
        actionCardsIntrigue.add(scout = new ActionCardImpl.Builder(Cards.Type.Scout, 4).addActions(1).description("Reveal the top 4 cards of your deck. Put the revealed Victory cards into your hand. Put the other cards on top of your deck in any order.").expansion("Intrigue").build());
        actionCardsIntrigue.add(secretChamber = new ActionCardImpl.Builder(Cards.Type.SecretChamber, 2).description("Discard any number of cards. +1 Coin per card discarded. When another player plays an Attack card, you may reveal this from your hand. If you do, +2 Cards, then put 2 cards from your hand on top of your deck.").expansion("Intrigue").build());
        actionCardsIntrigue.add(shantyTown = new ActionCardImpl.Builder(Cards.Type.ShantyTown, 3).addActions(2).description("Reveal you hand. If you have no Action cards in hand, +2 Cards.").expansion("Intrigue").build());
        actionCardsIntrigue.add(steward = new ActionCardImpl.Builder(Cards.Type.Steward, 3).description("Choose one: +2 Cards; or +2 Coins; or trash 2 cards from your hand.").expansion("Intrigue").build());
        actionCardsIntrigue.add(swindler = new ActionCardImpl.Builder(Cards.Type.Swindler, 3).addGold(2).attack().description("Each other player trashes the top card of his deck and gains a card with the same cost that you choose.").expansion("Intrigue").build());
        actionCardsIntrigue.add(torturer = new ActionCardImpl.Builder(Cards.Type.Torturer, 5).addCards(3).attack().description("Each other player chooses one: he discards 2 cards; or he gains a Curse card, putting it in his hand.").expansion("Intrigue").build());
        actionCardsIntrigue.add(tradingPost = new ActionCardImpl.Builder(Cards.Type.TradingPost, 5).trashForced().description("Trash 2 cards from your hand. If you do, gain a Silver card; put it into your hand.").expansion("Intrigue").build());
        actionCardsIntrigue.add(tribute = new ActionCardImpl.Builder(Cards.Type.Tribute, 5).description("The player to your left reveals then discards the top 2 cards of his deck. For each differently named card revealed, if it is an... Action Card, +2 Actions. Treasure Card, +2 Coins. Victory Card, +2 Cards.").expansion("Intrigue").build());
        actionCardsIntrigue.add(upgrade = new ActionCardImpl.Builder(Cards.Type.Upgrade, 5).trashForced().addCards(1).addActions(1).description("Trash a card from your hand. Gain a card costing exactly 1 Coin more than it.").expansion("Intrigue").build());
        actionCardsIntrigue.add(wishingWell = new ActionCardImpl.Builder(Cards.Type.WishingWell, 3).addCards(1).addActions(1).description("Name a card. Reveal the top card of your deck. If it's the named card, put it into your hand.").expansion("Intrigue").build());

        // Seaside
        actionCardsSeaside.add(ambassador = new ActionCardImpl.Builder(Cards.Type.Ambassador, 3).trashForced().attack().description("Reveal a card from your hand. Return up to 2 copies of it from your hand to the Supply. Then each other player gains a copy of it.").expansion("Seaside").build());
        actionCardsSeaside.add(bazaar = new ActionCardImpl.Builder(Cards.Type.Bazaar, 5).addCards(1).addActions(2).addGold(1).expansion("Seaside").build());
        actionCardsSeaside.add(caravan = new ActionDurationCardImpl.Builder(Cards.Type.Caravan, 4).addCardsNextTurn(1).addCards(1).addActions(1).expansion("Seaside").build());
        actionCardsSeaside.add(cutpurse = new ActionCardImpl.Builder(Cards.Type.Cutpurse, 4).addGold(2).attack().description("Each other player discards a Copper card (or reveals a hand with no Copper).").expansion("Seaside").build());
        actionCardsSeaside.add(embargo = new ActionCardImpl.Builder(Cards.Type.Embargo, 2).addGold(2).trashOnUse().description("Trash this card. Put an Embargo token on top of a Supply pile. When a player buys a card, he gains a Curse card per Embargo token on that pile.").expansion("Seaside").build());
        actionCardsSeaside.add(explorer = new ActionCardImpl.Builder(Cards.Type.Explorer, 5).description("You may reveal a Province card from your hand. If you do, gain a Gold card, putting it into your hand. Otherwise, gain a Silver card, putting it into your hand.").expansion("Seaside").build());
        actionCardsSeaside.add(fishingVillage = new ActionDurationCardImpl.Builder(Cards.Type.FishingVillage, 3).addGoldNextTurn(1).addActionsNextTurn(1).addActions(2).addGold(1).expansion("Seaside").build());
        actionCardsSeaside.add(ghostShip = new ActionCardImpl.Builder(Cards.Type.GhostShip, 5).addCards(2).attack().description("Each other player with 4 or more cards in hand puts cards from his hand on top of his deck until he has 3 cards in his hand.").expansion("Seaside").build());
        actionCardsSeaside.add(haven = new ActionDurationCardImpl.Builder(Cards.Type.Haven, 2).addCards(1).addActions(1).description("Set aside a card from your hand face down. At the start of your next turn, put it into your hand.").expansion("Seaside").build());
        actionCardsSeaside.add(island = new ActionVictoryCardImpl.Builder(Cards.Type.Island, 4).vp(2).description("Set aside this and another card from your hand. Return them to your deck at the end of the game.").expansion("Seaside").build());
        actionCardsSeaside.add(lighthouse = new ActionDurationCardImpl.Builder(Cards.Type.Lighthouse, 2).addGoldNextTurn(1).addActions(1).addGold(1).description("While this is in play, when another player plays an Attack card, it doesn't affect you.").expansion("Seaside").build());
        actionCardsSeaside.add(lookout = new ActionCardImpl.Builder(Cards.Type.Lookout, 3).addActions(1).description("Look at the top 3 cards of your deck. Trash one of them. Discard one of them. Put the other one on top of your deck.").expansion("Seaside").build());
        actionCardsSeaside.add(merchantShip = new ActionDurationCardImpl.Builder(Cards.Type.MerchantShip, 5).addGoldNextTurn(2).addGold(2).expansion("Seaside").build());
        actionCardsSeaside.add(nativeVillage = new ActionCardImpl.Builder(Cards.Type.NativeVillage, 2).addActions(2).description("Choose one: Set aside the top card of your deck face down on your Native Village mat; or put all the cards from your mat into your hand.").expansion("Seaside").build());
        actionCardsSeaside.add(navigator = new ActionCardImpl.Builder(Cards.Type.Navigator, 4).addGold(2).description("Look at the top 5 cards of your deck. Either discard all of them, or put them back on top of your deck in any order.").expansion("Seaside").build());
        actionCardsSeaside.add(outpost = new ActionDurationCardImpl.Builder(Cards.Type.Outpost, 5).takeAnotherTurn(3).description("You only draw 3 cards (instead of 5) in this turn's Clean-up phase. Take an extra turn after this one. This can't cause you to take more than two consecutive turns.").expansion("Seaside").build());
        actionCardsSeaside.add(pearlDiver = new ActionCardImpl.Builder(Cards.Type.PearlDiver, 2).addCards(1).addActions(1).description("Look at the bottom card of your deck. You may put it on top.").expansion("Seaside").build());
        actionCardsSeaside.add(pirateShip = new ActionCardImpl.Builder(Cards.Type.PirateShip, 4).attack().description("Choose one: Each other player reveals the top 2 cards of his deck, trashes a revealed Treasure that you choose, discards the rest, and if anyone trashed a Treasure you take a Coin token; or, +1 Coin per Coin token you've taken with Pirate Ships this game.").expansion("Seaside").build());
        actionCardsSeaside.add(salvager = new ActionCardImpl.Builder(Cards.Type.Salvager, 4).trashForced().addBuys(1).description("Trash a card from your hand. + Coins equal to its cost.").expansion("Seaside").build());
        actionCardsSeaside.add(seaHag = new ActionCardImpl.Builder(Cards.Type.SeaHag, 4).attack().description("Each other player discards the top card of his deck, then gains a Curse card, putting it on top of his deck.").expansion("Seaside").build());
        actionCardsSeaside.add(smugglers = new ActionCardImpl.Builder(Cards.Type.Smugglers, 3).description("Gain a copy of a card costing up to 6 Coins that the player to your right gained on his last turn.").expansion("Seaside").build());
        actionCardsSeaside.add(tactician = new ActionDurationCardImpl.Builder(Cards.Type.Tactician, 5).description("Discard your hand. If you discarded any cards this way, then at the start of your next turn, +5 Cards, +1 Buy, and +1 Action.").expansion("Seaside").build());
        actionCardsSeaside.add(treasureMap = new ActionCardImpl.Builder(Cards.Type.TreasureMap, 4).description("Trash this and another copy of Treasure Map from your hand. If you do trash two Treasure Maps, gain 4 Gold cards putting them on top of your deck.").expansion("Seaside").build());
        actionCardsSeaside.add(treasury = new ActionCardImpl.Builder(Cards.Type.Treasury, 5).addCards(1).addActions(1).addGold(1).description("When you discard this from play, if you didn't buy a Victory card this turn, you may put this on top of your deck.").expansion("Seaside").build());
        actionCardsSeaside.add(warehouse = new ActionCardImpl.Builder(Cards.Type.Warehouse, 3).addCards(3).addActions(1).description("Discard 3 cards.").expansion("Seaside").build());
        actionCardsSeaside.add(wharf = new ActionDurationCardImpl.Builder(Cards.Type.Wharf, 5).addCardsNextTurn(2).addBuysNextTurn(1).addCards(2).addBuys(1).expansion("Seaside").build());

        // Alchemy
        actionCardsAlchemy.add(alchemist = new ActionCardImpl.Builder(Cards.Type.Alchemist, 3).addActions(1).addCards(2).costPotion().description("When you discard this from play, you may put this on top of your deck if you have a Potion in play.").expansion("Alchemy").build());
        actionCardsAlchemy.add(apothecary = new ActionCardImpl.Builder(Cards.Type.Apothecary, 2).addActions(1).addCards(1).costPotion().description("Reveal the top 4 cards of your deck.  Put the revealed Coppers and Potions into your hand.  Put the other cards back on top of your deck in any order.").expansion("Alchemy").build());
        actionCardsAlchemy.add(apprentice = new ActionCardImpl.Builder(Cards.Type.Apprentice, 5).trashForced().addActions(1).description("Trash a card from your hand.  +1 Card per Coin it costs.  +2 Cards if it has a Potion in its cost.").expansion("Alchemy").build());
        actionCardsAlchemy.add(familiar = new ActionCardImpl.Builder(Cards.Type.Familiar, 3).addCards(1).addActions(1).attack().costPotion().description("Each other player gains a Curse.").expansion("Alchemy").build());
        actionCardsAlchemy.add(golem = new ActionCardImpl.Builder(Cards.Type.Golem, 4).costPotion().description("Reveal cards from your deck until you reveal 2 Action cards other than Golem cards.  Discard the other cards, then play the Action cards in either order.").expansion("Alchemy").build());
        actionCardsAlchemy.add(herbalist = new ActionCardImpl.Builder(Cards.Type.Herbalist, 2).addBuys(1).addGold(1).description("When you discard this from play, you may put one of your Treasures from play on top of your deck.").expansion("Alchemy").build());
        actionCardsAlchemy.add(philosophersStone = new TreasureCardImpl.Builder(Cards.Type.PhilosophersStone, 3, 0).costPotion().description("When you play this, count your deck and discard pile.  Worth (1) coin per 5 cards total between them (rounded down).").expansion("Alchemy").build());
        actionCardsAlchemy.add(possession = new ActionCardImpl.Builder(Cards.Type.Possession, 6).costPotion().description("The player to your left takes an extra turn after this one, in which you can see all cards he can and make all decisions for him. Any cards he would gain on that turn, you gain instead; any cards of his that are trashed are set aside and returned to his discard pile at end of turn.").expansion("Alchemy").build());
        actionCardsAlchemy.add(scryingPool = new ActionCardImpl.Builder(Cards.Type.ScryingPool, 2).addActions(1).attack().costPotion().description("Each player (including you) reveals the top card of his deck and either discards it or puts it back, your choice.  Then reveal cards from the top of your deck until you reveal one that is not an Action.  Put all of your revealed cards into your hand.").expansion("Alchemy").build());
        actionCardsAlchemy.add(transmute = new ActionCardImpl.Builder(Cards.Type.Transmute, 0).trashForced().costPotion().description("Trash a card from your hand.  If it is an . . . Action card, gain a Duchy; Treasure card, gain a Transmute; Victory card gain a Gold.").expansion("Alchemy").build());
        actionCardsAlchemy.add(university = new ActionCardImpl.Builder(Cards.Type.University, 2).addActions(2).costPotion().description("You may gain an Action card costing up to 5.").expansion("Alchemy").build());
        actionCardsAlchemy.add(vineyard = new VictoryCardImpl.Builder(Cards.Type.Vineyard, 0, 0).costPotion().description("Worth 1 Victory Point for every 3 Action cards in your deck (rounded down).").expansion("Alchemy").build());

        // Prosperity
        actionCardsProsperity.add(bank = new TreasureCardImpl.Builder(Cards.Type.Bank, 7, 0).description("When you play this, it's worth 1 coin per Treasure card you have in play (counting this).").expansion("Prosperity").build());
        actionCardsProsperity.add(bishop = new ActionCardImpl.Builder(Cards.Type.Bishop, 4).trashForced().addGold(1).addVictoryTokens(1).description("Trash a card from your hand.  Gain Victory tokens equal to half its cost in coins, rounded down.  Each other player may trash a card from his hand.").expansion("Prosperity").build());
        actionCardsProsperity.add(city = new ActionCardImpl.Builder(Cards.Type.City, 5).addActions(2).addCards(1).description("If there are one or more empty Supply piles, +1 Card.  If there are two or more, +1 Coin and +1 Buy.").expansion("Prosperity").build());
        // Add Buys hard-coded in TreasureCardImpl
        actionCardsProsperity.add(contraband = new TreasureCardImpl.Builder(Cards.Type.Contraband, 5, 3).description("+1 Buy  When you play this, the player to your left names a card.  You can't buy that card this turn.").expansion("Prosperity").build());
        actionCardsProsperity.add(countingHouse = new ActionCardImpl.Builder(Cards.Type.CountingHouse, 5).description("Look through your discard pile, reveal any number of Copper cards from it, and put them into your hand.").expansion("Prosperity").build());
        actionCardsProsperity.add(expand = new ActionCardImpl.Builder(Cards.Type.Expand, 7).trashForced().description("Trash a card from your hand.  Gain a card costing up to 3 coins more than the trashed card.").expansion("Prosperity").build());
        actionCardsProsperity.add(forge = new ActionCardImpl.Builder(Cards.Type.Forge, 7).trashForced().description("Trash any number of cards from your hand.  Gain a card with cost exactly equal to the total cost in coins of the trashed cards.").expansion("Prosperity").build());
        actionCardsProsperity.add(goons = new ActionCardImpl.Builder(Cards.Type.Goons, 6).addBuys(1).addGold(2).attack().description("Eash other player discards down to 3 cards in hand.  While this is in play, when you buy a card, +1 Victory token.").expansion("Prosperity").build());
        actionCardsProsperity.add(grandMarket = new ActionCardImpl.Builder(Cards.Type.GrandMarket, 6).addCards(1).addActions(1).addBuys(1).addGold(2).description("You can't buy this if you have any Copper in play.").expansion("Prosperity").build());
        actionCardsProsperity.add(hoard = new TreasureCardImpl.Builder(Cards.Type.Hoard, 6, 2).description("While this is in play, when you buy a Victory card, gain a Gold.").expansion("Prosperity").build());
        actionCardsProsperity.add(kingsCourt = new ActionCardImpl.Builder(Cards.Type.KingsCourt, 7).description("You may choose an Action card in your hand.  Play it three times.").expansion("Prosperity").build());
        actionCardsProsperity.add(loan = new TreasureCardImpl.Builder(Cards.Type.Loan, 3, 1).description("When you play this, reveal cards from your deck until you reveal a Treasure.  Discard it or trash it.  Discard the other cards.").expansion("Prosperity").build());
        actionCardsProsperity.add(mint = new ActionCardImpl.Builder(Cards.Type.Mint, 5).description("You may reveal a Treasure card from your hand.  Gain a copy of it.  When you buy this, trash all Treasures you have in play.").expansion("Prosperity").build());
        actionCardsProsperity.add(monument = new ActionCardImpl.Builder(Cards.Type.Monument, 4).addGold(2).addVictoryTokens(1).expansion("Prosperity").build());
        actionCardsProsperity.add(mountebank = new ActionCardImpl.Builder(Cards.Type.Mountebank, 5).addGold(2).attack().description("Each other player may discard a Curse.  If he doesn't, he gains a Curse and a Copper.").expansion("Prosperity").build());
        actionCardsProsperity.add(peddler = new ActionCardImpl.Builder(Cards.Type.Peddler, 8).addActions(1).addCards(1).addGold(1).description("During your Buy phase, this costs 2 coins less per Action card you have in play, but not less than 0 coins.").expansion("Prosperity").build());
        actionCardsProsperity.add(quarry = new TreasureCardImpl.Builder(Cards.Type.Quarry, 4, 1).description("While this is in play, Action cards cost 2 coins less, but not less than 0 coins.").expansion("Prosperity").build());
        actionCardsProsperity.add(rabble = new ActionCardImpl.Builder(Cards.Type.Rabble, 5).addCards(3).attack().description("Each other player reveals the top 3 cards of his deck, discards the revealed Actions and Treasures, and puts the rest back on top in any order he chooses.").expansion("Prosperity").build());
        actionCardsProsperity.add(royalSeal = new TreasureCardImpl.Builder(Cards.Type.RoyalSeal, 5, 2).description("While this is in play, when you gain a card, you may put that card on top of your deck.").expansion("Prosperity").build());
        actionCardsProsperity.add(talisman = new TreasureCardImpl.Builder(Cards.Type.Talisman, 4, 1).description("While this is in play, when you buy a card costing 4 coins or less that is not a Victory card, gain a copy of it.").expansion("Prosperity").build());
        actionCardsProsperity.add(tradeRoute = new ActionCardImpl.Builder(Cards.Type.TradeRoute, 3).trashForced().addBuys(1).description("+1 Coin per token on the Trade Route mat.  Trash a card from your hand.  Setup: Put a token on each Victory card Supply pile.  When a card is gained from that pile, move the token to the Trade Route mat.").expansion("Prosperity").build());
        actionCardsProsperity.add(vault = new ActionCardImpl.Builder(Cards.Type.Vault, 5).addCards(2).description("Discard any number of cards.  +1 coin per card discarded.  Each other player may discard 2 cards.  If he does, he draws a card.").expansion("Prosperity").build());
        actionCardsProsperity.add(venture = new TreasureCardImpl.Builder(Cards.Type.Venture, 5, 1).description("When you play this, reveal cards from your deck until you reveal a Treasure.  Discard the other cards.  Play that Treasure.").expansion("Prosperity").build());
        actionCardsProsperity.add(watchTower = new ActionCardImpl.Builder(Cards.Type.WatchTower, 3).description("Draw until you have 6 cards in hand.  When you gain a card, you may reveal this from your hand.  If you do, either trash that card, or put it on top of your deck.").expansion("Prosperity").build());
        actionCardsProsperity.add(workersVillage = new ActionCardImpl.Builder(Cards.Type.WorkersVillage, 4).addCards(1).addActions(2).addBuys(1).expansion("Prosperity").build());

        // Cornucopia
        actionCardsCornucopia.add(fairgrounds = new VictoryCardImpl.Builder(Cards.Type.Fairgrounds, 6, 0).description("Worth 2 points for every 5 differently named cards in your deck (round down).").expansion("Cornucopia").build());
        actionCardsCornucopia.add(farmingVillage = new ActionCardImpl.Builder(Cards.Type.FarmingVillage, 4).addActions(2).description("Reveal cards from the top of your deck until you reveal an Action or Treasure card.  Put that card into your hand and discard the other cards.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(fortuneTeller = new ActionCardImpl.Builder(Cards.Type.FortuneTeller, 3).addGold(2).attack().description("Each other player reveals cards from the top of his deck until he reveals a Victory or Curse card.  He puts it on top and discards the other revealed cards.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(hamlet = new ActionCardImpl.Builder(Cards.Type.Hamlet, 2).addActions(1).addCards(1).description("You may discard a card; if you do +1 Action.  You may discard a card; if you do +1 Buy.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(harvest = new ActionCardImpl.Builder(Cards.Type.Harvest, 5).description("Reveal the top 4 cards of your deck, then discard them.  +1 coin per differently named card revealed.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(hornOfPlenty = new TreasureCardImpl.Builder(Cards.Type.HornofPlenty, 5, 0).description("When you play this, gain a card costing up to 1 coin per differently named card you have in play, counting this.  If it's a Victory card, trash this.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(horseTraders = new ActionCardImpl.Builder(Cards.Type.HorseTraders, 4).addBuys(1).addGold(3).description("Discard 2 cards.  When another player plays an Attack card, you may set this aside from your hand.  If you do, then at the start of your next turn, +1 Card and return this to your hand.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(huntingParty = new ActionCardImpl.Builder(Cards.Type.HuntingParty, 5).addActions(1).addCards(1).description("Reveal your hand.  Reveal cards from your deck until you reveal a card that isn't a duplicate of one in your hand.  Put it into your hand and discard the rest.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(jester = new ActionCardImpl.Builder(Cards.Type.Jester, 5).addGold(2).attack().description("Each other player discards the top card of his deck.  If it's a Victory card, he gains a Curse.  Otherwise either he gains a copy of the discarded card or you do, your choice.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(menagerie = new ActionCardImpl.Builder(Cards.Type.Menagerie, 3).addActions(1).description("Reveal your hand.  If there are no duplicate cards in it, +3 Cards.  Otherwise, +1 Card.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(remake = new ActionCardImpl.Builder(Cards.Type.Remake, 4).trashForced().description("Do this twice.  Trash a card from your hand, then gain a card costing exactly 1 coin more than the trashed card.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(tournament = new ActionCardImpl.Builder(Cards.Type.Tournament, 4).addActions(1).description("Each player may reveal a Province from his hand.  If you do, discard it and gain a Prize (from the Prize pile) or a Duchy, putting it on top of your deck.  If no one else does, +1 Card, +1 coin.").expansion("Cornucopia").build());
        actionCardsCornucopia.add(youngWitch = new ActionCardImpl.Builder(Cards.Type.YoungWitch, 4).addCards(2).attack().description("Discard 2 cards.  Each other player may reveal a Bane card from his hand.  If he doesn't, he gains a Curse.  Setup:  Add an extra Kingdom card pile costing 2 or 3 coins to the Supply.  Cards from that pile are Bane cards.").expansion("Cornucopia").build());

        // Prizes
        prizeCards.add(bagOfGold   = new ActionCardImpl.Builder(Cards.Type.BagofGold, 0).addActions(1).isPrize().description("Gain a Gold, putting it on top of your deck.  (This is not in the Supply.)").expansion("Cornucopia").build());
        prizeCards.add(diadem      = new TreasureCardImpl.Builder(Cards.Type.Diadem, 0, 2).isPrize().description("When you play this, +1 coin per unused Action you have (Action, not Action card).  (This is not in the Supply.)").expansion("Cornucopia").build());
        prizeCards.add(followers   = new ActionCardImpl.Builder(Cards.Type.Followers, 0).addCards(2).attack().isPrize().description("Gain an Estate.  Each other player gains a Curse and discards down to 3 cards in hand.  (This is not in the Supply.)").expansion("Cornucopia").build());
        prizeCards.add(princess    = new ActionCardImpl.Builder(Cards.Type.Princess, 0).addBuys(1).isPrize().description("While this is in play, cards cost 2 coins less, but not less than 0.  (This is not in the Supply.)").expansion("Cornucopia").build());
        prizeCards.add(trustySteed = new ActionCardImpl.Builder(Cards.Type.TrustySteed, 0).isPrize().description("Choose two:  +2 Cards; +2 Actions; +2 coins; gain 4 silvers and put your deck into your discard pile.  (The choices must be different.)  (This is not in the Supply.)").expansion("Cornucopia").build());
        
        // Hinterlands
        actionCardsHinterlands.add(borderVillage = new ActionCardImpl.Builder(Cards.Type.BorderVillage, 6).addCards(1).addActions(2).description("When you gain this, gain a card costing less than this.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(cache = new TreasureCardImpl.Builder(Cards.Type.Cache, 5, 3).description("When you gain this, gain two Coppers.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(cartographer = new ActionCardImpl.Builder(Cards.Type.Cartographer, 5).addCards(1).addActions(1).description("Look at the top 4 cards of your deck. Discard any number of them. Put the rest back on top in any order.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(crossroads = new ActionCardImpl.Builder(Cards.Type.Crossroads, 2).description("Reveal your hand. +1 Card per Victory card revealed. If this is the first time you played a Crossroads this turn, +3 Actions.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(develop = new ActionCardImpl.Builder(Cards.Type.Develop, 3).trashForced().description("Trash a card from your hand. Gain a card costing exactly 1 coin more than it and a card costing exactly 1 less than it, in either order, putting them on top of your deck.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(duchess = new ActionCardImpl.Builder(Cards.Type.Duchess, 2).addGold(2).description("Each player (including you) looks at the top card of his deck, and discards it or puts it back - In games using this, when you gain a Duchy, you may gain a Duchess.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(embassy = new ActionCardImpl.Builder(Cards.Type.Embassy, 5).addCards(5).description("Discard 3 cards - When you gain this, each other player gains a Silver.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(farmland = new VictoryCardImpl.Builder(Cards.Type.Farmland, 6, 2).description("When you buy this, trash a card from your hand. Gain a card costing exactly 2 coins more than the trashed card.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(foolsGold = new TreasureCardImpl.Builder(Cards.Type.FoolsGold, 2, 1).description("If this is the first time you played a Fool's Gold this turn, this is worth 1 coin, otherwise it's worth 4 coins - When another player gains a Province, you may trash this from your hand. If you do, gain a Gold, putting it on your deck.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(haggler = new ActionCardImpl.Builder(Cards.Type.Haggler, 5).addGold(2).description("While this is in play, when you buy a card, gain a card costing less than it that is not a Victory card.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(highway = new ActionCardImpl.Builder(Cards.Type.Highway, 5).addCards(1).addActions(1).description("While this is in play, cards cost 1 coin less, but not less than 0 coin.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(illGottenGains = new TreasureCardImpl.Builder(Cards.Type.IllGottenGains, 5, 1).description("When you play this, you may gain a Copper, putting it into your hand - When you gain this, each other player gains a Curse.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(inn = new ActionCardImpl.Builder(Cards.Type.Inn, 5).addCards(2).addActions(2).description("Discard 2 cards - When you gain this, look through your discard pile (including this), reveal any number of Action cards from it, and shuffle them into your deck.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(jackOfAllTrades = new ActionCardImpl.Builder(Cards.Type.JackofallTrades, 4).description("Gain a Silver. Look at the top card of your deck; discard it or put it back. Draw until you have 5 cards in hand. You may trash a card from your hand that is not a Treasure.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(mandarin = new ActionCardImpl.Builder(Cards.Type.Mandarin, 5).addGold(3).description("Put a card from your hand on top of your deck - When you gain this, put all Treasures you have in play on top of your deck in any order.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(margrave = new ActionCardImpl.Builder(Cards.Type.Margrave, 5).addCards(3).addBuys(1).attack().description("Each other player draws a card, then discards down to 3 cards in hand.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(nobleBrigand = new ActionCardImpl.Builder(Cards.Type.NobleBrigand, 4).addGold(1).attack().description("When you buy this or play it, each other player reveals the top 2 cards of his deck, trashes a revealed Silver or Gold you choose, and discards the rest. If he didn't reveal a Treasure, he gains a Copper. You gain the trashed cards.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(nomadCamp = new ActionCardImpl.Builder(Cards.Type.NomadCamp, 4).addBuys(1).addGold(2).description("When you gain this, put it on top of your deck.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(oasis = new ActionCardImpl.Builder(Cards.Type.Oasis, 3).addCards(1).addActions(1).addGold(1).description("Discard a card.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(oracle = new ActionCardImpl.Builder(Cards.Type.Oracle, 3).attack().description("Each player (including you) reveals the top 2 cards of his deck, and you choose one: either he discards them, or he puts them back on top in an order he chooses.\n+2 Cards").expansion("Hinterlands").build());
        actionCardsHinterlands.add(scheme = new ActionCardImpl.Builder(Cards.Type.Scheme, 3).addCards(1).addActions(1).description("At the start of Clean-up this turn, you may choose an Action card you have in play. If you discard it from play this turn, put it on your deck.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(silkRoad = new VictoryCardImpl.Builder(Cards.Type.SilkRoad, 4, 0).description("Worth 1 VP for every 4 Victory cards in your deck (round down).").expansion("Hinterlands").build());
        actionCardsHinterlands.add(spiceMerchant = new ActionCardImpl.Builder(Cards.Type.SpiceMerchant, 4).trashForced().description("You may trash a Treasure from your hand. If you do, choose one: +2 Cards and +1 Action; or +2 Coin and +1 Buy.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(stables = new ActionCardImpl.Builder(Cards.Type.Stables, 5).description("You may discard a Treasure. If you do, +3 Cards and +1 Action.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(trader = new ActionCardImpl.Builder(Cards.Type.Trader, 4).trashForced().description("Trash a card from your hand. Gain a number of Silvers equal to its cost in coins - When you would gain a card, you may reveal this from your hand. If you do, instead, gain a silver.").expansion("Hinterlands").build());
        actionCardsHinterlands.add(tunnel = new VictoryCardImpl.Builder(Cards.Type.Tunnel, 3, 2).description("When you discard this other than during a Clean-up phase, you may reveal it. If you do, gain a Gold.").expansion("Hinterlands").build());
        
        // Dark Ages
        actionCardsDarkAges.add(altar = new ActionCardImpl.Builder(Cards.Type.Altar, 6).trashForced().description("Trash a card from your hand. Gain a card costing up to 5 coins.").expansion("DarkAges").build());
        actionCardsDarkAges.add(armory = new ActionCardImpl.Builder(Cards.Type.Armory, 4).description("Gain a card costing up to 4 coins. Put it on top of your deck.").expansion("DarkAges").build());
		actionCardsDarkAges.add(banditCamp = new ActionCardImpl.Builder(Cards.Type.BanditCamp, 5).addActions(2).addCards(1).description("Gain a Spoils from the Spoils pile.").expansion("DarkAges").build());
        actionCardsDarkAges.add(bandOfMisfits = new ActionCardImpl.Builder(Cards.Type.BandOfMisfits, 5).description("Play this as if it were an Action card in the supply costing less than it that you choose. This is that card until it leaves play.").expansion("DarkAges").build());
		actionCardsDarkAges.add(beggar = new ActionCardImpl.Builder(Cards.Type.Beggar, 2).description("Gain 3 Coppers, putting them into your hand. When another player plays an Attack card, you may discard this. If you do, gain two Silvers, putting one on top of your deck.").expansion("DarkAges").build());
		actionCardsDarkAges.add(catacombs = new ActionCardImpl.Builder(Cards.Type.Catacombs, 5).description("Look at the top 3 cards of your deck. Choose one: Put them into your hand; or discard them and +3 Cards. When you trash this, gain a cheaper card.").expansion("DarkAges").build());
        actionCardsDarkAges.add(count = new ActionCardImpl.Builder(Cards.Type.Count, 5).description("Choose one: Discard 2 cards; or put a card from your hand on top of your deck; or gain a Copper. Choose one: +3 coins; or trash your hand; or gain a Duchy.").expansion("DarkAges").build());
        actionCardsDarkAges.add(counterfeit = new TreasureCardImpl.Builder(Cards.Type.Counterfeit, 5, 1).description("+1 Buy\nWhen you play this, you may play a Treasure from your hand twice. If you do, trash that Treasure.").expansion("DarkAges").build());
        actionCardsDarkAges.add(cultist = new ActionCardImpl.Builder(Cards.Type.Cultist, 5).looter().addCards(2).attack().description("Each other player gains a Ruins. You may play a Cultist from your hand. When you trash this, +3 Cards.").expansion("DarkAges").build());
        actionCardsDarkAges.add(deathCart = new ActionCardImpl.Builder(Cards.Type.DeathCart, 4).addGold(5).looter().description("You may trash an Action card from your hand. If you don't, trash this. When you gain this, gain 2 Ruins.").expansion("DarkAges").build());
        actionCardsDarkAges.add(feodum = new VictoryCardImpl.Builder(Cards.Type.Feodum, 4, 0).description("Worth 1 VP for every 3 Silvers in your deck (round down). When you trash this, gain 3 Silvers.").expansion("DarkAges").build());
        actionCardsDarkAges.add(forager = new ActionCardImpl.Builder(Cards.Type.Forager, 3).addActions(1).addBuys(1).trashForced().description("Trash a card from your hand. +1 coin per differently named Treasure in the trash.").expansion("DarkAges").build());
        actionCardsDarkAges.add(fortress = new ActionCardImpl.Builder(Cards.Type.Fortress, 4).addCards(1).addActions(2).description("When you trash this, put it into your hand.").expansion("DarkAges").build());
        actionCardsDarkAges.add(graverobber = new ActionCardImpl.Builder(Cards.Type.Graverobber, 5).description("Choose one: Gain a card from the trash costing from 3 to 6 coins, putting it on top of your deck; or trash an Action card from your hand and gain a card costing up to 3 more than it.").expansion("DarkAges").build());
        actionCardsDarkAges.add(hermit = new ActionCardImpl.Builder(Cards.Type.Hermit, 3).description("Look through your discard pile. You may trash a card from your discard pile or hand that is not a Treasure. Gain a card costing up to 3 coins. When you discard this from play, if you did not buy any cards this turn, trash this and gain a Madman from the Madman pile.").expansion("DarkAges").build());
        actionCardsDarkAges.add(huntingGrounds = new ActionCardImpl.Builder(Cards.Type.HuntingGrounds, 6).addCards(4).description("When you trash this, gain a Duchy or 3 Estates.").expansion("DarkAges").build());
        actionCardsDarkAges.add(ironmonger = new ActionCardImpl.Builder(Cards.Type.Ironmonger, 4).addCards(1).addActions(1).description("Reveal the top card of your deck; you may discard it. Either way, if it is an Action card, +1 Action; Treasure card, + 1 coin; Victory card, +1 Card.").expansion("DarkAges").build());
        actionCardsDarkAges.add(junkDealer = new ActionCardImpl.Builder(Cards.Type.JunkDealer, 5).addCards(1).addActions(1).addGold(1).trashForced().description("Trash a card from your hand.").expansion("DarkAges").build());
        actionCardsDarkAges.add(marauder = new ActionCardImpl.Builder(Cards.Type.Marauder, 4).looter().attack().description("Gain a Spoils from the Spoils pile. Each other player gains a Ruins.").expansion("DarkAges").build());
        actionCardsDarkAges.add(marketSquare = new ActionCardImpl.Builder(Cards.Type.MarketSquare, 3).addCards(1).addActions(1).addBuys(1).description("When one of your cards is trashed, you may discard this from your hand. If you do, gain a Gold.").expansion("DarkAges").build());
        actionCardsDarkAges.add(mystic = new ActionCardImpl.Builder(Cards.Type.Mystic, 5).addActions(1).addGold(2).description("Name a card. Reveal the top card of your deck. If it’s the named card, put it into your hand.").expansion("DarkAges").build());
        actionCardsDarkAges.add(pillage = new ActionCardImpl.Builder(Cards.Type.Pillage, 5).trashOnUse().attack().description("Trash this. Each other player with 5 or more cards in hand reveals his hand and discards a card that you choose. Gain 2 Spoils from the Spoils pile.").expansion("DarkAges").build());
        actionCardsDarkAges.add(poorHouse = new ActionCardImpl.Builder(Cards.Type.PoorHouse, 1).addGold(4).description("Reveal your hand. -1 coin per treasure card in your hand, to a minimum of 0 coins.").expansion("DarkAges").build());
        actionCardsDarkAges.add(procession = new ActionCardImpl.Builder(Cards.Type.Procession, 4).description("You may play an Action card from your hand twice. Trash it. Gain an Action card costing exactly 1 more than it.").expansion("DarkAges").build());
        actionCardsDarkAges.add(rats = new ActionCardImpl.Builder(Cards.Type.Rats, 4).addCards(1).addActions(1).trashForced().description("Gain a Rats. Trash a card from your hand other than a Rats (or reveal a hand of all Rats).").expansion("DarkAges").build());
        actionCardsDarkAges.add(rebuild = new ActionCardImpl.Builder(Cards.Type.Rebuild, 5).addActions(1).description("Name a card. Reveal cards from the top of your deck until you reveal a Victory card that is not the named card. Discard the other cards. Trash the Victory card and gain a Victory card costing up to 3 more than it.").expansion("DarkAges").build());
        actionCardsDarkAges.add(rogue = new ActionCardImpl.Builder(Cards.Type.Rogue, 5).addGold(2).attack().description("If there are any cards in the trash costing from 3 to 6 coins, gain one of them. Otherwise, each other player reveals the top 2 cards of his deck, trashes one of them costing from 3 to 6 coins, and discards the rest.").expansion("DarkAges").build());
        actionCardsDarkAges.add(sage = new ActionCardImpl.Builder(Cards.Type.Sage, 3).addActions(1).description("Reveal cards from the top of your deck until you reveal one costing 3 coins or more. Put that card into your hand and discard the rest.").expansion("DarkAges").build());
        actionCardsDarkAges.add(scavenger = new ActionCardImpl.Builder(Cards.Type.Scavenger, 4).addGold(2).description("You may put your deck into your discard pile. Look through your discard pile and put one card from it on top of your deck.").expansion("DarkAges").build());
        actionCardsDarkAges.add(squire = new ActionCardImpl.Builder(Cards.Type.Squire, 2).addGold(1).description("Choose one: +2 Actions; or +2 Buys; or gain a Silver. - When you trash this, gain an Attack card.").expansion("DarkAges").build());
        actionCardsDarkAges.add(storeroom = new ActionCardImpl.Builder(Cards.Type.Storeroom, 3).addBuys(1).description("Discard any number of cards. +1 Card per card discarded. Discard any number of cards. +1 coin per card discarded the second time.").expansion("DarkAges").build());
        actionCardsDarkAges.add(urchin = new ActionCardImpl.Builder(Cards.Type.Urchin, 3).addCards(1).addActions(1).attack().description("Each other player discards down to 4 cards in hand. When you play another attack card with this in play, you may trash this. If you do, gain a Mercenary from the Mercenary pile.").expansion("DarkAges").build());
        actionCardsDarkAges.add(vagrant = new ActionCardImpl.Builder(Cards.Type.Vagrant, 2).addCards(1).addActions(1).description("Reveal the top card of your deck. If it's a Curse, Ruins, Shelter, or Victory card, put it into your hand.").expansion("DarkAges").build());
        actionCardsDarkAges.add(wanderingMinstrel = new ActionCardImpl.Builder(Cards.Type.WanderingMinstrel, 4).addCards(1).addActions(2).description("Reveal the top 3 cards of your deck. Put the Actions back on top in any order and discard the rest.").expansion("DarkAges").build());
        
        // Guilds
        actionCardsGuilds.add(advisor          = new ActionCardImpl.Builder(Cards.Type.Advisor, 4).addActions(1).description("Reveal the top 3 cards of your deck. The player to your left chooses one of them. Discard that card. Put the other cards into your hand.").expansion("Guilds").build());
        actionCardsGuilds.add(soothsayer       = new ActionCardImpl.Builder(Cards.Type.Soothsayer, 5).attack().description("Gain a Gold. Each other player gains a Curse. Each player who did draws a card.").expansion("Guilds").build());
        actionCardsGuilds.add(taxman           = new ActionCardImpl.Builder(Cards.Type.Taxman, 4).attack().description("You may trash a Treasure from your hand. Each other player with 5 or more cards in hand discards a copy of it (or reveals a hand without it). Gain a Treasure card costing up to $3 more than the trashed card, putting it on top of your deck.").expansion("Guilds").build());
        actionCardsGuilds.add(plaza            = new ActionCardImpl.Builder(Cards.Type.Plaza, 4).addCards(1).addActions(2).description("You may discard a Treasure card. If you do, take a Coin token.").expansion("Guilds").build());
        actionCardsGuilds.add(candlestickMaker = new ActionCardImpl.Builder(Cards.Type.CandlestickMaker, 2).addActions(1).addBuys(1).description("Take a Coin token.").expansion("Guilds").build());
        actionCardsGuilds.add(baker            = new ActionCardImpl.Builder(Cards.Type.Baker, 5).addCards(1).addActions(1).description("Take a Coin token. SETUP: Each Player takes a Coin token.").expansion("Guilds").build());
        actionCardsGuilds.add(merchantGuild    = new ActionCardImpl.Builder(Cards.Type.MerchantGuild, 5).addBuys(1).addGold(1).description("While this is in play, when you buy a card, take a Coin token.").expansion("Guilds").build());
        actionCardsGuilds.add(butcher          = new ActionCardImpl.Builder(Cards.Type.Butcher, 5).description("Take 2 Coin tokens. You may trash a card from your hand and then pay any number of Coin tokens. If you did trash a card, gain a card with a cost of up to the cost of the trashed card plus the number of Coin tokens you paid.").expansion("Guilds").build());
        actionCardsGuilds.add(journeyman       = new ActionCardImpl.Builder(Cards.Type.Journeyman, 5).description("Name a card. Reveal cards from the top of your deck until you reveal 3 cards that are not the named card. Put those cards into your hand and discard the rest.").expansion("Guilds").build());
        actionCardsGuilds.add(stonemason       = new ActionCardImpl.Builder(Cards.Type.StoneMason, 2).isOverpay().description("Trash a card from your hand, Gain 2 cards each costing less than it. When you buy this, you may overpay for it. If you do, gain 2 Action cards each costing the amount you overpaid.").expansion("Guilds").build());
        actionCardsGuilds.add(masterpiece      = new TreasureCardImpl.Builder(Cards.Type.Masterpiece, 3, 1).isOverpay().description("When you buy this, you may overpay for it. If you do, gain a Silver per $1 you overpaid.").expansion("Guilds").build());
        actionCardsGuilds.add(doctor           = new ActionCardImpl.Builder(Cards.Type.Doctor, 3).isOverpay().description("Name a card. Reveal the top 3 cards of your deck. Trash the matches. Put the rest back on top in any order.  When you buy this, you may overpay for it. For each $1 you overpaid, look at the top card of your deck; trash it, discard it, or put it back.").expansion("Guilds").build());
        actionCardsGuilds.add(herald           = new ActionCardImpl.Builder(Cards.Type.Herald, 4).addCards(1).addActions(1).isOverpay().description("Reveal the top card of your deck. If it is an Action, play it. When you buy this, you may overpay for it. For each $1 you overpaid, look through your discard pile and put a card from it on top of your deck.").expansion("Guilds").build());
        
        // Non-Supply Cards
        nonSupplyCards.add(madman    = new ActionCardImpl.Builder(Cards.Type.Madman, 0).addActions(2).description("Return this to the Madman pile. If you do, +1 Card per card in your hand. (This is not in the supply)").expansion("DarkAges").build());
        nonSupplyCards.add(mercenary = new ActionCardImpl.Builder(Cards.Type.Mercenary, 0).attack().description("You may trash 2 cards from your hand. If you do, +2 cards, +2 coins, and each other player discards down to 3 cards in hand. (This is not in the supply)").expansion("DarkAges").build());
        nonSupplyCards.add(spoils    = new TreasureCardImpl.Builder(Cards.Type.Spoils, 0, 3).description("When you play this, return it to the Spoils pile (This is not in the Supply).").expansion("DarkAges").build());
		
        // Ruins
        ruinsCards.add(abandonedMine     = new ActionCardImpl.Builder(Cards.Type.AbandonedMine, 0).addGold(1).isRuins().expansion("DarkAges").build());
        ruinsCards.add(ruinedLibrary     = new ActionCardImpl.Builder(Cards.Type.RuinedLibrary, 0).addCards(1).isRuins().expansion("DarkAges").build());
        ruinsCards.add(ruinedMarket      = new ActionCardImpl.Builder(Cards.Type.RuinedMarket, 0).addBuys(1).isRuins().expansion("DarkAges").build());
        ruinsCards.add(ruinedVillage     = new ActionCardImpl.Builder(Cards.Type.RuinedVillage, 0).addActions(1).isRuins().expansion("DarkAges").build());
        ruinsCards.add(survivors         = new ActionCardImpl.Builder(Cards.Type.Survivors, 0).isRuins().description("Look at the top 2 cards of your deck. Discard them or put them back in any order.").expansion("DarkAges").build());
        nonKingdomCards.add(virtualRuins = new CardImpl.Builder(Cards.Type.VirtualRuins, 0).isRuins().build());
        
        // Knights
        knightsCards.add(dameAnna = new ActionCardImpl.Builder(Cards.Type.DameAnna, 5).isKnight().description("You may trash up to 2 cards from your hand. " + KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(dameJosephine = new ActionVictoryCardImpl.Builder(Cards.Type.DameJosephine, 5).isKnight().vp(2).description(KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(dameMolly = new ActionCardImpl.Builder(Cards.Type.DameMolly, 5).isKnight().addActions(2).description(KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(dameNatalie = new ActionCardImpl.Builder(Cards.Type.DameNatalie, 5).isKnight().description("You may gain a card costing up to 3 coins. " + KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(dameSylvia = new ActionCardImpl.Builder(Cards.Type.DameSylvia, 5).isKnight().addGold(2).description(KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(sirBailey = new ActionCardImpl.Builder(Cards.Type.SirBailey, 5).isKnight().addActions(1).addCards(1).description(KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(sirDestry = new ActionCardImpl.Builder(Cards.Type.SirDestry, 5).isKnight().addCards(2).description(KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(sirMartin = new ActionCardImpl.Builder(Cards.Type.SirMartin, 4).isKnight().addBuys(2).description(KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(sirMichael = new ActionCardImpl.Builder(Cards.Type.SirMichael, 5).isKnight().description("Each other player discards down to 3 cards in hand. " + KNIGHTS_TEXT).expansion("DarkAges").build());
        knightsCards.add(sirVander = new ActionCardImpl.Builder(Cards.Type.SirVander, 5).isKnight().description(KNIGHTS_TEXT + " When you trash this, gain a Gold.").expansion("DarkAges").build());
        actionCardsDarkAges.add(virtualKnight = new ActionCardImpl.Builder(Cards.Type.VirtualKnight, 5).isKnight().build());
        
        // Shelters
        nonKingdomCards.add(necropolis = new ActionCardImpl.Builder(Cards.Type.Necropolis, 1).addActions(2).isShelter().expansion("DarkAges").build());
        nonKingdomCards.add(overgrownEstate = new VictoryCardImpl.Builder(Cards.Type.OvergrownEstate, 1, 0).isShelter().description("When you trash this, +1 Card").expansion("DarkAges").build());
        nonKingdomCards.add(hovel = new ReactionCardImpl.Builder(Cards.Type.Hovel,1).isShelter().description("When you buy a Victory card, you may trash this from your hand").expansion("DarkAges").build());
        
        // Adventures
        actionCardsAdventures.add(amulet          = new ActionDurationCardImpl.Builder(Cards.Type.Amulet, 3).description("Now and at the start of your next turn, choose one: +$1; or trash a card from your hand; or gain a Silver.").build());
        actionCardsAdventures.add(artificer       = new ActionCardImpl.Builder(Cards.Type.Artificer     , 5).addCards(1).addActions(1).addGold(1).description("Discard any number of cards. You may gain a card costing exactly $1 per card discarded, putting it on top of your deck.").build());
        actionCardsAdventures.add(bridgeTroll     = new ActionDurationCardImpl.Builder(Cards.Type.BridgeTroll   , 3).description("Each other player takes his -$1 token. Now and at the start of your next turn: +1 Buy. ~ While this is on play, cards cost $1 less on your turns, but not less than $0.").build());
        actionCardsAdventures.add(caravanGuard    = new ActionDurationCardImpl.Builder(Cards.Type.CaravanGuard, 2).addCards(1).addActions(1).attack().description("At the start of your next turn, +$1. ~ When another player plays an Attack card, you may play this from your hand. (+1 Action has no effect if it's not your turn.)").build());
        actionCardsAdventures.add(coinoftheRealm  = new TreasureCardImpl.Builder(Cards.Type.CoinoftheRealm, 5, 1).isReserve().description("When you play this, put it on your Tavern mat. ~ Directly after resolving an Action, you may call this, for +2 Actions.").build());
        actionCardsAdventures.add(distantLands    = new ActionVictoryCardImpl.Builder(Cards.Type.DistantLands  , 3).isReserve().description("Put this on your Tavern mat. ~ Worth 4 VP if on your Tavern mat at the end of the game (otherwise worth 0 VP).").build());
        actionCardsAdventures.add(dungeon         = new ActionDurationCardImpl.Builder(Cards.Type.Dungeon, 4).addActions(1).description("Now and at the start of your next turn: +2 Cards, then discard 2 cards.").build());
        actionCardsAdventures.add(duplicate       = new ActionCardImpl.Builder(Cards.Type.Duplicate, 3).isReserve().description("Put this on your Tavern mat. ~ When you gain a card costing up to $6, you may call this, to gain a copy of that card.").build());
        actionCardsAdventures.add(gear            = new ActionDurationCardImpl.Builder(Cards.Type.Gear, 3).addCards(2).description("Set aside up to 2 cards from your hand face down. At the start of your next turn, put them into your hand.").build());
        actionCardsAdventures.add(giant           = new ActionCardImpl.Builder(Cards.Type.Giant         , 5).attack().description("Turn your Journey token over (it starts face up). If it's face down, +$1. If it's face up, +$5, and each other player reveals the top card of his deck, trashes it if it costs from $3 to $6, and otherwise discards it and gains a Curse.").build());
        actionCardsAdventures.add(guide           = new ActionCardImpl.Builder(Cards.Type.Guide, 3).addCards(1).addActions(1).isReserve().description("Put this on your Tavern mat. ~ At the start of your turn, you may call this, to discard your hand and draw 5 cards.").build());
        actionCardsAdventures.add(hauntedWoods    = new ActionDurationCardImpl.Builder(Cards.Type.HauntedWoods  , 5).attack().description("Until your next turn, when any other player buys a card, he puts his hand on top of his deck in any order. At the start of your next turn, +3 Cards.").build());
        actionCardsAdventures.add(hireling        = new ActionDurationCardImpl.Builder(Cards.Type.Hireling      , 6).description("At the start of each of your turns for the rest of the game: +1 Card. (This stays in play.)").build());
        actionCardsAdventures.add(lostCity        = new ActionCardImpl.Builder(Cards.Type.LostCity      , 5).addCards(2).description("When you gain this, each other player draws a card.").build());
        actionCardsAdventures.add(magpie          = new ActionCardImpl.Builder(Cards.Type.Magpie        , 4).addCards(1).addActions(1).description("Reveal the top card of your deck. If it's a Treasure, put it into your hand. If it's an Action or Victory card, gain a Magpie.").build());
        actionCardsAdventures.add(messenger       = new ActionCardImpl.Builder(Cards.Type.Messenger     , 4).addGold(2).addBuys(1).description("You may put your deck into your discard pile. ~ When this is your first buy in a turn, gain a card costing up to $4, and each other player gains a copy of it.").build());
        actionCardsAdventures.add(miser           = new ActionCardImpl.Builder(Cards.Type.Miser         , 4).description("Choose one: Put a Copper from your hand onto your Tavern mat; or +$1 per Copper on your Tavern mat.").build());
        actionCardsAdventures.add(page            = new ActionCardImpl.Builder(Cards.Type.Page, 2).addCards(1).addActions(1).description("When you discard this from play, you may exchange it for a Treasure Hunter.").build());
        actionCardsAdventures.add(peasant         = new ActionCardImpl.Builder(Cards.Type.Peasant, 2).addGold(1).addBuys(1).description("When you discard this from play, you may exchange it for a Soldier.").build());
        actionCardsAdventures.add(port            = new ActionCardImpl.Builder(Cards.Type.Port          , 4).addCards(1).description("When you buy this, gain another Port.").build());
        actionCardsAdventures.add(ranger          = new ActionCardImpl.Builder(Cards.Type.Ranger        , 4).addBuys(1).description("Turn your Journey token over (it starts face up). If it's face up, +5 Cards.").build());
        actionCardsAdventures.add(ratcatcher      = new ActionCardImpl.Builder(Cards.Type.Ratcatcher, 2).addCards(1).addActions(1).isReserve().description("Put this on your Tavern mat. ~ At the start of your turn, you may call this, to trash a card from your hand.").build());
        actionCardsAdventures.add(raze            = new ActionCardImpl.Builder(Cards.Type.Raze, 2).addActions(1).addBuys(1).description("Trash this or a card from your hand. Look at a number of cards from the top of your deck equal to the cost in $ of the trashed card. Put one into your hand and discard the rest.").build());
        actionCardsAdventures.add(relic           = new TreasureCardImpl.Builder(Cards.Type.Relic         , 5, 2).attack().description("When you play this, each other player puts his -1 Card token on his deck.").build());
        actionCardsAdventures.add(royalCarriage   = new ActionCardImpl.Builder(Cards.Type.RoyalCarriage , 5).addActions(1).isReserve().description("Put this on your Tavern mat. ~ Directly after resolving an Action, if it's still in play, you may call this, to replay that Action.").build());
        actionCardsAdventures.add(storyteller     = new ActionCardImpl.Builder(Cards.Type.Storyteller   , 5).addActions(1).description("Play up to 3 Treasures from your hand. Pay all of your $; +1 Card per $ paid.").build());
        actionCardsAdventures.add(swampHag        = new ActionDurationCardImpl.Builder(Cards.Type.SwampHag      , 5).attack().description("Until your next turn, when any other player buys a card, he gains a Curse. At the start of your next turn, +$3.").build());
        actionCardsAdventures.add(transmogrify    = new ActionCardImpl.Builder(Cards.Type.Transmogrify  , 4).addActions(1).isReserve().description("Put this on your Tavern mat. ~ At the start of your turn, you may call this, to trash a card from your hand, gain a card costing up to $1 more than it, and put that card into your hand.").build());
        actionCardsAdventures.add(treasureTrove   = new TreasureCardImpl.Builder(Cards.Type.TreasureTrove , 5, 2).description("When you play this, gain a Gold and a Copper.").build());
        actionCardsAdventures.add(wineMerchant    = new ActionCardImpl.Builder(Cards.Type.WineMerchant  , 5).addGold(4).addBuys(1).isReserve().description("Put this on your Tavern mat. ~ At the end of your Buy phase, if you have at least $2 unspent, you may discard this from your Tavern mat.").build());
        
        // events
        eventsCards.add(alms             = new ActionCardImpl.Builder(Cards.Type.Alms            , 0).isEvent().description("Once per turn: If you have no Treasures in play, gain a card costing up to $4.").expansion("Adventures").build());
        eventsCards.add(ball             = new ActionCardImpl.Builder(Cards.Type.Ball            , 5).isEvent().description("Take your -$1 token. Gain 2 cards each costing up to $4.").expansion("Adventures").build());
        eventsCards.add(bonfire          = new ActionCardImpl.Builder(Cards.Type.Bonfire         , 3).isEvent().description("Trash up to 2 cards you have in play.").expansion("Adventures").build());
        eventsCards.add(borrow           = new ActionCardImpl.Builder(Cards.Type.Borrow          , 0).addBuys(1).isEvent().description("Once per turn: If your -1 Card token isn't on your deck, put it there and +$1.").expansion("Adventures").build());
        eventsCards.add(expedition       = new ActionCardImpl.Builder(Cards.Type.Expedition      , 3).isEvent().description("Draw 2 extra cards for your next hand.").expansion("Adventures").build());
        eventsCards.add(ferry            = new ActionCardImpl.Builder(Cards.Type.Ferry           , 3).isEvent().description("Move your -$2 cost token to an Action Supply pile (cards from that pile cost $2 less on your turns, but not less than $0).").expansion("Adventures").build());
        eventsCards.add(inheritance      = new ActionCardImpl.Builder(Cards.Type.Inheritance     , 7).isEvent().description("Once per game: Set aside a non-Victory Action card from the Supply costing up to $4. Move your Estate token to it (your Estates gain the abilities and types of that card).").expansion("Adventures").build());
        eventsCards.add(lostArts         = new ActionCardImpl.Builder(Cards.Type.LostArts        , 6).isEvent().description("Move your +1 Action token to an Action Supply pile (when you play a card from that pile, you first get +1 Action).").expansion("Adventures").build());
        eventsCards.add(mission          = new ActionCardImpl.Builder(Cards.Type.Mission         , 4).isEvent().description("Once per turn: If the previous turn wasn't yours, take another turn after this one, in which you can't buy cards.").expansion("Adventures").build());
        eventsCards.add(pathfinding      = new ActionCardImpl.Builder(Cards.Type.Pathfinding     , 8).isEvent().description("Move your +1 Card token to an Action Supply pile (when you play a card from that pile, you first get +1 Card).").expansion("Adventures").build());
        eventsCards.add(pilgrimage       = new ActionCardImpl.Builder(Cards.Type.Pilgrimage      , 4).isEvent().description("Once per turn: Turn your Journey token over (it starts face up); then if it's face up, choose up to 3 differently named cards you have in play and gain a copy of each.").expansion("Adventures").build());
        eventsCards.add(plan             = new ActionCardImpl.Builder(Cards.Type.Plan            , 3).isEvent().description("Move your Trashing token to an Action Supply pile (when you buy a card from that pile, you may trash a card from your hand.)").expansion("Adventures").build());
        eventsCards.add(quest            = new ActionCardImpl.Builder(Cards.Type.Quest           , 0).isEvent().description("You may discard an Attack, two Curses, or six cards. If you do, gain a Gold.").expansion("Adventures").build());
        eventsCards.add(raid             = new ActionCardImpl.Builder(Cards.Type.Raid            , 5).isEvent().description("Gain a Silver per Silver you have in play. Each other player puts his -1 Card token on his deck.").expansion("Adventures").build());
        eventsCards.add(save             = new ActionCardImpl.Builder(Cards.Type.Save            , 1).addBuys(1).isEvent().description("Once per turn: Set aside a card from your hand, and put it into your hand at end of turn (after drawing).").expansion("Adventures").build());
        eventsCards.add(scoutingParty    = new ActionCardImpl.Builder(Cards.Type.ScoutingParty   , 2).addBuys(1).isEvent().description("Look at the top 5 cards of your deck. Discard 3 and put the rest back in any order.").expansion("Adventures").build());
        eventsCards.add(seaway           = new ActionCardImpl.Builder(Cards.Type.Seaway          , 5).isEvent().description("Gain an Action card costing up to $4. Move your +1 Buy token to its pile (when you play a card from that pile, you first get +1 Buy).").expansion("Adventures").build());
        eventsCards.add(trade            = new ActionCardImpl.Builder(Cards.Type.Trade           , 5).isEvent().description("Trash up to 2 cards from your hand. Gain a Silver per card you trashed.").expansion("Adventures").build());
        eventsCards.add(training         = new ActionCardImpl.Builder(Cards.Type.Training        , 6).isEvent().description("Move your +$1 token to an Action Supply pile (when you play a card from that pile, you first get +$1).").expansion("Adventures").build());
        eventsCards.add(travellingFair   = new ActionCardImpl.Builder(Cards.Type.TravellingFair  , 2).addBuys(2).isEvent().description("When you gain a card this turn, you may put it on top of your deck.").expansion("Adventures").build());
        nonKingdomCards.add(virtualEvents = new CardImpl.Builder(Cards.Type.VirtualEvents        , 0).isEvent().build());

        // Travellers        
        travellersCards.add(champion        = new ActionDurationCardImpl.Builder(Cards.Type.Champion, 6).addActions(1).isTraveller().description("For the rest of the game, when another player plays an Attack, it doesn't affect you, and when you play an Action, +1 Action. (This stays in play.)").expansion("Adventures").build());
        travellersCards.add(disciple        = new ActionCardImpl.Builder(Cards.Type.Disciple        , 5).isTraveller().description("You may play an Action card from your hand twice. Gain a copy of it. ~ When you discard this from play, you may exchange it for a Teacher.").expansion("Adventures").build());
        travellersCards.add(fugitive        = new ActionCardImpl.Builder(Cards.Type.Fugitive        , 4).addCards(2).addActions(1).isTraveller().description("Discard a card. ~ When you discard this from play, you may exchange it for a Disciple.").expansion("Adventures").build());
        travellersCards.add(hero            = new ActionCardImpl.Builder(Cards.Type.Hero            , 5).addGold(2).isTraveller().description("When you discard this from play, you may exchange it for a Champion.").expansion("Adventures").build());
        travellersCards.add(soldier         = new ActionCardImpl.Builder(Cards.Type.Soldier         , 3).addGold(2).isTraveller().attack().description("+$1 per other Attack you have in play. Each other player with 4 or more cards in hand discards a card. ~ When you discard this from play, you may exchange it for a Fugitive.").expansion("Adventures").build());
        travellersCards.add(teacher         = new ActionCardImpl.Builder(Cards.Type.Teacher         , 6).isTraveller().isReserve().description("Put this on your Tavern mat. ~ At the start of your turn, you may call this, to move your +1 Card, +1 Action, +1 Buy, or +$1 token to an Action Supply pile you have no tokens on (when you play a card from that pile, you first get that bonus).").expansion("Adventures").build());
        travellersCards.add(treasureHunter  = new ActionCardImpl.Builder(Cards.Type.TreasureHunter  , 3).addGold(1).addActions(1).isTraveller().description("Gain a Silver per card the player to your right gained in his last turn. ~ When you discard this from play, you may exchange it for a Warrior.").expansion("Adventures").build());
        travellersCards.add(warrior         = new ActionCardImpl.Builder(Cards.Type.Warrior         , 4).addCards(1).isTraveller().attack().description("For each Traveller you have in play (including this), each other player discards the top card of his deck and trashes it if it costs $3 or $4. ~ When you discard this from play, you may exchange it for a Hero.").expansion("Adventures").build());
        nonKingdomCards.add(virtualTravellers = new ActionCardImpl.Builder(Cards.Type.VirtualTraveller, 5).isTraveller().build());

        // Promo Cards (Incomplete)
        actionCardsPromo.add(walledVillage = new ActionCardImpl.Builder(Cards.Type.WalledVillage, 4).addCards(1).addActions(2).description("At the start of Clean-up, if you have this and no more than one other Action card in play, you may put this on top of your deck.").expansion("Promo").build());
        actionCardsPromo.add(governor      = new ActionCardImpl.Builder(Cards.Type.Governor, 5).addActions(1).description("Choose one; you get the version in parentheses: Each player gets +1 (+3) Cards; or each player gains a Silver (Gold); or each player may trash a card from his hand and gain a card costing exactly 1 (2) more.").expansion("Promo").build());
        actionCardsPromo.add(envoy         = new ActionCardImpl.Builder(Cards.Type.Envoy, 4).description("Reveal the top 5 cards of your deck. The player to your left chooses one for you to discard. Draw the rest.").expansion("Promo").build());
        
        // Collect all Expansions
        for (Card card : actionCardsBaseGame)    { actionCards.add(card); }
        for (Card card : actionCardsIntrigue)    { actionCards.add(card); }
        for (Card card : actionCardsSeaside)     { actionCards.add(card); }
        for (Card card : actionCardsAlchemy)     { actionCards.add(card); }
        for (Card card : actionCardsProsperity)  { actionCards.add(card); }
        for (Card card : actionCardsCornucopia)  { actionCards.add(card); }
        for (Card card : actionCardsHinterlands) { actionCards.add(card); }
        for (Card card : actionCardsDarkAges)    { actionCards.add(card); }
        for (Card card : actionCardsGuilds)      { actionCards.add(card); }
        for (Card card : actionCardsAdventures)  { actionCards.add(card); }
        for (Card card : actionCardsPromo)       { actionCards.add(card); }
    }
    
    public static boolean isKingdomCard(Card c) {
        return !nonKingdomCards.contains(c);
    }
    
    public static boolean isSupplyCard(Card c) {
        return !(nonSupplyCards.contains(c) || prizeCards.contains(c));
    }
    
    public static boolean isReaction(Card c) {
        if ((c.equals(Cards.moat)) 
        		|| (c.equals(Cards.secretChamber)) 
        		|| (c.equals(Cards.watchTower)) 
        		|| (c.equals(Cards.horseTraders)) 
        		|| (c.equals(Cards.foolsGold)) 
        		|| (c.equals(Cards.trader)) 
        		|| (c.equals(Cards.tunnel)) 
        		|| (c.equals(Cards.beggar)) 
        		|| (c.equals(Cards.hovel)) 
        		|| (c.equals(Cards.marketSquare))
        		|| (c.equals(Cards.caravanGuard))
        		) {
        	return true;
        }
        return false;
    }
}
