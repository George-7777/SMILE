package com.gvayt.smile.model.network.dto;

import java.util.List;

public class ParentResponse {
    private long id;
    private String fio;
    private String email;
    private List<KidResponse> kidList;

    public ParentResponse(long id, String fio, String email, List<KidResponse> kidList) {
        this.id = id;
        this.fio = fio;
        this.email = email;
        this.kidList = kidList;
    }
    public ParentResponse() {}

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getFio() { return fio; }

    public void setFio(String fio) { this.fio = fio; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public List<KidResponse> getKidList() { return kidList; }

    public void setKidList(List<KidResponse> kidList) { this.kidList = kidList; }
}
