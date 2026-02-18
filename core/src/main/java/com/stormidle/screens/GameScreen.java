package com.stormidle.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.stormidle.objects.*;

public class GameScreen implements Screen {

    // Reference to game
    private final com.stormidle.Storm game;

    // Stage object where all input objects will be held
    private Stage stage = new Stage(new ScreenViewport());

    // Sprites and images used in rendering
    private SpriteBatch batch;
    private Texture cloudTexture;
    private Texture rainTexture;
    private Image cloud;
    private Array<Rain> rain;


    // TODO: Change to buttons instead of images
    private Texture buttonTexture;
    private Image button;

    // Variables used to position sprites
    private final float stageWidth = stage.getWidth();
    private final float stageHeight = stage.getHeight();
    private float rainFallSpeed = 300f;

    public GameScreen(com.stormidle.Storm game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        rain = new Array<>();

        Gdx.input.setInputProcessor(stage);

        cloudTexture = new Texture("cloud_1.png");
        cloud = new Image(cloudTexture);
        cloud.setPosition(stageWidth * 0.10f, stageHeight * 0.65f);

        cloud.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               float stageX = cloud.getX();
               float stageY = cloud.getY();

               rain.add(new Rain(stageX, stageY, rainFallSpeed));
           }
        });

        stage.addActor(cloud);

        rainTexture = new Texture("rain.png");
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        updateRainfall(delta);

        batch.begin();
        for (Rain drop : rain) {
            batch.draw(rainTexture, drop.x, drop.y);
        }
        batch.end();

    }

    private void updateRainfall(float delta) {
        for (int i = rain.size - 1; i >= 0; i--) {
            Rain drop = rain.get(i);
            drop.y -= drop.speed * delta;

            if (drop.y < 0) {
                rain.removeIndex(i);
            }
        }
    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        cloudTexture.dispose();
        rainTexture.dispose();
    }
}
