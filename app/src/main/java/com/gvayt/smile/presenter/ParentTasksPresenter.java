package com.gvayt.smile.presenter;

import com.gvayt.smile.contract.ParentTasksContract;
import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.TypeApiError;
import com.gvayt.smile.model.network.dto.kid.KidResponse;
import com.gvayt.smile.model.network.dto.task.TaskRequest;
import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.util.List;

public class ParentTasksPresenter implements ParentTasksContract.Presenter {
    private final ParentTasksContract.View view;
    private final ParentTasksContract.Model model;
    private String kidLogin;

    public ParentTasksPresenter(ParentTasksContract.View view, ParentTasksContract.Model model) {
        this.view = view;
        this.model = model;
    }

    @Override
    public void onViewCreate(String kidLogin) {
        this.kidLogin = kidLogin;
        model.getKidInfo(kidLogin, new ModelCallback<>() {
            @Override
            public void onSuccess(KidResponse result) {
                view.showKidInfo(result);
            }

            @Override
            public void onError(TypeApiError error) {
                showError(error);
            }
        });
        model.getTasksForKid(kidLogin, new ModelCallback<>() {
            @Override
            public void onSuccess(List<TaskResponse> result) {
                view.showTasks(result);
            }

            @Override
            public void onError(TypeApiError error) {
                showError(error);
            }
        });
    }

    @Override
    public void onButtonExitClick() {
        view.exitActivity();
    }

    @Override
    public void onAddTaskClick() {
        view.showAddTaskDialog();
    }

    @Override
    public void onDeleteTaskClick(long task_id) {
        model.deleteTaskForKid(kidLogin, task_id, new ModelCallback<>() {
            @Override
            public void onSuccess(Void result) {
                view.successDeleteTask();
            }

            @Override
            public void onError(TypeApiError error) {
                showError(error);
            }
        });
    }

    @Override
    public void confirmAddTask(TaskRequest taskRequest) {
        model.addTaskForKid(kidLogin, taskRequest, new ModelCallback<>() {
            @Override
            public void onSuccess(Void result) {
                view.successAddTask();
            }

            @Override
            public void onError(TypeApiError error) {
                switch (error) {
                    case SERVER:
                        view.showServerError();
                        break;
                    case NETWORK:
                        view.showNetworkError();
                        break;
                }
            }
        });
    }

    private void showError(TypeApiError error) {
        switch (error) {
            case NETWORK:
                view.showNetworkError();
                break;
            case SERVER:
                view.showServerError();
                break;
        }
    }
}
