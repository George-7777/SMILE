package com.gvayt.smile.contract;

import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.io.IOException;
import java.util.List;

public interface TaskContract {
    interface View {
        void checkExactAlarmPermissionAndRequest();
        void showTasks(List<TaskResponse> tasks);
        void showAddTaskDialog();
        void showError(int message);
        void showSuccess(int message);
        void navigateBack();
        void addTaskToUi(TaskResponse newTask);
        void removeTaskFromUi(int taskPosition);
    }

    interface Presenter {
        void onViewCreated();
        void onAddTaskButtonClicked();
        void addTask(TaskRequest taskRequest);
        void onTaskItemLongClick(int position);
        void onButtonExitClicked();
    }

    interface Model {
        void getTasks(ModelCallback<List<TaskResponse>> callback);
        void addTask(TaskRequest request, ModelCallback<TaskResponse> callback);
        void deleteTask(long taskId, ModelCallback<Void> callback);
        void saveTasksLocally(List<TaskResponse> tasks) throws IOException;
        List<TaskResponse> getTasksLocally() throws IOException;
        boolean isLogged();
    }
}
