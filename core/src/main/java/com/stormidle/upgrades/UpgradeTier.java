package com.stormidle.upgrades;

// Represents a single tier in a specific upgrade tree.
// This is just a general class for all tiers, the different upgrade trees have different classes that
// utilize this class.
// Each upgrade belongs to a named tree and has a tier number.
// An upgrade is only purchasable if the previous tier in the same tree has been bought.

public class UpgradeTier {

    // Upgrade tree identifiers - can add more in the future
    public static final String TREE_SPEED = "speed";
    public static final String TREE_VALUE = "value";

    // Data fields
    public String tree; // Which tree this upgrade belongs to
    public int tier; // Tier number within the tree
    public String name; // Short display name for the upgrade
    public String description; // One-line description of the upgrade, shown in popup
    public int cost; // How much currency the upgrade costs
    public float effectValue; // Magnitude of the effect
    public boolean purchased = false; // Whether the player has bought this tier or not

    // Constructor
    public UpgradeTier(String tree, int tier, String name, String description, int cost, float effectValue) {
        this.tree = tree;
        this.tier = tier;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.effectValue = effectValue;
    }

    // Helper function to determine if the upgrade is purchasable
    public boolean isUnlocked(UpgradeTier previousInTree) {
        // Tier 1 is always unlocked, so return true is tier is 1
        if (tier == 1)
            return true;
        // Checks if previous upgrade in tree has been purchased
        return previousInTree != null && previousInTree.purchased;
    }
}
