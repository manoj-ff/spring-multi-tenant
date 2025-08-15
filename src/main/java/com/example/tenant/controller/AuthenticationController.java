package com.example.tenant.controller;

import com.example.tenant.config.JWTUtil;
import com.example.tenant.entity.master.AppUser;
import com.example.tenant.entity.master.AppUserRole;
import com.example.tenant.model.AppUserDto;
import com.example.tenant.repo.master.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

@Slf4j
@RestController
@RequestMapping("/api/security")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JWTUtil jwtUtil,
                                    PasswordEncoder passwordEncoder,
                                    AppUserRepository appUserRepository) {
        this.authenticationManager = authenticationManager;

        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody CredentialsDto credentialsDto) {

        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                credentialsDto.username(),
                credentialsDto.password()
        ));

        if (authenticate.getPrincipal() instanceof AppUser user) {
            return ok(new LoginResponse(jwtUtil.generateAccessToken(user)));
        }

        throw new BadCredentialsException("User not authorized");
    }

    @PostMapping("/users")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<AppUserDto> createUser(@RequestBody AppUserDto userDto) {
        AppUser user = new AppUser();
        user.setTenantId(userDto.getTenantId());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEnabled(true);
        user.setLocked(false);
        user.setAppUserRole(AppUserRole.USER);
        user.setName(userDto.getName());
        AppUser dbUser = appUserRepository.save(user);
        userDto.setId(dbUser.getId());
        return ok(userDto);
    }

}


record CredentialsDto(String username, String password) {
}

record LoginResponse(String accessToken) {
}
