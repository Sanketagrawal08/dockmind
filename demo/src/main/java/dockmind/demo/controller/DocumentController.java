package dockmind.demo.controller;

import dockmind.demo.dto.ApiResponse;
import dockmind.demo.dto.DocumentResponseDto;
import dockmind.demo.service.DocumentMetaDataService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.CustomSQLErrorCodesTranslation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final  DocumentMetaDataService documentService;

            @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
            @Operation(summary = "upload files")
            public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file){
                DocumentResponseDto documentResponseDto = this.documentService.uploadAndProcess(file);
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.<DocumentResponseDto>builder()
                                .success(true)
                                .data(documentResponseDto).localDateTime(LocalDateTime.now())
                                .message("Documents uploaded and indexed successfully").build().getMessage());
            }

}