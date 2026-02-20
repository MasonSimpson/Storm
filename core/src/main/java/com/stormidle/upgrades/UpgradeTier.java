package com.stormidle.upgrades;

// Represents a single tier in a specific upgrade tree.
// This is just a general class for all tiers, the different upgrade trees have different classes that
// utilize this class.
// Each upgrade belongs to a named tree and has a tier number.
// An upgrade is only purchasable if the previous tier in the same tree has been bought.

import com.stormidle.objects.GameData;
import com.badlogic.gdx.utils.Array;
import java.util.function.Consumer;

public class UpgradeTier {
    private final Consumer<GameData> effect;

    // Upgrade tree identifiers - can add more in the future
    public static final String TREE_SPEED = "speed";
    public static final String TREE_VALUE = "value";
    public static final String TREE_AUTO = "auto";

    // Data fields
    public String tree; // Which tree this upgrade belongs to
    public int tier; // Tier number within the tree
    public String name; // Short display name for the upgrade
    public String description; // One-line description of the upgrade, shown in popup
    public int cost; // How much currency the upgrade costs
    public boolean purchased = false; // Whether the player has bought this tier or not

    // Constructor
    public UpgradeTier(String tree, int tier, String name, String description, int cost, Consumer<GameData> effect) {
        this.tree = tree;
        this.tier = tier;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.effect = effect;
    }

    // Each upgrade tree subclass has a different use case for applyEffect
    // So leaving this abstract and implementing it in each upgrade tree class
    public void applyEffect(GameData gameData) {
        effect.accept(gameData);
    }

    // Helper function to determine if the upgrade is purchasable
    public boolean isUnlocked(UpgradeTier previousInTree) {
        // Tier 1 is always unlocked, so return true is tier is 1
        if (tier == 1)
            return true;
        // Checks if previous upgrade in tree has been purchased
        return previousInTree != null && previousInTree.purchased;
    }

    // Attempts to purchase the upgrade at the given index in the tree
    // Returns true if successful, false otherwise
    public static boolean tryPurchase(Array<UpgradeTier> tree, int index, GameData gameData) {
        if (index < 0 || index >= tree.size) return false;

        UpgradeTier upgrade = tree.get(index);

        if (upgrade.purchased) return false;

        UpgradeTier prev = previous(tree, index);
        if (!upgrade.isUnlocked(prev)) return false;

        if (gameData.currency < upgrade.cost) return false;

        gameData.currency -= upgrade.cost;
        upgrade.purchased  = true;
        upgrade.applyEffect(gameData);

        return true;
    }

    // Returns the previous tier in the tree, or null if this is tier 1
    public static UpgradeTier previous(Array<UpgradeTier> tree, int index) {
        return (index > 0) ? tree.get(index - 1) : null;
    }
}
