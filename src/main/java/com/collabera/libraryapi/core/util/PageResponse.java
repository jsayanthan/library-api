package com.collabera.libraryapi.core.util;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content, int page, int size, long totalElements, int totalPages, String sort
) {
    public static <T> PageResponse<T> of(Page<T> page, String sortEcho) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), sortEcho == null ? "" : sortEcho);
    }
}
