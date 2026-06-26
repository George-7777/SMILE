package com.gvayt.smile.commands.commandsScripts.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import com.gvayt.smile.R;
import com.gvayt.smile.commands.VoiceCommand;
import com.gvayt.smile.contract.MainContract;

public class SettingNotificationCommand implements VoiceCommand {
    private final Context context;
    private final MainContract.View view;

    public SettingNotificationCommand(Context context, MainContract.View view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public boolean matches(String voiceRequest) {
        voiceRequest = voiceRequest.toLowerCase();
        return voiceRequest.startsWith(
                context.getString(R.string.command_notification_on))
                || voiceRequest.startsWith(context.getString(R.string.command_notification_off)
        );
    }

    @Override
    public void execute(String voiceRequest) {
        view.addDialog(context.getString(R.string.dialog_notification_setting), MainContract.DialogRole.SMILE);
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
