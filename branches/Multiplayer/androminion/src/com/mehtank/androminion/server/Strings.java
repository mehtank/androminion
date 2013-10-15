package com.mehtank.androminion.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;

import com.mehtank.androminion.R;
import com.vdom.api.Card;
import com.vdom.api.GameType;
import com.vdom.comms.SelectCardOptions;
import com.vdom.comms.SelectCardOptions.ActionType;
import com.vdom.comms.SelectCardOptions.PickType;
import com.vdom.core.Cards;
import com.vdom.core.Player.CountFirstOption;
import com.vdom.core.Player.CountSecondOption;
import com.vdom.core.Player.GovernorOption;
import com.vdom.core.Player.GraverobberOption;
import com.vdom.core.Player.HuntingGroundsOption;
import com.vdom.core.Player.JesterOption;
import com.vdom.core.Player.MinionOption;
import com.vdom.core.Player.NoblesOption;
import com.vdom.core.Player.PawnOption;
import com.vdom.core.Player.SpiceMerchantOption;
import com.vdom.core.Player.SquireOption;
import com.vdom.core.Player.StewardOption;
import com.vdom.core.Player.TorturerOption;
import com.vdom.core.Player.TournamentOption;
import com.vdom.core.Player.TrustySteedOption;
import com.vdom.core.Player.WatchTowerOption;

public class Strings {
    @SuppressWarnings("unused")
    private static final String TAG = "Strings";

    static HashMap<Card, String> nameCache = new HashMap<Card, String>();
    static HashMap<Card, String> descriptionCache = new HashMap<Card, String>();
    static HashMap<String, String> expansionCache = new HashMap<String, String>();
    static HashMap<GameType, String> gametypeCache = new HashMap<GameType, String>();
    public static Context context;

    public static String getCardName(Card c) {
        String name = nameCache.get(c);
        if(name == null) {
            try {
                Resources r = context.getResources();
                int id = r.getIdentifier(c.getSafeName() + "_name", "string", context.getPackageName());
                name = r.getString(id);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            if(name == null) {
                name = c.getName();
            }

            nameCache.put(c, name);
        }
        return name;
    }

    public static String getCardDescription(Card c) {
        String description = descriptionCache.get(c);
        if(description == null) {
            try {
                Resources r = context.getResources();
                int id = r.getIdentifier(c.getSafeName() + "_desc", "string", context.getPackageName());
                description = r.getString(id);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            if(description == null) {
                description = c.getDescription();
            }

            descriptionCache.put(c, description);
        }
        return description;
    }

    public static String getCardExpansion(Card c) {
        if (c.getExpansion().isEmpty()) {
            // Victory cards (e.g. "Duchy") don't have a single expansion;
            // they're both in Base and Intrigue.
            return "";
        }

        String expansion = expansionCache.get(c.getExpansion());

        if (expansion == null) {
            try {
                Resources r = context.getResources();
                int id = r.getIdentifier(c.getExpansion(), "string", context.getPackageName());
                expansion = r.getString(id);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            if(expansion.equals(""))
            {
                expansion = c.getExpansion();
            }

            expansionCache.put(c.getExpansion(), expansion);
        }
        return expansion;
    }

    public static String getGameTypeName(GameType g) {

        String gametype = gametypeCache.get(g);
        if (gametype==null){
            try {
                Resources r = context.getResources();
                int id = r.getIdentifier(g.name() + "_gametype", "string", context.getPackageName());
                gametype = r.getString(id);
            }
            catch(Exception e) {
                //e.printStackTrace();
            }
        }
        if (gametype == null){
            //          Fallback is the name in the enumeration
            gametype = g.getName();
        }

        gametypeCache.put(g, gametype);
        return gametype;
    }

    public static GameType getGameTypefromName(String s){

        for (GameType g : GameType.values()) {
            if (getGameTypeName(g).equals(s))
            { return g; }
        }
        return null;
    }


    public static String format(String str, Object... args) {
        return String.format(str, args);
    }

    public static String format(int resId, Object... args) {
        return String.format(context.getString(resId), args);
    }

    public static String getString(int resId) {
        return context.getString(resId);
    }

    /**
     * Takes an option object (TODO(matt): make a class that's more restrictive than Object that
     * these options can inherit from) and returns the string the corresponds to the option.
     */
    public static String getOptionText(Object option) {
        if (option instanceof SpiceMerchantOption) {
            // Actually, if this works without a cast, and it appears that it does, we don't need
            // the outer if-statements.  But maybe this way is a little more organized?  Or we
            // could do an outer switch?  Would that be any faster?
            if (option == SpiceMerchantOption.AddCardsAndAction) {
                return getString(R.string.spice_merchant_option_one);
            } else if (option == SpiceMerchantOption.AddGoldAndBuy) {
                return getString(R.string.spice_merchant_option_two);
            }
        } else if (option instanceof TorturerOption) {
            if (option == TorturerOption.TakeCurse) {
                return getString(R.string.torturer_option_one);
            } else if (option == TorturerOption.DiscardTwoCards) {
                return getString(R.string.torturer_option_two);
            }
        } else if (option instanceof StewardOption) {
            if (option == StewardOption.AddCards) {
                return getString(R.string.steward_option_one);
            } else if (option == StewardOption.AddGold) {
                return getString(R.string.steward_option_two);
            } else if (option == StewardOption.TrashCards) {
                return getString(R.string.steward_option_three);
            }
        } else if (option instanceof NoblesOption) {
            if (option == NoblesOption.AddCards) {
                return getString(R.string.nobles_option_one);
            } else if (option == NoblesOption.AddActions) {
                return getString(R.string.nobles_option_two);
            }
        } else if (option instanceof MinionOption) {
            if (option == MinionOption.AddGold) {
                return getString(R.string.minion_option_one);
            } else if (option == MinionOption.RolloverCards) {
                return getString(R.string.minion_option_two);
            }
        } else if (option instanceof WatchTowerOption) {
            if (option == WatchTowerOption.Normal) {
                return getString(R.string.watch_tower_option_one);
            } else if (option == WatchTowerOption.Trash) {
                return getString(R.string.trash);
            } else if (option == WatchTowerOption.TopOfDeck) {
                return getString(R.string.watch_tower_option_three);
            }
        } else if (option instanceof JesterOption) {
            if (option == JesterOption.GainCopy) {
                return getString(R.string.jester_option_one);
            } else if (option == JesterOption.GiveCopy) {
                /*
                 * TODO(matt): figure out the right API to make this work
                 return format(R.string.jester_option_two, targetPlayer.getPlayerName());
                 */
            }
        } else if (option instanceof TournamentOption) {
            if (option == TournamentOption.GainPrize) {
                return getString(R.string.tournament_option_two);
            } else if (option == TournamentOption.GainDuchy) {
                return getString(R.string.tournament_option_three);
            }
        } else if (option instanceof TrustySteedOption) {
            if (option == TrustySteedOption.AddActions) {
                return getString(R.string.trusty_steed_option_one);
            } else if (option == TrustySteedOption.AddCards) {
                return getString(R.string.trusty_steed_option_two);
            } else if (option == TrustySteedOption.AddGold) {
                return getString(R.string.trusty_steed_option_three);
            } else if (option == TrustySteedOption.GainSilvers) {
                return getString(R.string.trusty_steed_option_four);
            }
        } else if (option instanceof SquireOption) {
            if (option == SquireOption.AddActions) {
                return getString(R.string.squire_option_one);
            } else if (option == SquireOption.AddBuys) {
                return getString(R.string.squire_option_two);
            } else if (option == SquireOption.GainSilver) {
                return getString(R.string.squire_option_three);
            }
        } else if (option instanceof CountFirstOption) {
            if (option == CountFirstOption.Discard) {
                return getString(R.string.count_firstoption_one);
            } else if (option == CountFirstOption.PutOnDeck) {
                return getString(R.string.count_firstoption_two);
            } else if (option == CountFirstOption.GainCopper) {
                return getString(R.string.count_firstoption_three);
            }
        } else if (option instanceof CountSecondOption) {
            if (option == CountSecondOption.Coins) {
                return getString(R.string.count_secondoption_one);
            } else if (option == CountSecondOption.TrashHand) {
                return getString(R.string.count_secondoption_two);
            } else if (option == CountSecondOption.GainDuchy) {
                return getString(R.string.count_secondoption_three);
            }
        } else if (option instanceof GraverobberOption) {
            if (option == GraverobberOption.GainFromTrash) {
                return getString(R.string.graverobber_option_one);
            } else if (option == GraverobberOption.TrashActionCard) {
                return getString(R.string.graverobber_option_two);
            }
        } else if (option instanceof HuntingGroundsOption) {
            if (option == HuntingGroundsOption.GainDuchy) {
                return getString(R.string.hunting_grounds_option_one);
            } else if (option == HuntingGroundsOption.GainEstates) {
                return getString(R.string.hunting_grounds_option_two);
            }
        } else if (option instanceof GovernorOption) {
            if (option == GovernorOption.AddCards) {
                return getString(R.string.governor_option_one);
            } else if (option == GovernorOption.GainTreasure) {
                return getString(R.string.governor_option_two);
            } else if (option == GovernorOption.Upgrade) {
                return getString(R.string.governor_option_three);
            }
        } else if (option instanceof PawnOption) {
            if (option == PawnOption.AddCard) {
                return getString(R.string.pawn_one);
            } else if (option == PawnOption.AddAction) {
                return getString(R.string.pawn_two);
            } else if (option == PawnOption.AddBuy) {
                return getString(R.string.pawn_three);
            } else if (option == PawnOption.AddGold) {
                return getString(R.string.pawn_four);
            }
        }
        throw new RuntimeException("I got passed an option object that I don't understand!");
    }

    public static String getSelectCardText(SelectCardOptions sco, String header) {
        String minCostString = (sco.minCost <= 0) ? "" : "" + sco.minCost;
        String maxCostString = (sco.maxCost == Integer.MAX_VALUE) ?
                "" : "" + sco.maxCost + sco.potionString();
        String selectString;

        if (sco.fromTable) {
            if (sco.fromPrizes)
                selectString = header;
            else if (sco.minCost == sco.maxCost) {
                if (sco.isAttack) {
                    selectString = Strings.format(R.string.select_from_table_attack, maxCostString, header);
                } else if (sco.isAction) {
                    selectString = Strings.format(R.string.select_from_table_exact_action, maxCostString, header);
                } else {
                    selectString = Strings.format(R.string.select_from_table_exact, maxCostString, header);
                }
            } else if (sco.minCost <= 0 && sco.maxCost < Integer.MAX_VALUE) {
                if (sco.isVictory) {
                    selectString = Strings.format(R.string.select_from_table_max_vp, maxCostString, header);
                } else if (sco.isNonVictory) {
                    selectString = Strings.format(R.string.select_from_table_max_non_vp, maxCostString, header);
                } else if (sco.isTreasure) {
                    selectString = Strings.format(R.string.select_from_table_max_treasure, maxCostString, header);
                } else if (sco.isAction) {
                    selectString = Strings.format(R.string.select_from_table_max_action, maxCostString, header);
                } else {
                    selectString = Strings.format(R.string.select_from_table_max, maxCostString, header);
                }
            } else if (sco.minCost > 0 && sco.maxCost < Integer.MAX_VALUE) {
                selectString = Strings.format(R.string.select_from_table_between, minCostString, maxCostString, header);
            } else if (sco.minCost > 0) {
                selectString = Strings.format(R.string.select_from_table_min, minCostString + sco.potionString(), header);
            } else {
                selectString = Strings.format(R.string.select_from_table, header);
            }
            return selectString;
        } else if (sco.fromHand) {
            String str = "";
            if (sco.isAction) {
                if(sco.count == 1)
                    str = Strings.format(R.string.select_one_action_from_hand, header);
                else if(sco.exactCount)
                    str = Strings.format(R.string.select_exactly_x_actions_from_hand, "" + sco.count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_actions_from_hand, "" + sco.count, header);
            } else if (sco.isTreasure) {
                if(sco.count == 1)
                    str = Strings.format(R.string.select_one_treasure_from_hand, header);
                else if(sco.exactCount)
                    str = Strings.format(R.string.select_exactly_x_treasures_from_hand, "" + sco.count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_treasures_from_hand, "" + sco.count, header);
            } else if (sco.isVictory) {
                if(sco.count == 1)
                    str = Strings.format(R.string.select_one_victory_from_hand, header);
                else if(sco.exactCount)
                    str = Strings.format(R.string.select_exactly_x_victorys_from_hand, "" + sco.count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_victorys_from_hand, "" + sco.count, header);
            } else if (sco.isNonTreasure) {
                if(sco.count == 1)
                    str = Strings.format(R.string.select_one_nontreasure_from_hand, header);
                else if(sco.exactCount)
                    str = Strings.format(R.string.select_exactly_x_nontreasures_from_hand, "" + sco.count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_nontreasures_from_hand, "" + sco.count, header);
            } else {
                if(sco.count == 1)
                    str = Strings.format(R.string.select_one_card_from_hand, header);
                else if(sco.exactCount)
                    str = Strings.format(R.string.select_exactly_x_cards_from_hand, "" + sco.count, header);
                else
                    str = Strings.format(R.string.select_up_to_x_cards_from_hand, "" + sco.count, header);
            }
            return str;
        }
        throw new RuntimeException("SelectCardOptions isn't from table or from hand...");
    }

    public static String getActionString(SelectCardOptions sco) {
        return getActionString(sco.actionType, sco.cardResponsible);
    }

    public static String getActionString(ActionType action, Card cardResponsible) {
        return getActionString(action, cardResponsible, null);
    }

    public static String getActionString(ActionType action, Card cardResponsible, String opponentName) {
        // TODO(matt): ActionType seems mostly to be redundant with PickType in the
        // SelectCardOptions.  Just in terms of cleaning up the code, it would probably be nice to
        // remove one of them (probably ActionType).  But that's not necessary for the multiplayer
        // stuff, so I'll leave it for later.
        switch (action) {
            case DISCARD: return Strings.format(R.string.card_to_discard, getCardName(cardResponsible));
            case DISCARDFORCARD: return Strings.format(R.string.card_to_discard_for_card, getCardName(cardResponsible));
            case DISCARDFORCOIN: return Strings.format(R.string.card_to_discard_for_coin, getCardName(cardResponsible));
            case REVEAL: return Strings.format(R.string.card_to_reveal, getCardName(cardResponsible));
            case GAIN: return Strings.format(R.string.card_to_gain, getCardName(cardResponsible));
            case TRASH: return Strings.format(R.string.card_to_trash, getCardName(cardResponsible));
            case NAMECARD: return Strings.format(R.string.card_to_name, getCardName(cardResponsible));
            case OPPONENTDISCARD: return Strings.format(R.string.opponent_discard, opponentName, getCardName(cardResponsible));
        }
        return null;
    }

    public static Set<String> simpleActionStrings = new HashSet<String>(Arrays.asList(
            getCardName(Cards.altar),
            getCardName(Cards.ambassador),
            getCardName(Cards.apprentice),
            getCardName(Cards.armory),
            getCardName(Cards.borderVillage),
            getCardName(Cards.butcher),
            getCardName(Cards.catacombs),
            getCardName(Cards.cellar),
            getCardName(Cards.chapel),
            getCardName(Cards.counterfeit),
            getCardName(Cards.dameAnna),
            getCardName(Cards.dameNatalie),
            getCardName(Cards.deathCart),
            getCardName(Cards.develop),
            getCardName(Cards.doctor),
            getCardName(Cards.embassy),
            getCardName(Cards.expand),
            getCardName(Cards.farmland),
            getCardName(Cards.feast),
            getCardName(Cards.forager),
            getCardName(Cards.forge),
            getCardName(Cards.graverobber),
            getCardName(Cards.haggler),
            getCardName(Cards.hermit),
            getCardName(Cards.hornOfPlenty),
            getCardName(Cards.horseTraders),
            getCardName(Cards.inn),
            getCardName(Cards.ironworks),
            getCardName(Cards.jackOfAllTrades),
            getCardName(Cards.journeyman),
            getCardName(Cards.junkDealer),
            getCardName(Cards.oasis),
            getCardName(Cards.plaza),
            getCardName(Cards.rats),
            getCardName(Cards.rebuild),
            getCardName(Cards.remake),
            getCardName(Cards.remodel),
            getCardName(Cards.salvager),
            getCardName(Cards.secretChamber),
            getCardName(Cards.spiceMerchant),
            getCardName(Cards.squire),
            getCardName(Cards.stables),
            getCardName(Cards.steward),
            getCardName(Cards.stonemason),
            getCardName(Cards.storeroom),
            getCardName(Cards.torturer),
            getCardName(Cards.tradeRoute),
            getCardName(Cards.trader),
            getCardName(Cards.tradingPost),
            getCardName(Cards.transmute),
            getCardName(Cards.upgrade),
            getCardName(Cards.warehouse),
            getCardName(Cards.workshop),
            getCardName(Cards.youngWitch)
                    ));

    public static Map<String, String> actionStringMap = new HashMap<String, String>();
    static {
        actionStringMap.put(getCardName(Cards.bureaucrat), getString(R.string.bureaucrat_part));
        actionStringMap.put(getCardName(Cards.bandOfMisfits), getString(R.string.part_play));
        actionStringMap.put(getCardName(Cards.courtyard),
                            Strings.format(R.string.courtyard_part_top_of_deck,
                                           getCardName(Cards.courtyard)));
        actionStringMap.put(getCardName(Cards.contraband), getCardName(Cards.contraband));
        actionStringMap.put(getCardName(Cards.embargo), getCardName(Cards.embargo));
        actionStringMap.put(getCardName(Cards.followers), getString(R.string.followers_part));
        actionStringMap.put(getCardName(Cards.ghostShip), getString(R.string.ghostship_part));
        actionStringMap.put(getCardName(Cards.goons), getString(R.string.goons_part));
        actionStringMap.put(getCardName(Cards.haven), getCardName(Cards.haven));
        actionStringMap.put(getCardName(Cards.island), getCardName(Cards.island));
        actionStringMap.put(getCardName(Cards.kingsCourt), getCardName(Cards.kingsCourt));
        actionStringMap.put(getCardName(Cards.mandarin), getString(R.string.mandarin_part));
        actionStringMap.put(getCardName(Cards.margrave), getString(R.string.margrave_part));
        actionStringMap.put(getCardName(Cards.militia), getString(R.string.militia_part));
        actionStringMap.put(getCardName(Cards.mint), getCardName(Cards.mint));
        actionStringMap.put(getCardName(Cards.saboteur), getString(R.string.saboteur_part));
        actionStringMap.put(getCardName(Cards.secretChamber), getString(R.string.secretchamber_part));
        actionStringMap.put(getCardName(Cards.sirMichael), getString(R.string.sir_michael_part));
        actionStringMap.put(getCardName(Cards.throneRoom), getCardName(Cards.throneRoom));
        actionStringMap.put(getCardName(Cards.tournament), getString(R.string.select_prize));
        actionStringMap.put(getCardName(Cards.university), getString(R.string.university_part));
        actionStringMap.put(getCardName(Cards.urchin), getString(R.string.urchin_keep));
    }

    public static String getActionCardText(SelectCardOptions sco) {
        // We can't test for object equality with, e.g., Cards.militia here, because the object was
        // originally created in another process, possibly on a separate machine, serialized, sent
        // over a network, and then deserialized.  So we use the card name string throughout this
        // method.
        String cardName = getCardName(sco.cardResponsible);
        if (simpleActionStrings.contains(cardName)) {
            return getActionString(sco);
        }

        String actionString = actionStringMap.get(cardName);
        if (actionString != null) {
            return actionString;
        }

        // Here we just have special cases, where there is more than one possible string for a
        // particular card, and the difference isn't captured in the action type, or we need to
        // include some dynamic information from the select card options in the string.
        if (cardName.equals(getCardName(Cards.mine))) {
            if (sco.pickType == PickType.UPGRADE) {
                return getCardName(Cards.mine);
            } else {
                return getString(R.string.mine_part);
            }
        } else if (cardName.equals(getCardName(Cards.swindler))) {
            return Strings.format(R.string.swindler_part,
                                  "" + sco.maxCost + (sco.potionCost == 0 ? "" : "p"));
        } else if (cardName.equals(getCardName(Cards.masquerade))) {
            if (sco.pickType == PickType.GIVE) {
                return getString(R.string.masquerade_part);
            } else {
                return getActionString(sco);
            }
        } else if (cardName.equals(getCardName(Cards.bishop))) {
            if (sco.actionType == ActionType.TRASH) {
                return getActionString(sco);
            } else {
                return getString(R.string.bishop_part);
            }
        } else if (cardName.equals(getCardName(Cards.vault))) {
            if (sco.count == 2) {
                return getString(R.string.vault_part_discard_for_card);
            } else {
                return getString(R.string.vault_part_discard_for_gold);
            }
        } else if (cardName.equals(getCardName(Cards.hamlet))) {
            // WARNING: This is a total hack!  We need to differentiate the "discard for action" from
            // the "discard for buy", but we don't have any way in the SelectCardOptions to do
            // that.  So we set a fake action type in IndirectPlayer.java, and handle it as a
            // special case here.  This is fragile and could easily break if the rest of the code
            // changes and we aren't careful.  TODO(matt): come up with a better way to do this.
            // Probably the right way is to add an action type called DISCARDFORACTION and
            // DISCARDFORBUY, like there is DISCARDFORCARD and DISCARDFORCOIN.  That would require
            // adding some new strings, though, and getting rid of the strings here.
            if (sco.actionType == ActionType.DISCARD) {
                return getString(R.string.hamlet_part_discard_for_action);
            } else {
                return getString(R.string.hamlet_part_discard_for_buy);
            }
        } else if (cardName.equals(getCardName(Cards.count))) {
            if (sco.actionType == ActionType.DISCARD) {
                return getActionString(sco);
            } else {
                return Strings.format(R.string.count_part_top_of_deck, getCardName(Cards.count));
            }
        } else if (cardName.equals(getCardName(Cards.procession))) {
            if (sco.actionType == ActionType.GAIN) {
                return getActionString(sco);
            } else {
                return getCardName(Cards.procession);
            }
        } else if (cardName.equals(getCardName(Cards.mercenary))) {
            if (sco.actionType == ActionType.TRASH) {
                return getActionString(sco);
            } else {
                return getString(R.string.mercenary_part);
            }
        } else if (cardName.equals(getCardName(Cards.taxman))) {
            if (sco.actionType == ActionType.TRASH) {
                return getActionString(sco);
            } else {
                return getString(R.string.taxman_part);
            }
        }
        throw new RuntimeException("Found a card in getActionCardText that I don't know how to handle yet");
    }
}
