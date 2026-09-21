package dockmind.demo.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class DocumentParserService {

    public List<Document> parse(MultipartFile file) {

        String fileName = file.getOriginalFilename() != null
                ? file.getOriginalFilename()
                : "document";

        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase()
                : "";

        try {

            Resource resource = new ByteArrayResource(file.getBytes()) {

                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            if (fileName.toLowerCase().endsWith(".pdf")
                    || contentType.contains("pdf")) {

                return parsePdf(resource);

            } else {

                return parseGenericFile(resource);
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Could not read uploaded file: " + fileName, e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to parse document: " + fileName, e
            );
        }
    }

    private List<Document> parsePdf(Resource resource) {

        PdfDocumentReaderConfig config =
                PdfDocumentReaderConfig.builder()
                        .withPageBottomMargin(0)
                        .withPageTopMargin(0)
                        .build();

        PagePdfDocumentReader reader =
                new PagePdfDocumentReader(resource, config);

        return reader.read();
    }

    private List<Document> parseGenericFile(Resource resource) {

        TikaDocumentReader reader =
                new TikaDocumentReader(resource);

        return reader.read();
    }
}