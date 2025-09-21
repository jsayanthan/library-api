package com.collabera.libraryapi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book extends Auditable {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_isbn", referencedColumnName = "isbn", nullable = false)
    private BookCatalog catalog;

    @Column(nullable = false)
    @Builder.Default
    private boolean borrowed = false;

    @Version
    @Column(nullable = false)
    private long version;
}
