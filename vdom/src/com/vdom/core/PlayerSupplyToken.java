package com.vdom.core;

public enum PlayerSupplyToken {
	PlusOneCard(1), PlusOneAction(2), PlusOneBuy(3), PlusOneCoin(4), MinusTwoCost(5), Trashing(6);
	
	private int id;
	
	private PlayerSupplyToken(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static PlayerSupplyToken getById(int id) {
		for (PlayerSupplyToken token : PlayerSupplyToken.values()) {
			if (token.id == id)
				return token;
		}
		return null;
	}
}
