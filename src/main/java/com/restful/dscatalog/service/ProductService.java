package com.restful.dscatalog.service;

import com.restful.dscatalog.dto.product.DadosCadastroProduto;
import com.restful.dscatalog.dto.product.DadosCadastroProdutoPorNome;
import com.restful.dscatalog.dto.product.DadosDetalhamentoProduto;
import com.restful.dscatalog.entity.Product;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    @Transactional
    Product create(@Valid DadosCadastroProduto dto);

    @Transactional
    Product createByCategoryNames(DadosCadastroProdutoPorNome dto);

    Product findById(Long id);

    Page<DadosDetalhamentoProduto> listar(Pageable paginacao);


    List<DadosDetalhamentoProduto> listarWithoutPagination();

    @Transactional
    @Valid
    DadosDetalhamentoProduto update(Long id, @Valid DadosCadastroProduto dto);

    @Transactional
    DadosDetalhamentoProduto delete(Long id);
}
