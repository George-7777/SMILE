package com.gvayt.smile.model.network.dto.kid;

public class KidRegisterRequest {
    private String fio;
    private String login;
    private String password;

    public KidRegisterRequest(String fio, String login, String password) {
        this.fio = fio;
        this.login = login;
        this.password = password;
    }

    public KidRegisterRequest() {}

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
