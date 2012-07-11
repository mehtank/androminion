package com.mehtank.androminion.ui;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;
import com.mehtank.androminion.ui.CardView.CardState;
import com.mehtank.androminion.util.Achievements;
import com.mehtank.androminion.util.CardGroup;
import com.mehtank.androminion.util.HapticFeedback;
import com.mehtank.androminion.util.HapticFeedback.AlertType;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EventObject;
import com.vdom.comms.GameStatus;
import com.vdom.comms.MyCard;
import com.vdom.comms.SelectCardOptions;
import com.vdom.comms.SelectCardOptions.PickType;

public class GameTable extends LinearLayout implements OnSharedPreferenceChangeListener, OnItemClickListener, OnItemLongClickListener {
	private final GameActivity top;

	ArrayList<String> allPlayers = new ArrayList<String>();

	GridView handGV, playedGV, islandGV, villageGV;
	CardGroup hand, played, island, village;
	View islandColumn, villageColumn;
	TextView playedHeader;
	LinearLayout myCards;

	GridView moneyPileGV, vpPileGV, supplyPileGV, prizePileGV;
	CardGroup moneyPile, vpPile, supplyPile, prizePile;

	LinearLayout tr;
	LinearLayout gameOver;

	View supply;
	LinearLayout turnView;
	View myCardView;
	private static int[] costs = {};

	TextView actionText;
	TextView largeRefText;
	LinearLayout deckStatus;
	TurnView turnStatus;
	Button select, pass;
    String indicator;
	GameScrollerView gameScroller;
	TextView latestTurn;
	SelectStringView sv;

	Achievements achievements;
	CardAnimator animator;

	private class CardInfo {
		public CardState cs;
		public CardGroup parent;
		public int pos;

		public CardInfo(CardState cs, CardGroup parent, int pos) {
			this.cs = cs;
			this.parent = parent;
			this.pos = pos;
		}
	}
	ArrayList<CardInfo> openedCards = new ArrayList<CardInfo>();
	int maxOpened = 0;
	boolean exactOpened = true;
	boolean myTurn;

	boolean finalStatsReported = false;

	double textScale = GameTableViews.textScale;

	private HelpView helpView;

	private void initTable() {
    	moneyPile = new CardGroup(top, true);
    	moneyPileGV = (GridView) findViewById(R.id.moneyPileGV);
    	moneyPileGV.setAdapter(moneyPile);
    	moneyPileGV.setOnItemClickListener(this);
    	moneyPileGV.setOnItemLongClickListener(this);
    	
    	vpPile = new CardGroup(top, true);
    	vpPileGV = (GridView) findViewById(R.id.vpPileGV);
    	vpPileGV.setAdapter(vpPile);
    	vpPileGV.setOnItemClickListener(this);
    	vpPileGV.setOnItemLongClickListener(this);
    	
    	supplyPile = new CardGroup(top, true);
    	supplyPileGV = (GridView) findViewById(R.id.supplyPileGV);
    	supplyPileGV.setAdapter(supplyPile);
    	supplyPileGV.setOnItemClickListener(this);
    	supplyPileGV.setOnItemLongClickListener(this);
    	
    	prizePile = new CardGroup(top, true);
    	prizePileGV = (GridView) findViewById(R.id.prizePileGV);
    	prizePileGV.setAdapter(prizePile);
    	prizePileGV.setOnItemClickListener(this);
    	prizePileGV.setOnItemLongClickListener(this);
	}

	private void initHand() {
		hand = new CardGroup(top, false);
		// hand.enableSorting(new MyCard.CardHandComparator());
    	handGV = (GridView) findViewById(R.id.handGV);
    	handGV.setAdapter(hand);
    	handGV.setOnItemClickListener(this);
    	handGV.setOnItemLongClickListener(this);
    	
    	played = new CardGroup(top, false);
    	playedGV = (GridView) findViewById(R.id.playedGV);
    	playedGV.setAdapter(played);
    	playedGV.setOnItemLongClickListener(this);
    	
		island = new CardGroup(top, false);
    	islandGV = (GridView) findViewById(R.id.islandGV);
    	islandGV.setAdapter(island);
    	islandGV.setOnItemLongClickListener(this);
    	islandColumn = findViewById(R.id.islandColumn);
    	
		village = new CardGroup(top, false);
    	villageGV = (GridView) findViewById(R.id.villageGV);
    	villageGV.setAdapter(village);
    	villageGV.setOnItemLongClickListener(this);
    	villageColumn = findViewById(R.id.villageColumn);

    	playedHeader = (TextView) findViewById(R.id.playedHeader);
    	
    	//only for help
    	myCardView = findViewById(R.id.myCardView);
	}
	
	private void initTurnPanel() {
		turnView = (LinearLayout) findViewById(R.id.turnView);
		
		select = (Button) findViewById(R.id.select);
        setSelectText(SelectCardOptions.PickType.SELECT);
        select.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { cardSelected((Button) v); }
        });
        
        pass = (Button) findViewById(R.id.pass);
        pass.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { cardSelected((Button) v); }
        });
        
        deckStatus = (LinearLayout) findViewById(R.id.deckStatus);

    	turnStatus = new TurnView(top, largeRefText);
    	turnStatus.setTextSize(12.0f);
    	turnView.addView(turnStatus, 2);
    	
    	actionText = (TextView) findViewById(R.id.actionText);
	}

	public GameTable(Context context) {
	    this(context, null);
	}
	
	public GameTable(Context context, AttributeSet attrs) {
	    this(context, attrs, 0);
	}
	
	public GameTable(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs); //TODO remove this workaround
		//super(context, attrs, defStyle); // fails with exception...
		this.top = (GameActivity) context;

		setOrientation(VERTICAL);
		
		animator = new CardAnimator();

        try {
            achievements = new Achievements(top);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        LayoutInflater.from(context).inflate(R.layout.gametableview, this, true);

    	largeRefText = (TextView) findViewById(R.id.largeRefText);
    	
    	supply = findViewById(R.id.supply);
    	initTable();
    	tr = (LinearLayout) findViewById(R.id.tr);
    	initHand();
    	initTurnPanel();
    	gameOver = (LinearLayout) findViewById(R.id.gameOver);
    	gameScroller = (GameScrollerView) findViewById(R.id.gameScroller);
    	gameScroller.setGameEvent("Dominion app loaded!", true, 0);
    	
    	helpView = new HelpView(this.top, new View[] {supply, turnView, myCardView, gameScroller}, new View[] {tr, supply, supply, tr});
    }

	public void showHelp(int page) {
		try {
			top.addView(helpView);
		} catch (IllegalStateException e) {}
		helpView.showHelp(page);
	}


	public void logToggle() {
		if (gameScroller.getVisibility() != VISIBLE)
	    	gameScroller.setVisibility(VISIBLE);
		else
			gameScroller.setVisibility(GONE);
	}

	public void newGame(MyCard[] cards, String[] players) {
		GameTableViews.clearCards();
		openedCards.clear();
		moneyPile.clear();
		vpPile.clear();
		supplyPile.clear();
		hand.clear();
		played.clear();
		island.clear();
		village.clear();
		allPlayers.clear();

		actionText.setText("");
		deckStatus.removeAllViews();
		gameScroller.clear();
		gameScroller.setNumPlayers(players.length);
		gameOver.setVisibility(GONE);
		tr.setVisibility(VISIBLE);

		for (MyCard c : cards)
			addCardToTable(c);
		for (String s : players)
			addPlayer(s);

	    boolean platInPlay = false;
        for (MyCard c : cards)
            if(c.originalSafeName.equals("Platinum")) {
                platInPlay = true;
                break;
            }

        boolean colonyInPlay = false;
        for (MyCard c : cards)
            if(c.originalSafeName.equals("Colony")) {
                colonyInPlay = true;
                break;
            }

        boolean potionInPlay = false;
        for (MyCard c : cards)
            if(c.isPotion) {
                potionInPlay = true;
                break;
            }

        if(potionInPlay && platInPlay)
            moneyPileGV.setNumColumns(5);
        else
            moneyPileGV.setNumColumns(4);

        if(!colonyInPlay)
            vpPileGV.setNumColumns(4);
        else
            vpPileGV.setNumColumns(5);

		top.nosplash();
		gameScroller.setGameEvent(top.getString(R.string.game_loaded), true, 0);
	}

	public void addCardToTable(MyCard c) {
		GameTableViews.addCard(c.id, c);

		if (c.pile == MyCard.MONEYPILE)
			moneyPile.addCard(c);
		else if (c.pile == MyCard.VPPILE)
			vpPile.addCard(c);
		else if (c.pile == MyCard.SUPPLYPILE)
			supplyPile.addCard(c);
		else if (c.pile == MyCard.PRIZEPILE)
		    prizePile.addCard(c);
	}

	private void canSelect() {
		select.setClickable(true);
	}
	private void cannotSelect() {
		select.setClickable(false);
	}

	boolean isAcceptable(MyCard c, CardGroup parent) {
		if (sco.fromHand && (parent != hand)) return false;
		else if (sco.fromTable) {
			if (sco.fromPrizes) {
				if ((parent != vpPile)
				&&  (parent != moneyPile)
				&&  (parent != supplyPile)
				&&  (parent != prizePile)) return false;
			} else {
				if ((parent != vpPile)
				&&  (parent != moneyPile)
				&&  (parent != supplyPile)) return false;
			}
		} else if (sco.fromPrizes) {
			if (parent != prizePile) return false;
		}

		return sco.checkValid(c, getCardCost(c));
	}

	SelectCardOptions sco = null;

	boolean firstPass = false;
	boolean canClick = true;
	String prompt = "";

	void resetButtons() {
		CharSequence selectText = select.getText();
		pass.setText(selectText.subSequence(0, selectText.length()-1));
        selectButtonState();
		actionText.setText(prompt);
		canClick = true;
	}

	void passButtons() {
        select.setText(pass.getText() + "!");
		pass.setText(R.string.confirm_no);
		actionText.setText(R.string.confirmation);

		for (CardInfo ci : openedCards) {
			CardState cs = ci.cs;
			cs.opened = false;
			cs.order = -1;
			cs.indicator = sco.getPickType().indicator();
			ci.parent.updateState(ci.pos, cs);
		}
		openedCards.clear();

		canClick = false;
		canSelect();
	}
	public void cardSelected(Button b) {
		if (sco == null)
			return;

		if (b == select) {
			if (firstPass) {
				top.handle(new Event(Event.EType.CARD)
								.setInteger(0));
			} else {
				int[] cards = new int[openedCards.size()];
				for (int i = 0; i < openedCards.size(); i++) {
					CardInfo ci = openedCards.get(i);
					if (!isAcceptable(ci.cs.c, ci.parent))
						return;
					cards[i] = ci.cs.c.id;
				}

                if (sco.getPickType() == PickType.SELECT_WITH_ALL && openedCards.size() == 0 && !select.getText().toString().endsWith("!")) {
				    // Hack to notify that "All" was selected
	                top.handle(new Event(Event.EType.CARD)
	                            .setInteger(1)
	                            .setObject(new EventObject(new int[] { -1 })));
                } else if ((sco.getPickType() == PickType.PLAY_IN_ORDER || sco.getPickType() == PickType.PLAY) && openedCards.size() == 0 && !select.getText().toString().endsWith("!")) {
                    // Hack to notify that "All" was selected
                    top.handle(new Event(Event.EType.CARD).setInteger(1).setObject(new EventObject(new int[] { -1 })));
                } else {
				    top.handle(new Event(Event.EType.CARD)
								.setInteger(openedCards.size())
								.setObject(new EventObject(cards)));
				}
			}
		} else if (b == pass) {
			if (!sco.isPassable())
				return;
			if (firstPass) {
				firstPass = false;
				resetButtons();
			} else {
				firstPass = true;
				passButtons();
			}
			return;
		} else
			return;

		for (CardInfo ci : openedCards) {
			CardState cs = ci.cs;
			cs.opened = false;
			cs.order = -1;
			cs.indicator = sco.getPickType().indicator();
			ci.parent.updateState(ci.pos, cs);
		}
		openedCards.clear();
		sco = null;
		pass.setVisibility(INVISIBLE);
		select.setVisibility(INVISIBLE);
		firstPass = false;
		resetButtons();
	}

	public void selectString(String title, String[] options) {
		HapticFeedback.vibrate(getContext(),AlertType.SELECT);
		new SelectStringView(top, title, options);
	}

	public void selectCard(SelectCardOptions sco, String s, int maxOpened, boolean exactOpened) {
		this.sco = sco;

		this.maxOpened = maxOpened;
		this.exactOpened = exactOpened;

		prompt = s;

		firstPass = false;
		resetButtons();

		HapticFeedback.vibrate(getContext(),AlertType.SELECT);
		select.setVisibility(VISIBLE);
		if (sco.isPassable()) {
			pass.setVisibility(VISIBLE);
			pass.setText(sco.passString);
		} else
			pass.setVisibility(INVISIBLE);

        selectButtonState();
	}

    protected void selectButtonState() {
        if (sco == null || sco.pickType == null) {
            setSelectText(PickType.SELECT);
            canSelect();
            return;
        }

        // nothing picked yet
        if (openedCards.size() == 0) {
            setSelectText(sco.pickType);
            if (sco.pickType == SelectCardOptions.PickType.SELECT_WITH_ALL) {
                canSelect();
            } else if (sco.pickType == SelectCardOptions.PickType.PLAY && maxOpened == 1 && sco.allowedCards.size() == 1) {
                canSelect();
            } else if (sco.pickType == SelectCardOptions.PickType.PLAY_IN_ORDER && maxOpened == 1) {
                canSelect();
            } else {
                cannotSelect();
            }
            return;
        }

        // something picked
        if (sco.pickType == SelectCardOptions.PickType.SELECT_WITH_ALL) {
            setSelectText(PickType.SELECT);
        } else if (sco.getPickType() == SelectCardOptions.PickType.PLAY_IN_ORDER) {
            setSelectText(SelectCardOptions.PickType.PLAY);
        } else {
            setSelectText(sco.pickType);
        }

        if (exactOpened && (openedCards.size() != maxOpened)) {
            cannotSelect();
        } else {
            canSelect();
		}
    }

    public void setSelectText(SelectCardOptions.PickType key) {
        indicator = key.indicator();
        String text;

        switch (key) {
        case SELECT:
        case SELECT_IN_ORDER:
            text = top.getString(R.string.select_button);
            break;
        case SELECT_WITH_ALL:
            text = top.getString(R.string.all_button);
            break;
        case PLAY:
        case PLAY_IN_ORDER:
            text = top.getString(R.string.play_button);
            break;
        case BUY:
            text = top.getString(R.string.buy_button);
            break;
        case DISCARD:
            text = top.getString(R.string.discard_button);
            break;
        case KEEP:
            text = top.getString(R.string.keep_button);
            break;
        case GIVE:
            text = top.getString(R.string.give_button);
            break;
        case TRASH:
            text = top.getString(R.string.trash_button);
            break;
        case UPGRADE:
            text = top.getString(R.string.upgrade_button);
            break;
        case MINT:
            text = top.getString(R.string.mint_button);
            break;
        case SWINDLE:
            text = top.getString(R.string.swindle_button);
            break;
        default:
            text = "";
            break;
        }
        select.setText(text);
	}

	public void orderCards(String header, int[] cards) {
		HapticFeedback.vibrate(getContext(),AlertType.SELECT);
		new OrderCardsView(top, header, cards);
	}

	private void updateCounts(GridView g, int[] supplySizes, int[] embargos) {
		for (int i = 0; i < g.getChildCount(); i++) {
			CardView cv = (CardView) g.getChildAt(i);
			if (cv.getCard() != null) {
				cv.setCountLeft(supplySizes[cv.getCard().id]);
				cv.setEmbargos(embargos[cv.getCard().id]);
			}
		}
	}

	public void setSupplySizes(int[] supplySizes, int[] embargos) {
		updateCounts(moneyPileGV, supplySizes, embargos);
		updateCounts(vpPileGV, supplySizes, embargos);
		updateCounts(supplyPileGV, supplySizes, embargos);
		updateCounts(prizePileGV, supplySizes, embargos);
	}

	public void finalStatus(GameStatus gs) {
		if (!gs.isFinal)
			return;

		int maxVP = 0;
		int minTurns = 10000;
		ArrayList<Integer> winners = new ArrayList<Integer>();

		for (int i=0; i<allPlayers.size(); i++) {
			if (gs.handSizes[i] > maxVP) {
				winners.clear(); winners.add(i);
				maxVP = gs.handSizes[i];
                minTurns = gs.turnCounts[i];
			} else if (gs.handSizes[i] == maxVP) {
                if (gs.turnCounts[i] < minTurns) {
					winners.clear(); winners.add(i);
                    minTurns = gs.turnCounts[i];
                } else if (gs.turnCounts[i] == minTurns)
					winners.add(i);
			}
		}

		try {
		    if(!finalStatsReported) {
		        finalStatsReported = true;
		        achievements.gameOver(allPlayers, winners);
		    }
		}
		catch(Exception e) {
		    e.printStackTrace();
		}

		boolean won = false;
		for (int i : winners)
			if (i == gs.whoseTurn)
				won = true;

		tr.setVisibility(GONE);
		gameOver.setVisibility(VISIBLE);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

        FinalView fv = new FinalView(top, this, allPlayers.get(gs.whoseTurn), gs.turnCounts[gs.whoseTurn],
				gs.embargos,
				gs.numCards[gs.whoseTurn], gs.supplySizes,
				gs.handSizes[gs.whoseTurn], won);
		fv.setLayoutParams(lp);
		gameOver.addView(fv);
	}

	public void achieved(String achievement) {
	    try {
	        achievements.achieved(achievement);
	    }
	    catch(Exception e) {
	        e.printStackTrace();
	    }
	}

	ArrayList<DeckView> dvs = new ArrayList<DeckView>();

	public void setStatus(GameStatus gs, String s, boolean newTurn) {
		if (s != null)
			gameScroller.setGameEvent(s, newTurn, gs.isFinal ? 0 : gs.turnCounts[gs.whoseTurn]);

		if (gs.isFinal) {
			HapticFeedback.vibrate(getContext(),AlertType.FINAL);
			finalStatus(gs);
			return;
		}
		if (allPlayers.size() <= gs.whoseTurn) {
			for (int i = allPlayers.size(); i < gs.whoseTurn; i++)
				addPlayer("--");
			addPlayer(gs.name);
		} else
			allPlayers.set(gs.whoseTurn, gs.name);

		if (newTurn) {
			myTurn = gs.whoseTurn == 0;
			if (myTurn)
				HapticFeedback.vibrate(getContext(),AlertType.TURNBEGIN);
		}

		turnStatus.setStatus(gs.turnStatus, gs.potions, myTurn);
		deckStatus.removeAllViews();
		for (int i=0; i<allPlayers.size(); i++) {
	        dvs.get(i).set(allPlayers.get(i) + top.getString(R.string.turn_header) + gs.turnCounts[i], gs.deckSizes[i], gs.handSizes[i], gs.numCards[i], gs.pirates[i], gs.victoryTokens[i], gs.whoseTurn == i);
			deckStatus.addView(dvs.get(i));
		}

		actionText.setText("");

        if(newTurn) {
            String header = top.getString(R.string.played_header);
            if(!myTurn) {
                header = allPlayers.get(gs.whoseTurn) + ":";
            }
            playedHeader.setText(header);
        }

		GameTableViews.newCardGroup(hand, gs.myHand);
		GameTableViews.newCardGroup(played, gs.playedCards);
		GameTableViews.newCardGroup(island, gs.myIsland);
		if (gs.myIsland.length > 0) {
			islandColumn.setVisibility(VISIBLE);
		} else {
			islandColumn.setVisibility(GONE);
		}
		GameTableViews.newCardGroup(village, gs.myVillage);
		if (gs.myVillage.length > 0) {
			villageColumn.setVisibility(VISIBLE);
		} else {
			villageColumn.setVisibility(GONE);
		}
		setSupplySizes(gs.supplySizes, gs.embargos);
		costs = gs.costs;
        setCardCosts(top.findViewById(android.R.id.content));
	}

	static int getCardCost(MyCard c) {
		if (c == null)
			return 0;
		if (costs != null && c.id < costs.length)
			return costs[c.id];
		return c.cost;
	}

	private void setCardCosts(View v) {
		if (v instanceof CardView) {
			((CardView) v).setCost(getCardCost(((CardView) v).getCard()));
		} else if (v instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
				setCardCosts(((ViewGroup) v).getChildAt(i));
		}
	}

	private void addPlayer(String name) {
		allPlayers.add(name);
		dvs.add(new DeckView(top));

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		dvs.get(dvs.size()-1).setLayoutParams(lp);
	}
	public String cardObtained(int i, String s) {
	    return top.getString(R.string.obtained, showCard(i, s, CardAnimator.ShowCardType.OBTAINED));
	}
	public String cardTrashed(int i, String s) {
        return top.getString(R.string.trashed, showCard(i, s, CardAnimator.ShowCardType.TRASHED));
	}
	public String cardRevealed(int i, String s) {
        return top.getString(R.string.revealed, showCard(i, s, CardAnimator.ShowCardType.REVEALED));
	}
	public String showCard (int card, String playerInt, CardAnimator.ShowCardType type) {
		int player = 0;
		CardView c = GameTableViews.getCardView(top, this, card);
		try {
			player = Integer.parseInt(playerInt);
		} catch (NumberFormatException e) {
			return "";
		}
		animator.init(dvs.get(player));
		animator.showCard(c, type);

		return allPlayers.get(player)+ ": " + c.getCard().name;
	}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("abc_header")) {
            if(largeRefText != null) {
                if(!sharedPreferences.getBoolean("abc_header", true)) {
                    largeRefText.setVisibility(GONE);
                } else {
                    largeRefText.setVisibility(VISIBLE);
                }
            }
        }
    }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		CardView clickedCard = (CardView) view;
		if (clickedCard == null)
			return;

		if (sco == null)
			return;

		if (!canClick)
			return;

        if (clickedCard.isChecked()) {
			HapticFeedback.vibrate(getContext(), AlertType.CLICK);
			for(int i=0;i<openedCards.size();i++){
				CardInfo ci = openedCards.get(i);
				if(ci.cs == clickedCard.getState() && ci.pos == position) {
					openedCards.remove(i);
					ci.cs.indicator = sco.getPickType().indicator();
					ci.cs.order = -1;
					ci.cs.opened = false;
		            clickedCard.setState(ci.cs);
		            selectButtonState();
		            break;
				}
			}
		} else {
			if (isAcceptable(clickedCard.getCard(), clickedCard.parent)) {
				HapticFeedback.vibrate(getContext(), AlertType.CLICK);
				if (openedCards.size() >= maxOpened) {
                    CardInfo ci = openedCards.get(0);
                    ci.cs.opened = false;
                    ci.cs.order = -1;
                    ci.cs.indicator = sco.getPickType().indicator();
                    ci.parent.updateState(ci.pos, ci.cs);
					openedCards.remove(0);
				}
                clickedCard.setChecked(true, sco.getPickType().indicator());
                CardInfo ci = new CardInfo(clickedCard.getState(), (CardGroup) parent.getAdapter(), position);
				openedCards.add(ci);
                selectButtonState();
			}
		}
		if (sco.ordered)
			for(int i=0;i < openedCards.size(); i++) {
				CardInfo ci = openedCards.get(i);
				ci.cs.opened = true;
				ci.cs.indicator = sco.getPickType().indicator();
				ci.cs.order = i;
                ci.parent.updateState(ci.pos, ci.cs);
			}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		CardView clickedCard = (CardView) view;
		return clickedCard.onLongClick(clickedCard);
	}
}
