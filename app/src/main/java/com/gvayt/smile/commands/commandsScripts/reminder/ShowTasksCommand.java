package com.gvayt.smile.commands.commandsScripts.reminder;

import android.content.Context;
import android.content.Intent;

import com.gvayt.smile.R;
import com.gvayt.smile.commands.VoiceCommand;
import com.gvayt.smile.ui.commandsView.TaskActivity;

public class ShowTasksCommand implements VoiceCommand {
    private final Context context;

    public ShowTasksCommand(Context context) {
        this.context = context;
    }

    @Override
    public boolean matches(String voiceRequest) {
        return voiceRequest.toLowerCase().startsWith(context.getString(R.string.command_tasks_show));
    }

    @Override
    public void execute(String voiceRequest) {
        context.startActivity(new Intent(context, TaskActivity.class));
    }
}
