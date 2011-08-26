package com.shadowcraft.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class ShadowcraftMain extends Activity {

    private String          name, realm, region;
    private EditText        etName, etRealm;
    private RadioButton     rbEU, rbUS, rbTW, rbKR, rbCN;
    private Button          bStart;
    private CharJSONHandler charHandler;
    private TextView        tvResult;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        init();
    }

    public void init() {
        etName = (EditText) findViewById(R.id.start_field_name);
        etRealm = (EditText) findViewById(R.id.start_field_realm);
        rbEU = (RadioButton) findViewById(R.id.start_radio_eu);
        rbUS = (RadioButton) findViewById(R.id.start_radio_us);
        rbTW = (RadioButton) findViewById(R.id.start_radio_tw);
        rbKR = (RadioButton) findViewById(R.id.start_radio_kr);
        rbCN = (RadioButton) findViewById(R.id.start_radio_cn);
        bStart = (Button) findViewById(R.id.start_start_button);

        // Have a text view to check some stuff as we go.
        tvResult = (TextView) findViewById(R.id.start_result);
        tvResult.setText("");

        bStart.setOnClickListener(new View.OnClickListener() {
            // @Override
            public void onClick(View v) {
                setCharHandler();
            }
        });
    }

    public void extractStrings() {
        name = etName.getText().toString();
        realm = etRealm.getText().toString();
        region = getRegion();
    }

    public String getRegion() {
        if (rbEU.isChecked())
            return "eu";
        else if (rbUS.isChecked())
            return "us";
        else if (rbTW.isChecked())
            return "tw";
        else if (rbKR.isChecked())
            return "kr";
        else if (rbCN.isChecked())
            return "cn";
        else
            return "eu";
    }

    public void setCharHandler() {
        extractStrings();
        charHandler = new CharJSONHandler(name, realm, region);
        Double dps = charHandler.getDPS();
        tvResult.setText(dps.toString());
    }

}
