package com.gvayt.smile.model.network.dto.parent;

public class ParentRegisterRequest {
    private String fio;
    private String email;
    private String password;

    public ParentRegisterRequest() {}

    public ParentRegisterRequest(String fio, String email, String password) {
        this.fio = fio;
        this.email = email;
        this.password = password;
    }

    // геттеры и сеттеры
    public String getFio() { return fio; }
    public void setFio(String fio) { this.fio = fio; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}