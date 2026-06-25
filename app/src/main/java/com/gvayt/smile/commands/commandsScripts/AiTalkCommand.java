package com.gvayt.smile.commands.commandsScripts;

import android.content.Context;

import com.gvayt.smile.R;
import com.gvayt.smile.commands.VoiceCommand;
import com.gvayt.smile.contract.MainContract;
import com.gvayt.smile.model.ai.AIProvider;

import java.util.ArrayList;

public class AiTalkCommand implements VoiceCommand {
    private final Context context;
    private final MainContract.View view;
    private final AIProvider aiProvider;

    public AiTalkCommand(Context context, MainContract.View view, AIProvider aiProvider) {
        this.context = context;
        this.view = view;
        this.aiProvider = aiProvider;
    }

    @Override
    public boolean matches(String voiceRequest) {
        return false;
    }

    @Override
    public void execute(String voiceRequest) {
        view.addDialog(context.getString(R.string.dialog_smile_loading_ai), MainContract.DialogRole.SYSTEM);

        aiProvider.generateResponse(voiceRequest, new ArrayList<>(), new AIProvider.AIResponseCallback() {
            @Override
            public void onSuccess(String response) {
                view.addDialog(response, MainContract.DialogRole.SMILE);
            }

            @Override
            public void onError(Exception e) {
                view.addDialog(context.getString(R.string.dialog_ai_error), MainContract.DialogRole.SMILE);
            }
        });
    }
}
