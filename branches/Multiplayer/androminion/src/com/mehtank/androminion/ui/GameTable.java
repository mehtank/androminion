package com.mehtank.androminion.ui;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mehtank.androminion.R;
import com.mehtank.androminion.activities.GameActivity;
import com.mehtank.androminion.ui.CardView.CardState;
import com.mehtank.androminion.util.Achievements;
import com.mehtank.androminion.util.CardGroup;
import com.mehtank.androminion.util.HapticFeedback;
import com.mehtank.androminion.util.HapticFeedback.AlertType;
import com.mehtank.androminion.util.PlayerAdapter;
import com.mehtank.androminion.util.PlayerSummary;
import com.vdom.api.Card;
import com.vdom.comms.Event;
import com.vdom.comms.Event.EventObject;
import com.vdom.comms.GameStatus;
import com.vdom.comms.MyCard;
import com.vdom.comms.SelectCardOptions;
import com.vdom.comms.SelectCardOptions.PickType;
import com.vdom.core.Cards;
import com.vdom.core.Player.SpiceMerchantOption;
import com.vdom.core.Player.TorturerOption;

public class GameTable extends LinearLayout implements OnItemClickListener, OnItemLongClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "GameTable";

    private final GameActivity top;

    private PlayerAdapter players = new PlayerAdapter(getContext());

    public PlayerAdapter getPlayerAdapter() {
        return players;
    }

    GridView handGV, playedGV, islandGV, villageGV, trashGV;
    CardGroup hand, played, island, village, trash;
    View islandColumn, villageColumn, trashColumn;
    TextView playedHeader;
    LinearLayout myCards;

    GridView moneyPileGV, vpPileGV, supplyPileGV, prizePileGV, nonSupplyPileGV;
    CardGroup moneyPile, vpPile, supplyPile, prizePile, nonSupplyPile;

    LinearLayout tr;
    LinearLayout gameOver;
    ScrollView gameOverScroll;

    View supply;
    LinearLayout turnView;
    View myCardView;
    private static int[] costs = {};

    TextView actionText;
    LinearLayout deckStatus;
    TurnView turnStatus;
    /**
     * Button that are pressed to confirm or decline an option
     */
    Button select, pass;
    String indicator;
    GameScrollerView gameScroller;
    TextView latestTurn;
    SelectStringView sv;

    Achievements achievements;
    CardAnimator animator;

    ArrayList<ToggleButton> showCardsButtons = new ArrayList<ToggleButton>();

    /**
     * Information about a selected card: group, position in that group, CardState
     */
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
    /**
     * List of cards that were selected
     */
    ArrayList<CardInfo> openedCards = new ArrayList<CardInfo>();
    int maxOpened = 0;
    boolean exactOpened = true;
    boolean myTurn;

    boolean finalStatsReported = false;

    double textScale = GameTableViews.textScale;

    private HelpView helpView;

    /**
     * Initialize the card groups at the top of the screen, where the card piles go
     */
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

        nonSupplyPile = new CardGroup(top, true);
        nonSupplyPileGV = (GridView) findViewById(R.id.nonSupplyPileGV);
        nonSupplyPileGV.setAdapter(nonSupplyPile);
        nonSupplyPileGV.setOnItemClickListener(this);
        nonSupplyPileGV.setOnItemLongClickListener(this);

        prizePile = new CardGroup(top, true);
        prizePileGV = (GridView) findViewById(R.id.prizePileGV);
        prizePileGV.setAdapter(prizePile);
        prizePileGV.setOnItemClickListener(this);
        prizePileGV.setOnItemLongClickListener(this);
    }

    /**
     * Initialize card groups that belong to the user (hand, played, island, village)
     */
    private void initHand() {
        hand = new CardGroup(top, false);
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

        trash = new CardGroup(top, false);
        trashGV = (GridView) findViewById(R.id.trashGV);
        trashGV.setAdapter(trash);
        trashGV.setOnItemLongClickListener(this);
        trashColumn = findViewById(R.id.trashColumn);

        playedHeader = (TextView) findViewById(R.id.playedHeader);

        //only for help
        myCardView = findViewById(R.id.myCardView);
    }

    /**
     * Initialize player information list (right of the screen)
     */
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
        players.setContainer(deckStatus);
        deckStatus.setEnabled(true);

        turnStatus = new TurnView(top);
        turnStatus.setTextSize(12.0f);

        //turnView.addView(turnStatus, 2);

        actionText = (TextView) findViewById(R.id.actionText);
        players.setTurnStatus(turnStatus);
        showCardsButtons.clear();
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

        LayoutInflater.from(context).inflate(R.layout.view_gametable, this, true);

        supply = findViewById(R.id.supply);
        initTable();
        tr = (LinearLayout) findViewById(R.id.tr);
        initHand();
        initTurnPanel();
        gameOver = (LinearLayout) findViewById(R.id.gameOver);
        gameOverScroll = (ScrollView) findViewById(R.id.gameOverScroll);
        gameScroller = (GameScrollerView) findViewById(R.id.gameScroller);
        gameScroller.setGameEvent("Dominion app loaded!", true, 0);

        /**
         * findViewById(R.id.actionText) must be in here so that it gets fadet out whenever everything else fades out.
         */
        helpView = new HelpView(this.top, new View[] {supply, turnView, myCardView, gameScroller, findViewById(R.id.actionText)}, new View[] {tr, supply, supply, tr});
    }

    public void showHelp(int page) {
        try {
            top.addView(helpView);
        } catch (IllegalStateException e) {}
        helpView.showHelp(page);
    }


    /**
     * Turn log view on or off
     */
    public void logToggle() {
        if (gameScroller.getVisibility() != VISIBLE)
            gameScroller.setVisibility(VISIBLE);
        else
            gameScroller.setVisibility(GONE);
    }

    /**
     * A new game was stared, we are given the information about the game setup
     * @param cards Cards that are in play
     * @param players Names of players
     */
    public void newGame(MyCard[] cards, String[] players) {
        GameTableViews.clearCards();
        openedCards.clear();
        moneyPile.clear();
        vpPile.clear();
        supplyPile.clear();
        prizePile.clear();
        nonSupplyPile.clear();
        hand.clear();
        played.clear();
        island.clear();
        village.clear();
        trash.clear();
        this.players.clear();

        actionText.setText("");
        gameScroller.clear();
        gameScroller.setNumPlayers(players.length);
        gameOver.setVisibility(GONE);
        gameOver.removeAllViews(); // FIX: After playing two games in a row, winners of both games are displayed without this fix.
        gameOverScroll.setVisibility(GONE);
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
        //check if there's a colony
        for (MyCard c : cards)
            if(c.originalSafeName.equals("Colony")) {
                colonyInPlay = true;
                break;
            }

        boolean potionInPlay = false;
        // check if there's a potion
        for (MyCard c : cards)
            if(c.isPotion) {
                potionInPlay = true;
                break;
            }

        // adjust size of pile table
        if(potionInPlay && platInPlay)
            moneyPileGV.setNumColumns(5);
        else
            moneyPileGV.setNumColumns(4);

        if(!colonyInPlay)
            vpPileGV.setNumColumns(4);
        else
            vpPileGV.setNumColumns(5);

        short nonSupplyCardsInPlay = 0;

        for (MyCard c : cards)
        {
            // More types to check for eventually...
            if (c.originalSafeName.equals("Spoils") ||
                c.originalSafeName.equals("Mercenary") ||
                c.originalSafeName.equals("Madman"))
            {
                ++nonSupplyCardsInPlay;
            }
        }

        if (nonSupplyCardsInPlay > 4)
        {
            nonSupplyPileGV.setNumColumns(nonSupplyCardsInPlay);
        }
        else
        {
            nonSupplyPileGV.setNumColumns(4);
        }

        // done setting up, remove splash screen
        top.nosplash();
        // log start of game
        gameScroller.setGameEvent(top.getString(R.string.game_loaded), true, 0);
    }

    /**
     * initialize a card pile for the table
     *
     * This is called upon setup from within newGame
     *
     * @param c cart type
     */
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
        else if (c.pile == MyCard.NON_SUPPLY_PILE)
            nonSupplyPile.addCard(c);
    }

    /**
     * Make the 'select' button clickable. Call this when the current selection of cards is valid, e.g. enough cards selected.
     */
    private void canSelect() {
        select.setEnabled(true);
    }
    /**
     * Make the 'select' button not clickable, if selection of cards is invalid
     */
    private void cannotSelect() {
        select.setEnabled(false);
    }

    /**
     * Is the given card an acceptable choice given the constrains saved in sco?
     * @param c chosen card
     * @param parent which pile the card was selected from
     * @return
     */
    boolean isAcceptable(MyCard c, CardGroup parent) {
        if (sco.fromHand && (parent != hand)) return false;
        else if (sco.fromTable) {
            //          if (!sco.allowEmpty) {
            //              if (lastSupplySizes[c.id] == 0)
            //                  return false;
            //          }
            if (sco.fromPrizes) {
                if ((parent != vpPile)
                    &&  (parent != moneyPile)
                    &&  (parent != supplyPile)
                    &&  (parent != prizePile)
                    &&  (parent != nonSupplyPile)) return false;
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

    /**
     * Save the state of what kind of cards we may currently select
     */
    SelectCardOptions sco = null;

    /**
     * firstPass is true if the user clicked 'pass' once already and is asked if he is sure right now.
     */
    boolean firstPass = false;
    boolean canClick = true;
    String prompt = "";

    private int[] lastSupplySizes;

    private int[] lastEmbargos;

    void resetButtons() {
        CharSequence selectText = select.getText();
        pass.setText(selectText.subSequence(0, selectText.length()-1));
        selectButtonState();
        actionText.setText(prompt);
        canClick = true;
    }

    /**
     * Something was declined and we ask if the user is really, really sure
     */
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

    /**
     * One of the two buttons was clicked
     * @param b Button that was clicked
     */
    public void cardSelected(Button b) {
        if (sco == null)
            return;

        if (b == select) {
            if (firstPass) { // 'yes, we want to decline', send empty selection.
                // if 'firstPass' is true, we already checked that an empty selection is valid
                top.handle(new Event(Event.EType.CARD)
                           .setInteger(0));
            } else { // We selected cards and now send a CARD event.
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
        // we accepted or declined => remove selection markers, make buttons invisible
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

    /**
     * Calls selectString after getting the right (localized) strings from R.
     */
    public void selectOption(Event e) {
        String[] options = Strings.getOptions(e.c, e.o.os);
        selectString(Strings.getSelectOptionHeader(e.c, e.o.os), options, Event.EType.OPTION);
    }

    /**
     * Prompt the user about options that a card gives us. (E.g. Thief: Which card to trash)
     *
     * This isn't ever called directly anymore, because having the server send strings to display
     * is just messy.  But this is still a useful primitive method that other methods call.  For
     * example, the server will send a GETOPTION event to the client (this code), and then we use
     * the information in the GETOPTION event to figure out what strings to display, then call this
     * method.
     *
     * @param title Message to the user about what to choose
     * @param options Options that the user has
     */
    public void selectString(String title, String[] options, Event.EType eventType) {
        /*
         * TODO(matt): Because we got rid of the STRING event, we need to handle this differently
         * for each eventType we're passed in (it could be OPTION or BOOLEAN).  But I think all of
         * these cases are caught in the VDOM code, anyway, so this shouldn't change anything.  I'm
         * leaving the code here, though, in case I'm wrong about that and we need to fix it.
        if (options.length == 1) {
            Toast.makeText(top, title + ":\n" + options[0], Toast.LENGTH_LONG / 2).show();
            top.handle(new Event(Event.EType.STRING).setString(options[0]));
            return;
        }
        */
        HapticFeedback.vibrate(getContext(),AlertType.SELECT);
        new SelectStringView(top, title, options, eventType);
    }

    /**
     * RemotePlayer wants us to choose between two options.  We call selectString after setting up
     * the strings that represent the options we're choosing between.  The action that represents
     * "true" should always be placed first in the list of options.
     * @param cardResponsible The card that initiated this action.  We use this, along with extras,
     * to determine what the two options are.
     * @param extras Extra information (if necessary) to determine the specifics of the options.
     */
    public void selectBoolean(Card cardResponsible, Object[] extras) {
        String[] strings = Strings.getBooleanStrings(cardResponsible, extras);
        // We lump all of the strings together into a single method to make the logic simpler in
        // Strings.getBooleanStrings.  It makes for a little bit of moving things around here, but
        // I think it's better to keep the Strings call simpler.
        String header = strings[0];
        String[] options = new String[2];
        options[0] = strings[1];
        options[1] = strings[2];
        selectString(header, options, Event.EType.BOOLEAN);
    }

    /**
     * RemotePlayer wants us to choose card(s)
     * @param sco What kind of cards to choose, and what for
     * @param s Prompt to display
     * @param maxOpened How many cards may be selected
     * @param exactOpened May we choose less than the max number of cards?
     */
    public void selectCard(SelectCardOptions sco, String s, int maxOpened, boolean exactOpened) {
        this.sco = sco;

        this.maxOpened = maxOpened;
        this.exactOpened = exactOpened;

        if (sco.isBuyPhase) {
            s = Strings.getString(R.string.part_buy);
        } else if (sco.isActionPhase) {
            s = Strings.getString(R.string.part_play);
        } else if (sco.isTreasurePhase) {
            s = Strings.getString(R.string.use_for_money);
        } else if (s == null) {
            // TODO(matt): maybe these methods could be named better...
            s = Strings.getActionCardText(sco);
        }
        prompt = Strings.getSelectCardText(sco, s);

        firstPass = false;
        resetButtons();

        HapticFeedback.vibrate(getContext(),AlertType.SELECT);
        select.setVisibility(VISIBLE);
        if (sco.isPassable()) {
            pass.setVisibility(VISIBLE);
            if (sco.isBuyPhase) {
                pass.setText(Strings.getString(R.string.end_turn));
            } else {
                pass.setText(Strings.getString(R.string.none));
            }
        } else
            pass.setVisibility(INVISIBLE);

        selectButtonState();
    }

    /**
     * Update the 'state' (grayed out or not, text) of the select button, when a card was (un)selected.
     */
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

            // update indicator for default play action card
            if (sco.defaultCardSelected != -1 && sco.fromHand) {
                int pos = hand.getPos(sco.defaultCardSelected);
                if (pos != -1) {
                    CardView cv = (CardView) hand.getView(pos, null, null);
                    cv.setChecked(true, sco.getPickType().indicator());
                    CardInfo ci = new CardInfo(cv.getState(), hand, pos);
                    openedCards.add(ci);
                    hand.updateState(pos, cv.getState());
                }
            } else {
                return;
            }
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

    /**
     * Sets the button text corresponding to why the user has to pick cards
     * @param key PickType
     */
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

    /**
     * Prompt user about ordering cards (mostly to put them back on the deck in an order of his choosing)
     * @param cards Indices into the cards the user should put into order
     */
    public void orderCards(int[] cards) {
        String name = GameTableViews.cardsInPlay.get(cards[0]).name;
        boolean allequal = true;
        for (int c: cards) {
            if (GameTableViews.cardsInPlay.get(c).name != name) {
                allequal = false;
                break;
            }
        }
        if (allequal) {
            int[] is = new int[cards.length];
            for (int j = 0; j < cards.length; j++) {
                is[j] = j;
            }
            top.handle(new Event(Event.EType.CARDORDER).setObject(new EventObject(is)));
        } else {
            HapticFeedback.vibrate(getContext(),AlertType.SELECT);
            new OrderCardsView(top, Strings.getString(R.string.card_order_on_deck), cards);
        }
    }

    /**
     * Display the numbers of pile sizes for the card piles on the 'table'
     * @param supplySizes Sizes of piles
     * @param embargos number of embargos
     */
    public void setSupplySizes(int[] supplySizes, int[] embargos) {
        moneyPile.updateCounts(supplySizes, embargos);
        vpPile.updateCounts(supplySizes, embargos);
        supplyPile.updateCounts(supplySizes, embargos);
        prizePile.updateCounts(supplySizes, embargos);
        nonSupplyPile.updateCounts(supplySizes, embargos);
    }

    /**
     * Is executed instead of setStatus if the gs.isFinal flag is set.
     *
     * This gets executed /for each player/.
     *
     * @param gs
     */
    public void finalStatus(GameStatus gs) {
        if (!gs.isFinal) // does not happen as far as we know
            return;

        int maxVP = 0;
        int minTurns = 10000;
        /**
         * The player(s) to show as winner
         */
        ArrayList<Integer> winners = new ArrayList<Integer>();


        for (int i=0; i<players.getCount(); i++) { //winners are calculated in this loop
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
                ArrayList<String> pl = new ArrayList<String>(players.getCount());
                for(int i=0; i < players.getCount(); i++) {
                    pl.add(gs.realNames[i]);
                }
                achievements.gameOver(pl, winners);
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
        gameOverScroll.setVisibility(VISIBLE);
        @SuppressWarnings("deprecation")
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        FinalView fv = new FinalView(top, this, gs.realNames[gs.whoseTurn], gs.turnCounts[gs.whoseTurn],
                                     gs.embargos,
                                     gs.numCards[gs.whoseTurn], gs.supplySizes,
                                     gs.handSizes[gs.whoseTurn], won);
        fv.setLayoutParams(lp);
        showCardsButtons.add(fv.showCards);
        gameOver.addView(fv);
    }

    void uncheckAllShowCardsButtons() {
        for (ToggleButton t : showCardsButtons)
            t.setChecked(false);
        setSupplySizes(this.lastSupplySizes, this.lastEmbargos);
    }

    /**
     * User got an achievement
     * @param achievement Achievement name
     */
    public void achieved(String achievement) {
        try {
            achievements.achieved(achievement);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The RemotePlayer updated us about the setup of the game, and we display the changes to the user.
     * @param gs GameStatus object contains all this information
     * @param s ?? don't know yet ??
     * @param newTurn ?? don't know yet ??
     */
    public void setStatus(GameStatus gs, String s, boolean newTurn) {
        if (s != null)
            gameScroller.setGameEvent(s, newTurn, gs.isFinal ? 0 : gs.turnCounts[gs.whoseTurn]);

        if (gs.isFinal) {
            HapticFeedback.vibrate(getContext(),AlertType.FINAL);
            finalStatus(gs);
            return;
        }
        if (players.getCount() <= gs.whoseTurn) {
            for (int i = players.getCount(); i < gs.whoseTurn; i++)
                addPlayer("--");
            addPlayer(gs.name);
        } else {
            PlayerSummary ps = players.getItem(gs.whoseTurn);
            ps.name = gs.name;
        }

        if (newTurn) {
            myTurn = gs.whoseTurn == 0;
            if (myTurn)
                HapticFeedback.vibrate(getContext(),AlertType.TURNBEGIN);
        }

        turnStatus.setStatus(gs.turnStatus, gs.potions, myTurn);
        for (int i=0; i<players.getCount(); i++) {
            players.getItem(i).set(players.getItem(i).name, gs.turnCounts[i], gs.deckSizes[i], gs.handSizes[i], gs.numCards[i], gs.pirates[i], gs.victoryTokens[i], gs.guildsCoinTokens[i], gs.whoseTurn == i);
        }
        players.notifyDataSetChanged();

        actionText.setText("");

        if(newTurn) {
            String header = top.getString(R.string.played_header);
            if(!myTurn) {
                header = players.getItem(gs.whoseTurn).name + ":";
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

        GameTableViews.newCardGroup(trash, gs.trashPile);
        if (gs.trashPile.length > 0) {
            trashColumn.setVisibility(VISIBLE);
        } else {
            trashColumn.setVisibility(GONE);
        }

        this.lastSupplySizes = gs.supplySizes;
        this.lastEmbargos = gs.embargos;
        costs = gs.costs;

        supplyPile.updateCardName(gs.ruinsID, gs.ruinsTopCard, gs.ruinsTopCardDesc);
        supplyPile.updateCardName(gs.knightsID, gs.knightsTopCard, gs.knightsTopCardDesc, gs.knightsTopCardCost);
        setSupplySizes(gs.supplySizes, gs.embargos);
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
            ((CardView) v).setCost(getCardCost(((CardView) v).getCard()), ((CardView) v).getCard().isOverpay);
        } else if (v instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
                setCardCosts(((ViewGroup) v).getChildAt(i));
        }
    }

    private void addPlayer(String name) {
        players.add(new PlayerSummary(name));
    }

    /**
     * A player obtained a card and we notify the user
     * @param i card index
     * @param s player number, as a string
     * @return a message, either something like 'card obtained' from resources, or "&lt;playername&gt;: &lt;cardname&gt;"
     */
    public String cardObtained(int i, String s) {
        return top.getString(R.string.obtained, showCard(i, s, CardAnimator.ShowCardType.OBTAINED));
    }

    /**
     * A player trashed a card and we notify the user
     * @param i card index
     * @param s player number, as a string
     * @return a message, either something like 'card trashed' from resources, or "&lt;playername&gt;: &lt;cardname&gt;"
     */
    public String cardTrashed(int i, String s) {
        return top.getString(R.string.trashed, showCard(i, s, CardAnimator.ShowCardType.TRASHED));
    }

    /**
     * A player revealed a card and we notify the user
     * @param i card index
     * @param s player number, as a string
     * @return a message, either something like 'card revealed' from resources, or "&lt;playername&gt;: &lt;cardname&gt;"
     */
    public String cardRevealed(int i, String s) {
        return top.getString(R.string.revealed, showCard(i, s, CardAnimator.ShowCardType.REVEALED));
    }
    public String cardRevealedFromHand(int i, String s) {
        return top.getString(R.string.revealed_from_hand, showCard(i, s, CardAnimator.ShowCardType.REVEALED));
    }
    /**
     * Notify the user of a card played/revealed/... by a player.
     * @param card card index
     * @param playerInt player number, as a string
     * @param type how to show the card
     * @return "&lt;playername&gt;: &lt;cardname&gt;"
     */
    public String showCard (int card, String playerInt, CardAnimator.ShowCardType type) {
        int player = 0;
        CardView c = GameTableViews.getCardView(top, this, card);
        try {
            player = Integer.parseInt(playerInt);
        } catch (NumberFormatException e) {
            return "";
        }
        View anchor = deckStatus.getChildAt(player);
        if (anchor != null) {
            animator.init(anchor);
            c.setOnTable(false);
            animator.showCard(c, type);
        }

        return players.getItem(player).name + ": " + c.getCard().name;
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
                if(ci.cs == clickedCard.getState() && ci.pos == position) { // this is the card we clicked
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
