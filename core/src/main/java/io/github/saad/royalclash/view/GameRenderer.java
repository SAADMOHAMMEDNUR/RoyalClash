package io.github.saad.royalclash.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.saad.royalclash.model.Arena;
import io.github.saad.royalclash.model.Card;
import io.github.saad.royalclash.model.Deck;
import io.github.saad.royalclash.model.Match;
import io.github.saad.royalclash.model.Player;
import io.github.saad.royalclash.model.Team;
import io.github.saad.royalclash.model.Unit;

/** The VIEW. Only reads the match and draws it. Never changes anything. */
public class GameRenderer {
    private static final Color PLAYER_COLOR = new Color(0.25f, 0.5f, 1f, 1f);
    private static final Color ENEMY_COLOR = new Color(0.95f, 0.3f, 0.3f, 1f);
    private static final Color ELIXIR_COLOR = new Color(0.85f, 0.3f, 0.95f, 1f);

    private final Camera camera;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();

    public GameRenderer(Camera camera) {
        this.camera = camera;
        font.setUseIntegerPositions(false);
    }

    public void render(Match match, int selectedCard) {
        ScreenUtils.clear(0.08f, 0.08f, 0.1f, 1f);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // pass 1: shapes
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawArena(selectedCard >= 0);
        drawUnits(match);
        drawHud(match, selectedCard);
        if (match.isGameOver()) {
            shapes.setColor(0f, 0f, 0f, 0.65f);
            shapes.rect(0, 0, Arena.WORLD_WIDTH, Arena.WORLD_HEIGHT);
        }
        shapes.end();

        // pass 2: text
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawText(match);
        batch.end();
    }

    private void drawArena(boolean showDeployZone) {
        shapes.setColor(0.33f, 0.58f, 0.3f, 1f); // grass
        shapes.rect(0, Arena.ARENA_BOTTOM, Arena.WORLD_WIDTH, Arena.ARENA_TOP - Arena.ARENA_BOTTOM);

        shapes.setColor(0.25f, 0.5f, 0.85f, 1f); // river
        shapes.rect(0, Arena.RIVER_Y - Arena.RIVER_HALF_HEIGHT, Arena.WORLD_WIDTH, Arena.RIVER_HALF_HEIGHT * 2f);

        shapes.setColor(0.55f, 0.4f, 0.25f, 1f); // bridges
        float bridgeY = Arena.RIVER_Y - Arena.RIVER_HALF_HEIGHT - 4f;
        float bridgeH = Arena.RIVER_HALF_HEIGHT * 2f + 8f;
        shapes.rect(Arena.LEFT_LANE_X - Arena.BRIDGE_WIDTH / 2f, bridgeY, Arena.BRIDGE_WIDTH, bridgeH);
        shapes.rect(Arena.RIGHT_LANE_X - Arena.BRIDGE_WIDTH / 2f, bridgeY, Arena.BRIDGE_WIDTH, bridgeH);

        if (showDeployZone) { // highlight where you're allowed to drop troops
            shapes.setColor(1f, 1f, 1f, 0.12f);
            float top = Arena.RIVER_Y - Arena.RIVER_HALF_HEIGHT - 5f;
            shapes.rect(0, Arena.ARENA_BOTTOM, Arena.WORLD_WIDTH, top - Arena.ARENA_BOTTOM);
        }
    }

    private void drawUnits(Match match) {
        for (Unit u : match.getUnits()) {
            Color base = u.getTeam() == Team.PLAYER ? PLAYER_COLOR : ENEMY_COLOR;
            float shade = u.isBuilding() ? 0.7f : 1f;
            if (!u.isActive()) shade = 0.45f;              // sleeping king
            float alpha = u.isDeploying() ? 0.45f : 1f;    // still deploying
            shapes.setColor(base.r * shade, base.g * shade, base.b * shade, alpha);
            float s = u.getCard().getSize();
            shapes.rect(u.getX() - s / 2f, u.getY() - s / 2f, s, s);
        }

        shapes.setColor(1f, 0.95f, 0.5f, 1f); // hit lines
        for (Unit u : match.getUnits()) {
            if (u.getAttackFlash() > 0f && u.getTarget() != null) {
                shapes.rectLine(u.getX(), u.getY(), u.getTarget().getX(), u.getTarget().getY(), 2f);
            }
        }

        for (Unit u : match.getUnits()) { // troop health bars (towers show a number instead)
            if (u.isBuilding() || u.getHp() >= u.getCard().getMaxHP()) continue;
            float w = u.getCard().getSize();
            float bx = u.getX() - w / 2f;
            float by = u.getY() + u.getCard().getSize() / 2f + 3f;
            shapes.setColor(0f, 0f, 0f, 0.7f);
            shapes.rect(bx, by, w, 4f);
            shapes.setColor(u.getTeam() == Team.PLAYER ? PLAYER_COLOR : ENEMY_COLOR);
            shapes.rect(bx, by, w * u.getHp() / (float) u.getCard().getMaxHP(), 4f);
        }
    }

    private void drawHud(Match match, int selectedCard) {
        Player p = match.getPlayer();
        shapes.setColor(0.12f, 0.12f, 0.16f, 1f);
        shapes.rect(0, 0, Arena.WORLD_WIDTH, Arena.HUD_HEIGHT);

        Rectangle bar = Hud.ELIXIR_BAR;
        shapes.setColor(0.25f, 0.15f, 0.3f, 1f);
        shapes.rect(bar.x, bar.y, bar.width, bar.height);
        shapes.setColor(ELIXIR_COLOR);
        shapes.rect(bar.x, bar.y, bar.width * p.getElixir() / Player.MAX_ELIXIR, bar.height);
        shapes.setColor(0.12f, 0.12f, 0.16f, 1f);
        for (int i = 1; i < 10; i++) shapes.rect(bar.x + bar.width * i / 10f - 1f, bar.y, 2f, bar.height);

        for (int i = 0; i < Deck.HAND_SIZE; i++) {
            Rectangle r = Hud.cardBounds(i);
            if (i == selectedCard) {
                shapes.setColor(1f, 0.85f, 0.2f, 1f);
                shapes.rect(r.x - 3f, r.y - 3f, r.width + 6f, r.height + 6f);
            }
            if (p.canAfford(p.getDeck().getCard(i))) shapes.setColor(0.3f, 0.32f, 0.45f, 1f);
            else shapes.setColor(0.2f, 0.2f, 0.24f, 1f);
            shapes.rect(r.x, r.y, r.width, r.height);
        }

        Rectangle next = Hud.NEXT_CARD;
        shapes.setColor(0.2f, 0.2f, 0.24f, 1f);
        shapes.rect(next.x, next.y, next.width, next.height);
    }

    private void drawText(Match match) {
        float cx = Arena.WORLD_WIDTH / 2f;
        Player p = match.getPlayer();
        Player e = match.getEnemy();

        if (match.isGameOver()) {
            String result = match.getWinner() == null ? "DRAW"
                : match.getWinner() == Team.PLAYER ? "YOU WIN!" : "YOU LOSE";
            font.getData().setScale(2.5f);
            drawCentered(result, cx, Arena.WORLD_HEIGHT / 2f + 40f, Color.WHITE);
            font.getData().setScale(1.2f);
            drawCentered("Crowns: You " + p.getCrowns() + " vs Enemy " + e.getCrowns(), cx, Arena.WORLD_HEIGHT / 2f - 10f, Color.WHITE);
            drawCentered("Click to play again", cx, Arena.WORLD_HEIGHT / 2f - 50f, Color.LIGHT_GRAY);
            font.getData().setScale(1f);
            return;
        }

        // clock
        int secs = (int) Math.ceil(match.getTimeLeft());
        font.getData().setScale(1.4f);
        drawCentered(String.format("%d:%02d", secs / 60, secs % 60), Arena.WORLD_WIDTH - 45f, Arena.WORLD_HEIGHT - 20f, Color.WHITE);
        font.getData().setScale(1f);
        if (match.isOvertime()) drawCentered("OVERTIME", Arena.WORLD_WIDTH - 45f, Arena.WORLD_HEIGHT - 45f, Color.ORANGE);
        else if (match.isDoubleElixir()) drawCentered("2x ELIXIR", Arena.WORLD_WIDTH - 45f, Arena.WORLD_HEIGHT - 45f, ELIXIR_COLOR);

        // crowns
        drawCentered("Enemy crowns: " + e.getCrowns(), 75f, Arena.WORLD_HEIGHT - 20f, ENEMY_COLOR);
        drawCentered("Your crowns: " + p.getCrowns(), 75f, Arena.ARENA_BOTTOM + 20f, PLAYER_COLOR);

        // labels on units, hp on towers
        for (Unit u : match.getUnits()) {
            String text = u.isBuilding() ? String.valueOf(u.getHp()) : u.getCard().getLabel();
            drawCentered(text, u.getX(), u.getY(), Color.WHITE);
        }

        // cards
        for (int i = 0; i < Deck.HAND_SIZE; i++) {
            Rectangle r = Hud.cardBounds(i);
            Card c = p.getDeck().getCard(i);
            float mid = r.x + r.width / 2f;
            drawCentered(c.getName(), mid, r.y + r.height - 16f, Color.WHITE);
            drawCentered(c.getLabel(), mid, r.y + r.height / 2f, Color.LIGHT_GRAY);
            drawCentered(String.valueOf(c.getElixirCost()), mid, r.y + 14f, ELIXIR_COLOR);
        }

        Rectangle n = Hud.NEXT_CARD;
        drawCentered("Next", n.x + n.width / 2f, n.y + n.height + 12f, Color.LIGHT_GRAY);
        Card next = p.getDeck().peekNext();
        if (next != null) {
            drawCentered(next.getLabel() + " (" + next.getElixirCost() + ")", n.x + n.width / 2f, n.y + n.height / 2f, Color.WHITE);
        }

        drawCentered(String.valueOf((int) p.getElixir()), 45f, Hud.ELIXIR_BAR.y + 10f, ELIXIR_COLOR);
    }

    private void drawCentered(String text, float cx, float cy, Color color) {
        font.setColor(color);
        layout.setText(font, text);
        font.draw(batch, layout, cx - layout.width / 2f, cy + layout.height / 2f);
    }

    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
