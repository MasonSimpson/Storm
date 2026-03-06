package com.stormidle.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class TutorialManager {

    public interface TutorialCallback {
        void onComplete();
    }

    private static final float TOOLTIP_W     = 320f;
    private static final float TOOLTIP_H     = 120f;
    private static final float FADE_DURATION = 0.2f;

    private int     step   = 0;
    private boolean active = false;
    private boolean step1b = false;

    private final Stage            stage;
    private final TutorialCallback callback;
    private final float            stageW, stageH;

    private Group   overlay;
    private Texture tooltipTex;

    public TutorialManager(Stage stage, float stageW, float stageH,
                           TutorialCallback callback) {
        this.stage    = stage;
        this.stageW   = stageW;
        this.stageH   = stageH;
        this.callback = callback;

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(0.1f, 0.1f, 0.18f, 0.97f);
        pm.fill();
        tooltipTex = new Texture(pm);
        pm.dispose();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void start()       { active = true; step = 0; showStep(); }
    public boolean isActive() { return active; }
    public int     getStep()  { return step;   }

    public void onFirstCloudClick()  { if (active && step == 0) advance(); }
    public void onFirstDropLanded()  { if (active && step == 1) showStep1b(); }
    public void onFirstConversion()  { if (active && step == 1) advance(); }
    public void onRainPopupOpened()  { if (active && step == 2) advance(); }
    public void onUpgradePurchased() { if (active && step == 3) advance(); }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void advance() {
        step1b = false;
        step++;
        if (step >= 9) finish();
        else { removeOverlay(); showStep(); }
    }

    private void showStep1b() {
        if (step1b) return;
        step1b = true;
        removeOverlay();
        showStep();
    }

    private void finish() {
        removeOverlay();
        active = false;
        callback.onComplete();
    }

    private void removeOverlay() {
        if (overlay != null) { overlay.remove(); overlay = null; }
    }

    private void showStep() {
        overlay = new Group() {
            @Override
            public Actor hit(float x, float y, boolean touchable) {
                Actor child = super.hit(x, y, touchable);
                return child == this ? null : child;
            }
        };
        overlay.setSize(stageW, stageH);

        // Tooltip box — bottom centre of screen
        float tooltipX = (stageW / 2f) - (TOOLTIP_W / 2f);
        float tooltipY = stageH * 0.12f;

        Image tooltipBg = new Image(tooltipTex);
        tooltipBg.setSize(TOOLTIP_W, TOOLTIP_H);
        tooltipBg.setPosition(tooltipX, tooltipY);
        overlay.addActor(tooltipBg);

        // Message
        BitmapFont msgFont = new BitmapFont();
        Label msg = new Label(messageFor(step), new Label.LabelStyle(msgFont, Color.WHITE));
        msg.setWrap(true);
        msg.setWidth(TOOLTIP_W - 20f);
        msg.layout();
        msg.setPosition(tooltipX + 10f, tooltipY + TOOLTIP_H - msg.getPrefHeight() - 10f);
        overlay.addActor(msg);

        // Skip button
        Label skipBtn = new Label("[Skip tutorial]",
            new Label.LabelStyle(new BitmapFont(), new Color(0.55f, 0.55f, 0.55f, 1f)));
        skipBtn.setPosition(tooltipX + TOOLTIP_W - skipBtn.getPrefWidth() - 8f, tooltipY + 8f);
        skipBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { finish(); }
        });
        overlay.addActor(skipBtn);

        // Next button (steps 4-8 only)
        if (step >= 4) {
            String btnText = (step == 8) ? "Let's go!" : "Next";
            Label nextBtn = new Label(btnText,
                new Label.LabelStyle(new BitmapFont(), new Color(0.3f, 0.85f, 0.4f, 1f)));
            nextBtn.setPosition(tooltipX + 10f, tooltipY + 8f);
            nextBtn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) { advance(); }
            });
            overlay.addActor(nextBtn);
        }

        overlay.getColor().a = 0f;
        overlay.addAction(Actions.fadeIn(FADE_DURATION));
        stage.addActor(overlay);
    }

    private String messageFor(int s) {
        switch (s) {
            case 0: return "Click the cloud to make it rain!";
            case 1: return step1b
                ? "Keep clicking to generate more rain!\nFill the bowl to earn currency."
                : "The rain will fall into the bowl and fill up the meter.\nWatch it fill up!";
            case 2: return "You earned your first currency!\nNow open Rain Upgrades to spend it.";
            case 3: return "Buy Rainfall Speed I to make drops fall faster!";
            case 4: return "Rain Upgrades:\nSpeed up rainfall and reduce how many\ndrops you need to earn currency.";
            case 5: return "Auto Upgrades:\nMake your cloud generate rain automatically,\neven while you're away!";
            case 6: return "Econ Upgrades:\nEarn more currency per bowl and unlock\ncondensation passive income.";
            case 7: return "Abilities:\nPowerful temporary boosts.\nActivate them when you need a push!";
            case 8: return "Prestige:\nReset everything and upgrade your cloud\nfor permanent multipliers. Aim for max level!\n\nGood luck!";
            default: return "";
        }
    }

    public void dispose() {
        if (tooltipTex != null) tooltipTex.dispose();
    }
}
