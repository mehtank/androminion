package com.mehtank.androminion.ui;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
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

	private String viewstyle;
	private boolean autodownload;
	private Context top;

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
		this.top = context;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		viewstyle = prefs.getString("viewstyle", context.getString(R.string.pref_viewstyle_default));
		autodownload = prefs.getBoolean("autodownload", false);

		if (viewstyle.equals("viewstyle-classic"))
			LayoutInflater.from(context).inflate(R.layout.view_card_classic, this, true);
		else if (viewstyle.equals("viewstyle-descriptive"))
			LayoutInflater.from(context).inflate(R.layout.view_card_descriptive, this, true);
		else
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
		if (viewstyle.equals("viewstyle-simple")) {
			if (c.isBane) {
				setBackgroundResource(R.drawable.thinbaneborder);
			} else {
				setBackgroundResource(R.drawable.thinborder);
			}
		} else {
			if (c.isBane) {
				setBackgroundResource(R.drawable.baneborder);
		} 
		else if (c.isShelter)
		{
			setBackgroundResource(R.drawable.shelterborder);
		}
		else {
				setBackgroundResource(R.drawable.cardborder);
			}
		}

		name.setText(c.name, TextView.BufferType.SPANNABLE);
		if(cost != null) {
			setCost(GameTable.getCardCost(c), c.isOverpay);
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
			cardDesc.setTextColor(countColor);
			if (c.pile == MyCard.MONEYPILE || c.pile == MyCard.VPPILE) {
				ViewGroup.LayoutParams params = cardDesc.getLayoutParams();
				int pixels = (int) (0.5f + 20 * getContext().getResources().getDisplayMetrics().density);
				params.height = pixels;
				cardDesc.setLayoutParams(params);
			}
		}
	}
	
	//TODO: Use this to update the VirtualKnights pile
	/*public void updateCardStyle(MyCard c) {
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
	}*/

	private static int getStyleForCard(MyCard c) {
		if (c.isReaction && c.isVictory) {
			return R.style.CardView_Reaction_Victory;
		} else if (c.isReaction && c.isTreasure) {
			return R.style.CardView_Treasure_Reaction;
		} else if (c.isReaction) {
			return R.style.CardView_Reaction;
		} else if (c.isDuration) {
			return R.style.CardView_Duration;
		} else if (c.isRuins) {
			return R.style.CardView_Ruins;
		} else if (c.isVictory && c.isAttack) { 
			return R.style.CardView_Attack_Victory;
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
		if (s == 0)
			shade(true);
		else
			shade(false);
	}

	public void shade(boolean on) {
		float alpha = (on ? 0.3f : 1.0f);
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

	public void setCost(int newCost, boolean overpay) {
		cost.setText(" " + newCost + (overpay ? "+" : "") + " ");
	}

	public void setState(CardState s) {
		state = s;
		setCard(s.c);
		setChecked(s.opened, s.order, s.indicator);
		setOnTable(s.onTable);
		if (s.shade)
			shade(true);
		else
			shade(false);
	}

	void setOnTable(boolean onTable) {
		countLeft.setVisibility(onTable ? VISIBLE : GONE);
		if (cardDesc != null)
			cardDesc.setVisibility((onTable && "viewstyle-descriptive".equals(viewstyle)) ? VISIBLE : GONE);
		if (onTable && "viewstyle-classic".equals(viewstyle)) {
			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.TOP + Gravity.CENTER_HORIZONTAL);
			name.setLayoutParams(p);
		} else if ("viewstyle-classic".equals(viewstyle)) {
			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT,
					Gravity.CENTER);
			name.setLayoutParams(p);
		}
	}

	public CardState getState() {
		return state;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onLongClick(View view) {
		CardView cardView = (CardView) view;
		if(cardView.getCard() == null) {
			return false;
		}

		HapticFeedback.vibrate(getContext(),AlertType.LONGCLICK);

		String str = cardView.getCard().originalSafeName;
		str = str.toLowerCase(Locale.US);
		StringTokenizer st = new StringTokenizer(str," ",false);
		String filename = "";
		while (st.hasMoreElements()) filename += st.nextElement();

		str = cardView.getCard().originalExpansion;
		str = str.toLowerCase(Locale.US);
		st = new StringTokenizer(str," ",false);
		String exp = "";
		while (st.hasMoreElements()) exp += st.nextElement();
		
		View v;

		String subdir = "/images/full/";
		str = GameActivity.BASEDIR + subdir + filename + ".jpg";
		File f = new File(str);
		
		if (!f.exists() && autodownload) {
			if (isDownloadManagerAvailable(top)) {
				new File(GameActivity.BASEDIR + subdir).mkdirs();
				String imgurl = "http://dominion.diehrstraits.com/scans/" + exp + "/" + filename + ".jpg";
	
				// from: http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
				DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imgurl));
				request.setTitle(top.getString(R.string.img_download_title, cardView.getCard().name));
				request.setDescription(top.getString(R.string.img_download_desc, imgurl));
				// in order for this if to run, you must use the android 3.2 to compile your app
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				    request.allowScanningByMediaScanner();
				    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				}
				request.setDestinationInExternalPublicDir(GameActivity.BASEDIRFROMEXT + subdir, filename + ".jpg");
	
				// get download service and enqueue file
				DownloadManager manager = (DownloadManager) top.getSystemService(Context.DOWNLOAD_SERVICE);
				manager.enqueue(request);
			}
		}

		if (f.exists()) {
			Uri u = Uri.parse(str);
			ImageView im = new ImageView(view.getContext());
            im.setImageURI(u);
            im.setScaleType(ImageView.ScaleType.FIT_CENTER);
            v = im;
		} else {
			TextView textView = new TextView(view.getContext());
			textView.setPadding(15, 0, 15, 5);
			String text = GetCardTypeString(cardView.getCard());
			
			if(cardView.getCard().expansion != null && cardView.getCard().expansion.length() != 0) 
			{
                text += " (" + cardView.getCard().expansion + ")";
            }
			
			text += "\n";
			
			text += cardView.getCard().desc;
			textView.setText( text );
			v = textView;
		}

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
	
	public String GetCardTypeString(MyCard c)
    {
	    String cardType = "";
	    Context context = getContext();
        
        if (c.isAction)
        {
            cardType += context.getString(R.string.type_action);
            
            if (c.isAttack)
            {
                cardType += " - " + context.getString(R.string.type_attack);
            }
            
            if (c.isLooter)
            {
                cardType += " - " + context.getString(R.string.type_looter);
            }
            
            if (c.isRuins)
            {
                cardType += " - " + context.getString(R.string.type_ruins);
            }
            
            if (c.isPrize)
            {
                cardType += " - " + context.getString(R.string.type_prize);
            }
            
            if (c.isReaction)
            {
                cardType += " - " + context.getString(R.string.type_reaction);
            }
            
            if (c.isDuration)
            {
                cardType += " - " + context.getString(R.string.type_duration);
            }
            
            if (c.isVictory)
            {
                cardType += " - " + context.getString(R.string.type_victory);
            }
            
            if (c.isKnight)
            {
                cardType += " - " + context.getString(R.string.type_knight);
            }
            
            if (c.isShelter)
            {
                cardType += " - " + context.getString(R.string.type_shelter);
            }
        }
        else if (c.isTreasure)
        {
            cardType += context.getString(R.string.type_treasure);
            
            if (c.isVictory)
            {
                cardType += " - " + context.getString(R.string.type_victory);
            }
            
            if (c.isReaction)
            {
                cardType += " - " + context.getString(R.string.type_reaction);
            }
            
            if (c.isPrize)
            {
                cardType += " - " + context.getString(R.string.type_prize);
            }
        }
        else if (c.isVictory)
        {
            cardType += context.getString(R.string.type_victory);
            
            if (c.isShelter)
            {
                cardType += " - " + context.getString(R.string.type_shelter);
            }
            
            if (c.isReaction)
            {
                cardType += " - " + context.getString(R.string.type_reaction);
            }
        }
        else if (c.name.equalsIgnoreCase("hovel"))
        {
            cardType += context.getString(R.string.type_reaction) + " - " + context.getString(R.string.type_shelter);
        }
        
        return cardType;
    }
		
	/**
	 * @param context used to check the device version and DownloadManager information
	 * @return true if the download manager is available
	 * 
	 * from: http://stackoverflow.com/questions/3028306/download-a-file-with-android-and-showing-the-progress-in-a-progressdialog
	 */
	public static boolean isDownloadManagerAvailable(Context context) {
	    try {
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
	            return false;
	        }
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
	                PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}

}
