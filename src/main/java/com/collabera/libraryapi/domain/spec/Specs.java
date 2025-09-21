package com.collabera.libraryapi.domain.spec;

import org.springframework.data.jpa.domain.Specification;

public final class Specs {
    private Specs() {}
    public static <T> Specification<T> truth() { return Specification.where(null); }
}
