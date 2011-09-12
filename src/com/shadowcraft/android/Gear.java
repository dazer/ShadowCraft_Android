package com.shadowcraft.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        View headItem = gearLayout.getChildAt(0);
        initItem(headItem, "head");
        View otherItem = gearLayout.getChildAt(4);
        initItem(otherItem, "other");
    }

    private void initItem(View v, String slot) {
        ImageView icon = (ImageView) v.findViewById(R.id.ivItemIcon);
        icon.setImageBitmap(icons.getItemIcon("ability_backstab", 4));
        ImageView gem0 = (ImageView) v.findViewById(R.id.ivGem0);
        gem0.setImageDrawable(icons.getGemIcon("ability_backstab", "YELLOW"));
        ImageView gem1 = (ImageView) v.findViewById(R.id.ivGem1);
        gem1.setImageDrawable(icons.getGemIcon("ability_criticalstrike", "META"));
        ImageView gem2 = (ImageView) v.findViewById(R.id.ivGem2);
        gem2.setImageDrawable(icons.getGemIcon("ability_rogue_cuttothechase", "RED"));
        TextView tv1 = (TextView) v.findViewById(R.id.textView1);
        tv1.setTextColor(icons.COLORS[4]);
        tv1.setText("Name of the Item");
        TextView tv2 = (TextView) v.findViewById(R.id.textView2);
        tv2.setTextColor(icons.COLORS[2]);
        tv2.setTextSize(8);
        tv2.setText("Enchant of the Item");
    }

}
