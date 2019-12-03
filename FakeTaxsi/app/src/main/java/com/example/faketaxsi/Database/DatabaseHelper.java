package com.example.faketaxsi.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.faketaxsi.Driver;

/**
 * Created by ProgrammingKnowledge on 4/3/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    protected Context context;

    private static final String DATABASE_NAME = "Taxi.db";
    private static final String TABLE_NAME = "drivers_table";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "VARDAS";
    private static final String COL_3 = "PAVARDE";
    private static final String COL_4 = "CARID";

    private static final String TABLE_NAME2 = "cars_table";
    private static final String COL2_1 = "ID";
    private static final String COL2_2 = "NUMBER";
    private static final String COL2_3 = "MAKE";
    private static final String COL2_4 = "MODEL";
    private static final String COL2_5 = "COLOR";

    private static final String TABLE_NAME3 = "history_table";
    private static final String COL3_1 = "ID";
    private static final String COL3_2 = "DISTANCE";
    private static final String COL3_3 = "PRICE";
    private static final String COL3_4 = "DURATION";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,VARDAS TEXT," +
                " PAVARDE TEXT, CARID INTEGER)");
        db.execSQL("create table " + TABLE_NAME2 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,NUMBER TEXT," +
                " MAKE TEXT, MODEL TEXT, COLOR TEXT)");
        db.execSQL("create table " + TABLE_NAME3 +" (ID INTEGER PRIMARY KEY AUTOINCREMENT,DISTANCE FLOAT," +
                " PRICE FLOAT, DURATION FLOAT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME2);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME3);
        onCreate(db);
    }

    public boolean insertDriverCar(String[] arr) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, arr[0]);
        contentValues.put(COL_3, arr[1]);
        contentValues.put(COL_4, Integer.valueOf(arr[2]));

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(COL2_2, arr[3]);
        contentValues2.put(COL2_3, arr[4]);
        contentValues2.put(COL2_4, arr[5]);
        contentValues2.put(COL2_5, arr[6]);

        long result = db.insert(TABLE_NAME,null ,contentValues);
        long result2 = db.insert(TABLE_NAME2,null ,contentValues2);

        if(result == -1 || result2 == -1)
            return false;
        else
            return true;
    }

    public boolean insertHistory(float distance, float price, float duration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL3_2, distance);
        contentValues.put(COL3_3, price);
        contentValues.put(COL3_4, duration);

        long result = db.insert(TABLE_NAME3,null ,contentValues);

        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getDriverAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME+" INNER JOIN "
                +TABLE_NAME2+ " ON " + "drivers_table.CARID = cars_table.ID",null);

        return res;
    }
    public Cursor getAllHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME3,null);
        return res;
    }

    public void deleteDatabase(){
        if(!context.deleteDatabase(DATABASE_NAME))
            Log.v("Inform", "Database delete failed");
        else
            Log.v("Inform", "Database deleted");
    }
}