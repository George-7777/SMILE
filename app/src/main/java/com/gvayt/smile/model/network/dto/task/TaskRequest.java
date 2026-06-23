package com.gvayt.smile.model.network.dto.task;

import java.time.LocalTime;

public class TaskRequest {
    private String text;
    private LocalTime localTime;

    public TaskRequest(String text, LocalTime localTime) {
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

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }
}
