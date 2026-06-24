package com.gvayt.smile.model.network.dto.task;

import java.time.LocalTime;

public class TaskRequest {
    private String text;
    private String localTime;

    public TaskRequest(String text, String localTime) {
        this.text = text;
        this.localTime = localTime;
    }
    public TaskRequest() {}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLocalTime() {
        return localTime;
    }

    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }
}
