package com.scaler.userservicemwfeve.services;

import com.scaler.userservicemwfeve.dtos.LoginResponseDto;
import com.scaler.userservicemwfeve.exceptions.PasswordNotMatchingException;
import com.scaler.userservicemwfeve.exceptions.TokenNotFoundException;
import com.scaler.userservicemwfeve.exceptions.UserNotFoundException;
import com.scaler.userservicemwfeve.models.Role;
import com.scaler.userservicemwfeve.models.User;
import com.scaler.userservicemwfeve.repositories.RoleRepository;
import com.scaler.userservicemwfeve.repositories.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       RoleRepository roleRepository,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
    }

    public User signUp(String fullName, String email, String password, List<String> roleNames) {
        User u = new User();
        u.setEmail(email);
        u.setName(fullName);
        u.setHashedPassword(bCryptPasswordEncoder.encode(password));
        u.setRoles(resolveRoles(roleNames));

        return userRepository.save(u);
    }

    private List<Role> resolveRoles(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = List.of(DEFAULT_ROLE);
        }
        List<Role> roles = new ArrayList<>();
        for (String name : roleNames) {
            roleRepository.findByName(name).ifPresent(roles::add);
        }
        if (roles.isEmpty()) {
            roleRepository.findByName(DEFAULT_ROLE).ifPresent(roles::add);
        }
        return roles;
    }

    /** Stateless login: returns JWT only, no DB persistence. */
    public LoginResponseDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        if (!bCryptPasswordEncoder.matches(password, user.getHashedPassword())) {
            throw new PasswordNotMatchingException("Invalid password");
        }

        String jwt = jwtService.createToken(user);
        return new LoginResponseDto(jwt);
    }

    /** Stateless validate: verifies JWT and returns user from DB by userId in token. No token table. */
    public User validateToken(String tokenValue) {
        try {
            var claims = jwtService.parseAndValidate(tokenValue);
            String subject = claims.getSubject();
            Long userId = Long.parseLong(subject);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new TokenNotFoundException("User not found for token"));
        } catch (NumberFormatException e) {
            throw new TokenNotFoundException("Invalid token");
        }
    }
}
