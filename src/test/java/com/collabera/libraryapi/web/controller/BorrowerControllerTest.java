package com.collabera.libraryapi.web.controller;

import com.collabera.libraryapi.domain.dto.borrower.BorrowerCreateRequest;
import com.collabera.libraryapi.domain.dto.borrower.BorrowerResponse;
import com.collabera.libraryapi.domain.repository.BorrowerRepository;
import com.collabera.libraryapi.service.BorrowerService;
import com.collabera.libraryapi.web.exception.ApiError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BorrowerControllerTest extends BaseMockMvcTest {
    @MockBean
    private BorrowerService service;
    @MockBean
    private BorrowerRepository borrowerRepository; // for uniqueness validator

    private static Stream<Arguments> invalidPayloads() {
        return Stream.of(
                Arguments.of(new BorrowerCreateRequest("", "sayan@example.com"), "name: Name cannot be blank"),
                Arguments.of(new BorrowerCreateRequest("Sayan", ""), "email: Email cannot be blank"),
                Arguments.of(new BorrowerCreateRequest("Sayan", "not-an-email"), "email: Invalid email format")
        );
    }

    @Test
    void register_201() throws Exception {
        BorrowerCreateRequest req = new BorrowerCreateRequest("Sayan", "sayan@example.com");
        BorrowerResponse resp = new BorrowerResponse(UUID.randomUUID(), req.name(), req.email());
        given(service.register(any())).willReturn(resp);
        MvcResult result = mockMvc.perform(post("/api/v1/borrowers").contentType(json()).content(toJson(req)))
                .andExpect(status().isCreated())
                .andReturn();
        BorrowerResponse actual = fromJson(result.getResponse().getContentAsString(), BorrowerResponse.class);
        assertThat(actual).usingRecursiveComparison().isEqualTo(resp);
    }

    @ParameterizedTest
    @MethodSource("invalidPayloads")
    void register_400_validation(BorrowerCreateRequest req, String expectedPart) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/borrowers").contentType(json()).content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.message()).contains(expectedPart);
    }

    @Test
    void register_400_emailNotUnique() throws Exception {
        // We do NOT stub service.register because validation should fail before service layer invoked
        BorrowerCreateRequest req = new BorrowerCreateRequest("Sayan", "duplicate@example.com");
        // Simulate existing borrower with same email
        given(borrowerRepository.existsByEmailIgnoreCase("duplicate@example.com")).willReturn(true);
        MvcResult result = mockMvc.perform(post("/api/v1/borrowers")
                        .contentType(json())
                        .content(toJson(req)))
                .andExpect(status().isBadRequest())
                .andReturn();
        ApiError error = fromJson(result.getResponse().getContentAsString(), ApiError.class);
        assertThat(error.message()).contains("email: Email must be unique");
        // Ensure service not called
        verify(service, never()).register(any());
    }
}
