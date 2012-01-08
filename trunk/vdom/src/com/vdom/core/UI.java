package com.vdom.core;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.api.GameEventListener;

@SuppressWarnings("serial")
public class UI extends JFrame implements GameEventListener {
    // speed bar
    // if no image, use text

    static final String VP_TEXT = "VP: ";
    static final String GAME_TYPE_TEXT = "Game: ";
    static final String TURN_TEXT = "Turn:   ";

    float speedFactor = 100f;
    boolean speedMax = false;

    static String downloadSite;

    public static final String[] downloadSiteDirs = new String[] { "common", "promo", "base", "intrigue", "seaside", "alchemy", "prosperity", };

    public static int GAME_END_SLEEP = 300;
    public static int TURN_END_SLEEP = 25;
    public static int TURN_BEGIN_SLEEP = 10;
    public static int PLAYING_ACTION_SLEEP = 5;
    public static int ACTION_PLAYED_SLEEP = 5;
    public static int COIN_PLAYED_SLEEP = 1;
    public static int CARD_OBTAINED_SLEEP = 5;
    public static int CARD_ADDED_TO_HAND_SLEEP = 1;
    public static int CARD_REMOVED_FROM_HAND_SLEEP = 1;

    public ConcurrentHashMap<String, ImageIcon> cardImages = new ConcurrentHashMap<String, ImageIcon>();

    double imageScale = .53;
    public File imagesDir;
    public ImageIcon defaultImage;
    public String defaultImageName = "blackmarket";

    static final Dimension minimumSize = new Dimension(1, 400);

    Game game;

    JLabel turnLabel;
    JLabel gameTypeLabel;

    PlayerStats[] playerStats;

    JPanel parentPanel;

    JPanel handPanel;
    JPanel playedPanel;
    JPanel obtainedPanel;

    public UI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void init(Game game) {
        this.game = game;
        setTitle("Dominion");

        String tempDirString = System.getProperty("java.io.tmpdir");
        File tempDir = new File(tempDirString);

        imagesDir = new File(tempDir, "dominioncards");

        if (downloadSite != null && !imagesDir.exists()) {
            imagesDir.mkdirs();
            downloadCards(downloadSite);
        }

        defaultImage = loadCardImage(defaultImageName);
        if (defaultImage == null) {
            System.err.println("Could not locate default image:" + imagesDir + "/" + defaultImageName + ".jpg");
            System.exit(1);
        }

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        parentPanel = new JPanel();
        // BoxLayout boxLayout = new BoxLayout(parentPanel, BoxLayout.Y_AXIS);
        // parentPanel.setLayout(boxLayout);
        parentPanel.setLayout(new GridLayout(3, 1));
        contentPane.add(parentPanel, BorderLayout.CENTER);

        JPanel statsPanel = new JPanel(new GridLayout(1, Game.numPlayers + 2));

        JPanel northPanel = new JPanel(new FlowLayout());
        contentPane.add(northPanel, BorderLayout.NORTH);
        northPanel.add(statsPanel);

        JPanel gameStatsPanel = new JPanel(new GridLayout(3, 1));
        gameStatsPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        gameTypeLabel = new JLabel();
        gameTypeLabel.setText(GAME_TYPE_TEXT + Game.gameType.toString());

        turnLabel = new JLabel();
        turnLabel.setText(TURN_TEXT + "1");

        gameStatsPanel.add(gameTypeLabel);
        gameStatsPanel.add(turnLabel);
        statsPanel.add(gameStatsPanel);

        playerStats = new PlayerStats[Game.numPlayers];
        int i = 0;
        for (Player player : Game.players) {
            playerStats[i] = new PlayerStats(player);
            playerStats[i].setBorder(new EmptyBorder(0, 10, 0, 10));
            statsPanel.add(playerStats[i]);
            i++;
        }

        // statsPanel.add(new JLabel(" "));
        speedFactor = 190;
        final JSlider speedSlider = new JSlider(1, 200, (int) speedFactor);
        speedSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (speedSlider.getValue() == 10) {
                    speedMax = true;
                } else {
                    speedMax = false;
                    speedFactor = 201 - speedSlider.getValue();
                }
            }
        });
        statsPanel.add(speedSlider);

        handPanel = new JPanel(new FlowLayout());
        handPanel.setMinimumSize(minimumSize);
        handPanel.setSize(minimumSize);
        parentPanel.add(handPanel);

        playedPanel = new JPanel(new FlowLayout());
        playedPanel.setMinimumSize(minimumSize);
        playedPanel.setSize(minimumSize);
        parentPanel.add(playedPanel);

        obtainedPanel = new JPanel(new FlowLayout());
        obtainedPanel.setMinimumSize(minimumSize);
        obtainedPanel.setSize(minimumSize);
        parentPanel.add(obtainedPanel);

        clearCardPanels();

        setSize(1500, 870);
        setVisible(true);
    }

    public void clear(JPanel panel) {
        panel.removeAll();
        // JLabel spacer = new JLabel("x");
        // spacer.setMinimumSize(minimumSize);
        // spacer.setSize(minimumSize);
        // panel.add(spacer);
    }

    public void showHand(Player player) {
        clear(handPanel);
        for (Card card : player.hand) {
            JLabel cardLabel = new JLabel();
            cardLabel.setIcon(getCardIcon(card));
            handPanel.add(cardLabel);
        }
        handPanel.revalidate();
        handPanel.repaint();
    }

    public void addCardToPlayedPanel(Card card) {
        JLabel cardLabel = new JLabel();
        cardLabel.setIcon(getCardIcon(card));
        playedPanel.add(cardLabel);
        playedPanel.revalidate();
        playedPanel.repaint();
    }

    public void addCardToObtainedPanel(Card card) {
        JLabel cardLabel = new JLabel();
        cardLabel.setIcon(getCardIcon(card));
        obtainedPanel.add(cardLabel);
        obtainedPanel.revalidate();
        obtainedPanel.repaint();
    }

    public void clearCardPanels() {
        clear(handPanel);
        clear(playedPanel);
        clear(obtainedPanel);

        // parentPanel.revalidate();

        handPanel.revalidate();
        playedPanel.revalidate();
        obtainedPanel.revalidate();

        handPanel.repaint();
        playedPanel.repaint();
        obtainedPanel.repaint();
    }

    public ImageIcon getCardIcon(Card card) {
        synchronized (cardImages) {
            if (!cardImages.contains(card.getName())) {
                ImageIcon cardImage = loadCardImage(card.getName());

                if (cardImage == null) {
                    Util.debug("ERROR::::::::::::::::::::::::::::::::::::: Could not load image for:" + card.getName());
                    cardImages.put(card.getName(), defaultImage);
                } else {
                    cardImages.put(card.getName(), cardImage);
                }
            }
        }

        return cardImages.get(card.getName());
    }

    public ImageIcon loadCardImage(String name) {
        try {
            File imageFile = new File(imagesDir, name + ".jpg");

            if (!imageFile.exists()) {
                StringBuilder sb = new StringBuilder();
                for (char c : name.toCharArray()) {
                    if (Character.isLetter(c)) {
                        sb.append(c);
                    }
                }
                String fixedName = sb.toString().toLowerCase();
                imageFile = new File(imagesDir, fixedName + ".jpg");
            }

            if (imageFile.exists() && imageFile.isFile() && imageFile.canRead()) {
                ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
                Image img = icon.getImage();
                Image newimg = img.getScaledInstance((int) (icon.getIconWidth() * imageScale), (int) (icon.getIconHeight() * imageScale),
                    java.awt.Image.SCALE_SMOOTH);
                icon = new ImageIcon(newimg);
                return icon;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateTurn() {
        turnLabel.setText(TURN_TEXT + game.turnCount);
    }

    public void gameEvent(GameEvent event) {
        if (event.getType() == GameEvent.Type.TurnBegin) {
            updateTurn();

            for (int i = 0; i < playerStats.length; i++) {
                playerStats[i].showTurnIndicator(i == Game.playersTurn);
            }

            // System.out.println("turn begin:" + event.getPlayer().getClass().getName());
            showHand(Game.players[Game.playersTurn]);
            sleep(TURN_BEGIN_SLEEP);
        }

        if (event.getType() == GameEvent.Type.TurnEnd) {

            // update the vps to show any cards that were trashed by other players
            for (PlayerStats stats : playerStats) {
                stats.refreshVps();
            }

            // System.out.println("turn end:" + event.getPlayer().getClass().getName());
            // showHand(Game.players[Game.playersTurn]);
            sleep(TURN_END_SLEEP);
            clearCardPanels();
        }

        if (event.getType() == GameEvent.Type.GameStarting) {
            clearCardPanels();
        }

        if (event.getType() == GameEvent.Type.GameOver) {
            clearCardPanels();
            playedPanel.add(new JLabel("(Game Over)"));
            playedPanel.revalidate();
            playedPanel.repaint();

            for (int i = 0; i < playerStats.length; i++) {
                playerStats[i].showTurnIndicator(false);
            }

            sleep(GAME_END_SLEEP);
        }

        if ((event.getType() == GameEvent.Type.PlayingAction || event.getType() == GameEvent.Type.PlayingDurationAction)
            && event.getPlayer() == Game.players[Game.playersTurn]) {
            // System.out.println("playing action:" + event.getCard().getName());
            showHand(Game.players[Game.playersTurn]);
            addCardToPlayedPanel(event.getCard());
            sleep(PLAYING_ACTION_SLEEP);
        }

        if ((event.getType() == GameEvent.Type.PlayedAction) && event.getPlayer() == Game.players[Game.playersTurn]) {
            // System.out.println("played action:" + event.getCard().getName());
            showHand(Game.players[Game.playersTurn]);
            sleep(ACTION_PLAYED_SLEEP);
        }

        if (event.getType() == GameEvent.Type.CardObtained && event.getPlayer() == Game.players[Game.playersTurn]) {
            // System.out.println("obtaining card:" + event.getCard().getName());
            playerStats[Game.playersTurn].refreshVps();
            addCardToObtainedPanel(event.getCard());
            sleep(CARD_OBTAINED_SLEEP);
        }

        if (event.getType() == GameEvent.Type.BuyingCard && event.getPlayer() == Game.players[Game.playersTurn]) {
            sleep(CARD_OBTAINED_SLEEP / 2);
            // System.out.println("obtaining card:" + event.getCard().getName());
            playerStats[Game.playersTurn].refreshVps();
            addCardToObtainedPanel(event.getCard());
            sleep(CARD_OBTAINED_SLEEP);
        }

        if (event.getType() == GameEvent.Type.CardAddedToHand && event.getPlayer() == Game.players[Game.playersTurn]) {
            // System.out.println("adding card:" + event.getCard().getName());

            showHand(Game.players[Game.playersTurn]);
            sleep(CARD_ADDED_TO_HAND_SLEEP);
        }

        if (event.getType() == GameEvent.Type.CardRemovedFromHand && event.getPlayer() == Game.players[Game.playersTurn]) {
            // System.out.println("removing card:" + event.getCard().getName());

            showHand(Game.players[Game.playersTurn]);
            sleep(CARD_REMOVED_FROM_HAND_SLEEP);
        }

        if ((event.getType() == GameEvent.Type.PlayingCoin) && event.getPlayer() == Game.players[Game.playersTurn]) {
            // System.out.println("playing coin:" + event.getCard().getName());
            showHand(Game.players[Game.playersTurn]);
            addCardToPlayedPanel(event.getCard());
            sleep(COIN_PLAYED_SLEEP);
        }
    }

    void downloadCards(String site) {
        try {
            if (!site.endsWith("/")) {
                site = site + "/";
            }
            for (String s : downloadSiteDirs) {
                String url = site + s;
                String listPage = downloadUrlAsString(url);

                if (!url.endsWith("/")) {
                    url = url + "/";
                }

                int at = 0;

                while (at != -1) {
                    at = listPage.indexOf(".jpg\"", at);
                    if (at != -1) {
                        int start = listPage.lastIndexOf("\"", at);
                        int end = listPage.indexOf("\"", at);

                        String image = listPage.substring(start + 1, end);

                        File imageFile = new File(imagesDir, image);

                        Util.debug("downloading:" + url + image);
                        downloadUrlToFile(url + image, imageFile);

                        at = end;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String downloadUrlAsString(String s) throws Exception {
        InputStream is = null;
        try {
            StringBuilder sb = new StringBuilder();
            URL u = new URL(s);
            is = new BufferedInputStream(u.openStream());

            int b;

            while ((b = is.read()) != -1) {
                sb.append((char) b);
            }

            return sb.toString();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                // Ignore...
            }
        }
    }

    void downloadUrlToFile(String s, File file) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        try {
            URL u = new URL(s);
            is = new BufferedInputStream(u.openStream());

            os = new BufferedOutputStream(new FileOutputStream(file));

            int b;
            while ((b = is.read()) != -1) {
                os.write(b);
            }
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                // Ignore...
            }

            try {
                os.close();
            } catch (Exception e) {
                // Ignore...
            }
        }
    }

    void sleep(int time) {
        try {
            int sleepTime = 0;

            if (speedMax) {
                sleepTime = 1;
            } else {
                sleepTime = (int) (time * speedFactor);
            }

            Thread.sleep(sleepTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class PlayerStats extends JPanel {
        Player player;
        // JLabel winsLabel;
        JLabel nameLabel;
        JLabel vpLabel;
        JLabel turnIndicator;

        public PlayerStats(Player player) {
            this.player = player;
            setLayout(new GridLayout(3, 1));

            // winsLabel = new JLabel();
            // winsLabel.setText(WINS_TEXT + );

            nameLabel = new JLabel();
            nameLabel.setText(player.getPlayerName());
            add(nameLabel);

            vpLabel = new JLabel();
            add(vpLabel);

            turnIndicator = new JLabel("^^^^^^^");
            add(turnIndicator);

            refreshVps();
        }

        public void showTurnIndicator(boolean on) {
            turnIndicator.setVisible(on);
        }

        public void refreshVps() {
            vpLabel.setText(VP_TEXT + Game.calculateVps(player));
        }
    }
}
