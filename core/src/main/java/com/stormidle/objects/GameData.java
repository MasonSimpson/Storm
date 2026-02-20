package com.stormidle.objects;

// Holds the game state
// Exports to json upon close to save game data

public class GameData {

    // Economy data
    public int currency = 0;

    // Rain upgrade effects
    public float fallSpeed = 300f;
    public int dropsToFill = 50;

    // TODO: Add more upgrades and other data that needs to be saved

}
