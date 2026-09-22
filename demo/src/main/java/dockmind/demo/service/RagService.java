package dockmind.demo.service;

import dockmind.demo.config.AppProperties;
import dockmind.demo.dto.CitationDto;
import dockmind.demo.dto.SearchRequestDto;
import dockmind.demo.dto.SearchResultDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RagService {
    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final AppProperties appProperties;
    private final ChatClient chatClient;


    public SearchResultDto searchSimilarChunk(SearchRequestDto searchRequestDto){

        List<Document> matchedDocs = retriveRelevantDocuments(
                searchRequestDto.getQuery(),
                searchRequestDto.getDocumentId(),
                searchRequestDto.getTopK(),
                searchRequestDto.getSimilaritySearch()
        );

        List<CitationDto> citations = new ArrayList<>();

        for (Document doc : matchedDocs) {
            CitationDto citation = mapToCitation(doc);
            citations.add(citation);
        }

        return SearchResultDto.builder()
                .query(searchRequestDto.getQuery())
                .totalMatches(citations.size())
                .matches(citations)
                .build();
    }

    private CitationDto mapToCitation(Document document) {

        Map<String, Object> meta = document.getMetadata();
        UUID docId = null;
        docId = UUID.fromString(meta.get("documentId").toString());

        Integer chunkIndex = null;
        if (meta.get("chunkIndex") instanceof Number n) {
            chunkIndex = n.intValue();
        }

        Integer pageNumber = null;
        if (meta.get("pageNumber") instanceof Number n) {
            pageNumber = n.intValue();
        } else if (meta.get("page_number") instanceof Number n) {
            pageNumber = n.intValue();
        }

        Double score = null;
        if (meta.get("distance") instanceof Number n) {
            score = 1.0 - n.doubleValue();
        }
        return CitationDto.builder().documentId(docId).fileName((String) meta.getOrDefault("fileName", "Unknown")).chunkIndex(chunkIndex).pageNumber(pageNumber).snippet(document.getText()).similarityScore(score).metadata(meta).build();
    }


    private List<Document> retriveRelevantDocuments(String query, UUID documentId, Integer topK, Double similaritySearch) {

        int effectiveTopK = appProperties.getRag().getTopK();
        double effectiveSimilarity=  appProperties.getRag().getSimilarityThreshold();

        //searchrequestbuilder
        SearchRequest.Builder searchRequestBuilder = SearchRequest.builder().
                query(query).
                topK(effectiveTopK).
                similarityThreshold(effectiveSimilarity);


        //filterexpression
        if (documentId != null) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();

            searchRequestBuilder.filterExpression(b.eq("documentId", documentId.toString()).build());
        }

        try {
            List<Document> documents = vectorStore.similaritySearch(searchRequestBuilder.build());
            log.info("Retrieved {} chunks for query: '{}' (scoped docId: {})", documents.size(), query, documentId);
            return documents;
        } catch (Exception e) {
            log.error("Similarity search failed for query: '{}'", query, e);
            return Collections.emptyList();
        }

    }

}
