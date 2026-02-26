package com.stormidle.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
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

    // Max time offline that counts toward progress — hardcoded to 1 hour for now
    // TODO: Hook into an upgrade that increases this cap
    private static final long MAX_OFFLINE_SECONDS = 3600L;

    // Returned by load() so GameScreen knows whether to show the offline progress popup
    public static class OfflineResult {
        public final boolean hasProgress;
        public final long secondsAway;
        public final int rainfallCurrency;    // Currency earned from auto-rain conversions
        public final int condensationCurrency; // Currency earned from condensation

        public OfflineResult(boolean hasProgress, long secondsAway, int rainfallCurrency, int condensationCurrency) {
            this.hasProgress           = hasProgress;
            this.secondsAway           = secondsAway;
            this.rainfallCurrency      = rainfallCurrency;
            this.condensationCurrency  = condensationCurrency;
        }
    }

    // Serializes GameData and all purchased upgrades to JSON and writes to disk
    public static void save(GameData gameData, UpgradeManager upgrades) {
        SaveData data = new SaveData();

        // Copy GameData fields into save container
        data.currency           = gameData.currency;
        data.fallSpeed          = gameData.fallSpeed;
        data.dropsToFill        = gameData.dropsToFill;
        data.rps                = gameData.rps;
        data.cps                = gameData.cps;
        data.currencyEarned     = gameData.currencyGained;
        data.lastClosedTime     = System.currentTimeMillis() / 1000L;

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
    // Returns an OfflineResult describing how much progress the player earned while away.
    // Returns OfflineResult with hasProgress=false if there is no save file or rps is zero.
    public static OfflineResult load(GameData gameData, UpgradeManager upgrades) {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) {
            Gdx.app.log("SaveManager", "No save file found, starting fresh.");
            return new OfflineResult(false, 0, 0,0);
        }

        try {
            Json json = new Json();
            SaveData data = json.fromJson(SaveData.class, file.readString());

            gameData.currency           = data.currency;
            gameData.fallSpeed          = data.fallSpeed;
            gameData.dropsToFill        = data.dropsToFill;
            gameData.rps                = data.rps;
            gameData.cps                = data.cps;
            gameData.currencyGained     = data.currencyEarned;

            // Restore purchased upgrade states
            for (Array<UpgradeTier> tree : upgrades.getAllTrees()) {
                for (UpgradeTier tier : tree) {
                    String id = tier.tree + "_" + tier.tier;
                    if (data.purchasedUpgrades.contains(id, false)) {
                        tier.purchased = true;
                        // GameData values from save already include all upgrade effects,
                        // so we only mark purchased = true for UI state without re-applying.
                    }
                }
            }

            // Calculate offline progress
            OfflineResult result = new OfflineResult(false, 0, 0, 0);
            if (data.lastClosedTime > 0) {
                long now            = System.currentTimeMillis() / 1000L;
                long rawSecondsAway = now - data.lastClosedTime;
                long secondsAway    = Math.min(rawSecondsAway, MAX_OFFLINE_SECONDS);

                int rainfallCurrency     = 0;
                int condensationCurrency = 0;

                // Rainfall offline earnings — multiplier applied to conversions
                if (gameData.rps > 0) {
                    float totalDrops  = gameData.rps * secondsAway;
                    int conversions   = (int)(totalDrops / gameData.dropsToFill);
                    rainfallCurrency  = conversions * gameData.currencyGained;
                }

                // Condensation offline earnings — no multiplier
                if (gameData.cps > 0) {
                    condensationCurrency = (int)(gameData.cps * secondsAway);
                }

                int totalEarned = rainfallCurrency + condensationCurrency;
                if (totalEarned > 0) {
                    gameData.currency += totalEarned;
                    result = new OfflineResult(true, secondsAway, rainfallCurrency, condensationCurrency);
                }
            }

            Gdx.app.log("SaveManager", "Game loaded. Currency: " + gameData.currency);
            return result;

        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to load save file: " + e.getMessage());
            return new OfflineResult(false, 0, 0, 0);
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
        public int currency                    = 0;
        public float fallSpeed                 = 300f;
        public int dropsToFill                 = 50;
        public float rps                       = 0f;
        public float cps                       = 0f;
        public int currencyEarned              = 1;
        public long lastClosedTime             = 0L;
        public Array<String> purchasedUpgrades = new Array<>();
    }
}
