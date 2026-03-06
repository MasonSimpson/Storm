package com.stormidle.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.Texture;

/**
 * Plays the prestige transition:
 *   1. Fade to black over FADE_IN seconds
 *   2. Hold and display "Cloud Level X" for HOLD seconds
 *   3. Run the onMidpoint callback (resets game state, swaps cloud texture)
 *   4. Fade back in over FADE_OUT seconds
 *   5. Run the onComplete callback (re-enables input etc.)
 *
 * The overlay is added directly to the stage and removes itself when done.
 */
public class PrestigeTransition {

    private static final float FADE_IN   = 0.8f;
    private static final float HOLD      = 1.6f;
    private static final float FADE_OUT  = 0.8f;

    public interface TransitionCallback { void run(); }

    public static void play(Stage stage, int newLevel,
                            Texture overlayTex,
                            TransitionCallback onMidpoint,
                            TransitionCallback onComplete) {

        float w = stage.getWidth();
        float h = stage.getHeight();

        // Full-screen black overlay
        Image overlay = new Image(overlayTex);
        overlay.setSize(w, h);
        overlay.setColor(0, 0, 0, 0);
        stage.addActor(overlay);

        // "Cloud Level X" label — starts invisible
        BitmapFont font = new BitmapFont();
        font.getData().setScale(2.5f);
        Label levelLabel = new Label("Cloud Level " + newLevel,
            new Label.LabelStyle(font, Color.WHITE));
        levelLabel.setPosition(
            w / 2f - levelLabel.getPrefWidth()  / 2f,
            h / 2f - levelLabel.getPrefHeight() / 2f);
        levelLabel.setColor(1, 1, 1, 0);
        stage.addActor(levelLabel);

        // Sequence:
        //   overlay fades in → midpoint callback runs → label appears →
        //   hold → label fades out → overlay fades out → done
        overlay.addAction(Actions.sequence(
            Actions.alpha(1f, FADE_IN),
            Actions.run(onMidpoint::run),
            Actions.run(() -> levelLabel.addAction(Actions.alpha(1f, 0.3f))),
            Actions.delay(HOLD),
            Actions.run(() -> levelLabel.addAction(Actions.alpha(0f, 0.3f))),
            Actions.alpha(0f, FADE_OUT),
            Actions.run(() -> {
                overlay.remove();
                levelLabel.remove();
                onComplete.run();
            })
        ));
    }
}
