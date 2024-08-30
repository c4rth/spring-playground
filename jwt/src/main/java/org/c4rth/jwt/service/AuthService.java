package org.c4rth.jwt.service;


import org.c4rth.jwt.dto.LoginDto;

public interface AuthService {
    String login(LoginDto loginDto);
}