package com.gvayt.smile.presenter;

import android.util.Log;

import com.gvayt.smile.R;
import com.gvayt.smile.commands.commandsScripts.reminder.ReminderData;
import com.gvayt.smile.commands.commandsScripts.reminder.ReminderScheduler;
import com.gvayt.smile.contract.TaskContract;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskPresenter implements TaskContract.Presenter {
    private static final String TAG = "TasksPresenter";

    private final TaskContract.View view;
    private final TaskContract.Model model;
    private List<TaskResponse> currentTasks;
    private final ReminderScheduler reminderScheduler;

    public TaskPresenter(TaskContract.View view, TaskContract.Model model, ReminderScheduler reminderScheduler) {
        this.view = view;
        this.model = model;
        this.reminderScheduler = reminderScheduler;
        this.currentTasks = new ArrayList<>();
    }

    @Override
    public void onViewCreated() {
        view.checkExactAlarmPermissionAndRequest();
        List<TaskResponse> localTasks = tryGetTaskLocal();
        System.out.println(localTasks.isEmpty());
        if (!localTasks.isEmpty()) {
            view.showTasks(localTasks);
            currentTasks = localTasks;
            registerNotificationsForTasks(localTasks);
        }
        if(!model.isLogged()) {
            view.showSuccess(R.string.anon_mode);
            return;
        }
        model.getTasks(new ModelCallback<>() {
            @Override
            public void onSuccess(List<TaskResponse> tasks) {
                currentTasks = tasks;
                view.showTasks(tasks);
                trySaveTaskLocal(tasks);
                view.showSuccess(R.string.toast_tasks_update);
                registerNotificationsForTasks(tasks);
            }

            @Override
            public void onError(TypeApiError typeApiError) { showError(typeApiError); }
        });
    }

    @Override
    public void onAddTaskButtonClicked() {
        view.showAddTaskDialog();
    }

    @Override
    public void addTask(TaskRequest taskRequest) {
        if (model.isLogged()) {
            model.addTask(taskRequest, new ModelCallback<>() {
                @Override
                public void onSuccess(TaskResponse newTask) {
                    if (currentTasks != null) {
                        currentTasks.add(newTask);
                    }
                    view.addTaskToUi(newTask);
                    trySaveTaskLocal(currentTasks);
                    registerNotificationForTask(newTask);

                    view.showSuccess(R.string.toast_task_added_success_kid);
                }

                @Override
                public void onError(TypeApiError typeApiError) {
                    showError(typeApiError);
                    createTaskLocal(taskRequest);
                }
            });
        } else {
            createTaskLocal(taskRequest);
        }
    }

    @Override
    public void onTaskItemLongClick(int pos) {
        TaskResponse t = currentTasks.get(pos);
        deleteTaskLocal(pos);
        if (model.isLogged()) {
            model.deleteTask(t.getId(), new ModelCallback<>() {
                @Override
                public void onSuccess(Void result) {
                    view.showSuccess(R.string.toast_task_deleted_success_kid);
                }

                @Override
                public void onError(TypeApiError error) {
                    showError(error);
                }
            });
        }
    }

    @Override
    public void onButtonExitClicked() {
        view.navigateBack();
    }

    // private methods

    private void trySaveTaskLocal(List<TaskResponse> tasks) {
        try {
            model.saveTasksLocally(tasks);
        } catch (IOException e) {
            view.showError(R.string.toast_noname_error);
        }
    }

    private List<TaskResponse> tryGetTaskLocal() {
        try {
            System.out.println("ищем ищем");
            return model.getTasksLocally();
        } catch (IOException e) {
            System.out.println("ОБШИБКА");
            view.showError(R.string.toast_noname_error);
            return new ArrayList<>();
        }
    }

    private void deleteTaskLocal(int pos) {
        currentTasks.remove(pos);
        view.removeTaskFromUi(pos);
        trySaveTaskLocal(currentTasks);
    }
    private void createTaskLocal(TaskRequest taskRequest) {
        TaskResponse localTask = new TaskResponse(-1, taskRequest.getText(), taskRequest.getLocalTime());
        currentTasks.add(localTask);

        view.addTaskToUi(localTask);
        trySaveTaskLocal(currentTasks);
        registerNotificationForTask(localTask);

        view.showSuccess(R.string.toast_task_added_success_kid);
    }

    /**
     * Регистрирует уведомления для всех задач из списка.
     * Проверяет, что время задачи ещё не прошло.
     */
    private void registerNotificationsForTasks(List<TaskResponse> tasks) {
        System.out.println(tasks.size() + "столько накопилось");
        if (tasks == null) return;
        for (TaskResponse task : tasks) {
            System.out.println(task.getText());
            registerNotificationForTask(task);
        }
    }

    /**
     * Регистрирует уведомление для одной задачи, если время ещё не наступило.
     */
    private void registerNotificationForTask(TaskResponse task) {
        try {
            String[] parts = task.getLocalTime().split(":");

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return;

            java.util.Calendar now = java.util.Calendar.getInstance();
            java.util.Calendar taskTime = java.util.Calendar.getInstance();
            taskTime.set(java.util.Calendar.HOUR_OF_DAY, hour);
            taskTime.set(java.util.Calendar.MINUTE, minute);
            taskTime.set(java.util.Calendar.SECOND, 0);

            if (taskTime.getTimeInMillis() <= now.getTimeInMillis()) {
                System.out.println("Task time already passed: " + task.getText());
                return;
            }

            ReminderData data = new ReminderData();
            data.setType(ReminderData.ReminderType.TIME_STRING);
            data.setMessage(task.getText());
            data.setHour(hour);
            data.setMinute(minute);

            reminderScheduler.scheduleReminder(data);

            System.out.println("Notification registered for task: " + task.getText());

        } catch (Exception e) {
            System.out.println("Failed to register notification for task: " + task.getText() + e.getMessage());
        }
    }
    private void showError(TypeApiError typeApiError) {
        switch (typeApiError) {
            case SERVER:
                view.showError(R.string.server_error);
                break;
            case NETWORK:
                view.showError(R.string.network_error);
                break;
        }
    }
}
