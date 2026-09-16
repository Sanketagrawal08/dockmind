package dockmind.demo.entity;


import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

    @Entity
    @Table(name = "document_metadata")
    @Builder
    public class DocumentMetadata {
        public UUID getId() {
            return id;
        }
        public DocumentMetadata(){

        }
        public DocumentMetadata(UUID id, String filename, String contentType, Long fileSize, Integer totalPages, Integer totalChunks, DocumentStatus status, String errorMessage, LocalDateTime createdAt, LocalDateTime updatedAt, User user) {
            this.id = id;
            this.filename = filename;
            this.contentType = contentType;
            this.fileSize = fileSize;
            this.totalPages = totalPages;
            this.totalChunks = totalChunks;
            this.status = status;
            this.errorMessage = errorMessage;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.user = user;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public Integer getTotalChunks() {
            return totalChunks;
        }

        public void setTotalChunks(Integer totalChunks) {
            this.totalChunks = totalChunks;
        }

        public DocumentStatus getStatus() {
            return status;
        }

        public void setStatus(DocumentStatus status) {
            this.status = status;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(unique = false)
        private String filename;

        @Column(nullable = false)
        private String contentType;
        private Long fileSize;
        private Integer totalPages;
        private Integer totalChunks;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private DocumentStatus status;

        @Column(length = 1000)
        private String errorMessage;
        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;


        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

    }