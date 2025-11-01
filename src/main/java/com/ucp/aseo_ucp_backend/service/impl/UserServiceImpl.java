package com.ucp.aseo_ucp_backend.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucp.aseo_ucp_backend.dto.UserDto;
import com.ucp.aseo_ucp_backend.entity.User;
import com.ucp.aseo_ucp_backend.repository.UserRepository;
import com.ucp.aseo_ucp_backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll(); // Simplemente obtiene todos
        return users.stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }
}