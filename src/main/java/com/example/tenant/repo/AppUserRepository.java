package com.example.tenant.repo;

import com.example.tenant.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String email);

    boolean existsAppUserByUsername(String username);
}
