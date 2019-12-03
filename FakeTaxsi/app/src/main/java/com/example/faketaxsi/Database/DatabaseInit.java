package com.example.faketaxsi.Database;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.faketaxsi.Driver;

import java.util.ArrayList;

public class DatabaseInit extends DatabaseHelper {

    public DatabaseInit(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        super.onCreate(db);
    }

    public ArrayList<Driver> readData(){
        Cursor res = getDriverAll();
        ArrayList<Driver> drivers = new ArrayList<>();

        if(res.getCount() == 0) {
            // show message
            Log.v("inform", "nieko nerasta");
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        for(int i=0;i<res.getCount();i++) {
            if(res.moveToNext()) {
                for(int j=0;j<9;j++) {
                    String str = res.getString(j);
                    if(str != null)
                        buffer.append(str + "\n");
                    else break;
                }
                buffer.append("\n\n");

                //patiekalai.add(new Patiekalas(res.getString(1), Float.valueOf(res.getString(2)),
                        //res.getString(3), res.getString(4), res.getInt(5)));
                drivers.add(new Driver(res.getString(1), res.getString(2),
                        res.getString(5), res.getString(6), res.getString(7), res.getString(8)));
            }
        }

        // Show all data
        //showMessage("Data",buffer.toString());
        return drivers;
    }

    public ArrayList<float[]> readHistory() {
        Cursor res = getAllHistory();

        if(res.getCount() == 0) {
            // show message
            Log.v("inform", "nieko nerasta");
            return null;
        }

        ArrayList<float[]> data = new ArrayList<>();
        for(int i=0;i<res.getCount();i++) {
            if(res.moveToNext()) {
                data.add(new float[]{Float.valueOf(res.getString(0)), Float.valueOf(res.getString(1)),
                        Float.valueOf(res.getString(2)), Float.valueOf(res.getString(3))});
            }
        }

        return data;
    }

    public void addData() {
        ArrayList<String[]> values = new ArrayList<>();
        values.add(new String[]{"Jonas", "Zemaitis", "1", "KJF981", "Toyota", "Avensins", "Grey"});
        values.add(new String[]{"Stasys", "Zukevicius", "2", "LMN001", "Opel", "Astra", "Red"});
        values.add(new String[]{"Mikalojus", "Zbiska", "3", "ZBS911", "Citroen", "C6", "Orange"});
        values.add(new String[]{"Zbignevas", "Wtfiskevicius", "4", "LOX123", "Toyota", "Yaris", "White"});
        values.add(new String[]{"Jonas", "Ponas", "5", "DUX112", "Toyota", "Corolla", "Grey"});

        for (String[] val : values) {
            boolean isInserted = insertDriverCar(val);
            if (isInserted)
                Log.v("Inform", "Data Inserted");
            else
                Log.v("Inform", "Data NOT Inserted");
        }
    }

    public void showMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    public void populateDatabase(){
        addData();
        //readData();
    }
}
