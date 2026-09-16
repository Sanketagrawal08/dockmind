package dockmind.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("/api/v1/documents")
public class DocumentController {

            @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
            @Operation(summary = "upload files")
            public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file){

                return ResponseEntity.ok("aagayi file..");
            }

}