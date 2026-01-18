package com.zosh.service;

import com.zosh.exception.SellerException;
import com.zosh.exception.UserException;
import com.zosh.request.LoginRequest;
import com.zosh.request.SignupRequest;
import com.zosh.response.AuthResponse;

public interface AuthService {

    void sentLoginOtp(String email) throws UserException;

    String createUser(SignupRequest req) throws SellerException;

    AuthResponse signin(LoginRequest req) throws SellerException;
}

