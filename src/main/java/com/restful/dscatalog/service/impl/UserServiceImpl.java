package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.user.UserDTO;
import com.restful.dscatalog.dto.user.UserInsertDTO;
import com.restful.dscatalog.dto.user.UserUpdateDTO;
import com.restful.dscatalog.entity.Role;
import com.restful.dscatalog.entity.User;
import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
import com.restful.dscatalog.exception.ValidationException;
import com.restful.dscatalog.projections.UserDetailsProjection;
import com.restful.dscatalog.repository.RoleRepository;
import com.restful.dscatalog.repository.UserRepository;
import com.restful.dscatalog.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Locale.ROOT;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Long ROLE_CLIENT_ID = 2L;

    private final PasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(
            PasswordEncoder bcryptpasswordencoder,
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.bCryptPasswordEncoder = bcryptpasswordencoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserDetailsProjection> results = userRepository.searchUserAndRolesByEmail(username);
        if (results.isEmpty())
            throw new UsernameNotFoundException("Email not found: " + username);
        User user = new User();
        user.setEmail(results.get(0).getUsername());
        user.setPassword(results.get(0).getPassword());
        results.forEach(projection -> user.addRole(
                new Role(projection.getRoleId(), projection.getAuthority())
        ));
        return user;
    }

    @Override
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        Page<User> list = userRepository.findAll(pageable);
        return list.map(UserDTO::new);
    }

    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO insert(@Valid UserInsertDTO dto) {
        final String normalizedEmail = dto.email().trim().toLowerCase();

        if (userRepository.findByEmail(normalizedEmail).isPresent())
            throw new DuplicateEntryException("Email already exists: " + normalizedEmail);

        try {
            User entity = new User();
            entity.initializeProfile(
                    dto.firstName(),
                    dto.lastName(),
                    normalizedEmail,
                    bCryptPasswordEncoder.encode(dto.password())
            );

            entity.getRoles().clear();
            entity.getRoles().add(roleRepository.getReferenceById(ROLE_CLIENT_ID));

            userRepository.saveAndFlush(entity);
            return new UserDTO(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException("Email already exists: " + normalizedEmail);
        }
    }

    @Override
    @Transactional
    public UserDTO update(final Long id, final UserUpdateDTO dto) {
        final User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        final Authentication auth = requireAuthenticated();
        final String requester = resolveRequesterIdentity(auth);

        assertOwner(entity, requester);

        final String normalizedEmail = normalizeEmail(dto.email());

        validateEmailChange(entity, normalizedEmail, id);

        entity.updateProfile(dto.firstName(), dto.lastName(), normalizedEmail);
        userRepository.save(entity);

        return new UserDTO(entity);
    }

    private Authentication requireAuthenticated() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            throw new AuthenticationCredentialsNotFoundException("No authentication");
        return auth;
    }

    private String resolveRequesterIdentity(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwt) {
            Object username = jwt.getTokenAttributes().get("username");
            return (username != null) ? username.toString() : jwt.getName();
        }
        return auth.getName();
    }

    private void assertOwner(User entity, String requesterIdentity) {
        String ownerEmail = entity.getEmail();
        boolean isOwner = ownerEmail != null
                && ownerEmail.equalsIgnoreCase(requesterIdentity);

        if (!isOwner) throw new AccessDeniedException("You are not allowed to update this user");
    }

    private String normalizeEmail(String raw) {
        if (raw == null) throw new ValidationException("Email must not be null");
        return raw.trim().toLowerCase(ROOT);
    }

    private void validateEmailChange(User entity, String newEmail, Long id) {
        if (entity.getEmail() != null && entity.getEmail().equalsIgnoreCase(newEmail))
            throw new DuplicateEntryException("New email must be different from current");
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, id))
            throw new DuplicateEntryException("Email already exists: " + newEmail);
    }
}
