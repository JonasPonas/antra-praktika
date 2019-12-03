package com.example.faketaxsi;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.faketaxsi.Database.DatabaseInit;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private DatabaseInit myDb;
    private ArrayList<float[]> history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        myDb = new DatabaseInit(this);
        history = myDb.readHistory();

        for(float[] data : history){

            ViewGroup layout = findViewById(R.id.mainH);
            View child = getLayoutInflater().inflate(R.layout.history_example, null);

            ((TextView)child.findViewById(R.id.nr)).setText("Nr" + (int)data[0]);
            ((TextView)child.findViewById(R.id.dist)).setText("Distance:" + String.format("%.02f", data[1]) + "m");
            ((TextView)child.findViewById(R.id.price)).setText("Price:" + String.format("%.02f", data[3]) + "Eur");
            ((TextView)child.findViewById(R.id.dur)).setText("Duration:" + String.format("%.02f", data[2]) + "min");

            layout.addView(child);

        }
    }
}
