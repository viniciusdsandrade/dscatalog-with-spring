package com.restful.dscatalog.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.data.domain.Page;


import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpHeaders.LINK;

@RestControllerAdvice
public class PaginationHeadersAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(
            @NotNull MethodParameter returnType,
            @NotNull Class converterType
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NotNull MethodParameter returnType,
            @NotNull MediaType selectedContentType,
            @NotNull Class selectedConverterType,
            @NotNull ServerHttpRequest request,
            @NotNull ServerHttpResponse response
    ) {
        if (!(body instanceof Page<?> page)) return body;

        HttpHeaders headers = response.getHeaders();
        headers.set("X-Page-Number", String.valueOf(page.getNumber()));
        headers.set("X-Page-Size", String.valueOf(page.getSize()));
        headers.set("X-Total-Count", String.valueOf(page.getTotalElements()));

        if (page.getTotalPages() > 0) {
            String link = buildLinkHeader(request, page);
            if (!link.isEmpty()) {
                headers.set(LINK, link);
            }
        }

        return page;
    }

    private static String buildLinkHeader(ServerHttpRequest request, Page<?> page) {
        UriComponentsBuilder base = UriComponentsBuilder.fromUri(request.getURI())
                .replaceQueryParam("size", page.getSize());

        StringBuilder sb = new StringBuilder();

        appendRel(sb, pageUrl(base, 0), "first");
        appendRel(sb, pageUrl(base, Math.max(page.getTotalPages() - 1, 0)), "last");

        if (page.hasPrevious()) appendRel(sb, pageUrl(base, page.getNumber() - 1), "prev");
        if (page.hasNext()) appendRel(sb, pageUrl(base, page.getNumber() + 1), "next");

        return sb.toString();
    }

    private static String pageUrl(UriComponentsBuilder base, int pageIndex) {
        return base.cloneBuilder()
                .replaceQueryParam("page", pageIndex)
                .build(true)
                .toUriString();
    }

    private static void appendRel(StringBuilder sb, String url, String rel) {
        if (url == null || url.isEmpty()) return;
        if (!sb.isEmpty()) sb.append(", ");
        sb.append("<").append(url).append(">; rel=\"").append(rel).append("\"");
    }
}

