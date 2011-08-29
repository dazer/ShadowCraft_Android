package com.shadowcraft.android;

import android.app.Application;

public class APP extends Application {

    private CharJSONHandler charHandler = null;

    /**
     * @return the charHandler
     */
    public CharJSONHandler getCharHandler() {
        return charHandler;
    }

    /**
     * @param charHandler the charHandler to set
     */
    public void setCharHandler(CharJSONHandler charHandler) {
        this.charHandler = charHandler;
    }

    /**
     * @return true if charHandler is set.
     */
    public boolean existsCharHandler() {
        return this.charHandler != null;
    }


}
