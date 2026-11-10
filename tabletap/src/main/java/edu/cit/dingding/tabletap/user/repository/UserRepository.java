package edu.cit.dingding.tabletap.user.repository;

import edu.cit.dingding.tabletap.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// This repository provides database access for TableTap users.
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
}
