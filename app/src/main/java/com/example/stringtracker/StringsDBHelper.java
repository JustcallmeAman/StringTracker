package com.example.stringtracker;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

// StringSets DB helper class  AZ, WKD
public class StringsDBHelper extends SQLiteOpenHelper {

    //Strings Table
    private static final String DATABASE_NAME = "strings";
    private static final int DATABASE_VERSION = 1;
    private static final String KEY_STRINGS_ID = "StringsID";
    private static final String KEY_BRAND = "Brand";
    private static final String KEY_MODEL = "Model";

    private static final String CREATE_TABLE_STRINGS =
            "create table strings (StringsID integer primary key autoincrement, "
                    + "Brand text not null,"
                    + "Model text not null,"
                    + "Tension text not null,"
                    + "InstrType text ,"
                    + "Cost REAL not null,"
                    + "AvgLife REAL not null,"
                    + "ChangeCnt integer not null,"
                    + "FirstSession boolean not null,"
                    + "AvgProjStr text not null,"
                    + "AvgToneStr text not null,"
                    + "AvgIntonStr text not null)";

    public StringsDBHelper(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public StringsDBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // Method to return a Hashmap (dictionary) list from the DB of all stringsets with
    // data keys StringsID = "stringsID" , Brand = "brand", Model = "model" in each hash item (note all values are String data type) wkd
    public ArrayList<HashMap<String, String>> getStringsList(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> instrList = new ArrayList<>();
        String query = "SELECT "+KEY_STRINGS_ID+", "+KEY_BRAND+", "+KEY_MODEL+" FROM "+ DATABASE_NAME;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            HashMap<String,String> instr = new HashMap<>();
            instr.put("stringsID",cursor.getString(Integer.parseInt(String.valueOf(cursor.getColumnIndex(KEY_STRINGS_ID)))));
            instr.put("brand",cursor.getString(cursor.getColumnIndex(KEY_BRAND)));
            instr.put("model",cursor.getString(cursor.getColumnIndex(KEY_MODEL)));
            instrList.add(instr);
        }
        return  instrList;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_STRINGS);
        } catch (SQLException e){
            System.out.println("### ERROR SQL Create Strings Table");
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.w(StringTrackerDBHelper.class.getName(),
                "Upgrading database from version" + oldVersion + "to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS STRINGS ;");
        onCreate(db);

    }

}
