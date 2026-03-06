package com.stormidle.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.stormidle.objects.GameData;
import com.stormidle.upgrades.UpgradeTier;

import static com.stormidle.screens.GameScreen.*;

/**
 * A self-contained widget for one upgrade tier row.
 * Implements Refreshable so it can update its own visuals in-place
 * without the parent ScrollPane needing to be rebuilt.
 */
public class UpgradeRow extends com.badlogic.gdx.scenes.scene2d.Group implements Refreshable {

    // Data references — never change, safe to hold
    private final Array<UpgradeTier> tree;
    private final int index;
    private final GameData gameData;

    // Shared textures from GameScreen
    private final Texture rowTex;
    private final Texture rowLockedTex;
    private final Texture rowPurchasedTex;
    private final Texture buyBtnTex;
    private final Texture buyBtnDisabledTex;

    // Callback so the row can tell GameScreen to update currency display etc.
    public interface PurchaseCallback {
        void onPurchased();
    }
    private final PurchaseCallback callback;

    // Mutable visual actors — updated in refresh()
    private final Image rowBg;
    private final Label nameLabel;
    private final Label descLabel;

    // Right-side widgets (swapped out on refresh)
    private Image buyBtn;
    private Label costLabel;
    private Label badgeLabel;   // "Purchased"
    private Label lockLabel;    // "Locked"
    private Label btnLabel;     // "Buy"

    private final float rowWidth;
    private final BitmapFont font;

    public UpgradeRow(Array<UpgradeTier> tree, int index, GameData gameData,
                      float rowWidth, BitmapFont font,
                      Texture rowTex, Texture rowLockedTex, Texture rowPurchasedTex,
                      Texture buyBtnTex, Texture buyBtnDisabledTex,
                      PurchaseCallback callback) {
        this.tree            = tree;
        this.index           = index;
        this.gameData        = gameData;
        this.rowWidth        = rowWidth;
        this.font            = font;
        this.rowTex          = rowTex;
        this.rowLockedTex    = rowLockedTex;
        this.rowPurchasedTex = rowPurchasedTex;
        this.buyBtnTex       = buyBtnTex;
        this.buyBtnDisabledTex = buyBtnDisabledTex;
        this.callback        = callback;

        setSize(rowWidth, ROW_HEIGHT);

        // Background — created once, texture swapped on refresh
        rowBg = new Image(rowTex);
        rowBg.setSize(rowWidth, ROW_HEIGHT);
        addActor(rowBg);

        // Name label — text is fixed, color changes
        nameLabel = new Label("", new Label.LabelStyle(font, Color.WHITE));
        nameLabel.setPosition(6f, ROW_HEIGHT - nameLabel.getPrefHeight() - 4f);
        addActor(nameLabel);

        // Description label — text is fixed, color changes
        BitmapFont descFont = new BitmapFont();
        descLabel = new Label("", new Label.LabelStyle(descFont, Color.LIGHT_GRAY));
        descLabel.setPosition(6f, 6f);
        addActor(descLabel);

        // Do a full refresh to set all initial state
        refresh();
    }

    @Override
    public void refresh() {
        UpgradeTier upgrade  = tree.get(index);
        UpgradeTier previous = UpgradeTier.previous(tree, index);
        boolean purchased = upgrade.purchased;
        boolean unlocked  = upgrade.isUnlocked(previous);
        boolean canAfford = gameData.currency >= upgrade.cost;

        // --- Background ---
        rowBg.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(
            new com.badlogic.gdx.graphics.g2d.TextureRegion(
                purchased ? rowPurchasedTex : unlocked ? rowTex : rowLockedTex)));

        // --- Name ---
        nameLabel.setText(upgrade.name);
        nameLabel.setColor(purchased ? Color.GREEN : unlocked ? Color.WHITE : Color.DARK_GRAY);
        nameLabel.setPosition(6f, ROW_HEIGHT - nameLabel.getPrefHeight() - 4f);

        // --- Description ---
        descLabel.setText(upgrade.getDescription());
        descLabel.setColor(unlocked ? Color.LIGHT_GRAY : Color.DARK_GRAY);

        // --- Remove old right-side widgets ---
        removeRightSide();

        // --- Add new right-side widget for current state ---
        if (purchased) {
            badgeLabel = new Label("Purchased", new Label.LabelStyle(new BitmapFont(), Color.GREEN));
            badgeLabel.setPosition(
                rowWidth - badgeLabel.getPrefWidth() - 6f,
                (ROW_HEIGHT / 2f) - (badgeLabel.getPrefHeight() / 2f));
            addActor(badgeLabel);

        } else if (unlocked) {
            float btnX = rowWidth - BTN_BUY_W - 6f;

            costLabel = new Label(upgrade.cost + " drops",
                new Label.LabelStyle(new BitmapFont(),
                    canAfford ? new Color(0.9f, 0.85f, 0.3f, 1f) : Color.RED));
            costLabel.setPosition(btnX, BTN_BUY_H + 10f);
            addActor(costLabel);

            buyBtn = new Image(canAfford ? buyBtnTex : buyBtnDisabledTex);
            buyBtn.setSize(BTN_BUY_W, BTN_BUY_H);
            buyBtn.setPosition(btnX, 6f);
            addActor(buyBtn);

            btnLabel = new Label("Buy", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
            btnLabel.setPosition(
                btnX + (BTN_BUY_W / 2f) - (btnLabel.getPrefWidth()  / 2f),
                6f   + (BTN_BUY_H / 2f) - (btnLabel.getPrefHeight() / 2f));
            btnLabel.setTouchable(Touchable.disabled);
            addActor(btnLabel);

            if (canAfford) {
                buyBtn.addListener(new ClickListener() {
                    @Override public void clicked(InputEvent event, float x, float y) {
                        if (gameData.currency < tree.get(index).cost) return;
                        boolean bought = UpgradeTier.tryPurchase(tree, index, gameData);
                        if (bought) callback.onPurchased();
                    }
                });
            }

        } else {
            lockLabel = new Label("Locked", new Label.LabelStyle(new BitmapFont(), Color.DARK_GRAY));
            lockLabel.setPosition(
                rowWidth - lockLabel.getPrefWidth() - 6f,
                (ROW_HEIGHT / 2f) - (lockLabel.getPrefHeight() / 2f));
            addActor(lockLabel);
        }
    }

    /** Removes whichever right-side actors are currently present. */
    private void removeRightSide() {
        if (buyBtn     != null) { buyBtn.remove();     buyBtn     = null; }
        if (costLabel  != null) { costLabel.remove();  costLabel  = null; }
        if (btnLabel   != null) { btnLabel.remove();   btnLabel   = null; }
        if (badgeLabel != null) { badgeLabel.remove(); badgeLabel = null; }
        if (lockLabel  != null) { lockLabel.remove();  lockLabel  = null; }
    }
}
