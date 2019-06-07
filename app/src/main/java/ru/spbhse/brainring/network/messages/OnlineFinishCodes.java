package ru.spbhse.brainring.network.messages;

/** Error codes to write readable message about online game finish in {@code Network} */
public class OnlineFinishCodes {
    /** Too much time no answer on handshake */
    public static final int UNSUCCESSFUL_HANDSHAKE = 1;
    public static final int GAME_FINISHED_CORRECTLY_WON = 2;
    /**
     * Server haven't received messages from opponent for a long time
     * or online opponent was not found
     */
    public static final int SERVER_TIMER_TIMEOUT = 3;
    public static final int GAME_FINISHED_CORRECTLY_LOST = 4;
}
