package com.mehtank.androminion.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.Androminion.AlertType;
import com.mehtank.androminion.R;
import com.vdom.comms.Event;
import com.vdom.comms.GameStatus;
import com.vdom.comms.MyCard;
import com.vdom.comms.SelectCardOptions;
import com.vdom.comms.Event.EventObject;

public class GameTable extends LinearLayout implements OnClickListener, OnLongClickListener, OnSharedPreferenceChangeListener {
	Androminion top;

	ArrayList<String> allPlayers = new ArrayList<String>();
	ArrayList<Integer> turns = new ArrayList<Integer>();
	
	GridView handGV, playedGV, islandGV, villageGV;
	CardGroup hand, played, island, village;
	LinearLayout handCS, playedCS, islandCS, villageCS;
	TextView playedHeader;
	LinearLayout myCards;
	boolean playedAdded = false, islandAdded = false, villageAdded = false;
	
	GridView moneyPileGV, vpPileGV, supplyPileGV, prizePileGV;
	CardGroup moneyPile, vpPile, supplyPile, prizePile;
	
	LinearLayout tr;
	LinearLayout gameOver;
	
	View supply;
	View turnView;
	View myCardView;
	
	TextView actionText;
	TextView largeRefText;
	LinearLayout deckStatus;
	TurnView turnStatus;
	Button select, pass;
	GameScrollerView gameScroller;
	TextView latestTurn;
	SelectStringView sv;

	Achievements achievements;
	
	ArrayList<CardView> openedCards = new ArrayList<CardView>();
	int maxOpened = 0;
	boolean exactOpened = true;
	boolean myTurn;
	
	boolean finalStatsReported = false;
		
	double textScale = GameTableViews.textScale;

	private HelpView helpView;
	
	private LinearLayout makeTable(Context top) {
    	moneyPile = new CardGroup(top, this, this, true);
    	vpPile = new CardGroup(top, this, this, true);
    	supplyPile = new CardGroup(top, this, this, true, 8);
    	prizePile = new CardGroup(top, this, this, true);

    	moneyPileGV = GameTableViews.makeGV(top, moneyPile, 5);
    	vpPileGV = GameTableViews.makeGV(top, vpPile, 5);
    	supplyPileGV = GameTableViews.makeGV(top, supplyPile, 4);
    	prizePileGV = GameTableViews.makeGV(top, prizePile, 5);

    	LinearLayout table = new LinearLayout(top);
    	table.setOrientation(VERTICAL);
    	
    	table.addView(moneyPileGV);
    	table.addView(vpPileGV);
    	table.addView(supplyPileGV);
    	table.addView(prizePileGV);
    	table.setBackgroundResource(R.drawable.roundborder);
    	
    	return table;
	}
	
	private View makeMyCards(Context top) {
		hand = new CardGroup(top, this, this, false);
    	played = new CardGroup(top, this, this, false);
		island = new CardGroup(top, this, this, false);
		village = new CardGroup(top, this, this, false);

    	handGV = GameTableViews.makeGV(top, hand, 1);
    	playedGV = GameTableViews.makeGV(top, played, 1);
    	islandGV = GameTableViews.makeGV(top, island, 1);
    	villageGV = GameTableViews.makeGV(top, village, 1);

//    	handGV.setBackgroundResource(R.drawable.roundborder);
//    	playedGV.setBackgroundResource(R.drawable.roundborder);
//    	islandGV.setBackgroundResource(R.drawable.roundborder);
//    	villageGV.setBackgroundResource(R.drawable.roundborder);

    	playedHeader = new TextView(top);
    	
    	handCS = (GameTableViews.myCardSet(top, Strings.getString(top, R.string.hand_header), handGV, null));
    	playedCS = (GameTableViews.myCardSet(top, Strings.getString(top, R.string.played_header), playedGV, playedHeader));
    	islandCS = (GameTableViews.myCardSet(top, Strings.getString(top, R.string.island_header), islandGV, null));
    	villageCS = (GameTableViews.myCardSet(top, Strings.getString(top, R.string.village_header), villageGV, null));

    	islandCS.setBackgroundResource(R.drawable.roundborder);
    	villageCS.setBackgroundResource(R.drawable.roundborder);
    	
    	myCards = new LinearLayout(top);
    	myCards.setOrientation(HORIZONTAL);
    	
    	myCards.addView(handCS);
    	myCards.addView(playedCS);
		playedAdded = true;

    	for (int i=0; i<8; i++) 
    		hand.addCard(new MyCard(0, "default"));
    	    	
    	HorizontalScrollView sv = new HorizontalScrollView(top) {
    		boolean f = false;
    		@Override 
    		public void onSizeChanged (int w, int h, int oldw, int oldh) {
    			// System.err.println(" ******   onSizeChanged " + w + " " + h + " " + oldw + " " + oldh);
    			if ((w != 0) && (h != 0) && (oldw == 0) && (oldh == 0)) 
    				f = true;
    			else if ((w != 0) && (h != 0) && f) {
    		    	LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, Math.max(h, oldh));
    		    	setLayoutParams(p);
    			}
    		}
    	};
    	sv.addView(myCards);    	
    	myCardView = sv;
    	return sv;
	}
	private LinearLayout makeTurnPanel(Context top) {
    	select = new Button(top);
    	select.setVisibility(INVISIBLE);
    	select.setText(SelectCardOptions.SELECT);
        select.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { cardSelected((Button) v); }
        });
        LayoutParams p = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.FILL_PARENT, 
                1.0f
                );
        p.leftMargin = 0;
        p.rightMargin = 0;
        select.setLayoutParams(p);
        select.setPadding(0, 0, 0, 0);

    	pass = new Button(top);
    	pass.setVisibility(INVISIBLE);
    	pass.setText("Pass");
        pass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { cardSelected((Button) v); }
        });
        p = new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.FILL_PARENT,
                1.0f
				);
        p.leftMargin = 0;
        p.rightMargin = 0;
        pass.setLayoutParams(p);
        pass.setPadding(0, 0, 0, 0);

        LinearLayout buttons = new LinearLayout(top);
        buttons.addView(select);
        buttons.addView(pass);
    	
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        		LinearLayout.LayoutParams.FILL_PARENT,
        		LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 0;
        lp.rightMargin = 0;
        buttons.setLayoutParams(lp);
        
        deckStatus = new LinearLayout(top);
        deckStatus.setOrientation(VERTICAL);
        deckStatus.setLayoutParams(lp);
        DeckView.setTextScale(textScale);

    	turnStatus = new TurnView(top, largeRefText);
    	turnStatus.setTextSize((float) (turnStatus.getTextSize() * textScale));

    	actionText = new TextView(top);
		actionText.setTextSize((float) (actionText.getTextSize() * textScale));
    	
    	LinearLayout ll = new LinearLayout(top);
    	ll.setOrientation(VERTICAL);
    	ll.addView(buttons);
    	ll.addView(deckStatus);
    	ll.addView(turnStatus);
    	ll.addView(actionText);
    	
        lp = new LinearLayout.LayoutParams(
        		LinearLayout.LayoutParams.FILL_PARENT,
        		LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(lp);
    	
        turnView = ll;
    	return ll;
	}

    private LinearLayout makeLargeRef(Context top) {
        largeRefText = new TextView(top);
        largeRefText.setTextSize((float) (largeRefText.getTextSize() * 1.6f));
        largeRefText.setText("  ");
        LinearLayout ll = new LinearLayout(top);
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_HORIZONTAL);
        ll.setOrientation(HORIZONTAL);
        ll.addView(largeRefText);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(lp);

        if(!PreferenceManager.getDefaultSharedPreferences(top).getBoolean("abc_header", true)) {
            ll.setVisibility(GONE);
        }
        
        return ll;
    }
    
	private void makeGameOver(Context top) {
		gameOver = new LinearLayout(top);
		gameOver.setOrientation(LinearLayout.VERTICAL);
		
		TextView tv = new TextView(top);
		tv.setText("Game over!\n");
		
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        		LinearLayout.LayoutParams.FILL_PARENT,
        		LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);

		gameOver.addView(tv);
		gameOver.setLayoutParams(lp);
	}
	public GameTable(Androminion top) {
		super(top);
		this.top = top;
		    	
		setOrientation(VERTICAL);
		
        try {
            achievements = new Achievements(top);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
		
    	TextView titleText = new TextView(top);
    	titleText.setText(Strings.getString(top, R.string.title));
    	makeGameOver(top);

    	LinearLayout largeRef = makeLargeRef(top);
    	tr = new LinearLayout(top);
    	tr.addView(makeMyCards(top));
    	tr.addView(makeTurnPanel(top));
    	
    	addView(titleText);
    	
        addView(largeRef);
        
    	addView(supply = makeTable(top));
    	
    	addView(tr);
    	// addView(new TalkView(top));
    	gameScroller = new GameScrollerView(top, textScale);
    	/*
    	FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(
    			FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, 
    			Gravity.BOTTOM); 
    	gameScroller.setLayoutParams(fp);
    	top.addView(gameScroller);
    	gameScroller.setVisibility(INVISIBLE);
    	*/
    	
    	addView(gameScroller);
    	gameScroller.setGameEvent("Dominion app loaded!", true);
    	
    	helpView = new HelpView(this.top, new View[] {supply, turnView, myCardView, gameScroller}, new View[] {tr, supply, supply, tr});
    	// helpView = new HelpView(this.top, new View[] {supply, tr, tr, gameScroller}, new View[] {tr, supply, supply, tr});
    }
	
	public void showHelp(int page) {
		try {
			top.addView(helpView);
		} catch (IllegalStateException e) {}
		helpView.showHelp(page);
	}
	
	
	public void logToggle() {
		if (gameScroller.getVisibility() == INVISIBLE)
	    	gameScroller.setVisibility(VISIBLE);
		else
			gameScroller.setVisibility(INVISIBLE);
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
		turns.clear();
		
		actionText.setText("");
		deckStatus.removeAllViews();
		gameScroller.clear();
		gameScroller.setNumPlayers(players.length);
		gameOver.removeAllViews();
		tr.removeAllViews();
    	tr.addView(makeMyCards(top));
    	tr.addView(makeTurnPanel(top));
    	helpView.setShowViews(new View[] {supply, turnView, myCardView, gameScroller});
		
		for (MyCard c : cards)
			addCardToTable(c);
		for (String s : players)
			addPlayer(s);

	    boolean platInPlay = false;
        for (MyCard c : cards)
            if(c.name.equals("Platinum")) {
                platInPlay = true;
                break;
            }
		    
        boolean colonyInPlay = false;
        for (MyCard c : cards)
            if(c.name.equals("Colony")) {
                colonyInPlay = true;
                break;
            }
        
        boolean potionInPlay = false;
        for (MyCard c : cards)
            if(c.name.equals("Potion")) {
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
		gameScroller.setGameEvent(Strings.getString(top, R.string.game_loaded), true);

		showSupplySizes(); // TODO doesn't do anything
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
		select.setTextColor(Color.BLACK);
	}
	private void cannotSelect() {
		select.setClickable(false);
		select.setTextColor(Color.GRAY);
	}
	public void onClick(View v) {
		CardView clickedCard = (CardView) v;
		boolean opened = clickedCard.opened;
		
		if (clickedCard.c == null)
			return;
		
		if (sco == null) 
			return;

		if (!canClick) 
			return;
		
		if (opened) {
			top.alert(AlertType.CLICK);
			if (openedCards.contains(clickedCard))
				openedCards.remove(clickedCard);
			clickedCard.setOpened(false, -1);
			if (openedCards.size() == 0) {
			    if(sco != null && sco.getButtonText().equals(SelectCardOptions.SELECT_WITH_ALL)) {
			        select.setText(SelectCardOptions.SELECT_WITH_ALL);
			    }
			    else {
			        cannotSelect();
			    }
			}
			else if (exactOpened && (openedCards.size() != maxOpened))
				cannotSelect();
		} else {
			if (isAcceptable(sco, clickedCard)) {
				top.alert(AlertType.CLICK);
				if (openedCards.size() >= maxOpened) {
					openedCards.get(0).setOpened(false, -1);
					openedCards.remove(0);
				}
				clickedCard.setOpened(true, -1);
				openedCards.add(clickedCard);
				if (!exactOpened && (openedCards.size() > 0))
					canSelect();
				else if (exactOpened && (openedCards.size() == maxOpened))
					canSelect();
				
                if(sco != null && sco.getButtonText().equals(SelectCardOptions.SELECT_WITH_ALL)) {
                    select.setText(SelectCardOptions.SELECT);
                }
			}
		}
		if (sco.ordered)
			for (CardView c : openedCards)
				c.setOpened(true, openedCards.indexOf(c));
	}

	boolean isAcceptable(SelectCardOptions sco, CardView cv) {
		MyCard c = cv.c;
		if (sco.fromHand && (cv.parent != hand)) return false;
		if (sco.fromTable && (cv.parent != vpPile)
					  && (cv.parent != moneyPile) 
					  && (cv.parent != supplyPile)) return false;
		if (sco.fromPrizes && cv.parent != prizePile) return false;

		return sco.checkValid(c);
	}
	
	SelectCardOptions sco = null;
	
	boolean firstPass = false;
	boolean canClick = true;
	String prompt = "";
	
	void resetButtons() {
		CharSequence selectText = select.getText();
		pass.setText(selectText.subSequence(0, selectText.length()-1));

		if (sco != null && sco.getButtonText() != null) 
            setSelectText(sco.getButtonText());
		else 
            setSelectText(SelectCardOptions.SELECT);

		if(sco != null && sco.getButtonText().equals(SelectCardOptions.SELECT_WITH_ALL)) 
            canSelect();
		else
		    cannotSelect();
		
		actionText.setText(prompt);
		canClick = true;
	}
	void passButtons() {
		select.setText(pass.getText() + "!");	
		pass.setText(Strings.getString(top, R.string.confirm_no));
		actionText.setText(Strings.getString(top, R.string.confirmation));

		for (CardView cv : openedCards)
			cv.setOpened(false, -1);
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
					CardView cv = openedCards.get(i);
					if (!isAcceptable(sco, cv))
						return;
					cards[i] = cv.c.id;
				}
				
				if(sco != null && sco.getButtonText().equals(SelectCardOptions.SELECT_WITH_ALL) && openedCards.size() == 0 && !select.getText().toString().endsWith("!")) {
				    // Hack to notify that "All" was selected
	                top.handle(new Event(Event.EType.CARD)
	                            .setInteger(1)
	                            .setObject(new EventObject(new int[] { -1 })));
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

		for (CardView cv : openedCards)
			cv.setOpened(false, -1);
		openedCards.clear();
		sco = null;
		pass.setVisibility(INVISIBLE);
		select.setVisibility(INVISIBLE);
		firstPass = false;
		resetButtons();
	}

	public void selectString(String title, String[] options) {
		top.alert(AlertType.SELECT);
		new SelectStringView(top, title, options);
	}

	public void selectCard(SelectCardOptions sco, String s, int maxOpened, boolean exactOpened) {
		this.sco = sco;
		
		this.maxOpened = maxOpened;
		this.exactOpened = exactOpened;

		prompt = s;

		firstPass = false;
		resetButtons();
		
        if (sco != null) {
            if(sco.getButtonText() != null) {
                setSelectText(sco.getButtonText());
            }
        }
        
		top.alert(AlertType.SELECT);
		select.setVisibility(VISIBLE);
		if (sco.isPassable()) {
			pass.setVisibility(VISIBLE);
			pass.setText(sco.passString);
		} else
			pass.setVisibility(INVISIBLE);
		
		if(sco.getButtonText().equals(SelectCardOptions.SELECT_WITH_ALL)) {
		    canSelect();
		}
	}
	
	public void setSelectText(String key) {
	    // TODO: Change key from string to enum
	    String text;
	    if(key.equals(SelectCardOptions.SELECT)) {
	        text = Strings.getString(top, R.string.select_button);
	    } else if(key.equals(SelectCardOptions.BUY)) {
            text = Strings.getString(top, R.string.buy_button);
        } else if(key.equals(SelectCardOptions.PLAY)) {
            text = Strings.getString(top, R.string.play_button);
        } else if(key.equals(SelectCardOptions.DISCARD)) {
            text = Strings.getString(top, R.string.discard_button);
        } else if(key.equals(SelectCardOptions.KEEP)) {
            text = Strings.getString(top, R.string.keep_button);
        } else if(key.equals(SelectCardOptions.GIVE)) {
            text = Strings.getString(top, R.string.give_button);
        } else if(key.equals(SelectCardOptions.TRASH)) {
            text = Strings.getString(top, R.string.trash_button);
        } else if(key.equals(SelectCardOptions.UPGRADE)) {
            text = Strings.getString(top, R.string.upgrade_button);
        } else if(key.equals(SelectCardOptions.MINT)) {
            text = Strings.getString(top, R.string.mint_button);
        } else if(key.equals(SelectCardOptions.SWINDLE)) {
            text = Strings.getString(top, R.string.swindle_button);
        } else if(key.equals(SelectCardOptions.SELECT_WITH_ALL)) {
            text = Strings.getString(top, R.string.all_button);
        } else {
            text = key;
        }
        select.setText(text);
	}

	public void orderCards(String header, int[] cards) {
		top.alert(AlertType.SELECT);
		new OrderCardsView(top, this, header, cards);
	}

	public boolean onLongClick(View vin) {
		CardView cardView = (CardView) vin;
		
		top.alert(AlertType.LONGCLICK);
		String str = cardView.c.name;
		str = str.toLowerCase();
		
		StringTokenizer st = new StringTokenizer(str," ",false);
		String filename = "";
		while (st.hasMoreElements()) filename += st.nextElement();
		
		View v;

		int resID = getResources().getIdentifier("com.mehtank.androminion:drawable/" + filename, null, null);
		if (resID != 0) {
			ImageView im = new ImageView(top);
            im.setBackgroundResource(resID);
            im.setScaleType(ImageView.ScaleType.FIT_CENTER);
            v = im;
		} else {		
			str = "/sdcard/Dominion/images/full/" + filename + ".jpg";
			File f = new File(str);
			if (f.exists()) {
				Uri u = Uri.parse(str);
				ImageView im = new ImageView(top);
	            im.setImageURI(u);  
	            im.setScaleType(ImageView.ScaleType.FIT_CENTER);
	            v = im;
			} else {
				TextView tv = new TextView(top);
				tv.setPadding(15, 0, 15, 5);
				String text = ""; //cardView.c.name;
				if(cardView.c.expansion != null && cardView.c.expansion.length() != 0) {
				    text += "(" + cardView.c.expansion + ")\n";
				}
				text += cardView.c.desc;
				tv.setText( text );
				v = tv;
			}
		}
		new AlertDialog.Builder(top)
			.setTitle(cardView.c.name)
			.setView(v)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {} })
			.show();

		return true;
	}

	private void updateSizes(GridView g, int[] supplySizes, int[] embargos) {
		for (int i = 0; i < g.getChildCount(); i++) {
			CardView cv = (CardView) g.getChildAt(i);
			if (cv.c != null) {
				cv.setSize(supplySizes[cv.c.id]);
				cv.setEmbargos(embargos[cv.c.id]);
			}
		}
	}

	public void setSupplySizes(int[] supplySizes, int[] embargos) {
		updateSizes(moneyPileGV, supplySizes, embargos);
		updateSizes(vpPileGV, supplySizes, embargos);
		updateSizes(supplyPileGV, supplySizes, embargos);
		updateSizes(prizePileGV, supplySizes, embargos);
		showSupplySizes();  // TODO only needs to happen once
	}
	
	private void toggleView(GridView g, int mode) {
		for (int i = 0; i < g.getChildCount(); i++) {
			CardView cv = (CardView) g.getChildAt(i);
			if (cv.c != null)
				cv.swapNum(mode);
		}
	}
	
	public void showSupplySizes() {
		toggleView(moneyPileGV, CardView.SHOWCOUNT);
		toggleView(vpPileGV, CardView.SHOWCOUNT);
		toggleView(supplyPileGV, CardView.SHOWCOUNT);
		toggleView(prizePileGV, CardView.SHOWCOUNT);
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
				minTurns = turns.get(i);
			} else if (gs.handSizes[i] == maxVP) {
				if (turns.get(i) < minTurns) {
					winners.clear(); winners.add(i);
					minTurns = turns.get(i);
				} else if (turns.get(i) == minTurns) 
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
		
		tr.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);

		FinalView fv = new FinalView(top, this, allPlayers.get(gs.whoseTurn), turns.get(gs.whoseTurn),
				gs.embargos,
				gs.numCards[gs.whoseTurn], gs.supplySizes, 
				gs.handSizes[gs.whoseTurn], won);
		fv.setLayoutParams(lp);
		gameOver.addView(fv);
		tr.addView(gameOver);
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
			gameScroller.setGameEvent(s, newTurn);
		
		if (gs.isFinal) {
			top.alert(AlertType.FINAL);
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
			turns.set(gs.whoseTurn, turns.get(gs.whoseTurn) + 1);
			if (myTurn)
				top.alert(AlertType.TURNBEGIN);
		}
		
		turnStatus.setStatus(gs.turnStatus, gs.bridges, myTurn);
		deckStatus.removeAllViews();
		for (int i=0; i<allPlayers.size(); i++) {
	        dvs.get(i).set(allPlayers.get(i), gs.deckSizes[i], gs.handSizes[i], gs.numCards[i], gs.pirates[i], gs.victoryTokens[i], gs.whoseTurn == i);
			deckStatus.addView(dvs.get(i));
		}
		
		actionText.setText("");
 
        if(newTurn) {
            String header = Strings.getString(top, R.string.played_header);
            if(!myTurn) {
                header = allPlayers.get(gs.whoseTurn) + ":";
            }
            playedHeader.setText(header);
        }
		
		GameTableViews.newCardGroup(hand, gs.myHand);
		GameTableViews.newCardGroup(played, gs.playedCards);
		if (!playedAdded && (gs.playedCards.length > 0)) {
			myCards.addView(playedCS);
			playedAdded = true;
		}
		GameTableViews.newCardGroup(island, gs.myIsland);
		if (!islandAdded && (gs.myIsland.length > 0)) {
			myCards.addView(islandCS);
			islandAdded = true;
		}
		GameTableViews.newCardGroup(village, gs.myVillage);
		if (!villageAdded && (gs.myVillage.length > 0)) {
			myCards.addView(villageCS);
			villageAdded = true;
		}
		setSupplySizes(gs.supplySizes, gs.embargos);
	}
	private void addPlayer(String name) {
		allPlayers.add(name);
		turns.add(0);
		dvs.add(new DeckView(top));
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		dvs.get(dvs.size()-1).setLayoutParams(lp);
	}
	public String cardObtained(int i, String s) {
	    return Strings.format(top, R.string.obtained, showCard(i, s, DeckView.ShowCardType.OBTAINED));
	}
	public String cardTrashed(int i, String s) {
        return Strings.format(top, R.string.trashed, showCard(i, s, DeckView.ShowCardType.TRASHED));
	}
	public String cardRevealed(int i, String s) {
        return Strings.format(top, R.string.revealed, showCard(i, s, DeckView.ShowCardType.REVEALED));
	}
	public String showCard (int card, String playerInt, DeckView.ShowCardType type) {
		int player = 0;
		CardView c = GameTableViews.getCardView(top, this, card);
		try {
			player = Integer.parseInt(playerInt);
		} catch (NumberFormatException e) {
			return "";
		}
		dvs.get(player).showCard(c, type);
		
		return allPlayers.get(player)+ ": " + c.c.name;
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
}
