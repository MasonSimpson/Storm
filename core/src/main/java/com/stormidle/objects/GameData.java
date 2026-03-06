package com.stormidle.objects;

// Holds the game state
// Exports to json upon close to save game data

public class GameData {

    // Economy data
    public int currency = 0;

    // Values that will be affected by upgrades
    public float fallSpeed = 300f;
    public int dropsToFill = 50;
    public float rps = 0; // Rain auto generated per second
    public int currencyGained = 1; // Multiplier applied to bowl conversion payouts
    public float cps = 0f; // Currency generated passively per second (condensation)
    public int maxIdleTime = 1; // How many hours you can be idle and still get progress

    // Tutorial complete boolean
    public boolean tutorialComplete = false;

    // Prestige values
    public int prestigeLevel = 0; // Prestige/cloud level
    public int rainMultiplier = 1; // Multiplies the amount of rain generated from clicking/auto

    // Resets the GameData to default values and sets new prestige levels and rain multipliers
    public void prestige() {
        currency = 0;
        fallSpeed = 300f;
        dropsToFill = 50;
        rps = 0;
        currencyGained = 1;
        cps = 0f;
        maxIdleTime = 1;
        prestigeLevel++;
        rainMultiplier++;
    }
}

