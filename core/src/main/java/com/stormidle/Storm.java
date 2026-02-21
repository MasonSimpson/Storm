package com.stormidle;

import com.stormidle.screens.IntroScreen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.Gdx;

public class Storm extends com.badlogic.gdx.Game {

    public Music music;

    @Override
    public void create() {
        // Start playing music
        music = Gdx.audio.newMusic(Gdx.files.internal("game_music.wav"));
        music.play();
        music.setLooping(true);
        // Game holds the active Screen. setScreen swaps it
        setScreen(new IntroScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
