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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.stormidle.objects.Rain;
import com.stormidle.objects.GameData;
import com.stormidle.upgrades.UpgradeTier;
import com.stormidle.upgrades.RainUpgrades;
import com.stormidle.upgrades.AutoUpgrades;

public class GameScreen implements Screen {

    // Tweakable values for game UI
    private static final float BOWL_WIDTH =       400f;
    private static final float BOWL_HEIGHT =      200f;
    private static final float BAR_WIDTH =        80f; // Progress bar for bowl
    private static final float BAR_HEIGHT =       8f;
    private static final float BAR_Y_OFFSET =     3f; // Gap between bottom of bowl and top of bar
    private static final float ICON_SIZE =        32f;
    private static final float ICON_PADDING =     8f; // Gap between icon and label
    private static final float BTN_WIDTH =        400f;
    private static final float BTN_HEIGHT =       200f;
    private static final float BTN_PADDING =      3f;   // Gap between buttons
    private static final float BTN_RIGHT_MARGIN = 20f;   // Gap from right edge of screen
    private static final float POPUP_WIDTH =      400f;
    private static final float POPUP_HEIGHT =     600f;

    // Upgrade row layout inside upgrade popup
    private static final float ROW_HEIGHT =       80f;
    private static final float ROW_PADDING =       8f;
    private static final float ROW_MARGIN =       10f;
    private static final float BTN_BUY_W =        70f;
    private static final float BTN_BUY_H =        30f;
    private static final float HEADER_H =         40f; // Height reserved for title + close button
    private static final float POPUP_PADDING =     8f; // Padding around scroll pane

    // Core of game
    private final com.stormidle.Storm game;
    private Stage stage = new Stage(new ScreenViewport());
    private SpriteBatch batch;
    private GameData gameData = new GameData();
    private RainUpgrades rainUpgrades = new RainUpgrades();
    private AutoUpgrades autoUpgrades = new AutoUpgrades();

    // Sprite textures
    private Texture cloudTexture;
    private Texture rainTexture;
    private Texture bowlTexture;
    private Texture currencyTexture;
    private Texture rainButtonTexture;
    private Texture autoButtonTexture;
    private Texture econButtonTexture;
    private Texture abilitiesButtonTexture;
    private Texture prestigeButtonTexture;
    private Texture notUnlockedTexture;
    private Texture popupBgTexture;

    // Textures for upgrade row buttons (reused by all trees)
    private Texture rowTexture;
    private Texture rowLockedTexture;
    private Texture rowPurchasedTexture;
    private Texture buyButtonTexture;
    private Texture buyButtonDisabledTexture;

    // Tracks which upgrade popup is open (null = none are open)
    private Group activePopup = null;
    private String activePopupType = null;

    // Actors, or sprites, for stage
    private Image cloud;
    private Image currency;
    private ProgressBar fillBar;
    private Label currencyLabel;

    // Rain / bowl state
    private Array<Rain> rain;
    private int dropsCollected = 0; // Resets to 0 each time the bowl converts
    private float bowlX;
    private float bowlY;

    // Pause menu and exit confirmation
    private Group pauseMenu = null;
    private Group exitConfirm = null;

    // Accumulates drops from auto-rain
    private float autoRainAccumulator = 0f;

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

        // Use a multiplexer so the stage and key listener both receive input
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    togglePauseMenu();
                    return true;
                }
                return false;
            }
        });
        Gdx.input.setInputProcessor(multiplexer);
        stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        stageWidth  = stage.getWidth();
        stageHeight = stage.getHeight();

        // Initialize cloud and add to stage
        cloudTexture = new Texture("cloud_1.png");
        cloud = new Image(cloudTexture);
        cloud.setSize(cloud.getPrefWidth(), cloud.getPrefHeight());
        cloud.setPosition(stageWidth * 0.10f, stageHeight * 0.65f);
        cloud.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               rain.add(new Rain(cloud.getX(), cloud.getY(), gameData.fallSpeed));
           }
        });
        stage.addActor(cloud);

        // Bowl to catch the rainfall
        bowlTexture = new Texture("bowl.png");
        bowlX = cloud.getX() + (cloud.getWidth() / 2f) - (BOWL_WIDTH / 2f);
        bowlY = 20f;

        // Progress bar for bowl, sits underneath the bowl actor
        fillBar = new ProgressBar(0f, gameData.dropsToFill, 1f, false, createFillBarStyle());
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

        // Currency icon
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
        abilitiesButtonTexture = new Texture("abilities_button.png");
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
        addUpgradeButton(abilitiesButtonTexture, btnX, startY - 3 * (BTN_HEIGHT + BTN_PADDING), "ult");
        // Prestige pinned to bottom right
        addUpgradeButton(prestigeButtonTexture, btnX, 20f, "prestige");

        // Upgrade row textures
        rowTexture =               makeColorTexture(0.2f, 0.25f, 0.35f, 1f);
        rowLockedTexture =         makeColorTexture(0.15f, 0.15f, 0.20f, 1f);
        rowPurchasedTexture =      makeColorTexture(0.15f, 0.35f, 0.20f, 1f);
        buyButtonTexture =         makeColorTexture(0.25f, 0.55f, 0.85f, 1f);
        buyButtonDisabledTexture = makeColorTexture(0.35f, 0.35f, 0.40f, 1f);
    }

    // Toggles pause menu open or closed
    private void togglePauseMenu() {
        if (pauseMenu != null) {
            closePauseMenu();
        } else {
            openPauseMenu();
        }
    }

    // Opens pause menu
    private void openPauseMenu() {
        // Close any open upgrade popup
        if (activePopup != null) {
            activePopup.remove();
            activePopupType = null;
            activePopup = null;
        }
        pauseMenu = buildPauseMenu();
        stage.addActor(pauseMenu);
    }

    // Closes pause menu
    private void closePauseMenu() {
        if (exitConfirm != null) {
            exitConfirm.remove();
            exitConfirm = null;
        }
        if (pauseMenu != null) {
            pauseMenu.remove();
            pauseMenu = null;
        }
    }

    // Builds and returns the pause menu Group
    private Group buildPauseMenu() {
        float menuW = 340f;
        float menuH = 280f;
        float menuX = (stageWidth  / 2f) - (menuW / 2f);
        float menuY = (stageHeight / 2f) - (menuH / 2f);

        Group menu = new Group();
        menu.setSize(menuW, menuH);
        menu.setPosition(menuX, menuY);

        // Background
        Image bg = new Image(makeColorTexture(0.08f, 0.08f, 0.12f, 0.97f));
        bg.setSize(menuW, menuH);
        menu.addActor(bg);

        // Title
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.8f);
        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        Label title = new Label("PAUSED", titleStyle);
        title.setPosition((menuW / 2f) - (title.getPrefWidth() / 2f), menuH - 45f);
        menu.addActor(title);

        // Volume label
        BitmapFont labelFont = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle(labelFont, Color.LIGHT_GRAY);
        Label volLabel = new Label("Music Volume", labelStyle);
        volLabel.setPosition(20f, menuH - 90f);
        menu.addActor(volLabel);

        // Volume slider
        Slider.SliderStyle sliderStyle = createVolumeSliderStyle();
        Slider volumeSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        volumeSlider.setValue(game.music != null ? game.music.getVolume() : 0.5f);
        volumeSlider.setSize(menuW - 40f, 16f);
        volumeSlider.setPosition(20f, menuH - 125f);
        volumeSlider.addListener(event -> {
            if (game.music != null) game.music.setVolume(volumeSlider.getValue());
            return false;
        });
        menu.addActor(volumeSlider);

        // Resume button
        Image resumeBtn = new Image(makeColorTexture(0.2f, 0.5f, 0.25f, 1f));
        resumeBtn.setSize(130f, 40f);
        resumeBtn.setPosition(20f, 50f);
        resumeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                closePauseMenu();
            }
        });
        menu.addActor(resumeBtn);

        BitmapFont btnFont = new BitmapFont();
        Label.LabelStyle btnStyle = new Label.LabelStyle(btnFont, Color.WHITE);
        Label resumeLabel = new Label("Resume", btnStyle);
        resumeLabel.setPosition(
            20f  + (130f / 2f) - (resumeLabel.getPrefWidth()  / 2f),
            50f  + (40f  / 2f) - (resumeLabel.getPrefHeight() / 2f)
        );
        resumeLabel.setTouchable(Touchable.disabled);
        menu.addActor(resumeLabel);

        // Exit button
        Image exitBtn = new Image(makeColorTexture(0.55f, 0.15f, 0.15f, 1f));
        exitBtn.setSize(130f, 40f);
        exitBtn.setPosition(menuW - 150f, 50f);
        exitBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                showExitConfirmation();
            }
        });
        menu.addActor(exitBtn);

        Label exitLabel = new Label("Exit", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        exitLabel.setPosition(
            (menuW - 150f) + (130f / 2f) - (exitLabel.getPrefWidth()  / 2f),
            50f             + (40f  / 2f) - (exitLabel.getPrefHeight() / 2f)
        );
        exitLabel.setTouchable(Touchable.disabled);
        menu.addActor(exitLabel);

        return menu;
    }

    // Builds a volume slider style with a filled blue bar
    private Slider.SliderStyle createVolumeSliderStyle() {
        Drawable bg       = new TextureRegionDrawable(new TextureRegion(makeColorTexture(0.25f, 0.25f, 0.3f, 1f)));
        Drawable knob     = new TextureRegionDrawable(new TextureRegion(makeColorTexture(1f,    1f,    1f,   1f)));
        Drawable knobFill = new TextureRegionDrawable(new TextureRegion(makeColorTexture(0.3f,  0.6f,  1f,   1f)));

        knob.setMinWidth(12f);
        knob.setMinHeight(16f);

        Slider.SliderStyle style = new Slider.SliderStyle(bg, knob);
        style.knobBefore = knobFill;
        return style;
    }

    // Shows the exit confirmation dialog on top of the pause menu
    private void showExitConfirmation() {
        // Only one confirmation at a time
        if (exitConfirm != null) return;

        float w = 280f;
        float h = 160f;
        float x = (stageWidth  / 2f) - (w / 2f);
        float y = (stageHeight / 2f) - (h / 2f);

        exitConfirm = new Group();
        exitConfirm.setSize(w, h);
        exitConfirm.setPosition(x, y);

        // Background — slightly different shade so it reads as a new layer
        Image bg = new Image(makeColorTexture(0.05f, 0.05f, 0.1f, 1f));
        bg.setSize(w, h);
        exitConfirm.addActor(bg);

        // Message
        BitmapFont msgFont = new BitmapFont();
        Label.LabelStyle msgStyle = new Label.LabelStyle(msgFont, Color.WHITE);
        Label msg = new Label("Are you sure you want to exit?", msgStyle);
        msg.setWrap(true);
        msg.setWidth(w - 20f);
        msg.setPosition(10f, h - 50f);
        exitConfirm.addActor(msg);

        // Confirm exit button
        Image confirmBtn = new Image(makeColorTexture(0.55f, 0.15f, 0.15f, 1f));
        confirmBtn.setSize(110f, 36f);
        confirmBtn.setPosition(15f, 20f);
        confirmBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        exitConfirm.addActor(confirmBtn);

        Label confirmLabel = new Label("Exit", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        confirmLabel.setPosition(
            15f  + (110f / 2f) - (confirmLabel.getPrefWidth()  / 2f),
            20f  + (36f  / 2f) - (confirmLabel.getPrefHeight() / 2f)
        );
        confirmLabel.setTouchable(Touchable.disabled);
        exitConfirm.addActor(confirmLabel);

        // Cancel button
        Image cancelBtn = new Image(makeColorTexture(0.2f, 0.2f, 0.25f, 1f));
        cancelBtn.setSize(110f, 36f);
        cancelBtn.setPosition(w - 125f, 20f);
        cancelBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                exitConfirm.remove();
                exitConfirm = null;
            }
        });
        exitConfirm.addActor(cancelBtn);

        Label cancelLabel = new Label("Cancel", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        cancelLabel.setPosition(
            (w - 125f) + (110f / 2f) - (cancelLabel.getPrefWidth()  / 2f),
            20f         + (36f  / 2f) - (cancelLabel.getPrefHeight() / 2f)
        );
        cancelLabel.setTouchable(Touchable.disabled);
        exitConfirm.addActor(cancelLabel);

        stage.addActor(exitConfirm);
    }

    // Creates a 1x1 Texture filled with the given RGBA color
    private Texture makeColorTexture(float r, float g, float b, float a) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(r, g, b, a);
        pm.fill();
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
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
            stage.setScrollFocus(null);
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

    // Rebuilds and re-opens the current popup in place. Call this after any upgrade is purchased
    // so button states can refresh
    private void refreshActivePopup() {
        if (activePopup == null) return;
        String type = activePopupType;
        activePopup.remove();
        activePopup     = buildPopup(type);
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

        // Content — rain popup gets real upgrade rows; others keep placeholder
        // TODO: Replace placeholder with other upgrade content
        if ("rain".equals(type)) {
            buildPopupContent(popup,
                new String[][]{{"Rain Fall Speed"}, {"Rain Value"}},
                new Array[]{rainUpgrades.speedTree, rainUpgrades.bowlTree}
            );
        } else if ("auto".equals(type)) {
            buildPopupContent(popup,
                new String[][]{{"Rain Generation"}},
                new Array[]{autoUpgrades.autoTree}
            );
        } else {
            BitmapFont bodyFont = new BitmapFont();
            Label.LabelStyle bodyStyle = new Label.LabelStyle(bodyFont, Color.LIGHT_GRAY);
            Label placeholder = new Label("No upgrades yet.", bodyStyle);
            placeholder.setPosition(10, POPUP_HEIGHT - 60);
            popup.addActor(placeholder);
        }

        // Close button
        BitmapFont closeFont = new BitmapFont();
        Label.LabelStyle closeStyle = new Label.LabelStyle(closeFont, Color.RED);
        Label closeBtn = new Label("[X]", closeStyle);
        closeBtn.setPosition(POPUP_WIDTH - 30, POPUP_HEIGHT - 25);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popup.remove();
                stage.setScrollFocus(null);
                activePopup     = null;
                activePopupType = null;
            }
        });
        popup.addActor(closeBtn);

        return popup;
    }

    // Populates the given upgrade group with all upgrade trees rendered as stacked rows.
    // Rows are drawn top-to-bottom with a small section header separating the trees
    private void buildPopupContent(Group popup, String[][] sections, Array<UpgradeTier>[] trees) {
        // Height and width of scroll pane
        float scrollW = POPUP_WIDTH - POPUP_PADDING * 2f;
        float scrollH = POPUP_HEIGHT - HEADER_H - POPUP_PADDING * 2f;

        BitmapFont font = new BitmapFont();
        font.getData().setScale(0.9f);

        BitmapFont headerFont = new BitmapFont();
        headerFont.getData().setScale(1.1f);

        // Inner table that holds all rows and wrapped in ScrollPane
        Table content = new Table();
        content.top().left();
        content.defaults().left();

        // Iterate over each tree, drawing a section header followed by its upgrade rows
        for (int t = 0; t < trees.length; t++) {
            if (t > 0) content.add().height(ROW_PADDING * 2f).row(); // gap between trees
            addSectionHeader(content, sections[t][0], headerFont, scrollW);
            for (int i = 0; i < trees[t].size; i++) {
                addUpgradeRow(content, font, trees[t], i, scrollW);
            }
        }

        // Wrap content in a ScrollPane
        ScrollPane.ScrollPaneStyle spStyle = new ScrollPane.ScrollPaneStyle();
        ScrollPane scrollPane = new ScrollPane(content, spStyle);
        scrollPane.setScrollingDisabled(true, false); // Horizontal scrolling disabled, vertical scrolling on
        scrollPane.setOverscroll(false, false);
        scrollPane.setFadeScrollBars(true);
        scrollPane.setSize(scrollW, scrollH);
        scrollPane.setPosition(POPUP_PADDING, POPUP_PADDING);

        popup.addActor(scrollPane);

        // Give scroll pane immediate focus so player can scroll without having to click
        stage.setScrollFocus(scrollPane);
    }

    // Draws a section header label to the content table
    private void addSectionHeader(Table content, String text, BitmapFont font, float rowWidth) {
        Label.LabelStyle style = new Label.LabelStyle(font, new Color(0.6f, 0.85f, 1f, 1f));
        Label header = new Label(text, style);
        content.add(header).width(rowWidth).padTop(ROW_PADDING).padBottom(4f).row();
    }

    // Draws one upgrade row for the given tree/index. Returns the new Y below it.
    // Row states:
    // * PURCHASED - green tint, no buy button
    // * UNLOCKED - normal tint, buy button (greyed if not affordable)
    // * LOCKED - dark tint, padlock label, no buy button
    private void addUpgradeRow(Table content, BitmapFont font,
                               final Array<UpgradeTier> tree, final int index, float rowWidth) {
        UpgradeTier upgrade  = tree.get(index);
        UpgradeTier previous = UpgradeTier.previous(tree, index);
        boolean purchased = upgrade.purchased;
        boolean unlocked  = upgrade.isUnlocked(previous);
        boolean canAfford = gameData.currency >= upgrade.cost;

        // Outer group sized to ROW_HEIGHT so we can layer background + widgets
        Group row = new Group();
        row.setSize(rowWidth, ROW_HEIGHT);

        // Background
        Texture bgTex = purchased ? rowPurchasedTexture
            : unlocked  ? rowTexture
            : rowLockedTexture;
        Image rowBg = new Image(bgTex);
        rowBg.setSize(rowWidth, ROW_HEIGHT);
        row.addActor(rowBg);

        // Upgrade name
        Color nameColor = purchased ? Color.GREEN
            : unlocked  ? Color.WHITE
            : Color.DARK_GRAY;
        Label.LabelStyle nameStyle = new Label.LabelStyle(font, nameColor);
        Label nameLabel = new Label(upgrade.name, nameStyle);
        nameLabel.setPosition(6f, ROW_HEIGHT - nameLabel.getPrefHeight() - 4f);
        row.addActor(nameLabel);

        // Description
        BitmapFont descFont = new BitmapFont();
        Label.LabelStyle descStyle = new Label.LabelStyle(descFont,
            unlocked ? Color.LIGHT_GRAY : Color.DARK_GRAY);
        Label descLabel = new Label(upgrade.description, descStyle);
        descLabel.setPosition(6f, 6f);
        row.addActor(descLabel);

        // Right-side widget varies by state
        if (purchased) {
            BitmapFont badgeFont = new BitmapFont();
            Label.LabelStyle badgeStyle = new Label.LabelStyle(badgeFont, Color.GREEN);
            Label badge = new Label("Purchased", badgeStyle);
            badge.setPosition(
                rowWidth - badge.getPrefWidth() - 6f,
                (ROW_HEIGHT / 2f) - (badge.getPrefHeight() / 2f)
            );
            row.addActor(badge);

        } else if (unlocked) {
            float btnX = rowWidth - BTN_BUY_W - 6f;

            // Cost label
            BitmapFont costFont = new BitmapFont();
            Label.LabelStyle costStyle = new Label.LabelStyle(costFont,
                canAfford ? new Color(0.9f, 0.85f, 0.3f, 1f) : Color.RED);
            Label costLabel = new Label(upgrade.cost + " drops", costStyle);
            costLabel.setPosition(btnX, BTN_BUY_H + 10f);
            row.addActor(costLabel);

            // Buy button background
            Image buyBtn = new Image(canAfford ? buyButtonTexture : buyButtonDisabledTexture);
            buyBtn.setSize(BTN_BUY_W, BTN_BUY_H);
            buyBtn.setPosition(btnX, 6f);
            row.addActor(buyBtn);

            // "Buy" label
            BitmapFont btnFont = new BitmapFont();
            Label.LabelStyle btnStyle = new Label.LabelStyle(btnFont, Color.WHITE);
            Label btnLabel = new Label("Buy", btnStyle);
            btnLabel.setPosition(
                btnX + (BTN_BUY_W / 2f) - (btnLabel.getPrefWidth()  / 2f),
                6f   + (BTN_BUY_H / 2f) - (btnLabel.getPrefHeight() / 2f)
            );
            btnLabel.setTouchable(Touchable.disabled);
            row.addActor(btnLabel);

            buyBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    // Re-check affordability at click time, not at popup-build time
                    if (gameData.currency < upgrade.cost) return;
                    boolean bought = UpgradeTier.tryPurchase(tree, index, gameData);
                    if (bought) {
                        updateFillBar();
                        updateCurrencyDisplay();
                        refreshActivePopup();
                    }
                }
            });

        } else {
            BitmapFont lockFont = new BitmapFont();
            Label.LabelStyle lockStyle = new Label.LabelStyle(lockFont, Color.DARK_GRAY);
            Label lockLabel = new Label("Locked", lockStyle);
            lockLabel.setPosition(
                rowWidth - lockLabel.getPrefWidth() - 6f,
                (ROW_HEIGHT / 2f) - (lockLabel.getPrefHeight() / 2f)
            );
            row.addActor(lockLabel);
        }

        content.add(row).width(rowWidth).height(ROW_HEIGHT).padBottom(ROW_PADDING).row();
    }

    // Quick helper function for title that displays on upgrade popup windows
    private String getPopupTitle(String type) {
        return switch (type) {
            case "rain" -> "Rain Upgrades";
            case "auto" -> "Auto Upgrades";
            case "econ" -> "Econ Upgrades";
            case "ult" -> "Ult Upgrades";
            case "prestige" -> "Cloud Prestige";
            default -> "Upgrades";
        };
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

    // Syncs the progress bar's max value to match the drops needed to convert to currency
    private void updateFillBar() {
        // Cap collected drops so bar doesn't need the new lower max value
        if (dropsCollected > gameData.dropsToFill) dropsCollected = gameData.dropsToFill;
        fillBar.setRange(0f, gameData.dropsToFill);
        fillBar.setValue(dropsCollected);
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

        // Auto-rain based on rps (rainfall per second) in GameData.java
        if (gameData.rps > 0) {
            autoRainAccumulator += gameData.rps * delta;
            while (autoRainAccumulator >= 1f) {
                autoRainAccumulator -= 1f;
                rain.add(new Rain(cloud.getX(), cloud.getY(), gameData.fallSpeed));
            }
        }

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

        if (dropsCollected >= gameData.dropsToFill) {
            dropsCollected = 0;
            fillBar.setValue(0f);
            gameData.currency++;
            updateCurrencyDisplay();
            // Refresh popup affordability after earning currency
            if("rain".equals(activePopupType)) refreshActivePopup();
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
        abilitiesButtonTexture.dispose();
        prestigeButtonTexture.dispose();
        notUnlockedTexture.dispose();
        popupBgTexture.dispose();
        rowTexture.dispose();
        rowLockedTexture.dispose();
        rowPurchasedTexture.dispose();
        buyButtonTexture.dispose();
        buyButtonDisabledTexture.dispose();
    }
}
