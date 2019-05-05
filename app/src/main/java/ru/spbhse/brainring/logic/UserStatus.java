package ru.spbhse.brainring.logic;

class UserStatus {
    public boolean alreadyAnswered;
    public String opponentAnswer;
    public String participantId;

    public UserStatus(String participantId) {
        this.participantId = participantId;
    }

    public void onNewQuestion() {
        alreadyAnswered = false;
        opponentAnswer = null;
    }
}
