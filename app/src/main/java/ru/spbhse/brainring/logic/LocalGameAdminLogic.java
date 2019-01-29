package ru.spbhse.brainring.logic;

import ru.spbhse.brainring.Controller;
import ru.spbhse.brainring.utils.Question;

public class LocalGameAdminLogic {
    final private int ROUND_TIME = 20;

    private UserStatus user1;
    private UserStatus user2;
    private GameStatus status;

    public LocalGameAdminLogic(UserStatus user1, UserStatus user2) {
        this.user1 = user1;
        this.user2 = user2;
        status = new GameStatus();
    }

    /*
    Admin buttons:
    1. Start round
    2. Start time
    3. Accept answer
    4. Reject answer
     */
    public void onClickStartRound() {
        status.setQuestion(Controller.getQuestion()); // May be parameters will be added
        Controller.showPhase(/* Show start time button and question */);

    }
}
