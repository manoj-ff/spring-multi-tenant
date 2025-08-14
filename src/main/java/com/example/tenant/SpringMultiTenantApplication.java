package com.example.tenant;

import com.example.tenant.entity.AppUser;
import com.example.tenant.entity.AppUserRole;
import com.example.tenant.repo.AppUserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringMultiTenantApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringMultiTenantApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(AppUserRepository userRepository) {
		return args -> {
			if(!userRepository.existsAppUserByUsername("manoj")) {
				AppUser user = new AppUser();
				user.setName("Manoj");
				user.setUsername("manoj");
				user.setEmail("manoj@test.com");
				user.setPassword("pass");
				user.setAppUserRole(AppUserRole.USER);
				user.setLocked(false);
				user.setEnabled(true);

				userRepository.save(user);
			}
		};
	}
}
