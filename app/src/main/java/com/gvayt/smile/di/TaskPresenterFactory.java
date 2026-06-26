package com.gvayt.smile.di;

import android.content.Context;

import com.gvayt.smile.commands.commandsScripts.reminder.ReminderScheduler;
import com.gvayt.smile.contract.TaskContract;
import com.gvayt.smile.model.TaskModel;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.local.SharedPrefStorage;
import com.gvayt.smile.model.network.ApiService;
import com.gvayt.smile.model.network.RetrofitClient;
import com.gvayt.smile.presenter.TaskPresenter;

public class TaskPresenterFactory {
    public static TaskContract.Presenter create(TaskContract.View view, Context context) {
        LocalStorage localStorage = new SharedPrefStorage(context);
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
        TaskContract.Model model = new TaskModel(localStorage, apiService);
        ReminderScheduler reminderScheduler = new ReminderScheduler(context);
        return new TaskPresenter(view, model, reminderScheduler);
    }
}
