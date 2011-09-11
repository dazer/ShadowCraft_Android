package com.shadowcraft.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Gear extends Activity {

    IconHandler icons;
    LinearLayout gearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items);
        APP app = (APP) getApplication();
        icons = app.getIconHandler();
        init();
    }

    private void init() {
        gearLayout = (LinearLayout) findViewById(R.id.llGear);
        View headItem = gearLayout.getChildAt(0);
        ImageView icon = (ImageView) headItem.findViewById(R.id.ivItemIcon);
        icon.setImageBitmap(icons.getItemIcon(R.drawable.ability_backstab, icons.COLORS[4]));
    }
}
