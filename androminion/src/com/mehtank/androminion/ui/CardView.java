package com.mehtank.androminion.ui;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringTokenizer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.method.LinkMovementMethod;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;
import com.mehtank.androminion.util.CardGroup;
import com.mehtank.androminion.util.CheckableEx;
import com.mehtank.androminion.util.HapticFeedback;
import com.mehtank.androminion.util.HapticFeedback.AlertType;
import com.mehtank.androminion.util.PlayerAdapter;
import com.vdom.comms.MyCard;
import com.vdom.core.PlayerSupplyToken;

/**
 * Corresponds to a single card that is visible on the 'table'
 *
 */
public class CardView extends FrameLayout implements OnLongClickListener, CheckableEx {
	private static final String TAG = "CardView";

	private TextView name;
	private View cardBox;
	private TextView cost, debtCost, countLeft, embargos, pileVpTokens, pileDebtTokens, pileTradeRouteTokens;
	private int numEmbargos;
	private int numPileVpTokens;
	private int numPileDebtTokens;
	private int numPileTradeRouteTokens;
	private LinearLayout tokens;
	private TextView checked;
	private TextView cardDesc;
	private PlayerAdapter players;

	private String viewstyle;
	private boolean autodownload;
	private Context top;
	private boolean hideCountLeft;
	private String showimages;
	private boolean wikilink;
	
	private int[][] currentTokens;
	private static final int MAX_TOKENS_ON_CARD = 4;

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
		showimages = prefs.getString("showimages", context.getString(R.string.showimages_pref_default));
		wikilink = prefs.getBoolean("wikilink", false);

		if (viewstyle.equals("viewstyle-classic"))
			LayoutInflater.from(context).inflate(R.layout.view_card_classic, this, true);
		else if (viewstyle.equals("viewstyle-descriptive"))
			LayoutInflater.from(context).inflate(R.layout.view_card_descriptive, this, true);
		else if (viewstyle.equals("viewstyle-condensed"))
			LayoutInflater.from(context).inflate(R.layout.view_card_condensed, this, true);
		else /*if (viewstyle.equals("viewstyle-simple"))*/
			LayoutInflater.from(context).inflate(R.layout.view_card, this, true);

		name = (TextView) findViewById(R.id.name);
		cardBox = findViewById(R.id.cardBox);
		cost = (TextView) findViewById(R.id.cost);
		debtCost = (TextView) findViewById(R.id.debtCost);
		countLeft = (TextView) findViewById(R.id.countLeft);
		embargos = (TextView) findViewById(R.id.embargos);
		pileVpTokens = (TextView) findViewById(R.id.pileVpTokens);
		pileDebtTokens = (TextView) findViewById(R.id.pileDebtTokens);
		pileTradeRouteTokens = (TextView) findViewById(R.id.pileTradeRouteTokens);
		tokens = (LinearLayout) findViewById(R.id.tokens);
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
		
		if (c.debtCost > 0) {
			debtCost.setVisibility(VISIBLE);			
		} else {
			debtCost.setVisibility(GONE);
		}

		if (c.isPrize || c.isLandmark || (c.debtCost > 0 && !c.costPotion && c.cost == 0)) {
			cost.setVisibility(GONE);
		} else {
			cost.setVisibility(VISIBLE);
		}
		
		if (c.isEvent || c.isLandmark){
			hideCountLeft = true;
			countLeft.setVisibility(GONE);
		}

		// TODO: Merge this border with the color setting below, then get rid of cardBox.
		if (viewstyle.equals("viewstyle-simple")) {
			if (c.isBane) {
				setBackgroundResource(R.drawable.thinbaneborder);
			} else if (c.isObeliskCard) {
				setBackgroundResource(R.drawable.thinobeliskborder);
			} else if (c.isStash) {
				setBackgroundResource(R.drawable.thinstashborder);
			} else {
				setBackgroundResource(R.drawable.thinborder);
			}
		} else {
			if (c.isBane) {
				setBackgroundResource(R.drawable.baneborder);
			} else if (c.isObeliskCard) {
				setBackgroundResource(R.drawable.obeliskborder);
			} else if (c.isStash) {
				setBackgroundResource(R.drawable.stashborder);
			} else if (c.isShelter) {
				setBackgroundResource(R.drawable.shelterborder);
			} else {
				setBackgroundResource(R.drawable.cardborder);
			}
		}

		name.setText(c.name, TextView.BufferType.SPANNABLE);
		if (cost != null) {
			setCost(GameTable.getCardCost(c), c.isOverpay, c.debtCost);
			int costTextColor = (c.debtCost > 0) ? R.color.cardDebtCostTextColor : R.color.cardCostTextColor;
			cost.setTextColor(costTextColor);
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
		cardStyle.recycle();
		
		cardBox.setBackgroundColor(bgColor);
		name.setTextColor(textColor);
        name.setBackgroundColor(nameBgColor);
		countLeft.setTextColor(countColor);
		
		if (cardDesc != null) {
			cardDesc.setTextColor(countColor);
			if (c.pile == MyCard.MONEYPILE || c.pile == MyCard.VPPILE) {
				ViewGroup.LayoutParams params = cardDesc.getLayoutParams();
				int pixels = (int) (0.5f + (viewstyle.equals("viewstyle-condensed") ? 15 : 20) * getContext().getResources().getDisplayMetrics().density);
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
		} else if (c.isDuration && c.isReaction) {
			return R.style.CardView_Duration_Reaction;
		} else if (c.isShelter && c.isVictory) {
			return R.style.CardView_Shelter_Victory;
		} else if (c.isShelter && c.isReaction) {
			return R.style.CardView_Shelter_Reaction;
		} else if (c.isReaction) {
			return R.style.CardView_Reaction;
		} else if (c.isDuration && c.isAttack) {
			return R.style.CardView_Duration_Attack;
		} else if (c.isDuration) {
			return R.style.CardView_Duration;
		} else if (c.isReserve && c.isVictory) {
			return R.style.CardView_Reserve_Victory;
		} else if (c.isReserve && c.isTreasure) {
			return R.style.CardView_Treasure_Reserve;
		} else if (c.isReserve) {
			return R.style.CardView_Reserve;
		} else if (c.isRuins) {
			return R.style.CardView_Ruins;
		} else if (c.isVictory && c.isAttack) { 
			return R.style.CardView_Attack_Victory;
		} else if (c.isTreasure && c.isAttack) {
			return R.style.CardView_Treasure_Attack;
		} else if (c.isAttack) {
			return R.style.CardView_Attack;
		} else if (c.isTreasure && c.isVictory && c.isCastle) {
			return R.style.CardView_Treasure_Victory_Castle;
		} else if (c.isTreasure && c.isVictory) {
			return R.style.CardView_Treasure_Victory;
		} else if (c.isAction && c.isVictory) {
			return R.style.CardView_Victory_Action;
		} else if (c.isTreasure && c.isPotion) {
			return R.style.CardView_Treasure_Potion;
		} else if (c.isTreasure && c.isAction) {
			return R.style.CardView_Treasure_Action;
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
		} else if (c.isShelter) {
			return R.style.CardView_Shelter;
		} else if (c.isEvent) {
			return R.style.CardView_Event;
		} else if (c.isLandmark) {
			return R.style.CardView_Landmark;
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
		countLeft.setVisibility(hideCountLeft ? GONE : VISIBLE);
		
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
		numEmbargos = s;
		if (s != 0) {
			embargos.setText(" " + s + " ");
			embargos.setVisibility(VISIBLE);
		} else {
			embargos.setVisibility(GONE);
		}
	}
	
	public void setPileVpTokens(int val) {
		numPileVpTokens = val;
		if (val != 0) {
			pileVpTokens.setText(" " + val + " ");
			pileVpTokens.setVisibility(VISIBLE);
		} else {
			pileVpTokens.setVisibility(GONE);
		}
	}
	
	public void setPileDebtTokens(int val) {
		numPileDebtTokens = val;
		if (val != 0) {
			pileDebtTokens.setText(" " + val + " ");
			pileDebtTokens.setVisibility(VISIBLE);
		} else {
			pileDebtTokens.setVisibility(GONE);
		}
	}
	
	public void setPileTradeRouteTokens(int val) {
		numPileTradeRouteTokens = val;
		if (val != 0) {
			pileTradeRouteTokens.setText("     ");
			pileTradeRouteTokens.setVisibility(VISIBLE);
		} else {
			pileTradeRouteTokens.setVisibility(GONE);
		}
	}
	
	public void setTokens(int[][] newTokens, PlayerAdapter players) {
		this.players = players;
		if (Arrays.deepEquals(newTokens, currentTokens)) {
			return;
		}
		currentTokens = newTokens;
		tokens.removeAllViews();
		int numTokens = countTokens(newTokens);
		if (numTokens > MAX_TOKENS_ON_CARD) {
			if (countPlayersWithTokens(newTokens) > MAX_TOKENS_ON_CARD) {
				tokens.addView(getViewForToken(-1, -1, numTokens));
				return;
			}
			for (int i = 0; i < newTokens.length; ++i) {
				int numPlayerTokens = newTokens[i].length;
				if (numPlayerTokens == 0)
					continue;
				tokens.addView(getViewForToken(i, -1, numPlayerTokens));
			}
			return;
		}
		
		for (int i = 0; i < newTokens.length; ++i) {
			int[] playerTokens = newTokens[i];
			for (int tokenId : playerTokens) {
				View tokenView = getViewForToken(i, tokenId, -1);
				if (tokenView != null)
					tokens.addView(tokenView);
			}
		}
	}
	
	private int countPlayersWithTokens(int[][] tokensPerPlayer) {
		int result = 0;
		for (int[] tokens : tokensPerPlayer) {
			if (tokens.length > 0)
				result++;
		}
		return result;
	}

	private int countTokens(int[][] tokensPerPlayer) {
		int result = 0;
		for (int[] tokens : tokensPerPlayer) {
			result += tokens.length;
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	private View getViewForToken(int player, int tokenId, int multiplier) {
		int backgroundId = R.drawable.circulartoken;
		String text;
		PlayerSupplyToken tokenType = PlayerSupplyToken.getById(tokenId);
		if (tokenType != null) {
			switch(tokenType) {
				case PlusOneCard:
					backgroundId = R.drawable.rectangulartoken;
					text = "+1";
					break;
				case PlusOneAction:
					text = "+" + getContext().getString(R.string.token_plus_one_action_initial); 
					break;
				case PlusOneBuy:
					text = "+" + getContext().getString(R.string.token_plus_one_buy_initial);
					break;
				case PlusOneCoin:
					text = "+1";
					break;
				case MinusTwoCost:
					text = "-2";
					break;
				case Trashing:
					text = "X";
					break;
				default:
					return null;
			}
		} else {
			text = "x" + multiplier;
		}

		float dp = getResources().getDisplayMetrics().density;
		TextView token = new TextView(getContext());
		if (player >= 0) {
			GradientDrawable background = (GradientDrawable)getResources().getDrawable(backgroundId).mutate();
			background.setColor(getPlayerColor(player));
			background.setStroke((int)Math.ceil(2 * dp), getPlayerStrokeColor(player));
			token.setBackgroundDrawable(background);
		}
		int pad = (int) Math.ceil(dp);
		int padSides = (int) Math.ceil(dp * 2);
		token.setTextAppearance(getContext(), R.style.style_cardview_count);
		token.setTextColor(Color.BLACK);
		token.setText(text);
		token.setPadding(padSides, pad, padSides, pad);
		return token;
	}
	
	private int getPlayerColor(int playerIndex) {
		return GameTable.getPlayerColor(getResources(), playerIndex);
	}
	
	private int getPlayerStrokeColor(int playerIndex) {
		return GameTable.getPlayerStrokeColor(getResources(), playerIndex);
	}
	
	public void setCost(int newCost, boolean overpay, int newDebtCost) {
		debtCost.setText(" " + newDebtCost + " ");
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
		countLeft.setVisibility(onTable && !hideCountLeft ? VISIBLE : GONE);
		if (cardDesc != null)
			cardDesc.setVisibility((onTable && ("viewstyle-descriptive".equals(viewstyle) || "viewstyle-condensed".equals(viewstyle))) ? VISIBLE : GONE);
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
		if (cardView.getCard() == null) {
			return false;
		}

		HapticFeedback.vibrate(getContext(), AlertType.LONGCLICK);

		String str = cardView.getCard().originalSafeName;
		str = str.toLowerCase(Locale.US);
		StringTokenizer st = new StringTokenizer(str, " ", false);
		String filename = "";
		String titlename = cardView.getCard().name.replace(" ", "_").replace("'", "");
		while (st.hasMoreElements())
			filename += st.nextElement();

		str = cardView.getCard().originalExpansion;
		str = str.toLowerCase(Locale.US);
		st = new StringTokenizer(str, " ", false);
		String exp = "";
		while (st.hasMoreElements())
			exp += st.nextElement();
		/*if (exp.length() == 0)
			exp = "common";
		if (filename.equals("potion"))
			exp = "alchemy";
		else if (filename.equals("colony"))
			exp = "prosperity";
		else if (filename.equals("platinum"))
			exp = "prosperity";*/
		if (exp.length() == 0)
			exp = "basecards";
		
		View v;

		String subdir = "/images/full/";
		str = GameActivity.BASEDIR + subdir + titlename + ".jpg";
		File f = new File(str);
		if (!f.exists()) {
			str = GameActivity.BASEDIR + subdir + filename + ".jpg";
			f = new File(str);
		}
		
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

		//float dp = getResources().getDisplayMetrics().density;
		float wp = getResources().getDisplayMetrics().widthPixels;
		float hp = getResources().getDisplayMetrics().heightPixels;

		LinearLayout ll = new LinearLayout(view.getContext());
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);

		TextView textView = new TextView(view.getContext());
		//textView.setPadding(15, 0, 15, 5);
		textView.setPadding(30, 10, 30, 5);
		textView.setGravity(Gravity.CENTER);
		String text = GetCardTypeString(cardView.getCard());

		if (cardView.getCard().expansion != null && cardView.getCard().expansion.length() != 0) {
			text += " (" + cardView.getCard().expansion + ")";
		}

		text += "\n";
		text += cardView.getCard().desc;
		String extraDescription = getExtraDescription();
		if (!extraDescription.isEmpty()) {
			text += "\n\n" + extraDescription;
		}
		textView.setText(text);


		if (f.exists() && (showimages.equals("Image") || showimages.equals("Both"))) {
			Uri u = Uri.parse(str);
			ImageView im = new ImageView(view.getContext());
			im.setImageURI(u);

			// Calculate how to best fit the card graphics
			float zoom = wp / im.getDrawable().getIntrinsicWidth() / 1.3f;
			if (im.getDrawable().getIntrinsicHeight() * zoom > hp / 2)
				zoom = hp / im.getDrawable().getIntrinsicHeight() / 2;

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					(int)(im.getDrawable().getIntrinsicWidth() * zoom), (int)(im.getDrawable().getIntrinsicHeight() * zoom));
			//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int)(296 * 3.0), (int)(473 * 3.0));
			im.setLayoutParams(params);
			im.setScaleType(ImageView.ScaleType.FIT_CENTER);
			ll.addView(im);
		}
		if (!f.exists() || !showimages.equals("Image"))
			ll.addView(textView);
			
		Configuration configuration = new Configuration(getContext().getResources().getConfiguration());
		configuration.setLocale(new Locale("en"));
		String englishName = this.getContext().createConfigurationContext(configuration).getResources().getString(this.getContext().createConfigurationContext(configuration).getResources().getIdentifier(cardView.getCard().originalSafeName + "_name", "string", this.getContext().getPackageName()));
			
		if (wikilink) {
			TextView linkView = new TextView(view.getContext());
			String str2 = englishName.replace(" ", "_");
			text = "<a href=\"http://wiki.dominionstrategy.com/index.php/" + str2 + "\">" + view.getContext().getString(R.string.card_more_info) + "</a>";
			linkView.setClickable(true);
			linkView.setMovementMethod(LinkMovementMethod.getInstance());
			linkView.setText(android.text.Html.fromHtml(text));
			linkView.setGravity(Gravity.CENTER);
			ll.addView(linkView);
		}
		v = ll;

		String title = cardView.getCard().name;
		Log.d(TAG, "card title = " + title);
		boolean isEnglish = "en".equals(getResources().getConfiguration().locale.getLanguage());
		if (!isEnglish && PreferenceManager.getDefaultSharedPreferences(view.getContext()).getBoolean("showenglishnames", false)) {
			title += " (" + englishName + ")";
			Log.d(TAG, "card title now: " + title);
		}

		TextView titlev = new TextView(view.getContext());
		titlev.setText(title);
		titlev.setPadding(10, 10, 10, 10);
		titlev.setTextSize(20);
		titlev.setGravity(Gravity.CENTER);

		AlertDialog ad = new AlertDialog.Builder(view.getContext())
			//.setTitle(title)
			.setCustomTitle(titlev)
			.setView(v)
			.setPositiveButton(android.R.string.ok, null)
			.show();
		ad.getButton(AlertDialog.BUTTON_POSITIVE).setGravity(Gravity.CENTER);

		return true;
	}
	
	private String getExtraDescription() {
		String text = "";
		if (state.c.isBane) {
			text += getContext().getString(R.string.bane_card);
		}
		if (state.c.isObeliskCard) {
			if (text.length() > 0)
				text += "\n";
			text += getContext().getString(R.string.obelisk_card);
		}
		boolean hasPlayerTokens = players != null && currentTokens != null && countTokens(currentTokens) > 0;
		if (hasPlayerTokens || numEmbargos > 0 || numPileVpTokens > 0 || numPileDebtTokens > 0 || numPileTradeRouteTokens > 0) {
			if (text.length() > 0)
				text += "\n\n";
			text += getContext().getString(R.string.token_header);
			text += "\n";
			if (numEmbargos > 0) {
				text += getContext().getString(R.string.token_embargo) + getContext().getString(R.string.token_colon) + numEmbargos;
				text += "\n";
			}
			if (numPileVpTokens > 0) {
				text += getContext().getString(R.string.token_victory) + getContext().getString(R.string.token_colon) + numPileVpTokens;
				text += "\n";
			}
			if (numPileDebtTokens > 0) {
				text += getContext().getString(R.string.token_debt) + getContext().getString(R.string.token_colon) + numPileDebtTokens;
				text += "\n";
			}
			if (numPileTradeRouteTokens > 0) {
				text += getContext().getString(R.string.token_trade_route) + getContext().getString(R.string.token_colon) + numPileTradeRouteTokens;
				text += "\n";
			}
			if (hasPlayerTokens) {
				for (int i = 0; i < currentTokens.length; ++i) {
					int[] playerTokens = currentTokens[i];
					if (playerTokens.length == 0)
						continue;
					text += players.getItem(i).name + getContext().getString(R.string.token_colon);
					String separator = "";
					boolean first = true;
					for (int tokenId : playerTokens) {
						PlayerSupplyToken tokenType = PlayerSupplyToken.getById(tokenId);
						int tokenNameId;
						if (tokenType != null) {
							switch(tokenType) {
								case PlusOneCard:
									tokenNameId = R.string.token_plus_one_card;
									break;
								case PlusOneAction:
									tokenNameId = R.string.token_plus_one_action; 
									break;
								case PlusOneBuy:
									tokenNameId = R.string.token_plus_one_buy;
									break;
								case PlusOneCoin:
									tokenNameId = R.string.token_plus_one_coin;
									break;
								case MinusTwoCost:
									tokenNameId = R.string.token_minus_2_cost;
									break;
								case Trashing:
									tokenNameId = R.string.token_trashing;
									break;
								default:
									continue;
							}
							text += separator + getContext().getString(tokenNameId);
							if (first) {
								first = false;
								separator = getContext().getString(R.string.token_separator);
							}
						}
					}
					text += "\n";
				}
			}
		}
		return text;
	}
	
	public String GetCardTypeString(MyCard c) {
	    String cardType = "";
	    Context context = getContext();
        
        if (c.isAction) {
            cardType += context.getString(R.string.type_action);
            
            if (c.isTreasure) {
                cardType += " - " + context.getString(R.string.type_treasure);
            }
            
            if (c.isAttack) {
                cardType += " - " + context.getString(R.string.type_attack);
            }
            
            if (c.isLooter) {
                cardType += " - " + context.getString(R.string.type_looter);
            }
            
            if (c.isRuins) {
                cardType += " - " + context.getString(R.string.type_ruins);
            }
            
            if (c.isPrize) {
                cardType += " - " + context.getString(R.string.type_prize);
            }
            
            if (c.isTraveller) {
                cardType += " - " + context.getString(R.string.type_traveller);
            }
            
            if (c.isReserve) {
                cardType += " - " + context.getString(R.string.type_reserve);
            }
            
            if (c.isDuration) {
                cardType += " - " + context.getString(R.string.type_duration);
            }
            
            if (c.isReaction) {
                cardType += " - " + context.getString(R.string.type_reaction);
            }
            
            if (c.isVictory) {
                cardType += " - " + context.getString(R.string.type_victory);
            }
            
            if (c.isKnight) {
                cardType += " - " + context.getString(R.string.type_knight);
            }
            
            if (c.isShelter) {
                cardType += " - " + context.getString(R.string.type_shelter);
            }
            
            if (c.isCastle) {
                cardType += " - " + context.getString(R.string.type_castle);
            }
            
            if (c.isGathering) {
                cardType += " - " + context.getString(R.string.type_gathering);
            }
        }
        else if (c.isTreasure) {
            cardType += context.getString(R.string.type_treasure);
            
            if (c.isAttack) {
                cardType += " - " + context.getString(R.string.type_attack);
            }

            if (c.isReserve) {
                cardType += " - " + context.getString(R.string.type_reserve);
            }
            
            if (c.isVictory) {
                cardType += " - " + context.getString(R.string.type_victory);
            }
            
            if (c.isReaction) {
                cardType += " - " + context.getString(R.string.type_reaction);
            }
            
            if (c.isPrize) {
                cardType += " - " + context.getString(R.string.type_prize);
            }
            
            if (c.isCastle) {
                cardType += " - " + context.getString(R.string.type_castle);
            }
        }
        else if (c.isVictory) {
            cardType += context.getString(R.string.type_victory);
            
            if (c.isShelter) {
                cardType += " - " + context.getString(R.string.type_shelter);
            }
            
            if (c.isReaction) {
                cardType += " - " + context.getString(R.string.type_reaction);
            }
            
            if (c.isCastle) {
                cardType += " - " + context.getString(R.string.type_castle);
            }
        }
        else if (c.isEvent) {
            cardType += context.getString(R.string.type_event);
        }
        else if (c.isLandmark) {
            cardType += context.getString(R.string.type_landmark);
        }
        else if (c.name.equalsIgnoreCase("hovel")) {
            cardType += context.getString(R.string.type_reaction) + " - " + context.getString(R.string.type_shelter);
        }
        else if (c.isCurse) {
            cardType += context.getString(R.string.type_curse);
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return true;
		}
		return false;
	}
}
