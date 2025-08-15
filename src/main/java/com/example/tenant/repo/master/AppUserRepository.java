package com.example.tenant.repo.master;

import com.example.tenant.entity.master.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    boolean existsAppUserByUsername(String username);
}
