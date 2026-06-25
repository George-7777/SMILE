package com.gvayt.smile.di;

import android.content.Context;

import com.gvayt.smile.commands.CommandExecutor;
import com.gvayt.smile.commands.commandsScripts.radio.RadioPlayer;
import com.gvayt.smile.commands.commandsScripts.reminder.ReminderScheduler;
import com.gvayt.smile.contract.MainContract;
import com.gvayt.smile.model.MainModel;
import com.gvayt.smile.model.ai.GeminiProvider;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.local.SharedPrefStorage;
import com.gvayt.smile.presenter.MainPresenter;

public class MainPresenterFactory {
    public static MainContract.Presenter create(MainContract.View view, Context context) {
        LocalStorage localStorage = new SharedPrefStorage(context);
        MainContract.Model model = new MainModel(localStorage);
        CommandExecutor commandExecutor = new CommandExecutor(
                context,
                view,
                new ReminderScheduler(context),
                RadioPlayer.getInstance(context, ""),
                localStorage,
                new GeminiProvider()
        );
        return new MainPresenter(view, model, commandExecutor);
    }
}
