package com.gvayt.smile.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.gvayt.smile.R;
import com.gvayt.smile.contract.KidRegisterContract;
import com.gvayt.smile.di.KidRegisterPresenterFactory;

public class KidRegisterActivity extends AppCompatActivity implements KidRegisterContract.View {
    private ImageButton logoutBtn;
    private TextView textError;
    private EditText usernameEnter;
    private EditText fioEnter;
    private EditText passwordEnter;
    private MaterialButton buttonRegister;

    private KidRegisterContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_kid_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logoutBtn = findViewById(R.id.btn_logout);
        textError = findViewById(R.id.tv_error);
        usernameEnter = findViewById(R.id.et_username);
        fioEnter = findViewById(R.id.et_fio);
        passwordEnter = findViewById(R.id.et_password);
        buttonRegister = findViewById(R.id.btn_register);

        presenter = KidRegisterPresenterFactory.create(this, this);

        logoutBtn.setOnClickListener(view -> presenter.onButtonReturnClick());
        buttonRegister.setOnClickListener(
                view -> presenter.onButtonRegisterClick(
                        usernameEnter.getText().toString(),
                        fioEnter.getText().toString(),
                        passwordEnter.getText().toString()
                )
        );
    }

    @Override
    public void showRegisterSuccess() {
        textError.setVisibility(GONE);
        Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRegisterFailed() {
        textError.setVisibility(VISIBLE);
        textError.setText(R.string.register_kid_error);
        Toast.makeText(this, R.string.register_kid_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showServerError() {
        textError.setVisibility(VISIBLE);
        textError.setText(R.string.server_error);
    }

    @Override
    public void showNetworkError() {
        textError.setVisibility(VISIBLE);
        textError.setText(R.string.network_error);
    }

    @Override
    public void returnToParentActivity() {
        Intent intent = new Intent(this, ParentActivity.class);
        startActivity(intent);
        finish();
    }
}