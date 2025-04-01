package com.rummy.repository;

import com.rummy.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByMobileNumber(String mobileNumber);
    boolean existsByUsername(String username);
    boolean existsByMobileNumber(String mobileNumber);
}