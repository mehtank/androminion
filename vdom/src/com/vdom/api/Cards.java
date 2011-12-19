package com.vdom.api;

import com.vdom.core.ActionCardImpl;
import com.vdom.core.ActionDurationCardImpl;
import com.vdom.core.ActionVictoryCardImpl;
import com.vdom.core.TreasureCardImpl;
import com.vdom.core.VictoryCardImpl;
import com.vdom.core.VictoryTreasureCardImpl;

public class Cards {
    public static final Card platinum;
    public static final Card gold;
    public static final Card silver;
    public static final Card copper;
    public static final Card potion;

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

    static {
        platinum = new TreasureCardImpl("Platinum", 9, 5, false, false);
        gold = new TreasureCardImpl("Gold", 6, 3, false, false);
        silver = new TreasureCardImpl("Silver", 3, 2, false, false);
        copper = new TreasureCardImpl("Copper", 0, 1, false, false);
        potion = new TreasureCardImpl("Potion", 4, 0, false, true);
        philosophersStone = new TreasureCardImpl("Philosopher's Stone", 3, 0, true, false);

        colony = new VictoryCardImpl("Colony", 11, 10, false);
        province = new VictoryCardImpl("Province", 8, 6, false);
        duchy = new VictoryCardImpl("Duchy", 5, 3, false);
        estate = new VictoryCardImpl("Estate", 2, 1, false);

        gardens = new VictoryCardImpl("Gardens", 4, 0, false);
        curse = new VictoryCardImpl("Curse", 0, -1, false);

        workshop = new ActionCardImpl.Builder("Workshop", 3).build();
        woodcutter = new ActionCardImpl.Builder("Woodcutter", 3).addBuys(1).addGold(2).build();
        witch = new ActionCardImpl.Builder("Witch", 5).addCards(2).attack(true).build();
        village = new ActionCardImpl.Builder("Village", 3).addCards(1).addActions(2).build();
        throneRoom = new ActionCardImpl.Builder("Throne Room", 4).dontAutoRecycleOnUse(true).build();
        thief = new ActionCardImpl.Builder("Thief", 4).attack(true).build();
        spy = new ActionCardImpl.Builder("Spy", 4).addCards(1).addActions(1).attack(true).build();
        smithy = new ActionCardImpl.Builder("Smithy", 4).addCards(3).build();
        remodel = new ActionCardImpl.Builder("Remodel", 4).build();
        moneyLender = new ActionCardImpl.Builder("Money Lender", 4).build();
        mine = new ActionCardImpl.Builder("Mine", 5).build();
        militia = new ActionCardImpl.Builder("Militia", 4).addGold(2).attack(true).build();
        market = new ActionCardImpl.Builder("Market", 5).addActions(1).addBuys(1).addGold(1).addCards(1).build();
        library = new ActionCardImpl.Builder("Library", 5).build();
        laboratory = new ActionCardImpl.Builder("Laboratory", 5).addActions(1).addCards(2).build();
        festival = new ActionCardImpl.Builder("Festival", 5).addActions(2).addBuys(1).addGold(2).build();
        feast = new ActionCardImpl.Builder("Feast", 4).dontAutoRecycleOnUse(true).build();
        councilRoom = new ActionCardImpl.Builder("Council Room", 5).addCards(4).addBuys(1).build();
        chapel = new ActionCardImpl.Builder("Chapel", 2).build();
        chancellor = new ActionCardImpl.Builder("Chancellor", 3).addGold(2).build();
        cellar = new ActionCardImpl.Builder("Cellar", 2).addActions(1).build();
        bureaucrat = new ActionCardImpl.Builder("Bureaucrat", 4).attack(true).build();
        adventurer = new ActionCardImpl.Builder("Adventurer", 6).build();
        moat = new ActionCardImpl.Builder("Moat", 2).addCards(2).build();

        warehouse = new ActionCardImpl.Builder("Warehouse", 3).addCards(3).addActions(1).build();
        ambassador = new ActionCardImpl.Builder("Ambassador", 3).attack(true).build();
        lookout = new ActionCardImpl.Builder("Lookout", 3).addActions(1).build();
        embargo = new ActionCardImpl.Builder("Embargo", 2).addGold(2).dontAutoRecycleOnUse(true).build();
        navigator = new ActionCardImpl.Builder("Navigator", 4).addGold(2).build();
        treasury = new ActionCardImpl.Builder("Treasury", 5).addCards(1).addActions(1).addGold(1).build();
        haven = new ActionDurationCardImpl.Builder("Haven", 2).addCards(1).addActions(1).build();
        treasureMap = new ActionCardImpl.Builder("Treasure Map", 4).dontAutoRecycleOnUse(true).build();
        pearlDiver = new ActionCardImpl.Builder("Pearl Diver", 2).addCards(1).addActions(1).build();
        explorer = new ActionCardImpl.Builder("Explorer", 5).build();
        smugglers = new ActionCardImpl.Builder("Smugglers", 3).build();
        bazaar = new ActionCardImpl.Builder("Bazaar", 5).addCards(1).addActions(2).addGold(1).build();
        cutpurse = new ActionCardImpl.Builder("Cutpurse", 4).addGold(2).attack(true).build();
        island = new ActionVictoryCardImpl.Builder("Island", 4).vp(2).dontAutoRecycleOnUse(true).build();
        nativeVillage = new ActionCardImpl.Builder("Native Village", 2).addActions(2).build();
        pirateShip = new ActionCardImpl.Builder("Pirate Ship", 4).attack(true).build();
        salvager = new ActionCardImpl.Builder("Salvager", 4).addBuys(1).build();
        ghostShip = new ActionCardImpl.Builder("Ghost Ship", 5).addCards(2).attack(true).build();
        seaHag = new ActionCardImpl.Builder("Sea Hag", 4).attack(true).build();
        tactician = new ActionDurationCardImpl.Builder("Tactician", 5).addCardsNextTurn(5).addBuysNextTurn(1).addActionsNextTurn(1).dontAutoRecycleOnUse(true)
            .build();
        caravan = new ActionDurationCardImpl.Builder("Caravan", 4).addCardsNextTurn(1).addCards(1).addActions(1).build();
        lighthouse = new ActionDurationCardImpl.Builder("Lighthouse", 2).addGoldNextTurn(1).addActions(1).addGold(1).build();
        fishingVillage = new ActionDurationCardImpl.Builder("Fishing Village", 3).addGoldNextTurn(1).addActionsNextTurn(1).addActions(2).addGold(1).build();
        wharf = new ActionDurationCardImpl.Builder("Wharf", 5).addCardsNextTurn(2).addBuysNextTurn(1).addCards(2).addBuys(1).build();
        merchantShip = new ActionDurationCardImpl.Builder("Merchant Ship", 5).addGoldNextTurn(2).addGold(2).build();
        outpost = new ActionDurationCardImpl.Builder("Outpost", 5).takeAnotherTurn(true).build();

        duke = new VictoryCardImpl("Duke", 5, 0, false);

        greatHall = new ActionVictoryCardImpl.Builder("Great Hall", 3).addCards(1).addActions(1).vp(1).build();
        secretChamber = new ActionCardImpl.Builder("Secret Chamber", 2).build();
        tribute = new ActionCardImpl.Builder("Tribute", 5).build();
        upgrade = new ActionCardImpl.Builder("Upgrade", 5).addCards(1).addActions(1).build();
        wishingWell = new ActionCardImpl.Builder("Wishing Well", 3).addCards(1).addActions(1).build();
        tradingPost = new ActionCardImpl.Builder("Trading Post", 5).build();
        nobles = new ActionVictoryCardImpl.Builder("Nobles", 6).vp(2).build();
        swindler = new ActionCardImpl.Builder("Swindler", 3).addGold(2).attack(true).build();
        steward = new ActionCardImpl.Builder("Steward", 3).build();
        scout = new ActionCardImpl.Builder("Scout", 4).addActions(1).build();
        shantyTown = new ActionCardImpl.Builder("Shanty Town", 3).addActions(2).build();
        saboteur = new ActionCardImpl.Builder("Saboteur", 5).attack(true).build();
        pawn = new ActionCardImpl.Builder("Pawn", 2).build();
        minion = new ActionCardImpl.Builder("Minion", 5).addActions(1).attack(true).build();
        miningVillage = new ActionCardImpl.Builder("Mining Village", 4).addCards(1).addActions(2).dontAutoRecycleOnUse(true).build();
        masquerade = new ActionCardImpl.Builder("Masquerade", 3).addCards(2).build();
        ironworks = new ActionCardImpl.Builder("Ironworks", 4).build();
        coppersmith = new ActionCardImpl.Builder("Coppersmith", 4).build();
        conspirator = new ActionCardImpl.Builder("Conspirator", 4).addGold(2).build();
        bridge = new ActionCardImpl.Builder("Bridge", 4).addBuys(1).addGold(1).build();
        baron = new ActionCardImpl.Builder("Baron", 4).addBuys(1).build();
        courtyard = new ActionCardImpl.Builder("Courtyard", 2).addCards(3).build();
        harem = new VictoryTreasureCardImpl("Harem", 6, 2, 2, false);
        torturer = new ActionCardImpl.Builder("Torturer", 5).addCards(3).attack(true).build();

        alchemist = new ActionCardImpl.Builder("Alchemist", 3).addActions(1).addCards(2).costsPotion().build();
        apothecary = new ActionCardImpl.Builder("Apothecary", 2).addActions(1).addCards(1).costsPotion().build();
        apprentice = new ActionCardImpl.Builder("Apprentice", 5).addActions(1).build();
        familiar = new ActionCardImpl.Builder("Familiar", 3).addCards(1).addActions(1).attack(true).costsPotion().build();
        golem = new ActionCardImpl.Builder("Golem", 4).costsPotion().build();
        herbalist = new ActionCardImpl.Builder("Herbalist", 2).addBuys(1).addGold(1).build();
        possession = new ActionCardImpl.Builder("Possession", 6).costsPotion().build();
        scryingPool = new ActionCardImpl.Builder("Scrying Pool", 2).addActions(1).costsPotion().build();
        transmute = new ActionCardImpl.Builder("Transmute", 0).costsPotion().build();
        university = new ActionCardImpl.Builder("University", 2).addActions(2).costsPotion().build();
        vineyard = new VictoryCardImpl("Vineyard", 0, 0, true);

        bank = new TreasureCardImpl("Bank", 7, 0, false, false);
        contraband = new TreasureCardImpl("Contraband", 5, 3, false, false);
        hoard = new TreasureCardImpl("Hoard", 6, 2, false, false);
        loan = new TreasureCardImpl("Loan", 3, 1, false, false);
        quarry = new TreasureCardImpl("Quarry", 4, 1, false, false);
        royalSeal = new TreasureCardImpl("Royal Seal", 5, 2, false, false);
        talisman = new TreasureCardImpl("Talisman", 4, 1, false, false);
        venture = new TreasureCardImpl("Venture", 5, 1, false, false);

        bishop = new ActionCardImpl.Builder("Bishop", 4).addGold(1).addVictoryTokens(1).build();
        city = new ActionCardImpl.Builder("City", 5).addActions(2).addCards(1).build();
        countingHouse = new ActionCardImpl.Builder("Counting House", 5).build();
        expand = new ActionCardImpl.Builder("Expand", 7).build();
        forge = new ActionCardImpl.Builder("Forge", 7).build();
        goons = new ActionCardImpl.Builder("Goons", 6).addBuys(1).addGold(2).attack(true).build();
        grandMarket = new ActionCardImpl.Builder("Grand Market", 6).addCards(1).addActions(1).addBuys(1).addGold(2).build();
        kingsCourt = new ActionCardImpl.Builder("King's Court", 7).build();
        mint = new ActionCardImpl.Builder("Mint", 5).build();
        monument = new ActionCardImpl.Builder("Monument", 4).addGold(2).addVictoryTokens(1).build();
        mountebank = new ActionCardImpl.Builder("Mountebank", 5).addGold(2).attack(true).build();
        peddler = new ActionCardImpl.Builder("Peddler", 8).addActions(1).addCards(1).addGold(1).build();
        rabble = new ActionCardImpl.Builder("Rabble", 5).addCards(3).attack(true).build();
        tradeRoute = new ActionCardImpl.Builder("Trade Route", 3).addBuys(1).build();
        vault = new ActionCardImpl.Builder("Vault", 5).addCards(2).build();
        watchTower = new ActionCardImpl.Builder("Watch Tower", 3).build();
        workersVillage = new ActionCardImpl.Builder("Worker's Village", 4).addCards(1).addActions(2).addBuys(1).build();
        
        hornOfPlenty = new TreasureCardImpl("Horn of Plenty", 5, 0, false, false);
        
        fairgrounds = new VictoryCardImpl("Fairgrounds", 6, 0, false);
        
        farmingVillage = new ActionCardImpl.Builder("Farming Village", 4).addActions(2).build();
        fortuneTeller = new ActionCardImpl.Builder("Fortune Teller", 3).addGold(2).attack(true).build();
        hamlet = new ActionCardImpl.Builder("Hamlet", 2).addActions(1).addCards(1).build();
        harvest = new ActionCardImpl.Builder("Harvest", 5).build();
        horseTraders = new ActionCardImpl.Builder("Horse Traders", 4).addBuys(1).addGold(3).build();
        huntingParty = new ActionCardImpl.Builder("Hunting Party", 5).addActions(1).addCards(1).build();
        jester = new ActionCardImpl.Builder("Jester", 5).addGold(2).attack(true).build();
        menagerie = new ActionCardImpl.Builder("Menagerie", 3).addActions(1).build();
        remake = new ActionCardImpl.Builder("Remake", 4).build();
        tournament = new ActionCardImpl.Builder("Tournament", 4).addActions(1).build();
        youngWitch = new ActionCardImpl.Builder("Young Witch", 4).addCards(2).attack(true).build();
        
        diadem = new TreasureCardImpl("Diadem", 0, 2, false, false).setIsPrize();
        
        bagOfGold = new ActionCardImpl.Builder("Bag of Gold", 0).addActions(1).isPrize().build();
        followers = new ActionCardImpl.Builder("Followers", 0).addCards(2).attack(true).isPrize().build();
        princess = new ActionCardImpl.Builder("Princess", 0).addBuys(1).isPrize().build();
        trustySteed = new ActionCardImpl.Builder("Trusty Steed", 0).isPrize().build();
        
        borderVillage = new ActionCardImpl.Builder("Border Village", 6).addCards(1).addActions(2).build();
        cache = new TreasureCardImpl("Cache", 5, 3, false, false);
        cartographer = new ActionCardImpl.Builder("Cartographer", 5).addCards(1).addActions(1).build();
        crossroads = new ActionCardImpl.Builder("Crossroads", 2).build();
        develop = new ActionCardImpl.Builder("Develop", 3).build();
        duchess = new ActionCardImpl.Builder("Duchess", 2).addGold(2).build();
        embassy = new ActionCardImpl.Builder("Embassy", 5).addCards(5).build();
        farmland = new VictoryCardImpl("Farmland", 6, 2, false);
        foolsGold = new TreasureCardImpl("Fool's Gold", 2, 1, false, false);
        haggler = new ActionCardImpl.Builder("Haggler", 5).addGold(2).build();
        highway = new ActionCardImpl.Builder("Highway", 5).addCards(1).addActions(1).build();
        illGottenGains = new TreasureCardImpl("Ill-Gotten Gains", 5, 1, false, false);
        inn = new ActionCardImpl.Builder("Inn", 5).addCards(2).addActions(2).build();
        jackOfAllTrades = new ActionCardImpl.Builder("Jack of all Trades", 4).build();
        mandarin = new ActionCardImpl.Builder("Mandarin", 5).addGold(3).build();
        margrave = new ActionCardImpl.Builder("Margrave", 5).addCards(3).addBuys(1).attack(true).build();
        nobleBrigand = new ActionCardImpl.Builder("Noble Brigand", 4).addGold(1).attack(true).build();
        nomadCamp = new ActionCardImpl.Builder("Nomad Camp", 4).addBuys(1).addGold(2).build();
        oasis = new ActionCardImpl.Builder("Oasis", 3).addCards(1).addActions(1).addGold(1).build();
        oracle = new ActionCardImpl.Builder("Oracle", 3).attack(true).build();
        scheme = new ActionCardImpl.Builder("Scheme", 3).addCards(1).addActions(1).build();
        silkRoad = new VictoryCardImpl("Silk Road", 4, 0, false);
        spiceMerchant = new ActionCardImpl.Builder("Spice Merchant", 4).build();
        stables = new ActionCardImpl.Builder("Stables", 5).build();
        trader = new ActionCardImpl.Builder("Trader", 4).build();
        tunnel = new VictoryCardImpl("Tunnel", 3, 2, false);
    }
}
