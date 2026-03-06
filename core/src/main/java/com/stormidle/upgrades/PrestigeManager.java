package com.stormidle.upgrades;

import com.stormidle.objects.GameData;

public class PrestigeManager {

    // Thresholds for being able to prestige
    // Exponential curve, 6 tiers
    public static final int[] THRESHOLDS = {
        100,
        5000,
        25000,
        125000,
        625000,
        3125000
    };

    public static final int MAX_LEVEL = THRESHOLDS.length;

    // Returns the currency threshold for the next prestige, or -1 if already maxed
    public static int nextThreshold(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) return -1;
        return THRESHOLDS[currentLevel];
    }

    // True if the player has enough currency to prestige right now
    public static boolean canPrestige(int currentLevel, int currency) {
        int threshold = nextThreshold(currentLevel);
        return threshold != -1 && currency >= threshold;
    }


    // Executes a prestige: resets GameData and all upgrades, then increments
    // the prestige level and applies the cloud bonus.
    public static void doPrestige(GameData gameData, UpgradeManager upgrades) {
        gameData.prestige();
        upgrades.reset(gameData);
    }
}
