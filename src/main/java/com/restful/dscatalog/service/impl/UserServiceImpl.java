package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.user.UserDTO;
import com.restful.dscatalog.dto.user.UserInsertDTO;
import com.restful.dscatalog.dto.user.UserUpdateDTO;
import com.restful.dscatalog.entity.User;
import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.exception.ResourceNotFoundException;
import com.restful.dscatalog.repository.RoleRepository;
import com.restful.dscatalog.repository.UserRepository;
import com.restful.dscatalog.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Long ROLE_CLIENT_ID = 2L;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(
            BCryptPasswordEncoder bcryptpasswordencoder,
            UserRepository userRepository,
            RoleRepository roleRepository
    ) {
        this.bCryptPasswordEncoder = bcryptpasswordencoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
    public UserDTO update(Long id, UserUpdateDTO dto) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String normalizedEmail = dto.email().trim().toLowerCase();

        if (normalizedEmail.equalsIgnoreCase(entity.getEmail()))
            throw new DuplicateEntryException("New email must be different from current");

        if (userRepository.existsByEmailIgnoreCaseAndIdNot(normalizedEmail, id))
            throw new DuplicateEntryException("Email already exists: " + normalizedEmail);

        entity.updateProfile(
                dto.firstName(),
                dto.lastName(),
                normalizedEmail
        );
        userRepository.save(entity);
        return new UserDTO(entity);
    }
}

