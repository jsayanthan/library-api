package com.collabera.libraryapi.mapper;

import com.collabera.libraryapi.domain.dto.borrower.BorrowerCreateRequest;
import com.collabera.libraryapi.domain.entity.Borrower;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowerMapper {
    @Mapping(target = "id", ignore = true)
    Borrower toEntity(BorrowerCreateRequest request);
    BorrowerResponse toResponse(Borrower borrower);
}
