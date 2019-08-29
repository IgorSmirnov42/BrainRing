package ru.spbhse.brainring.logic.timers;

import android.os.CountDownTimer;
import android.util.Log;

import ru.spbhse.brainring.R;
import ru.spbhse.brainring.logic.LocalGameAdminLogic;
import ru.spbhse.brainring.utils.Constants;

import static java.lang.Math.max;

/**
 * Timer used in local game to countdown time on question
 * If finishes without cancel starts new question
 */
public class LocalTimer extends CountDownTimer {
    private LocalGameAdminLogic logic;
    private int fault;
    private int sendingCountdown;

    public LocalTimer(int totalTimeSec, int faultMillis, int sendingCountdownSec,
                      LocalGameAdminLogic logic) {
        super(totalTimeSec * Constants.SECOND + faultMillis, Constants.SECOND);
        fault = faultMillis;
        sendingCountdown = sendingCountdownSec;
        this.logic = logic;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (logic.getTimer() == this) {
            logic.getManager().getActivity().showTime(max((millisUntilFinished - fault) / Constants.SECOND, 0));

            if (millisUntilFinished - fault <= sendingCountdown * Constants.SECOND) {
                logic.getPlayer().play(logic.getManager().getActivity(), R.raw.countdown);
            }
        }
    }

    @Override
    public void onFinish() {
        Log.d(Constants.APP_TAG, "Finish timer");
        if (logic.getTimer() == this) {
            logic.getPlayer().play(logic.getManager().getActivity(), R.raw.beep);
            logic.newQuestion();
        }
    }
}
