package com.gvayt.smile.contract;

import com.gvayt.smile.model.network.ModelCallback;
import com.gvayt.smile.model.network.dto.kid.KidResponse;

import java.util.List;

public interface MainContract {
    interface View {
        void checkPermissions();
        void startVoiceTriggerService();
        void stopVoiceTriggerService();
        void showServerError();
        void showNetworkError();
        void showAnonMode();
        void showCommands(List<Integer> commands);
        void startVoiceRecognition();
        void logout();
        void addDialog(int text, DialogRole dialogRole);
        void addDialog(String text, DialogRole dialogRole);
    }
    interface Presenter {
        void onTTSInit();
        void onViewCreate(boolean anonKid);
        void onCommandEntered(String command);
        void onVoiceButtonClicked();
        void onWakeWordDetected();
        void onSpinnerCategorySelected(int position);
        void onLogoutClicked();
    }
    interface Model {
        void getKidInfo(ModelCallback<KidResponse> callback);
        void logout();
    }
    enum DialogRole {
        USER,
        SMILE,
        SYSTEM
    }
}
