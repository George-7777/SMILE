package com.example.smile.ui.commandsView;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smile.commands.commandsScripts.reminder.ReminderData;
import com.example.smile.data.PreferencesManager;
import com.example.smile.R;
import com.example.smile.ui.MainActivity;
import com.example.smile.commands.commandsScripts.reminder.ReminderScheduler;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView listView;
    private Button button;
    private Button buttonBack;
    private PreferencesManager preferencesManager;
    private Intent intent;
    private ReminderScheduler reminderScheduler;

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
        listView = findViewById(R.id.ListView);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    addItem(v);
                } catch (ParseException | IOException e) {
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
        preferencesManager = new PreferencesManager(this);
        reminderScheduler = new ReminderScheduler(this);

        items = new ArrayList<>();
        items = preferencesManager.getTasks();
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        setUpListViewListener();
    }

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener((parent, view, i, l) -> {
            Context context = getApplicationContext();
            Toast.makeText(context, "Задача выполнена", Toast.LENGTH_LONG).show();

            try {
                preferencesManager.removeTask(i);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            items.remove(i);
            itemsAdapter.notifyDataSetChanged();
            return true;
        });
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void addItem(View v) throws ParseException, IOException {
        EditText inputTitle = findViewById(R.id.editTextText);
        EditText inputTime = findViewById(R.id.editTextTime);

        String title = inputTitle.getText().toString().trim();
        String time = inputTime.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, напишите задачу", Toast.LENGTH_LONG).show();
            return;
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, укажите время (например, 15:30)", Toast.LENGTH_LONG).show();
            return;
        }

        ReminderData reminderData = createReminderData(title, time);

        if (reminderData != null) {
            reminderScheduler.scheduleReminder(reminderData);

            String itemText = title + " в " + time;
            items.add(itemText);
            itemsAdapter.notifyDataSetChanged();

            preferencesManager.saveTasks(items);

            inputTitle.setText("");
            inputTime.setText("");

            String formattedTime = reminderScheduler.getFormattedTriggerTime(reminderData);
            Toast.makeText(this,
                    "Напоминание \"" + title + "\" установлено на " + formattedTime,
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,
                    "Ошибка: неверный формат времени. Используйте HH:MM (например, 15:30)",
                    Toast.LENGTH_LONG).show();
        }
    }
    private ReminderData createReminderData(String title, String time) {
        try {
            // парсинг времени формата HH:MM
            String[] timeParts = time.split(":");
            if (timeParts.length != 2) {
                return null;
            }

            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return null;
            }

            ReminderData data = new ReminderData();
            data.setType(ReminderData.ReminderType.TIME_STRING);
            data.setMessage(title);
            data.setHour(hour);
            data.setMinute(minute);

            return data;

        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
}