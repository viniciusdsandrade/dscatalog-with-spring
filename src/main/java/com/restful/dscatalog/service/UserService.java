package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.user.UserDTO;
import com.restful.dscatalog.dto.user.UserInsertDTO;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserDTO> findAllPaged(Pageable pageable);

    @Transactional
    UserDTO insert(UserInsertDTO userInsertDTO);

    UserDTO findById(Long id);
}
