package dockmind.demo.service;

import dockmind.demo.dto.DocumentResponseDto;
import dockmind.demo.entity.DocumentMetadata;
import dockmind.demo.entity.DocumentStatus;
import dockmind.demo.entity.User;
import dockmind.demo.exception.ResourceNotFound;
import dockmind.demo.repository.DocumentMetaDataRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@RequiredArgsConstructor
@Service
public class DocumentMetaDataService {

    private final JdbcTemplate jdbcTemplate;
    private final DocumentMetaDataRepo documentMetaDataRepo;
    private final DocumentParserService parserService;
    private final DocumentIngestionService ingestionService;
    private final ModelMapper modelMapper;


    private static Logger logger = LoggerFactory.getLogger(DocumentMetaDataService.class);

    public DocumentResponseDto uploadAndProcess(MultipartFile file) {

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octat-stream";

        DocumentMetadata documentMetadata =
                DocumentMetadata.builder()
                        .filename(fileName)
                        .contentType(contentType)
                        .status(DocumentStatus.UPLOADING)
                        .fileSize(file.getSize())
                        .build();


        DocumentMetadata savedDocumentMetadata =  documentMetaDataRepo.save(documentMetadata);
        //now parse the file
        List<Document> parsedDocs = parserService.parse(file);

        // ingestion service;
        int chunksCreated = ingestionService.ingest(savedDocumentMetadata, parsedDocs);



        return DocumentResponseDto.builder() .id(savedDocumentMetadata.getId())
                .fileName(savedDocumentMetadata.getFilename())
                .fileSize(savedDocumentMetadata.getFileSize())
                .chunksCreated(chunksCreated)
                .status(savedDocumentMetadata.getStatus())
                .message("Document successfully processed and indexed.")
                .build();
    }

    public List<DocumentResponseDto> uploadMultipleDocuments(List<MultipartFile> files) {
        List<DocumentResponseDto> responseDtos = new ArrayList<>();

        for (MultipartFile file : files) {
            DocumentResponseDto result = this.uploadAndProcess(file);
            responseDtos.add(result);
        }

        return responseDtos;

    }
    public List<DocumentResponseDto> getAllDocuments() {
        List<DocumentMetadata> allDocuments = documentMetaDataRepo.findAll();
        return allDocuments.stream()
                .map((Function<? super DocumentMetadata, ?>) documentMetadata -> modelMapper.map(documentMetadata, DocumentMetadata.class))
                .toList();

    }

    public List<DocumentResponseDto> getAllDocumentsByUser(User user) {
        List<DocumentMetadata> documents = documentMetaDataRepo.findByUserOrderByCreatedAtDesc(user);
        return documents.stream()
                .map((Function<? super DocumentMetadata, ?>) documentMetadata -> modelMapper.map(documentMetadata, DocumentResponseDto.class))
                .toList();
    }

    public DocumentResponseDto getDocumentById(UUID id) {
        DocumentMetadata documentMetadata = documentMetaDataRepo.findById(id).orElseThrow(() -> new RuntimeException ("Document with given id not found !!"));
        return modelMapper.map(documentMetadata, DocumentResponseDto.class);

    }
    @Transactional
    public void deleteDocument(UUID id) {
        DocumentMetadata documentMetadata = documentMetaDataRepo.findById(id).orElseThrow(() -> new ResourceNotFound("Document with given id not found !!"));


        documentMetaDataRepo.delete(documentMetadata);
        //delete the vector entries
        try {
            String deleteVectorsSql = "DELETE FROM vector_store WHERE metadata->>'documentId' = ?";
            int deletedCount = jdbcTemplate.update(deleteVectorsSql, id.toString());
            logger.info("Deleted {} vector chunks for document id {} ", deletedCount, id);

        } catch (Exception e) {
            logger.warn("cloud not delete vectors from vector store directly: {}", e.getMessage());
        }

    }
}
