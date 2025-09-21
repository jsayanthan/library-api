package com.collabera.libraryapi.core.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class Paging {
    private Paging() {}

    public static String echoSort(Sort sort) {
        if (sort == null || sort.isUnsorted()) return "";
        return sort.stream()
                .map(o -> o.getProperty() + "," + o.getDirection().name().toLowerCase())
                .reduce((a, b) -> a + ";" + b).orElse("");
    }

    public static <T> PageResponse<T> toResponse(Page<T> page, Pageable pageable) {
        return PageResponse.of(page, echoSort(pageable.getSort()));
    }
}
