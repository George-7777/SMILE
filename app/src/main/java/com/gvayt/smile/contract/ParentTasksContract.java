package com.gvayt.smile.contract;

import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.dto.kid.KidResponse;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.util.List;

public interface ParentTasksContract {
    interface View {
        void showTasks(List<TaskResponse> taskList);
        void showKidInfo(KidResponse kidResponse);
        void showNetworkError();
        void showServerError();
        void successAddTask();
        void successDeleteTask();
        void exitActivity();
        void showAddTaskDialog();
    }
    interface Presenter {
        void onViewCreate(String kidLogin);
        void onButtonExitClick();
        void onAddTaskClick();
        void onDeleteTaskClick(long task_id);
        void confirmAddTask(TaskRequest taskRequest);
    }
    interface Model {
        void getTasksForKid(String kidLogin, ModelCallback<List<TaskResponse>> callback);
        void addTaskForKid(String kidLogin, TaskRequest taskRequest, ModelCallback<Void> callback);
        void deleteTaskForKid(String kidLogin, long task_id, ModelCallback<Void> callback);
        void getKidInfo(String kidLogin, ModelCallback<KidResponse> callback);
    }
}
