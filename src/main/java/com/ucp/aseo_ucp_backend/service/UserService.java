package com.ucp.aseo_ucp_backend.service;

import java.util.List;

import com.ucp.aseo_ucp_backend.dto.UserDto;

public interface UserService {
    List<UserDto> getAllUsers();
    void deleteUser(Long id);
}