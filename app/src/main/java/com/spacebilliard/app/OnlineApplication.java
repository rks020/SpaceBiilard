package com.spacebilliard.app;

import android.app.Application;
import com.spacebilliard.app.network.OnlineGameManager;

public class OnlineApplication extends Application {
    private OnlineGameManager gameManager;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setGameManager(OnlineGameManager manager) {
        this.gameManager = manager;
    }

    public OnlineGameManager getGameManager() {
        return gameManager;
    }
}
