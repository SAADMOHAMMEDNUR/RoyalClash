package io.github.saad.royalclash.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 4 cards in hand, the rest wait in a queue. Played card goes to the back, front card fills the slot. */
public class Deck {
    public static final int HAND_SIZE = 4;

    private final Card[] hand = new Card[HAND_SIZE];
    private final ArrayDeque<Card> queue = new ArrayDeque<>();

    public Deck(List<Card> cards) {
        if (cards.size() <= HAND_SIZE) {
            throw new IllegalArgumentException("Deck needs more than " + HAND_SIZE + " cards");
        }
        List<Card> shuffled = new ArrayList<>(cards);
        Collections.shuffle(shuffled);
        for (int i = 0; i < HAND_SIZE; i++) hand[i] = shuffled.get(i);
        for (int i = HAND_SIZE; i < shuffled.size(); i++) queue.addLast(shuffled.get(i));
    }

    public Card getCard(int handIndex) { return hand[handIndex]; }
    public Card peekNext() { return queue.peekFirst(); }

    void cycle(int handIndex) {
        queue.addLast(hand[handIndex]);
        hand[handIndex] = queue.pollFirst();
    }
}
