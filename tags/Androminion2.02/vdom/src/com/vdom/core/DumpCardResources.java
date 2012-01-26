package com.vdom.core;

import java.util.ArrayList;

import com.vdom.api.Card;

public class DumpCardResources {
    public static void main(String[] args) throws Exception {
        Game game = new Game();
        game.initGameBoard();
        
        ArrayList<Card> cards = new ArrayList<Card>();
        cards.add(Cards.copper);
        cards.add(Cards.silver);
        cards.add(Cards.gold);
        cards.add(Cards.platinum);
        cards.add(Cards.potion);
        cards.add(Cards.curse);
        cards.add(Cards.estate);
        cards.add(Cards.duchy);
        cards.add(Cards.province);
        cards.add(Cards.colony);
        cards.addAll(Cards.actionCardsBaseGame);
        cards.addAll(Cards.actionCardsIntrigue);
        cards.addAll(Cards.actionCardsSeaside);
        cards.addAll(Cards.actionCardsAlchemy);
        cards.addAll(Cards.actionCardsProsperity);
        cards.addAll(Cards.actionCardsCornucopia);
        cards.addAll(Cards.actionCardsHinterlands);
        
        cards.add(Cards.princess);
        cards.add(Cards.bagOfGold);
        cards.add(Cards.diadem);
        cards.add(Cards.followers);
        cards.add(Cards.trustySteed);

        
        for(Card c : cards) {
            System.out.println("    <string name=\"" + c.getSafeName() + "_name\">" + xmlify(c.getName()) + "</string>");
            System.out.println("    <string name=\"" + c.getSafeName() + "_desc\">" + xmlify(c.getDescription()) + "</string>");
//            System.out.println();
        }
    }
    
    static String xmlify(String s) {
        String ret = s.replaceAll("'", "\\\\'"); //"\\'");
        ret = ret.replaceAll("  ", " ");
        return ret;
    }
}
