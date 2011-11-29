package com.vdom.core;

import java.util.ArrayList;
import java.util.Iterator;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;

public class CardList implements Iterable<Card> {
    ArrayList<Card> a = new ArrayList<Card>();
    Player player;
    String name;

    public CardList(Player player, String name) {
        this.player = player;
        this.name = name;

        if (player == null) {
            Util.log("ERROR:Trying to create CardList " + name + " for null player");
            Thread.dumpStack();
        }
    }

    public void checkValid() {
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i) == null) {
                Util.playerError(player, name + " contains null card.", true);
            } else if (((CardImpl) a.get(i)).templateCard) {
                Util.playerError(player, "Trying to add template card to " + name, true);
            }
        }
    }

    public boolean contains(Card card) {
        return a.contains(card);
    }

    public boolean remove(Card card) {
        return a.remove(card);
    }

    public Card get(int i) {
        return a.get(i);
    }

    public Card get(Card card) {
        for (Card handCard : a) {
            if (handCard.equals(card)) {
                return handCard;
            }
        }

        return null;
    }

    public Card remove(int i) {
        return remove(i, true);
    }

    public Card remove(int i, boolean showUI) {

        Card card = a.remove(i);

        if (showUI && name.equals("Hand")) {
            MoveContext context = new MoveContext(player.game, player);
            GameEvent event = new GameEvent(GameEvent.Type.CardRemovedFromHand, context);
            event.card = card;
            player.game.broadcastEvent(event);
        }

        return card;
    }

    public void add(int index, Card card) {
        if (card == null) {
            Util.playerError(player, "Trying to add null card to " + name, true);
        } else if (((CardImpl) card).templateCard) {
            Util.playerError(player, "Trying to add template card to " + name, true);
        } else {
            a.add(index, card);
        }
    }

    public void add(Card card) {
        add(card, true);
    }

    public void add(Card card, boolean showUI) {
        if (card == null) {
            Util.playerError(player, "Trying to add null card to " + name, true);
        } else if (((CardImpl) card).templateCard) {
            Util.playerError(player, "Trying to add template card to " + name, true);
        } else {
            a.add(card);

            if (showUI && name.equals("Hand")) {
                MoveContext context = new MoveContext(player.game, player);
                GameEvent event = new GameEvent(GameEvent.Type.CardAddedToHand, context);
                event.card = card;
                player.game.broadcastEvent(event);
            }
        }
    }

    public int size() {
        return a.size();
    }

    public Card[] toArray() {
        return a.toArray(new Card[0]);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void clear() {
        a.clear();
    }

    public Iterator<Card> iterator() {
        return a.iterator();
    }
}
