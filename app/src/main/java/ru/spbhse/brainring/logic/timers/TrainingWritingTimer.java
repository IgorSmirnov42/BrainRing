package ru.spbhse.brainring.logic.timers;

import android.os.CountDownTimer;

import ru.spbhse.brainring.logic.TrainingPlayerLogic;
import ru.spbhse.brainring.utils.Constants;

public class TrainingWritingTimer extends CountDownTimer {
    private TrainingPlayerLogic logic;
    private int timeToWrite;

    public TrainingWritingTimer(int writingTimeSec, TrainingPlayerLogic logic) {
        super(writingTimeSec * Constants.SECOND, Constants.SECOND);
        this.logic = logic;
        timeToWrite = writingTimeSec;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (logic.getTimer() == this) {
            if (millisUntilFinished <= timeToWrite * Constants.SECOND / 2) {
                logic.onReceivingTick(millisUntilFinished / Constants.SECOND);
            }
        }
    }

    @Override
    public void onFinish() {
        logic.answerIsWritten(logic.getActivity().getWhatWritten());
    }
}
