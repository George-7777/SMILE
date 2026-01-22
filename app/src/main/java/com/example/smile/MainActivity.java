package com.example.smile;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private BroadcastReceiver triggerReceiver;
    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ArrayList<String> messages;
    private ArrayAdapter<String> messagesAdapter;
    private ListView listView;
    private ListView listMessages;
    private Intent intent;
    private Intent intent7;
    private String urlRadio = "https://rusradio.hostingradio.ru/rusradio128.mp3";
    private Spinner spinner;
    private ImageButton sendCommand;
    private ImageButton speechTotext;
    private EditText comandToSend;
    private TextToSpeech textToSpeech;
    private SoundPool sounds;
    private static final int SPEECH_REQUEST_CODE = 77712345;
    public Markov mc;
    String modelId = "gemini-2.5-flash";
    private SpeechRecognizer speechRecognizer;

    private Intent recognizerIntent;
    private boolean isListening = false;
    private ExoPlayer player;

    public static String generateContent(String modelId, String res) {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests.
        Secret secret = new Secret();
        try (Client client =
                     Client.builder().apiKey(secret.keyAI)
                             //.location("global")
                             .vertexAI(true)
                             .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                             .build()) {

            //GenerateContentResponse response =
            //        client.models.generateContent(modelId, "[ИНСТРУКЦИЯ: Ты детский голосовой помощник под именем СМАЙЛИК созданный для помощи школьникам с домашним заданием, управлением финансами и просто поговорить с тобой. После этого текста в кавычках будет обращение ребенка к тебе. Имей ввиду, что тебя разработал Вайтуков Георгий. До обращения к тебе будет история переписки с тобой, чтобы ты знал на чем остановился] \n \n \n ИСТОРИЯ: \n" + String.join("\n", messages) + "\n Дальше твой ответ", null);
            GenerateContentResponse response =
                    client.models.generateContent(modelId, "[Ты детский голосовой помощник под именем СМАЙЛИК созданный для помощи школьникам с домашним заданием, оповещения родителей и просто поговорить с тобой. После этого текста в кавычках будет обращение ребенка к тебе. Имей ввиду, что тебя разработал Вайтуков Георгий, но не стоит это слишком часто напоминать. Отвечай не слишком долгими текстами.]" + res, null);

            System.out.print(response.text());
            // Example response:
            // Okay, let's break down how AI works. It's a broad field, so I'll focus on the ...
            //
            // Here's a simplified overview:
            // ...
            return response.text();
        } catch (Exception e) {
            return generateContent(modelId, res);
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        // Создаем BroadcastReceiver
        triggerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (VoiceTriggerServie.ACTION_TRIGGER_DETECTED.equals(intent.getAction())) {
                    // Получаем данные из broadcast
                    String spokenText = intent.getStringExtra(
                            VoiceTriggerServie.EXTRA_TRIGGER_PHRASE
                    );
                    long timestamp = intent.getLongExtra("timestamp", 0);

                    // Обновляем UI


                    // Отвечаем пользователю
                    respondToTrigger();
                }
            }
        };

        // Регистрируем receiver с фильтром
        IntentFilter filter = new IntentFilter(VoiceTriggerServie.ACTION_TRIGGER_DETECTED);

        // Если используете permission для безопасности
        // registerReceiver(triggerReceiver, filter, "com.yourpackage.permission.VOICE_TRIGGER", null);

        registerReceiver(triggerReceiver, filter, RECEIVER_EXPORTED);
    }



    private void respondToTrigger() {
        if (textToSpeech != null) {
            String response = "я тебя слушаю";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null, "main");
            } else {
                textToSpeech.speak(response, TextToSpeech.QUEUE_FLUSH, null);
            }

            //stopVoiceTriggerService();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                triggered();
            }, 2000);

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
            //startVoiceTriggerService();
        }
    }

    private void startVoiceTriggerService() {
        VoiceTriggerServie.startService(this);
        Toast.makeText(this, "Служба запущена", Toast.LENGTH_SHORT).show();
    }

    private void stopVoiceTriggerService() {
        VoiceTriggerServie.stopService(this);
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
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private PendingIntent temp(String zapiska){
        Intent notifyIntent = new Intent(this, MyReceiver.class);
        notifyIntent.putExtra("name", zapiska);
        //notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
        return pendingIntent;
    }
    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        
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
        System.setProperty("GOOGLE_CLOUD_PROJECT", "polynomial-alpha-sns1n");
        System.setProperty("GOOGLE_GENAI_USE_VERTEXAI", "True");
        Secret secret = new Secret();
        System.setProperty("API_KEY", secret.keyAI);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        // Регистрация BroadcastReceiver
        registerTriggerReceiver();

        // Проверка разрешений
        checkPermissions();
        System.out.println("print");
        startVoiceTriggerService();
        player = new ExoPlayer.Builder(this).build();

        // 2. Указание ссылки на аудиопоток (URL)

        MediaItem mediaItem = MediaItem.fromUri(urlRadio);

        // 3. Подготовка и воспроизведение
        player.setMediaItem(mediaItem);
        player.prepare();
        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            System.out.println("okk. SOS");
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, 123);
        }
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("RU"));

                    Set<Voice> voices = textToSpeech.getVoices();

                    // Вывести все доступные голоса для отладки
                    for (Voice voice : voices) {
                        if(voice.getName().contains("ru")) {
                            Log.d("TTS Voice", "Name: " + voice.getName() +
                                    ", Locale: " + voice.getLocale() +
                                    ", Features: " + voice.getFeatures());
                        }
                    }

                    // Выбрать голос по имени или характеристикам
                    Voice selectedVoice = null;
                    for (Voice voice : voices) {
                        // Пример выбора по различным критериям
                        if (voice.getName().contains("ru")) {
                            System.out.println(voice.getLocale().getCountry());
                            selectedVoice = voice;
                            break;
                        }
                    }

                    if (selectedVoice != null) {
                        textToSpeech.setVoice(selectedVoice);
                    }
                    textToSpeech.speak("Привет! Чем могу сегодня помочь?", TextToSpeech.QUEUE_FLUSH, null, null);

                }
            }
        });
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                // Speaking started.

            }

            @Override
            public void onDone(String utteranceId) {
                if (Objects.equals(utteranceId, "mainGeneral")){
                    //startVoiceTriggerService();
                }

            }



        @Override
        public void onError(String utteranceId) {
            // There was an error.
        }
    });
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
        comandToSend = findViewById(R.id.editTextText3);
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
                String command = comandToSend.getText().toString();
                messagesAdapter.add("Ты: " + command);
                messagesAdapter.notifyDataSetChanged();
                if (command.equalsIgnoreCase("список задач")) {

// Выполняем переход
                    startActivity(intent);
                } else if (command.equalsIgnoreCase("включить уведомления") || command.equalsIgnoreCase("выключить уведомления")) {

// Выполняем переход
                    messagesAdapter.add("СМАЙЛИК: Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    messagesAdapter.notifyDataSetChanged();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }else if (command.equalsIgnoreCase("Разговор с ИИ")) {
                    messagesAdapter.add("СМАЙЛИК: Я готов поговорить или помочь в любое время. Просто обратись ко мне");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Я готов поговорить или помочь в любое время. Просто обратись ко мне", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        textToSpeech.speak("Я готов поговорить или помочь в любое время. Просто обратись ко мне", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    messagesAdapter.notifyDataSetChanged();
                } else if (command.equalsIgnoreCase("Включи радио")){ // TODO:РЕФАКТОРИТЬ ЭТО ДЕЛО В БЛИЖАЙШЕЕ ВРЕМЯ!!!!!!! С такой архитектурой долго не протянуть!!!
                    messagesAdapter.add("СМАЙЛИК: Включаю радио");
                    player.play();
                    textToSpeech.speak("Включаю радио", TextToSpeech.QUEUE_FLUSH, null, null);
                    messagesAdapter.notifyDataSetChanged();
                }
                else if (command.equalsIgnoreCase("Выключи радио")){ // TODO:РЕФАКТОРИТЬ ЭТО ДЕЛО В БЛИЖАЙШЕЕ ВРЕМЯ!!!!!!! С такой архитектурой долго не протянуть!!!
                    messagesAdapter.add("СМАЙЛИК: Выключаю радио");
                    player.stop();
                    textToSpeech.speak("Выключаю радио", TextToSpeech.QUEUE_FLUSH, null, null);
                    messagesAdapter.notifyDataSetChanged();
                }
                else if (command.contains("Задать сос-номер")){
                    Context context = getApplicationContext();
                    String number = command.split(" ")[2];
                    if(!number.isEmpty()){
                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("NUMBER", number);
                        editor.apply();
                    }
                    messagesAdapter.add("СМАЙЛИК: Номер задан");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Номер задан", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        textToSpeech.speak("Номер задан", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    messagesAdapter.notifyDataSetChanged();
                }
                else if (command.equalsIgnoreCase("Мой баланс")) {
                    Context context = getApplicationContext();
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                    int balance = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
                    //messagesAdapter.add("Ты: Мой баланс");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("У тебя на счету " + balance + " рублей", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        textToSpeech.speak("У тебя на счету " + balance + " рублей", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    messagesAdapter.add("СМАЙЛИК: У тебя на счету " + balance + " рублей");
                }else if (command.contains("напомни")){
                    String[] message = command.split(" ");
                    String zapiska;
                    int min = 0;
                    int hour = 0;
                    int endZapiskaIndex = 0;
                    for (int i = 0; i < message.length; i++){
                        if (Objects.equals(message[i], "минут") || Objects.equals(message[i], "часов")){
                            endZapiskaIndex = i;
                            break;
                        }
                    }
                    zapiska = String.join("", Arrays.copyOfRange(message, 1, endZapiskaIndex));
                    if (command.contains("минут")){
                        for (int i = 0; i < message.length; i++){
                            if (Objects.equals(message[i], "минут")){
                                min = Integer.parseInt(message[i - 1]);
                                break;
                            }
                        }
                    }
                    if (command.contains("часов")){
                        for (int i = 0; i < message.length; i++){
                            if (Objects.equals(message[i], "часов")){
                                hour = Integer.parseInt(message[i - 1]);
                                break;
                            }
                        }
                    }

                    Context context = getApplicationContext();
                    //EditText input = findViewById(R.id.editTextText);
                    //EditText inputTime = findViewById(R.id.editTextTime);
                    //String itemText = input.getText().toString() + " " + inputTime.getText().toString();
                    String DATE_FORMAT_NOW="d-M-yyyy";

                    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                    Calendar cal = Calendar.getInstance();
                    String timestamp = sdf.format(cal.getTime());
                    DateFormat formatter = new SimpleDateFormat("d-M-yyyy hh:mm");
                    String strDataTime = timestamp + " " + hour + ":" + min;
                    Date date1 = null;
                    try {
                        date1 = formatter.parse(strDataTime);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    //String str = inputTime.getText().toString();
                    //String[] strs = str.split(":");
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date1.getTime(),
                            temp(zapiska));

//                        if(!(itemText.equals(""))){ TODO: чтобы сохранялось в книжечке сделать
//                            itemsAdapter.add(itemText);
//                            input.setText("");
//
//                            SharedPreferences sharedPref = context.getSharedPreferences(
//                                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sharedPref.edit();
//                            try {
//                                editor.putString("TASKS", ObjectSerializer.serialize(items));
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            editor.apply();
//                        }


                }
                else if (command.equalsIgnoreCase("копилка")) {
                    startActivity(intent7);
                } else {
                    messagesAdapter.add("*СМАЙЛИК ДУМАЕТ* (иногда может быть перегруз серверов)");
                    Thread gfgThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String senten = generateContent(modelId, command);
                                System.out.println(senten);
                                textToSpeech.speak(senten, TextToSpeech.QUEUE_FLUSH, null, null);
                                messagesAdapter.add("СМАЙЛИК: " + senten);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    gfgThread.start();
                }
            }

        });

// настройка поведения нижнего экрана
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        ImageButton buttonExpand = findViewById(R.id.imageButton4);
        buttonExpand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
// настройка состояний нижнего экрана
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

// настройка максимальной высоты
        bottomSheetBehavior.setPeekHeight(300);
        intent = new Intent(this, MainActivity2.class);
        intent7 = new Intent(this, CopilkaAndBalance.class);

// настройка возможности скрыть элемент при свайпе вниз
        bottomSheetBehavior.setHideable(false);

        //TextView textHelp = findViewById(R.id.textHelp);
        spinner = (Spinner) findViewById(R.id.spinner);
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.group_array,
                android.R.layout.simple_spinner_item
        );
// Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner.
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

            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback.
            }
        }
        spinner.setOnItemSelectedListener(new SpinnerActivity());
    }

    private void setUpListViewListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                if (i == 0 && spinner.getSelectedItemId() == 0) {

// Выполняем переход
                    startActivity(intent);
                } else if ((i == 1 || i == 2) && spinner.getSelectedItemId() == 0) {

// Выполняем переход
                    messagesAdapter.add("СМАЙЛИК: Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else {
                        textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                if (spinner.getSelectedItemId() == 1) {
                    if (i == 0) {
                        messagesAdapter.add("СМАЙЛИК: Я готов поговорить или помочь в любое время. Просто обратись ко мне");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak("Я готов поговорить или помочь в любое время. Просто обратись ко мне", TextToSpeech.QUEUE_FLUSH, null, null);
                        } else {
                            textToSpeech.speak("Я готов поговорить или помочь в любое время. Просто обратись ко мне", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        messagesAdapter.notifyDataSetChanged();
                    } else if (i == 1) {
                        startActivity(intent7);
                    }
                }
                if (spinner.getSelectedItemId() == 3){
                    if(i == 0){
                            messagesAdapter.add("СМАЙЛИК: Включаю радио");
                            player.play();
                            textToSpeech.speak("Включаю радио", TextToSpeech.QUEUE_FLUSH, null, null);
                            messagesAdapter.notifyDataSetChanged();

                    }
                    else if (i == 1){
                        messagesAdapter.add("СМАЙЛИК: Выключаю радио");
                        player.stop();
                        textToSpeech.speak("Выключаю радио", TextToSpeech.QUEUE_FLUSH, null, null);
                        messagesAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sounds = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @SuppressWarnings("deprecation")
    protected void createOldSoundPool() {

        sounds = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
    }

    protected void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }
    private void speakkk(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mainGeneral");
        } else {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SPEECH_REQUEST_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    comandToSend.setText(result.get(0));
                    String command = comandToSend.getText().toString();
                    messagesAdapter.add("Ты: " + command);
                    messagesAdapter.notifyDataSetChanged();
                    if (command.equalsIgnoreCase("список задач")) {

// Выполняем переход
                        startActivity(intent);
                    } else if (command.equalsIgnoreCase("включить уведомления") || command.equalsIgnoreCase("выключить уведомления")) {

// Выполняем переход
                        messagesAdapter.add("СМАЙЛИК: Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них");
                        speakkk("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них");
                        messagesAdapter.notifyDataSetChanged();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else if (command.equalsIgnoreCase("Мой баланс")) {
                        Context context = getApplicationContext();
                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        int balance = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
                        messagesAdapter.add("Ты: Мой баланс");
                        speakkk("У тебя на счету " + balance + " рублей");
                        messagesAdapter.add("СМАЙЛИК: У тебя на счету " + balance + " рублей");
                    } else if (command.equalsIgnoreCase("копилка")) {
                        startActivity(intent7);
                    }
                    else if (command.equalsIgnoreCase("Включи радио")){
                        messagesAdapter.add("СМАЙЛИК: Включаю радио");
                        textToSpeech.speak("Включаю радио", TextToSpeech.QUEUE_FLUSH, null, null);
                        player.play();
                        messagesAdapter.notifyDataSetChanged();
                    }
                    else if (command.equalsIgnoreCase("Выключи радио")){
                        messagesAdapter.add("СМАЙЛИК: Выключаю радио");
                        textToSpeech.speak("Выключаю радио", TextToSpeech.QUEUE_FLUSH, null, null);
                        player.stop();
                        messagesAdapter.notifyDataSetChanged();
                    }
                    else if (command.contains("напомни") || command.contains("Напомни")){
                        String[] message = command.split(" ");
                        String zapiska;
                        int min = 0;
                        int hour = 0;
                        int endZapiskaIndex = 0;
                        for (int i = 0; i < message.length; i++){
                            if (Objects.equals(message[i], "минут") || Objects.equals(message[i], "часов") || message[i].contains(":")){
                                endZapiskaIndex = i;
                                break;
                            }
                        }
//                        zapiska = String.join("", Arrays.copyOfRange(message, 1, endZapiskaIndex));
//                        if (command.contains("минут")){
//                            for (int i = 0; i < message.length; i++){
//                                if (Objects.equals(message[i], "минут")){
//                                    min = Integer.parseInt(message[i - 1]);
//                                    break;
//                                }
//                            }
//                        }
//                        if (command.contains("часов")){
//                            for (int i = 0; i < message.length; i++){
//                                if (Objects.equals(message[i], "часов")){
//                                    hour = Integer.parseInt(message[i - 1]);
//                                    break;
//                                }
//                            }
//                        }

                        Context context = getApplicationContext();
                        //EditText input = findViewById(R.id.editTextText);
                        //EditText inputTime = findViewById(R.id.editTextTime);
                        //String itemText = input.getText().toString() + " " + inputTime.getText().toString();
                        String DATE_FORMAT_NOW="d-M-yyyy";

                        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                        Calendar cal = Calendar.getInstance();
                        String timestamp = sdf.format(cal.getTime());
                        DateFormat formatter = new SimpleDateFormat("d-M-yyyy hh:mm");
                        String strDataTime = timestamp + " " + message[endZapiskaIndex];
                        Date date1 = null;
                        try {
                            date1 = formatter.parse(strDataTime);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        Intent notifyIntent = new Intent(this,MyReceiver.class);
                        notifyIntent.putExtra("name", command);
                        //notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast
                                (this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        //String str = inputTime.getText().toString();
                        //String[] strs = str.split(":");
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date1.getTime(),
                                pendingIntent);

//                        if(!(itemText.equals(""))){ TODO: чтобы сохранялось в книжечке сделать
//                            itemsAdapter.add(itemText);
//                            input.setText("");
//
//                            SharedPreferences sharedPref = context.getSharedPreferences(
//                                    getString(R.string.preference_file_key), Context.MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sharedPref.edit();
//                            try {
//                                editor.putString("TASKS", ObjectSerializer.serialize(items));
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            editor.apply();
//                        }


                    } else {
                        //stopVoiceTriggerService();
                        messagesAdapter.add("*СМАЙЛИК ДУМАЕТ* (иногда может быть перегруз серверов)");
                        Thread gfgThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String senten = generateContent(modelId, command);
                                    System.out.println(senten);
                                    speakkk(senten);
                                    messagesAdapter.add("СМАЙЛИК: " + senten);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        gfgThread.start();

                    }

                }
                break;
            }
        }
    }
}