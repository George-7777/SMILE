package com.gvayt.smile.ui;

import static com.gvayt.smile.Constant.WAKE_WORD;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
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

import com.gvayt.smile.R;
import com.gvayt.smile.contract.MainContract;
import com.gvayt.smile.di.MainPresenterFactory;
import com.gvayt.smile.model.tts.TTSManager;
import com.gvayt.smile.services.VoiceTriggerService;
import com.gvayt.smile.model.tts.TTSManagerDefault;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity implements MainContract.View {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private BroadcastReceiver triggerReceiver;
    private ArrayAdapter<String> itemsAdapter;
    private ArrayAdapter<String> messagesAdapter;
    private ListView listView;
    private EditText commandToSend;
    private TTSManager ttsManager;
    private static final int SPEECH_REQUEST_CODE = 77712345;
    private MainContract.Presenter presenter;

    // ЗАПРОС РАЗРЕШЕНИЙ

    /**
    * Запрашивает все нужные разрешения.
     */
    public void checkPermissions() {
        String[] permissions = getPermissions();

        List<String> permissionsNeeded = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            requestPermissions(
                    permissions,
                    PERMISSION_REQUEST_CODE
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (!nm.canUseFullScreenIntent()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);

                Toast.makeText(this, "Пожалуйста, разрешите приложению показывать полноэкранные уведомления для работы SOS-функции", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static String[] getPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.FOREGROUND_SERVICE,
                    Manifest.permission.CALL_PHONE
            };
        }
        else {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CALL_PHONE
            };
        }
        return permissions;
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

    // Регистрация сервиса распознования речи
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerTriggerReceiver() {
        triggerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (VoiceTriggerService.ACTION_TRIGGER_DETECTED.equals(intent.getAction())) {
                    String spokenText = intent.getStringExtra(
                            VoiceTriggerService.EXTRA_TRIGGER_PHRASE
                    );
                    if (Objects.equals(spokenText, WAKE_WORD))
                        presenter.onWakeWordDetected();
                }
            }
        };

        IntentFilter filter = new IntentFilter(VoiceTriggerService.ACTION_TRIGGER_DETECTED);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(triggerReceiver, filter, RECEIVER_EXPORTED);
        } else {
            registerReceiver(triggerReceiver, filter);
        }
    }

    public void startVoiceTriggerService() {
        registerTriggerReceiver();
        VoiceTriggerService.startService(this);
        Toast.makeText(this, "Служба запущена", Toast.LENGTH_SHORT).show();
    }

    public void stopVoiceTriggerService() {
        VoiceTriggerService.stopService(this);
        Toast.makeText(this, "Служба остановлена", Toast.LENGTH_SHORT).show();
    }
    // Жизненный цикл активити

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
        //stopVoiceTriggerService();
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
        createNewSoundPool();

        // получение вью нижнего экрана
        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
        ImageButton speechToText = findViewById(R.id.imageButton);
        ListView listMessages = findViewById(R.id.listChat);
        listView = findViewById(R.id.listCommands);
        ArrayList<String> messages = new ArrayList<>();
        messagesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        ArrayList<String> items = new ArrayList<>();
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);
        listMessages.setAdapter(messagesAdapter);
        commandToSend = findViewById(R.id.editTextText3);
        ImageButton sendCommand = findViewById(R.id.imageButton5);
        setUpListViewListener();

        speechToText.setOnClickListener(v -> presenter.onVoiceButtonClicked());
        sendCommand.setOnClickListener(v -> {
            String command = commandToSend.getText().toString();
            if (command.isEmpty()) return;
            presenter.onCommandEntered(command);
        });

// настройка поведения нижнего экрана
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        ImageButton buttonExpand = findViewById(R.id.imageButton4);
        buttonExpand.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

// настройка максимальной высоты
        bottomSheetBehavior.setPeekHeight(300);

// настройка возможности скрыть элемент при свайпе вниз
        bottomSheetBehavior.setHideable(false);

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.group_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
// настройка колбэков при изменениях
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {}
            @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
        class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                presenter.onSpinnerCategorySelected(pos);
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        }
        spinner.setOnItemSelectedListener(new SpinnerActivity());

        // init tts and presenter

        presenter = MainPresenterFactory.create(this, this);
        presenter.onViewCreate(getIntent().getBooleanExtra("ANON_MODE", true));

        ttsManager = new TTSManagerDefault(this, new TTSManagerDefault.TTSListener() {
            @Override public void onInit() { presenter.onTTSInit(); }
            @Override public void onSpeakStart(String utteranceId) {}
            @Override public void onSpeakDone(String utteranceId) {}
            @Override public void onError(String utteranceId) {}
        }, Locale.forLanguageTag("ru"));
    }
    // Вспомогательные методы

    private void setUpListViewListener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCommand = itemsAdapter.getItem(position);
            if (selectedCommand != null && !selectedCommand.isEmpty()) {
                presenter.onCommandEntered(selectedCommand);
            }
        });
    }

    protected void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String command = result.get(0);
                presenter.onCommandEntered(command);
            }
        }
    }
    // IMPL

    @Override
    public void showServerError() {
        Toast.makeText(this, R.string.server_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showNetworkError() {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showAnonMode() {
        addDialog(R.string.anon_mode, MainContract.DialogRole.SYSTEM);
    }

    @Override
    public void addDialog(int text, MainContract.DialogRole author) {
        String role = "";
        switch (author) {
            case USER:
                role = getString(R.string.dialog_you);
                break;
            case SMILE:
                role = getString(R.string.dialog_smile);
                ttsManager.speak(getString(text));
                break;
            case SYSTEM:
                role = "";
                break;
        }
        messagesAdapter.add(role + getString(text));
        messagesAdapter.notifyDataSetChanged();
    }
    @Override
    public void addDialog(String text, MainContract.DialogRole author) {
        String role = "";
        switch (author) {
            case USER:
                role = getString(R.string.dialog_you);
                break;
            case SMILE:
                role = getString(R.string.dialog_smile);
                ttsManager.speak(text);
                break;
            case SYSTEM:
                role = "";
                break;
        }
        messagesAdapter.add(role + text);
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void showCommands(List<Integer> commands) {
        itemsAdapter.clear();
        itemsAdapter.addAll(commands.stream().map(this::getString).collect(Collectors.toList()));
        itemsAdapter.notifyDataSetChanged();
    }

    @Override
    public void startVoiceRecognition() {
        Intent intentSpeech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intentSpeech.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intentSpeech.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.intent_speech_comment));

        try {
            startActivityForResult(intentSpeech, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.intent_speech_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void logout() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}