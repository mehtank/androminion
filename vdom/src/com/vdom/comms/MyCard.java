package com.vdom.comms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import com.vdom.core.Cards;
import com.vdom.core.Util.MultilevelComparator;

public class MyCard implements Serializable {
	private static final long serialVersionUID = -1367468781663470597L;

	public int id;
	public String name;
	public String expansion;
	public String originalExpansion;
	public String desc;
	
	public String originalSafeName;
	public String originalName;

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
	public boolean isBane = false;
	public boolean isShelter = false;
	public boolean isRuins = false;
	public boolean isKnight = false;
	
	public static final int SUPPLYPILE = 1;
	public static final int MONEYPILE = 2;
	public static final int VPPILE = 3;
	public static final int PRIZEPILE = 4;
	public static final int NON_SUPPLY_PILE = 5;	// Used for DA cards (for now)
	public static final int SHELTER_PILES = 6;
	public static final int RUINS_PILES = 7;
	public static final int KNIGHTS_PILES = 8;

	public int pile;

	 
	public MyCard(int id, String name, String originalSafeName, String originalName) {
		this.id = id;
		this.name = name;
		this.originalSafeName = originalSafeName;
		this.originalName = originalName;
		this.isKnight = originalName.equals("VirtualKnight");
	}
	
	public String toString() {
		return "Card #" + id + " (" + cost + ") " + name + ": " + desc;
	}
	
	static public class CardNameComparator implements Comparator<MyCard> {
		@Override
		public int compare(MyCard card0, MyCard card1) {
			return card0.name.compareTo(card1.name);
		}
	}

	static public class CardCostComparator implements Comparator<MyCard> {
		@Override
		public int compare(MyCard card0, MyCard card1) {
			if(card0.cost < card1.cost) {
				return -1;
			} else if(card0.cost > card1.cost) {
				return 1;
			} else if(card0.isKnight) {
				return -1;
			} else if(card1.isKnight) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	static public class CardPotionComparator implements Comparator<MyCard> {
		@Override
		public int compare(MyCard card0, MyCard card1) {
			if(card0.costPotion) {
				if(card1.costPotion) {
					return 0;
				} else {
					return 1;
				}
			} else if(card1.costPotion) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	static public class CardTypeComparator implements Comparator<MyCard> {
		@Override
		public int compare(MyCard card0, MyCard card1) {
			if(card0.isAction) {
				if(card1.isAction) {
					return 0;
				} else {
					return -1;
				}
			} else if(card1.isAction) {
				return 1;
			} else if(card0.isTreasure || card0.isPotion) {
				if(card1.isTreasure || card1.isPotion) {
					return 0;
				} else {
					return -1;
				}
			} else if(card1.isTreasure || card1.isPotion) {
				return 1;
			} else {
				return 0;
			}
		}
	}
	
	/**
	 * Comparator for sorting cards by cost, potion and then by name
	 * Used for sorting on table
	 */
	static public class CardCostNameComparator extends MultilevelComparator<MyCard> {
		private static final ArrayList<Comparator<MyCard>> cmps = new ArrayList<Comparator<MyCard>>();
		static {
			cmps.add(new CardCostComparator());
			cmps.add(new CardPotionComparator());
			cmps.add(new CardNameComparator());
		}
		public CardCostNameComparator() {
			super(cmps);
		}
	}
	
	/**
	 * Comparator for sorting cards in hand.
	 * Sort by type then by cost and last by name
	 */
	static public class CardHandComparator extends MultilevelComparator<MyCard> {
		private static final ArrayList<Comparator<MyCard>> cmps = new ArrayList<Comparator<MyCard>>();
		static {
			cmps.add(new CardTypeComparator());
			cmps.add(new CardCostComparator());
			cmps.add(new CardNameComparator());
		}
		public CardHandComparator() {
			super(cmps);
		}
	}
}
