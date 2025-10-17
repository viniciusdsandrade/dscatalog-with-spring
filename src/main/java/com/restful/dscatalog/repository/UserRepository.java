package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id); // Spring Data keywords
}
