package com.stormidle.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class IntroScreen implements Screen {

    // References
    private final com.stormidle.Storm game;

    // Sprites and images used in rendering
    private SpriteBatch batch;
    private Texture libgdxLogo;
    private Texture hexLogo;

    // Animation state
    private float elapsed; // Total time since screen shown
    private float alpha; // Transparency value for libgdx logo (used for fading animation)
    private float hexAlpha; // Transparency value for hex gaming logo

    // Values for animation timers
    private final float FADE_IN_TIME  = 2.5f;
    private final float FADE_OUT_TIME = 2.5f;

    public IntroScreen(com.stormidle.Storm game) {
        this.game = game;
    }

    @Override
    // Called once this screen becomes active
    public void show() {
        batch = new SpriteBatch();

        // TODO: swap to AssetManager later
        libgdxLogo = new Texture("libgdx_logo.png");
        hexLogo = new Texture("hex_gaming_logo.png");

        elapsed = 0f;
        alpha = 0f;
        hexAlpha = 0f;
    }

    @Override
    public void render(float delta) {

        elapsed += delta;
        float introEnd = (2f * FADE_IN_TIME) + (2f * FADE_OUT_TIME) + 1f;

        // Update alpha based on elapsed time
        alpha = computeAlpha(elapsed);
        // Hex logo animation will start 0.5 seconds after libgdx logo fades out
        float hexLogoStart = FADE_IN_TIME + FADE_OUT_TIME + 0.5f;
        hexAlpha = computeAlpha(elapsed - hexLogoStart);

        // Clear screen to black
        Gdx.gl.glClearColor(0f,0f,0f,1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw logo with alpha (constantly changes to mimic a fade in /fade out)
        // And center the logo on the screen
        batch.begin();
        if (isFinished(elapsed)) {
            alpha = 0f;
        }
        batch.setColor(1f,1f,1f, alpha);
        batch.draw(libgdxLogo,
            (Gdx.graphics.getWidth() - libgdxLogo.getWidth()) / 2f,
            (Gdx.graphics.getHeight() - libgdxLogo.getHeight()) / 2f
        );
        batch.setColor(1f, 1f, 1f, hexAlpha);
        batch.draw(hexLogo,
            (Gdx.graphics.getWidth() - hexLogo.getWidth()) / 2f,
            (Gdx.graphics.getHeight() - hexLogo.getHeight()) / 2f
        );
        batch.setColor(1f,1f,1f,1f);
        batch.end();

        // TODO: Switch screens when animation is done
        if (elapsed > introEnd) {
            game.setScreen(new GameScreen(game));
        }


    }

    private float computeAlpha(float t) {

        // Timeline:
        // [0 -> FADE_IN_TIME]                                  logo is fading in
        // [FADE_IN_TIME -> holdEnd(FADE_IN + HOLD_TIME)]       pause before fading out
        // [FADE_IN_TIME + HOLD_TIME -> end]                    fade out

        float fadeOutEnd = FADE_IN_TIME + FADE_OUT_TIME;

        if (t < 0f) return 0f;

        if (t <= FADE_IN_TIME) {
            alpha = t / FADE_IN_TIME;
            return alpha;
        }
        else if (t <= fadeOutEnd) {
            alpha = 1 - ((t - FADE_IN_TIME) / FADE_OUT_TIME);
            return alpha;
        }
        else {
            return 0f;
        }
    }

    // Checker for first logo animation
    private boolean isFinished(float t) {
        float end = FADE_IN_TIME + FADE_OUT_TIME;
        return t >= end;
    }

    @Override public void resize(int width, int height) { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        // Cleanup
        batch.dispose();
        hexLogo.dispose();
    }
}
