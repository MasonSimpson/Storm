package com.stormidle.upgrades;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.stormidle.objects.GameData;

// Manages all abilities: definitions, active state, cooldowns, and effects.
// GameScreen calls tick() every frame and tryActivate() when the player buys one.

public class AbilityManager {

    public static final float COOLDOWN_SECONDS   = 20f * 60f; // 20 minutes
    public static final String ID_HURRICANE      = "hurricane";
    public static final String ID_BIG_MONEY      = "big_money";
    public static final String ID_DOWNPOUR       = "downpour";
    public static final String ID_GOLDEN_HOUR    = "golden_hour";

    public final Array<AbilityDefinition> abilities = new Array<>();

    private String activeAbilityId      = null;
    private float  activeTimeRemaining  = 0f;

    // Persisted cooldown timestamps: ability id -> unix timestamp of last activation
    private final ObjectMap<String, Long> lastUsedTimestamps = new ObjectMap<>();

    // Backed-up GameData values so we can cleanly restore them on expiry
    private float savedRps            = -1f;
    private float savedFallSpeed      = -1f;
    private float savedCps            = -1f;
    private int   savedCurrencyGained = -1;

    public AbilityManager() {
        abilities.add(new AbilityDefinition(ID_HURRICANE,   "Hurricane",    "All rain sources doubled for 15 seconds",          200,  15f, COOLDOWN_SECONDS));
        abilities.add(new AbilityDefinition(ID_BIG_MONEY,   "Big Money",    "Bowl conversions tripled for 60 seconds",          500,  60f, COOLDOWN_SECONDS));
        abilities.add(new AbilityDefinition(ID_DOWNPOUR,    "Downpour",     "Raindrop fall speed doubled for 30 seconds",       150,  30f, COOLDOWN_SECONDS));
        abilities.add(new AbilityDefinition(ID_GOLDEN_HOUR, "Golden Hour",  "Condensation rate tripled for 45 seconds",         300,  45f, COOLDOWN_SECONDS));
    }

    public AbilityDefinition getActiveAbility() {
        if (activeAbilityId == null) return null;
        for (AbilityDefinition def : abilities) {
            if (def.id.equals(activeAbilityId)) return def;
        }
        return null;
    }

    public float getActiveTimeRemaining() { return activeTimeRemaining; }
    public boolean isAnyAbilityActive()   { return activeAbilityId != null; }

    public boolean isOnCooldown(String id) {
        if (!lastUsedTimestamps.containsKey(id)) return false;
        long elapsed = System.currentTimeMillis() / 1000L - lastUsedTimestamps.get(id);
        return elapsed < (long) COOLDOWN_SECONDS;
    }

    public float getCooldownRemaining(String id) {
        if (!lastUsedTimestamps.containsKey(id)) return 0f;
        long elapsed = System.currentTimeMillis() / 1000L - lastUsedTimestamps.get(id);
        return Math.max(0f, COOLDOWN_SECONDS - elapsed);
    }

    public void setCooldownTimestamps(ObjectMap<String, Long> saved) {
        lastUsedTimestamps.clear();
        lastUsedTimestamps.putAll(saved);
    }

    public ObjectMap<String, Long> getCooldownTimestamps() { return lastUsedTimestamps; }

    // Attempts to activate ability. Returns false if blocked (another active, on cooldown, can't afford).
    public boolean tryActivate(String abilityId, GameData gameData) {
        if (isAnyAbilityActive())    return false;
        if (isOnCooldown(abilityId)) return false;

        AbilityDefinition def = null;
        for (AbilityDefinition d : abilities) { if (d.id.equals(abilityId)) { def = d; break; } }
        if (def == null || gameData.currency < def.cost) return false;

        gameData.currency -= def.cost;

        // Back up values before modifying them
        savedRps            = gameData.rps;
        savedFallSpeed      = gameData.fallSpeed;
        savedCps            = gameData.cps;
        savedCurrencyGained = gameData.currencyGained;

        applyEffect(abilityId, gameData);

        activeAbilityId     = abilityId;
        activeTimeRemaining = def.duration;
        lastUsedTimestamps.put(abilityId, System.currentTimeMillis() / 1000L);
        return true;
    }

    // Called every frame from GameScreen to tick the active ability timer
    public void tick(float delta, GameData gameData) {
        if (activeAbilityId == null) return;
        activeTimeRemaining -= delta;
        if (activeTimeRemaining <= 0f) expireActive(gameData);
    }

    private void applyEffect(String id, GameData gameData) {
        switch (id) {
            case ID_HURRICANE:   break; // This effect will be handled in GameScreen
            case ID_BIG_MONEY:   gameData.currencyGained *= 3;  break;
            case ID_DOWNPOUR:    gameData.fallSpeed      *= 2f; break;
            case ID_GOLDEN_HOUR: gameData.cps            *= 3f; break;
        }
    }

    private void expireActive(GameData gameData) {
        switch (activeAbilityId) {
            case ID_HURRICANE:   break; // Handled in GameScreen
            case ID_BIG_MONEY:   gameData.currencyGained = savedCurrencyGained; break;
            case ID_DOWNPOUR:    gameData.fallSpeed       = savedFallSpeed;      break;
            case ID_GOLDEN_HOUR: gameData.cps             = savedCps;           break;
        }
        savedRps = savedFallSpeed = savedCps = -1f;
        savedCurrencyGained = -1;
        activeAbilityId     = null;
        activeTimeRemaining = 0f;
    }

    public boolean isHurricaneActive() {
        return ID_HURRICANE.equals(activeAbilityId);
    }
}
