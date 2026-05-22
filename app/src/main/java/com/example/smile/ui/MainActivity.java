package com.example.smile.ui;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import static com.example.smile.Constant.WAKE_WORD;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smile.services.MyReceiver;
import com.example.smile.R;
import com.example.smile.services.VoiceTriggerService;
import com.example.smile.ai.AIProvider;
import com.example.smile.ai.GeminiProvider;
import com.example.smile.commands.CommandExecutor;
import com.example.smile.commands.commandsScripts.radio.RadioPlayer;
import com.example.smile.data.PreferencesManager;
import com.example.smile.tts.TTSManager;
import com.example.smile.ui.commandsView.CopilkaAndBalance;
import com.example.smile.ui.commandsView.MainActivity2;
import com.example.smile.commands.commandsScripts.reminder.ReminderScheduler;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private BroadcastReceiver triggerReceiver;
    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ArrayList<String> messages;
    private ArrayAdapter<String> messagesAdapter;
    private ListView listView;
    private ListView listMessages;
    private Spinner spinner;
    private ImageButton sendCommand;
    private ImageButton speechTotext;
    private EditText commandToSend;
    private TTSManager ttsManager;
    private AIProvider aiProvider;
    private static final int SPEECH_REQUEST_CODE = 77712345;
    private CommandExecutor commandExecutor;

    private Intent recognizerIntent;
    private PreferencesManager preferencesManager;
    private RadioPlayer radioPlayer;
    private ReminderScheduler reminderScheduler;


    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.FOREGROUND_SERVICE
        };

        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissions(
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                Toast.makeText(this, "Разрешения получены", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Некоторые разрешения не получены", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerTriggerReceiver() {
        triggerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (VoiceTriggerService.ACTION_TRIGGER_DETECTED.equals(intent.getAction())) {
                    String spokenText = intent.getStringExtra(
                            VoiceTriggerService.EXTRA_TRIGGER_PHRASE
                    );
                    long timestamp = intent.getLongExtra("timestamp", 0);
                    Log.d("test", "registerTriggerReceiver");
                    if (Objects.equals(spokenText, WAKE_WORD))
                        respondToTrigger();
                }
            }
        };

        IntentFilter filter = new IntentFilter(VoiceTriggerService.ACTION_TRIGGER_DETECTED);

        registerReceiver(triggerReceiver, filter, RECEIVER_EXPORTED);
    }



    private void respondToTrigger() {
        if (ttsManager != null) {
            String response = "я тебя слушаю";
            Log.d("test", "respondToTrigger");

            ttsManager.speak(response);

            //stopVoiceTriggerService();
            new Handler(Looper.getMainLooper()).postDelayed(this::triggered, 2000);

        }
    }
    private void triggered(){
        Intent intentSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intentSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите команду");

        try {
            startActivityForResult(intentSpeech, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Произошла ошибка при распозновании", Toast.LENGTH_SHORT).show();
        }
    }

    private void startVoiceTriggerService() {
        VoiceTriggerService.startService(this);
        Toast.makeText(this, "Служба запущена", Toast.LENGTH_SHORT).show();
    }

    private void stopVoiceTriggerService() {
        VoiceTriggerService.stopService(this);
        Toast.makeText(this, "Служба остановлена", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Отписываемся от receiver
        if (triggerReceiver != null) {
            unregisterReceiver(triggerReceiver);
        }

        // Останавливаем TTS
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        if (radioPlayer != null) {
            radioPlayer.release();
        }
        stopVoiceTriggerService();
    }

    private PendingIntent temp(String zapiska){
        Intent notifyIntent = new Intent(this, MyReceiver.class);
        notifyIntent.putExtra("name", zapiska);
        return PendingIntent.getBroadcast
                (this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        aiProvider = new GeminiProvider(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        // регистрация BroadcastReceiver
        registerTriggerReceiver();

        // проверка разрешений
        checkPermissions();
        System.out.println("print");
        startVoiceTriggerService();


        ttsManager = new TTSManager(this, new TTSManager.TTSListener() {
            @Override
            public void onInit() {
                ttsManager.speak("Привет! Чем могу сегодня помочь?");
            }

            @Override
            public void onSpeakStart(String utteranceId) {

            }

            @Override
            public void onSpeakDone(String utteranceId) {

            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        preferencesManager = new PreferencesManager(this);
        radioPlayer = new RadioPlayer(this);
        aiProvider = new GeminiProvider(this);
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            System.out.println("okk. SOS");
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, 123);
        }
        // инициализация TTSManager


        createSoundPool();
        // получение вью нижнего экрана
        LinearLayout llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        speechTotext = findViewById(R.id.imageButton);
        listView = findViewById(R.id.listCommands);
        listMessages = findViewById(R.id.listChat);
        messages = new ArrayList<>();
        messagesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        items = new ArrayList<>();
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        listMessages.setAdapter(messagesAdapter);
        commandToSend = findViewById(R.id.editTextText3);
        sendCommand = findViewById(R.id.imageButton5);
        setUpListViewListener();
        messagesAdapter.add("Информация: Список команд можно открыть по кнопке в левом нижнем углу.");
        messagesAdapter.add("Нужную тебе команду можешь также выбрать прямиком из списка.");
        messagesAdapter.add("Просто поговорить с помощником можно голосом нажав на микрофон или в окошко сверху микрофона и нажав кнопку снизу справа.");
        messagesAdapter.add("СМАЙЛИК: Привет! Чем могу сегодня помочь?");


        messagesAdapter.notifyDataSetChanged();

        speechTotext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                triggered();
            }
        });
        sendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = commandToSend.getText().toString();
                if (command.isEmpty()) return;

                messagesAdapter.add("Ты: " + command);
                messagesAdapter.notifyDataSetChanged();
                commandToSend.setText("");

                commandExecutor.execute(command, new CommandExecutor.CommandCallback() {
                    @Override public void onResult(String message) {}
                    @Override public void onError(String error) {}
                });
            }

        });

// настройка поведения нижнего экрана
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        ImageButton buttonExpand = findViewById(R.id.imageButton4);
        buttonExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

// настройка максимальной высоты
        bottomSheetBehavior.setPeekHeight(300);
        Intent intent = new Intent(this, MainActivity2.class);
        Intent intent7 = new Intent(this, CopilkaAndBalance.class);

// настройка возможности скрыть элемент при свайпе вниз
        bottomSheetBehavior.setHideable(false);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.group_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
// настройка колбэков при изменениях
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                if (pos == 0) {
                    System.out.println("ok");
                    itemsAdapter.clear();
                    itemsAdapter.add("Список задач");
                    itemsAdapter.add("Включить уведомления");
                    itemsAdapter.add("Выключить уведомления");
                    itemsAdapter.notifyDataSetChanged();
                    //textHelp.setText("1. Список дел/расписание DONE\n 2. Включить уведомления DONE\n 3. Отключить уведомления DONE");
                } else if (pos == 1) {
                    itemsAdapter.clear();
                    itemsAdapter.add("Разговор с ИИ");

                    itemsAdapter.notifyDataSetChanged();
                    //textHelp.setText("1. Объяснение от ИИ \n 2. Калькулятор \n 3. Таблица Менделеева \n 4. Добавить свою заметку");
                } else if (pos == 2) {
                    itemsAdapter.clear();
                    itemsAdapter.add("Добавить сос-номер");

                    itemsAdapter.notifyDataSetChanged();
                    //textHelp.setText("1. Защита от продажи карты/Информация о мошеннических схемах \n 2. Оповещения о подозрительных переводах \n 3. Включить усиленную защиту от подозрительных переводов");
                } else if (pos == 3) {
                    itemsAdapter.clear();
                    itemsAdapter.add("Включи радио");
                    itemsAdapter.add("Выключи радио");
                    itemsAdapter.notifyDataSetChanged();
                    //textHelp.setText("1. Режим 'Разговоров' \n 2. Радио \n 3. Быстрые инструменты");
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        }
        spinner.setOnItemSelectedListener(new SpinnerActivity());

        reminderScheduler = new ReminderScheduler(this);

        commandExecutor = new CommandExecutor(
                this, ttsManager, preferencesManager, radioPlayer, aiProvider,
                messagesAdapter, reminderScheduler
        );
    }

    private void setUpListViewListener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCommand = itemsAdapter.getItem(position);
            if (selectedCommand != null && !selectedCommand.isEmpty()) {
                commandExecutor.execute(selectedCommand, new CommandExecutor.CommandCallback() {
                    @Override public void onResult(String message) {}
                    @Override public void onError(String error) {}
                });
            }
        });
    }

    protected void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    protected void createSoundPool() {
        createNewSoundPool();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String command = result.get(0);
                commandToSend.setText(command);

                messagesAdapter.add("Ты: " + command);
                messagesAdapter.notifyDataSetChanged();

                commandExecutor.execute(command, new CommandExecutor.CommandCallback() {
                    @Override public void onResult(String message) {}

                    @Override public void onError(String error) {}
                });
            }
        }
    }
}