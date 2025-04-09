package net.sourceforge.pokerapp;

import java.util.Random;
import java.awt.Image;

/****************************************************
 * The deck class represents the entire deck.  It contains functions to shuffle the deck
 * and deal the next card.  It also contains the image on the back of all the cards.
 *
 * @author Dan Puperi
 * @version 1.00
 *
 **/
public class Deck {

    /**
 * Constant number of cards in a deck
 **/
    public static final int NUM_CARDS = 52;

    /**
 * Array of the cards
 **/
    public Card cards[];

    /**
 * Current location in the array
 **/
    public int position;

    /**
 * Image on the back of the cards.
 **/
    public Image imgCardBack;

    private Random r;

    /***************************
 * The default constructor creates a deck with no back of the card image.
 **/
    public Deck() {
        cards = new Card[NUM_CARDS];
        position = 0;
        for (int i = 0; i < Card.NUM_SUITS; i++) {
            for (int j = 0; j < Card.NUM_RANKS; j++) {
                cards[position] = new Card(j + 1, i + 1);
                position++;
            }
        }
        position = 0;
        r = new Random(System.currentTimeMillis());
        imgCardBack = null;
    }

    /***************************
 * The constructor creates a deck with a given image used as the card backs.
 *
 * @param image The Image class which is the picture on the back of the cards.
 *
 **/
    public Deck(Image image) {
        cards = new Card[NUM_CARDS];
        position = 0;
        for (int i = 0; i < Card.NUM_SUITS; i++) {
            for (int j = 0; j < Card.NUM_RANKS; j++) {
                cards[position] = new Card(j + 1, i + 1);
                position++;
            }
        }
        position = 0;
        r = new Random(System.currentTimeMillis());
        imgCardBack = image;
    }

    /***************************
 *  shuffle() Shuffles the deck
 **/
    public void shuffle() {
        Card tempCard = new Card();
        for (int i = 0; i < NUM_CARDS; i++) {
            int j = i + r.nextInt(NUM_CARDS - i);
            tempCard = cards[j];
            cards[j] = cards[i];
            cards[i] = tempCard;
        }
        position = 0;
    }

    /***************************
 *  deal() returns the card at the top of the deck
 *
 * @return The Card a the top of the deck.
 *
 **/
    public Card deal() {
        if (position < NUM_CARDS) {
            position++;
            return cards[position - 1];
        } else {
            return null;
        }
    }
}
