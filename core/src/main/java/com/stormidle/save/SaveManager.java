package com.stormidle.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.stormidle.objects.GameData;
import com.stormidle.upgrades.UpgradeManager;
import com.stormidle.upgrades.UpgradeTier;
import com.badlogic.gdx.utils.ObjectMap;

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

    // Returned by load() so GameScreen knows whether to show the offline progress popup
    public static class OfflineResult {
        public final boolean hasProgress;
        public final long secondsAway;
        public final int rainfallCurrency;    // Currency earned from auto-rain conversions
        public final int condensationCurrency; // Currency earned from condensation
        public final boolean exceededCap; // Will be true if player was away for longer than max idle time
        public final int maxIdleHours; // Warning message in popup

        public OfflineResult(boolean hasProgress, long secondsAway, int rainfallCurrency, int condensationCurrency,
                             boolean exceededCap, int maxIdleHours) {
            this.hasProgress = hasProgress;
            this.secondsAway = secondsAway;
            this.rainfallCurrency = rainfallCurrency;
            this.condensationCurrency = condensationCurrency;
            this.exceededCap = exceededCap;
            this.maxIdleHours = maxIdleHours;
        }
    }

    // Serializes GameData and all purchased upgrades to JSON and writes to disk
    public static void save(GameData gameData, UpgradeManager upgrades) {
        SaveData data = new SaveData();

        // Copy GameData fields into save container
        data.currency = gameData.currency;
        data.fallSpeed = gameData.fallSpeed;
        data.dropsToFill = gameData.dropsToFill;
        data.rps = gameData.rps;
        data.cps = gameData.cps;
        data.currencyEarned = gameData.currencyGained;
        data.prestigeLevel = gameData.prestigeLevel;
        data.rainMultiplier = gameData.rainMultiplier;
        data.maxIdleTime = gameData.maxIdleTime;
        data.tutorialComplete = gameData.tutorialComplete;
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

        // Ability cooldown timestamps stored as parallel arrays
        for (ObjectMap.Entry<String, Long> entry : upgrades.abilities.getCooldownTimestamps()) {
            data.cooldownIds.add(entry.key);
            data.cooldownTimestamps.add(entry.value);
        }


        Json json = new Json();
        String jsonString = json.prettyPrint(data);

        FileHandle file = Gdx.files.local(SAVE_FILE);
        file.writeString(jsonString, false);

        Gdx.app.log("SaveManager", "Game saved. Prestige: " + data.prestigeLevel
            + " | Max idle: " + data.maxIdleTime + "h");
    }

    // Reads the save file and restores GameData and upgrade purchased states.
    // Returns an OfflineResult describing how much progress the player earned while away.
    // Returns OfflineResult with hasProgress=false if there is no save file or rps is zero.
    public static OfflineResult load(GameData gameData, UpgradeManager upgrades) {
        FileHandle file = Gdx.files.local(SAVE_FILE);
        if (!file.exists()) {
            Gdx.app.log("SaveManager", "No save file found, starting fresh.");
            return new OfflineResult(false, 0, 0, 0, false, gameData.maxIdleTime);
        }

        try {
            Json json = new Json();
            SaveData data = json.fromJson(SaveData.class, file.readString());

            gameData.currency = data.currency;
            gameData.fallSpeed = data.fallSpeed;
            gameData.dropsToFill = data.dropsToFill;
            gameData.rps = data.rps;
            gameData.cps = data.cps;
            gameData.currencyGained = data.currencyEarned;
            gameData.prestigeLevel = data.prestigeLevel;
            gameData.rainMultiplier = data.rainMultiplier;
            gameData.tutorialComplete = data.tutorialComplete;

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

            // Restore ability cooldown timestamps
            ObjectMap<String, Long> cooldowns = new ObjectMap<>();
            for (int i = 0; i < data.cooldownIds.size; i++) {
                cooldowns.put(data.cooldownIds.get(i), data.cooldownTimestamps.get(i));
            }
            upgrades.abilities.setCooldownTimestamps(cooldowns);

            // Calculate offline progress
            OfflineResult result = new OfflineResult(false, 0, 0, 0, false, gameData.maxIdleTime);
            if (data.lastClosedTime > 0) {
                long now            = System.currentTimeMillis() / 1000L;
                long rawSecondsAway = now - data.lastClosedTime;
                long maxSeconds     = gameData.maxIdleTime * 3600L;
                long cappedSeconds  = Math.min(rawSecondsAway, maxSeconds);
                boolean exceededCap = rawSecondsAway > maxSeconds;

                int rainfallCurrency     = 0;
                int condensationCurrency = 0;

                // Earnings calculated against capped time only
                if (gameData.rps > 0) {
                    float totalDrops = gameData.rps * cappedSeconds;
                    int conversions  = (int)(totalDrops / gameData.dropsToFill);
                    rainfallCurrency = conversions * gameData.currencyGained;
                }

                if (gameData.cps > 0) {
                    condensationCurrency = (int)(gameData.cps * cappedSeconds);
                }

                int totalEarned = rainfallCurrency + condensationCurrency;
                // Show popup if there's earnings OR if the cap was exceeded (even if earnings = 0)
                if (totalEarned > 0 || exceededCap) {
                    gameData.currency += totalEarned;
                    result = new OfflineResult(true, rawSecondsAway, rainfallCurrency,
                        condensationCurrency, exceededCap, gameData.maxIdleTime);
                }
            }

            Gdx.app.log("SaveManager", "Game loaded. Currency: " + gameData.currency);
            return result;

        } catch (Exception e) {
            Gdx.app.error("SaveManager", "Failed to load save file: " + e.getMessage());
            return new OfflineResult(false, 0, 0, 0, false, gameData.maxIdleTime);
        }
    }

    // Internal POJO used for JSON serialization.
    // libGDX's Json class needs a public no-arg constructor and public fields.
    public static class SaveData {
        public int currency = 0;
        public float fallSpeed = 300f;
        public int dropsToFill = 50;
        public float rps = 0f;
        public float cps = 0f;
        public int currencyEarned = 1;
        public int prestigeLevel = 0;
        public int rainMultiplier = 1;
        public int maxIdleTime = 1;
        public boolean tutorialComplete = false; // Tutorial only shows once when game is first ran
        public long lastClosedTime = 0L;
        public Array<String> purchasedUpgrades = new Array<>();
        public Array<String> cooldownIds = new Array<>();
        public Array<Long> cooldownTimestamps = new Array<>();
    }
}
