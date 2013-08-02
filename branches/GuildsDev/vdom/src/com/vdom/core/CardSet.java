package com.vdom.core;

import com.vdom.api.Card;
import com.vdom.api.GameType;

import java.util.*;

/**
 * This class contains a set of cards (for a given GameType) that can be used.
 * If the GameType value is one of the random sets, a random CardSet is generated
 * using the available cards specified.
 *
 * Note that the sets defined in the actual Dominion rule books are created when the application loads.
 * These are created in a static block at the end of the class.
 *
 * @author Michael Fazio
 *
 */
public class CardSet {

	public static Random rand = new Random(System.currentTimeMillis());
	private static final Map<GameType, CardSet> CardSetMap = new HashMap<GameType, CardSet>();

	public static final GameType defaultGameType = GameType.Random;

	private final List<Card> cards;
	private final Card baneCard;
	private final boolean isRandom;

	private CardSet(final List<Card> cards, final Card baneCard) {
		this.cards = cards;
		this.baneCard = baneCard;
		this.isRandom = false;
	}

	private CardSet(final Card[] cardsArray, final Card baneCard) {
		this.cards = Arrays.asList(cardsArray);
		this.baneCard = baneCard;
		this.isRandom = false;
	}

	private CardSet(final List<Card> cards, final boolean isRandom) {
		this.cards = cards;
		this.baneCard = null;
		this.isRandom = isRandom;
	}

	public static CardSet getCardSet(final GameType type) {
		CardSet set = CardSet.CardSetMap.get(type);

		if(set == null) {
			set = CardSet.getCardSet(CardSet.defaultGameType);
		}

		if(set.isRandom) {
			set = CardSet.getRandomCardSet(set.getCards());
		}

		return set;
	}

	/**
	 * This returns a random CardSet using a subset of the cards entered as a parameter.
	 * Note that a bane card is set aside every time to avoid having to swap out a card after
	 * the entire set is selected.  If a bane card is not required, that value is simply cleared away.
	 *
	 * @param possibleCards The set of cards that can be included in the random CardSet
	 * @return A random CardSet selected from the list of entered cards.
	 */
	private static CardSet getRandomCardSet(final List<Card> possibleCards) {
		final List<Card> cardSetList = new ArrayList<Card>();
		//Save off a possible bane card initially to avoid having to add another card later.
		Card baneCard = CardSet.getRandomBaneCard(possibleCards);

		int added = 0;
		while (added < 10) {
			Card card;
			do {
				card = possibleCards.get(rand.nextInt(possibleCards.size()));
				if (card.equals(baneCard) || cardSetList.contains(card)) {
					card = null;
				}
			} while (card == null);

			cardSetList.add(card);
			added++;
		}

		//If we don't need the bane card, just discard it.
		if(!cardSetList.contains(Cards.youngWitch)) {
			baneCard = null;
		}

		return new CardSet(cardSetList, baneCard);
	}

	private static Card getRandomBaneCard(final List<Card> possibleCards) {
		Card baneCard = null;

		Card card = null;
		do {
			card = possibleCards.get(rand.nextInt(possibleCards.size()));
			final int cost = card.getCost(null);
            if (!card.costPotion() && (cost == 2 || cost == 3)) {
				baneCard = card;
			}
		} while(baneCard == null);

		return baneCard;
	}

	public Card getBaneCard() {
		return baneCard;
	}

	public List<Card> getCards() {
		return cards;
	}

	public static Map<GameType, CardSet> getCardSetMap() {
		return CardSetMap;
	}

	public boolean isRandom() {
		return isRandom;
	}

	static {
		CardSetMap.put(GameType.Random, new CardSet(Cards.actionCards, true));
		CardSetMap.put(GameType.RandomBaseGame, new CardSet(Cards.actionCardsBaseGame, true));
		CardSetMap.put(GameType.RandomIntrigue, new CardSet(Cards.actionCardsIntrigue, true));
		CardSetMap.put(GameType.RandomSeaside, new CardSet(Cards.actionCardsSeaside, true));
		CardSetMap.put(GameType.RandomAlchemy, new CardSet(Cards.actionCardsAlchemy, true));
		CardSetMap.put(GameType.RandomProsperity, new CardSet(Cards.actionCardsProsperity, true));
		CardSetMap.put(GameType.RandomCornucopia, new CardSet(Cards.actionCardsCornucopia, true));
		CardSetMap.put(GameType.RandomHinterlands, new CardSet(Cards.actionCardsHinterlands, true));
		CardSetMap.put(GameType.RandomDarkAges, new CardSet(Cards.actionCardsDarkAges, true));
		CardSetMap.put(GameType.RandomGuilds, new CardSet(Cards.actionCardsGuilds, true));

        CardSetMap.put(GameType.ForbiddenArts, new CardSet(new Card[] { Cards.apprentice, Cards.familiar, Cards.possession, Cards.university, Cards.cellar, Cards.councilRoom, Cards.gardens, Cards.laboratory, Cards.thief, Cards.throneRoom }, null));
		CardSetMap.put(GameType.PotionMixers, new CardSet(new Card[]{Cards.alchemist, Cards.apothecary, Cards.golem, Cards.herbalist, Cards.transmute, Cards.cellar, Cards.chancellor, Cards.festival, Cards.militia, Cards.smithy}, null));
		CardSetMap.put(GameType.ChemistryLesson, new CardSet(new Card[]{Cards.alchemist, Cards.golem, Cards.philosophersStone, Cards.university, Cards.bureaucrat, Cards.market, Cards.moat, Cards.remodel, Cards.witch, Cards.woodcutter}, null));
        CardSetMap.put(GameType.Servants, new CardSet(new Card[] { Cards.golem, Cards.possession, Cards.scryingPool, Cards.transmute, Cards.vineyard, Cards.conspirator, Cards.greatHall, Cards.minion, Cards.pawn, Cards.steward }, null));
		CardSetMap.put(GameType.SecretResearch, new CardSet(new Card[]{Cards.familiar, Cards.herbalist, Cards.philosophersStone, Cards.university, Cards.bridge, Cards.masquerade, Cards.minion, Cards.nobles, Cards.shantyTown, Cards.torturer}, null));
		CardSetMap.put(GameType.PoolsToolsAndFools, new CardSet(new Card[]{Cards.apothecary, Cards.apprentice, Cards.golem, Cards.scryingPool, Cards.baron, Cards.coppersmith, Cards.ironworks, Cards.nobles, Cards.tradingPost, Cards.wishingWell}, null));
		CardSetMap.put(GameType.FirstGame, new CardSet(new Card[]{Cards.cellar, Cards.market, Cards.militia, Cards.mine, Cards.moat, Cards.remodel, Cards.smithy, Cards.village, Cards.woodcutter, Cards.workshop}, null));
		CardSetMap.put(GameType.BigMoney, new CardSet(new Card[]{Cards.adventurer, Cards.bureaucrat, Cards.chancellor, Cards.chapel, Cards.feast, Cards.laboratory, Cards.market, Cards.mine, Cards.moneyLender, Cards.throneRoom}, null));
		CardSetMap.put(GameType.Interaction, new CardSet(new Card[]{Cards.bureaucrat, Cards.chancellor, Cards.councilRoom, Cards.festival, Cards.library, Cards.militia, Cards.moat, Cards.spy, Cards.thief, Cards.village}, null));
		CardSetMap.put(GameType.SizeDistortion, new CardSet(new Card[]{Cards.cellar, Cards.chapel, Cards.feast, Cards.gardens, Cards.laboratory, Cards.thief, Cards.village, Cards.witch, Cards.woodcutter, Cards.workshop}, null));
		CardSetMap.put(GameType.VillageSquare, new CardSet(new Card[]{Cards.bureaucrat, Cards.cellar, Cards.festival, Cards.library, Cards.market, Cards.remodel, Cards.smithy, Cards.throneRoom, Cards.village, Cards.woodcutter}, null));
		CardSetMap.put(GameType.VictoryDance, new CardSet(new Card[]{Cards.bridge, Cards.duke, Cards.greatHall, Cards.harem, Cards.ironworks, Cards.masquerade, Cards.nobles, Cards.pawn, Cards.scout, Cards.upgrade}, null));
		CardSetMap.put(GameType.SecretSchemes, new CardSet(new Card[]{Cards.conspirator, Cards.harem, Cards.ironworks, Cards.pawn, Cards.saboteur, Cards.shantyTown, Cards.steward, Cards.swindler, Cards.tradingPost, Cards.tribute}, null));
		CardSetMap.put(GameType.BestWishes, new CardSet(new Card[]{Cards.coppersmith, Cards.courtyard, Cards.masquerade, Cards.scout, Cards.shantyTown, Cards.steward, Cards.torturer, Cards.tradingPost, Cards.upgrade, Cards.wishingWell}, null));
		CardSetMap.put(GameType.Deconstruction, new CardSet(new Card[]{Cards.bridge, Cards.miningVillage, Cards.remodel, Cards.saboteur, Cards.secretChamber, Cards.spy, Cards.swindler, Cards.thief, Cards.throneRoom, Cards.torturer}, null));
		CardSetMap.put(GameType.HandMadness, new CardSet(new Card[]{Cards.bureaucrat, Cards.chancellor, Cards.councilRoom, Cards.courtyard, Cards.mine, Cards.militia, Cards.minion, Cards.nobles, Cards.steward, Cards.torturer}, null));
		CardSetMap.put(GameType.Underlings, new CardSet(new Card[]{Cards.baron, Cards.cellar, Cards.festival, Cards.library, Cards.masquerade, Cards.minion, Cards.nobles, Cards.pawn, Cards.steward, Cards.witch}, null));
		CardSetMap.put(GameType.HighSeas, new CardSet(new Card[]{Cards.bazaar, Cards.caravan, Cards.embargo, Cards.explorer, Cards.haven, Cards.island, Cards.lookout, Cards.pirateShip, Cards.smugglers, Cards.wharf}, null));
		CardSetMap.put(GameType.BuriedTreasure, new CardSet(new Card[]{Cards.ambassador, Cards.cutpurse, Cards.fishingVillage, Cards.lighthouse, Cards.outpost, Cards.pearlDiver, Cards.tactician, Cards.treasureMap, Cards.warehouse, Cards.wharf}, null));
		CardSetMap.put(GameType.Shipwrecks, new CardSet(new Card[]{Cards.ghostShip, Cards.merchantShip, Cards.nativeVillage, Cards.navigator, Cards.pearlDiver, Cards.salvager, Cards.seaHag, Cards.smugglers, Cards.treasury, Cards.warehouse}, null));
		CardSetMap.put(GameType.ReachForTomorrow, new CardSet(new Card[]{Cards.adventurer, Cards.cellar, Cards.councilRoom, Cards.cutpurse, Cards.ghostShip, Cards.lookout, Cards.seaHag, Cards.spy, Cards.treasureMap, Cards.village}, null));
		CardSetMap.put(GameType.Repetition, new CardSet(new Card[]{Cards.caravan, Cards.chancellor, Cards.explorer, Cards.festival, Cards.militia, Cards.outpost, Cards.pearlDiver, Cards.pirateShip, Cards.treasury, Cards.workshop}, null));
		CardSetMap.put(GameType.GiveAndTake, new CardSet(new Card[]{Cards.ambassador, Cards.fishingVillage, Cards.haven, Cards.island, Cards.library, Cards.market, Cards.moneyLender, Cards.salvager, Cards.smugglers, Cards.witch}, null));
		CardSetMap.put(GameType.Beginners, new CardSet(new Card[]{Cards.bank, Cards.countingHouse, Cards.expand, Cards.goons, Cards.monument, Cards.rabble, Cards.royalSeal, Cards.venture, Cards.watchTower, Cards.workersVillage}, null));
		CardSetMap.put(GameType.FriendlyInteractive, new CardSet(new Card[]{Cards.bishop, Cards.city, Cards.contraband, Cards.forge, Cards.hoard, Cards.peddler, Cards.royalSeal, Cards.tradeRoute, Cards.vault, Cards.workersVillage}, null));
		CardSetMap.put(GameType.BigActions, new CardSet(new Card[]{Cards.city, Cards.expand, Cards.grandMarket, Cards.kingsCourt, Cards.loan, Cards.mint, Cards.quarry, Cards.rabble, Cards.talisman, Cards.vault}, null));
		CardSetMap.put(GameType.BiggestMoney, new CardSet(new Card[]{Cards.bank, Cards.grandMarket, Cards.mint, Cards.royalSeal, Cards.venture, Cards.adventurer, Cards.laboratory, Cards.mine, Cards.moneyLender, Cards.spy}, null));
		CardSetMap.put(GameType.TheKingsArmy, new CardSet(new Card[]{Cards.expand, Cards.goons, Cards.kingsCourt, Cards.rabble, Cards.vault, Cards.bureaucrat, Cards.councilRoom, Cards.moat, Cards.spy, Cards.village}, null));
		CardSetMap.put(GameType.TheGoodLife, new CardSet(new Card[]{Cards.contraband, Cards.countingHouse, Cards.hoard, Cards.monument, Cards.mountebank, Cards.bureaucrat, Cards.cellar, Cards.chancellor, Cards.gardens, Cards.village}, null));
		CardSetMap.put(GameType.PathToVictory, new CardSet(new Card[]{Cards.bishop, Cards.countingHouse, Cards.goons, Cards.monument, Cards.peddler, Cards.baron, Cards.harem, Cards.pawn, Cards.shantyTown, Cards.upgrade}, null));
		CardSetMap.put(GameType.AllAlongTheWatchtower, new CardSet(new Card[]{Cards.hoard, Cards.talisman, Cards.tradeRoute, Cards.vault, Cards.watchTower, Cards.bridge, Cards.greatHall, Cards.miningVillage, Cards.pawn, Cards.torturer}, null));
		CardSetMap.put(GameType.LuckySeven, new CardSet(new Card[]{Cards.bank, Cards.expand, Cards.forge, Cards.kingsCourt, Cards.vault, Cards.bridge, Cards.coppersmith, Cards.swindler, Cards.tribute, Cards.wishingWell}, null));
		CardSetMap.put(GameType.BountyOfTheHunt, new CardSet(new Card[]{Cards.harvest, Cards.hornOfPlenty, Cards.huntingParty, Cards.menagerie, Cards.tournament, Cards.cellar, Cards.festival, Cards.militia, Cards.moneyLender, Cards.smithy}, null));
		CardSetMap.put(GameType.BadOmens, new CardSet(new Card[]{Cards.fortuneTeller, Cards.hamlet, Cards.hornOfPlenty, Cards.jester, Cards.remake, Cards.adventurer, Cards.bureaucrat, Cards.laboratory, Cards.spy, Cards.throneRoom}, null));
		CardSetMap.put(GameType.TheJestersWorkshop, new CardSet(new Card[]{Cards.fairgrounds, Cards.farmingVillage, Cards.horseTraders, Cards.jester, Cards.youngWitch, Cards.feast, Cards.laboratory, Cards.market, Cards.remodel, Cards.workshop}, Cards.chancellor));
		CardSetMap.put(GameType.LastLaughs, new CardSet(new Card[]{Cards.farmingVillage, Cards.harvest, Cards.horseTraders, Cards.huntingParty, Cards.jester, Cards.minion, Cards.nobles, Cards.pawn, Cards.steward, Cards.swindler}, null));
		CardSetMap.put(GameType.TheSpiceOfLife, new CardSet(new Card[]{Cards.fairgrounds, Cards.hornOfPlenty, Cards.remake, Cards.tournament, Cards.youngWitch, Cards.coppersmith, Cards.courtyard, Cards.greatHall, Cards.miningVillage, Cards.tribute }, Cards.wishingWell));
		CardSetMap.put(GameType.SmallVictories, new CardSet(new Card[]{Cards.fortuneTeller, Cards.hamlet, Cards.huntingParty, Cards.remake, Cards.tournament, Cards.conspirator, Cards.duke, Cards.greatHall, Cards.harem, Cards.pawn}, null));
		CardSetMap.put(GameType.HinterlandsIntro, new CardSet(new Card[]{Cards.cache, Cards.crossroads, Cards.develop, Cards.haggler, Cards.jackOfAllTrades, Cards.margrave, Cards.nomadCamp, Cards.oasis, Cards.spiceMerchant, Cards.stables}, null));
		CardSetMap.put(GameType.FairTrades, new CardSet(new Card[]{Cards.borderVillage, Cards.cartographer, Cards.develop, Cards.duchess, Cards.farmland, Cards.illGottenGains, Cards.nobleBrigand, Cards.silkRoad, Cards.stables, Cards.trader}, null));
		CardSetMap.put(GameType.Bargains, new CardSet(new Card[]{Cards.borderVillage, Cards.cache, Cards.duchess, Cards.foolsGold, Cards.haggler, Cards.highway, Cards.nomadCamp, Cards.scheme, Cards.spiceMerchant, Cards.trader}, null));
		CardSetMap.put(GameType.Gambits, new CardSet(new Card[]{Cards.cartographer, Cards.crossroads, Cards.embassy, Cards.inn, Cards.jackOfAllTrades, Cards.mandarin, Cards.nomadCamp, Cards.oasis, Cards.oracle, Cards.tunnel}, null));
		CardSetMap.put(GameType.HighwayRobbery, new CardSet(new Card[]{Cards.cellar, Cards.library, Cards.moneyLender, Cards.throneRoom, Cards.workshop, Cards.highway, Cards.inn, Cards.margrave, Cards.nobleBrigand, Cards.oasis}, null));
		CardSetMap.put(GameType.AdventuresAbroad, new CardSet(new Card[]{Cards.adventurer, Cards.chancellor, Cards.festival, Cards.laboratory, Cards.remodel, Cards.crossroads, Cards.farmland, Cards.foolsGold, Cards.oracle, Cards.spiceMerchant}, null));
		CardSetMap.put(GameType.MoneyForNothing, new CardSet(new Card[]{Cards.coppersmith, Cards.greatHall, Cards.pawn, Cards.shantyTown, Cards.torturer, Cards.cache, Cards.cartographer, Cards.jackOfAllTrades, Cards.silkRoad, Cards.tunnel}, null));
		CardSetMap.put(GameType.TheDukesBall, new CardSet(new Card[]{Cards.conspirator, Cards.duke, Cards.harem, Cards.masquerade, Cards.upgrade, Cards.duchess, Cards.haggler, Cards.inn, Cards.nobleBrigand, Cards.scheme}, null));
		CardSetMap.put(GameType.Travelers, new CardSet(new Card[]{Cards.cutpurse, Cards.island, Cards.lookout, Cards.merchantShip, Cards.warehouse, Cards.cartographer, Cards.crossroads, Cards.farmland, Cards.silkRoad, Cards.stables}, null));
		CardSetMap.put(GameType.Diplomacy, new CardSet(new Card[]{Cards.ambassador, Cards.bazaar, Cards.caravan, Cards.embargo, Cards.smugglers, Cards.embassy, Cards.farmland, Cards.illGottenGains, Cards.nobleBrigand, Cards.trader}, null));
		CardSetMap.put(GameType.SchemesAndDreams, new CardSet(new Card[]{Cards.apothecary, Cards.apprentice, Cards.herbalist, Cards.philosophersStone, Cards.transmute, Cards.duchess, Cards.foolsGold, Cards.illGottenGains, Cards.jackOfAllTrades, Cards.scheme}, null));
		CardSetMap.put(GameType.WineCountry, new CardSet(new Card[]{Cards.apprentice, Cards.familiar, Cards.golem, Cards.university, Cards.vineyard, Cards.crossroads, Cards.farmland, Cards.haggler, Cards.highway, Cards.nomadCamp}, null));
		CardSetMap.put(GameType.InstantGratification, new CardSet(new Card[]{Cards.bishop, Cards.expand, Cards.hoard, Cards.mint, Cards.watchTower, Cards.farmland, Cards.haggler, Cards.illGottenGains, Cards.nobleBrigand, Cards.trader}, null));
		CardSetMap.put(GameType.TreasureTrove, new CardSet(new Card[]{Cards.bank, Cards.monument, Cards.royalSeal, Cards.tradeRoute, Cards.venture, Cards.cache, Cards.develop, Cards.foolsGold, Cards.illGottenGains, Cards.mandarin}, null));
		CardSetMap.put(GameType.BlueHarvest, new CardSet(new Card[]{Cards.hamlet, Cards.hornOfPlenty, Cards.horseTraders, Cards.jester, Cards.tournament, Cards.foolsGold, Cards.mandarin, Cards.nobleBrigand, Cards.trader, Cards.tunnel}, null));
		CardSetMap.put(GameType.TravelingCircus, new CardSet(new Card[]{Cards.fairgrounds, Cards.farmingVillage, Cards.huntingParty, Cards.jester, Cards.menagerie, Cards.borderVillage, Cards.embassy, Cards.foolsGold, Cards.nomadCamp, Cards.oasis}, null));
		CardSetMap.put(GameType.PlayingChessWithDeath, new CardSet(new Card[]{Cards.banditCamp, Cards.graverobber, Cards.junkDealer, Cards.mystic, Cards.pillage, Cards.rats, Cards.sage, Cards.scavenger, Cards.storeroom, Cards.vagrant}, null));
		CardSetMap.put(GameType.GrimParade, new CardSet(new Card[]{Cards.armory, Cards.bandOfMisfits, Cards.catacombs, Cards.cultist, Cards.forager, Cards.fortress, Cards.virtualKnight, Cards.marketSquare, Cards.procession, Cards.huntingGrounds}, null));
		CardSetMap.put(GameType.HighAndLow, new CardSet(new Card[]{Cards.hermit, Cards.huntingGrounds, Cards.mystic, Cards.poorHouse, Cards.wanderingMinstrel, Cards.cellar, Cards.moneyLender, Cards.throneRoom, Cards.witch, Cards.workshop}, null));
		CardSetMap.put(GameType.ChivalryAndRevelry, new CardSet(new Card[]{Cards.altar, Cards.virtualKnight, Cards.rats, Cards.scavenger, Cards.squire, Cards.festival, Cards.gardens, Cards.laboratory, Cards.library, Cards.remodel}, null));
		CardSetMap.put(GameType.Prophecy, new CardSet(new Card[]{Cards.armory, Cards.ironmonger, Cards.mystic, Cards.rebuild, Cards.vagrant, Cards.baron, Cards.conspirator, Cards.greatHall, Cards.nobles, Cards.wishingWell}, null));
		CardSetMap.put(GameType.Invasion, new CardSet(new Card[]{Cards.beggar, Cards.marauder, Cards.rogue, Cards.squire, Cards.urchin, Cards.harem, Cards.miningVillage, Cards.swindler, Cards.torturer, Cards.upgrade}, null));
		CardSetMap.put(GameType.WateryGraves, new CardSet(new Card[]{Cards.count, Cards.graverobber, Cards.hermit, Cards.scavenger, Cards.urchin, Cards.nativeVillage, Cards.pirateShip, Cards.salvager, Cards.treasureMap, Cards.treasury}, null));
		CardSetMap.put(GameType.Peasants, new CardSet(new Card[]{Cards.deathCart, Cards.feodum, Cards.poorHouse, Cards.urchin, Cards.vagrant, Cards.fishingVillage, Cards.haven, Cards.island, Cards.lookout, Cards.warehouse}, null));
		CardSetMap.put(GameType.Infestations, new CardSet(new Card[]{Cards.armory, Cards.cultist, Cards.feodum, Cards.marketSquare, Cards.rats, Cards.wanderingMinstrel, Cards.apprentice, Cards.scryingPool, Cards.transmute, Cards.vineyard}, null));
		CardSetMap.put(GameType.OneMansTrash, new CardSet(new Card[]{Cards.counterfeit, Cards.forager, Cards.graverobber, Cards.marketSquare, Cards.rogue, Cards.city, Cards.grandMarket, Cards.monument, Cards.talisman, Cards.venture}, null));
		CardSetMap.put(GameType.HonorAmongThieves, new CardSet(new Card[]{Cards.banditCamp, Cards.procession, Cards.rebuild, Cards.rogue, Cards.squire, Cards.forge, Cards.hoard, Cards.peddler, Cards.quarry, Cards.watchTower}, null));
		CardSetMap.put(GameType.DarkCarnival, new CardSet(new Card[]{Cards.bandOfMisfits, Cards.cultist, Cards.fortress, Cards.hermit, Cards.junkDealer, Cards.virtualKnight, Cards.fairgrounds, Cards.hamlet, Cards.hornOfPlenty, Cards.menagerie}, null));
		CardSetMap.put(GameType.ToTheVictor, new CardSet(new Card[]{Cards.banditCamp, Cards.counterfeit, Cards.deathCart, Cards.marauder, Cards.pillage, Cards.sage, Cards.harvest, Cards.huntingParty, Cards.remake, Cards.tournament}, null));
		CardSetMap.put(GameType.FarFromHome, new CardSet(new Card[]{Cards.beggar, Cards.count, Cards.feodum, Cards.marauder, Cards.wanderingMinstrel, Cards.cartographer, Cards.develop, Cards.embassy, Cards.foolsGold, Cards.haggler}, null));
		CardSetMap.put(GameType.Expeditions, new CardSet(new Card[]{Cards.altar, Cards.catacombs, Cards.ironmonger, Cards.poorHouse, Cards.storeroom, Cards.crossroads, Cards.farmland, Cards.highway, Cards.spiceMerchant, Cards.tunnel}, null));
		CardSetMap.put(GameType.Lamentations, new CardSet(new Card[]{Cards.beggar, Cards.catacombs, Cards.counterfeit, Cards.forager, Cards.ironmonger, Cards.pillage, Cards.apothecary, Cards.golem, Cards.herbalist, Cards.university}, null));
		
		CardSetMap.put(GameType.ArtsAndCrafts,      new CardSet(new Card[]{Cards.stonemason, Cards.advisor, Cards.baker, Cards.journeyman, Cards.merchantGuild, Cards.laboratory, Cards.cellar, Cards.workshop, Cards.festival, Cards.moneyLender}, null));
		CardSetMap.put(GameType.CleanLiving,        new CardSet(new Card[]{Cards.butcher, Cards.baker, Cards.candlestickMaker, Cards.doctor, Cards.soothsayer, Cards.militia, Cards.thief, Cards.moneyLender, Cards.gardens, Cards.village}, null));
		CardSetMap.put(GameType.GildingTheLily,     new CardSet(new Card[]{Cards.plaza, Cards.masterpiece, Cards.candlestickMaker, Cards.taxman, Cards.herald, Cards.library, Cards.remodel, Cards.adventurer, Cards.market, Cards.chancellor}, null));
		CardSetMap.put(GameType.NameThatCard,       new CardSet(new Card[]{Cards.baker, Cards.doctor, Cards.plaza, Cards.advisor, Cards.masterpiece, Cards.courtyard, Cards.wishingWell, Cards.harem, Cards.tribute, Cards.nobles}, null));
		CardSetMap.put(GameType.TricksOfTheTrade,   new CardSet(new Card[]{Cards.stonemason, Cards.herald, Cards.soothsayer, Cards.journeyman, Cards.butcher, Cards.greatHall, Cards.nobles, Cards.conspirator, Cards.masquerade, Cards.coppersmith}, null));
		CardSetMap.put(GameType.DecisionsDecisions, new CardSet(new Card[]{Cards.merchantGuild, Cards.candlestickMaker, Cards.masterpiece, Cards.taxman, Cards.butcher, Cards.bridge, Cards.pawn, Cards.miningVillage, Cards.upgrade, Cards.duke}, null));
		
		CardSetMap.put(GameType.TestGuilds, new CardSet(new Card[]{Cards.merchantGuild, Cards.herald, Cards.butcher, Cards.familiar, Cards.golem, Cards.counterfeit, Cards.herbalist, Cards.village, Cards.stonemason, Cards.bank}, null));
	}

}
