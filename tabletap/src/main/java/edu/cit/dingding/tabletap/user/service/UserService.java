package edu.cit.dingding.tabletap.user.service;

import edu.cit.dingding.tabletap.user.model.Role;
import edu.cit.dingding.tabletap.user.model.User;
import edu.cit.dingding.tabletap.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

// This service creates and looks up user accounts.
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerDiner(String fullName, String email, String phone, String rawPassword) {
        return create(fullName, email, phone, rawPassword, Role.DINER);
    }

    public User createWithRole(String fullName, String email, String rawPassword, Role role) {
        return create(fullName, email, null, rawPassword, role);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email));
    }

    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    public boolean passwordMatches(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    private User create(String fullName, String email, String phone, String rawPassword, Role role) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(normalizedEmail);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
