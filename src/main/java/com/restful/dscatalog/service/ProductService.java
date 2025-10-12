package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.DadosCadastroProduto;
import com.restful.dscatalog.dto.DadosDetalhamentoProduto;
import com.restful.dscatalog.entity.Product;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {


    @Transactional
    Product create(@Valid DadosCadastroProduto dto);

    Product findById(Long id);

    Page<DadosDetalhamentoProduto> listar(Pageable paginacao);

    @Transactional
    @Valid
    DadosDetalhamentoProduto update(Long id, @Valid DadosCadastroProduto dto);

    @Transactional
    DadosDetalhamentoProduto delete(Long id);
}
