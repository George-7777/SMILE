package com.gvayt.smile.commands;

import android.content.Context;

import com.gvayt.smile.commands.commandsScripts.AiTalkCommand;
import com.gvayt.smile.commands.commandsScripts.radio.RadioPlayer;
import com.gvayt.smile.commands.commandsScripts.radio.TurnOffRadioCommand;
import com.gvayt.smile.commands.commandsScripts.radio.TurnOnRadioCommand;
import com.gvayt.smile.commands.commandsScripts.reminder.ReminderScheduler;
import com.gvayt.smile.commands.commandsScripts.reminder.SetTaskCommand;
import com.gvayt.smile.commands.commandsScripts.reminder.ShowTasksCommand;
import com.gvayt.smile.commands.commandsScripts.settings.SetSosNumberCommand;
import com.gvayt.smile.commands.commandsScripts.settings.SettingNotificationCommand;
import com.gvayt.smile.contract.MainContract;
import com.gvayt.smile.model.ai.AIProvider;
import com.gvayt.smile.model.local.LocalStorage;
import com.gvayt.smile.model.local.SharedPrefStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Отвечает за выполнение и инициализацию команд.
 */
public class CommandExecutor {
    private List<VoiceCommand> commands;
    private VoiceCommand unknownCommand;
    public CommandExecutor(Context context, MainContract.View view, ReminderScheduler reminderScheduler, RadioPlayer radioPlayer, LocalStorage localStorage, AIProvider aiProvider) {
        commands = new ArrayList<>();
        commands.add(new ShowTasksCommand(context));
        commands.add(new TurnOnRadioCommand(context, view, radioPlayer));
        commands.add(new TurnOffRadioCommand(context, view, radioPlayer));
        commands.add(new SetTaskCommand(context, view, reminderScheduler));
        commands.add(new SetSosNumberCommand(context, view, localStorage));
        commands.add(new SettingNotificationCommand(context, view));

        unknownCommand = new AiTalkCommand(context, view, aiProvider);
    }

    /**
     * Выполняет команду исходя из запроса.
     * @param command Запрос пользователя
     */
    public void execute(String command) {
        for (VoiceCommand voiceCommand : commands) {
            if (voiceCommand.matches(command)) {
                voiceCommand.execute(command);
                return;
            }
        }
        unknownCommand.execute(command);
    }
}
