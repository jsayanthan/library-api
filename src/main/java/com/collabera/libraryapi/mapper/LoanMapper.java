package com.collabera.libraryapi.mapper;

import com.collabera.libraryapi.domain.entity.Loan;
import com.collabera.libraryapi.domain.dto.loan.LoanResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {BookMapper.class, BorrowerMapper.class})
public interface LoanMapper {
    LoanResponse toResponse(Loan loan);
}
