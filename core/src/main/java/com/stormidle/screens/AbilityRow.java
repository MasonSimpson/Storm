package com.stormidle.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.stormidle.objects.GameData;
import com.stormidle.upgrades.AbilityDefinition;
import com.stormidle.upgrades.AbilityManager;

import static com.stormidle.screens.GameScreen.*;

/**
 * A self-contained widget for one ability row.
 * Refreshes its cooldown timer and button state in-place.
 */
public class AbilityRow extends com.badlogic.gdx.scenes.scene2d.Group implements Refreshable {

    private final AbilityDefinition def;
    private final AbilityManager abilities;
    private final GameData gameData;
    private final float rowWidth;

    private final Texture rowTex;
    private final Texture rowLockedTex;
    private final Texture buyBtnTex;
    private final Texture buyBtnDisabledTex;

    public interface ActivateCallback {
        void onActivated();
    }
    private final ActivateCallback callback;

    // Fixed actors
    private final Image rowBg;
    private final Label nameLabel;
    private final Label descLabel;

    // Mutable right-side actors
    private Label cdLabel;
    private Label costLabel;
    private Image buyBtn;
    private Label btnLabel;

    public AbilityRow(AbilityDefinition def, AbilityManager abilities, GameData gameData,
                      float rowWidth, BitmapFont font,
                      Texture rowTex, Texture rowLockedTex,
                      Texture buyBtnTex, Texture buyBtnDisabledTex,
                      ActivateCallback callback) {
        this.def               = def;
        this.abilities         = abilities;
        this.gameData          = gameData;
        this.rowWidth          = rowWidth;
        this.rowTex            = rowTex;
        this.rowLockedTex      = rowLockedTex;
        this.buyBtnTex         = buyBtnTex;
        this.buyBtnDisabledTex = buyBtnDisabledTex;
        this.callback          = callback;

        setSize(rowWidth, ROW_HEIGHT);

        rowBg = new Image(rowTex);
        rowBg.setSize(rowWidth, ROW_HEIGHT);
        addActor(rowBg);

        nameLabel = new Label(def.name, new Label.LabelStyle(font, Color.WHITE));
        nameLabel.setPosition(6f, ROW_HEIGHT - nameLabel.getPrefHeight() - 4f);
        addActor(nameLabel);

        BitmapFont descFont = new BitmapFont();
        descLabel = new Label(def.description,
            new Label.LabelStyle(descFont, Color.LIGHT_GRAY));
        descLabel.setPosition(6f, 6f);
        addActor(descLabel);

        refresh();
    }

    @Override
    public void refresh() {
        boolean anyActive  = abilities.isAnyAbilityActive();
        boolean onCooldown = abilities.isOnCooldown(def.id);
        boolean canAfford  = gameData.currency >= def.cost;
        boolean canBuy     = !anyActive && !onCooldown && canAfford;

        // Background
        rowBg.setDrawable(new TextureRegionDrawable(
            new TextureRegion(onCooldown ? rowLockedTex : rowTex)));

        // Name / desc colors
        nameLabel.setColor(onCooldown ? Color.DARK_GRAY : Color.WHITE);
        descLabel.setColor(onCooldown ? Color.DARK_GRAY : Color.LIGHT_GRAY);

        // Remove old right-side actors
        removeRightSide();

        float btnX = rowWidth - BTN_BUY_W - 6f;

        if (onCooldown) {
            float remaining = abilities.getCooldownRemaining(def.id);
            int mins = (int)(remaining / 60);
            int secs = (int)(remaining % 60);
            cdLabel = new Label(String.format("%d:%02d", mins, secs),
                new Label.LabelStyle(new BitmapFont(), Color.GRAY));
            cdLabel.setPosition(
                btnX + (BTN_BUY_W / 2f) - (cdLabel.getPrefWidth()  / 2f),
                (ROW_HEIGHT / 2f)        - (cdLabel.getPrefHeight() / 2f));
            addActor(cdLabel);

        } else {
            costLabel = new Label(def.cost + " drops",
                new Label.LabelStyle(new BitmapFont(),
                    canAfford ? new Color(0.9f, 0.85f, 0.3f, 1f) : Color.RED));
            costLabel.setPosition(btnX, BTN_BUY_H + 10f);
            addActor(costLabel);

            buyBtn = new Image(canBuy ? buyBtnTex : buyBtnDisabledTex);
            buyBtn.setSize(BTN_BUY_W, BTN_BUY_H);
            buyBtn.setPosition(btnX, 6f);
            addActor(buyBtn);

            btnLabel = new Label(anyActive ? "Active" : "Buy",
                new Label.LabelStyle(new BitmapFont(), Color.WHITE));
            btnLabel.setPosition(
                btnX + (BTN_BUY_W / 2f) - (btnLabel.getPrefWidth()  / 2f),
                6f   + (BTN_BUY_H / 2f) - (btnLabel.getPrefHeight() / 2f));
            btnLabel.setTouchable(Touchable.disabled);
            addActor(btnLabel);

            if (canBuy) {
                buyBtn.addListener(new ClickListener() {
                    @Override public void clicked(InputEvent event, float x, float y) {
                        boolean activated = abilities.tryActivate(def.id, gameData);
                        if (activated) callback.onActivated();
                    }
                });
            }
        }
    }

    private void removeRightSide() {
        if (cdLabel   != null) { cdLabel.remove();   cdLabel   = null; }
        if (costLabel != null) { costLabel.remove();  costLabel = null; }
        if (buyBtn    != null) { buyBtn.remove();     buyBtn    = null; }
        if (btnLabel  != null) { btnLabel.remove();   btnLabel  = null; }
    }
}
