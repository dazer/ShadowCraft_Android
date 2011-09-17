package com.shadowcraft.android;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemView extends LinearLayout {

    View view;
    String slot;

    public ItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = layoutInflater.inflate(R.layout.item, this);
    }

    @Override
    public int getId() {
        return Data.itemMap.get(slot);
    }

    public void init(CharHandler charHandler, IconHandler icons, String slot) {
        this.slot = slot;
        HashMap<String, Object> itemData = charHandler.itemCache().get(slot);
        HashMap<String, Object> charData = charHandler.charItems().get(slot);
        if (itemData == null || charData.isEmpty()) {
            //TODO populate with question mark and 'no item found'
        }
        Map<Integer, HashMap<String, Object>> gemsData = charHandler.gemCache();
        Map<Integer, HashMap<String, Object>> enchData = charHandler.enchCache();
        int[] sockets = new int[] {R.id.ivGem0, R.id.ivGem1, R.id.ivGem2};

        ImageView icon = (ImageView) view.findViewById(R.id.ivItemIcon);
        icon.setImageBitmap(icons.getItemIcon("ability_backstab", 4));
        @SuppressWarnings("unchecked")
        List<String> socketsInItem = (List<String>) itemData.get("sockets");
        boolean prismaticAble = slot.equals("waist") || (
                (slot.equals("hands") || slot.equals("wrist")) &&
                charHandler.professions().contains("blacksmithing")
                );
        for (int i = 0; i<=2; i++) {
            ImageView gemView = (ImageView) view.findViewById(sockets[i]);
            boolean prismatic = socketsInItem.size() == i && prismaticAble;
            if (socketsInItem.size() <= i && !prismatic) {
                gemView.setVisibility(View.INVISIBLE);
                continue;
            }
            String socketType;
            if (prismatic)
                socketType = "PRISMATIC";
            else
                socketType = socketsInItem.get(i);
            Integer gemId = (Integer) charData.get("gem" + i);

            if (gemId == null) {
                Drawable gemIcon = icons.getGemIcon("sc_empty_gem", socketType);
                gemView.setImageDrawable(gemIcon);
            }
            else {
                String gemIconName = (String) gemsData.get(gemId).get("icon");
                Drawable gemIcon = icons.getGemIcon(gemIconName, socketType);
                gemView.setImageDrawable(gemIcon);
            }
        }
        TextView tv1 = (TextView) view.findViewById(R.id.textView1);
        tv1.setTextColor(icons.COLORS[(Integer)itemData.get("quality")]);
        tv1.setText((String)itemData.get("name"));
        TextView tv2 = (TextView) view.findViewById(R.id.textView2);
        Integer ench = (Integer) charData.get("enchant");
        if (ench == null || !charHandler.professions().contains("enchanting"))
            tv2.setVisibility(View.INVISIBLE);
        else  // this should never return null.
            tv2.setText((String)enchData.get(ench).get("name"));
        TextView tv3 = (TextView) view.findViewById(R.id.textView3);
        tv3.setText(((Integer)itemData.get("itemLevel")).toString());
    }

}
