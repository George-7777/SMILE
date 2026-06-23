package com.gvayt.smile.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.gvayt.smile.R;
import com.gvayt.smile.contract.ParentTasksContract;
import com.gvayt.smile.di.ParentTasksPresenterFactory;
import com.gvayt.smile.model.network.dto.kid.KidResponse;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;
import com.gvayt.smile.ui.adapters.TaskAdapter;
import com.gvayt.smile.ui.fragments.AddTaskBottomSheetFragment;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ParentTasksActivity extends AppCompatActivity implements ParentTasksContract.View, AddTaskBottomSheetFragment.OnTaskAddedListener {
    private ImageButton exitButton;
    private TextView infoKid;
    private TextView countTasks;
    private TextView errorText;
    private RecyclerView tasksListUi;
    private MaterialButton addTaskButton;

    private List<TaskResponse> tasksList = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private ParentTasksContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_tasks);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        exitButton = findViewById(R.id.btn_exit);
        infoKid = findViewById(R.id.tv_kid_info);
        countTasks = findViewById(R.id.tv_tasks_count);
        errorText = findViewById(R.id.error_text_kid_tasks);
        tasksListUi = findViewById(R.id.rv_tasks);
        addTaskButton = findViewById(R.id.btn_add_task);

        presenter = ParentTasksPresenterFactory.create(this, this);

        presenter.onViewCreate(getIntent().getStringExtra("LOGIN_KID"));

        taskAdapter = new TaskAdapter(tasksList, task -> {
            presenter.onDeleteTaskClick(task.getId());
        });
        tasksListUi.setAdapter(taskAdapter);
        exitButton.setOnClickListener(view -> presenter.onButtonExitClick());
        addTaskButton.setOnClickListener(view -> presenter.onAddTaskClick());
    }

    @Override
    public void showTasks(List<TaskResponse> taskListNew) {
        errorText.setVisibility(GONE);
        tasksList.clear();
        tasksList.addAll(taskListNew);
        taskAdapter.notifyDataSetChanged();
        countTasks.setText(getString(R.string.tasks_count_for_parent, (long) taskListNew.size()));
    }

    @Override
    public void showKidInfo(KidResponse kidResponse) {
        errorText.setVisibility(GONE);
        infoKid.setText(getString(R.string.name_of_kid_tasks, kidResponse.getFio()));
    }

    @Override
    public void showNetworkError() {
        errorText.setVisibility(VISIBLE);
        errorText.setText(R.string.network_error);
    }

    @Override
    public void showServerError() {
        errorText.setVisibility(VISIBLE);
        errorText.setText(R.string.server_error);
    }

    @Override
    public void successAddTask() {
        errorText.setVisibility(GONE);
        Toast.makeText(this, R.string.task_added, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void successDeleteTask() {
        errorText.setVisibility(GONE);
        Toast.makeText(this, R.string.task_deleted, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void exitActivity() {
        Intent intent = new Intent(this, ParentActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void showAddTaskDialog() {
        AddTaskBottomSheetFragment fragment = new AddTaskBottomSheetFragment();
        fragment.setOnTaskAddedListener(this);
        fragment.show(getSupportFragmentManager(), "AddTaskBottomSheet");
    }

    @Override
    public void onTaskAdded(String name, String time) {
        presenter.confirmAddTask(new TaskRequest(name, LocalTime.parse(time)));
    }
}