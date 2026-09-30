package io.github.saad.royalclash.model;

/** PLAYER is the bottom half of the arena, ENEMY is the top half. */
public enum Team {
    PLAYER, ENEMY;

    public Team opponent() {
        return this == PLAYER ? ENEMY : PLAYER;
    }
}
