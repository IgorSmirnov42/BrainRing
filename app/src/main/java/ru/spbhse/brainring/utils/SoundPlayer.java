package ru.spbhse.brainring.utils;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundPlayer {
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public void play(Context context, int resid) {
        executor.submit(() -> {
            MediaPlayer player = MediaPlayer.create(context, resid);
            player.setOnCompletionListener(MediaPlayer::release);
            player.start();
        });
    }

    public void finish() {
        executor.shutdown();
    }
}
