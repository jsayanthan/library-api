package com.collabera.libraryapi.service;

import com.collabera.libraryapi.domain.dto.borrower.BorrowerCreateRequest;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerResponse;
import com.collabera.libraryapi.domain.entity.Borrower;
import com.collabera.libraryapi.domain.repository.BorrowerRepository;
import com.collabera.libraryapi.mapper.BorrowerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BorrowerServiceTest {

    @Mock
    private BorrowerRepository repo;
    @Mock
    private BorrowerMapper mapper;
    @InjectMocks
    private BorrowerService service;

    @Test
    void registerPersistsAndMaps_withNameSayan() {
        BorrowerCreateRequest req = new BorrowerCreateRequest("Sayan", "sayan@example.com");
        Borrower entity = Borrower.builder().id(UUID.randomUUID()).name(req.name()).email(req.email()).build();
        BorrowerResponse response = new BorrowerResponse(entity.getId(), entity.getName(), entity.getEmail());
        given(mapper.toEntity(req)).willReturn(entity);
        given(repo.save(entity)).willReturn(entity);
        given(mapper.toResponse(entity)).willReturn(response);

        BorrowerResponse out = service.register(req);
        assertThat(out.name()).isEqualTo("Sayan");
        verify(repo).save(any(Borrower.class));
    }
}
