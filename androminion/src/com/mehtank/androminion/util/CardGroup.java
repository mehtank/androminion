package com.mehtank.androminion.util;

import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mehtank.androminion.ui.CardView;
import com.mehtank.androminion.ui.CardView.CardState;
import com.mehtank.androminion.ui.Strings;
import com.vdom.api.Card;
import com.vdom.comms.GameStatus;
import com.vdom.comms.MyCard;
import com.vdom.core.Type;

/**
 * Collection of cards (e.g. hand, row of piles) that is displayed in a row / table
 *
 */
public class CardGroup extends BaseAdapter {
    private static final String TAG = "CardGroup";

    private Context top;
    private boolean onTable = false;
    private ArrayList<CardState> cards = new ArrayList<CardState>();
    private Comparator<MyCard> cmp = new MyCard.CardCostNameComparator();
    private boolean sorted = false;
    private PlayerAdapter players;

    // fix bug that lets item countLeft jump around
    int[] supplySizes = null;
    int[] embargos = null;
    int[] pileVpTokens = null;
    int[] pileDebtTokens = null;
    int[] pileTradeRouteTokens = null;
    int[][][] tokens = null;

    public void updateCounts(int[] supplySizes, int[] embargos, int[] pileVpTokens, int[] pileDebtTokens, int[] pileTradeRouteTokens, int[][][] tokens) {
        this.supplySizes = supplySizes;
        this.embargos = embargos;
        this.pileVpTokens = pileVpTokens;
        this.pileDebtTokens = pileDebtTokens;
        this.pileTradeRouteTokens = pileTradeRouteTokens;
        this.tokens = tokens;
        notifyDataSetChanged();
    }

    public CardGroup(Context top, boolean onTable) {
    	this(top, onTable, new MyCard.CardCostNameComparator());
    }

    public CardGroup(Context top, boolean onTable, Comparator<MyCard> comparator) {
        this.top = top;
        this.onTable = onTable;
        this.cmp = comparator;
    }

    public void addCard(MyCard c) {
        addCard(c, true);
    }

    public void addCard(MyCard c, boolean state) {
        CardState ci = new CardState(c, false, "", -1, !state);
        ci.onTable = onTable;
        if (onTable || sorted) { // sort cards that are on the table
            int i = 0;
            for (i=0; i < cards.size(); i++) {
                if(cmp.compare(c, cards.get(i).c) < 0)
                    break;
            }
            cards.add(i, ci);
        } else
            cards.add(ci);
        notifyDataSetChanged();
    }
    
    public void setPlayers(PlayerAdapter players) {
    	this.players = players;
    }

    public void updateState(int pos, CardState cs){
        cards.set(pos, cs);
        notifyDataSetChanged();
    }

    public void removeCard(int pos) {
        cards.remove(pos);
        notifyDataSetChanged();
    }

    public void clear() {
        cards.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cards.size();
    }

    @Override
    public Object getItem(int pos) {
        return cards.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    @Override
    public View getView(int pos, View origView, ViewGroup parent) {
        CardView cv;
        CardState cs = cards.get(pos);
        if(origView == null) {
            cv = new CardView(top, this, null);
        } else {
            cv = (CardView) origView;
        }
        cv.setState(cs);
        try {
            if (supplySizes != null)
                cv.setCountLeft(supplySizes[cs.c.id]);
            if (embargos != null)
                cv.setEmbargos(embargos[cs.c.id]);
            if (pileVpTokens != null)
                cv.setPileVpTokens(pileVpTokens[cs.c.id]);
            if (pileDebtTokens != null)
                cv.setPileDebtTokens(pileDebtTokens[cs.c.id]);
            if (pileTradeRouteTokens != null)
                cv.setPileTradeRouteTokens(pileTradeRouteTokens[cs.c.id]);
            if (tokens != null)
            	cv.setTokens(tokens[cs.c.id], players);
        } catch (ArrayIndexOutOfBoundsException e) {
            // TODO See why this is happening?
            Log.w(TAG, "exception", e);
        }

        return cv;
    }

    /**
     * TODO SPLITPILES change description
     * This is to update the card information that's displayed when a pile is non-uniform, as with
     * Ruins and Knights cards.
     *
     * @param index Tells us the index of the card pile that we're supposed to update
     * @param card The card that is now on top, whose information we should display
     * @param cost The cost of the card as we should display it (as this depends on context, we
     *             can't just grab it from the card itself).
     */
    public void updateCardInfo(GameStatus.UpdateCardInfo uci) {
        for (CardState cs : cards) {
            if (cs.c.id == uci.cardId) {
                cs.c.name = Strings.getCardName(uci.card);
                cs.c.desc = Strings.getFullCardDescription(uci.card);
                if (uci.cost >= 0) cs.c.cost = uci.cost;
                if (uci.debtCost >= 0) cs.c.debtCost = uci.debtCost;
                if (uci.count >= 0) supplySizes[cs.c.id] = uci.count;
                cs.c.gold = uci.card.getAddGold();
                cs.c.vp = uci.card.getAddVictoryTokens();
                cs.c.isVictory = uci.card.is(Type.Victory);
                cs.c.isTreasure = uci.card.is(Type.Treasure);
                cs.c.isAction = uci.card.is(Type.Action);
                cs.c.isAttack = uci.card.is(Type.Attack);
                cs.c.isCastle = uci.card.is(Type.Castle);
                cs.c.isKnight = uci.card.is(Type.Knight);
                if (uci.card.getExpansion() != null) {
                    cs.c.expansion = uci.card.getExpansion().name();
                }

            }
        }
    }

    public int getPos(int index) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).c.id == index)
                return i;
        }
        return -1;
    }
}
