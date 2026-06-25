package com.gvayt.smile.commands.commandsScripts.radio;

import android.content.Context;

import com.gvayt.smile.R;
import com.gvayt.smile.commands.VoiceCommand;
import com.gvayt.smile.contract.MainContract;

public class TurnOnRadioCommand implements VoiceCommand {
    private final Context context;
    private final MainContract.View view;
    private final RadioPlayer radioPlayer;

    public TurnOnRadioCommand(Context context, MainContract.View view, RadioPlayer radioPlayer) {
        this.context = context;
        this.view = view;
        this.radioPlayer = radioPlayer;
    }

    @Override
    public boolean matches(String voiceRequest) {
        return voiceRequest.toLowerCase().startsWith(context.getString(R.string.command_radio_on));
    }

    @Override
    public void execute(String voiceRequest) {
        radioPlayer.play();
        view.addDialog(context.getString(R.string.dialog_radio_turn_on), MainContract.DialogRole.SMILE);
    }
}
