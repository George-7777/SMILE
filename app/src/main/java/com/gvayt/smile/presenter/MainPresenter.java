package com.gvayt.smile.presenter;

import android.os.Handler;
import android.os.Looper;

import com.gvayt.smile.R;
import com.gvayt.smile.commands.CommandExecutor;
import com.gvayt.smile.contract.MainContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainPresenter implements MainContract.Presenter {
    private final MainContract.View view;
    private final MainContract.Model model;
    private final CommandExecutor commandExecutor;
    private final HashMap<Integer, List<Integer>> commandList;

    public MainPresenter(MainContract.View view, MainContract.Model model, CommandExecutor commandExecutor) {
        this.view = view;
        this.model = model;
        this.commandExecutor = commandExecutor;
        commandList = new HashMap<>();

        ArrayList<Integer> list0 = new ArrayList<>();
        list0.add(R.string.command_list_tasks_list);
        list0.add(R.string.command_list_notification_on);
        list0.add(R.string.command_list_notification_off);
        commandList.put(0, list0);

        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(R.string.command_list_ai_tall);
        commandList.put(1, list1);

        ArrayList<Integer> list2 = new ArrayList<>();
        list2.add(R.string.command_list_add_sos_number);
        commandList.put(2, list2);

        ArrayList<Integer> list3 = new ArrayList<>();
        list3.add(R.string.command_list_radio_on);
        list3.add(R.string.command_list_radio_off);
        commandList.put(3, list3);
    }

    @Override
    public void onTTSInit() {
        view.addDialog(R.string.tts_init_hello, MainContract.DialogRole.SMILE);
        view.startVoiceTriggerService();
    }

    @Override
    public void onViewCreate(boolean anonKid) {
        view.checkPermissions();
        if (anonKid) {
            view.showAnonMode();
        }
        view.addDialog(R.string.tutorial_command_list, MainContract.DialogRole.SYSTEM);
        view.addDialog(R.string.dialog_tutorial_2, MainContract.DialogRole.SYSTEM);
    }

    @Override
    public void onCommandEntered(String command) {
        view.addDialog(command, MainContract.DialogRole.USER);
        commandExecutor.execute(command);
    }

    @Override
    public void onVoiceButtonClicked() {
        view.startVoiceRecognition();
    }

    @Override
    public void onWakeWordDetected() {
        view.addDialog(R.string.triggered_wake_word, MainContract.DialogRole.SMILE);
        new Handler(Looper.getMainLooper()).postDelayed(view::startVoiceRecognition, 2000);
    }

    @Override
    public void onSpinnerCategorySelected(int position) {
        view.showCommands(commandList.get(position));
    }

    @Override
    public void onLogoutClicked() {
        view.logout();
    }
}
