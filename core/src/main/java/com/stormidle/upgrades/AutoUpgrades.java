package com.stormidle.upgrades;

import com.badlogic.gdx.utils.Array;

// Manages the "Auto" upgrade trees
// There is only one upgrade tree here for now, still brainstorming other possible upgrades

public class AutoUpgrades {

    // Upgrade trees stored as indexed arrays (index 0 = tier 1, etc)
    public final Array<UpgradeTier> autoTree = new Array<>();
    public final Array<UpgradeTier> idleTimeTree = new Array<>();

    // Constructor
    // Calls helper functions to build the upgrade tree
    public AutoUpgrades(com.stormidle.objects.GameData gameData) {
        buildAutoTree(gameData);
        buildIdleTimeTree(gameData);
    }

    private void buildAutoTree(com.stormidle.objects.GameData gameData) {
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 1,
            "Rain Generation I",
            () -> "Cloud auto generates " + fmt(0.5f, gameData) + " drop(s) per second",
            5,
            g -> g.rps = 0.5f
        ));
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 2,
            "Rain Generation II",
            () -> "Cloud auto generates " + fmt(1f, gameData) + " drop(s) per second",
            50,
            g -> g.rps = 1f
        ));
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 3,
            "Rain Generation III",
            () -> "Cloud auto generates " + fmt(2f, gameData) + " drop(s) per second",
            1000,
            g -> g.rps = 2f
        ));
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 4,
            "Rain Generation IV",
            () -> "Cloud auto generates " + fmt(5f, gameData) + " drop(s) per second",
            100000,
            g -> g.rps = 5f
        ));
        autoTree.add(new UpgradeTier(
            UpgradeTier.TREE_AUTO, 5,
            "Rain Generation V",
            () -> "Cloud auto generates " + fmt(10f, gameData) + " drop(s) per second",
            1000000000,
            g -> g.rps += 10f
        ));
    }

    private void buildIdleTimeTree(com.stormidle.objects.GameData gameData) {
        idleTimeTree.add(new UpgradeTier(
            UpgradeTier.TREE_IDLE, 1,
            "Idle Time I",
            "Increases max idle time to 2 hours",
            20,
            g -> g.maxIdleTime = 2
        ));
        idleTimeTree.add(new UpgradeTier(
            UpgradeTier.TREE_IDLE, 2,
            "Idle Time II",
            "Increases max idle time to 4 hours",
            100,
            g -> g.maxIdleTime = 4
        ));
        idleTimeTree.add(new UpgradeTier(
            UpgradeTier.TREE_IDLE, 3,
            "Idle Time III",
            "Increases max idle time to 8 hours",
            2000,
            g -> g.maxIdleTime = 8
        ));
        idleTimeTree.add(new UpgradeTier(
            UpgradeTier.TREE_IDLE, 4,
            "Idle Time IV",
            "Increases max idle time to 16 hours",
            50000,
            g -> g.maxIdleTime = 16
        ));
        idleTimeTree.add(new UpgradeTier(
            UpgradeTier.TREE_IDLE, 5,
            "Idle Time V",
            "Increases max idle time to 24 hours",
            1000000,
            g -> g.maxIdleTime = 24
        ));
    }

    // Formats the effective drop rate accounting for rainMultiplier
    private static String fmt(float baseRps, com.stormidle.objects.GameData gameData) {
        float effective = baseRps * gameData.rainMultiplier;
        if (effective == (int) effective) return String.valueOf((int) effective);
        return String.format("%.1f", effective);
    }
}
