package com.vdom.api;

import java.util.Arrays;
import java.util.List;
import com.vdom.core.Expansion;

public enum GameType {

    // Test - remove this for full release
    Test("Test (for testing)"),
    
    // Random Games
    Random("Random"),
    RandomBaseGame("Random Base"),
    RandomIntrigue("Random Intrigue"),
    RandomSeaside("Random Seaside"),
    RandomAlchemy("Random Alchemy"),
    RandomProsperity("Random Prosperity"),
    RandomCornucopia("Random Cornucopia"),
    RandomHinterlands("Random Hinterlands"),
    RandomDarkAges("Random Dark Ages"),
    RandomGuilds("Random Guilds"),
    RandomAdventures("Random Adventures"),
    
    // Base Game
    FirstGame("First Game", Expansion.Base), BigMoney("Big Money", Expansion.Base), Interaction("Interaction", Expansion.Base), SizeDistortion("Size Distortion", Expansion.Base), VillageSquare("Village Square", Expansion.Base),
    // Base Game, Second Edition
    FirstGame2("First Game (2E)", Expansion.Base2E), SizeDistortion2("Size Distortion (2E)", Expansion.Base2E), DeckTop("Deck Top", Expansion.Base2E), SleightOfHand("Sleight of Hand", Expansion.Base2E), Improvements("Improvements", Expansion.Base2E), SilverAndGold("Silver & Gold", Expansion.Base2E),
    
    // Intrigue
    VictoryDance("Victory Dance", Expansion.Intrigue), SecretSchemes("Secret Schemes", Expansion.Intrigue), BestWishes("Best Wishes", Expansion.Intrigue),
    // Intrigue, Second Edition
    VictoryDance2("Victory Dance (2E)", Expansion.Intrigue2E), ThePlotThickens("The Plot Thickens", Expansion.Intrigue2E), BestWishes2("Best Wishes (2E)", Expansion.Intrigue2E),
    // Base and Intrigue
    Deconstruction("Deconstruction", Expansion.Base, Expansion.Intrigue), HandMadness("Hand Madness", Expansion.Base, Expansion.Intrigue), Underlings("Underlings", Expansion.Base, Expansion.Intrigue),
    // Base SE and Intrigue SE
    Underlings2("Underlings", Expansion.Base2E, Expansion.Intrigue2E), GrandScheme("Grand Scheme", Expansion.Base2E, Expansion.Intrigue2E), Deconstruction2("Deconstruction", Expansion.Base2E, Expansion.Intrigue2E),
    
    // Seaside
    HighSeas("High Seas (Sea)", Expansion.Seaside), BuriedTreasure("Buried Treasure (Sea)", Expansion.Seaside), Shipwrecks("Shipwrecks (Sea)", Expansion.Seaside),
    // Base and Seaside
    ReachForTomorrow("Reach for Tomorrow", Expansion.Seaside, Expansion.Base), Repetition("Repetition", Expansion.Seaside, Expansion.Base), GiveAndTake("Give and Take", Expansion.Seaside, Expansion.Base),
    // Base 2E and Seaside
    ReachForTomorrow2("Reach for Tomorrow", Expansion.Seaside, Expansion.Base2E), Repetition2("Repetition", Expansion.Seaside, Expansion.Base2E), GiveAndTake2("Give and Take", Expansion.Seaside, Expansion.Base2E),
    // Intrigue 2E and Seaside
    AStarToSteerBy("A Star to Steer By", Expansion.Seaside, Expansion.Intrigue2E), ShorePatrol("Shore Patrol", Expansion.Seaside, Expansion.Intrigue2E), BridgeCrossing("Bridge Crossing", Expansion.Seaside, Expansion.Intrigue2E),
    
    // Alchemy
    // Base and Alchemy
    ForbiddenArts("Forbidden Arts", Expansion.Alchemy, Expansion.Base), PotionMixers("Potion Mixers", Expansion.Alchemy, Expansion.Base), ChemistryLesson("Chemistry Lesson", Expansion.Alchemy, Expansion.Base),
    // Base 2E and Alchemy
    ForbiddenArts2("Forbidden Arts", Expansion.Alchemy, Expansion.Base2E), PotionMixers2("Potion Mixers", Expansion.Alchemy, Expansion.Base2E), ChemistryLesson2("Chemistry Lesson", Expansion.Alchemy, Expansion.Base2E),
    // Intrigue and Alchemy
    Servants("Servants", Expansion.Alchemy, Expansion.Intrigue), SecretResearch("Secret Research", Expansion.Alchemy, Expansion.Intrigue), PoolsToolsAndFools("Pools, Tools, and Fools", Expansion.Alchemy, Expansion.Intrigue),
    // Intrigue 2E and Alchemy
    Servants2("Servants", Expansion.Alchemy, Expansion.Intrigue2E), SecretResearch2("Secret Research", Expansion.Alchemy, Expansion.Intrigue2E), PoolsToolsAndFools2("Pools, Tools, and Fools", Expansion.Alchemy, Expansion.Intrigue2E),

    // Prosperity
    Beginners("Beginners", Expansion.Prosperity), FriendlyInteractive("Friendly Interactive", Expansion.Prosperity), BigActions("Big Actions", Expansion.Prosperity),
    // Base and Prosperity
    BiggestMoney("Biggest Money", Expansion.Prosperity, Expansion.Base), TheKingsArmy("The King's Army", Expansion.Prosperity, Expansion.Base), TheGoodLife("The Good Life", Expansion.Prosperity, Expansion.Base),
    // Base 2E and Prosperity
    BiggestMoney2("Biggest Money", Expansion.Prosperity, Expansion.Base2E), TheKingsArmy2("The King's Army", Expansion.Prosperity, Expansion.Base2E), TheGoodLife2("The Good Life", Expansion.Prosperity, Expansion.Base2E),
    // Intrigue and Prosperity
    PathsToVictory("Paths to Victory", Expansion.Prosperity, Expansion.Intrigue), AllAlongTheWatchtower("All Along the Watchtower", Expansion.Prosperity, Expansion.Intrigue), LuckySeven("Lucky Seven", Expansion.Prosperity, Expansion.Intrigue),
    // Intrigue 2E and Prosperity
    PathsToVictory2("Paths to Victory", Expansion.Prosperity, Expansion.Intrigue2E), AllAlongTheWatchtower2("All Along the Watchtower", Expansion.Prosperity, Expansion.Intrigue2E), LuckySeven2("Lucky Seven", Expansion.Prosperity, Expansion.Intrigue2E),

    // Cornucopia
    // Base and Cornucopia
    BountyOfTheHunt("Bounty of the Hunt", Expansion.Cornucopia, Expansion.Base), BadOmens("Bad Omens", Expansion.Cornucopia, Expansion.Base), TheJestersWorkshop("The Jester's Workshop", Expansion.Cornucopia, Expansion.Base),
    // Intrigue and Cornucopia
    LastLaughs("Last Laughs", Expansion.Cornucopia, Expansion.Intrigue), TheSpiceOfLife("The Spice of Life", Expansion.Cornucopia, Expansion.Intrigue), SmallVictories("Small Victories", Expansion.Cornucopia, Expansion.Intrigue),
    // Base 2E and Cornucopia
    BountyOfTheHunt2("Bounty of the Hunt", Expansion.Cornucopia, Expansion.Base2E), BadOmens2("Bad Omens", Expansion.Cornucopia, Expansion.Base2E), TheJestersWorkshop2("The Jester's Workshop", Expansion.Cornucopia, Expansion.Base2E),
    // Intrigue 2E and Cornucopia
    LastLaughs2("Last Laughs", Expansion.Cornucopia, Expansion.Intrigue2E), TheSpiceOfLife2("The Spice of Life", Expansion.Cornucopia, Expansion.Intrigue2E), SmallVictories2("Small Victories", Expansion.Cornucopia, Expansion.Intrigue2E),

    // Hinterlands
    HinterlandsIntro("Hinterlands Intro", Expansion.Hinterlands), FairTrades("Fair Trades", Expansion.Hinterlands), Bargains("Bargains", Expansion.Hinterlands), Gambits("Gambits", Expansion.Hinterlands),
    // Base and Hinterlands
    HighwayRobbery("Highway Robbery", Expansion.Hinterlands, Expansion.Base), AdventuresAbroad("Adventures Abroad", Expansion.Hinterlands, Expansion.Base),
    // Base 2E and Hinterlands
    HighwayRobbery2("Highway Robbery", Expansion.Hinterlands, Expansion.Base2E), AdventuresAbroad2("Adventures Abroad", Expansion.Hinterlands, Expansion.Base2E),
    // Hinterlands and Intrigue
    MoneyForNothing("Money for Nothing", Expansion.Hinterlands, Expansion.Intrigue), TheDukesBall("The Duke's Ball", Expansion.Hinterlands, Expansion.Intrigue),
    // Hinterlands and Intrigue 2E
    MoneyForNothing2("Money for Nothing", Expansion.Hinterlands, Expansion.Intrigue2E), TheDukesBall2("The Duke's Ball", Expansion.Hinterlands, Expansion.Intrigue2E),
    // Hinterlands and Seaside
    Travelers("Travelers", Expansion.Hinterlands, Expansion.Seaside), Diplomacy("Diplomacy", Expansion.Hinterlands, Expansion.Seaside),
    // Hinterlands and Alchemy
    SchemesAndDreams("Schemes and Dreams", Expansion.Hinterlands, Expansion.Alchemy), WineCountry("Wine Country", Expansion.Hinterlands, Expansion.Alchemy),
    // Hinterlands and Prosperity
    InstantGratification("Instant Gratification", Expansion.Hinterlands, Expansion.Prosperity), TreasureTrove("Treasure Trove", Expansion.Hinterlands, Expansion.Prosperity),
    // Hinterlands and Cornucopia
    BlueHarvest("Blue Harvest", Expansion.Hinterlands, Expansion.Cornucopia), TravelingCircus("Traveling Circus", Expansion.Hinterlands, Expansion.Cornucopia),

    // Dark Ages
    PlayingChessWithDeath("Playing Chess With Death", Expansion.DarkAges), GrimParade("Grim Parade", Expansion.DarkAges),
    // Dark Ages and Base
    HighAndLow("High and Low", Expansion.DarkAges, Expansion.Base), ChivalryAndRevelry("Chivalry and Revelry", Expansion.DarkAges, Expansion.Base),
    // Dark Ages and Base 2E
    HighAndLow2("High and Low", Expansion.DarkAges, Expansion.Base2E), ChivalryAndRevelry2("Chivalry and Revelry", Expansion.DarkAges, Expansion.Base2E),
    // Dark Ages and Intrigue
    Prophecy("Prophecy", Expansion.DarkAges, Expansion.Intrigue), Invasion("Invasion", Expansion.DarkAges, Expansion.Intrigue),
    // Dark Ages and Intrigue 2E
    Prophecy2("Prophecy", Expansion.DarkAges, Expansion.Intrigue2E), Invasion2("Invasion", Expansion.DarkAges, Expansion.Intrigue2E),
    // Dark Ages and Seaside
    WateryGraves("Watery Graves", Expansion.DarkAges, Expansion.Seaside), Peasants("Peasants", Expansion.DarkAges, Expansion.Seaside),
    // Dark Ages and Alchemy
    Infestations("Infestations", Expansion.DarkAges, Expansion.Alchemy), Lamentations("Lamentations", Expansion.DarkAges, Expansion.Alchemy),
    // Dark Ages and Prosperity
    OneMansTrash("One Man's Trash", Expansion.DarkAges, Expansion.Prosperity), HonorAmongThieves("Honor Among Thieves", Expansion.DarkAges, Expansion.Prosperity),
    // Dark Ages and Cornucopia
    DarkCarnival("Dark Carnival", Expansion.DarkAges, Expansion.Cornucopia), ToTheVictor("To The Victor", Expansion.DarkAges, Expansion.Cornucopia),
    // Dark Ages and Hinterlands
    FarFromHome("Far From Home", Expansion.DarkAges, Expansion.Hinterlands), Expeditions("Expeditions", Expansion.DarkAges, Expansion.Hinterlands),
    
    // Guilds
    // Guilds and Base
    ArtsAndCrafts("Arts and Crafts", Expansion.Guilds, Expansion.Base), CleanLiving("Clean Living", Expansion.Guilds, Expansion.Base), GildingTheLily("Gilding the Lily", Expansion.Guilds, Expansion.Base),
    // Guilds and Base 2E
    ArtsAndCrafts2("Arts and Crafts", Expansion.Guilds, Expansion.Base2E), CleanLiving2("Clean Living", Expansion.Guilds, Expansion.Base2E), GildingTheLily2("Gilding the Lily", Expansion.Guilds, Expansion.Base2E),
    // Guilds and Intrigue
    NameThatCard("Name that Card", Expansion.Guilds, Expansion.Intrigue), TricksOfTheTrade("Tricks of the Trade", Expansion.Guilds, Expansion.Intrigue), DecisionsDecisions("Decisions, Decisions", Expansion.Guilds, Expansion.Intrigue),
    // Guilds and Intrigue 2E
    NameThatCard2("Name that Card", Expansion.Guilds, Expansion.Intrigue2E), TricksOfTheTrade2("Tricks of the Trade", Expansion.Guilds, Expansion.Intrigue2E), DecisionsDecisions2("Decisions, Decisions", Expansion.Guilds, Expansion.Intrigue2E),
    
    // Adventures
    GentleIntro("Gentle Intro", Expansion.Adventures), ExpertIntro("Expert Intro", Expansion.Adventures),
    // Adventures and Base
    LevelUp("Level Up", Expansion.Adventures, Expansion.Base), SonOfSizeDistortion("Son of Size Distortion", Expansion.Adventures, Expansion.Base),
    // Adventures and Base 2E
    LevelUp2("Level Up", Expansion.Adventures, Expansion.Base2E), SonOfSizeDistortion2("Son of Size Distortion", Expansion.Adventures, Expansion.Base2E),
    // Adventures and Intrigue
    RoyaltyFactory("Royalty Factory", Expansion.Adventures, Expansion.Intrigue), MastersOfFinance("Masters of Finance", Expansion.Adventures, Expansion.Intrigue),
    // Adventures and Intrigue
    RoyaltyFactory2("Royalty Factory", Expansion.Adventures, Expansion.Intrigue2E), MastersOfFinance2("Masters of Finance", Expansion.Adventures, Expansion.Intrigue2E),
    // Adventures and Seaside
    PrinceOfOrange("Prince of Orange", Expansion.Adventures, Expansion.Seaside), GiftsAndMathoms("Gifts and Mathoms", Expansion.Adventures, Expansion.Seaside),
    // Adventures and Alchemy
    HastePotion("Haste Potion", Expansion.Adventures, Expansion.Alchemy), Cursecatchers("Cursecatchers", Expansion.Adventures, Expansion.Alchemy),
    // Adventures and Prosperity
    LastWillAndMonument("Last Will and Monument", Expansion.Adventures, Expansion.Prosperity), ThinkBig("Think Big", Expansion.Adventures, Expansion.Prosperity),
    // Adventures and Cornucopia
    TheHerosReturn("The Hero's Return", Expansion.Adventures, Expansion.Cornucopia), SeacraftAndWitchcraft("Seacraft and Witchcraft", Expansion.Adventures, Expansion.Cornucopia),
    // Adventures and Hinterlands
    TradersAndRaiders("Traders and Raiders", Expansion.Adventures, Expansion.Hinterlands), Journeys("Journeys", Expansion.Adventures, Expansion.Hinterlands),
    // Adventures and Dark Ages
    CemeteryPolka("Cemetery Polka", Expansion.Adventures, Expansion.DarkAges), GroovyDecay("Groovy Decay", Expansion.Adventures, Expansion.DarkAges),
    // Adventures and Guilds
    Spendthrift("Spendthrift", Expansion.Adventures, Expansion.Guilds), QueenOfTan("Queen of Tan", Expansion.Adventures, Expansion.Guilds), 
    
    // Empires
    BasicIntro("Basic Intro", Expansion.Empires), AdvancedIntro("Advanced Intro", Expansion.Empires),
    // Empires and Base
    EverythingInModeration("Everything in Moderation", Expansion.Empires, Expansion.Base), SilverBullets("Silver Bullets", Expansion.Empires, Expansion.Base),
    // Empires and Base 2E
    EverythingInModeration2("Everything in Moderation", Expansion.Empires, Expansion.Base2E), SilverBullets2("Silver Bullets", Expansion.Empires, Expansion.Base2E),
    // Empires and Intrigue
    DeliciousTorture("Delicious Torture", Expansion.Empires, Expansion.Intrigue), BuddySystem("Buddy System", Expansion.Empires, Expansion.Intrigue),
    // Empires and Intrigue
    DeliciousTorture2("Delicious Torture", Expansion.Empires, Expansion.Intrigue2E), BuddySystem2("Buddy System", Expansion.Empires, Expansion.Intrigue2E),
    // Empires and Seaside
    BoxedIn("Boxed In", Expansion.Empires, Expansion.Seaside), KingOfTheSea("King of the Sea", Expansion.Empires, Expansion.Seaside),
    // Empires and Alchemy
    Collectors("Collectors", Expansion.Empires, Expansion.Alchemy),
    // Empires and Prosperity
    BigTime("Big Time", Expansion.Empires, Expansion.Prosperity), GildedGates("Gilded Gates", Expansion.Empires, Expansion.Prosperity),
    // Empires and Cornucopia
    Zookeepers("Zookeepers", Expansion.Empires, Expansion.Cornucopia),
    // Empires and Hinterlands
    SimplePlans("Simple Plans", Expansion.Empires, Expansion.Hinterlands), HinterExpansion("Expansion", Expansion.Empires, Expansion.Hinterlands),
    // Empires and Dark Ages
    TombOfTheRatKing("Tomb of the Rat King", Expansion.Empires, Expansion.DarkAges), TriumphOfTheBanditKing("Triumph of the Bandit King", Expansion.Empires, Expansion.DarkAges), TheSquiresRitual("The Squire's Ritual", Expansion.Empires, Expansion.DarkAges),
    // Empires and Guilds
    CashFlow("Cash Flow", Expansion.Empires, Expansion.Guilds),
    // Empires and Adventures
    AreaControl("Area Control", Expansion.Empires, Expansion.Adventures), NoMoneyNoProblems("No Money No Problems", Expansion.Empires, Expansion.Adventures),
    
    // Card set is specified from Dominion Shuffle
    Specified("Specified");

    private final String name;
    private final List<Expansion> expansions;
    GameType(String name, Expansion... expansions) {
        this(name, Arrays.asList(expansions));
    }
    GameType(String name, List<Expansion> expansions) {
        this.name = name;
        this.expansions = expansions;
    }

    public String getName() {
        return name;
    }
    
    public List<Expansion> getExpansions() {
    	return expansions;
    }
}
