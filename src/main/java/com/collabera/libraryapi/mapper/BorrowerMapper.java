package com.collabera.libraryapi.mapper;

import com.collabera.libraryapi.domain.dto.borrower.BorrowerCreateRequest;
import com.collabera.libraryapi.domain.entity.Borrower;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BorrowerMapper {
    Borrower toEntity(BorrowerCreateRequest request);
    BorrowerResponse toResponse(Borrower borrower);
}
