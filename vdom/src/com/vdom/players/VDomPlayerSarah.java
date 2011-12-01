package com.vdom.players;

import java.util.ArrayList;
import java.util.Random;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.Cards;
import com.vdom.api.GameType;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.core.BasePlayer;
import com.vdom.core.MoveContext;

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
    
    public boolean isAi() {
        return true;
    }

    public void setupGameVariables(GameType gameType, Card[] cardsInPlay) {
        trashCards = new Card[] { Cards.curse, Cards.estate, Cards.copper };
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
        for (Card card : cardsInPlay) {
            if (card.getExpansion() != null && card.getExpansion().equals("Cornucopia")) {
                numCornucopia++;
            }
        }

        if (gameType == GameType.BigMoney || gameType == GameType.VillageSquare || gameType == GameType.SecretSchemes || numCornucopia >= 5) {
            favorSilverGoldPlat = false;
        }

        if (gameType == GameType.FirstGame) {
            midGame = 12;

            this.valuedCards = new Card[] { Cards.cellar, Cards.militia, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.BigMoney) {
            midGame = 20;
            earlyCardBuyMax = 2;
            this.valuedCards = new Card[] { Cards.feast, Cards.laboratory, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.Interaction) {
            midGame = 12;
            earlyCardBuyMax = 2;
        } else if (gameType == GameType.SizeDistortion) {
            midGame = 14;
            this.valuedCards = new Card[] { Cards.feast, Cards.chapel, Cards.laboratory, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.VillageSquare) {
            midGame = 12;
            actionCardMax = 7;
            this.valuedCards = new Card[] { Cards.smithy, Cards.festival, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.Repetition) {
            earlyCardBuyMax = 2;
        } else if (gameType == GameType.GiveAndTake) {
            actionCardMax = 10;
            this.valuedCards = new Card[] { Cards.fishingVillage, Cards.library, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.Shipwrecks) {
            midGame = 18;
            this.valuedCards = new Card[] { Cards.treasury, Cards.pearlDiver, Cards.ghostShip, Cards.seaHag, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.RandomSeaside) {
            midGame = 16;
            earlyCardBuyMax = 1;
            actionCardMax = 16;
            this.valuedCards = new Card[] { Cards.warehouse, Cards.wharf, Cards.bazaar, Cards.pearlDiver, Cards.ghostShip, Cards.platinum, Cards.gold,
                    Cards.silver };
        } else if (gameType == GameType.ReachForTomorrow) {
            earlyCardBuys = new Card[] { Cards.seaHag };
            earlyCardBuyMax = 1;
            midGame = 14;
            actionCardMax = 8;
            this.valuedCards = new Card[] { Cards.treasureMap, Cards.ghostShip, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.BuriedTreasure) {
            midGame = 14;
            earlyCardBuys = new Card[] { Cards.treasureMap };
            // onlyBuyEarlySingle = true;
            earlyCardBuyMax = 2;
            actionCardMax = 12;

            this.valuedCards = new Card[] { Cards.warehouse, Cards.treasureMap, Cards.wharf, Cards.pearlDiver, Cards.fishingVillage, Cards.platinum,
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
            this.valuedCards = new Card[] { Cards.laboratory, Cards.adventurer, Cards.venture, Cards.royalSeal, Cards.mint, Cards.platinum, Cards.gold, Cards.silver };
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
            this.valuedCards = new Card[] { Cards.countingHouse, Cards.contraband, Cards.bureaucrat, Cards.chancellor, Cards.mountebank, Cards.platinum, Cards.gold, Cards.silver, Cards.copper };
            trashCards = new Card[] { Cards.curse, Cards.estate };
        } else if (gameType == GameType.PathToVictory) {
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
            this.valuedCards = new Card[] { Cards.watchTower, Cards.miningVillage, Cards.platinum, Cards.gold, Cards.silver };
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
            this.valuedCards = new Card[] { Cards.festival, Cards.moneyLender, Cards.menagerie, Cards.harvest, Cards.platinum, Cards.gold, Cards.silver };
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
            this.valuedCards = new Card[] { Cards.jester, Cards.throneRoom, Cards.fortuneTeller, Cards.laboratory, Cards.platinum, Cards.gold, Cards.silver };
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
            this.valuedCards = new Card[] { Cards.jester, Cards.chancellor, Cards.farmingVillage, Cards.market, Cards.platinum, Cards.gold, Cards.silver };
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
            this.valuedCards = new Card[] { Cards.jester, Cards.wishingWell, Cards.miningVillage, Cards.platinum, Cards.gold, Cards.silver };
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
            this.valuedCards = new Card[] { Cards.fortuneTeller, Cards.greatHall, Cards.harem, Cards.duke, Cards.pawn, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.RandomCornucopia) {
            midGame = 15;
            actionCardMax = 6;
            this.valuedCards = new Card[] { Cards.tournament, Cards.jester, Cards.youngWitch, Cards.hamlet, Cards.fortuneTeller, Cards.platinum, Cards.gold, Cards.silver };
        } else {
            improvise = true;
            actionCardMax = 5;
        }
    }

    @Override
    public String getPlayerName() {
        return "Sarah";
    }

    @Override
    public Card doAction(MoveContext context) {
        int treasureMapCount = 0;

        for (Card card : getHand()) {
            if (card.equals(Cards.treasureMap)) {
                treasureMapCount++;
            }
        }

        if (treasureMapCount >= 2)
            return fromHand(Cards.treasureMap);
        
        ActionCard action;
        for (Card card : getHand()) {
            if (context.canPlay(card)) {
                action = (ActionCard) card;
                if (action.getAddActions() > 0) {
                    return action;
                }
            }
        }

        if(inHand(Cards.throneRoom) && context.canPlay(Cards.throneRoom)) {
            return Cards.throneRoom;
        }
        
        //TODO: ...
        //if(context.getKingsCourtsInEffect() == 0) {
            if(inHand(Cards.kingsCourt) && context.canPlay(Cards.kingsCourt)) {
                return Cards.kingsCourt;
            }
        //}
        
        //TODO: simple action play order list instead of just picking the most expensive card
        int cost = COST_MAX;
        ArrayList<Card> randList = new ArrayList<Card>();
        while (cost >= 0) {
            for (Card card : getHand()) {
                if (
                        !context.canPlay(card) || 
                        card.equals(Cards.treasureMap) ||
                        card.getCost(context) != cost
                   )
                      continue;
                
                if(card.getCost(context) == cost) {
                    randList.add(card);
                }
            }

            if (randList.size() > 0) {
                return (Card) randList.get(this.rand.nextInt(randList.size()));
            }

            cost--;
        }

        return null;
    }
    
    public boolean shouldPassOnBuy(MoveContext context, Card card) {
        return 
                !context.canBuy(card) || 
                ((card instanceof ActionCard) && actionCardCount >= actionCardMax) || 
                (!favorSilverGoldPlat && (card.equals(Cards.silver) || card.equals(Cards.gold) || card.equals(Cards.platinum))) ||
                card.equals(Cards.curse) || 
                card.equals(Cards.copper) || 
                (card.equals(Cards.potion) && !shouldBuyPotion()) ||
                (card.equals(Cards.throneRoom) && throneRoomAndKingsCourtCount >= throneRoomsAndKingsCourtsMax) ||
                (card.equals(Cards.kingsCourt) && throneRoomAndKingsCourtCount >= throneRoomsAndKingsCourtsMax) ||
                context.getEmbargos(card) > 0 ||
                (!(card instanceof ActionCard) && !(card instanceof TreasureCard));
    }

    @Override
    public Card doBuy(MoveContext context) {
        int coinAvailableForBuy = context.getCoinAvailableForBuy();

        if (earlyCardBuyCount < earlyCardBuyMax) {
            ArrayList<Card> randList = new ArrayList<Card>();
            for (Card card : earlyCardBuys) {
                if (context.canBuy(card)) { // && (coinAvailableForBuy == card.getCost()) {
                    randList.add(card);
                }
            }

            if (randList.size() > 0) {
                earlyCardBuyCount++;
                return randList.get(this.rand.nextInt(randList.size()));
            }
        }

        if (onlyBuyEarlySingle && earlyCardBuyCount < earlyCardBuyMax) {
            return null;
        }

        if (coinAvailableForBuy == 0) {
            return null;
        }

        if (context.canBuy(Cards.colony)) {
            return Cards.colony;
        }
        
        if (favorSilverGoldPlat && coinAvailableForBuy >= 9 && (context.canBuy(Cards.platinum)) && turnCount < midGame) {
            return Cards.platinum;
        }

        if ((alwaysBuyProvince || turnCount > earlyGame) && context.canBuy(Cards.province)) {
            return Cards.province;
        }
        
        if ((this.turnCount > midGame) && (coinAvailableForBuy <= 7) && (context.canBuy(Cards.duchy) && rand.nextInt(3) == 0)) {
            if(context.getEmbargos(Cards.duchy) == 0) {
                return Cards.duchy;
            }
        }

        if (buyEstates) {
            if ((this.turnCount > midGame) && (coinAvailableForBuy <= 2) && (context.canBuy(Cards.estate))) {
                if(context.getEmbargos(Cards.estate) == 0) {
                    return Cards.estate;
                }
            }
        }
        
        if(context.canBuy(Cards.grandMarket) && rand.nextInt(MAX_OF_ONE_ACTION_CARD * 2) < inHandCount(Cards.grandMarket)) {
            return Cards.grandMarket;
        }

        double silverLine = .5d;

        if (favorSilverGoldPlat) {
            if ((coinAvailableForBuy >= 3) && (coinAvailableForBuy <= silverMax) && (this.rand.nextFloat() > silverLine)) {
                if(context.getEmbargos(Cards.silver) == 0) {
                    return Cards.silver;
                }
            }

            if ((coinAvailableForBuy >= 6) && (coinAvailableForBuy <= 6) && (this.rand.nextFloat() > silverLine)) {
                if(context.getEmbargos(Cards.gold) == 0) {
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

        ret = bestBuy(coinAvailableForBuy, context, context.getCardsInPlay());
        if(ret != null) {
            return ret;
        }

        if (context.canBuy(Cards.platinum)) {
            return Cards.platinum;
        }

        if (context.canBuy(Cards.gold)) {
            if(context.getEmbargos(Cards.gold) == 0) {
                return Cards.gold;
            }
        }

        if (context.canBuy(Cards.silver)) {
            if(context.getEmbargos(Cards.silver) == 0) {
                return Cards.silver;
            }
        }

        return null;
    }
    
    @Override
    public ArrayList<TreasureCard> treasureCardsToPlayInOrder(MoveContext context) {
        if(context.cardInPlay(Cards.grandMarket)) {
            ArrayList<TreasureCard> cards = new ArrayList<TreasureCard>();
            int coinWithoutCopper = 0;
            for(Card c : getHand()) {
                if(c instanceof TreasureCard && !c.equals(Cards.copper)) {
                    TreasureCard tc = (TreasureCard) c;
                    cards.add(tc);
                    coinWithoutCopper += tc.getValue();
                }
            }
            
            if(coinWithoutCopper >= Cards.grandMarket.getCost(context)) {
                return cards;
            }
        }
        
        return super.treasureCardsToPlayInOrder(context);
    }
    

    @Override
    public void newGame(MoveContext context) {
        super.newGame(context);
        earlyCardBuyCount = 0;
        setupGameVariables(context.getGameType(), context.getCardsInPlay());
    }        
        
    @Override
    public Card[] getTrashCards() {
        return trashCards;
    }
    
    public Card bestBuy(int coinAvailableForBuy, MoveContext context, Card[] cards) {
        // Try to buy valued cards...
        int cost = coinAvailableForBuy;
        while (cost >= 0) {
            ArrayList<Card> randList = new ArrayList<Card>();
            for (Card card : cards) {
                if (card.getCost(context) != cost)
                    continue;
                
                if(shouldPassOnBuy(context, card))
                     continue;
                
                int currentCount = inHandCount(card);
                if(isOnlyTreasure(card) || card instanceof VictoryCard || currentCount == 0 || rand.nextInt(MAX_OF_ONE_ACTION_CARD) < currentCount) {
                    randList.add(card);
                }
            }

            if (randList.size() > 0) {
                return randList.get(this.rand.nextInt(randList.size()));
            }
            
            cost--;
        }

        return null;
    }
}
