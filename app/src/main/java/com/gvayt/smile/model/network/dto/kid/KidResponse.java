package com.gvayt.smile.model.network.dto.kid;

import com.gvayt.smile.model.network.dto.task.TaskResponse;

import java.util.List;

public class KidResponse {
    private long id;
    private String fio;
    private String login;
    private Integer parent_id;
    private List<TaskResponse> tasks;

    public KidResponse(long id, String fio, String login, Integer parentId, List<TaskResponse> tasks) {
        this.id = id;
        this.fio = fio;
        this.login = login;
        parent_id = parentId;
        this.tasks = tasks;
    }
    public KidResponse() {}

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getFio() { return fio; }

    public void setFio(String fio) { this.fio = fio; }

    public String getLogin() { return login; }

    public void setLogin(String login) { this.login = login; }

    public Integer getParent_id() { return parent_id; }

    public void setParent_id(Integer parent_id) { this.parent_id = parent_id; }

    public List<TaskResponse> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskResponse> tasks) {
        this.tasks = tasks;
    }
}
