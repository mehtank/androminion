package com.vdom.players;

import com.vdom.api.Card;
import com.vdom.api.Cards;
import com.vdom.api.GameType;

public class VDomPlayerDrew extends VDomPlayerSarah {

    public boolean isAi() {
        return true;
    }
    
    public void setupGameVariables(GameType gameType, Card[] cardsInPlay) {
        trashCards = new Card[] { Cards.curse, Cards.estate, Cards.copper };
        valuedCards = new Card[] { Cards.torturer, Cards.bazaar, Cards.masquerade, Cards.ghostShip, Cards.wharf, Cards.smithy, Cards.harem, Cards.adventurer,
                Cards.shantyTown, Cards.festival, Cards.moneyLender, Cards.platinum, Cards.gold, Cards.silver };
        improvise = false;
        midGame = 12;
        actionCardMax = 12;
        alwaysBuyProvince = true;
        buyEstates = true;
        favorSilverGoldPlat = true;
        onlyBuyEarlySingle = false;
        
        earlyCardBuys = new Card[] { Cards.militia, Cards.seaHag, Cards.familiar, Cards.youngWitch, Cards.thief, Cards.pirateShip, Cards.rabble, Cards.goons,
                Cards.fortuneTeller, Cards.jester };
        earlyCardBuyMax = 1;
        
        silverMax = 4;

        if (gameType == GameType.Underlings || gameType == GameType.Repetition) {
            buyEstates = false;
        }

        if (gameType == GameType.FirstGame || gameType == GameType.RandomBaseGame || gameType == GameType.VictoryDance || gameType == GameType.HandMadness
                || gameType == GameType.Underlings) {
            silverMax = 5;
        }

        if (gameType == GameType.FirstGame) {
            midGame = 12;

            this.valuedCards = new Card[] { Cards.cellar, Cards.militia, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.BigMoney) {
            midGame = 20;
            earlyCardBuyMax = 2;
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

            if (gameType == GameType.BigMoney || gameType == GameType.SizeDistortion || gameType == GameType.VictoryDance || gameType == GameType.SecretSchemes
                    || gameType == GameType.BestWishes || gameType == GameType.RandomIntrigue || gameType == GameType.Underlings
                    || gameType == GameType.HighSeas || gameType == GameType.BuriedTreasure || gameType == GameType.GiveAndTake) {
                alwaysBuyProvince = false;
            }
        }

        else if (gameType == GameType.Beginners) {
            // addPile(Cards.bank);
            // addPile(Cards.countingHouse);
            // addPile(Cards.expand);
            // addPile(Cards.goons);
            // addPile(Cards.monument);
            // addPile(Cards.rabble);
            // addPile(Cards.royalSeal);
            // addPile(Cards.venture);
            // addPile(Cards.watchTower);
            // addPile(Cards.workersVillage);
            improvise = true;
        } else if (gameType == GameType.FriendlyInteractive) {
            // addPile(Cards.bishop);
            // addPile(Cards.city);
            // addPile(Cards.contraband);
            // addPile(Cards.forge);
            // addPile(Cards.hoard);
            // addPile(Cards.peddler);
            // addPile(Cards.royalSeal);
            // addPile(Cards.tradeRoute);
            // addPile(Cards.vault);
            // addPile(Cards.workersVillage);
            improvise = true;
        } else if (gameType == GameType.BigActions) {
            // addPile(Cards.city);
            // addPile(Cards.expand);
            // addPile(Cards.grandMarket);
            // addPile(Cards.kingsCourt);
            // addPile(Cards.loan);
            // addPile(Cards.mint);
            // addPile(Cards.quarry);
            // addPile(Cards.rabble);
            // addPile(Cards.talisman);
            // addPile(Cards.vault);
            improvise = true;
        } else if (gameType == GameType.BiggestMoney) {
            // addPile(Cards.bank);
            // addPile(Cards.grandMarket);
            // addPile(Cards.mint);
            // addPile(Cards.royalSeal);
            // addPile(Cards.venture);
            // addPile(Cards.adventurer);
            // addPile(Cards.laboratory);
            // addPile(Cards.mine);
            // addPile(Cards.moneyLender);
            // addPile(Cards.spy);
            improvise = true;
        } else if (gameType == GameType.TheKingsArmy) {
            // addPile(Cards.expand);
            // addPile(Cards.goons);
            // addPile(Cards.kingsCourt);
            // addPile(Cards.rabble);
            // addPile(Cards.vault);
            // addPile(Cards.bureaucrat);
            // addPile(Cards.councilRoom);
            // addPile(Cards.moat);
            // addPile(Cards.spy);
            // addPile(Cards.village);
            improvise = true;
        } else if (gameType == GameType.TheGoodLife) {
            // addPile(Cards.contraband);
            // addPile(Cards.countingHouse);
            // addPile(Cards.hoard);
            // addPile(Cards.monument);
            // addPile(Cards.mountebank);
            // addPile(Cards.bureaucrat);
            // addPile(Cards.cellar);
            // addPile(Cards.chancellor);
            // addPile(Cards.gardens);
            // addPile(Cards.village);
            improvise = true;
        } else if (gameType == GameType.PathToVictory) {
            // addPile(Cards.bishop);
            // addPile(Cards.countingHouse);
            // addPile(Cards.goons);
            // addPile(Cards.monument);
            // addPile(Cards.peddler);
            // addPile(Cards.baron);
            // addPile(Cards.harem);
            // addPile(Cards.pawn);
            // addPile(Cards.shantyTown);
            // addPile(Cards.upgrade);
            improvise = true;
        } else if (gameType == GameType.AllAlongTheWatchtower) {
            // addPile(Cards.hoard);
            // addPile(Cards.talisman);
            // addPile(Cards.tradeRoute);
            // addPile(Cards.vault);
            // addPile(Cards.watchTower);
            // addPile(Cards.bridge);
            // addPile(Cards.greatHall);
            // addPile(Cards.miningVillage);
            // addPile(Cards.pawn);
            // addPile(Cards.torturer);
            improvise = true;
        } else if (gameType == GameType.LuckySeven) {
            // addPile(Cards.bank);
            // addPile(Cards.expand);
            // addPile(Cards.forge);
            // addPile(Cards.kingsCourt);
            // addPile(Cards.vault);
            // addPile(Cards.bridge);
            // addPile(Cards.coppersmith);
            // addPile(Cards.swindler);
            // addPile(Cards.tribute);
            // addPile(Cards.wishingWell);
            improvise = true;
        } else if (gameType == GameType.BountyOfTheHunt) {
            // addPile(Cards.harvest);
            // addPile(Cards.hornOfPlenty);
            // addPile(Cards.huntingParty);
            // addPile(Cards.menagerie);
            // addPile(Cards.tournament);
            // addPile(Cards.cellar);
            // addPile(Cards.festival);
            // addPile(Cards.militia);
            // addPile(Cards.moneyLender);
            // addPile(Cards.smithy);
            earlyCardBuys = new Card[] { Cards.militia };
            earlyCardBuyMax = 1;
            midGame = 14;
            actionCardMax = 5;
            this.valuedCards = new Card[] { Cards.festival, Cards.moneyLender, Cards.menagerie, Cards.harvest, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.BadOmens) {
            // addPile(Cards.fortuneTeller);
            // addPile(Cards.hamlet);
            // addPile(Cards.hornOfPlenty);
            // addPile(Cards.jester);
            // addPile(Cards.remake);
            // addPile(Cards.adventurer);
            // addPile(Cards.bureaucrat);
            // addPile(Cards.laboratory);
            // addPile(Cards.spy);
            // addPile(Cards.throneRoom);
            midGame = 15;
            actionCardMax = 7;
            this.valuedCards = new Card[] { Cards.jester, Cards.throneRoom, Cards.fortuneTeller, Cards.laboratory, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.TheJestersWorkshop) {
            // addPile(Cards.fairgrounds);
            // addPile(Cards.farmingVillage);
            // addPile(Cards.horseTraders);
            // addPile(Cards.jester);
            // addPile(Cards.youngWitch);
            // addPile(Cards.feast);
            // addPile(Cards.laboratory);
            // addPile(Cards.market);
            // addPile(Cards.remodel);
            // addPile(Cards.workshop);
            // addPile(banePile = new Pile(Cards.chancellor,
            // kingdomCardPileSize));
            earlyCardBuys = new Card[] { Cards.youngWitch };
            earlyCardBuyMax = 1;
            midGame = 14;
            actionCardMax = 5;
            this.valuedCards = new Card[] { Cards.jester, Cards.chancellor, Cards.farmingVillage, Cards.market, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.LastLaughs) {
            // addPile(Cards.farmingVillage);
            // addPile(Cards.harvest);
            // addPile(Cards.horseTraders);
            // addPile(Cards.huntingParty);
            // addPile(Cards.jester);
            // addPile(Cards.minion);
            // addPile(Cards.nobles);
            // addPile(Cards.pawn);
            // addPile(Cards.steward);
            // addPile(Cards.swindler);
            improvise = true;
        } else if (gameType == GameType.TheSpiceOfLife) {
            // addPile(Cards.fairgrounds);
            // addPile(Cards.hornOfPlenty);
            // addPile(Cards.remake);
            // addPile(Cards.tournament);
            // addPile(Cards.youngWitch);
            // addPile(Cards.coppersmith);
            // addPile(Cards.courtyard);
            // addPile(Cards.greatHall);
            // addPile(Cards.miningVillage);
            // addPile(Cards.tribute);
            // addPile(banePile = new Pile(Cards.wishingWell,
            // kingdomCardPileSize));
            earlyCardBuys = new Card[] { Cards.youngWitch };
            earlyCardBuyMax = 1;
            midGame = 15;
            actionCardMax = 5;
            this.valuedCards = new Card[] { Cards.jester, Cards.wishingWell, Cards.courtyard, Cards.miningVillage, Cards.platinum, Cards.gold, Cards.silver };
        } else if (gameType == GameType.SmallVictories) {
            // addPile(Cards.fortuneTeller);
            // addPile(Cards.hamlet);
            // addPile(Cards.huntingParty);
            // addPile(Cards.remake);
            // addPile(Cards.tournament);
            // addPile(Cards.conspirator);
            // addPile(Cards.duke);
            // addPile(Cards.greatHall);
            // addPile(Cards.harem);
            // addPile(Cards.pawn);
            midGame = 15;
            actionCardMax = 10;
            this.valuedCards = new Card[] { Cards.fortuneTeller, Cards.greatHall, Cards.harem, Cards.duke, Cards.pawn, Cards.platinum, Cards.gold, Cards.silver };
        } else {
            improvise = true;
            actionCardMax = 7;
        }
    }

    @Override
    public String getPlayerName() {
        return "Drew";
    }
}
