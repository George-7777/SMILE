package com.example.smile.ui.commandsView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smile.R;
import com.example.smile.ui.MainActivity;

public class CopilkaAndBalance extends AppCompatActivity {
    Spinner spinner;
    LinearLayout ln1;
    LinearLayout ln2;
    LinearLayout ln3;
    LinearLayout ln4;
    Button button;
    TextView balance;
    EditText toAdd;
    EditText toMinus;
    EditText toCopilkaAdd;
    EditText missia;
    Button add;
    Button minus;
    Button missiaAdd;
    Button copilkaAdd;
    Button copilkaStop;
    TextView coplika;
    TextView copilkaMissia;

    int balanceI;
    String goal;
    int copilkaBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_copilka_and_balance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        button = findViewById(R.id.button8);
        ln1 = findViewById(R.id.linearLayout10);
        ln2 = findViewById(R.id.linearLayout20);
        ln3 = findViewById(R.id.linearLayout30);
        ln4 = findViewById(R.id.linearLayout40);
        spinner = (Spinner) findViewById(R.id.spinner2);
        add = findViewById(R.id.addBalance);
        minus = findViewById(R.id.noAddBalance);
        missiaAdd = findViewById(R.id.buttonMissia);
        copilkaAdd = findViewById(R.id.addCopilka);
        copilkaStop = findViewById(R.id.getCopilka);
        balance = findViewById(R.id.textView2);
        toAdd = findViewById(R.id.editTextNumberDecimal);
        toMinus = findViewById(R.id.editTextNumberDecimal2);
        toCopilkaAdd = findViewById(R.id.editTextNumberDecimal23);
        missia = findViewById(R.id.editTextText2);
        coplika = findViewById(R.id.copilkaText);
        copilkaMissia = findViewById(R.id.missiaText);
        Context context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        balanceI = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
        copilkaBalance = Integer.parseInt(sharedPref.getString("COPILKA_BALANCE", "0"));
        goal = sharedPref.getString("GOAL_COPILKA", "не задана");
        balance.setText("Баланс карты: " + Integer.toString(balanceI) + " рублей");
        copilkaMissia.setText("Цель " + goal);
        coplika.setText("В копилке " + Integer.toString(copilkaBalance) + " рублей");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                balanceI = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
                int toAddd = Integer.parseInt(toAdd.getText().toString());
                int itog = balanceI + toAddd;
                editor.putString("BALANCE", Integer.toString(itog));
                balanceI = itog;
                editor.apply();
                balance.setText("Баланс карты: " + Integer.toString(itog) + " рублей");
            }
        });
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                balanceI = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
                int toAddd = Integer.parseInt(toMinus.getText().toString());
                int itog = balanceI - toAddd;
                editor.putString("BALANCE", Integer.toString(itog));
                balanceI = itog;
                editor.apply();
                balance.setText("Баланс карты: " + Integer.toString(itog) + " рублей");
            }
        });
        missiaAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                goal = missia.getText().toString();
                editor.putString("GOAL_COPILKA", goal);
                editor.apply();
                copilkaMissia.setText("Цель " + goal);
            }
        });
        copilkaAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copilkaBalance = Integer.parseInt(sharedPref.getString("COPILKA_BALANCE", "0"));
                int toAddd = Integer.parseInt(toCopilkaAdd.getText().toString());
                if (balanceI >= toAddd) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    int itog = copilkaBalance + toAddd;
                    copilkaBalance = itog;
                    editor.putString("COPILKA_BALANCE", Integer.toString(itog));
                    coplika.setText("В копилке " + Integer.toString(itog) + " рублей");
                    balanceI = Integer.parseInt(sharedPref.getString("BALANCE", "1000"));
                    itog = balanceI - toAddd;
                    editor.putString("BALANCE", Integer.toString(itog));
                    balanceI = itog;
                    editor.apply();
                    balance.setText("Баланс карты: " + Integer.toString(itog) + " рублей");
                }
            }
        });
        copilkaStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPref.edit();
                int itog = copilkaBalance + balanceI;
                editor.putString("BALANCE", Integer.toString(itog));
                balance.setText("Баланс карты: " + Integer.toString(itog) + " рублей");
                editor.putString("COPILKA_BALANCE", "0");
                copilkaBalance = 0;
                balanceI = itog;
                editor.apply();
                coplika.setText("В копилке 0 рублей");
            }
        });

// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.group_roles,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0){
                    ln1.setVisibility(VISIBLE);
                    ln2.setVisibility(VISIBLE);
                    ln3.setVisibility(GONE);
                    ln4.setVisibility(GONE);
                }
                else if (pos == 1){
                    ln3.setVisibility(VISIBLE);
                    ln4.setVisibility(VISIBLE);
                    ln1.setVisibility(GONE);
                    ln2.setVisibility(GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Intent intent = new Intent(this, MainActivity.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });

    }
}