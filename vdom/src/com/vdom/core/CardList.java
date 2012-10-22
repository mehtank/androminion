package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    public boolean checkValid() {
    	boolean isValid = true; 
		for (Card card : a) 
			isValid = checkValid(card) ? isValid : false;
		return isValid;
    }

    public boolean checkValid(Card card) {
    	if (!(card == null || ((CardImpl) card).templateCard))
    		return true;
        if (card == null) {
            Util.playerError(player, name + " contains null card.", true);
        } else {
            Util.playerError(player, "Trying to add template card to " + name, true);
        }
        return false;
    }

    public boolean contains(Card card) {
        return a.contains(card);
    }

    public Card get(int i) {
        return a.get(i);
    }

    public Card get(Card card) {
        for (Card c : a) 
            if (c.equals(card)) 
                return c;
        return null;
    }

    public boolean remove(Card card) {
        return a.remove(card);
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

    public void add(Card card) {
        add(card, false, -1);
    }

    public void add(int index, Card card) {
        add(card, false, index);
    }

    public void add(Card card, boolean showUI) {
        add(card, showUI, -1);
    }

    public void add(Card card, boolean showUI, int index) {
    	if (checkValid(card)) {
            if (index != -1)
            	a.add(index, card);
            else 
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

    public ArrayList<Card> toArrayList() {
    	return a;
    }

    public ArrayList<Card> toArrayListClone() {
    	return (ArrayList<Card>) a.clone();
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

    public int indexOf(Card card) {
        return a.indexOf(card);
    }

    public int lastIndexOf(Card card) {
        return a.lastIndexOf(card);
    }

	public Card[] sort(Comparator<Card> comp) {
		Card[] sorted = this.toArray();
	    Arrays.sort(sorted, comp);
	    return sorted;
	}
}
