package com.gencom.poker.cards;

import java.util.HashMap;
import java.util.Map;

public final class Deck {

    public String[] table;

    public String[] hand;

    protected int[] deck;

    protected int currentDeckPosition;

    public static final Map<String, Integer> cardMap = new HashMap<String, Integer>();

    public static final String[] defaultDeck = new String[52];

    private final RandomNumberSource rand = new RandomNumberSource();

    static {
        String[] colors = { "s", "h", "d", "t" };
        String[] highCards = { "T", "J", "Q", "K", "A" };
        for (int i = 2; i < 11; i++) {
            for (int j = 0; j < 4; j++) {
                defaultDeck[(i - 2) * 4 + j] = i + colors[j];
                cardMap.put(defaultDeck[(i - 2) * 4 + j], (i - 2) * 4 + j);
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < highCards.length; j++) {
                defaultDeck[32 + (4 * j) + i] = highCards[j] + colors[i];
                cardMap.put(defaultDeck[32 + (4 * j) + i], 32 + (4 * j) + i);
            }
        }
    }

    public Deck() {
        deck = new int[52];
        for (int i = 0; i < deck.length; i++) {
            deck[i] = i;
        }
    }

    public void shuffle() {
        currentDeckPosition = 0;
        int n = 52;
        while (--n > 0) {
            int k = rand.nextInt(n + 1);
            int temp = deck[n];
            deck[n] = deck[k];
            deck[k] = temp;
        }
    }

    public String getNextCard(String[] table, String[][] hand) {
        String card = null;
        do {
            card = defaultDeck[deck[currentDeckPosition++]];
        } while (isAlreadyGiven(card, table, hand));
        return card;
    }

    public String getNextCard() {
        String card = defaultDeck[deck[currentDeckPosition++]];
        return card;
    }

    private boolean isAlreadyGiven(String card, String[] table, String[][] hand) {
        boolean result = false;
        for (String cardT : table) {
            if (cardT != null && cardT.equals(card)) result = true;
        }
        for (int i = 0; i < hand.length; i++) {
            for (String cardT : hand[i]) {
                if (cardT != null && cardT.equals(card)) result = true;
            }
        }
        return result;
    }
}
