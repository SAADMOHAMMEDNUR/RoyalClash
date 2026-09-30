package io.github.saad.royalclash.model;

/**
 * Something on the board (troop or tower). HAS a Card (composition), holds its own live state.
 * Methods without "public" are package private: only classes in model can call them,
 * so controllers and the view physically can't change a unit. The compiler enforces MVC for you.
 */
public class Unit {
    public static final float DEPLOY_TIME = 1f;

    private final Card card;
    private final Team team;
    private float x;
    private float y;
    private int hp;
    private float attackCooldown;
    private float deployTimer;
    private boolean active = true;   // the King Tower starts asleep
    private Unit target;
    private float attackFlash;       // only for drawing the hit line

    Unit(Card card, Team team, float x, float y) {
        this.card = card;
        this.team = team;
        this.x = x;
        this.y = y;
        this.hp = card.getMaxHP();
        this.deployTimer = card.isBuilding() ? 0f : DEPLOY_TIME;
    }

    /** Ticks personal timers. Decisions (who to hit, where to walk) are made by Match. */
    void update(float delta) {
        attackFlash = Math.max(0f, attackFlash - delta);
        attackCooldown = Math.max(0f, attackCooldown - delta);
        if (deployTimer > 0f) deployTimer -= delta;
    }

    void moveToward(float goalX, float goalY, float delta) {
        float dx = goalX - x;
        float dy = goalY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.001f) return;
        float step = Math.min(dist, card.getSpeed() * delta);
        x += dx / dist * step;
        y += dy / dist * step;
    }

    void attack(Unit other) {
        other.takeDamage(card.getDamage());
        attackCooldown = card.getAttackSpeed();
        attackFlash = 0.12f;
    }

    void takeDamage(int amount) {
        hp = Math.max(0, hp - amount);
        if (card == Cards.KING_TOWER) active = true; // hitting the king wakes him up
    }

    void setTarget(Unit target) { this.target = target; }
    void setActive(boolean active) { this.active = active; }

    /** Edge to edge distance (squares treated like circles to keep the math easy). */
    public float edgeDistanceTo(Unit other) {
        float dx = other.x - x;
        float dy = other.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy) - (card.getSize() + other.card.getSize()) / 2f;
    }

    public boolean isInRange(Unit other) {
        return edgeDistanceTo(other) <= card.getRange();
    }

    /** Towers shoot troops, Giant and Hog only hit buildings, everyone else hits anything. */
    public boolean canTarget(Unit other) {
        if (other.team == team || !other.isAlive()) return false;
        if (isBuilding()) return !other.isBuilding();
        if (card.targetsBuildingsOnly()) return other.isBuilding();
        return true;
    }

    public Card getCard() { return card; }
    public Team getTeam() { return team; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHp() { return hp; }
    public boolean isAlive() { return hp > 0; }
    public boolean isBuilding() { return card.isBuilding(); }
    public boolean isActive() { return active; }
    public boolean isDeploying() { return deployTimer > 0f; }
    public boolean canAttackNow() { return attackCooldown <= 0f; }
    public Unit getTarget() { return target; }
    public float getAttackFlash() { return attackFlash; }
}
