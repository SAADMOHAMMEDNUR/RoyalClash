package io.github.saad.royalclash.controller;

import io.github.saad.royalclash.model.Arena;
import io.github.saad.royalclash.model.Card;
import io.github.saad.royalclash.model.Deck;
import io.github.saad.royalclash.model.Match;
import io.github.saad.royalclash.model.Player;
import io.github.saad.royalclash.model.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Plays the ENEMY side. It can READ the match, but the only way it can change anything
 * is match.playCard(), exactly like you. So it can't cheat.
 */
public class AIController {
    private final Match match;
    private final Player me;
    private final Random random = new Random();
    private float thinkTimer = 2f;

    public AIController(Match match) {
        this.match = match;
        this.me = match.getEnemy();
    }

    public void update(float delta) {
        if (match.isGameOver()) return;
        thinkTimer -= delta;
        if (thinkTimer > 0f) return;
        thinkTimer = 0.8f + random.nextFloat() * 1.2f; // "reaction time"

        Unit threat = findThreat();
        if (threat != null && !alreadyDefending(threat)) {
            defend(threat);
        } else if (me.getElixir() >= 9f || (me.getElixir() >= 7f && random.nextFloat() < 0.3f)) {
            attack();
        }
    }

    /** The enemy troop that has pushed furthest toward our towers. */
    private Unit findThreat() {
        Unit worst = null;
        for (Unit u : match.getUnits()) {
            if (u.getTeam() == me.getTeam() || u.isBuilding() || !u.isAlive()) continue;
            if (u.getY() < Arena.RIVER_Y - 60f) continue; // still far away
            if (worst == null || u.getY() > worst.getY()) worst = u;
        }
        return worst;
    }

    private boolean alreadyDefending(Unit threat) {
        for (Unit u : match.getUnits()) {
            if (u.getTeam() == me.getTeam() && !u.isBuilding() && u.edgeDistanceTo(threat) < 150f) return true;
        }
        return false;
    }

    private void defend(Unit threat) {
        int card = pickCard(true);
        if (card < 0) return;
        float x = clamp(threat.getX(), 20f, Arena.WORLD_WIDTH - 20f);
        float y = clamp(threat.getY() + 60f, Arena.RIVER_Y + Arena.RIVER_HALF_HEIGHT + 10f, Arena.ARENA_TOP - 20f);
        match.playCard(me, card, x, y);
    }

    private void attack() {
        int card = pickCard(false);
        if (card < 0) return;
        float laneX = random.nextBoolean() ? Arena.LEFT_LANE_X : Arena.RIGHT_LANE_X;
        float x = laneX + (random.nextFloat() * 40f - 20f);
        float y = 520f + random.nextFloat() * 100f;
        match.playCard(me, card, x, y);
    }

    /** Random affordable card. Defense skips building only cards (Giant and Hog can't defend). */
    private int pickCard(boolean forDefense) {
        List<Integer> options = new ArrayList<>();
        for (int i = 0; i < Deck.HAND_SIZE; i++) {
            Card c = me.getDeck().getCard(i);
            if (!me.canAfford(c)) continue;
            if (forDefense && c.targetsBuildingsOnly()) continue;
            options.add(i);
        }
        if (options.isEmpty()) return -1;
        return options.get(random.nextInt(options.size()));
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
