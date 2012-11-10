package com.mehtank.androminion.ui;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mehtank.androminion.R;
import com.mehtank.androminion.util.Achievements;

/**
 * Shows statistics about wins and losses for each player known to Androminion
 * (humans and computers).
 */
public class WinLossView extends FrameLayout {
	@SuppressWarnings("unused")
	private static final String TAG = "WinLossView";

	public WinLossView(Context context) {
		this(context, null);
	}

	public WinLossView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public WinLossView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context top) {
		LinearLayout linearLayout = new LinearLayout(top);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		Achievements achievements = new Achievements(top);

		// Put human players to the top of the list
		ArrayList<String> players = new ArrayList<String>();
		int nrOfHumans = 0;
		for (String s : achievements.getAllPlayers()) {
			if (Achievements.isHumanPlayer(s)) {
				players.add(nrOfHumans, s);
				nrOfHumans++; // This should put players in the same order as Achievements delivers.
			} else {
				players.add(s);
			}
		}

		// When there are neither human nor computer players, create the human player
		if (players.size() == 0) {
			players.add("You");
		}

		for (String player : players) {
			TextView textView;

			// Calculate wins, losses and percentage
			int wins = achievements.getTotalWins(player);
			int losses = achievements.getTotalLosses(player);
			int percent = (int) ((float) wins / (wins + losses) * 100);

			// Headline with player name and basic statistics
			textView = new TextView(top);
			textView.setTextAppearance(getContext(), android.R.style.TextAppearance_Large);
//			textView.setTextAppearance(top, android.R.attr.textColorPrimary);
//			textView.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
//			textView.setTextSize(getResources().getDimension(R.dimen.winloss_playername));

			String text = "  " + player + " - " + wins + "/" + (wins + losses) + " (" + percent + " %)";
			textView.setText(text);
			linearLayout.addView(textView);

			// Second headline with current winning streak
			textView = new TextView(top);
			textView.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
//			textView.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
//			textView.setTextSize(getResources().getDimension(R.dimen.winloss_streak));

			int streak = achievements.getWinStreak(player);
			if (streak > 0) {
				text = "  * " + top.getString(R.string.currentWinStreak) + streak;
				textView.setText(text);
				linearLayout.addView(textView);
			}

			// Detailed statistics split by number of players in game
			for (int numPlayers = 2; numPlayers <= 6; numPlayers++) {
				wins = achievements.getPlayerWins(player, numPlayers);
				losses = achievements.getPlayerLosses(player, numPlayers);
				percent = (int) ((float) wins / (wins + losses) * 100);
				
				textView = new TextView(top);
//				textView.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
//				textView.setTextSize(getResources().getDimension(R.dimen.winloss_detailed));

				text = "     " + numPlayers + " " + top.getString(R.string.win_loss_playerwins) + " " + wins + "/" + (wins + losses) + " (" + percent + " %)";
				textView.setText(text);
				linearLayout.addView(textView);
			}
		}
		addView(linearLayout);
	}
}
