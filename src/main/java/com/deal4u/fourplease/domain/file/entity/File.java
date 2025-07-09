package com.deal4u.fourplease.domain.file.entity;

import com.deal4u.fourplease.domain.BaseDateEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    @Column(length = 500)
    private String filePath;
    private Long fileSize;
    @Column(length = 100)
    private String contentType;
    @Column(length = 50)
    private String status;
}
