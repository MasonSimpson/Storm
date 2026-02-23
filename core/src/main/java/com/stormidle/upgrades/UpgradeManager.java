package com.stormidle.upgrades;

import com.badlogic.gdx.utils.Array;

// Central container for all upgrade trees.
// Pass this around instead of individual tree classes so save/load and other systems
// only need one reference to access everything.

public class UpgradeManager {

    public final RainUpgrades rain = new RainUpgrades();
    public final AutoUpgrades auto = new AutoUpgrades();
    // Add future upgrade trees here (e.g. econ, ult)

    // Returns all trees across all categories as a flat list of arrays.
    // Used by SaveManager to iterate every tree without knowing about each one individually.
    public Array<Array<UpgradeTier>> getAllTrees() {
        Array<Array<UpgradeTier>> all = new Array<>();
        // Rain trees
        all.add(rain.speedTree);
        all.add(rain.bowlTree);
        // Auto trees
        all.add(auto.autoTree);
        // Add future trees here
        return all;
    }
}
