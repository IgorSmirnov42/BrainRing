package ru.spbhse.brainring.network.messages;

/** Codes of messages used in {@code Network} and {@code LocalNetwork} */
public class MessageCodes {
    /** From player to server. Indicates that player pushed button */
    public static final int ANSWER_IS_READY = 0;
    /** From answering player to server. Indicates that player wrote an answer */
    public static final int ANSWER_IS_WRITTEN = 1;
    /** From server to player. Forbiddance to answer */
    public static final int FORBIDDEN_TO_ANSWER = 2;
    /** From server to player. Allowance to answer */
    public static final int ALLOWED_TO_ANSWER = 3;
    /** From server to player. Sending question data */
    public static final int SENDING_QUESTION = 4;
    /** From server to player. On opponent's incorrect answer */
    public static final int SENDING_INCORRECT_OPPONENT_ANSWER = 5;
    /** From server to player. Finishing round. Showing answer */
    public static final int SENDING_CORRECT_ANSWER_AND_SCORE = 6;
    /** From server to player. On opponent's pushing */
    public static final int OPPONENT_IS_ANSWERING = 7;
    /** From player to server. Time is ran out */
    public static final int TIME_LIMIT = 8;
    /** From player to server. Player pushed button before signal */
    public static final int FALSE_START = 9;
    /** From server to player. Allowance to push button */
    public static final int TIME_START = 10;
    /** From server to player. Game's finish reason */
    public static final int FINISH = 11;
    /** From player to server. Player received question data */
    public static final int HANDSHAKE = 12;
    /** From server to player. Written answer is correct */
    public static final int CORRECT_ANSWER = 13;
    /** From player to server. Ready to get next question */
    public static final int READY_FOR_QUESTION = 14;
    // for local game only
    ///** From server to player. First handshake to determine color */
    //public static final int INITIAL_HANDSHAKE = 15;
    /** From player to server. Player has green table */
    public static final int I_AM_GREEN = 16;
    /** From player to server. Player has red table */
    public static final int I_AM_RED = 17;
}
