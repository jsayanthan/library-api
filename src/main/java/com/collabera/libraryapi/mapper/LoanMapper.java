package com.collabera.libraryapi.mapper;

import com.collabera.libraryapi.domain.dto.loan.LoanCreateRequest;
import com.collabera.libraryapi.domain.entity.Loan;
import com.collabera.libraryapi.domain.dto.loan.LoanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {BookMapper.class, BorrowerMapper.class}
)
public interface LoanMapper {
    Loan toEntity(LoanCreateRequest request);
    LoanResponse toResponse(Loan loan);
}
