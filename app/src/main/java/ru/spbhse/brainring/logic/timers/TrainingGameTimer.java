package ru.spbhse.brainring.logic.timers;

import android.os.CountDownTimer;

import ru.spbhse.brainring.logic.TrainingPlayerLogic;
import ru.spbhse.brainring.utils.Constants;

public class TrainingGameTimer extends CountDownTimer {
    private TrainingPlayerLogic logic;

    public TrainingGameTimer(int readingTimeSec, TrainingPlayerLogic logic) {
        super(readingTimeSec * Constants.SECOND, Constants.SECOND);
        this.logic = logic;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (logic.getTimer() == this) {
            long secondsLeft = millisUntilFinished / Constants.SECOND;
            logic.getActivity().setTime(String.valueOf(secondsLeft));
        }
    }

    @Override
    public void onFinish() {
        logic.startAnswer();
    }
}
