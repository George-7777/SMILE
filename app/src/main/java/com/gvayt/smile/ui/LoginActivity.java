package com.gvayt.smile.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.gvayt.smile.R;
import com.gvayt.smile.contract.LoginContract;
import com.gvayt.smile.di.LoginPresenterFactory;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {
    private enum State {
        LOGIN,
        REGISTER
    }
    private TextView welcomeText;
    private TextView errorText;
    private EditText etLogin;
    private EditText etPassword;
    private RadioGroup radioRole;
    private EditText etFio;
    private Button btnLogin;
    private Button btnNoInternet;
    private TextView switchLoginRegister;
    private TextView commentForKids;
    private State currentState = State.LOGIN;

    private LoginContract.Presenter loginPresenter; // DepInj

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        welcomeText = findViewById(R.id.tv_title);
        errorText = findViewById(R.id.tv_error);
        etLogin = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        radioRole = findViewById(R.id.radio_role);
        etFio = findViewById(R.id.et_fio);
        btnLogin = findViewById(R.id.btn_login);
        btnNoInternet = findViewById(R.id.btn_no_internet);
        switchLoginRegister = findViewById(R.id.tv_switch_mode);
        commentForKids = findViewById(R.id.btn_no_internet);

        loginPresenter = LoginPresenterFactory.create(this, this);

        loginPresenter.onViewCreated();
        btnLogin.setOnClickListener(view -> {
            if (currentState == State.LOGIN) {
                LoginContract.RoleUser role = LoginContract.RoleUser.KID;
                if (radioRole.getCheckedRadioButtonId() == R.id.secondR) {
                    role = LoginContract.RoleUser.PARENT;
                }
                loginPresenter.onButtonLoginClick(etLogin.getText().toString(), etPassword.getText().toString(), role);
            }
            else {
                loginPresenter.onButtonRegisterClick(etLogin.getText().toString(), etFio.getText().toString(), etPassword.getText().toString());
            }
        });
        btnNoInternet.setOnClickListener(view -> loginPresenter.onButtonAnonClick());
        switchLoginRegister.setOnClickListener(view -> loginPresenter.onSwitchMode(currentState == State.REGISTER));
    }

    // =======================IMPL=======================

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showLoginSuccess() {
        errorText.setVisibility(GONE);
        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginFailed() {
        errorText.setVisibility(VISIBLE);
        errorText.setText(R.string.login_failed);
        Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRegisterSuccess() {
        errorText.setVisibility(GONE);
        Toast.makeText(this, R.string.register_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showRegisterFailed() {
        errorText.setVisibility(VISIBLE);
        errorText.setText(R.string.register_failed);
        Toast.makeText(this, R.string.register_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showServerError() {
        errorText.setVisibility(VISIBLE);
        errorText.setText(R.string.server_error);
        Toast.makeText(this, R.string.server_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNetworkError() {
        errorText.setVisibility(VISIBLE);
        errorText.setText(R.string.network_error);
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void switchToRegisterMode() {
        currentState = State.REGISTER;
        welcomeText.setText(R.string.auth_title);
        commentForKids.setVisibility(VISIBLE);
        etLogin.setHint(R.string.hint_login_email);
        etFio.setVisibility(VISIBLE);
        radioRole.setVisibility(GONE);
        btnLogin.setText(R.string.register_button);
        switchLoginRegister.setText(R.string.switch_to_login);
    }

    @Override
    public void switchToLoginMode() {
        currentState = State.LOGIN;
        welcomeText.setText(R.string.auth_title_again);
        etLogin.setHint(R.string.hint_login);
        commentForKids.setVisibility(GONE);
        etFio.setVisibility(GONE);
        radioRole.setVisibility(VISIBLE);
        btnLogin.setText(R.string.login_button);
        switchLoginRegister.setText(R.string.switch_to_register);
    }

    @Override
    public void redirectToParentActivity() {
        Intent intent = new Intent(this, ParentActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void redirectToKidActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void redirectToAnonKid() {
        // Пока-что переход к авторизованному ребенку и анонимному никак не отличается
        redirectToKidActivity();
    }
}