package com.vdom.players;

import java.util.ArrayList;

import com.vdom.api.Card;
import com.vdom.api.Cards;
import com.vdom.api.GameType;

public class VDomPlayerMary extends VDomPlayerSarah {
    public boolean isAi() {
        return true;
    }

    public void setupGameVariables(GameType gameType, Card[] cardsInPlay) {
        super.setupGameVariables(gameType, cardsInPlay);
        
        onlyBuyEarlySingle = false;
        earlyCardBuys = new Card[] { Cards.militia, Cards.seaHag, Cards.familiar, Cards.youngWitch, Cards.torturer, Cards.thief, Cards.minion, Cards.saboteur, Cards.pirateShip, Cards.ghostShip, Cards.rabble, Cards.goons, Cards.followers, Cards.fortuneTeller, Cards.jester };
        earlyCardBuyMax = 3;
        
        ArrayList<Card> cards = new ArrayList<Card>();
//        for(Card c : valuedCards) {
//            cards.add(c);
//        }
        for(Card c : earlyCardBuys) {
            cards.add(c);
        }
        valuedCards = cards.toArray(new Card[0]);
        
        favorSilverGoldPlat = false;
    }

    @Override
    public String getPlayerName() {
        return "Mary";
    }
}
