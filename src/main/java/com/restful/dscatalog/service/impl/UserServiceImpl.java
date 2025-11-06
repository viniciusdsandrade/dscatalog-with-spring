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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Locale.ROOT;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Long DEFAULT_CLIENT_ROLE_ID = 2L;

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
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserDetailsProjection> results = userRepository.searchUserAndRolesByEmail(username);

        if (results.isEmpty())
            throw new UsernameNotFoundException("Email not found: " + username);

        User user = new User(
                results.getFirst().getUsername(),
                results.getFirst().getPassword()
        );

        results.forEach(projection -> user.addRole(
                new Role(
                        projection.getRoleId(),
                        projection.getAuthority()
                )
        ));

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAllPaged(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(UserDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO insert(@Valid UserInsertDTO userInsertDTO) {
        final String normalizedEmailAddress = normalizeEmailAddress(userInsertDTO.email());

        if (userRepository.findByEmail(normalizedEmailAddress).isPresent())
            throw new DuplicateEntryException("Email already exists: " + normalizedEmailAddress);

        try {
            User user = new User();
            user.initializeProfile(
                    userInsertDTO.firstName(),
                    userInsertDTO.lastName(),
                    normalizedEmailAddress,
                    bCryptPasswordEncoder.encode(userInsertDTO.password())
            );

            user.getRoles().clear();
            user.getRoles().add(roleRepository.getReferenceById(DEFAULT_CLIENT_ROLE_ID));

            userRepository.saveAndFlush(user);

            return new UserDTO(user);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw new DuplicateEntryException("Email already exists: " + normalizedEmailAddress);
        }
    }

    @Override
    @Transactional
    public UserDTO update(final Long id, final UserUpdateDTO userInsertDTO) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        final Authentication authentication = requireAuthenticated();
        final String requesterEmail = resolveRequesterIdentity(authentication);

        assertRequesterIsOwner(user, requesterEmail);

        final String normalizedEmailAddress = normalizeEmailAddress(userInsertDTO.email());

        validateEmailUpdate(user, normalizedEmailAddress, id);

        user.updateProfile(userInsertDTO.firstName(), userInsertDTO.lastName(), normalizedEmailAddress);
        userRepository.save(user);

        return new UserDTO(user);
    }

    @Override
    public User authenticated() {
        final Authentication authentication = requireAuthenticated();
        final String requesterEmail = resolveRequesterIdentity(authentication);
        return userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserDTO getMe() {
        final Authentication authentication = requireAuthenticated();
        final String requesterEmail = resolveRequesterIdentity(authentication);
        final User user = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserDTO(user);
    }

    private Authentication requireAuthenticated() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            throw new AuthenticationCredentialsNotFoundException("No authentication");
        return authentication;
    }

    private String resolveRequesterIdentity(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwt) {
            Object username = jwt.getTokenAttributes().get("username");
            return (username != null) ? username.toString() : jwt.getName();
        }
        return authentication.getName();
    }

    private void assertRequesterIsOwner(User entity, String requesterIdentity) {
        String ownerEmail = entity.getEmail();
        boolean isOwner = ownerEmail != null
                          && ownerEmail.equalsIgnoreCase(requesterIdentity);

        if (!isOwner) throw new AccessDeniedException("You are not allowed to update this user");
    }

    private String normalizeEmailAddress(String rawEmail) {
        if (rawEmail == null) throw new ValidationException("Email must not be null");
        return rawEmail.trim().toLowerCase(ROOT);
    }

    private void validateEmailUpdate(User user, String candidateEmail, Long userId) {
        if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(candidateEmail))
            throw new DuplicateEntryException("New email must be different from current");
        if (userRepository.existsByEmailIgnoreCaseAndIdNot(candidateEmail, userId))
            throw new DuplicateEntryException("Email already exists: " + candidateEmail);
    }
}
