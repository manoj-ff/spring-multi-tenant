package com.example.tenant;

import com.example.tenant.entity.master.AppUser;
import com.example.tenant.entity.master.AppUserRole;
import com.example.tenant.repo.master.AppUserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class SpringMultiTenantApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringMultiTenantApplication.class, args);
    }

    @Bean
    ApplicationRunner runner(AppUserRepository userRepository) {
        return args -> {
            if (!userRepository.existsAppUserByUsername("manoj")) {
                AppUser user = new AppUser();
                user.setName("Manoj");
                user.setUsername("manoj");
                user.setEmail("manoj@test.com");
                user.setPassword("pass");
                user.setAppUserRole(AppUserRole.USER);
                user.setLocked(false);
                user.setEnabled(true);
                user.setTenantId("tenant1"); // I'll assign a default tenant
                userRepository.save(user);
            }
        };
    }
}
