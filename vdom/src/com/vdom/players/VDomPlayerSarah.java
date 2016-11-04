package com.vdom.players;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.vdom.api.Card;
import com.vdom.api.GameType;
import com.vdom.core.BasePlayer;
import com.vdom.core.Cards;
import com.vdom.core.Expansion;
import com.vdom.core.Game;
import com.vdom.core.GetCardsInGameOptions;
import com.vdom.core.MoveContext;
import com.vdom.core.Type;

public class VDomPlayerSarah extends BasePlayer {
    protected Random rand = new Random(System.currentTimeMillis());
    protected static final int earlyGame = 5;
    protected static final int MAX_OF_ONE_ACTION_CARD = 4;
    protected int earlyCardBuyCount;

    
    protected boolean alwaysBuyProvince;
    protected boolean buyEstates;
    protected boolean favorSilverGoldPlat;
    protected int silverMax;
    protected Card[] valuedCards;
    protected int actionCardMax;
    protected Card[] earlyCardBuys;
    protected int earlyCardBuyMax;
    protected boolean onlyBuyEarlySingle;
    protected int throneRoomsAndKingsCourtsMax = 2;
    protected boolean improvise = false;
    protected Card[] trashCards;
    
    @Override
    public boolean isAi() {
        return true;
    }

    public void setupGameVariables(GameType gameType, Card[] cardsInPlay) {
        //trash in this order!
        trashCards = new Card[] { Cards.curse, Cards.rats, Cards.overgrownEstate, Cards.ruinedVillage, Cards.ruinedMarket, Cards.survivors, Cards.ruinedLibrary, Cards.abandonedMine, Cards.virtualRuins, Cards.hovel, Cards.estate, Cards.copper, Cards.masterpiece };
        valuedCards = new Card[] { Cards.torturer, Cards.bazaar, Cards.masquerade, Cards.ghostShip, Cards.wharf, Cards.smithy, Cards.harem, Cards.adventurer,
            Cards.shantyTown, Cards.festival, Cards.moneyLender, Cards.venture, Cards.tournament, Cards.miningVillage, Cards.mint, Cards.farmingVillage,
            Cards.kingsCourt, Cards.jester, Cards.youngWitch, Cards.goons, Cards.monument, Cards.bishop, Cards.hamlet, Cards.fortuneTeller, Cards.watchTower,
            Cards.familiar, Cards.duke, Cards.platinum, Cards.gold, Cards.silver };

        improvise = false;
        midGame = 12;
        actionCardMax = 12;

        alwaysBuyProvince = true;
        
//        buyEstates = true;
        buyEstates = false;
        
        favorSilverGoldPlat = true; 

        onlyBuyEarlySingle = false;
        earlyCardBuys = new Card[] { Cards.militia, Cards.seaHag, Cards.familiar, Cards.youngWitch, Cards.thief, Cards.pirateShip, Cards.rabble, Cards.goons,
                Cards.fortuneTeller, Cards.jester };
        earlyCardBuyCount = 1;

        silverMax = 4;

        if (gameType == GameType.BigMoney || gameType == GameType.SizeDistortion || gameType == GameType.VictoryDance || gameType == GameType.SecretSchemes
                || gameType == GameType.BestWishes || gameType == GameType.RandomIntrigue || gameType == GameType.Underlings || gameType == GameType.HighSeas
                || gameType == GameType.BuriedTreasure || gameType == GameType.GiveAndTake) {
            alwaysBuyProvince = false;
        }

        if (gameType == GameType.Underlings || gameType == GameType.Repetition) {
            buyEstates = false;
        }

        if (gameType == GameType.FirstGame || gameType == GameType.RandomBaseGame || gameType == GameType.VictoryDance || gameType == GameType.HandMadness
                || gameType == GameType.Underlings) {
            silverMax = 5;
        }

        int numCornucopia = 0;
        for (final Card card : cardsInPlay) {
            if (card.getExpansion() == Expansion.Cornucopia && !card.is(Type.Prize, null)) {
                numCornucopia++;
            }
        }

        if (gameType == GameType.BigMoney || gameType == GameType.VillageSquare || gameType == GameType.SecretSchemes || numCornucopia >= 5) {
            favorSilverGoldPlat = false;
        }

        if (gameType == GameType.FirstGame) {
            midGame = 12;

            valuedCards = new Card[] { Cards.cellar, Cards.militia, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.BigMoney) {
            midGame = 20;
            earlyCardBuyMax = 2;
            valuedCards = new Card[] { Cards.feast, Cards.laboratory, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.Interaction) {
            midGame = 12;
            earlyCardBuyMax = 2;
        } else if (gameType == GameType.SizeDistortion) {
            midGame = 14;
            valuedCards = new Card[] { Cards.feast, Cards.chapel, Cards.laboratory, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.VillageSquare) {
            midGame = 12;
            actionCardMax = 7;
            valuedCards = new Card[] { Cards.smithy, Cards.festival, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.Repetition) {
            earlyCardBuyMax = 2;
        } else if (gameType == GameType.GiveAndTake) {
            actionCardMax = 10;
            valuedCards = new Card[] { Cards.fishingVillage, Cards.library, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.Shipwrecks) {
            midGame = 18;
            valuedCards = new Card[] { Cards.treasury, Cards.pearlDiver, Cards.ghostShip, Cards.seaHag, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.RandomSeaside) {
            midGame = 16;
            earlyCardBuyMax = 1;
            actionCardMax = 16;
            valuedCards = new Card[] { Cards.warehouse, Cards.wharf, Cards.bazaar, Cards.pearlDiver, Cards.ghostShip, Cards.platinum, Cards.gold,
                    Cards.silver };
        } else if (gameType == GameType.ReachForTomorrow) {
            earlyCardBuys = new Card[] { Cards.seaHag };
            earlyCardBuyMax = 1;
            midGame = 14;
            actionCardMax = 8;
            valuedCards = new Card[] { Cards.treasureMap, Cards.ghostShip, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.BuriedTreasure) {
            midGame = 14;
            earlyCardBuys = new Card[] { Cards.treasureMap };
            // onlyBuyEarlySingle = true;
            earlyCardBuyMax = 2;
            actionCardMax = 12;

            valuedCards = new Card[] { Cards.warehouse, Cards.treasureMap, Cards.wharf, Cards.pearlDiver, Cards.fishingVillage, Cards.platinum,
                    Cards.gold, Cards.silver };
        } else if (gameType == GameType.Beginners) {
            // bank
            // countingHouse
            // expand
            // goons
            // monument
            // rabble
            // royalSeal
            // venture
            // watchTower
            // workersVillage
            improvise = true;
        } else if (gameType == GameType.FriendlyInteractive) {
            // bishop
            // city
            // contraband
            // forge
            // hoard
            // peddler
            // royalSeal
            // tradeRoute
            // vault
            // workersVillage
            improvise = true;
        } else if (gameType == GameType.BigActions) {
            // city
            // expand
            // grandMarket
            // kingsCourt
            // loan
            // mint
            // quarry
            // rabble
            // talisman
            // vault
            improvise = true;
        } else if (gameType == GameType.BiggestMoney) {
            // bank
            // grandMarket
            // mint
            // royalSeal
            // venture
            // adventurer
            // laboratory
            // mine
            // moneyLender
            // spy
            earlyCardBuys = new Card[] { Cards.moneyLender };
            earlyCardBuyMax = 1;
            midGame = 14;
            actionCardMax = 5;
            valuedCards = new Card[] { Cards.laboratory, Cards.adventurer, Cards.venture, Cards.royalSeal, Cards.mint, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.TheKingsArmy) {
            // expand
            // goons
            // kingsCourt
            // rabble
            // vault
            // bureaucrat
            // councilRoom
            // moat
            // spy
            // village
            improvise = true;
        } else if (gameType == GameType.TheGoodLife) {
            // contraband
            // countingHouse
            // hoard
            // monument
            // mountebank
            // bureaucrat
            // cellar
            // chancellor
            // gardens
            // village
            earlyCardBuys = new Card[] { Cards.mountebank };
            earlyCardBuyMax = 1;
            midGame = 15;
            actionCardMax = 6;
            valuedCards = new Card[] { Cards.countingHouse, Cards.contraband, Cards.bureaucrat, Cards.chancellor, Cards.mountebank, Cards.platinum, Cards.gold, Cards.silver, Cards.copper };
            trashCards = new Card[] { Cards.curse, Cards.estate };
        } else if (gameType == GameType.PathsToVictory) {
            // bishop
            // countingHouse
            // goons
            // monument
            // peddler
            // baron
            // harem
            // pawn
            // shantyTown
            // upgrade
            improvise = true;
        } else if (gameType == GameType.AllAlongTheWatchtower) {
            // hoard
            // talisman
            // tradeRoute
            // vault
            // watchTower
            // bridge
            // greatHall
            // miningVillage
            // pawn
            // torturer
            earlyCardBuys = new Card[] { Cards.torturer };
            earlyCardBuyMax = 1;
            midGame = 14;
            actionCardMax = 7;
            valuedCards = new Card[] { Cards.watchTower, Cards.miningVillage, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.LuckySeven) {
            // bank
            // expand
            // forge
            // kingsCourt
            // vault
            // bridge
            // coppersmith
            // swindler
            // tribute
            // wishingWell
            improvise = true;
        } else if (gameType == GameType.BountyOfTheHunt) {
            // harvest
            // hornOfPlenty
            // huntingParty
            // menagerie
            // tournament
            // cellar
            // festival
            // militia
            // moneyLender
            // smithy
            earlyCardBuys = new Card[] { Cards.militia };
            earlyCardBuyMax = 1;
            midGame = 14;
            actionCardMax = 5;
            valuedCards = new Card[] { Cards.festival, Cards.moneyLender, Cards.menagerie, Cards.harvest, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.BadOmens) {
            // fortuneTeller
            // hamlet
            // hornOfPlenty
            // jester
            // remake
            // adventurer
            // bureaucrat
            // laboratory
            // spy
            // throneRoom
            midGame = 15;
            actionCardMax = 7;
            valuedCards = new Card[] { Cards.jester, Cards.throneRoom, Cards.fortuneTeller, Cards.laboratory, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.TheJestersWorkshop) {
            // fairgrounds
            // farmingVillage
            // horseTraders
            // jester
            // youngWitch
            // feast
            // laboratory
            // market
            // remodel
            // workshop
            // addPile(banePile = new Pile(Cards.chancellor,
            // kingdomCardPileSize)
            earlyCardBuys = new Card[] { Cards.youngWitch };
            earlyCardBuyMax = 1;
            midGame = 14;
            actionCardMax = 5;
            valuedCards = new Card[] { Cards.jester, Cards.chancellor, Cards.farmingVillage, Cards.market, Cards.baker, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.LastLaughs) {
            // farmingVillage
            // harvest
            // horseTraders
            // huntingParty
            // jester
            // minion
            // nobles
            // pawn
            // steward
            // swindler
            improvise = true;
        } else if (gameType == GameType.TheSpiceOfLife) {
            // fairgrounds
            // hornOfPlenty
            // remake
            // tournament
            // youngWitch
            // coppersmith
            // courtyard
            // greatHall
            // miningVillage
            // tribute
            // addPile(banePile = new Pile(Cards.wishingWell,
            // kingdomCardPileSize)
            earlyCardBuys = new Card[] { Cards.youngWitch, Cards.wishingWell };
            earlyCardBuyMax = 2;
            midGame = 15;
            actionCardMax = 5;
            valuedCards = new Card[] { Cards.jester, Cards.wishingWell, Cards.miningVillage, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.SmallVictories) {
            // fortuneTeller
            // hamlet
            // huntingParty
            // remake
            // tournament
            // conspirator
            // duke
            // greatHall
            // harem
            // pawn
            midGame = 15;
            actionCardMax = 10;
            valuedCards = new Card[] { Cards.fortuneTeller, Cards.greatHall, Cards.harem, Cards.duke, Cards.pawn, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.RandomCornucopia) {
            midGame = 15;
            actionCardMax = 6;
            valuedCards = new Card[] { Cards.tournament, Cards.jester, Cards.youngWitch, Cards.hamlet, Cards.fortuneTeller, Cards.platinum, Cards.gold, Cards.silver };
        } else {
            improvise = true;
            actionCardMax = 5;
        }
    }

    @Override
    public String getPlayerName() {
        return getPlayerName(Game.maskPlayerNames);
    }
    
    @Override
    public String getPlayerName(boolean maskName) {
        return maskName ? "Player " + (playerNumber + 1) : "Sarah";
    }

    @Override
    public Card doAction(MoveContext context) {
        ArrayList<Card> actionCards = context.getPlayer().getActionsInHand(context.player);

        // don't play rats
        if (game.cardInGame(Cards.rats)) {
            for (Iterator<Card> it = actionCards.iterator(); it.hasNext(); ) {
                if (Cards.rats.equals(it.next())) {
                    it.remove();
                }
            }
        }
        
        // Pair of Treasure Maps goes first
        int treasureMapCount = 0;
        for (Iterator<Card> it = actionCards.iterator(); it.hasNext(); ) {
            if (Cards.treasureMap.equals(it.next())) {
                treasureMapCount++;
                it.remove();
            }
        }
        if (treasureMapCount >= 2) {
            return context.player.fromHand(Cards.treasureMap);
        }

        // play prince if action card candidate available
        Card[] princeCards;
        if (actionCards.contains(Cards.prince)) {
            ArrayList<Card> cardList = new ArrayList<Card>();
            for (Card c : actionCards) {
                cardList.add(c);
            }
            princeCards = prince_cardCandidates(context, cardList, false);
        }
        else {
            princeCards = new Card[0];
        }
                
        // don't play trashForced cards if no trash cards available (Apprentice, Ambassador, etc)
        Card[] trashableCards = pickOutCards(context.getPlayer().getHand(), 1, getTrashCards());
        if (trashableCards == null) {
            for (Iterator<Card> it = actionCards.iterator(); it.hasNext(); ) {
                if (it.next().trashForced())
                    it.remove();
            }
        }        

        // play Action Cards that add more actions
        for (final Card card : actionCards) {
            if (context.canPlay(card)) {
                Card action = card;
                if (action.getAddActions() > 0 && !isInCardArray(card, princeCards)) 
                    return action;
            }
        }

        if (princeCards.length != 0) {
            return context.player.fromHand(Cards.prince);
        }
            
        if (context.player.inHand(Cards.throneRoom) && context.canPlay(Cards.throneRoom)) {
            return context.player.fromHand(Cards.throneRoom);
        }
        
        if (context.player.inHand(Cards.disciple) && context.canPlay(Cards.disciple)) {
            return context.player.fromHand(Cards.disciple);
        }
        
        //TODO: ...
        //if(context.getKingsCourtsInEffect() == 0) {
        if (context.player.inHand(Cards.kingsCourt) && context.canPlay(Cards.kingsCourt))
            return context.player.fromHand(Cards.kingsCourt);
        //}
        
        //TODO: simple action play order list instead of just picking the most expensive card
        int cost = COST_MAX;
        final ArrayList<Card> randList = new ArrayList<Card>();
        while (cost >= 0) {
            for (final Card card : actionCards) {
                if (   !context.canPlay(card) 
                    || card.equals(Cards.treasureMap)
                    || card.getCost(context) != cost
                    || card.equals(Cards.tactician) && context.countCardsInPlay(Cards.tactician) > 0
                   ) {
                    continue;
                }
                
                if(card.getCost(context) == cost) {
                    randList.add(card);
                }
            }

            if (randList.size() > 0) {
                return randList.get(rand.nextInt(randList.size()));
            }

            cost--;
        }

        return null;
    }
    
  
    
    public boolean shouldPassOnBuy(MoveContext context, Card card) {
        return 
                !context.canBuy(card) || 
                card.is(Type.Action, context.player) && actionCardCount >= actionCardMax || 
                !favorSilverGoldPlat && (card.equals(Cards.silver) || card.equals(Cards.masterpiece) || card.equals(Cards.gold) || card.equals(Cards.platinum)) ||
                card.equals(Cards.curse) || 
                card.equals(Cards.virtualRuins) ||
                card.equals(Cards.copper) || 
                card.equals(Cards.rats) || 
                card.equals(Cards.potion) && !shouldBuyPotion() ||
                card.equals(Cards.throneRoom) && throneRoomAndKingsCourtCount >= throneRoomsAndKingsCourtsMax ||
                card.equals(Cards.disciple) && throneRoomAndKingsCourtCount >= throneRoomsAndKingsCourtsMax ||
                card.equals(Cards.kingsCourt) && throneRoomAndKingsCourtCount >= throneRoomsAndKingsCourtsMax ||
                context.getEmbargosIfCursesLeft(card) > 0 ||
                !(card.is(Type.Action, context.player)) && !(card.is(Type.Treasure, null)) && !(card.is(Type.Event, null));
    }

    @Override
    public Card doBuy(MoveContext context) {
        final int coinAvailableForBuy = context.getCoinAvailableForBuy();

        if (earlyCardBuyCount < earlyCardBuyMax) {
            final ArrayList<Card> randList = new ArrayList<Card>();
            for (final Card card : earlyCardBuys) {
                if (context.canBuy(card)) { // && (coinAvailableForBuy == card.getCost()) {
                    randList.add(card);
                }
            }

            if (randList.size() > 0) {
                earlyCardBuyCount++;
                return randList.get(rand.nextInt(randList.size()));
            }
        }

        if (onlyBuyEarlySingle && earlyCardBuyCount < earlyCardBuyMax) {
            return null;
        }

        if (context.canBuy(Cards.colony)) {
            return Cards.colony;
        }
        
        if (favorSilverGoldPlat && coinAvailableForBuy >= 9 && context.canBuy(Cards.platinum) && turnCount < midGame) {
            return Cards.platinum;
        }

        if(context.canBuy(Cards.prince) && turnCount < midGame && context.cardInGame(Cards.colony) && getMyCardCount(Cards.prince) < 2) {
            ArrayList<Card> allCards = new ArrayList<Card>(getAllCards());
            if (prince_cardCandidates(context, allCards, false).length >= 2 + 2*getMyCardCount(Cards.prince))
                return Cards.prince;
        }
        
        if ((alwaysBuyProvince || turnCount > earlyGame) && context.canBuy(Cards.province)) {
            return Cards.province;
        }
        
        if (turnCount > midGame && coinAvailableForBuy <= 7 && rand.nextInt(3) == 0) {
            if (context.canBuy(Cards.vineyard) && actionCardCount >=9 && context.getEmbargosIfCursesLeft(Cards.vineyard) == 0) {
                return Cards.vineyard;
            }
            if(context.canBuy(Cards.duchy) && context.getEmbargosIfCursesLeft(Cards.duchy) == 0) {
                return Cards.duchy;
            }
            if (context.canBuy(Cards.vineyard) && actionCardCount >=6 && context.getEmbargosIfCursesLeft(Cards.vineyard) == 0) {
                return Cards.vineyard;
            }
        }

        if (buyEstates) {
            if (turnCount > midGame && coinAvailableForBuy <= 2 && context.canBuy(Cards.estate)) {
                if(context.getEmbargosIfCursesLeft(Cards.estate) == 0) {
                    return Cards.estate;
                }
            }
        }
        
        if(context.canBuy(Cards.possession) && getMyCardCount(Cards.possession) < 2) {
            return Cards.possession;
        }

        if(context.canBuy(Cards.grandMarket) && rand.nextInt(MAX_OF_ONE_ACTION_CARD * 2) < getMyCardCount(Cards.grandMarket)) {
            return Cards.grandMarket;
        }

        //try cards with potion before silver 
        if (context.getPotions() > 0) {
            //buy in this order
            final Card[] POTION_CARDS = new Card[] { Cards.possession, Cards.golem, Cards.familiar, Cards.alchemist, Cards.philosophersStone, Cards.scryingPool, Cards.apothecary, Cards.university };
            for (Card card : POTION_CARDS) {
            	if (context.canBuy(card)) {
            		if (   getMyCardCount(card) >= 2
            			&& !(card.equals(Cards.alchemist) || card.equals(Cards.philosophersStone) || card.equals(Cards.scryingPool) ) ) {
            			continue;
            		}
            		if (card.equals(Cards.familiar) && (context.game.pileSize(Cards.curse) <= 3 || turnCount > midGame)) {
            			continue;
            		}
            		if (context.getEmbargosIfCursesLeft(card) > 0) {
            			continue;
            		}
            		if (coinAvailableForBuy >= card.getCost(context) + 3) {
            			continue;
            		}
            		return card;
            	}
            }
        }

        final double silverLine = .5d;

        if (favorSilverGoldPlat) {
            if (context.canBuy(Cards.silver) && game.pileSize(Cards.silver) > 0 && coinAvailableForBuy >= 3 && coinAvailableForBuy <= silverMax && rand.nextFloat() > silverLine) {
                if(context.getEmbargosIfCursesLeft(Cards.silver) == 0) {
                    return Cards.silver;
                }
            }

            if (context.canBuy(Cards.gold) && game.pileSize(Cards.gold) > 0 && coinAvailableForBuy >= 6 && coinAvailableForBuy <= 6 && rand.nextFloat() > silverLine) {
                if(context.getEmbargosIfCursesLeft(Cards.gold) == 0) {
                    return Cards.gold;
                }
            }

        }

        Card ret = null;
        if(!improvise) {
            ret = bestBuy(coinAvailableForBuy, context, valuedCards);
            if(ret != null) {
                return ret;
            }
        }

        ret = bestBuy(coinAvailableForBuy, context, context.getCardsInGame(GetCardsInGameOptions.Buyables));
        if(ret != null) {
            return ret;
        }

        if (context.canBuy(Cards.platinum)) {
            return Cards.platinum;
        }

        if (context.canBuy(Cards.gold)) {
            if(context.getEmbargosIfCursesLeft(Cards.gold) == 0) {
                return Cards.gold;
            }
        }

        if (context.canBuy(Cards.silver)) {
            if(context.getEmbargosIfCursesLeft(Cards.silver) == 0) {
                return Cards.silver;
            }
        }

        return null;
    }
    
    @Override
    public ArrayList<Card> treasureCardsToPlayInOrder(MoveContext context, int maxCards, Card responsible) {
        if(context.cardInGame(Cards.grandMarket)) {
            final ArrayList<Card> cards = new ArrayList<Card>();
            int coinWithoutCopper = 0;
            for(final Card c : context.getPlayer().getHand()) {
                if(c.is(Type.Treasure, this) && !c.equals(Cards.copper)) {
                    cards.add(c);
                    coinWithoutCopper += c.getAddGold();
                }
            }
            
            if(coinWithoutCopper >= Cards.grandMarket.getCost(context)) {
                return cards;
            }
        }
        
        return super.treasureCardsToPlayInOrder(context, maxCards, responsible);
    }
    

    @Override
    public void newGame(MoveContext context) {
        super.newGame(context);
        earlyCardBuyCount = 0;
        setupGameVariables(context.getGameType(), context.getCardsInGame(GetCardsInGameOptions.All));
    }        
        
    @Override
    public Card[] getTrashCards() {
        return trashCards;
    }
    
    public Card bestBuy(int coinAvailableForBuy, MoveContext context, Card[] cards) {
        // Try to buy valued cards...
        int cost = coinAvailableForBuy;
        while (cost >= 0) {
            final ArrayList<Card> randList = new ArrayList<Card>();
            for (final Card card : cards) {
                if (card.getCost(context) != cost) {
                    continue;
                }
                
                if(shouldPassOnBuy(context, card)) {
                    continue;
                }
                
                final int currentCount = getMyCardCount(card);
                if(isOnlyTreasure(card, context.getPlayer()) || card.is(Type.Victory) || currentCount == 0 || rand.nextInt(MAX_OF_ONE_ACTION_CARD) < currentCount) {
                    randList.add(card);
                }
            }

            // prefer silver instead of masterpiece if you can't overpay by 2
            if (randList.contains(Cards.masterpiece) && randList.contains(Cards.silver) && coinAvailableForBuy < 5) {
                randList.remove(Cards.masterpiece);
            }
            
            if (randList.size() > 0) {
                return randList.get(rand.nextInt(randList.size()));
            }
            
            cost--;
        }

        return null;
    }
    
    public Card[] chapel_cardsToTrash(MoveContext context) {
        ArrayList<Card> cards = new ArrayList<Card>();
    
        for (Card card : context.player.getHand()) {
            if (card.equals(Cards.estate) || card.equals(Cards.curse)) {
                cards.add(card);
            }
        }
     
        if (getCurrencyTotal(context) >= 3) {
            for (Card card : context.player.getHand()) {
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

    
}