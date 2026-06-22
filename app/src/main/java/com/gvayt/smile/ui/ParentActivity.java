package com.gvayt.smile.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.gvayt.smile.R;
import com.gvayt.smile.contract.ParentContract;
import com.gvayt.smile.di.ParentPresenterFactory;
import com.gvayt.smile.model.network.dto.kid.KidResponse;
import com.gvayt.smile.ui.adapters.KidAdapter;

import java.util.ArrayList;
import java.util.List;

public class ParentActivity extends AppCompatActivity implements ParentContract.View {
    private ImageButton logoutButton;
    private TextView logoutText;
    private TextView welcomeText;
    private TextView errorText;
    private TextView yourKidsText;
    private RecyclerView kidListUi;
    private MaterialButton addKid;
    private List<KidResponse> kidsList;
    private KidAdapter kidAdapter;
    private ParentContract.Presenter parentPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        logoutButton = findViewById(R.id.btn_logout);
        logoutText = findViewById(R.id.logout_text);
        welcomeText = findViewById(R.id.welcome_text_parent);
        errorText = findViewById(R.id.error_text_parent);
        yourKidsText = findViewById(R.id.your_kids);
        kidListUi = findViewById(R.id.kids_list);
        addKid = findViewById(R.id.btn_add_child);
        kidsList = new ArrayList<>();

        parentPresenter = ParentPresenterFactory.create(this, this);

        parentPresenter.onViewCreate();

        kidListUi.setLayoutManager(new LinearLayoutManager(this));
        kidAdapter = new KidAdapter(kidsList, new KidAdapter.OnChildClickListener() {
            @Override
            public void onChildClick(KidResponse child) {
                parentPresenter.onKidClick(child.getLogin());
            }

            @Override
            public void onDeleteClick(KidResponse child) {
                // бедный ребенок
            }
        });
        kidListUi.setAdapter(kidAdapter);

        logoutButton.setOnClickListener(view -> parentPresenter.onButtonLogoutClick());
        addKid.setOnClickListener(view -> parentPresenter.onButtonAddKidClick());
    }

    @Override
    public void showParentInfo(String fio) {
        errorText.setVisibility(GONE);
        welcomeText.setText(getString(R.string.template_welcome_parent, fio));
    }

    @Override
    public void showNetworkError() {
        errorText.setVisibility(VISIBLE);
    }

    @Override
    public void showKidsList(List<KidResponse> kidsListGet) {
        errorText.setVisibility(GONE);
        kidsList.clear();
        kidsList.addAll(kidsListGet);
        kidAdapter.notifyDataSetChanged();
    }

    @Override
    public void showRegistrationDialog() {
        System.out.println("Регистрация ребенка...");
        Intent intent = new Intent(this, KidRegisterActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showTasksDialog(String usernameKid) {
        System.out.println("Открываем задания у " + usernameKid);
        // задания ребенка
    }

    @Override
    public void exitActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}