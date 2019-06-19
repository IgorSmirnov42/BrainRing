package ru.spbhse.brainring.logic.timers;

import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.util.Log;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.logic.LocalGameAdminLogic;
import ru.spbhse.brainring.utils.Constants;

import static java.lang.Math.max;

public class LocalTimer extends CountDownTimer {
    private LocalGameAdminLogic logic;
    private int fault;
    private int sendingCountdown;

    public LocalTimer(int totalTimeSec, int faultMillis, int sendingCountdownSec,
                      LocalGameAdminLogic logic) {
        super(totalTimeSec * Constants.SECOND, Constants.SECOND);
        fault = faultMillis;
        sendingCountdown = sendingCountdownSec;
        this.logic = logic;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (logic.getTimer() == this) {
            logic.getManager().getActivity().showTime(max((millisUntilFinished - fault) / Constants.SECOND, 0));

            if (millisUntilFinished - fault <= sendingCountdown * Constants.SECOND) {
                new Thread(() -> {
                    MediaPlayer player = MediaPlayer.create(logic.getManager().getActivity(),
                            R.raw.countdown);
                    player.setOnCompletionListener(MediaPlayer::release);
                    player.start();
                }).start();
            }
        }
    }

    @Override
    public void onFinish() {
        Log.d(Controller.APP_TAG, "Finish timer");
        if (logic.getTimer() == this) {
            new Thread(() -> {
                MediaPlayer player = MediaPlayer.create(logic.getManager().getActivity(), R.raw.beep);
                player.setOnCompletionListener(MediaPlayer::release);
                player.start();
            }).start();
            logic.newQuestion();
        }
    }
}
