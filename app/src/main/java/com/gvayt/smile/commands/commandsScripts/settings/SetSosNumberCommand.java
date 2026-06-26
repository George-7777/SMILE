package com.gvayt.smile.commands.commandsScripts.settings;

import android.content.Context;

import com.gvayt.smile.Constant;
import com.gvayt.smile.R;
import com.gvayt.smile.commands.VoiceCommand;
import com.gvayt.smile.contract.MainContract;
import com.gvayt.smile.model.local.LocalStorage;

public class SetSosNumberCommand implements VoiceCommand {
    private final Context context;
    private final MainContract.View view;
    private final LocalStorage localStorage;

    public SetSosNumberCommand(Context context, MainContract.View view, LocalStorage localStorage) {
        this.context = context;
        this.view = view;
        this.localStorage = localStorage;
    }

    @Override
    public boolean matches(String voiceRequest) {
        voiceRequest = voiceRequest.toLowerCase();
        return voiceRequest.startsWith(context.getString(R.string.command_add_phone_number))
                || voiceRequest.startsWith(context.getString(R.string.command_add_phone_number2));
    }

    @Override
    public void execute(String voiceRequest) {
        String number = extractSosNumber(voiceRequest);
        if (!number.isEmpty()) {
            localStorage.putString(Constant.KEY_SOS_NUMBER, number);
            view.addDialog(context.getString(R.string.dialog_number_added), MainContract.DialogRole.SMILE);
        } else {
            view.addDialog(context.getString(R.string.dialog_number_failed), MainContract.DialogRole.SMILE);
        }
    }

    private String extractSosNumber(String command) {
        String[] parts = command.split(" ");
        if (parts.length >= 3) {
            return parts[2];
        }
        return "";
    }
}
