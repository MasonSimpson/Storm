package com.stormidle.upgrades;

import com.badlogic.gdx.utils.Array;
import com.stormidle.objects.GameData;

// Manages all the "Rain" upgrade trees
// Exposes helpers used by the UI to render them and by GameScreen.java to apply their effects at runtime



public class RainUpgrades {

    // Upgrade trees stored as ordered arrays (index 0 = tier 1, etc)
    public final Array<UpgradeTier> speedTree = new Array<>();
    public final Array<UpgradeTier> bowlTree = new Array<>();

    // Constructor
    // Calls helper functions to build the upgrade trees
    public RainUpgrades() {
        buildSpeedTree();
        buildBowlTree();
    }

    // Helper function that builds the Rainfall Speed upgrade tree
    // effectValue = flat speed bonus added on top of the base FALL_SPEED constant
    private void buildSpeedTree() {
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 1,
            "Fall Speed I",
            "Increases rainfall speed",
            1,
            50f
        ));
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 2,
            "Fall Speed II",
            "Increases rainfall speed",
            20,
            100f
        ));
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 3,
            "Fall Speed III",
            "Increases rainfall speed",
            1000,
            200f
        ));
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 4,
            "Fall Speed IV",
            "Increases rainfall speed",
            100000,
            400f
        ));
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 5,
            "Fall Speed V",
            "Increases rainfall speed",
            1000000000,
            800f
        ));
    }

    // Helper function that builds the Rain Value upgrade tree
    // effectValue - the new DROPS_TO_FILL value (lower value = less drops needed to convert to currency)
    private void buildBowlTree() {
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 1,
            "Rain Value I",
            "Less drops required to convert to currency",
            20,
            45f
        ));
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 2,
            "Rain Value II",
            "Less drops required to convert to currency",
            200,
            40f
        ));
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 3,
            "Rain Value III",
            "Less drops required to convert to currency",
            20000,
            35f
        ));
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 4,
            "Rain Value IV",
            "Less drops required to convert to currency",
            2000000,
            25f
        ));
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 5,
            "Rain Value V",
            "Less drops required to convert to currency",
            2000000000,
            10f
        ));
    }

    // Helper function called whenever the player attempts to purchase an upgrade
    // Returns true if the purchase succeeded, false otherwise
    public boolean tryPurchase(Array<UpgradeTier> tree, int index, GameData gameData) {
        // Quick checker that returns false if the index is out of bounds
        if (index < 0 || index >= tree.size) return false;

        UpgradeTier upgrade = tree.get(index);

        // Returns false if the upgrade has already been purchased (shouldn't be possible to buy, this is just a sanity check)
        if (upgrade.purchased) return false;

        // Checks to make sure the current upgrade is unlocked
        UpgradeTier previous = (index > 0) ? tree.get(index - 1) : null;
        if (!upgrade.isUnlocked(previous)) return false;

        // Checks if user has enough currency
        if (gameData.currency < upgrade.cost) return false;

        // Deduct cost and mark purchased
        gameData.currency -= upgrade.cost;
        upgrade.purchased = true;

        // Apply the upgrade effect immediately after purchase
        applyEffect(upgrade, gameData);

        return true;
    }

    // Applies a purchased upgrade to the user's GameData
    private void applyEffect(UpgradeTier upgrade, GameData gameData) {
        if ("speed".equals(upgrade.tree)) {
            gameData.fallSpeed += upgrade.effectValue;
        } else if ("value".equals(upgrade.tree)) {
            gameData.dropsToFill = (int) upgrade.effectValue;
        }
    }

    // Getter used by the popup builder in GameScreen for convenience
    public UpgradeTier previous(Array<UpgradeTier> tree, int index) {
        return (index > 0) ? tree.get(index - 1) : null;
    }
}
