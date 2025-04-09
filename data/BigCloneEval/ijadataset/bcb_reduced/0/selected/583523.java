package org.silentsquare.p24;

import java.util.ArrayList;
import java.util.Random;

public class Deck {

    private ArrayList<Card> cards;

    private int idx;

    private Deck(ArrayList<Card> cards) {
        this.cards = cards;
    }

    ArrayList<Card> getCards() {
        return cards;
    }

    public static Deck getShuffledDeck() {
        Deck deck = new Deck(Card.newDeck());
        deck.shuffle();
        return deck;
    }

    void shuffle() {
        Random random = new Random();
        for (int i = cards.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Card c = cards.get(j);
            cards.set(j, cards.get(i));
            cards.set(i, c);
        }
    }

    public Card[] getTopFourCards() {
        Card[] four = new Card[4];
        if (idx + 4 > cards.size()) {
            shuffle();
            idx = 0;
        }
        for (int i = 0; i < 4; i++) four[i] = cards.get(idx++);
        return four;
    }
}
