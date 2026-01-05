package com.seatwise.user_service.model;

import com.seatwise.common.enums.EFileSizeType;
import com.seatwise.common.enums.EFileStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
@Data
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class File extends BaseEntity {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @Transient
    private String url;

    @Column(name = "size")
    private int size;

    @Column(name = "size_type")
    @Enumerated(EnumType.STRING)
    private EFileSizeType sizeType;

    @Column(name = "type")
    private String type;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EFileStatus status;
    // Soft delete column
    @Column(name = "deleted_at")
    private Instant deletedAt;
//TODO : build url well in   response DTO
}
