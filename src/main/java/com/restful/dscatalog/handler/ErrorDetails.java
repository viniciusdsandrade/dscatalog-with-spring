package com.restful.dscatalog.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;

/**
 * Representa os detalhes de um erro ocorrido durante a validação ou processamento da requisição.
 * <p>
 * Esta classe é utilizada para fornecer informações estruturadas sobre os erros de validação de campos,
 * como o nome do campo que causou o erro, uma mensagem detalhada e o código do erro.
 * Ela também registra o timestamp de quando o erro ocorreu.
 * <p>
 * A classe utiliza o padrão record do Java, tornando-a imutável e adequada para transportar dados de erro.
 *
 * @param timestamp O momento exato em que o erro ocorreu.
 * @param field     O campo do objeto que apresentou o erro.
 * @param details   Detalhes sobre o erro, geralmente a mensagem padrão de erro.
 * @param error     O código do erro que descreve a natureza do problema (ex: "BAD_REQUEST").
 * @see FieldError
 */
@Schema(description = "Detalhes do erro")
public record ErrorDetails(
        LocalDateTime timestamp,
        String field,
        String details,
        String error
) {

    /**
     * Construtor que recebe um {@link FieldError} e mapeia seus valores para os campos da classe.
     * <p>
     * Este construtor é usado para criar um objeto de detalhes de erro com base em um erro de validação
     * de campo fornecido pelo Spring Framework.
     *
     * @param erro O objeto {@link FieldError} contendo as informações do erro de validação.
     */
    public ErrorDetails(FieldError erro) {
        this(
                now(), // Registra o timestamp atual
                erro.getField(), // O campo que causou o erro
                erro.getDefaultMessage(), // A mensagem padrão do erro
                erro.getCode() // Código de erro
        );
    }
}