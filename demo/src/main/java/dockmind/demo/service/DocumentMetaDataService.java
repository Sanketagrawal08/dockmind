package dockmind.demo.service;

import dockmind.demo.dto.DocumentResponseDto;
import dockmind.demo.entity.DocumentMetadata;
import dockmind.demo.entity.DocumentStatus;
import dockmind.demo.repository.DocumentMetaDataRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ai.document.Document;

import javax.print.Doc;
import java.util.List;
@RequiredArgsConstructor
@Service
public class DocumentMetaDataService {

    private final JdbcTemplate jdbcTemplate;
    private final DocumentMetaDataRepo documentMetaDataRepo;
    private final DocumentParserService parserService;
    private final DocumentIngestionService ingestionService;



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
}
