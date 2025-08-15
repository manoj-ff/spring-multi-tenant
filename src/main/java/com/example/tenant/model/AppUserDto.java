package com.example.tenant.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppUserDto implements Serializable {
    private Long id;
    private String name;
    private String email;
    private String username;
    private String password;
    private String tenantId;
}
