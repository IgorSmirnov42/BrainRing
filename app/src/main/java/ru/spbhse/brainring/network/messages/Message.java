package ru.spbhse.brainring.network.messages;

public class Message {
    public static final int ANSWER_IS_READY = 0;
    public static final int ANSWER_IS_WRITTEN = 1;
    public static final int FORBIDDEN_TO_ANSWER = 2;
    public static final int ALLOWED_TO_ANSWER = 3;
    public static final int SENDING_QUESTION = 4;
    public static final int SENDING_INCORRECT_OPPONENT_ANSWER = 5;
    public static final int SENDING_CORRECT_ANSWER_AND_SCORE = 6;
    public static final int OPPONENT_IS_ANSWERING = 7;
    public static final int TIME_LIMIT = 8;
    public static final int FALSE_START = 9;
    public static final int TIME_START = 10;
    public static final int FINISH = 11;
    public static final int HANDSHAKE = 12;
    public static final int CORRECT_ANSWER = 13;
}
