package com.stormidle;

import com.stormidle.screens.IntroScreen;

public class Storm extends com.badlogic.gdx.Game {

    @Override
    public void create() {
        // Game holds the active Screen. setScreen swaps it
        setScreen(new IntroScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
