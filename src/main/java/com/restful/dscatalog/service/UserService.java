package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.user.UserDTO;
import com.restful.dscatalog.dto.user.UserInsertDTO;
import com.restful.dscatalog.dto.user.UserUpdateDTO;
import com.restful.dscatalog.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    Page<UserDTO> findAllPaged(Pageable pageable);

    @Transactional
    UserDTO insert(@Valid UserInsertDTO userInsertDTO);

    UserDTO findById(Long id);

    @Transactional
    UserDTO update(Long id, @Valid UserUpdateDTO userInsertDTO);

    User authenticated();

    UserDTO getMe();
}
