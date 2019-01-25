package ru.spbhse.brainring.logic;

/** Class responsible for one game */
public class LocalNetworkLogic {
    private UserStatus user1;
    private UserStatus user2;
    private GameStatus admin;
    final private int GAME_POINTS;

    public LocalNetworkLogic() {
        // TODO: information about user to construct
        user1 = new UserStatus();
        user2 = new UserStatus();
        admin = new GameStatus();
        GAME_POINTS = 5;
    }

    public void setFasleStart(boolean isFirstUser) {
        (isFirstUser ? user1 : user2).setFalseStart();
    }

    public void givePoints(boolean isFirstUser, int points) {
        (isFirstUser ? user1 : user2).givePoints(points);
    }

    public int[] getCount() {
        return new int[] {user1.getPoints(), user2.getPoints()};
    }

    public boolean allowedToAnswer(boolean isFirstUser) {
        return (isFirstUser ? user1 : user2).allowedToAnswer();
    }

    public void newRound() {
        user1.allowAnswer();
        user2.allowAnswer();
        admin.setFalseStart(true);
    }

    public void startTime() {
        admin.setFalseStart(false);
    }

    public boolean isFalseStart() {
        return admin.getFalseStart();
    }

    public boolean isGameFinished() {
        return user1.getPoints() >= GAME_POINTS || user2.getPoints() >= GAME_POINTS;
    }

    public void buttonPushed(boolean isFirstUser) {
        if (isFalseStart()) {
            setFasleStart(isFirstUser);
            return;
        }
        if (!allowedToAnswer(isFirstUser)) {
            return;
        }
        // TODO : Stop time
        // TODO : Take answer
    }
}
