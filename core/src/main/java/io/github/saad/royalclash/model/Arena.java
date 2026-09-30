package io.github.saad.royalclash.model;

/** Every fixed number about the map. World is 450 x 800, about 25 units per tile. */
public final class Arena {
    private Arena() {}

    public static final float WORLD_WIDTH = 450f;
    public static final float WORLD_HEIGHT = 800f;

    public static final float HUD_HEIGHT = 150f;          // bottom strip for cards + elixir
    public static final float ARENA_BOTTOM = HUD_HEIGHT;
    public static final float ARENA_TOP = WORLD_HEIGHT;

    public static final float RIVER_Y = 475f;
    public static final float RIVER_HALF_HEIGHT = 15f;

    public static final float LEFT_LANE_X = 100f;
    public static final float RIGHT_LANE_X = 350f;
    public static final float BRIDGE_WIDTH = 50f;

    public static float laneXNear(float x) {
        if (Math.abs(x - LEFT_LANE_X) < Math.abs(x - RIGHT_LANE_X)) {
            return LEFT_LANE_X;
        } else {
            return RIGHT_LANE_X;
        }
    }

    /** Each team can only drop troops on its own half. */
    public static boolean isDeployable(Team team, float x, float y) {
        if (x < 10f || x > WORLD_WIDTH - 10f) return false;
        if (team == Team.PLAYER) {
            return y > ARENA_BOTTOM + 10f && y < RIVER_Y - RIVER_HALF_HEIGHT - 5f;
        }
        return y > RIVER_Y + RIVER_HALF_HEIGHT + 5f && y < ARENA_TOP - 10f;
    }
}
