package com.mehtank.androminion.ui;

import java.util.ArrayList;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;
import com.mehtank.androminion.util.CardGroup;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EventObject;


import android.view.animation.ScaleAnimation;

public class OrderCardsView extends BottomInputView implements OnItemClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "OrderCardsView";
	
	/**
	 * The main view, is shown by hsv to be side-scrollable
	 */
	LinearLayout ll;
	/**
	 * Contains ll and is the main content view displayed by the BottomInputView
	 */
	HorizontalScrollView hsv;
	
	/*
	 * The CardGroup objects are the Adapters and hence contain the actual information.
	 * The GridViews are the table of cards
	 * The LinearLayout contain this GridView and a Title
	 */

	/*
	 * orig and ordered: show both next to each other; the user clicks on orig-elements in the
	 * wished order. ordered shows the resulting choice
	 */
	CardGroup orig;
	GridView origGV;
	LinearLayout origCS; // shows original order(?). Not shown in standard layout

	CardGroup ordered;
	GridView orderedGV;
	LinearLayout orderedCS; // not shown in standard layout

	DragNDropListView touch;
	LinearLayout touchCS;

	Button select, reset;

	int[] cards; // cards is an array of card-IDs in the original order. 
	
	ArrayList<Integer> orderedCards = new ArrayList<Integer>(); // this is an array of indices into cards[]
	ArrayList<Integer> origCards = new ArrayList<Integer>(); // this is an array of indices into cards[]

	/**
	 * Generate /orig/ and /ordered/ from origCards and orderedCards
	 */
	void orderCardGroups() {
		int[] cs = new int[origCards.size()];
		for (int i=0; i<cs.length; i++)
			cs[i] = cards[origCards.get(i)];
    	GameTableViews.newCardGroup(orig, cs);

    	cs = new int[orderedCards.size()];
		for (int i=0; i<cs.length; i++)
			cs[i] = cards[orderedCards.get(i)];
    	GameTableViews.newCardGroup(ordered, cs);

        canSelect();
	}

	public OrderCardsView (GameActivity top, String header, int[] cards) {
		super(top, header); // initializes ll by calling makeContentView
		this.top = top;

		this.cards = cards;
		for (int i=0; i<cards.length; i++)
			origCards.add(i);

		/*
		 * Two ListViews containing GridVidws showing /orig/ and /ordered/
		 */
		orig = new CardGroup(top, false);
    	origGV = GameTableViews.makeGV(top, orig, 1);
    	origGV.setOnItemClickListener(this);
       	origCS = (GameTableViews.myCardSet(top, top.getString(R.string.cards), origGV, null));

		ordered = new CardGroup(top, false);
    	orderedGV = GameTableViews.makeGV(top, ordered, 1);
    	orderedGV.setOnItemClickListener(this);
    	orderedCS = (GameTableViews.myCardSet(top, top.getString(R.string.top_of_deck), orderedGV, null));

    	/*
    	 * DragNDropListView showing /orig/
    	 */
		touch = new DragNDropListView(top);
		final float scale = getResources().getDisplayMetrics().density;
		System.out.println(scale);
		touch.setLayoutParams(new LinearLayout.LayoutParams((int) (getResources().getDimension(R.dimen.cardWidth)), ViewGroup.LayoutParams.WRAP_CONTENT));
		touch.setDropListener(new DragNDropListView.DragListener() {
			@Override
			public void onDrag(int from, int to) { // Do this on Drag/Drop: reorder cards
				int c = origCards.get(from);
				origCards.remove(from);
				origCards.add(to, c);
				orderCardGroups();
				// TODO Auto-generated method stub
			}
		});
        touch.setAdapter(orig); // orig is now the adapter of origGV /and/ touch
    	touchCS = (GameTableViews.myCardSet(top, top.getString(R.string.top_of_deck), touch, null));

    	/*
    	 * Accept-button
    	 */
    	select = new Button(top);
    	select.setText(R.string.accept);
        select.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { go(); }
        });

        /*
         * Reset-button, not shown in standard layout
         */
    	reset = new Button(top);
    	reset.setText(R.string.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { reset(); }
        });

        /*
         * instructions: "Drag/drop the cards blablabla
         */
        TextView inst = new TextView(top);
        inst.setText(R.string.order_cards_summary);

		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.addView(select);
		/*
		 * The following two make us the drag/drop
		 */
		ll.addView(touchCS);
		ll.addView(inst);
		/*
		 * The following would make us click each card in order
		 */
		// ll.addView(origCS); // original order
		// ll.addView(orderedCS); // order we want to submut
		// ll.addView(reset); // reset to old order
		ll.setPadding(0, 0, 0, 15);

    	orderCardGroups();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int i = origGV.getPositionForView(view);
		if (i != AdapterView.INVALID_POSITION) {
			int c = origCards.get(i);
			origCards.remove(i);
			orderedCards.add(0, c);
		} else {
			i = orderedGV.getPositionForView(view);
			if (i != AdapterView.INVALID_POSITION) {
				int c = orderedCards.get(i);
				orderedCards.remove(i);
				origCards.add(0, c);
			}
		}
		orderCardGroups();
	}
	/**
	 * Make the Accept-button clickable
	 */
	private void canSelect() {
		select.setClickable(true);
		select.setTextColor(Color.BLACK);
	}

	/**
	 * 'accept' pressed: return cards in order and close view
	 */
	private void go() {
		((FrameLayout) this.getParent()).removeView(this);
//		top.handle(new Event(Event.EType.CARDORDER).setObject(orderedCards.toArray(new Integer[0])));
		int[] is = new int[origCards.size()];
		for (int i = 0; i < origCards.size(); i++)
			is[i] = origCards.get(i);

		top.handle(new Event(Event.EType.CARDORDER).setObject(new EventObject(is)));
	}
	/**
	 * put origCards back in order 1, 2, 3, 4, ...
	 */
	private void reset() {
		origCards.clear();
		orderedCards.clear();
		for (int i=0; i<cards.length; i++)
			origCards.add(i);
		orderCardGroups();
	}


	/**
	 * Genrate a horizontal scroll view containing a (horizontal) list view that shows what we want
	 */
	@Override
	protected View makeContentView(GameActivity activity) {
		ll = new LinearLayout(top);
		
		hsv = new HorizontalScrollView(top);/* {
    		@Override
    		public void onSizeChanged (int w, int h, int oldw, int oldh) {
    			i += 20;
    			RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(w, Math.max(h, oldh) + i); // this doesn't work
    			setLayoutParams(p);
    		}
    	};*/
		hsv.addView(ll);

		return hsv;
	}
}