package com.collabera.libraryapi.service;

import com.collabera.libraryapi.domain.repository.BorrowerRepository;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerCreateRequest;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerResponse;
import com.collabera.libraryapi.mapper.BorrowerMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BorrowerService {
    private final BorrowerRepository repo;
    private final BorrowerMapper mapper;

    @Transactional
    public BorrowerResponse register(BorrowerCreateRequest request) {
        var saved = repo.save(mapper.toEntity(request));
        return mapper.toResponse(saved);
    }
}
