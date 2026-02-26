package com.stormidle.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.stormidle.objects.GameData;
import com.stormidle.upgrades.UpgradeManager;
import com.stormidle.upgrades.UpgradeTier;

// Handles saving and loading all game state to/from a JSON file.
// Save format:
// {
//   "currency": 100,
//   "fallSpeed": 350.0,
//   "dropsToFill": 45,
//   "rps": 0.5,
//   "purchasedUpgrades": ["speed_1", "value_1", "auto_1"]
// }

public class SaveManager {

    private static final String SAVE_FILE = "stormidle_save.json";

    // Returns true if a save file exists
    public static boolean exists() {
        return Gdx.files.local(SAVE_FILE).exists();
    }

    // Max time offline that counts towards progress
    // TODO: implement an upgrade to increase this cap
    private static final long MAX_OFFLINE_SECONDS = 3600L;

    // Returned by load() so GameScreen knows whether to show the offline progress popup
    public static class OfflineResult {
        public boolean hasProgress; // True if the player was logged off for enough time to earn currency
        public long secondsAway; // How long the player was gone (capped at MAX_OFFLINE_SECONDS
        public int currencyEarned; // How much currency they earned while gone

        public OfflineResult(boolean hasProgress, long secondsAway, int currencyEarned) {
            this.hasProgress = hasProgress;
            this.secondsAway = secondsAway;
            this.currencyEarned = currencyEarned;
        }
    }

    // Serializes GameData and all purchased upgrades to JSON and writes to disk
    public static void save(GameData gameData, UpgradeManager upgrades) {
        SaveData data = new SaveData();

        // Copy GameData fields into save container
        data.currency    = gameData.currency;
        data.fallSpeed   = gameData.fallSpeed;
        data.dropsToFill = gameData.dropsToFill;
        data.rps         = gameData.rps;
        data.lastClosedTime = System.currentTimeMillis() / 1000L;

        // Collect IDs of all purchased upgrades across every tree
        for (Array<UpgradeTier> tree : upgrades.getAllTrees()) {
            for (UpgradeTier tier : tree) {
                if (tier.purchased) {
                    // ID format: "tree_tier" e.g. "speed_1", "auto_3"
                    data.purchasedUpgrades.add(tier.tree + "_" + tier.tier);
                }
            }
        }

        Json json = new Json();
        String jsonString = json.prettyPrint(data);

        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(jsonString, false);

        Gdx.app.log("SaveManager", "Game saved. Purchased upgrades: " + data.purchasedUpgrades.size);
    }

    // Reads the save file and restores GameData and upgrade purchased states.
    // Re-applies each purchased upgrade's effect so GameData values are correct on load.
    public static OfflineResult load(GameData gameData, UpgradeManager upgrades) {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) {
            Gdx.app.log("SaveManager", "No save file found, starting fresh.");
            return new OfflineResult(false, 0, 0);
        }

        try {
            Json json = new Json();
            SaveData data = json.fromJson(SaveData.class, file.readString());

            // Restore GameData fields
            gameData.currency    = data.currency;
            gameData.fallSpeed   = data.fallSpeed;
            gameData.dropsToFill = data.dropsToFill;
            gameData.rps         = data.rps;

            // Restore purchased upgrade states and re-apply their effects
            for (Array<UpgradeTier> tree : upgrades.getAllTrees()) {
                for (UpgradeTier tier : tree) {
                    String id = tier.tree + "_" + tier.tier;
                    if (data.purchasedUpgrades.contains(id, false)) {
                        tier.purchased = true;
                        // Re-apply the effect so GameData reflects all purchased upgrades.
                        // Note: GameData values loaded from save already include these effects,
                        // so we mark purchased without calling applyEffect again to avoid doubling.
                        // purchased = true is enough for UI state — effects are already in GameData.
                    }
                }
            }

            // Calculate offline progress
            OfflineResult result = new OfflineResult(false, 0, 0);
            if (data.lastClosedTime > 0 && gameData.rps > 0) {
                long now = System.currentTimeMillis() / 1000L;
                long rawSecondsAway = now - data.lastClosedTime;
                // Cap at max offline time
                long secondsAway = Math.min(rawSecondsAway, MAX_OFFLINE_SECONDS);

                // Calculate drops generated
                float totalDrops = gameData.rps * secondsAway;
                int currencyEarned = (int) (totalDrops / gameData.dropsToFill);

                if (currencyEarned > 0) {
                    gameData.currency += currencyEarned;
                    result = new OfflineResult(true, secondsAway, currencyEarned);
                }
            }

            Gdx.app.log("SaveManager", "Game loaded. Currency: " + gameData.currency);
            return result;

        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to load save file: " + e.getMessage());
            return new OfflineResult(false, 0, 0);
        }
    }

    // Deletes the save file. Call this on prestige reset.
    public static void deleteSave() {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (file.exists()) {
            file.delete();
            Gdx.app.log("SaveManager", "Save file deleted.");
        }
    }

    // Internal POJO used for JSON serialization.
    // libGDX's Json class needs a public no-arg constructor and public fields.
    public static class SaveData {
        public int currency        = 0;
        public float fallSpeed     = 300f;
        public int dropsToFill     = 50;
        public float rps           = 0f;
        public long lastClosedTime = 0L;
        public Array<String> purchasedUpgrades = new Array<>();
    }
}
