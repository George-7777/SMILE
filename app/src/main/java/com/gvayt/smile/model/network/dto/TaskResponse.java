package com.gvayt.smile.model.network.dto;

import java.time.LocalTime;

public class TaskResponse {
    private long id;
    private String text;
    private LocalTime localTime;

    public TaskResponse(long id, String text, LocalTime localTime) {
        this.id = id;
        this.text = text;
        this.localTime = localTime;
    }
    public TaskResponse() {}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
