package com.example.tenant;

import com.example.tenant.entity.master.AppUser;
import com.example.tenant.entity.master.AppUserRole;
import com.example.tenant.repo.master.AppUserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;


@EnableScheduling
@EnableAsync
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        LiquibaseAutoConfiguration.class
})
public class SpringMultiTenantApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMultiTenantApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsAppUserByEmail("admin@test.com")) {
                AppUser user = new AppUser();
                user.setName("Admin");
                user.setEmail("admin@test.com");
                user.setPassword(passwordEncoder.encode("pass"));
                user.setAppUserRole(AppUserRole.ADMIN);
                user.setLocked(false);
                user.setEnabled(true);
                user.setTenantId("master");
                userRepository.save(user);
            }
        };
    }
}
