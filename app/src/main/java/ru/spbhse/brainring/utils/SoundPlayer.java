package ru.spbhse.brainring.utils;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Class to play sounds using {@code SingleThreadExecutor} */
public class SoundPlayer {
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /** Plays sound by it's {@code resid} */
    public void play(Context context, int resid) {
        executor.submit(() -> {
            MediaPlayer player = MediaPlayer.create(context, resid);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        });
    }

    /** Finishes executor. Should be called on every game finish */
    public void finish() {
        executor.shutdown();
    }
}
