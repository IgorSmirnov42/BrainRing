package ru.spbhse.brainring.logic.timers;

import android.os.CountDownTimer;
import android.util.Log;

import ru.spbhse.brainring.logic.OnlineGameUserLogic;
import ru.spbhse.brainring.utils.Constants;

public class OnlineGameTimer extends CountDownTimer {
    private OnlineGameUserLogic logic;
    private int sendingCountdown;
    private int roundNumber;

    public OnlineGameTimer(int timeSec, int sendingCountdownSec, int roundNumber,
                           OnlineGameUserLogic logic) {
        super(timeSec * Constants.SECOND, Constants.SECOND);
        this.logic = logic;
        sendingCountdown = sendingCountdownSec * Constants.SECOND;
        this.roundNumber = roundNumber;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (logic.getTimer() == this) {
            if (millisUntilFinished <= sendingCountdown) {
                logic.onReceivingTick(millisUntilFinished / Constants.SECOND);
            }
        }
    }

    @Override
    public void onFinish() {
        if (logic.getTimer() == this) {
            Log.d(Constants.APP_TAG, "Finish first timer");
            logic.sendTimeLimitedAnswer(roundNumber);
        }
    }
}
