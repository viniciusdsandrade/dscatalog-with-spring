package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.DadosCadastroCategoria;
import com.restful.dscatalog.entity.Category;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryService {

    @Transactional
    Category create(DadosCadastroCategoria category);

    Category buscarPorId(Long id);
}
