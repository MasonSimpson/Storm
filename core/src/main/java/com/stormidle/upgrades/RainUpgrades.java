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
            "Increases rainfall speed by 17%",
            1,
            g -> g.fallSpeed += 50f
        ));
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 2,
            "Fall Speed II",
            "Further increases rainfall speed by 29%",
            20,
            g -> g.fallSpeed += 100f

        ));
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 3,
            "Fall Speed III",
            "Further increases rainfall speed by 45%",
            1000,
            g -> g.fallSpeed += 200f
        ));
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 4,
            "Fall Speed IV",
            "Further increases rainfall speed by 62%",
            100000,
            g -> g.fallSpeed += 400f
        ));
        speedTree.add(new UpgradeTier(
            UpgradeTier.TREE_SPEED, 5,
            "Fall Speed V",
            "Further increases rainfall speed by 76%",
            1000000000,
            g -> g.fallSpeed += 800f
        ));
    }

    // Helper function that builds the Rain Value upgrade tree
    // effectValue - the new DROPS_TO_FILL value (lower value = less drops needed to convert to currency)
    private void buildBowlTree() {
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 1,
            "Rain Value I",
            "Less drops required to convert to currency (50 -> 45)",
            10,
            g -> g.dropsToFill = 45
        ));
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 2,
            "Rain Value II",
            "Less drops required to convert to currency (45 -> 40)",
            200,
            g -> g.dropsToFill = 40
        ));
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 3,
            "Rain Value III",
            "Less drops required to convert to currency (40 -> 35)",
            20000,
            g -> g.dropsToFill = 35
        ));
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 4,
            "Rain Value IV",
            "Less drops required to convert to currency (35 -> 25)",
            2000000,
            g -> g.dropsToFill = 25
        ));
        bowlTree.add(new UpgradeTier(
            UpgradeTier.TREE_VALUE, 5,
            "Rain Value V",
            "Less drops required to convert to currency (25 -> 10)",
            2000000000,
            g -> g.dropsToFill = 10
        ));
    }
}
