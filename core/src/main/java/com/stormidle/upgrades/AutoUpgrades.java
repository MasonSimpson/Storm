package com.stormidle.upgrades;

import com.badlogic.gdx.utils.Array;

// Manages the "Auto" upgrade trees
// There is only one upgrade tree here for now, still brainstorming other possible upgrades

public class AutoUpgrades {

    // Upgrade trees stored as indexed arrays (index 0 = tier 1, etc)
    public final Array<UpgradeTier> autoTree = new Array<>();

    // Constructor
    // Calls helper functions to build the upgrade tree
    public AutoUpgrades() {
        buildAutoTree();
    }

    private void buildAutoTree() {
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 1,
            "Rain Generation I",
            "Cloud auto generates 1 drop every 2 seconds",
            5,
            g -> g.rps += 0.5f
        ));
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 2,
            "Rain Generation II",
            "Increases raindrop generation to 1/second",
            50,
            g -> g.rps += 1f
        ));
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 3,
            "Rain Generation III",
            "Increases raindrop generation to 2/second",
            1000,
            g -> g.rps += 2f
        ));
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 4,
            "Rain Generation IV",
            "Increases raindrop generation to 5/second",
            100000,
            g -> g.rps += 5f
        ));
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 5,
            "Rain Generation V",
            "Increases raindrop generation to 10/second",
            1000000000,
            g -> g.rps += 10f
        ));
    }
}
