package com.shadowcraft.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

public class Gear extends Activity {

    IconHandler icons;
    CharHandler charHandler;
    LinearLayout gearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.items);
        APP app = (APP) getApplication();
        icons = app.getIconHandler();
        charHandler = app.getCharHandler();
        init();
    }

    private void init() {
        gearLayout = (LinearLayout) findViewById(R.id.llGear);
        // TODO populate with real items from the charHandler.
        String[] slots = new String[] {"head", "neck", "shoulder", "back",
                "chest", "wrist", "hands", "waist", "legs", "feet", "finger1",
                "finger2", "trinket1", "trinket2", "mainHand", "offHand", "ranged"};
        for (String slot : slots) {
            ItemView item = new ItemView(this, null);
            item.init(charHandler, icons, slot);
            item.setId(0);
            gearLayout.addView(item);

        }
    }

}
