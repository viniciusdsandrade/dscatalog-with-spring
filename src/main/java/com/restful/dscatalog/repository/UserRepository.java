package com.restful.dscatalog.repository;

import com.restful.dscatalog.entity.User;
import com.restful.dscatalog.projections.UserDetailsProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query(value = """
            SELECT u.email     AS username,
                   u.password  AS password,
                   r.id        AS roleId,
                   r.authority AS authority
            FROM tb_user u
            JOIN tb_user_role ur ON u.id = ur.user_id
            JOIN tb_role r       ON r.id = ur.role_id
            WHERE u.email = :email
            """, nativeQuery = true)
    List<UserDetailsProjection> searchUserAndRolesByEmail(String email);
}
