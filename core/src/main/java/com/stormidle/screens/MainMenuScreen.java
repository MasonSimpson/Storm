package com.stormidle.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class MainMenuScreen implements Screen {

    // Reference to game
    private final com.stormidle.Storm game;

    // Stage object where all input objects will be held
    private Stage stage;

    // Sprites and images used in rendering
    private Skin skin;
    private SpriteBatch batch;
    private Texture gameLogo;
    private Image image;
    /*
    TODO: Change start game button to "new game" and "load game"
          Won't be done until more gameplay components are added
     */
    private TextButton startGameButton;

    public MainMenuScreen(com.stormidle.Storm game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        gameLogo = new Texture("storm_logo.png");
        image = new Image(gameLogo);
        image.setPosition(
            (Gdx.graphics.getWidth() - image.getWidth()) / 2f,
            Gdx.graphics.getHeight() - image.getHeight() - 40
        );

        stage.addActor(image);


    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

    }

    @Override public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {

    }
}
