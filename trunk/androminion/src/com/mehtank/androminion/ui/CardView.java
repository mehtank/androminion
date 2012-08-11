package com.mehtank.androminion.ui;

import java.io.File;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;
import com.mehtank.androminion.util.CardGroup;
import com.mehtank.androminion.util.CheckableEx;
import com.mehtank.androminion.util.HapticFeedback;
import com.mehtank.androminion.util.HapticFeedback.AlertType;
import com.vdom.comms.MyCard;


/**
 * Corresponds to a single card that is visible on the 'table'
 *
 */
public class CardView extends FrameLayout implements OnLongClickListener, CheckableEx {
	private static final String TAG = "CardView";
	
	private TextView name;
	private View cardBox;
	private TextView cost, countLeft, embargos;
	private TextView checked;
	private TextView cardDesc;

	CardGroup parent;
	private CardState state;
/**
 * Information about a card type opened, onTable, indicator, order
 *
 */
	static public class CardState {
		public MyCard c; // card type
		public boolean opened; // was selected
		public boolean onTable;
		public String indicator;
		public int order;
		public boolean shade;

		public CardState(MyCard c) {
			this(c, false, "", -1, false);
		}

		public CardState(MyCard c, boolean opened, String indicator, int order) {
		    this(c, opened, indicator, order, false);
		}
		
		public CardState(MyCard c, boolean opened, String indicator, int order, boolean shade) {
			this.c = c;
			this.opened = opened;
			this.indicator = indicator;
			this.order = order;
			this.shade = shade;
		}
	}

	public CardView(Context context) {
		this(context, null);
	}

	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, null, null);
	}

	public CardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, null, null);
	}

	public CardView(Context context, CardGroup parent, MyCard c) {
		super(context);
		init(context, parent, c);
	}

	private void init(Context context, CardGroup parent, MyCard c) {
		this.parent = parent;

		LayoutInflater.from(context).inflate(R.layout.view_card, this, true);
		name = (TextView) findViewById(R.id.name);
		cardBox = findViewById(R.id.cardBox);
		cost = (TextView) findViewById(R.id.cost);
		countLeft = (TextView) findViewById(R.id.countLeft);
		embargos = (TextView) findViewById(R.id.embargos);
		checked = (TextView) findViewById(R.id.checked);
		cardDesc = (TextView) findViewById(R.id.cardDesc);

		state = new CardState(null);

		if (c != null) {
			setCard(c);
		}
	}

	public MyCard getCard() {
		return state.c;
	}

	public void setCard(MyCard c) {
		this.state.c = c;

		if (cardDesc != null) {
			cardDesc.setText(c.desc);
		}

		if (c.costPotion) {
			cost.setBackgroundResource(R.drawable.coinpotion);
		} else {
			cost.setBackgroundResource(R.drawable.coin);
		}

		if (c.isPrize) {
			cost.setVisibility(INVISIBLE);
		} else {
			cost.setVisibility(VISIBLE);
		}

		// TODO: Merge this border with the color setting below, then get rid of cardBox.
		if (c.isBane) {
			setBackgroundResource(R.drawable.baneborder);
		} else {
			setBackgroundResource(R.drawable.sharpborder);
		}

		name.setText(c.name, TextView.BufferType.SPANNABLE);
		if(cost != null) {
			setCost(GameTable.getCardCost(c));
		}

		int cardStyleId = getStyleForCard(c);
		TypedArray cardStyle = getContext().obtainStyledAttributes(cardStyleId,
				new int[] {
					R.attr.cardBackgroundColor,
					R.attr.cardNameBackgroundColor,
					R.attr.cardTextColor,
					R.attr.cardCountColor });
		int bgColor = cardStyle.getColor(0, R.color.cardDefaultBackgroundColor);
		int textColor = cardStyle.getColor(2, R.color.cardDefaultTextColor);
        int nameBgColor = cardStyle.getColor(1, R.color.cardDefaultTextBackgroundColor);
		int countColor = cardStyle.getColor(3, R.color.cardDefaultTextColor);

		cardBox.setBackgroundColor(bgColor);
		name.setTextColor(textColor);
        name.setBackgroundColor(nameBgColor);
		countLeft.setTextColor(countColor);
		if (cardDesc != null) {
			// TODO: Check if using the same color here looks good in all cases.
			cardDesc.setTextColor(textColor);
		}
	}

	private static int getStyleForCard(MyCard c) {
		if (c.isReaction && c.isVictory) {
			return R.style.CardView_Reaction_Victory;
		} else if (c.isReaction && c.isTreasure) {
			return R.style.CardView_Treasure_Reaction;
		} else if (c.isReaction) {
			return R.style.CardView_Reaction;
		} else if (c.isDuration) {
			return R.style.CardView_Duration;
		} else if (c.isAttack) {
			return R.style.CardView_Attack;
		} else if (c.isTreasure && c.isVictory) {
			return R.style.CardView_Treasure_Victory;
		} else if (c.isAction && c.isVictory) {
			return R.style.CardView_Victory_Action;
		} else if (c.isTreasure && c.isPotion) {
			return R.style.CardView_Treasure_Potion;
		} else if (c.isTreasure) {
			switch (c.gold) {
			case 1:
				return R.style.CardView_Treasure_Copper;
			case 2:
				return R.style.CardView_Treasure_Silver;
			case 3:
				return R.style.CardView_Treasure_Gold;
			case 5:
				return R.style.CardView_Treasure_Platinum;
			default:
				return R.style.CardView_Treasure;
			}
		} else if (c.isCurse) {
			return R.style.CardView_Curse;
		} else if (c.isVictory) {
			return R.style.CardView_Victory;
		} else {
			return R.style.CardView;
		}
	}

	@Override
	public boolean isChecked() {
		return state.opened;
	}

	@Override
	public void toggle() {
		setChecked(!state.opened);
	}

	@Override
	public void setChecked(boolean arg0) {
		setChecked(arg0,-1, "");
	}

	@Override
	public void setChecked(boolean arg0, String indicator) {
		setChecked(arg0, -1, indicator);
	}

	@Override
	public void setChecked(boolean arg0, int order, String indicator) {
		state.opened = arg0;
		state.indicator = indicator;
		state.order = order;
		if (order > 0) {
			checked.setText(" " + (order+1));
		} else {
            checked.setText(indicator);
		}

        if (state.opened)
            checked.setVisibility(VISIBLE);
        else
            checked.setVisibility(INVISIBLE);
	}

	public void setCountLeft(int s) {
		countLeft.setText(" " + s + " ");
		countLeft.setVisibility(VISIBLE);
		if (s == 0) shade();
	}

	public void shade() {	
		float alpha = 0.3f;

		// setAlpha() is API level 11+ only, so we use an instant animation instead.
		AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
		alphaAnimation.setDuration(0L);
		alphaAnimation.setFillAfter(true);
		cardBox.startAnimation(alphaAnimation);
	}

	public void setEmbargos(int s) {
		if (s != 0) {
			embargos.setText(" " + s + " ");
			embargos.setVisibility(VISIBLE);
		} else {
			embargos.setVisibility(GONE);
		}
	}

	public void setCost(int newCost) {
		cost.setText(" " + newCost + " ");
	}

	public void setState(CardState s) {
		state = s;
		setCard(s.c);
		setChecked(s.opened, s.order, s.indicator);
		setOnTable(s.onTable);
		if (s.shade)
			shade();
	}

	private void setOnTable(boolean onTable) {
		countLeft.setVisibility(onTable ? VISIBLE : GONE);
	}

	public CardState getState() {
		return state;
	}

	@Override
	public boolean onLongClick(View view) {
		CardView cardView = (CardView) view;
		if(cardView.getCard() == null) {
			return false;
		}

		HapticFeedback.vibrate(getContext(),AlertType.LONGCLICK);
		String str = cardView.getCard().name;
		str = str.toLowerCase();

		StringTokenizer st = new StringTokenizer(str," ",false);
		String filename = "";
		while (st.hasMoreElements()) filename += st.nextElement();

		View v;

        // int resID =
        // getResources().getIdentifier("com.mehtank.androminion:drawable/" +
        // filename, null, null);
        // if (resID != 0) {
        // ImageView im = new ImageView(top);
        // im.setBackgroundResource(resID);
        // im.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // v = im;
        // } else {
			str = GameActivity.BASEDIR + "/images/full/" + filename + ".jpg";
			File f = new File(str);
			if (f.exists()) {
				Uri u = Uri.parse(str);
				ImageView im = new ImageView(view.getContext());
	            im.setImageURI(u);
	            im.setScaleType(ImageView.ScaleType.FIT_CENTER);
	            v = im;
			} else {
				TextView textView = new TextView(view.getContext());
				textView.setPadding(15, 0, 15, 5);
				String text = ""; //cardView.c.name;
				if(cardView.getCard().expansion != null && cardView.getCard().expansion.length() != 0) {
				    text += "(" + cardView.getCard().expansion + ")\n";
				}
				text += cardView.getCard().desc;
				textView.setText( text );
				v = textView;
			}
        // }
			String title = cardView.getCard().name;
			Log.d(TAG, "card title = " + title);
			if(PreferenceManager.getDefaultSharedPreferences(view.getContext()).getBoolean("showenglishnames", false)) {
				title += " (" + cardView.getCard().originalName + ")";
				Log.d(TAG, "card title now: " + title);
			}
		new AlertDialog.Builder(view.getContext())
			.setTitle(title)
			.setView(v)
			.setPositiveButton(android.R.string.ok, null)
			.show();

		return true;
	}
}
