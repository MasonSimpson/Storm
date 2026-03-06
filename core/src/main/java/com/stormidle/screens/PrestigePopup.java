package com.stormidle.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.graphics.Texture;
import com.stormidle.objects.GameData;
import com.stormidle.upgrades.PrestigeManager;

import static com.stormidle.screens.GameScreen.*;

 // Prestige popup — shows current level, a progress bar toward the next
 // threshold, and a prestige button that only enables when affordable.
 // Implements Refreshable so GameScreen can update it in-place.
public class PrestigePopup extends Group implements Refreshable {

    public interface PrestigeCallback {
        void onPrestige();
        void onClose();
    }

    private final GameData gameData;
    private final PrestigeCallback callback;

    // Mutable widgets
    private final Label  levelLabel;
    private final Label  thresholdLabel;
    private final Label  progressLabel;
    private final ProgressBar progressBar;
    private final Image  prestigeBtn;
    private final Label  prestigeBtnLabel;

    // Textures for button enabled/disabled states
    private final Texture buyBtnTex;
    private final Texture buyBtnDisabledTex;

    public PrestigePopup(GameData gameData,
                         Texture popupBgTex,
                         Texture buyBtnTex,
                         Texture buyBtnDisabledTex,
                         PrestigeCallback callback) {
        this.gameData          = gameData;
        this.buyBtnTex         = buyBtnTex;
        this.buyBtnDisabledTex = buyBtnDisabledTex;
        this.callback          = callback;

        setSize(POPUP_WIDTH, POPUP_HEIGHT);

        // Background
        Image bg = new Image(popupBgTex);
        bg.setSize(POPUP_WIDTH, POPUP_HEIGHT);
        addActor(bg);

        // Title
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.5f);
        Label title = new Label("Cloud Prestige", new Label.LabelStyle(titleFont, Color.WHITE));
        title.setPosition(10f, POPUP_HEIGHT - 30f);
        addActor(title);

        // Close button
        BitmapFont closeFont = new BitmapFont();
        Label closeBtn = new Label("[X]", new Label.LabelStyle(closeFont, Color.RED));
        closeBtn.setPosition(POPUP_WIDTH - 30f, POPUP_HEIGHT - 25f);
        closeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                callback.onClose();
            }
        });
        addActor(closeBtn);

        float centerX = POPUP_WIDTH / 2f;
        float y = POPUP_HEIGHT - 90f;

        // Current level label
        BitmapFont levelFont = new BitmapFont();
        levelFont.getData().setScale(1.3f);
        levelLabel = new Label("", new Label.LabelStyle(levelFont, new Color(0.4f, 0.9f, 1f, 1f)));
        addActor(levelLabel);

        y -= 60f;

        // Bonus description
        BitmapFont descFont = new BitmapFont();
        Label bonusLabel = new Label("Each prestige doubles drops per click\nand upgrades your cloud.",
            new Label.LabelStyle(descFont, Color.LIGHT_GRAY));
        bonusLabel.setPosition(centerX - bonusLabel.getPrefWidth() / 2f, y);
        addActor(bonusLabel);

        y -= 70f;

        // "Next prestige requires:" label
        BitmapFont subFont = new BitmapFont();
        thresholdLabel = new Label("", new Label.LabelStyle(subFont, Color.LIGHT_GRAY));
        addActor(thresholdLabel);

        y -= 30f;

        // Progress label (current / required)
        progressLabel = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        addActor(progressLabel);

        y -= 24f;

        // Progress bar
        progressBar = new ProgressBar(0f, 1f, 0.001f, false, createBarStyle());
        progressBar.setSize(POPUP_WIDTH - 40f, 18f);
        progressBar.setPosition(20f, y);
        addActor(progressBar);

        y -= 70f;

        // Prestige button
        prestigeBtn = new Image(buyBtnDisabledTex);
        prestigeBtn.setSize(160f, 44f);
        prestigeBtn.setPosition(centerX - 80f, y);
        addActor(prestigeBtn);

        prestigeBtnLabel = new Label("PRESTIGE", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        prestigeBtnLabel.setTouchable(Touchable.disabled);
        addActor(prestigeBtnLabel);

        prestigeBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (PrestigeManager.canPrestige(gameData.prestigeLevel, gameData.currency)) {
                    callback.onPrestige();
                }
            }
        });

        // Initial state
        refresh();
    }

    @Override
    public void refresh() {
        int level     = gameData.prestigeLevel;
        int threshold = PrestigeManager.nextThreshold(level);
        boolean maxed = threshold == -1;
        boolean can   = PrestigeManager.canPrestige(level, gameData.currency);

        // Level label
        levelLabel.setText("Cloud Level " + (level + 1));
        levelLabel.setPosition(POPUP_WIDTH / 2f - levelLabel.getPrefWidth() / 2f, POPUP_HEIGHT - 90f);

        if (maxed) {
            thresholdLabel.setText("Maximum prestige reached!");
            thresholdLabel.setPosition(POPUP_WIDTH / 2f - thresholdLabel.getPrefWidth() / 2f,
                POPUP_HEIGHT - 210f);
            progressLabel.setText("");
            progressBar.setValue(1f);
            prestigeBtn.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(buyBtnDisabledTex)));
            prestigeBtnLabel.setText("MAXED");
        } else {
            thresholdLabel.setText("Next prestige requires: " + threshold + " currency");
            thresholdLabel.setPosition(POPUP_WIDTH / 2f - thresholdLabel.getPrefWidth() / 2f,
                POPUP_HEIGHT - 210f);

            int cur = gameData.currency;
            progressLabel.setText(cur + " / " + threshold);
            progressLabel.setPosition(POPUP_WIDTH / 2f - progressLabel.getPrefWidth() / 2f,
                POPUP_HEIGHT - 240f);

            progressBar.setRange(0f, threshold);
            progressBar.setValue(Math.min(cur, threshold));

            // Button state
            Texture btnTex = can ? buyBtnTex : buyBtnDisabledTex;
            prestigeBtn.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(btnTex)));
            prestigeBtnLabel.setText("PRESTIGE");
        }

        // Re-center button label over button
        float btnX = prestigeBtn.getX();
        float btnY = prestigeBtn.getY();
        prestigeBtnLabel.setPosition(
            btnX + (160f / 2f) - prestigeBtnLabel.getPrefWidth()  / 2f,
            btnY + (44f  / 2f) - prestigeBtnLabel.getPrefHeight() / 2f);
    }

    private ProgressBar.ProgressBarStyle createBarStyle() {
        com.badlogic.gdx.graphics.Pixmap bg = new com.badlogic.gdx.graphics.Pixmap(1, 1,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        bg.setColor(0.2f, 0.2f, 0.3f, 1f); bg.fill();
        com.badlogic.gdx.graphics.Pixmap fill = new com.badlogic.gdx.graphics.Pixmap(1, 1,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        fill.setColor(0.4f, 0.8f, 0.3f, 1f); fill.fill();
        com.badlogic.gdx.graphics.Pixmap knob = new com.badlogic.gdx.graphics.Pixmap(1, 1,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        knob.setColor(0f, 0f, 0f, 0f); knob.fill();

        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable bgD =
            new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(new Texture(bg)));
        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable fillD =
            new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(new Texture(fill)));
        com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable knobD =
            new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
                new com.badlogic.gdx.graphics.g2d.TextureRegion(new Texture(knob)));

        bg.dispose(); fill.dispose(); knob.dispose();

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle(bgD, knobD);
        style.knobBefore = fillD;
        return style;
    }
}
