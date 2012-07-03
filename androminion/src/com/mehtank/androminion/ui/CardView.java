package com.mehtank.androminion.ui;

import java.io.File;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
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

public class CardView extends FrameLayout implements OnLongClickListener, CheckableEx {

	public static final int SHOWTOGGLE = 0;
	public static final int SHOWCOUNT = 1;
	public static final int SHOWCOIN = 2;

	public static final int WIDTH = 110;
	private TextView name;
	private View colorBox;
	private TextView cost, countLeft, embargos;
	private TextView checked;
	private View nomore;

	CardGroup parent;
	private CardState state;
	
	static public class CardState {
		public MyCard c;
		public boolean opened;
		public String indicator;
		public int order;
		
		public CardState(MyCard c) {
			this(c, false, "", -1);
		}
		
		public CardState(MyCard c, boolean opened, String indicator, int order) {
			this.c = c;
			this.opened = opened;
			this.indicator = indicator;
			this.order = order;
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

		LayoutInflater.from(context).inflate(R.layout.cardview, this, true);
		name = (TextView) findViewById(R.id.name);
		colorBox = findViewById(R.id.colorBox);
		cost = (TextView) findViewById(R.id.cost);
		countLeft = (TextView) findViewById(R.id.countLeft);
		embargos = (TextView) findViewById(R.id.embargos);
		checked = (TextView) findViewById(R.id.checked);
		nomore = findViewById(R.id.nomore);

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
		
		if(c.costPotion) {
			cost.setBackgroundResource(R.drawable.coinpotion);
		} else {
			cost.setBackgroundResource(R.drawable.coin);
		}

		if(c.isPrize) {
			cost.setVisibility(INVISIBLE);
		} else {
			cost.setVisibility(VISIBLE);
		}

		if (c.isBane) {
			setBackgroundResource(R.drawable.baneborder);
		} else {
			setBackgroundResource(R.drawable.roundborder);
		}

		name.setText(c.name, TextView.BufferType.SPANNABLE);
		if(cost != null) {
			setCost(GameTable.getCardCost(c));
		}

		int fgColor = Color.WHITE;
		int countColor = Color.WHITE;
		int bgColor = Color.BLACK;

		if (c.isReaction) {
			bgColor = Color.rgb(0x00, 0x70, 0xcc);
		    if (c.isVictory) {
	            fgColor = (Color.BLACK);
		        name.setBackgroundColor(Color.rgb(0x32, 0xcd, 0x32));
		    }
		    else if (c.isTreasure) {
				bgColor = Color.rgb(0xdb, 0xdb, 0x70);
	            fgColor = Color.WHITE;
				countColor = Color.BLACK;
                name.setBackgroundColor(Color.rgb(0x00, 0x70, 0xcc));
            }
		}
		else if (c.isDuration) {
			fgColor = (Color.BLACK);
			countColor = Color.BLACK;
			bgColor = (Color.rgb(0xff, 0x8c, 0x00));
		}
		else if (c.isAttack) {
			fgColor = (Color.rgb(0xff, 0x80, 0x60));
			countColor = (Color.rgb(0xff, 0x80, 0x60));
		} else if (c.isTreasure && c.isVictory) {
			bgColor = (Color.rgb(0xc0, 0xc0, 0xc0));
			fgColor = (Color.BLACK);
			countColor = Color.BLACK;
			name.setBackgroundColor(Color.rgb(0x32, 0xcd, 0x32));
		}
		else if (c.isAction && c.isVictory) {
			bgColor = (Color.BLACK);
			fgColor = (Color.BLACK);
			name.setBackgroundColor(Color.rgb(0x32, 0xcd, 0x32));
		}
		else if (c.isTreasure) {
			fgColor = (Color.BLACK);
			countColor = Color.BLACK;
			if (c.isPotion)
				bgColor = (Color.rgb(0x33, 0xcc, 0xff));
			else if (c.gold == 1)
				bgColor = (Color.rgb(0xcf, 0xb5, 0x3b));
			else if (c.gold == 2)
				bgColor = (Color.rgb(0xc0, 0xc0, 0xc0));
			else if (c.gold == 3)
				bgColor = (Color.YELLOW);
			else if (c.gold == 5)
				bgColor = (Color.WHITE);
			else
				bgColor = (Color.rgb(0xdb, 0xdb, 0x70));
		}
		else if (c.isCurse)
			bgColor = (Color.rgb(0x94, 0, 0xd3));
		else if (c.isVictory) {
			fgColor = (Color.BLACK);
			countColor = Color.BLACK;
			bgColor = (Color.rgb(0x32, 0xcd, 0x32));
		}

		name.setTextColor(fgColor);
		countLeft.setTextColor(countColor);
		if (bgColor != 0)
			colorBox.setBackgroundColor(bgColor);
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

	public void setSize(int s) {
		countLeft.setText(" " + s + " ");
		if (s == 0)
			nomore.setVisibility(VISIBLE);
		else
			nomore.setVisibility(INVISIBLE);
	}

	public void setEmbargos(int s) {
		embargos.setText(" " + s + " ");
		if (s != 0)
			embargos.setVisibility(VISIBLE);
	}

	public void setCost(int newCost) {
		cost.setText(" " + newCost + " ");
	}

	public void swapNum(int mode) {
		if (mode == SHOWCOIN) {
			// cost.setVisibility(VISIBLE);
			countLeft.setVisibility(INVISIBLE);
			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER);
			name.setLayoutParams(p);
			return;
		} else if (mode == SHOWCOUNT) {
			// cost.setVisibility(INVISIBLE);
			countLeft.setVisibility(VISIBLE);
			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT,
					Gravity.TOP + Gravity.CENTER_HORIZONTAL);
			name.setLayoutParams(p);
			return;
		} else {
			if (countLeft.getVisibility() == View.VISIBLE) {
				swapNum(SHOWCOIN);
			} else {
				swapNum(SHOWCOUNT);
			}
		}
	}
	
	public void setState(CardState s) {
		state = s;
		setCard(s.c);
		setChecked(s.opened, s.order, s.indicator);
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
			if(PreferenceManager.getDefaultSharedPreferences(view.getContext()).getBoolean("showenglishnames", false)) {
				title += " (" + cardView.getCard().originalName + ")";
			}
		new AlertDialog.Builder(view.getContext())
			.setTitle(title)
			.setView(v)
			.setPositiveButton(android.R.string.ok, null)
			.show();

		return true;
	}
}
