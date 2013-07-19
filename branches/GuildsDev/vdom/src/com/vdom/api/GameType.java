package com.vdom.api;

public enum GameType {

    // Base Game
    FirstGame("First Game (Base)"), BigMoney("Big Money (Base)"), Interaction("Interaction (Base)"), SizeDistortion("Size Distortion (Base)"), VillageSquare("Village Square (Base)"), RandomBaseGame("Random Base Game"),

    // Intrigue
    VictoryDance("Victory Dance (Intr)"), SecretSchemes("Secret Schemes (Intr)"), BestWishes("Best Wishes (Intr)"), RandomIntrigue("Random Intrigue"),

    // Base and Intrigue
    Deconstruction("Deconstruction (Base, Intr)"), HandMadness("Hand Madness (Base, Intr)"), Underlings("Underlings (Base, Intr)"),

    // Seaside
    HighSeas("High Seas (Sea)"), BuriedTreasure("Buried Treasure (Sea)"), Shipwrecks("Shipwrecks (Sea)"), RandomSeaside("Random Seaside"),

    // Alchemy
    RandomAlchemy("Random Alchemy"),

    // Base and Alchemy
    ForbiddenArts("Forbidden Arts (Base, Alch)"), PotionMixers("Potion Mixers (Base, Alch)"), ChemistryLesson("Chemistry Lesson (Base, Alch)"),

    // Intrigue and Alchemy
    Servants("Servants (Intr, Alch)"), SecretResearch("Secret Research (Intr, Alch)"), PoolsToolsAndFools("Pools, Tools, and Fools (Intr, Alch)"),

    // Base and Seaside
    ReachForTomorrow("Reach for Tomorrow (Base, Sea)"), Repetition("Repetition (Base, Sea)"), GiveAndTake("Give and Take (Base, Sea)"),

    // Prosperity
    Beginners("Beginners (Prsp)"), FriendlyInteractive("Friendly Interactive (Prsp)"), BigActions("Big Actions (Prsp)"), RandomProsperity("Random Prosperity"),

    // Base and Prosperity
    BiggestMoney("Biggest Money (Base, Prsp)"), TheKingsArmy("The King's Army (Base, Prsp)"), TheGoodLife("The Good Life (Base, Prsp)"),

    // Intrigue and Prosperity
    PathToVictory("Path to Victory (Intr, Prsp)"), AllAlongTheWatchtower("All Along the Watchtower (Intr, Prsp)"), LuckySeven("Lucky Seven (Intr, Prsp)"),

    // Cornucopia
    RandomCornucopia("Random Cornucopia"),

    // Base and Cornucopia
    BountyOfTheHunt("Bounty of the Hunt (Base, Corn)"), BadOmens("Bad Omens (Base, Corn)"), TheJestersWorkshop("The Jester's Workshop (Base, Corn)"),

    // Intrigue and Cornucopia
    LastLaughs("Last Laughs (Intr, Corn)"), TheSpiceOfLife("The Spice of Life (Intr, Corn)"), SmallVictories("Small Victories (Intr, Corn)"),

    // Hinterlands
    HinterlandsIntro("Hinterlands Intro (Hntr)"), FairTrades("Fair Trades (Hntr)"), Bargains("Bargains (Hntr)"), Gambits("Gambits (Hntr)"), RandomHinterlands("Random Hinterlands"),

    // Base and Hinterlands
    HighwayRobbery("Highway Robbery (Base, Hntr)"), AdventuresAbroad("Adventures Abroad (Base, Hntr)"),

    // Hinterlands and Intrigue
    MoneyForNothing("Money for Nothing (Hntr, Intr)"), TheDukesBall("The Duke's Ball (Hntr, Intr)"),

    // Hinterlands and Seaside
    Travelers("Travelers (Hntr, Sea)"), Diplomacy("Diplomacy (Hntr, Sea)"),

    // Hinterlands and Alchemy
    SchemesAndDreams("Schemes and Dreams (Hntr, Alch)"), WineCountry("Wine Country (Hntr, Alch)"),

    // Hinterlands and Prosperity
    InstantGratification("Instant Gratification (Hntr, Prsp)"), TreasureTrove("Treasure Trove (Hntr, Prsp)"),

    // Hinterlands and Cornucopia
    BlueHarvest("Blue Harvest (Hntr, Corn)"), TravelingCircus("Traveling Circus (Hntr, Corn)"),

    // Dark Ages
    RandomDarkAges("Random Dark Ages"), PlayingChessWithDeath("Playing Chess With Death (DA)"), GrimParade("Grim Parade (DA)"),

    // Dark Ages and Base
    HighAndLow("HighAndLow (DA, Base)"), ChivalryAndRevelry("ChivalryAndRevelry (DA, Base)"),

    // Dark Ages and Intrigue
    Prophecy("Prophecy (DA, Intr)"), Invasion("Invasion (DA, Intr)"),

    // Dark Ages and Seaside
    WateryGraves("Watery Graves (DA, Sea)"), Peasants("Peasants (DA, Sea)"),

    Infestations("Infestations (DA, Alch)"), Lamentations("Lamentations (DA, Alch)"),
    OneMansTrash("One Man's Trash (DA, Prsp)"), HonorAmongThieves("Honor Among Thieves (DA, Prsp)"),
    DarkCarnival("Dark Carnival (DA, Corn)"), ToTheVictor("To The Victor (DA, Corn)"),
    FarFromHome("Far From Home (DA, Hntr)"), Expeditions("Expeditions (DA, Hntr)"),
    
    // Guilds
    RandomGuilds("Random Guilds"),
    
    // Guilds + Base
    ArtsAndCrafts("Arts and Crafts (Guilds, Base)"), CleanLiving("Clean Living (Guilds, Base)"), GildingTheLily("Gilding the Lily (Guilds, Base)"),
    
    // Guilds + Intrigue
    NameThatCard("Name That Card (Guilds, Intr)"), TricksOfTheTrade("Tricks of the Trade (Guilds, Intr)"), DecisionsDecisions("Decisions, Decisions (Guilds, Intr)"),

    // All Cards
    Random("Random"),

	// Card set is specified from Dominion Shuffle
	Specified("Specified");


    private final String name;
    GameType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static GameType fromName(String name) {
        name = name.replace(" the ", " The ");
        name = name.replace(" for ", " For ");
        name = name.replace(" of ", " Of ");
        name = name.replace(" to ", " To ");
        name = name.replace(" and ", " And ");
        name = name.replace("," , "");
        name = name.replace("'" , "");
        name = name.replace(" ", "");
        int paren = name.indexOf("(");
        if(paren != -1) {
            name = name.substring(0, paren);
        }
        return GameType.valueOf(name);
    }
}
