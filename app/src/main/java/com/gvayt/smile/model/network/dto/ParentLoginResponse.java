package com.gvayt.smile.model.network.dto;

public class ParentLoginResponse {
    private long id;
    private String fio;
    private String email;

    public ParentLoginResponse(long id, String fio, String email) {
        this.id = id;
        this.fio = fio;
        this.email = email;
    }
    public ParentLoginResponse() {}

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getFio() { return fio; }

    public void setFio(String fio) { this.fio = fio; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }
}
