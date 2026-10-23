package com.econest.service;

import com.econest.dto.RegisterForm;
import com.econest.entity.User;

import java.util.Optional;

public interface UserService {

    User register(RegisterForm form);

    User findByEmail(String email);

    Optional<User> getByEmail(String email);

    User findById(Long id);

    boolean existsByEmail(String email);

    User updateProfile(Long userId, String fullName, String phone);

    long countCustomers();
}
