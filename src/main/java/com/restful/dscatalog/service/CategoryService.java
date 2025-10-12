package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.categoria.DadosCadastroCategoria;
import com.restful.dscatalog.dto.categoria.DadosDetalhamentoCategoria;
import com.restful.dscatalog.entity.Category;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface CategoryService {

    @Transactional
    Category create(DadosCadastroCategoria category);

    Category findById(Long id);

    Page<DadosDetalhamentoCategoria> listar(Pageable paginacao);

    @Transactional
    @Valid DadosDetalhamentoCategoria update(Long id, @Valid DadosCadastroCategoria dto);

    @Transactional
    DadosDetalhamentoCategoria delete(Long id);
}
