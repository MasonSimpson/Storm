package com.stormidle.upgrades;

import com.badlogic.gdx.utils.Array;
import com.stormidle.objects.GameData;
import com.stormidle.objects.Rain;

// Central container for all upgrade trees.
// Pass this around instead of individual tree classes so save/load and other systems
// only need one reference to access everything.

public class UpgradeManager {

    public RainUpgrades rain;
    public AutoUpgrades auto;
    public EconUpgrades econ;
    public AbilityManager abilities;

    public UpgradeManager(GameData gameData) {
        rain = new RainUpgrades();
        auto = new AutoUpgrades(gameData);
        econ = new EconUpgrades();
        abilities = new AbilityManager();
    }

    // Called on prestige to reset all upgrade trees
    public void reset(GameData gameData) {
        rain = new RainUpgrades();
        auto = new AutoUpgrades(gameData);
        econ = new EconUpgrades();
        abilities = new AbilityManager();
    }

    // Returns all trees across all categories as a flat list of arrays.
    // Used by SaveManager to iterate every tree without knowing about each one individually.
    public Array<Array<UpgradeTier>> getAllTrees() {
        Array<Array<UpgradeTier>> all = new Array<>();
        // Rain trees
        all.add(rain.speedTree);
        all.add(rain.bowlTree);
        // Auto trees
        all.add(auto.autoTree);
        all.add(auto.idleTimeTree);
        // Econ trees
        all.add(econ.conversionTree);
        all.add(econ.condensationTree);
        // Add future trees here
        return all;
    }

}
