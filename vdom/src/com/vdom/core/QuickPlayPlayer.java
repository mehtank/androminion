package com.vdom.core;

import java.util.ArrayList;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.core.MoveContext.TurnPhase;

public abstract class QuickPlayPlayer extends BasePlayer {
    //trash in this order!
    protected static final Card[] TRASH_CARDS = new Card[] { Cards.curse, Cards.rats, Cards.overgrownEstate, Cards.ruinedVillage, Cards.ruinedMarket, Cards.survivors, Cards.ruinedLibrary, Cards.abandonedMine, Cards.virtualRuins, Cards.hovel, Cards.estate, Cards.copper, Cards.masterpiece };

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

    public boolean shouldAutoPlay_remodel_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_militia_attack_cardsToKeep(MoveContext context) {
        int canDiscard = 0;
        int total = 0;
        for (Card card : getHand()) {
            total++;
            if (card.is(Type.Victory, context.getPlayer())) {
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
        ArrayList<Card> handCards = context.getPlayer().getTreasuresInHand();
        return hasOnlyBasicTreasure(handCards);
    }
    
    private boolean hasOnlyBasicTreasure(Iterable<Card> cards) {
    	boolean hasOnlyBasicTreasure = true;
        for (Card c : cards) {
        	//Don't count Potion as a basic card here since you may not want to upgrade it automatically with Mine
        	if (!c.equals(Cards.copper) &&
        			!c.equals(Cards.silver) &&
        			!c.equals(Cards.gold) && 
        			!c.equals(Cards.platinum)) {
        		hasOnlyBasicTreasure = false;
        		break;
        	}
        }
        return hasOnlyBasicTreasure;
    }
    
    public boolean shouldAutoPlay_moneylender_shouldTrashCopper(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_chapel_cardsToTrash(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_cellar_cardsToDiscard(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_library_shouldKeepAction(MoveContext context, Card action) {
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
    
    public boolean shouldAutoPlay_bandit_treasureToTrash(MoveContext context, Card[] treasures) {
        return false;
    }
    
    public boolean shouldAutoPlay_poacher_cardsToDiscard(MoveContext context, int numToDiscard) {
        return false;
    }
    
    public boolean shouldAutoPlay_sentry_chooseOption(MoveContext context, Card card, Card[] cards) {
        return false;
    }
    
    public boolean shouldAutoPlay_sentry_cardOrder(MoveContext context, Card[] cards) {
        return false;
    }
        
    public boolean shouldAutoPlay_vassal_shouldPlayCard(MoveContext context, Card card) {
    	return false;
    }
    
    public boolean shouldAutoPlay_secretChamber_cardsToDiscard(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_secretChamber_cardsToPutOnDeck(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_courtier_cardToReveal(MoveContext context) { return true; }

    public boolean shouldAutoPlay_courtier_chooseOptions(MoveContext context, CourtierOption[] options, int numberOfChoices) { return false; }

    public boolean shouldAutoPlay_diplomat_cardsToDiscard(MoveContext context) { return true; }

    public boolean shouldAutoPlay_lurker_selectChoice(MoveContext context, LurkerOption[] options) {return false; }

    public boolean shouldAutoPlay_lurker_cardToTrash(MoveContext context) {return false; }

    public boolean shouldAutoPlay_lurker_cardToGainFromTrash(MoveContext context) {return false; }
    
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

    
    public boolean shouldAutoPlay_swindler_cardToSwitch(MoveContext context, int cost, int debtCost, boolean potion) {
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

    
    public boolean shouldAutoPlay_miningVillage_shouldTrashMiningVillage(MoveContext context, Card responsible) {
        return false;
    }

    
    public boolean shouldAutoPlay_saboteur_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion) {
        return false;
    }

    
    public boolean shouldAutoPlay_topOfDeck_orderCards(MoveContext context, Card[] cards) {
        return true;
    }

    public boolean shouldAutoPlay_scoutPatrol_orderCards(MoveContext context, Card[] cards) {
        return true;
    }

    public boolean shouldAutoPlay_replace_cardToTrash(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_replace_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion) {
        return false;
    }

    public boolean shouldAutoPlay_mill_cardsToDiscard(MoveContext context) { return false; }

    public boolean shouldAutoPlay_mandarin_orderCards(MoveContext context, Card[] cards) {
        return true;
    }

    public boolean shouldAutoPlay_secretPassage_cardToPutInDeck(MoveContext context) {
        return false;
    }
    public boolean shouldAutoPlay_secretPassage_positionToPutCard(MoveContext context, Card card) {
        return false;
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

    public boolean shouldAutoPlay_scavenger_shouldDiscardDeck(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_upgrade_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_upgrade_cardToObtain(MoveContext context, int exactCost, int debtCost, boolean potion) {
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

    
    public boolean shouldAutoPlay_prince_cardToSetAside(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_duration_cardToPlay(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_blackMarket_chooseCard(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_blackMarket_orderCards(MoveContext context, Card[] cards) {
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

    
    public boolean shouldAutoPlay_herbalist_backOnDeck(MoveContext context, Card[] cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_apothecary_cardsForDeck(MoveContext context, ArrayList<Card> cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_bishop_cardToTrashForVictoryTokens(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_bishop_cardToTrash(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_countingHouse_coppersIntoHand(MoveContext context) {
        return true;
    }

    
    public boolean shouldAutoPlay_expand_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_expand_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion) {
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
            if(c.is(Type.Treasure, this) && Cards.isSupplyCard(c)) {
                treasureCount++;
            }
        }
        
        if(treasureCount <= 1) {
            return true;
        }
        
        for(Card c : getHand()) {
            if(c.is(Type.Treasure, this) && Cards.isSupplyCard(c) && !(c.equals(Cards.copper) || c.equals(Cards.silver) || c.equals(Cards.gold) || c.equals(Cards.platinum))) {
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

    
    public boolean shouldAutoPlay_royalSealTravellingFair_shouldPutCardOnDeck(MoveContext context, Card responsible, Card card) {
        return true;
    }

    
    public boolean shouldAutoPlay_watchTower_chooseOption(MoveContext context, Card card) {
        return true;
    }

    
    public boolean shouldAutoPlay_treasureCardsToPlayInOrder(MoveContext context, int maxCards) {
        return true;
    }

    
    public boolean shouldAutoPlay_golem_cardOrder(MoveContext context, Card[] cards) {
        return true;
    }

    
    public boolean shouldAutoPlay_hamlet_cardToDiscardForAction(MoveContext context) {
        int actionCards = 0;
        int trashCards = 0;
        for(Card c : getHand()) {
            if(c.is(Type.Action, context.player)) {
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

    
    public boolean shouldAutoPlay_discardMultiple_cardsToDiscard(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_jester_chooseOption(MoveContext context, Player targetPlayer, Card card) {
        return true;
    }

    
    public boolean shouldAutoPlay_remake_cardToTrash(MoveContext context) {
        return false;
    }

    
    public boolean shouldAutoPlay_remake_cardToObtain(MoveContext context, int exactCost, int debtCost, boolean potion) {
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

    
    public boolean shouldAutoPlay_mine_treasureToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion) {
        Card[] cards = context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Treasure);
        List<Card> obtainableCards = new ArrayList<Card>();
        for (Card c : cards) {
        	if ((!c.costPotion() || potion) && (maxCost >= c.getCost(context)) && maxDebtCost >= c.getDebtCost(context)) {
        		obtainableCards.add(c);
        	}
        }
        return hasOnlyBasicTreasure(obtainableCards);
    }
    
    public boolean shouldAutoPlay_bureaucrat_cardToReplace(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_thief_treasureToTrash(MoveContext context, Card[] treasures) {
        return true;
    }

    public boolean shouldAutoPlay_thief_treasuresToGain(MoveContext context, Card[] treasures) {
        return true;
    }

    public boolean shouldAutoPlay_pirateShip_treasureToTrash(MoveContext context, Card[] treasures) {
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
        if(isOnlyVictory(card, context.getPlayer())) {
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
            if(isTrashCard(c) || isOnlyTreasure(c, context.player)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean shouldAutoPlay_jackOfAllTrades_shouldDiscardCardFromTopOfDeck(MoveContext context, Card card) {
        if(isTrashCard(card) || isOnlyVictory(card, context.getPlayer())) {
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
    
    public boolean shouldAutoPlay_nobleBrigand_silverOrGoldToTrash(MoveContext context, Card[] silverOrGoldCards) {
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
    
    public boolean shouldAutoPlay_develop_lowCardToGain(MoveContext context, int cost, int debt, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_develop_highCardToGain(MoveContext context, int cost, int debt, boolean potion) {
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
                if(trash.equals(card) && (card.is(Type.Treasure, this))) {
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
    
    public boolean shouldAutoPlay_haggler_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_scheme_actionToPutOnTopOfDeck(MoveContext context, Card[] actions) {
        return false;
    }
    
    public boolean shouldAutoPlay_inn_cardsToDiscard(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_inn_shuffleCardBackIntoDeck(MoveContext context, Card card) {
        return true;
    }
    
    public boolean shouldAutoPlay_farmland_cardToTrash(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_farmland_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        return false;
    }
    
    public boolean shouldAutoPlay_stables_treasureToDiscard(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_borderVillage_cardToObtain(MoveContext context, int maxCost) {
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
    
    public boolean shouldAutoPlay_governor_cardToTrash(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_governor_cardToObtain(MoveContext context, int exactCost, int debt, boolean potion) {
    	return false;
    }
    
    public boolean shouldAutoPlay_envoy_opponentCardToDiscard(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_stash_chooseDeckPosition(MoveContext context, int deckSize, int numStashes, int cardsToDraw) {
    	return context.phase == TurnPhase.Buy;
    }
        
    public boolean shouldAutoPlay_sauna_shouldPlayAvanto(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_sauna_cardToTrash(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_avanto_shouldPlaySauna(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_cultist_shouldPlayNext(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_urchin_shouldTrashForMercenary(MoveContext context, Card responsible) {
        return true;
    }

    public boolean shouldAutoPlay_madman_shouldReturnToPile(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_procession_cardToObtain(MoveContext context, int maxCost, int debt, boolean potion) {
        return false;
    }

    public boolean shouldAutoPlay_bandOfMisfits_actionCardToImpersonate(MoveContext context, int maxCost) {
        return false;
    }
    
    public boolean shouldAutoPlay_taxman_treasureToObtain(MoveContext context, int maxCost, int debt, boolean potion) {
        return true;
    }

    /*Adventures*/
    public boolean shouldAutoPlay_amulet_chooseOption(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_amulet_cardToTrash(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_artificer_cardsToDiscard(MoveContext context) {
        return false;
    }

    public boolean shouldAutoPlay_artificer_cardToObtain(MoveContext context, int cost) {
        return false;
    }
    
    public boolean shouldAutoPlay_call_whenGainCardToCall(MoveContext context, Card gainedCard, Card[] possibleCards) {
    	for (Card c : possibleCards) {
    		if (!(c.equals(Cards.duplicate) || c.equals(Cards.estate)))
    			return false;
    	}
    	if (!Cards.isSupplyCard(gainedCard)) return true;
    	CardPile pile = context.game.getPile(gainedCard);
    	return game.isPileEmpty(gainedCard) || !gainedCard.equals(pile.topCard());
    }
    
    public boolean shouldAutoPlay_call_whenActionResolveCardToCall(MoveContext context, Card resolvedCard, Card[] possibleCards) {
    	return false;
    }
    
    public boolean shouldAutoPlay_call_whenTurnStartCardToCall(MoveContext context, Card[] possibleCards) {
    	return false;
    }
    
    public boolean shouldAutoPlay_disciple_cardToPlay(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_fugitive_cardToDiscard(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_hero_treasureToObtain(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_messenger_shouldDiscardDeck(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_messenger_cardToObtain(MoveContext context) {
    	return false;
    }

    public boolean shouldAutoPlay_miser_shouldTakeTreasure(MoveContext context) {
    	if (getMiserTreasure() == 0) {
            return true;
        }
        
        if (!hand.contains(Cards.copper)) {
            return true;
        }
        return false;
    }
    
    public boolean shouldAutoPlay_ratcatcher_cardToTrash(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_raze_shouldTrashRazePlayed(MoveContext context, Card responsible) {
    	if (getHand().size() == 0) {
    		return true;
    	}
    	return containsCardCostingAtLeast(context, getHand(), getTrashCards(), Cards.raze.getCost(context));
    }
    
    public boolean shouldAutoPlay_raze_cardToTrash(MoveContext context) {
    	return containsCardCostingAtLeast(context, getHand(), getTrashCards(), Cards.raze.getCost(context));
    }
    
    public boolean shouldAutoPlay_raze_cardToKeep(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_soldier_cardToDiscard(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_teacher_tokenTypeToMove(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_teacher_actionCardPileToHaveToken(MoveContext context, PlayerSupplyToken token) {
    	return false;
    }
    
    public boolean shouldAutoPlay_transmogrify_cardToTrash(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_transmogrify_cardToObtain(MoveContext context, int maxCost, int maxDebtCost, boolean potion) {
    	return false;
    }

    public boolean shouldAutoPlay_traveller_shouldExchange(MoveContext context, Card traveller, Card exchange) {
        return true;
    }

    public boolean shouldAutoPlay_cleanup_wineMerchantToDiscard(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_cleanup_wineMerchantEstateToDiscard(MoveContext context) {
        return true;
    }
    
    public boolean shouldAutoPlay_alms_cardToObtain(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_ball_cardToObtain(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_ferry_actionCardPileToHaveToken(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_inheritance_actionCardTosetAside(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_lostArts_actionCardPileToHaveToken(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_pathfinding_actionCardPileToHaveToken(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_pilgrimage_cardsToGain(MoveContext context) {
    	return false;
	}
    
    public boolean shouldAutoPlay_plan_actionCardPileToHaveToken(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_quest_chooseOption(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_quest_attackCardToDiscard(MoveContext context, Card[] attacks) {
    	return true;
    }
    
    public boolean shouldAutoPlay_quest_cardsToDiscard(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_save_cardToSetAside(MoveContext context) {
    	return false;
    }

    public boolean shouldAutoPlay_scoutingParty_cardToDiscard(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_seaway_cardToObtain(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_summon_cardToObtain(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_trade_cardsToTrash(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_training_actionCardPileToHaveToken(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_trashingToken_cardToTrash(MoveContext context) {
        return true;
    }

    public boolean shouldAutoPlay_extraTurn_chooseOption(MoveContext context, ExtraTurnOption[] options) {
    	return true;
    }
    
    public boolean shouldAutoPlay_arena_cardToDiscard(MoveContext context) {
    	for (Card c : context.getPlayer().getHand()) {
    		if (c.is(Type.Action, context.getPlayer()) && !c.is(Type.Treasure, context.getPlayer())) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean shouldAutoPlay_bustlingVillage_settlersIntoHand(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_catapult_cardToTrash(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_catapult_attack_cardsToKeep(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_encampment_chooseOption(MoveContext context, EncampmentOption[] options) {
    	return true;
    }
    
    public boolean shouldAutoPlay_engineer_cardToObtain(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_engineer_shouldTrashEngineerPlayed(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_hauntedCastle_gain_cardsToPutBackOnDeck(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_gladiator_revealedCard(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_gladiator_revealCopy(MoveContext context, Card card) {
    	return true;
    }
    
    public boolean shouldAutoPlay_legionary_revealGold(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_legionary_attack_cardsToKeep(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_sacrifice_cardToTrash(MoveContext context) {
    	return false;
    }
    
    public boolean shouldAutoPlay_opulentCastle_cardsToDiscard(MoveContext context) {
    	Player p = context.getPlayer();
    	for(Card c: p.getHand()) {
    		if (c.is(Type.Victory, p) && c.is(Type.Action, p)) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public boolean shouldAutoPlay_overlord_actionCardToImpersonate(MoveContext context) {
        return false;
    }
    
    public boolean shouldAutoPlay_settlers_copperIntoHand(MoveContext context) {
    	return true;
    }
    
    public boolean shouldAutoPlay_temple_cardsToTrash(MoveContext context) {
    	return false;
    }

    public boolean shouldAutoPlay_smallCastle__shouldTrashSmallCastlePlayed(MoveContext context, Card responsible) { return false; }
    public boolean shouldAutoPlay_smallCastle_castleToTrash(MoveContext context) { return false; }

    public boolean shouldAutoPlay_sprawlingCastle_chooseOption(MoveContext context) { return false; }

    public boolean shoudlAutoPlay_payoffDebt(MoveContext context) {
        //Player might not want to payoff debt for the player he is possessing.
        if (context.game.possessingPlayer == this && context.game.getCurrentPlayer() != this) {
            return false;
        }

        //Player might want to call back the wine merchant from the tavern mat.
        if (context.player.getTavern().contains(Cards.wineMerchant) && context.getCoins() >= 2 && context.getCoins() - getDebtTokenCount() < 2) {
            return false;
        }

        return true;
    }

}
