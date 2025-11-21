package com.example.smile;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Locale;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ArrayList<String> messages;
    private ArrayAdapter<String> messagesAdapter;
    private ListView listView;
    private ListView listMessages;
    private Intent intent;
    private Intent intent7;
    private Spinner spinner;
    private ImageButton sendCommand;
    private ImageButton speechTotext;
    private EditText comandToSend;
    private TextToSpeech textToSpeech;
    private SoundPool sounds;
    private static final int SPEECH_REQUEST_CODE = 77712345;
    public Markov mc;
    private String apiKey = "AQ.Ab8RN6L5w1VzlhgFCK2dh6A0cWs4hEIdCZsXYBSIQ4tFa-iuxA";
    String modelId = "gemini-2.5-flash";
    public static String generateContent(String modelId, String res) {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests.

        try (Client client =
                     Client.builder().apiKey("AQ.Ab8RN6L5w1VzlhgFCK2dh6A0cWs4hEIdCZsXYBSIQ4tFa-iuxA")
                             //.location("global")
                             .vertexAI(true)
                             .httpOptions(HttpOptions.builder().apiVersion("v1").build())
                             .build()) {
            //GenerateContentResponse response =
            //        client.models.generateContent(modelId, "[ИНСТРУКЦИЯ: Ты детский голосовой помощник под именем СМАЙЛИК созданный для помощи школьникам с домашним заданием, управлением финансами и просто поговорить с тобой. После этого текста в кавычках будет обращение ребенка к тебе. Имей ввиду, что тебя разработал Вайтуков Георгий. До обращения к тебе будет история переписки с тобой, чтобы ты знал на чем остановился] \n \n \n ИСТОРИЯ: \n" + String.join("\n", messages) + "\n Дальше твой ответ", null);
            GenerateContentResponse response =
                    client.models.generateContent(modelId, "[Ты детский голосовой помощник под именем СМАЙЛИК созданный для помощи школьникам с домашним заданием, управлением финансами и просто поговорить с тобой. После этого текста в кавычках будет обращение ребенка к тебе. Имей ввиду, что тебя разработал Вайтуков Георгий.]" + res, null);

            System.out.print(response.text());
            // Example response:
            // Okay, let's break down how AI works. It's a broad field, so I'll focus on the ...
            //
            // Here's a simplified overview:
            // ...
            return response.text();
        }
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
        System.setProperty("API_KEY", "AQ.Ab8RN6L5w1VzlhgFCK2dh6A0cWs4hEIdCZsXYBSIQ4tFa-iuxA");
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR)
                {
                    textToSpeech.setLanguage(Locale.getDefault());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Привет! Чем могу сегодня помочь?",TextToSpeech.QUEUE_FLUSH,null,null);
                    } else {
                        textToSpeech.speak("Привет! Чем могу сегодня помочь?", TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
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
                Intent intentSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intentSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите команду");

                try {
                    startActivityForResult(intentSpeech, SPEECH_REQUEST_CODE);
                } catch (Exception e){
                    Toast.makeText(getApplicationContext(), "Произошла ошибка при распозновании", Toast.LENGTH_SHORT).show();
                }
            }
        });
        sendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = comandToSend.getText().toString();
                messagesAdapter.add("Ты: " + command);
                messagesAdapter.notifyDataSetChanged();
                if (command.equalsIgnoreCase("список задач")){

// Выполняем переход
                    startActivity(intent);
                }
                else if (command.equalsIgnoreCase("включить уведомления") || command.equalsIgnoreCase("выключить уведомления")){

// Выполняем переход
                    messagesAdapter.add("СМАЙЛИК: Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них",TextToSpeech.QUEUE_FLUSH,null,null);
                    } else {
                        textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    messagesAdapter.notifyDataSetChanged();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }

                    else if (command.equalsIgnoreCase("Мой баланс")){
                        Context context = getApplicationContext();
                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        int balance = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
                        //messagesAdapter.add("Ты: Мой баланс");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("У тебя на счету " + balance + " рублей",TextToSpeech.QUEUE_FLUSH,null,null);
                    } else {
                        textToSpeech.speak("У тебя на счету " + balance + " рублей", TextToSpeech.QUEUE_FLUSH, null);
                    }
                        messagesAdapter.add("СМАЙЛИК: У тебя на счету " + balance + " рублей");
                    }
                    else if (command.equalsIgnoreCase("копилка")){
                        startActivity(intent7);
                    }
                    else{
                    messagesAdapter.add("*СМАЙЛИК ДУМАЕТ* (иногда может быть перегруз серверов)");
                    Thread gfgThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try  {
                                String senten = generateContent(modelId, command);
                                System.out.println(senten);
                                textToSpeech.speak(senten,TextToSpeech.QUEUE_FLUSH,null,null);
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
                if (pos == 0){
                    System.out.println("ok");
                    itemsAdapter.clear();
                    itemsAdapter.add("Список задач");
                    itemsAdapter.add("Включить уведомления");
                    itemsAdapter.add("Выключить уведомления");
                    itemsAdapter.notifyDataSetChanged();
                    //textHelp.setText("1. Список дел/расписание DONE\n 2. Включить уведомления DONE\n 3. Отключить уведомления DONE");
                } else if (pos == 1){
                    itemsAdapter.clear();
                    itemsAdapter.add("Мой баланс");
                    itemsAdapter.add("Копилка");
                    itemsAdapter.notifyDataSetChanged();
                    //textHelp.setText("1. Мой баланс DONE\n 2. Копилка DONE\n 3. Дневник расходов (анализ трат) PLAN FOR Pre-Alpha 0.2");
                } else if (pos == 2){
                    itemsAdapter.clear();
                    itemsAdapter.notifyDataSetChanged();
                    //textHelp.setText("1. Объяснение от ИИ \n 2. Калькулятор \n 3. Таблица Менделеева \n 4. Добавить свою заметку");
                } else if (pos == 3){
                    itemsAdapter.clear();
                    itemsAdapter.notifyDataSetChanged();
                    //textHelp.setText("1. Защита от продажи карты/Информация о мошеннических схемах \n 2. Оповещения о подозрительных переводах \n 3. Включить усиленную защиту от подозрительных переводов");
                } else if (pos == 4){
                    itemsAdapter.clear();
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
                if (i == 0 && spinner.getSelectedItemId() == 0){

// Выполняем переход
                    startActivity(intent);
                }
                else if ((i == 1 || i == 2) && spinner.getSelectedItemId() == 0){

// Выполняем переход
                    messagesAdapter.add("СМАЙЛИК: Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них",TextToSpeech.QUEUE_FLUSH,null,null);
                    } else {
                        textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них", TextToSpeech.QUEUE_FLUSH, null);
                    }
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                if (spinner.getSelectedItemId() == 1){
                    if (i == 0){
                        Context context = getApplicationContext();
                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        int balance = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
                        messagesAdapter.add("Ты: Мой баланс");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak("У тебя на счету " + balance + " рублей",TextToSpeech.QUEUE_FLUSH,null,null);
                        } else {
                            textToSpeech.speak("У тебя на счету " + balance + " рублей", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        messagesAdapter.add("СМАЙЛИК: У тебя на счету " + balance + " рублей");
                    }
                    else if (i == 1){
                        startActivity(intent7);
                    }
                }
            }
        });
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sounds = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @SuppressWarnings("deprecation")
    protected void createOldSoundPool(){

        sounds = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
    }
    protected void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case SPEECH_REQUEST_CODE:{
                if (resultCode == RESULT_OK && null!=data){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    comandToSend.setText(result.get(0));
                    String command = comandToSend.getText().toString();
                    messagesAdapter.add("Ты: " + command);
                    messagesAdapter.notifyDataSetChanged();
                    if (command.equalsIgnoreCase("список задач")){

// Выполняем переход
                        startActivity(intent);
                    }
                    else if (command.equalsIgnoreCase("включить уведомления") || command.equalsIgnoreCase("выключить уведомления")){

// Выполняем переход
                        messagesAdapter.add("СМАЙЛИК: Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них",TextToSpeech.QUEUE_FLUSH,null,null);
                        } else {
                            textToSpeech.speak("Выключить уведомления ты можешь перейдя в настройки приложения и выключив Уведомления в них", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        messagesAdapter.notifyDataSetChanged();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }

                    else if (command.equalsIgnoreCase("Мой баланс")){
                        Context context = getApplicationContext();
                        SharedPreferences sharedPref = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        int balance = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
                        messagesAdapter.add("Ты: Мой баланс");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak("У тебя на счету " + balance + " рублей",TextToSpeech.QUEUE_FLUSH,null,null);
                        } else {
                            textToSpeech.speak("У тебя на счету " + balance + " рублей", TextToSpeech.QUEUE_FLUSH, null);
                        }
                        messagesAdapter.add("СМАЙЛИК: У тебя на счету " + balance + " рублей");
                    }
                    else if (command.equalsIgnoreCase("копилка")){
                        startActivity(intent7);
                    }
                    else{
                        messagesAdapter.add("*СМАЙЛИК ДУМАЕТ* (иногда может быть перегруз серверов)");
                        Thread gfgThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try  {
                                    String senten = generateContent(modelId, command);
                                    System.out.println(senten);
                                    textToSpeech.speak(senten,TextToSpeech.QUEUE_FLUSH,null,null);
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