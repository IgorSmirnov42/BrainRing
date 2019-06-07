package ru.spbhse.brainring.logic;

/** Class to store data about user in two-players game */
class UserStatus {
    private boolean alreadyAnswered;
    private String participantId;

    UserStatus(String participantId) {
        this.participantId = participantId;
    }

    void onNewQuestion() {
        alreadyAnswered = false;
    }

    String getParticipantId() {
        return participantId;
    }

    boolean getAlreadyAnswered() {
        return alreadyAnswered;
    }

    void setAlreadyAnswered(boolean status) {
        alreadyAnswered = status;
    }
}
