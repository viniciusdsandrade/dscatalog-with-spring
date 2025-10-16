package com.restful.dscatalog.service.impl;

import com.restful.dscatalog.dto.user.UserDTO;
import com.restful.dscatalog.dto.user.UserInsertDTO;
import com.restful.dscatalog.entity.User;
import com.restful.dscatalog.exception.DuplicateEntryException;
import com.restful.dscatalog.repository.RoleRepository;
import com.restful.dscatalog.repository.UserRepository;
import com.restful.dscatalog.service.UserService;
import jakarta.transaction.Transactional;
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
    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent())
            throw new DuplicateEntryException("Email already exists: " + dto.email());

        try {
            User entity = new User();
            copyDtoToEntity(dto, entity);
            entity.setPassword(bCryptPasswordEncoder.encode(dto.password()));
            userRepository.saveAndFlush(entity);
            return new UserDTO(entity);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntryException("Email already exists: " + dto.email());
        }
    }

    @Override
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user);
    }

    private void copyDtoToEntity(UserInsertDTO dto, User entity) {
        entity.setFirstName(dto.firstName());
        entity.setLastName(dto.lastName());
        entity.setEmail(dto.email());

        entity.getRoles().clear();
        entity.getRoles().add(roleRepository.getReferenceById(ROLE_CLIENT_ID));
    }
}

