package com.stormidle.upgrades;

// Describes a single ability — its identity, cost, duration, and cooldown.
// AbilityManager holds instances of these and applies their effects at runtime.

public class AbilityDefinition {

    public final String id;          // Unique identifier used for save/load
    public final String name;        // Display name shown in the popup
    public final String description; // One-line description shown in the popup
    public final int cost;           // Currency cost to activate
    public final float duration;     // How long the effect lasts in seconds
    public final float cooldown;     // How long before it can be bought again in seconds

    public AbilityDefinition(String id, String name, String description,
                             int cost, float duration, float cooldown) {
        this.id          = id;
        this.name        = name;
        this.description = description;
        this.cost        = cost;
        this.duration    = duration;
        this.cooldown    = cooldown;
    }
}
