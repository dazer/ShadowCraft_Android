package com.shadowcraft.android;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.drawable.Drawable;
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
        String[] slots = new String[] {"head", "neck", "shoulder", "back",
                "chest", "wrist", "hands", "waist", "legs", "feet", "finger1",
                "finger2", "trinket1", "trinket2", "mainHand", "offHand", "ranged"};
        int counter = 0;
        for (String slot : slots) {
            View item = gearLayout.getChildAt(counter);
            initItem(item, slot);
            counter++;
        }
    }

    @SuppressWarnings("unchecked")
    private void initItem(View v, String slot) {
        HashMap<String, Object> itemData = charHandler.itemCache().get(slot);
        HashMap<String, Object> charData = charHandler.charItems().get(slot);
        //TODO cache these two fields? they might change during runtime.
        Map<Integer, HashMap<String, Object>> gemsData = charHandler.gemCache();
        Map<Integer, HashMap<String, Object>> enchData = charHandler.enchCache();
        int[] sockets = new int[] {R.id.ivGem0, R.id.ivGem1, R.id.ivGem2};

        ImageView icon = (ImageView) v.findViewById(R.id.ivItemIcon);
        icon.setImageBitmap(icons.getItemIcon("ability_backstab", 4));
        List<String> socketsInItem = (List<String>) itemData.get("sockets");
        for (int i = 0; i<=2; i++) {
            ImageView gemView = (ImageView) v.findViewById(sockets[i]);
            if (socketsInItem.size() <= i) {
                gemView.setVisibility(View.INVISIBLE); //TODO prismatic
                continue;
            }
            String socketType = socketsInItem.get(i);
            int gemId = (Integer) charData.get("gem"+i);
            String gemIconName = (String) gemsData.get(gemId).get("icon");
            Drawable gemIcon = icons.getGemIcon(gemIconName, socketType);
            gemView.setImageDrawable(gemIcon);
        }
        TextView tv1 = (TextView) v.findViewById(R.id.textView1);
        tv1.setTextColor(icons.COLORS[(Integer)itemData.get("quality")]);
        tv1.setText((String)itemData.get("name"));
        TextView tv2 = (TextView) v.findViewById(R.id.textView2);
        tv2.setTextSize(8);
        tv2.setTextColor(icons.COLORS[2]);
        Integer ench = (Integer) charData.get("enchant");
        if (ench == null)
            tv2.setVisibility(View.INVISIBLE);
        else  // this should never return null.
            tv2.setText((String)enchData.get(ench).get("name"));
        TextView tv3 = (TextView) v.findViewById(R.id.textView3);
        tv3.setText(((Integer)itemData.get("itemLevel")).toString());
    }

    private void initItemAux(View v, String slot) {
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
