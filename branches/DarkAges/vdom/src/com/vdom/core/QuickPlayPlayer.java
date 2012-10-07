package com.vdom.core;

import java.util.ArrayList;
import java.util.List;

import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;

public abstract class QuickPlayPlayer extends BasePlayer {
    protected static final Card[] TRASH_CARDS = new Card[] { Cards.curse, Cards.estate, Cards.copper };

//    public static QuickPlayPlayer instance = new QuickPlayPlayer();
    
    public QuickPlayPlayer() {
        // Set mid game to 0, because usually the late game logic is better.
        // Better fix would be to keep the turnCount accurate for the game.
        midGame = 0;
    }
    
    @Override
    public String getPlayerName() {
        return "Quick Play";
    }

    @Override
    public Card doAction(MoveContext context) {
        // Should never be called
        return null;
    }

    @Override
    public Card doBuy(MoveContext context) {
        // Should never be called
        return null;
    }

    public Card[] getTrashCards() {
        return TRASH_CARDS;
    }

    public boolean shouldAutoPlay_workshop_cardToObtain(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_feast_cardToObtain(MoveContext context) {
        return false;
    }


    public boolean shouldAutoPlay_remodel_cardToTrash(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_remodel_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_militia_attack_cardsToKeep(MoveContext context) {
        int canDiscard = 0;
        int total = 0;
        for (Card card : getHand()) {
            total++;
            if (card instanceof VictoryCard) {
                canDiscard++;
            }
            else {
                for(Card trash : getTrashCards()) {
                    if(trash.equals(card)) {
                        canDiscard++;
                        break;
                    }
                }
            }
        }
        
        if(total - canDiscard <= 3) {
            return true;
        }
        
        return false;
    }
    
    public boolean shouldAutoPlay_chancellor_shouldDiscardDeck(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_mine_treasureFromHandToUpgrade(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_chapel_cardsToTrash(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_cellar_cardsToDiscard(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_library_shouldKeepAction(MoveContext context, ActionCard action) {
        if(context.getActionsLeft() == 0) {
            return true;
        }
        
        return false;
    }

    
    public boolean shouldAutoPlay_spy_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
        return true;
    }

    
    public boolean shouldAutoPlay_scryingPool_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
        return true;
    }

    public boolean shouldAutoPlay_secretChamber_cardsToDiscard(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_secretChamber_cardsToPutOnDeck(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_pawn_chooseOptions(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_torturer_attack_chooseOption(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_steward_chooseOption(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_steward_cardsToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_swindler_cardToSwitch(MoveContext context, int cost, boolean potion) {
        return true;
    }

    
    public boolean shouldAutoPlay_torturer_attack_cardsToDiscard(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_courtyard_cardToPutBackOnDeck(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_baron_shouldDiscardEstate(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_ironworks_cardToObtain(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_masquerade_cardToPass(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_masquerade_cardToTrash(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_miningVillage_shouldTrashMiningVillage(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_saboteur_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return false;
    }

    
    public boolean shouldAutoPlay_topOfDeck_orderCards(MoveContext context, Card[] cards) {
        return true;
    }

    public boolean shouldAutoPlay_scout_orderCards(MoveContext context, Card[] cards) {
        return true;
    }

    public boolean shouldAutoPlay_mandarin_orderCards(MoveContext context, Card[] cards) {
        return true;
    }

    public boolean shouldAutoPlay_nobles_chooseOptions(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_tradingPost_cardsToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_wishingWell_cardGuess(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_upgrade_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_upgrade_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return false;
    }

    
    public boolean shouldAutoPlay_minion_chooseOption(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_ghostShip_attack_cardsToPutBackOnDeck(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_warehouse_cardsToDiscard(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_salvager_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_treasury_putBackOnDeck(MoveContext context, int treasuryCardsInPlay) {
        return true;
    }

    
    public boolean shouldAutoPlay_pirateShip_takeTreasure(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_nativeVillage_takeCards(MoveContext context) {
        if(nativeVillage == null || nativeVillage.isEmpty()) {
            return true;
        }
        return false;
    }

    
    public boolean shouldAutoPlay_smugglers_cardToObtain(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_island_cardToSetAside(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_haven_cardToSetAside(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_navigator_shouldDiscardTopCards(MoveContext context, Card[] cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_navigator_cardOrder(MoveContext context, Card[] cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_embargo_supplyToEmbargo(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_lookout_cardToTrash(MoveContext context, Card[] cards) {
        return true;
    }

    public boolean shouldAutoPlay_lookout_cardToDiscard(MoveContext context, Card[] cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_ambassador_revealedCard(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_ambassador_returnToSupplyFromHand(MoveContext context, Card card) {
        return true;
    }

    
    public boolean shouldAutoPlay_pearlDiver_shouldMoveToTop(MoveContext context, Card card) {
        return true;
    }

    public boolean shouldAutoPlay_explorer_shouldRevealProvince(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_university_actionCardToObtain(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_apprentice_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_transmute_cardToTrash(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_alchemist_backOnDeck(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_herbalist_backOnDeck(MoveContext context, TreasureCard[] cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_apothecary_cardsForDeck(MoveContext context, ArrayList<Card> cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_bishop_cardToTrashForVictoryTokens(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_bishop_cardToTrash(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_expand_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_expand_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return false;
    }

    
    public boolean shouldAutoPlay_forge_cardsToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_forge_cardToObtain(MoveContext context, int exactCost) {
        return false;
    }

    
    public boolean shouldAutoPlay_goons_attack_cardsToKeep(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_mint_treasureToMint(MoveContext context) {
        int treasureCount = 0;

        for(Card c : getHand()) {
            if(c instanceof TreasureCard) {
                treasureCount++;
            }
        }
        
        if(treasureCount <= 1) {
            return true;
        }
        
        for(Card c : getHand()) {
            if(c instanceof TreasureCard && !(c.equals(Cards.copper) || c.equals(Cards.silver) || c.equals(Cards.gold) || c.equals(Cards.platinum))) {
                return false;
            }
        }

        return true;
    }

    
    public boolean shouldAutoPlay_mountebank_attack_shouldDiscardCurse(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_rabble_attack_cardOrder(MoveContext context, Card[] cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_tradeRoute_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_vault_cardsToDiscardForGold(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_vault_cardsToDiscardForCard(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_contraband_cardPlayerCantBuy(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_kingsCourt_cardToPlay(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_throneRoom_cardToPlay(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_loan_shouldTrashTreasure(MoveContext context, Card treasure) {
        return false;
    }

    
    public boolean shouldAutoPlay_royalSeal_shouldPutCardOnDeck(MoveContext context, Card card) {
        return true;
    }

    
    public boolean shouldAutoPlay_watchTower_chooseOption(MoveContext context, Card card) {
        return true;
    }

    
    public boolean shouldAutoPlay_treasureCardsToPlayInOrder(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_golem_cardOrder(MoveContext context, Card[] cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_hamlet_cardToDiscardForAction(MoveContext context) {
        int actionCards = 0;
        int trashCards = 0;
        for(Card c : getHand()) {
            if(c instanceof ActionCard) {
                actionCards++;
            }
            for(Card trash : getTrashCards()) {
                if(c.equals(trash)) {
                    trashCards++;
                }
            }
        }
        
        if(actionCards == 0) {
            return true;
        }
        
        if(actionCards > 0 && trashCards > 0) {
            return true;
        }
        
        return false;
    }

    
    public boolean shouldAutoPlay_hamlet_cardToDiscardForBuy(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_hornOfPlenty_cardToObtain(MoveContext context, int maxCost) {
        return false;
    }

    
    public boolean shouldAutoPlay_horseTraders_cardsToDiscard(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_jester_chooseOption(MoveContext context, Player targetPlayer, Card card) {
        return true;
    }

    
    public boolean shouldAutoPlay_remake_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_remake_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return false;
    }

    
    public boolean shouldAutoPlay_tournament_shouldRevealProvince(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_tournament_chooseOption(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_tournament_choosePrize(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_youngWitch_cardsToDiscard(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_followers_attack_cardsToKeep(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_trustySteed_chooseOptions(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_mine_treasureToObtain(MoveContext context, int maxCost, boolean potion) {
        return true;
    }

    
    public boolean shouldAutoPlay_bureaucrat_cardToReplace(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_thief_treasureToTrash(MoveContext context, TreasureCard[] treasures) {
        return true;
    }

    public boolean shouldAutoPlay_thief_treasuresToGain(MoveContext context, TreasureCard[] treasures) {
        return true;
    }

    public boolean shouldAutoPlay_pirateShip_treasureToTrash(MoveContext context, TreasureCard[] treasures) {
        return true;
    }
    
    public boolean shouldAutoPlay_tunnel_shouldReveal(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_duchess_shouldGainBecauseOfDuchy(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_duchess_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
        for(Card trash : getTrashCards()) {
            if(trash.equals(card)) {
                return true;
            }
        }
        if(isOnlyVictory(card)) {
            return true;
        }
        
        return false;
    }
    
    public boolean shouldAutoPlay_foolsGold_shouldTrash(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_trader_shouldGainSilverInstead(MoveContext context, Card card) {
        if(isTrashCard(card)) {
            return true;
        }
        
        return false;
    }
    
    public boolean shouldAutoPlay_trader_cardToTrash(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_oasis_cardToDiscard(MoveContext context) {
        for(Card c : getHand()) {
            if(isTrashCard(c) || isOnlyTreasure(c)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean shouldAutoPlay_jackOfAllTrades_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
        if(isTrashCard(card) || isOnlyVictory(card)) {
            return true;
        }
        return false;
    }

    public boolean shouldAutoPlay_jackOfAllTrades_nonTreasureToTrash(MoveContext context) {
        return true; 
//        for(Card card : getHand()) {
//            if(isTrashCard(card) && !(card instanceof TreasureCard)) {
//                return true;
//            }
//        }
//        
//        return false;
    }
    
    public boolean shouldAutoPlay_nobleBrigand_silverOrGoldToTrash(MoveContext context, TreasureCard[] silverOrGoldCards) {
        return true;
    }
    
    public boolean shouldAutoPlay_oracle_shouldDiscard(MoveContext context, Player player, ArrayList<Card> cards) {
        return true;
    }
    
    public boolean shouldAutoPlay_oracle_orderCards(MoveContext context, Card[] cards) {
        return true;
    }

    public boolean shouldAutoPlay_develop_cardToTrash(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_develop_lowCardToGain(MoveContext context, int cost, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_develop_highCardToGain(MoveContext context, int cost, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_develop_orderCards(MoveContext context, Card[] cards) {
        return true;
    }
    
    public boolean shouldAutoPlay_cartographer_cardsFromTopOfDeckToDiscard(MoveContext context, Card[] cards) {
        return true;
    }
    
    public boolean shouldAutoPlay_cartographer_cardOrder(MoveContext context, Card[] cards) {
        return true;
    }
    
    public boolean shouldAutoPlay_spiceMerchant_chooseOption(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_spiceMerchant_treasureToTrash(MoveContext context) {
        for(Card card : getHand()) {
            for(Card trash : getTrashCards()) {
                if(trash.equals(card) && (card instanceof TreasureCard)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    public boolean shouldAutoPlay_embassy_cardsToDiscard(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_illGottenGains_gainCopper(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_haggler_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_scheme_actionToPutOnTopOfDeck(MoveContext context, ActionCard[] actions) {
        return false;
    }
    
    public boolean shouldAutoPlay_inn_cardsToDiscard(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_inn_shuffleCardBackIntoDeck(MoveContext context, ActionCard card) {
        return true;
    }
    
    public boolean shouldAutoPlay_farmland_cardToTrash(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_farmland_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_stables_treasureToDiscard(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_borderVillage_cardToObtain(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_mandarin_cardToReplace(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_rats_cardToTrash(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_pillage_opponentCardToDiscard(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_margrave_attack_cardsToKeep(MoveContext context) {
        return shouldAutoPlay_militia_attack_cardsToKeep(context);
    }

    public boolean shouldAutoPlay_revealBane(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_selectPutBackOption(MoveContext context, List<PutBackOption> putBacks) {
    	return true;
    }
    
    public boolean shouldAutoPlay_walledVillage_backOnDeck(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_envoy_opponentCardToDiscard(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_cultist_shouldPlayNext(MoveContext context) {
		return true;
	}
	
	public boolean shouldAutoPlay_urchin_shouldTrashForMercenary(MoveContext context) {
		return true;
	}
	
	public boolean shouldAutoPlay_madman_shouldReturnToPile(MoveContext context) {
		return true;
	}
	
	public boolean shouldAutoPlay_hermit_trashForMadman(MoveContext context) {
		return true;
	}
	
	public boolean shouldAutoPlay_procession_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        return false;
    }
}
