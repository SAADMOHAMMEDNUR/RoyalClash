package io.github.saad.royalclash.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One game from start to finish. Owns every unit, both players, and the clock.
 * All game RULES live here. It knows nothing about drawing, the mouse, or the AI.
 */
public class Match {
    public static final float REGULATION_TIME = 180f;
    public static final float OVERTIME_TIME = 60f;
    public static final float DOUBLE_ELIXIR_AT = 60f;    // seconds left
    public static final float ELIXIR_REGEN_TIME = 2.8f;  // seconds per elixir at 1x
    private static final float SIGHT_RANGE = 140f;       // how far troops notice enemies

    private final List<Unit> units = new ArrayList<>();
    private final List<Unit> unitsReadOnly = Collections.unmodifiableList(units);
    private final Player player;
    private final Player enemy;

    private float timeLeft = REGULATION_TIME;
    private boolean overtime;
    private boolean gameOver;
    private Team winner; // null + gameOver means draw

    public Match(List<Card> playerDeck, List<Card> enemyDeck) {
        player = new Player(Team.PLAYER, new Deck(playerDeck));
        enemy = new Player(Team.ENEMY, new Deck(enemyDeck));
        spawnTowers(Team.PLAYER, 260f, 195f);
        spawnTowers(Team.ENEMY, 690f, 755f);
    }

    private void spawnTowers(Team team, float princessY, float kingY) {
        units.add(new Unit(Cards.PRINCESS_TOWER, team, Arena.LEFT_LANE_X, princessY));
        units.add(new Unit(Cards.PRINCESS_TOWER, team, Arena.RIGHT_LANE_X, princessY));
        Unit king = new Unit(Cards.KING_TOWER, team, Arena.WORLD_WIDTH / 2f, kingY);
        king.setActive(false);
        units.add(king);
    }

    // ===================== the ONE action anyone can take =====================

    /** PlayerController and AIController both call this. Returns false if the play isn't allowed. */
    public boolean playCard(Player p, int handIndex, float x, float y) {
        if (gameOver) return false;
        Card card = p.getDeck().getCard(handIndex);
        if (!p.canAfford(card)) return false;
        if (!Arena.isDeployable(p.getTeam(), x, y)) return false;

        p.spendElixir(card.getElixirCost());
        int count = card.getCount();
        for (int i = 0; i < count; i++) {
            // groups spawn in a little circle
            double angle = i * 2 * Math.PI / count;
            float offset = count > 1 ? 14f : 0f;
            units.add(new Unit(card, p.getTeam(),
                x + (float) Math.cos(angle) * offset,
                y + (float) Math.sin(angle) * offset));
        }
        p.getDeck().cycle(handIndex);
        return true;
    }

    // ===================== update loop =====================

    public void update(float delta) {
        if (gameOver) return;
        updateClock(delta);
        if (gameOver) return;

        float rate = isDoubleElixir() ? 2f : 1f;
        player.addElixir(delta * rate / ELIXIR_REGEN_TIME);
        enemy.addElixir(delta * rate / ELIXIR_REGEN_TIME);

        for (Unit u : units) updateUnit(u, delta);
        units.removeIf(u -> !u.isAlive());
    }

    private void updateClock(float delta) {
        timeLeft -= delta;
        if (timeLeft > 0f) return;
        timeLeft = 0f;
        if (!overtime && player.getCrowns() == enemy.getCrowns()) {
            overtime = true;            // tied after 3 min: sudden death
            timeLeft = OVERTIME_TIME;
            return;
        }
        finish(leaderByCrowns());
    }

    private void updateUnit(Unit u, float delta) {
        if (!u.isAlive()) return;
        u.update(delta);
        if (u.isDeploying() || !u.isActive()) return;

        // 1) targeting: keep hitting the current target if possible, otherwise look again
        Unit t = u.getTarget();
        if (t == null || !t.isAlive() || !u.isInRange(t)) {
            t = findTarget(u);
            u.setTarget(t);
        }
        if (t == null) return;

        // 2) attack if in range, otherwise walk
        if (u.isInRange(t)) {
            if (u.canAttackNow()) {
                u.attack(t);
                if (!t.isAlive() && t.isBuilding()) onTowerDestroyed(t);
            }
        } else if (u.getCard().getSpeed() > 0f) {
            walkToward(u, t, delta);
        }
    }

    private Unit findTarget(Unit u) {
        Unit best = null;
        float bestDist = Float.MAX_VALUE;

        float sight = u.isBuilding() ? u.getCard().getRange() : Math.max(SIGHT_RANGE, u.getCard().getRange());
        for (Unit o : units) {
            if (!u.canTarget(o)) continue;
            float d = u.edgeDistanceTo(o);
            if (d <= sight && d < bestDist) {
                best = o;
                bestDist = d;
            }
        }
        if (best != null || u.isBuilding()) return best;

        // nothing close: march to the nearest enemy building
        for (Unit o : units) {
            if (o.getTeam() == u.getTeam() || !o.isAlive() || !o.isBuilding()) continue;
            float d = u.edgeDistanceTo(o);
            if (d < bestDist) {
                best = o;
                bestDist = d;
            }
        }
        return best;
    }

    /** Walk at the target, but use a bridge if the river is in the way. */
    private void walkToward(Unit u, Unit t, float delta) {
        float goalX = t.getX();
        float goalY = t.getY();

        boolean targetAbove = t.getY() > Arena.RIVER_Y;
        float nearEdge = targetAbove ? Arena.RIVER_Y - Arena.RIVER_HALF_HEIGHT : Arena.RIVER_Y + Arena.RIVER_HALF_HEIGHT;
        float farEdge = targetAbove ? Arena.RIVER_Y + Arena.RIVER_HALF_HEIGHT : Arena.RIVER_Y - Arena.RIVER_HALF_HEIGHT;
        boolean needsToCross = targetAbove ? u.getY() < farEdge : u.getY() > farEdge;

        if (needsToCross) {
            float bridgeX = Arena.laneXNear(u.getX());
            boolean linedUp = Math.abs(u.getX() - bridgeX) < 4f;
            boolean onRiver = Math.abs(u.getY() - Arena.RIVER_Y) <= Arena.RIVER_HALF_HEIGHT;
            goalX = bridgeX;
            // first go to the start of the bridge, then straight across
            goalY = (linedUp || onRiver) ? farEdge + (targetAbove ? 2f : -2f) : nearEdge;
        }
        u.moveToward(goalX, goalY, delta);
    }

    /** Basically an event handler: a tower fell, several things react. */
    private void onTowerDestroyed(Unit tower) {
        Player scorer = getPlayer(tower.getTeam().opponent());

        if (tower.getCard() == Cards.KING_TOWER) {
            scorer.setCrowns(3);
            finish(scorer.getTeam());
            return;
        }
        scorer.addCrown();
        for (Unit o : units) { // losing a princess tower wakes that side's king
            if (o.getTeam() == tower.getTeam() && o.getCard() == Cards.KING_TOWER) o.setActive(true);
        }
        if (overtime) finish(scorer.getTeam()); // sudden death
    }

    private Team leaderByCrowns() {
        if (player.getCrowns() > enemy.getCrowns()) return Team.PLAYER;
        if (enemy.getCrowns() > player.getCrowns()) return Team.ENEMY;
        return null;
    }

    private void finish(Team winner) {
        this.gameOver = true;
        this.winner = winner;
    }

    // ===================== read only getters for view + controllers =====================

    public List<Unit> getUnits() { return unitsReadOnly; }
    public Player getPlayer() { return player; }
    public Player getEnemy() { return enemy; }
    public Player getPlayer(Team team) { return team == Team.PLAYER ? player : enemy; }
    public float getTimeLeft() { return timeLeft; }
    public boolean isOvertime() { return overtime; }
    public boolean isDoubleElixir() { return overtime || timeLeft <= DOUBLE_ELIXIR_AT; }
    public boolean isGameOver() { return gameOver; }
    public Team getWinner() { return winner; }
}
