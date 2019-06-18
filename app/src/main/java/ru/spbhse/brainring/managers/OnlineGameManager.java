package ru.spbhse.brainring.managers;

import android.util.Log;

import ru.spbhse.brainring.controllers.Controller;
import ru.spbhse.brainring.logic.OnlineGameAdminLogic;
import ru.spbhse.brainring.logic.OnlineGameUserLogic;
import ru.spbhse.brainring.messageProcessing.OnlineMessageProcessor;
import ru.spbhse.brainring.network.Network;
import ru.spbhse.brainring.ui.OnlineGameActivity;

public class OnlineGameManager {
    private Network network;
    private OnlineGameAdminLogic adminLogic;
    private OnlineGameUserLogic userLogic;
    private OnlineGameActivity activity;
    private OnlineMessageProcessor messageProcessor;

    public OnlineGameManager(OnlineGameActivity activity) {
        network = new Network(this);
        userLogic = new OnlineGameUserLogic(this);
        this.activity = activity;
        messageProcessor = new OnlineMessageProcessor(this);
    }

    public Network getNetwork() {
        return network;
    }

    public OnlineGameAdminLogic getAdminLogic() {
        return adminLogic;
    }

    public OnlineGameUserLogic getUserLogic() {
        return userLogic;
    }

    public OnlineGameActivity getActivity() {
        return activity;
    }

    public OnlineMessageProcessor getMessageProcessor() {
        return messageProcessor;
    }

    public void startOnlineGame() {
        adminLogic = new OnlineGameAdminLogic(this);
        adminLogic.newQuestion();
    }

    /** Finishes current game */
    public void finishOnlineGame() {
        if (adminLogic != null) {
            Log.d(Controller.APP_TAG,"Clearing admin logic");
            adminLogic.finishGame();
        }
        Log.d(Controller.APP_TAG,"Clearing user logic");
        userLogic.finishGame();

        Log.d(Controller.APP_TAG,"Clearing network");
        network.finish();
    }
}
