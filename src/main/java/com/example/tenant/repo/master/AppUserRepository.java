package com.example.tenant.repo.master;

import com.example.tenant.entity.master.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    boolean existsAppUserByEmail(String email);
}
