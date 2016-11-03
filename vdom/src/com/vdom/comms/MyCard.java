package com.vdom.comms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

import com.vdom.core.Util.MultilevelComparator;

public class MyCard implements Serializable {

	private static final long serialVersionUID = 4918045082221992494L;
	
	public int id;
	public String name;
	public String expansion;
	public String originalExpansion;
	public String desc;
	
	public String originalSafeName;
	public String originalName;

	public int cost = 0;
	public int debtCost = 0;
	public boolean costPotion = false;
	public int vp = 0;
	public int gold = 0;

	public boolean isVictory  = false;
	public boolean isCurse    = false;
	public boolean isTreasure = false;
	public boolean isAction   = false;
	public boolean isReaction = false;
	public boolean isAttack   = false;
	public boolean isDuration = false;
	public boolean isPrize    = false;
	public boolean isPotion   = false;
	public boolean isBane     = false;
	public boolean isObeliskCard  = false;
	public boolean isShelter  = false;
	public boolean isRuins    = false;
	public boolean isLooter   = false;
	public boolean isKnight   = false;
	public boolean isOverpay  = false;
	public boolean isEvent     = false;
	public boolean isReserve   = false;
	public boolean isTraveller = false;
	public boolean isCastle    = false;
	public boolean isGathering = false;
	public boolean isLandmark  = false;
	public boolean isBlackMarket = false;
	public boolean isStash    = false;
	
	public static final int SUPPLYPILE = 1;
	public static final int MONEYPILE = 2;
	public static final int VPPILE = 3;
	public static final int PRIZEPILE = 4;
	public static final int NON_SUPPLY_PILE = 5;	// Used for DA cards (for now)
	public static final int SHELTER_PILES = 6;
	public static final int VARIABLE_CARDS_PILE = 7;
	public static final int BLACKMARKET_PILE = 8;
	public static final int EVENTPILE = 9;

	public int pile;

	 
	public MyCard(int id, String name, String originalSafeName, String originalName) {
		this.id = id;
		this.name = name;
		this.originalSafeName = originalSafeName;
		this.originalName = originalName;
	}
	
	/* This method is now unused. It had been copied to CardView.java
	 * to simplify card type internationalization.
	 */
	public String GetCardTypeString()
    {
        String cardType = "";
        
        if (isAction)
        {
            cardType += "Action ";
            
            if (isAttack)
            {
                cardType += "- Attack ";
            }
            
            if (isLooter)
            {
                cardType += "- Looter ";
            }
            
            if (isRuins)
            {
                cardType += "- Ruins ";
            }
            
            if (isPrize)
            {
                cardType += "- Prize ";
            }
            
            if (isReaction)
            {
                cardType += "- Reaction ";
            }
            
            if (isDuration)
            {
                cardType += "- Duration ";
            }
            
            if (isVictory)
            {
                cardType += "- Victory ";
            }
            
            if (isKnight)
            {
                cardType += "- Knight ";
            }
            
            if (isShelter)
            {
                cardType += "- Shelter";
            }
        }
        else if (isTreasure)
        {
            cardType += "Treasure ";
            
            if (isVictory)
            {
                cardType += "- Victory ";
            }
            
            if (isReaction)
            {
                cardType += "- Reaction ";
            }
            
            if (isPrize)
            {
                cardType += "- Prize ";
            }
        }
        else if (isVictory)
        {
            cardType += "Victory ";
            
            if (isShelter)
            {
                cardType += "- Shelter";
            }
            
            if (isReaction)
            {
                cardType += "- Reaction";
            }
        }
        else if (isEvent)
        {
            cardType += "Event";
        }
        else if (name.equalsIgnoreCase("hovel"))
        {
            cardType += "Reaction - Shelter";
        }

		if (isCastle) {
			cardType += "- Castle";
		}
        
        return cardType;
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
	
	static public class CardDebtComparator implements Comparator<MyCard> {
		@Override
		public int compare(MyCard card0, MyCard card1) {
			if(card0.debtCost < card1.debtCost) {
				return -1;
			} else if(card0.debtCost > card1.debtCost) {
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
	
	static private class CardNonSupplyGroupComparator implements Comparator<MyCard> {
		@Override
		public int compare(MyCard card0, MyCard card1) {
			return getCardNonSupplyGroup(card0) - getCardNonSupplyGroup(card1);
		}

		private int getCardNonSupplyGroup(MyCard c) {
			if (c.originalSafeName.equals("TreasureHunter")
					|| c.originalSafeName.equals("Warrior")
					|| c.originalSafeName.equals("Hero")
					|| c.originalSafeName.equals("Champion"))
				return 1;
			if (c.originalSafeName.equals("Soldier")
					|| c.originalSafeName.equals("Fugitive")
					|| c.originalSafeName.equals("Disciple")
					|| c.originalSafeName.equals("Teacher"))
				return 2;
			return 3;
		}
	}
	
	static private class CardEventLandmarkTypeComparator implements Comparator<MyCard> {
		@Override
		public int compare(MyCard a, MyCard b) {
			return getCardTypeOrder(a) - getCardTypeOrder(b);
		}

		private int getCardTypeOrder(MyCard c) {
			if (c.isEvent) return 1;
			return 2;
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
			cmps.add(new CardDebtComparator());
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
	
	/**
	 * Comparator for sorting cards in hand.
	 * Sort by type then by cost and last by name
	 */
	static public class CardNonSupplyComparator extends MultilevelComparator<MyCard> {
		private static final ArrayList<Comparator<MyCard>> cmps = new ArrayList<Comparator<MyCard>>();
		static {
			cmps.add(new CardNonSupplyGroupComparator());
			cmps.add(new CardCostComparator());
			cmps.add(new CardNameComparator());
		}
		public CardNonSupplyComparator() {
			super(cmps);
		}
	}
	
	/**
	 * Comparator for sorting cards in event/landmark pile.
	 * Sort by type then by cost and last by name
	 */
	static public class CardEventLandmarkComparator extends MultilevelComparator<MyCard> {
		private static final ArrayList<Comparator<MyCard>> cmps = new ArrayList<Comparator<MyCard>>();
		static {
			cmps.add(new CardEventLandmarkTypeComparator());
			cmps.add(new CardCostComparator());
			cmps.add(new CardNameComparator());
		}
		public CardEventLandmarkComparator() {
			super(cmps);
		}
	}
}
