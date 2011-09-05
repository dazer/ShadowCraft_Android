package com.shadowcraft.android;

import android.app.Application;

public class APP extends Application {

    private CharHandler charHandler = null;

    /**
     * @return the charHandler
     */
    public CharHandler getCharHandler() {
        return charHandler;
    }

    /**
     * @param charHandler the charHandler to set
     */
    public void setCharHandler(CharHandler charHandler) {
        this.charHandler = charHandler;
    }

    /**
     * @return true if charHandler is set.
     */
    public boolean existsCharHandler() {
        return this.charHandler != null;
    }


}
