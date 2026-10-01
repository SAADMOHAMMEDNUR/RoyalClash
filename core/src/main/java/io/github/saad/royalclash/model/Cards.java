package io.github.saad.royalclash.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Cards {
    private Cards() {}

    public static final Card KNIGHT = new Card("Knight", "K",  3, 1766, 202,  12f, 22f, 35f, 1.2f, 1, false, false);
    public static final Card ARCHERS = new Card("Archers","A",  3,  304, 107, 125f, 16f, 35f, 0.9f, 2, false, false);
    public static final Card GIANT = new Card("Giant","G",  5, 4091, 254,  12f, 30f, 25f, 1.5f, 1, true,  false);
    public static final Card MINI_PEKKA = new Card("Mini PEKKA","MP", 4, 1361, 720,  12f, 20f, 50f, 1.6f, 1, false, false);
    public static final Card SKELETONS = new Card("Skeletons","S",  1,   81,  81,  10f, 12f, 60f, 1.0f, 3, false, false);
    public static final Card MUSKETEER = new Card("Musketeer","M",  4,  720, 218, 150f, 18f, 35f, 1.0f, 1, false, false);
    public static final Card HOG_RIDER = new Card("Hog Rider", "H",  4, 1697, 318,  12f, 22f, 60f, 1.6f, 1, true,  false);
    public static final Card GOBLINS = new Card("Goblins", "Gb", 2,  202, 120,  10f, 14f, 60f, 1.1f, 3, false, false);

    // Tower cards
    public static final Card PRINCESS_TOWER = new Card("Princess Tower", "", 0, 3052, 109, 150f, 60f, 0f, 0.8f, 1, false, true);
    public static final Card KING_TOWER     = new Card("King Tower",     "", 0, 4824, 109, 140f, 80f, 0f, 1.0f, 1, false, true);

    public static final List<Card> STARTER_DECK = Collections.unmodifiableList(Arrays.asList(
        KNIGHT, ARCHERS, GIANT, MINI_PEKKA, SKELETONS, MUSKETEER, HOG_RIDER, GOBLINS));
}
