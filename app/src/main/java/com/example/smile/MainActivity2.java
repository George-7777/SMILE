package com.example.smile;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity2 extends AppCompatActivity {

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView listView;
    private Button button;
    private Button buttonBack;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        listView = findViewById(R.id.ListView);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addItem(v);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        buttonBack = findViewById(R.id.buttonBack);
        intent = new Intent(this, MainActivity.class);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });

        items = new ArrayList<>();
        try {
            items = (ArrayList<String>) ObjectSerializer.deserialize(sharedPref.getString("TASKS", ObjectSerializer.serialize(new ArrayList<>())));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        setUpListViewListener();
    }

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long l) {
                Context context = getApplicationContext();
                Toast.makeText(context, "Задача выполнена", Toast.LENGTH_LONG).show();

                items.remove(i);
                itemsAdapter.notifyDataSetChanged();
                SharedPreferences sharedPref = context.getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                try {
                    editor.putString("TASKS", ObjectSerializer.serialize(items));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                editor.apply();
                return true;
            }
        });
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void addItem(View v) throws ParseException {
        Context context = getApplicationContext();;
        EditText input = findViewById(R.id.editTextText);
        EditText inputTime = findViewById(R.id.editTextTime);
        String itemText = input.getText().toString() + " " + inputTime.getText().toString();
//        TextView text = findViewById(R.id.textView);
//        text.setText(inputTime.getText().toString());
        String DATE_FORMAT_NOW="d-M-yyyy";

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        Calendar cal = Calendar.getInstance();
        String timestamp = sdf.format(cal.getTime());
        DateFormat formatter = new SimpleDateFormat("d-M-yyyy hh:mm");
        String strDataTime = timestamp + " " + inputTime.getText().toString();
        Date date1 = formatter.parse(strDataTime);
        Intent notifyIntent = new Intent(this,MyReceiver.class);
        notifyIntent.putExtra("name", input.getText().toString());
        //notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String str = inputTime.getText().toString();
        String[] strs = str.split(":");
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date1.getTime(),
                pendingIntent);

        if(!(itemText.equals(""))){
            itemsAdapter.add(itemText);
            input.setText("");

            SharedPreferences sharedPref = context.getSharedPreferences(
                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            try {
                editor.putString("TASKS", ObjectSerializer.serialize(items));
            } catch (IOException e) {
                e.printStackTrace();
            }
            editor.apply();
        }
        else{
            Toast.makeText(getApplicationContext(), "Пожалуйста, напиши задачу", Toast.LENGTH_LONG).show();
        }
    }
}