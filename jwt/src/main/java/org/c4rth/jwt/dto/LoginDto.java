package org.c4rth.jwt.dto;

import lombok.Data;

@Data
public class LoginDto {

    private String username;
    private String password;

}