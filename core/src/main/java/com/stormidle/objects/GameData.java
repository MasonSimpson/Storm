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

    // TODO: Add more upgrades and other data that needs to be saved

}
