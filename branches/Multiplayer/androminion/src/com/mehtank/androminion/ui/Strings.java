package com.mehtank.androminion.ui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.res.Resources;

import com.mehtank.androminion.R;
import com.vdom.api.ActionCard;
import com.vdom.api.Card;
import com.vdom.api.CurseCard;
import com.vdom.api.DurationCard;
import com.vdom.api.GameEvent;
import com.vdom.api.GameType;
import com.vdom.api.TreasureCard;
import com.vdom.api.VictoryCard;
import com.vdom.comms.Event;
import com.vdom.comms.SelectCardOptions;
import com.vdom.comms.SelectCardOptions.ActionType;
import com.vdom.comms.SelectCardOptions.PickType;
import com.vdom.core.Cards;
import com.vdom.core.Player.CountFirstOption;
import com.vdom.core.Player.CountSecondOption;
import com.vdom.core.Player.DoctorOverpayOption;
import com.vdom.core.Player.GovernorOption;
import com.vdom.core.Player.GraverobberOption;
import com.vdom.core.Player.HuntingGroundsOption;
import com.vdom.core.Player.JesterOption;
import com.vdom.core.Player.MinionOption;
import com.vdom.core.Player.NoblesOption;
import com.vdom.core.Player.PawnOption;
import com.vdom.core.Player.PutBackOption;
import com.vdom.core.Player.SpiceMerchantOption;
import com.vdom.core.Player.SquireOption;
import com.vdom.core.Player.StewardOption;
import com.vdom.core.Player.TorturerOption;
import com.vdom.core.Player.TournamentOption;
import com.vdom.core.Player.TrustySteedOption;
import com.vdom.core.Player.WatchTowerOption;

public class Strings {

    @SuppressWarnings("unused")
    private static final String TAG = "Androminion.Strings";

    static HashMap<Card, String> nameCache = new HashMap<Card, String>();
    static HashMap<Card, String> descriptionCache = new HashMap<Card, String>();
    static HashMap<String, String> expansionCache = new HashMap<String, String>();
    static HashMap<GameType, String> gametypeCache = new HashMap<GameType, String>();
    private static Map<String, String> actionStringMap;
    private static Set<String> simpleActionStrings;
    private static Context context;

    public static void initContext(Context c) {
        context = c;
        initActionStrings();
    }

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
     * Given an Event object, return the string that should be shown in the game log.  We look into
     * the GameEvent stored inside the Event object to decide what that string should be (GameEvent
     * is how the Game talks to RemotePlayer, while Event is how RemotePlayer talks to
     * Androminion).
     *
     * @param event The event object that we're generating status text from.
     * @param extras Some extra objects that contain things we need to render the status text.
     *               This is quite ugly, I think, but it was the easiest way make RemotePlayer less
     *               ugly...  We could clean this up in a few ways, notably by adding a
     *               StatusObject or something to the Event.  Items in the extras list:
     *               extras[0]: string possessingPlayerName (or null)
     *               extras[1]: string attackedPlayerName (or null)
     *               extras[2]: string context.getMessage() (or null)
     *               extras[3:4]: If GameOver event, contains some specific things for that.
     */
    public static String getStatusText(Event event, Object[] extras) {
        String statusText = event.s;
        // The first part of the text tells us what kind of event we're dealing with.
        if (event.gameEventType == GameEvent.Type.Embargo) {
            statusText += getString(R.string.Embargo);
        } else if (event.gameEventType == GameEvent.Type.GameStarting) {
            statusText += getString(R.string.GameStarting);
        } else if (event.gameEventType == GameEvent.Type.GameOver) {
            statusText += getGameTimeString((Long) extras[6]);
            if (extras[7] != null) {
                statusText += getGameTypeName((GameType) extras[7]);
            }

            statusText += "\n\n";
            statusText = getVPOutput((String) extras[3],
                                     (Map<Object, Integer>) extras[4],
                                     (Map<Card, Integer>) extras[5]);
            statusText += getString(R.string.GameOver);
        } else if (event.gameEventType == GameEvent.Type.CardRevealed) {
            statusText += getString(R.string.CardRevealed);
        } else if (event.gameEventType == GameEvent.Type.CardRevealedFromHand) {
            statusText += getString(R.string.CardRevealedFromHand);
        } else if (event.gameEventType == GameEvent.Type.CardDiscarded) {
            statusText += getString(R.string.CardDiscarded);
        } else if (event.gameEventType == GameEvent.Type.CardAddedToHand) {
            statusText += getString(R.string.CardAddedToHand);
        } else if (event.gameEventType == GameEvent.Type.CardRemovedFromHand) {
            statusText += getString(R.string.CardRemovedFromHand);
        } else if (event.gameEventType == GameEvent.Type.CardTrashed) {
            statusText += getString(R.string.CardTrashed);
        } else if (event.gameEventType == GameEvent.Type.NoBuy) {
            statusText += getString(R.string.NoBuy);
        } else if (event.gameEventType == GameEvent.Type.DeckReplenished) {
            statusText += getString(R.string.DeckReplenished);
        } else if (event.gameEventType == GameEvent.Type.PlayerAttacking) {
            statusText += getString(R.string.PlayerAttacking);
        } else if (event.gameEventType == GameEvent.Type.PlayerDefended) {
            statusText += getString(R.string.PlayerDefended);
        } else if (event.gameEventType == GameEvent.Type.CardOnTopOfDeck) {
            statusText += getString(R.string.CardOnTopOfDeck);
        } else if (event.gameEventType == GameEvent.Type.CardObtained) {
            statusText += getString(R.string.CardObtained);
        } else if (event.gameEventType == GameEvent.Type.PlayingAction) {
            statusText += getString(R.string.PlayingAction);
        } else if (event.gameEventType == GameEvent.Type.PlayedAction) {
            statusText += getString(R.string.PlayedAction);
        } else if (event.gameEventType == GameEvent.Type.PlayingDurationAction) {
            statusText += getString(R.string.PlayingDurationAction);
        } else if (event.gameEventType == GameEvent.Type.PlayingCoin) {
            statusText += getString(R.string.PlayingCoin);
        } else if (event.gameEventType == GameEvent.Type.BuyingCard) {
            statusText += getString(R.string.BuyingCard);
        } else if (event.gameEventType == GameEvent.Type.TurnEnd) {
            statusText += getString(R.string.TurnEnd);
        } else if (event.gameEventType == GameEvent.Type.VictoryPoints) {
            statusText += getString(R.string.VictoryPoints);
        } else if (event.gameEventType == GameEvent.Type.NewHand) {
            statusText += getString(R.string.NewHand);
        } else if (event.gameEventType == GameEvent.Type.TurnBegin) {
            statusText += getString(R.string.TurnBegin);
        } else if (event.gameEventType == GameEvent.Type.CantBuy) {
            Card[] cantBuy = event.o.cs;
            String cards = "";
            boolean first = true;
            for(Card card : cantBuy) {
                if(first) {
                    first = false;
                }
                else {
                    cards += ", ";
                }
                cards += getCardName(card);
            }
            statusText += format(R.string.CantBuy, cards);
        } else if (event.gameEventType == GameEvent.Type.Status) {
            statusText += getString(R.string.Status);
        } else {
            statusText += event.gameEventType.toString();
        }

        // Then, if there's a card associated with the event, we display it here.
        if (event.c != null
                && event.gameEventType != GameEvent.Type.CardAddedToHand
                && event.gameEventType != GameEvent.Type.PlayerAttacking) {
            statusText += " " + getCardName(event.c) + " ";
        }

        // And a few other random things that should be added to the status text.
        if (event.gameEventType == GameEvent.Type.TurnBegin && extras[0] != null) {
            statusText += " possessed by " + extras[0] + "!";
        }
        if (extras[1] != null) {
            statusText += " (" + extras[1] + ") ";  // this is the player that's being attacked.
        }
        if (extras[2] != null) {
            statusText += "\n" + extras[2];  // if there's a message from the context, add it here.
        }
        return statusText;
    }

    public static String getFullCardDescription(Card c) {
        String ret = Strings.getCardDescription(c);

        if (c.equals(Cards.curse)) {
            ret = Strings.format(R.string.vp_single, "" + ((CurseCard) c).getVictoryPoints()) + "\n" + ret;
        }
        if (c instanceof VictoryCard) {
            if (((VictoryCard) c).getVictoryPoints() > 1)
                ret = Strings.format(R.string.vp_multiple, "" + ((VictoryCard) c).getVictoryPoints()) + "\n" + ret;
            else if (((VictoryCard) c).getVictoryPoints() > 0)
                ret = Strings.format(R.string.vp_single, "" + ((VictoryCard) c).getVictoryPoints()) + "\n" + ret;
            else if (((VictoryCard) c).getVictoryPoints() < -1)
                ret = Strings.format(R.string.vp_multiple, "" + ((VictoryCard) c).getVictoryPoints()) + "\n" + ret;
            else if (((VictoryCard) c).getVictoryPoints() < 0)
                ret = Strings.format(R.string.vp_single, "" + ((VictoryCard) c).getVictoryPoints()) + "\n" + ret;
        }
        if (c instanceof TreasureCard) {
            ret = Strings.format(R.string.coin_worth, "" + ((TreasureCard) c).getValue()) + "\n" + ret;
        }
        if (c instanceof ActionCard) {
            ActionCard ac = (ActionCard) c;
            if (c instanceof DurationCard) {
                DurationCard dc = (DurationCard) c;
                if (dc.getAddGoldNextTurn() > 0) ret = Strings.format(R.string.coin_next_turn, "" + dc.getAddGoldNextTurn()) + "\n" + ret;
                if (dc.getAddBuysNextTurn() > 1) ret = Strings.format(R.string.buys_next_turn_multiple, "" + dc.getAddBuysNextTurn()) + "\n" + ret;
                else if (dc.getAddBuysNextTurn() > 0) ret = Strings.format(R.string.buy_next_turn_single, "" + dc.getAddBuysNextTurn()) + "\n" + ret;
                if (dc.getAddActionsNextTurn() > 1) ret =  Strings.format(R.string.actions_next_turn_multiple, "" + dc.getAddActionsNextTurn()) + "\n" + ret;
                else if (dc.getAddActionsNextTurn() > 0) ret =  Strings.format(R.string.action_next_turn_single, "" + dc.getAddActionsNextTurn()) + "\n" + ret;
                if (dc.getAddCardsNextTurn() > 1) ret = Strings.format(R.string.cards_next_turn_multiple, "" + dc.getAddCardsNextTurn()) + "\n" + ret;
                else if (dc.getAddCardsNextTurn() > 0) ret = Strings.format(R.string.card_next_turn_single, "" + dc.getAddCardsNextTurn()) + "\n" + ret;

            }

            if (ac.getAddGold() > 0) ret = Strings.format(R.string.card_coin, "" + ac.getAddGold()) + "\n" + ret;
            if (ac.getAddBuys() > 1) ret = Strings.format(R.string.card_buys_multiple, "" + ac.getAddBuys()) + "\n" + ret;
            else if (ac.getAddBuys() > 0) ret = Strings.format(R.string.card_buy_single, "" + ac.getAddBuys()) + "\n" + ret;
            if (ac.getAddActions() > 1) ret = Strings.format(R.string.card_actions_multiple, "" + ac.getAddActions()) + "\n" + ret;
            else if (ac.getAddActions() > 0) ret = Strings.format(R.string.card_action_single, "" + ac.getAddActions()) + "\n" + ret;
            if (ac.getAddCards() > 1) ret = Strings.format(R.string.card_cards_multiple, "" + ac.getAddCards()) + "\n" + ret;
            else if (ac.getAddCards() > 0) ret = Strings.format(R.string.card_card_single, "" + ac.getAddCards()) + "\n" + ret;
            if (ac.getAddVictoryTokens() > 1) ret = Strings.format(R.string.card_victory_tokens_multiple, "" + ac.getAddVictoryTokens()) + "\n" + ret;
            else if (ac.getAddVictoryTokens() > 0) ret = Strings.format(R.string.card_victory_token_single, "" + ac.getAddVictoryTokens()) + "\n" + ret;
        }
        return ret;
    }

    /**
     * Takes a card and an array of "options", and returns an array of strings, where each string
     * corresponds to an actual option in the options array.  Some of the items in the options
     * array might not actually be options, they might be information that tells us about the
     * options, or about the header to show, and that depends on the card.  That's why we take the
     * card as input here.
     */
    public static String[] getOptions(Card card, Object[] options) {
        int startIndex = getOptionStartIndex(card, options);
        String[] strings = new String[options.length - startIndex];
        if (card != null && getCardName(card).equals(getCardName(Cards.hermit))) {
            strings = new String[options.length - 1];
            strings[0] = getString(R.string.none);
            int nonTreasureCountInDiscard = (Integer) options[0];
            String pile = " (discard pile)";  // TODO(matt): put these hard-coded strings into R?
            for (int i = 2; i < options.length; i++) {
                if (i - 2 >= nonTreasureCountInDiscard) {
                    pile = " (hand)";
                }
                strings[i - 1] = getCardName((Card)options[i]) + pile;
            }
            return strings;
        }
        for (int i = startIndex; i < options.length; i++) {
            strings[i - startIndex] = Strings.getOptionText(options[i], options);
        }
        return strings;
    }

    private static int getOptionStartIndex(Card card, Object[] options) {
        // TODO(matt): it'd be cleaner to make this an enum, or something, instead of using these
        // strings.
        if (options[0] instanceof String) {
            String optionString = (String) options[0];
            if (optionString.equals("REACTION")) {
                return 1;
            } else if (optionString.equals("PUTBACK")) {
                return 1;
            } else if (optionString.equals("GUILDCOINS")) {
                return 1;
            } else if (optionString.equals("OVERPAY")) {
                return 1;
            } else if (optionString.equals("OVERPAYP")) {
                return 1;
            }
        }
        if (card == null)
            return 0;
        String cardName = getCardName(card);
        // TODO(matt): I could put these into sets, for startIndex = 1, 2, etc., to make this more
        // compact.
        if (cardName.equals(getCardName(Cards.advisor))) {
            return 1;
        } else if (cardName.equals(getCardName(Cards.ambassador))) {
            return 1;
        } else if (cardName.equals(getCardName(Cards.doctor))) {
            if (options[0] == null) {
                return 0;
            } else {
                return 1;
            }
        } else if (cardName.equals(getCardName(Cards.envoy))) {
            return 1;
        } else if (cardName.equals(getCardName(Cards.jester))) {
            return 2;
        } else if (cardName.equals(getCardName(Cards.lookout))) {
            return 1;
        } else if (cardName.equals(getCardName(Cards.pillage))) {
            return 1;
        }
        return 0;
    }

    public static String getSelectOptionHeader(Card card, Object[] extras) {
        if (extras[0] instanceof String && ((String)extras[0]).equals("PUTBACK")) {
            return getString(R.string.putback_query);
        } else if (extras[0] instanceof String && ((String)extras[0]).equals("REACTION")) {
            return getString(R.string.reaction_query) + " [" + getCardName(card) + "]";
        } else if (extras[0] instanceof String && ((String)extras[0]).equals("GUILDCOINS")) {
            return "Spend Guilds Coin Tokens";
        } else if (extras[0] instanceof String && ((String)extras[0]).equals("OVERPAY")) {
            return "Overpay?";
        } else if (extras[0] instanceof String && ((String)extras[0]).equals("OVERPAYP")) {
            return "Overpay by Potion(s)?";
        }
        String cardName = getCardName(card);
        if (cardName.equals(getCardName(Cards.advisor))) {
            return getActionString(ActionType.OPPONENTDISCARD, card, (String) extras[0]);
        } else if (cardName.equals(getCardName(Cards.ambassador))) {
            return format(R.string.ambassador_query, getCardName((Card)extras[0]));
        } else if (cardName.equals(getCardName(Cards.cartographer))) {
            return getString(R.string.Cartographer_query) + " [" + cardName + "]";
        } else if (cardName.equals(getCardName(Cards.doctor))) {
            if (extras[0] == null) {
                return cardName;
            } else {
                return "Doctor revealed " + getCardName((Card)extras[0]);  // TODO(matt): fix this string
            }
        } else if (cardName.equals(getCardName(Cards.envoy))) {
            return getActionString(ActionType.OPPONENTDISCARD, card, (String) extras[0]);
        } else if (cardName.equals(getCardName(Cards.golem))) {
            return getString(R.string.golem_first_action);
        } else if (cardName.equals(getCardName(Cards.herald))) {
            return getString(R.string.herald_overpay_query) + " [" + cardName + "]";
        } else if (cardName.equals(getCardName(Cards.herbalist))) {
            return getString(R.string.herbalist_query);
        } else if (cardName.equals(getCardName(Cards.hermit))) {
            return getActionString(ActionType.TRASH, Cards.hermit);
        } else if (cardName.equals(getCardName(Cards.jester))) {
            return format(R.string.card_revealed, cardName, getCardName((Card)extras[1]));
        } else if (cardName.equals(getCardName(Cards.lookout))) {
            if (extras[0] == ActionType.TRASH) {
                return getString(R.string.lookout_query_trash);
            } else if (extras[0] == ActionType.DISCARD) {
                return getString(R.string.lookout_query_discard);
            }
        } else if (cardName.equals(getCardName(Cards.pillage))) {
            return getActionString(ActionType.OPPONENTDISCARD, card, (String) extras[0]);
        } else if (cardName.equals(getCardName(Cards.pirateShip))) {
            return getString(R.string.treasure_to_trash);
        } else if (cardName.equals(getCardName(Cards.scheme))) {
            return getString(R.string.scheme_query);
        } else if (cardName.equals(getCardName(Cards.smugglers))) {
            return getString(R.string.smuggle_query);
        } else if (cardName.equals(getCardName(Cards.thief))) {
            if (extras[0] == null) {
                // In this case we're gaining treasures that have been trashed.
                return getString(R.string.thief_query);
            } else {
                // And in this case we're deciding which of two treasures we should trash.
                return getString(R.string.treasure_to_trash);
            }
        }
        return cardName;
    }
    /**
     * Takes an option object and returns the string the corresponds to the option.  The API could
     * be a little cleaner than just passing Objects around, but it was the easiest way to change
     * the API to decouple to server code from the android framework.
     */
    public static String getOptionText(Object option, Object[] extras) {
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
                 return format(R.string.jester_option_two, extras[0]);
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
        } else if (option instanceof DoctorOverpayOption) {
            if (option == DoctorOverpayOption.TrashIt) {
                return getString(R.string.doctor_overpay_option_one);
            } else if (option == DoctorOverpayOption.DiscardIt) {
                return getString(R.string.doctor_overpay_option_two);
            } else if (option == DoctorOverpayOption.PutItBack) {
                return getString(R.string.doctor_overpay_option_three);
            }
        } else if (option instanceof PutBackOption) {
            if (option == PutBackOption.Action) {
                return getString(R.string.putback_option_two);
            } else if (option == PutBackOption.Alchemist) {
                return getCardName(Cards.alchemist);
            } else if (option == PutBackOption.Coin) {
                return getString(R.string.putback_option_one);
            } else if (option == PutBackOption.None) {
                return getString(R.string.none);
            } else if (option == PutBackOption.Treasury) {
                return getCardName(Cards.treasury);
            } else if (option == PutBackOption.WalledVillage) {
                return getCardName(Cards.walledVillage);
            }
        } else if (option instanceof Card) {
            return getCardName((Card) option);
        } else if (option == null) {
            return getString(R.string.none);
        } else if (option instanceof Integer) {
            return "" + option;
        }
        throw new RuntimeException("I got passed an option object that I don't understand!");
    }

    /**
     * Get the strings necessary for selecting a boolean option initiated by cardResponsible,
     * using extras to fill in the necessary information for creating the strings.
     *
     * The first item in the returned array is the header to show, the second item is the string
     * that will be interpreted as "true" if selected, and the third item is the string that will
     * be interpreted as "false".
     */
    public static String[] getBooleanStrings(Card cardResponsible, Object[] extras) {
        // See note below under getActionCardText for why we can't test for object equality here,
        // and instead use string equality.
        String cardName = getCardName(cardResponsible);
        String[] strings = new String[3];
        strings[0] = cardName;  // common enough to set this as a default; override if necessary.
        if (cardName.equals(getCardName(Cards.alchemist))) {
            strings[1] = getString(R.string.alchemist_option_one);
            strings[2] = getString(R.string.alchemist_option_two);
        } else if (cardName.equals(getCardName(Cards.baron))) {
            strings[1] = getString(R.string.baron_option_one);
            strings[2] = getString(R.string.baron_option_two);
        } else if (cardName.equals(getCardName(Cards.catacombs))) {
            strings[0] = format(R.string.catacombs_header, combineCardNames(extras));
            strings[1] = getString(R.string.catacombs_option_one);
            strings[2] = getString(R.string.catacombs_option_two);
        } else if (cardName.equals(getCardName(Cards.chancellor))) {
            strings[1] = getString(R.string.chancellor_query);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.cultist))) {
            strings[1] = getString(R.string.cultist_play_next);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.duchess))) {
            if (extras == null) {
                // This is asking if you want to _gain_ a duchess (upon purchase of a duchy).
                strings[0] = getString(R.string.duchess_query);
                strings[1] = getString(R.string.duchess_option_one);
                strings[2] = getString(R.string.pass);
            } else {
                // And this one is from _playing_ the duchess.
                strings[0] = getCardRevealedHeader(extras);
                strings[1] = getString(R.string.duchess_play_option_one);
                strings[2] = getString(R.string.discard);
            }
        } else if (cardName.equals(getCardName(Cards.explorer))) {
            strings[1] = getString(R.string.explorer_reveal);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.foolsGold))) {
            strings[1] = getString(R.string.fools_gold_option_one);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.hovel))) {
            strings[1] = getString(R.string.hovel_option);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.illGottenGains))) {
            strings[1] = getString(R.string.ill_gotten_gains_option_one);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.inn))) {
            strings[0] = getCardRevealedHeader(extras);
            strings[1] = getString(R.string.inn_option_one);
            strings[2] = getString(R.string.inn_option_two);
        } else if (cardName.equals(getCardName(Cards.ironmonger))) {
            strings[0] = getCardRevealedHeader(extras);
            strings[1] = getString(R.string.ironmonger_option_one);
            strings[2] = getString(R.string.discard);
        } else if (cardName.equals(getCardName(Cards.jackOfAllTrades))) {
            strings[0] = getCardRevealedHeader(extras);
            strings[1] = getString(R.string.jack_of_all_trades_option_one);
            strings[2] = getString(R.string.discard);
        } else if (cardName.equals(getCardName(Cards.library))) {
            strings[0] = getCardRevealedHeader(extras);
            strings[1] = getString(R.string.keep);
            strings[2] = getString(R.string.discard);
        } else if (cardName.equals(getCardName(Cards.loan))) {
            strings[0] = getCardRevealedHeader(extras);
            strings[1] = getString(R.string.trash);
            strings[2] = getString(R.string.discard);
        } else if (cardName.equals(getCardName(Cards.madman))) {
            strings[1] = getString(R.string.madman_option);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.marketSquare))) {
            strings[1] = getString(R.string.discard);
            strings[2] = getString(R.string.keep);
        } else if (cardName.equals(getCardName(Cards.miningVillage))) {
            strings[1] = getString(R.string.mining_village_option_one);
            strings[2] = getString(R.string.keep);
        } else if (cardName.equals(getCardName(Cards.mountebank))) {
            strings[0] = getString(R.string.mountebank_query);
            strings[1] = getString(R.string.mountebank_option_one);
            strings[2] = getString(R.string.mountebank_option_two);
        } else if (cardName.equals(getCardName(Cards.pearlDiver))) {
            strings[0] = getCardRevealedHeader(extras);
            strings[1] = getString(R.string.pearldiver_option_one);
            strings[2] = getString(R.string.pearldiver_option_two);
        } else if (cardName.equals(getCardName(Cards.pirateShip))) {
            strings[1] = format(R.string.pirate_ship_option_one, "" + (Integer) extras[0]);
            strings[2] = getString(R.string.pirate_ship_option_two);
        } else if (cardName.equals(getCardName(Cards.nativeVillage))) {
            strings[1] = getString(R.string.native_village_option_one);
            strings[2] = getString(R.string.native_village_option_two);
        } else if (cardName.equals(getCardName(Cards.navigator))) {
            strings[0] = Strings.format(R.string.navigator_header, combineCardNames(extras));
            strings[1] = getString(R.string.discard);
            strings[2] = getString(R.string.navigator_option_two);
        } else if (cardName.equals(getCardName(Cards.nobleBrigand))) {
            strings[0] = Strings.format(R.string.noble_brigand_query, extras[0]);
            strings[1] = getCardName((Card) extras[1]);
            strings[2] = getCardName((Card) extras[2]);
        } else if (cardName.equals(getCardName(Cards.oracle))) {
            String cardNames = combineCardNames(extras, 1);
            strings[0] = format(R.string.card_revealed, extras[0], cardNames);
            strings[1] = getString(R.string.top_of_deck);
            strings[2] = getString(R.string.discard);
        } else if (cardName.equals(getCardName(Cards.royalSeal))) {
            strings[0] = getCardRevealedHeader(extras);
            strings[1] = getString(R.string.top_of_deck);
            strings[2] = getString(R.string.take_normal);
        } else if (cardName.equals(getCardName(Cards.scavenger))) {
            // This is pretty ugly, but that's how it's written.  That's the problem with using
            // card names as variables instead of english.  An ambitious goal would be to redo all
            // of these names using something a little more sane and reusable (but it's probably
            // not worth the effort, as this works).
            strings[1] = getString(R.string.chancellor_query);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.scryingPool))) {
            strings[0] = getPlayerRevealedCardHeader(extras);
            strings[1] = getString(R.string.discard);
            strings[2] = getString(R.string.replace);
        } else if (cardName.equals(getCardName(Cards.spy))) {
            strings[0] = getPlayerRevealedCardHeader(extras);
            strings[1] = getString(R.string.discard);
            strings[2] = getString(R.string.replace);
        } else if (cardName.equals(getCardName(Cards.survivors))) {
            strings[0] = Strings.format(R.string.survivors_header, combineCardNames(extras));
            strings[1] = getString(R.string.discard);
            strings[2] = getString(R.string.navigator_option_two);
        } else if (cardName.equals(getCardName(Cards.tournament))) {
            strings[1] = getString(R.string.tournament_reveal);
            strings[2] = getString(R.string.tournament_option_one);
        } else if (cardName.equals(getCardName(Cards.trader))) {
            String c_name = getCardName((Card)extras[0]);
            strings[1] = format(R.string.trader_gain, c_name);
            strings[2] = format(R.string.trader_gain_instead_of, getCardName(Cards.silver), c_name);
        } else if (cardName.equals(getCardName(Cards.tunnel))) {
            strings[0] = getString(R.string.tunnel_query);
            strings[1] = getString(R.string.tunnel_option_one);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.urchin))) {
            strings[1] = getString(R.string.urchin_trash_for_mercenary);
            strings[2] = getString(R.string.pass);
        } else if (cardName.equals(getCardName(Cards.walledVillage))) {
            strings[1] = getString(R.string.walledVillage_option_one);
            strings[2] = getString(R.string.walledVillage_option_two);
        } else if (cardName.equals(getCardName(Cards.youngWitch))) {
            strings[1] = format(R.string.bane_option_one, getCardName((Card)extras[0]));
            strings[2] = getString(R.string.pass);
        }
        if (strings[1] != null) {
            return strings;
        }
        throw new RuntimeException("I got passed a card that I don't know how to create a boolean "
                                   + "option for!");
    }

    public static String combineCardNames(Object[] cards) {
        return combineCardNames(cards, 0);
    }

    public static String getCardRevealedHeader(Object[] extras) {
        return getCardRevealedHeader((Card) extras[0], (Card) extras[1]);
    }

    public static String getCardRevealedHeader(Card responsible, Card revealed) {
        return format(R.string.card_revealed, getCardName(responsible), getCardName(revealed));
    }

    public static String getPlayerRevealedCardHeader(Object[] extras) {
        return getPlayerRevealedCardHeader((String) extras[0], (Card) extras[1], (Card) extras[2]);
    }

    public static String getPlayerRevealedCardHeader(String name, Card responsible, Card revealed) {
        return format(R.string.card_revealed_from_player,
                      name,
                      getCardName(responsible),
                      getCardName(revealed));
    }

    public static String combineCardNames(Object[] cards, int startIndex) {
        String cardNames = "";
        for (int i = startIndex; i < cards.length; i++) {
            cardNames += getCardName((Card) cards[i]);
            if (i != cards.length - 1) {
                cardNames += ", ";
            }
        }
        return cardNames;
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
        // remove one of them (probably ActionType).  The only tricky thing is that ActionType
        // distinguishes between DISCARDFORCARD and DISCARDFORCOIN, while PickType doesn't.  But
        // this isn't necessary for the multiplayer stuff, so I'll leave it for later.
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

    private static void initActionStrings() {
        if (simpleActionStrings != null) return;
        simpleActionStrings = new HashSet<String>(Arrays.asList(
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
        actionStringMap = new HashMap<String, String>();
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

    /**
     * Not totally sure what the real use of this is yet, but it was a private method in
     * RemotePlayer that didn't need to be there.
     */
    public static String getCardText(final Map<Object, Integer> counts,
                                     final Map<Card, Integer> totals,
                                     final Card card) {
        final StringBuilder sb = new StringBuilder()
                .append('\t')
                .append(card.getName())
                .append(" x")
                .append(counts.get(card))
                .append(": ")
                .append(totals.get(card))
                .append(" ")
                .append(Strings.getString(R.string.game_over_vps))
                .append('\n');

        return sb.toString();
    }

    /**
     * Get the end game text that shows victory point totals for a particular player.
     */
    public static String getVPOutput(String playerName,
                                     Map<Object, Integer> counts,
                                     Map<Card, Integer> totals) {
        int totalVPs = 0;
        for (Integer total : totals.values()) {
            totalVPs += total;
        }
        final StringBuilder sb
                = new StringBuilder()
                .append(playerName)
                .append(": ")
                .append(totalVPs)
                .append(" ")
                .append(Strings.getString(R.string.game_over_vps))
                .append('\n');

        sb.append(Strings.getCardText(counts, totals, Cards.estate));
        sb.append(Strings.getCardText(counts, totals, Cards.duchy));
        sb.append(Strings.getCardText(counts, totals, Cards.province));
        if(counts.containsKey(Cards.colony)) {
            sb.append(Strings.getCardText(counts, totals, Cards.colony));
        }

        // display victory cards from sets

        for(Card card : totals.keySet()) {
            if(!Cards.nonKingdomCards.contains(card)) {
                sb.append(Strings.getCardText(counts, totals, card));
            }
        }

        sb.append(Strings.getCardText(counts, totals, Cards.curse));

        sb.append("\tVictory Tokens: ")
                .append(totals.get(Cards.victoryTokens))
                .append('\n');

        return sb.toString();
    }

    public static String getGameTimeString(long duration) {
        String time = getString(R.string.game_over_status);
        time += " ";
        if (duration > 1000 * 60 * 60)
            time += (duration / (1000 * 60 * 60)) + "h ";
        duration = duration % (1000 * 60 * 60);
        if (duration > 1000 * 60)
            time += (duration / (1000 * 60)) + "m ";
        duration = duration % (1000 * 60);
        time += (duration / (1000)) + "s.\n";
        return time;
    }
}
