package io.github.saad.royalclash.view;

import com.badlogic.gdx.math.Rectangle;

/** Where the HUD pieces sit on screen. The renderer draws them, PlayerController checks clicks on them. */
public final class Hud {
    private Hud() {}

    public static final Rectangle ELIXIR_BAR = new Rectangle(90f, 12f, 344f, 20f);
    public static final Rectangle NEXT_CARD = new Rectangle(12f, 45f, 62f, 70f);

    private static final float CARD_W = 80f;
    private static final float CARD_H = 95f;
    private static final float CARD_GAP = 8f;
    private static final float CARDS_X = 90f;
    private static final float CARDS_Y = 42f;

    public static Rectangle cardBounds(int handIndex) {
        return new Rectangle(CARDS_X + handIndex * (CARD_W + CARD_GAP), CARDS_Y, CARD_W, CARD_H);
    }
}
