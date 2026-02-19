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
import com.badlogic.gdx.scenes.scene2d.Group;
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
    private static final int DROPS_TO_FILL =      10; // How many drops needed to convert the rainfall into currency
    private static final float FALL_SPEED =       300f;
    private static final float BOWL_WIDTH =       400f;
    private static final float BOWL_HEIGHT =      200f;
    private static final float BAR_WIDTH =        80f; // Progress bar for bowl
    private static final float BAR_HEIGHT =       8f;
    private static final float BAR_Y_OFFSET =     6f; // Gap between bottom of bowl and top of bar
    private static final float ICON_SIZE =        32f;
    private static final float ICON_PADDING =     8f; // Gap between icon and label
    private static final float BTN_WIDTH =        400f;
    private static final float BTN_HEIGHT =       200f;
    private static final float BTN_PADDING =      6f;   // Gap between buttons
    private static final float BTN_RIGHT_MARGIN = 20f;   // Gap from right edge of screen
    private static final float POPUP_WIDTH =      400f;
    private static final float POPUP_HEIGHT =     600f;

    // Core of game
    private final com.stormidle.Storm game;
    private Stage stage = new Stage(new ScreenViewport());
    private SpriteBatch batch;
    private GameData gameData = new GameData();

    // Sprite textures
    private Texture cloudTexture;
    private Texture rainTexture;
    private Texture bowlTexture;
    private Texture currencyTexture;

    // Button textures
    private Texture rainButtonTexture;
    private Texture autoButtonTexture;
    private Texture econButtonTexture;
    private Texture ultButtonTexture;
    private Texture prestigeButtonTexture;
    private Texture notUnlockedTexture;
    private Texture popupBgTexture;

    // Tracks which upgrade popup is open (null = none are open)
    private Group activePopup = null;
    private String activePopupType = null;

    // Actors, or sprites, for stage
    private Image cloud;
    private Image currency;
    private ProgressBar fillBar;
    private Label currencyLabel;

    // Rain and bowl states
    private Array<Rain> rain;
    private int dropsCollected = 0; // Resets to 0 each time the bowl converts
    private float bowlX;
    private float bowlY;

    // Variables used to position sprites
    private float stageWidth;
    private float stageHeight;

    public GameScreen(com.stormidle.Storm game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Initialize batch and rain array
        batch = new SpriteBatch();
        rain = new Array<>();

        Gdx.input.setInputProcessor(stage);
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        stageWidth  = stage.getWidth();
        stageHeight = stage.getHeight();

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

        // Bowl to catch the rainfall
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
        currencyLabel.setPosition(0, stageHeight - currencyLabel.getPrefHeight() - 33);

        stage.addActor(currencyLabel);

        currencyTexture = new Texture("currency.png");
        currency = new Image(currencyTexture);
        currency.setSize(ICON_SIZE, ICON_SIZE);
        stage.addActor(currency);

        // Set initial positions of icon + label together
        updateCurrencyDisplay();

        // Rain texture
        rainTexture = new Texture("rain.png");

        // Upgrade button textures
        rainButtonTexture = new Texture("rain_upgrades_button.png");
        autoButtonTexture = new Texture("auto_upgrades_button.png");
        econButtonTexture = new Texture("econ_upgrades_button.png");
        ultButtonTexture = new Texture("ult_upgrades_button.png");
        prestigeButtonTexture = new Texture("prestige_button.png");
        notUnlockedTexture = new Texture("not_unlocked_button.png");

        // Upgrade popup background
        Pixmap popupPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        popupPixmap.setColor(0.1f, 0.1f, 0.15f, 0.95f);
        popupPixmap.fill();
        popupBgTexture = new Texture(popupPixmap);
        popupPixmap.dispose();

        // Upgrade buttons stacked on the right, from top down
        float btnX  = stageWidth - BTN_WIDTH - BTN_RIGHT_MARGIN;
        float startY = stageHeight - BTN_HEIGHT - 60f;

        addUpgradeButton(rainButtonTexture, btnX, startY - 0 * (BTN_HEIGHT + BTN_PADDING), "rain");
        addUpgradeButton(autoButtonTexture, btnX, startY - 1 * (BTN_HEIGHT + BTN_PADDING), "auto");
        addUpgradeButton(econButtonTexture, btnX, startY - 2 * (BTN_HEIGHT + BTN_PADDING), "econ");
        addUpgradeButton(ultButtonTexture, btnX, startY - 3 * (BTN_HEIGHT + BTN_PADDING), "ult");
        // Prestige pinned to bottom right
        addUpgradeButton(prestigeButtonTexture, btnX, 20f, "prestige");
    }

    // Helper function to add and position upgrade buttons on the screen properly
    private void addUpgradeButton(Texture texture, float x, float y, final String type) {
        Image btn = new Image(texture);
        btn.setSize(BTN_WIDTH, BTN_HEIGHT);
        btn.setPosition(x, y);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float ex, float ey) {
                togglePopup(type);
            }
        });
        stage.addActor(btn);
    }

    // Helper function that opens the popup for the given type, or closes it if it's already open
    private void togglePopup(String type) {
        if (activePopup != null) {
            activePopup.remove();
            String wasOpen = activePopupType;
            activePopup     = null;
            activePopupType = null;
            // If we clicked the same button that was already open, just close it
            if (type.equals(wasOpen)) return;
        }

        activePopup = buildPopup(type);
        activePopupType = type;
        stage.addActor(activePopup);
    }

    // Helper function that builds and returns a popup Group for the given upgrade category
    private Group buildPopup(String type) {
        Group popup = new Group();
        popup.setName(type);

        // Position on center of screen
        float popupX = (stageWidth / 2f) - (POPUP_WIDTH / 2f);
        float popupY = (stageHeight / 2f) - (POPUP_HEIGHT / 2f);

        popup.setPosition(popupX, popupY);
        popup.setSize(POPUP_WIDTH, POPUP_HEIGHT);

        // Background panel
        Image bg = new Image(popupBgTexture);
        bg.setSize(POPUP_WIDTH, POPUP_HEIGHT);
        popup.addActor(bg);

        // Title
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.5f);
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        Label title = new Label(getPopupTitle(type), titleStyle);
        title.setPosition(10, POPUP_HEIGHT - 30);
        popup.addActor(title);

        // TODO: Replace with real upgrade text
        BitmapFont bodyFont = new BitmapFont();
        Label.LabelStyle bodyStyle = new Label.LabelStyle(bodyFont, Color.LIGHT_GRAY);
        Label placeholder = new Label("No upgrades yet.", bodyStyle);
        placeholder.setPosition(10, POPUP_HEIGHT - 60);
        popup.addActor(placeholder);

        // Close button
        BitmapFont closeFont = new BitmapFont();
        Label.LabelStyle closeStyle = new Label.LabelStyle(closeFont, Color.RED);
        Label closeBtn = new Label("[X]", closeStyle);
        closeBtn.setPosition(POPUP_WIDTH - 30, POPUP_HEIGHT - 25);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                activePopup     = null;
                activePopupType = null;
            }
        });
        popup.addActor(closeBtn);

        return popup;
    }

    // Quick helper function for title that displays on upgrade popup windows
    private String getPopupTitle(String type) {
        switch (type) {
            case "rain":     return "Rain Upgrades";
            case "auto":     return "Auto Upgrades";
            case "econ":     return "Econ Upgrades";
            case "ult":      return "Ult Upgrades";
            case "prestige": return "Cloud Prestige";
            default:         return "Upgrades";
        }
    }

    // Helper function for positioning the currency icon + label
    // Useful later on when currency value becomes bigger
    private void updateCurrencyDisplay() {
        currencyLabel.setText(getCurrencyText());

        float labelW = currencyLabel.getPrefWidth();
        float totalW = ICON_SIZE + ICON_PADDING + labelW;
        float startX = (stageWidth / 2f) - (totalW / 2f);
        float yPos = stageHeight - currencyLabel.getPrefHeight() - 33;

        currency.setPosition(startX, yPos + (currencyLabel.getPrefHeight() / 2f) - (ICON_SIZE / 2f));
        currencyLabel.setPosition(startX + ICON_SIZE + ICON_PADDING, yPos);
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
        return gameData.currency + "";
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
            updateCurrencyDisplay();
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
        currencyTexture.dispose();
        rainButtonTexture.dispose();
        autoButtonTexture.dispose();
        econButtonTexture.dispose();
        ultButtonTexture.dispose();
        prestigeButtonTexture.dispose();
        notUnlockedTexture.dispose();
        popupBgTexture.dispose();
    }
}
