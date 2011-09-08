package com.shadowcraft.android;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class ShadowcraftMain extends Activity implements OnClickListener {

    private APP             app;
    private String          name, realm, region;
    private EditText        etName, etRealm;
    private RadioButton     rbEU, rbUS, rbTW, rbKR, rbCN;
    private Button          bStart, bAux;
    private CharHandler     charHandler;
    private TextView        tvResult;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        initDB();
        init();
    }

    public void init() {
        app = (APP) getApplication();
        etName = (EditText) findViewById(R.id.start_field_name);
        etRealm = (EditText) findViewById(R.id.start_field_realm);
        rbEU = (RadioButton) findViewById(R.id.start_radio_eu);
        rbUS = (RadioButton) findViewById(R.id.start_radio_us);
        rbTW = (RadioButton) findViewById(R.id.start_radio_tw);
        rbKR = (RadioButton) findViewById(R.id.start_radio_kr);
        rbCN = (RadioButton) findViewById(R.id.start_radio_cn);
        bStart = (Button) findViewById(R.id.start_start_button);
        bAux = (Button) findViewById(R.id.bStartTalents);

        // Have a text view to check some stuff as we go.
        tvResult = (TextView) findViewById(R.id.start_result);
        tvResult.setText("");
        bStart.setOnClickListener(this);
        bAux.setOnClickListener(this);
    }

    /**
     * Creates and deletes the DB helper class to ensure that the DB exists and
     * is up to date.
     * It may be a good idea to pass this object to the class that actually uses
     * the database instead of destroying it. Note that current version of the
     * charHandler has no onCreate, no context, no way to initialize a database
     * when the system doesn't have it yet, or any way to pass intents to it.
     */
    public void initDB() {
        DataBaseHelper dbHandler = new DataBaseHelper(this);
        try {
            dbHandler.createDataBase();
        }
        catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        dbHandler.close();
        dbHandler = null;
    };

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
        charHandler = new CharHandler(name, realm, region);
        app.setCharHandler(charHandler);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.start_start_button:
            setCharHandler();
            Double dps = charHandler.getDPS();
            tvResult.setText(dps.toString());
            break;
        case R.id.bStartTalents:
            if (app.existsCharHandler())
                ;
            // quick access to talents
            Intent i = new Intent(ShadowcraftMain.this, Talents.class);
            startActivity(i);
            break;
        }
    }

}
