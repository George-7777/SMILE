package com.gvayt.smile.ui.commandsView;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.gvayt.smile.contract.TaskContract;
import com.gvayt.smile.R;
import com.gvayt.smile.di.TaskPresenterFactory;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;
import com.gvayt.smile.ui.adapters.TaskAdapter;
import com.gvayt.smile.ui.fragments.AddTaskBottomSheetFragment;

import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends AppCompatActivity implements TaskContract.View, AddTaskBottomSheetFragment.OnTaskAddedListener {

    private List<TaskResponse> tasksList = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private RecyclerView tasksListUi;
    private MaterialButton buttonAddTask;
    private MaterialButton buttonBack;

    private TaskContract.Presenter presenter;

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
        tasksListUi = findViewById(R.id.ListView);
        buttonAddTask = findViewById(R.id.button);
        buttonBack = findViewById(R.id.buttonBack);

        presenter = TaskPresenterFactory.create(this, this);



        tasksListUi.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(tasksList, task -> presenter.onTaskItemLongClick(tasksList.indexOf(task)));
        tasksListUi.setAdapter(taskAdapter);
        setUpListViewListener();

        presenter.onViewCreated();
    }

    private void setUpListViewListener() {
        buttonAddTask.setOnClickListener(view -> presenter.onAddTaskButtonClicked());
        buttonBack.setOnClickListener(view -> presenter.onButtonExitClicked());
    }
    // IMPL

    @Override
    public void showTasks(List<TaskResponse> tasks) {
        tasksList.clear();
        tasksList.addAll(tasks);

        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void showAddTaskDialog() {
        AddTaskBottomSheetFragment fragment = new AddTaskBottomSheetFragment();
        fragment.setOnTaskAddedListener(this);
        fragment.show(getSupportFragmentManager(), "AddTaskBottomSheet");
    }

    @Override
    public void showError(int message) {
        showToast(message);
    }

    @Override
    public void showSuccess(int message) {
        showToast(message);
    }

    private void showToast(int msg) {
        Toast.makeText(this, getString(msg), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateBack() {
        finish();
    }

    @Override
    public void addTaskToUi(TaskResponse newTask) {
        tasksList.add(newTask);
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeTaskFromUi(int taskId) {
        tasksList.remove(taskId);
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskAdded(String name, String time) {
        presenter.addTask(new TaskRequest(name, time));
    }
    public void checkExactAlarmPermissionAndRequest() {
        Context context = getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if(!(alarmManager != null && alarmManager.canScheduleExactAlarms())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

}