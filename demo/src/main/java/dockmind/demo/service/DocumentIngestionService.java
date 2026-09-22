package dockmind.demo.service;

import dockmind.demo.config.AppProperties;
import dockmind.demo.entity.DocumentMetadata;
import dockmind.demo.entity.DocumentStatus;
import dockmind.demo.exception.DocumentProcessingException;
import dockmind.demo.repository.DocumentMetaDataRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    Logger log=  LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;
    private final DocumentMetaDataRepo documentMetaDataRepo;
    private final AppProperties appProperties;



    // list of document jo parse hoke aayenge unko chunks me convert krengi and mapping kregi ki konsa chunk kis documentko belong karta he
    public int ingest(DocumentMetadata savedDocumentMetadata, List<Document> parsedDocs) {

        try{
            savedDocumentMetadata.setStatus(DocumentStatus.PROCESSING);
            savedDocumentMetadata.setTotalPages(parsedDocs.size());

            //tokentextsplitter -> splits text into chunks based on token count (text chunking1)
            TokenTextSplitter tokenTextSplitter= TokenTextSplitter.builder()
                    .withChunkSize(appProperties.getRag().getChunkSize())
                    .withMinChunkSizeChars(appProperties.getRag().getMinChunkSizeChars())
                    .withMinChunkLengthToEmbed(appProperties.getRag().getMinChunkLengthToEmbed())
                    .withMaxNumChunks(appProperties.getRag().getMaxNumChunks())
                    .withKeepSeparator(true)
                    .build();

            List<Document> chunks = tokenTextSplitter.apply(parsedDocs);
            if(chunks.isEmpty()){
                savedDocumentMetadata.setStatus(DocumentStatus.FAILED);
                savedDocumentMetadata.setErrorMessage("Document appears to be empty or unscannable");
                documentMetaDataRepo.save(savedDocumentMetadata);
                return 0;
            }


            // metadata enrichment on each chunks
            List<Document> enrichedChunk = new ArrayList<>();
            for (int i = 0;i < chunks.size();i++) {
                Document chunk = chunks.get(i);
                Map<String , Object> enrichedMetadata = new HashMap<>(chunk.getMetadata());
                enrichedMetadata.put("documentId", savedDocumentMetadata.getId().toString());
                enrichedMetadata.put("fileName", savedDocumentMetadata.getFilename());
                enrichedMetadata.put("contentType", savedDocumentMetadata.getContentType());
                enrichedMetadata.put("chunkIndex", i);
                Object pageNumber = chunk.getMetadata().get("page_number");
                if (pageNumber == null) {
                    pageNumber = chunk.getMetadata().get("pageNumber");

                }

                if (pageNumber != null) {
                    enrichedMetadata.put("pageNumber", pageNumber);
                }
                Document enrichedDoc = new Document(chunk.getText(), enrichedMetadata);
                enrichedChunk.add(enrichedDoc);

               // 3. write chunks and embedding to pg vector
                log.info("Writing {} vector chunks to PgVectorStore for document: {}", enrichedChunk.size(), savedDocumentMetadata.getFilename());
                vectorStore.add(enrichedChunk);

                //4. update doc status;
                savedDocumentMetadata.setStatus(DocumentStatus.INDEXED);
                savedDocumentMetadata.setTotalChunks(enrichedChunk.size());
                savedDocumentMetadata.setErrorMessage(null);
                documentMetaDataRepo.save(savedDocumentMetadata);
                log.info("Successfully indexed document [id={}, name={}, chunks={}]", savedDocumentMetadata.getId(), savedDocumentMetadata.getFilename(), enrichedChunk.size());

                return enrichedChunk.size();
            }

        }catch (Exception ex){
                log.error("Failed to ingest document into vector store: {}", savedDocumentMetadata.getFilename(), ex);
                savedDocumentMetadata.setStatus(DocumentStatus.FAILED);
                savedDocumentMetadata.setErrorMessage(ex.getMessage());
                documentMetaDataRepo.save(savedDocumentMetadata);
                throw new DocumentProcessingException("Failed to index document: " + ex.getMessage(), ex);
            }



        return 1;
    }
}