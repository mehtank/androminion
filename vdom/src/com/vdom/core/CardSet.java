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
	
	public static enum UseOptionalCards {
		Random,
		Use,
		DontUse
	}

	private final List<Card> cards;
	private final Card baneCard;
	private final boolean isRandom;
	private final UseOptionalCards useShelters;
	private final UseOptionalCards usePlatColony;

	private CardSet(final List<Card> cards, final Card baneCard) {
		this.cards = cards;
		this.baneCard = baneCard;
		this.isRandom = false;
		this.usePlatColony = UseOptionalCards.Random;
		this.useShelters = UseOptionalCards.Random;
	}
	
	private CardSet(final Card[] cardsArray) {
		this(cardsArray, null, UseOptionalCards.Random, UseOptionalCards.Random);
	}
	
	private CardSet(final Card[] cardsArray, final Card baneCard) {
		this(cardsArray, baneCard, UseOptionalCards.Random, UseOptionalCards.Random);
	}

	private CardSet(final Card[] cardsArray, final Card baneCard, UseOptionalCards usePlatColony, UseOptionalCards useShelters) {
		this.cards = Arrays.asList(cardsArray);
		this.baneCard = baneCard;
		this.isRandom = false;
		this.usePlatColony = usePlatColony;
		this.useShelters = useShelters;
	}

	private CardSet(final List<Card> cards, final boolean isRandom) {
		this.cards = cards;
		this.baneCard = null;
		this.isRandom = isRandom;
		this.usePlatColony = UseOptionalCards.Random;
		this.useShelters = UseOptionalCards.Random;
	}
	
	public static CardSet getCardSet(final GameType type, int count) {
		return getCardSet(type, count, null, null, false, -1, false, -1, false, false);
	}

	public static CardSet getCardSet(final GameType type, int count, List<Expansion> randomExpansions, 
			List<Expansion> randomExcludedExpansions,
			boolean randomIncludeEvents, int numRandomEvents, 
			boolean randomIncludeLandmarks, int numRandomLandmarks, 
			boolean linkMaxEventsAndLandmarks, 
			boolean adjustRandomForAlchemy) {
		CardSet set = CardSet.CardSetMap.get(type);

		if(set == null) {
			set = CardSet.getCardSet(CardSet.defaultGameType, count);
		}

		if(set.isRandom) {
			List<Card> cards;
			if (randomExpansions != null && randomExpansions.size() > 0) {
				Set<Card> cardSet = new HashSet<Card>();
				for (Expansion expansion : randomExpansions) {
					if (randomExcludedExpansions != null && randomExcludedExpansions.contains(expansion))
						continue;
					cardSet.addAll(expansion.getKingdomCards());
				}
				cards = new ArrayList<Card>();
				cards.addAll(cardSet);
			} else {
				List<Expansion> expansions = new ArrayList<Expansion>();
				for (Expansion expansion : Expansion.values()) {
					if (expansion.isAggregate() || 
							(randomExcludedExpansions != null && randomExcludedExpansions.contains(expansion)))
						continue;
					expansions.add(expansion);
				}
				Set<Card> cardSet = new HashSet<Card>();
				for (Expansion expansion : expansions) {
					cardSet.addAll(expansion.getKingdomCards());
				}
				cards = new ArrayList<Card>();
				cards.addAll(cardSet);
			}
			if (randomIncludeEvents) {
				cards.addAll(Cards.eventsCards);
			}
			if (randomIncludeLandmarks) {
				cards.addAll(Cards.landmarkCards);
			}
			set = CardSet.getRandomCardSet(cards, count, numRandomEvents, numRandomLandmarks, linkMaxEventsAndLandmarks, adjustRandomForAlchemy);
		}

		return set;
	}
	
	public static CardSet getRandomCardSet(final List<Card> possibleCards) {
		return getRandomCardSet(possibleCards, -1, 0, 0, false, false);
	}

	public static CardSet getRandomCardSet(final List<Card> possibleCards, int count) {
		return getRandomCardSet(possibleCards, count, 0, 0, false, false);
	}
	
	/**
	 * Gets a random card set containing the specified number of cards from the given possible cards
	 * 
	 * @param possibleCards Possible cards and Events to choose from
	 * @param count the number of cards to include in the result. -1 means to use the default number 
	 *        of Kingdom piles and include a Bane card if needed.
	 * @param eventCount number of Events to include in the result. Negative numbers mean to include
	 *        at most that absolute value of Events using the selection method suggested in the rules 
	 * @param landmarkCount number of Landmarks to include in the result. Negative numbers mean to include
	 *        at most that absolute value of Landmarks using the selection method suggested in the rules
	 * @param linkMaxEventsAndLandmarks true means to use the max of eventCount and landmarkCount if both are
	 *        negative and use the combined total for where to stop
	 * @return A random CardSet selected from the list of entered cards
	 */
	public static CardSet getRandomCardSet(List<Card> possibleCards, int count, int eventCount, int landmarkCount, boolean linkMaxEventsAndLandmarks, boolean adjustForAlchemy) {
		final List<Card> cardSetList = new ArrayList<Card>();
		final List<Card> eventList = new ArrayList<Card>();
		final List<Card> landmarkList = new ArrayList<Card>();
		
		possibleCards = new ArrayList<Card>(possibleCards);
		shuffle(possibleCards);
		
		Card baneCard = null;
		boolean findBandIfNeeded = false;
		if (count < 0) {
			count = 10;
			findBandIfNeeded = true;
		}
		
		//negative number means take events as they come up until abs(eventCount) events - as in Dominion Adventures rules
		boolean drawEvents = eventCount < 0;
		boolean drawLandmarks = landmarkCount < 0;
		int maxEvents = Math.abs(eventCount);
		int maxLandmarks = Math.abs(landmarkCount);
		if (drawEvents && drawLandmarks && linkMaxEventsAndLandmarks) {
			maxEvents = maxLandmarks = Math.min(maxEvents, maxLandmarks);
		} else {
			linkMaxEventsAndLandmarks = false;
		}
		int numEvents = countEvents(possibleCards);
		int numLandmarks = countLandmarks(possibleCards);
		count = Math.min(possibleCards.size() - numEvents - numLandmarks, count);
		for (Card c : possibleCards) {
			if (c.is(Type.Event, null)) { 
				if(drawEvents) {
					if (linkMaxEventsAndLandmarks) {
						if (eventList.size() + landmarkList.size() < maxEvents)
							eventList.add(c);
					} else if (eventList.size() < maxEvents) {
						eventList.add(c);
					}
				}
			} else if (c.is(Type.Landmark, null)) { 
				if (drawLandmarks) {
					if (linkMaxEventsAndLandmarks) {
						if (eventList.size() + landmarkList.size() < maxEvents)
							landmarkList.add(c);
					} else if (landmarkList.size() < maxEvents) {
						landmarkList.add(c);
					}
				}
			} else {
				cardSetList.add(c);
				if (cardSetList.size() == count) {
					break;
				}
			}
		}
		
		// if needed, partition events/landmarks into a separate list and pick them separately
		if ((!drawEvents && eventCount > 0) || (!drawLandmarks && landmarkCount > 0)) {
			List<Card> events = new ArrayList<Card>();
			List<Card> landmarks = new ArrayList<Card>();
			for (Card c : possibleCards) {
				if (c.is(Type.Event, null))
					events.add(c);
				else if (c.is(Type.Landmark, null))
					landmarks.add(c);
			}
			eventCount = Math.min(eventCount, events.size());
			landmarkCount = Math.min(landmarkCount, landmarks.size());
			if (eventCount > 0) {
				pick(events, eventList, eventCount);
			}
			if (landmarkCount > 0) {
				pick(landmarks, landmarkList, landmarkCount);
			}
		}
		
		// Do Alchemy recommendation - if at least one Alchemy card is in, use 3 to 5 at once
		if (adjustForAlchemy)
			performAlchemyRecommendation(cardSetList, possibleCards);
		
		// pick a bane card if needed
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
		cardSetList.addAll(landmarkList);
		
		return new CardSet(cardSetList, baneCard);
	}
	
	public static Card getBaneCard(List<Card> cards) {
		for (Card c : cards) {
			if (isValidBane(c)) return c;
		}
		return null;
	}

	private static int MIN_RECOMMENDED_ALCHEMY = 3;
	private static int MAX_RECOMMENDED_ALCHEMY = 5;
	private static void performAlchemyRecommendation(List<Card> cardSetList, List<Card> possibleCards) {
		if (cardSetList.size() < MIN_RECOMMENDED_ALCHEMY)
			return;
		
		Set<Card> alchemyCardsUsed = new HashSet<Card>();
		for (Card c : cardSetList) {
			if (c.getExpansion() == Expansion.Alchemy)
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
			if (c.getExpansion() == Expansion.Alchemy)
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
						|| c.getExpansion() == Expansion.Alchemy)
					continue;
				cardsToPickFrom.add(c);
			}
			
			for(int i = 0; i < cardSetList.size(); ++i) {
				if (cardSetList.get(i).getExpansion() == Expansion.Alchemy) {
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
			if (!c.equals(bane) && !swapFrom.contains(c) && !c.is(Type.Event) && !c.is(Type.Landmark)) {
				swapFrom.add(c);
				break;
			}
		}
		return bane;
	}
	
	private static boolean isValidBane(Card c) {
		int cost = c.getCost(null);
		return !c.costPotion() && (cost == 2 || cost == 3) && !c.is(Type.Event, null) && !c.is(Type.Landmark, null);
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
			if (c.is(Type.Event, null))
				numEvents++;
		}
		return numEvents;
	}
	
	private static int countLandmarks(List<Card> allCards) {
		int numLandmarks = 0;
		for (Card c : allCards) {
			if (c.is(Type.Landmark, null))
				numLandmarks++;
		}
		return numLandmarks;
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
	
	public UseOptionalCards getUsePlatColony() {
		return usePlatColony;
	}
	
	public UseOptionalCards getUseShelters() {
		return useShelters;
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
		// Dominion
		CardSetMap.put(GameType.FirstGame, new CardSet(new Card[]{Cards.cellar, Cards.market, Cards.militia, Cards.mine, Cards.moat, Cards.remodel, Cards.smithy, Cards.village, Cards.woodcutter, Cards.workshop}));
		CardSetMap.put(GameType.BigMoney, new CardSet(new Card[]{Cards.adventurer, Cards.bureaucrat, Cards.chancellor, Cards.chapel, Cards.feast, Cards.laboratory, Cards.market, Cards.mine, Cards.moneyLender, Cards.throneRoom}));
		CardSetMap.put(GameType.Interaction, new CardSet(new Card[]{Cards.bureaucrat, Cards.chancellor, Cards.councilRoom, Cards.festival, Cards.library, Cards.militia, Cards.moat, Cards.spy, Cards.thief, Cards.village}));
		CardSetMap.put(GameType.SizeDistortion, new CardSet(new Card[]{Cards.cellar, Cards.chapel, Cards.feast, Cards.gardens, Cards.laboratory, Cards.thief, Cards.village, Cards.witch, Cards.woodcutter, Cards.workshop}));
		CardSetMap.put(GameType.VillageSquare, new CardSet(new Card[]{Cards.bureaucrat, Cards.cellar, Cards.festival, Cards.library, Cards.market, Cards.remodel, Cards.smithy, Cards.throneRoom, Cards.village, Cards.woodcutter}, null));
		// Dominion 2E
		CardSetMap.put(GameType.FirstGame2, new CardSet(new Card[]{Cards.cellar, Cards.market, Cards.merchant, Cards.militia, Cards.mine, Cards.moat, Cards.remodel, Cards.smithy, Cards.village, Cards.workshop}));
		CardSetMap.put(GameType.SizeDistortion2, new CardSet(new Card[]{Cards.artisan, Cards.bandit, Cards.bureaucrat, Cards.chapel, Cards.festival, Cards.gardens, Cards.sentry, Cards.throneRoom, Cards.witch, Cards.workshop}));
		CardSetMap.put(GameType.DeckTop, new CardSet(new Card[]{Cards.artisan, Cards.bureaucrat, Cards.councilRoom, Cards.festival, Cards.harbinger, Cards.laboratory, Cards.moneyLender, Cards.sentry, Cards.vassal, Cards.village}));
		CardSetMap.put(GameType.SleightOfHand, new CardSet(new Card[]{Cards.cellar, Cards.councilRoom, Cards.festival, Cards.gardens, Cards.library, Cards.harbinger, Cards.militia, Cards.poacher, Cards.smithy, Cards.throneRoom}));
		CardSetMap.put(GameType.Improvements, new CardSet(new Card[]{Cards.artisan, Cards.cellar, Cards.market, Cards.merchant, Cards.mine, Cards.moat, Cards.moneyLender, Cards.poacher, Cards.remodel, Cards.witch}));
		CardSetMap.put(GameType.SilverAndGold, new CardSet(new Card[]{Cards.bandit, Cards.bureaucrat, Cards.chapel, Cards.harbinger, Cards.laboratory, Cards.merchant, Cards.mine, Cards.moneyLender, Cards.throneRoom, Cards.vassal}));
		
		// Intrigue
		CardSetMap.put(GameType.VictoryDance, new CardSet(new Card[]{Cards.bridge, Cards.duke, Cards.greatHall, Cards.harem, Cards.ironworks, Cards.masquerade, Cards.nobles, Cards.pawn, Cards.scout, Cards.upgrade}));
		CardSetMap.put(GameType.SecretSchemes, new CardSet(new Card[]{Cards.conspirator, Cards.harem, Cards.ironworks, Cards.pawn, Cards.saboteur, Cards.shantyTown, Cards.steward, Cards.swindler, Cards.tradingPost, Cards.tribute}));
		CardSetMap.put(GameType.BestWishes, new CardSet(new Card[]{Cards.coppersmith, Cards.courtyard, Cards.masquerade, Cards.scout, Cards.shantyTown, Cards.steward, Cards.torturer, Cards.tradingPost, Cards.upgrade, Cards.wishingWell}));
		// Intrigue 2E
		CardSetMap.put(GameType.VictoryDance2, new CardSet(new Card[]{Cards.baron, Cards.courtier, Cards.duke, Cards.harem, Cards.ironworks, Cards.masquerade, Cards.mill, Cards.nobles, Cards.patrol, Cards.replace}));
		CardSetMap.put(GameType.ThePlotThickens, new CardSet(new Card[]{Cards.conspirator, Cards.ironworks, Cards.lurker, Cards.pawn, Cards.miningVillage, Cards.secretPassage, Cards.steward, Cards.swindler, Cards.torturer, Cards.tradingPost}));
		CardSetMap.put(GameType.BestWishes2, new CardSet(new Card[]{Cards.baron, Cards.conspirator, Cards.courtyard, Cards.diplomat, Cards.duke, Cards.secretPassage, Cards.shantyTown, Cards.torturer, Cards.upgrade, Cards.wishingWell}));
		// Intrigue & Dominion
		CardSetMap.put(GameType.Deconstruction, new CardSet(new Card[]{Cards.bridge, Cards.miningVillage, Cards.remodel, Cards.saboteur, Cards.secretChamber, Cards.spy, Cards.swindler, Cards.thief, Cards.throneRoom, Cards.torturer}));
		CardSetMap.put(GameType.HandMadness, new CardSet(new Card[]{Cards.bureaucrat, Cards.chancellor, Cards.councilRoom, Cards.courtyard, Cards.mine, Cards.militia, Cards.minion, Cards.nobles, Cards.steward, Cards.torturer}));
		CardSetMap.put(GameType.Underlings, new CardSet(new Card[]{Cards.baron, Cards.cellar, Cards.festival, Cards.library, Cards.masquerade, Cards.minion, Cards.nobles, Cards.pawn, Cards.steward, Cards.witch}));
		// Intrigue 2E & Dominion 2E
		CardSetMap.put(GameType.Underlings2, new CardSet(new Card[]{Cards.cellar, Cards.festival, Cards.library, Cards.sentry, Cards.vassal, Cards.courtier, Cards.diplomat, Cards.minion, Cards.nobles, Cards.pawn}));
		CardSetMap.put(GameType.GrandScheme, new CardSet(new Card[]{Cards.artisan, Cards.councilRoom, Cards.market, Cards.militia, Cards.workshop, Cards.bridge, Cards.mill, Cards.miningVillage, Cards.patrol, Cards.shantyTown}));
		CardSetMap.put(GameType.Deconstruction2, new CardSet(new Card[]{Cards.bandit, Cards.mine, Cards.remodel, Cards.throneRoom, Cards.village, Cards.diplomat, Cards.harem, Cards.lurker, Cards.replace, Cards.swindler}));
		
		// Seaside
		CardSetMap.put(GameType.HighSeas, new CardSet(new Card[]{Cards.bazaar, Cards.caravan, Cards.embargo, Cards.explorer, Cards.haven, Cards.island, Cards.lookout, Cards.pirateShip, Cards.smugglers, Cards.wharf}));
		CardSetMap.put(GameType.BuriedTreasure, new CardSet(new Card[]{Cards.ambassador, Cards.cutpurse, Cards.fishingVillage, Cards.lighthouse, Cards.outpost, Cards.pearlDiver, Cards.tactician, Cards.treasureMap, Cards.warehouse, Cards.wharf}));
		CardSetMap.put(GameType.Shipwrecks, new CardSet(new Card[]{Cards.ghostShip, Cards.merchantShip, Cards.nativeVillage, Cards.navigator, Cards.pearlDiver, Cards.salvager, Cards.seaHag, Cards.smugglers, Cards.treasury, Cards.warehouse}));
		// Seaside & Dominion
		CardSetMap.put(GameType.ReachForTomorrow, new CardSet(new Card[]{Cards.adventurer, Cards.cellar, Cards.councilRoom, Cards.cutpurse, Cards.ghostShip, Cards.lookout, Cards.seaHag, Cards.spy, Cards.treasureMap, Cards.village}));
		CardSetMap.put(GameType.Repetition, new CardSet(new Card[]{Cards.caravan, Cards.chancellor, Cards.explorer, Cards.festival, Cards.militia, Cards.outpost, Cards.pearlDiver, Cards.pirateShip, Cards.treasury, Cards.workshop}));
		CardSetMap.put(GameType.GiveAndTake, new CardSet(new Card[]{Cards.ambassador, Cards.fishingVillage, Cards.haven, Cards.island, Cards.library, Cards.market, Cards.moneyLender, Cards.salvager, Cards.smugglers, Cards.witch}));
		// Seaside & Dominion 2E
		CardSetMap.put(GameType.ReachForTomorrow2, new CardSet(new Card[]{Cards.artisan, Cards.cellar, Cards.councilRoom, Cards.vassal, Cards.village, Cards.cutpurse, Cards.ghostShip, Cards.lookout, Cards.seaHag, Cards.treasureMap}));
		CardSetMap.put(GameType.Repetition2, new CardSet(new Card[]{Cards.festival, Cards.harbinger, Cards.militia, Cards.workshop, Cards.caravan, Cards.explorer, Cards.outpost, Cards.pearlDiver, Cards.pirateShip, Cards.treasury}));
		CardSetMap.put(GameType.GiveAndTake2, new CardSet(new Card[]{Cards.library, Cards.market, Cards.moneyLender, Cards.witch, Cards.ambassador, Cards.fishingVillage, Cards.haven, Cards.island, Cards.salvager, Cards.smugglers}));
		// Seaside & Intrigue 2E
		CardSetMap.put(GameType.AStarToSteerBy, new CardSet(new Card[]{Cards.secretPassage, Cards.diplomat, Cards.swindler, Cards.wishingWell, Cards.courtier, Cards.lookout, Cards.treasureMap, Cards.ghostShip, Cards.haven, Cards.outpost}));
		CardSetMap.put(GameType.ShorePatrol, new CardSet(new Card[]{Cards.patrol, Cards.replace, Cards.shantyTown, Cards.tradingPost, Cards.pawn, Cards.island, Cards.wharf, Cards.cutpurse, Cards.lighthouse, Cards.warehouse}));
		CardSetMap.put(GameType.BridgeCrossing, new CardSet(new Card[]{Cards.lurker, Cards.nobles, Cards.duke, Cards.conspirator, Cards.bridge, Cards.salvager, Cards.embargo, Cards.smugglers, Cards.nativeVillage, Cards.treasury}));
		
		// Alchemy & Dominion
		CardSetMap.put(GameType.ForbiddenArts, new CardSet(new Card[] { Cards.apprentice, Cards.familiar, Cards.possession, Cards.university, Cards.cellar, Cards.councilRoom, Cards.gardens, Cards.laboratory, Cards.thief, Cards.throneRoom }));
		CardSetMap.put(GameType.PotionMixers, new CardSet(new Card[]{Cards.alchemist, Cards.apothecary, Cards.golem, Cards.herbalist, Cards.transmute, Cards.cellar, Cards.chancellor, Cards.festival, Cards.militia, Cards.smithy}));
		CardSetMap.put(GameType.ChemistryLesson, new CardSet(new Card[]{Cards.alchemist, Cards.golem, Cards.philosophersStone, Cards.university, Cards.bureaucrat, Cards.market, Cards.moat, Cards.remodel, Cards.witch, Cards.woodcutter}));
		// Alchemy & Dominion 2E
		CardSetMap.put(GameType.ForbiddenArts2, new CardSet(new Card[] { Cards.bandit, Cards.cellar, Cards.councilRoom, Cards.gardens, Cards.laboratory, Cards.throneRoom, Cards.apprentice, Cards.familiar, Cards.possession, Cards.university }));
		CardSetMap.put(GameType.PotionMixers2, new CardSet(new Card[]{Cards.cellar, Cards.festival, Cards.militia, Cards.poacher, Cards.smithy, Cards.alchemist, Cards.apothecary, Cards.golem, Cards.herbalist, Cards.transmute}));
		CardSetMap.put(GameType.ChemistryLesson2, new CardSet(new Card[]{Cards.bureaucrat, Cards.market, Cards.moat, Cards.remodel, Cards.vassal, Cards.witch, Cards.alchemist, Cards.golem, Cards.philosophersStone, Cards.university}));
		// Alchemy & Intrigue
        CardSetMap.put(GameType.Servants, new CardSet(new Card[] { Cards.golem, Cards.possession, Cards.scryingPool, Cards.transmute, Cards.vineyard, Cards.conspirator, Cards.greatHall, Cards.minion, Cards.pawn, Cards.steward }));
		CardSetMap.put(GameType.SecretResearch, new CardSet(new Card[]{Cards.familiar, Cards.herbalist, Cards.philosophersStone, Cards.university, Cards.bridge, Cards.masquerade, Cards.minion, Cards.nobles, Cards.shantyTown, Cards.torturer}));
		CardSetMap.put(GameType.PoolsToolsAndFools, new CardSet(new Card[]{Cards.apothecary, Cards.apprentice, Cards.golem, Cards.scryingPool, Cards.baron, Cards.coppersmith, Cards.ironworks, Cards.nobles, Cards.tradingPost, Cards.wishingWell}));
		// Alchemy & Intrigue 2E
        CardSetMap.put(GameType.Servants2, new CardSet(new Card[] { Cards.conspirator, Cards.mill, Cards.minion, Cards.pawn, Cards.steward, Cards.golem, Cards.possession, Cards.scryingPool, Cards.transmute, Cards.vineyard }));
		CardSetMap.put(GameType.SecretResearch2, new CardSet(new Card[]{Cards.bridge, Cards.masquerade, Cards.minion, Cards.nobles, Cards.shantyTown, Cards.torturer, Cards.familiar, Cards.herbalist, Cards.philosophersStone, Cards.university}));
		CardSetMap.put(GameType.PoolsToolsAndFools2, new CardSet(new Card[]{Cards.baron, Cards.ironworks, Cards.lurker, Cards.nobles, Cards.tradingPost, Cards.wishingWell, Cards.apothecary, Cards.apprentice, Cards.golem, Cards.scryingPool}));
		// Prosperity		
		CardSetMap.put(GameType.Beginners, new CardSet(new Card[]{Cards.bank, Cards.countingHouse, Cards.expand, Cards.goons, Cards.monument, Cards.rabble, Cards.royalSeal, Cards.venture, Cards.watchTower, Cards.workersVillage}));
		CardSetMap.put(GameType.FriendlyInteractive, new CardSet(new Card[]{Cards.bishop, Cards.city, Cards.contraband, Cards.forge, Cards.hoard, Cards.peddler, Cards.royalSeal, Cards.tradeRoute, Cards.vault, Cards.workersVillage}));
		CardSetMap.put(GameType.BigActions, new CardSet(new Card[]{Cards.city, Cards.expand, Cards.grandMarket, Cards.kingsCourt, Cards.loan, Cards.mint, Cards.quarry, Cards.rabble, Cards.talisman, Cards.vault}));
		// Prosperity & Dominion
		CardSetMap.put(GameType.BiggestMoney, new CardSet(new Card[]{Cards.bank, Cards.grandMarket, Cards.mint, Cards.royalSeal, Cards.venture, Cards.adventurer, Cards.laboratory, Cards.mine, Cards.moneyLender, Cards.spy}));
		CardSetMap.put(GameType.TheKingsArmy, new CardSet(new Card[]{Cards.expand, Cards.goons, Cards.kingsCourt, Cards.rabble, Cards.vault, Cards.bureaucrat, Cards.councilRoom, Cards.moat, Cards.spy, Cards.village}));
		CardSetMap.put(GameType.TheGoodLife, new CardSet(new Card[]{Cards.contraband, Cards.countingHouse, Cards.hoard, Cards.monument, Cards.mountebank, Cards.bureaucrat, Cards.cellar, Cards.chancellor, Cards.gardens, Cards.village}));
		// Prosperity & Dominion 2E
		CardSetMap.put(GameType.BiggestMoney2, new CardSet(new Card[]{Cards.artisan, Cards.harbinger, Cards.laboratory, Cards.mine, Cards.moneyLender, Cards.bank, Cards.grandMarket, Cards.mint, Cards.royalSeal, Cards.venture}));
		CardSetMap.put(GameType.TheKingsArmy2, new CardSet(new Card[]{Cards.bureaucrat, Cards.councilRoom, Cards.merchant, Cards.moat, Cards.village, Cards.expand, Cards.goons, Cards.kingsCourt, Cards.rabble, Cards.vault}));
		CardSetMap.put(GameType.TheGoodLife2, new CardSet(new Card[]{Cards.artisan, Cards.bureaucrat, Cards.cellar, Cards.gardens, Cards.village, Cards.contraband, Cards.countingHouse, Cards.hoard, Cards.monument, Cards.mountebank}));
		// Prosperity & Intrigue
		CardSetMap.put(GameType.PathsToVictory, new CardSet(new Card[]{Cards.bishop, Cards.countingHouse, Cards.goons, Cards.monument, Cards.peddler, Cards.baron, Cards.harem, Cards.pawn, Cards.shantyTown, Cards.upgrade}));
		CardSetMap.put(GameType.AllAlongTheWatchtower, new CardSet(new Card[]{Cards.hoard, Cards.talisman, Cards.tradeRoute, Cards.vault, Cards.watchTower, Cards.bridge, Cards.greatHall, Cards.miningVillage, Cards.pawn, Cards.torturer}));
		CardSetMap.put(GameType.LuckySeven, new CardSet(new Card[]{Cards.bank, Cards.expand, Cards.forge, Cards.kingsCourt, Cards.vault, Cards.bridge, Cards.coppersmith, Cards.swindler, Cards.tribute, Cards.wishingWell}));
		// Prosperity & Intrigue 2E
		CardSetMap.put(GameType.PathsToVictory2, new CardSet(new Card[]{Cards.baron, Cards.harem, Cards.pawn, Cards.shantyTown, Cards.upgrade, Cards.bishop, Cards.countingHouse, Cards.goons, Cards.monument, Cards.peddler}));
		CardSetMap.put(GameType.AllAlongTheWatchtower2, new CardSet(new Card[]{Cards.bridge, Cards.mill, Cards.miningVillage, Cards.pawn, Cards.torturer, Cards.hoard, Cards.talisman, Cards.tradeRoute, Cards.vault, Cards.watchTower}));
		CardSetMap.put(GameType.LuckySeven2, new CardSet(new Card[]{Cards.bridge, Cards.lurker, Cards.patrol, Cards.swindler, Cards.wishingWell, Cards.bank, Cards.expand, Cards.forge, Cards.kingsCourt, Cards.vault}));

		// Cornucopia & Base
		CardSetMap.put(GameType.BountyOfTheHunt, new CardSet(new Card[]{Cards.harvest, Cards.hornOfPlenty, Cards.huntingParty, Cards.menagerie, Cards.tournament, Cards.cellar, Cards.festival, Cards.militia, Cards.moneyLender, Cards.smithy}));
		CardSetMap.put(GameType.BadOmens, new CardSet(new Card[]{Cards.fortuneTeller, Cards.hamlet, Cards.hornOfPlenty, Cards.jester, Cards.remake, Cards.adventurer, Cards.bureaucrat, Cards.laboratory, Cards.spy, Cards.throneRoom}));
		CardSetMap.put(GameType.TheJestersWorkshop, new CardSet(new Card[]{Cards.fairgrounds, Cards.farmingVillage, Cards.horseTraders, Cards.jester, Cards.youngWitch, Cards.feast, Cards.laboratory, Cards.market, Cards.remodel, Cards.workshop}, Cards.chancellor));
		// Cornucopia & Base 2E
		CardSetMap.put(GameType.BountyOfTheHunt2, new CardSet(new Card[]{Cards.cellar, Cards.festival, Cards.militia, Cards.moneyLender, Cards.smithy, Cards.harvest, Cards.hornOfPlenty, Cards.huntingParty, Cards.menagerie, Cards.tournament}));
		CardSetMap.put(GameType.BadOmens2, new CardSet(new Card[]{Cards.bureaucrat, Cards.laboratory, Cards.merchant, Cards.poacher, Cards.throneRoom, Cards.fortuneTeller, Cards.hamlet, Cards.hornOfPlenty, Cards.jester, Cards.remake}));
		CardSetMap.put(GameType.TheJestersWorkshop2, new CardSet(new Card[]{Cards.artisan, Cards.laboratory, Cards.market, Cards.remodel, Cards.workshop, Cards.fairgrounds, Cards.farmingVillage, Cards.horseTraders, Cards.jester, Cards.youngWitch}, Cards.merchant));		
		// Cornucopia & Intrigue
		CardSetMap.put(GameType.LastLaughs, new CardSet(new Card[]{Cards.farmingVillage, Cards.harvest, Cards.horseTraders, Cards.huntingParty, Cards.jester, Cards.minion, Cards.nobles, Cards.pawn, Cards.steward, Cards.swindler}));
		CardSetMap.put(GameType.TheSpiceOfLife, new CardSet(new Card[]{Cards.fairgrounds, Cards.hornOfPlenty, Cards.remake, Cards.tournament, Cards.youngWitch, Cards.coppersmith, Cards.courtyard, Cards.greatHall, Cards.miningVillage, Cards.tribute }, Cards.wishingWell));
		CardSetMap.put(GameType.SmallVictories, new CardSet(new Card[]{Cards.fortuneTeller, Cards.hamlet, Cards.huntingParty, Cards.remake, Cards.tournament, Cards.conspirator, Cards.duke, Cards.greatHall, Cards.harem, Cards.pawn}));
		// Cornucopia & Intrigue 2E
		CardSetMap.put(GameType.LastLaughs2, new CardSet(new Card[]{Cards.minion, Cards.nobles, Cards.pawn, Cards.steward, Cards.swindler, Cards.farmingVillage, Cards.harvest, Cards.horseTraders, Cards.huntingParty, Cards.jester}));
		CardSetMap.put(GameType.TheSpiceOfLife2, new CardSet(new Card[]{Cards.courtier, Cards.courtyard, Cards.diplomat, Cards.miningVillage, Cards.replace, Cards.fairgrounds, Cards.hornOfPlenty, Cards.remake, Cards.tournament, Cards.youngWitch }, Cards.wishingWell));
		CardSetMap.put(GameType.SmallVictories2, new CardSet(new Card[]{Cards.conspirator, Cards.duke, Cards.harem, Cards.pawn, Cards.secretPassage, Cards.fortuneTeller, Cards.hamlet, Cards.huntingParty, Cards.remake, Cards.tournament}));
		// Hinterlands
		CardSetMap.put(GameType.HinterlandsIntro, new CardSet(new Card[]{Cards.cache, Cards.crossroads, Cards.develop, Cards.haggler, Cards.jackOfAllTrades, Cards.margrave, Cards.nomadCamp, Cards.oasis, Cards.spiceMerchant, Cards.stables}));
		CardSetMap.put(GameType.FairTrades, new CardSet(new Card[]{Cards.borderVillage, Cards.cartographer, Cards.develop, Cards.duchess, Cards.farmland, Cards.illGottenGains, Cards.nobleBrigand, Cards.silkRoad, Cards.stables, Cards.trader}));
		CardSetMap.put(GameType.Bargains, new CardSet(new Card[]{Cards.borderVillage, Cards.cache, Cards.duchess, Cards.foolsGold, Cards.haggler, Cards.highway, Cards.nomadCamp, Cards.scheme, Cards.spiceMerchant, Cards.trader}));
		CardSetMap.put(GameType.Gambits, new CardSet(new Card[]{Cards.cartographer, Cards.crossroads, Cards.embassy, Cards.inn, Cards.jackOfAllTrades, Cards.mandarin, Cards.nomadCamp, Cards.oasis, Cards.oracle, Cards.tunnel}));
		// Hinterlands and Base
		CardSetMap.put(GameType.HighwayRobbery, new CardSet(new Card[]{Cards.cellar, Cards.library, Cards.moneyLender, Cards.throneRoom, Cards.workshop, Cards.highway, Cards.inn, Cards.margrave, Cards.nobleBrigand, Cards.oasis}));
		CardSetMap.put(GameType.AdventuresAbroad, new CardSet(new Card[]{Cards.adventurer, Cards.chancellor, Cards.festival, Cards.laboratory, Cards.remodel, Cards.crossroads, Cards.farmland, Cards.foolsGold, Cards.oracle, Cards.spiceMerchant}));
		// Hinterlands and Base 2E
		CardSetMap.put(GameType.HighwayRobbery2, new CardSet(new Card[]{Cards.cellar, Cards.library, Cards.moneyLender, Cards.throneRoom, Cards.workshop, Cards.highway, Cards.inn, Cards.margrave, Cards.nobleBrigand, Cards.oasis}));
		CardSetMap.put(GameType.AdventuresAbroad2, new CardSet(new Card[]{Cards.festival, Cards.laboratory, Cards.moneyLender, Cards.throneRoom, Cards.workshop, Cards.crossroads, Cards.farmland, Cards.foolsGold, Cards.oracle, Cards.spiceMerchant}));
		// Hinterlands and Intrigue
		CardSetMap.put(GameType.MoneyForNothing, new CardSet(new Card[]{Cards.coppersmith, Cards.greatHall, Cards.pawn, Cards.shantyTown, Cards.torturer, Cards.cache, Cards.cartographer, Cards.jackOfAllTrades, Cards.silkRoad, Cards.tunnel}));
		CardSetMap.put(GameType.TheDukesBall, new CardSet(new Card[]{Cards.conspirator, Cards.duke, Cards.harem, Cards.masquerade, Cards.upgrade, Cards.duchess, Cards.haggler, Cards.inn, Cards.nobleBrigand, Cards.scheme}));
		// Hinterlands and Intrigue 2E
		CardSetMap.put(GameType.MoneyForNothing2, new CardSet(new Card[]{Cards.patrol, Cards.pawn, Cards.replace, Cards.shantyTown, Cards.torturer, Cards.cache, Cards.cartographer, Cards.jackOfAllTrades, Cards.silkRoad, Cards.tunnel}));
		CardSetMap.put(GameType.TheDukesBall2, new CardSet(new Card[]{Cards.conspirator, Cards.duke, Cards.harem, Cards.masquerade, Cards.upgrade, Cards.duchess, Cards.haggler, Cards.inn, Cards.nobleBrigand, Cards.scheme}));
		// Hinterlands and Seaside
		CardSetMap.put(GameType.Travelers, new CardSet(new Card[]{Cards.cutpurse, Cards.island, Cards.lookout, Cards.merchantShip, Cards.warehouse, Cards.cartographer, Cards.crossroads, Cards.farmland, Cards.silkRoad, Cards.stables}));
		CardSetMap.put(GameType.Diplomacy, new CardSet(new Card[]{Cards.ambassador, Cards.bazaar, Cards.caravan, Cards.embargo, Cards.smugglers, Cards.embassy, Cards.farmland, Cards.illGottenGains, Cards.nobleBrigand, Cards.trader}));
		// Hinterlands and Alchemy
		CardSetMap.put(GameType.SchemesAndDreams, new CardSet(new Card[]{Cards.apothecary, Cards.apprentice, Cards.herbalist, Cards.philosophersStone, Cards.transmute, Cards.duchess, Cards.foolsGold, Cards.illGottenGains, Cards.jackOfAllTrades, Cards.scheme}));
		CardSetMap.put(GameType.WineCountry, new CardSet(new Card[]{Cards.apprentice, Cards.familiar, Cards.golem, Cards.university, Cards.vineyard, Cards.crossroads, Cards.farmland, Cards.haggler, Cards.highway, Cards.nomadCamp}));
		// Hinterlands and Prosperity
		CardSetMap.put(GameType.InstantGratification, new CardSet(new Card[]{Cards.bishop, Cards.expand, Cards.hoard, Cards.mint, Cards.watchTower, Cards.farmland, Cards.haggler, Cards.illGottenGains, Cards.nobleBrigand, Cards.trader}));
		CardSetMap.put(GameType.TreasureTrove, new CardSet(new Card[]{Cards.bank, Cards.monument, Cards.royalSeal, Cards.tradeRoute, Cards.venture, Cards.cache, Cards.develop, Cards.foolsGold, Cards.illGottenGains, Cards.mandarin}));
		// Hinterlands and Cornucopia
		CardSetMap.put(GameType.BlueHarvest, new CardSet(new Card[]{Cards.hamlet, Cards.hornOfPlenty, Cards.horseTraders, Cards.jester, Cards.tournament, Cards.foolsGold, Cards.mandarin, Cards.nobleBrigand, Cards.trader, Cards.tunnel}));
		CardSetMap.put(GameType.TravelingCircus, new CardSet(new Card[]{Cards.fairgrounds, Cards.farmingVillage, Cards.huntingParty, Cards.jester, Cards.menagerie, Cards.borderVillage, Cards.embassy, Cards.foolsGold, Cards.nomadCamp, Cards.oasis}));
		
		// Dark Ages
		CardSetMap.put(GameType.PlayingChessWithDeath, new CardSet(new Card[]{Cards.banditCamp, Cards.graverobber, Cards.junkDealer, Cards.mystic, Cards.pillage, Cards.rats, Cards.sage, Cards.scavenger, Cards.storeroom, Cards.vagrant}));
		CardSetMap.put(GameType.GrimParade, new CardSet(new Card[]{Cards.armory, Cards.bandOfMisfits, Cards.catacombs, Cards.cultist, Cards.forager, Cards.fortress, Cards.virtualKnight, Cards.marketSquare, Cards.procession, Cards.huntingGrounds}));
		// Dark Ages and Base
		CardSetMap.put(GameType.HighAndLow, new CardSet(new Card[]{Cards.hermit, Cards.huntingGrounds, Cards.mystic, Cards.poorHouse, Cards.wanderingMinstrel, Cards.cellar, Cards.moneyLender, Cards.throneRoom, Cards.witch, Cards.workshop}));
		CardSetMap.put(GameType.ChivalryAndRevelry, new CardSet(new Card[]{Cards.altar, Cards.virtualKnight, Cards.rats, Cards.scavenger, Cards.squire, Cards.festival, Cards.gardens, Cards.laboratory, Cards.library, Cards.remodel}));
		// Dark Ages and Base 2E
		CardSetMap.put(GameType.HighAndLow2, new CardSet(new Card[]{Cards.cellar, Cards.moneyLender, Cards.throneRoom, Cards.witch, Cards.workshop, Cards.hermit, Cards.huntingGrounds, Cards.mystic, Cards.poorHouse, Cards.wanderingMinstrel}));
		CardSetMap.put(GameType.ChivalryAndRevelry2	, new CardSet(new Card[]{Cards.festival, Cards.gardens, Cards.laboratory, Cards.library, Cards.remodel, Cards.altar, Cards.virtualKnight, Cards.rats, Cards.scavenger, Cards.squire}));
		// Dark Ages and Intrigue
		CardSetMap.put(GameType.Prophecy, new CardSet(new Card[]{Cards.armory, Cards.ironmonger, Cards.mystic, Cards.rebuild, Cards.vagrant, Cards.baron, Cards.conspirator, Cards.greatHall, Cards.nobles, Cards.wishingWell}));
		CardSetMap.put(GameType.Invasion, new CardSet(new Card[]{Cards.beggar, Cards.marauder, Cards.rogue, Cards.squire, Cards.urchin, Cards.harem, Cards.miningVillage, Cards.swindler, Cards.torturer, Cards.upgrade}));
		// Dark Ages and Intrigue 2E
		CardSetMap.put(GameType.Prophecy2, new CardSet(new Card[]{Cards.baron, Cards.conspirator, Cards.nobles, Cards.secretPassage, Cards.wishingWell, Cards.armory, Cards.ironmonger, Cards.mystic, Cards.rebuild, Cards.vagrant}));
		CardSetMap.put(GameType.Invasion2, new CardSet(new Card[]{Cards.diplomat, Cards.harem, Cards.swindler, Cards.torturer, Cards.upgrade, Cards.beggar, Cards.marauder, Cards.rogue, Cards.squire, Cards.urchin}));
		// Dark Ages and Seaside
		CardSetMap.put(GameType.WateryGraves, new CardSet(new Card[]{Cards.count, Cards.graverobber, Cards.hermit, Cards.scavenger, Cards.urchin, Cards.nativeVillage, Cards.pirateShip, Cards.salvager, Cards.treasureMap, Cards.treasury}));
		CardSetMap.put(GameType.Peasants, new CardSet(new Card[]{Cards.deathCart, Cards.feodum, Cards.poorHouse, Cards.urchin, Cards.vagrant, Cards.fishingVillage, Cards.haven, Cards.island, Cards.lookout, Cards.warehouse}));
		// Dark Ages and Alchemy
		CardSetMap.put(GameType.Infestations, new CardSet(new Card[]{Cards.armory, Cards.cultist, Cards.feodum, Cards.marketSquare, Cards.rats, Cards.wanderingMinstrel, Cards.apprentice, Cards.scryingPool, Cards.transmute, Cards.vineyard}));
		CardSetMap.put(GameType.Lamentations, new CardSet(new Card[]{Cards.beggar, Cards.catacombs, Cards.counterfeit, Cards.forager, Cards.ironmonger, Cards.pillage, Cards.apothecary, Cards.golem, Cards.herbalist, Cards.university}));
		// Dark Ages and Prosperity
		CardSetMap.put(GameType.OneMansTrash, new CardSet(new Card[]{Cards.counterfeit, Cards.forager, Cards.graverobber, Cards.marketSquare, Cards.rogue, Cards.city, Cards.grandMarket, Cards.monument, Cards.talisman, Cards.venture}));
		CardSetMap.put(GameType.HonorAmongThieves, new CardSet(new Card[]{Cards.banditCamp, Cards.procession, Cards.rebuild, Cards.rogue, Cards.squire, Cards.forge, Cards.hoard, Cards.peddler, Cards.quarry, Cards.watchTower}));
		// Dark Ages and Cornucopia
		CardSetMap.put(GameType.DarkCarnival, new CardSet(new Card[]{Cards.bandOfMisfits, Cards.cultist, Cards.fortress, Cards.hermit, Cards.junkDealer, Cards.virtualKnight, Cards.fairgrounds, Cards.hamlet, Cards.hornOfPlenty, Cards.menagerie}));
		CardSetMap.put(GameType.ToTheVictor, new CardSet(new Card[]{Cards.banditCamp, Cards.counterfeit, Cards.deathCart, Cards.marauder, Cards.pillage, Cards.sage, Cards.harvest, Cards.huntingParty, Cards.remake, Cards.tournament}));
		// Dark Ages and Hinterlands
		CardSetMap.put(GameType.FarFromHome, new CardSet(new Card[]{Cards.beggar, Cards.count, Cards.feodum, Cards.marauder, Cards.wanderingMinstrel, Cards.cartographer, Cards.develop, Cards.embassy, Cards.foolsGold, Cards.haggler}));
		CardSetMap.put(GameType.Expeditions, new CardSet(new Card[]{Cards.altar, Cards.catacombs, Cards.ironmonger, Cards.poorHouse, Cards.storeroom, Cards.crossroads, Cards.farmland, Cards.highway, Cards.spiceMerchant, Cards.tunnel}));
		
		// Guilds and Base
		CardSetMap.put(GameType.ArtsAndCrafts, new CardSet(new Card[]{Cards.stonemason, Cards.advisor, Cards.baker, Cards.journeyman, Cards.merchantGuild, Cards.laboratory, Cards.cellar, Cards.workshop, Cards.festival, Cards.moneyLender}));
		CardSetMap.put(GameType.CleanLiving, new CardSet(new Card[]{Cards.butcher, Cards.baker, Cards.candlestickMaker, Cards.doctor, Cards.soothsayer, Cards.militia, Cards.thief, Cards.moneyLender, Cards.gardens, Cards.village}));
		CardSetMap.put(GameType.GildingTheLily, new CardSet(new Card[]{Cards.plaza, Cards.masterpiece, Cards.candlestickMaker, Cards.taxman, Cards.herald, Cards.library, Cards.remodel, Cards.adventurer, Cards.market, Cards.chancellor}));
		// Guilds and Base 2E
		CardSetMap.put(GameType.ArtsAndCrafts2, new CardSet(new Card[]{Cards.laboratory, Cards.cellar, Cards.workshop, Cards.festival, Cards.moneyLender, Cards.stonemason, Cards.advisor, Cards.baker, Cards.journeyman, Cards.merchantGuild}));
		CardSetMap.put(GameType.CleanLiving2, new CardSet(new Card[]{Cards.bandit, Cards.militia, Cards.moneyLender, Cards.gardens, Cards.village, Cards.butcher, Cards.baker, Cards.candlestickMaker, Cards.doctor, Cards.soothsayer}));
		CardSetMap.put(GameType.GildingTheLily2, new CardSet(new Card[]{Cards.library, Cards.merchant, Cards.remodel, Cards.market, Cards.sentry, Cards.plaza, Cards.masterpiece, Cards.candlestickMaker, Cards.taxman, Cards.herald}));
		// Guilds and Intrigue
		CardSetMap.put(GameType.NameThatCard, new CardSet(new Card[]{Cards.baker, Cards.doctor, Cards.plaza, Cards.advisor, Cards.masterpiece, Cards.courtyard, Cards.wishingWell, Cards.harem, Cards.tribute, Cards.nobles}));
		CardSetMap.put(GameType.TricksOfTheTrade, new CardSet(new Card[]{Cards.stonemason, Cards.herald, Cards.soothsayer, Cards.journeyman, Cards.butcher, Cards.greatHall, Cards.nobles, Cards.conspirator, Cards.masquerade, Cards.coppersmith}));
		CardSetMap.put(GameType.DecisionsDecisions, new CardSet(new Card[]{Cards.merchantGuild, Cards.candlestickMaker, Cards.masterpiece, Cards.taxman, Cards.butcher, Cards.bridge, Cards.pawn, Cards.miningVillage, Cards.upgrade, Cards.duke}));
		// Guilds and Intrigue 2E
		CardSetMap.put(GameType.NameThatCard2, new CardSet(new Card[]{Cards.courtyard, Cards.harem, Cards.nobles, Cards.replace, Cards.wishingWell, Cards.baker, Cards.doctor, Cards.plaza, Cards.advisor, Cards.masterpiece}));
		CardSetMap.put(GameType.TricksOfTheTrade2, new CardSet(new Card[]{Cards.conspirator, Cards.masquerade, Cards.mill, Cards.nobles, Cards.secretPassage, Cards.stonemason, Cards.herald, Cards.soothsayer, Cards.journeyman, Cards.butcher}));
		CardSetMap.put(GameType.DecisionsDecisions2, new CardSet(new Card[]{Cards.bridge, Cards.pawn, Cards.miningVillage, Cards.upgrade, Cards.duke, Cards.merchantGuild, Cards.candlestickMaker, Cards.masterpiece, Cards.taxman, Cards.butcher}));

		// Adventures
		CardSetMap.put(GameType.GentleIntro, new CardSet(new Card[] { Cards.amulet, Cards.distantLands, Cards.dungeon, Cards.duplicate, Cards.giant, Cards.hireling, Cards.port, Cards.ranger, Cards.ratcatcher, Cards.treasureTrove, Cards.scoutingParty}));
		CardSetMap.put(GameType.ExpertIntro, new CardSet(new Card[] { Cards.caravanGuard, Cards.coinOfTheRealm, Cards.hauntedWoods, Cards.lostCity, Cards.magpie, Cards.peasant, Cards.raze, Cards.swampHag, Cards.transmogrify, Cards.wineMerchant, Cards.mission, Cards.plan}));
		// Adventures and Base
		CardSetMap.put(GameType.LevelUp, new CardSet(new Card[] { Cards.dungeon, Cards.gear, Cards.guide, Cards.lostCity, Cards.miser, Cards.market, Cards.militia, Cards.spy, Cards.throneRoom, Cards.workshop, Cards.training}));
		CardSetMap.put(GameType.SonOfSizeDistortion, new CardSet(new Card[] { Cards.amulet, Cards.duplicate, Cards.giant, Cards.messenger, Cards.treasureTrove, Cards.bureaucrat, Cards.gardens, Cards.moneyLender, Cards.thief, Cards.witch, Cards.bonfire, Cards.raid}));
		// Adventures and Base 2E
		CardSetMap.put(GameType.LevelUp2, new CardSet(new Card[] { Cards.market, Cards.merchant, Cards.militia, Cards.throneRoom, Cards.workshop, Cards.dungeon, Cards.gear, Cards.guide, Cards.lostCity, Cards.miser, Cards.training}));
		CardSetMap.put(GameType.SonOfSizeDistortion2, new CardSet(new Card[] { Cards.bandit, Cards.bureaucrat, Cards.gardens, Cards.moneyLender, Cards.witch, Cards.amulet, Cards.duplicate, Cards.giant, Cards.messenger, Cards.treasureTrove, Cards.bonfire, Cards.raid}));
		// Adventures and Intrigue
		CardSetMap.put(GameType.RoyaltyFactory, new CardSet(new Card[] { Cards.bridgeTroll, Cards.duplicate, Cards.page, Cards.raze, Cards.royalCarriage, Cards.conspirator, Cards.harem, Cards.nobles, Cards.secretChamber, Cards.swindler, Cards.pilgrimage}));
		CardSetMap.put(GameType.MastersOfFinance, new CardSet(new Card[] { Cards.artificer, Cards.distantLands, Cards.gear, Cards.transmogrify, Cards.wineMerchant, Cards.bridge, Cards.pawn, Cards.shantyTown, Cards.steward, Cards.upgrade, Cards.ball, Cards.borrow}));
		// Adventures and Intrigue
		CardSetMap.put(GameType.RoyaltyFactory2, new CardSet(new Card[] { Cards.conspirator, Cards.courtier, Cards.harem, Cards.nobles, Cards.swindler, Cards.bridgeTroll, Cards.duplicate, Cards.page, Cards.raze, Cards.royalCarriage, Cards.pilgrimage}));
		CardSetMap.put(GameType.MastersOfFinance2, new CardSet(new Card[] { Cards.bridge, Cards.pawn, Cards.shantyTown, Cards.steward, Cards.upgrade, Cards.artificer, Cards.distantLands, Cards.gear, Cards.transmogrify, Cards.wineMerchant, Cards.ball, Cards.borrow}));
		// Adventures and Seaside
		CardSetMap.put(GameType.PrinceOfOrange, new CardSet(new Card[] { Cards.amulet, Cards.dungeon, Cards.hauntedWoods, Cards.page, Cards.swampHag, Cards.caravan, Cards.fishingVillage, Cards.merchantShip, Cards.tactician, Cards.treasureMap, Cards.mission}));
		CardSetMap.put(GameType.GiftsAndMathoms, new CardSet(new Card[] { Cards.bridgeTroll, Cards.caravanGuard, Cards.hireling, Cards.lostCity, Cards.messenger, Cards.ambassador, Cards.embargo, Cards.haven, Cards.salvager, Cards.smugglers, Cards.expedition, Cards.quest}));
		// Adventures and Alchemy
		CardSetMap.put(GameType.HastePotion, new CardSet(new Card[] { Cards.magpie, Cards.messenger, Cards.port, Cards.royalCarriage, Cards.treasureTrove, Cards.apprentice, Cards.scryingPool, Cards.transmute, Cards.university, Cards.vineyard, Cards.potion, Cards.plan}));
		CardSetMap.put(GameType.Cursecatchers, new CardSet(new Card[] { Cards.amulet, Cards.bridgeTroll, Cards.caravanGuard, Cards.peasant, Cards.ratcatcher, Cards.apothecary, Cards.familiar, Cards.golem, Cards.herbalist, Cards.philosophersStone, Cards.potion, Cards.save, Cards.trade}));
		// Adventures and Prosperity
		CardSetMap.put(GameType.LastWillAndMonument, new CardSet(new Card[] { Cards.coinOfTheRealm, Cards.dungeon, Cards.messenger, Cards.relic, Cards.treasureTrove, Cards.bishop, Cards.countingHouse, Cards.monument, Cards.rabble, Cards.vault, Cards.inheritance}));
		CardSetMap.put(GameType.ThinkBig, new CardSet(new Card[] { Cards.distantLands, Cards.giant, Cards.hireling, Cards.miser, Cards.storyteller, Cards.contraband, Cards.expand, Cards.hoard, Cards.kingsCourt, Cards.peddler, Cards.ball, Cards.ferry}));
		// Adventures and Cornucopia
		CardSetMap.put(GameType.TheHerosReturn, new CardSet(new Card[] { Cards.artificer, Cards.miser, Cards.page, Cards.ranger, Cards.relic, Cards.fairgrounds, Cards.farmingVillage, Cards.horseTraders, Cards.jester, Cards.menagerie}));
		CardSetMap.put(GameType.SeacraftAndWitchcraft, new CardSet(new Card[] { Cards.peasant, Cards.storyteller, Cards.swampHag, Cards.transmogrify, Cards.wineMerchant, Cards.fortuneTeller, Cards.hamlet, Cards.hornOfPlenty, Cards.tournament, Cards.youngWitch, Cards.ferry, Cards.seaway}, Cards.guide));
		// Adventures and Hinterlands
		CardSetMap.put(GameType.TradersAndRaiders, new CardSet(new Card[] { Cards.hauntedWoods, Cards.lostCity, Cards.page, Cards.port, Cards.wineMerchant, Cards.develop, Cards.farmland, Cards.haggler, Cards.spiceMerchant, Cards.trader, Cards.raid}));
		CardSetMap.put(GameType.Journeys, new CardSet(new Card[] { Cards.bridgeTroll, Cards.distantLands, Cards.giant, Cards.guide, Cards.ranger, Cards.cartographer, Cards.crossroads, Cards.highway, Cards.inn, Cards.silkRoad, Cards.expedition, Cards.inheritance}));
		// Adventures and Dark Ages
		CardSetMap.put(GameType.CemeteryPolka, new CardSet(new Card[] { Cards.amulet, Cards.caravanGuard, Cards.hireling, Cards.peasant, Cards.relic, Cards.graverobber, Cards.marauder, Cards.procession, Cards.rogue, Cards.wanderingMinstrel, Cards.alms}));
		CardSetMap.put(GameType.GroovyDecay, new CardSet(new Card[] { Cards.dungeon, Cards.hauntedWoods, Cards.ratcatcher, Cards.raze, Cards.transmogrify, Cards.cultist, Cards.deathCart, Cards.fortress, Cards.rats, Cards.lostArts, Cards.pathfinding}));
		// Adventures and Guilds
		CardSetMap.put(GameType.Spendthrift, new CardSet(new Card[] { Cards.artificer, Cards.gear, Cards.magpie, Cards.miser, Cards.storyteller, Cards.doctor, Cards.masterpiece, Cards.merchantGuild, Cards.soothsayer, Cards.stonemason, Cards.lostArts}));
		CardSetMap.put(GameType.QueenOfTan, new CardSet(new Card[] { Cards.coinOfTheRealm, Cards.duplicate, Cards.guide, Cards.ratcatcher, Cards.royalCarriage, Cards.advisor, Cards.butcher, Cards.candlestickMaker, Cards.herald, Cards.journeyman, Cards.pathfinding, Cards.save}));

		// Empires
		CardSetMap.put(GameType.BasicIntro, new CardSet(new Card[] { Cards.virtualCastle, Cards.chariotRace, Cards.cityQuarter, Cards.engineer, Cards.farmersMarket, Cards.forum, Cards.legionary, Cards.virtualPatricianEmporium, Cards.sacrifice, Cards.villa, Cards.wedding, Cards.tower}));
		CardSetMap.put(GameType.AdvancedIntro, new CardSet(new Card[] { Cards.archive, Cards.capital, Cards.virtualCatapultRocks, Cards.crown, Cards.enchantress, Cards.virtualGladiatorFortune, Cards.groundskeeper, Cards.royalBlacksmith, Cards.virtualSettlersBustlingVillage, Cards.temple, Cards.arena, Cards.triumphalArch}));
		// Empires and Base
		CardSetMap.put(GameType.EverythingInModeration, new CardSet(new Card[] { Cards.enchantress, Cards.forum, Cards.legionary, Cards.overlord, Cards.temple, Cards.cellar, Cards.library, Cards.remodel, Cards.village, Cards.workshop, Cards.orchard, Cards.windfall}));
		CardSetMap.put(GameType.SilverBullets, new CardSet(new Card[] { Cards.virtualCatapultRocks, Cards.charm, Cards.farmersMarket, Cards.groundskeeper, Cards.virtualPatricianEmporium, Cards.bureaucrat, Cards.gardens, Cards.laboratory, Cards.market, Cards.moneyLender, Cards.aqueduct, Cards.conquest}));
		// Empires and Base 2E
		CardSetMap.put(GameType.EverythingInModeration2, new CardSet(new Card[] { Cards.enchantress, Cards.forum, Cards.legionary, Cards.overlord, Cards.temple, Cards.cellar, Cards.library, Cards.remodel, Cards.village, Cards.workshop, Cards.orchard, Cards.windfall}));
		CardSetMap.put(GameType.SilverBullets2, new CardSet(new Card[] { Cards.virtualCatapultRocks, Cards.charm, Cards.farmersMarket, Cards.groundskeeper, Cards.virtualPatricianEmporium, Cards.bureaucrat, Cards.gardens, Cards.laboratory, Cards.market, Cards.moneyLender, Cards.aqueduct, Cards.conquest}));
		// Empires and Intrigue
		CardSetMap.put(GameType.DeliciousTorture, new CardSet(new Card[] { Cards.virtualCastle, Cards.crown, Cards.enchantress, Cards.sacrifice, Cards.virtualSettlersBustlingVillage, Cards.baron, Cards.bridge, Cards.harem, Cards.ironworks, Cards.torturer, Cards.arena, Cards.banquet}));
		CardSetMap.put(GameType.BuddySystem, new CardSet(new Card[] { Cards.archive, Cards.capital, Cards.virtualCatapultRocks, Cards.engineer, Cards.forum, Cards.masquerade, Cards.miningVillage, Cards.nobles, Cards.pawn, Cards.tradingPost, Cards.saltTheEarth, Cards.wolfDen}));
		// Empires and Intrigue
		CardSetMap.put(GameType.DeliciousTorture2, new CardSet(new Card[] { Cards.virtualCastle, Cards.crown, Cards.enchantress, Cards.sacrifice, Cards.virtualSettlersBustlingVillage, Cards.baron, Cards.bridge, Cards.harem, Cards.ironworks, Cards.torturer, Cards.arena, Cards.banquet}));
		CardSetMap.put(GameType.BuddySystem2, new CardSet(new Card[] { Cards.archive, Cards.capital, Cards.virtualCatapultRocks, Cards.engineer, Cards.forum, Cards.masquerade, Cards.miningVillage, Cards.nobles, Cards.pawn, Cards.tradingPost, Cards.saltTheEarth, Cards.wolfDen}));
		// Empires and Seaside
		CardSetMap.put(GameType.BoxedIn, new CardSet(new Card[] { Cards.virtualCastle, Cards.chariotRace, Cards.virtualEncampmentPlunder, Cards.enchantress, Cards.virtualGladiatorFortune, Cards.salvager, Cards.smugglers, Cards.tactician, Cards.warehouse, Cards.wharf, Cards.wall, Cards.tax}));
		CardSetMap.put(GameType.KingOfTheSea, new CardSet(new Card[] { Cards.archive, Cards.farmersMarket, Cards.overlord, Cards.temple, Cards.wildHunt, Cards.explorer, Cards.haven, Cards.nativeVillage, Cards.pirateShip, Cards.seaHag, Cards.delve, Cards.fountain}));
		// Empires and Alchemy
		CardSetMap.put(GameType.Collectors, new CardSet(new Card[] { Cards.cityQuarter, Cards.crown, Cards.virtualEncampmentPlunder, Cards.enchantress, Cards.farmersMarket, Cards.apothecary, Cards.apprentice, Cards.herbalist, Cards.transmute, Cards.university, Cards.colonnade, Cards.museum}));
		// Empires and Prosperity
		CardSetMap.put(GameType.BigTime, new CardSet(new Card[] { Cards.capital, Cards.virtualGladiatorFortune, Cards.virtualPatricianEmporium, Cards.royalBlacksmith, Cards.villa, Cards.bank, Cards.forge, Cards.grandMarket, Cards.loan, Cards.royalSeal, Cards.dominate, Cards.obelisk}, null,UseOptionalCards.Use, UseOptionalCards.DontUse));
		CardSetMap.put(GameType.GildedGates, new CardSet(new Card[] { Cards.chariotRace, Cards.cityQuarter, Cards.virtualEncampmentPlunder, Cards.groundskeeper, Cards.wildHunt, Cards.bishop, Cards.monument, Cards.mint, Cards.peddler, Cards.talisman, Cards.basilica, Cards.palace}, null,UseOptionalCards.Use, UseOptionalCards.DontUse));
		// Empires and Cornucopia
		CardSetMap.put(GameType.Zookeepers, new CardSet(new Card[] { Cards.overlord, Cards.sacrifice, Cards.virtualSettlersBustlingVillage, Cards.villa, Cards.wildHunt, Cards.fairgrounds, Cards.horseTraders, Cards.menagerie, Cards.jester, Cards.tournament, Cards.annex, Cards.colonnade}));
		CardSetMap.put(GameType.CashFlow, new CardSet(new Card[] { Cards.virtualCastle, Cards.cityQuarter, Cards.engineer, Cards.virtualGladiatorFortune, Cards.royalBlacksmith, Cards.baker, Cards.butcher, Cards.doctor, Cards.herald, Cards.soothsayer, Cards.baths, Cards.mountainPass}));
		// Empires and Hinterlands
		CardSetMap.put(GameType.SimplePlans, new CardSet(new Card[] { Cards.virtualCatapultRocks, Cards.forum, Cards.virtualPatricianEmporium, Cards.temple, Cards.villa, Cards.borderVillage, Cards.develop, Cards.haggler, Cards.illGottenGains, Cards.stables, Cards.donate, Cards.labyrinth}));
		CardSetMap.put(GameType.HinterExpansion, new CardSet(new Card[] { Cards.virtualCastle, Cards.charm, Cards.virtualEncampmentPlunder, Cards.engineer, Cards.legionary, Cards.cache, Cards.farmland, Cards.highway, Cards.spiceMerchant, Cards.tunnel, Cards.battlefield, Cards.fountain}));
		// Empires and Dark Ages
		CardSetMap.put(GameType.TombOfTheRatKing, new CardSet(new Card[] { Cards.virtualCastle, Cards.chariotRace, Cards.cityQuarter, Cards.legionary, Cards.sacrifice, Cards.deathCart, Cards.fortress, Cards.pillage, Cards.rats, Cards.storeroom, Cards.advance, Cards.tomb}, null, UseOptionalCards.DontUse, UseOptionalCards.Use));
		CardSetMap.put(GameType.TriumphOfTheBanditKing, new CardSet(new Card[] { Cards.capital, Cards.charm, Cards.engineer, Cards.groundskeeper, Cards.legionary, Cards.banditCamp, Cards.catacombs, Cards.huntingGrounds, Cards.marketSquare, Cards.procession, Cards.defiledShrine, Cards.triumph}, null, UseOptionalCards.DontUse, UseOptionalCards.Use));
		CardSetMap.put(GameType.TheSquiresRitual, new CardSet(new Card[] { Cards.archive, Cards.virtualCatapultRocks, Cards.crown, Cards.virtualPatricianEmporium, Cards.virtualSettlersBustlingVillage, Cards.feodum, Cards.hermit, Cards.ironmonger, Cards.rogue, Cards.squire, Cards.museum, Cards.ritual}, null, UseOptionalCards.DontUse, UseOptionalCards.Use));
		// Empires and Adventures
		CardSetMap.put(GameType.AreaControl, new CardSet(new Card[] { Cards.capital, Cards.virtualCatapultRocks, Cards.charm, Cards.crown, Cards.farmersMarket, Cards.coinOfTheRealm, Cards.page, Cards.relic, Cards.treasureTrove, Cards.wineMerchant, Cards.banquet, Cards.keep}));
		CardSetMap.put(GameType.NoMoneyNoProblems, new CardSet(new Card[] { Cards.archive, Cards.virtualEncampmentPlunder, Cards.royalBlacksmith, Cards.temple, Cards.villa, Cards.dungeon, Cards.duplicate, Cards.hireling, Cards.peasant, Cards.transmogrify, Cards.banditFort, Cards.mission}));

		CardSetMap.put(GameType.Test, new CardSet(new Card[] { Cards.throneRoom, Cards.squire, Cards.gear, Cards.bandOfMisfits, Cards.overlord, Cards.ferry, Cards.inheritance, Cards.donate, Cards.bonfire, Cards.militia, Cards.margrave, Cards.page, Cards.peasant, Cards.virtualCastle, Cards.urchin, Cards.raze, Cards.miningVillage, Cards.amulet, Cards.wharf, Cards.highway}));
	}
}
