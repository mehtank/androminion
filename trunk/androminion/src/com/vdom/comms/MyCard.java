package com.vdom.comms;

import java.io.Serializable;

public class MyCard implements Serializable {
	private static final long serialVersionUID = -1367468781663470598L;

	public int id;
	public String name;
	public String expansion;
	public String desc;

	public int cost = 0;
	public boolean costPotion = false;
	public int vp = 0;
	public int gold = 0;

	public boolean isVictory = false;
	public boolean isCurse = false;
	public boolean isTreasure = false;
	public boolean isAction = false;
	public boolean isReaction = false;
	public boolean isAttack = false;
	public boolean isDuration = false;
	public boolean isPrize = false;
	public boolean isPotion = false;
	
	public static final int SUPPLYPILE = 1;
	public static final int MONEYPILE = 2;
	public static final int VPPILE = 3;
	public static final int PRIZEPILE = 4;

	public int pile;
	 
	public MyCard(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public String toString() {
		return "Card #" + id + " (" + cost + ") " + name + ": " + desc;
	}
}
