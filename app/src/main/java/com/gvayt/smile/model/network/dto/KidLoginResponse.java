package com.gvayt.smile.model.network.dto;

public class KidLoginResponse {
    private long id;
    private String fio;
    private String username;

    public KidLoginResponse(long id, String fio, String username) {
        this.id = id;
        this.fio = fio;
        this.username = username;
    }
    public KidLoginResponse() {}

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getFio() { return fio; }

    public void setFio(String fio) { this.fio = fio; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }
}
