package com.vdom.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.vdom.core.CardList;
import com.vdom.core.MoveContext;
import com.vdom.core.Player;
import com.vdom.core.QuickPlayPlayer;
import com.vdom.core.Util;

/**
 * Class that you can use to play both interactively and programmatically.
 */
public class InteractivePlayer extends QuickPlayPlayer implements GameEventListener {

    public boolean quickPlay = false;
    
    @Override
    public void newGame(MoveContext context) {
    }

    @Override
    public String getPlayerName() {
        if(name == null) {
            return "You";
        }
        return name;
    }

    @Override
    public Card doAction(MoveContext context) {
        CardList hand = getHand();
        if (hand.size() == 0) {
            return null;
        }

        ArrayList<Card> actions = new ArrayList<Card>();
        for (Card card : hand) {
            if (card instanceof ActionCard) {
                actions.add(card);
            }
        }

        if (actions.size() == 0) {
            return null;
        }

        return pickACard(context, "Select Action:", actions.toArray(new Card[0]), true);
    }

    @Override
    public Card doBuy(MoveContext context) {
        Card[] cards = context.getCardsInPlay();

        ArrayList<Card> buyableCards = new ArrayList<Card>();
        for (int i = 0; i < cards.length; i++) {
            if (context.canBuy(cards[i])) {
                buyableCards.add(cards[i]);
            }
        }

        Collections.sort(buyableCards, new Comparator<Card>() {
            public int compare(Card c1, Card c2) {
                return c1.getCost() - c2.getCost();
            }
        });

        return pickACard(context, "Select Buy (" + context.getCoinAvailableForBuy() + ") Coin:", buyableCards.toArray(new Card[0]), true);
    }

    @Override
    public ArrayList<TreasureCard> treasureCardsToPlayInOrder(MoveContext context) {
        if(quickPlay && shouldAutoPlay_treasureCardsToPlayInOrder(context)) {
            return super.treasureCardsToPlayInOrder(context);
        }
        
        Card[] hand = getHand().toArray();
        if (hand.length == 0) {
            return null;
        }

        ArrayList<TreasureCard> treasures = new ArrayList<TreasureCard>();
        for (Card card : hand) {
            if (card instanceof TreasureCard) {
                treasures.add((TreasureCard) card);
            }
        }

        if (treasures.size() == 0) {
            return null;
        }

        int[] nums = null;
        while (nums == null) {
            showGetInputHeader(context);
            System.out.println("Select treasures in order:");
            System.out.println("0-All");

            for (int i = 0; i < treasures.size(); i++) {
                System.out.println("" + (i + 1) + "-" + Util.getShortText(treasures.get(i)));
            }

            nums = getInputAsIntArray(context, 0, treasures.size());
        }

        boolean quickPlay = false;
        if(nums != null) {
            for(int i : nums) {
                if(i <= 0) {
                    quickPlay = true;
                    break;
                }
            }
        }
        
        if(quickPlay) {
            return super.treasureCardsToPlayInOrder(context);
        }
        
        ArrayList<TreasureCard> rtn = new ArrayList<TreasureCard>();
        for (int i = 0; i < nums.length; i++) {
            rtn.add(treasures.get(nums[i] - 1));
        }

        return rtn;
    }

    // ////////////////////////////////////////////
    // Card interactions - cards from the base game
    // ////////////////////////////////////////////

    @Override
    public Card workshop_cardToObtain(MoveContext context) {
        if(quickPlay && shouldAutoPlay_workshop_cardToObtain(context)) {
            return super.workshop_cardToObtain(context);
        }
        
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() >= 0 && card.getCost() <= 4 && context.getCardsLeft(card) > 0) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Workshop:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card feast_cardToObtain(MoveContext context) {
        if(quickPlay && shouldAutoPlay_feast_cardToObtain(context)) {
            return super.feast_cardToObtain(context);
        }
        
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() >= 0 && card.getCost() <= 5 && context.getCardsLeft(card) > 0) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Feast:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card remodel_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_remodel_cardToTrash(context)) {
            return super.remodel_cardToTrash(context);
        }
        return pickACard(context, "Remodel:Card to trash", getHand().toArray(), true);
    }

    @Override
    public Card remodel_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        if(quickPlay && shouldAutoPlay_remodel_cardToObtain(context, maxCost, potion)) {
            return super.remodel_cardToObtain(context, maxCost, potion);
        }
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() >= 0 && card.getCost() <= maxCost && context.getCardsLeft(card) > 0 && (potion || !card.costPotion())) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Remodel:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card[] militia_attack_cardsToKeep(MoveContext context) {
        if(quickPlay && shouldAutoPlay_militia_attack_cardsToKeep(context)) {
            return super.militia_attack_cardsToKeep(context);
        }
        
        return pickCards(context, "Militia:3 cards to keep", getHand().toArray(), false);
    }

    @Override
    public boolean chancellor_shouldDiscardDeck(MoveContext context) {
        if(quickPlay && shouldAutoPlay_chancellor_shouldDiscardDeck(context)) {
            return super.chancellor_shouldDiscardDeck(context);
        }
        return pickYesNo(context, "Discard deck?");
    }

    @Override
    public TreasureCard mine_treasureFromHandToUpgrade(MoveContext context) {
        if(quickPlay && shouldAutoPlay_mine_treasureFromHandToUpgrade(context)) {
            return super.mine_treasureFromHandToUpgrade(context);
        }
        return (TreasureCard) pickACard(context, "Mine:Treasure card to upgrade", getHand().toArray(), true);
    }

    @Override
    public Card[] chapel_cardsToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_chapel_cardsToTrash(context)) {
            return super.chapel_cardsToTrash(context);
        }
        return pickCards(context, "Chapel:Up to 4 cards to trash", getHand().toArray(), true);
    }

    @Override
    public Card[] cellar_cardsToDiscard(MoveContext context) {
        if(quickPlay && shouldAutoPlay_cellar_cardsToDiscard(context)) {
            return super.cellar_cardsToDiscard(context);
        }
        return pickCards(context, "Cellar:Cards to discard", getHand().toArray(), true);
    }

    @Override
    public boolean library_shouldKeepAction(MoveContext context, ActionCard action) {
        if(quickPlay && shouldAutoPlay_library_shouldKeepAction(context, action)) {
            return super.library_shouldKeepAction(context, action);
        }
        return pickYesNo(context, "Keep " + action.getName() + "?");
    }

    @Override
    public boolean spy_shouldDiscard(MoveContext context, Player targetPlayer, Card card) {
        if(quickPlay && shouldAutoPlay_spy_shouldDiscard(context, targetPlayer, card)) {
            return super.spy_shouldDiscard(context, targetPlayer, card);
        }
        return pickYesNo(context, "Make " + targetPlayer.getPlayerName() + " discard " + card.getName() + "?");
    }

    // ////////////////////////////////////////////
    // Card interactions - cards from the Intrigue
    // ////////////////////////////////////////////

    @Override
    public Card courtyard_cardToPutBackOnDeck(MoveContext context) {
        if(quickPlay && shouldAutoPlay_courtyard_cardToPutBackOnDeck(context)) {
            return super.courtyard_cardToPutBackOnDeck(context);
        }
        
        return pickACard(context, "Courtyard:Card to put back on deck", getHand().toArray(), false);
    }

    @Override
    public Card[] secretChamber_cardsToDiscard(MoveContext context) {
        if(quickPlay && shouldAutoPlay_secretChamber_cardsToDiscard(context)) {
            return super.secretChamber_cardsToDiscard(context);
        }
        
        return pickCards(context, "Secret Chamber:Cards to discard", getHand().toArray(), true);
    }

    @Override
    public Card[] secretChamber_cardsToPutOnDeck(MoveContext context) {
        if(quickPlay && shouldAutoPlay_secretChamber_cardsToPutOnDeck(context)) {
            return super.secretChamber_cardsToPutOnDeck(context);
        }
        
        return pickCards(context, "Secret Chamber:Cards to put on top of deck", getHand().toArray(), true);
    }

    @Override
    public PawnOption[] pawn_chooseOptions(MoveContext context) {
        if(quickPlay && shouldAutoPlay_pawn_chooseOptions(context)) {
            return super.pawn_chooseOptions(context);
        }
        
        int[] nums = null;
        while (nums == null) {
            showGetInputHeader(context);
            System.out.println("Pawn:Choose 2 different options");
            System.out.println("1-Add 1 action");
            System.out.println("2-Draw 1 card");
            System.out.println("3-Add 1 coin");
            System.out.println("4-Add 1 buy");

            nums = getInputAsIntArray(context, 1, 4);
        }

        PawnOption[] options = new PawnOption[2];
        for (int i = 0; i < nums.length && i < 2; i++) {
            options[i] = nums[i] == 1 ? PawnOption.AddAction : (nums[i] == 2 ? PawnOption.AddCard : (nums[i] == 3 ? PawnOption.AddGold : PawnOption.AddBuy));
        }
        return options;
    }

    @Override
    public TorturerOption torturer_attack_chooseOption(MoveContext context) {
        if(quickPlay && shouldAutoPlay_torturer_attack_chooseOption(context)) {
            return super.torturer_attack_chooseOption(context);
        }

        int num = -1;
        while (num == -1) {
            showGetInputHeader(context);
            System.out.println("Torturer:Choose option");
            System.out.println("1-Take curse");
            System.out.println("2-Discard 2 cards");

            num = getInputAsInt(context, 1, 2);
        }

        return num == 1 ? TorturerOption.DiscardTwoCards : TorturerOption.TakeCurse;
    }

    @Override
    public StewardOption steward_chooseOption(MoveContext context) {
        if(quickPlay && shouldAutoPlay_steward_chooseOption(context)) {
            return super.steward_chooseOption(context);
        }

        int num = -1;
        while (num == -1) {
            showGetInputHeader(context);
            System.out.println("Steward:Choose option");
            System.out.println("1-Draw 2 cards");
            System.out.println("2-Add 2 coin");
            System.out.println("3-Trash 2 cards");

            num = getInputAsInt(context, 1, 3);
        }

        return num == 0 ? StewardOption.AddCards : (num == 1 ? StewardOption.AddGold : StewardOption.TrashCards);
    }

    @Override
    public Card[] steward_cardsToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_steward_cardsToTrash(context)) {
            return super.steward_cardsToTrash(context);
        }

        return pickCards(context, "Steward:Cards to trash", getHand().toArray(), true);
    }

    @Override
    public Card swindler_cardToSwitch(MoveContext context, int cost) {
        if(quickPlay && shouldAutoPlay_swindler_cardToSwitch(context, cost)) {
            return super.swindler_cardToSwitch(context, cost);
        }

        ArrayList<Card> available = new ArrayList<Card>();

        for (Card card : context.getCardsInPlay()) {
            if (card.getCost() == cost && context.getCardsLeft(card) > 0) {
                available.add(card);
            }
        }

        if (available.isEmpty()) {
            return null;
        } else {
            return pickACard(context, "Swindler:Card to switch", available.toArray(new Card[0]), false);
        }
    }

    @Override
    public Card[] torturer_attack_cardsToDiscard(MoveContext context) {
        if(quickPlay && shouldAutoPlay_torturer_attack_cardsToDiscard(context)) {
            return super.torturer_attack_cardsToDiscard(context);
        }

        return pickCards(context, "Torturer:2 cards to discard", getHand().toArray(), false);
    }

    @Override
    public boolean baron_shouldDiscardEstate(MoveContext context) {
        if(quickPlay && shouldAutoPlay_baron_shouldDiscardEstate(context)) {
            return super.baron_shouldDiscardEstate(context);
        }

        return pickYesNo(context, "Discard an Estate?");
    }

    @Override
    public Card ironworks_cardToObtain(MoveContext context) {
        if(quickPlay && shouldAutoPlay_ironworks_cardToObtain(context)) {
            return super.ironworks_cardToObtain(context);
        }

        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() >= 0 && card.getCost() <= 4 && context.getCardsLeft(card) > 0) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Ironworks:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card masquerade_cardToPass(MoveContext context) {
        if(quickPlay && shouldAutoPlay_masquerade_cardToPass(context)) {
            return super.masquerade_cardToPass(context);
        }

        return pickACard(context, "Masquerade:Card to pass", getHand().toArray(), false);
    }

    @Override
    public Card masquerade_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_masquerade_cardToTrash(context)) {
            return super.masquerade_cardToTrash(context);
        }

        return pickACard(context, "Masquerade:Card to trash", getHand().toArray(), true);
    }

    @Override
    public boolean miningVillage_shouldTrashMiningVillage(MoveContext context) {
        if(quickPlay && shouldAutoPlay_miningVillage_shouldTrashMiningVillage(context)) {
            return super.miningVillage_shouldTrashMiningVillage(context);
        }

        return pickYesNo(context, "Trash the Mining Village?");
    }

    @Override
    public Card saboteur_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        if(quickPlay && shouldAutoPlay_saboteur_cardToObtain(context, maxCost, potion)) {
            return super.saboteur_cardToObtain(context, maxCost, potion);
        }

        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() >= 0 && card.getCost() <= maxCost && context.getCardsLeft(card) > 0 && (potion || !card.costPotion())) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Saboteur:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card[] scout_orderCards(MoveContext context, Card[] cards) {
        if(quickPlay && shouldAutoPlay_scout_orderCards(context, cards)) {
            return super.scout_orderCards(context, cards);
        }

        return pickCards(context, "Scout:Order cards to put back on top of deck", cards, false);
    }

    @Override
    public NoblesOption nobles_chooseOptions(MoveContext context) {
        if(quickPlay && shouldAutoPlay_nobles_chooseOptions(context)) {
            return super.nobles_chooseOptions(context);
        }

        int num = -1;
        while (num == -1) {
            showGetInputHeader(context);
            System.out.println("Nobles:Choose option");
            System.out.println("1-Add 2 actions");
            System.out.println("2-Draw 3 cards");

            num = getInputAsInt(context, 1, 2);
        }

        return num == 1 ? NoblesOption.AddActions : NoblesOption.AddCards;
    }

    @Override
    public Card[] tradingPost_cardsToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_tradingPost_cardsToTrash(context)) {
            return super.tradingPost_cardsToTrash(context);
        }

        return pickCards(context, "Trading Post:2 cards to trash", getHand().toArray(), false);
    }

    @Override
    public Card wishingWell_cardGuess(MoveContext context) {
        if(quickPlay && shouldAutoPlay_wishingWell_cardGuess(context)) {
            return super.wishingWell_cardGuess(context);
        }

        return pickACard(context, "Wishing Well:Guess next card", context.getCardsInPlay(), true);
    }

    @Override
    public Card upgrade_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_upgrade_cardToTrash(context)) {
            return super.upgrade_cardToTrash(context);
        }

        return pickACard(context, "Upgrade:Card to trash", getHand().toArray(), true);
    }

    @Override
    public Card upgrade_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        if(quickPlay && shouldAutoPlay_upgrade_cardToObtain(context, exactCost, potion)) {
            return super.upgrade_cardToObtain(context, exactCost, potion);
        }

        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() == exactCost && context.getCardsLeft(card) > 0 && card.costPotion() == potion) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Upgrade:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public MinionOption minion_chooseOption(MoveContext context) {
        if(quickPlay && shouldAutoPlay_minion_chooseOption(context)) {
            return super.minion_chooseOption(context);
        }

        int num = -1;
        while (num == -1) {
            showGetInputHeader(context);
            System.out.println("Minion:Choose option");
            System.out.println("1-Add 2 coin");
            System.out.println("2-Discard hand to draw 4 cards");

            num = getInputAsInt(context, 1, 2);
        }

        return num == 1 ? MinionOption.AddGold : MinionOption.RolloverCards;
    }

    // ////////////////////////////////////////////
    // Card interactions - cards from the Seaside
    // ////////////////////////////////////////////

    @Override
    public boolean nativeVillage_takeCards(MoveContext context) {
        if(quickPlay && shouldAutoPlay_nativeVillage_takeCards(context)) {
            return super.nativeVillage_takeCards(context);
        }
        
        boolean takeCards = false;
        int num = -1;
        while (num == -1) {
            showGetInputHeader(context);
            // TODO shouldn't access Util here, its not safe
            System.out.println("Native Village:" + Util.cardArrayToString(getNativeVillage()));
            System.out.println("1-Add card");
            System.out.println("2-Take cards");

            num = getInputAsInt(context, 1, 2);

            if (num == 1) {
                takeCards = false;
            } else if (num == 2) {
                takeCards = true;
            }
        }

        return takeCards;
    }

    @Override
    public Card[] warehouse_cardsToDiscard(MoveContext context) {
        if(quickPlay && shouldAutoPlay_warehouse_cardsToDiscard(context)) {
            return super.warehouse_cardsToDiscard(context);
        }
        
        ArrayList<Card> handCopy = new ArrayList<Card>();
        for (Card card : getHand().toArray()) {
            handCopy.add(card);
        }
        Card[] cards = new Card[3];

        for (int i = 0; i < 3; i++) {
            Card card = pickACard(context, "Warehouse:Card to discard", handCopy.toArray(new Card[0]), false);
            handCopy.remove(card);
            cards[i] = card;
        }

        return cards;
    }

    @Override
    public Card lookout_cardToTrash(MoveContext context, Card[] cards) {
        if(quickPlay && shouldAutoPlay_lookout_cardToTrash(context, cards)) {
            return super.lookout_cardToTrash(context, cards);
        }
        
        return pickACard(context, "Lookout:Card to trash", cards, false);
    }

    @Override
    public Card lookout_cardToDiscard(MoveContext context, Card[] cards) {
        if(quickPlay && shouldAutoPlay_lookout_cardToDiscard(context, cards)) {
            return super.lookout_cardToDiscard(context, cards);
        }
        
        return pickACard(context, "Lookout:Card to discard", cards, false);
    }

    @Override
    public Card[] ghostShip_attack_cardsToPutBackOnDeck(MoveContext context) {
        if(quickPlay && shouldAutoPlay_ghostShip_attack_cardsToPutBackOnDeck(context)) {
            return super.ghostShip_attack_cardsToPutBackOnDeck(context);
        }
        
        // TODO Auto-generated method stub
        return super.ghostShip_attack_cardsToPutBackOnDeck(context);
    }

    @Override
    public Card salvager_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_salvager_cardToTrash(context)) {
            return super.salvager_cardToTrash(context);
        }
        
        return pickACard(context, "Salvager:Card to trash", getHand().toArray(), false);
    }

    @Override
    public int treasury_putBackOnDeck(MoveContext context, int treasuryCardsInPlay) {
        if(quickPlay && shouldAutoPlay_treasury_putBackOnDeck(context, treasuryCardsInPlay)) {
            return super.treasury_putBackOnDeck(context, treasuryCardsInPlay);
        }
        
        System.out.println();
        System.out.println("Number of treasury cards to put on top of deck?");
        return getInputAsInt(context, 0, treasuryCardsInPlay);
    }

    @Override
    public boolean pirateShip_takeTreasure(MoveContext context) {
        if(quickPlay && shouldAutoPlay_pirateShip_takeTreasure(context)) {
            return super.pirateShip_takeTreasure(context);
        }
        
        return pickYesNo(context, "Take pirate treasure?");
    }

    @Override
    public Card smugglers_cardToObtain(MoveContext context) {
        if(quickPlay && shouldAutoPlay_smugglers_cardToObtain(context)) {
            return super.smugglers_cardToObtain(context);
        }
        
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsObtainedByLastPlayer()) {
            if (card.getCost() <= 6 && context.getCardsLeft(card) > 0) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Smugglers:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card island_cardToSetAside(MoveContext context) {
        if(quickPlay && shouldAutoPlay_island_cardToSetAside(context)) {
            return super.island_cardToSetAside(context);
        }
        
        if (getHand().toArray().length > 0) {
            return pickACard(context, "Island:Card to set aside:", getHand().toArray(), false);
        } else {
            return null;
        }
    }

    @Override
    public Card haven_cardToSetAside(MoveContext context) {
        if(quickPlay && shouldAutoPlay_haven_cardToSetAside(context)) {
            return super.haven_cardToSetAside(context);
        }
        
        if (getHand().toArray().length > 0) {
            return pickACard(context, "Haven:Card to set aside:", getHand().toArray(), false);
        } else {
            return null;
        }
    }

    @Override
    public boolean navigator_shouldDiscardTopCards(MoveContext context, Card[] cards) {
        if(quickPlay && shouldAutoPlay_navigator_shouldDiscardTopCards(context, cards)) {
            return super.navigator_shouldDiscardTopCards(context, cards);
        }
        
        String header = "Discard ";

        for (int i = 0; i < cards.length; i++) {
            header += (i > 0 ? ", " : "") + cards[i].getName();
        }

        return pickYesNo(context, header + "?");
    }

    @Override
    public Card[] navigator_cardOrder(MoveContext context, Card[] cards) {
        if(quickPlay && shouldAutoPlay_navigator_cardOrder(context, cards)) {
            return super.navigator_cardOrder(context, cards);
        }
        
        return pickCards(context, "Navigator:Order cards to put back on top of deck", cards, false);
    }

    @Override
    public Card embargo_supplyToEmbargo(MoveContext context) {
        if(quickPlay && shouldAutoPlay_embargo_supplyToEmbargo(context)) {
            return super.embargo_supplyToEmbargo(context);
        }
        
        ArrayList<Card> canEmbargo = new ArrayList<Card>();

        for (Card card : context.getCardsObtainedByLastPlayer()) {
            if (context.getCardsLeft(card) > 0) {
                canEmbargo.add(card);
            }
        }

        return pickACard(context, "Embargo:Card to embargo", canEmbargo.toArray(new Card[0]), false);
    }

    @Override
    public Card ambassador_revealedCard(MoveContext context) {
        if(quickPlay && shouldAutoPlay_ambassador_revealedCard(context)) {
            return super.ambassador_revealedCard(context);
        }
        
        if (getHand().toArray().length > 0) {
            return pickACard(context, "Ambassador:Card to reveal", getHand().toArray(), false);
        } else {
            return null;
        }
    }

    @Override
    public int ambassador_returnToSupplyFromHand(MoveContext context, Card card) {
        if(quickPlay && shouldAutoPlay_ambassador_returnToSupplyFromHand(context, card)) {
            return super.ambassador_returnToSupplyFromHand(context, card);
        }
        
        int max = 0;
        for (Card c : getHand().toArray()) {
            if (c.equals(card)) {
                max++;
            }
        }

        System.out.println();
        System.out.println("Ambassador:Number of " + card.getName() + " to return to supply");
        return getInputAsInt(context, 1, max);
    }

    @Override
    public boolean pearlDiver_shouldMoveToTop(MoveContext context, Card card) {
        if(quickPlay && shouldAutoPlay_pearlDiver_shouldMoveToTop(context, card)) {
            return super.pearlDiver_shouldMoveToTop(context, card);
        }
        
        return pickYesNo(context, "Put " + card.getName() + " on top of deck?");
    }

    // ////////////////////////////////////////////
    // Card interactions - cards from the Alchemy
    // ////////////////////////////////////////////

    @Override
    public ActionCard university_actionCardToObtain(MoveContext context) {
        if(quickPlay && shouldAutoPlay_university_actionCardToObtain(context)) {
            return super.university_actionCardToObtain(context);
        }
        
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card instanceof ActionCard && card.getCost() <= 5) {
                canObtain.add(card);
            }
        }

        return (ActionCard) pickACard(context, "University:Action card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card apprentice_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_apprentice_cardToTrash(context)) {
            return super.apprentice_cardToTrash(context);
        }
        
        if (getHand().toArray().length > 0) {
            return pickACard(context, "Apprentice:Card to trash", getHand().toArray(), false);
        } else {
            return null;
        }
    }

    @Override
    public Card transmute_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_transmute_cardToTrash(context)) {
            return super.transmute_cardToTrash(context);
        }
        
        if (getHand().toArray().length > 0) {
            return pickACard(context, "Transmute:Card to trash", getHand().toArray(), false);
        } else {
            return null;
        }
    }

    @Override
    public boolean alchemist_backOnDeck(MoveContext context) {
        if(quickPlay && shouldAutoPlay_alchemist_backOnDeck(context)) {
            return super.alchemist_backOnDeck(context);
        }
        
        return pickYesNo(context, "Put alchemist on top of deck?");
    }

    @Override
    public TreasureCard herbalist_backOnDeck(MoveContext context, TreasureCard[] cards) {
        if(quickPlay && shouldAutoPlay_herbalist_backOnDeck(context, cards)) {
            return super.herbalist_backOnDeck(context, cards);
        }
        
        ArrayList<Card> treasuresPlayed = new ArrayList<Card>();

        for (Card card : cards) {
            treasuresPlayed.add(card);
        }

        if (treasuresPlayed.isEmpty()) {
            return null;
        } else {
            return (TreasureCard) pickACard(context, "Herbalist:Treasure to put on top of deck", treasuresPlayed.toArray(new Card[0]), true);
        }
    }

    @Override
    public ArrayList<Card> apothecary_cardsForDeck(MoveContext context, ArrayList<Card> cards) {
        if(quickPlay && shouldAutoPlay_apothecary_cardsForDeck(context, cards)) {
            return super.apothecary_cardsForDeck(context, cards);
        }
        
        //TODO: Finish out apothecary put asking the user for card order here...
        return cards;
    }

    @Override
    public ActionCard[] golem_cardOrder(MoveContext context, ActionCard[] cards) {
        if(quickPlay && shouldAutoPlay_golem_cardOrder(context, cards)) {
            return super.golem_cardOrder(context, cards);
        }
        
        return (ActionCard[]) pickCards(context, "Golem:Order cards", cards, false);
    }

    // ////////////////////////////////////////////
    // Card interactions - cards from the Prosperity
    // ////////////////////////////////////////////

    @Override
    public Card bishop_cardToTrashForVictoryTokens(MoveContext context) {
        if(quickPlay && shouldAutoPlay_bishop_cardToTrashForVictoryTokens(context)) {
            return super.bishop_cardToTrashForVictoryTokens(context);
        }
        
        if (getHand().toArray().length > 0) {
            return pickACard(context, "Bishop:Card to trash for victory tokens", getHand().toArray(), false);
        } else {
            return null;
        }
    }

    @Override
    public Card bishop_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_bishop_cardToTrash(context)) {
            return super.bishop_cardToTrash(context);
        }
        
        return pickACard(context, "Bishop:Card to trash", getHand().toArray(), true);
    }

    @Override
    public Card contraband_cardPlayerCantBuy(MoveContext context) {
        if(quickPlay && shouldAutoPlay_contraband_cardPlayerCantBuy(context)) {
            return super.contraband_cardPlayerCantBuy(context);
        }
        
        return pickACard(context, "Contraband:Card " + context.getPlayer()
            .getPlayerName() + " can't buy", context.getCardsInPlayOrderByCost(), true);
    }

    @Override
    public Card expand_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_expand_cardToTrash(context)) {
            return super.expand_cardToTrash(context);
        }
        
        if (getHand().toArray().length > 0) {
            return pickACard(context, "Expand:Card to trash", getHand().toArray(), false);
        } else {
            return null;
        }
    }

    @Override
    public Card expand_cardToObtain(MoveContext context, int maxCost, boolean potion) {
        if(quickPlay && shouldAutoPlay_expand_cardToObtain(context, maxCost, potion)) {
            return super.expand_cardToObtain(context, maxCost, potion);
        }
        
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() >= 0 && card.getCost() <= maxCost && context.getCardsLeft(card) > 0 && (potion || !card.costPotion())) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Expand:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card[] forge_cardsToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_forge_cardsToTrash(context)) {
            return super.forge_cardsToTrash(context);
        }
        
        return pickCards(context, "Forge:Cards to trash", getHand().toArray(), true);
    }

    @Override
    public Card forge_cardToObtain(MoveContext context, int exactCost) {
        if(quickPlay && shouldAutoPlay_forge_cardToObtain(context, exactCost)) {
            return super.forge_cardToObtain(context, exactCost);
        }
        
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() >= exactCost && card.getCost() <= exactCost && context.getCardsLeft(card) > 0) {
                canObtain.add(card);
            }
        }

        if (canObtain.isEmpty()) {
            return null;
        } else {
            return pickACard(context, "Forge:Card to obtain", canObtain.toArray(new Card[0]), false);
        }
    }

    @Override
    public Card[] goons_attack_cardsToKeep(MoveContext context) {
        if(quickPlay && shouldAutoPlay_goons_attack_cardsToKeep(context)) {
            return super.goons_attack_cardsToKeep(context);
        }
        
        return pickCards(context, "Goons:3 cards to keep", getHand().toArray(), false);
    }

    @Override
    public ActionCard kingsCourt_cardToPlay(MoveContext context) {
        if(quickPlay && shouldAutoPlay_kingsCourt_cardToPlay(context)) {
            return super.kingsCourt_cardToPlay(context);
        }
        
        ArrayList<Card> actions = new ArrayList<Card>();
        for (Card card : getHand().toArray()) {
            if (card instanceof ActionCard) {
                actions.add(card);
            }
        }

        if (actions.isEmpty()) {
            return null;
        } else {
            return (ActionCard) pickACard(context, "King's Court:Action to play 3 times", actions.toArray(new Card[0]), false);
        }
    }

    @Override
    public boolean loan_shouldTrashTreasure(MoveContext context, TreasureCard treasure) {
        if(quickPlay && shouldAutoPlay_loan_shouldTrashTreasure(context, treasure)) {
            return super.loan_shouldTrashTreasure(context, treasure);
        }
        
        return pickYesNo(context, "Loan:Trash the " + treasure.getName() + "?");
    }

    @Override
    public TreasureCard mint_treasureToMint(MoveContext context) {
        if(quickPlay && shouldAutoPlay_mint_treasureToMint(context)) {
            return super.mint_treasureToMint(context);
        }
        
        ArrayList<Card> canMint = new ArrayList<Card>();
        for (Card card : getHand().toArray()) {
            if (card instanceof TreasureCard && context.getCardsLeft(card) > 0) {
                canMint.add(card);
            }
        }

        if (canMint.isEmpty()) {
            return null;
        } else {
            return (TreasureCard) pickACard(context, "Mint:Treasure to copy", canMint.toArray(new Card[0]), false);
        }
    }

    @Override
    public boolean mountebank_attack_shouldDiscardCurse(MoveContext context) {
        if(quickPlay && shouldAutoPlay_mountebank_attack_shouldDiscardCurse(context)) {
            return super.mountebank_attack_shouldDiscardCurse(context);
        }
        
        return pickYesNo(context, "Discard curse?");
    }

    @Override
    public Card[] rabble_attack_cardOrder(MoveContext context, Card[] cards) {
        if(quickPlay && shouldAutoPlay_rabble_attack_cardOrder(context, cards)) {
            return super.rabble_attack_cardOrder(context, cards);
        }
        
        return pickCards(context, "Rabble:Order cards", cards, false);
    }

    @Override
    public boolean royalSeal_shouldPutCardOnDeck(MoveContext context, Card card) {
        if(quickPlay && shouldAutoPlay_royalSeal_shouldPutCardOnDeck(context, card)) {
            return super.royalSeal_shouldPutCardOnDeck(context, card);
        }
        
        return pickYesNo(context, "Put " + card.getName() + " on top of deck?");
    }

    @Override
    public Card tradeRoute_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_tradeRoute_cardToTrash(context)) {
            return super.tradeRoute_cardToTrash(context);
        }
        
        if (getHand().toArray().length > 0) {
            return pickACard(context, "Trade Route:Card to trash", getHand().toArray(), false);
        } else {
            return null;
        }
    }

    @Override
    public Card[] vault_cardsToDiscardForGold(MoveContext context) {
        if(quickPlay && shouldAutoPlay_vault_cardsToDiscardForGold(context)) {
            return super.vault_cardsToDiscardForGold(context);
        }
        
        Card[] hand = getHand().toArray();
        if (hand.length == 0) {
            return null;
        }

        return pickCards(context, "Vault:Cards to discard for coin", hand, true);
    }

    @Override
    public Card[] vault_cardsToDiscardForCard(MoveContext context) {
        if(quickPlay && shouldAutoPlay_vault_cardsToDiscardForCard(context)) {
            return super.vault_cardsToDiscardForCard(context);
        }
        
        Card[] hand = getHand().toArray();
        if (hand.length == 0) {
            return null;
        }

        return pickCards(context, "Vault:2 cards to discard to draw 1", hand, true);
    }

    @Override
    public WatchTowerOption watchTower_chooseOption(MoveContext context, Card card) {
        if(quickPlay && shouldAutoPlay_watchTower_chooseOption(context, card)) {
            return super.watchTower_chooseOption(context, card);
        }
        
        return super.watchTower_chooseOption(context, card);
    }

    @Override
    public Card hamlet_cardToDiscardForAction(MoveContext context) {
        if(quickPlay && shouldAutoPlay_hamlet_cardToDiscardForAction(context)) {
            return super.hamlet_cardToDiscardForAction(context);
        }
        
        Card[] hand = getHand().toArray();
        if (hand.length == 0) {
            return null;
        }

        return pickACard(context, "Hamlet:Card to discard for +1 Action", hand, true);
    }

    @Override
    public Card hamlet_cardToDiscardForBuy(MoveContext context) {
        if(quickPlay && shouldAutoPlay_hamlet_cardToDiscardForBuy(context)) {
            return super.hamlet_cardToDiscardForBuy(context);
        }
        
        Card[] hand = getHand().toArray();
        if (hand.length == 0) {
            return null;
        }

        return pickACard(context, "Hamlet:Card to discard for +1 Buy", hand, true);
    }

    @Override
    public Card hornOfPlenty_cardToObtain(MoveContext context, int maxCost) {
        if(quickPlay && shouldAutoPlay_hornOfPlenty_cardToObtain(context, maxCost)) {
            return super.hornOfPlenty_cardToObtain(context, maxCost);
        }
        
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlay()) {
            if (card.getCost() >= 0 && card.getCost() <= maxCost && context.getCardsLeft(card) > 0) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Horn of Plenty:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public Card[] horseTraders_cardsToDiscard(MoveContext context) {
        if(quickPlay && shouldAutoPlay_horseTraders_cardsToDiscard(context)) {
            return super.horseTraders_cardsToDiscard(context);
        }
        
        return pickCards(context, "Horse Traders:2 cards to discard", getHand().toArray(), false);
    }

    @Override
    public JesterOption jester_chooseOption(MoveContext context, Player targetPlayer, Card card) {
        if(quickPlay && shouldAutoPlay_jester_chooseOption(context, targetPlayer, card)) {
            return super.jester_chooseOption(context, targetPlayer, card);
        }
        
        int num = -1;
        while (num == -1) {
            showGetInputHeader(context);
            System.out.println("Jester:choose option for " + card.getName());
            System.out.println("1-Gain copy for you");
            System.out.println("2-Give copy to " + targetPlayer.getPlayerName());

            num = getInputAsInt(context, 1, 2);
        }

        return num == 1 ? JesterOption.GainCopy : JesterOption.GiveCopy;
    }

    @Override
    public Card remake_cardToTrash(MoveContext context) {
        if(quickPlay && shouldAutoPlay_remake_cardToTrash(context)) {
            return super.remake_cardToTrash(context);
        }
        
        return pickACard(context, "Remake:Card to trash", getHand().toArray(), false);
    }

    @Override
    public Card remake_cardToObtain(MoveContext context, int exactCost, boolean potion) {
        if(quickPlay && shouldAutoPlay_remake_cardToObtain(context, exactCost, potion)) {
            return super.remake_cardToObtain(context, exactCost, potion);
        }
        
        ArrayList<Card> canObtain = new ArrayList<Card>();

        for (Card card : context.getCardsInPlayOrderByCost()) {
            if (card.getCost() >= 0 && card.getCost() == exactCost && context.getCardsLeft(card) > 0 && card.costPotion() == potion) {
                canObtain.add(card);
            }
        }

        return pickACard(context, "Remake:Card to obtain", canObtain.toArray(new Card[0]), true);
    }

    @Override
    public TournamentOption tournament_chooseOption(MoveContext context) {
        if(quickPlay && shouldAutoPlay_tournament_chooseOption(context)) {
            return super.tournament_chooseOption(context);
        }
        
        String availablePrizes = "";
        ArrayList<Card> prizes = new ArrayList<Card>();
        for (Card card : context.getCardsInPlay()) {
            if (card.isPrize() && context.getPileSize(card) > 0) {
                prizes.add(card);
            }
        }
        
        for (Card prize : prizes) {
            availablePrizes += (availablePrizes.length() > 0 ? ", " : "") + prize.getName();
        }

        int num = -1;
        while (num == -1) {
            showGetInputHeader(context);
            System.out.println("Tournament:choose option");
            System.out.println("0-Don't reveal Province");
            System.out.println("1-Gain Prize (" + availablePrizes + " left)");
            System.out.println("2-Gain Duchy (" + context.getCardsLeft(Cards.duchy) + " left)");

            num = getInputAsInt(context, 0, 2);
        }

        return num == 0 ? TournamentOption.DontRevealProvince : (num == 1 ? TournamentOption.GainPrize : TournamentOption.GainDuchy);
    }

    @Override
    public Card tournament_choosePrize(MoveContext context) {
        if(quickPlay && shouldAutoPlay_tournament_choosePrize(context)) {
            return super.tournament_choosePrize(context);
        }
        
        ArrayList<Card> prizes = new ArrayList<Card>();
        for (Card card : context.getCardsInPlay()) {
            if (card.isPrize() && context.getPileSize(card) > 0) {
                prizes.add(card);
            }
        }

        return pickACard(context, "Tournament:choose prize", prizes.toArray(new Card[0]), true);
    }

    @Override
    public Card[] youngWitch_cardsToDiscard(MoveContext context) {
        if(quickPlay && shouldAutoPlay_youngWitch_cardsToDiscard(context)) {
            return super.youngWitch_cardsToDiscard(context);
        }
        
        return pickCards(context, "Young Witch:2 cards to discard", getHand().toArray(), false);
    }

    @Override
    public Card[] followers_attack_cardsToKeep(MoveContext context) {
        if(quickPlay && shouldAutoPlay_followers_attack_cardsToKeep(context)) {
            return super.followers_attack_cardsToKeep(context);
        }
        
        return pickCards(context, "Followers:3 cards to keep", getHand().toArray(), false);
    }

    @Override
    public TrustySteedOption[] trustySteed_chooseOptions(MoveContext context) {
        if(quickPlay && shouldAutoPlay_trustySteed_chooseOptions(context)) {
            return super.trustySteed_chooseOptions(context);
        }
        
        int[] nums = null;
        while (nums == null) {
            showGetInputHeader(context);
            System.out.println("Trusty Steed:Choose 2 different options");
            System.out.println("1-Add 2 actions");
            System.out.println("2-Draw 2 cards");
            System.out.println("3-Add 2 coins");
            System.out.println("4-Gain 4 Silvers and move deck to discard pile");

            nums = getInputAsIntArray(context, 1, 4);
        }

        TrustySteedOption[] options = new TrustySteedOption[2];
        for (int i = 0; i < nums.length && i < 2; i++) {
            options[i] = nums[i] == 1 ? TrustySteedOption.AddActions : (nums[i] == 2 ? TrustySteedOption.AddCards : (nums[i] == 3 ? TrustySteedOption.AddGold
                : TrustySteedOption.GainSilvers));
        }
        return options;
    }

    // ////////////////////////
    // Helper Methods
    // ////////////////////////

    public static int getInputAsInt(MoveContext context, int low, int high) {
        // TODO shouldn't access Util here, its not safe
        String choice = Util.getInput(context, context == null?null:context.getPlayer());

        if (choice == null) {
            return -1;
        }
        
        int num;
        try {
            num = Integer.parseInt(choice);
        } catch (NumberFormatException e) {
            num = -1;
        }

        if (num < low || num > high) {
            num = -1;
        }

        if (num == -1) {
            System.out.println("** Invalid **");
        }

        return num;
    }

    public int[] getInputAsIntArray(MoveContext context, int low, int high) {
        // TODO shouldn't access Util here, its not safe
        String choices = Util.getInput(context, context.getPlayer());

        if (choices == null) {
            return null;
        }

        String[] choiceArray = choices.split(",");
        int[] nums = new int[choiceArray.length];

        for (int i = 0; i < choiceArray.length; i++) {
            int num;
            try {
                num = Integer.parseInt(choiceArray[i]);
            } catch (NumberFormatException e) {
                num = -1;
            }

            if (num < low || num > high) {
                num = -1;
            }

            if (num == -1) {
                System.out.println("** Invalid **");
                return null;
            }

            nums[i] = num;
        }

        return nums;
    }

    protected void showGetInputHeader(MoveContext context) {
        System.out.println();
    }

    protected boolean pickYesNo(MoveContext context, String header) {
        int num = -1;
        while (num == -1) {
            showGetInputHeader(context);
            System.out.println(header);
            System.out.println("0-No");
            System.out.println("1-Yes");

            num = getInputAsInt(context, 0, 1);
        }

        return num == 1;
    }

    protected Card pickACard(MoveContext context, String header, Card[] cards, boolean allowNull) {
        int num = -1;
        Card card = null;
        while (num == -1) {
            showGetInputHeader(context);
            System.out.println(header);

            if (allowNull) {
                System.out.println("0-<Pass>");
            }

            for (int i = 0; i < cards.length; i++) {
                // TODO shouldn't access Util here, its not safe
                System.out.println("" + (i + 1) + "-" + Util.getShortText(cards[i]));
            }

            int start;
            if (allowNull) {
                start = 0;
            } else {
                start = 1;
            }

            num = getInputAsInt(context, start, cards.length);

            if (num > 0) {
                card = cards[num - 1];
            }
        }

        return card;
    }

    protected Card[] pickCards(MoveContext context, String header, Card[] cards, boolean allowNull) {
        int[] nums = null;
        while (nums == null) {
            showGetInputHeader(context);
            System.out.println(header);

            if (allowNull) {
                System.out.println("0-<Pass>");
            }

            for (int i = 0; i < cards.length; i++) {
                // TODO shouldn't access Util here, its not safe
                System.out.println("" + (i + 1) + "-" + Util.getShortText(cards[i]));
            }

            int start;
            if (allowNull) {
                start = 0;
            } else {
                start = 1;
            }

            nums = getInputAsIntArray(context, start, cards.length);
        }

        Card[] rtn = new Card[nums.length];
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] == 0) {
                return null;
            }
            rtn[i] = cards[nums[i] - 1];
        }

        return rtn;
    }
}
