package com.shadowcraft.android;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper{

    private static String PACKAGE_NAME = "com.shadowcraft.android";
    private static String DB_PATH = "/data/data/" + PACKAGE_NAME + "/databases/";
    private static String DB_NAME = "SC_DATABASE.sqlite";
    private static int DB_VERSION = 1;
    private SQLiteDatabase db;
    private final Context context;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to
     * the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    /**
     * Creates a empty database on the system and rewrites it.
     */
    public void createDataBase() throws IOException{
        if(checkDataBase()){
            //do nothing - database already exists
        }
        else{
            this.getReadableDatabase();
            try {
                copyDataBase();
            }
            catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time we open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        //context.deleteDatabase(DB_NAME);
        SQLiteDatabase checkDB = null;
        try{
            String path = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        }
        catch(SQLiteException ignore){}  //database does't exist yet.

        if(checkDB != null){
            checkDB.close();
        }
        return (checkDB != null) ? true : false;
    }

    /**
     * Copies the database from the assets-folder to the just created empty
     * database in the system folder, from where it can be accessed and handled.
     * This is done by transferring byte stream.
     */
    private void copyDataBase() throws IOException{
        InputStream iStream = context.getAssets().open(DB_NAME);
        String path = DB_PATH + DB_NAME;
        OutputStream oStream = new FileOutputStream(path);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = iStream.read(buffer))>0){
            oStream.write(buffer, 0, length);
        }
        oStream.flush();
        oStream.close();
        iStream.close();
    }

    public void openDataBase() throws SQLException{
        String path = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if(db != null)
            db.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // /////////////////////////////////////////////////////////////////////////
    // Custom methods to get info from the data base
    // /////////////////////////////////////////////////////////////////////////

    public HashMap<String, Object> getItem(long id) {
        HashMap<String, Object> itemMap = new HashMap<String, Object>();
        String[] columns = new String[] {"name", "icon", "quality", "itemLevel",
                "gem1", "gem2", "gem3", "Stat1Amount", "Stat1Id", "Stat2Amount",
                "Stat2Id", "Stat3Amount", "Stat3Id", "Stat4Amount", "Stat4Id"};
        Cursor c = db.query("items", columns, "_id=" + id, null, null, null, null);
        if (c != null) {
            c.moveToFirst();

            for (String key : Arrays.asList("name", "icon")) {
                itemMap.put(key, c.getString(c.getColumnIndexOrThrow(key)));
            }
            for (String key : Arrays.asList("quality", "itemLevel")) {
                itemMap.put(key, c.getInt(c.getColumnIndexOrThrow(key)));
            }

            List<String> gems = new ArrayList<String>();
            for (String s : Arrays.asList("gem1", "gem2", "gem3")) {
                String gem = c.getString(c.getColumnIndexOrThrow(s));
                if (gem==null)
                    break;
                gems.add(gem);
            }
            itemMap.put("sockets",  gems);

            List<Stat> stats = new ArrayList<Stat>();
            for (int i = 1; i<=4; i++) {
                int statId = c.getInt(c.getColumnIndexOrThrow("Stat" + i + "Id"));
                if (statId == 0)
                    break;
                float statVl = c.getInt(c.getColumnIndexOrThrow("Stat" + i + "Amount"));
                Stat stat = new Stat(statId, statVl);
                stats.add(stat);
            }
            itemMap.put("stats", stats);
        }
        c.close();
        return itemMap;
    }

}
