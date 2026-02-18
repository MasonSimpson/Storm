package com.stormidle.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.stormidle.objects.Rain;
import com.stormidle.objects.GameData;

public class GameScreen implements Screen {

    // Tweakable values for game UI
    private static final int DROPS_TO_FILL =  10; // How many drops needed to convert the rainfall into currency
    private static final float FALL_SPEED =   300f;
    private static final float BOWL_WIDTH =   400f;
    private static final float BOWL_HEIGHT =  200f;
    private static final float BAR_WIDTH =    80f; // Progress bar for bowl
    private static final float BAR_HEIGHT =   12f;
    private static final float BAR_Y_OFFSET = 6f; // Gap between bottom of bowl and top of bar

    // Core of game
    private final com.stormidle.Storm game;
    private Stage stage = new Stage(new ScreenViewport());
    private SpriteBatch batch;
    private GameData gameData = new GameData();

    // Sprite textures
    private Texture cloudTexture;
    private Texture rainTexture;
    private Texture bowlTexture;

    // Actors, or sprites, for stage
    private Image cloud;
    private Image bowl;
    private ProgressBar fillBar;
    private Label currencyLabel;

    // Rain and bowl states
    private Array<Rain> rain;
    private int dropsCollected = 0; // Resets to 0 each time the bowl converts
    private float bowlX;
    private float bowlY;

    // Variables used to position sprites
    private final float stageWidth = stage.getWidth();
    private final float stageHeight = stage.getHeight();

    public GameScreen(com.stormidle.Storm game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Initialize batch and rain array
        batch = new SpriteBatch();
        rain = new Array<>();

        Gdx.input.setInputProcessor(stage);

        // Initialize cloud
        cloudTexture = new Texture("cloud_1.png");
        cloud = new Image(cloudTexture);
        cloud.setSize(cloud.getPrefWidth(), cloud.getPrefHeight());
        cloud.setPosition(stageWidth * 0.10f, stageHeight * 0.65f);

        cloud.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               rain.add(new Rain(cloud.getX(), cloud.getY(), FALL_SPEED));
           }
        });

        stage.addActor(cloud);

        // Bowl position
        bowlTexture = new Texture("bowl.png");
        bowlX = cloud.getX() + (cloud.getWidth() / 2f) - (BOWL_WIDTH / 2f);
        bowlY = 20f;

        // Progress bar for bowl, sits underneath the bowl actor
        fillBar = new ProgressBar(0f, DROPS_TO_FILL, 1f, false, createFillBarStyle());
        fillBar.setValue(0f);
        fillBar.setSize(BAR_WIDTH, BAR_HEIGHT);
        fillBar.setPosition(
            bowlX + (BOWL_WIDTH / 2f) - (BAR_WIDTH / 2f),
            bowlY - BAR_HEIGHT - BAR_Y_OFFSET
        );

        stage.addActor(fillBar);

        // Currency label, top center of screen
        BitmapFont font = new BitmapFont();
        font.getData().setScale(2f);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        currencyLabel = new Label(getCurrencyText(), labelStyle);
        currencyLabel.setWidth(stageWidth);
        currencyLabel.setAlignment(Align.center);
        currencyLabel.setPosition(0, stageHeight - currencyLabel.getPrefHeight() - 20);

        stage.addActor(currencyLabel);

        // Rain texture
        rainTexture = new Texture("rain.png");
    }

    // Helper function for creating the progress bar, uses Pixmaps
    private ProgressBar.ProgressBarStyle createFillBarStyle() {
        // Background - dark blue/grey
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0.2f, 0.2f, 0.3f, 1f);
        bgPixmap.fill();
        Drawable background = new TextureRegionDrawable(new TextureRegion(new Texture(bgPixmap)));
        bgPixmap.dispose();

        // Knob — zero width so it's invisible (we just want the fill, not a slider knob)
        Pixmap knobPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(0f, 0f, 0f, 0f);
        knobPixmap.fill();
        Drawable knob = new TextureRegionDrawable(new TextureRegion(new Texture(knobPixmap)));
        knobPixmap.dispose();

        // Knob before (filled portion) — light blue
        Pixmap fillPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        fillPixmap.setColor(0.3f, 0.7f, 1f, 1f);
        fillPixmap.fill();
        Drawable knobBefore = new TextureRegionDrawable(new TextureRegion(new Texture(fillPixmap)));
        fillPixmap.dispose();

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle(background, knob);
        style.knobBefore = knobBefore;
        return style;
    }

    private String getCurrencyText() {
        return gameData.currency + " currency";
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

        batch.draw(bowlTexture, bowlX, bowlY, BOWL_WIDTH, BOWL_HEIGHT);

        batch.end();
    }

    // Updates the rainfall array and keeps track of collected rainfall
    private void updateRainfall(float delta) {
        for (int i = rain.size - 1; i >= 0; i--) {
            Rain drop = rain.get(i);
            drop.y -= drop.speed * delta;

            if (drop.y < 0) {
                rain.removeIndex(i);
                collectDrop();
            }
        }
    }

    // Helper function for collecting rainfall in the bowl
    private void collectDrop() {
        dropsCollected++;
        fillBar.setValue(dropsCollected);

        if (dropsCollected >= DROPS_TO_FILL) {
            dropsCollected = 0;
            fillBar.setValue(0f);
            gameData.currency++;
            currencyLabel.setText(getCurrencyText());
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
        bowlTexture.dispose();
    }
}
