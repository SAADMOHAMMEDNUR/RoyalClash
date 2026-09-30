package io.github.saad.royalclash.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.saad.royalclash.model.Deck;
import io.github.saad.royalclash.model.Match;
import io.github.saad.royalclash.view.Hud;

/** Turns your clicks into match.playCard(). Click a card to select it, then click your half of the arena. */
public class PlayerController {
    private final Match match;
    private final Viewport viewport;
    private final Vector2 touch = new Vector2();
    private int selectedCard = -1;

    public PlayerController(Match match, Viewport viewport) {
        this.match = match;
        this.viewport = viewport;
    }

    public void update() {
        if (!Gdx.input.justTouched()) return;

        // convert window pixels into world coordinates
        touch.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touch);

        for (int i = 0; i < Deck.HAND_SIZE; i++) {
            if (Hud.cardBounds(i).contains(touch)) {
                selectedCard = (selectedCard == i) ? -1 : i; // click again to deselect
                return;
            }
        }

        if (selectedCard >= 0 && match.playCard(match.getPlayer(), selectedCard, touch.x, touch.y)) {
            selectedCard = -1;
        }
    }

    public int getSelectedCard() {
        return selectedCard;
    }
}
