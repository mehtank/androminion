package com.mehtank.androminion.ui;

import java.util.ArrayList;

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mehtank.androminion.Androminion;
import com.mehtank.androminion.R;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EventObject;

public class OrderCardsView extends BottomInputView implements OnClickListener {
	LinearLayout ll;
	HorizontalScrollView hsv;
	
	CardGroup orig;
	GridView origGV;
	LinearLayout origCS;

	CardGroup ordered;
	GridView orderedGV;
	LinearLayout orderedCS;
	
	DragNDropListView touch;
	LinearLayout touchCS;
	
	Button select, reset;

	int[] cards;
	ArrayList<Integer> orderedCards = new ArrayList<Integer>();
	ArrayList<Integer> origCards = new ArrayList<Integer>();
		
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
	
	public OrderCardsView (Androminion top, String header, int[] cards) {
		super(top, header);
		this.top = top;
		
		this.cards = cards;
		for (int i=0; i<cards.length; i++)
			origCards.add(i);
		
		orig = new CardGroup(top, this, false);
    	origGV = GameTableViews.makeGV(top, orig, 1);
    	origCS = (GameTableViews.myCardSet(top, top.getString(R.string.cards), origGV, null));
    	
		ordered = new CardGroup(top, this, false);
    	orderedGV = GameTableViews.makeGV(top, ordered, 1);
    	orderedCS = (GameTableViews.myCardSet(top, top.getString(R.string.top_of_deck), orderedGV, null));
    	
        touch = new DragNDropListView (top);
        touch.setLayoutParams(new LinearLayout.LayoutParams(CardView.WIDTH, LinearLayout.LayoutParams.WRAP_CONTENT));
        touch.setDropListener(new DragNDropListView.DragListener() {
			@Override
			public void onDrag(int from, int to) {
				int c = origCards.get(from);
				origCards.remove(from);
				origCards.add(to, c);
				orderCardGroups();
				// TODO Auto-generated method stub
			}
		});
        touch.setAdapter(orig);
    	touchCS = (GameTableViews.myCardSet(top, top.getString(R.string.top_of_deck), touch, null));
		
    	select = new Button(top);
    	select.setText(R.string.accept);
        select.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { go(); }
        });

    	reset = new Button(top);
    	reset.setText(R.string.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { reset(); }
        });
        
        TextView inst = new TextView(top);
        inst.setText(R.string.order_cards_summary);
        
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.addView(select);
		ll.addView(touchCS);
		ll.addView(inst);
		// ll.addView(origCS);
		// ll.addView(orderedCS);
		// ll.addView(reset);
		ll.setPadding(0, 0, 0, 15);
		
    	orderCardGroups();
	}
	
	@Override
	public void onClick(View v) {
		int i = origGV.getPositionForView(v);
		if (i != GridView.INVALID_POSITION) {
			int c = origCards.get(i);
			origCards.remove(i);
			orderedCards.add(0, c);
		} else {
			i = orderedGV.getPositionForView(v);
			if (i != GridView.INVALID_POSITION) {
				int c = orderedCards.get(i);
				orderedCards.remove(i);
				origCards.add(0, c);
			}
		}
		
		orderCardGroups();
	}
	private void canSelect() {
		select.setClickable(true);
		select.setTextColor(Color.BLACK);
	}
	private void cannotSelect() {
		select.setClickable(false);
		select.setTextColor(Color.GRAY);
	}

	private void go() {
		((FrameLayout) this.getParent()).removeView(this);
//		top.handle(new Event(Event.EType.CARDORDER).setObject(orderedCards.toArray(new Integer[0])));
		int[] is = new int[origCards.size()];
		for (int i = 0; i < origCards.size(); i++)
			is[i] = origCards.get(i);
		
		top.handle(new Event(Event.EType.CARDORDER).setObject(new EventObject(is)));
	}
	private void reset() {
		origCards.clear();
		orderedCards.clear();
		for (int i=0; i<cards.length; i++)
			origCards.add(i);
		orderCardGroups();
	}

	@Override
	protected View makeContentView(Androminion top) {
		ll = new LinearLayout(top);
		hsv = new HorizontalScrollView(top) {
    		@Override 
    		public void onSizeChanged (int w, int h, int oldw, int oldh) {
    			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, Math.max(h, oldh));
    			setLayoutParams(p);
    		}
    	};
		hsv.addView(ll);

		return hsv;
	}
}