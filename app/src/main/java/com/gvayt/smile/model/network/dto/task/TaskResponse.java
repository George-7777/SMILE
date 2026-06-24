package com.gvayt.smile.model.network.dto.task;

public class TaskResponse {
    private long id;
    private String text;
    private String localTime;

    public TaskResponse(long id, String text, String localTime) {
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

    public String getLocalTime() {
        return localTime;
    }

    public void setLocalTime(String localTime) {
        this.localTime = localTime;
    }
}
