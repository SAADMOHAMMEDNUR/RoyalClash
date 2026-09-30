package io.github.saad.royalclash;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.saad.royalclash.controller.AIController;
import io.github.saad.royalclash.controller.PlayerController;
import io.github.saad.royalclash.model.Arena;
import io.github.saad.royalclash.model.Cards;
import io.github.saad.royalclash.model.Match;
import io.github.saad.royalclash.view.GameRenderer;

/** Just wires model, view, and controllers together and runs the frame loop. */
public class Main extends ApplicationAdapter {
    private OrthographicCamera camera;
    private FitViewport viewport;
    private GameRenderer renderer;

    private Match match;
    private PlayerController playerController;
    private AIController aiController;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(Arena.WORLD_WIDTH, Arena.WORLD_HEIGHT, camera); // same arena on any window size
        renderer = new GameRenderer(camera);
        startNewMatch();
    }

    private void startNewMatch() {
        match = new Match(Cards.STARTER_DECK, Cards.STARTER_DECK);
        playerController = new PlayerController(match, viewport);
        aiController = new AIController(match);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        // cap delta so a lag spike doesn't teleport units
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f / 30f);

        if (match.isGameOver()) {
            if (Gdx.input.justTouched()) startNewMatch();
        } else {
            playerController.update();   // 1. input
            aiController.update(delta);  // 2. AI
            match.update(delta);         // 3. update
        }

        viewport.apply();
        renderer.render(match, playerController.getSelectedCard()); // 4. draw
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
