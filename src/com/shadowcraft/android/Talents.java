package com.shadowcraft.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Talents extends Activity implements OnClickListener {

    private int gameClass = 4;
    private int[][] maxTalents = TalentsData.maxTalentMap[gameClass];
    private int[][] iconIds = TalentsData.talentIconID[gameClass];
    private List<String> spentTalents;
    private TextView tvAux;
    private IconHandler icons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talents);
        icons = ((APP) getApplication()).getIconHandler();
        init();
    }

    private void init() {
        tvAux = (TextView) findViewById(R.id.tvAux);

        // just a standard assassination spec for testing.
        spentTalents = new ArrayList<String>();
        spentTalents.add("0333230113022110321");
        spentTalents.add("0020000000000000000");
        spentTalents.add("2030030000000000000");

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        //int height = display.getHeight();

        TableLayout tree = (TableLayout) findViewById(R.id.tree1);
        tree.setMinimumWidth(width);

        int[] currentTree = maxTalents[0];
        int[] currentIcons = iconIds[0];
        String currentSpent = spentTalents.get(0);
        int talentCounter = 0;

        for (int i = 0; i<7; i++) {
            TableRow row = (TableRow) tree.getChildAt(i);
            for (int j = 0; j<4; j++) {
                View talentView = row.getChildAt(j);
                int maxValue = currentTree[4 * i + j];
                if (maxValue == 0) {
                    talentView.setVisibility(View.INVISIBLE);
                    continue;
                }
                //talentView.setMinimumWidth(width/4);

                char spent = currentSpent.charAt(talentCounter);
                int iconId = currentIcons[talentCounter];
                initTalentView(talentView, spent, maxValue, iconId);
                talentView.setId(100 * 0 + 10 * i + j);
                talentView.setOnClickListener(this);
                talentCounter++;
            }
        }
    }

    private void initTalentView(View view, char spent, int maxVl, int iconId) {
        ImageView talent = (ImageView) view.findViewById(R.id.ivTalentIcon);
        Bitmap icon = icons.getTalentIcon(iconId);
        talent.setImageBitmap(icon);
        TextView text = (TextView) view.findViewById(R.id.tvTalentSpent);
        text.setText(spent + "/" + maxVl);
    }

    @Override
    public void onClick(View v) {
        Integer id = v.getId();
        switch (id) {
        default:
            setTalentValue(v);
            tvAux.setText(id.toString());
            break;
        }
    }

    private void setTalentValue(View view) {
        TextView text = (TextView) view.findViewById(R.id.tvTalentSpent);
        String str = (String) text.getText();
        int curValue = Integer.parseInt(""+str.charAt(0));
        int maxValue = Integer.parseInt(""+str.charAt(2));
        int newValue = (curValue<maxValue) ? curValue+1 : 0;
        text.setText(newValue + "/" + maxValue);
    }



}
