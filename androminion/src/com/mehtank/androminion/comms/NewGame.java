package com.mehtank.androminion.comms;

import java.io.Serializable;

public class NewGame implements Serializable {
	private static final long serialVersionUID = 229362050690595201L;

	public MyCard[] cards = null;
	public String[] players = null;
	
	public NewGame(MyCard[] cards, String[] players) {			
		this.cards = cards;
		this.players = players;
	}
}
