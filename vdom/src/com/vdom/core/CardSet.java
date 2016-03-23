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
	
	public static CardSet getCardSet(final GameType type, int count) {
		return getCardSet(type, count, null, false, -1, false);
	}

	public static CardSet getCardSet(final GameType type, int count, List<Expansion> randomExpansions, boolean randomIncludeEvents, int numRandomEvents, boolean adjustRandomForAlchemy) {
		CardSet set = CardSet.CardSetMap.get(type);

		if(set == null) {
			set = CardSet.getCardSet(CardSet.defaultGameType, count);
		}

		if(set.isRandom) {
			List<Card> cards;
			if (randomExpansions != null && randomExpansions.size() > 0) {
				cards = new ArrayList<Card>();
				for (Expansion expansion : randomExpansions) {
					cards.addAll(expansion.getKingdomCards());
				}
			} else {
				cards = new ArrayList<Card>(set.getCards());
			}
			if (randomIncludeEvents) {
				cards.addAll(Cards.eventsCards);
			}
			set = CardSet.getRandomCardSet(cards, count, numRandomEvents, adjustRandomForAlchemy);
		}

		return set;
	}
	
	public static CardSet getRandomCardSet(final List<Card> possibleCards) {
		return getRandomCardSet(possibleCards, -1, 0, false);
	}

	public static CardSet getRandomCardSet(final List<Card> possibleCards, int count) {
		return getRandomCardSet(possibleCards, count, 0, false);
	}
	
	/**
	 * Gets a random card set containing the specified number of cards from the given possible cards
	 * 
	 * @param possibleCards Possible cards and Events to choose from
	 * @param count the number of cards to include in the result. -1 means to use the default number 
	 *        of Kingdom piles and include a Bane card if needed.
	 * @param eventCount number of Events to include in the result. Negative numbers mean to include
	 *        at most that absolute value of Events using the selection method suggested in the rules 
	 * @return A random CardSet selected from the list of entered cards
	 */
	public static CardSet getRandomCardSet(List<Card> possibleCards, int count, int eventCount, boolean adjustForAlchemy) {
		final List<Card> cardSetList = new ArrayList<Card>();
		final List<Card> eventList = new ArrayList<Card>();
		
		possibleCards = new ArrayList<Card>(possibleCards);
		shuffle(possibleCards);
		
		Card baneCard = null;
		boolean findBandIfNeeded = false;
		if (count < 0) {
			count = 10;
			findBandIfNeeded = true;
		}
		
		if (eventCount <= 0) {
			//negative number means take events as they come up until abs(eventCount) events - as in Dominion Adventures rules
			int maxEvents = Math.abs(eventCount);
			int numEvents = countEvents(possibleCards);
			count = Math.min(possibleCards.size() - numEvents, count);
			for (Card c : possibleCards) {
				if (c.isEvent()) {
					if (eventList.size() < maxEvents) {
						eventList.add(c);
					}
				} else {
					cardSetList.add(c);
					if (cardSetList.size() == count) {
						break;
					}
				}
			}
			
		} else {
			//partition events into a separate list and pick them separately
			List<Card> events = new ArrayList<Card>();
			List<Card> cards = new ArrayList<Card>();
			for (Card c : possibleCards) {
				if (c.isEvent())
					events.add(c);
				else
					cards.add(c);
			}
			
			eventCount = Math.min(eventCount, events.size());
			pick(cards, cardSetList, count);
			pick(events, eventList, eventCount);
		}
		
		// Do Alchemy recommendation - if at least one Alchemy card is in, use 3 to 5 at once
		if (adjustForAlchemy)
			performAlchemyRecommendation(cardSetList, possibleCards);
		
		// pick a band card if needed
		if (findBandIfNeeded && cardSetList.contains(Cards.youngWitch)) {
			for (Card c : possibleCards) {
				if (cardSetList.contains(c))
					continue;
	            if (isValidBane(c)) {
					baneCard = c;
				}
			}
			if (baneCard == null) {
				baneCard = swapOutBaneCard(cardSetList, possibleCards);
			}
		}
		
		cardSetList.addAll(eventList);
		
		return new CardSet(cardSetList, baneCard);
	}

	private static int MIN_RECOMMENDED_ALCHEMY = 3;
	private static int MAX_RECOMMENDED_ALCHEMY = 5;
	private static void performAlchemyRecommendation(List<Card> cardSetList, List<Card> possibleCards) {
		if (cardSetList.size() < MIN_RECOMMENDED_ALCHEMY)
			return;
		
		Set<Card> alchemyCardsUsed = new HashSet<Card>();
		for (Card c : cardSetList) {
			if (c.getExpansion().equals("Alchemy"))
				alchemyCardsUsed.add(c);
		}
		
		int numAlchemyCards = alchemyCardsUsed.size();
		if (!(numAlchemyCards < MIN_RECOMMENDED_ALCHEMY))
			return;
		
		//count alchemy cards available out of total cards
		int numAlchemyCardsAvailable = 0;
		int numKindgomCardsAvailable = 0;
		for (Card c : possibleCards) {
			if (!Cards.isKingdomCard(c))
				continue;
			numKindgomCardsAvailable++;
			if (c.getExpansion().equals("Alchemy"))
				numAlchemyCardsAvailable++;
		}
		
		if (numKindgomCardsAvailable - numAlchemyCardsAvailable < cardSetList.size()) {
			return;
		}
		
		// whether we use the adjustment for the recommended number of Alchemy cards is dependent on the proportion of
		//   cards that are Alchemy cards - if not we're not using, we remove all Alchemy cards rather than leave
		//   an amount less than the recommended number to play with
		boolean useAlchemyCards = rand.nextInt(numKindgomCardsAvailable) < numAlchemyCardsAvailable;
		
		if (!useAlchemyCards) {
			if (numAlchemyCards == 0)
				return;
			// replace any alchemy cards with others
			LinkedList<Card> cardsToPickFrom = new LinkedList<Card>();
			for (Card c : possibleCards) {
				if (!Cards.isKingdomCard(c) 
						|| cardSetList.contains(c) 
						|| c.getExpansion().equals("Alchemy"))
					continue;
				cardsToPickFrom.add(c);
			}
			
			for(int i = 0; i < cardSetList.size(); ++i) {
				if (cardSetList.get(i).getExpansion().equals("Alchemy")) {
					Card replacement = cardsToPickFrom.remove(rand.nextInt(cardsToPickFrom.size()));
					cardSetList.set(i,  replacement);
				}
			}
			return;
		}
		
		int numAlchemyCardsToUse = rand.nextInt(MAX_RECOMMENDED_ALCHEMY - MIN_RECOMMENDED_ALCHEMY + 1) + MIN_RECOMMENDED_ALCHEMY;
		numAlchemyCardsToUse = Math.min(numAlchemyCardsToUse, Expansion.Alchemy.getKingdomCards().size() - numAlchemyCards);
		
		LinkedList<Card> cardsToPickFrom = new LinkedList<Card>();
		for (Card c : Expansion.Alchemy.getKingdomCards()) {
			if (!alchemyCardsUsed.contains(c))
				cardsToPickFrom.add(c);
		}

		for(int i = 0; i < cardSetList.size(); ++i) {
			if (alchemyCardsUsed.contains(cardSetList.get(i)))
				continue;
			Card replacement = cardsToPickFrom.remove(rand.nextInt(cardsToPickFrom.size()));
			cardSetList.set(i, replacement);
			numAlchemyCards++;
			if (numAlchemyCards == numAlchemyCardsToUse)
				break;
		}
	}

	private static Card swapOutBaneCard(List<Card> swapFrom, List<Card> replaceFrom) {
		Card bane = null;
		for (Card c : swapFrom) {
			if (isValidBane(c)) {
				bane = c;
				break;
			}
		}
		
		if (bane == null)
			return null;
		
		swapFrom.remove(bane);
		for (Card c : replaceFrom) {
			if (!c.equals(bane) && !swapFrom.contains(c)) {
				swapFrom.add(c);
				break;
			}
		}
		return bane;
	}
	
	private static boolean isValidBane(Card c) {
		int cost = c.getCost(null);
		return !c.costPotion() && (cost == 2 || cost == 3) && !c.isEvent();
	}

	private static void pick(List<Card> source, List<Card> target, int count) {
		shuffle(source);
		for (Card c : source) {
			target.add(c);
			if (target.size() == count)
				break;
		}
	}

	private static int countEvents(List<Card> allCards) {
		int numEvents = 0;
		for (Card c : allCards) {
			if (c.isEvent())
				numEvents++;
		}
		return numEvents;
	}

	private static void shuffle(List<Card> cards) {
		int numCards = cards.size();
		for (int i = 0; i < numCards; ++i) {
			int newIdx = rand.nextInt(numCards);
			Card tmp = cards.get(i);
			cards.set(i, cards.get(newIdx));
			cards.set(newIdx, tmp);
		}
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
		CardSetMap.put(GameType.RandomAdventures, new CardSet(Cards.actionCardsAdventures, true));

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

    /*frr18 AdventureTest*/
		CardSetMap.put(GameType.AdventureTest, new CardSet(new Card[]{Cards.page, Cards.peasant, Cards.university, Cards.watchTower, Cards.hermit, Cards.deathCart, Cards.marauder, Cards.urchin, Cards.tournament, Cards.borrow, Cards.ferry, Cards.youngWitch}, Cards.menagerie));
		CardSetMap.put(GameType.GentleIntro, new CardSet(new Card[] { Cards.amulet, Cards.distantLands, Cards.dungeon, Cards.duplicate, Cards.giant, Cards.hireling, Cards.port, Cards.ranger, Cards.ratcatcher, Cards.treasureTrove, Cards.scoutingParty}, null));
		CardSetMap.put(GameType.ExpertIntro, new CardSet(new Card[] { Cards.caravanGuard, Cards.coinOfTheRealm, Cards.hauntedWoods, Cards.lostCity, Cards.magpie, Cards.peasant, Cards.raze, Cards.swampHag, Cards.transmogrify, Cards.wineMerchant, Cards.mission, Cards.plan}, null));
		CardSetMap.put(GameType.LevelUp, new CardSet(new Card[] { Cards.dungeon, Cards.gear, Cards.guide, Cards.lostCity, Cards.miser, Cards.market, Cards.militia, Cards.spy, Cards.throneRoom, Cards.workshop, Cards.training}, null));
		CardSetMap.put(GameType.SonOfSizeDistortion, new CardSet(new Card[] { Cards.amulet, Cards.duplicate, Cards.giant, Cards.messenger, Cards.treasureTrove, Cards.bureaucrat, Cards.gardens, Cards.moneyLender, Cards.thief, Cards.witch, Cards.bonfire, Cards.raid}, null));
		CardSetMap.put(GameType.RoyaltyFactory, new CardSet(new Card[] { Cards.bridgeTroll, Cards.duplicate, Cards.page, Cards.raze, Cards.royalCarriage, Cards.conspirator, Cards.harem, Cards.nobles, Cards.secretChamber, Cards.swindler, Cards.pilgrimage}, null));
		CardSetMap.put(GameType.MastersOfFinance, new CardSet(new Card[] { Cards.artificer, Cards.distantLands, Cards.gear, Cards.transmogrify, Cards.wineMerchant, Cards.bridge, Cards.pawn, Cards.shantyTown, Cards.steward, Cards.upgrade, Cards.ball, Cards.borrow}, null));
		CardSetMap.put(GameType.PrinceOfOrange, new CardSet(new Card[] { Cards.amulet, Cards.dungeon, Cards.hauntedWoods, Cards.page, Cards.swampHag, Cards.caravan, Cards.fishingVillage, Cards.merchantShip, Cards.tactician, Cards.treasureMap, Cards.mission}, null));
		CardSetMap.put(GameType.GiftsAndMathoms, new CardSet(new Card[] { Cards.bridgeTroll, Cards.caravanGuard, Cards.hireling, Cards.lostCity, Cards.messenger, Cards.ambassador, Cards.embargo, Cards.haven, Cards.salvager, Cards.smugglers, Cards.expedition, Cards.quest}, null));
		CardSetMap.put(GameType.HastePotion, new CardSet(new Card[] { Cards.magpie, Cards.messenger, Cards.port, Cards.royalCarriage, Cards.treasureTrove, Cards.apprentice, Cards.scryingPool, Cards.transmute, Cards.university, Cards.vineyard, Cards.potion, Cards.plan}, null));
		CardSetMap.put(GameType.Cursecatchers, new CardSet(new Card[] { Cards.amulet, Cards.bridgeTroll, Cards.caravanGuard, Cards.peasant, Cards.ratcatcher, Cards.apothecary, Cards.familiar, Cards.golem, Cards.herbalist, Cards.philosophersStone, Cards.potion, Cards.save, Cards.trade}, null));
		CardSetMap.put(GameType.LastWillAndMonument, new CardSet(new Card[] { Cards.coinOfTheRealm, Cards.dungeon, Cards.messenger, Cards.relic, Cards.treasureTrove, Cards.bishop, Cards.countingHouse, Cards.monument, Cards.rabble, Cards.vault, Cards.inheritance}, null));
		CardSetMap.put(GameType.ThinkBig, new CardSet(new Card[] { Cards.distantLands, Cards.giant, Cards.hireling, Cards.miser, Cards.storyteller, Cards.contraband, Cards.expand, Cards.hoard, Cards.kingsCourt, Cards.peddler, Cards.ball, Cards.ferry}, null));
		CardSetMap.put(GameType.TheHerosReturn, new CardSet(new Card[] { Cards.artificer, Cards.miser, Cards.page, Cards.ranger, Cards.relic, Cards.fairgrounds, Cards.farmingVillage, Cards.horseTraders, Cards.jester, Cards.menagerie}, null));
		CardSetMap.put(GameType.SeacraftAndWitchcraft, new CardSet(new Card[] { Cards.peasant, Cards.storyteller, Cards.swampHag, Cards.transmogrify, Cards.wineMerchant, Cards.fortuneTeller, Cards.hamlet, Cards.hornOfPlenty, Cards.tournament, Cards.youngWitch, Cards.ferry, Cards.seaway}, Cards.guide));
		CardSetMap.put(GameType.TradersAndRaiders, new CardSet(new Card[] { Cards.hauntedWoods, Cards.lostCity, Cards.page, Cards.port, Cards.wineMerchant, Cards.develop, Cards.farmland, Cards.haggler, Cards.spiceMerchant, Cards.trader, Cards.raid}, null));
		CardSetMap.put(GameType.Journeys, new CardSet(new Card[] { Cards.bridgeTroll, Cards.distantLands, Cards.giant, Cards.guide, Cards.ranger, Cards.cartographer, Cards.crossroads, Cards.highway, Cards.inn, Cards.silkRoad, Cards.expedition, Cards.inheritance}, null));
		CardSetMap.put(GameType.CemeteryPolka, new CardSet(new Card[] { Cards.amulet, Cards.caravanGuard, Cards.hireling, Cards.peasant, Cards.relic, Cards.graverobber, Cards.marauder, Cards.procession, Cards.rogue, Cards.wanderingMinstrel, Cards.alms}, null));
		CardSetMap.put(GameType.GroovyDecay, new CardSet(new Card[] { Cards.dungeon, Cards.hauntedWoods, Cards.ratcatcher, Cards.raze, Cards.transmogrify, Cards.cultist, Cards.deathCart, Cards.fortress, Cards.rats, Cards.lostArts, Cards.pathfinding}, null));
		CardSetMap.put(GameType.Spendthrift, new CardSet(new Card[] { Cards.artificer, Cards.gear, Cards.magpie, Cards.miser, Cards.storyteller, Cards.doctor, Cards.masterpiece, Cards.merchantGuild, Cards.soothsayer, Cards.stonemason, Cards.lostArts}, null));
		CardSetMap.put(GameType.QueenOfTan, new CardSet(new Card[] { Cards.coinOfTheRealm, Cards.duplicate, Cards.guide, Cards.ratcatcher, Cards.royalCarriage, Cards.advisor, Cards.butcher, Cards.candlestickMaker, Cards.herald, Cards.journeyman, Cards.pathfinding, Cards.save}, null));

	}

}
