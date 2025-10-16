package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.user.UserDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDTO> findAllPaged(Pageable pageable);

    @Transactional
    UserDTO insert(UserDTO dto);

    UserDTO findById(Long id);
}
