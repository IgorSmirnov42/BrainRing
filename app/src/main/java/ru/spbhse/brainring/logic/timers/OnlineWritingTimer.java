package ru.spbhse.brainring.logic.timers;

import android.os.CountDownTimer;

import ru.spbhse.brainring.logic.OnlineGameUserLogic;
import ru.spbhse.brainring.utils.Constants;

/**
 * Timer for writing answer in online game.
 * If finishes without cancel takes what user had written
 */
public class OnlineWritingTimer extends CountDownTimer {
    private OnlineGameUserLogic logic;

    public OnlineWritingTimer(int timeToWriteAnswerSec, OnlineGameUserLogic logic) {
        super(timeToWriteAnswerSec * Constants.SECOND,
                timeToWriteAnswerSec * Constants.SECOND);
        this.logic = logic;
    }

    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
        if (logic.getTimer() == this) {
            logic.answerIsWritten(logic.getManager().getActivity().getWhatWritten());
        }
    }
}
